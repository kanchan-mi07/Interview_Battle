package com.interviewarena.entity;

public enum BattleOutcome {
    WIN(25), LOSS(-15), DRAW(0);

    private final int ratingChange;

    BattleOutcome(int ratingChange) {
        this.ratingChange = ratingChange;
    }

    public int getRatingChange() {
        return ratingChange;
    }
}