package com.dreamcloud.esa_tuner;

public class TuningOptions {
    private String type = "spearman";
    private int startingWindowSize;
    private int endingWindowSize;
    private int windowSizeStep;
    private float startingWindowDrop;
    private float endingWindowDrop;
    private float windowDropStep;
    private int startingVectorLimit;
    private int endingVectorLimit;
    private int vectorLimitStep;

    public int getStartingWindowSize() {
        return startingWindowSize;
    }

    public void setStartingWindowSize(int startingWindowSize) {
        this.startingWindowSize = startingWindowSize;
    }

    public int getEndingWindowSize() {
        return endingWindowSize;
    }

    public void setEndingWindowSize(int endingWindowSize) {
        this.endingWindowSize = endingWindowSize;
    }

    public int getWindowSizeStep() {
        return windowSizeStep;
    }

    public void setWindowSizeStep(int windowSizeStep) {
        this.windowSizeStep = windowSizeStep;
    }

    public float getStartingWindowDrop() {
        return startingWindowDrop;
    }

    public void setStartingWindowDrop(float startingWindowDrop) {
        this.startingWindowDrop = startingWindowDrop;
    }

    public float getEndingWindowDrop() {
        return endingWindowDrop;
    }

    public void setEndingWindowDrop(float endingWindowDrop) {
        this.endingWindowDrop = endingWindowDrop;
    }

    public float getWindowDropStep() {
        return windowDropStep;
    }

    public void setWindowDropStep(float windowDropStep) {
        this.windowDropStep = windowDropStep;
    }

    public int getStartingVectorLimit() {
        return startingVectorLimit;
    }

    public void setStartingVectorLimit(int startingVectorLimit) {
        this.startingVectorLimit = startingVectorLimit;
    }

    public int getEndingVectorLimit() {
        return endingVectorLimit;
    }

    public void setEndingVectorLimit(int endingVectorLimit) {
        this.endingVectorLimit = endingVectorLimit;
    }

    public int getVectorLimitStep() {
        return vectorLimitStep;
    }

    public void setVectorLimitStep(int vectorLimitStep) {
        this.vectorLimitStep = vectorLimitStep;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
