package rs.ac.bg.etf.rti.sensorlogger.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.JournalActivity;
import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.DailyActivityListAdapter;
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;


public class JournalFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    List<DailyActivity> journalList;
    HashMap<String,List<DailyActivity>> listHash;
    List<String> listDataHeader;

    public JournalFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareAdapterData();
    }


    public void updateFragment() {
        prepareAdapterData();
        refreshAdapter();
    }

    private void prepareAdapterData() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        journalList = dbManager.getDailyActivities();

        listHash = new HashMap<>(
                Stream.of(journalList)
                        .collect(Collectors.groupingBy(new Function<DailyActivity, String>() {
                            @Override
                            public String apply(DailyActivity dailyActivity) {
                                if (isDateToday(dailyActivity.getDate())) {
                                    return "Today";
                                }

                                if (isDateYesterday(dailyActivity.getDate())) {
                                    return "Yesterday";
                                }

                                SimpleDateFormat sdf_date = new SimpleDateFormat("EEE, MMM d");
                                return sdf_date.format(dailyActivity.getDate());
                            }
                        }))
        );

        listDataHeader = new ArrayList<>(listHash.keySet());

    }

    private void refreshAdapter() {
        ExpandableListView listView = (ExpandableListView) getActivity().findViewById(R.id.list);
        DailyActivityListAdapter listAdapter = (DailyActivityListAdapter)listView.getExpandableListAdapter();
        listAdapter.setListDataHeader(listDataHeader);
        listAdapter.setListHashMap(listHash);
        listAdapter.notifyDataSetChanged();
    }

    private boolean isDateToday(Date _date) {
        Calendar today = Calendar.getInstance();

        Calendar date = Calendar.getInstance();
        date.setTime(_date);

        if (today.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) {
            return true;
        }

        return false;
    }

    private boolean isDateYesterday(Date _date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        Calendar date = Calendar.getInstance();
        date.setTime(_date);

        if (yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) {
            return true;
        }

        return false;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_journal, container, false);

        ExpandableListView listView = (ExpandableListView) inflatedView.findViewById(R.id.list);
        ExpandableListAdapter listAdapter = new DailyActivityListAdapter(getActivity(), listDataHeader, listHash);
        listView.setAdapter(listAdapter);

        FloatingActionButton addJournalButton = inflatedView.findViewById(R.id.add_journal_button);
        addJournalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), JournalActivity.class));
            }
        });

        return inflatedView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
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