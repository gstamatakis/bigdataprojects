package Spark;

import CoreUtils.Requests;
import Yarn.App;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;


public class SparkParser {
    private ObjectMapper objectMapper;
    private String master;

    public SparkParser(String master, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.master = master;
    }

    public List<SparkJobApp> fetchSparkJobs(List<App> apps) {
        if (apps == null || apps.isEmpty()) {
            return new ArrayList<>();
        }
        List<SparkJobApp> jobs = new ArrayList<>();
        for (App app : apps) {
            try {
                jobs = objectMapper.readValue(Requests.GET(this.master + "/proxy/" + app.id + "/api/v1/applications", null), new TypeReference<List<SparkJobApp>>() {
                });
                SparkJobApp jobApp = jobs.get(0);
                jobApp.getPeriodicMetrics(app.id, objectMapper);
            } catch (Exception e) {
                System.out.println("Failed to fetch SparkJobs.");
            }
        }
        return jobs;
    }
}
