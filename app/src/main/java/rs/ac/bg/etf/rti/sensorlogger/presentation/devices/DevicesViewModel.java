package rs.ac.bg.etf.rti.sensorlogger.presentation.devices;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Wearable;

import static rs.ac.bg.etf.rti.sensorlogger.presentation.main.MainViewModel.CLIENT_APP_CAPABILITY;

public class DevicesViewModel extends BaseObservable implements CapabilityClient.OnCapabilityChangedListener {

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

        Wearable.getNodeClient(context).getConnectedNodes().addOnSuccessListener(nodes -> {
            devicesListAdapter.addAll(nodes);
            notifyChange();
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
