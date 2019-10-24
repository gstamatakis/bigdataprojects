# Instructions 
In order to launch the requested container(s) simply cd into the correct directory and run
the following instructions to spin up/down the necessary containers.

    docker-compose up
    
    docker-compose down

## Containers
Most docker-compose files contain multiple services.

* Confluent Platform
    * Kafka
    * Zookeeper
    * Registry
    * Connect
    * Control Center
    * KSQL server
    * REST proxy
* ELK stack
    * Elasticsearch
    * Kibana
    * Logstash
    * Beat
* NiFi
    * Zookeeper
    * NiFi service
* Hive
    * Hadoop Namenode/Datanode
    * Hive
    * Presto coordinator