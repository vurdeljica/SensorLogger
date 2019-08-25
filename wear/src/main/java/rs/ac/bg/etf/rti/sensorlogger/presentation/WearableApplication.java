package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.app.Application;
import android.content.Context;

public class WearableApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
