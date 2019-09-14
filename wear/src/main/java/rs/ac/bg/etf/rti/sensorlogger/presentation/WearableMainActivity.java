package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import androidx.databinding.DataBindingUtil;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.ActivityMainBinding;
import rs.ac.bg.etf.rti.sensorlogger.services.WearableSensorBackgroundService;

public class WearableMainActivity extends WearableActivity implements ServiceHandler {

    private WearableMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new WearableMainViewModel(getApplicationContext(), this);
        binding.setVm(viewModel);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startCapabilityListener();
    }

    @Override
    protected void onPause() {
        viewModel.stopCapabilityListener();
        super.onPause();
    }

    @Override
    protected void onStop() {
        viewModel.destroy();
        super.onStop();
    }

    @Override
    public void bindSensorService(Intent intent, ServiceConnection serviceConnection) {
        bindService(new Intent(this, WearableSensorBackgroundService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void unbindSensorService(ServiceConnection serviceConnection) {
        unbindService(serviceConnection);
    }
}
