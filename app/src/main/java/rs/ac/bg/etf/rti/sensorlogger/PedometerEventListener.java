package rs.ac.bg.etf.rti.sensorlogger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class PedometerEventListener implements SensorEventListener {
    private final static String TAG = "Pedometer";

    private Context context;

    public PedometerEventListener(Context context) {
        this.context = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, prettyPrintFloatArray(event.values));
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
