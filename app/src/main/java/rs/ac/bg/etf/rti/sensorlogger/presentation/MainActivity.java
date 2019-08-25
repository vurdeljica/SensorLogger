package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.network.NetworkManager;
import rs.ac.bg.etf.rti.sensorlogger.network.ServerInfo;
import rs.ac.bg.etf.rti.sensorlogger.persistency.PersitencyManager;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeFragment;
import rs.ac.bg.etf.rti.sensorlogger.presentation.journal.JournalFragment;
import rs.ac.bg.etf.rti.sensorlogger.presentation.logs.LogsFragment;

public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

    private static String TAG = "MainActivity";
    private static final String CLIENT_APP_CAPABILITY = "sensor_app_client";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String COUNT_KEY = "com.example.key.count";

    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        final NetworkManager networkManager = NetworkManager.getInstance(getApplicationContext());

        Thread thread = new Thread(new Runnable() {
            boolean transfered = false;
            @Override
            public void run() {
                try {
                    PersitencyManager.getInstance().saveDailyActivity();

                    Random rand = new Random();

                     /* for (int i = 0; i < 50; i++) {

                        List<SensorDataProtos.MobileData> sensorDataList = new ArrayList<>();
                        List<SensorDataProtos.DeviceData> deviceSensorDataList = new ArrayList<>();
                        long time = System.currentTimeMillis();
                        for(int j = 0; j < 30000; j++) {
                            time += 30;
                            SensorDataProtos.MobileData mobileData = SensorDataProtos.MobileData.newBuilder()
                                    .setGpsAccuracy((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setGpsAltitude((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setGpsLatitude((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setGpsLongitude((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setHeartRate((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setStepCount(rand.nextInt(20000))
                                    .setTimestamp(time)
                                    .build();

                            sensorDataList.add(mobileData);

                            SensorDataProtos.DeviceData deviceData = SensorDataProtos.DeviceData.newBuilder()
                                    .setAccX((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setAccY((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setAccZ((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setGyrX((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setGyrY((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setGyrZ((float)rand.nextFloat() * 150 * (rand.nextFloat() > 0.5 ? 1 : -1))
                                    .setMacAddress(j % 2 == 0 ? "first mac address": "second mac address")
                                    .build();

                            deviceSensorDataList.add(deviceData);
                        }

                        PersitencyManager.getInstance().saveMobileData(sensorDataList);
                        PersitencyManager.getInstance().saveDeviceData(deviceSensorDataList);
                    }
*/
                    while(!transfered) {
                        Thread.sleep(1000);
                        List<ServerInfo> sInfoList = networkManager.getServersInformation();
                        for(ServerInfo serverInfo : sInfoList) {
                            Log.d("ZEROCONF TEST", serverInfo.toString());

                            File dirP = new File(Environment.getExternalStorageDirectory() + "/testDirectory");

                            networkManager.uploadDirectoryContentToServer(serverInfo, dirP);

                            transfered = true;

                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();



        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (selectedFragment != null && selectedFragment instanceof JournalFragment) {
            ((JournalFragment) selectedFragment).updateFragment();
        }

        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(this, CLIENT_APP_CAPABILITY);

        startWearableActivity();
    }


    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                }
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

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: ");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    int count = dataMap.getInt(COUNT_KEY);
                    Log.d(TAG, "onDataChanged: data changed" + count);
                    Toast.makeText(this, String.valueOf(count), Toast.LENGTH_SHORT).show();
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
                Log.d(TAG, "onDataChanged: data deleted");
            }
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: ");
    }

    /**
     * Sends an RPC to start a fullscreen Activity on the wearable.
     */
    public void startWearableActivity() {
        Log.d(TAG, "Generating RPC");

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

