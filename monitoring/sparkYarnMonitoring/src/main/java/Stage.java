import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "status",
        "stageId",
        "attemptId",
        "numActiveTasks",
        "numCompleteTasks",
        "numFailedTasks",
        "executorRunTime",
        "executorCpuTime",
        "submissionTime",
        "firstTaskLaunchedTime",
        "inputBytes",
        "inputRecords",
        "outputBytes",
        "outputRecords",
        "shuffleReadBytes",
        "shuffleReadRecords",
        "shuffleWriteBytes",
        "shuffleWriteRecords",
        "memoryBytesSpilled",
        "diskBytesSpilled",
        "name",
        "details",
        "schedulingPool",
        "accumulatorUpdates",
        "completionTime"
})
public class Stage {

    @JsonProperty("status")
    public String status;
    @JsonProperty("stageId")
    public Long stageId;
    @JsonProperty("attemptId")
    public Long attemptId;
    @JsonProperty("numActiveTasks")
    public Long numActiveTasks;
    @JsonProperty("numCompleteTasks")
    public Long numCompleteTasks;
    @JsonProperty("numFailedTasks")
    public Long numFailedTasks;
    @JsonProperty("executorRunTime")
    public Long executorRunTime;
    @JsonProperty("executorCpuTime")
    public Long executorCpuTime;
    @JsonProperty("submissionTime")
    public String submissionTime;
    @JsonProperty("firstTaskLaunchedTime")
    public String firstTaskLaunchedTime;
    @JsonProperty("inputBytes")
    public Long inputBytes;
    @JsonProperty("inputRecords")
    public Long inputRecords;
    @JsonProperty("outputBytes")
    public Long outputBytes;
    @JsonProperty("outputRecords")
    public Long outputRecords;
    @JsonProperty("shuffleReadBytes")
    public Long shuffleReadBytes;
    @JsonProperty("shuffleReadRecords")
    public Long shuffleReadRecords;
    @JsonProperty("shuffleWriteBytes")
    public Long shuffleWriteBytes;
    @JsonProperty("shuffleWriteRecords")
    public Long shuffleWriteRecords;
    @JsonProperty("memoryBytesSpilled")
    public Long memoryBytesSpilled;
    @JsonProperty("diskBytesSpilled")
    public Long diskBytesSpilled;
    @JsonProperty("name")
    public String name;
    @JsonProperty("details")
    public String details;
    @JsonProperty("schedulingPool")
    public String schedulingPool;
    @JsonProperty("accumulatorUpdates")
    public List<AccumulatorUpdate> accumulatorUpdates = null;
    @JsonProperty("completionTime")
    public String completionTime;

    @Override
    public String toString() {
        return "Stage{" +
                "status='" + status + '\'' +
                ", stageId=" + stageId +
                ", attemptId=" + attemptId +
                ", numActiveTasks=" + numActiveTasks +
                ", numCompleteTasks=" + numCompleteTasks +
                ", numFailedTasks=" + numFailedTasks +
                ", executorRunTime=" + executorRunTime +
                ", executorCpuTime=" + executorCpuTime +
                ", submissionTime='" + submissionTime + '\'' +
                ", firstTaskLaunchedTime='" + firstTaskLaunchedTime + '\'' +
                ", inputBytes=" + inputBytes +
                ", inputRecords=" + inputRecords +
                ", outputBytes=" + outputBytes +
                ", outputRecords=" + outputRecords +
                ", shuffleReadBytes=" + shuffleReadBytes +
                ", shuffleReadRecords=" + shuffleReadRecords +
                ", shuffleWriteBytes=" + shuffleWriteBytes +
                ", shuffleWriteRecords=" + shuffleWriteRecords +
                ", memoryBytesSpilled=" + memoryBytesSpilled +
                ", diskBytesSpilled=" + diskBytesSpilled +
                ", name='" + name + '\'' +
                ", details='" + details + '\'' +
                ", schedulingPool='" + schedulingPool + '\'' +
                ", accumulatorUpdates=" + accumulatorUpdates +
                ", completionTime='" + completionTime + '\'' +
                '}';
    }
}


/**
 * Example
 * <p>
 * {
 * "status": "COMPLETE",
 * "stageId": 2990,
 * "attemptId": 0,
 * "numActiveTasks": 0,
 * "numCompleteTasks": 2,
 * "numFailedTasks": 0,
 * "executorRunTime": 2,
 * "executorCpuTime": 1357893,
 * "submissionTime": "2018-10-29T15:53:25.021GMT",
 * "firstTaskLaunchedTime": "2018-10-29T15:53:25.021GMT",
 * "completionTime": "2018-10-29T15:53:25.027GMT",
 * "inputBytes": 0,
 * "inputRecords": 0,
 * "outputBytes": 0,
 * "outputRecords": 0,
 * "shuffleReadBytes": 0,
 * "shuffleReadRecords": 0,
 * "shuffleWriteBytes": 0,
 * "shuffleWriteRecords": 0,
 * "memoryBytesSpilled": 0,
 * "diskBytesSpilled": 0,
 * "name": "print at NetworkWordCount.scala:56",
 * "details": "org.apache.spark.streaming.dstream.DStream.print(DStream.scala:724)\norg.apache.spark.examples.streaming.NetworkWordCount$.main(NetworkWordCount.scala:56)\norg.apache.spark.examples.streaming.NetworkWordCount.main(NetworkWordCount.scala)\nsun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\nsun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\nsun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\njava.lang.reflect.Method.invoke(Method.java:498)\norg.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:782)\norg.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:180)\norg.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:205)\norg.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:119)\norg.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)",
 * "schedulingPool": "default",
 * "accumulatorUpdates": [
 * {
 * "id": 35935,
 * "name": "internal.metrics.memoryBytesSpilled",
 * "value": "0"
 * },
 * {
 * "id": 35943,
 * "name": "internal.metrics.shuffle.read.fetchWaitTime",
 * "value": "0"
 * },
 * {
 * "id": 35928,
 * "name": "internal.metrics.executorDeserializeTime",
 * "value": "6"
 * },
 * {
 * "id": 35937,
 * "name": "internal.metrics.peakExecutionMemory",
 * "value": "0"
 * },
 * {
 * "id": 35940,
 * "name": "internal.metrics.shuffle.read.localBlocksFetched",
 * "value": "0"
 * },
 * {
 * "id": 35931,
 * "name": "internal.metrics.executorCpuTime",
 * "value": "1357893"
 * },
 * {
 * "id": 35939,
 * "name": "internal.metrics.shuffle.read.remoteBlocksFetched",
 * "value": "0"
 * },
 * {
 * "id": 35930,
 * "name": "internal.metrics.executorRunTime",
 * "value": "2"
 * },
 * {
 * "id": 35942,
 * "name": "internal.metrics.shuffle.read.localBytesRead",
 * "value": "0"
 * },
 * {
 * "id": 35936,
 * "name": "internal.metrics.diskBytesSpilled",
 * "value": "0"
 * },
 * {
 * "id": 35944,
 * "name": "internal.metrics.shuffle.read.recordsRead",
 * "value": "0"
 * },
 * {
 * "id": 35929,
 * "name": "internal.metrics.executorDeserializeCpuTime",
 * "value": "3486800"
 * },
 * {
 * "id": 35938,
 * "name": "internal.metrics.updatedBlockStatuses",
 * "value": "[(broadcast_1496_piece0,BlockStatus(StorageLevel(memory, 1 replicas),1719,0)), (broadcast_1496,BlockStatus(StorageLevel(memory, deserialized, 1 replicas),2888,0)), (broadcast_1496_piece0,BlockStatus(StorageLevel(memory, 1 replicas),1719,0)), (broadcast_1496,BlockStatus(StorageLevel(memory, deserialized, 1 replicas),2888,0))]"
 * },
 * {
 * "id": 35932,
 * "name": "internal.metrics.resultSize",
 * "value": "3762"
 * },
 * {
 * "id": 35941,
 * "name": "internal.metrics.shuffle.read.remoteBytesRead",
 * "value": "0"
 * }
 * ]
 * },
 */