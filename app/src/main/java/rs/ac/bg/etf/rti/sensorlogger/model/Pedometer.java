package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Pedometer extends RealmObject {
    @PrimaryKey
    private long timestamp;

    private int stepCount;

    @Index
    private String nodeId;

    public Pedometer() {
    }

    public Pedometer(long timestamp, int stepCount, String nodeId) {
        this.timestamp = timestamp;
        this.stepCount = stepCount;
        this.nodeId = nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getStepCount() {
        return stepCount;
    }

    public String getNodeId() {
        return nodeId;
    }
}
