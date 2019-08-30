package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
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
        // Do the work here--in this case, upload the images.
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 15 * 60 * 1000;
        DatabaseManager dbManager = DatabaseManager.getInstance();
        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        List<SensorDataProtos.MobileData> mobileDataList = new ArrayList<>();
        List<SensorDataProtos.DeviceData> deviceDataList = new ArrayList<>();

        for (long timestamp = startTime; timestamp < endTime; timestamp += 33) {
            Accelerometer accelerometer = dbManager.getAccelerometer(timestamp);
            Gyroscope gyroscope = dbManager.getGyroscope(timestamp);
            Pedometer pedometer = dbManager.getPedometer(timestamp);
            HeartRateMonitor heartRateMonitor = dbManager.getHeartRateMonitor(timestamp);
            GPSData gpsData = dbManager.getGPSData(timestamp);
            SensorDataProtos.MobileData mobileData = SensorDataProtos.MobileData.newBuilder()
                    .setGpsAltitude((float) gpsData.getAltitude())
                    .setGpsLongitude((float) gpsData.getLongitude())
                    .setGpsLatitude((float) gpsData.getLatitude())
                    .setHeartRate(heartRateMonitor.getHeartRate())
                    .setStepCount(pedometer.getStepCount())
                    .setTimestamp(timestamp)
                    .build();
            mobileDataList.add(mobileData);
            SensorDataProtos.DeviceData deviceData = SensorDataProtos.DeviceData.newBuilder()
                    .setAccX(accelerometer.getX())
                    .setAccY(accelerometer.getY())
                    .setAccZ(accelerometer.getZ())
                    .setGyrX(gyroscope.getX())
                    .setGyrY(gyroscope.getY())
                    .setGyrZ(gyroscope.getZ())
                    .build();
            deviceDataList.add(deviceData);
        }
        persistenceManager.saveDeviceData(deviceDataList);
        persistenceManager.saveMobileData(mobileDataList);
        dbManager.deleteDataBefore(endTime);

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
