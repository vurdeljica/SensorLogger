package rs.ac.bg.etf.rti.sensorlogger.listeners;

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

import rs.ac.bg.etf.rti.sensorlogger.R;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class AccelerometerEventListener implements SensorEventListener {
    private final static String TAG = "Accelerometer";
    private final static String PATH = "/accelerometer";
    private final static String DATA_KEY = "rs.ac.bg.etf.rti.sensorlogger.accelerometer.data";
    private final static String TIMESTAMP_KEY = "rs.ac.bg.etf.rti.sensorlogger.accelerometer.timestamp";
    private static final String NODE_KEY = "rs.ac.bg.etf.rti.sensorlogger.accelerometer.node";
    private static final String NODE_ID_KEY = "nodeId";

    private Context context;

    public AccelerometerEventListener(Context context) {
        this.context = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, prettyPrintFloatArray(event.values));
        String nodeId = getDefaultSharedPreferences(context).getString(NODE_ID_KEY, context.getString(R.string.unknown));
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putFloatArray(DATA_KEY, event.values);
        putDataMapReq.getDataMap().putLong(TIMESTAMP_KEY, event.timestamp);
        putDataMapReq.getDataMap().putString(NODE_KEY, nodeId);
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