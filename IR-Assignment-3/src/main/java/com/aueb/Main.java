package com.aueb;

import org.apache.lucene.queryparser.classic.ParseException;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Preprocess documents collection text in order to train model on.
            String processedDocumentCollectionFilePath = Utils.preProcessText(args[0]);

            // Train model, store model, get reference to it.
            int layerSize = 50;
            int windowSize = 5;
            String architecture = "CBOW";
//            Word2VecModelBuilder word2VecModelBuilder = new Word2VecModelBuilder(layerSize, windowSize, architecture);
//            Word2Vec vec = word2VecModelBuilder.buildModelFromDocumentCollectionFile(processedDocumentCollectionFilePath);
//            Word2Vec vec = word2VecModelBuilder.loadModel();
//            word2VecModelBuilder.storeModel(vec);
//
            Word2Vec vec = WordVectorSerializer.readWord2VecModel("pretrained-wikipedia-model.txt");
            // Build index and specify WordEmbeddingSimilarity
            Index index = new Index(args[0], vec);
            index.buildIndex();

            // Retrieve top k ranked docs - QueriedDoc instances have accessible score in order for eval to create ir_results_file for each query.
            ArrayList<List<Utils.QueriedDoc>> topDocsPerQuery = index.getDocsForQueries(args[1], 50, WordEmbeddingsSimilarity.Smoothing.IDF  );

            // Create output files for trec_eval
            Evaluation eval = new Evaluation(topDocsPerQuery);
//            eval.storeResults("EVAL3_LS" + layerSize + "_WS" + windowSize +"_" + architecture + "_MEAN.txt", "69420");
            eval.storeResults("EVAL_PRETRAINED_WIKIPEDIA_IDF.txt", "69420");

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}




