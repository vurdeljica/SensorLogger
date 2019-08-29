package rs.ac.bg.etf.rti.sensorlogger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSensorManager {
    private static final long UPDATE_INTERVAL = 33333;
    private final SensorManager sensorManager;
    private List<SensorEventListener> activeSmListeners;

    public ApplicationSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        activeSmListeners = new ArrayList<>();
    }

    public void startListening(Context context) {
        Sensor pedometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (pedometerSensor != null) {
            PedometerEventListener pedometerEventListener = new PedometerEventListener();
            activeSmListeners.add(pedometerEventListener);
            sensorManager.registerListener(
                    pedometerEventListener,
                    pedometerSensor,
                    SensorManager.SENSOR_DELAY_FASTEST,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }

    public void stopListening() {
        for (SensorEventListener sensorEventListener : activeSmListeners) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }
}
