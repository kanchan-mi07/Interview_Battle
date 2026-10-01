package com.interviewarena.dto;

import com.interviewarena.entity.User;

public record UserResponse(
        Long id,
        String username,
        String email,
        int rating,
        int wins,
        int losses,
        int totalBattles,
        double winRate
) {
    public static UserResponse from(User u) {
        double winRate = u.getTotalBattles() == 0
                ? 0.0
                : Math.round(u.getWins() * 1000.0 / u.getTotalBattles()) / 10.0;
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(),
                u.getRating(), u.getWins(), u.getLosses(), u.getTotalBattles(), winRate);
    }
}