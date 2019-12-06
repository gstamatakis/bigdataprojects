# Projects on various BigData platforms 

## Projects
This repository hosts the following projects, more can be found on each projects individual README.

Clinking one of the following links takes you directly to the projects module. 

[Skyline operator implemented in HadooopMR](skyline/hadoopSkyline).

[Distributed Bloom Filter and Count-Min sketches in Apache Storm](sketches/BloomCountMinStorm).

[Scheduling workloads in Spark, Flink, Apex and GPUs based on various metrics](beamScheduler).

[Calculating the Jaccard Index of terms and categories using a Per-Split SemiJoin algorithm in HadoopMR](jaccardIndex/hadoopJI).


## Used frameworks
Links redirect to each framework's download page.

[Apache Spark](https://spark.apache.org/downloads.html)

[Apache Storm](https://storm.apache.org/downloads.html)

[Apache Flink](https://flink.apache.org/downloads.html)

[Apache Hadoop](https://hadoop.apache.org/releases.html)

[Apache Hive](https://www-eu.apache.org/dist/hive/)

[Apache Kafka](https://kafka.apache.org/downloads)

[Apache NiFi](https://nifi.apache.org/download.html)

[Elasticsearch (entire ELK stack)](https://www.elastic.co/downloads/)

## Docker
The [docker](docker) folder in the root directory contains various docker-compose.yml files for some
of the Frameworks used in these projects. Docker is extremely powerful when complex networking is 
involved or rapid prototyping is necessary. 

# Structure
Inside each module there may be more submodules, usually one for each implementation (eg. Spark,Hadoop,...)

# Building
This repository uses Maven3 to build its submodules.
In order to build <b>all</b> of the submodules simply run the following from the root of this repo.

    mvn clean package
    
Inside each submodule there will be a target directory with the module's uberjar.

To build just a single artifact (eg. The hadoop implementation of the skyline) simply:
    
    mvn clean pacakge -pl :hadoopSkyline
