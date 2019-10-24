package Spark;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "streamId",
        "streamName",
        "isActive",
        "executorId",
        "executorHost",
        "lastErrorTime",
        "lastErrorMessage",
        "lastError",
        "avgEventRate",
        "eventRates"
})
public class StreamingReceiver {

    @JsonProperty("streamId")
    public Long streamId;
    @JsonProperty("streamName")
    public String streamName;
    @JsonProperty("isActive")
    public Boolean isActive;
    @JsonProperty("executorId")
    public String executorId;
    @JsonProperty("executorHost")
    public String executorHost;
    @JsonProperty("lastErrorTime")
    public String lastErrorTime;
    @JsonProperty("lastErrorMessage")
    public String lastErrorMessage;
    @JsonProperty("lastError")
    public String lastError;
    @JsonProperty("avgEventRate")
    public Long avgEventRate;
    @JsonProperty("eventRates")
    public List<List<Long>> eventRates = null;

    @Override
    public String toString() {
        return "StreamingReceiver{" +
                "streamId=" + streamId +
                ", streamName='" + streamName + '\'' +
                ", isActive=" + isActive +
                ", executorId='" + executorId + '\'' +
                ", executorHost='" + executorHost + '\'' +
                ", lastErrorTime='" + lastErrorTime + '\'' +
                ", lastErrorMessage='" + lastErrorMessage + '\'' +
                ", lastError='" + lastError + '\'' +
                ", avgEventRate=" + avgEventRate +
                ", eventRates=" + eventRates +
                '}';
    }
}

/**
 * http://rserver01:8189/proxy/application_1532029170450_0623/api/v1/applications/application_1532029170450_0623/streaming/receivers
 * <p>
 * [
 * {
 * "streamId": 0,
 * "streamName": "SocketReceiver-0",
 * "isActive": false,
 * "executorId": "2",
 * "executorHost": "clu19.rserver",
 * "lastErrorTime": "2018-10-29T16:21:25.770GMT",
 * "lastErrorMessage": "Restarting receiver with delay 2000ms: Error connecting to localhost:9999",
 * "lastError": "java.net.ConnectException: Connection refused\n\tat java.net.PlainSocketImpl.socketConnect(Native Method)\n\tat java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)\n\tat java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:206)\n\tat java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:188)\n\tat java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)\n\tat java.net.Socket.connect(Socket.java:589)\n\tat java.net.Socket.connect(Socket.java:538)\n\tat java.net.Socket.<init>(Socket.java:434)\n\tat java.net.Socket.<init>(Socket.java:211)\n\tat org.apache.spark.streaming.dstream.SocketReceiver.onStart(SocketInputDStream.scala:61)\n\tat org.apache.spark.streaming.receiver.ReceiverSupervisor.startReceiver(ReceiverSupervisor.scala:149)\n\tat org.apache.spark.streaming.receiver.ReceiverSupervisor$$anonfun$restartReceiver$1.apply$mcV$sp(ReceiverSupervisor.scala:198)\n\tat org.apache.spark.streaming.receiver.ReceiverSupervisor$$anonfun$restartReceiver$1.apply(ReceiverSupervisor.scala:189)\n\tat org.apache.spark.streaming.receiver.ReceiverSupervisor$$anonfun$restartReceiver$1.apply(ReceiverSupervisor.scala:189)\n\tat scala.concurrent.impl.Future$PromiseCompletingRunnable.liftedTree1$1(Future.scala:24)\n\tat scala.concurrent.impl.Future$PromiseCompletingRunnable.run(Future.scala:24)\n\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)\n\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)\n\tat java.lang.Thread.run(Thread.java:745)\n",
 * "avgEventRate": 0,
 * "eventRates": [
 * [
 * 1540829087000,
 * 0
 * ]
 * ]
 * }
 * ]
 */