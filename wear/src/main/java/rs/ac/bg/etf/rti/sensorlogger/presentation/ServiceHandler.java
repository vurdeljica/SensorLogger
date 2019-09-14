package rs.ac.bg.etf.rti.sensorlogger.presentation;

import android.content.Intent;
import android.content.ServiceConnection;

public interface ServiceHandler {
    void bindSensorService(Intent intent, ServiceConnection serviceConnection);
    void unbindSensorService(ServiceConnection serviceConnection);
}
