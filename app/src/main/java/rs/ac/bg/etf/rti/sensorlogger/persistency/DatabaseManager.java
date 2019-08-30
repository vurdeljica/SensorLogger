package rs.ac.bg.etf.rti.sensorlogger.persistency;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rs.ac.bg.etf.rti.sensorlogger.model.Accelerometer;
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;
import rs.ac.bg.etf.rti.sensorlogger.model.GPSData;
import rs.ac.bg.etf.rti.sensorlogger.model.Gyroscope;
import rs.ac.bg.etf.rti.sensorlogger.model.HeartRateMonitor;
import rs.ac.bg.etf.rti.sensorlogger.model.Pedometer;

public class DatabaseManager {
    private static final String TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager instance;

    private DatabaseManager() {

    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }

        return instance;
    }

    public void init(Context context) {
        Realm.init(context);
        //RealmConfiguration config = new RealmConfiguration.Builder().name("sensor_logger.realm").build();
        //Realm.setDefaultConfiguration(config);
    }

    public void insertOrUpdateGPSData(GPSData _gpsData) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final GPSData gpsData = _gpsData;

            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(gpsData));
        }
    }

    public void insertOrUpdateHeartRateMonitor(HeartRateMonitor _heartRateMonitor) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final HeartRateMonitor heartRateMonitor = _heartRateMonitor;

            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(heartRateMonitor));
        }
    }

    public void insertOrUpdateAccelerometer(Accelerometer _accelerometer) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final Accelerometer accelerometer = _accelerometer;

            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(accelerometer));
        }
    }

    public void insertOrUpdateGyroscope(Gyroscope _gyroscope) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final Gyroscope gyroscope = _gyroscope;

            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(gyroscope));
        }
    }

    public void insertOrUpdatePedometer(Pedometer _pedometer) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final Pedometer pedometer = _pedometer;

            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(pedometer));
        }
    }

    public void insertOrUpdateDailyActivity(DailyActivity _dailyActivity) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final DailyActivity dailyActivity = _dailyActivity;
            boolean isNewActivity = dailyActivity.getId() == -1;

            if (isNewActivity) {
                Number currentIdNum = realm.where(DailyActivity.class).max("id");
                int nextId;
                if (currentIdNum == null) {
                    nextId = 1;
                } else {
                    nextId = currentIdNum.intValue() + 1;
                }
                dailyActivity.setId(nextId);
            }

            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(dailyActivity));

        }
    }

    public void deleteDailyActivity(long dailyActivityId) {

        try (Realm realm = Realm.getDefaultInstance()) {
            final long id = dailyActivityId;
            realm.executeTransaction(realm1 -> {
                RealmResults<DailyActivity> result = realm1.where(DailyActivity.class).equalTo("id", id).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    public DailyActivity getDailyActivity(long dailyActivityId) {
        DailyActivity dailyActivity;

        try (Realm realm = Realm.getDefaultInstance()) {
            DailyActivity dailyActivityFromRealm = realm.where(DailyActivity.class).equalTo("id", dailyActivityId).findFirst();
            dailyActivity = realm.copyFromRealm(dailyActivityFromRealm);
        }

        return dailyActivity;
    }

    @NonNull
    public Gyroscope getGyroscope(long timestamp) {
        Gyroscope gyroscope = new Gyroscope();

        try (Realm realm = Realm.getDefaultInstance()) {
            Gyroscope gyroscopeFromRealm = realm.where(Gyroscope.class).lessThanOrEqualTo("timestamp", timestamp).findFirst();
            if (gyroscopeFromRealm != null) {
                gyroscope = realm.copyFromRealm(gyroscopeFromRealm);
            }
        }

        return gyroscope;
    }

    @NonNull
    public Accelerometer getAccelerometer(long timestamp) {
        Accelerometer accelerometer = new Accelerometer();

        try (Realm realm = Realm.getDefaultInstance()) {
            Accelerometer accelerometerFromRealm = realm.where(Accelerometer.class).lessThanOrEqualTo("timestamp", timestamp).findFirst();
            if (accelerometerFromRealm != null) {
                accelerometer = realm.copyFromRealm(accelerometerFromRealm);
            }
        }

        return accelerometer;
    }

    @NonNull
    public HeartRateMonitor getHeartRateMonitor(long timestamp) {
        HeartRateMonitor heartRateMonitor = new HeartRateMonitor();

        try (Realm realm = Realm.getDefaultInstance()) {
            HeartRateMonitor heartRateMonitorFromRealm = realm.where(HeartRateMonitor.class).lessThanOrEqualTo("timestamp", timestamp).findFirst();
            if (heartRateMonitorFromRealm != null) {
                heartRateMonitor = realm.copyFromRealm(heartRateMonitorFromRealm);
            }
        }

        return heartRateMonitor;
    }

    @NonNull
    public Pedometer getPedometer(long timestamp) {
        Pedometer pedometer = new Pedometer();

        try (Realm realm = Realm.getDefaultInstance()) {
            Pedometer pedometerFromRealm = realm.where(Pedometer.class).lessThanOrEqualTo("timestamp", timestamp).findFirst();
            if (pedometerFromRealm != null) {
                pedometer = realm.copyFromRealm(pedometerFromRealm);
            }
        }

        return pedometer;
    }

    @NonNull
    public GPSData getGPSData(long timestamp) {
        GPSData gpsData = new GPSData();

        try (Realm realm = Realm.getDefaultInstance()) {
            GPSData gpsDataFromRealm = realm.where(GPSData.class).lessThanOrEqualTo("timestamp", timestamp).findFirst();
            if (gpsDataFromRealm != null) {
                gpsData = realm.copyFromRealm(gpsDataFromRealm);
            }
        }

        return gpsData;
    }

    public List<DailyActivity> getDailyActivities() {
        List<DailyActivity> dailyActivities = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<DailyActivity> results = realm.where(DailyActivity.class).sort("date", Sort.DESCENDING).findAll();
            dailyActivities.addAll(realm.copyFromRealm(results));
        }

        return dailyActivities;
    }

    public void deleteDataBefore(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<Gyroscope> result1 = realm1.where(Gyroscope.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result1.deleteAllFromRealm();
                RealmResults<Accelerometer> result2 = realm1.where(Accelerometer.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result2.deleteAllFromRealm();
                RealmResults<Pedometer> result3 = realm1.where(Pedometer.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result3.deleteAllFromRealm();
                RealmResults<HeartRateMonitor> result4 = realm1.where(HeartRateMonitor.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result4.deleteAllFromRealm();
                RealmResults<GPSData> result5 = realm1.where(GPSData.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result5.deleteAllFromRealm();
            });
        }
    }

    public void saveToJson(File jsonFile) {
        Gson gson = new GsonBuilder().create(); //... obtain your Gson;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<DailyActivity> results = realm.where(DailyActivity.class).findAll();
        List<DailyActivity> dailyActivities = new ArrayList<>(realm.copyFromRealm(results));

        try {
            FileWriter writer = new FileWriter(jsonFile);
            writer.append("{\"activities\":[");
            for (int i = 0; i < dailyActivities.size(); i++) {
                DailyActivity dailyActivity = dailyActivities.get(i);

                String json = gson.toJson(dailyActivity);
                writer.append(json);
                if (i != dailyActivities.size() - 1) {
                    writer.append(",");
                }
            }
            writer.append("]}");
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Log.e(TAG, "saveToJson: error while making JSON");
        }
    }

}
