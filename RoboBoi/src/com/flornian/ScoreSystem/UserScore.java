package com.flornian.ScoreSystem;

public class UserScore {
    private final int score;
    private final String uid;

    public UserScore(String uid, int score) {
        this.score = score;
        this.uid = uid;
    }

    public int getScore() {
        return score;
    }

    public String getUID() {
        return uid;
    }
}
