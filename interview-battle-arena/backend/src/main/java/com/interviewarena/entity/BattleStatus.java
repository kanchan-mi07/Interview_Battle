package com.interviewarena.entity;

public enum BattleStatus {
    WAITING,      // created, waiting for the second player
    READY,        // both players present
    IN_PROGRESS,  // questions are being answered
    COMPLETED     // result saved
}