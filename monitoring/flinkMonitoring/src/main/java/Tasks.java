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
public class Tasks implements Serializable {

    private final static long serialVersionUID = -1373227501152895832L;
    @JsonProperty("RECONCILING")
    public Long Reconciling;
    @JsonProperty("DEPLOYING")
    public Long Deploying;
    @JsonProperty("FINISHED")
    public Long Finished;
    @JsonProperty("RUNNING")
    public Long Running;
    @JsonProperty("CANCELED")
    public Long Canceled;
    @JsonProperty("FAILED")
    public Long Failed;
    @JsonProperty("SCHEDULED")
    public Long Scheduled;
    @JsonProperty("CANCELING")
    public Long Canceling;
    @JsonProperty("CREATED")
    public Long Created;

    @Override
    public String toString() {
        return "Tasks{" +
                "Reconciling=" + Reconciling +
                ", Deploying=" + Deploying +
                ", Finished=" + Finished +
                ", Running=" + Running +
                ", Canceled=" + Canceled +
                ", Failed=" + Failed +
                ", Scheduled=" + Scheduled +
                ", Canceling=" + Canceling +
                ", Created=" + Created +
                '}';
    }
}
