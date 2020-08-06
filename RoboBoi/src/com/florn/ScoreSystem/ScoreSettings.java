package com.florn.ScoreSystem;

public class ScoreSettings {
    private Range rppm;
    private int rppmd, cpd;

    public ScoreSettings(Range randomPointsPerMessage, int randomPointsPerMessageDelay, int callPointsDelay) {
        rppm = randomPointsPerMessage;
        rppmd = randomPointsPerMessageDelay;
        cpd = callPointsDelay;
    }

    public Range getRandomPointsPerMessage() {
        return rppm;
    }

    public int getRandomPointsPerMessageDelay() {
        return rppmd;
    }

    public int getCallPointsDelay() {
        return cpd;
    }

    public void setRandomPointsPerMessage(Range range) {
        rppm = range;
    }

    public void setRandomPointsPerMessageDelay(int randomPointsPerMessageDelay) {
        rppmd = randomPointsPerMessageDelay;
    }

    public void setCallPointsDelay(int callPointsDelay) {
        cpd = callPointsDelay;
    }
}
