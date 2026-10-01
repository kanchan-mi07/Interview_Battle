package com.interviewarena.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinBattleRequest(
        @NotBlank(message = "Battle code is required.")
        @Size(min = 6, max = 6, message = "Battle code must be 6 characters.")
        String battleCode
) {}