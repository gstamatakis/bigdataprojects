import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "hostPort",
        "isActive",
        "rddBlocks",
        "memoryUsed",
        "diskUsed",
        "totalCores",
        "maxTasks",
        "activeTasks",
        "failedTasks",
        "completedTasks",
        "totalTasks",
        "totalDuration",
        "totalGCTime",
        "totalInputBytes",
        "totalShuffleRead",
        "totalShuffleWrite",
        "isBlacklisted",
        "maxMemory",
        "executorLogs",
        "memoryMetrics"
})
public class Executor {

    @JsonProperty("id")
    public String id;
    @JsonProperty("hostPort")
    public String hostPort;
    @JsonProperty("isActive")
    public Boolean isActive;
    @JsonProperty("rddBlocks")
    public Long rddBlocks;
    @JsonProperty("memoryUsed")
    public Long memoryUsed;
    @JsonProperty("diskUsed")
    public Long diskUsed;
    @JsonProperty("totalCores")
    public Long totalCores;
    @JsonProperty("maxTasks")
    public Long maxTasks;
    @JsonProperty("activeTasks")
    public Long activeTasks;
    @JsonProperty("failedTasks")
    public Long failedTasks;
    @JsonProperty("completedTasks")
    public Long completedTasks;
    @JsonProperty("totalTasks")
    public Long totalTasks;
    @JsonProperty("totalDuration")
    public Long totalDuration;
    @JsonProperty("totalGCTime")
    public Long totalGCTime;
    @JsonProperty("totalInputBytes")
    public Long totalInputBytes;
    @JsonProperty("totalShuffleRead")
    public Long totalShuffleRead;
    @JsonProperty("totalShuffleWrite")
    public Long totalShuffleWrite;
    @JsonProperty("isBlacklisted")
    public Boolean isBlacklisted;
    @JsonProperty("maxMemory")
    public Long maxMemory;
    @JsonProperty("executorLogs")
    public ExecutorLogs executorLogs;
    @JsonProperty("memoryMetrics")
    public MemoryMetrics memoryMetrics;

    @Override
    public String toString() {
        return "Executor{" +
                "id='" + id + '\'' +
                ", hostPort='" + hostPort + '\'' +
                ", isActive=" + isActive +
                ", rddBlocks=" + rddBlocks +
                ", memoryUsed=" + memoryUsed +
                ", diskUsed=" + diskUsed +
                ", totalCores=" + totalCores +
                ", maxTasks=" + maxTasks +
                ", activeTasks=" + activeTasks +
                ", failedTasks=" + failedTasks +
                ", completedTasks=" + completedTasks +
                ", totalTasks=" + totalTasks +
                ", totalDuration=" + totalDuration +
                ", totalGCTime=" + totalGCTime +
                ", totalInputBytes=" + totalInputBytes +
                ", totalShuffleRead=" + totalShuffleRead +
                ", totalShuffleWrite=" + totalShuffleWrite +
                ", isBlacklisted=" + isBlacklisted +
                ", maxMemory=" + maxMemory +
                ", executorLogs=" + executorLogs +
                ", memoryMetrics=" + memoryMetrics +
                '}';
    }
}

/**
 * http://rserver01:8189/proxy/application_1532029170450_0623/api/v1/applications/application_1532029170450_0623/executors
 * <p>
 * [
 * {
 * "id": "2",
 * "hostPort": "clu19.rserver:54031",
 * "isActive": true,
 * "rddBlocks": 1,
 * "memoryUsed": 33191,
 * "diskUsed": 0,
 * "totalCores": 1,
 * "maxTasks": 1,
 * "activeTasks": 1,
 * "failedTasks": 0,
 * "completedTasks": 23,
 * "totalTasks": 24,
 * "totalDuration": 1612,
 * "totalGCTime": 56,
 * "totalInputBytes": 0,
 * "totalShuffleRead": 322,
 * "totalShuffleWrite": 736,
 * "isBlacklisted": false,
 * "maxMemory": 97832140,
 * "executorLogs": {
 * "stdout": "http://clu19.rserver:8042/node/containerlogs/container_e30_1532029170450_0455_01_000003/gstamatakis/stdout?start=-4096",
 * "stderr": "http://clu19.rserver:8042/node/containerlogs/container_e30_1532029170450_0455_01_000003/gstamatakis/stderr?start=-4096"
 * },
 * "memoryMetrics": {
 * "usedOnHeapStorageMemory": 33191,
 * "usedOffHeapStorageMemory": 0,
 * "totalOnHeapStorageMemory": 97832140,
 * "totalOffHeapStorageMemory": 0
 * }
 * },
 * {
 * "id": "driver",
 * "hostPort": "127.0.0.1:59577",
 * "isActive": true,
 * "rddBlocks": 13,
 * "memoryUsed": 53831,
 * "diskUsed": 0,
 * "totalCores": 0,
 * "maxTasks": 0,
 * "activeTasks": 0,
 * "failedTasks": 0,
 * "completedTasks": 0,
 * "totalTasks": 0,
 * "totalDuration": 0,
 * "totalGCTime": 0,
 * "totalInputBytes": 0,
 * "totalShuffleRead": 0,
 * "totalShuffleWrite": 0,
 * "isBlacklisted": false,
 * "maxMemory": 97832140,
 * "executorLogs": {},
 * "memoryMetrics": {
 * "usedOnHeapStorageMemory": 53831,
 * "usedOffHeapStorageMemory": 0,
 * "totalOnHeapStorageMemory": 97832140,
 * "totalOffHeapStorageMemory": 0
 * }
 * },
 * {
 * "id": "1",
 * "hostPort": "clu26.rserver:38610",
 * "isActive": true,
 * "rddBlocks": 11,
 * "memoryUsed": 18920,
 * "diskUsed": 0,
 * "totalCores": 1,
 * "maxTasks": 1,
 * "activeTasks": 0,
 * "failedTasks": 0,
 * "completedTasks": 3189,
 * "totalTasks": 3189,
 * "totalDuration": 23377,
 * "totalGCTime": 167,
 * "totalInputBytes": 0,
 * "totalShuffleRead": 230,
 * "totalShuffleWrite": 1150,
 * "isBlacklisted": false,
 * "maxMemory": 97832140,
 * "executorLogs": {
 * "stdout": "http://clu26.rserver:8042/node/containerlogs/container_e30_1532029170450_0455_01_000002/gstamatakis/stdout?start=-4096",
 * "stderr": "http://clu26.rserver:8042/node/containerlogs/container_e30_1532029170450_0455_01_000002/gstamatakis/stderr?start=-4096"
 * },
 * "memoryMetrics": {
 * "usedOnHeapStorageMemory": 18920,
 * "usedOffHeapStorageMemory": 0,
 * "totalOnHeapStorageMemory": 97832140,
 * "totalOffHeapStorageMemory": 0
 * }
 * },
 * {
 * "id": "3",
 * "hostPort": "clu16.rserver:54643",
 * "isActive": true,
 * "rddBlocks": 7,
 * "memoryUsed": 12040,
 * "diskUsed": 0,
 * "totalCores": 1,
 * "maxTasks": 1,
 * "activeTasks": 0,
 * "failedTasks": 0,
 * "completedTasks": 3107,
 * "totalTasks": 3107,
 * "totalDuration": 23737,
 * "totalGCTime": 135,
 * "totalInputBytes": 0,
 * "totalShuffleRead": 184,
 * "totalShuffleWrite": 414,
 * "isBlacklisted": false,
 * "maxMemory": 97832140,
 * "executorLogs": {
 * "stdout": "http://clu16.rserver:8042/node/containerlogs/container_e30_1532029170450_0455_01_000004/gstamatakis/stdout?start=-4096",
 * "stderr": "http://clu16.rserver:8042/node/containerlogs/container_e30_1532029170450_0455_01_000004/gstamatakis/stderr?start=-4096"
 * },
 * "memoryMetrics": {
 * "usedOnHeapStorageMemory": 12040,
 * "usedOffHeapStorageMemory": 0,
 * "totalOnHeapStorageMemory": 97832140,
 * "totalOffHeapStorageMemory": 0
 * }
 * }
 * ]
 */