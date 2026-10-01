package com.interviewarena.dto;

public record LeaderboardEntry(
        int rank,
        String username,
        int rating,
        int wins,
        int losses,
        int totalBattles,
        boolean you
) {}