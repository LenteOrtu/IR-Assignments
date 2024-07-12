import pandas as pd
from gensim.models import Word2Vec, KeyedVectors
from sklearn.metrics.pairwise import cosine_similarity
import logging
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

class Index:
    def __init__(self, data=None, model_weights=None):
        self.doc_data = data
        self.model_weights = model_weights 

    # Create vector representation for documents/queries. (Smoothing method: MEAN)
    def fit(self, data=None):
        if data is None:
            data = self.doc_data

        data['vector'] = data['tokens'].apply(lambda tokens: self.model_weights.get_mean_vector(tokens))
        return data
    
    def get_top_results_for_query(self, k, docs, query_df):
        # Calculate score for each (query_df, doc)-pair.
        docs['score'] = docs['vector'].apply(lambda vec: cosine_similarity(
            [vec, query_df['vector'].iloc[0]])[0, 1])
        
        # get top k highest scored docs for query.
        top_docs = docs.nlargest(k, 'score')[['score', 'docID']]

        # create table repr for trec_eval (q_id , iter, docno, rank, sim, run_id ).
        df = top_docs.merge(query_df.rename(columns={'docID': 'qID'}), how='cross')
        df['qID'] = df['qID'].apply(lambda id: 'Q' + ('0' if id < 10 else '') + str(id))
        df['empty1'] = 0
        df['empty2'] = 0
        df['name'] = 'wordEmbeddingsMethod'
        return df[['qID', 'empty1', 'docID', 'empty2', 'score', 'name']]
        
    def get_top_docs_for_queries(self, k, queries_df):
        # if vector representation of docs has not been created
        if 'vector' not in self.doc_data:
            docs = self.fit(self.doc_data) # create vec representation for each doc. 

        # if vector representation of queries has not been created
        if 'vector' not in queries_df:
            queries_df = self.fit(queries_df) # create vec representation for each query. 

        all_dfs = []

        # Get top k docs for each query.
        for i in range(len(queries_df)):
            tmp_df = pd.DataFrame(queries_df[['docID', 'vector']].iloc[i]).T
            all_dfs.append(self.get_top_results_for_query(k, docs, tmp_df))
        
        results = pd.concat(all_dfs)
        return results