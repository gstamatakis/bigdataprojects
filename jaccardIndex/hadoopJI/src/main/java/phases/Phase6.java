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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Read the Li.uk files (Join2Phase1Mapper) and build a HT.
 * Read the correct RLi file and probe the HT.
 * If a stem isn't found simply emmit its tid.
 */
public class Phase6 {
    public static class Join2Phase3Mapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
        private String Li_suffix;
        private HashMap<Integer, String> tid_stem; // <tid> <stem>
        private OutputCollector<Text, Text> outputCollector;
        private FileSystem hdfs;
        private ArrayList<Path> RLi_paths;

        @Override
        public void configure(JobConf job) {
            this.RLi_paths = new ArrayList<>();
            this.tid_stem = new HashMap<>();
            this.Li_suffix = (new Path(job.get("map.input.file"))).getName().substring(0, 6); //Get the input file name suffix. eg. L00001

            try {
                String root = job.get("root");
                this.hdfs = FileSystem.get(new Path(root).toUri(), job);
                RemoteIterator<LocatedFileStatus> fileStatusListIterator = hdfs.listFiles(new Path(root + job.get("out4")), true);
                while (fileStatusListIterator.hasNext()) {
                    RLi_paths.add(fileStatusListIterator.next().getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override   //Read the Li files containing <tid> <stem>
        public void map(LongWritable key, Text line, OutputCollector<Text, Text> outputCollector, Reporter reporter) {
            String[] parts = line.toString().split("\t");
            this.tid_stem.put(Integer.parseInt(parts[0]), parts[1]); //build the HT with tuples: <tid> <stem>  (tid-stem  is 1-1)
            this.outputCollector = outputCollector;
        }

        @Override
        public void close() throws IOException {
            Text key = new Text();
            Text val = new Text();
            for (Path RLi: this.RLi_paths) {
                if (RLi.toString().contains(this.Li_suffix)) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(hdfs.open(RLi))); //Fetch the correct RLi
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\t", -1);      // <cat> <tid> \t <JI>
                        String[] tokens = parts[0].split(" ", -1);  // <cat> <tid> , <JI>
                        val.set(parts[1]);  //<JI>
                        int tid = Integer.parseInt(tokens[1]);
                        if (this.tid_stem.containsKey(tid)) {
                            key.set(tokens[0] + " " + this.tid_stem.get(tid));//<cat> <tid>
                            this.outputCollector.collect(key, val);
                        }
                    }
                    br.close();
                }
            }
        }
    }

    /*
     * This is just for merging the result in fewer files (usually just 1).
     * Keep in mind that the incoming tuples are always unique.
     */
    public static class Join2Phase3Reducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            output.collect(key, values.next());
        }
    }
}
