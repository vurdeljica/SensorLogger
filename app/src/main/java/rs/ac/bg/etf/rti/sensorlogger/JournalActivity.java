package rs.ac.bg.etf.rti.sensorlogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;

public class JournalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        prepareCollapsingToolbar();

        prepareDatePickerDialog();

        prepareTimeFromPickerDialog();
        prepareTimeToPickerDialog();
    }

    private void prepareCollapsingToolbar() {
        Toolbar toolbar = findViewById(R.id.journal_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        setOnTimeClickListener(R.id.journal_time_from_dropdown);
    }

    private void prepareTimeToPickerDialog() {
        setOnTimeClickListener(R.id.journal_time_to_dropdown);
    }

    private void setOnTimeClickListener(int editTextId) {
        final TextInputEditText timeEditText = findViewById(editTextId);

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
}
