package com.dreamcloud.esa_tuner;

import com.dreamcloud.esa_core.similarity.DocumentSimilarity;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PValueCalculator {
    private boolean outputWordPairs = true;
    private final File file;
    private final File documentFile;
    private ArrayList<DocumentPair> humanSimilarityList;

    public PValueCalculator(File file, File documentFile) {
        this.file = file;
        this.documentFile = documentFile;
    }

    public PValueCalculator(File file) {
        this(file, null);
    }

    public void setOutputWordPairs(boolean outputWordPairs) {
        this.outputWordPairs = outputWordPairs;
    }

    public double getPearsonCorrelation(DocumentSimilarity similarity) throws Exception {
        ArrayList<DocumentPair> humanSimilarityList = this.readHumanScores();
        ArrayList<DocumentPair> esaScoreList = this.getEsaScores(humanSimilarityList, similarity);
        return this.getPearsonCorrelation(humanSimilarityList, esaScoreList);
    }

    public double getPearsonCorrelation(ArrayList<DocumentPair> docs1, ArrayList<DocumentPair> docs2) {
        double[] doc1Scores = docs1.stream().mapToDouble(DocumentPair::getScore).toArray();
        double[] doc2Scores = docs2.stream().mapToDouble(DocumentPair::getScore).toArray();
        return new PearsonsCorrelation().correlation(doc1Scores, doc2Scores);
    }

    public double getSpearmanCorrelation(DocumentSimilarity similarity) throws Exception {
        ArrayList<DocumentPair> humanSimilarityList = this.readHumanScores();
        ArrayList<DocumentPair> esaScoreList = this.getEsaScores(humanSimilarityList, similarity);
        return this.getSpearmanCorrelation(humanSimilarityList, esaScoreList);
    }

    public double getSpearmanCorrelation(ArrayList<DocumentPair> docs1, ArrayList<DocumentPair> docs2) {
        double[] doc1Scores = docs1.stream().mapToDouble(DocumentPair::getScore).toArray();
        double[] doc2Scores = docs2.stream().mapToDouble(DocumentPair::getScore).toArray();
        return new SpearmansCorrelation().correlation(doc1Scores, doc2Scores);
    }

    public ArrayList<DocumentPair> getEsaScores(ArrayList<DocumentPair> humanSimilarityList, DocumentSimilarity similarity) throws Exception {
        ArrayList<DocumentPair> esaScoreList = new ArrayList<>();
        for(int i=0; i<humanSimilarityList.size(); i++) {
            DocumentPair docSim = humanSimilarityList.get(i);
            float score = similarity.score(docSim.getDoc1(), docSim.getDoc2()).getScore();
            esaScoreList.add(new DocumentPair(docSim.getDoc1(), docSim.getDoc2(), score));

            String sourceDesc = docSim.getDoc1().substring(0, Math.min(16, docSim.getDoc1().length()));
            String compareDesc = docSim.getDoc2().substring(0, Math.min(16, docSim.getDoc2().length()));

            if (outputWordPairs) {
                System.out.println("doc " + i + "\t ('" + sourceDesc + "', '" + compareDesc + "'):\t" + score);
            }
        }
        return esaScoreList;
    }

    public ArrayList<DocumentPair> readHumanScores() throws IOException, CsvValidationException {
        if (humanSimilarityList == null) {
            humanSimilarityList = DocumentPairCsvReader.readDocumentPairs(file, documentFile);
        }
        return humanSimilarityList;
    }
}
