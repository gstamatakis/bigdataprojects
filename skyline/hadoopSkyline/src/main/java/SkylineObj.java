import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created  by gstamatakis on 15-May-17.
 */
public class SkylineObj {
    private boolean candidate_sort;

    public SkylineObj(boolean candidate_sort) {
        this.candidate_sort = candidate_sort;
    }

    private boolean isDominated(double[] me, double[] you) {
        boolean same = true;
        for (int i = 1; i < me.length; i++) {
            if (me[i] < you[i]) {
                return false;
            } else if (same && me[i] > you[i]) {
                same = false;
            }
        }
        return !same;
    }

    private void sort(List<double[]> dataset) {
        Collections.sort(dataset, new Comparator<double[]>() {
            public int compare(double[] o1, double[] o2) {
                if (o1[0] > o2[0]) {
                    return 1;
                } else if (o1[0] < o2[0]) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

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
            sort(candidates);
        }
        return candidates;
    }
}
