package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.SensorDataProtos;
import rs.ac.bg.etf.rti.sensorlogger.model.Accelerometer;
import rs.ac.bg.etf.rti.sensorlogger.model.GPSData;
import rs.ac.bg.etf.rti.sensorlogger.model.Gyroscope;
import rs.ac.bg.etf.rti.sensorlogger.model.HeartRateMonitor;
import rs.ac.bg.etf.rti.sensorlogger.model.Pedometer;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.persistency.PersistenceManager;

public class StoreFileWorker extends Worker {
    public StoreFileWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long endTime = System.currentTimeMillis();
        DatabaseManager dbManager = DatabaseManager.getInstance();
        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        List<SensorDataProtos.MobileData> mobileDataList = new ArrayList<>();
        HashMap<String, List<SensorDataProtos.DeviceData>> deviceDataHashMap = new HashMap<>();

        for (GPSData gpsData : dbManager.getGPSData()) {
            SensorDataProtos.MobileData mobileData = SensorDataProtos.MobileData.newBuilder()
                    .setGpsAltitude((float) gpsData.getAltitude())
                    .setGpsLongitude((float) gpsData.getLongitude())
                    .setGpsLatitude((float) gpsData.getLatitude())
                    .setTimestamp(gpsData.getTimestamp())
                    .build();
            mobileDataList.add(mobileData);
        }

        if (!mobileDataList.isEmpty()) {
            persistenceManager.saveMobileData(mobileDataList, dbManager.getGPSTimestamp());
            dbManager.deleteGPSDataBefore(endTime);
        }

        Pair<String, Long> latestSensorData = dbManager.getLatestSensorData();
        String nodeId = latestSensorData.first;
        long timestamp = latestSensorData.second;
        if (timestamp == 0) {
            return Result.success();
        }
        while (true) {
            Accelerometer accelerometer = dbManager.getLatestAccelerometer(nodeId);
            Gyroscope gyroscope = dbManager.getLatestGyroscope(nodeId);
            Pedometer pedometer = dbManager.getLatestPedometer(nodeId);
            HeartRateMonitor heartRateMonitor = dbManager.getLatestHeartRateMonitor(nodeId);

            SensorDataProtos.DeviceData.Builder deviceDataBuilder = SensorDataProtos.DeviceData.newBuilder()
                    .setTimestamp(timestamp)
                    .setNodeId(nodeId);

            if (accelerometer != null) {
                deviceDataBuilder
                        .setAccX(accelerometer.getX())
                        .setAccY(accelerometer.getY())
                        .setAccZ(accelerometer.getZ());
                if (accelerometer.getTimestamp() == timestamp) {
                    dbManager.deleteAccelerometer(accelerometer.getTimestamp());
                }
            }
            if (gyroscope != null) {
                deviceDataBuilder
                        .setGyrX(gyroscope.getX())
                        .setGyrY(gyroscope.getY())
                        .setGyrZ(gyroscope.getZ());
                if (gyroscope.getTimestamp() == timestamp) {
                    dbManager.deleteGyroscope(gyroscope.getTimestamp());
                }
            }
            if (pedometer != null) {
                deviceDataBuilder.setStepCount(pedometer.getStepCount());
                if (pedometer.getTimestamp() == timestamp) {
                    dbManager.deletePedometer(pedometer.getTimestamp());
                }
            }
            if (heartRateMonitor != null) {
                deviceDataBuilder.setHeartRate(heartRateMonitor.getHeartRate());
                if (heartRateMonitor.getTimestamp() == timestamp) {
                    dbManager.deleteHeartRateMonitor(heartRateMonitor.getTimestamp());
                }
            }

            SensorDataProtos.DeviceData deviceData = deviceDataBuilder.build();
            if (!deviceDataHashMap.containsKey(nodeId)) {
                deviceDataHashMap.put(nodeId, new ArrayList<>());
            }
            deviceDataHashMap.get(nodeId).add(deviceData);

            latestSensorData = dbManager.getLatestSensorData();
            if (latestSensorData.second == 0) {
                break;
            }
            nodeId = latestSensorData.first;
            timestamp = latestSensorData.second;
        }

        for (String node : deviceDataHashMap.keySet()) {
            persistenceManager.saveDeviceData(deviceDataHashMap.get(node), node, timestamp);
        }

//        dbManager.deleteSensorDataBefore(endTime);

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
