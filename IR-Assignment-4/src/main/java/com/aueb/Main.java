package com.aueb;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.*;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    static class MultiSimilarityWithName {
        public Similarity multiSim;
        public String name;

        public MultiSimilarityWithName(Similarity sim1, Similarity sim2) {
            this.multiSim = new MultiSimilarity(new Similarity[] { sim1, sim2 });
            this.name = (sim1 + "_" + sim2).replace(".", "").replace(" ", "");
        }
    }

    public static void main(String[] args) {
        try {
            String directory = args[2];
            ArrayList<MultiSimilarityWithName> multiSimilarities = getMultiSimilarities();
            for (MultiSimilarityWithName multiSim : multiSimilarities) {
                IndexBuilder builder = new IndexBuilder(args[0], multiSim.multiSim);
                builder.buildIndex();
                ArrayList<Utils.QueriedDoc[]> res = builder.getDocsForQueries(args[1], 50, multiSim.multiSim);
                Evaluation eval = new Evaluation(res);
                eval.storeResults(directory + multiSim.name + ".txt", args[3]);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    static ArrayList<MultiSimilarityWithName> getMultiSimilarities() {
        ArrayList<MultiSimilarityWithName> multiSimilarities = new ArrayList<>();

        Float[] k1_values = new Float[]{1.8f, 2.0f, 3.0f};
        Float[] lambda_values = new Float[]{0.3f, 0.7f, 0.9f};
        Float[] b_values = new Float[]{0.0f, 0.25f, 0.5f};

        MultiSimilarityWithName multiSim;
        for (float k1 : k1_values) {
            for (float b : b_values) {
                multiSim = new MultiSimilarityWithName(new ClassicSimilarity(), new BM25Similarity(k1, b));
                multiSimilarities.add(multiSim);

                for (float lambda : lambda_values) {
                    multiSim = new MultiSimilarityWithName(
                            new BM25Similarity(k1, b), new LMJelinekMercerSimilarity(lambda));
                    multiSimilarities.add(multiSim);
                }
            }
        }
        for (float lambda : lambda_values) {
            multiSim = new MultiSimilarityWithName(new ClassicSimilarity(), new LMJelinekMercerSimilarity(lambda));
            multiSimilarities.add(multiSim);
        }

        return multiSimilarities;
    }
}