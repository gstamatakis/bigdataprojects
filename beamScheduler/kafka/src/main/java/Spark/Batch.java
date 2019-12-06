package Spark;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "batchId",
        "firstFailureReason",
        "batchTime",
        "status",
        "batchDuration",
        "inputSize",
        "schedulingDelay",
        "numActiveOutputOps",
        "numCompletedOutputOps",
        "numFailedOutputOps",
        "numTotalOutputOps",
        "processingTime",
        "totalDelay"
})
public class Batch {


    @JsonProperty("batchId")
    public Long batchId;
    @JsonProperty("firstFailureReason")
    @JsonIgnore
    public String firstFailureReason;
    @JsonProperty("batchTime")
    public String batchTime;
    @JsonProperty("status")
    public String status;
    @JsonProperty("batchDuration")
    public Long batchDuration;
    @JsonProperty("inputSize")
    public Long inputSize;
    @JsonProperty("schedulingDelay")
    public Long schedulingDelay;
    @JsonProperty("numActiveOutputOps")
    public Long numActiveOutputOps;
    @JsonProperty("numCompletedOutputOps")
    public Long numCompletedOutputOps;
    @JsonProperty("numFailedOutputOps")
    public Long numFailedOutputOps;
    @JsonProperty("numTotalOutputOps")
    public Long numTotalOutputOps;
    @JsonProperty("processingTime")
    public Long processingTime;
    @JsonProperty("totalDelay")
    public Long totalDelay;

    @Override
    public String toString() {
        return "Batch{" +
                "batchId=" + batchId +
                ", batchTime='" + batchTime + '\'' +
                ", status='" + status + '\'' +
                ", batchDuration=" + batchDuration +
                ", inputSize=" + inputSize +
                ", schedulingDelay=" + schedulingDelay +
                ", numActiveOutputOps=" + numActiveOutputOps +
                ", numCompletedOutputOps=" + numCompletedOutputOps +
                ", numFailedOutputOps=" + numFailedOutputOps +
                ", numTotalOutputOps=" + numTotalOutputOps +
                ", processingTime=" + processingTime +
                ", totalDelay=" + totalDelay +
                '}';
    }
}

/**
 * http://rserver01:8189/proxy/application_1532029170450_0623/api/v1/applications/application_1532029170450_0623/streaming/batches
 * <p>
 * [
 * {
 * "batchId": 1541490297000,
 * "batchTime": "2018-11-06T07:44:57.000GMT",
 * "status": "COMPLETED",
 * "batchDuration": 1000,
 * "inputSize": 0,
 * "schedulingDelay": 0,
 * "processingTime": 31,
 * "totalDelay": 31,
 * "numActiveOutputOps": 0,
 * "numCompletedOutputOps": 1,
 * "numFailedOutputOps": 0,
 * "numTotalOutputOps": 1
 * },
 * {
 * "batchId": 1541490296000,
 * "batchTime": "2018-11-06T07:44:56.000GMT",
 * "status": "COMPLETED",
 * "batchDuration": 1000,
 * "inputSize": 0,
 * "schedulingDelay": 1,
 * "processingTime": 32,
 * "totalDelay": 33,
 * "numActiveOutputOps": 0,
 * "numCompletedOutputOps": 1,
 * "numFailedOutputOps": 0,
 * "numTotalOutputOps": 1
 * },
 * <p>
 * ...
 */