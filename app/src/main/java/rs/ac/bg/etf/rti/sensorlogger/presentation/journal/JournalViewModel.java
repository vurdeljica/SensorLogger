package rs.ac.bg.etf.rti.sensorlogger.presentation.journal;

import android.database.DataSetObserver;

import androidx.databinding.BaseObservable;

import rs.ac.bg.etf.rti.sensorlogger.database.DatabaseManager;

public class JournalViewModel extends BaseObservable {

    private JournalListAdapter journalListAdapter;

    public JournalViewModel() {
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

    public void inflateJournalListAdapter() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        journalListAdapter.clear();
        journalListAdapter.addAll(dbManager.getDailyActivities());
        journalListAdapter.notifyDataSetChanged();
        notifyChange();
    }

    public JournalListAdapter getJournalListAdapter() {
        return journalListAdapter;
    }
}
