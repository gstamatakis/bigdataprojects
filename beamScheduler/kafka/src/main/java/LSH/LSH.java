/*
 *      _______                       _        ____ _     _
 *     |__   __|                     | |     / ____| |   | |
 *        | | __ _ _ __ ___  ___  ___| |    | (___ | |___| |
 *        | |/ _` | '__/ __|/ _ \/ __| |     \___ \|  ___  |
 *        | | (_| | |  \__ \ (_) \__ \ |____ ____) | |   | |
 *        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|   |_|
 *
 * -----------------------------------------------------------
 *
 *  TarsosLSH is developed by Joren Six.
 *
 * -----------------------------------------------------------
 *
 *  Info    : http://0110.be/tag/TarsosLSH
 *  Github  : https://github.com/JorenSix/TarsosLSH
 *  Releases: http://0110.be/releases/TarsosLSH/
 *
 */

package LSH;

import LSH.families.DistanceComparator;
import LSH.families.DistanceMeasure;
import LSH.families.HashFamily;

import java.util.*;
import java.util.concurrent.*;

/**
 * Implements a Locality Sensitive Hash scheme.
 *
 * @author Joren Six
 */
public class LSH {
    List<Vector> dataset;
    private Index index;
    private final HashFamily hashFamily;

    public LSH(List<Vector> dataset, HashFamily hashFamily) {
        this.dataset = dataset;
        this.hashFamily = hashFamily;
    }

    /**
     * Benchmark the current LSH construction.
     *
     * @param neighboursSize the expected size of the neighbourhood.
     * @param measure        The measure to use to check for correctness.
     */
    public void benchmark(int neighboursSize, DistanceMeasure measure) {
        long startTime = 0;
        double linearSearchTime = 0;
        double lshSearchTime = 0;
        int numbercorrect = 0;
        int falsePositives = 0;
        int truePositives = 0;
        int falseNegatives = 0;
        //int intersectionSize = 0;
        for (int i = 0; i < dataset.size(); i++) {
            Vector query = dataset.get(i);
            startTime = System.currentTimeMillis();
            List<Vector> lshResult = index.query(query, neighboursSize);
            lshSearchTime += System.currentTimeMillis() - startTime;

            startTime = System.currentTimeMillis();
            List<Vector> linearResult = linearSearch(dataset, query, neighboursSize, measure);
            linearSearchTime += System.currentTimeMillis() - startTime;

            Set<Vector> set = new HashSet<Vector>();
            set.addAll(lshResult);
            set.addAll(linearResult);
            //intersectionSize += set.size();
            //In the best case, LSH result and linear result contain the exact same elements.
            //The number of false positives is the number of vectors that exceed the number of linear results.
            falsePositives += set.size() - linearResult.size();
            //The number of true positives is Union of results - intersection.
            truePositives += lshResult.size() + linearResult.size() - set.size();
            //The number of false Negatives the number of vectors that exceed the number of lsh results .
            falseNegatives = set.size() - lshResult.size();

            //result is only correct if all nearest neighbours are the same (rather strict).
            boolean correct = true;
            for (int j = 0; j < Math.min(lshResult.size(), linearResult.size()); j++) {
                correct = correct && lshResult.get(j) == linearResult.get(j);
            }
            if (correct) {
                numbercorrect++;
            }
        }
        double numberOfqueries = dataset.size();
        double dataSetSize = dataset.size();
        double precision = truePositives / Double.valueOf(truePositives + falsePositives) * 100;
        double recall = truePositives / Double.valueOf(truePositives + falseNegatives) * 100;
        double percentageCorrect = numbercorrect / dataSetSize * 100;
        double percentageTouched = index.getTouched() / numberOfqueries / dataSetSize * 100;
        linearSearchTime /= 1000.0;
        lshSearchTime /= 1000.0;
        int hashes = index.getNumberOfHashes();
        int hashTables = index.getNumberOfHashTables();

        //System.out.printf("%10s%15s%10s%10s%10s%10s%10s%10s\n","#hashes","#hashTables","Correct","Touched","linear","lsh","Precision","Recall");
        System.out.printf("%10d%15d%9.2f%%%9.2f%%%9.4fs%9.4fs%9.2f%%%9.2f%%\n", hashes, hashTables, percentageCorrect, percentageTouched, linearSearchTime, lshSearchTime, precision, recall);
    }

    /**
     * Find the nearest neighbours for a query in the index.
     *
     * @param query          The query vector.
     * @param neighboursSize The size of the neighbourhood. The returned list length
     *                       contains the maximum number of elements, or less. Zero
     *                       elements are possible.
     * @return A list of nearest neigbours, according to the index. The returned
     * list length contains the maximum number of elements, or less.
     * Zero elements are possible.
     */
    public List<Vector> query(final Vector query, int neighboursSize) {
        return index.query(query, neighboursSize);
    }

    /**
     * Search for the actual nearest neighbours for a query vector using an
     * exhaustive linear search. For each vector a priority queue is created,
     * the distance between the query and other vectors is used to sort the
     * priority queue. The closest k neighbours show up at the head of the
     * priority queue.
     *
     * @param dataset    The data set with a bunch of vectors.
     * @param query      The query vector.
     * @param resultSize The k nearest neighbours to find. Returns k vectors if the
     *                   data set size is larger than k.
     * @param measure    The distance measure used to sort the priority queue with.
     * @return The list of k nearest neighbours to the query vector, according
     * to the given distance measure.
     */
    public static List<Vector> linearSearch(List<Vector> dataset, final Vector query, int resultSize, DistanceMeasure measure) {
        DistanceComparator dc = new DistanceComparator(query, measure);
        PriorityQueue<Vector> pq = new PriorityQueue<Vector>(dataset.size(), dc);
        pq.addAll(dataset);
        List<Vector> vectors = new ArrayList<Vector>();
        for (int i = 0; i < resultSize; i++) {
            vectors.add(pq.poll());
        }
        return vectors;
    }

    static double determineRadius(List<Vector> dataset, DistanceMeasure measure, int timeout) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        double radius = 0.0;
        DetermineRadiusTask drt = new DetermineRadiusTask(dataset, measure);
        Future<Double> future = executor.submit(drt);
        try {
            System.out.println("Determine radius..");
            radius = 0.90 * future.get(timeout, TimeUnit.SECONDS);
            System.out.println("Determined radius: " + radius);
        } catch (TimeoutException e) {
            System.err.println("Terminated!");
            radius = 0.90 * drt.getRadius();
        } catch (InterruptedException e) {
            System.err.println("Execution interrupted!" + e.getMessage());
            radius = 0.90 * drt.getRadius();
        } catch (ExecutionException e) {
            radius = 0.90 * drt.getRadius();
        }
        executor.shutdownNow();
        return radius;
    }

    static class DetermineRadiusTask implements Callable<Double> {
        private double queriesDone = 0;
        private double radiusSum = 0.0;
        private final List<Vector> dataset;
        private final Random rand;
        private final DistanceMeasure measure;

        public DetermineRadiusTask(List<Vector> dataset, DistanceMeasure measure) {
            this.dataset = dataset;
            this.rand = new Random();
            this.measure = measure;
        }

        @Override
        public Double call() throws Exception {
            for (int i = 0; i < 30; i++) {
                Vector query = dataset.get(rand.nextInt(dataset.size()));
                List<Vector> result = linearSearch(dataset, query, 2, measure);
                //the first vector is the query self, the second the closest.
                radiusSum += measure.distance(query, result.get(1));
                queriesDone++;
            }
            return radiusSum / queriesDone;
        }

        public double getRadius() {
            return radiusSum / queriesDone;
        }
    }
}
