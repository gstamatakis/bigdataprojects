public class LatencyNode {
    public String stddev;
    public String min;
    public String max;
    public String median;
    public String p99;

    public LatencyNode() {
    }

    @Override
    public String toString() {
        return "LatencyNode{" +
                "stddev='" + stddev + '\'' +
                ", min='" + min + '\'' +
                ", max='" + max + '\'' +
                ", median='" + median + '\'' +
                ", p99='" + p99 + '\'' +
                '}';
    }
}
