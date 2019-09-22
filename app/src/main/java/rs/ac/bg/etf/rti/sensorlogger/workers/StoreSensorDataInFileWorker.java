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

        persistenceManager.saveSensorData(sensorDataToStore, nodeId, dbManager.getDeviceSensorDataTimestamp(nodeId));

        dbManager.deleteSpecificSensorDataBefore(nodeId, endTime);

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
