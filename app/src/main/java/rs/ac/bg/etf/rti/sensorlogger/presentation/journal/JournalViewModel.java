package rs.ac.bg.etf.rti.sensorlogger.presentation.journal;

import android.database.DataSetObserver;

import androidx.databinding.BaseObservable;

import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;

/**
 * View model for the journal fragment
 */
public class JournalViewModel extends BaseObservable {

    private JournalListAdapter journalListAdapter;

    JournalViewModel() {
        journalListAdapter = new JournalListAdapter();
        journalListAdapter.registerDataSetObserver(getDataSetObserver());
    }


    private DataSetObserver getDataSetObserver() {
        return new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                notifyChange();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                notifyChange();
            }
        };
    }

    /**
     * Reloads the journal entries list
     */
    void inflateJournalListAdapter() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        journalListAdapter.clear();
        journalListAdapter.addAll(dbManager.getDailyActivities());
        journalListAdapter.notifyDataSetChanged();
        notifyChange();
    }

    JournalListAdapter getJournalListAdapter() {
        return journalListAdapter;
    }
}
