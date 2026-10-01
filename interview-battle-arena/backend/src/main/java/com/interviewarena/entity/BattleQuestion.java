package com.interviewarena.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "battle_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"battle_id", "question_order"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BattleQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "question_order", nullable = false)
    private int questionOrder; // 1..10, same for both players
}