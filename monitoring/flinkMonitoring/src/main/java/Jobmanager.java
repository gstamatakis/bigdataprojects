import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;


public class Jobmanager {
    public List<Taskmanager> taskmanagers;
    public String rest_port;
    public String parallelism_default;
    public String address;
    public String task_slots;
    public String tmpdir;
    public String rpc_port;
    public String taskmanager_heap_size;
    public String jobmanager_heap_size;

    public Jobmanager(List<Taskmanager> tmlist, List<HashMap<String, String>> jmstats) {
        this.taskmanagers = tmlist;
        for (HashMap<String, String> pair : jmstats) {
            String key = pair.get("key");
            String val = pair.get("value");
            if (key.equals("rest.port")) {
                this.rest_port = val;
            } else if (key.equals("parallelism.default")) {
                this.parallelism_default = val;
            } else if (key.equals("jobmanager.rpc.address")) {
                this.address = val;
            } else if (key.equals("taskmanager.numberOfTaskSlots")) {
                this.task_slots = val;
            } else if (key.equals("web.tmpdir")) {
                this.tmpdir = val;
            } else if (key.equals("jobmanager.rpc.port")) {
                this.rpc_port = val;
            } else if (key.equals("taskmanager.heap.size")) {
                taskmanager_heap_size = val;
            } else if (key.equals("jobmanager.heap.size")) {
                jobmanager_heap_size = val;
            }
        }
    }

    @Override
    public String toString() {
        return "FlinkStats.Jobmanager{" +
                "taskmanagers=" + taskmanagers +
                ", rest_port='" + rest_port + '\'' +
                ", parallelism_default='" + parallelism_default + '\'' +
                ", address='" + address + '\'' +
                ", task_slots='" + task_slots + '\'' +
                ", tmpdir='" + tmpdir + '\'' +
                ", rpc_port='" + rpc_port + '\'' +
                ", taskmanager_heap_size='" + taskmanager_heap_size + '\'' +
                ", jobmanager_heap_size='" + jobmanager_heap_size + '\'' +
                '}';
    }

    public String print(ObjectMapper objectMapper) throws JsonProcessingException {
        return objectMapper.writeValueAsString(this).replace("-&gt;", "->");
    }
}

