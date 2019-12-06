import org.apache.beam.sdk.transforms.Combine.CombineFn;


public class SumFn extends CombineFn<Integer, Integer, Integer> {

    @Override
    public Integer createAccumulator() {
        return 0;
    }

    @Override
    public Integer addInput(Integer accum, Integer input) {
        return accum + input;
    }

    @Override
    public Integer mergeAccumulators(Iterable<Integer> accums) {
        Integer merged = createAccumulator();
        for (Integer accum : accums) {
            merged += accum;
        }
        return merged;
    }

    @Override
    public Integer extractOutput(Integer accum) {
        return accum;
    }

    @Override
    public Integer defaultValue() {
        return 0;
    }
}