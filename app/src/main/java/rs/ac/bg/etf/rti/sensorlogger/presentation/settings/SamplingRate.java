package rs.ac.bg.etf.rti.sensorlogger.presentation.settings;

/**
 * Enum representing the sampling rate of the application
 */
public enum SamplingRate {

    VERY_HIGH("50 Hz", 20000),
    HIGH("30 Hz", 33333),
    MEDIUM("10 Hz", 100000),
    LOW("5 Hz", 200000);

    private final String title;
    private final int rate;

    SamplingRate(String title, int rate) {
        this.title = title;
        this.rate = rate;
    }

    public String getTitle() {
        return title;
    }

    public int getRate() {
        return rate;
    }

    public static SamplingRate fromRate(int rate) {
        switch (rate) {
            case 200000:
                return VERY_HIGH;
            case 100000:
                return HIGH;
            case 33333:
                return MEDIUM;
            default:
                return LOW;
        }
    }
}
