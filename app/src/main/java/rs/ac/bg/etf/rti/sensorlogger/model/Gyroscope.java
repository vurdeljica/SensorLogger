package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;

public class Gyroscope extends RealmObject {

    private float x;
    private float y;
    private float z;

    public Gyroscope() {
    }

    public Gyroscope(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Gyroscope(Gyroscope gyr) {
        if (gyr == null) return;

        this.x = gyr.x;
        this.y = gyr.y;
        this.z = gyr.z;
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
