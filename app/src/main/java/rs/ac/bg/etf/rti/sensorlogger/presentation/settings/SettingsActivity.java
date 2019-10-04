package rs.ac.bg.etf.rti.sensorlogger.presentation.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.ActivitySettingsBinding;

/**
 * Activity for setting the sampling rate
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        setupToolbar();

        SettingsViewModel settingsViewModel = new SettingsViewModel(this);
        binding.setVm(settingsViewModel);

        binding.samplingRateLv.setAdapter(settingsViewModel.getSettingsAdapter());
        binding.samplingRateLv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        setSupportActionBar(binding.settingsToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
