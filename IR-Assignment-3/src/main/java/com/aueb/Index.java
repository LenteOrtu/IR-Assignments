package com.aueb;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.QueryBuilder;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.reduce3.CosineSimilarity;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class Index {
    private final String path;
    private final Directory directory;
    private final IndexWriter indexWriter;

    private final Word2Vec vec;

    public Index(String filePath, Word2Vec vec) throws IOException {

        this.vec = vec;
        path = filePath;
        Analyzer analyzer = new WhitespaceAnalyzer();
        String indexPath = "/tmp/indexPath";
        Path path = FileSystems.getDefault().getPath(indexPath);
        directory = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(new ClassicSimilarity()); // needed in order to compute document related statistics (length normalization info)
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

            FieldType ft = new FieldType();
            ft.setStoreTermVectors(true); // in order for WordEmbeddingSimilarity score method to work
//            ft.setStoreTermVectorOffsets(true);
//            ft.setStoreTermVectorPayloads(true);
//            ft.setStoreTermVectorPositions(true); // probably not needed TODO REMOVE
            ft.setTokenized(true);
            ft.setStored((true)); // TODO REMOVE, ONLY USED FOR TESTING PURPOSES
            ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
            doc.add(new Field("body", body, ft));

//            doc.add(new TextField("body", body, Field.Store.NO));
            doc.add(new StoredField("id", id));
            indexWriter.addDocument(doc);
        }

        indexWriter.close();
    }

    // Returns hashmap containing (document id, doc vector representation)-pairs. Vec representation uses specified smoothing (MEAN, TF, TF_IDF, IDF)
    private HashMap<String, INDArray> getDocVectors(WordEmbeddingsSimilarity.Smoothing smoothing) throws IOException {
        DirectoryReader reader = DirectoryReader.open(directory);
        HashMap<String, INDArray> docVectors = new HashMap<>();

        for (int docId = 0; docId < reader.numDocs(); docId++) {
            if (docId % 1000 == 0){
                System.out.println(docId);
            }
            INDArray denseDocumentVector = VectorizeUtils.toDenseAverageVector(
                    reader.getTermVector(docId, "body"), reader.numDocs(), this.vec, smoothing);
            docVectors.put(reader.document(docId).get("id"), denseDocumentVector);
        }

        reader.close();

        return docVectors;
    }
//    // Returns hashmap containing (document id, doc vector representation)-pairs. Vec representation uses specified smoothing (MEAN, TF, TF_IDF, IDF)
//    private HashMap<String, INDArray> getDocVectors(WordEmbeddingsSimilarity.Smoothing smoothing) throws IOException {
//        DirectoryReader reader = DirectoryReader.open(directory);
//        HashMap<String, INDArray> docVectors = new HashMap<>();
//
//        for (int docId = 0; docId < reader.numDocs(); docId++) {
//            if (docId % 1000 == 0){
//                System.out.println(docId);
//            }
//            INDArray denseDocumentVector = VectorizeUtils.toDenseAverageVector(
//                    reader.getTermVector(docId, "body"), reader.numDocs(), this.vec, smoothing);
//            docVectors.put(reader.document(docId).get("id"), denseDocumentVector);
//        }
//
//        reader.close();
//
//        return docVectors;
//    }

    // Returns hashmap containing (document id, doc vector representation)-pairs. Vec representation uses specified smoothing (MEAN, TF, TF_IDF, IDF)
//    private HashMap<String, INDArray> getDocVectors(WordEmbeddingsSimilarity.Smoothing smoothing) throws IOException {
//        DirectoryReader reader = DirectoryReader.open(directory);
//        HashMap<String, INDArray> docVectors = new HashMap<>();
//        INDArray denseDocumentVector;
//
//        for (int docId = 0; docId < reader.numDocs(); docId++) {
//            denseDocumentVector = VectorizeUtils.toDenseAverageVector(
//                    reader.document(docId).get("body"), reader.numDocs(), this.vec, smoothing);
//            docVectors.put(reader.document(docId).get("id"), denseDocumentVector);
//        }
//
//        reader.close();
//
//        return docVectors;
//    }


    private INDArray getQueryVector(String queryString, WordEmbeddingsSimilarity.Smoothing smoothing) throws IOException {
        INDArray denseQueryVector = Nd4j.zeros(this.vec.getLayerSize());
        DirectoryReader reader = DirectoryReader.open(directory);
        String[] queryTerms = queryString.split(" ");
        Terms fieldTerms = MultiFields.getTerms(reader, "body");

        for (String queryTerm : queryTerms) {
            TermsEnum iterator = fieldTerms.iterator();
            BytesRef term;
            while ((term = iterator.next()) != null) {
                TermsEnum.SeekStatus seekStatus = iterator.seekCeil(term);
                if (seekStatus.equals(TermsEnum.SeekStatus.END)) {
                    iterator = fieldTerms.iterator();
                }
                if (seekStatus.equals(TermsEnum.SeekStatus.FOUND)) {
                    String string = term.utf8ToString();
                    if (string.equals(queryTerm)) {
                        INDArray vector = this.vec.getLookupTable().vector(queryTerm);
                        if (vector != null) {
                            double tf = iterator.totalTermFreq();
                            double docFreq = iterator.docFreq();
                            double smooth;
                            switch (smoothing) {
                                case MEAN:
                                    smooth = queryTerms.length;
                                    break;
                                case TF:
                                    smooth = tf;
                                    break;
                                case IDF:
                                    smooth = docFreq;
                                    break;
                                case TF_IDF:
                                    smooth = VectorizeUtils.tfIdf(reader.numDocs(), tf, docFreq);
                                    break;
                                default:
                                    smooth = VectorizeUtils.tfIdf(reader.numDocs(), tf, docFreq);
                            }
                            denseQueryVector.addi(vector.div(smooth));
                        }
                        break;
                    }
                }
            }
        }

        reader.close();

        return denseQueryVector;
    }

    public ArrayList<List<Utils.QueriedDoc>> getDocsForQueries(String queriesPath, int k, WordEmbeddingsSimilarity.Smoothing smoothing)
            throws IOException, ParseException {


        HashMap<String, INDArray> docVectors = getDocVectors(smoothing);
        ArrayList<List<Utils.QueriedDoc>> rankedDocsPerQuery = new ArrayList<>();

        for (String s : Utils.getQueryStrings(queriesPath)) {
            INDArray queryVector = getQueryVector(s, smoothing);
            rankedDocsPerQuery.add(getTopDocsForQuery(queryVector, docVectors, k));
        }

        return rankedDocsPerQuery;
    }


    private List<Utils.QueriedDoc> getTopDocsForQuery(INDArray queryVector, HashMap<String, INDArray> docVectors, int k) {
        List<Utils.QueriedDoc> queriedDocs = new ArrayList<>();
        System.out.println("QUERY");
        for (Map.Entry<String, INDArray> entry : docVectors.entrySet()) {
            float score = (float) Transforms.cosineSim(queryVector, entry.getValue());
            queriedDocs.add(new Utils.QueriedDoc(entry.getKey(), score));
        }
        Collections.sort(queriedDocs);
        return queriedDocs.subList(0, k);
    }
}










