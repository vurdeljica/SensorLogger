package rs.ac.bg.etf.rti.sensorlogger.presentation.journalEntry;

/**
 * Handler interface for responding to journal entry validation and status change
 */
public interface JournalEntryHandler {
    /**
     * Called when a journal entry is saved to the database
     */
    void onJournalEntrySaved();

    /**
     * Called when a journal entry is deleted from the database
     */
    void onJournalEntryDeleted();

    /**
     * Called when the journal entry validation fails
     */
    void onActivityTypeValidationFailed();

    /**
     * Called when the validation errors are cleared
     */
    void clearErrors();
}
