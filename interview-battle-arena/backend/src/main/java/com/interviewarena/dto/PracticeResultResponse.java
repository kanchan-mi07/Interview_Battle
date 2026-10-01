package com.interviewarena.dto;

import com.interviewarena.entity.OptionChoice;

import java.util.List;
import java.util.Map;

public record PracticeResultResponse(
        int total,
        int correct,
        int wrong,      // includes unanswered
        int score,
        List<QuestionResult> results
) {
    public record QuestionResult(
            Long questionId,
            String questionText,
            Map<String, String> options,
            OptionChoice selectedOption,
            OptionChoice correctOption,
            boolean correct,
            String explanation
    ) {}
}