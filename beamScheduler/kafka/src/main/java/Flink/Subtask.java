package Flink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "subtask",
        "backpressure-level",
        "ratio"
})
public class Subtask {

    @JsonProperty("subtask")
    public Long subtask;
    @JsonProperty("backpressure-level")
    public String backpressureLevel;
    @JsonProperty("ratio")
    public Double ratio;

    @Override
    public String toString() {
        return "Subtask{" +
                "subtask=" + subtask +
                ", backpressureLevel='" + backpressureLevel + '\'' +
                ", ratio=" + ratio +
                '}';
    }
}
