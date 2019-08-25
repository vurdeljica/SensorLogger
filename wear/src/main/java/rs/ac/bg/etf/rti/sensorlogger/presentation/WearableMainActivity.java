package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

import java.util.Set;

import rs.ac.bg.etf.rti.sensorlogger.R;

public class WearableMainActivity extends WearableActivity implements DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

    private static final String TAG = "WearableMainActivity";
    private static final String SERVER_APP_CAPABILITY = "sensor_app_server";
    private static final String COUNT_KEY = "com.example.key.count";

    private TextView mTextView;

    private WearableSensorManager wearableSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);

        wearableSensorManager = new WearableSensorManager(getSystemService(SensorManager.class));

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(this, SERVER_APP_CAPABILITY);

        wearableSensorManager.startListening(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);

        wearableSensorManager.startListening(getApplicationContext());
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: ");
        Set<Node> nodes = capabilityInfo.getNodes();

        if (nodes.isEmpty()) {
            return;
        }
        for (Node node : nodes) {
            Toast.makeText(WearableMainActivity.this, node.getDisplayName() + " " + node.getId(), Toast.LENGTH_SHORT).show();
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
                    mTextView.setText(String.valueOf(count));
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
