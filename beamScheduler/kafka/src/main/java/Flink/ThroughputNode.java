package Flink;

public class ThroughputNode {
    public TPStats numRecordsIn;
    public TPStats numRecordsInPerSecond;
    public TPStats numBytesInRemotePerSecond;
    public TPStats numRecordsOut;
    public TPStats numRecordsOutPerSecond;
    public TPStats numBytesOutPerSecond;
    public TPStats numBytesInLocalPerSecond;

    public ThroughputNode() {
    }

    public ThroughputNode(TPStats numRecordsIn, TPStats numRecordsInPerSecond, TPStats numBytesInRemotePerSecond, TPStats numRecordsOut, TPStats numRecordsOutPerSecond, TPStats numBytesOutPerSecond, TPStats numBytesInLocalPerSecond) {
        this.numRecordsIn = numRecordsIn;
        this.numRecordsInPerSecond = numRecordsInPerSecond;
        this.numBytesInRemotePerSecond = numBytesInRemotePerSecond;
        this.numRecordsOut = numRecordsOut;
        this.numRecordsOutPerSecond = numRecordsOutPerSecond;
        this.numBytesOutPerSecond = numBytesOutPerSecond;
        this.numBytesInLocalPerSecond = numBytesInLocalPerSecond;
    }

    @Override
    public String toString() {
        return "ThroughputNode{" +
                "numRecordsIn=" + numRecordsIn +
                ", numRecordsInPerSecond=" + numRecordsInPerSecond +
                ", numBytesInRemotePerSecond=" + numBytesInRemotePerSecond +
                ", numRecordsOut=" + numRecordsOut +
                ", numRecordsOutPerSecond=" + numRecordsOutPerSecond +
                ", numBytesOutPerSecond=" + numBytesOutPerSecond +
                ", numBytesInLocalPerSecond=" + numBytesInLocalPerSecond +
                '}';
    }
}
