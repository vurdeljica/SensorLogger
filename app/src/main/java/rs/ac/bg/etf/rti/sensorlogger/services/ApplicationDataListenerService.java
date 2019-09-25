package rs.ac.bg.etf.rti.sensorlogger.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.annimon.stream.Stream;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.config.SensorLoggerApplication;
import rs.ac.bg.etf.rti.sensorlogger.model.DeviceSensorData;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.presentation.main.MainActivity;

import static rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager.deviceSensorDataBuffer;

/**
 * Background service that listens for sensor data sent from wearables
 * and the connection status changes in the node network
 */
public class ApplicationDataListenerService extends WearableListenerService {
    private static final String TAG = "DataLayerService";

    private final static String SENSOR_DATA_PATH = "/sensor_data";
    private final static String SENSOR_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.sensor_data";
    private final static String DATA_TYPE_KEY = "rs.ac.bg.etf.rti.sensorlogger.sensor_data_type";

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

    //channel id for the connection status change of the
    private static final String CHANNEL_ID = "lostConnectionChannel";

    //key for saving the connected nodes in the shared preferences
    private static final String CONNECTED_NODES_KEY = "connectedNodesKey";

    /**
     * Method called when data is received from other devices (nodes) in the network
     * @param dataEvents buffer containing the received data
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        for (DataEvent dataEvent : dataEvents) {
            DataItem dataItem = dataEvent.getDataItem();

            if (dataItem == null || dataItem.getUri() == null || dataItem.getUri().getPath() == null || !dataItem.getUri().getPath().equals(SENSOR_DATA_PATH)) {
                Log.e(TAG, "Incorrect path");
                continue;
            }

            if (dataItem.getData() == null) {
                Log.e(TAG, "No data");
                continue;
            }

            DataMap sensorDataMap = DataMap.fromByteArray(dataItem.getData());
            String nodeId = dataItem.getUri().getAuthority();

            DeviceSensorData latestDeviceSensorData = databaseManager.getLatestDeviceSensorData(nodeId);
            if (latestDeviceSensorData == null) {
                latestDeviceSensorData = new DeviceSensorData(nodeId, System.currentTimeMillis());
            }

            for (DataMap dataMap : sensorDataMap.getDataMapArrayList(SENSOR_DATA_KEY)) {
                DeviceSensorData deviceSensorData = new DeviceSensorData(latestDeviceSensorData);

                switch (dataMap.getString(DATA_TYPE_KEY)) {
                    case DATA_HEART_RATE_TYPE: {
                        float data = dataMap.getFloatArray(DATA_HEART_RATE_DATA_KEY)[0];
                        long timestamp = dataMap.getLong(DATA_HEART_RATE_TIMESTAMP_KEY);
                        deviceSensorData.setTimestamp(timestamp);
                        deviceSensorData.setHeartRate((int) data);
                        break;
                    }
                    case DATA_ACCELEROMETER_TYPE: {
                        float[] data = dataMap.getFloatArray(DATA_ACCELEROMETER_DATA_KEY);
                        long timestamp = dataMap.getLong(DATA_ACCELEROMETER_TIMESTAMP_KEY);
                        deviceSensorData.setTimestamp(timestamp);
                        deviceSensorData.setAccX(data[0]);
                        deviceSensorData.setAccY(data[1]);
                        deviceSensorData.setAccZ(data[2]);
                        break;
                    }
                    case DATA_GYROSCOPE_TYPE: {
                        float[] data = dataMap.getFloatArray(DATA_GYROSCOPE_DATA_KEY);
                        long timestamp = dataMap.getLong(DATA_GYROSCOPE_TIMESTAMP_KEY);
                        deviceSensorData.setTimestamp(timestamp);
                        deviceSensorData.setGyrX(data[0]);
                        deviceSensorData.setGyrY(data[1]);
                        deviceSensorData.setGyrZ(data[2]);
                        break;
                    }
                    case DATA_STEPS_TYPE: {
                        float[] data = dataMap.getFloatArray(DATA_STEPS_DATA_KEY);
                        long timestamp = dataMap.getLong(DATA_STEPS_TIMESTAMP_KEY);
                        deviceSensorData.setTimestamp(timestamp);
                        deviceSensorData.setStepCount((int) data[0]);
                        break;
                    }
                    case DATA_MAGNETOMETER_TYPE: {
                        float[] data = dataMap.getFloatArray(DATA_MAGNETOMETER_DATA_KEY);
                        long timestamp = dataMap.getLong(DATA_MAGNETOMETER_TIMESTAMP_KEY);
                        deviceSensorData.setTimestamp(timestamp);
                        deviceSensorData.setMagX(data[0]);
                        deviceSensorData.setMagY(data[1]);
                        deviceSensorData.setMagZ(data[2]);
                        break;
                    }
                    default: {
                        Log.e(TAG, "Unknown data type: " + dataMap.getString(DATA_TYPE_KEY));
                        return;
                    }
                }

                deviceSensorDataBuffer.add(deviceSensorData);
                latestDeviceSensorData = deviceSensorData;
            }
        }
        if (deviceSensorDataBuffer.size() >= 2000) {
            databaseManager.insertOrUpdateDeviceSensorData(deviceSensorDataBuffer);
            deviceSensorDataBuffer.clear();
        }
    }

    /**
     * Method called when connection status changes
     * @param capabilityInfo information about the capabilities of the connected devices (nodes),
     *                       devices with the wearable application have a special capability
     */
    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        createNotificationChannel();

        SharedPreferences sharedPref = getSharedPreferences(SensorLoggerApplication.SHARED_PREFERENCES_ID, Context.MODE_PRIVATE);
        Set<String> lastConnectedNodesSet = sharedPref.getStringSet(CONNECTED_NODES_KEY, Collections.emptySet());
        List<String> nodes = Stream.of(capabilityInfo.getNodes()).map(node -> node.getDisplayName() + " : " + node.getId()).toList();
        Set<String> newConnectedNodesSet = new HashSet<>(nodes);

        if (lastConnectedNodesSet.size() > nodes.size()) {
            lastConnectedNodesSet.removeAll(nodes);
            for (String node : lastConnectedNodesSet) {
                postNotification(node, true);
            }
        } else {
            nodes.removeAll(lastConnectedNodesSet);
            for (String node : nodes) {
                postNotification(node, false);
            }
        }

        sharedPref.edit().putStringSet(CONNECTED_NODES_KEY, newConnectedNodesSet).apply();
    }

    /**
     * Creates notification channel for the connection status change notification
     */
    private void createNotificationChannel() {
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


    /**
     * Posts the connection status change notification
     */
    private void postNotification(String nodeId, boolean isRemoved) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        CharSequence title = isRemoved ? getString(R.string.lost_connection) : getString(R.string.establish_connection);
        CharSequence text = String.format(isRemoved ? getString(R.string.lost_watch_connection) : getString(R.string.establish_watch_connection), nodeId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(title)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        int NOTIFICATION_ID = new Random(System.currentTimeMillis()).nextInt();
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
