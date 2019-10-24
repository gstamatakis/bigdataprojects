import org.apache.beam.sdk.transforms.Combine.CombineFn;
import org.apache.beam.sdk.values.KV;


public class AverageFn extends CombineFn<Double, KV<Double, Integer>, Double> {

    @Override
    public KV<Double, Integer> createAccumulator() {
        return KV.of(0.0, 0);
    }

    @Override
    public KV<Double, Integer> addInput(KV<Double, Integer> accum, Double input) {
        return KV.of(accum.getKey() + input, accum.getValue() + 1);
    }

    @Override
    public KV<Double, Integer> mergeAccumulators(Iterable<KV<Double, Integer>> accums) {
        KV<Double, Integer> merged = createAccumulator();
        for (KV<Double, Integer> accum : accums) {
            merged = KV.of(merged.getKey() + accum.getKey(), merged.getValue() + accum.getValue());
        }
        return merged;
    }

    @Override
    public Double extractOutput(KV<Double, Integer> accum) {
        return accum.getKey() / accum.getValue();
    }

    @Override
    public Double defaultValue() {
        return 99999.0;
    }
}