package Yarn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class contains all the JSON placeholder classes required for a YARN job.
 * Main class is Apps, it contains a List<App> with all YARN jobs available from the REST api.
 */
public class YARNcontroller {
    /**
     * Used for fetching YARN jobs that fit the given parameters.
     *
     * @param master       YARN master URL.
     * @param user         The user whose jobs will be fetched.
     * @param objectMapper The OM for deserialising the JSON objects correctly.
     * @param time         All jobs must start AFTER this timestamp. Set to '-1' in order to use the JVM start time as timestamp.
     * @return A  List of all jobs submitted by the given user.
     */
    public static List<App> getYarnApps(String master, ObjectMapper objectMapper, String user, long time, String states) {
        if (time == -1) {
            time = ManagementFactory.getRuntimeMXBean().getStartTime();
        }
        List<App> jobs = new ArrayList<>();
        String params = "user=" + user + "&startedTimeBegin=" + time + "&states=" + states; //Accepts all jobs that started AFTER the JVM started.
        try {
            HashMap<String, Apps> app_list = objectMapper.readValue(Requests.GET(master + "/ws/v1/cluster/apps?" + params, null), new TypeReference<HashMap<String, Apps>>() {
            });
            if (app_list.get("apps") == null) {
                return jobs;
            }
            jobs = app_list.get("apps").app;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }
}
