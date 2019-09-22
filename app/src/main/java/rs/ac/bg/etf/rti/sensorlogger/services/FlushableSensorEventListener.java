package rs.ac.bg.etf.rti.sensorlogger.services;

import android.hardware.SensorEventListener;

public interface FlushableSensorEventListener extends SensorEventListener {
    void flushData();
}
