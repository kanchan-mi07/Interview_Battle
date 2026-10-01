package com.interviewarena.repository;

import com.interviewarena.entity.Battle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.interviewarena.entity.BattleStatus;
import org.springframework.data.domain.Pageable;
import java.util.List;

import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, Long> {

    boolean existsByBattleCode(String battleCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Battle b where b.id = :id")
    Optional<Battle> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Battle b where b.battleCode = :code")
    Optional<Battle> findByBattleCodeForUpdate(@Param("code") String code);

    @Query("""
            select b from Battle b
            join fetch b.creator
            join fetch b.opponent
            left join fetch b.winner
            where b.status = :status
              and (b.creator.id = :userId or b.opponent.id = :userId)
            order by b.completedAt desc
            """)
    List<Battle> findHistory(@Param("userId") Long userId,
                             @Param("status") BattleStatus status,
                             Pageable pageable);
}