#!/bin/bash

eval_results() {
  echo "-------------$1-------------" >> $2_map.txt
  trec_eval -q -m map qrels.txt $1.txt >> $2_map.txt

  for p in 5 10 15 20
  do
    echo "-------------$1-------------" >> $2_prec.txt
    trec_eval -q -m P.$p qrels.txt $1.txt >> $2_prec.txt
  done;
}

for k in 18 20 30
do
  for b in 00 05 025
  do
    for lambda in 0300000 0700000 0900000
    do
      fileName="BM25(k1=$k,b=$b)_LMJelinek-Mercer($lambda)"
      eval_results $fileName "lmj_bm25"
    done;

    fileName="ClassicSimilarity_BM25(k1=$k,b=$b)"
    eval_results $fileName "bm25_classic"
  done;
done;

for lambda in 0300000 0700000 0900000
do
  fileName="ClassicSimilarity_LMJelinek-Mercer($lambda)"
  eval_results $fileName "lmj_classic"
done;