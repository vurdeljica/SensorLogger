package rs.ac.bg.etf.rti.sensorlogger.managers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.listeners.AccelerometerEventListener;
import rs.ac.bg.etf.rti.sensorlogger.listeners.GyroscopeEventListener;
import rs.ac.bg.etf.rti.sensorlogger.listeners.HeartRateEventListener;
import rs.ac.bg.etf.rti.sensorlogger.listeners.PedometerEventListener;

public class WearableSensorManager {
    //sensor sampling period in microseconds - frequency approx. 30 Hz
    private final int SAMPLING_PERIOD = 33333;
    private final SensorManager sensorManager;
    private List<SensorEventListener> activeSmListeners;

    public WearableSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        activeSmListeners = new ArrayList<>();
    }

    public void startListening(Context context) {
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            AccelerometerEventListener accelerometerEventListener = new AccelerometerEventListener(context);
            activeSmListeners.add(accelerometerEventListener);
            sensorManager.registerListener(
                    accelerometerEventListener,
                    accelerometerSensor,
                    SAMPLING_PERIOD
            );
        }

        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscopeSensor != null) {
            GyroscopeEventListener gyroscopeEventListener = new GyroscopeEventListener(context);
            activeSmListeners.add(gyroscopeEventListener);
            sensorManager.registerListener(
                    gyroscopeEventListener,
                    gyroscopeSensor,
                    SAMPLING_PERIOD
            );
        }

        Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (heartRateSensor != null) {
            HeartRateEventListener heartRateEventListener = new HeartRateEventListener(context);
            activeSmListeners.add(heartRateEventListener);
            sensorManager.registerListener(
                    heartRateEventListener,
                    heartRateSensor,
                    SensorManager.SENSOR_DELAY_FASTEST,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }

        Sensor stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCountSensor != null) {
            PedometerEventListener pedometerEventListener = new PedometerEventListener(context);
            activeSmListeners.add(pedometerEventListener);
            sensorManager.registerListener(
                    pedometerEventListener,
                    stepCountSensor,
                    SensorManager.SENSOR_DELAY_FASTEST,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }

    public void stopListening() {
        for (SensorEventListener sensorEventListener: activeSmListeners) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }
}
