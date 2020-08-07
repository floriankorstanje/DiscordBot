package com.florn.ScoreSystem;

//Just an easy class to transfer some user data about their rank
public class Rank {
    private UserScore above, below;
    int totalMembers, position, topPercentage, score;
    boolean higherPeeps, superPeeps;
    double higherPeepsProgress, superPeepsProgress;

    public Rank(int _score, UserScore _above, UserScore _below, int _totalMembers, int _position, boolean _higherPeeps, boolean _superPeeps, double _higherPeepsProgress, double _superPeepsProgress) {
        above = _above;
        below = _below;
        totalMembers = _totalMembers;
        position = _position;
        higherPeeps = _higherPeeps;
        superPeeps = _superPeeps;
        score = _score;
        higherPeepsProgress = _higherPeepsProgress;
        superPeepsProgress = _superPeepsProgress;
    }

    public int getScore() {
        return score;
    }

    public UserScore getAbove() {
        return above;
    }

    public UserScore getBelow() {
        return below;
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    public int getPosition() {
        return position;
    }

    public boolean getUserAchievedHigherPeeps() {
        return higherPeeps;
    }

    public boolean getUserAchievedSuperPeeps() {
        return superPeeps;
    }

    public int getTopPercentage() {
        return (int) Math.round((double) position / (double) totalMembers * 100);
    }

    public int getHigherPeepsProgress() {
        return (int) Math.round(higherPeepsProgress);
    }

    public int getSuperPeepsProgress() {
        return (int) Math.round(superPeepsProgress);
    }
}
