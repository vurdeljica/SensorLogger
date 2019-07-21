package rs.ac.bg.etf.rti.sensorlogger.database;

import android.content.Context;

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

    public List<DailyActivity> getDailyActivities() {
        List<DailyActivity> dailyActivities = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<DailyActivity> results = realm.where(DailyActivity.class).sort("date", Sort.DESCENDING).findAll();
            dailyActivities.addAll(realm.copyFromRealm(results));
        }

        return dailyActivities;
    }

}
