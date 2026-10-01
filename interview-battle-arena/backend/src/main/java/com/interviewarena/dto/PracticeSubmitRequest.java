package com.interviewarena.dto;

import com.interviewarena.entity.OptionChoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PracticeSubmitRequest(
        @NotEmpty(message = "Answers are required.")
        @Size(max = 20, message = "Too many answers.")
        @Valid
        List<PracticeAnswer> answers
) {
    // selectedOption is null when the player ran out of time
    public record PracticeAnswer(
            @NotNull(message = "Question id is required.") Long questionId,
            OptionChoice selectedOption
    ) {}
}