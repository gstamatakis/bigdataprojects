package Yarn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "memory",
        "memorySize",
        "virtualCores"
})
public class Capability {

    @JsonProperty("memory")
    public Long memory;
    @JsonProperty("memorySize")
    public Long memorySize;
    @JsonProperty("virtualCores")
    public Long virtualCores;

}