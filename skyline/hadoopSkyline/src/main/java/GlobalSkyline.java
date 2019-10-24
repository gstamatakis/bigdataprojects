import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


class GlobalMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    @Override
    public void configure(JobConf job) {
        super.configure(job);
    }

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        output.collect(new Text("1"), value);
    }
}

class GlobalReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
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
        ArrayList<double[]> GlobalDataset = new ArrayList<>();

        while (values.hasNext()) {
            String[] words = values.next().toString().split(" , ");
            double[] cols = new double[dims];
            for (int i = 0; i < dims; i++) {
                cols[i] = Double.parseDouble(words[i]);
            }
            GlobalDataset.add(cols);
        }

        SkylineObj skylineObj = new SkylineObj(true);
        ArrayList<double[]> result = skylineObj.skyline(GlobalDataset);

        assert result != null;
        for (double[] tuple : result) {
            output.collect(toText(tuple), null);
        }
    }
}

