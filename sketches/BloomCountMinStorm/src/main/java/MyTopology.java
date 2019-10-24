import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.spout.Configs;
import org.apache.storm.hdfs.spout.HdfsSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;
import org.mortbay.log.Log;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MyTopology {
    public static void main(String[] args) {
        // Parse args
        if (args.length < 14) {
            System.out.println("Wrong input args...\nYou provided " + args.length + " args instead.");
            return;
        }

        int width = Integer.parseInt(args[0]);              // also bloom m
        int unique_elements = Integer.parseInt(args[1]);    // also bloom n
        int numOfHashFuncs = Integer.parseInt(args[2]);     // also bloom k

        //Pipeline hints
        int hint_querySpout = Integer.parseInt(args[3]);      //Hint for query spout
        int hint_dataSpout = Integer.parseInt(args[4]);       //Hint for data spout
        int hint_lookupBolt = Integer.parseInt(args[5]);      //Hint for lookup bolts
        int hint_sinkBolt = Integer.parseInt(args[6]);      //Hint for sink bolts

        int query_type = Integer.parseInt(args[7]);        //0:Bloom , 1:CM
        String fsRoot = args[8];
        String inputFolder = args[9];
        String queryFileName = args[10];
        long tupleDelay = Integer.parseInt(args[11]);   //Spout tuple delay (in ns)
        long shutdown = Integer.parseInt(args[12]);   //Spout tuple delay (in ns)

        String ClusterType = args[13]; //Local / Distributed mode

        List<String> RemoteIP = new ArrayList<>();    //Requires an Iterable structure.
        int port = -1;
        if (ClusterType.equals("remote")) {
            RemoteIP.add(args[14]);
            port = Integer.parseInt(args[15]);
        }


        width /= hint_lookupBolt;
        unique_elements /= hint_lookupBolt;

        System.out.println("Params" +
                "\nQuery: " + (query_type == 0 ? "Bloom" : "CountMin") + "" +
                "\nm: " + width + "" +
                "\nn: " + unique_elements + "" +
                "\nk: " + numOfHashFuncs + "" +
                "\nWorkers: " + Runtime.getRuntime().availableProcessors() + "" +
                "\nData spouts: " + hint_dataSpout + "" +
                "\nQuery spouts: " + hint_querySpout + "" +
                "\nLookup bolts: " + hint_lookupBolt + "" +
                "\nSinks: " + hint_sinkBolt + "" +
                "\nQuery spout tuple delay: " + tupleDelay + " ms" +
                "\nFS root: " + fsRoot + "\n\n");

        // Config setup
        Config config = new Config();


        //Config
        config.put("QUERY_TYPE", query_type);
        config.put("QUERY_SPOUT_TUPLE_DELAY", tupleDelay);//Delay between spout messages. Low values may lead to race conditions when using small datasets!!!

        config.put(Configs.HDFS_URI, fsRoot);
        config.put(Configs.SOURCE_DIR, "/" + inputFolder);
        config.put("QUERY_PATH", "/" + queryFileName);
        config.put("OUTPUT_PATH", "/output/");
        config.put(Configs.ARCHIVE_DIR, "/archive/"); //Set to the same path as SOURCE_DIR for cyclic reading !!
        config.put(Configs.BAD_DIR, "/badfiles/");
        config.put(Configs.READER_TYPE, "text");

        //Cluster setup
        //config.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-Xmx14g -Xms4g");
        config.setNumWorkers(Runtime.getRuntime().availableProcessors());   //Total processes
        config.setMaxTaskParallelism(Runtime.getRuntime().availableProcessors());
        config.put(Config.TOPOLOGY_EVENTLOGGER_EXECUTORS, 1);
        config.put(Configs.MAX_OUTSTANDING, 15000);
        config.put(Config.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 1024);
        config.put(Config.TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE, 1024);
        config.put(Config.TOPOLOGY_TRANSFER_BUFFER_SIZE, 512);
        config.setDebug(true);

        //Since the HDFS spout will MOVE the files from the input folder to the archive folder AFTER consumption
        //we attempt to copy them back to the input folder before starting the topology.
        try {
            Configuration conf = new Configuration();
            FileSystem hdfs = FileSystem.get(URI.create(fsRoot), conf);
            Path archivesPath = new Path("/archive/");
            Path inputPath = new Path("/input/");
            RemoteIterator<LocatedFileStatus> sourceFiles = hdfs.listFiles(archivesPath, true);
            if (sourceFiles != null) {
                while (sourceFiles.hasNext()) {
                    FileUtil.copy(hdfs, sourceFiles.next().getPath(), hdfs, inputPath, true, conf);
                }
                System.out.println("Fixed archives directory..");
            }
        } catch (IOException ignored) {
        }

        //Registering metrics consumer,used mostly for latency/throughput tests.
        config.registerMetricsConsumer(org.apache.storm.metric.LoggingMetricsConsumer.class, 1);

        TopologyBuilder builder = new TopologyBuilder();

        //Data spout
        builder.setSpout("dataSpout", new HdfsSpout().withOutputFields("data"), hint_dataSpout);

        //Query spout
        builder.setSpout("querySpout", new QuerySpout(), hint_querySpout);

        //Lookup bolt
        builder.setBolt("lookupBolt", new LookupBolt(width, numOfHashFuncs), hint_lookupBolt)
                .fieldsGrouping("dataSpout", new Fields("data"))
                .fieldsGrouping("querySpout", new Fields("data"));

        //Sink bolt
        builder.setBolt("sinkBolt", new HdfsBolt()
                .withFsUrl(config.get(Configs.HDFS_URI).toString())
                .withFileNameFormat(new DefaultFileNameFormat().withPath(config.get("OUTPUT_PATH").toString()))
                .withRecordFormat(new DelimitedRecordFormat().withFieldDelimiter(" "))
                .withRotationPolicy(new FileSizeRotationPolicy(1000.0f, FileSizeRotationPolicy.Units.MB))  //Create 2nd file after X MBs
                .withSyncPolicy(new CountSyncPolicy(1000)), hint_sinkBolt)   //Flush to HDFS every X tuples.
                .shuffleGrouping("lookupBolt");

        // Launch topology and wait for termination
        try {
            switch (ClusterType) {
                case "local":   //Local mode, used for debugging
                    LocalCluster cluster = new LocalCluster();
                    cluster.submitTopology("MyTopology", config, builder.createTopology());
                    Log.info("Running with LocalCluster.");
                    break;
                case "distributed": //Distributed mode, used for submitting topologies to a local Storm Cluster
                    StormSubmitter.submitTopology("MyTopology", config, builder.createTopology());
                    Log.info("Running with StormSubmitter.");
                    break;
                case "remote":  //Use this mode to submit a topology to a remote cluster.
                    config.put(Config.NIMBUS_SEEDS, RemoteIP);
                    config.put(Config.NIMBUS_THRIFT_PORT, port);
                    config.put(Config.STORM_ZOOKEEPER_SERVERS, RemoteIP);
                    config.put(Config.STORM_ZOOKEEPER_PORT, 2181);
                    System.setProperty("storm.jar", "target/storm2-1.0-SNAPSHOT.jar");

                    StormSubmitter.submitTopology("MyTopology", config, builder.createTopology());
                    break;
                default:
                    System.out.println("Modes: local,distributed,remote");
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.sleep(shutdown);
            System.out.println("\n\n\nCompleted!!");
        }
        System.exit(0);
    }
}
