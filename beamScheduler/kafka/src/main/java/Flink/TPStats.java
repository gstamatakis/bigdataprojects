package Flink;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "min",
        "max",
        "avg",
        "sum"
})
public class TPStats {
    @JsonProperty("id")
    public String id;
    @JsonProperty("min")
    public Long min;
    @JsonProperty("max")
    public Long max;
    @JsonProperty("avg")
    public Double avg;
    @JsonProperty("sum")
    public Long sum;

    @Override
    public String toString() {
        return "TPStats{" +
                "id='" + id + '\'' +
                ", min=" + min +
                ", max=" + max +
                ", avg=" + avg +
                ", sum=" + sum +
                '}';
    }
}