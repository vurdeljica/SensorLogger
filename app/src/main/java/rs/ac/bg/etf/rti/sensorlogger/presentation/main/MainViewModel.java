package rs.ac.bg.etf.rti.sensorlogger.presentation.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel;
import rs.ac.bg.etf.rti.sensorlogger.services.ApplicationSensorBackgroundService;
import rs.ac.bg.etf.rti.sensorlogger.services.LocationListenerService;
import rs.ac.bg.etf.rti.sensorlogger.workers.StoreLocationInFileWorker;
import rs.ac.bg.etf.rti.sensorlogger.workers.StoreSensorDataInFileWorker;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainViewModel implements CapabilityClient.OnCapabilityChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private static final String STORE_WORKER_LOCATION_ID = "StoreLocationInFileWorker";
    private static final String STORE_WORKER_SENSOR_ID = "StoreSensorDataInFileWorker";
    private static final String NODE_ID_KEY = "nodeId";
    public static final String CLIENT_APP_CAPABILITY = "sensor_app_client";
    private static final String SHOULD_START_LISTENING_PATH = "/should-start-listening";

    private Context context;
    private ServiceHandler serviceHandler;

    // Used for saving the listening state
    private boolean isListening;

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

    MainViewModel(Context context, ServiceHandler serviceHandler) {
        this.context = context;
        this.serviceHandler = serviceHandler;
        isListening = getDefaultSharedPreferences(context).getBoolean(HomeViewModel.IS_LISTENING_KEY, false);

        Wearable.getNodeClient(context).getLocalNode()
                .addOnSuccessListener(node -> getDefaultSharedPreferences(context).edit().putString(NODE_ID_KEY, node.getId()).apply());
    }

    void startCapabilityListener() {
        Wearable.getCapabilityClient(context)
                .addListener(this, CLIENT_APP_CAPABILITY);
    }

    void stopCapabilityListener() {
        Wearable.getCapabilityClient(context).removeListener(this);
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ");
    }

    void startStoreWorker() {
        WorkManager workManager = WorkManager.getInstance(context);
        Constraints constraints = new Constraints.Builder().setRequiresStorageNotLow(true).build();
        PeriodicWorkRequest storeLocationWorkRequest = new PeriodicWorkRequest.Builder(StoreLocationInFileWorker.class, 15, TimeUnit.MINUTES)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        workManager.enqueueUniquePeriodicWork(STORE_WORKER_LOCATION_ID, ExistingPeriodicWorkPolicy.KEEP, storeLocationWorkRequest);
        PeriodicWorkRequest storeSensorDataWorkRequest = new PeriodicWorkRequest.Builder(StoreSensorDataInFileWorker.class, 15, TimeUnit.MINUTES)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        workManager.enqueueUniquePeriodicWork(STORE_WORKER_SENSOR_ID, ExistingPeriodicWorkPolicy.KEEP, storeSensorDataWorkRequest);
    }

    void bindServices() {
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        serviceHandler.bind(new Intent(context, LocationListenerService.class), mLocationServiceConnection);
        serviceHandler.bind(new Intent(context, ApplicationSensorBackgroundService.class), mSensorServiceConnection);
    }

    void unbindServices() {
        if (mLocationServiceBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            serviceHandler.unbind(mLocationServiceConnection);
            mLocationServiceBound = false;
        }
        if (mSensorServiceBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            serviceHandler.unbind(mSensorServiceConnection);
            mSensorServiceBound = false;
        }
    }

    void registerSharedPreferenceListener() {
        getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this);
    }

    void unregisterSharedPreferenceListener() {
        getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    void updateServicesState() {
        if (isListening) {
            mLocationService.requestLocationUpdates();
            mSensorService.requestSensorEventUpdates();
        } else {
            mLocationService.removeLocationUpdates();
            mSensorService.removeSensorEventUpdates();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(HomeViewModel.IS_LISTENING_KEY)) {
            isListening = sharedPreferences.getBoolean(s, false);
            updateServicesState();
            setWearableShouldStartListeners();
        }
    }

    void setWearableShouldStartListeners() {
        Log.d(TAG, "Generating start/stop listening RPC");

        // Trigger an AsyncTask that will query for a list of connected nodes and send a
        // "should-start-listening" message to each connected node.
        new WearableShouldStartListenersTask().execute(isListening);
    }

    @WorkerThread
    private void sendShouldStartListenersMessage(String node, boolean payload) {

        byte[] bytes = {(byte) (payload ? 1 : 0)};
        Task<Integer> sendMessageTask =
                Wearable.getMessageClient(context).sendMessage(node, SHOULD_START_LISTENING_PATH, bytes);

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
                Wearable.getNodeClient(context).getConnectedNodes();

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

    private class WearableShouldStartListenersTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... booleans) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendShouldStartListenersMessage(node, booleans[0]);
            }
            return null;
        }
    }
}
