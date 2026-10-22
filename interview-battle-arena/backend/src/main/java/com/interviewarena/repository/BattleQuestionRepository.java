package com.interviewarena.repository;

import com.interviewarena.entity.BattleQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BattleQuestionRepository extends JpaRepository<BattleQuestion, Long> {

    @EntityGraph(attributePaths = "question") // load questions in the same query (avoids N+1)
    List<BattleQuestion> findByBattleIdOrderByQuestionOrder(Long battleId);

    /** Ids of questions in battles this user is playing (or about to play) and that are not finished yet. */
    @Query("""
            select bq.question.id from BattleQuestion bq
            where bq.battle.status <> com.interviewarena.entity.BattleStatus.COMPLETED
              and (bq.battle.creator.id = :userId or bq.battle.opponent.id = :userId)
            """)
    List<Long> findActiveQuestionIds(@Param("userId") Long userId);
}