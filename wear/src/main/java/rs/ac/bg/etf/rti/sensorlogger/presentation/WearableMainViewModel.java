package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableBoolean;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Wearable;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.services.WearableDataLayerListenerService;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class WearableMainViewModel extends BaseObservable implements CapabilityClient.OnCapabilityChangedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = WearableMainViewModel.class.getSimpleName();
    private static final String SERVER_APP_CAPABILITY = "sensor_app_server";
    private static final String NODE_ID_KEY = "nodeId";

    private final Context context;
    public ObservableBoolean listening = new ObservableBoolean();

    WearableMainViewModel(Context context) {
        this.context = context;

        getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);

        listening.set(getDefaultSharedPreferences(context).getBoolean(WearableDataLayerListenerService.IS_LISTENING_KEY, false));

        Wearable.getNodeClient(context).getLocalNode()
                .addOnSuccessListener(node -> getDefaultSharedPreferences(context).edit().putString(NODE_ID_KEY, node.getId()).apply());

    }

    void startCapabilityListener() {
        Wearable.getCapabilityClient(context)
                .addListener(this, SERVER_APP_CAPABILITY);
    }

    void stopCapabilityListener() {
        Wearable.getCapabilityClient(context).removeListener(this);
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(WearableDataLayerListenerService.IS_LISTENING_KEY)) {
            listening.set(sharedPreferences.getBoolean(key, false));
        }
    }

    @Bindable
    public String getNodeId() {
        return "Node Id: " + getDefaultSharedPreferences(context).getString(NODE_ID_KEY, context.getString(R.string.unknown));
    }
}
