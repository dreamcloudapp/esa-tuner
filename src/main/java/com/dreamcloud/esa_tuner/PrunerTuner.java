package com.dreamcloud.esa_tuner;

import com.dreamcloud.esa_core.similarity.DocumentSimilarity;
import com.dreamcloud.esa_core.vectorizer.VectorBuilder;
import com.dreamcloud.esa_core.vectorizer.VectorizationOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrunerTuner {
    protected DocumentSimilarity similarity;

    public PrunerTuner(DocumentSimilarity similarity) {
        this.similarity = similarity;
    }

    public PrunerTuning tune(PValueCalculator pValueCalculator, TuningOptions options, VectorizationOptions vectorizationOptions) throws Exception {
        AtomicBoolean skipWindow = new AtomicBoolean(false);
        Thread skipThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine();
                if ("s".equals(line)) {
                    System.out.println("skipping (user)");
                    skipWindow.set(true);
                }
            }
        });
        skipThread.setDaemon(true);
        skipThread.start();
        float initialDropOffStart = options.getStartingWindowDrop();

        int windowStart = options.getStartingWindowSize();
        int windowEnd = options.getEndingWindowSize();
        int windowStep = options.getWindowSizeStep();

        float dropOffStart = options.getStartingWindowDrop();
        float dropOffEnd = options.getEndingWindowDrop();
        float dropOffStep = options.getWindowDropStep();

        int vectorLimitStart = options.getStartingVectorLimit();
        int vectorLimitEnd = options.getEndingVectorLimit();

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(8);
        format.setMinimumFractionDigits(8);

        double bestScore = 0;
        int bestWindowSize = 0;
        float bestDropOff = 0;
        float bestVectorLimit = 0;

        //Tuning the window size and dropoff
        if (windowStart != windowEnd || dropOffStart != dropOffEnd) {
            int iterationIdx = 0;
            int iterationCount = (((windowEnd - windowStart) / windowStep) + 1) * (int) (((dropOffEnd - dropOffStart) / dropOffStep) + 1);

            while (windowStart <= windowEnd) {
                dropOffStart = initialDropOffStart;
                ArrayList<Double> lastScores = new ArrayList<>();
                while (dropOffStart <= dropOffEnd) {
                    if (skipWindow.get()) {
                        skipWindow.set(false);
                        break;
                    }

                    //Change prune options (same object as in the pvalue calculator!)
                    vectorizationOptions.setWindowSize(windowStart);
                    vectorizationOptions.setWindowDrop(dropOffStart);
                    //hacky shmack
                    VectorBuilder.cache.clear();


                    double score;
                    if ("spearman".equals(options.getType())) {
                        score = pValueCalculator.getSpearmanCorrelation(similarity);
                    } else {
                        score = pValueCalculator.getPearsonCorrelation(similarity);
                    }
                    lastScores.add(score);
                    if (lastScores.size() > 10) {
                        lastScores.remove(0);
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestWindowSize = windowStart;
                        bestDropOff = dropOffStart;
                        System.out.println("!!! best score !!!");
                    }

                    //If we have 10 scores, check to see if 7 of the 10 are lower than the previous ones
                    if (lastScores.size() == 10) {
                        int lowerScores = 0;
                        for (int scoreIdx = 1; scoreIdx < 10; scoreIdx++) {
                            double currentScore = lastScores.get(scoreIdx);
                            double previousScore = lastScores.get(scoreIdx - 1);
                            if (currentScore < previousScore) {
                                lowerScores++;
                            }
                        }
                        if (lowerScores >= 8) {
                            System.out.println("skipping (9)");
                            iterationIdx += (int) (((dropOffEnd - dropOffStart) / dropOffStep) + 1);
                            break;
                        }
                    }

                    System.out.println(format.format(score) + ": " + windowStart + "/" + format.format(dropOffStart) + "\t[" + iterationIdx + "|" + iterationCount + "]\tbest: " + format.format(bestScore));
                    dropOffStart += dropOffStep;
                    iterationIdx++;
                }
                windowStart += windowStep;
            }
        } else if(vectorLimitStart != vectorLimitEnd) {
            //Tuning the vector limit
            while (vectorLimitStart <= vectorLimitEnd) {
                vectorizationOptions.setVectorLimit(vectorLimitStart);
                VectorBuilder.cache.clear();
                double score;
                if ("spearman".equals(options.getType())) {
                    score = pValueCalculator.getSpearmanCorrelation(similarity);
                } else {
                    score = pValueCalculator.getPearsonCorrelation(similarity);
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestVectorLimit = vectorLimitStart;
                    System.out.println("!!! best score !!!");
                }
               vectorLimitStart += options.getVectorLimitStep();
            }
        } else {
            System.out.println("warning: nothing to tune");
        }

        return new PrunerTuning(bestScore, bestWindowSize, bestDropOff, bestVectorLimit);
    }
}
