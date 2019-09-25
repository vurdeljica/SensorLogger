package rs.ac.bg.etf.rti.sensorlogger.presentation.journal;

import rs.ac.bg.etf.rti.sensorlogger.model.DailyActivity;

/**
 * View model of a journal entries list item
 */
public class JournalListItemViewModel {
    private DailyActivity journalEntry;

    public JournalListItemViewModel(DailyActivity journalEntry) {
        this.journalEntry = journalEntry;
    }

    public DailyActivity getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(DailyActivity journalEntry) {
        this.journalEntry = journalEntry;
    }
}
