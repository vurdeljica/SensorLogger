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

/**
 * Singleton manager for working with the Realm database used in the application
 */
public class DatabaseManager {
    private static final String TAG = DatabaseManager.class.getSimpleName();

    //Singleton instance of the DatabaseManager
    private static DatabaseManager instance;

    //Buffer for storing the sensor data collected from wearables
    //Used for buffering the data to avoid multiple transactions
    public static List<DeviceSensorData> deviceSensorDataBuffer = new ArrayList<>();

    private DatabaseManager() {

    }

    /**
     * @return the shared instance of the DatabaseManager
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }

        return instance;
    }

    /**
     * Initialises the database
     * @param context context that is used for initialising the database
     */
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

    /**
     * Insert or updates the {@link GPSData} object in the database
     * @param _gpsData object to be inserted or updated
     */
    public void insertOrUpdateGPSData(GPSData _gpsData) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final GPSData gpsData = _gpsData;

            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(gpsData));
        }
    }

    /**
     * Insert or updates the {@link DeviceSensorData} objects in the database
     * @param deviceSensorDataList list of objects to be inserted or updated
     */
    public void insertOrUpdateDeviceSensorData(List<DeviceSensorData> deviceSensorDataList) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(deviceSensorDataList));
        }
    }

    /**
     * Returns the latest {@link DeviceSensorData} object collected from a device (node)
     * @param nodeId id of the device the data was collected from
     * @return the latest sensor data of the node
     */
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

    /**
     * Insert or updates the {@link DailyActivity} object in the database
     * @param _dailyActivity object to be inserted or updated
     */
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

    /**
     * Deletes the daily activity from the database
     * @param dailyActivityId daily activity to be deleted
     */
    public void deleteDailyActivity(long dailyActivityId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final long id = dailyActivityId;
            realm.executeTransaction(realm1 -> {
                RealmResults<DailyActivity> result = realm1.where(DailyActivity.class).equalTo("id", id).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    /**
     * @param dailyActivityId id of the daily activity to be returned
     * @return daily activity with the passed id
     */
    public DailyActivity getDailyActivity(long dailyActivityId) {
        DailyActivity dailyActivity;

        try (Realm realm = Realm.getDefaultInstance()) {
            DailyActivity dailyActivityFromRealm = realm.where(DailyActivity.class).equalTo("id", dailyActivityId).findFirst();
            dailyActivity = realm.copyFromRealm(dailyActivityFromRealm);
        }

        return dailyActivity;
    }

    /**
     * @return list of GPSData objects from the database
     */
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

    /**
     * @return the timestamp of the oldest GPSData entry in the database
     */
    public long getGPSTimestamp() {
        try (Realm realm = Realm.getDefaultInstance()) {
            Number timestamp = realm.where(GPSData.class).min("timestamp");
            return timestamp != null ? timestamp.longValue() : 0;
        }
    }

    /**
     * @param nodeId id of the device (node) the sensor data was collected from
     * @return the timestamp of the oldest {@link DeviceSensorData} entry in the database from the selected node
     */
    public long getDeviceSensorDataTimestamp(String nodeId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Number timestamp = realm.where(DeviceSensorData.class).equalTo("nodeId", nodeId).min("timestamp");
            return timestamp != null ? timestamp.longValue() : 0;
        }
    }

    /**
     * @return a list of daily activities from the database
     */
    public List<DailyActivity> getDailyActivities() {
        List<DailyActivity> dailyActivities = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<DailyActivity> results = realm.where(DailyActivity.class).sort("date", Sort.DESCENDING).findAll();
            dailyActivities.addAll(realm.copyFromRealm(results));
        }

        return dailyActivities;
    }

    /**
     * Deletes {@link GPSData} objects older than the passed timestamp
     * @param timestamp timestamp before which objects need to be deleted
     */
    public void deleteGPSDataBefore(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<GPSData> result = realm1.where(GPSData.class).lessThan("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    /**
     * Deletes {@link DeviceSensorData} objects older than the passed timestamp
     * @param timestamp timestamp before which objects need to be deleted
     */
    public void deleteSensorDataBefore(long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                RealmResults<DeviceSensorData> result = realm1.where(DeviceSensorData.class).lessThan("timestamp", timestamp).findAll();
                result.deleteAllFromRealm();
            });
        }
    }

    /**
     * Deletes {@link DeviceSensorData} objects older than the passed timestamp collected from the specified device
     * @param nodeId id of the device (node) the data was collected from
     * @param timestamp timestamp before which objects need to be deleted
     */
    public void deleteSpecificSensorDataBefore(String nodeId, long timestamp) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(realm1 -> {
                Number latest = realm1.where(DeviceSensorData.class).equalTo("nodeId", nodeId).lessThan("timestamp", timestamp).max("timestamp");
                if (latest == null) {
                    return;
                }
                RealmResults<DeviceSensorData> result = realm1.where(DeviceSensorData.class).equalTo("nodeId", nodeId).lessThan("timestamp", latest.longValue()).findAll();
                Log.d("REALM", "DELETED: " + result.size() + " . NodeId: " + nodeId);
                result.deleteAllFromRealm();
            });
        }
    }

    /**
     * @return list of ids of the devices (nodes) sensor data has been collected from
     */
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

    /**
     * @param nodeId id of the device (node) the data was collected from
     * @param timestamp timestamp before which objects need to be returned
     * @return list of {@link DeviceSensorData} objects older than the passed timestamp collected from the specified device
     */
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

    /**
     * Saves all daily activities from the database in a json file
     * @param jsonFile json file containing the daily activities
     */
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
