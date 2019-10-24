package Flink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "parallelism",
        "operator",
        "operator_strategy",
        "description",
        "inputs",
        "optimizer_properties"
})
public class Node implements Serializable {

    @JsonProperty("id")
    public String id;
    @JsonProperty("parallelism")
    public Integer parallelism;
    @JsonProperty("operator")
    public String operator;
    @JsonProperty("operator_strategy")
    public String operatorStrategy;
    @JsonProperty("description")
    public String description;
    @JsonProperty("inputs")
    public List<Input> inputs = null;
    @JsonProperty("optimizer_properties")
    public OptimizerProperties optimizerProperties;
    private final static long serialVersionUID = 3026475090445305681L;


    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", parallelism=" + parallelism +
                ", operator='" + operator + '\'' +
                ", operatorStrategy='" + operatorStrategy + '\'' +
                ", description='" + description + '\'' +
                ", inputs=" + inputs +
                ", optimizerProperties=" + optimizerProperties +
                '}';
    }
}
