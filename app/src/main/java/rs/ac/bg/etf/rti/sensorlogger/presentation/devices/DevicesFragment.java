package rs.ac.bg.etf.rti.sensorlogger.presentation.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.FragmentDevicesBinding;

/**
 * Device fragment class for the Devices tab
 */
public class DevicesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDevicesBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices, container, false);
        DevicesViewModel vm = new DevicesViewModel(getContext());
        binding.setVm(vm);
        binding.devicesLv.setAdapter(vm.getDevicesListAdapter());
        binding.devicesLv.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }
}
