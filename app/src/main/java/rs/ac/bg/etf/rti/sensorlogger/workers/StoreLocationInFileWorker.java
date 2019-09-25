package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.SensorDataProtos;
import rs.ac.bg.etf.rti.sensorlogger.model.GPSData;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.persistency.PersistenceManager;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel;

/**
 * Worker that stores collected location data in local memory of the phone, compresses it
 * and removes the saved objects from the database
 */
public class StoreLocationInFileWorker extends Worker {
    public StoreLocationInFileWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(HomeViewModel.WORK_TAG, "Store location worker started");
        long endTime = System.currentTimeMillis();
        DatabaseManager dbManager = DatabaseManager.getInstance();
        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        List<SensorDataProtos.LocationData> mobileDataList = new ArrayList<>();

        for (GPSData gpsData : dbManager.getGPSData()) {
            SensorDataProtos.LocationData locationData = SensorDataProtos.LocationData.newBuilder()
                    .setGpsAltitude((float) gpsData.getAltitude())
                    .setGpsLongitude((float) gpsData.getLongitude())
                    .setGpsLatitude((float) gpsData.getLatitude())
                    .setTimestamp(gpsData.getTimestamp())
                    .build();
            mobileDataList.add(locationData);
        }

        if (!mobileDataList.isEmpty()) {
            persistenceManager.saveLocationData(mobileDataList, dbManager.getGPSTimestamp());
            dbManager.deleteGPSDataBefore(endTime);
        }

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
