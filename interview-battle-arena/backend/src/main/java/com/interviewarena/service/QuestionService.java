package com.interviewarena.service;

import com.interviewarena.dto.PracticeResultResponse;
import com.interviewarena.dto.PracticeResultResponse.QuestionResult;
import com.interviewarena.dto.PracticeSubmitRequest;
import com.interviewarena.dto.PracticeSubmitRequest.PracticeAnswer;
import com.interviewarena.dto.QuestionResponse;
import com.interviewarena.entity.Category;
import com.interviewarena.entity.Difficulty;
import com.interviewarena.entity.Question;
import com.interviewarena.entity.User;
import com.interviewarena.exception.ApiException;
import com.interviewarena.repository.BattleQuestionRepository;
import com.interviewarena.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final int DEFAULT_COUNT = 10;
    private static final int MAX_COUNT = 20;
    private static final int POINTS_PER_CORRECT = 100;

    private final QuestionRepository questionRepository;
    private final BattleQuestionRepository battleQuestionRepository;

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestions(User me, Category category, Difficulty difficulty, Integer count) {
        int size = count == null ? DEFAULT_COUNT : Math.max(1, Math.min(count, MAX_COUNT));

        // Never hand out practice questions that belong to one of the player's live battles.
        List<Long> excluded = battleQuestionRepository.findActiveQuestionIds(me.getId());
        if (excluded.isEmpty()) excluded = List.of(-1L); // "NOT IN ()" is invalid SQL

        List<Question> questions = questionRepository.findRandomFiltered(
                category == null ? null : category.name(),
                difficulty == null ? null : difficulty.name(),
                excluded,
                size);

        if (questions.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No questions found for that selection.");
        }
        return questions.stream().map(QuestionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public QuestionResponse getById(Long id) {
        return questionRepository.findById(id)
                .map(QuestionResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found."));
    }

    /** Practice only: nothing is saved and no rating or stats change. */
    @Transactional(readOnly = true)
    public PracticeResultResponse submitPractice(User me, PracticeSubmitRequest request) {
        List<PracticeAnswer> answers = request.answers();

        Set<Long> seen = new HashSet<>();
        for (PracticeAnswer a : answers) {
            if (!seen.add(a.questionId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Each question can only be answered once.");
            }
        }

        // Practice results reveal the correct answer, so they must not work for questions in a live battle.
        Set<Long> inLiveBattle = new HashSet<>(battleQuestionRepository.findActiveQuestionIds(me.getId()));
        if (seen.stream().anyMatch(inLiveBattle::contains)) {
            throw new ApiException(HttpStatus.CONFLICT, "Finish your current battle before practicing those questions.");
        }

        Map<Long, Question> byId = questionRepository.findAllById(seen).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        if (byId.size() != seen.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Question not found.");
        }

        int correctCount = 0;
        List<QuestionResult> results = new java.util.ArrayList<>();

        for (PracticeAnswer a : answers) {
            Question q = byId.get(a.questionId());
            boolean correct = a.selectedOption() != null && a.selectedOption() == q.getCorrectOption();
            if (correct) correctCount++;

            results.add(new QuestionResult(
                    q.getId(), q.getQuestionText(), QuestionResponse.from(q).options(),
                    a.selectedOption(), q.getCorrectOption(), correct, q.getExplanation()));
        }

        int total = answers.size();
        return new PracticeResultResponse(total, correctCount, total - correctCount,
                correctCount * POINTS_PER_CORRECT, results);
    }
}