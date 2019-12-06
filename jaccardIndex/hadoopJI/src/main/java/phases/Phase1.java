package phases;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Phase 1, Full MapReduce job.
 * Input lines from the topics file: "category did 1"    eg. E11 2286 1
 * The goal is to group by category so we can extract the count of dids in each tid.
 * This will be our Log relation and it will be used again in the following phases.
 * Example of and output line in an output file: <cat_cntC> [<did>]+
 */
public class Phase1 {

    public static class Join1Phase1Mapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
        private Text cat;
        private Text did;

        @Override
        public void configure(JobConf job) {
            this.cat = new Text();
            this.did = new Text();
        }

        @Override
        public void map(LongWritable key, Text line, OutputCollector<Text, Text> outputCollector, Reporter reporter) throws IOException {
            String[] tokens = line.toString().trim().split(" ", -1);    // [<cat>]+ [<did>]+
            cat.set(tokens[0]);
            did.set(tokens[1]);
            outputCollector.collect(cat, did);      //For the cat group by.
        }
    }

    public static class Join1Phase1Combiner extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        private Text value;

        @Override
        public void configure(JobConf job) {
            super.configure(job);
            this.value = new Text();
        }

        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            StringBuilder vals = new StringBuilder();
            while (values.hasNext()) {
                vals.append(values.next()).append(" ");
            }
            value.set(vals.toString().trim());
            output.collect(key, value);
        }
    }

    public static class Join1Phase1Partitioner extends MapReduceBase implements Partitioner<Text, Text> {

        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            return Math.floorMod(key.hashCode(), numPartitions);
        }
    }

    public static class Join1Phase1Reducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        private Text k, v;

        @Override
        public void configure(JobConf job) {
            k = new Text();
            v = new Text();
        }

        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            //Gather the dids for this cat and count them.
            StrBuilder did_sb = new StrBuilder();
            while (values.hasNext()) {  // One iteration per mapper task, a very small number.
                did_sb.append(values.next().toString().trim()).append(" ");
            }
            String dids = did_sb.toString().trim();
            //Category followed by its did count, aka DOC(C). For the dids count the spaces in the string, one for each did.
            String cat_cntC = key.toString().trim() + "_" + (dids.split(" ").length);
            k.set(cat_cntC);
            v.set(dids);
            output.collect(k, v);
        }
    }
}
