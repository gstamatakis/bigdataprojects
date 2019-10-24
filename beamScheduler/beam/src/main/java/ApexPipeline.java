import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.DoubleCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;

import java.util.*;


@SuppressWarnings("Duplicates")
public class ApexPipeline {
    public static void main(String[] args) {
        PipelineOptionsIface options = PipelineOptionsFactory.fromArgs(args).withValidation().as(PipelineOptionsIface.class);
        Pipeline pipeline = Pipeline.create(options);


        //Used to separate incoming kafka streams
        final TupleTag<KV<KV<String, String>, String>> tag_main = new TupleTag<>();
        final TupleTag<KV<KV<String, String>, String>> tag_sec = new TupleTag<>();

        //Used to join the incoming streams in CoGroupByKey
        final TupleTag<String> left_tag = new TupleTag<>();
        final TupleTag<String> right_tag = new TupleTag<>();

        //For kafka sinks.
        final TupleTag<String> tag_queries = new TupleTag<>();
        final TupleTag<Double> tag_latency = new TupleTag<>();
        final TupleTag<Integer> tag_throughput = new TupleTag<>();


        //For standard kafka sinks
        Map<String, Object> incomingDataStreamProps = new HashMap<>();
        incomingDataStreamProps.put("auto.offset.reset", "latest");
        incomingDataStreamProps.put("group.id", "apex");

        Map<String, Object> metricsSinkProps = new HashMap<>();
        metricsSinkProps.put("linger.ms", 1);

        Map<String, Object> sinkProps = new HashMap<>();
        sinkProps.put("linger.ms", 1);


        PCollectionTuple input_stream = pipeline
                .apply("ReadKafkaMainStream", KafkaIO.<String, String>read()
                        .withKeyDeserializer(StringDeserializer.class)
                        .withValueDeserializer(StringDeserializer.class)
                        .withBootstrapServers("rserver02:6667,rserver04:6667,rserver05:6667,rserver06:6667,rserver07:6667")
                        .withTopics(Arrays.asList("g3", "g14"))
                        .updateConsumerProperties(incomingDataStreamProps)
                        .withCreateTime(Duration.millis(100))
                )
                .apply(ParDo.of(new DoFn<KafkaRecord<String, String>, KV<KV<String, String>, String>>() {

                    @ProcessElement
                    public void processElement(ProcessContext c) {
                        Set<String> queries = new HashSet<>();
                        String[] tuple_tokens = c.element().getKV().getValue().split("\\*");//<payload*queries>
                        for (String qid_query_token : tuple_tokens[1].split(" ")) {
                            queries.add(qid_query_token.split("_")[1]);
                        }

                        for (String query : queries) {
                            switch (c.element().getTopic()) {
                                case "g3":   //Primary stream
                                    c.output(tag_main, KV.of(KV.of(query, tuple_tokens[0].split(",")[0]), c.timestamp().getMillis() + " " + tuple_tokens[0]));
                                    break;
                                case "g14": //Secondary stream
                                    c.output(tag_sec, KV.of(KV.of(query, tuple_tokens[0].split(",")[0]), c.timestamp().getMillis() + " " + tuple_tokens[0]));
                                    break;
                                default:
                                    throw new IllegalStateException("Else case in c.element().getTopic() in apexPipeline stage 1");
                            }
                        }
                    }
                }).withOutputTags(tag_main, TupleTagList.of(tag_sec)));

        //Windowing the 2 streams before the Join takes place
        PCollection<KV<KV<String, String>, String>> left_window = input_stream.get(tag_main)
                .setCoder(KvCoder.of(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of()), StringUtf8Coder.of()))
                .apply(Window.<KV<KV<String, String>, String>>into(FixedWindows.of(Duration.standardSeconds(5)))
                        .withAllowedLateness(Duration.ZERO)
                        .discardingFiredPanes());

        PCollection<KV<KV<String, String>, String>> right_window = input_stream.get(tag_sec)
                .setCoder(KvCoder.of(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of()), StringUtf8Coder.of()))
                .apply(Window.<KV<KV<String, String>, String>>into(FixedWindows.of(Duration.standardSeconds(5)))
                        .withAllowedLateness(Duration.ZERO)
                        .discardingFiredPanes());

        PCollectionTuple result = KeyedPCollectionTuple
                .of(left_tag, left_window)
                .and(right_tag, right_window)
                .apply("CoGBK", CoGroupByKey.create())
                .apply("ParDo CoGBK", ParDo.of(new DoFn<KV<KV<String, String>, CoGbkResult>, String>() {

                    @ProcessElement
                    public void processElement(ProcessContext c) {
                        List<String> leftVals = new ArrayList<>();
                        List<String> rightVals = new ArrayList<>();
                        c.element().getValue().getAll(left_tag).forEach(leftVals::add);
                        c.element().getValue().getAll(right_tag).forEach(rightVals::add);
                        String key = c.element().getKey().getValue();//join key
                        String temp_ts = null;
                        c.output(tag_throughput, leftVals.size() + rightVals.size());

                        switch (c.element().getKey().getKey()) {
                            case "1"://innerJoin
                                for (String leftValue : leftVals) {
                                    String[] leftTokens = leftValue.split(" ", 2);
                                    temp_ts = leftTokens[0];
                                    for (String rightValue : rightVals) {
                                        String[] rightTokens = rightValue.split(" ", 2);
                                        c.output(tag_queries, key + " " + leftTokens[1] + " " + rightTokens[1]);
                                    }
                                }
                                break;
                            case "2"://leftOuterJoin
                                for (String leftValue : leftVals) {
                                    String[] leftTokens = leftValue.split(" ", 2);
                                    temp_ts = leftTokens[0];
                                    if (rightVals.iterator().hasNext()) {
                                        for (String rightValue : rightVals) {
                                            String[] rightTokens = rightValue.split(" ", 2);
                                            c.output(tag_queries, key + " " + leftTokens[1] + " " + rightTokens[1]);
                                        }
                                    } else {
                                        c.output(tag_queries, key + " " + leftTokens[1] + " " + "null");
                                    }
                                }
                                break;
                            case "3"://rightOuterJoin
                                for (String rightValue : rightVals) {
                                    String[] rightTokens = rightValue.split(" ", 2);
                                    temp_ts = rightTokens[0];
                                    if (leftVals.iterator().hasNext()) {
                                        for (String leftValue : leftVals) {
                                            String[] leftTokens = leftValue.split(" ", 2);
                                            c.output(tag_queries, key + " " + leftTokens[1] + " " + rightTokens[1]);
                                        }
                                    } else {
                                        c.output(tag_queries, key + " " + "null" + " " + rightTokens[1]);
                                    }
                                }
                                break;
                            case "4"://fullOuterJoin
                                if (leftVals.iterator().hasNext() && rightVals.iterator().hasNext()) {
                                    for (String rightValue : rightVals) {
                                        String[] rightTokens = rightValue.split(" ", 2);
                                        temp_ts = rightTokens[0];
                                        for (String leftValue : leftVals) {
                                            String[] leftTokens = leftValue.split(" ", 2);
                                            c.output(tag_queries, key + " " + leftTokens[1] + " " + rightTokens[1]);
                                        }
                                    }
                                } else if (leftVals.iterator().hasNext() && !rightVals.iterator().hasNext()) {
                                    for (String leftValue : leftVals) {
                                        String[] leftTokens = leftValue.split(" ", 2);
                                        c.output(tag_queries, key + " " + leftTokens[1] + " " + "null");
                                        temp_ts = leftTokens[0];
                                    }
                                } else if (!leftVals.iterator().hasNext() && rightVals.iterator().hasNext()) {
                                    for (String rightValue : rightVals) {
                                        String[] rightTokens = rightValue.split(" ", 2);
                                        c.output(tag_queries, key + " " + "null" + " " + rightTokens[1]);
                                        temp_ts = rightTokens[0];
                                    }
                                }
                                break;
                            case "5"://semiJoin
                                for (String leftValue : leftVals) {
                                    String[] leftTokens = leftValue.split(" ", 2);
                                    temp_ts = leftTokens[0];
                                    if (rightVals.iterator().hasNext()) {
                                        c.output(tag_queries, key + " " + leftTokens[1]);
                                    }
                                }
                                break;
                            default:
                                throw new IllegalStateException("Default case at at result with key: " + c.element().getKey().getKey());
                        }

                        if (temp_ts != null) {
                            c.output(tag_latency, ((double) System.currentTimeMillis() - Long.parseLong(temp_ts)));
                        }
                    }
                }).withOutputTags(tag_queries, TupleTagList.of(tag_latency).and(tag_throughput)));


        //Kafka sinks
        result.get(tag_queries)
                .apply("query_results_sink", KafkaIO.<Void, String>write()
                        .withBootstrapServers("rserver02:6667,rserver04:6667,rserver05:6667,rserver06:6667,rserver07:6667")
                        .withTopic("g6")
                        .withValueSerializer(StringSerializer.class)
                        .updateProducerProperties(sinkProps)
                        .values());


        if (options.getSkip().equals("yes")) {
            result.get(tag_latency)
                    .setCoder(DoubleCoder.of())
                    .apply(Window.<Double>into(FixedWindows.of(Duration.standardSeconds(1))).discardingFiredPanes())
                    .apply(Combine.globally(new AverageFn()).withoutDefaults())
                    .apply(ParDo.of(new DoFn<Double, String>() {
                        @ProcessElement
                        public void processElement(ProcessContext c) {
                        }
                    }))
                    .apply("latency_sink", KafkaIO.<Void, String>write()
                            .withBootstrapServers("rserver07:6667")
                            .withTopic("g11")
                            .withValueSerializer(StringSerializer.class)
                            .updateProducerProperties(metricsSinkProps)
                            .values());
        } else {
            result.get(tag_latency)
                    .setCoder(DoubleCoder.of())
                    .apply(Window.<Double>into(FixedWindows.of(Duration.standardSeconds(1))).discardingFiredPanes())
                    .apply(Combine.globally(new AverageFn()).withoutDefaults())
                    .apply(ParDo.of(new DoFn<Double, String>() {
                        @ProcessElement
                        public void processElement(ProcessContext c) {
                            if (!c.element().toString().equals("NaN")) {
                                c.output("apex " + c.element().toString());
                            }
                        }
                    }))
                    .apply("latency_sink", KafkaIO.<Void, String>write()
                            .withBootstrapServers("rserver07:6667")
                            .withTopic("g11")
                            .withValueSerializer(StringSerializer.class)
                            .updateProducerProperties(metricsSinkProps)
                            .values());
        }

        result.get(tag_throughput)
                .setCoder(VarIntCoder.of())
                .apply(Window.<Integer>into(FixedWindows.of(Duration.standardSeconds(1))).discardingFiredPanes())
                .apply(Combine.globally(new SumFn()).withoutDefaults())
                .apply(ParDo.of(new DoFn<Integer, String>() {
                    @ProcessElement
                    public void processElement(ProcessContext c) {
                        if (c.element() > 0) {
                            c.output("apex " + c.element() / 2);
                        }
                    }
                }))
                .apply("throughput_sink", KafkaIO.<Void, String>write()
                        .withBootstrapServers("rserver07:6667")
                        .withTopic("g12")
                        .withValueSerializer(StringSerializer.class)
                        .updateProducerProperties(metricsSinkProps)
                        .values());

        try {
            pipeline.run().waitUntilFinish();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
