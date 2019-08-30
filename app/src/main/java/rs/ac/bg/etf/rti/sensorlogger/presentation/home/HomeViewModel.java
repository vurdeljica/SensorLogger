package rs.ac.bg.etf.rti.sensorlogger.presentation.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CompoundButton;

import androidx.databinding.BaseObservable;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class HomeViewModel extends BaseObservable {
    public static final String IS_LISTENING_KEY = "isListeningKey";

    private Context context;

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, b) -> {
        SharedPreferences sharedPref = getDefaultSharedPreferences(context);
        sharedPref.edit().putBoolean(IS_LISTENING_KEY, b).apply();
    };

    HomeViewModel(Context context) {
        this.context = context;
    }
}
