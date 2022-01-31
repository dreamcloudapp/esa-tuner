package com.dreamcloud.esa_tuner;

public class PrunerTuning {
    private final double tunedScore;
    private final int tunedWindowSize;
    private final double tunedWindowDropOff;
    private final double tunedVectorLimit;

    public PrunerTuning(double tunedScore, int tunedWindowSize, double tunedWindowDropOff, double tunedVectorLimit) {
        this.tunedScore = tunedScore;
        this.tunedWindowSize = tunedWindowSize;
        this.tunedWindowDropOff = tunedWindowDropOff;
        this.tunedVectorLimit = tunedVectorLimit;
    }

    public double getTunedScore() {
        return tunedScore;
    }

    public double getTunedWindowDropOff() {
        return tunedWindowDropOff;
    }

    public int getTunedWindowSize() {
        return tunedWindowSize;
    }

    public double getTunedVectorLimit() {
        return tunedVectorLimit;
    }
}
