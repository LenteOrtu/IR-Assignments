import pandas as pd
from gensim.models import Word2Vec, KeyedVectors
from sklearn.metrics.pairwise import cosine_similarity
import logging
import os
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

class WordEmbeddingsModel:
    def __init__(self, data=None, size=None, model=None, sg=0):
        self.doc_data = data
        self.size = size
        self.model_weights = model.wv if model else None 
        self.sg = sg # sg = 0 CBOW, sg = 1 SkipGram

    def store_weights(self, output_path):
        self.model_weights.save_word2vec_format(output_path, binary=False)
        print("Successfully stored weights")
    
    def store_bin(self, output_path):
        self.model_weights.save_word2vec_format(output_path, binary=True)
        print("Successfully stored bin")

    # Loads weights of pretrained model.
    def load_weights(self, input_path, binary=True):
        self.model_weights = KeyedVectors.load_word2vec_format(input_path, binary=binary)
        print("Successfully loaded weights")

    def load_model(self, model_path):
        model = Word2Vec.load(model_path)
        self.model_weights = model.wv
        print("Successfully loaded model")

    # Train model on doc data and store.
    def train(self, output_path=None, window=3, epochs=10):
        print("Training model...")
        model = Word2Vec(self.doc_data['tokens'], vector_size=self.size, min_count=5,
                         window=window, negative=15, epochs=epochs, workers=5, sg=self.sg)

        if output_path is not None:
            os.makedirs("resources\\models", exist_ok=True) # if directory "models" does not exist create.
            model.save(output_path)
        self.model_weights = model.wv
    
    def get_model_weights(self):
        return self.model_weights