import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "num",
        "id",
        "ship_strategy",
        "exchange"
})
public class Input implements Serializable {

    private final static long serialVersionUID = 8504684533081251099L;
    @JsonProperty("num")
    public Integer num;
    @JsonProperty("id")
    public String id;
    @JsonProperty("ship_strategy")
    public String shipStrategy;
    @JsonProperty("exchange")
    public String exchange;

    @Override
    public String toString() {
        return "Input{" +
                "num=" + num +
                ", id='" + id + '\'' +
                ", shipStrategy='" + shipStrategy + '\'' +
                ", exchange='" + exchange + '\'' +
                '}';
    }
}
