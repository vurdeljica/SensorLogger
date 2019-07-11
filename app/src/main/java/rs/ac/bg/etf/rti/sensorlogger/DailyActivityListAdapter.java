package rs.ac.bg.etf.rti.sensorlogger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;

public class DailyActivityListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String,List<DailyActivity>> listHashMap;

    public DailyActivityListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<DailyActivity>> listHashMap) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;
    }

    public List<String> getListDataHeader() {
        return listDataHeader;
    }

    public void setListDataHeader(List<String> listDataHeader) {
        this.listDataHeader = listDataHeader;
    }

    public HashMap<String, List<DailyActivity>> getListHashMap() {
        return listHashMap;
    }

    public void setListHashMap(HashMap<String, List<DailyActivity>> listHashMap) {
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

    static class ViewHolder {
        protected TextView txtStartTime;
        protected TextView txtActivityName;
        protected TextView txtDuration;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup viewGroup) {
        final DailyActivity dailyActivity = (DailyActivity)getChild(i,i1);
        View view = null;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_entry_daily_activity,null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.txtStartTime = (TextView) view.findViewById(R.id.daily_acivity_start_time);
            viewHolder.txtActivityName = (TextView) view.findViewById(R.id.daily_acivity_activity_name);
            viewHolder.txtDuration = (TextView) view.findViewById(R.id.daily_acivity_duration);
            view.setTag(viewHolder);
        }

        else {
            view = convertView;
        }

        final ViewHolder holder = (ViewHolder) view.getTag();

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");

        holder.txtStartTime.setText(sdf.format(dailyActivity.getStartTime()));
        holder.txtActivityName.setText(dailyActivity.getActivityTitle());

        long timeDiff = dailyActivity.getEndTime().getTime() - dailyActivity.getStartTime().getTime();
        long hours = timeDiff / 3600000;
        long minutes = (timeDiff % 3600000) / 60000;
        String hoursTxt = hours == 0 ? "" : hours + " hr ";
        String minutesTxt = minutes == 0 ? "" : minutes + " min";
        holder.txtDuration.setText(hoursTxt + minutesTxt);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, JournalActivity.class);
                intent.putExtra("id", dailyActivity.getId());
                context.startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
