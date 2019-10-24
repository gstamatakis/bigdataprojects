import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.storm.hdfs.spout.Configs;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Spout responsible for feeding query tuples to the topology.
 * Tuples have the following format (QueryID,IP).
 */
public class QuerySpout extends BaseRichSpout {
    private SpoutOutputCollector collector;
    private int msg_count;      // Amount of tuples emitted.
    private Logger log;
    private BufferedReader br;
    private boolean completed;  // All tuples have been emitted successfully.
    private long tupleDelay;
    Instant start, finish;
    boolean started;

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        this.tupleDelay = (long) conf.getOrDefault("QUERY_SPOUT_TUPLE_DELAY", 10);
        this.log = Logger.getLogger(QuerySpout.class.getName());
        this.completed = false;
        this.msg_count = 0;
        this.started = false;
        try {
            FileSystem fs = FileSystem.get(URI.create(conf.get(Configs.HDFS_URI).toString()), new Configuration());
            Path path = new Path(conf.get("QUERY_PATH").toString());
            br = new BufferedReader(new InputStreamReader(fs.open(path)));
            start = Instant.now();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void nextTuple() {
        if (completed) {
            Utils.sleep(10000);
            return;
        }

        Utils.sleep(tupleDelay);

        try {
            String line;
            if ((line = br.readLine()) == null) {
                finish = Instant.now();
                log.info("QuerySpout finished after " + msg_count + " messages and " + Duration.between(start, finish).toMillis() + " ms.");
                completed = true;
                return;
            }
            String[] data = line.split("\\s+");
            collector.emit(new Values(Integer.parseInt(data[0]), data[1]));
            msg_count++;
        } catch (IOException e) {
            log.info(e.toString());
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("queryID", "data"));
    }

}
