package Spark;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "javaVersion",
        "javaHome",
        "scalaVersion"
})
public class SparkRuntime {

    @JsonProperty("javaVersion")
    public String javaVersion;
    @JsonProperty("javaHome")
    public String javaHome;
    @JsonProperty("scalaVersion")
    public String scalaVersion;

    @Override
    public String toString() {
        return "SparkRuntime{" +
                "javaVersion='" + javaVersion + '\'' +
                ", javaHome='" + javaHome + '\'' +
                ", scalaVersion='" + scalaVersion + '\'' +
                '}';
    }
}