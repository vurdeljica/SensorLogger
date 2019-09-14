package rs.ac.bg.etf.rti.sensorlogger.presentation.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.wearable.Wearable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import rs.ac.bg.etf.rti.sensorlogger.BuildConfig;
import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.Utils;
import rs.ac.bg.etf.rti.sensorlogger.presentation.devices.DevicesFragment;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeFragment;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel;
import rs.ac.bg.etf.rti.sensorlogger.presentation.journal.JournalFragment;
import rs.ac.bg.etf.rti.sensorlogger.services.ApplicationSensorBackgroundService;
import rs.ac.bg.etf.rti.sensorlogger.services.LocationListenerService;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";
    private static final String NODE_ID_KEY = "nodeId";

    private MainViewModel viewModel;

    Fragment selectedFragment = null;

    // Used for saving the listening state
    private boolean isListening;

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // A reference to the service used to get location updates.
    private LocationListenerService mLocationService = null;

    // A reference to the service used to get sensor updates.
    private ApplicationSensorBackgroundService mSensorService = null;

    // Tracks the bound state of the location service.
    private boolean mLocationServiceBound = false;

    // Tracks the bound state of the sensor service.
    private boolean mSensorServiceBound = false;

    // Monitors the state of the connection to the location service.
    private final ServiceConnection mLocationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationListenerService.LocalBinder binder = (LocationListenerService.LocalBinder) service;
            mLocationService = binder.getService();
            mLocationServiceBound = true;
            if (isListening) {
                mLocationService.requestLocationUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationService = null;
            mLocationServiceBound = false;
        }
    };

    // Monitors the state of the connection to the sensor service.
    private final ServiceConnection mSensorServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ApplicationSensorBackgroundService.LocalBinder binder = (ApplicationSensorBackgroundService.LocalBinder) service;
            mSensorService = binder.getService();
            mSensorServiceBound = true;
            if (isListening) {
                mSensorService.requestSensorEventUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSensorService = null;
            mSensorServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        viewModel = new MainViewModel(getApplicationContext());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        viewModel.startStoreWorker();

        isListening = getDefaultSharedPreferences(getApplicationContext()).getBoolean(HomeViewModel.IS_LISTENING_KEY, false);

        Wearable.getNodeClient(this).getLocalNode()
                .addOnSuccessListener(node -> getDefaultSharedPreferences(this).edit().putString(NODE_ID_KEY, node.getId()).apply());
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationListenerService.class), mLocationServiceConnection,
                Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, ApplicationSensorBackgroundService.class), mSensorServiceConnection,
                Context.BIND_AUTO_CREATE);

        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (selectedFragment != null && selectedFragment instanceof JournalFragment) {
            ((JournalFragment) selectedFragment).updateFragment();
        }

        viewModel.startCapabilityListener();

        viewModel.startWearableActivity();
    }

    @Override
    protected void onPause() {
        viewModel.stopCapabilityListener();

        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mLocationServiceBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mLocationServiceConnection);
            mLocationServiceBound = false;
        }
        if (mSensorServiceBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mSensorServiceConnection);
            mSensorServiceBound = false;
        }
        getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        // Request permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[1] == PackageManager.PERMISSION_GRANTED && isListening) {
                // Permission was granted.
                mLocationService.requestLocationUpdates();
                mSensorService.requestSensorEventUpdates();
            } else {
                // Permission denied.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, view -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        })
                        .show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {

        }
        if (s.equals(HomeViewModel.IS_LISTENING_KEY)) {
            if (sharedPreferences.getBoolean(s, true)) {
                isListening = true;
                mLocationService.requestLocationUpdates();
                mSensorService.requestSensorEventUpdates();
            } else {
                isListening = false;
                if (mLocationService != null) {
                    mLocationService.removeLocationUpdates();
                }
                if (mSensorService != null) {
                    mSensorService.removeSensorEventUpdates();
                }
            }
            viewModel.setWearableShouldStartListeners(isListening);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_logs:
                        selectedFragment = new DevicesFragment();
                        break;
                    case R.id.nav_journal:
                        selectedFragment = new JournalFragment();
                        break;
                }

                MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();

                return true;
            };

}

