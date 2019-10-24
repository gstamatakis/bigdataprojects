package phases;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.MultipleOutputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Read the join output of the first cycle ( cat,tid,JI ) and build a HT. <tid> [<cat> <tid> <JI>]+
 * Read the Li.uk files from the previous phase and generate the RLi files by probing the vector tuples.
 */
public class Phase5 {

    public static class Join2Phase2Mapper implements Mapper<LongWritable, Text, Text, Text> {
        private HashMap<Integer, ArrayList<String>> tidHT;// <tid> , [<cat> <tid> <JI>]+   load the whole tuple for speed.
        private FileSystem hdfs;
        private ArrayList<Path> Li_paths;
        private int id;
        private MultipleOutputs mos;
        private Reporter reporter;


        @Override
        public void configure(JobConf job) {
            this.tidHT = new HashMap<>();
            this.Li_paths = new ArrayList<>();
            String root = job.get("root");
            this.id = Integer.parseInt(job.get("mapred.task.id").split("_")[4]);
            this.mos = new MultipleOutputs(job);

            try {
                this.hdfs = FileSystem.get(new Path(root).toUri(), job);
                RemoteIterator<LocatedFileStatus> fileStatusListIterator = this.hdfs.listFiles(new Path(root + job.get("out3")), true);//Li path, aka Join1
                while (fileStatusListIterator.hasNext()) {
                    Path path = fileStatusListIterator.next().getPath();
                    if (!path.toString().contains("_SUCCESS")) {
                        this.Li_paths.add(path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void map(LongWritable key, Text value, OutputCollector<Text, Text> collector, Reporter reporter) {
            Integer tid = Integer.valueOf(value.toString().split(" ", -1)[1].split("\t")[0]);
            if (this.tidHT.containsKey(tid)) {
                this.tidHT.get(tid).add(value.toString());
            } else {
                ArrayList<String> list = new ArrayList<>();
                list.add(value.toString());
                this.tidHT.put(tid, list);
            }
            this.reporter = reporter;
        }

        @Override
        public void close() throws IOException {
            Text value = new Text();
            for (Path LI_path: Li_paths) {
                String RLi_file = "R" + String.format("%05d", this.id) + LI_path.getName().split("-")[0].substring(0, 6); //eg. R00001L00002
                BufferedReader br = new BufferedReader(new InputStreamReader(this.hdfs.open(LI_path)));
                String line;
                while ((line = br.readLine()) != null) {
                    int tid = Integer.parseInt(line.split("\t", -1)[0]);
                    if (this.tidHT.containsKey(tid)) {
                        for (String triple: this.tidHT.get(tid)) {
                            value.set(triple);
                            this.mos.getCollector(RLi_file, reporter).collect(null, value); //<cat> <tid> <JI>
                        }
                    }
                }
                br.close();
            }
            this.mos.close();
        }
    }
}
