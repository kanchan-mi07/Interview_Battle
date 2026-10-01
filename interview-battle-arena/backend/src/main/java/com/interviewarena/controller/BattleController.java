package com.interviewarena.controller;

import com.interviewarena.dto.*;
import com.interviewarena.entity.User;
import com.interviewarena.service.BattleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.interviewarena.service.BattleHistoryService;
import java.util.List;

@RestController
@RequestMapping("/api/battles")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;
    private final BattleHistoryService historyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BattleStateResponse create(@AuthenticationPrincipal User me) {
        return battleService.create(me);
    }

    @PostMapping("/join")
    public BattleStateResponse join(@AuthenticationPrincipal User me,
                                    @Valid @RequestBody JoinBattleRequest request) {
        return battleService.join(me, request.battleCode());
    }

    @GetMapping("/{id}")
    public BattleStateResponse get(@AuthenticationPrincipal User me, @PathVariable Long id) {
        return battleService.getBattle(id, me);
    }

    @PostMapping("/{id}/answer")
    public AnswerResponse answer(@AuthenticationPrincipal User me, @PathVariable Long id,
                                 @Valid @RequestBody AnswerRequest request) {
        return battleService.submitAnswer(id, me, request);
    }

    @PostMapping("/{id}/finish")
    public BattleStateResponse finish(@AuthenticationPrincipal User me, @PathVariable Long id) {
        return battleService.finish(id, me);
    }
    @GetMapping("/history")
    public List<HistoryItemResponse> history(@AuthenticationPrincipal User me) {
        return historyService.getHistory(me);
    }
}
