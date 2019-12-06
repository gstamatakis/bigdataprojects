package main;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created  by gstamatakis on 13-May-17.
 */
public class Tests {
    public static void main(String[] args) throws IOException {
        ArrayList<String> files = new ArrayList<>();
//        files.add("hdfs://127.0.0.1:9000/input/input_file_10.csv");
//        files.add("hdfs://127.0.0.1:9000/input/input_file_100.csv");
//        files.add("hdfs://127.0.0.1:9000/input/input_file_1000.csv");
//        files.add("hdfs://127.0.0.1:9000/input/input_file_10000.csv");
//        files.add("hdfs://127.0.0.1:9000/input/input_file_100000.csv");
//        files.add("hdfs://127.0.0.1:9000/input/input_file_1000000.csv");
        files.add("hdfs://127.0.0.1:9000/input/input_file_5000000.csv");


        OutputStream os = new FileOutputStream(new File("experiment1.txt"));

        int iter = 1;
        for (String filename : files) {
            System.out.println("Filename: " + filename);

            Path inputPath = new Path(filename);
            Path localSL = new Path("hdfs://127.0.0.1:9000/output/LocalSkylines");
            Path outputPath = new Path("hdfs://127.0.0.1:9000/output/GlobalSkyline");
            String HDFSstring = "hdfs://127.0.0.1:9000";

            double time = 0;
            for (int j = 0; j < iter; j++) {
                long startTime = System.nanoTime();
                Configuration conf = new Configuration();
                conf.set("type", "angle");
                conf.set("dimensions", "3d");
                conf.set("partitions", String.valueOf(9));

                //Local Skyline Job
                JobConf job = new JobConf(conf, SkyLine.class);
                FileSystem hdfs_local = FileSystem.get(URI.create(HDFSstring), job);
                job.setJarByClass(SkyLine.class);
                job.setJobName("LocalSkylineJob:" + filename);

                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(Text.class);

                job.setMapperClass(LocalMapper.class);
                job.setMapOutputKeyClass(Text.class);
                job.setMapOutputValueClass(Text.class);
                job.setCombinerClass(LocalReducer.class);
                job.setReducerClass(LocalReducer.class);

                job.setInputFormat(TextInputFormat.class);
                job.setOutputFormat(TextOutputFormat.class);

                if (hdfs_local.exists(localSL)) {
                    hdfs_local.delete(localSL, true);
                }

                FileInputFormat.setInputPaths(job, inputPath);
                FileOutputFormat.setOutputPath(job, localSL);

                try {
                    RunningJob runningJob = JobClient.runJob(job);
                    System.out.println("Local skyline job is Successful: " + runningJob.isComplete());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

                //Global Skyline Job
                JobConf job2 = new JobConf(conf, SkyLine.class);
                FileSystem hdfs_global = FileSystem.get(URI.create(HDFSstring), job2);

                job2.setJarByClass(SkyLine.class);
                job2.setJobName("GlobalSkylineJob: " + filename);

                job2.setOutputKeyClass(Text.class);
                job2.setOutputValueClass(Text.class);

                job2.setMapperClass(GlobalMapper.class);
                job2.setMapOutputKeyClass(Text.class);
                job2.setMapOutputValueClass(Text.class);
                job2.setCombinerClass(GlobalReducer.class);
                job2.setReducerClass(GlobalReducer.class);

                job2.setOutputKeyClass(Text.class);
                job2.setOutputValueClass(Text.class);
                job2.setInputFormat(TextInputFormat.class);
                job2.setOutputFormat(TextOutputFormat.class);

                if (hdfs_global.exists(outputPath)) {
                    hdfs_global.delete(outputPath, true);
                }

                FileInputFormat.setInputPaths(job2, localSL);
                FileOutputFormat.setOutputPath(job2, outputPath);

                try {
                    RunningJob runningJob = JobClient.runJob(job2);
                    System.out.println("Global skyline job is Successful: " + runningJob.isComplete());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                time += ((System.nanoTime() - startTime) / 1000000000.0) / (double) iter;
                System.out.println("Iteration  " + (j + 1) + " : " + time);
            }

            String line = "File " + filename + " : " + time + "\n";
            os.write(line.getBytes());
        }
        os.close();
    }
}
