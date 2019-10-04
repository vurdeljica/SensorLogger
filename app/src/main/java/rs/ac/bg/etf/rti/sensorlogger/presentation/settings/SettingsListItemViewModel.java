package rs.ac.bg.etf.rti.sensorlogger.presentation.settings;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * View model for the sampling rate settings list items
 */
public class SettingsListItemViewModel extends BaseObservable {

    private final SamplingRate samplingRate;
    private boolean selected;

    SettingsListItemViewModel(SamplingRate samplingRate, boolean selected) {
        this.samplingRate = samplingRate;
        this.selected = selected;
    }

    @Bindable
    public String getSamplingRate() {
        return samplingRate.getTitle();
    }

    @Bindable
    public boolean isSelected() {
        return selected;
    }
}
