package rs.ac.bg.etf.rti.sensorlogger.services;

import android.hardware.SensorEventListener;

/**
 * Interface for receiving new periodic sensor values and batches of values when the sensor buffer is flushed
 */
public interface FlushableSensorEventListener extends SensorEventListener {
    void flushData();
}
