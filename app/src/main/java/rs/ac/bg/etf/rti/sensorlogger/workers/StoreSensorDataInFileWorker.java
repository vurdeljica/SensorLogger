package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.SensorDataProtos;
import rs.ac.bg.etf.rti.sensorlogger.model.Accelerometer;
import rs.ac.bg.etf.rti.sensorlogger.model.DeviceSensorData;
import rs.ac.bg.etf.rti.sensorlogger.model.GPSData;
import rs.ac.bg.etf.rti.sensorlogger.model.Gyroscope;
import rs.ac.bg.etf.rti.sensorlogger.model.HeartRateMonitor;
import rs.ac.bg.etf.rti.sensorlogger.model.Pedometer;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.persistency.PersistenceManager;

public class StoreSensorDataInFileWorker extends Worker {
    public StoreSensorDataInFileWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long endTime = System.currentTimeMillis();
        DatabaseManager dbManager = DatabaseManager.getInstance();
        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        HashMap<String, List<SensorDataProtos.DeviceData>> deviceDataHashMap = new HashMap<>();

        for (DeviceSensorData deviceSensorData : dbManager.getDeviceSensorData()) {
            String nodeId = deviceSensorData.getNodeId();

            SensorDataProtos.DeviceData.Builder deviceDataBuilder = SensorDataProtos.DeviceData.newBuilder()
                    .setTimestamp(deviceSensorData.getTimestamp())
                    .setNodeId(deviceSensorData.getNodeId());

            Accelerometer accelerometer = deviceSensorData.getAccelerometer();
            if (accelerometer != null) {
                deviceDataBuilder
                        .setAccX(accelerometer.getX())
                        .setAccY(accelerometer.getY())
                        .setAccZ(accelerometer.getZ());
            }

            Gyroscope gyroscope = deviceSensorData.getGyroscope();
            if (gyroscope != null) {
                deviceDataBuilder
                        .setGyrX(gyroscope.getX())
                        .setGyrY(gyroscope.getY())
                        .setGyrZ(gyroscope.getZ());
            }

            Pedometer pedometer = deviceSensorData.getPedometer();
            if (pedometer != null) {
                deviceDataBuilder.setStepCount(pedometer.getStepCount());
            }

            HeartRateMonitor heartRateMonitor = deviceSensorData.getHeartRateMonitor();
            if (heartRateMonitor != null) {
                deviceDataBuilder.setHeartRate(heartRateMonitor.getHeartRate());
            }

            SensorDataProtos.DeviceData deviceData = deviceDataBuilder.build();

            if (!deviceDataHashMap.containsKey(nodeId)) {
                deviceDataHashMap.put(nodeId, new ArrayList<>());
            }
            deviceDataHashMap.get(nodeId).add(deviceData);
        }

        if (!deviceDataHashMap.isEmpty()) {
            for (String node : deviceDataHashMap.keySet()) {
                persistenceManager.saveDeviceData(deviceDataHashMap.get(node), node, dbManager.getDeviceSensorDataTimestamp(node));
            }
            dbManager.deleteSensorDataBefore(endTime);
        }

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
