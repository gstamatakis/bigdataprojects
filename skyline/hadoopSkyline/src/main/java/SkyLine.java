import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.net.URI;

/**
 * Created  by gstamatakis on 10-Apr-17.
 * hadoop jar your_jar.jar SkyLine input_file.csv skyline.csv #paritions random|angle 2d|3d outtime
 * #                                    0               1           2          3        4       5
 */
public class SkyLine {
    public static void main(String[] args) throws IOException {
        double time = 0;

        if (args.length < 5) {
            System.out.println("Need more arguements.\nGiven args: " + args.length);
        }
        long startTime = System.nanoTime();

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);
        Path localSL = new Path("hdfs://127.0.0.1:9000/output/LocalSkylines");
        String HDFSstring = "hdfs://127.0.0.1:9000";

        /* Local SkyLine computation */
        Configuration conf = new Configuration();
        try {
            conf.set("type", args[3]);
            conf.set("dimensions", args[4]);

            if (args[4].equals("3d")) {
                conf.set("partitions", "9");
            } else {
                conf.set("partitions", args[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Local Skyline Job
        JobConf job = new JobConf(conf, SkyLine.class);
        FileSystem hdfs_local = FileSystem.get(URI.create(HDFSstring), job);
        job.setJarByClass(SkyLine.class);
        job.setJobName("LocalSkylineJob");

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(LocalMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
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
            System.out.println("Local Skyline job is Successful: " + runningJob.isComplete());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //Global Skyline Job
        JobConf job2 = new JobConf(conf, SkyLine.class);
        FileSystem hdfs_global = FileSystem.get(URI.create(HDFSstring), job2);

        job2.setJarByClass(SkyLine.class);
        job2.setJobName("GlobalSkylineJob");

        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);

        job2.setMapperClass(GlobalMapper.class);
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
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
            System.out.println("Global Skyline job is Successful: " + runningJob.isComplete());
            time = (System.nanoTime() - startTime) / 1000000000.0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n\nTotal time of execution: " + time + " sec");
    }
}
