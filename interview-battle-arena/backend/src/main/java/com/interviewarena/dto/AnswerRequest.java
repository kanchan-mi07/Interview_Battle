package com.interviewarena.dto;

import com.interviewarena.entity.OptionChoice;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull(message = "Question id is required.") Long questionId,
        OptionChoice selectedOption // null = no answer (time ran out)
) {}