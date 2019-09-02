package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Accelerometer extends RealmObject {
    @PrimaryKey
    private long timestamp;

    private float x;
    private float y;
    private float z;

    @Index
    private String nodeId;

    public Accelerometer() {
    }

    public Accelerometer(long timestamp, float x, float y, float z, String nodeId) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.nodeId = nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public String getNodeId() {
        return nodeId;
    }
}
