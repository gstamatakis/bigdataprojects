package CoreUtils;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import static java.lang.Math.floor;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;

@SuppressWarnings("Duplicates")
public class Histogram {
    private List<Double> data;

    public Histogram(List<Double> data) {
        this.data = data;
    }

    private static Integer findBin(Double datum, int bins, double min, double max) {
        final double binWidth = (max - min) / bins;
        if (datum >= max) {
            return bins - 1;
        } else if (datum <= min) {
            return 0;
        }
        return (int) floor((datum - min) / binWidth);
    }

    public Map<Integer, Integer> histogram(int bins) {
        final DoubleSummaryStatistics statistics = this.data.stream()
                .mapToDouble(x -> x)
                .summaryStatistics();

        final double max = statistics.getMax();
        final double min = statistics.getMin();

        final Map<Integer, Integer> histogram = range(0, bins).boxed()
                .collect(toMap(identity(), x -> 0));

        histogram.putAll(this.data.stream()
                .collect(groupingBy(x -> findBin(x, bins, min, max),
                        mapping(x -> 1, summingInt(x -> x)))));

        return histogram;
    }

    public double calculateMean(List<Double> array) {
        double sum = 0;
        for (double v : array) {
            sum += v;
        }
        return sum / array.size();
    }

    //this method calculats the standard deviation of the values in the array
    //recall, if x represents a value in the array, then the standard deviation
    //is defined as the square root of the quotient of (x-mean)^2 summed over all
    //possible values of x, and the quantity (n-1)
    public double calculateStandardDeviation() {

        //the mean of the data is used in the formula for the standard deviation
        //in several places, so calculate it once, and store it in a variable
        double mean = calculateMean(this.data);

        //find sum of the squared differences between the data values and the mean
        double sum = 0;
        for (Double datum : this.data) {
            sum += Math.pow((datum - mean), 2);
        }

        //calculate the variance (which is a bias-corrected "average" of the
        //sum of the squared differences between the data values and the mean)
        double variance = sum / (this.data.size() - 1);

        //calculate the standard deviation (which is the square root of the variance)

        return Math.sqrt(variance);
    }

    //https://help.gooddata.com/display/doc/Normality+Testing+-+Skewness+and+Kurtosis
    public double calculateSkewness() {
        if (this.data.isEmpty()) {
            return 0;
        }
        double mean = this.calculateMean(this.data);
        double nom = 0, denom = 0;
        int n = this.data.size();

        for (double x : this.data) {
            nom += Math.pow(x - mean, 3);
        }
        nom /= n;

        for (double x : this.data) {
            denom += Math.pow(x - mean, 2);
        }
        denom /= n;
        denom = Math.pow(denom, 3 / (double) 2);

        return Math.abs(nom / denom);   //Symmetry around 0
    }

    public double calculateKurtosis() {
        if (this.data.isEmpty()) {
            return 0;
        }
        double mean = this.calculateMean(this.data);
        double nom = 0, denom = 0;
        int n = this.data.size();

        for (double x : this.data) {
            nom += Math.pow(x - mean, 4);
        }
        nom /= n;

        for (double x : this.data) {
            denom += Math.pow(x - mean, 2);
        }
        denom /= n;
        denom = Math.pow(denom, 2);

        return (nom / denom) - 3;
    }

    public String toString(int bins) {
        final Map<Integer, Integer> frequencies = this.histogram(bins);
        final int maxFrequency = frequencies.values().stream()
                .mapToInt(x -> x).max().orElse(0);

        String[] CHARACTERS = {"0", "1", "2", "3", "4", "5"};
        return frequencies.values().stream()
                .mapToDouble(x -> x / (double) maxFrequency)
                .mapToInt(x -> findBin(x, CHARACTERS.length, 0, 1))
                .mapToObj(x -> CHARACTERS[x])
                .collect(joining());
    }

    public static Histogram from(List<Double> data) {
        return new Histogram(data);
    }

}