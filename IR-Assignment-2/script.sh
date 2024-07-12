#!/bin/bash

for i in 0 1 3 5 7 9 10
do
    echo ------$i------ >> lmj_map.txt;
    trec_eval -q -m map qrels.txt lmj_$i.txt >> lmj_map.txt;

    for j in 5 10 15 20
    do
      echo ------$i------ >> lmj_prec.txt;
      trec_eval -q -m P.$j qrels.txt lmj_$i.txt >> lmj_prec.txt;
    done;
done;

for i in 0 1 2 3 4
do
    for j in 0 12 15 18 20 30
    do
      echo ------$i,$j------ >> bm25_map.txt;
      trec_eval -q -m map qrels.txt bm25_${i}_${j}.txt >> bm25_map.txt;

      for k in 5 10 15 20
      do
        echo ------$i,$j------ >> bm25_prec.txt;
        trec_eval -q -m P.$k qrels.txt bm25_${i}_${j}.txt >> bm25_prec.txt;
      done;
    done;
done
