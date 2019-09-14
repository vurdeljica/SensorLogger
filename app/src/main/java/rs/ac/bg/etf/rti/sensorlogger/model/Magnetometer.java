package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;

public class Magnetometer extends RealmObject {

    private float x;
    private float y;
    private float z;

    public Magnetometer() {
    }

    public Magnetometer(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
