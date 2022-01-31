package com.dreamcloud.esa_tuner;

import com.dreamcloud.esa_core.analyzer.AnalyzerOptions;
import com.dreamcloud.esa_core.analyzer.EsaAnalyzer;
import com.dreamcloud.esa_core.analyzer.TokenizerFactory;
import com.dreamcloud.esa_core.cli.AnalyzerOptionsReader;
import com.dreamcloud.esa_core.cli.VectorizationOptionsReader;
import com.dreamcloud.esa_core.similarity.DocumentSimilarity;
import com.dreamcloud.esa_core.vectorizer.TextVectorizer;
import com.dreamcloud.esa_core.vectorizer.VectorBuilder;
import com.dreamcloud.esa_core.vectorizer.VectorizationOptions;
import com.dreamcloud.esa_core.vectorizer.Vectorizer;
import com.dreamcloud.esa_score.analysis.TfIdfAnalyzer;
import com.dreamcloud.esa_score.analysis.TfIdfOptions;
import com.dreamcloud.esa_score.analysis.TfIdfStrategyFactory;
import com.dreamcloud.esa_score.analysis.strategy.TfIdfStrategy;
import com.dreamcloud.esa_score.cli.FileSystemScoringReader;
import com.dreamcloud.esa_score.cli.TfIdfOptionsReader;
import com.dreamcloud.esa_tuner.cli.TuningOptionsReader;
import org.apache.commons.cli.*;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();

        FileSystemScoringReader fileSystemScoringReader = new FileSystemScoringReader();
        fileSystemScoringReader.addOptions(options);
        TfIdfOptionsReader tfIdfOptionsReader = new TfIdfOptionsReader();
        tfIdfOptionsReader.addOptions(options);
        AnalyzerOptionsReader analyzerOptionsReader = new AnalyzerOptionsReader();
        analyzerOptionsReader.addOptions(options);
        VectorizationOptionsReader vectorizationOptionsReader = new VectorizationOptionsReader();
        vectorizationOptionsReader.addOptions(options);
        TuningOptionsReader tuningOptionsReader = new TuningOptionsReader();
        tuningOptionsReader.addOptions(options);

        //Spearman correlations to get tool p-value
        Option spearmanOption = new Option(null, "spearman", true, "correlation file / Calculates Spearman correlations to get the p-value of the tool");
        spearmanOption.setRequired(false);
        options.addOption(spearmanOption);

        //Pearson correlations to get tool p-value
        Option pearsonOption = new Option(null, "pearson", true, "correlation file [document file] / Calculates Pearson correlations to get the p-value of the tool");
        pearsonOption.setRequired(false);
        options.addOption(pearsonOption);

        //Tune vectorization options to find best settings
        Option tuneOption = new Option(null, "tune", false, "Finds ideal pruning options for spearman and pearson");
        tuneOption.setRequired(false);
        options.addOption(tuneOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cli = parser.parse(options, args);

            TuningOptions tuneOptions = tuningOptionsReader.getOptions(cli);
            VectorizationOptions vectorOptions = vectorizationOptionsReader.getOptions(cli);
            AnalyzerOptions analyzerOptions = analyzerOptionsReader.getOptions(cli);
            TfIdfOptions tfIdfOptions = tfIdfOptionsReader.getOptions(cli);
            fileSystemScoringReader.parseOptions(cli);

            //Just for Wikipedia
            Set<String> stopTokenTypes = new HashSet<>();
            stopTokenTypes.add(WikipediaTokenizer.EXTERNAL_LINK_URL);
            stopTokenTypes.add(WikipediaTokenizer.EXTERNAL_LINK);
            stopTokenTypes.add(WikipediaTokenizer.CITATION);
            analyzerOptions.setStopTokenTypes(stopTokenTypes);
            analyzerOptions.setTokenizerFactory(new TokenizerFactory() {
                public Tokenizer getTokenizer() {
                    return new WikipediaTokenizer();
                }
            });

            TfIdfStrategyFactory tfIdfFactory = new TfIdfStrategyFactory();
            TfIdfStrategy tfIdfStrategy = tfIdfFactory.getStrategy(tfIdfOptions);
            TfIdfAnalyzer tfIdfAnalyzer = new TfIdfAnalyzer(tfIdfStrategy, new EsaAnalyzer(analyzerOptions), fileSystemScoringReader.getCollectionInfo());
            VectorBuilder vectorBuilder = new VectorBuilder(fileSystemScoringReader.getScoreReader(), fileSystemScoringReader.getCollectionInfo(), tfIdfAnalyzer, analyzerOptions.getPreprocessor(), vectorOptions);
            TextVectorizer textVectorizer = new Vectorizer(vectorBuilder);

            if (cli.hasOption("spearman")) {
                String spearman = cli.getOptionValue("spearman");
                if ("en-wordsim353".equals(spearman)) {
                    spearman = "./src/data/en-wordsim353.csv";
                }

                DocumentSimilarity similarityTool = new DocumentSimilarity(textVectorizer);
                PValueCalculator calculator = new PValueCalculator(new File(spearman));
                System.out.println("Calculating P-value using Spearman correlation...");
                System.out.println("----------------------------------------");
                System.out.println("p-value:\t" + calculator.getSpearmanCorrelation(similarityTool));
                System.out.println("----------------------------------------");
            }

            else if (cli.hasOption("pearson")) {
                String[] pearsonArgs = cli.getOptionValues("pearson");
                String pearsonFile = pearsonArgs[0];
                File documentFile = pearsonArgs.length > 1 ? new File(pearsonArgs[1]) : null;
                if ("en-lp50".equals(pearsonFile)) {
                    pearsonFile = "./src/data/en-lp50.csv";
                    documentFile = new File("./src/data/en-lp50-documents.csv");
                }
                DocumentSimilarity similarityTool = new DocumentSimilarity(textVectorizer);
                PValueCalculator calculator = new PValueCalculator(new File(pearsonFile), documentFile);
                System.out.println("Calculating P-value using Pearson correlation...");
                System.out.println("----------------------------------------");
                System.out.println("p-value:\t" + calculator.getPearsonCorrelation(similarityTool));
                System.out.println("----------------------------------------");
            }

            else if (cli.hasOption("tune")) {
                File spearmanFile = new File("./src/data/en-wordsim353.csv");
                File pearsonFile = new File("./src/data/en-lp50.csv");
                File documentFile = new File("./src/data/en-lp50-documents.csv");

                PValueCalculator pValueCalculator;
                if ("spearman".equals(tuneOptions.getType())) {
                    pValueCalculator = new PValueCalculator(spearmanFile);
                } else {
                    pValueCalculator = new PValueCalculator(pearsonFile, documentFile);
                }

                DocumentSimilarity similarityTool = new DocumentSimilarity(textVectorizer);
                PrunerTuner tuner = new PrunerTuner(similarityTool);
                System.out.println("Analyzing wordsim-353 to find the ideal vector limit...");
                System.out.println("----------------------------------------");
                PrunerTuning tuning = tuner.tune(pValueCalculator, tuneOptions, vectorOptions);
                System.out.println("tuned p-value:\t" + tuning.getTunedScore());
                System.out.println("tuned window size:\t" + tuning.getTunedWindowSize());
                System.out.println("tuned window dropoff:\t" + tuning.getTunedWindowDropOff());
                System.out.println("tuned vector limit:\t" + tuning.getTunedVectorLimit());
                System.out.println("----------------------------------------");
            }
            else {
                formatter.printHelp("esa-tuner", options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
