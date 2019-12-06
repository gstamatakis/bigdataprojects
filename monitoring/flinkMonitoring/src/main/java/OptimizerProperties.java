import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class OptimizerProperties implements Serializable {

    private final static long serialVersionUID = -6251651994333267254L;

    @Override
    public String toString() {
        return "OptimizerProperties{}";
    }
}
