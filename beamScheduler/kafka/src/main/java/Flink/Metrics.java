package Flink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "read-bytes",
        "read-bytes-complete",
        "write-bytes",
        "write-bytes-complete",
        "read-records",
        "read-records-complete",
        "write-records",
        "write-records-complete"
})
public class Metrics implements Serializable {

    @JsonProperty("read-bytes")
    public Long readBytes;
    @JsonProperty("read-bytes-complete")
    public Boolean readBytesComplete;
    @JsonProperty("write-bytes")
    public Long writeBytes;
    @JsonProperty("write-bytes-complete")
    public Boolean writeBytesComplete;
    @JsonProperty("read-records")
    public Long readRecords;
    @JsonProperty("read-records-complete")
    public Boolean readRecordsComplete;
    @JsonProperty("write-records")
    public Long writeRecords;
    @JsonProperty("write-records-complete")
    public Boolean writeRecordsComplete;
    private final static long serialVersionUID = -6842658656289929507L;


    @Override
    public String toString() {
        return "Metrics{" +
                "readBytes=" + readBytes +
                ", readBytesComplete=" + readBytesComplete +
                ", writeBytes=" + writeBytes +
                ", writeBytesComplete=" + writeBytesComplete +
                ", readRecords=" + readRecords +
                ", readRecordsComplete=" + readRecordsComplete +
                ", writeRecords=" + writeRecords +
                ", writeRecordsComplete=" + writeRecordsComplete +
                '}';
    }
}
