package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class HeartRateMonitor extends RealmObject {
    @PrimaryKey
    private long timestamp;

    private float heartRate;

    private int accuracy;

    public HeartRateMonitor() {
    }

    public HeartRateMonitor(long timestamp, float heartRate, int accuracy) {
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.accuracy = accuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(float heartRate) {
        this.heartRate = heartRate;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
}
