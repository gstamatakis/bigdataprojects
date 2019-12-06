package CoreUtils;

import java.util.*;

/**
 * Implementation of <i>Algorithm L</i> by Li in <b>Reservoir-sampling algorithms of time complexity
 * O(n(1 + log(N/n)))</b> (page 5 of the paper).
 * <p>
 * Unlike { WatermanSampling}, the { VitterXSampling}, { VitterZSampling} and {@code LiLSampling}
 * algorithms decide how many items to skip, rather than deciding whether or not to skip an item each time it is feeded.
 * This property allows these algorithms to perform better by efficiently calculating the number of items that need to
 * be skipped, while making fewer calls to the RNG.
 * <p>
 * This implementation throws {StreamOverflowException} if more than {@link Long#MAX_VALUE} items are feeded.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @see <a href="https://doi.org/10.1145/198429.198435">Reservoir-sampling algorithms of time complexity
 * O(n(1 + log(N/n)))</a>
 */
public class LilSampling {
    private int sampleSize;
    private double W;
    private Random random;
    private List<Integer> sample;
    private long streamSize;
    private long skip;

    public LilSampling(int size) {
        this.sampleSize = size;
        this.random = new Random();
        this.W = Math.pow(randomExclusive(random), 1.0 / sampleSize);
        this.streamSize = 0;
        this.sample = new ArrayList<>(sampleSize);
        this.skip = skipLength(random);
    }

    private long skipLength(Random random) {
        final double random1 = randomExclusive(random);
        final double random2 = randomExclusive(random);
        long skip = (long) (Math.log(random1) / Math.log(1 - W));
        assert skip >= 0 || skip == Long.MIN_VALUE;
        if (skip == Long.MIN_VALUE) {  // Sometimes when W is very small, 1 - W = 1 and Math.log(1) = +0 instead of -0
            skip = Long.MAX_VALUE;     // This results in negative infinity skip
        }
        W *= Math.pow(random2, 1.0 / sampleSize);
        return skip;
    }

    public final void feed(Integer item) {
        // Checks
        if (item == null) {
            throw new NullPointerException("Item was null");
        }
        if (streamSize == Long.MAX_VALUE) {
            throw new IllegalStateException("");
        }

        // Increase stream size
        this.streamSize++;
        assert this.streamSize > 0;

        // Skip items and add to reservoir
        if (sample.size() < sampleSize) {
            sample.add(item);
            assert sample.size() == Math.min(sampleSize, this.streamSize);
        } else {
            assert sample.size() == sampleSize;
            if (skip > 0) {
                skip--;
            } else {
                assert skip == 0;
                sample.set(random.nextInt(sampleSize), item);
                skip = skipLength(random);
                assert this.skip >= 0;
            }
        }
    }

    public long getStreamSize() {
        return this.streamSize;
    }

    public String getSampleAsString() {
        StringBuilder strbul = new StringBuilder();
        Iterator<Integer> iter = this.sample.iterator();
        while (iter.hasNext()) {
            strbul.append(iter.next());
            if (iter.hasNext()) {
                strbul.append(",");
            }
        }
        return strbul.toString();
    }

    private double randomExclusive(Random random) {
        double r = 0.0;
        while (r == 0.0) {
            r = random.nextDouble();
        }
        assert r > 0.0 && r < 1.0;
        return r;
    }

    public String toString() {
        return "Sample size: " + Arrays.toString(this.sample.toArray(new Integer[0])) + "\n";
    }
}
