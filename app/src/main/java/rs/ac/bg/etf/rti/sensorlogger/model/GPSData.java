package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Data object for storing the location received from the phone (not collected from wearables)
 */
public class GPSData extends RealmObject {

    //Timestamp of the data
    @PrimaryKey
    private long timestamp;

    //The three GPS coordinates
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
