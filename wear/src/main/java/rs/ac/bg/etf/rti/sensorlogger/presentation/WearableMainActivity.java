package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;

import androidx.databinding.DataBindingUtil;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.ActivityMainBinding;
import rs.ac.bg.etf.rti.sensorlogger.services.WearableDataLayerListenerService;
import rs.ac.bg.etf.rti.sensorlogger.services.WearableSensorBackgroundService;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class WearableMainActivity extends WearableActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private WearableMainViewModel viewModel;

    // Used for saving the listening state
    private boolean listening;

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
            if (listening) {
                mService.requestSensorEventUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new WearableMainViewModel(getApplicationContext());
        binding.setVm(viewModel);

        listening = getDefaultSharedPreferences(this).getBoolean(WearableDataLayerListenerService.IS_LISTENING_KEY, false);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, WearableSensorBackgroundService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);

        getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.startCapabilityListener();
    }

    @Override
    protected void onPause() {
        viewModel.stopCapabilityListener();

        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }

        getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);

        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(WearableDataLayerListenerService.IS_LISTENING_KEY)) {
            listening = sharedPreferences.getBoolean(key, false);
            if (listening) {
                mService.requestSensorEventUpdates();
            } else {
                if (mService != null) {
                    mService.removeSensorEventUpdates();
                }
            }
        }
    }
}
