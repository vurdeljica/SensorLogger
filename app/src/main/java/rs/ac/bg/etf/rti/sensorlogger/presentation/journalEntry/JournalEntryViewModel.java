package rs.ac.bg.etf.rti.sensorlogger.presentation.journalEntry;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.adapters.TextViewBindingAdapter;

import java.util.Calendar;

import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;

/**
 * View model fo the Journal entry activity
 */
public class JournalEntryViewModel extends BaseObservable {

    /**
     * Displayed daily activity
     */
    private DailyActivity journalEntry;
    private JournalEntryHandler handler;
    /**
     * Default daily activity duration
     */
    private long diff = 60L;
    /**
     * List of daily activity types
     */
    private String[] intensities = new String[]{"Low intensity", "Medium Intensity", "High intensity"};

    JournalEntryViewModel(JournalEntryHandler handler, @NonNull DailyActivity journalEntry) {
        this.journalEntry = journalEntry;
        this.handler = handler;
    }

    public DailyActivity getJournalEntry() {
        return journalEntry;
    }

    /**
     * @return listener that gets called when text is changed in a text field
     */
    public TextViewBindingAdapter.OnTextChanged getOnTextChangedListener() {
        return (s, start, before, count) -> handler.clearErrors();
    }

    /**
     * @return listener that gets called when the date dialog should be displayed
     */
    public View.OnClickListener getDateOnClickListener() {
        final Calendar calendar = Calendar.getInstance();
        return v -> new DatePickerDialog(v.getContext(), JournalEntryViewModel.this.getOnDateSetListener(), calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * @return Listener that gets called when the date has changed
     */
    private DatePickerDialog.OnDateSetListener getOnDateSetListener() {
        final Calendar calendar = Calendar.getInstance();
        return (view, year, month, day) -> {
            calendar.set(year, month, day, 0, 0);
            journalEntry.setDate(calendar.getTime());
            JournalEntryViewModel.this.notifyChange();
        };
    }

    /**
     * @return Listener that gets called when the time dialog should be displayed
     */
    public View.OnClickListener getTimeOnClickListener(final boolean isStartTime) {
        return v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(isStartTime ? journalEntry.getStartTime() : journalEntry.getEndTime());
            new TimePickerDialog(v.getContext(), JournalEntryViewModel.this.getOnTimeSetListener(isStartTime),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                    true).show();
        };
    }

    /**
     * @return Listener that gets called when the time has changed
     */
    private TimePickerDialog.OnTimeSetListener getOnTimeSetListener(final boolean isStartTime) {
        return (timePicker, hour, minute) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(0, 0, 0, hour, minute);
            if (isStartTime) {
                journalEntry.setStartTime(calendar.getTime());
                calendar.add(Calendar.MINUTE, (int) diff);
                journalEntry.setEndTime(calendar.getTime());
            } else {
                journalEntry.setEndTime(calendar.getTime());
                if (journalEntry.getStartTime().after(journalEntry.getEndTime())) {
                    calendar.add(Calendar.MINUTE, (int) -diff);
                    journalEntry.setStartTime(calendar.getTime());
                } else {
                    diff = ((journalEntry.getEndTime().getTime() - journalEntry.getStartTime().getTime()) / (1000L * 60L));
                }
            }
            JournalEntryViewModel.this.notifyChange();
        };
    }

    /**
     * Saves the displayed daily activity to the database
     */
    void saveJournalEntry() {
        if (activityValidation()) {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.insertOrUpdateDailyActivity(journalEntry);
            handler.onJournalEntrySaved();
        }
    }

    /**
     * Validates the displayed daily activity - checks if it contains a valid activity type
     * @return true if the validation has passed, otherwise returns false
     */
    private boolean activityValidation() {
        boolean typeValid = journalEntry.getActivityType() != null && !journalEntry.getActivityType().isEmpty();
        if (!typeValid)
            handler.onActivityTypeValidationFailed();
        return typeValid;
    }

    /**
     * Deletes the displayed daily activity
     */
    void deleteJournalEntry() {
        DatabaseManager.getInstance().deleteDailyActivity(journalEntry.getId());
        handler.onJournalEntryDeleted();
    }

    /**
     * @param context to be used for initialising the adapter
     * @return Adapter for the activity type list
     */
    ArrayAdapter<String> getIntensityAdapter(Context context) {
        return new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                intensities);
    }
}
