package rs.ac.bg.etf.rti.sensorlogger.persistency;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Nullable
    public String getLatestSensorDataNodeId() {
        String nodeId = null;

        try (Realm realm = Realm.getDefaultInstance()) {
            Accelerometer accelerometer = realm.where(Accelerometer.class).sort("timestamp", Sort.DESCENDING).findFirst();
            Gyroscope gyroscope = realm.where(Gyroscope.class).sort("timestamp", Sort.DESCENDING).findFirst();
            HeartRateMonitor heartRateMonitor = realm.where(HeartRateMonitor.class).sort("timestamp", Sort.DESCENDING).findFirst();
            Pedometer pedometer = realm.where(Pedometer.class).sort("timestamp", Sort.DESCENDING).findFirst();

            long maxTimestamp = 0;

            if (accelerometer != null) {
                nodeId = accelerometer.getNodeId();
                maxTimestamp = accelerometer.getTimestamp();
            }
            if (gyroscope != null && gyroscope.getTimestamp() > maxTimestamp) {
                nodeId = gyroscope.getNodeId();
                maxTimestamp = gyroscope.getTimestamp();
            }
            if (heartRateMonitor != null && heartRateMonitor.getTimestamp() > maxTimestamp) {
                nodeId = heartRateMonitor.getNodeId();
                maxTimestamp = heartRateMonitor.getTimestamp();
            }
            if (pedometer != null && pedometer.getTimestamp() > maxTimestamp) {
                nodeId = pedometer.getNodeId();
            }
        }

        return nodeId;
    }

    @Nullable
    public Gyroscope getLatestGyroscope(String nodeId) {
        Gyroscope gyroscope = null;

        try (Realm realm = Realm.getDefaultInstance()) {
            Gyroscope gyroscopeFromRealm = realm.where(Gyroscope.class).equalTo("nodeId", nodeId).sort("timestamp", Sort.DESCENDING).findFirst();
            if (gyroscopeFromRealm != null) {
                gyroscope = realm.copyFromRealm(gyroscopeFromRealm);
            }
        }

        return gyroscope;
    }

    public void deleteGyroscope(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<Gyroscope> result = realm1.where(Gyroscope.class).equalTo("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    @Nullable
    public Accelerometer getLatestAccelerometer(String nodeId) {
        Accelerometer accelerometer = null;

        try (Realm realm = Realm.getDefaultInstance()) {
            Accelerometer accelerometerFromRealm = realm.where(Accelerometer.class).equalTo("nodeId", nodeId).sort("timestamp", Sort.DESCENDING).findFirst();
            if (accelerometerFromRealm != null) {
                accelerometer = realm.copyFromRealm(accelerometerFromRealm);
            }
        }

        return accelerometer;
    }

    public void deleteAccelerometer(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<Accelerometer> result = realm1.where(Accelerometer.class).equalTo("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    @Nullable
    public HeartRateMonitor getLatestHeartRateMonitor(String nodeId) {
        HeartRateMonitor heartRateMonitor = null;

        try (Realm realm = Realm.getDefaultInstance()) {
            HeartRateMonitor heartRateMonitorFromRealm = realm.where(HeartRateMonitor.class).equalTo("nodeId", nodeId).sort("timestamp", Sort.DESCENDING).findFirst();
            if (heartRateMonitorFromRealm != null) {
                heartRateMonitor = realm.copyFromRealm(heartRateMonitorFromRealm);
            }
        }

        return heartRateMonitor;
    }

    public void deleteHeartRateMonitor(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<HeartRateMonitor> result = realm1.where(HeartRateMonitor.class).equalTo("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    @Nullable
    public Pedometer getLatestPedometer(String nodeId) {
        Pedometer pedometer = null;

        try (Realm realm = Realm.getDefaultInstance()) {
            Pedometer pedometerFromRealm = realm.where(Pedometer.class).equalTo("nodeId", nodeId).sort("timestamp", Sort.DESCENDING).findFirst();
            if (pedometerFromRealm != null) {
                pedometer = realm.copyFromRealm(pedometerFromRealm);
            }
        }

        return pedometer;
    }

    public void deletePedometer(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<Pedometer> result = realm1.where(Pedometer.class).equalTo("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    @NonNull
    public List<GPSData> getGPSData() {
        List<GPSData> gpsData = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {
            List<GPSData> gpsDataFromRealm = realm.where(GPSData.class).findAll();
            if (gpsDataFromRealm != null) {
                gpsData.addAll(realm.copyFromRealm(gpsDataFromRealm));
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

    public void deleteGPSDataBefore(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<GPSData> result = realm1.where(GPSData.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    public void deleteSensorDataBefore(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<Accelerometer> result1 = realm1.where(Accelerometer.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result1.deleteAllFromRealm();
                RealmResults<HeartRateMonitor> result2 = realm1.where(HeartRateMonitor.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result2.deleteAllFromRealm();
                RealmResults<Gyroscope> result3 = realm1.where(Gyroscope.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result3.deleteAllFromRealm();
                RealmResults<Pedometer> result4 = realm1.where(Pedometer.class).lessThanOrEqualTo("timestamp", timestamp).findAll();
                result4.deleteAllFromRealm();
            });
        }
    }

    void saveToJson(File jsonFile) {
        Gson gson = new GsonBuilder().create();//... obtain your Gson;
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<DailyActivity> results = realm.where(DailyActivity.class).findAll();
            List<DailyActivity> dailyActivities = new ArrayList<>(realm.copyFromRealm(results));

            try (FileWriter writer = new FileWriter(jsonFile)) {
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
            } catch (IOException ex) {
                Log.e(TAG, "saveToJson: error while making JSON");
            }
        }
    }

}
