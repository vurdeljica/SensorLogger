package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Accelerometer extends RealmObject {
    @PrimaryKey
    private long timestamp;

    private float x;
    private float y;
    private float z;

    private int accuracy;

    public Accelerometer() {
    }

    public Accelerometer(long timestamp, float x, float y, float z, int accuracy) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.accuracy = accuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
}
