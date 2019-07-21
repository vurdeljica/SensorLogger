package rs.ac.bg.etf.rti.sensorlogger.presentation.journalEntry;

public interface JournalEntryHandler {
    void onJournalEntrySaved();
    void onJournalEntryDeleted();
    void onEndTimeValidationFailed();
    void onActivityTypeValidationFailed();
    void clearErrors();
}
