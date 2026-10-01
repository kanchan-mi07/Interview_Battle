package com.interviewarena.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "battles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "battle_code", nullable = false, unique = true, length = 6)
    private String battleCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_id")
    private User opponent; // null until someone joins

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BattleStatus status = BattleStatus.WAITING;

    @Column(name = "creator_score", nullable = false)
    @Builder.Default
    private int creatorScore = 0;

    @Column(name = "opponent_score", nullable = false)
    @Builder.Default
    private int opponentScore = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner; // null while running, and for a draw

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt; // set when the opponent joins: now + start delay

    @Column(name = "creator_rating_before")
    private Integer creatorRatingBefore; // filled when the battle completes

    @Column(name = "opponent_rating_before")
    private Integer opponentRatingBefore;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}