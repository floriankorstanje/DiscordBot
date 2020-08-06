package com.florn.ScoreSystem;

public class UserScore {
    private int score;
    private String id;

    public UserScore(String uid, int uscore) {
        score = uscore;
        id = uid;
    }

    public int getScore() {
        return score;
    }

    public String getId() {
        return id;
    }
}
