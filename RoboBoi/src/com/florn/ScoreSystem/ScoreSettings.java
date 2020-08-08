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

    public void setRandomPointsPerMessage(Range range) {
        rppm = range;
    }

    public int getRandomPointsPerMessageDelay() {
        return rppmd;
    }

    public void setRandomPointsPerMessageDelay(int randomPointsPerMessageDelay) {
        rppmd = randomPointsPerMessageDelay;
    }

    public int getCallPointsDelay() {
        return cpd;
    }

    public void setCallPointsDelay(int callPointsDelay) {
        cpd = callPointsDelay;
    }
}
