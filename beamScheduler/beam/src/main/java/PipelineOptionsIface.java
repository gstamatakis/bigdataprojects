import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

public interface PipelineOptionsIface extends PipelineOptions {
    @Description("skip")
    @Default.String("no")
    String getSkip();

    void setSkip(String value);
}
