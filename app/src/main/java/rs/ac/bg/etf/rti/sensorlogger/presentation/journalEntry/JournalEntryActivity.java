package rs.ac.bg.etf.rti.sensorlogger.presentation.journalEntry;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.databinding.ActivityJournalEntryBinding;
import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;

/**
 * Activity for preview and editing of journal entries - daily activities
 */
public class JournalEntryActivity extends AppCompatActivity implements JournalEntryHandler {

    private JournalEntryViewModel journalEntryViewModel;
    private ActivityJournalEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_journal_entry);

        setupToolbar();

        DailyActivity activity;
        DatabaseManager dbManager = DatabaseManager.getInstance();
        if (getIntent().getExtras() != null) {
            long dailyActivityId = getIntent().getExtras().getLong("id");
            activity = dbManager.getDailyActivity(dailyActivityId);
            String dailyActivityTitle = activity.getActivityTitle();
            setTitle(dailyActivityTitle);
        } else {
            activity = new DailyActivity();
            setTitle(R.string.new_activity);
        }

        journalEntryViewModel = new JournalEntryViewModel(this, activity);
        binding.setVm(journalEntryViewModel);

        binding.journalActivityType.setAdapter(journalEntryViewModel.getIntensityAdapter(this));
    }

    private void setupToolbar() {
        setSupportActionBar(binding.journalToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private boolean isExistingActivity() {
        return journalEntryViewModel.getJournalEntry().getId() != -1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.journal_toolbar_items, menu);

        if (!isExistingActivity()) {
            menu.findItem(R.id.delete_activity).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.save_activity:
                journalEntryViewModel.saveJournalEntry();
                break;

            case R.id.delete_activity:
                journalEntryViewModel.deleteJournalEntry();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onJournalEntrySaved() {
        onNavigateUp();
    }

    @Override
    public void onJournalEntryDeleted() {
        onNavigateUp();
    }

    @Override
    public void onActivityTypeValidationFailed() {
        binding.journalActivityTypeWrapper.setError("Activity must have a type");
    }

    @Override
    public void clearErrors() {
        binding.journalActivityTypeWrapper.setError(null);
    }
}
