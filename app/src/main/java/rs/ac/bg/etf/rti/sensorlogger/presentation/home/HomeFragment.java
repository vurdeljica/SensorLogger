package rs.ac.bg.etf.rti.sensorlogger.presentation.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.FragmentHomeBinding;

/**
 * Fragment class for the Home tab
 */
public class HomeFragment extends Fragment {

    private Animation fabOpen, fabClose, fabClock, fabAntiClock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentHomeBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        fabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        fabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fabClock = AnimationUtils.loadAnimation(getContext(), R.anim.fab_rotate_clock);
        fabAntiClock = AnimationUtils.loadAnimation(getContext(), R.anim.fab_rotate_anticlock);

        HomeViewModel vm = new HomeViewModel(getActivity(), (a) -> {
            binding.fabSend.startAnimation(fabClose);
            binding.fabDelete.startAnimation(fabClose);
            binding.fabMenu.startAnimation(fabAntiClock);
        }, (a) -> {
            binding.fabSend.startAnimation(fabOpen);
            binding.fabDelete.startAnimation(fabOpen);
            binding.fabMenu.startAnimation(fabClock);
        });

        binding.setVm(vm);
        return binding.getRoot();
    }
}
