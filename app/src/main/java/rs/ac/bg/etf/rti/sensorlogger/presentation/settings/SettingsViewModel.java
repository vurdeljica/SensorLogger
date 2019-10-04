package rs.ac.bg.etf.rti.sensorlogger.presentation.settings;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

/**
 * View model for the sampling rate settings
 */
public class SettingsViewModel {

    private SettingsListAdapter settingsListAdapter;

    SettingsViewModel(Context context) {
        settingsListAdapter = new SettingsListAdapter(context);
    }

    RecyclerView.Adapter getSettingsAdapter() {
        return settingsListAdapter;
    }

}
