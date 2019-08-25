package rs.ac.bg.etf.rti.sensorlogger.presentation.journalEntry;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.adapters.TextViewBindingAdapter;

import java.util.Calendar;

import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;

public class JournalEntryViewModel extends BaseObservable {

    private DailyActivity journalEntry;
    private JournalEntryHandler handler;

    public JournalEntryViewModel(JournalEntryHandler handler, @NonNull DailyActivity journalEntry) {
        this.journalEntry = journalEntry;
        this.handler = handler;
    }

    public DailyActivity getJournalEntry() {
        return journalEntry;
    }

    public TextViewBindingAdapter.OnTextChanged getOnTextChangedListener() {
        return new TextViewBindingAdapter.OnTextChanged() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.clearErrors();
            }
        };
    }

    public View.OnClickListener getDateOnClickListener() {
        final Calendar calendar = Calendar.getInstance();
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(v.getContext(), JournalEntryViewModel.this.getOnDateSetListener(), calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        };
    }

    private DatePickerDialog.OnDateSetListener getOnDateSetListener() {
        final Calendar calendar = Calendar.getInstance();
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(year, month, day, 0, 0);
                journalEntry.setDate(calendar.getTime());
                JournalEntryViewModel.this.notifyChange();
            }
        };
    }

    public View.OnClickListener getTimeOnClickListener(final boolean isStartTime) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(isStartTime ? journalEntry.getStartTime() : journalEntry.getEndTime());
                new TimePickerDialog(v.getContext(), JournalEntryViewModel.this.getOnTimeSetListener(isStartTime),
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        true).show();
            }
        };
    }

    private TimePickerDialog.OnTimeSetListener getOnTimeSetListener(final boolean isStartTime) {
        return new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(0, 0, 0, hour, minute);
                if (isStartTime) {
                    journalEntry.setStartTime(calendar.getTime());
                } else {
                    journalEntry.setEndTime(calendar.getTime());
                }
                JournalEntryViewModel.this.notifyChange();
            }
        };
    }

    public void saveJournalEntry() {
        if (activityValidation()) {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.insertOrUpdateDailyActivity(journalEntry);
            handler.onJournalEntrySaved();
        }
    }

    private boolean activityValidation() {
        boolean timeValid = journalEntry.getStartTime().before(journalEntry.getEndTime());
        boolean typeValid = journalEntry.getActivityType() != null && !journalEntry.getActivityType().isEmpty();
        if (!timeValid)
            handler.onEndTimeValidationFailed();
        if (!typeValid)
            handler.onActivityTypeValidationFailed();
        return timeValid && typeValid;
    }

    public void deleteJournalEntry() {
        DatabaseManager.getInstance().deleteDailyActivity(journalEntry.getId());
        handler.onJournalEntryDeleted();
    }
}
