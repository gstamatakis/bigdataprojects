package main;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created  by gstamatakis on 15-May-17.
 */
public class SkylineHelper {
    private boolean candidate_sort;

    public SkylineHelper(boolean candidate_sort) {
        this.candidate_sort = candidate_sort;
    }

    /**
     * Checks if a series of points is dominated by another.
     */
    private boolean isDominated(double[] left, double[] right) {
        boolean same = true;
        for (int i = 1; i < left.length; i++) {
            if (left[i] < right[i]) {
                return false;
            } else if (same && left[i] > right[i]) {
                same = false;
            }
        }
        return !same;
    }

    /**
     * Main skyline method.
     *
     * @param dataset Input vector of points.
     * @return The skyline of the input vector.
     */
    public ArrayList<double[]> skyline(ArrayList<double[]> dataset) {
        ArrayList<double[]> candidates = new ArrayList<>();
        for (double[] current : dataset) {
            boolean addFlag = false;
            ArrayList<double[]> losers = new ArrayList<>();
            for (double[] candidate : candidates) {
                if (isDominated(current, candidate)) {
                    addFlag = true;
                    break;
                } else if (isDominated(candidate, current)) {
                    losers.add(candidate);
                }
            }
            candidates.removeAll(losers);
            if (!addFlag) {
                candidates.add(current);
            }
        }
        if (candidate_sort) {
            candidates.sort(Comparator.comparingDouble(o -> o[0]));
        }
        return candidates;
    }
}
