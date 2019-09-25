package rs.ac.bg.etf.rti.sensorlogger.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Simple data object for storing daily activities of the user
 */
public class DailyActivity extends RealmObject {

    private static SimpleDateFormat sdf_date = new SimpleDateFormat("MMM d", Locale.US);
    private static SimpleDateFormat sdf_time = new SimpleDateFormat("h:mm a", Locale.US);

    @PrimaryKey
    private long id;

    private String activityTitle;

    //Type of activity, can be high, medium or low intensity
    @Required
    private String activityType;

    @Required
    private Date date;

    @Required
    private Date startTime;

    @Required
    private Date endTime;

    private String notes;

    public DailyActivity() {
        id = -1;
        date = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(0, 0, 0);

        endTime = calendar.getTime();

        calendar.add(Calendar.HOUR_OF_DAY, -1);
        startTime = calendar.getTime();

        activityType = "";
        activityTitle = "";
        notes = "";
    }

    public DailyActivity(long id, String activityTitle, String activityType, Date date, Date startTime, Date endTime, String notes) {
        this.id = id;
        this.activityTitle = activityTitle;
        this.activityType = activityType;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    public String getDateAsString() {
        return sdf_date.format(date);
    }

    public void setDateAsString(String date) {
        try {
            this.date = sdf_date.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStartTimeAsString() {
        return sdf_time.format(startTime);
    }

    public void setStartTimeAsString(String date) {
        try {
            this.startTime = sdf_time.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getEndTimeAsString() {
        return sdf_time.format(endTime);
    }

    public void setEndTimeAsString(String date) {
        try {
            this.endTime = sdf_time.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return activity duration as a String suitable for showing on the UI
     */
    public String getActivityDuration() {
        long timeDiff = endTime.getTime() - startTime.getTime();
        long hours = timeDiff / 3600000;
        long minutes = (timeDiff % 3600000) / 60000;
        String hoursTxt = hours == 0 ? "" : hours + " hr ";
        String minutesTxt = minutes == 0 ? "" : minutes + " min";
        return hoursTxt + minutesTxt;
    }

    public String getActivityTitle() {
        return activityTitle != null && !activityTitle.isEmpty() ? activityTitle : activityType;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
