import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "path",
        "dataPort",
        "timeSinceLastHeartbeat",
        "slotsNumber",
        "freeSlots",
        "hardware"
})
public class Taskmanager {
    @JsonProperty("id")
    public String id;
    @JsonProperty("path")
    public String path;
    @JsonProperty("dataPort")
    public Long dataPort;
    @JsonProperty("timeSinceLastHeartbeat")
    public Long timeSinceLastHeartbeat;
    @JsonProperty("slotsNumber")
    public Long slotsNumber;
    @JsonProperty("freeSlots")
    public Long freeSlots;
    @JsonProperty("hardware")
    public Hardware hardware;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "Taskmanager{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", dataPort=" + dataPort +
                ", timeSinceLastHeartbeat=" + timeSinceLastHeartbeat +
                ", slotsNumber=" + slotsNumber +
                ", freeSlots=" + freeSlots +
                ", hardware=" + hardware +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
