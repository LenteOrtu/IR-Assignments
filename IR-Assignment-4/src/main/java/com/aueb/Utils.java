package com.aueb;

import org.apache.lucene.document.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Utils {
    public static class QueriedDoc {
        public float score;
        public Document doc;

        QueriedDoc(Document doc, float score) { this.doc = doc; this.score = score; }
    }

    public static ArrayList<String> readFile(String path) throws IOException {
        ArrayList<String> contents = new ArrayList<>();

        String text = Files.readString(Paths.get(path));
        String[] fileLines = text.split("///");

        for (String line : fileLines)
            contents.add(line);

        return contents;
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
