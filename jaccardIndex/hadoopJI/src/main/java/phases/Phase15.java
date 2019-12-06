package phases;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.MultipleOutputs;

import java.io.IOException;
import java.util.Iterator;

/**
 * This phase simply loads the results of phase 1 and the tuples are grouped by did.
 * Input line: <cat_cntS> [<did>]+
 * Output tuples: <did> [<cat_cntS>]+
 */
public class Phase15 {

    //Group by did
    public static class Join1Phase15Mapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
        private Text k, v;

        @Override
        public void configure(JobConf job) {
            this.k = new Text();
            this.v = new Text();
        }

        @Override
        public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            String[] cat_cntC_dids = value.toString().split("\t");  //<cat_cntC>\t[<did>]+
            v.set(cat_cntC_dids[0]);
            for (String token: cat_cntC_dids[1].split(" ", -1)) {// for each did
                k.set(token);
                output.collect(k, v);   //<did> <cat_cntC>
            }
        }
    }

    //Standard key partitioner
    public static class Join1Phase15Partitioner extends MapReduceBase implements Partitioner<Text, Text> {
        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            return Math.floorMod(key.hashCode(), numPartitions);
        }
    }

    //Gather the cat_cntC tuples for each did and group them into a single String, basically perform a local reduce.
    public static class Join1Phase15Combiner extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        private Text v;

        @Override
        public void configure(JobConf job) {
            v = new Text();
        }

        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            StringBuilder cat_cntC_sb = new StringBuilder();
            while (values.hasNext()) {
                cat_cntC_sb.append(values.next().toString().trim()).append(" ");
            }
            v.set(cat_cntC_sb.toString().trim());
            output.collect(key, v);
        }
    }

    public static class Join1Phase15Reducer implements Reducer<Text, Text, Text, Text> {
        private MultipleOutputs mos;
        private Text v;
        private int task_id;
        private String dest_file;

        @Override
        public void configure(JobConf job) {
            this.mos = new MultipleOutputs(job);
            this.task_id = Integer.parseInt(job.get("mapred.task.id").split("_")[4]);
            this.dest_file = String.format("L%05d", Math.floorMod(this.task_id, job.getNumReduceTasks()));
            v = new Text();
        }

        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            StringBuilder sb = new StringBuilder();
            while (values.hasNext()) {
                sb.append(values.next().toString().trim()).append(" ");
            }
            v.set(sb.toString().trim());
            this.mos.getCollector(dest_file, reporter).collect(key, v);
        }

        @Override
        public void close() throws IOException {
            this.mos.close();
        }
    }
}
