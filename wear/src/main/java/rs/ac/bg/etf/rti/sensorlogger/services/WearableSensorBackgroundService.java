package rs.ac.bg.etf.rti.sensorlogger.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import rs.ac.bg.etf.rti.sensorlogger.R;

import static rs.ac.bg.etf.rti.sensorlogger.services.WearableDataLayerListenerService.SHARED_PREFERENCES_ID;

/**
 * Service for receiving sensor data from the wearable
 */
public class WearableSensorBackgroundService extends Service {

    private static final String TAG = WearableSensorBackgroundService.class.getSimpleName();
    public static final String SENSOR_SAMPLING_RATE_KEY = "sensorSamplingRate";

    /**
     * Path and keys of the data item to be sent to the phone
     */
    private final static String SENSOR_DATA_PATH = "/sensor_data";
    private final static String SENSOR_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.sensor_data";
    private final static String DATA_TYPE_KEY = "rs.ac.bg.etf.rti.sensorlogger.sensor_data_type";

    /**
     * Keys for each sensor type for storing the sensor data in the data item to be sent to the phone
     */
    private final static String DATA_HEART_RATE_TYPE = "rs.ac.bg.etf.rti.sensorlogger.heart_rate";
    private final static String DATA_HEART_RATE_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.heart_rate.data";
    private final static String DATA_HEART_RATE_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.heart_rate.timestamp";

    private final static String DATA_ACCELEROMETER_TYPE = "rs.ac.bg.etf.rti.sensorlogger.accelerometer";
    private final static String DATA_ACCELEROMETER_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.accelerometer.data";
    private final static String DATA_ACCELEROMETER_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.accelerometer.timestamp";

    private final static String DATA_MAGNETOMETER_TYPE = "rs.ac.bg.etf.rti.sensorlogger.magnetometer";
    private final static String DATA_MAGNETOMETER_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.magnetometer.data";
    private final static String DATA_MAGNETOMETER_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.magnetometer.timestamp";

    private final static String DATA_GYROSCOPE_TYPE = "rs.ac.bg.etf.rti.sensorlogger.gyroscope";
    private final static String DATA_GYROSCOPE_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.gyroscope.data";
    private final static String DATA_GYROSCOPE_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.gyroscope.timestamp";

    private final static String DATA_STEPS_TYPE = "rs.ac.bg.etf.rti.sensorlogger.steps";
    private final static String DATA_STEPS_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.steps.data";
    private final static String DATA_STEPS_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.steps.timestamp";

    private SensorManager sensorManager = null;

    private FlushableSensorEventListener sensorEventListener;

    private static final String PACKAGE_NAME =
            "rs.ac.bg.etf.rti.sensorlogger.services";

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_02";

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 23456789;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private Handler mServiceHandler;

    private PowerManager.WakeLock wakeLock;


    public WearableSensorBackgroundService() {
    }

    @Override
    public void onCreate() {
        sensorManager = getSystemService(SensorManager.class);

        PowerManager pm = getSystemService(PowerManager.class);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getCanonicalName());
        }

        sensorEventListener = new FlushableSensorEventListener() {
            @Override
            public void flushData() {
                sendData();
            }

            /**
             * Buffer for storing the sensor data to be sent
             */
            private ArrayList<DataMap> dataMaps = new ArrayList<>();

            @Override
            public void onSensorChanged(SensorEvent event) {
//                Log.d(TAG, prettyPrintFloatArray(event.values));

                event.timestamp = (System.currentTimeMillis() + ((event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L)) & ~3L;

                DataMap dataMap = new DataMap();

                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER: {
                        dataMap.putString(DATA_TYPE_KEY, DATA_ACCELEROMETER_TYPE);
                        dataMap.putFloatArray(DATA_ACCELEROMETER_DATA_KEY, event.values);
                        dataMap.putLong(DATA_ACCELEROMETER_TIMESTAMP_KEY, event.timestamp);
                        break;
                    }
                    case Sensor.TYPE_GYROSCOPE: {
                        dataMap.putString(DATA_TYPE_KEY, DATA_GYROSCOPE_TYPE);
                        dataMap.putFloatArray(DATA_GYROSCOPE_DATA_KEY, event.values);
                        dataMap.putLong(DATA_GYROSCOPE_TIMESTAMP_KEY, event.timestamp);
                        break;
                    }
                    case Sensor.TYPE_HEART_RATE: {
                        dataMap.putString(DATA_TYPE_KEY, DATA_HEART_RATE_TYPE);
                        dataMap.putFloatArray(DATA_HEART_RATE_DATA_KEY, event.values);
                        dataMap.putLong(DATA_HEART_RATE_TIMESTAMP_KEY, event.timestamp);
                        break;
                    }
                    case Sensor.TYPE_STEP_COUNTER: {
                        dataMap.putString(DATA_TYPE_KEY, DATA_STEPS_TYPE);
                        dataMap.putFloatArray(DATA_STEPS_DATA_KEY, event.values);
                        dataMap.putLong(DATA_STEPS_TIMESTAMP_KEY, event.timestamp);
                        break;
                    }
                    case Sensor.TYPE_MAGNETIC_FIELD: {
                        dataMap.putString(DATA_TYPE_KEY, DATA_MAGNETOMETER_TYPE);
                        dataMap.putFloatArray(DATA_MAGNETOMETER_DATA_KEY, event.values);
                        dataMap.putLong(DATA_MAGNETOMETER_TIMESTAMP_KEY, event.timestamp);
                        break;
                    }
                    default: {
                        Log.e(TAG, "onSensorChanged: Invalid sensor type");
                        return;
                    }
                }
                dataMaps.add(dataMap);
                if (dataMaps.size() >= 100) {
                    sendData();
                }

            }

            /**
             * Adds the collected sensor data to the data request and sends it to the phone
             */
            private void sendData() {
                PutDataMapRequest putDataRequest = PutDataMapRequest.create(SENSOR_DATA_PATH);
                putDataRequest.getDataMap().putDataMapArrayList(SENSOR_DATA_KEY, dataMaps);
                Task<DataItem> dataItemTask = Wearable.getDataClient(getApplicationContext()).putDataItem(putDataRequest.asPutDataRequest());
                dataItemTask.addOnFailureListener(e -> Log.e(TAG, "Failed to send data"));
                dataItemTask.addOnSuccessListener(e -> Log.i(TAG, "Sent data"));
                dataMaps.clear();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");

        if (intent == null && getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE).getBoolean(WearableDataLayerListenerService.IS_LISTENING_KEY, false)) {
            requestSensorEventUpdates();
        }

        // Tells the system to not try to recreate the service after it has been killed.
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration) {
            Log.i(TAG, "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Makes a request for sensor event updates.
     */
    public void requestSensorEventUpdates() {
        Log.i(TAG, "Requesting sensor data");

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        int samplingRate = getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE).getInt(SENSOR_SAMPLING_RATE_KEY, 200000);

        startService(new Intent(getApplicationContext(), WearableSensorBackgroundService.class));
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    accelerometerSensor,
                    samplingRate,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mServiceHandler
            );
        }

        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    gyroscopeSensor,
                    samplingRate,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mServiceHandler
            );
        }

        Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (heartRateSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    heartRateSensor,
                    samplingRate,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mServiceHandler
            );
        }

        Sensor stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCountSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    stepCountSensor,
                    samplingRate,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mServiceHandler
            );
        }

        Sensor magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometerSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    magnetometerSensor,
                    samplingRate,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mServiceHandler
            );
        }
    }

    /**
     * Removes sensor event updates.
     */
    public void removeSensorEventUpdates() {
        Log.i(TAG, "Removing sensor event updates");

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        sensorManager.unregisterListener(sensorEventListener);
        sensorEventListener.flushData();
        stopSelf();
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, WearableSensorBackgroundService.class);

        CharSequence text = getString(R.string.collecting_data);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(text)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        return builder.build();
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public WearableSensorBackgroundService getService() {
            return WearableSensorBackgroundService.this;
        }
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
