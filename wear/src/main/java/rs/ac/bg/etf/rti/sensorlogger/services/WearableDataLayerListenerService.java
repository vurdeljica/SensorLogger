package rs.ac.bg.etf.rti.sensorlogger.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import rs.ac.bg.etf.rti.sensorlogger.presentation.WearableMainActivity;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class WearableDataLayerListenerService extends WearableListenerService {
    private static final String TAG = "DataLayerService";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String SHOULD_START_LISTENING_PATH = "/should-start-listening";

    public static final String IS_LISTENING_KEY = "isListening";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, WearableMainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(startIntent);
        } else if (messageEvent.getPath().equals(SHOULD_START_LISTENING_PATH)) {
            getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(IS_LISTENING_KEY, messageEvent.getData()[0] != 0).apply();
            Intent startIntent = new Intent(this, WearableMainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(startIntent);
        }
    }
}