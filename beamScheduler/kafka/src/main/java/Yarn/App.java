package Yarn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "user",
        "name",
        "queue",
        "state",
        "finalStatus",
        "progress",
        "trackingUI",
        "trackingUrl",
        "diagnostics",
        "clusterId",
        "applicationType",
        "applicationTags",
        "priority",
        "startedTime",
        "finishedTime",
        "elapsedTime",
        "amContainerLogs",
        "amHostHttpAddress",
        "allocatedMB",
        "allocatedVCores",
        "runningContainers",
        "memorySeconds",
        "vcoreSeconds",
        "queueUsagePercentage",
        "clusterUsagePercentage",
        "preemptedResourceMB",
        "preemptedResourceVCores",
        "numNonAMContainerPreempted",
        "numAMContainerPreempted",
        "resourceRequests",
        "logAggregationStatus",
        "unmanagedApplication",
        "amNodeLabelExpression"
})
public class App {

    @JsonProperty("id")
    public String id;
    @JsonProperty("user")
    public String user;
    @JsonProperty("name")
    public String name;
    @JsonProperty("queue")
    public String queue;
    @JsonProperty("state")
    public String state;
    @JsonProperty("finalStatus")
    public String finalStatus;
    @JsonProperty("progress")
    public Long progress;
    @JsonProperty("trackingUI")
    public String trackingUI;
    @JsonProperty("trackingUrl")
    public String trackingUrl;
    @JsonProperty("diagnostics")
    public String diagnostics;
    @JsonProperty("clusterId")
    public Long clusterId;
    @JsonProperty("applicationType")    //Spark,Apex
    public String applicationType;
    @JsonProperty("applicationTags")
    public String applicationTags;
    @JsonProperty("priority")
    public Long priority;
    @JsonProperty("startedTime")
    public Long startedTime;
    @JsonProperty("finishedTime")
    public Long finishedTime;
    @JsonProperty("elapsedTime")
    public Long elapsedTime;
    @JsonProperty("amContainerLogs")
    public String amContainerLogs;
    @JsonProperty("amHostHttpAddress")
    public String amHostHttpAddress;
    @JsonProperty("allocatedMB")
    public Long allocatedMB;
    @JsonProperty("allocatedVCores")
    public Long allocatedVCores;
    @JsonProperty("runningContainers")
    public Long runningContainers;
    @JsonProperty("memorySeconds")  //The aggregated amount of memory (in megabytes) the application has allocated times the number of seconds the application has been running.
    public Long memorySeconds;
    @JsonProperty("vcoreSeconds")
    public Long vcoreSeconds;       //The aggregated number of vcores that the application has allocated times the number of seconds the application has been running.
    @JsonProperty("queueUsagePercentage")
    public Double queueUsagePercentage;
    @JsonProperty("clusterUsagePercentage")
    public Double clusterUsagePercentage;
    @JsonProperty("preemptedResourceMB")
    public Long preemptedResourceMB;
    @JsonProperty("preemptedResourceVCores")
    public Long preemptedResourceVCores;
    @JsonProperty("numNonAMContainerPreempted")
    public Long numNonAMContainerPreempted;
    @JsonProperty("numAMContainerPreempted")
    public Long numAMContainerPreempted;
    @JsonProperty("resourceRequests")
    public List<ResourceRequest> resourceRequests = null;
    @JsonProperty("logAggregationStatus")
    public String logAggregationStatus;
    @JsonProperty("unmanagedApplication")
    public Boolean unmanagedApplication;
    @JsonProperty("amNodeLabelExpression")
    public String amNodeLabelExpression;

    public String print(ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(this).replace("-&gt;", "->");
        } catch (JsonProcessingException e) {
            return "Can't parse YARN job: " + this.id;
        }
    }
}