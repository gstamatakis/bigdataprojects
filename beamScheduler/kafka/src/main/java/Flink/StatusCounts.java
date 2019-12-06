package Flink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "RECONCILING",
        "DEPLOYING",
        "FINISHED",
        "RUNNING",
        "CANCELED",
        "FAILED",
        "SCHEDULED",
        "CANCELING",
        "CREATED"
})
public class StatusCounts implements Serializable {

    @JsonProperty("RECONCILING")
    public Long rECONCILING;
    @JsonProperty("DEPLOYING")
    public Long dEPLOYING;
    @JsonProperty("FINISHED")
    public Long fINISHED;
    @JsonProperty("RUNNING")
    public Long rUNNING;
    @JsonProperty("CANCELED")
    public Long cANCELED;
    @JsonProperty("FAILED")
    public Long fAILED;
    @JsonProperty("SCHEDULED")
    public Long sCHEDULED;
    @JsonProperty("CANCELING")
    public Long cANCELING;
    @JsonProperty("CREATED")
    public Long cREATED;
    private final static long serialVersionUID = -3460687372492235558L;


    @Override
    public String toString() {
        return "StatusCounts{" +
                "rECONCILING=" + rECONCILING +
                ", dEPLOYING=" + dEPLOYING +
                ", fINISHED=" + fINISHED +
                ", rUNNING=" + rUNNING +
                ", cANCELED=" + cANCELED +
                ", fAILED=" + fAILED +
                ", sCHEDULED=" + sCHEDULED +
                ", cANCELING=" + cANCELING +
                ", cREATED=" + cREATED +
                '}';
    }
}
