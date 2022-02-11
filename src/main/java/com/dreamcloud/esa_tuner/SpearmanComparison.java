package com.dreamcloud.esa_tuner;

import java.util.*;

public class SpearmanComparison {
    private ArrayList<DocumentPair> pairs1;
    private ArrayList<DocumentPair> pairs2;

    public SpearmanComparison(ArrayList<DocumentPair> pairs1, ArrayList<DocumentPair> pairs2) {
        this.pairs1 = pairs1;
        this.pairs2 = pairs2;
    }

    public Map<String, Integer> getDocsByRankDiff() {
        pairs1.sort((DocumentPair p1, DocumentPair p2) -> Float.compare(p2.getScore(), p1.getScore()));
        pairs2.sort((DocumentPair p1, DocumentPair p2) -> Float.compare(p2.getScore(), p1.getScore()));

        Map<String, Integer> ranks = new HashMap<>();

        for (int pairIdx = 0; pairIdx < pairs1.size(); pairIdx++) {
            DocumentPair pair = pairs1.get(pairIdx);
            int otherPairIdx = 0;
            while (otherPairIdx < pairs2.size()) {
                DocumentPair otherPair = pairs2.get(otherPairIdx);
                if (otherPair.getDoc1().equals(pair.getDoc1()) && otherPair.getDoc2().equals(pair.getDoc2())) {
                    break;
                }
                otherPairIdx++;
            }

            int rankDiff = Math.abs(pairIdx - otherPairIdx);
            ranks.put(pair.getDoc1() + "_" + pair.getDoc2(), rankDiff);
        }

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        ranks.keySet().stream().sorted((String wordPair1, String wordPair2) -> Integer.compare(ranks.get(wordPair2), ranks.get(wordPair1))).forEach((String wordPair) -> {
            sortedMap.put(wordPair, ranks.get(wordPair));
        });

        return sortedMap;
    }
}
