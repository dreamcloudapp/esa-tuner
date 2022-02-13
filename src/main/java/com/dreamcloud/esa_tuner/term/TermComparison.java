package com.dreamcloud.esa_tuner.term;

import com.dreamcloud.esa_score.analysis.CollectionInfo;
import com.dreamcloud.esa_score.score.DocumentScoreReader;
import com.dreamcloud.esa_score.score.TfIdfScore;

import java.io.IOException;
import java.util.Vector;

public class TermComparison {
    private final String term;

    public TermComparison(String term) {
        this.term = term;
    }

    public TermComparisonStats compare(CollectionInfo indexInfo1, DocumentScoreReader scoreReader1,
     CollectionInfo indexInfo2, DocumentScoreReader scoreReader2) throws IOException {
        TermComparisonStats stats = new TermComparisonStats();
        stats.term = term;
        stats.docFreq1 = indexInfo1.getDocumentFrequency(term);
        stats.docFreq2 = indexInfo2.getDocumentFrequency(term);

        Vector<TfIdfScore> scores1 = new Vector<>();
        scoreReader1.getTfIdfScores(term, scores1);
        float score1Total = 0;
        for (TfIdfScore score: scores1) {
            score1Total += score.getScore();
            stats.scoreDocs1.put(score.getDocument(), (float) score.getScore());
        }
        stats.avgDocScore1 = score1Total / scores1.size();


        Vector<TfIdfScore> scores2 = new Vector<>();
        scoreReader2.getTfIdfScores(term, scores2);
        float score2Total = 0;
        for (TfIdfScore score: scores2) {
            score2Total += score.getScore();
            stats.scoreDocs2.put(score.getDocument(), (float) score.getScore());
        }
        stats.avgDocScore2 = score2Total / scores2.size();
        return stats;
    }
}
