package com.aueb;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try {
            IndexBuilder builder = new IndexBuilder(args[0]);
            builder.buildIndex();
            ArrayList<Utils.QueriedDoc[]> res = builder.getDocsForQueries(args[1], 50);
            Evaluation eval = new Evaluation(res);
            eval.storeResults(args[2], args[3]);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}