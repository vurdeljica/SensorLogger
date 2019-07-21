package rs.ac.bg.etf.rti.sensorlogger.presentation.journalEntry;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.adapters.TextViewBindingAdapter;

import java.util.Calendar;

import rs.ac.bg.etf.rti.sensorlogger.database.DatabaseManager;
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
        return (s, start, before, count) -> handler.clearErrors();
    }

    public View.OnClickListener getDateOnClickListener() {
        Calendar calendar = Calendar.getInstance();
        return v -> new DatePickerDialog(v.getContext(), getOnDateSetListener(), calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private DatePickerDialog.OnDateSetListener getOnDateSetListener() {
        final Calendar calendar = Calendar.getInstance();
        return (view, year, month, day) -> {
            calendar.set(year, month, day, 0, 0);
            journalEntry.setDate(calendar.getTime());
            notifyChange();
        };
    }

    public View.OnClickListener getTimeOnClickListener(boolean isStartTime) {
        return v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(isStartTime ? journalEntry.getStartTime() : journalEntry.getEndTime());
            new TimePickerDialog(v.getContext(), getOnTimeSetListener(isStartTime),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                    true).show();
        };
    }

    private TimePickerDialog.OnTimeSetListener getOnTimeSetListener(boolean isStartTime) {
        return (timePicker, hour, minute) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(0, 0, 0, hour, minute);
            if (isStartTime) {
                journalEntry.setStartTime(calendar.getTime());
            } else {
                journalEntry.setEndTime(calendar.getTime());
            }
            notifyChange();
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
