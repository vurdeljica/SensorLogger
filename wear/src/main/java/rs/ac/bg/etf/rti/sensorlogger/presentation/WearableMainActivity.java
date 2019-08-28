package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

import rs.ac.bg.etf.rti.sensorlogger.R;

public class WearableMainActivity extends WearableActivity implements CapabilityClient.OnCapabilityChangedListener {

    private static final String TAG = "WearableMainActivity";
    private static final String SERVER_APP_CAPABILITY = "sensor_app_server";

    private WearableSensorManager wearableSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wearableSensorManager = new WearableSensorManager(getSystemService(SensorManager.class));

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Wearable.getCapabilityClient(this)
                .addListener(this, SERVER_APP_CAPABILITY);

        wearableSensorManager.startListening(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();

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
}
