package com.aueb;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Evaluation {
    private ArrayList<List<Utils.QueriedDoc>> resultsPerQuery;

    Evaluation(ArrayList<List<Utils.QueriedDoc>> resultsPerQuery) { this.resultsPerQuery = resultsPerQuery; }

    public void storeResults(String output_path, String run_id) throws IOException {
        FileWriter fileWriter = new FileWriter(output_path);

        // Query id = i
        for (int i = 1; i <= resultsPerQuery.size(); i++) {
            for (Utils.QueriedDoc doc : resultsPerQuery.get(i-1)) {
                String query_id = "Q" + (i <= 9 ? "0" : "") + i;
                String line = query_id + " 0 " + doc.id.trim() + " 0 " + doc.score + " " + run_id + "\n";
                fileWriter.write(line);
            }
        }
        fileWriter.close();
        System.out.println("Successfully stored");
    }
}
