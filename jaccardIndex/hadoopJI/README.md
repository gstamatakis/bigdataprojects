# Summary
This project calculates the Jaccard Index of (term,category) pairs of a news dataset.
Terms, categories and words appear in different files.
In order to join and group terms and categories a series of MapReduce cycles need to take place.
The classic (and naive) Map/Reduce side joins are avoided, instead we use a Per-Split SemiJoin algorithm which is 
properly described in the logproc.pdf paper.

# Datasets
The following dataset need to be downloaded and placed in the HDFS root directory (see below).

Both the 'terms' and a 'terms' dataset.
    
    rcv1-v2.topics.qrels"; 
    stem.termid.idf.map.txt

At least one of the following vector datasets.

    lyrl2004_vectors_test_pt0.dat
    lyrl2004_vectors_test_pt1.dat
    lyrl2004_vectors_test_pt2.dat
    lyrl2004_vectors_test_pt3.dat
    lyrl2004_vectors_train.dat

Reuters Datasets can be found here:
    
    http://www.ai.mit.edu/projects/jmlr/papers/volume5/lewis04a/lyrl2004_rcv1v2_README.htm
    
# Instructions
In order to build this maven project simply execute the package goal.

    mvn clean package

Place the all of the files in a folder with the name "dataset" (or change it in the args below) and run the uber jar created by maven. 

    hadoop jar logproc-1.0-SNAPSHOT.jar Jaccard hdfs://127.0.0.1:9000/root/ datasets 0 2 2 3 3 2 2 1
   
Main class name: Jaccard
1st path is the root directory (everything will be saved there).
"0" is the vector file. Can be anything from -1 to 4. 4 is for the TEST vector file and -1 loads EVERY part including the test vector file.

The following are the task arguments (AFTER '0')
1st and 2nd arg are the map and reduce tasks for the 1st phase.
3rd and 4th arg are the map and reduce tasks for the 2nd phase.
5th arg is for the tasks of the 3rd phase reducer.
6th arg is for the tasks of the 4rth phase.
7th arg is for the number of the reduce tasks of phase 6.

# Results can be fetched from HDFS
hadoop fs -get hdfs://127.0.0.1:9000/reuters/phase6/part-00000 .

