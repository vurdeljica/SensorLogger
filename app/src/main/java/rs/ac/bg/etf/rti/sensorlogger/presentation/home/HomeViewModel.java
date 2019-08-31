package rs.ac.bg.etf.rti.sensorlogger.presentation.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CompoundButton;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class HomeViewModel extends BaseObservable {
    public static final String IS_LISTENING_KEY = "isListeningKey";

    private boolean listening;
    private Context context;

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, b) -> {
        SharedPreferences sharedPref = getDefaultSharedPreferences(context);
        sharedPref.edit().putBoolean(IS_LISTENING_KEY, b).apply();
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
