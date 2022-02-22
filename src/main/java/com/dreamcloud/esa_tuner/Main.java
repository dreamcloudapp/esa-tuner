package com.dreamcloud.esa_tuner;

import com.dreamcloud.esa_core.analyzer.AnalyzerOptions;
import com.dreamcloud.esa_core.analyzer.EsaAnalyzer;
import com.dreamcloud.esa_core.analyzer.TokenizerFactory;
import com.dreamcloud.esa_core.cli.AnalyzerOptionsReader;
import com.dreamcloud.esa_core.cli.VectorizationOptionsReader;
import com.dreamcloud.esa_core.similarity.DocumentSimilarity;
import com.dreamcloud.esa_core.vectorizer.*;
import com.dreamcloud.esa_score.analysis.CollectionInfo;
import com.dreamcloud.esa_score.analysis.TfIdfAnalyzer;
import com.dreamcloud.esa_score.analysis.TfIdfOptions;
import com.dreamcloud.esa_score.analysis.TfIdfStrategyFactory;
import com.dreamcloud.esa_score.analysis.strategy.TfIdfStrategy;
import com.dreamcloud.esa_score.cli.FileSystemScoringReader;
import com.dreamcloud.esa_score.cli.TfIdfOptionsReader;
import com.dreamcloud.esa_score.fs.DocumentScoreDataReader;
import com.dreamcloud.esa_score.fs.DocumentScoreMemoryReader;
import com.dreamcloud.esa_score.fs.TermIndex;
import com.dreamcloud.esa_score.fs.TermIndexReader;
import com.dreamcloud.esa_score.score.DocumentNameResolver;
import com.dreamcloud.esa_score.score.DocumentScoreReader;
import com.dreamcloud.esa_score.score.ScoreReader;
import com.dreamcloud.esa_tuner.cli.TuningOptionsReader;
import com.dreamcloud.esa_tuner.term.TermComparison;
import com.dreamcloud.esa_tuner.term.TermComparisonStats;
import org.apache.commons.cli.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    private static String SPEARMAN_OUT = "spearman-out";
    private static String SPEARMAN_COMPARE = "spearman-compare";
    private static String TERM_COMPARE = "term-compare";

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

        Option spearmanOutOption = new Option(null, SPEARMAN_OUT, true, "output CSV file of spearman correlations");
        spearmanOutOption.setRequired(false);
        options.addOption(spearmanOutOption);

        Option spearmanCompareOption = new Option(null, SPEARMAN_COMPARE, true, "spearman csv 1, spearman csv 2");
        spearmanCompareOption.setRequired(false);
        spearmanCompareOption.setArgs(2);
        options.addOption(spearmanCompareOption);

        Option termCompareOption = new Option(null, TERM_COMPARE, true, "term, spearman csv 1, spearman csv 2");
        termCompareOption.setRequired(false);
        termCompareOption.setArgs(3);
        options.addOption(termCompareOption);

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

            //Plain text tokenizer
            analyzerOptions.setTokenizerFactory(new TokenizerFactory() {
                public Tokenizer getTokenizer() {
                    return new StandardTokenizer();
                }
            });

            TfIdfStrategyFactory tfIdfFactory = new TfIdfStrategyFactory();
            TfIdfStrategy tfIdfStrategy = tfIdfFactory.getStrategy(tfIdfOptions);

            if (cli.hasOption("spearman")) {
                String spearman = cli.getOptionValue("spearman");
                if ("en-wordsim353".equals(spearman)) {
                    spearman = "./src/data/en-wordsim353.csv";
                }

                File outputFile = null;
                if (cli.hasOption(SPEARMAN_OUT)) {
                    outputFile = new File(cli.getOptionValue(SPEARMAN_OUT));
                }

                TfIdfAnalyzer tfIdfAnalyzer = new TfIdfAnalyzer(tfIdfStrategy, new EsaAnalyzer(analyzerOptions), fileSystemScoringReader.getCollectionInfo());
                VectorBuilder vectorBuilder = new VectorBuilder(fileSystemScoringReader.getScoreReader(), fileSystemScoringReader.getCollectionInfo(), tfIdfAnalyzer, analyzerOptions.getPreprocessor(), vectorOptions);
                TextVectorizer textVectorizer = new Vectorizer(vectorBuilder);

                DocumentSimilarity similarityTool = new DocumentSimilarity(textVectorizer);
                PValueCalculator calculator = new PValueCalculator(new File(spearman));
                ArrayList<DocumentPair> humanScores = calculator.readHumanScores();
                ArrayList<DocumentPair> esaScores = calculator.getEsaScores(humanScores, similarityTool);
                float spearmanScore = (float) calculator.getSpearmanCorrelation(humanScores, esaScores);

                if (outputFile != null) {
                    DocumentPairCsvWriter csvWriter = new DocumentPairCsvWriter(outputFile);
                    csvWriter.writePairs(esaScores);
                }

                System.out.println("Calculating P-value using Spearman correlation...");
                System.out.println("----------------------------------------");
                System.out.println("p-value:\t" + spearmanScore);
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

                TfIdfAnalyzer tfIdfAnalyzer = new TfIdfAnalyzer(tfIdfStrategy, new EsaAnalyzer(analyzerOptions), fileSystemScoringReader.getCollectionInfo());
                VectorBuilder vectorBuilder = new VectorBuilder(fileSystemScoringReader.getScoreReader(), fileSystemScoringReader.getCollectionInfo(), tfIdfAnalyzer, analyzerOptions.getPreprocessor(), vectorOptions);
                TextVectorizer textVectorizer = new Vectorizer(vectorBuilder);

                DocumentSimilarity similarityTool = new DocumentSimilarity(textVectorizer);
                PValueCalculator calculator = new PValueCalculator(new File(pearsonFile), documentFile);
                System.out.println("Calculating P-value using Pearson correlation...");
                System.out.println("----------------------------------------");
                System.out.println("p-value:\t" + calculator.getPearsonCorrelation(similarityTool));
                System.out.println("----------------------------------------");
            }

            else if (cli.hasOption(SPEARMAN_COMPARE)) {
                //Compare two spearman correlations to analyze the difference
                File spearman1 = new File(cli.getOptionValues(SPEARMAN_COMPARE)[0]);
                File spearman2 = new File(cli.getOptionValues(SPEARMAN_COMPARE)[1]);

                ArrayList<DocumentPair> spearman1Scores = DocumentPairCsvReader.readDocumentPairs(spearman1);
                ArrayList<DocumentPair> spearman2Scores = DocumentPairCsvReader.readDocumentPairs(spearman2);

                SpearmanComparison spearmanComparison = new SpearmanComparison(spearman1Scores, spearman2Scores);
                Map<String, Integer> sortedScoreDiffs = spearmanComparison.getDocsByRankDiff();
                for (String wordPair: sortedScoreDiffs.keySet()) {
                    System.out.println(wordPair + ": " + sortedScoreDiffs.get(wordPair));
                }
            }

            else if(cli.hasOption(TERM_COMPARE)) {
                String[] arguments = cli.getOptionValues(TERM_COMPARE);

                //Process term
                String term = arguments[0];
                Analyzer analyzer = new EsaAnalyzer(analyzerOptions);
                TokenStream tokens = analyzer.tokenStream("text", term);
                CharTermAttribute termAttribute = tokens.addAttribute(CharTermAttribute.class);
                try {
                    tokens.reset();
                    while(tokens.incrementToken()) {
                        term = termAttribute.toString();
                    }
                    tokens.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                //Build index1
                File idTitles1 = new File(arguments[1] + "/id-titles.txt");
                File termIndexFile1 = new File(arguments[1] + "/term-index.dc");
                File documentScoresFile1 = new File(arguments[1] + "/document-scores.dc");
                TermIndexReader termIndexReader1 = new TermIndexReader();
                termIndexReader1.open(termIndexFile1);
                TermIndex termIndex1 = termIndexReader1.readIndex();
                termIndexReader1.close();
                CollectionInfo collectionInfo1 = new CollectionInfo(termIndex1.getDocumentCount(), termIndex1.getAverageDocumentLength(), termIndex1.getDocumentFrequencies());

                DocumentScoreDataReader scoreDataReader1 = new DocumentScoreMemoryReader(documentScoresFile1);
                DocumentScoreReader scoreReader1 = new ScoreReader(termIndex1, scoreDataReader1);


                //Build index2
                File idTitles2 = new File(arguments[2] + "/id-titles.txt");
                File termIndexFile2 = new File(arguments[2] + "/term-index.dc");
                File documentScoresFile2 = new File(arguments[2] + "/document-scores.dc");
                TermIndexReader termIndexReader2 = new TermIndexReader();
                termIndexReader2.open(termIndexFile2);
                TermIndex termIndex2 = termIndexReader2.readIndex();
                termIndexReader2.close();
                CollectionInfo collectionInfo2 = new CollectionInfo(termIndex2.getDocumentCount(), termIndex2.getAverageDocumentLength(), termIndex2.getDocumentFrequencies());

                DocumentScoreDataReader scoreDataReader2 = new DocumentScoreMemoryReader(documentScoresFile2);
                DocumentScoreReader scoreReader2 = new ScoreReader(termIndex2, scoreDataReader2);

                TermComparison termComparison = new TermComparison(term);
                TermComparisonStats termComparisonStats = termComparison.compare(collectionInfo1, scoreReader1, collectionInfo2, scoreReader2);

                termComparisonStats.print(idTitles1, idTitles2);
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

                pValueCalculator.setOutputWordPairs(false);

                TfIdfAnalyzer tfIdfAnalyzer = new TfIdfAnalyzer(tfIdfStrategy, new EsaAnalyzer(analyzerOptions), fileSystemScoringReader.getCollectionInfo());
                DocumentScoreVectorBuilder vectorBuilder = new VectorBuilder(fileSystemScoringReader.getScoreReader(), fileSystemScoringReader.getCollectionInfo(), tfIdfAnalyzer, analyzerOptions.getPreprocessor(), vectorOptions);

                BackrubLinkMapReader linkMapReader = new BackrubLinkMapReader();
                linkMapReader.parse(new File("../esa-wiki/index/2022s/link-map.xml.bz2"));
                vectorBuilder = new BackrubVectorBuilder(vectorBuilder, linkMapReader.getLinkMap());
                TextVectorizer textVectorizer = new Vectorizer(vectorBuilder);

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
