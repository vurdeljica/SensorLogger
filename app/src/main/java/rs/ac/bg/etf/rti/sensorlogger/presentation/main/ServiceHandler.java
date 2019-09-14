package rs.ac.bg.etf.rti.sensorlogger.presentation.main;

import android.content.Intent;
import android.content.ServiceConnection;

public interface ServiceHandler {
    void bind(Intent intent, ServiceConnection mLocationServiceConnection);
    void unbind(ServiceConnection mLocationServiceConnection);
}
