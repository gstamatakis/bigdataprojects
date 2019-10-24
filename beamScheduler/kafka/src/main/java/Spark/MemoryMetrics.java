package Spark;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "usedOnHeapStorageMemory",
        "usedOffHeapStorageMemory",
        "totalOnHeapStorageMemory",
        "totalOffHeapStorageMemory"
})
public class MemoryMetrics {

    @JsonProperty("usedOnHeapStorageMemory")
    public Long usedOnHeapStorageMemory;
    @JsonProperty("usedOffHeapStorageMemory")
    public Long usedOffHeapStorageMemory;
    @JsonProperty("totalOnHeapStorageMemory")
    public Long totalOnHeapStorageMemory;
    @JsonProperty("totalOffHeapStorageMemory")
    public Long totalOffHeapStorageMemory;

    @Override
    public String toString() {
        return "MemoryMetrics{" +
                "usedOnHeapStorageMemory=" + usedOnHeapStorageMemory +
                ", usedOffHeapStorageMemory=" + usedOffHeapStorageMemory +
                ", totalOnHeapStorageMemory=" + totalOnHeapStorageMemory +
                ", totalOffHeapStorageMemory=" + totalOffHeapStorageMemory +
                '}';
    }
}