import CoreUtils.ClusterConstants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class KStreamer {
    static Timer timer = new Timer("Timer");
    static long period = 1000L;

    static int numOfRecords;
    static Instant file_start = Instant.now();


    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("[file_name1] [file_name2] [repeats] [topic1] [topic2] [max_msg_rate]");
        }

        String fileName1 = args[0];
        String fileName2 = args[1];
        int file_repeats = Integer.parseInt(args[2]);
        String topicName1 = args[3];
        String topicName2 = args[4];
        int msg_rate = Integer.parseInt(args[5]);//Max number of messages per second

        Properties props = new Properties();

        props.put("bootstrap.servers", ClusterConstants.bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("max.block.ms", 30000);
        props.put("request.timeout.ms", 30000);
        props.put("linger.ms", 5);
        props.put("acks", "all");

        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                System.out.println("\n\n\nNum of records in the last " + period + "ms: " + numOfRecords +
                        "\nTotal time spent : " + Duration.between(file_start, Instant.now()).getSeconds());
                numOfRecords = 0;
            }
        };
        timer.scheduleAtFixedRate(repeatedTask, 0, period);

        //Read the file many times
        Producer<String, String> producer1 = new KafkaProducer<>(props);
        Producer<String, String> producer2 = new KafkaProducer<>(props);
        Random random = new Random(0);

        for (int i = 0; i < file_repeats; i++) {
            file_start = Instant.now();
            try (BufferedReader bufferedReader1 = new BufferedReader(new FileReader(fileName1)); BufferedReader bufferedReader2 = new BufferedReader(new FileReader(fileName2))) {
                String line1, line2;
                while (true) {
                    if (numOfRecords < msg_rate) {
                        line1 = bufferedReader1.readLine();
                        line2 = bufferedReader2.readLine();
                        if (line1 == null || line2 == null) {
                            break;
                        }
                        for (int k = 0; k < random.nextInt(9) + 1; k++) {
                            producer1.send(new ProducerRecord<>(topicName1, null, line1));
                            producer2.send(new ProducerRecord<>(topicName2, null, line2));
                            numOfRecords += 2;
                        }
                    } else {
                        try {
                            Thread.sleep(1);//sleep 1 ms
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        System.out.println("Streamer finished!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                producer1.close();
                producer2.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.exit(0);
            }
        }));
    }
}
