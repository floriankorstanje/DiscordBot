package com.florn.ScoreSystem;

public class Range {
    private int min, max;

    public Range(int _min, int _max) {
        min = _min;
        max = _max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int _min) {
        min = _min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int _max) {
        max = _max;
    }
}
