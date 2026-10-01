package com.interviewarena.service;

import com.interviewarena.dto.HistoryItemResponse;
import com.interviewarena.entity.*;
import com.interviewarena.repository.BattleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BattleHistoryService {

    private static final int MAX_ITEMS = 50;

    private final BattleRepository battleRepository;

    @Transactional(readOnly = true)
    public List<HistoryItemResponse> getHistory(User me) {
        return battleRepository
                .findHistory(me.getId(), BattleStatus.COMPLETED, PageRequest.of(0, MAX_ITEMS))
                .stream()
                .map(b -> toItem(b, me.getId()))
                .toList();
    }

    private HistoryItemResponse toItem(Battle b, Long meId) {
        boolean iAmCreator = b.getCreator().getId().equals(meId);
        User opponent = iAmCreator ? b.getOpponent() : b.getCreator();

        BattleOutcome outcome = b.getWinner() == null ? BattleOutcome.DRAW
                : b.getWinner().getId().equals(meId) ? BattleOutcome.WIN : BattleOutcome.LOSS;

        return new HistoryItemResponse(
                b.getId(),
                opponent.getUsername(),
                iAmCreator ? b.getCreatorScore() : b.getOpponentScore(),
                iAmCreator ? b.getOpponentScore() : b.getCreatorScore(),
                outcome,
                outcome.getRatingChange(), // same enum Phase 6 used to change the rating
                b.getCompletedAt());
    }
}