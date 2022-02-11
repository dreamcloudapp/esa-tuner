package com.dreamcloud.esa_tuner;

import com.dreamcloud.esa_tuner.DocumentPair;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DocumentPairCsvReader {
    private static void resolveDocuments(ArrayList<DocumentPair> docSims, File documentFile) throws IOException, CsvValidationException {
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

    public static ArrayList<DocumentPair> readDocumentPairs(File inputFile) throws CsvValidationException, IOException {
        return readDocumentPairs(inputFile, null);
    }

    public static ArrayList<DocumentPair> readDocumentPairs(File inputFile, File documentFile) throws IOException, CsvValidationException {
        ArrayList<DocumentPair> humanSimilarityList = new ArrayList<>();
        CSVReader csvReader = new CSVReader(new FileReader(inputFile));
        String[] values;
        while ((values = csvReader.readNext()) != null) {
            if (values.length < 3 ) {
                throw new CsvValidationException("Word sim file improperly formatted.");
            }
            humanSimilarityList.add(new DocumentPair(values[0], values[1], Float.parseFloat(values[2]) / 10.0f));
        }
        if (documentFile != null) {
            resolveDocuments(humanSimilarityList, documentFile);
        }
        return humanSimilarityList;
    }
}
