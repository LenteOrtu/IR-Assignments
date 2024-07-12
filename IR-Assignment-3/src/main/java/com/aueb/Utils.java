package com.aueb;

import org.apache.lucene.document.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Utils {
    public static class QueriedDoc implements Comparable<QueriedDoc> {
        public float score;
        public String id;

        QueriedDoc(String id, float score) { this.id = id; this.score = score; }

        @Override
        public int compareTo(QueriedDoc o) {
            return -Float.compare(score, o.score);
        }
    }

    public static ArrayList<String> readFile(String path) throws IOException {
        ArrayList<String> contents = new ArrayList<>();

        String text = Files.readString(Paths.get(path));
        String[] fileLines = text.split("///");

        for (String line : fileLines)
            contents.add(line);

        return contents;
    }

    // Removes lines containing document id's, also removes '///' separator.
    public static String preProcessText(String inputPath) throws IOException {
        ArrayList<String> contents = readFile(inputPath);
        String outputPath = inputPath.replace(".txt", "-processed.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String s : contents) {
                int i = s.indexOf('\n', 2);
                String line = s.substring(i+1);
                writer.write(line);
                writer.newLine();
            }
        }

        return outputPath;
    }

    // Returns array containing the queries provided from queries.txt
    public static ArrayList<String> getQueryStrings(String path) throws IOException {
        ArrayList<String> contents = new ArrayList<>();
        File file = new File(path);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            contents.add(br.readLine()); // add query
            br.readLine(); // ignore ///
        }

        br.close();
        return contents;
    }
}










