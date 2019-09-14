package rs.ac.bg.etf.rti.sensorlogger.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.model.Accelerometer;
import rs.ac.bg.etf.rti.sensorlogger.model.Gyroscope;
import rs.ac.bg.etf.rti.sensorlogger.model.HeartRateMonitor;
import rs.ac.bg.etf.rti.sensorlogger.model.Pedometer;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel.IS_LISTENING_KEY;

public class ApplicationSensorBackgroundService extends Service {
    private static final String NODE_ID_KEY = "nodeId";
    private static final String TAG = ApplicationSensorBackgroundService.class.getSimpleName();

    private final int SAMPLING_PERIOD = 33333;
    private final int LATENCY_PERIOD = 100000;

    private SensorManager sensorManager = null;

    private SensorEventListener sensorEventListener;

    private static final String PACKAGE_NAME =
            "rs.ac.bg.etf.rti.sensorlogger.services";

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_03";

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 34567890;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private Handler mServiceHandler;

    public ApplicationSensorBackgroundService() {
    }

    @Override
    public void onCreate() {
        sensorManager = getSystemService(SensorManager.class);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
//                Log.d(TAG, prettyPrintFloatArray(event.values));

                event.timestamp = System.currentTimeMillis() + ((event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L);

                String nodeId = getDefaultSharedPreferences(getApplicationContext()).getString(NODE_ID_KEY, getApplicationContext().getString(R.string.unknown));

                DatabaseManager databaseManager = DatabaseManager.getInstance();

                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER: {
                        Accelerometer accelerometer = new Accelerometer(event.timestamp, event.values[0], event.values[1], event.values[2], nodeId);
                        databaseManager.insertOrUpdateAccelerometer(accelerometer);
                        break;
                    }
                    case Sensor.TYPE_GYROSCOPE: {
                        Gyroscope gyroscope = new Gyroscope(event.timestamp, event.values[0], event.values[1], event.values[2], nodeId);
                        databaseManager.insertOrUpdateGyroscope(gyroscope);
                        break;
                    }
                    case Sensor.TYPE_HEART_RATE: {
                        HeartRateMonitor heartRate = new HeartRateMonitor(event.timestamp, (int) event.values[0], nodeId);
                        databaseManager.insertOrUpdateHeartRateMonitor(heartRate);
                        break;
                    }
                    case Sensor.TYPE_STEP_COUNTER: {
                        Pedometer pedometer = new Pedometer(event.timestamp, (int) event.values[0], nodeId);
                        databaseManager.insertOrUpdatePedometer(pedometer);
                        break;
                    }
                    default: {
                        Log.e(TAG, "onSensorChanged: Invalid sensor type");
                    }
                }
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
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");

        if (intent == null && getDefaultSharedPreferences(this).getBoolean(IS_LISTENING_KEY, false)) {
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
        startService(new Intent(getApplicationContext(), ApplicationSensorBackgroundService.class));
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    accelerometerSensor,
                    SAMPLING_PERIOD,
                    LATENCY_PERIOD,
                    mServiceHandler
            );
        }

        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    gyroscopeSensor,
                    SAMPLING_PERIOD,
                    LATENCY_PERIOD,
                    mServiceHandler
            );
        }

        Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (heartRateSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    heartRateSensor,
                    SensorManager.SENSOR_DELAY_FASTEST,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    mServiceHandler
            );
        }

        Sensor stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCountSensor != null) {
            sensorManager.registerListener(
                    sensorEventListener,
                    stepCountSensor,
                    SensorManager.SENSOR_DELAY_FASTEST,
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
        sensorManager.unregisterListener(sensorEventListener);
        stopSelf();
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, ApplicationSensorBackgroundService.class);

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
        public ApplicationSensorBackgroundService getService() {
            return ApplicationSensorBackgroundService.this;
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
