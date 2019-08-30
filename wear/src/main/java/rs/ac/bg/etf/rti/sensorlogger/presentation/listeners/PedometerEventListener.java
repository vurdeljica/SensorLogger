package rs.ac.bg.etf.rti.sensorlogger.presentation.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class PedometerEventListener implements SensorEventListener {
    private final static String TAG = "Pedometer";
    private final static String PATH = "/steps";
    private final static String DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.steps.data";
    private final static String TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.steps.timestamp";

    private Context context;

    public PedometerEventListener(Context context) {
        this.context = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, prettyPrintFloatArray(event.values));
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putFloatArray(DATA_KEY, event.values);
        putDataMapReq.getDataMap().putLong(TIMESTAMP_KEY, event.timestamp);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(putDataReq);
        dataItemTask.addOnFailureListener(e -> Log.e(TAG, "Failed to send data"));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String prettyPrintFloatArray(float[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i < values.length - 1) {
                sb.append("|");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
