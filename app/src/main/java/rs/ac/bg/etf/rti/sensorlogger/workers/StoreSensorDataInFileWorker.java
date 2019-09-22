package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private Context context;

    public StoreSensorDataInFileWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
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
            } else {
                sensorDataBuilder
                        .setAccX(0)
                        .setAccY(0)
                        .setAccZ(0);
            }

            Gyroscope gyroscope = deviceSensorData.getGyroscope();
            if (gyroscope != null) {
                sensorDataBuilder
                        .setGyrX(gyroscope.getX())
                        .setGyrY(gyroscope.getY())
                        .setGyrZ(gyroscope.getZ());
            } else {
                sensorDataBuilder
                        .setGyrX(0)
                        .setGyrY(0)
                        .setGyrZ(0);
            }

            Magnetometer magnetometer = deviceSensorData.getMagnetometer();
            if (magnetometer != null) {
                sensorDataBuilder
                        .setMagX(magnetometer.getX())
                        .setMagY(magnetometer.getY())
                        .setMagZ(magnetometer.getZ());
            } else {
                sensorDataBuilder
                        .setMagX(0)
                        .setMagY(0)
                        .setMagZ(0);
            }

            Pedometer pedometer = deviceSensorData.getPedometer();
            if (pedometer != null) {
                sensorDataBuilder.setStepCount(pedometer.getStepCount());
            } else {
                sensorDataBuilder.setStepCount(0);
            }

            HeartRateMonitor heartRateMonitor = deviceSensorData.getHeartRateMonitor();
            if (heartRateMonitor != null) {
                sensorDataBuilder.setHeartRate(heartRateMonitor.getHeartRate());
            } else {
                sensorDataBuilder.setHeartRate(0);
            }

            SensorDataProtos.SensorData sensorData = sensorDataBuilder.build();

            if (!sensorDataMap.containsKey(nodeId)) {
                sensorDataMap.put(nodeId, new ArrayList<>());
            }
            sensorDataMap.get(nodeId).add(sensorData);
        }

        List<Future<?>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        if (!sensorDataMap.isEmpty()) {
            for (String node : sensorDataMap.keySet()) {

                Callable<Boolean> callableTask = () -> {
                    persistenceManager.saveSensorData(sensorDataMap.get(node), node, dbManager.getDeviceSensorDataTimestamp(node));
                    return true;
                };

                Future<?> f = executor.submit(callableTask);
                futures.add(f);

            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            executor.shutdown();

            dbManager.deleteSensorDataBefore(endTime);
        }

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
