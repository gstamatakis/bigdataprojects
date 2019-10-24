# CM Sketches and distributed Bloom filters

### Arguments (in order of appearance)
m: Array Size
n: Number of unique elements
k: Number of Hash Functions
Q: Query Spout hint
D: Data Spout hint
L: Lookup Bolt hint
S: Sink Bolt(HDFS bolt) hint
q: Query (0 for Bloom, 1 for CM)
HDFS Root: The hdfs directory that contains the input folder and the queries.txt
dataFolder: The name of the folder that contains the data file(s). The reason a folder is required is because HDFS bolt only accepts a folder path as an arg.
TD: Tuple delay (in ms), used in query spout to "slow down" query tuples.
Cluster: Use one of the following modes: local,distributed,remote
         local -> submit topology in a LocalCluster (local mode)
         distributed -> stormSubmit without setting any nimbus/zookeeper IP/port  (distributed mode)
         remote -> stormSubmit but requires cluster IP/port


Distributed / Local
                                                          
                                                          m      n    k Q D L S q    HDFS Root          dataFolder queryFile  TD Time  Cluster mode

    storm jar target/storm2-1.0-SNAPSHOT.jar MyTopology 663456 100000 7 1 1 2 1 1 hdfs://147.27.70.106:9000 input queries.txt 10 120000 distributed

Remote                                                         

                                                               m      n    k Q D L S q    HDFS Root          dataFolder queryFile  TD Time  Cluster   IP         Port
    storm jar target/storm2-1.0-SNAPSHOT.jar MyTopology 445237219 67108864 7 1 1 2 1 0 hdfs://147.27.70.106:9000 input queries.txt 0 180000 remote 147.27.70.106 49627


WARNING!
The result will be in the output folder of the same directory.
The data file(s) MUST be inside a folder (the folder name is given as an arg) because 
HDFS spout only accepts an HDFS folder as an arg.