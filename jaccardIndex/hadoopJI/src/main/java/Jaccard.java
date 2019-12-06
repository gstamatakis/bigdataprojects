import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.LazyOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputs;
import phases.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class Jaccard {
    static Instant total_time;

    public static void main(String[] args) throws Exception {
        total_time = Instant.now();
        String root = args[0];
        String datasetsPath = args[1];
        int vector_idx = Integer.parseInt(args[2]); //Chose which vector file you want to load (parts 0-3 and 4 for the test_vector).
        int m1 = Integer.parseInt(args[3]);
        int r1 = Integer.parseInt(args[4]);
        int m2 = Integer.parseInt(args[5]);
        int r2 = Integer.parseInt(args[6]);
        int r3 = Integer.parseInt(args[7]);
        int m4 = Integer.parseInt(args[8]);
        int r6 = Integer.parseInt(args[9]);

        String outputPath1 = "phase1";
        String outputPath15 = "phase15";
        String outputPath2 = "phase2";
        String final_out1 = "phase3";
        String outputPath3 = "phase4";
        String outputPath4 = "phase5";
        String final_out2 = "phase6";

        String fname2 = root + datasetsPath + "/" + "rcv1-v2.topics.qrels";     // rcv1-v2.topics.qrels:      <category name> <did> 1
        String fname3 = root + datasetsPath + "/" + "stem.termid.idf.map.txt";  // stem.termid.idf.map.txt:   <stem> <tid> <idf>

        String[] vectors = new String[]{
                root + datasetsPath + "/" + "lyrl2004_vectors_test_pt0.dat",    // lyrl2004_vectors_test_pt0.dat.gz:    <did> [<tid>:<weight>]+
                root + datasetsPath + "/" + "lyrl2004_vectors_test_pt1.dat",
                root + datasetsPath + "/" + "lyrl2004_vectors_test_pt2.dat",
                root + datasetsPath + "/" + "lyrl2004_vectors_test_pt3.dat",
                root + datasetsPath + "/" + "lyrl2004_vectors_train.dat"
        };

        Configuration config = new Configuration();
        FileSystem hdfs = FileSystem.get(new Path(root).toUri(), config);
        int _successCnt = 0;

        config.set("root", root);
        config.set("out1", outputPath1);
        config.set("out15", outputPath15);
        config.set("out2", outputPath2);
        config.set("final_out1", final_out1);
        config.set("out3", outputPath3);
        config.set("out4", outputPath4);
        config.set("final_out2", final_out2);

        delPath(new Path(root + "/" + outputPath1), hdfs, true);
        delPath(new Path(root + "/" + outputPath15), hdfs, true);
        delPath(new Path(root + "/" + outputPath2), hdfs, true);
        delPath(new Path(root + "/" + final_out1), hdfs, true);
        delPath(new Path(root + "/" + outputPath3), hdfs, true);
        delPath(new Path(root + "/" + outputPath4), hdfs, true);
        delPath(new Path(root + "/" + final_out2), hdfs, true);
        delPath(new Path(root + "/JOIN"), hdfs, true);

        /*
         * Phase 1
         */

        JobConf job1 = new JobConf(config, Jaccard.class);
        job1.setJarByClass(Jaccard.class);
        job1.setJobName("Phase 1");

        job1.setMapperClass(Phase1.Join1Phase1Mapper.class);
        job1.setPartitionerClass(Phase1.Join1Phase1Partitioner.class);
        job1.setCombinerClass(Phase1.Join1Phase1Combiner.class);
        job1.setReducerClass(Phase1.Join1Phase1Reducer.class);

        job1.setInputFormat(TextInputFormat.class);
        LazyOutputFormat.setOutputFormatClass(job1, TextOutputFormat.class);    //Lazy output (write file ONLY if it has records)
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        job1.setNumMapTasks(m1);
        job1.setNumReduceTasks(r1);
        FileInputFormat.setInputPaths(job1, new Path(fname2));
        FileOutputFormat.setOutputPath(job1, new Path(root + "/" + outputPath1));

        runJob(job1);

        if (hdfs.exists(new Path(root + outputPath1 + "/_SUCCESS"))) {
            delPath(new Path(root + outputPath1 + "/_SUCCESS"), hdfs, false);
            _successCnt++;
        }


        /*
         *  Phase 1.5
         */
        JobConf job15 = new JobConf(config, Jaccard.class);
        job15.setJarByClass(Jaccard.class);
        job15.setJobName("Phase 1.5");

        job15.setMapperClass(Phase15.Join1Phase15Mapper.class);
        job15.setPartitionerClass(Phase15.Join1Phase15Partitioner.class);
        job15.setCombinerClass(Phase15.Join1Phase15Combiner.class);
        job15.setReducerClass(Phase15.Join1Phase15Reducer.class);

        job15.setInputFormat(TextInputFormat.class);
        LazyOutputFormat.setOutputFormatClass(job15, TextOutputFormat.class);
        job15.setOutputKeyClass(Text.class);
        job15.setOutputValueClass(Text.class);
        job15.setNumMapTasks(m1);
        job15.setNumReduceTasks(r1);
        FileInputFormat.setInputPaths(job15, new Path(root + "/" + outputPath1));
        FileOutputFormat.setOutputPath(job15, new Path(root + "/" + outputPath15));

        for (int i = 0; i < r1; i++) {
            MultipleOutputs.addNamedOutput(job15, String.format("L%05d", i), TextOutputFormat.class, Text.class, Text.class);
        }

        runJob(job15);

        if (hdfs.exists(new Path(root + outputPath15 + "/_SUCCESS"))) {
            delPath(new Path(root + outputPath15 + "/_SUCCESS"), hdfs, false);
            _successCnt++;
        }

        /*
         * PHASE 2
         */

        JobConf job2 = new JobConf(config, Jaccard.class);
        job2.setJarByClass(Jaccard.class);
        job2.setJobName("Phase 2");

        job2.setMapperClass(Phase2.Join1Phase2Mapper.class);
        job2.setPartitionerClass(Phase2.Join1Phase2Partitioner.class);
        job2.setCombinerClass(Phase2.Join1Phase2Combiner.class);
        job2.setReducerClass(Phase2.Join1Phase2Reducer.class);
        job2.setNumMapTasks(m2);
        job2.setNumReduceTasks(r2);

        job2.setInputFormat(TextInputFormat.class);
        LazyOutputFormat.setOutputFormatClass(job2, TextOutputFormat.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);

        //Add vector file(s).
        if (vector_idx == -1) { //Add everything
            for (int i = 0; i < 5; i++) {
                FileInputFormat.addInputPath(job2, new Path(vectors[i]));
            }
        } else if (vector_idx < -1 || vector_idx > 4) { //Invalid index
            throw new IllegalStateException("Vector file index out of bounds!");
        } else {    //Add a part (pt0-pt3 and 4 for test_vector file).
            FileInputFormat.setInputPaths(job2, new Path(vectors[vector_idx]));
        }

        //Add the named outputs that will be used to distinguish between partitions.
        for (int i = 0; i < r2; i++) {  //for each Ri.
            for (int j = 0; j < r1; j++) { // for each Li.uk file.
                MultipleOutputs.addNamedOutput(job2, "R" + String.format("%05d", i) + "L" + String.format("%05d", j),//eg. R00002L00001
                        TextOutputFormat.class, TextOutputFormat.class, Text.class);
            }
        }
        FileOutputFormat.setOutputPath(job2, new Path(root + "/" + outputPath2));

        runJob(job2);

        if (hdfs.exists(new Path(root + outputPath2 + "/_SUCCESS"))) {
            delPath(new Path(root + outputPath2 + "/_SUCCESS"), hdfs, false);
            _successCnt++;
        }

        /*
         *PHASE 3
         */
        JobConf job3 = new JobConf(config, Jaccard.class);
        job3.setJarByClass(Jaccard.class);
        job3.setJobName("Phase 3");
        job3.setMapperClass(Phase3.Join1Phase3Mapper.class);
        job3.setCombinerClass(Phase3.Join1Phase3Combiner.class);
        job3.setPartitionerClass(Phase3.Join1Phase3Partitioner.class);
        job3.setReducerClass(Phase3.Join1Phase3Reducer.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(Text.class);
        LazyOutputFormat.setOutputFormatClass(job3, TextOutputFormat.class);
        job3.setOutputFormat(TextOutputFormat.class);
        job3.setNumMapTasks(r1);    //The input is Li files.
        job3.setNumReduceTasks(r3);
        FileInputFormat.setInputPaths(job3, root + "/" + outputPath15);
        FileOutputFormat.setOutputPath(job3, new Path(root + "/" + final_out1));

        runJob(job3);

        if (hdfs.exists(new Path(root + final_out1 + "/_SUCCESS"))) {
            delPath(new Path(root + final_out1 + "/_SUCCESS"), hdfs, false);
            _successCnt++;
        }

///////////////////////////////////////////////////////////////////////////////////////////////////////////

        JobConf job4 = new JobConf(config, Jaccard.class);
        job4.setJarByClass(Jaccard.class);
        job4.setJobName("Phase 4");

        job4.set("tasks_4", String.valueOf(m4));
        job4.setMapperClass(Phase4.Join2Phase1Mapper.class);
        job4.setPartitionerClass(Phase4.Join2Phase1Partitioner.class);
        job4.setInputFormat(TextInputFormat.class);
        LazyOutputFormat.setOutputFormatClass(job4, TextOutputFormat.class);
        job4.setOutputKeyClass(IntWritable.class);
        job4.setOutputValueClass(Text.class);
        job4.setNumMapTasks(m4);
        job4.setNumReduceTasks(0);
        FileInputFormat.setInputPaths(job4, new Path(fname3));
        FileOutputFormat.setOutputPath(job4, new Path(root + "/" + outputPath3));

        for (int i = 0; i < m4; i++) {
            MultipleOutputs.addNamedOutput(job4, "L" + String.format("%05d", i), TextOutputFormat.class, IntWritable.class, Text.class);
        }

        runJob(job4);

        if (hdfs.exists(new Path(root + outputPath3 + "/_SUCCESS"))) {
            delPath(new Path(root + outputPath3 + "/_SUCCESS"), hdfs, false);
            _successCnt++;
        }

        JobConf job5 = new JobConf(config, Jaccard.class);
        job5.setJarByClass(Jaccard.class);
        job5.setJobName("Phase 5");

        job5.setMapperClass(Phase5.Join2Phase2Mapper.class);
        job5.setNumMapTasks(r3);
        job5.setNumReduceTasks(0);

        job5.setInputFormat(TextInputFormat.class);
        LazyOutputFormat.setOutputFormatClass(job5, TextOutputFormat.class);
        job5.setOutputKeyClass(Text.class);
        job5.setOutputValueClass(Text.class);
        FileInputFormat.setInputPaths(job5, new Path(root + "/" + final_out1));
        FileOutputFormat.setOutputPath(job5, new Path(root + "/" + outputPath4));

        for (int i = 0; i < r3; i++) {  //Ri files are the output of the 1st join.
            for (int j = 0; j < m4; j++) {  //Li files are the output of phase 4.
                MultipleOutputs.addNamedOutput(job5, "R" + String.format("%05d", i) + "L" + String.format("%05d", j), TextOutputFormat.class, TextOutputFormat.class, Text.class);
            }
        }

        runJob(job5);

        if (hdfs.exists(new Path(root + outputPath4 + "/_SUCCESS"))) {
            delPath(new Path(root + outputPath4 + "/_SUCCESS"), hdfs, false);
            _successCnt++;
        }

        JobConf job6 = new JobConf(config, Jaccard.class);
        job6.setJarByClass(Jaccard.class);
        job6.setJobName("Phase 6");

        job6.setMapperClass(Phase6.Join2Phase3Mapper.class);
        job6.setReducerClass(Phase6.Join2Phase3Reducer.class);
        job6.setOutputKeyClass(Text.class);
        job6.setOutputValueClass(Text.class);
        LazyOutputFormat.setOutputFormatClass(job6, TextOutputFormat.class);
        job6.setOutputFormat(TextOutputFormat.class);
        job6.setNumMapTasks(m4);
        job6.setNumReduceTasks(r6);
        FileInputFormat.setInputPaths(job6, root + "/" + outputPath3);
        FileOutputFormat.setOutputPath(job6, new Path(root + "/" + final_out2));

        runJob(job6);

        if (hdfs.exists(new Path(root + final_out2 + "/_SUCCESS"))) {
            delPath(new Path(root + final_out2 + "/_SUCCESS"), hdfs, false);
            _successCnt++;
        }

        hdfs.close();
        System.out.println("\nDriver finished with " + _successCnt + " _SUCCESS. Total execution time: " + Duration.between(total_time, Instant.now()).toMillis() + " ms.");
        System.exit(0);
    }


    private static void delPath(Path path, FileSystem fs1, boolean print) throws IOException {
        if (fs1.exists(path)) {
            fs1.delete(path, true);
            if (print) {
                System.out.println("Deleted : " + path.toString());
            }
        }
    }

    private static void runJob(JobConf job) {
        try {
            Instant time = Instant.now();
            RunningJob runningJob = JobClient.runJob(job);
            System.out.println(job.getJobName() + " exit status: " + runningJob.isComplete() + ". Took " + Duration.between(time, Instant.now()).toMillis() + " ms");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
