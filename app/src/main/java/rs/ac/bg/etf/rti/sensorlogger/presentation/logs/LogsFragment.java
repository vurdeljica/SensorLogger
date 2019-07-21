package rs.ac.bg.etf.rti.sensorlogger.presentation.logs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.FragmentLogsBinding;

public class LogsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentLogsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_logs, container, false);
        binding.setVm(new LogsViewModel());
        return binding.getRoot();
    }
}
