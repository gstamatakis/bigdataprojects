import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "attemptId",
        "startTime",
        "endTime",
        "lastUpdated",
        "duration",
        "sparkUser",
        "completed",
        "endTimeEpoch",
        "startTimeEpoch",
        "lastUpdatedEpoch"
})
public class Attempt {
    @JsonIgnoreProperties
    @JsonProperty("attemptId")
    public String attemptId;
    @JsonProperty("startTime")
    public String startTime;
    @JsonProperty("endTime")
    public String endTime;
    @JsonProperty("lastUpdated")
    public String lastUpdated;
    @JsonProperty("duration")
    public Long duration;
    @JsonProperty("sparkUser")
    public String sparkUser;
    @JsonProperty("completed")
    public Boolean completed;
    @JsonProperty("endTimeEpoch")
    public Long endTimeEpoch;
    @JsonProperty("startTimeEpoch")
    public Long startTimeEpoch;
    @JsonProperty("lastUpdatedEpoch")
    public Long lastUpdatedEpoch;

    @Override
    public String toString() {
        return "Attempt{" +
                "attemptId='" + attemptId + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", duration=" + duration +
                ", sparkUser='" + sparkUser + '\'' +
                ", completed=" + completed +
                ", endTimeEpoch=" + endTimeEpoch +
                ", startTimeEpoch=" + startTimeEpoch +
                ", lastUpdatedEpoch=" + lastUpdatedEpoch +
                '}';
    }
}