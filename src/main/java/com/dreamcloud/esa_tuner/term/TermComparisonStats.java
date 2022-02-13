package com.dreamcloud.esa_tuner.term;

import com.dreamcloud.esa_score.score.DocumentNameResolver;

import javax.print.attribute.standard.DocumentName;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TermComparisonStats {
    public String term;

    public int docFreq1;
    public int docFreq2;

    public float avgDocScore1;
    public float avgDocScore2;

    public Map<Integer, Float> scoreDocs1 = new LinkedHashMap<>();
    public Map<Integer, Float> scoreDocs2 = new LinkedHashMap<>();

    public void print(File idTitles1, File idTitles2) throws IOException {
        System.out.println("Term Stats for '" + term + "'");
        System.out.println("----------------------------------------");
        System.out.println("Document Frequency 1: " + docFreq1);
        System.out.println("Document Frequency 2: " + docFreq2);
        System.out.println("Average Document Score 1: " + avgDocScore1);
        System.out.println("Average Document Score 2: " + avgDocScore2);
        System.out.println("---Document 1 Scores---");
        DocumentNameResolver.loadFile(idTitles1);
        for (Integer docId: scoreDocs1.keySet()) {
            System.out.println(docId + "\t" + scoreDocs1.get(docId) + "\t" + DocumentNameResolver.getTitle(docId));
        }
        System.out.println("---Document 2 Scores---");
        DocumentNameResolver.loadFile(idTitles2);
        for (Integer docId: scoreDocs2.keySet()) {
            System.out.println(docId + "\t" + scoreDocs2.get(docId) + "\t" + DocumentNameResolver.getTitle(docId));
        }
        System.out.println("----------------------------------------");
    }
}
