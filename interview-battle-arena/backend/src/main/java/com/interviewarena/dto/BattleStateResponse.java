package com.interviewarena.dto;

import com.interviewarena.entity.BattleOutcome;
import com.interviewarena.entity.BattleStatus;

public record BattleStateResponse(
        Long id,
        String battleCode,
        BattleStatus status,
        OpponentInfo opponent,          // null while WAITING
        int myScore,
        int opponentScore,
        int totalQuestions,
        int answeredCount,
        int opponentAnsweredCount,
        long startsInSeconds,           // > 0 during the pre-battle countdown
        int secondsRemaining,           // for the current question, computed by the server
        int questionTimeSeconds,
        QuestionResponse currentQuestion,
        int currentQuestionNumber,
        ResultInfo result               // null until COMPLETED
) {
    public record OpponentInfo(String username, int rating) {}

    public record ResultInfo(BattleOutcome outcome, int ratingBefore, int ratingAfter) {}
}