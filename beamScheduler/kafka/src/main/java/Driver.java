import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.Stores;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import static CoreUtils.ClusterConstants.bootstrapServers;


@SuppressWarnings({"unchecked", "Duplicates"})
public class Driver {
    static final String INPUT_DATA_1 = "g5";
    static final String INPUT_DATA_2 = "g7";
    static final String INPUT_QUERIES = "g9";
    private static final String OUTPUT_TOPIC_SPARK = "g1";
    private static final String OUTPUT_TOPIC_FLINK = "g2";
    private static final String OUTPUT_TOPIC_APEX = "g3";
    private static final String OUTPUT_DATA_SPARK = "g10";
    private static final String OUTPUT_DATA_FLINK = "g13";
    private static final String OUTPUT_DATA_APEX = "g14";

    public static void main(final String[] args) throws Exception {
        int port = args.length >= 1 ? Integer.parseInt(args[0]) : 7070;
        String host = args.length >= 2 ? args[1] : "localhost";
        String instance_id = String.valueOf(port).substring(3);    //The last digit of the port.

        String state_dir = "tmp/kafka-streams/" + instance_id;
        try {
            deleteFolder(new File(state_dir));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(String.format("Args: port:%d host:%s instance:%s state_dir:%s", port, host, instance_id, state_dir));

        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put("instance_id", instance_id);
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "gStream"); //Should be the same for ALL workers/instances. Acts as part of the GroupID as well
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, state_dir);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, host + ":" + port);

        KafkaStreams streams = buildTopology(streamsConfiguration);

        streams.cleanUp();
        streams.start();

        final InteractiveQueriesRestService restService = startRestProxy(streams, port, host);

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutting down...");
                restService.stop();
                System.out.println("REST stopped");
                streams.close();
                System.out.println("Streams closed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    private static InteractiveQueriesRestService startRestProxy(final KafkaStreams streams, final int port, final String host) throws Exception {
        final HostInfo hostInfo = new HostInfo(host, port);
        final InteractiveQueriesRestService interactiveQueriesRestService = new InteractiveQueriesRestService(streams, hostInfo);
        interactiveQueriesRestService.start(port);
        return interactiveQueriesRestService;
    }

    private static KafkaStreams buildTopology(Properties streamsConfiguration) {
        StreamsBuilder builder = new StreamsBuilder();

        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("histogram"), Serdes.String(), Serdes.String()));

        KStream<String, String> input = builder.stream(Arrays.asList(INPUT_DATA_1, INPUT_DATA_2));

        KStream<String, String> processed_data = input.transform(MyTransformer::new, "histogram");

        //KEY-> bitVector of frameworks
        KStream<String, String>[] branches = processed_data.branch(
                (k, v) -> k.equals("spark"),   //Spark
                (k, v) -> k.equals("flink"),   //Flink
                (k, v) -> k.equals("apex"),    //Apex
                (k, v) -> k.equals("data_spark"),
                (k, v) -> k.equals("data_flink"),
                (k, v) -> k.equals("data_apex")
        );

        branches[0].selectKey((k, v) -> null).to(OUTPUT_TOPIC_SPARK);
        branches[1].selectKey((k, v) -> null).to(OUTPUT_TOPIC_FLINK);
        branches[2].selectKey((k, v) -> null).to(OUTPUT_TOPIC_APEX);
        branches[3].selectKey((k, v) -> null).to(OUTPUT_DATA_SPARK);
        branches[4].selectKey((k, v) -> null).to(OUTPUT_DATA_FLINK);
        branches[5].selectKey((k, v) -> null).to(OUTPUT_DATA_APEX);

        return new KafkaStreams(builder.build(), streamsConfiguration);
    }

    private static void deleteFolder(File folder) {
        if (folder == null) {
            return;
        }
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}