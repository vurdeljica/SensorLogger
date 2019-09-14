package rs.ac.bg.etf.rti.sensorlogger.presentation.main;

import android.content.Context;
import android.os.AsyncTask;
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
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import rs.ac.bg.etf.rti.sensorlogger.workers.StoreFileWorker;

public class MainViewModel implements CapabilityClient.OnCapabilityChangedListener {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private static final String STORE_WORKER_ID = "StoreFileWorker";

    public static final String CLIENT_APP_CAPABILITY = "sensor_app_client";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String SHOULD_START_LISTENING_PATH = "/should-start-listening";

    private Context context;

    MainViewModel(Context context) {
        this.context = context;
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
        Constraints constraints = new Constraints.Builder().setRequiresStorageNotLow(true).build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(StoreFileWorker.class, 15, TimeUnit.MINUTES)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniquePeriodicWork(STORE_WORKER_ID, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    /**
     * Sends an RPC to start a fullscreen Activity on the wearable.
     */
    void startWearableActivity() {
        Log.d(TAG, "Generating start activity RPC");

        // Trigger an AsyncTask that will query for a list of connected nodes and send a
        // "start-activity" message to each connected node.
        new StartWearableActivityTask().execute();
    }

    void setWearableShouldStartListeners(boolean shouldStartListening) {
        Log.d(TAG, "Generating start/stop listening RPC");

        // Trigger an AsyncTask that will query for a list of connected nodes and send a
        // "should-start-listening" message to each connected node.
        new WearableShouldStartListenersTask().execute(shouldStartListening);
    }

    @WorkerThread
    private void sendStartActivityMessage(String node) {

        Task<Integer> sendMessageTask =
                Wearable.getMessageClient(context).sendMessage(node, START_ACTIVITY_PATH, new byte[0]);

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
