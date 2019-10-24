package Flink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "FINISHED",
        "RESTARTING",
        "RUNNING",
        "FAILING",
        "FAILED",
        "CANCELED",
        "SUSPENDED",
        "RECONCILING",
        "CREATED",
        "CANCELLING",
        "SUSPENDING"
})
public class Timestamps implements Serializable {

    @JsonProperty("FINISHED")
    public Long Finished;
    @JsonProperty("RESTARTING")
    public Long Restarting;
    @JsonProperty("RUNNING")
    public Long Running;
    @JsonProperty("FAILING")
    public Long Failing;
    @JsonProperty("FAILED")
    public Long Failed;
    @JsonProperty("CANCELED")
    public Long Canceled;
    @JsonProperty("SUSPENDED")
    public Long Suspended;
    @JsonProperty("RECONCILING")
    public Long Reconciling;
    @JsonProperty("CREATED")
    public Long Created;
    @JsonProperty("CANCELLING")
    public Long Cancelling;
    @JsonProperty("SUSPENDING")
    public Long Suspending;
    private final static long serialVersionUID = -2500126687391811486L;


    @Override
    public String toString() {
        return "Timestamps{" +
                "Finished=" + Finished +
                ", Restarting=" + Restarting +
                ", Running=" + Running +
                ", Failing=" + Failing +
                ", Failed=" + Failed +
                ", Canceled=" + Canceled +
                ", Suspended=" + Suspended +
                ", Reconciling=" + Reconciling +
                ", Created=" + Created +
                ", Cancelling=" + Cancelling +
                ", Suspending=" + Suspending +
                '}';
    }
}
