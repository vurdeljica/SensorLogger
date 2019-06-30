package rs.ac.bg.etf.rti.sensorlogger;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.beans.DailyActivity;

public class DailyActivityListAdapter extends ArrayAdapter<DailyActivity> {

    private List<DailyActivity> dailyActivityList;
    private final Activity context;

    public DailyActivityListAdapter(Activity context, List<DailyActivity> list) {
        super(context, R.layout.list_entry_daily_activity, list);
        this.context = context;
        this.dailyActivityList = list;
    }

    static class ViewHolder {
        protected TextView txtStartTime;
        protected TextView txtActivityName;
        protected TextView txtDuration;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.list_entry_daily_activity, null, true);
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

        holder.txtStartTime.setText(dailyActivityList.get(position).getStartTime());
        holder.txtActivityName.setText(dailyActivityList.get(position).getActivityTitle());

        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");

        try {
            Date timeStart = formatter.parse(dailyActivityList.get(position).getStartTime());
            Date timeEnd = formatter.parse(dailyActivityList.get(position).getEndTime());
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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, JournalActivity.class);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });

        return view;
    }

}
