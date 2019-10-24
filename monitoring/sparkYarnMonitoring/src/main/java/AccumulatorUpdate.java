import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "update",
        "value"

})
public class AccumulatorUpdate {

    @JsonProperty("id")
    public Long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("update")
    public String update;
    @JsonProperty("value")
    public String value;

    @Override
    public String toString() {
        return "AccumulatorUpdate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", update='" + update + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}