package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;

public class Accelerometer extends RealmObject {

    private float x;
    private float y;
    private float z;

    public Accelerometer() {
    }

    public Accelerometer(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Accelerometer(Accelerometer acc) {
        if (acc == null) return;

        this.x = acc.x;
        this.y = acc.y;
        this.z = acc.z;
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

}
