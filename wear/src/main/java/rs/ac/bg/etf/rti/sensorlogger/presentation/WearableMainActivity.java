package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import rs.ac.bg.etf.rti.sensorlogger.BuildConfig;
import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.ActivityMainBinding;
import rs.ac.bg.etf.rti.sensorlogger.services.WearableSensorBackgroundService;

/**
 * Main activity of the application
 */
public class WearableMainActivity extends WearableActivity implements ServiceHandler {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

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

        if (!checkPermissions()) {
            requestPermissions();
        }
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

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS);
    }

    /**
     * Requests missing permissions
     */
    private void requestPermissions() {
            ActivityCompat.requestPermissions(WearableMainActivity.this,
                    new String[]{Manifest.permission.BODY_SENSORS}, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

}
