package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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

import java.util.Set;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeFragment;
import rs.ac.bg.etf.rti.sensorlogger.presentation.journal.JournalFragment;
import rs.ac.bg.etf.rti.sensorlogger.presentation.logs.LogsFragment;

public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

    private String TAG = "MainActivitiy";
    private static final String CLIENT_APP_CAPABILITY = "sensor_app_client";
    private static final String COUNT_KEY = "com.example.key.count";

    Fragment selectedFragment = null;

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
}

