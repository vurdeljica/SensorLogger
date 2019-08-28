package rs.ac.bg.etf.rti.sensorlogger.presentation.journal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.FragmentJournalBinding;
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;
import rs.ac.bg.etf.rti.sensorlogger.presentation.journalEntry.JournalEntryActivity;


public class JournalFragment extends Fragment {

    private JournalViewModel viewModel;

    private OnFragmentInteractionListener mListener;

    public JournalFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void updateFragment() {
        viewModel.inflateJournalListAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentJournalBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_journal, container, false);

        binding.addJournalButton.setOnClickListener(v -> JournalFragment.this.startActivity(new Intent(JournalFragment.this.getActivity(), JournalEntryActivity.class)));

        viewModel = new JournalViewModel();
        binding.setVm(viewModel);

        binding.journalList.setAdapter(viewModel.getJournalListAdapter());
        binding.journalList.setOnChildClickListener(getOnChildClickListener());

        viewModel.inflateJournalListAdapter();

        return binding.getRoot();
    }

    private ExpandableListView.OnChildClickListener getOnChildClickListener() {
        return (parent, v, groupPosition, childPosition, id) -> {
            Log.d("TEST", "Journal Entry on click");
            DailyActivity activity = (DailyActivity) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
            JournalFragment.this.startJournalEntryActivity(activity.getId(), v.getContext());
            return true;
        };
    }

    private void startJournalEntryActivity(long id, Context context) {
        Intent intent = new Intent(context, JournalEntryActivity.class);
        intent.putExtra("id", id);
        context.startActivity(intent);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}