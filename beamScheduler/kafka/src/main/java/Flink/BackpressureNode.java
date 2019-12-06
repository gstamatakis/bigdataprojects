package Flink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "status",
        "backpressure-level",
        "end-timestamp",
        "subtasks"
})
public class BackpressureNode {

    @JsonProperty("status")
    public String status;
    @JsonProperty("backpressure-level")
    public String backpressureLevel;
    @JsonProperty("end-timestamp")
    public Long endTimestamp;
    @JsonProperty("subtasks")
    public List<Subtask> subtasks;

    @Override
    public String toString() {
        return "BackpressureNode{" +
                "status='" + status + '\'' +
                ", backpressureLevel='" + backpressureLevel + '\'' +
                ", endTimestamp=" + endTimestamp +
                ", subtasks=" + subtasks +
                '}';
    }
}

