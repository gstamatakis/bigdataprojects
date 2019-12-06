import CoreUtils.LilSampling;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.Sensor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.time.Instant;
import java.util.*;


import static CoreUtils.ClusterConstants.*;

@SuppressWarnings({"unchecked", "Duplicates"})
public class MyTransformer implements Transformer<String, String, KeyValue<String, String>> {
    private final Duration query_update_period = Duration.ofSeconds(3);
    private final Duration histogram_update_period = Duration.ofSeconds(30);
    private KeyValueStore<String, String> kvStore;
    private LilSampling[] sampler;
    private ProcessorContext context;
    private KafkaConsumer<String, String> query_consumer;
    private Map<String, List<KeyValue<String, String>>> fw_queries; //Drop tuples by just not forwarding them.1
    private Sensor sensor;
    private Random random = new Random();
    private HashMap<String, String> fw_qVector;

    @Override
    public void init(ProcessorContext context) {
        this.kvStore = (KeyValueStore<String, String>) context.getStateStore("histogram");
        this.context = context;
        this.sampler = new LilSampling[numOfStreams];

        this.fw_queries = new HashMap<>(fw_size);
        this.fw_qVector = new HashMap<>(fw_size);
        this.fw_queries.put("spark", new ArrayList<>());
        this.fw_queries.put("flink", new ArrayList<>());
        this.fw_queries.put("apex", new ArrayList<>());
        this.fw_qVector.put("spark", "");
        this.fw_qVector.put("flink", "");
        this.fw_qVector.put("apex", "");

        for (int i = 0; i < this.sampler.length; i++) {
            this.sampler[i] = new LilSampling(sample_size);
        }

        this.sensor = this.context.metrics().addLatencyAndThroughputSensor("scope", "entity", "op", Sensor.RecordingLevel.INFO, "tag1", "tag1");

        //Schedule a sub-stream sampling process
        this.context.schedule(histogram_update_period.getSeconds() * 1000, PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
            String latency = "", throughput = "";

            for (Object entry : ((Map<MetricName, ? extends Metric>) this.context.metrics().metrics()).entrySet().toArray()) {
                KafkaMetric metric = ((KafkaMetric) ((Map.Entry) (entry)).getValue());
                if (metric.metricName().tags().containsKey("tag1")) {
                    if (metric.metricName().toString().contains("op-latency-avg")) {
                        latency = metric.metricValue().toString();
                    } else if (metric.metricName().toString().contains("op-rate")) {
                        throughput = metric.metricValue().toString();
                    }
                }
            }

            for (int i = 0; i < numOfStreams; i++) {
                kvStore.put(String.format("stream %d sample", i), this.sampler[i].getSampleAsString());
                kvStore.put(String.format("stream %d size", i), String.valueOf(this.sampler[i].getStreamSize()));
                kvStore.put(String.format("stream %s lat", i), latency);
                kvStore.put(String.format("stream %s thr", i), throughput);
                sampler[i] = new LilSampling(sample_size); //Truncate histogram
            }
            this.context.commit();
        });

        //Kafka consumer for queued_queries
        Properties props = new Properties();
        props.put("bootstrap.servers", "rserver07:6667");
        props.put("group.id", "query_consumer_" + random.nextInt()); //Unique group id for each KStreams Instance to achieve broadcast
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        this.query_consumer = new KafkaConsumer<>(props);
        this.query_consumer.subscribe(Collections.singletonList(Driver.INPUT_QUERIES));

        //Schedule periodic checks for new queries
        this.context.schedule(query_update_period.getSeconds() * 1000, PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
            //Keep only the last record in case we have fallen behind
            ConsumerRecord<String, String> record = null;
            for (ConsumerRecord<String, String> last_record : this.query_consumer.poll(0)) {
                record = last_record;
            }
            if (record == null) {   //NO updates needed when there are no new queries
                return;
            }
            Map<String, List<KeyValue<String, String>>> temp_fw_queries = new HashMap<>();
            //Although String manipulation is hard to read this block simply adds to the Map each framework with each queries.
            for (String fw_list : record.value().split("\\|")) {
                List<KeyValue<String, String>> query_list = new ArrayList<>();
                String[] fw_queries = fw_list.split("-");
                for (String qid_query_tuple : fw_queries[1].split(" ")) {
                    String[] qid_query = qid_query_tuple.split("_");
                    query_list.add(new KeyValue<>(qid_query[0], qid_query[1]));
                }
                temp_fw_queries.put(fw_queries[0], query_list); // fw,<qid,query>
            }
            this.fw_queries = temp_fw_queries;

            //Query vector in different format
            for (String fw : this.fw_queries.keySet()) {
                if (this.fw_queries.get(fw).isEmpty()) {
                    this.fw_qVector.put(fw, "");
                }
                StringBuilder query_str = new StringBuilder();
                query_str.append("*");
                for (KeyValue<String, String> qid_query : this.fw_queries.get(fw)) {
                    query_str.append(qid_query.key).append("_").append(qid_query.value).append(" ");
                }
                this.fw_qVector.put(fw, query_str.toString().trim());
            }
        });

    }

    @Override
    public KeyValue<String, String> transform(final String key, final String msg) {
        Instant start = Instant.now();
        try {
            switch (this.context.topic()) {
                case Driver.INPUT_DATA_1:
                    sampler[0].feed(Integer.parseInt(msg.split(",", 2)[0]));
                    break;
                case Driver.INPUT_DATA_2:
                    sampler[1].feed(Integer.parseInt(msg.split(",", 2)[0]));
                    break;
                default:
                    throw new IllegalStateException("Default case in Driver#process with topic: " + this.context.topic());
            }

            for (String fw : this.fw_qVector.keySet()) {
                if (!this.fw_qVector.get(fw).isEmpty()) {
                    switch (this.context.topic()) {
                        case Driver.INPUT_DATA_1:
                            context.forward(fw, msg + fw_qVector.get(fw));
                            break;
                        case Driver.INPUT_DATA_2:
                            context.forward("data_" + fw, msg + fw_qVector.get(fw));
                            break;
                        default:
                            throw new IllegalStateException("Default case in Driver#process with topic: " + this.context.topic());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(key + " " + msg);
        }
        this.sensor.record(Duration.between(start, Instant.now()).toMillis());//Latency of transformation.
        return null;
    }

    @Override
    public KeyValue<String, String> punctuate(long timestamp) {
        return null;
    }

    @Override
    public void close() {
        try {
            this.query_consumer.close();
        } catch (Exception ignored) {
        }
    }
}
