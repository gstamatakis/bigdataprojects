package phases;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.MultipleOutputs;

import java.io.IOException;

/**
 * Phase 1, Map-only job.
 * Input line from the terms file (stem-termid-idf.map.txt) : "stem tid idf".
 * The goal is to generate the unique cat files (Li.uk) by keeping distinct tid`s.
 * This will be our Log relation and it will be used again in the 3rd phase when joining with the ref relation.
 * Output line in some Li.uk file: "tid stem".
 */
public class Phase4 {
    public static class Join2Phase1Mapper extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text> {
        private IntWritable k;
        private Text v;
        private MultipleOutputs mos;
        private int id;

        @Override
        public void configure(JobConf job) {
            this.k = new IntWritable();
            this.v = new Text();
            this.id = Integer.parseInt(job.get("mapred.task.id").split("_")[4]);
            this.mos = new MultipleOutputs(job);
        }

        @Override
        public void map(LongWritable key, Text line, OutputCollector<IntWritable, Text> outputCollector, Reporter reporter) throws IOException {
            String[] tokens = line.toString().trim().split(" ", -1);
            k.set(Integer.parseInt(tokens[1]));
            v.set(tokens[0]);
            this.mos.getCollector("L" + String.format("%05d", this.id), reporter).collect(k, v);
        }

        @Override
        public void close() throws IOException {
            this.mos.close();
        }
    }

    public static class Join2Phase1Partitioner extends MapReduceBase implements Partitioner<Text, Text> {

        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            return Math.floorMod(key.hashCode(), numPartitions);
        }
    }
}
