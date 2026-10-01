package com.interviewarena.dto;

import com.interviewarena.entity.BattleOutcome;

import java.time.LocalDateTime;

public record HistoryItemResponse(
        Long battleId,
        String opponentUsername,
        int myScore,
        int opponentScore,
        BattleOutcome result,
        int ratingChange,
        LocalDateTime completedAt
) {}
