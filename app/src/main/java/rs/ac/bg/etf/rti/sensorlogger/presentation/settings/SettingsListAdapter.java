package rs.ac.bg.etf.rti.sensorlogger.presentation.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.config.SensorLoggerApplication;
import rs.ac.bg.etf.rti.sensorlogger.databinding.ListEntrySamplingRateBinding;

import static rs.ac.bg.etf.rti.sensorlogger.services.ApplicationSensorBackgroundService.SENSOR_SAMPLING_RATE_KEY;

/**
 * List adapter for the sampling rate settings
 */
public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.DeviceViewHolder> {

    /**
     * List of the sampling rates
     */
    private final List<SamplingRate> samplingRateList;
    private final Context context;

    SettingsListAdapter(Context context) {
        this.context = context;
        samplingRateList = new ArrayList<>();
        samplingRateList.addAll(Arrays.asList(SamplingRate.values()));
    }

    private void clear() {
        samplingRateList.clear();
        notifyDataSetChanged();
    }

    private void addAll() {
        samplingRateList.addAll(Arrays.asList(SamplingRate.values()));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListEntrySamplingRateBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.list_entry_sampling_rate, parent, false);
        return new DeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        SamplingRate samplingRate = samplingRateList.get(position);
        int rate = context.getSharedPreferences(SensorLoggerApplication.SHARED_PREFERENCES_ID, Context.MODE_PRIVATE).getInt(SENSOR_SAMPLING_RATE_KEY, 200000);
        holder.binding.setVm(new SettingsListItemViewModel(samplingRate, samplingRate.getRate() == rate));
        holder.binding.checkBox.setOnClickListener(view -> {
            context.getSharedPreferences(SensorLoggerApplication.SHARED_PREFERENCES_ID, Context.MODE_PRIVATE).edit().putInt(SENSOR_SAMPLING_RATE_KEY, samplingRate.getRate()).apply();
            clear();
            addAll();
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return samplingRateList.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        private ListEntrySamplingRateBinding binding;

        DeviceViewHolder(@NonNull ListEntrySamplingRateBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }

}
