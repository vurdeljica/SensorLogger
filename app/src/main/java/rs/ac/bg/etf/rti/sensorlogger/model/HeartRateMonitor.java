package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;

public class HeartRateMonitor extends RealmObject {

    private int heartRate;

    public HeartRateMonitor() {
    }

    public HeartRateMonitor(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getHeartRate() {
        return heartRate;
    }

}
