from preprocessing import *
from embeddings_model import *
from index import *
import os

# Specify directory of resources folder (must contain IR2024 folder with documents.txt and queries.txt)
dir = "C:\\PythonProjects\\IR-Assignment-3\\resources\\"

def main():
    # If there are no preprocessed files, process them and save them
    docs_df = get_df(dir + "IR2024\\documents.txt", read=False, output_path=dir + "tables\\processed_docs.pkl")
    queries_df = get_df(dir + "IR2024\\queries.txt", read=False, output_path=dir + "tables\\processed_queries.pkl")

    # If there are already processed documents and queries read
    # docs_df = get_df(dir + "tables\\processed_docs.pkl", read=True)
    # queries_df = get_df(dir + "tables\\processed_queries.pkl", read=True)

    # If there are no saved models train and save a new model
    # model_name = "skipgram_300d_w5_epochs_25"
    # model = WordEmbeddingsModel(docs_df, 300, sg=1)
    # model.train(dir + f"models\\{model_name}.model", window=5, epochs=25)

    # If the model has been stored, load model (.model)
    # model_name = "skipgram_300d_w5_epochs_25"
    # model = WordEmbeddingsModel()
    # model.load_model(dir + f"models\\{model_name}.model")

    # Load saved pretrained weights (uncomment the 3 following lines and comment out the 3 previous lines) (.bin)
    # model_name = "custom_model"
    # model = WordEmbeddingsModel()
    # model.load_weights(dir + f"models\\{model_name}.bin")

    # Load saved pretrained weights (uncomment the 3 following lines and comment out the 3 previous lines) (.bin)
    model_name = "skipgram_300d_w5_epochs_25"
    model = WordEmbeddingsModel()
    model.load_weights(dir + f"models\\{model_name}.bin")

    index = Index(docs_df, model.get_model_weights())

    print('Analyzing results...')
    results = index.get_top_docs_for_queries(50, queries_df)
    os.makedirs("resources\\results", exist_ok=True) # if directory "results" does not exist create.
    results.to_csv(dir + f"results\\ir_results_{model_name}.txt", header=None, index=None, sep = ' ')

if __name__ == "__main__":
    main()