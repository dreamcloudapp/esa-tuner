package com.dreamcloud.esa_tuner;

import com.dreamcloud.esa_tuner.DocumentPair;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class DocumentPairCsvWriter {
    private final File outputFile;

    public DocumentPairCsvWriter(File outputFile) {
        this.outputFile = outputFile;
    }

    public void writePairs(ArrayList<DocumentPair> documentPairs) throws IOException {
        Writer fileWriter = new FileWriter(outputFile);
        CSVWriter writer = new CSVWriter(fileWriter);
        for (DocumentPair documentPair: documentPairs) {
            writer.writeNext(new String[]{documentPair.getDoc1(), documentPair.getDoc2(), String.valueOf(documentPair.getScore())});
        }
        writer.close();
    }
}
