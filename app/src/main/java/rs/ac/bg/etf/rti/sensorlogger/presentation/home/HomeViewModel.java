package rs.ac.bg.etf.rti.sensorlogger.presentation.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CompoundButton;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import rs.ac.bg.etf.rti.sensorlogger.workers.StoreFileWorker;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class HomeViewModel extends BaseObservable {
    public static final String IS_LISTENING_KEY = "isListeningKey";

    private boolean listening;
    private Context context;

    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, on) -> {
        SharedPreferences sharedPref = getDefaultSharedPreferences(context);
        sharedPref.edit().putBoolean(IS_LISTENING_KEY, on).apply();
        if (!on) {
            Constraints constraints = new Constraints.Builder().setRequiresStorageNotLow(true).build();
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(StoreFileWorker.class)
                    .setConstraints(constraints)
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);
        }
    };

    HomeViewModel(Context context) {
        this.context = context;
        listening = getDefaultSharedPreferences(context).getBoolean(IS_LISTENING_KEY, false);
    }

    @Bindable
    public boolean isListening() {
        return listening;
    }

    @Bindable
    public void setListening(boolean listening) {
        this.listening = listening;
    }

}
