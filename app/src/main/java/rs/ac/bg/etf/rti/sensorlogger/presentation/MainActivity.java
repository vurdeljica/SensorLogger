package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import rs.ac.bg.etf.rti.sensorlogger.BuildConfig;
import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.Utils;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeFragment;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel;
import rs.ac.bg.etf.rti.sensorlogger.presentation.journal.JournalFragment;
import rs.ac.bg.etf.rti.sensorlogger.presentation.logs.LogsFragment;
import rs.ac.bg.etf.rti.sensorlogger.services.LocationListenerService;
import rs.ac.bg.etf.rti.sensorlogger.workers.StoreFileWorker;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements CapabilityClient.OnCapabilityChangedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";
    private static final String CLIENT_APP_CAPABILITY = "sensor_app_client";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String SHOULD_START_LISTENING_PATH = "/should-start-listening";

    Fragment selectedFragment = null;

    // Used for saving the listening state
    private boolean isListening;

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // A reference to the service used to get location updates.
    private LocationListenerService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationListenerService.LocalBinder binder = (LocationListenerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (isListening) {
                mService.requestLocationUpdates();
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

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

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

        Constraints constraints = new Constraints.Builder().setRequiresStorageNotLow(true).build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(StoreFileWorker.class, 15, TimeUnit.MINUTES)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);

        isListening = getDefaultSharedPreferences(this).getBoolean(HomeViewModel.IS_LISTENING_KEY, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationListenerService.class), mServiceConnection,
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

        Wearable.getCapabilityClient(this)
                .addListener(this, CLIENT_APP_CAPABILITY);

        startWearableActivity();
    }

    @Override
    protected void onPause() {
        Wearable.getCapabilityClient(this).removeListener(this);

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
        getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
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
                mService.requestLocationUpdates();
            } else {
                isListening = false;
                mService.removeLocationUpdates();
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_logs:
                        selectedFragment = new LogsFragment();
                        break;
                    case R.id.nav_journal:
                        selectedFragment = new JournalFragment();
                        break;
                }

                MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();

                return true;
            };

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ");
        Set<Node> nodes = capabilityInfo.getNodes();

        if (nodes.isEmpty()) {
            return;
        }
        for (Node node : nodes) {
            Toast.makeText(MainActivity.this, node.getDisplayName() + " " + node.getId(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sends an RPC to start a fullscreen Activity on the wearable.
     */
    public void startWearableActivity() {
        Log.d(TAG, "Generating start activity RPC");

        // Trigger an AsyncTask that will query for a list of connected nodes and send a
        // "start-activity" message to each connected node.
        new StartWearableActivityTask().execute();
    }

    @WorkerThread
    private void sendStartActivityMessage(String node) {

        Task<Integer> sendMessageTask =
                Wearable.getMessageClient(this).sendMessage(node, START_ACTIVITY_PATH, new byte[0]);

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendMessageTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

    @WorkerThread
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());
            }

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }

        return results;
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }
}

