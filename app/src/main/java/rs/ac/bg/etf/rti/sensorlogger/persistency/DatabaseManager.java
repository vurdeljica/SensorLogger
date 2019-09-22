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
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;
import rs.ac.bg.etf.rti.sensorlogger.model.DeviceSensorData;
import rs.ac.bg.etf.rti.sensorlogger.model.GPSData;

public class DatabaseManager {
    private static final String TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager instance;

    public static List<DeviceSensorData> deviceSensorDataBuffer = new ArrayList<>();

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
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
//        try (Realm realm = Realm.getDefaultInstance()) {
//            realm.executeTransaction(realm1 -> realm1.deleteAll());
//        }
    }

    public void insertOrUpdateGPSData(GPSData _gpsData) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final GPSData gpsData = _gpsData;

            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(gpsData));
        }
    }

    public void insertOrUpdateDeviceSensorData(List<DeviceSensorData> deviceSensorDataList) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(deviceSensorDataList));
        }
    }

    @Nullable
    public DeviceSensorData getLatestDeviceSensorData(String nodeId) {
        DeviceSensorData deviceSensorData = null;

        try (Realm realm = Realm.getDefaultInstance()) {
            DeviceSensorData deviceSensorDataFromRealm = realm.where(DeviceSensorData.class).equalTo("nodeId", nodeId).sort("timestamp", Sort.DESCENDING).findFirst();
            if (deviceSensorDataFromRealm != null) {
                deviceSensorData = realm.copyFromRealm(deviceSensorDataFromRealm);
            }
        }

        return deviceSensorData;
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

    @NonNull
    public List<DeviceSensorData> getDeviceSensorData() {
        List<DeviceSensorData> deviceSensorData = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {
            List<DeviceSensorData> DeviceSensorDataFromRealm = realm.where(DeviceSensorData.class).findAll();
            if (DeviceSensorDataFromRealm != null) {
                deviceSensorData.addAll(realm.copyFromRealm(DeviceSensorDataFromRealm));
            }
        }

        return deviceSensorData;
    }

    public long getGPSTimestamp() {
        try (Realm realm = Realm.getDefaultInstance()) {
            Number timestamp = realm.where(GPSData.class).min("timestamp");
            return timestamp != null ? timestamp.longValue() : 0;
        }
    }

    public long getDeviceSensorDataTimestamp(String nodeId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Number timestamp = realm.where(DeviceSensorData.class).equalTo("nodeId", nodeId).min("timestamp");
            return timestamp != null ? timestamp.longValue() : 0;
        }
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
                RealmResults<GPSData> result = realm1.where(GPSData.class).lessThan("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    public void deleteSensorDataBefore(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<DeviceSensorData> result = realm1.where(DeviceSensorData.class).lessThan("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    public void deleteSpecificSensorDataBefore(String nodeId, long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<DeviceSensorData> result = realm1.where(DeviceSensorData.class).equalTo("nodeId", nodeId).lessThan("timestamp", timestamp).findAll();
                Log.d("REALM", "DELETED: " + result.size() + " . NodeId: " + nodeId);
                result.deleteAllFromRealm();
            });
        }
    }

    public List<String> getNodeIds() {
        List<String> nodeIds = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {
            List<DeviceSensorData> deviceSensorData = realm.where(DeviceSensorData.class).distinct("nodeId").findAll();
            for (DeviceSensorData sensorData : deviceSensorData) {
                nodeIds.add(sensorData.getNodeId());
            }
        }

        return nodeIds;
    }

    @NonNull
    public List<DeviceSensorData> getDeviceSensorData(String nodeId, long timestamp) {
        List<DeviceSensorData> deviceSensorData = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {
            List<DeviceSensorData> DeviceSensorDataFromRealm = realm.where(DeviceSensorData.class).equalTo("nodeId", nodeId).lessThan("timestamp", timestamp).findAll();
            if (DeviceSensorDataFromRealm != null) {
                deviceSensorData.addAll(realm.copyFromRealm(DeviceSensorDataFromRealm));
                Log.d("REALM", "GET: " + deviceSensorData.size() + " NODE ID: " + nodeId);
            }
        }

        return deviceSensorData;
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
