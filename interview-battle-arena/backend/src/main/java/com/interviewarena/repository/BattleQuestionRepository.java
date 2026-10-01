package com.interviewarena.repository;

import com.interviewarena.entity.BattleQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BattleQuestionRepository extends JpaRepository<BattleQuestion, Long> {

    @EntityGraph(attributePaths = "question") // load questions in the same query (avoids N+1)
    List<BattleQuestion> findByBattleIdOrderByQuestionOrder(Long battleId);
}