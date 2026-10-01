package com.interviewarena.controller;

import com.interviewarena.dto.PracticeResultResponse;
import com.interviewarena.dto.PracticeSubmitRequest;
import com.interviewarena.dto.QuestionResponse;
import com.interviewarena.entity.Category;
import com.interviewarena.entity.Difficulty;
import com.interviewarena.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public List<QuestionResponse> getQuestions(@RequestParam(required = false) Category category,
                                               @RequestParam(required = false) Difficulty difficulty,
                                               @RequestParam(required = false) Integer count) {
        return questionService.getQuestions(category, difficulty, count);
    }

    @GetMapping("/{id}")
    public QuestionResponse getQuestion(@PathVariable Long id) {
        return questionService.getById(id);
    }

    @PostMapping("/practice/submit")
    public PracticeResultResponse submitPractice(@Valid @RequestBody PracticeSubmitRequest request) {
        return questionService.submitPractice(request);
    }
}