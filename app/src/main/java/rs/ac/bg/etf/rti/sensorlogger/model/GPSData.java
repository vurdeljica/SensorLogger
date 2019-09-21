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
    public GPSData(GPSData gps) {
        if (gps == null) return;

        this.timestamp = gps.timestamp;
        this.longitude = gps.longitude;
        this.latitude = gps.latitude;
        this.altitude = gps.altitude;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }
}
