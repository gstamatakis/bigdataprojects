package phases;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Phase 3
 * Input line from the Li files: <did> [<cat_cntC>]+, build a hashtable from Li tuples.
 * In close() read the correct RLi and group its tuples by did, (RLi tuples: <tid_cntS> [<did>]+)
 * Then send matching tuples straight to the reducer for the JI.
 */
public class Phase3 {
    public static class Join1Phase3Mapper implements Mapper<LongWritable, Text, Text, Text> {
        private HashMap<Integer, ArrayList<String>> did_cat_cntC;   //Used by LOG relation  <did> [<cat_cntC>]+
        private FileSystem hdfs;
        private ArrayList<Path> RLi_paths;
        private OutputCollector<Text, Text> collector;
        private String Li_suffix;

        @Override
        public void configure(JobConf job) {
            this.did_cat_cntC = new HashMap<>();
            this.RLi_paths = new ArrayList<>();
            this.Li_suffix = (new Path(job.get("map.input.file"))).getName().substring(0,6); //Get the input file name suffix. eg. L00001

            String root = job.get("root");
            String out2 = job.get("out2");
            try {
                this.hdfs = FileSystem.get(new Path(root).toUri(), job);
                RemoteIterator<LocatedFileStatus> fileStatusListIterator = hdfs.listFiles(new Path(root + out2), true);
                while (fileStatusListIterator.hasNext()) {
                    Path path = fileStatusListIterator.next().getPath();
                    if (!path.toString().contains("_SUCCESS")) {
                        RLi_paths.add(path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override   //Read the Li and build a HT. Li containts tuples like: <did> [<cat_cntC>]+
        public void map(LongWritable key, Text value, OutputCollector<Text, Text> outputCollector, Reporter reporter) {
            String[] did_cat_cntC_token = value.toString().split("\t", -1); //<did>\t[<cat_cntC>]+
            int did = Integer.parseInt(did_cat_cntC_token[0]);//<did>
            for (String token: did_cat_cntC_token[1].split(" ", -1)) {//[<cat_cntC>]+
                if (this.did_cat_cntC.containsKey(did)) {
                    this.did_cat_cntC.get(did).add(token);
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(token);
                    this.did_cat_cntC.put(did, list);
                }
            }

            this.collector = outputCollector;
        }

        @Override
        public void close() throws IOException {
            Text k = new Text();
            Text v = new Text("1");
            for (Path RLi: this.RLi_paths) {
                if (RLi.toString().contains(this.Li_suffix)) {  //Fetch the RLi that matches this Li file.
                    BufferedReader br = new BufferedReader(new InputStreamReader(hdfs.open(RLi)));
                    String line;
                    while ((line = br.readLine()) != null) {    //for each <tid_cnts> [<did>]+  tuple
                        String[] RLi_parts2 = line.trim().split("\t", -1);  // <tid>_<cntS>\t[<did>]+
                        for (String RLi_did_token: RLi_parts2[1].split(" ", -1)) {  //for each RLi did
                            int RLi_did = Integer.parseInt(RLi_did_token);
                            if (this.did_cat_cntC.containsKey(RLi_did)) {   // Look for a match
                                for (String cat_cntC: this.did_cat_cntC.get(RLi_did)) { //For each Li <cat_cntC>
                                    k.set(cat_cntC + " " + RLi_parts2[0]);
                                    this.collector.collect(k, v); // <cat_cntC> <tid_cntS> <1>
                                }
                            }
                        }
                    }
                    br.close();
                }
            }
        }
    }

    public static class Join1Phase3Combiner extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        private Text v;

        @Override
        public void configure(JobConf job) {
            this.v = new Text();
        }

        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            int cntCS = 0;
            while (values.hasNext()) {
                values.next();
                cntCS++;
            }
            v.set(String.valueOf(cntCS));
            output.collect(key, this.v);
        }
    }

    public static class Join1Phase3Partitioner extends MapReduceBase implements Partitioner<Text, Text> {

        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            return Math.floorMod(key.hashCode(), numPartitions);
        }
    }

    /**
     * Collect the join tuple keys (cat,tid) along with cntS (count of dids for this tid) and cntC (count of dids for this cat).
     * Calculate the Jaccard Index and flush to HDFS.
     */
    public static class Join1Phase3Reducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        private DecimalFormat df;
        private Text v;
        private Text k;

        @Override
        public void configure(JobConf job) {
            df = new DecimalFormat("#.############"); //12 digit precision for Jaccard Index
            v = new Text();
            k = new Text();
        }

        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            String[] key_tokens = key.toString().split(" ");

            String[] cat_tokens = key_tokens[0].split("_");
            String cat = cat_tokens[0];
            int cntC = Integer.parseInt(cat_tokens[1]);

            String[] tid_tokens = key_tokens[1].split("_");
            String tid = tid_tokens[0];
            int cntS = Integer.parseInt(tid_tokens[1]);

            int cntCS = 0;
            while (values.hasNext()) {
                cntCS += Integer.parseInt(values.next().toString().trim());
            }

            k.set(cat + " " + tid);
            v.set(df.format((cntCS) / (double) (cntC + cntS + cntCS)));//Calculate the Jaccard Index.
            output.collect(k, v);
        }
    }

}
