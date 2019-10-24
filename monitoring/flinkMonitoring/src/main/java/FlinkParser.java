import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class FlinkParser {
    private String masterURL;
    private ObjectMapper objectMapper;

    public FlinkParser(String masterURL, ObjectMapper objectMapper) {
        this.masterURL = masterURL;
        this.objectMapper = objectMapper;
    }

    public List<FlinkJob> fetchFlinkJobs(String specificJobID, String state) {
        List<FlinkJob> jobs = new ArrayList<>();
        try {
            HashMap<String, HashMap<String, String>[]> jobs_map = objectMapper.readValue(Requests.GET("http://" + this.masterURL + "/jobs", null), new TypeReference<HashMap<String, HashMap<String, String>[]>>() {
            });
            for (Map<String, String> job : jobs_map.get("jobs")) { //get the rest of the content ~4.5KB / Job
                FlinkJob flinkJob = objectMapper.readValue(Requests.GET("http://" + this.masterURL + "/jobs/" + job.get("id"), null), new TypeReference<FlinkJob>() {
                });
                if (state != null) {
                    if (!flinkJob.state.equals(state)) {
                        continue;
                    }
                }
                flinkJob.getPeriodicMetrics(masterURL, objectMapper);
                jobs.add(flinkJob);
                if (flinkJob.jid.equals(specificJobID)) {
                    ArrayList<FlinkJob> flinkJ = new ArrayList<>();
                    flinkJ.add(flinkJob);
                    return flinkJ;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch Flink jobs.");
        }
        return jobs;

    }

    public Jobmanager fetchFlinkJobManager() {
        try {
            List<HashMap<String, String>> jmstats = objectMapper.readValue(Requests.GET("http://" + this.masterURL + "/jobmanager/config", null), new TypeReference<List<HashMap<String, String>>>() {
            });
            Map<String, Taskmanager[]> tm_map = objectMapper.readValue(Requests.GET("http://" + this.masterURL + "/taskmanagers", null), new TypeReference<Map<String, Taskmanager[]>>() {
            });
            List<Taskmanager> tmlist = new ArrayList<Taskmanager>();
            Collections.addAll(tmlist, tm_map.get("taskmanagers"));
            return new Jobmanager(tmlist, jmstats);
        } catch (Exception e) {
            System.out.println("Failed to contact Flink JM.");
        }
        return null;
    }
}
