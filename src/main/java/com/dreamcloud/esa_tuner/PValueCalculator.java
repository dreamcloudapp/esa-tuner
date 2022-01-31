package com.dreamcloud.esa_tuner;

import com.dreamcloud.esa_core.similarity.DocumentSimilarity;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PValueCalculator {
    private final File file;
    private final File documentFile;
    private ArrayList<DocumentPair> humanSimilarityList;

    public static void resolveDocuments(ArrayList<DocumentPair> docSims, File documentFile) throws IOException, CsvValidationException {
        CSVReader csvReader = new CSVReader(new FileReader(documentFile));
        ArrayList<String> documents = new ArrayList<>();
        String[] values;
        while ((values = csvReader.readNext()) != null) {
            if (values.length < 1 ) {
                throw new CsvValidationException("LP50 document file improperly formatted.");
            }
            documents.add(values[0]);
        }

        for (DocumentPair docSim: docSims) {
            int doc1 = Integer.parseInt(docSim.getDoc1());
            int doc2 = Integer.parseInt(docSim.getDoc2());
            if (documents.size() <= doc1) {
                throw new CsvValidationException("LP50 document " + doc1 + " could not be found.");
            }
            if (documents.size() <= doc2) {
                throw new CsvValidationException("LP50 document " + doc1 + " could not be found.");
            }
            docSim.setDoc1(documents.get(doc1));
            docSim.setDoc2(documents.get(doc2));
        }
    }

    public PValueCalculator(File file, File documentFile) {
        this.file = file;
        this.documentFile = documentFile;
    }

    public PValueCalculator(File file) {
        this(file, null);
    }

    public double getPearsonCorrelation(DocumentSimilarity similarity) throws Exception {
        ArrayList<DocumentPair> humanSimilarityList = this.readHumanScores();
        double[] humanScores = this.getHumanScores(humanSimilarityList);
        double[] esaScores = this.getEsaScores(humanSimilarityList, similarity);
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        return pearsonsCorrelation.correlation(humanScores, esaScores);
    }

    public double getSpearmanCorrelation(DocumentSimilarity similarity) throws Exception {
        ArrayList<DocumentPair> humanSimilarityList = this.readHumanScores();
        double[] humanScores = this.getHumanScores(humanSimilarityList);
        double[] esaScores = this.getEsaScores(humanSimilarityList, similarity);
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        return spearmansCorrelation.correlation(humanScores, esaScores);
    }

    private double[] getEsaScores(ArrayList<DocumentPair> humanSimilarityList, DocumentSimilarity similarity) throws Exception {
        double[] esaScores = new double[humanSimilarityList.size()];
        for(int i=0; i<humanSimilarityList.size(); i++) {
            DocumentPair docSim = humanSimilarityList.get(i);
            esaScores[i] = similarity.score(docSim.getDoc1(), docSim.getDoc2()).getScore();

            String sourceDesc = docSim.getDoc1().substring(0, Math.min(16, docSim.getDoc1().length()));
            String compareDesc = docSim.getDoc2().substring(0, Math.min(16, docSim.getDoc2().length()));
            System.out.println("doc " + i + "\t ('" + sourceDesc + "', '" + compareDesc + "'):\t" + esaScores[i]);
        }
        return esaScores;
    }

    private double[] getHumanScores(ArrayList<DocumentPair> humanSimilarityList) {
        double[] humanScores = new double[humanSimilarityList.size()];
        for(int i=0; i<humanSimilarityList.size(); i++) {
            DocumentPair docSim = humanSimilarityList.get(i);
            humanScores[i] = docSim.getScore();
        }
        return humanScores;
    }

    public ArrayList<DocumentPair> readHumanScores() throws IOException, CsvValidationException {
        if (humanSimilarityList == null) {
            humanSimilarityList = new ArrayList<>();
            CSVReader csvReader = new CSVReader(new FileReader(file));
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                if (values.length < 3 ) {
                    throw new CsvValidationException("Word sim file improperly formatted.");
                }
                DocumentPair docSim = new DocumentPair();
                docSim.setDoc1(values[0]);
                docSim.setDoc2(values[1]);
                docSim.setScore(Float.parseFloat(values[2]) / 10.0f);
                humanSimilarityList.add(docSim);
            }
            if (documentFile != null) {
                resolveDocuments(humanSimilarityList, documentFile);
            }
        }
        return humanSimilarityList;
    }
}
