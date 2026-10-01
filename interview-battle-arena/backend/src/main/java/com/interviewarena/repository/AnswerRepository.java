package com.interviewarena.repository;

import com.interviewarena.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    boolean existsByBattleIdAndUserIdAndQuestionId(Long battleId, Long userId, Long questionId);
    List<Answer> findByBattleIdAndUserId(Long battleId, Long userId);
}