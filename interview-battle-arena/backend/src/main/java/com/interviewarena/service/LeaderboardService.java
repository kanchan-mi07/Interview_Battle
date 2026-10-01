package com.interviewarena.service;

import com.interviewarena.dto.LeaderboardEntry;
import com.interviewarena.dto.LeaderboardResponse;
import com.interviewarena.entity.User;
import com.interviewarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public LeaderboardResponse getLeaderboard(int page, int size, User me) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        // Sorting happens in MySQL. Wins, then id, break rating ties so pages stay stable.
        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("wins"), Sort.Order.asc("id")));

        Page<User> result = userRepository.findAll(pageable);

        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = safePage * safeSize;
        for (User u : result.getContent()) {
            entries.add(new LeaderboardEntry(++rank, u.getUsername(), u.getRating(),
                    u.getWins(), u.getLosses(), u.getTotalBattles(), u.getId().equals(me.getId())));
        }

        return new LeaderboardResponse(entries, safePage, safeSize,
                result.getTotalPages(), result.getTotalElements());
    }
}