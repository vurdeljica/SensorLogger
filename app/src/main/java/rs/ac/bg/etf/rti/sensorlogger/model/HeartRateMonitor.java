package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class HeartRateMonitor extends RealmObject {
    @PrimaryKey
    private long timestamp;

    private float heartRate;

    private String nodeId;

    public HeartRateMonitor() {
    }

    public HeartRateMonitor(long timestamp, float heartRate, String nodeId) {
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.nodeId = nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getHeartRate() {
        return heartRate;
    }

    public String getNodeId() {
        return nodeId;
    }
}
