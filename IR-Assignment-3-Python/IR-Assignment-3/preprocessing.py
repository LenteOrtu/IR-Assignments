import keras.preprocessing
import nltk
import keras
import pandas as pd
import os

try:
    nltk.corpus.stopwords.words('english')
except:
    nltk.download('stopwords')
    nltk.download('wordnet')


def get_df(file_path, read=False, output_path=None):
    # If file has already been processed and stored.
    if read:
        return pd.read_pickle(file_path)

    # If not, process and store data table to output_path.
    print("Processing documents...")
    df = get_processed_data(file_path)
    os.makedirs("resources\\tables", exist_ok=True) # if directory "tables" does not exist create.
    df.to_pickle(output_path)
    return df

# parse documents.txt / queries.txt and get df containing (docId, tokens)-pairs/(queryId, tokens)-pairs
def get_processed_data(file_path):
    file = open(file_path, "r", encoding="utf8")
    docs = []

    curr_text = ""
    curr_doc_id = 0

    # Parse lines of documents.txt / queries.txt and retrieve (docId, doc-body)-pair / (queryId, query-body)-pair.
    for line in file.readlines():
        line = line.strip()
        if line.replace("Q", "").isdigit(): curr_doc_id = int(line.replace("Q", "")) # get queryId (e.x. Q01 -> 01) or docId(e.x. 193201)
        elif line == '///':
            docs.append([curr_doc_id, curr_text])
            curr_text = ""
        else:
            curr_text += line
    
    data_map = pd.DataFrame(docs, columns=['docID', 'tokens'])
    data_map['tokens'] = data_map['tokens'].apply(lambda doc: tokenize_seq(doc)) # tokenize body of doc/query.

    return data_map

# Tokenize body of text with lemmatization and stopword removal.
def tokenize_seq(seq):
    tokens = keras.preprocessing.text.text_to_word_sequence(seq)

    # stopwords = nltk.corpus.stopwords.words('english')
    # lemmatizer = nltk.WordNetLemmatizer()
    # tokens = [lemmatizer.lemmatize(token) for token in tokens if token not in stopwords]


    return tokens