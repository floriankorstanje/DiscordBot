package com.flornian.ScoreSystem;

//Just an easy class to transfer some user data about their rank
public class Rank {
    private final UserScore above, below;
    private final int totalMembers, position, score;
    private final boolean higherPeeps, superPeeps;
    private final double higherPeepsProgress, superPeepsProgress;

    public Rank(int score, UserScore above, UserScore below, int totalMembers, int position, boolean higherPeeps, boolean superPeeps, double higherPeepsProgress, double superPeepsProgress) {
        this.score = score;
        this.above = above;
        this.below = below;
        this.totalMembers = totalMembers;
        this.position = position;
        this.higherPeeps = higherPeeps;
        this.superPeeps = superPeeps;
        this.higherPeepsProgress = higherPeepsProgress;
        this.superPeepsProgress = superPeepsProgress;
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
