package com.aueb;

import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;


import java.io.File;

public class Word2VecModelBuilder {

    private int layerSize;
    private int windowSize;
    private String architecture; // default is CBOW
    private final String modelPath;


    public Word2VecModelBuilder(int layerSize, int windowSize, String architecture) {
        this.layerSize = layerSize;
        this.windowSize = windowSize;
        this.architecture = architecture == null ? "CBOW" : architecture;
        this.architecture = !this.architecture.equals("SkipGram") ? "CBOW" : architecture;

        this.modelPath = this.architecture + "-LS" + layerSize + "-WS" + windowSize + ".txt";
    }

    public Word2Vec buildModelFromDocumentCollectionFile(String filepath) {
        LineSentenceIterator iter = new LineSentenceIterator(new File(filepath));

        Word2Vec vec = new Word2Vec.Builder()
                .layerSize(this.layerSize)
                .windowSize(this.windowSize)
                .epochs(3)
                .elementsLearningAlgorithm(this.architecture.equals("CBOW") ? new CBOW<>() : new SkipGram<>())
                .iterate(iter)
                .build();
        vec.fit();

       return vec;
    }

    public Word2Vec loadModel() {
        return WordVectorSerializer.readWord2VecModel(modelPath);
    }

    public void storeModel(Word2Vec vec) {
        WordVectorSerializer.writeWord2VecModel(vec, modelPath);
    }

    public void setLayerSize(int layerSize) {
        this.layerSize = layerSize;
    }


    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }


    public void setArchitecture(String architecture) {
        this.architecture = architecture == null ? "CBOW" : architecture;
        this.architecture = !this.architecture.equals("SkipGram") ? "CBOW" : architecture;
    }
}
