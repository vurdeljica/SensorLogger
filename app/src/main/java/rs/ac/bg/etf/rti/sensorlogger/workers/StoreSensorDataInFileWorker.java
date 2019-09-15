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
import rs.ac.bg.etf.rti.sensorlogger.model.Gyroscope;
import rs.ac.bg.etf.rti.sensorlogger.model.HeartRateMonitor;
import rs.ac.bg.etf.rti.sensorlogger.model.Magnetometer;
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
        HashMap<String, List<SensorDataProtos.SensorData>> sensorDataMap = new HashMap<>();

        for (DeviceSensorData deviceSensorData : dbManager.getDeviceSensorData()) {
            String nodeId = deviceSensorData.getNodeId();

            SensorDataProtos.SensorData.Builder sensorDataBuilder = SensorDataProtos.SensorData.newBuilder()
                    .setTimestamp(deviceSensorData.getTimestamp())
                    .setNodeId(deviceSensorData.getNodeId());

            Accelerometer accelerometer = deviceSensorData.getAccelerometer();
            if (accelerometer != null) {
                sensorDataBuilder
                        .setAccX(accelerometer.getX())
                        .setAccY(accelerometer.getY())
                        .setAccZ(accelerometer.getZ());
            }

            Gyroscope gyroscope = deviceSensorData.getGyroscope();
            if (gyroscope != null) {
                sensorDataBuilder
                        .setGyrX(gyroscope.getX())
                        .setGyrY(gyroscope.getY())
                        .setGyrZ(gyroscope.getZ());
            }

            Magnetometer magnetometer = deviceSensorData.getMagnetometer();
            if (magnetometer != null) {
                sensorDataBuilder
                        .setMagX(magnetometer.getX())
                        .setMagY(magnetometer.getY())
                        .setMagZ(magnetometer.getZ());
            }

            Pedometer pedometer = deviceSensorData.getPedometer();
            if (pedometer != null) {
                sensorDataBuilder.setStepCount(pedometer.getStepCount());
            }

            HeartRateMonitor heartRateMonitor = deviceSensorData.getHeartRateMonitor();
            if (heartRateMonitor != null) {
                sensorDataBuilder.setHeartRate(heartRateMonitor.getHeartRate());
            }

            SensorDataProtos.SensorData sensorData = sensorDataBuilder.build();

            if (!sensorDataMap.containsKey(nodeId)) {
                sensorDataMap.put(nodeId, new ArrayList<>());
            }
            sensorDataMap.get(nodeId).add(sensorData);
        }

        if (!sensorDataMap.isEmpty()) {
            for (String node : sensorDataMap.keySet()) {
                persistenceManager.saveSensorData(sensorDataMap.get(node), node, dbManager.getDeviceSensorDataTimestamp(node));
            }
            dbManager.deleteSensorDataBefore(endTime);
        }

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
