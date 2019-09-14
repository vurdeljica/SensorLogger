package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
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
import rs.ac.bg.etf.rti.sensorlogger.services.WearableSensorBackgroundService;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class WearableMainViewModel extends BaseObservable implements CapabilityClient.OnCapabilityChangedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = WearableMainViewModel.class.getSimpleName();
    private static final String SERVER_APP_CAPABILITY = "sensor_app_server";
    private static final String NODE_ID_KEY = "nodeId";

    private final Context context;
    private final ServiceHandler serviceHandler;

    // Used for saving the listening state
    public ObservableBoolean listening = new ObservableBoolean();

    // A reference to the service used to get location updates.
    private WearableSensorBackgroundService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WearableSensorBackgroundService.LocalBinder binder = (WearableSensorBackgroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (listening.get()) {
                mService.requestSensorEventUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    WearableMainViewModel(Context context, ServiceHandler serviceHandler) {
        this.context = context;
        this.serviceHandler = serviceHandler;

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

    void init() {
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        serviceHandler.bindSensorService(new Intent(context, WearableSensorBackgroundService.class), mServiceConnection);

        getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }

    void destroy() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            serviceHandler.unbindSensorService(mServiceConnection);
            mBound = false;
        }

        getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(WearableDataLayerListenerService.IS_LISTENING_KEY)) {
            listening.set(sharedPreferences.getBoolean(key, false));
            if (listening.get()) {
                mService.requestSensorEventUpdates();
            } else {
                if (mService != null) {
                    mService.removeSensorEventUpdates();
                }
            }
        }
    }

    @Bindable
    public String getNodeId() {
        return "Node Id: " + getDefaultSharedPreferences(context).getString(NODE_ID_KEY, context.getString(R.string.unknown));
    }
}
