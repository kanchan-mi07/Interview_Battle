package com.interviewarena.service;

import com.interviewarena.dto.*;
import com.interviewarena.dto.BattleStateResponse.OpponentInfo;
import com.interviewarena.dto.BattleStateResponse.ResultInfo;
import com.interviewarena.entity.*;
import com.interviewarena.exception.ApiException;
import com.interviewarena.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BattleService {

    static final int QUESTIONS_PER_BATTLE = 10;
    static final int SECONDS_PER_QUESTION = 20;
    static final int GRACE_SECONDS = 2;        // allowance for network latency
    static final int START_DELAY_SECONDS = 8;  // countdown so both players are ready
    static final int POINTS_PER_CORRECT = 100;

    // No 0/O or 1/I so codes are easy to read out loud
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final BattleRepository battleRepository;
    private final BattleQuestionRepository battleQuestionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    /** Where a player is: how many answered, which question is next, and when it started. */
    private record Progress(int answered, BattleQuestion current, LocalDateTime questionStart) {}

    // ---------------------------------------------------------------- create

    @Transactional
    public BattleStateResponse create(User me) {
        List<Question> questions = questionRepository.findRandom(QUESTIONS_PER_BATTLE);
        if (questions.size() < QUESTIONS_PER_BATTLE) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Not enough questions to start a battle.");
        }

        Battle battle = battleRepository.save(Battle.builder()
                .battleCode(generateCode())
                .creator(userRepository.getReferenceById(me.getId()))
                .build());

        List<BattleQuestion> links = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            links.add(BattleQuestion.builder()
                    .battle(battle).question(questions.get(i)).questionOrder(i + 1).build());
        }
        battleQuestionRepository.saveAll(links);

        return buildState(battle, me.getId(), LocalDateTime.now());
    }

    // ------------------------------------------------------------------ join

    @Transactional
    public BattleStateResponse join(User me, String code) {
        LocalDateTime now = LocalDateTime.now();

        // Row lock: a second simultaneous join waits here, then sees the opponent already set.
        Battle battle = battleRepository.findByBattleCodeForUpdate(code.trim().toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Battle not found."));

        if (battle.getCreator().getId().equals(me.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot join your own battle.");
        }
        if (battle.getOpponent() != null) {
            if (battle.getOpponent().getId().equals(me.getId())) {
                return buildState(battle, me.getId(), now); // double-click or refresh: same result
            }
            throw new ApiException(HttpStatus.CONFLICT, "Battle is full.");
        }
        if (battle.getStatus() != BattleStatus.WAITING) {
            throw new ApiException(HttpStatus.CONFLICT, "Battle has already started.");
        }

        battle.setOpponent(userRepository.getReferenceById(me.getId()));
        battle.setStatus(BattleStatus.READY);
        battle.setStartedAt(now.plusSeconds(START_DELAY_SECONDS));

        return buildState(battle, me.getId(), now);
    }

    // ------------------------------------------------------------ get / poll

    @Transactional
    public BattleStateResponse getBattle(Long battleId, User me) {
        LocalDateTime now = LocalDateTime.now();
        Battle battle = loadParticipantBattle(battleId, me);
        refresh(battle, now);
        return buildState(battle, me.getId(), now);
    }

    // ---------------------------------------------------------------- answer

    // noRollbackFor: when we reject a late answer we still keep the "unanswered" rows we recorded.
    @Transactional(noRollbackFor = ApiException.class)
    public AnswerResponse submitAnswer(Long battleId, User me, AnswerRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Battle battle = loadParticipantBattle(battleId, me);

        if (battle.getStatus() == BattleStatus.COMPLETED) {
            throw new ApiException(HttpStatus.CONFLICT, "Battle is already completed.");
        }
        if (battle.getOpponent() == null) {
            throw new ApiException(HttpStatus.CONFLICT, "Waiting for an opponent to join.");
        }
        if (now.isBefore(battle.getStartedAt())) {
            throw new ApiException(HttpStatus.CONFLICT, "The battle has not started yet.");
        }
        if (battle.getStatus() == BattleStatus.READY) {
            battle.setStatus(BattleStatus.IN_PROGRESS);
        }

        boolean iAmCreator = battle.getCreator().getId().equals(me.getId());
        User player = iAmCreator ? battle.getCreator() : battle.getOpponent();
        List<BattleQuestion> bqs = battleQuestionRepository.findByBattleIdOrderByQuestionOrder(battle.getId());

        BattleQuestion target = bqs.stream()
                .filter(bq -> bq.getQuestion().getId().equals(request.questionId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found in this battle."));

        if (answerRepository.existsByBattleIdAndUserIdAndQuestionId(
                battle.getId(), player.getId(), target.getQuestion().getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Answer already submitted.");
        }

        Progress progress = progress(battle, player.getId(), bqs);
        if (!progress.current().getId().equals(target.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Answer the current question first.");
        }

        // The server decides whether the answer is on time
        LocalDateTime deadline = progress.questionStart().plusSeconds(SECONDS_PER_QUESTION + GRACE_SECONDS);
        if (now.isAfter(deadline)) {
            expireOverdue(battle, battle.getCreator(), bqs, now);
            expireOverdue(battle, battle.getOpponent(), bqs, now);
            tryComplete(battle, bqs, now);
            throw new ApiException(HttpStatus.CONFLICT, "Time has expired.");
        }

        Question question = target.getQuestion();
        boolean correct = request.selectedOption() != null
                && request.selectedOption() == question.getCorrectOption();
        saveAnswer(battle, player, question, request.selectedOption(), correct, now);

        if (correct) { // the score is calculated here and only here
            if (iAmCreator) battle.setCreatorScore(battle.getCreatorScore() + POINTS_PER_CORRECT);
            else battle.setOpponentScore(battle.getOpponentScore() + POINTS_PER_CORRECT);
        }

        User other = iAmCreator ? battle.getOpponent() : battle.getCreator();
        expireOverdue(battle, other, bqs, now);
        tryComplete(battle, bqs, now);

        return new AnswerResponse(correct, buildState(battle, me.getId(), now));
    }

    // ---------------------------------------------------------------- finish

    @Transactional
    public BattleStateResponse finish(Long battleId, User me) {
        LocalDateTime now = LocalDateTime.now();
        Battle battle = loadParticipantBattle(battleId, me);

        if (battle.getOpponent() == null || now.isBefore(battle.getStartedAt())) {
            throw new ApiException(HttpStatus.CONFLICT, "The battle has not started yet.");
        }
        refresh(battle, now);

        List<BattleQuestion> bqs = battleQuestionRepository.findByBattleIdOrderByQuestionOrder(battle.getId());
        if (progress(battle, me.getId(), bqs).current() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You have not answered all questions yet.");
        }
        // Completed if the opponent is also done. Otherwise the state says we are still waiting.
        return buildState(battle, me.getId(), now);
    }

    // --------------------------------------------------------------- helpers

    private Battle loadParticipantBattle(Long battleId, User me) {
        Battle battle = battleRepository.findByIdForUpdate(battleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Battle not found."));
        boolean participant = battle.getCreator().getId().equals(me.getId())
                || (battle.getOpponent() != null && battle.getOpponent().getId().equals(me.getId()));
        if (!participant) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not part of this battle.");
        }
        return battle;
    }

    /** Status sync + expire overdue questions for both players + complete if both are done. */
    private void refresh(Battle battle, LocalDateTime now) {
        if (battle.getOpponent() == null
                || battle.getStatus() == BattleStatus.COMPLETED
                || now.isBefore(battle.getStartedAt())) {
            return;
        }
        if (battle.getStatus() == BattleStatus.READY) {
            battle.setStatus(BattleStatus.IN_PROGRESS);
        }
        List<BattleQuestion> bqs = battleQuestionRepository.findByBattleIdOrderByQuestionOrder(battle.getId());
        expireOverdue(battle, battle.getCreator(), bqs, now);
        expireOverdue(battle, battle.getOpponent(), bqs, now);
        tryComplete(battle, bqs, now);
    }

    private Progress progress(Battle battle, Long userId, List<BattleQuestion> bqs) {
        Map<Long, Answer> byQuestion = answerRepository.findByBattleIdAndUserId(battle.getId(), userId)
                .stream().collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity()));

        LocalDateTime start = battle.getStartedAt();
        for (int i = 0; i < bqs.size(); i++) {
            Answer answer = byQuestion.get(bqs.get(i).getQuestion().getId());
            if (answer == null) {
                return new Progress(i, bqs.get(i), start);
            }
            start = answer.getAnsweredAt(); // the next question's clock starts here
        }
        return new Progress(bqs.size(), null, null);
    }

    /** Records "unanswered" (0 points) for every question whose time ran out. */
    private void expireOverdue(Battle battle, User user, List<BattleQuestion> bqs, LocalDateTime now) {
        while (true) {
            Progress p = progress(battle, user.getId(), bqs);
            if (p.current() == null) return;

            LocalDateTime deadline = p.questionStart().plusSeconds(SECONDS_PER_QUESTION + GRACE_SECONDS);
            if (!now.isAfter(deadline)) return;

            saveAnswer(battle, user, p.current().getQuestion(), null, false,
                    p.questionStart().plusSeconds(SECONDS_PER_QUESTION));
        }
    }

    private void tryComplete(Battle battle, List<BattleQuestion> bqs, LocalDateTime now) {
        if (battle.getStatus() == BattleStatus.COMPLETED) return;
        if (progress(battle, battle.getCreator().getId(), bqs).current() != null) return;
        if (progress(battle, battle.getOpponent().getId(), bqs).current() != null) return;

        User creator = battle.getCreator();
        User opponent = battle.getOpponent();
        battle.setCreatorRatingBefore(creator.getRating());
        battle.setOpponentRatingBefore(opponent.getRating());

        int c = battle.getCreatorScore();
        int o = battle.getOpponentScore();
        BattleOutcome creatorOutcome = c > o ? BattleOutcome.WIN : c < o ? BattleOutcome.LOSS : BattleOutcome.DRAW;
        BattleOutcome opponentOutcome = c > o ? BattleOutcome.LOSS : c < o ? BattleOutcome.WIN : BattleOutcome.DRAW;

        applyOutcome(creator, creatorOutcome);
        applyOutcome(opponent, opponentOutcome);

        battle.setWinner(creatorOutcome == BattleOutcome.WIN ? creator
                : opponentOutcome == BattleOutcome.WIN ? opponent : null);
        battle.setStatus(BattleStatus.COMPLETED);
        battle.setCompletedAt(now);
    }

    private void applyOutcome(User user, BattleOutcome outcome) {
        user.setRating(user.getRating() + outcome.getRatingChange());
        user.setTotalBattles(user.getTotalBattles() + 1);
        if (outcome == BattleOutcome.WIN) user.setWins(user.getWins() + 1);
        if (outcome == BattleOutcome.LOSS) user.setLosses(user.getLosses() + 1);
    }

    private void saveAnswer(Battle battle, User user, Question question,
                            OptionChoice selected, boolean correct, LocalDateTime at) {
        answerRepository.save(Answer.builder()
                .battle(battle).user(user).question(question)
                .selectedOption(selected).correct(correct).answeredAt(at)
                .build());
    }

    private String generateCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            String code = sb.toString();
            if (!battleRepository.existsByBattleCode(code)) return code;
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate a battle code. Please try again.");
    }

    private BattleStateResponse buildState(Battle battle, Long meId, LocalDateTime now) {
        boolean iAmCreator = battle.getCreator().getId().equals(meId);
        User other = iAmCreator ? battle.getOpponent() : battle.getCreator();

        long startsIn = 0;
        int answered = 0, otherAnswered = 0, secondsRemaining = 0, currentNumber = 0;
        QuestionResponse current = null;

        if (battle.getOpponent() != null) {
            List<BattleQuestion> bqs = battleQuestionRepository.findByBattleIdOrderByQuestionOrder(battle.getId());

            long millis = Duration.between(now, battle.getStartedAt()).toMillis();
            startsIn = millis > 0 ? (millis + 999) / 1000 : 0;

            Progress mine = progress(battle, meId, bqs);
            answered = mine.answered();
            otherAnswered = progress(battle, other.getId(), bqs).answered();

            if (mine.current() != null && startsIn == 0 && battle.getStatus() != BattleStatus.COMPLETED) {
                current = QuestionResponse.from(mine.current().getQuestion()); // no correct answer in this DTO
                currentNumber = answered + 1;
                long elapsed = Duration.between(mine.questionStart(), now).getSeconds();
                secondsRemaining = (int) Math.max(0, SECONDS_PER_QUESTION - elapsed);
            }
        }

        ResultInfo result = null;
        if (battle.getStatus() == BattleStatus.COMPLETED) {
            BattleOutcome outcome = battle.getWinner() == null ? BattleOutcome.DRAW
                    : battle.getWinner().getId().equals(meId) ? BattleOutcome.WIN : BattleOutcome.LOSS;
            int before = iAmCreator ? battle.getCreatorRatingBefore() : battle.getOpponentRatingBefore();
            result = new ResultInfo(outcome, before, before + outcome.getRatingChange());
        }

        return new BattleStateResponse(
                battle.getId(), battle.getBattleCode(), battle.getStatus(),
                other == null ? null : new OpponentInfo(other.getUsername(), other.getRating()),
                iAmCreator ? battle.getCreatorScore() : battle.getOpponentScore(),
                iAmCreator ? battle.getOpponentScore() : battle.getCreatorScore(),
                QUESTIONS_PER_BATTLE, answered, otherAnswered,
                startsIn, secondsRemaining, SECONDS_PER_QUESTION,
                current, currentNumber, result);
    }
}