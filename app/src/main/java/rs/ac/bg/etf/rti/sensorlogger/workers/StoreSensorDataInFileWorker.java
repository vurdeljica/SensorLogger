package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
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
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel;

public class StoreSensorDataInFileWorker extends Worker {

    private Context context;
    private String nodeId;

    public StoreSensorDataInFileWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.nodeId = getInputData().getString("nodeId");
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(HomeViewModel.WORK_TAG, "Store sensor worker started. NodeId: " + nodeId);
        long endTime = System.currentTimeMillis();
        DatabaseManager dbManager = DatabaseManager.getInstance();
        PersistenceManager persistenceManager = PersistenceManager.getInstance();

        List<SensorDataProtos.SensorData> sensorDataToStore = new ArrayList<>();

        for (DeviceSensorData deviceSensorData : dbManager.getDeviceSensorData(nodeId, endTime)) {
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
            sensorDataToStore.add(sensorData);
        }

        dbManager.deleteSpecificSensorDataBefore(nodeId, endTime);

        persistenceManager.saveSensorData(sensorDataToStore, nodeId, dbManager.getDeviceSensorDataTimestamp(nodeId));


        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
