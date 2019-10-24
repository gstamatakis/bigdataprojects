package Flink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "parallelism",
        "status",
        "start-time",
        "end-time",
        "duration",
        "tasks",
        "metrics"
})
public class Vertex implements Serializable {
    public HashMap<String, LatencyNode> vertexLatencies; //Works only for sources
    public ThroughputNode throughputNode;
    public BackpressureNode backpressureNode;

    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("parallelism")
    public Integer parallelism;
    @JsonProperty("status")
    public String status;
    @JsonProperty("start-time")
    public Long startTime;
    @JsonProperty("end-time")
    public Long endTime;
    @JsonProperty("duration")
    public Long duration;
    @JsonProperty("tasks")
    public Tasks tasks;
    @JsonProperty("metrics")
    public Metrics metrics;
    private final static long serialVersionUID = -7885628830990459202L;

    @Override
    public String toString() {
        return "Vertex{" +
                "vertexLatencies=" + vertexLatencies +
                ", throughputNode=" + throughputNode +
                ", backpressureNode=" + backpressureNode +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", parallelism=" + parallelism +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", tasks=" + tasks +
                ", metrics=" + metrics +
                '}';
    }

}
