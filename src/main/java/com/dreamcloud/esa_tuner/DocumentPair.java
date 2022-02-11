package com.dreamcloud.esa_tuner;

public class DocumentPair {
    private String doc1;
    private String doc2;
    private float score;

    public DocumentPair(String doc1, String doc2, float score) {
        this.doc1 = doc1;
        this.doc2 = doc2;
        this.score = score;
    }

    public String getDoc1() {
        return doc1;
    }

    public void setDoc1(String doc1) {
        this.doc1 = doc1;
    }

    public String getDoc2() {
        return doc2;
    }

    public void setDoc2(String doc2) {
        this.doc2 = doc2;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
