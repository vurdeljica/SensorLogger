package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.SensorDataProtos;
import rs.ac.bg.etf.rti.sensorlogger.model.DeviceSensorData;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.persistency.PersistenceManager;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel;

/**
 * Worker that stores collected sensor data in local memory of the phone, compresses it
 * and removes the saved objects from the database
 */
public class StoreSensorDataInFileWorker extends Worker {

    private String nodeId;

    public StoreSensorDataInFileWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
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

        while (true) {
            List<DeviceSensorData> dataList = dbManager.getDeviceSensorData(nodeId, endTime);
            for (DeviceSensorData deviceSensorData : dataList) {
                SensorDataProtos.SensorData.Builder sensorDataBuilder = SensorDataProtos.SensorData.newBuilder()
                        .setTimestamp(deviceSensorData.getTimestamp())
                        .setNodeId(deviceSensorData.getNodeId());

                sensorDataBuilder
                        .setAccX(deviceSensorData.getAccX())
                        .setAccY(deviceSensorData.getAccY())
                        .setAccZ(deviceSensorData.getAccZ());

                sensorDataBuilder
                        .setGyrX(deviceSensorData.getGyrX())
                        .setGyrY(deviceSensorData.getGyrY())
                        .setGyrZ(deviceSensorData.getGyrZ());

                sensorDataBuilder
                        .setMagX(deviceSensorData.getMagX())
                        .setMagY(deviceSensorData.getMagY())
                        .setMagZ(deviceSensorData.getMagZ());

                sensorDataBuilder.setStepCount(deviceSensorData.getStepCount());

                sensorDataBuilder.setHeartRate(deviceSensorData.getHeartRate());

                SensorDataProtos.SensorData sensorData = sensorDataBuilder.build();
                sensorDataToStore.add(sensorData);
            }

            if (sensorDataToStore.size() <= 1)
                return Result.success();

            long startTime = dataList.get(dataList.size() - 1).getTimestamp();
            persistenceManager.saveSensorData(sensorDataToStore, nodeId, startTime);

            dbManager.deleteSpecificSensorDataBefore(nodeId, endTime);

            sensorDataToStore.clear();
        }
    }
}
