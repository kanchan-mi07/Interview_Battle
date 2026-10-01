package com.interviewarena.controller;

import com.interviewarena.dto.LeaderboardResponse;
import com.interviewarena.entity.User;
import com.interviewarena.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public LeaderboardResponse get(@AuthenticationPrincipal User me,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return leaderboardService.getLeaderboard(page, size, me);
    }
}