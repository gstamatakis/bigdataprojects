package Yarn;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "appsSubmitted",
        "appsCompleted",
        "appsPending",
        "appsRunning",
        "appsFailed",
        "appsKilled",
        "reservedMB",
        "availableMB",
        "allocatedMB",
        "reservedVirtualCores",
        "availableVirtualCores",
        "allocatedVirtualCores",
        "containersAllocated",
        "containersReserved",
        "containersPending",
        "totalMB",
        "totalVirtualCores",
        "totalNodes",
        "lostNodes",
        "unhealthyNodes",
        "decommissionedNodes",
        "rebootedNodes",
        "activeNodes"
})
public class ClusterMetrics {

    @JsonProperty("appsSubmitted")
    public Long appsSubmitted;
    @JsonProperty("appsCompleted")
    public Long appsCompleted;
    @JsonProperty("appsPending")
    public Long appsPending;
    @JsonProperty("appsRunning")
    public Long appsRunning;
    @JsonProperty("appsFailed")
    public Long appsFailed;
    @JsonProperty("appsKilled")
    public Long appsKilled;
    @JsonProperty("reservedMB")
    public Long reservedMB;
    @JsonProperty("availableMB")
    public Long availableMB;
    @JsonProperty("allocatedMB")
    public Long allocatedMB;
    @JsonProperty("reservedVirtualCores")
    public Long reservedVirtualCores;
    @JsonProperty("availableVirtualCores")
    public Long availableVirtualCores;
    @JsonProperty("allocatedVirtualCores")
    public Long allocatedVirtualCores;
    @JsonProperty("containersAllocated")
    public Long containersAllocated;
    @JsonProperty("containersReserved")
    public Long containersReserved;
    @JsonProperty("containersPending")
    public Long containersPending;
    @JsonProperty("totalMB")
    public Long totalMB;
    @JsonProperty("totalVirtualCores")
    public Long totalVirtualCores;
    @JsonProperty("totalNodes")
    public Long totalNodes;
    @JsonProperty("lostNodes")
    public Long lostNodes;
    @JsonProperty("unhealthyNodes")
    public Long unhealthyNodes;
    @JsonProperty("decommissionedNodes")
    public Long decommissionedNodes;
    @JsonProperty("rebootedNodes")
    public Long rebootedNodes;
    @JsonProperty("activeNodes")
    public Long activeNodes;

}