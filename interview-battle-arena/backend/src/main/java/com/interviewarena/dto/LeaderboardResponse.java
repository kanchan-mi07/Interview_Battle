package com.interviewarena.dto;

import java.util.List;

public record LeaderboardResponse(
        List<LeaderboardEntry> entries,
        int page,
        int size,
        int totalPages,
        long totalPlayers
) {}
