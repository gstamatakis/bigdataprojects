import CoreUtils.Histogram;
import CoreUtils.Requests;
import Flink.*;
import LSH.Index;
import LSH.Vector;
import LSH.families.CosineHashFamily;
import LSH.families.HashFamily;
import Yarn.App;
import Yarn.YARNcontroller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static CoreUtils.ClusterConstants.*;

/**
 * Class used to read and sent queries to Beam pipelines based on metrics gathered by the resource managers.
 */
@SuppressWarnings({"Duplicates", "StringConcatenationInLoop", "unused", "SameParameterValue"})
public class MyController {
    private static Map<String, Double> boost;

    private static final Random random = new Random();
    private static int query_execution_interval = 20000; //Emmit new queries every N ms

    //Delays and periods of timers
    private static final long FlinkRefreshPeriod = 10000;
    private static final long YarnRefreshPeriod = 10000;
    private static final long FlinkDelay = 0;
    private static final long YarnDelay = 0;

    //Objects that collect and store framework metrics
    private static FlinkParser flinkParser;
    private static Jobmanager jobmanager;
    private static List<App> yarnApps;
    private static List<FlinkJob> flinkJobs;

    //List of the available fw names
    private static List<String> fw_list = new ArrayList<>();

    //Kafka streams
    private static String[] kstream_URLs;   //host1:port1,host2:port2,...,host6,port6
    private static KSMetrics[] kStreamsMetrics;    //Metrics from kafka streams (1 for each stream)

    //Latency and throughput metrics of Beam
    private static Map<String, Double> beam_lat_map;
    private static Map<String, Double> beam_thr_map;

    //Keep log files of metrics (thr,lat) as well as query choices eg. query 42 assigned to spark.
    private static Map<String, BufferedWriter> metrics_writer;
    private static BufferedWriter queries_assigned;

    //Keep  track of the Vectors for each query for each framework.
    private static Map<String, Map<String, List<Vector>>> query_fw_vectors;

    //Active queries for each framework
    private static Map<String, List<String>> fw_active_queries;
    private static final int LSH_DIMS = 17;

    //Sends queries to frameworks and KStreams
    private static Producer<String, String> query_producer;
    private static KafkaConsumer<String, String> metrics_consumer;

    private static int RRcnt = 0;

    //    private static HashFamily family = new EuclidianHashFamily(1, LSH_DIMS);
    //    private static HashFamily family = new CityBlockHashFamily(1, LSH_DIMS);
    private static HashFamily family = new CosineHashFamily(LSH_DIMS);

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: [kafka streams URLs (host:port)] [query filename]");
            return;
        }

        //Frameworks used
        fw_list.add("spark");
        fw_list.add("flink");
        fw_list.add("apex");

        //Boost options
        boost = new HashMap<>();
        boost.put("flink", 0.0);
        boost.put("spark", 0.0);
        boost.put("apex", 0.0);

        //Kafka streams URLs
        kstream_URLs = args[0].split(",");
        for (String url : kstream_URLs) {
            System.out.println("Monitoring: " + url);
        }

        //Query text file name
        String query_fname = args[1];

        //Metrics from Kafka Streams queryable state
        kStreamsMetrics = new KSMetrics[numOfStreams];

        //Sends queries to fw
        Properties query_prod_props = new Properties();
        query_prod_props.put("bootstrap.servers", "rserver07:6667");
        query_prod_props.put("linger.ms", 50);   //Wait UP TO this many ms for a batch to be filled, else sent a half-finished batch.
        query_prod_props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        query_prod_props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        //Sends queries
        query_producer = new KafkaProducer<>(query_prod_props);

        //Init throughput/latency maps
        beam_thr_map = new HashMap<>();
        for (String fw : fw_list) {
            beam_thr_map.put(fw, 0.0);
        }
        beam_lat_map = new HashMap<>();
        for (String fw : fw_list) {
            beam_lat_map.put(fw, 99999.0);
        }

        //Active queries per fw
        fw_active_queries = new HashMap<>();
        for (String fw : fw_list) {
            fw_active_queries.put(fw, new ArrayList<>());
        }

        //Store the LSH vectors for each query and for each framework
        //By default this structure contains all queries and all frameworks but has no vectors.
        query_fw_vectors = new HashMap<>();

        for (int i = 1; i <= query_num; i++) {  //Queries start from '1'
            Map<String, List<Vector>> fw_map = new HashMap<>();
            for (String fw : fw_list) {
                fw_map.put(fw, new ArrayList<>());
            }
            query_fw_vectors.put(String.valueOf(i), fw_map);
        }

        //Log the metrics of each framework in files
        metrics_writer = new HashMap<>();
        for (String fw : fw_list) {
            metrics_writer.put(fw, new BufferedWriter(new FileWriter("metrics" + fw + ".csv", false)));          //Init the files containing the fw metrics
            metrics_writer.get(fw).write("\n" + System.currentTimeMillis() + "\n");
            metrics_writer.get(fw).flush();
        }
        queries_assigned = new BufferedWriter(new FileWriter("assigned_queries.csv", false));


        //Object used to handle data from REST api calls.
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        //Used for JSON deserialization.
        flinkParser = new FlinkParser(flink_master, objectMapper);

        //Flink metrics collected by this periodic task
        Timer flink_timer = new Timer("FlinkMetricsTimer");
        flink_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Connect to Flink JM and retrieve stats.
                jobmanager = flinkParser.fetchFlinkJobManager();
                flinkJobs = flinkParser.fetchFlinkJobs(null, "RUNNING");
                for (FlinkJob job : flinkJobs) {
//                    System.out.println("Monitoring flink job:" + job.jid);
                }
            }
        }, FlinkDelay, FlinkRefreshPeriod);

        //YARN job metrics (For Spark AND Apex)
        Timer yarn_timer = new Timer("YarnMetricsTimer");
        yarn_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                yarnApps = YARNcontroller.getYarnApps(yarn_master, objectMapper, "gstamatakis", 0, "RUNNING");
                for (App app : yarnApps) {
//                    System.out.println("Monitoring yarn job: " + app.id);
                }
            }
        }, YarnDelay, YarnRefreshPeriod);



        /*
          Gets throughput and latency metrics from the BEAM metric sinks.
         */
        Properties beam_metrics_props = new Properties();
        beam_metrics_props.put("bootstrap.servers", "rserver07:6667");
        beam_metrics_props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        beam_metrics_props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        beam_metrics_props.put("auto.offset.reset", "latest");
        beam_metrics_props.put("group.id", "myController");
        metrics_consumer = new KafkaConsumer<>(beam_metrics_props);
        metrics_consumer.subscribe(Arrays.asList("g11", "g12"));
        Timer timer = new Timer("MetricsTimer");
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                fetchBeamMetrics();
                fetchKafkaMetrics();
            }
        }, 0, 5000);

        sleepMs(15000);//Wait for other threads to init properly.

        //Save frameworks and their queries before sending them
        Map<String, List<String>> fw_queries = new HashMap<>();
        for (int i = 0; i < fw_size; i++) {
            fw_queries.put(fw_list.get(i), new ArrayList<>());
        }

        //Stage 1. Warm up by running each query a few times in EVERY framework.
        for (int i = 1; i <= 15; i++) {
            System.out.println("Query: " + i);
            clearActiveQueries();
            String qid = String.valueOf(i);
            String query = String.valueOf((i % 5) + 1);
            String msg = "";
            msg += String.format("%s-%s_%s|", "spark", qid, query);
            Vector sparkVec = genQueryVector("spark");
            msg += String.format("%s-%s_%s|", "flink", qid, query);
            Vector flinkVec = genQueryVector("flink");
            msg += String.format("%s-%s_%s", "apex", qid, query);
            Vector apexVec = genQueryVector("apex");
            send_query_vector(msg);
            System.out.println("time: " + System.currentTimeMillis() + " qid: " + qid + " with query vector: " + msg);

            sleepMs(query_execution_interval);

            //Store the vector
            query_fw_vectors.get(query).get("spark").add(sparkVec);
            query_fw_vectors.get(query).get("flink").add(flinkVec);
            query_fw_vectors.get(query).get("apex").add(apexVec);
        }

        //Send pairs of queries x2 and then x3
        int cur_query = 0;
        for (int i = 16; i < 60; i += 3) { //15-39
            String msg = "";
            msg += String.format("%s-%s_%s|", "spark", "1", "1");
            msg += String.format("%s-%s_%s|", "flink", "1", "1");
            msg += String.format("%s-%s_%s", "apex", "1", "1");
            send_query_vector(msg);
            sleepMs(5000);


            System.out.println("Query: " + i);
            clearActiveQueries();
            for (List<String> list : fw_queries.values()) { //Clear the fw and their queries
                list.clear();
            }

            String qid = String.valueOf(i);
            String query1 = String.valueOf((cur_query++ % 5) + 1);
            String fw1 = decide(query1);   //Creates internally a new Vector with current stats. Decides after clustering the fw. Does NOT store any vectors.
            fw_queries.get(fw1).add(qid + "_" + query1);

            qid = String.valueOf(i + 1);
            String query2 = String.valueOf((cur_query++ % 5) + 1);
            String fw2 = decide(query2);
            fw_queries.get(fw2).add(qid + "_" + query2);

            qid = String.valueOf(i + 2);
            String query3 = String.valueOf((cur_query++ % 5) + 1);
            String fw3 = decide(query3);
            fw_queries.get(fw3).add(qid + "_" + query3);


            String query_msg = "";
            for (String fw : fw_list) {
                if (fw_queries.get(fw).isEmpty()) {//If this framework has no queries just go to the next one.
                    continue;
                }
                query_msg += fw + "-";
                for (String q : fw_queries.get(fw)) {
                    query_msg += q + " ";
                }
                query_msg = query_msg.substring(0, query_msg.length() - 1);//Remove the last space char
                query_msg += "|";//For the next fw
            }
            String send = query_msg.substring(0, query_msg.length() - 1);//Remove the last '|' char
            send_query_vector(send);
            System.out.println("time: " + System.currentTimeMillis() + " qid: " + qid + " with query vector: " + send);

            sleepMs(query_execution_interval);

            query_fw_vectors.get(query1).get(fw1).add(genQueryVector(fw1));
            query_fw_vectors.get(query2).get(fw2).add(genQueryVector(fw2));
            query_fw_vectors.get(query3).get(fw3).add(genQueryVector(fw3));
        }

        cur_query = 0;
        for (int i = 60; i < 100; i += 5) {
            String msg = "";
            msg += String.format("%s-%s_%s|", "spark", "1", "1");
            msg += String.format("%s-%s_%s|", "flink", "1", "1");
            msg += String.format("%s-%s_%s", "apex", "1", "1");
            send_query_vector(msg);
            sleepMs(5000);


            System.out.println("Query: " + i);
            clearActiveQueries();

            for (List<String> list : fw_queries.values()) {
                list.clear();
            }

            String qid = String.valueOf(i);
            String query1 = String.valueOf((cur_query++ % 5) + 1);
            String fw1 = decide(query1);
            fw_queries.get(fw1).add(qid + "_" + query1);

            qid = String.valueOf(i + 1);
            String query2 = String.valueOf((cur_query++ % 5) + 1);
            String fw2 = decide(query2);
            fw_queries.get(fw2).add(qid + "_" + query2);

            qid = String.valueOf(i + 2);
            String query3 = String.valueOf((cur_query++ % 5) + 1);
            String fw3 = decide(query3);
            fw_queries.get(fw3).add(qid + "_" + query3);

            msg = "";
            msg += String.format("%s-%s_%s|", "spark", "1", "1");
            msg += String.format("%s-%s_%s|", "flink", "1", "1");
            msg += String.format("%s-%s_%s", "apex", "1", "1");
            send_query_vector(msg);
            sleepMs(5000);

            qid = String.valueOf(i + 3);
            String query4 = String.valueOf((cur_query++ % 5) + 1);
            String fw4 = decide(query4);
            fw_queries.get(fw4).add(qid + "_" + query4);

            qid = String.valueOf(i + 4);
            String query5 = String.valueOf((cur_query++ % 5) + 1);
            String fw5 = decide(query5);
            fw_queries.get(fw5).add(qid + "_" + query5);


            String query_msg = "";
            for (String fw : fw_queries.keySet()) {
                if (fw_queries.get(fw).isEmpty()) {//If this framework has no queries just go to the next one.
                    continue;
                }
                query_msg += fw + "-";
                for (String q : fw_queries.get(fw)) {
                    query_msg += q + " ";
                }
                query_msg = query_msg.substring(0, query_msg.length() - 1);//Remove the last space char
                query_msg += "|";//For the next fw
            }
            String send = query_msg.substring(0, query_msg.length() - 1);//Remove the last '|' char
            send_query_vector(send);
            System.out.println("time: " + System.currentTimeMillis() + " qid: " + qid + " with query vector: " + send);

            sleepMs(query_execution_interval);

            query_fw_vectors.get(query1).get(fw1).add(genQueryVector(fw1));
            query_fw_vectors.get(query2).get(fw2).add(genQueryVector(fw2));
            query_fw_vectors.get(query3).get(fw3).add(genQueryVector(fw3));
            query_fw_vectors.get(query4).get(fw4).add(genQueryVector(fw4));
            query_fw_vectors.get(query5).get(fw5).add(genQueryVector(fw5));
        }

        System.exit(0);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            metrics_consumer.close();
            query_producer.close();
            try {
                queries_assigned.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String fw : fw_list) {
                try {
                    metrics_writer.get(fw).close();
                } catch (IOException e) {
                    System.out.println("At shutdown hook.");
                    e.printStackTrace();
                }
            }
            System.out.println("Finished!");
        }));

    }

    private static void send_query_vector(String msg) {
        for (int i = 0; i < 1; i++) { //"Broadcast"
            query_producer.send(new ProducerRecord<>("g9", null, msg));
        }
    }

    /*
     *  Given a framework and its metrics generate a query vector.
     */
    private static Vector genQueryVector(String fw) {
        Vector vector = new Vector(LSH_DIMS);
        vector.set(0, kStreamsMetrics[0].getKurtosis());
        vector.set(1, kStreamsMetrics[0].getSkewness());
        vector.set(2, kStreamsMetrics[0].getAvgLat());
        vector.set(3, kStreamsMetrics[0].getAvgThr());
        vector.set(4, kStreamsMetrics[1].getKurtosis());
        vector.set(5, kStreamsMetrics[1].getSkewness());
        vector.set(6, kStreamsMetrics[1].getAvgLat());
        vector.set(7, kStreamsMetrics[1].getAvgThr());
        vector.set(8, 0.0);
        vector.set(9, beam_lat_map.get(fw));    //Latency
        vector.set(10, beam_thr_map.get(fw));   //Throughput

        switch (fw) {
            case "spark":
                List<App> sparkApps = yarnApps.stream().filter(app -> app.applicationType.toLowerCase().contains("spark")).collect(Collectors.toList());
                if (sparkApps.isEmpty()) {
                    break;
                }
                App sparkApp = sparkApps.get(0);
                vector.set(11, sparkApp.priority);
                vector.set(12, sparkApp.allocatedMB);
                vector.set(13, sparkApp.allocatedVCores);
                vector.set(14, sparkApp.runningContainers);
                vector.set(15, sparkApp.memorySeconds);
                vector.set(16, sparkApp.vcoreSeconds);
                break;
            case "flink":
                if (flinkJobs.isEmpty()) {
                    break;
                }
                FlinkJob job = flinkJobs.get(0);

                double numRecordsIn = 0;
                double numRecordsInPerSecond = 0;
                double numBytesInRemotePerSecond = 0;
                double numRecordsOut = 0;
                double numRecordsOutPerSecond = 0;
                double numBytesOutPerSecond = 0;

                int vertex_cnt = job.vertices.size();
                for (Vertex vertex : job.vertices) {
                    ThroughputNode node = vertex.throughputNode;
                    if (node.numRecordsIn == null) {
                        continue;
                    }
                    numRecordsIn += node.numRecordsIn.avg / (double) vertex_cnt;
                    numRecordsInPerSecond += node.numRecordsInPerSecond.avg / (double) vertex_cnt;
                    numBytesInRemotePerSecond += node.numBytesInRemotePerSecond.avg / (double) vertex_cnt;
                    numRecordsOut += node.numRecordsOut.avg / (double) vertex_cnt;
                    numRecordsOutPerSecond += node.numRecordsOutPerSecond.avg / (double) vertex_cnt;
                    numBytesOutPerSecond += node.numBytesOutPerSecond.avg / (double) vertex_cnt;
                }

                vector.set(11, numRecordsIn);
                vector.set(12, numRecordsInPerSecond);
                vector.set(13, numBytesInRemotePerSecond);
                vector.set(14, numRecordsOut);
                vector.set(15, numRecordsOutPerSecond);
                vector.set(16, numBytesOutPerSecond);

                break;
            case "apex":
                List<App> apexApps = yarnApps.stream().filter(app -> app.applicationType.toLowerCase().contains("apex")).collect(Collectors.toList());
                if (apexApps.isEmpty()) {
                    break;
                }
                App apexApp = apexApps.get(0);
                vector.set(11, apexApp.priority);
                vector.set(12, apexApp.allocatedMB);
                vector.set(13, apexApp.allocatedVCores);
                vector.set(14, apexApp.runningContainers);
                vector.set(15, apexApp.memorySeconds);
                vector.set(16, apexApp.vcoreSeconds);
                break;
            default:
                throw new IllegalStateException("Default case in genQueryVector() " + fw);
        }

        return vector;
    }

    /**
     * Use this to make decisions for new queries.
     *
     * @param query A number between 1 and 5 representing a query.
     * @return The framework that got selected.Can
     */
    private static String decide(String query) {
//        String res_fw = decideMaxThroughput(query, 1);
//        String res_fw = decideMinLatency(query, 3);
//        String res_fw = decideRoundRobin(query);
//        String res_fw = "flink";
        String res_fw = decideRandom(query);


        fw_active_queries.get(res_fw).add(query);

        if (!fw_list.contains(res_fw)) {
            throw new IllegalStateException("Invalid fw chosen by decide():" + res_fw);
        }

        Random random = new Random();
        double toss1 = random.nextDouble();
        double toss2 = random.nextDouble();
        double toss3 = random.nextDouble();

        if (toss1 < boost.get("spark")) {
            res_fw = "spark";
        } else if (toss2 < boost.get("flink")) {
            res_fw = "flink";
        } else if (toss3 < boost.get("apex")) {
            res_fw = "apex";
        }

        try {
            queries_assigned.write("\n" + System.currentTimeMillis() + "\t" + query + "\t" + res_fw);
            queries_assigned.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-3);
        }

        return res_fw;
    }

    private static void clearActiveQueries() {
        for (String fw : fw_active_queries.keySet()) {
            fw_active_queries.get(fw).clear();
        }
    }

    private static String decideRandom(String query) {
        return fw_list.get(random.nextInt(3));
    }

    /**
     * Assigns queries to frameworks in a round robin fashion.
     *
     * @param query The query to be assigned
     * @return The selected framework
     */
    private static String decideRoundRobin(String query) {
        return fw_list.get(Math.abs((RRcnt++) % fw_list.size()));
    }

    /**
     * Returns the framework with the Vector that has the highest throughput.
     * Performs a NN search on the Vectors of each framework.
     *
     * @param query The query [1,5]
     * @param k     The number of neighbours.
     * @return The chosen framework.
     */
    private static String decideMaxThroughput(String query, int k) {
        Map<String, List<Vector>> fw_candidates = new HashMap<>();

        //Generate a vector for each framework
        for (String fw : fw_list) {
            Vector cur_vec = genQueryVector(fw);
            Index index = new Index(family, 32, 4);
            for (Vector vector : query_fw_vectors.get(query).get(fw)) {
                index.index(vector);
            }
            fw_candidates.put(fw, index.query(cur_vec, k));
        }


        double max = 0;
        String sel_fw = fw_list.get(0);
        for (String fw : fw_candidates.keySet()) {
            for (Vector vector : fw_candidates.get(fw)) {
                if (vector.get(10) > max) {
                    max = vector.get(10);
                    sel_fw = fw;
                }
            }
        }
        return sel_fw;
    }

    /**
     * Returns the framework with the Vector that has the least average latency.
     * Performs a NN search on the Vectors of each framework.
     *
     * @param query The query [1,5]
     * @param k     The number of neighbours.
     * @return The chosen framework.
     */
    private static String decideMinLatency(String query, int k) {
        Map<String, List<Vector>> fw_candidates = new HashMap<>();

        for (String fw : fw_list) {
            Vector cur_vec = genQueryVector(fw);
            Index index = new Index(family, 32, 3);
            for (Vector vector : query_fw_vectors.get(query).get(fw)) {
                index.index(vector);
            }
            fw_candidates.put(fw, index.query(cur_vec, k));
        }

        double min = Double.MAX_VALUE;
        String sel_fw = fw_list.get(0);
        for (String fw : fw_candidates.keySet()) {
            for (Vector vector : fw_candidates.get(fw)) {
                if (vector.get(9) < min) {
                    min = vector.get(9);
                    sel_fw = fw;
                }
            }
        }
        return sel_fw;
    }

    private static void fetchBeamMetrics() {
        try {
            //Initialise to some very small/big values
            Map<String, Double> temp_map_thr = new HashMap<>();

            for (String entry : fw_list) {
                temp_map_thr.put(entry, 0.0);
            }

            //Gather all latencies first and then calculate their average
            Map<String, List<Double>> latencies = new HashMap<>();
            for (String fw : fw_list) {
                latencies.put(fw, new ArrayList<>());
            }

            for (ConsumerRecord<String, String> beam_record : metrics_consumer.poll(50)) {
                String[] val_tokens = beam_record.value().split(" ");
                if (val_tokens.length == 1) {
                    System.out.println(beam_record.value());
                    return;
                }
                String fw = val_tokens[0];
                String value = val_tokens[1];

                if (!fw_list.contains(fw)) {
                    return;
                }
                if (beam_record.topic().equals("g12")) {
                    double prev = temp_map_thr.get(fw);
                    prev += Double.parseDouble(value);
                    temp_map_thr.put(fw, prev);
                } else {
                    latencies.get(fw).add(Double.parseDouble(value));
                }
            }

            for (String fw : temp_map_thr.keySet()) {
                Double val = temp_map_thr.get(fw);
                if (val > 0) {
                    beam_thr_map.put(fw, val);
                }
            }

            for (String fw : latencies.keySet()) {
                OptionalDouble lat = latencies.get(fw).stream().mapToDouble(x -> x).average();
                if (lat.isPresent()) {
                    beam_lat_map.put(fw, lat.getAsDouble());
                }
            }

            //Record throughput/latency of Beam pipeline iff there where records.
            for (String entry : fw_list) {
                metrics_writer.get(entry).write("\n" + System.currentTimeMillis() + "\t" + beam_lat_map.get(entry) + "\t" + beam_thr_map.get(entry));
                metrics_writer.get(entry).flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fetchKafkaMetrics() {
        try {
            for (int stream = 0; stream < numOfStreams; stream++) {    //For each sampled stream
                KSMetrics temp_metrics = new KSMetrics(kstream_URLs.length);
                Map<String, List<Integer>> instance_substream = new HashMap<>();
                Map<String, Integer> instance_sizes = new HashMap<>();

                int kcnt = 0;
                for (String url : kstream_URLs) {    //Gather metrics for each substream via REST api.
                    List<HashMap<String, String>> results = null;
                    try {
                        results = objectMapper.readValue(Requests.GET("http://" + url + "/state/keyvalues/histogram/all", null),
                                new TypeReference<List<HashMap<String, String>>>() {
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (results == null) {
                        System.out.println("Failed to get kstreams stats from " + url);
                        System.exit(-2);
                    }

                    //Initial values
                    instance_substream.put(url, new ArrayList<>());
                    instance_sizes.put(url, 0);

                    for (HashMap<String, String> map : results) {
                        if (map.get("key").equals("stream " + stream + " sample")) {
                            if (map.get("value").isEmpty()) {
                                instance_substream.get(url).add(0);
                            } else {
                                for (String token : map.get("value").split(",")) {
                                    instance_substream.get(url).add(Integer.parseInt(token));
                                }
                            }
                        }
                        if (map.get("key").equals("stream " + stream + " size")) {
                            instance_sizes.put(url, Integer.parseInt(map.get("value")));
                        }
                        if (map.get("key").equals("stream " + stream + " lat")) {
                            temp_metrics.lat[kcnt] = Double.parseDouble(map.get("value"));
                        }
                        if (map.get("key").equals("stream " + stream + " thr")) {
                            temp_metrics.thr[kcnt] = Double.parseDouble(map.get("value"));
                        }
                    }
                    kcnt++;
                }

                //Calculate the number of all samples.
                int total_sample_cnt = 0;
                for (int size : instance_sizes.values()) {
                    total_sample_cnt += size;
                }

                //Sample from the sub streams and produce the final sample
                //Select randomly an element from the substreams until the final sample has been produced.
                List<Double> final_sample = new ArrayList<>();
                for (int j = 0; j < sample_size; j++) {
                    double rand_num = random.nextDouble();
                    double sum = 0; //
                    for (String url : kstream_URLs) {
                        sum += instance_sizes.get(url) / (double) total_sample_cnt;  //Chance of selecting an element from this substream
                        if (rand_num < sum) {
                            int rand_pos = random.nextInt(instance_substream.get(url).size());
                            final_sample.add((double) instance_substream.get(url).get(rand_pos));
                            instance_substream.get(url).remove(rand_pos);
                            break;
                        }
                    }
                }
                temp_metrics.histogram = new Histogram(final_sample);
                kStreamsMetrics[stream] = temp_metrics;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-3);
        }
    }

    public static class KSMetrics { //one for each stream
        Histogram histogram;
        double[] lat;
        double[] thr;

        KSMetrics(int instances) {
            this.lat = new double[instances];
            this.thr = new double[instances];
        }

        double getAvgLat() {
            return Arrays.stream(this.lat).average().orElse(0);
        }

        double getAvgThr() {
            return Arrays.stream(this.thr).average().orElse(0);
        }

        double getKurtosis() {
            return this.histogram.calculateKurtosis();
        }

        double getSkewness() {
            return this.histogram.calculateSkewness();
        }
    }

    private static void sleepMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {
        }
    }
}
