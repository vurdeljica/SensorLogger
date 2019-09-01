package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        long timestamp = endTime;
        DatabaseManager dbManager = DatabaseManager.getInstance();
        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        List<SensorDataProtos.MobileData> mobileDataList = new ArrayList<>();
        HashMap<String, List<SensorDataProtos.DeviceData>> deviceDataHashMap = new HashMap<>();

        while (true) {
            GPSData gpsData = dbManager.getGPSData(timestamp);
            List<Accelerometer> accelerometerList = dbManager.getAccelerometer(timestamp);
            List<Gyroscope> gyroscopeList = dbManager.getGyroscope(timestamp);
            List<Pedometer> pedometerList = dbManager.getPedometer(timestamp);
            List<HeartRateMonitor> heartRateMonitorList = dbManager.getHeartRateMonitor(timestamp);

            if (gpsData == null && accelerometerList.isEmpty() && gyroscopeList.isEmpty() && pedometerList.isEmpty() && heartRateMonitorList.isEmpty()) {
                for (String nodeId: deviceDataHashMap.keySet()) {
                    persistenceManager.saveDeviceData(deviceDataHashMap.get(nodeId), nodeId, timestamp + 33);
                }
                persistenceManager.saveMobileData(mobileDataList, timestamp + 33);
                break;
            }

            if (gpsData != null) {
                SensorDataProtos.MobileData mobileData = SensorDataProtos.MobileData.newBuilder()
                        .setGpsAltitude((float) gpsData.getAltitude())
                        .setGpsLongitude((float) gpsData.getLongitude())
                        .setGpsLatitude((float) gpsData.getLatitude())
                        .setTimestamp(timestamp)
                        .build();
                mobileDataList.add(mobileData);
            }

            if (!accelerometerList.isEmpty() || !gyroscopeList.isEmpty() || !pedometerList.isEmpty() || !heartRateMonitorList.isEmpty()) {

                Set<String> nodeIds = new HashSet<>();
                nodeIds.addAll(Stream.of(accelerometerList).map(Accelerometer::getNodeId).toList());
                nodeIds.addAll(Stream.of(gyroscopeList).map(Gyroscope::getNodeId).toList());
                nodeIds.addAll(Stream.of(pedometerList).map(Pedometer::getNodeId).toList());
                nodeIds.addAll(Stream.of(heartRateMonitorList).map(HeartRateMonitor::getNodeId).toList());

                for (String nodeId : nodeIds) {
                    Accelerometer accelerometer = Stream.of(accelerometerList).filter(value -> value.getNodeId().equals(nodeId)).single();
                    Gyroscope gyroscope = Stream.of(gyroscopeList).filter(value -> value.getNodeId().equals(nodeId)).single();
                    Pedometer pedometer = Stream.of(pedometerList).filter(value -> value.getNodeId().equals(nodeId)).single();
                    HeartRateMonitor heartRateMonitor = Stream.of(heartRateMonitorList).filter(value -> value.getNodeId().equals(nodeId)).single();
                    SensorDataProtos.DeviceData deviceData = SensorDataProtos.DeviceData.newBuilder()
                            .setAccX(accelerometer.getX())
                            .setAccY(accelerometer.getY())
                            .setAccZ(accelerometer.getZ())
                            .setGyrX(gyroscope.getX())
                            .setGyrY(gyroscope.getY())
                            .setGyrZ(gyroscope.getZ())
                            .setHeartRate(heartRateMonitor.getHeartRate())
                            .setStepCount(pedometer.getStepCount())
                            .setTimestamp(timestamp)
                            .build();
                    if (!deviceDataHashMap.containsKey(nodeId)) {
                        deviceDataHashMap.put(nodeId, new ArrayList<>());
                    }
                    deviceDataHashMap.get(nodeId).add(deviceData);
                }
            }

            timestamp -= 33;
        }
        dbManager.deleteDataBefore(endTime);

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
