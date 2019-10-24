import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created  by gstamatakis on 09-Apr-17.
 * hadoop jar your_jar.jar SkyLine input_file.csv skyline.csv #paritions random|angle 2d|3d
 * #                                    0               1           2          3        4
 */
class LocalMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    private int partitions;
    private String type;
    private String dimensions;

    @Override
    public void configure(JobConf job) {
        super.configure(job);
        partitions = Integer.parseInt(job.get("partitions"));
        type = job.get("type");
        dimensions = job.get("dimensions");
    }

    private int getPartition(Text text, String type, String dimensions, int partitions) {
        if (type.equals("random")) {
            return (new Random()).nextInt(partitions);
        }

        String[] words = text.toString().split(" , ");
        double[] dims = new double[3];

        for (int i = 1; i < 4; i++) {
            dims[i - 1] = Double.parseDouble(words[i]);
        }

        if (dimensions.equals("2d")) {
            double part_width = (90.0 / partitions);
            double phi1 = Math.toDegrees(Math.atan(Math.sqrt(Math.pow(dims[1], 2) + Math.pow(dims[2], 2)) / dims[0]));

            int temp_p = (int) (phi1 / part_width);
            if ((phi1 % part_width) != 0) {
                temp_p++;
            }
            assert temp_p - 1 < partitions;
            return temp_p - 1;
        } else {
            double phi1 = Math.toDegrees(Math.atan(Math.sqrt(Math.pow(dims[1], 2) + Math.pow(dims[2], 2)) / dims[0]));

            double phi2 = Math.toDegrees(Math.atan(Math.sqrt(Math.pow(dims[2], 2) / dims[1])));

            assert phi1 > 0 && phi2 > 0 && phi1 < 90.0 && phi2 < 90;

            if (phi1 <= 48.24) {
                if (phi2 <= 30.0) {
                    return 0;
                } else if (phi2 < 60.0) {
                    return 3;
                } else {
                    return 6;
                }
            } else if (phi1 <= 70.52) {
                if (phi2 <= 30.0) {
                    return 1;
                } else if (phi2 < 60.0) {
                    return 4;
                } else {
                    return 7;
                }
            } else if (phi1 <= 90.0) {
                if (phi2 <= 30.0) {
                    return 2;
                } else if (phi2 < 60.0) {
                    return 5;
                } else {
                    return 8;
                }
            } else {
                System.out.println("Unhandled if - else case in InputReader at 3d angle with values: " + dims[0] + dims[1] + dims[2]);
                return -1;
            }
        }
    }

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        if (key.get() == 0) {   // skip .csv headers
            return;
        }
        int partition = getPartition(value, type, dimensions, partitions);
        Text word = new Text(String.valueOf(partition));
        output.collect(word, value);
    }
}

class LocalReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
    private int dims;

    @Override
    public void configure(JobConf job) {
        super.configure(job);
        dims = job.get("dimensions").equals("2d") ? 3 : 4;
    }

    private static Text toText(double[] dims) {
        StringBuilder buf = new StringBuilder("" + String.valueOf((int) dims[0]));
        for (int i = 1; i < dims.length; i++) {
            buf.append(" , ").append(String.valueOf(dims[i]));
        }
        return new Text(buf.toString());
    }

    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        ArrayList<double[]> dataset = new ArrayList<>();
        reporter.setStatus("Reducing locally...");
        while (values.hasNext()) {
            String[] words = values.next().toString().split(" , ");
            double[] cols = new double[dims];
            for (int i = 0; i < dims; i++) {
                cols[i] = Double.parseDouble(words[i]);
            }
            dataset.add(cols);
        }

        SkylineObj skylineObj = new SkylineObj(false);
        ArrayList<double[]> result = skylineObj.skyline(dataset);

        assert result != null;
        for (double[] tuple : result) {
            output.collect(toText(tuple), null);
        }
    }
}

