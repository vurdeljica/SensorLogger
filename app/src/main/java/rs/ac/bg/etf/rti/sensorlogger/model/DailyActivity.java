package rs.ac.bg.etf.rti.sensorlogger.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class DailyActivity extends RealmObject {

    @PrimaryKey
    private long id;

    private String activityTitle;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getActivityTitle() {
        return activityTitle;
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
