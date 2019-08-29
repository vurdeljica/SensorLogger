package rs.ac.bg.etf.rti.sensorlogger;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import rs.ac.bg.etf.rti.sensorlogger.model.Pedometer;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;

public class PedometerEventListener implements SensorEventListener {
    private final static String TAG = "Pedometer";

    @Override
    public void onSensorChanged(SensorEvent event) {
        Pedometer pedometer = new Pedometer(event.timestamp, (int) event.values[0]);
        DatabaseManager.getInstance().insertOrUpdatePedometer(pedometer);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String prettyPrintFloatArray(float[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i < values.length - 1) {
                sb.append("|");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
