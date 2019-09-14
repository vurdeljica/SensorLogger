package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class DeviceSensorData extends RealmObject {
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private HeartRateMonitor heartRateMonitor;
    private Pedometer pedometer;

    @Index
    private String nodeId;

    @PrimaryKey
    private long timestamp;

    public DeviceSensorData() {
    }

    public DeviceSensorData(Accelerometer accelerometer, Gyroscope gyroscope, HeartRateMonitor heartRateMonitor, Pedometer pedometer, String nodeId, long timestamp) {
        this.accelerometer = accelerometer;
        this.gyroscope = gyroscope;
        this.heartRateMonitor = heartRateMonitor;
        this.pedometer = pedometer;
        this.nodeId = nodeId;
        this.timestamp = timestamp;
    }

    public Accelerometer getAccelerometer() {
        return accelerometer;
    }

    public Gyroscope getGyroscope() {
        return gyroscope;
    }

    public HeartRateMonitor getHeartRateMonitor() {
        return heartRateMonitor;
    }

    public Pedometer getPedometer() {
        return pedometer;
    }

    public String getNodeId() {
        return nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setAccelerometer(Accelerometer accelerometer) {
        this.accelerometer = accelerometer;
    }

    public void setGyroscope(Gyroscope gyroscope) {
        this.gyroscope = gyroscope;
    }

    public void setHeartRateMonitor(HeartRateMonitor heartRateMonitor) {
        this.heartRateMonitor = heartRateMonitor;
    }

    public void setPedometer(Pedometer pedometer) {
        this.pedometer = pedometer;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void cascadeDelete() {
        if (accelerometer != null) {
            accelerometer.deleteFromRealm();
        }
        if (gyroscope != null) {
            gyroscope.deleteFromRealm();
        }
        if (heartRateMonitor != null) {
            heartRateMonitor.deleteFromRealm();
        }
        if (pedometer != null) {
            pedometer.deleteFromRealm();
        }
        deleteFromRealm();
    }
}
