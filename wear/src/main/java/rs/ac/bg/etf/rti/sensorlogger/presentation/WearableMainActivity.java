package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import androidx.databinding.DataBindingUtil;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.ActivityMainBinding;

public class WearableMainActivity extends WearableActivity {

    private WearableMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new WearableMainViewModel(getApplicationContext());
        binding.setVm(viewModel);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.startCapabilityListener();
    }

    @Override
    protected void onPause() {
        super.onPause();

        viewModel.stopCapabilityListener();
    }
}
