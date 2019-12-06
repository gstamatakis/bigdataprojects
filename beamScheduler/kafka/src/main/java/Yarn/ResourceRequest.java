package Yarn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "capability",
        "nodeLabelExpression",
        "numContainers",
        "priority",
        "relaxLocality",
        "resourceName"
})
public class ResourceRequest {

    @JsonProperty("capability")
    public Capability capability;
    @JsonProperty("nodeLabelExpression")
    public String nodeLabelExpression;
    @JsonProperty("numContainers")
    public Long numContainers;
    @JsonProperty("priority")
    public Priority priority;
    @JsonProperty("relaxLocality")
    public Boolean relaxLocality;
    @JsonProperty("resourceName")
    public String resourceName;

}