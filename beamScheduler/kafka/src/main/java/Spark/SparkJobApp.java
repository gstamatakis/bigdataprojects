package Spark;

import CoreUtils.ClusterConstants;
import CoreUtils.Requests;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "attempts"
})
public class SparkJobApp {

    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("attempts")
    public List<Attempt> attempts;

    //Extra components
    List<Batch> batches;
    int processed_batches_cnt;
    StreamingStatistic statistics;
    List<StreamingReceiver> receivers;
    HashMap<Long, Integer> eventRates_cnt;
    SparkEnv environment;
    List<Executor> executors;
    List<Stage> stages;
    int stage_cnt;

    public void getPeriodicMetrics(String appName, ObjectMapper objectMapper) throws Exception {
        String master = String.format(ClusterConstants.yarn_master + "/proxy/%s/api/v1/applications/%s", appName, appName);
        this.batches = objectMapper.readValue(Requests.GET(master + "/streaming/batches", null), new TypeReference<List<Batch>>() {
        });
        this.processed_batches_cnt = 0;
        this.statistics = objectMapper.readValue(Requests.GET(master + "/streaming/statistics", null), new TypeReference<StreamingStatistic>() {
        });
        this.receivers = objectMapper.readValue(Requests.GET(master + "/streaming/receivers", null), new TypeReference<List<StreamingReceiver>>() {
        });
        this.eventRates_cnt = new HashMap<>();
        for (StreamingReceiver receiver : this.receivers) {
            this.eventRates_cnt.put(receiver.streamId, 0);
        }
        this.environment = objectMapper.readValue(Requests.GET(master + "/environment", null), new TypeReference<SparkEnv>() {
        });
        this.executors = objectMapper.readValue(Requests.GET(master + "/executors", null), new TypeReference<List<Executor>>() {
        });
        this.stages = objectMapper.readValue(Requests.GET(master + "/stages", null), new TypeReference<List<Stage>>() {
        });
        this.stage_cnt = 0;
    }

    @Override
    public String toString() {
        return "SparkJobApp{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", attempts=" + attempts +
                ", batches[0]=" + batches.get(0) +
                ", statistics=" + statistics +
                ", receivers=" + receivers +
                ", stage[0]=" + stages.get(0) +
                '}';
    }

    public void putDBpoints(BatchPoints batchPoints) {
        long time = System.currentTimeMillis(); //TODO change to sth else.

        //Job details
        batchPoints.point(Point.measurement("spark_job")
                .time(time, TimeUnit.MILLISECONDS)
                .tag("id", this.id)
                .addField("name", this.name)
                .build());

        //Attempts
        for (Attempt attempt : this.attempts) {
            batchPoints.point(Point.measurement("attempts")
                    .time(time, TimeUnit.MILLISECONDS)
                    .tag("name", this.name)
                    .addField("attemptId", attempt.attemptId == null ? "null" : attempt.attemptId)
                    .addField("startTime", attempt.startTime)
                    .addField("endTime", attempt.endTime)
                    .addField("lastUpdated", attempt.lastUpdated)
                    .addField("duration", attempt.duration)
                    .addField("sparkUser", attempt.sparkUser)
                    .addField("completed", attempt.completed)
                    .addField("endTimeEpoch", attempt.endTimeEpoch)
                    .addField("startTimeEpoch", attempt.startTimeEpoch)
                    .addField("lastUpdatedEpoch", attempt.lastUpdatedEpoch)
                    .build());
        }

        //Batches, process each batch only once.
//        DateFormat format = new SimpleDateFormat("yyyy mm dd HH:mm:ss.SSS zzz");
        for (Batch batch : this.batches.subList(this.processed_batches_cnt, this.batches.size())) {
            batchPoints.point(Point.measurement("batches")
//                    .time(format.parse(batch.batchTime).getTime(), TimeUnit.MILLISECONDS)
                    .time(time, TimeUnit.MILLISECONDS)
                    .tag("name", this.name)
                    .addField("batchId", batch.batchId)
                    .addField("batchTime", batch.batchTime)
                    .addField("status", batch.status)
                    .addField("batchDuration", batch.batchDuration)
                    .addField("inputSize", batch.inputSize)
                    .addField("schedulingDelay", batch.schedulingDelay)
                    .addField("numActiveOutputOps", batch.numActiveOutputOps)
                    .addField("numCompletedOutputOps", batch.numCompletedOutputOps)
                    .addField("numFailedOutputOps", batch.numFailedOutputOps)
                    .addField("numTotalOutputOps", batch.numTotalOutputOps)
                    .addField("processingTime", batch.processingTime)
                    .addField("totalDelay", batch.totalDelay)
                    .build());
        }
        this.processed_batches_cnt = this.batches.size();

        //Streaming statistics
        batchPoints.point(Point.measurement("streaming_statistics")
                .time(time, TimeUnit.MILLISECONDS)
                .tag("name", this.name)
                .addField("startTime", this.statistics.startTime)
                .addField("batchDuration", this.statistics.batchDuration.longValue())
                .addField("numReceivers", this.statistics.numReceivers)
                .addField("numActiveReceivers", this.statistics.numActiveReceivers)
                .addField("numInactiveReceivers", this.statistics.numInactiveReceivers)
                .addField("numTotalCompletedBatches", this.statistics.numTotalCompletedBatches)
                .addField("numRetainedCompletedBatches", this.statistics.numRetainedCompletedBatches)
                .addField("numActiveBatches", this.statistics.numActiveBatches)
                .addField("numProcessedRecords", this.statistics.numProcessedRecords)
                .addField("numReceivedRecords", this.statistics.numReceivedRecords)
                .addField("avgInputRate", this.statistics.avgInputRate)
                .addField("avgSchedulingDelay", this.statistics.avgSchedulingDelay)
                .addField("avgProcessingTime", this.statistics.avgProcessingTime)
                .addField("avgTotalDelay", this.statistics.avgTotalDelay)
                .build());

        //Streaming receiver
        for (StreamingReceiver receiver : this.receivers) {
            HashMap<Long, Integer> rcnt = this.eventRates_cnt;
            for (List<Long> tuples : receiver.eventRates.subList(rcnt.get(receiver.streamId), receiver.eventRates.size())) {
                batchPoints.point(Point.measurement("streaming_receiver")
//                        .time(tuples.get(0), TimeUnit.MILLISECONDS) //event_rate_ts
                        .time(time, TimeUnit.MILLISECONDS)
                        .tag("name", String.valueOf(receiver.streamId))
                        .tag("streamName", receiver.streamName)
                        .tag("isActive", String.valueOf(receiver.isActive))
                        .tag("executorId", receiver.executorId == null ? "null" : receiver.executorId)
                        .tag("executorHost", receiver.executorHost == null ? "null" : receiver.executorHost)
                        .addField("lastErrorTime", receiver.lastErrorTime == null ? "null" : receiver.lastErrorTime)
                        .addField("lastErrorMessage", receiver.lastErrorMessage == null ? "null" : receiver.lastErrorMessage)
                        .addField("lastError", receiver.lastError == null ? "null" : receiver.lastError)
                        .addField("avgEventRate", receiver.avgEventRate.longValue())
                        .addField("event_rate_val", tuples.get(1))
                        .build());
            }
        }

        //Executors
        for (Executor executor : this.executors) {
            batchPoints.point(Point.measurement("executors")
                    .tag("name", this.name)
                    .tag("id", executor.id)
                    .tag("hostPort", executor.hostPort)
                    .tag("isActive", String.valueOf(executor.isActive))
                    .addField("rrdBlocks", executor.rddBlocks)
                    .addField("memoryUsed", executor.memoryUsed)
                    .addField("diskUsed", executor.diskUsed)
                    .addField("totalCores", executor.totalCores)
                    .addField("maxTasks", executor.maxTasks)
                    .addField("activeTasks", executor.activeTasks)
                    .addField("failedTasks", executor.failedTasks)
                    .addField("completedTasks", executor.completedTasks)
                    .addField("totalTasks", executor.totalTasks)
                    .addField("totalDuration", executor.totalDuration)
                    .addField("totalGCTime", executor.totalGCTime)
                    .addField("totalInputBytes", executor.totalInputBytes)
                    .addField("totalShuffleRead", executor.totalShuffleRead)
                    .addField("totalShuffleWrite", executor.totalShuffleWrite)
                    .tag("isBlacklisted", String.valueOf(executor.isBlacklisted))
                    .addField("maxMemory", executor.maxMemory)
                    .addField("totalOffHeapStorageMemory", executor.memoryMetrics.totalOffHeapStorageMemory)
                    .addField("totalOnHeapStorageMemory", executor.memoryMetrics.totalOnHeapStorageMemory)
                    .addField("usedOffHeapStorageMemory", executor.memoryMetrics.usedOffHeapStorageMemory)
                    .addField("usedOnHeapStorageMemory", executor.memoryMetrics.usedOnHeapStorageMemory)
                    .build());
        }

        //Stages
        for (Stage stage : this.stages.subList(this.stage_cnt, this.stages.size())) {
            batchPoints.point(Point.measurement("stages")
                    .tag("name", name)
                    .tag("status", stage.status)
                    .addField("stageId", stage.stageId.longValue())
                    .addField("attemptId", stage.attemptId.longValue())
                    .addField("numActiveTasks", stage.numActiveTasks.longValue())
                    .addField("numCompleteTasks", stage.numCompleteTasks.longValue())
                    .addField("numFailedTasks", stage.numFailedTasks.longValue())
                    .addField("executorRunTime", stage.executorRunTime.longValue())
                    .addField("executorCpuTime", stage.executorCpuTime.longValue())
                    .addField("submissionTime", stage.submissionTime == null ? "null" : stage.submissionTime)
                    .addField("firstTaskLaunchedTime", stage.firstTaskLaunchedTime == null ? "null" : stage.firstTaskLaunchedTime)
                    .addField("inputBytes", stage.inputBytes == null ? -1 : stage.inputBytes)
                    .addField("inputBytes", stage.inputBytes == null ? -1 : stage.inputBytes)
                    .addField("outputBytes", stage.outputBytes == null ? -1 : stage.outputBytes)
                    .addField("outputRecords", stage.outputRecords == null ? -1 : stage.outputRecords)
                    .addField("shuffleReadBytes", stage.shuffleReadBytes == null ? -1 : stage.shuffleReadBytes)
                    .addField("shuffleReadRecords", stage.shuffleReadRecords == null ? -1 : stage.shuffleReadRecords)
                    .addField("shuffleWriteBytes", stage.shuffleWriteBytes == null ? -1 : stage.shuffleWriteBytes)
                    .addField("shuffleWriteRecords", stage.shuffleWriteRecords == null ? -1 : stage.shuffleWriteRecords)
                    .addField("memoryBytesSpilled", stage.memoryBytesSpilled == null ? -1 : stage.memoryBytesSpilled)
                    .addField("diskBytesSpilled", stage.diskBytesSpilled == null ? -1 : stage.diskBytesSpilled)
                    .addField("stage_name", stage.name == null ? "null" : stage.name)
                    .addField("details", stage.details == null ? "null" : stage.details)
                    .addField("schedulingPool", stage.schedulingPool == null ? "null" : stage.schedulingPool)
                    .addField("completionTime", stage.completionTime == null ? "null" : stage.completionTime)
//                    .addField("",stage.accumulatorUpdates.)
                    .build());
        }
        stage_cnt += this.stages.size();
    }
}