import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "stdout",
        "stderr"
})
public class ExecutorLogs {

    @JsonProperty("stdout")
    public String stdout;
    @JsonProperty("stderr")
    public String stderr;

    @Override
    public String toString() {
        return "ExecutorLogs{" +
                "stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                '}';
    }
}