package rs.ac.bg.etf.rti.sensorlogger.fragments;

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
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.JournalActivity;
import rs.ac.bg.etf.rti.sensorlogger.DailyActivityListAdapter;
import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.TestAdapter;
import rs.ac.bg.etf.rti.sensorlogger.beans.DailyActivity;


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

        journalList = new ArrayList<>();
        DailyActivity activity1 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 29", "5:37 PM",
                "6:37 PM", "No notes");

        DailyActivity activity2 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 29", "5:37 PM",
                "6:37 PM", "No notes");

        DailyActivity activity3 = new DailyActivity("Jutarnje trcanje",
                "Very high intensity", "Jun 28", "2:37 PM",
                "4:37 PM", "Bilo je bas naporno");

        DailyActivity activity4 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 28", "5:37 PM",
                "6:37 PM", "No notes");

        DailyActivity activity5 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 28", "5:37 PM",
                "6:37 PM", "No notes");

        DailyActivity activity6 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 27", "5:37 PM",
                "6:37 PM", "No notes");

        DailyActivity activity7 = new DailyActivity("Jutarnje trcanje",
                "Very high intensity", "Jun 27", "2:37 PM",
                "4:37 PM", "Bilo je bas naporno");

        DailyActivity activity8 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 27", "5:35 PM",
                "6:37 PM", "No notes");

        DailyActivity activity9 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 26", "5:37 PM",
                "6:37 PM", "No notes");

        DailyActivity activity10 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 26", "5:37 PM",
                "6:37 PM", "No notes");

        DailyActivity activity11 = new DailyActivity("Jutarnje trcanje",
                "Very high intensity", "Jun 26", "2:37 PM",
                "4:37 PM", "Bilo je bas naporno");

        DailyActivity activity12 = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 26", "5:37 PM",
                "6:37 PM", "No notes");

        journalList.add(activity1);
        journalList.add(activity2);
        journalList.add(activity3);
        journalList.add(activity4);
        journalList.add(activity5);
        journalList.add(activity6);
        journalList.add(activity7);
        journalList.add(activity8);
        journalList.add(activity9);
        journalList.add(activity10);
        journalList.add(activity11);
        journalList.add(activity12);

        List<List<DailyActivity>> groups = new ArrayList<>(
                Stream.of(journalList)
                        .collect(Collectors.groupingBy(new Function<DailyActivity, String>() {
                            @Override
                            public String apply(DailyActivity dailyActivity) {
                                return dailyActivity.getDate();
                            }
                        }))
                        .values());

        listHash = new HashMap<>();;
        listDataHeader = new ArrayList<>();

        int i = 0;
        for (List<DailyActivity> group : groups) {
            listDataHeader.add(group.get(0).getDate());
            listHash.put(group.get(0).getDate(), group);
        }


        //DailyActivityListAdapter adapter = new DailyActivityListAdapter(getActivity(), journalList);
        //setListAdapter(adapter);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_journal, container, false);

        ExpandableListView listView = (ExpandableListView) inflatedView.findViewById(R.id.list);
        ExpandableListAdapter listAdapter = new TestAdapter(getActivity(), listDataHeader, listHash);
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