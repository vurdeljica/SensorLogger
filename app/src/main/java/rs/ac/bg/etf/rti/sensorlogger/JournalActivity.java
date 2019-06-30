package rs.ac.bg.etf.rti.sensorlogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import rs.ac.bg.etf.rti.sensorlogger.beans.DailyActivity;

public class JournalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        prepareCollapsingToolbar();

        prepareDatePickerDialog();

        prepareTimeFromPickerDialog();
        prepareTimeToPickerDialog();

        if (isExistingActivity()) {
            editExistingActivity();
        }
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
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        Time time = new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);

        setOnTimeClickListener(R.id.journal_time_from_dropdown, time);
    }

    private void prepareTimeToPickerDialog() {
        Calendar calendar = Calendar.getInstance();
        Time time = new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);

        setOnTimeClickListener(R.id.journal_time_to_dropdown, time);
    }

    private void setOnTimeClickListener(int editTextId, Time initTime) {
        final TextInputEditText timeEditText = findViewById(editTextId);

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        timeEditText.setText(sdf.format(initTime));

        final TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Time time = new Time(hour, minute, 0);
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);

                timeEditText.setText(sdf.format(time));
            }

        };

        timeEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
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
        int dailyActivityPosition = getIntent().getExtras().getInt("position");

        DailyActivity activity = new DailyActivity("Nocno trcanje",
                "Hight intensity", "Jun 28", "5:37 PM",
                "6:37 PM", "No notes");


        TextInputEditText name = findViewById(R.id.journal_activity_name);
        AutoCompleteTextView type = findViewById(R.id.journal_acitivty_type);
        TextInputEditText date = findViewById(R.id.journal_date_dropdown);
        TextInputEditText timeFrom = findViewById(R.id.journal_time_from_dropdown);
        TextInputEditText timeTo = findViewById(R.id.journal_time_to_dropdown);
        TextInputEditText notes = findViewById(R.id.journal_notes);

        name.setText(activity.getActivityTitle());
        type.setText(activity.getActivityType());
        date.setText(activity.getDate());
        timeFrom.setText(activity.getStartTime());
        timeTo.setText(activity.getEndTime());
        notes.setText(activity.getNotes());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.journal_toolbar_items, menu);
        setSaveDailyActivityCallback(menu);
        return true;
    }

    private void setSaveDailyActivityCallback(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.save_activity_text);
        AppCompatTextView save_text = (AppCompatTextView) searchItem.getActionView();
        save_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExistingActivity()) {
                    int position = getIntent().getExtras().getInt("position");
                    updateDailyActivity(position);
                }
                else {
                    addNewDailyActivity();
                }

                JournalActivity.this.onBackPressed();
            }
        });
    }

    private void updateDailyActivity(int position) {

    }

    private void addNewDailyActivity() {

    }

    private void deleteActivity() {

    }

}
