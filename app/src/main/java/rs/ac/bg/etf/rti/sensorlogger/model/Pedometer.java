package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Pedometer extends RealmObject {
    @PrimaryKey
    private long timestamp;

    private int stepCount;

    public Pedometer() {
    }

    public Pedometer(long timestamp, int stepCount) {
        this.timestamp = timestamp;
        this.stepCount = stepCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
}
