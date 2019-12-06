package Flink;

import CoreUtils.Requests;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "jid",
        "name",
        "isStoppable",
        "state",
        "start-time",
        "end-time",
        "duration",
        "now",
        "timestamps",
        "vertices",
        "status-counts",
        "plan"
})
public class FlinkJob implements Serializable {

    @JsonProperty("jid")
    public String jid;
    @JsonProperty("name")
    public String name;
    @JsonProperty("isStoppable")
    public Boolean isStoppable;
    @JsonProperty("state")
    public String state;
    @JsonProperty("start-time")
    public Long startTime;
    @JsonProperty("end-time")
    public Long endTime;
    @JsonProperty("duration")
    public Long duration;
    @JsonProperty("now")
    public Long now;
    @JsonProperty("timestamps")
    public Timestamps timestamps;
    @JsonProperty("vertices")
    public List<Vertex> vertices = null;
    @JsonProperty("status-counts")
    public StatusCounts statusCounts;
    @JsonProperty("plan")
    public Plan plan;
    private final static long serialVersionUID = 8756157243539207649L;

    @Override
    public String toString() {
        return "FlinkJob{" +
                "jid='" + jid + '\'' +
                ", name='" + name + '\'' +
                ", isStoppable=" + isStoppable +
                ", state='" + state + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", now=" + now +
                ", timestamps=" + timestamps +
                ", vertices=" + vertices +
                ", statusCounts=" + statusCounts +
                ", plan=" + plan +
                '}';
    }


    /**
     * Updates the necessary metrics by contacting the REST api endpoint.
     *
     * @param masterURL    Flink master URL.
     * @param objectMapper
     * @throws Exception
     */
    public void getPeriodicMetrics(String masterURL, ObjectMapper objectMapper) {
        //Latency metrics
        try {
            String lat_url = "http://" + masterURL + "/jobs/" + this.jid + "/metrics";
            String base_latency_fmt = "latency.source_id.%s.source_subtask_index.0.operator_id.%s.operator_subtask_index.0.latency_";

            List<Vertex> sources = this.vertices.stream().filter(v -> v.name.startsWith("Source")).collect(Collectors.toList());
            List<Vertex> non_sources = this.vertices.stream().filter(v -> !v.name.startsWith("Source")).collect(Collectors.toList());

            for (Vertex source : sources) {
                HashMap<String, LatencyNode> vLatencies = new HashMap<>(); // <Non-Source vertex id,Latencies>
                for (Vertex non_source : non_sources) {
                    List<String> params = new ArrayList<>();
                    params.add(String.format(base_latency_fmt + "stddev", source.id, non_source.id));
                    params.add(String.format(base_latency_fmt + "min", source.id, non_source.id));
                    params.add(String.format(base_latency_fmt + "max", source.id, non_source.id));
                    params.add(String.format(base_latency_fmt + "median", source.id, non_source.id));
                    params.add(String.format(base_latency_fmt + "p99", source.id, non_source.id));
                    List<HashMap<String, String>> lat_map = objectMapper.readValue(Requests.GET(lat_url, params), new TypeReference<List<HashMap<String, String>>>() {
                    });
                    LatencyNode latencyNode = new LatencyNode();
                    for (HashMap<String, String> json_map : lat_map) {
                        String id_field = json_map.get("id");
                        String val_field = json_map.get("value");
                        if (id_field.endsWith("stddev")) {
                            latencyNode.stddev = val_field;
                        } else if (id_field.endsWith("min")) {
                            latencyNode.min = val_field;
                        } else if (id_field.endsWith("max")) {
                            latencyNode.max = val_field;
                        } else if (id_field.endsWith("median")) {
                            latencyNode.median = val_field;
                        } else if (id_field.endsWith("p99")) {
                            latencyNode.p99 = val_field;
                        } else {
                            System.out.println("Error parsing latency at source:" + source + " where val=" + id_field);
                        }

                    }
                    vLatencies.put(non_source.id, latencyNode);
                }
                source.vertexLatencies = vLatencies; //Add all latency metrics for each non-source vertex to this source.
            }
        } catch (Exception ignored) {
        }

        //Update Throughput metrics
        try {
            for (Vertex v : this.vertices) {
                String tp_url = "http://" + masterURL + "/jobs/" + this.jid + "/vertices/" + v.id + "/subtasks/metrics";
                List<String> params = new ArrayList<>();
                params.add("numRecordsIn");
                params.add("numRecordsInPerSecond");
                params.add("numBytesInRemotePerSecond");
                params.add("numRecordsOut");
                params.add("numRecordsOutPerSecond");
                params.add("numBytesOutPerSecond");
                params.add("numBytesInLocalPerSecond");
                List<TPStats> tp_list = objectMapper.readValue(Requests.GET(tp_url, params), new TypeReference<List<TPStats>>() {
                });
                ThroughputNode tp = new ThroughputNode();
                for (TPStats entry : tp_list) {
                    switch (entry.id) {
                        case "numRecordsIn":
                            tp.numRecordsIn = entry;
                            break;
                        case "numRecordsInPerSecond":
                            tp.numRecordsInPerSecond = entry;
                            break;
                        case "numBytesInRemotePerSecond":
                            tp.numBytesInRemotePerSecond = entry;
                            break;
                        case "numRecordsOut":
                            tp.numRecordsOut = entry;
                            break;
                        case "numRecordsOutPerSecond":
                            tp.numRecordsOutPerSecond = entry;
                            break;
                        case "numBytesOutPerSecond":
                            tp.numBytesOutPerSecond = entry;
                            break;
                        case "numBytesInLocalPerSecond":
                            tp.numBytesInLocalPerSecond = entry;
                            break;
                        default:
                            System.out.println("Error parsing tp node: " + entry);
                            break;
                    }
                }
                v.throughputNode = tp;
            }
        } catch (Exception ignored) {
        }

        //Update Backpressure metrics.
        //Backpressure vertices go online AFTER topology
        try {
            for (Vertex v : this.vertices) {
                String bp_url = "http://" + masterURL + "/jobs/" + this.jid + "/vertices/" + v.id + "/backpressure";
                String res = Requests.GET(bp_url, null);
                if (res != null) {
                    v.backpressureNode = objectMapper.readValue(res, new TypeReference<BackpressureNode>() {
                    });
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Pretty print method.
     *
     * @param objectMapper The OM object.
     * @return The equivalent of toString() method.
     */
    public String print(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this).replace("-&gt;", "->");
        } catch (JsonProcessingException e) {
            return "Can't parse flink job: " + this.jid;
        }
    }

    /**
     * Commits influxDB measurements based on the collected Metrics.
     *
     * @param batchPoints Points to be commited.
     */
    public void putDBpoints(BatchPoints batchPoints) {
        //Job details
        batchPoints.point(Point.measurement("flink_job")
                .time(this.now, TimeUnit.MILLISECONDS)
                .tag("jid", this.jid)
                .tag("name", this.name)
                .addField("isStoppable", this.isStoppable)
                .addField("state", this.state)
                .addField("endTime", this.endTime)
                .addField("duration", this.duration)
                .addField("now", this.now)
                .build());

        //Create a serial number for each vertex, easier to visualise.
        HashMap<String, String> vid_sn = new HashMap<>();
        int cnt = 0;
        for (Vertex v : this.vertices) {
            vid_sn.put(v.id, String.valueOf(cnt++));
        }

        //for each vertex-operator of this job
        for (Vertex v : this.vertices) {
            //Metrics
            batchPoints.point(Point.measurement("readBytes")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.metrics.readBytes)
                    .build());
            batchPoints.point(Point.measurement("readBytesComplete")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.metrics.readBytesComplete)
                    .build());
            batchPoints.point(Point.measurement("writeBytes")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.metrics.writeBytes)
                    .build());
            batchPoints.point(Point.measurement("writeBytesComplete")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.metrics.writeBytesComplete)
                    .build());
            batchPoints.point(Point.measurement("readRecordsComplete")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.metrics.readRecordsComplete)
                    .build());
            batchPoints.point(Point.measurement("writeRecordsComplete")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.metrics.writeRecordsComplete)
                    .build());

            //Throughput
            batchPoints.point(Point.measurement("numRecordsInMin")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsIn.min)
                    .build());
            batchPoints.point(Point.measurement("numRecordsInMax")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsIn.max)
                    .build());
            batchPoints.point(Point.measurement("numRecordsInAvg")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsIn.avg)
                    .build());
            batchPoints.point(Point.measurement("numRecordsInSum")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsIn.sum)
                    .build());

            batchPoints.point(Point.measurement("numRecordsInPerSecondMin")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsInPerSecond.min)
                    .build());
            batchPoints.point(Point.measurement("numRecordsInPerSecondMax")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsInPerSecond.max)
                    .build());
            batchPoints.point(Point.measurement("numRecordsInPerSecondAvg")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsInPerSecond.avg)
                    .build());
            batchPoints.point(Point.measurement("numRecordsInPerSecondSum")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsInPerSecond.sum)
                    .build());

            batchPoints.point(Point.measurement("numBytesInRemotePerSecondMin")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesInRemotePerSecond.min)
                    .build());
            batchPoints.point(Point.measurement("numBytesInRemotePerSecondMax")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesInRemotePerSecond.max)
                    .build());
            batchPoints.point(Point.measurement("numBytesInRemotePerSecondAvg")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesInRemotePerSecond.avg)
                    .build());
            batchPoints.point(Point.measurement("numBytesInRemotePerSecondSum")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesInRemotePerSecond.sum)
                    .build());

            batchPoints.point(Point.measurement("numRecordsOutMin")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOut.min)
                    .build());
            batchPoints.point(Point.measurement("numRecordsOutMax")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOut.max)
                    .build());
            batchPoints.point(Point.measurement("numRecordsOutAvg")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOut.avg)
                    .build());
            batchPoints.point(Point.measurement("numRecordsOutSum")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOut.sum)
                    .build());

            batchPoints.point(Point.measurement("numRecordsOutPerSecondMin")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOutPerSecond.min)
                    .build());
            batchPoints.point(Point.measurement("numRecordsOutPerSecondMax")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOutPerSecond.max)
                    .build());
            batchPoints.point(Point.measurement("numRecordsOutPerSecondAvg")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOutPerSecond.avg)
                    .build());
            batchPoints.point(Point.measurement("numRecordsOutPerSecondSum")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOutPerSecond.sum)
                    .build());

            batchPoints.point(Point.measurement("numBytesOutPerSecondMin")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesOutPerSecond.min)
                    .build());
            batchPoints.point(Point.measurement("numBytesOutPerSecondMax")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesOutPerSecond.max)
                    .build());
            batchPoints.point(Point.measurement("numBytesOutPerSecondAvg")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesOutPerSecond.avg)
                    .build());
            batchPoints.point(Point.measurement("numBytesOutPerSecondSum")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesOutPerSecond.sum)
                    .build());

            batchPoints.point(Point.measurement("numBytesInLocalPerSecond")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesInLocalPerSecond.min)
                    .build());
            batchPoints.point(Point.measurement("numBytesInLocalPerSecondMax")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesInLocalPerSecond.max)
                    .build());
            batchPoints.point(Point.measurement("numBytesInLocalPerSecondAvg")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numBytesInLocalPerSecond.avg)
                    .build());
            batchPoints.point(Point.measurement("numBytesInLocalPerSecondSum")
                    .time(this.now, TimeUnit.MILLISECONDS)
                    .tag("jid", this.jid)
                    .tag("platform", "flink")
                    .tag("vertex_id", v.id)
                    .tag("vertex", vid_sn.get(v.id))
                    .addField("value", v.throughputNode.numRecordsOut.sum)
                    .build());

            //Backpressure
            if (v.backpressureNode != null) {
                if (!v.backpressureNode.status.equals("deprecated")) {
                    for (Subtask subtask : v.backpressureNode.subtasks) {
                        batchPoints.point(Point.measurement("backpressure")
                                .time(this.now, TimeUnit.MILLISECONDS)
                                .tag("jid", this.jid)
                                .tag("platform", "flink")
                                .tag("vertex_id", v.id)
                                .tag("vertex", vid_sn.get(v.id))
                                .tag("vertex_status", v.backpressureNode.status)
                                .tag("subtask", String.valueOf(subtask.subtask))
                                .tag("subtask_level", subtask.backpressureLevel)
                                .addField("subtask_ratio", subtask.ratio.doubleValue())
                                .build());
                    }
                }
            }

            //Latencies (Only available at source vertices)
            HashMap<String, LatencyNode> vid_lnode = v.vertexLatencies;
            if (vid_lnode == null) {
                continue;
            }
            for (String vid : vid_lnode.keySet()) { //For each
                batchPoints.point(Point.measurement("lat_stddev")
                        .time(this.now, TimeUnit.MILLISECONDS)
                        .tag("jid", this.jid)
                        .tag("platform", "flink")
                        .tag("vertex_id", vid)
                        .tag("vertex", vid_sn.get(vid))
                        .tag("source", vid_sn.get(v.id))
                        .addField("value", Double.parseDouble(vid_lnode.get(vid).stddev))
                        .build());
                batchPoints.point(Point.measurement("lat_max")
                        .time(this.now, TimeUnit.MILLISECONDS)
                        .tag("jid", this.jid)
                        .tag("platform", "flink")
                        .tag("platform", "flink")
                        .tag("vertex_id", vid)
                        .tag("vertex", vid_sn.get(vid))
                        .tag("source", vid_sn.get(v.id))
                        .addField("value", Double.parseDouble(vid_lnode.get(vid).max))
                        .build());
                batchPoints.point(Point.measurement("lat_median")
                        .time(this.now, TimeUnit.MILLISECONDS)
                        .tag("jid", this.jid)
                        .tag("platform", "flink")
                        .tag("platform", "flink")
                        .tag("vertex_id", vid)
                        .tag("vertex", vid_sn.get(vid))
                        .tag("source", vid_sn.get(v.id))
                        .addField("value", Double.parseDouble(vid_lnode.get(vid).median))
                        .build());
                batchPoints.point(Point.measurement("lat_p99")
                        .time(this.now, TimeUnit.MILLISECONDS)
                        .tag("jid", this.jid)
                        .tag("platform", "flink")
                        .tag("platform", "flink")
                        .tag("vertex_id", vid)
                        .tag("vertex", vid_sn.get(vid))
                        .tag("source", vid_sn.get(v.id))
                        .addField("value", Double.parseDouble(vid_lnode.get(vid).p99))
                        .build());
                batchPoints.point(Point.measurement("lat_min")
                        .time(this.now, TimeUnit.MILLISECONDS)
                        .tag("jid", this.jid)
                        .tag("platform", "flink")
                        .tag("platform", "flink")
                        .tag("vertex_id", vid)
                        .tag("vertex", vid_sn.get(vid))
                        .tag("source", vid_sn.get(v.id))
                        .addField("value", Double.parseDouble(vid_lnode.get(vid).min))
                        .build());
            }
        }
    }

    public void putSingleDBpoint(BatchPoints batchPoints) {

        Point.Builder point = Point.measurement("flink_job")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("jid", this.jid)
                .addField("name", this.name)
                .addField("isStoppable", this.isStoppable)
                .addField("state", this.state)
                .addField("endTime", this.endTime)
                .addField("duration", this.duration)
                .addField("now", this.now);


        //Iterate through all vertices (ALIAS vertex_id AS cnt)
        HashMap<String, Integer> vertexId_cnt = new HashMap<>();
        int cnt = 0;
        for (Vertex vertex : this.vertices) {
            String prefix = "vertex_" + cnt + "_";
            vertexId_cnt.put(vertex.id, cnt++);
            point
                    //Vertex properties
                    .addField(prefix + "id", vertex.id)
                    .addField(prefix + "name", vertex.name)
                    .addField(prefix + "parallelism", vertex.name)
                    .addField(prefix + "status", vertex.status)
                    .addField(prefix + "startTime", vertex.startTime)
                    .addField(prefix + "endTime", vertex.endTime)
                    .addField(prefix + "duration", vertex.duration)
                    //Data size
                    .addField(prefix + "readBytes", vertex.metrics.readBytes)
                    .addField(prefix + "readBytesComplete", vertex.metrics.readBytesComplete)
                    .addField(prefix + "writeBytes", vertex.metrics.writeBytes)
                    .addField(prefix + "writeBytesComplete", vertex.metrics.writeBytesComplete)
                    //Throughput
                    .addField(prefix + "readRecords", vertex.metrics.readRecords)
                    .addField(prefix + "readRecordsComplete", vertex.metrics.readRecordsComplete)
                    .addField(prefix + "writeRecords", vertex.metrics.writeRecords)
                    .addField(prefix + "writeRecords", vertex.metrics.writeRecordsComplete)
                    .addField(prefix + "numRecordsInMin", vertex.throughputNode.numRecordsIn.min)
                    .addField(prefix + "numRecordsInMax", vertex.throughputNode.numRecordsIn.max)
                    .addField(prefix + "numRecordsInAvg", vertex.throughputNode.numRecordsIn.avg)
                    .addField(prefix + "numRecordsInSum", vertex.throughputNode.numRecordsIn.sum)
                    .addField(prefix + "numRecordsIn", vertex.throughputNode.numRecordsIn.sum);


            //Only source vertices keep track of vertex latencies.
            //Collect latencies FROM each source FOR each non-source vertex.
            for (Vertex v : this.vertices) {
                if (v.vertexLatencies != null) {//Source vertex
                    for (String vid : v.vertexLatencies.keySet()) {
                        if (vertex.id.equals(vid)) { //Source vertex with latency metrics for this non-source vertex.
                            LatencyNode node_for_me = v.vertexLatencies.get(vid);
                            String lat_prefix = prefix + vertexId_cnt.get(vid) + "_";
                            point.addField(lat_prefix + "stddev", node_for_me.stddev);
                            point.addField(lat_prefix + "min", node_for_me.min);
                            point.addField(lat_prefix + "max", node_for_me.max);
                            point.addField(lat_prefix + "median", node_for_me.median);
                            point.addField(lat_prefix + "p99", node_for_me.p99);
                        }
                    }
                }
            }

        }
        batchPoints.point(point.build());
    }
}
