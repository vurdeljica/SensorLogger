package rs.ac.bg.etf.rti.sensorlogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.BaseExpandableListAdapter;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.TimePicker;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;

public class JournalActivity extends AppCompatActivity {

    TextInputEditText name;
    AutoCompleteTextView type;
    TextInputEditText dateText;
    TextInputEditText timeFromText;
    TextInputEditText timeToText;
    TextInputEditText notes;

    long diffInMillies = 60 * 1000 * 1000;

    Date date;
    Date timeFrom;
    Date timeTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        getUIComponents();

        initTimeAndDate();

        prepareCollapsingToolbar();

        prepareDatePickerDialog();

        if (isExistingActivity()) {
            editExistingActivity();
        }

        prepareTimeFromPickerDialog();
        prepareTimeToPickerDialog();

    }

    private void getUIComponents() {
        name = findViewById(R.id.journal_activity_name);
        type = findViewById(R.id.journal_acitivty_type);
        dateText = findViewById(R.id.journal_date_dropdown);
        timeFromText = findViewById(R.id.journal_time_from_dropdown);
        timeToText = findViewById(R.id.journal_time_to_dropdown);
        notes = findViewById(R.id.journal_notes);
    }

    private void initTimeAndDate() {
        date = new Date();

        Calendar calendar = Calendar.getInstance();

        timeTo = new Date();
        timeTo.setSeconds(0);

        calendar.add(Calendar.HOUR_OF_DAY, -1);
        timeFrom = calendar.getTime();
        timeFrom.setSeconds(0);
    }

    private void prepareCollapsingToolbar() {
        Toolbar toolbar = findViewById(R.id.journal_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JournalActivity.this.onBackPressed();
            }
        });

        CollapsingToolbarLayout ctl = findViewById(R.id.journal_collapsing_toolbar);
        ctl.setTitle("Add activity");
    }

    private void prepareDatePickerDialog() {
        final TextInputEditText dateEditText = findViewById(R.id.journal_date_dropdown);

        final Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.US);
        dateEditText.setText(sdf.format(calendar.getTime()));

        final DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(year, month, day, 0, 0);
                date = calendar.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.US);
                dateEditText.setText(sdf.format(calendar.getTime()));
            }

        };

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(JournalActivity.this, dateListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void prepareTimeFromPickerDialog() {
        setOnTimeClickListener(R.id.journal_time_from_dropdown);
    }

    private void prepareTimeToPickerDialog() {
        setOnTimeClickListener(R.id.journal_time_to_dropdown);
    }

    private void setOnTimeClickListener(int editTextId) {
        final TextInputEditText timeEditText = findViewById(editTextId);
        final Date time = editTextId == R.id.journal_time_from_dropdown ? timeFrom : timeTo;

        final Date timeFromRef = timeFrom;
        final Date timeToRef = timeTo;
        final TextInputEditText timeFromText = findViewById(R.id.journal_time_from_dropdown);
        final TextInputEditText timeToText = findViewById(R.id.journal_time_to_dropdown);

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        timeEditText.setText(sdf.format(time));

        final TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                time.setHours(hour);
                time.setMinutes(minute);
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);

                Log.d("Difference in millis", (timeToRef.getTime() - timeFromRef.getTime()) + "");

                timeEditText.setText(sdf.format(time));

                if (time.compareTo(timeFromRef) < 0) {
                    timeFromRef.setTime(timeToRef.getTime() - diffInMillies);
                    timeFromText.setText(sdf.format(timeFromRef));
                }
                else if (time.compareTo(timeToRef) > 0) {
                    timeToRef.setTime(time.getTime() + diffInMillies);
                    timeToText.setText(sdf.format(timeToRef));
                }
            }

        };

        timeEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(time);
                new TimePickerDialog(JournalActivity.this, timeListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        false).show();
            }

        });
    }

    private boolean isExistingActivity() {
        return getIntent().getExtras() != null;
    }

    public void editExistingActivity() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        long dailyActivityId = getIntent().getExtras().getLong("id");

        DailyActivity activity = dbManager.getDailyActivity(dailyActivityId);

        SimpleDateFormat sdf_date = new SimpleDateFormat("MMM d");
        SimpleDateFormat sdf_time = new SimpleDateFormat("h:mm a");

        name.setText(activity.getActivityTitle());
        type.setText(activity.getActivityType());
        dateText.setText(sdf_date.format(activity.getDate()));
        timeFromText.setText(sdf_time.format(activity.getStartTime()));
        timeToText.setText(sdf_time.format(activity.getEndTime()));
        notes.setText(activity.getNotes());

        timeFrom = activity.getStartTime();
        timeTo = activity.getEndTime();

        diffInMillies = Math.abs(activity.getStartTime().getTime() - activity.getEndTime().getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.journal_toolbar_items, menu);

        if (isExistingActivity() == false) {
            menu.findItem(R.id.delete_activity).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.save_activity:
                saveActivity();
                break;

            case R.id.delete_activity:
                deleteActivity();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void saveActivity() {
        DatabaseManager dbManager = DatabaseManager.getInstance();

        DailyActivity dailyActivity = new DailyActivity();
        dailyActivity.setActivityTitle(name.getText().toString());
        dailyActivity.setActivityType(type.getText().toString());
        dailyActivity.setDate(date);
        dailyActivity.setStartTime(timeFrom);
        dailyActivity.setEndTime(timeTo);
        dailyActivity.setNotes(notes.getText().toString());

        if (isExistingActivity()) {
            long dailyActivityId = getIntent().getExtras().getLong("id");
            dailyActivity.setId(dailyActivityId);
        }

        dbManager.insertOrUpdateDailyActivity(dailyActivity);

        JournalActivity.this.onBackPressed();
    }

    private void deleteActivity() {
        long dailyActivityId = getIntent().getExtras().getLong("id");
        DatabaseManager.getInstance().deleteDailyActivity(dailyActivityId);

        JournalActivity.this.onBackPressed();
    }

}
