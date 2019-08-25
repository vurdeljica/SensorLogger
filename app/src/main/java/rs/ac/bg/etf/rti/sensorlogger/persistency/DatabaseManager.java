package rs.ac.bg.etf.rti.sensorlogger.persistency;

import android.content.Context;

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
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;

public class DatabaseManager {

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

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm1) {
                    realm1.insertOrUpdate(dailyActivity);
                }
            });

        }
    }

    public void deleteDailyActivity(long dailyActivityId) {

        try (Realm realm = Realm.getDefaultInstance()) {
            final long id = dailyActivityId;
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm1) {
                    RealmResults<DailyActivity> result = realm1.where(DailyActivity.class).equalTo("id", id).findAll();
                    result.deleteAllFromRealm();
                }
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

    public List<DailyActivity> getDailyActivities() {
        List<DailyActivity> dailyActivities = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<DailyActivity> results = realm.where(DailyActivity.class).sort("date", Sort.DESCENDING).findAll();
            dailyActivities.addAll(realm.copyFromRealm(results));
        }

        return dailyActivities;
    }

    public void saveToJson(File jsonFile) {
        List<DailyActivity> dailyActivities = new ArrayList<>();
        Gson gson = new GsonBuilder().create(); //... obtain your Gson;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<DailyActivity> results = realm.where(DailyActivity.class).findAll();
        dailyActivities.addAll(realm.copyFromRealm(results));

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
            for (DailyActivity dailyActivity : dailyActivities) {

            }
            writer.append("]}");
            writer.flush();
            writer.close();
        }
        catch(IOException ex) {

        }
    }

}
