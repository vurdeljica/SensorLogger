package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GPSData extends RealmObject {
    @PrimaryKey
    private long timestamp;

    private double longitude;
    private double latitude;
    private double altitude;

    public GPSData() {
    }

    public GPSData(long timestamp, double longitude, double latitude, double altitude) {
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
