package rs.ac.bg.etf.rti.sensorlogger.presentation.devices;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Wearable;

import static rs.ac.bg.etf.rti.sensorlogger.presentation.main.MainViewModel.CLIENT_APP_CAPABILITY;

public class DevicesViewModel extends BaseObservable implements CapabilityClient.OnCapabilityChangedListener {

    private static final String CAPABILITY_WATCH_APP = "sensor_app_client";
    public static String TAG = DevicesViewModel.class.getSimpleName();

    private DevicesListAdapter devicesListAdapter;
    private Context context;

    private long updateInterval = 1500L;
    private Handler updateHandler = new Handler();

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateDevicesList(context);
            updateHandler.postDelayed(this, updateInterval);
        }
    };

    DevicesViewModel(Context context) {
        this.context = context;
        devicesListAdapter = new DevicesListAdapter();
        Wearable.getCapabilityClient(context).addListener(this, CLIENT_APP_CAPABILITY);
        updateDevicesList(context);
        startUpdates();
    }

    private void updateDevicesList(Context context) {
        devicesListAdapter.clear();

        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(context)
                .getCapability(CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE);

        capabilityInfoTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                CapabilityInfo capabilityInfo = task.getResult();
                if (capabilityInfo != null && capabilityInfo.getNodes() != null) {
                    devicesListAdapter.addAll(capabilityInfo.getNodes());
                }
            } else {
                Log.d(TAG, "Capability request failed to return any results.");
            }

        });
    }

    RecyclerView.Adapter getDevicesListAdapter() {
        return devicesListAdapter;
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        devicesListAdapter.clear();
        updateDevicesList(context);
    }

    private void startUpdates() {
        updateHandler.postDelayed(updateRunnable, updateInterval);
    }
}
