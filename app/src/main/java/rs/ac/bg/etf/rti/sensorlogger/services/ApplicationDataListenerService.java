package rs.ac.bg.etf.rti.sensorlogger.services;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import rs.ac.bg.etf.rti.sensorlogger.model.DeviceSensorData;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;

import static rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager.deviceSensorDataBuffer;

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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
    }
}
