package com.aueb;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class IndexBuilder {
    private final String path;
    private final Directory directory;
    private final IndexWriter indexWriter;

    public IndexBuilder(String filePath) throws IOException {
        path = filePath;
        Analyzer analyzer = new EnglishAnalyzer();
        String indexPath = "/tmp/indexPath";
        Path path = FileSystems.getDefault().getPath(indexPath);
        directory = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(new ClassicSimilarity());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // override existing index.

        indexWriter = new IndexWriter(directory, config);
    }

    public void buildIndex() throws IOException {
        ArrayList<String> documentsStr = Utils.readFile(path);

        for (String s : documentsStr) {
            Document doc = new Document();
            int i = s.indexOf('\n', 2);
            String id = s.substring(0, i).trim(); // get document id
            String body = s.substring(i+1);

            doc.add(new TextField("body", body, Field.Store.NO));
            doc.add(new StoredField("id", id));
            indexWriter.addDocument(doc);
        }

        indexWriter.close();
    }

    public ArrayList<Utils.QueriedDoc[]> getDocsForQueries(String queriesPath, int k) throws IOException, ParseException {
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new ClassicSimilarity());
        Analyzer analyzer = new EnglishAnalyzer();
        QueryParser parser = new QueryParser("body", analyzer);

        ArrayList<ScoreDoc[]> topDocsPerQuery = new ArrayList<>();
        for (String s : Utils.readFile(queriesPath)) {
            Query query = parser.parse(s);
            TopDocs results = searcher.search(query, k);
            ScoreDoc[] hits = results.scoreDocs;
            topDocsPerQuery.add(hits);
        }

        ArrayList<Utils.QueriedDoc[]> rankedDocsPerQuery = new ArrayList<>();
        for (ScoreDoc[] hits : topDocsPerQuery) {
            Utils.QueriedDoc[] docs = new Utils.QueriedDoc[hits.length];
            for (int i = 0; i < hits.length; i++) {
                Document doc = searcher.doc(hits[i].doc);

                docs[i] = new Utils.QueriedDoc(doc, hits[i].score);
            }
            rankedDocsPerQuery.add(docs);
        }

        reader.close();

        return rankedDocsPerQuery;
    }
}