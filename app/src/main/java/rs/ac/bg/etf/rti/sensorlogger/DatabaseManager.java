package rs.ac.bg.etf.rti.sensorlogger;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;

public class DatabaseManager {

    public static DatabaseManager instance;

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
        Realm realm = Realm.getDefaultInstance();
        final DailyActivity dailyActivity = _dailyActivity;

        try {
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
                public void execute(Realm realm) {
                    realm.insertOrUpdate(dailyActivity);
                }
            });

        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void deleteDailyActivity(long dailyActivityId) {
        Realm realm = Realm.getDefaultInstance();
        final long id = dailyActivityId;

        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<DailyActivity> result = realm.where(DailyActivity.class).equalTo("id", id).findAll();
                    result.deleteAllFromRealm();
                }
            });
        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public DailyActivity getDailyActivity(long dailyActivityId) {
        Realm realm = Realm.getDefaultInstance();
        DailyActivity dailyActivity;

        try {
            DailyActivity dailyActivityFromRealm = realm.where(DailyActivity.class).equalTo("id", dailyActivityId).findFirst();
            dailyActivity = realm.copyFromRealm(dailyActivityFromRealm);
        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }

        return dailyActivity;
    }

    public List<DailyActivity> getDailyActivities() {
        Realm realm = Realm.getDefaultInstance();
        List<DailyActivity> dailyActivities = new ArrayList<>();

        try {
            RealmResults<DailyActivity> results = realm.where(DailyActivity.class).sort("date", Sort.DESCENDING).findAll();
            dailyActivities.addAll(realm.copyFromRealm(results));
        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }

        return dailyActivities;
    }

}
