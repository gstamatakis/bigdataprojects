import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "startTime",
        "batchDuration",
        "numReceivers",
        "numActiveReceivers",
        "numInactiveReceivers",
        "numTotalCompletedBatches",
        "numRetainedCompletedBatches",
        "numActiveBatches",
        "numProcessedRecords",
        "numReceivedRecords",
        "avgInputRate",
        "avgSchedulingDelay",
        "avgProcessingTime",
        "avgTotalDelay"
})
public class StreamingStatistic {

    @JsonProperty("startTime")
    public String startTime;
    @JsonProperty("batchDuration")
    public Long batchDuration;
    @JsonProperty("numReceivers")
    public Long numReceivers;
    @JsonProperty("numActiveReceivers")
    public Long numActiveReceivers;
    @JsonProperty("numInactiveReceivers")
    public Long numInactiveReceivers;
    @JsonProperty("numTotalCompletedBatches")
    public Long numTotalCompletedBatches;
    @JsonProperty("numRetainedCompletedBatches")
    public Long numRetainedCompletedBatches;
    @JsonProperty("numActiveBatches")
    public Long numActiveBatches;
    @JsonProperty("numProcessedRecords")
    public Long numProcessedRecords;
    @JsonProperty("numReceivedRecords")
    public Long numReceivedRecords;
    @JsonProperty("avgInputRate")
    public Long avgInputRate;           //Records per second
    @JsonProperty("avgSchedulingDelay")
    public Long avgSchedulingDelay;
    @JsonProperty("avgProcessingTime")
    public Long avgProcessingTime;
    @JsonProperty("avgTotalDelay")
    public Long avgTotalDelay;

    @Override
    public String toString() {
        return "StreamingStatistic{" +
                "startTime='" + startTime + '\'' +
                ", batchDuration=" + batchDuration +
                ", numReceivers=" + numReceivers +
                ", numActiveReceivers=" + numActiveReceivers +
                ", numInactiveReceivers=" + numInactiveReceivers +
                ", numTotalCompletedBatches=" + numTotalCompletedBatches +
                ", numRetainedCompletedBatches=" + numRetainedCompletedBatches +
                ", numActiveBatches=" + numActiveBatches +
                ", numProcessedRecords=" + numProcessedRecords +
                ", numReceivedRecords=" + numReceivedRecords +
                ", avgInputRate=" + avgInputRate +
                ", avgSchedulingDelay=" + avgSchedulingDelay +
                ", avgProcessingTime=" + avgProcessingTime +
                ", avgTotalDelay=" + avgTotalDelay +
                '}';
    }
}

/**
 * http://rserver01:8189/proxy/application_1532029170450_0623/api/v1/applications/application_1532029170450_0623/streaming/statistics
 * <p>
 * {
 * "startTime": "2018-10-29T15:40:58.896GMT",
 * "batchDuration": 1000,
 * "numReceivers": 1,
 * "numActiveReceivers": 0,
 * "numInactiveReceivers": 1,
 * "numTotalCompletedBatches": 2332,
 * "numRetainedCompletedBatches": 1000,
 * "numActiveBatches": 0,
 * "numProcessedRecords": 0,
 * "numReceivedRecords": 0,
 * "avgInputRate": 0,
 * "avgSchedulingDelay": 0,
 * "avgProcessingTime": 24,
 * "avgTotalDelay": 25
 * }
 */