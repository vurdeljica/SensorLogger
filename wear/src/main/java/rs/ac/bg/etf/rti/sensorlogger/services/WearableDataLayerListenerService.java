package rs.ac.bg.etf.rti.sensorlogger.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;

import rs.ac.bg.etf.rti.sensorlogger.presentation.WearableMainActivity;

/**
 * Service for listening to messages sent from the phone
 */
public class WearableDataLayerListenerService extends WearableListenerService {
    private static final String TAG = "DataLayerService";

    /**
     * Path of the message to start the activity and update the collection status
     */
    private static final String SHOULD_START_LISTENING_PATH = "/should-start-listening";

    /**
     * Path of the sampling rate message
     */
    private static final String SAMPLING_RATE_PATH = "/sampling-rate";

    /**
     * Key for storing the collection status in the shared preferences
     */
    public static final String IS_LISTENING_KEY = "isListening";
    public static final String SAMPLING_RATE_KEY = "samplingRate";
    public static final String SHARED_PREFERENCES_ID = "rs.ac.bg.etf.rti.sensorlogger.shared_preferences";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(SHOULD_START_LISTENING_PATH)) {
            getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE).edit().putBoolean(IS_LISTENING_KEY, messageEvent.getData()[0] != 0).apply();
            Intent startIntent = new Intent(this, WearableMainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(startIntent);
        } else if (messageEvent.getPath().equals(SAMPLING_RATE_PATH)) {
            byte[] data = messageEvent.getData();
            int num = fromByteArray(data);
            getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE).edit().putInt(SAMPLING_RATE_KEY, num).apply();
        }
    }

    int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }
}
