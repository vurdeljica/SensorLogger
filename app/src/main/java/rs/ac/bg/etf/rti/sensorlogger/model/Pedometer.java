package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;

public class Pedometer extends RealmObject {

    private int stepCount;

    public Pedometer() {
    }

    public Pedometer(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getStepCount() {
        return stepCount;
    }

}
