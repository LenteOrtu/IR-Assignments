package com.aueb;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try {
            String directory = args[2];
            Float[] k1_values = new Float[]{0f, 1.2f, 1.5f, 1.8f, 2.0f, 3.0f};
            for (int b = 0; b < 5; b++) {
                for (int i = 0; i < k1_values.length; i++) {
                    IndexBuilder builder = new IndexBuilder(args[0], -1, b * 0.25f, k1_values[i]);
                    builder.buildIndex();
                    ArrayList<Utils.QueriedDoc[]> res = builder.getDocsForQueries(args[1], 50, -1,
                            b * 0.25f, k1_values[i]);
                    Evaluation eval = new Evaluation(res);
                    eval.storeResults(directory + "bm25_" + b + "_" + Math.round(k1_values[i] * 10)
                            + ".txt",  args[3]);
                }
            }

            Float[] lambda_values = new Float[]{0.0f, 0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 1.0f};
            for (int i = 0; i < lambda_values.length; i++) {
                IndexBuilder builder = new IndexBuilder(args[0], lambda_values[i], -1, -1);
                builder.buildIndex();
                ArrayList<Utils.QueriedDoc[]> res = builder.getDocsForQueries(args[1], 50, lambda_values[i],
                        -1, -1);
                Evaluation eval = new Evaluation(res);
                eval.storeResults(directory + "lmj_" + Math.round(lambda_values[i] * 10) + ".txt", args[3]);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}