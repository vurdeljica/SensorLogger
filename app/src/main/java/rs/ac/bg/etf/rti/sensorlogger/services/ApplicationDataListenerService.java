package rs.ac.bg.etf.rti.sensorlogger.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import rs.ac.bg.etf.rti.sensorlogger.model.Accelerometer;
import rs.ac.bg.etf.rti.sensorlogger.model.Gyroscope;
import rs.ac.bg.etf.rti.sensorlogger.model.HeartRateMonitor;
import rs.ac.bg.etf.rti.sensorlogger.model.Pedometer;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.presentation.MainActivity;

public class ApplicationDataListenerService extends WearableListenerService {
    private static final String TAG = "DataLayerService";

    private final static String DATA_HEART_RATE_PATH = "/heart_rate";
    private final static String DATA_HEART_RATE_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.heart_rate.data";
    private final static String DATA_HEART_RATE_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.heart_rate.timestamp";
    private final static String DATA_HEART_RATE_NODE_KEY = "rs.ac.bg.etf.rti.sensorlogger.heart_rate.node";

    private final static String DATA_ACCELEROMETER_PATH = "/accelerometer";
    private final static String DATA_ACCELEROMETER_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.accelerometer.data";
    private final static String DATA_ACCELEROMETER_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.accelerometer.timestamp";
    private final static String DATA_ACCELEROMETER_NODE_KEY = "rs.ac.bg.etf.rti.sensorlogger.accelerometer.node";

    private final static String DATA_GYROSCOPE_PATH = "/gyroscope";
    private final static String DATA_GYROSCOPE_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.gyroscope.data";
    private final static String DATA_GYROSCOPE_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.gyroscope.timestamp";
    private final static String DATA_GYROSCOPE_NODE_KEY = "rs.ac.bg.etf.rti.sensorlogger.gyroscope.node";

    private final static String DATA_STEPS_PATH = "/steps";
    private final static String DATA_STEPS_DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.steps.data";
    private final static String DATA_STEPS_TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.steps.timestamp";
    private final static String DATA_STEPS_NODE_KEY = "rs.ac.bg.etf.rti.sensorlogger.steps.noed";

    private static final String MESSAGE_START_ACTIVITY_PATH = "/start-activity";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            DataItem dataItem = dataEvent.getDataItem();
            DatabaseManager databaseManager = DatabaseManager.getInstance();
            switch (dataItem.getUri().getPath()) {
                case DATA_HEART_RATE_PATH: {
                    DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
                    float data = dataMap.getFloatArray(DATA_HEART_RATE_DATA_KEY)[0];
                    long timestamp = dataMap.getLong(DATA_HEART_RATE_TIMESTAMP_KEY);
                    String nodeId = dataMap.getString(DATA_HEART_RATE_NODE_KEY);
                    HeartRateMonitor heartRate = new HeartRateMonitor(timestamp, data, nodeId);
                    databaseManager.insertOrUpdateHeartRateMonitor(heartRate);
                    break;
                }
                case DATA_ACCELEROMETER_PATH: {
                    DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
                    float[] data = dataMap.getFloatArray(DATA_ACCELEROMETER_DATA_KEY);
                    long timestamp = dataMap.getLong(DATA_ACCELEROMETER_TIMESTAMP_KEY);
                    String nodeId = dataMap.getString(DATA_ACCELEROMETER_NODE_KEY);
                    Accelerometer accelerometer = new Accelerometer(timestamp, data[0], data[1], data[2], nodeId);
                    databaseManager.insertOrUpdateAccelerometer(accelerometer);
                    break;
                }
                case DATA_GYROSCOPE_PATH: {
                    DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
                    float[] data = dataMap.getFloatArray(DATA_GYROSCOPE_DATA_KEY);
                    long timestamp = dataMap.getLong(DATA_GYROSCOPE_TIMESTAMP_KEY);
                    String nodeId = dataMap.getString(DATA_GYROSCOPE_NODE_KEY);
                    Gyroscope gyroscope = new Gyroscope(timestamp, data[0], data[1], data[2], nodeId);
                    databaseManager.insertOrUpdateGyroscope(gyroscope);
                    break;
                }
                case DATA_STEPS_PATH: {
                    DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
                    float[] data = dataMap.getFloatArray(DATA_STEPS_DATA_KEY);
                    long timestamp = dataMap.getLong(DATA_STEPS_TIMESTAMP_KEY);
                    String nodeId = dataMap.getString(DATA_STEPS_NODE_KEY);
                    Pedometer pedometer = new Pedometer(timestamp, (int) data[0], nodeId);
                    databaseManager.insertOrUpdatePedometer(pedometer);
                    break;
                }
                default: {
                    Log.e(TAG, "Unknown data path: " + dataItem.getUri().getPath());
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(MESSAGE_START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }
}
