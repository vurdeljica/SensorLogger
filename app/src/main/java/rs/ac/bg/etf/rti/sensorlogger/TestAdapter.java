package rs.ac.bg.etf.rti.sensorlogger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.beans.DailyActivity;

public class TestAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String,List<DailyActivity>> listHashMap;

    public TestAdapter(Context context, List<String> listDataHeader, HashMap<String, List<DailyActivity>> listHashMap) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;
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
        String headerTitle = (String)getGroup(i);
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_header_daily_activity,null);
        }
        TextView lblListHeader = (TextView)view.findViewById(R.id.lblListHeader);
        lblListHeader.setText(headerTitle);

        ExpandableListView mExpandableListView = (ExpandableListView) viewGroup;
        mExpandableListView.expandGroup(i);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup viewGroup) {
        final DailyActivity dailyActivity = (DailyActivity)getChild(i,i1);
        View view = null;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_entry_daily_activity,null);
            final DailyActivityListAdapter.ViewHolder viewHolder = new DailyActivityListAdapter.ViewHolder();
            viewHolder.txtStartTime = (TextView) view.findViewById(R.id.daily_acivity_start_time);
            viewHolder.txtActivityName = (TextView) view.findViewById(R.id.daily_acivity_activity_name);
            viewHolder.txtDuration = (TextView) view.findViewById(R.id.daily_acivity_duration);
            view.setTag(viewHolder);
        }

        else {
            view = convertView;
        }

        final DailyActivityListAdapter.ViewHolder holder = (DailyActivityListAdapter.ViewHolder) view.getTag();

        holder.txtStartTime.setText(dailyActivity.getStartTime());
        holder.txtActivityName.setText(dailyActivity.getActivityTitle());

        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");

        try {
            Date timeStart = formatter.parse(dailyActivity.getStartTime());
            Date timeEnd = formatter.parse(dailyActivity.getEndTime());
            long timeDiff = timeEnd.getTime() - timeStart.getTime();
            long hours = timeDiff / 3600000;
            long minutes = (timeDiff % 3600000) / 60000;
            String hoursTxt = hours == 0 ? "" : hours + " hr ";
            String minutesTxt = minutes == 0 ? "" : minutes + " min";
            holder.txtDuration.setText(hoursTxt + minutesTxt);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
