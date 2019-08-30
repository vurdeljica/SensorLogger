package rs.ac.bg.etf.rti.sensorlogger.presentation.journal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.ListEntryDailyActivityBinding;
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;

public class JournalListAdapter extends BaseExpandableListAdapter {
    private List<DailyActivity> journalList;
    private List<String> listDataHeader;
    private HashMap<String, List<DailyActivity>> listHashMap;

    JournalListAdapter() {
        journalList = new ArrayList<>();
        listHashMap = new HashMap<>();
        listDataHeader = new ArrayList<>();
    }

    void clear() {
        journalList.clear();
        listDataHeader.clear();
        listHashMap.clear();
        notifyDataSetInvalidated();
    }

    void addAll(List<DailyActivity> list) {
        journalList.addAll(list);
        listHashMap = new HashMap<>(
                Stream.of(journalList)
                        .collect(Collectors.groupingBy(dailyActivity -> {
                            if (JournalListAdapter.this.isDateToday(dailyActivity.getDate())) {
                                return "Today";
                            }

                            if (JournalListAdapter.this.isDateYesterday(dailyActivity.getDate())) {
                                return "Yesterday";
                            }

                            SimpleDateFormat sdf_date = new SimpleDateFormat("EEE, MMM d", Locale.US);
                            return sdf_date.format(dailyActivity.getDate());
                        }))
        );

        listDataHeader = new ArrayList<>(listHashMap.keySet());
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listHashMap.get(listDataHeader.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return listDataHeader.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return listHashMap.get(listDataHeader.get(i)).get(i1); // i = Group Item , i1 = ChildItem
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String headerTitle = (String) getGroup(i);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_header_daily_activity, null);
        }
        TextView lblListHeader = view.findViewById(R.id.lblListHeader);
        lblListHeader.setText(headerTitle);

        ExpandableListView mExpandableListView = (ExpandableListView) viewGroup;
        mExpandableListView.expandGroup(i);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup viewGroup) {
        final DailyActivity dailyActivity = (DailyActivity) getChild(i, i1);
        ListEntryDailyActivityBinding journalEntryBinding = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                journalEntryBinding = DataBindingUtil.inflate(inflater, R.layout.list_entry_daily_activity, viewGroup, false);
            }
        } else {
            journalEntryBinding = DataBindingUtil.getBinding(convertView);
        }

        if (journalEntryBinding == null) {
            return null;
        }

        JournalListItemViewModel itemVM = new JournalListItemViewModel(dailyActivity);
        journalEntryBinding.setVm(itemVM);
        return journalEntryBinding.getRoot();
    }

    private boolean isDateToday(Date _date) {
        Calendar today = Calendar.getInstance();

        Calendar date = Calendar.getInstance();
        date.setTime(_date);

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isDateYesterday(Date _date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        Calendar date = Calendar.getInstance();
        date.setTime(_date);

        return yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
