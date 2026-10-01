package com.interviewarena.dto;

import com.interviewarena.entity.Category;
import com.interviewarena.entity.Difficulty;
import com.interviewarena.entity.Question;

import java.util.LinkedHashMap;
import java.util.Map;

public record QuestionResponse(
        Long id,
        String questionText,
        Category category,
        Difficulty difficulty,
        Map<String, String> options
) {
    public static QuestionResponse from(Question q) {
        Map<String, String> options = new LinkedHashMap<>(); // keeps A, B, C, D order
        options.put("A", q.getOptionA());
        options.put("B", q.getOptionB());
        options.put("C", q.getOptionC());
        options.put("D", q.getOptionD());
        return new QuestionResponse(q.getId(), q.getQuestionText(),
                q.getCategory(), q.getDifficulty(), options);
    }
}