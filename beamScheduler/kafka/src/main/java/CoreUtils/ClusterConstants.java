package CoreUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterConstants {
    public static final String flink_master = "rserver01:8081";
    public static final String yarn_master = "http://rserver01:8189";

    /**
     * Influx DB retention policy.Don't use the default RP since it mixes the data of all users.
     */
    public static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Kafka related options.
     */
    public static final String bootstrapServers = "rserver02:6667,rserver04:6667,rserver05:6667,rserver06:6667,rserver07:6667";


    /**
     * Number of frameworks inside the beam model.
     * Spark,Flink,apex
     */
    public static final int fw_size = 3;
    /**
     * Number of input streams.
     */
    public static int numOfStreams = 2;

    /**
     * Sample size of LiL
     */
    public static final int sample_size = 50;

    /**
     * Number of queries supported by the Beam pipeline.
     */
    public static final int query_num = 5;
}
