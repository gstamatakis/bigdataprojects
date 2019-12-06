package phases;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.MultipleOutputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Input line from (a) vector file(s),which from now on will be called Ref relation"
 * "did [tid:weight]+" eg."2286 \t 864:0.0497 864:0.0497"
 * The goal is to group by tid in order to extract the count of each stem and then produce the RLi file from each Li.uk file
 * created in the previous phase. Keep in mind that for each input split of the Ref we will need to make a pass over every Li file!
 * Output tuple of each RLi file: <tid_cntS> [<did>]+ for each RLi file.
 */
public class Phase2 {
    public static class Join1Phase2Mapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
        private Text k;
        private Text v;

        @Override
        public void configure(JobConf job) {
            this.k = new Text();
            this.v = new Text();
        }

        @Override
        public void map(LongWritable key, Text value, OutputCollector<Text, Text> collector, Reporter reporter) throws IOException {
            String[] parts = value.toString().split(" ", -1);   //<did> [tid]+
            v.set(parts[0]);
            for (int i = 2; i < parts.length; i++) {
                k.set(parts[i].split(":")[0]);
                collector.collect(k, v);
            }
        }
    }

    //Standard partitioner
    public static class Join1Phase2Partitioner extends MapReduceBase implements Partitioner<Text, Text> {
        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            return Math.floorMod(key.hashCode(), numPartitions);
        }
    }

    //(k,v1),(k,v2), ... ,(k,v_k) -> (k,v1 v2 .... v_k)
    public static class Join1Phase2Combiner extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        private Text v;

        @Override
        public void configure(JobConf job) {
            this.v = new Text();
        }

        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            StringBuilder sb = new StringBuilder();
            while (values.hasNext()) {
                sb.append(values.next().toString()).append(" ");
            }
            v.set(sb.toString().trim());
            output.collect(key, v); //emmit <tid> [<did>]+
        }
    }

    public static class Join1Phase2Reducer implements Reducer<Text, Text, Text, Text> {
        private HashMap<String, ArrayList<Integer>> tid_cntS_dids;// <tid_cntS> [<did>]+
        private MultipleOutputs mos;
        private FileSystem hdfs;
        private ArrayList<Path> Li_paths;
        private int id;
        private Reporter reporter;

        @Override
        public void configure(JobConf job) {
            this.mos = new MultipleOutputs(job);
            this.id = Integer.parseInt(job.get("mapred.task.id").split("_")[4]); //Get the task task_id
            this.tid_cntS_dids = new HashMap<>();
            this.Li_paths = new ArrayList<>();

            String root = job.get("root");
            try {   //Scan for Li.uk files in the output of the previous phase.
                this.hdfs = FileSystem.get(new Path(root).toUri(), job);
                RemoteIterator<LocatedFileStatus> fileStatusListIterator = this.hdfs.listFiles(new Path(root + job.get("out15")), true);
                while (fileStatusListIterator.hasNext()) {
                    Path path = fileStatusListIterator.next().getPath();
                    if (path.toString().contains("L")) {
                        this.Li_paths.add(path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter){
            ArrayList<Integer> did_list = new ArrayList<>();
            while (values.hasNext()) { // Build the hashtable: <tid_cntS> [<did>]+
                for (String did_token: values.next().toString().trim().split(" ", -1)) {
                    did_list.add(Integer.parseInt(did_token));
                }
            }
            String tid_cntS = key.toString() + "_" + String.valueOf(did_list.size());
            for (int did: did_list) {
                if (this.tid_cntS_dids.containsKey(tid_cntS)) {
                    this.tid_cntS_dids.get(tid_cntS).add(did);
                } else {
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(did);
                    this.tid_cntS_dids.put(tid_cntS, list);
                }
            }
            this.reporter = reporter;
        }

        @Override
        public void close() throws IOException {
            Text v = new Text();
            Text k = new Text();
            for (Path LI_path: Li_paths) {//For each Li file probe the hashtable.
                String RLi_file = "R" + String.format("%05d", this.id) + LI_path.getName().split("-")[0].substring(0, 6); //eg. R00001L00002
                // Extract tbe unique keys (did).
                Set<Integer> LI_unique_dids = new HashSet<>();
                BufferedReader br = new BufferedReader(new InputStreamReader(this.hdfs.open(LI_path)));
                String line;
                while ((line = br.readLine()) != null) {    // <did>\t[<cat_cntS>]+
                    String[] parts2 = line.split("\t", -1);
                    LI_unique_dids.add(Integer.parseInt(parts2[0]));
                }
                br.close();
                //Probe the hashtable and create the RLi file.
                for (String tid_cntS: this.tid_cntS_dids.keySet()) { //For each tid,cnt
                    k.set(tid_cntS.trim());
                    ArrayList<Integer> cand_dids = new ArrayList<>(this.tid_cntS_dids.get(tid_cntS));    //Copy the set
                    cand_dids.retainAll(LI_unique_dids);    //Contains the dids of this tid iff these dids exist in the unique keys of the Li file.
                    if (!cand_dids.isEmpty()) {
                        StringBuilder sb = new StringBuilder(); // Make a string of all the dids.
                        for (int did: cand_dids) {
                            sb.append(did).append(" ");
                        }
                        v.set(sb.toString().trim());
                        this.mos.getCollector(RLi_file, reporter).collect(k, v);// <tid_cntS> [<did>]+
                    }
                }
            }
            this.mos.close();
        }
    }

}
