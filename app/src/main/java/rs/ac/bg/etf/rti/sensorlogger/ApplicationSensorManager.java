package rs.ac.bg.etf.rti.sensorlogger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSensorManager {
    private final SensorManager sensorManager;
    private List<SensorEventListener> activeSmListeners;

    public ApplicationSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        activeSmListeners = new ArrayList<>();
    }

    public void startListening(Context context) {
//        Sensor gpsSensor = sensorManager.getDefaultSensor(Sensor.);
//        if (gpsSensor != null) {
//            GPSEventListener gpsEventListener = new GPSEventListener(context);
//            activeSmListeners.add(gpsEventListener);
//            sensorManager.registerListener(
//                    gpsEventListener,
//                    gpsSensor,
//                    SensorManager.SENSOR_DELAY_NORMAL,
//                    SensorManager.SENSOR_DELAY_FASTEST
//            );
//        }

        Sensor pedometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (pedometerSensor != null) {
            PedometerEventListener pedometerEventListener = new PedometerEventListener(context);
            activeSmListeners.add(pedometerEventListener);
            sensorManager.registerListener(
                    pedometerEventListener,
                    pedometerSensor,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_FASTEST
            );
        }
    }

    public void stopListening() {
        for (SensorEventListener sensorEventListener: activeSmListeners) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }
}
