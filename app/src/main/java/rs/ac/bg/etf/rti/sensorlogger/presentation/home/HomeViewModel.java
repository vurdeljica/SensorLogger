package rs.ac.bg.etf.rti.sensorlogger.presentation.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.View;
import android.widget.CompoundButton;

import androidx.core.util.Consumer;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableBoolean;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.network.NetworkManager;
import rs.ac.bg.etf.rti.sensorlogger.network.ServerInfo;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.persistency.PersistenceManager;
import rs.ac.bg.etf.rti.sensorlogger.workers.StoreFileWorker;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class HomeViewModel extends BaseObservable {
    public static final String IS_LISTENING_KEY = "isListeningKey";

    private boolean listening;
    private Context context;
    private ObservableBoolean isFabMenuOpen = new ObservableBoolean();
    private Consumer<Void> openMenu;
    private Consumer<Void> closeMenu;

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
    private NetworkManager networkManager;

    HomeViewModel(Context context, Consumer<Void> openMenu, Consumer<Void> closeMenu) {
        this.context = context;
        this.openMenu = openMenu;
        this.closeMenu = closeMenu;

        listening = getDefaultSharedPreferences(context).getBoolean(IS_LISTENING_KEY, false);
        networkManager = NetworkManager.getInstance(context);
    }

    public void showDeletionConfirmationDialog() {
        new MaterialAlertDialogBuilder(context, R.style.AlertDialogStyle)
                .setTitle(context.getString(R.string.delete_alert_title))
                .setMessage(context.getString(R.string.delete_alert_text))
                .setPositiveButton(context.getString(R.string.ok),
                        (dialog, which) -> {
                            deleteLastFourHoursOfData();
                            dialog.dismiss();
                        })
                .setNegativeButton(context.getString(android.R.string.cancel),
                        (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteLastFourHoursOfData() {
        long timestamp = System.currentTimeMillis();
        PersistenceManager.getInstance().deleteLastFourHoursOfSensorData(timestamp);
        DatabaseManager.getInstance().deleteGPSDataBefore(timestamp);
    }

    public void showTransferDialog() {
        List<ServerInfo> sInfoList = networkManager.getServersInformation();
        CharSequence[] serverIpAddresses;
        if (sInfoList.size() > 0) {
            serverIpAddresses = new CharSequence[sInfoList.size()];
            for (int i = 0; i < sInfoList.size(); i++) {
                serverIpAddresses[i] = sInfoList.get(i).getIpAddress();
            }
        } else {
            serverIpAddresses = new CharSequence[0];
        }
        int selectedIndex = -1;
        new MaterialAlertDialogBuilder(context, R.style.AlertDialogStyle)
                .setTitle(context.getString(R.string.send_alert_title))
                .setSingleChoiceItems(serverIpAddresses, selectedIndex, (dialogInterface, i) -> {
                    if (sInfoList.size() > 0) {
                        transferDataToServer(sInfoList.get(i));
                        dialogInterface.dismiss();
                    }
                })
                .setMessage(sInfoList.size() > 0 ? 0 : R.string.no_servers)
                .setNegativeButton(context.getString(android.R.string.cancel),
                        (dialog, which) -> dialog.dismiss())
                .setNeutralButton(R.string.refresh, (dialogInterface, i) -> showTransferDialog())
                .show();
    }

    private void transferDataToServer(ServerInfo serverInfo) {
        Thread thread = new Thread(new Runnable() {
            boolean transferred = false;

            @Override
            public void run() {
                try {
                    PersistenceManager.getInstance().saveDailyActivity();

                    File dirP = new File(Environment.getExternalStorageDirectory() + "/testDirectory");

                    networkManager.uploadDirectoryContentToServer(serverInfo, dirP);

                    transferred = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public View.OnClickListener menuFabOnClickListener = view -> {
        if (isFabMenuOpen.get()) {
            openMenu.accept(null);
            isFabMenuOpen.set(false);
        } else {
            closeMenu.accept(null);
            isFabMenuOpen.set(true);
        }
    };

    @Bindable
    public boolean isListening() {
        return listening;
    }

    @Bindable
    public void setListening(boolean listening) {
        this.listening = listening;
    }

    @Bindable
    public ObservableBoolean getIsFabMenuOpen() {
        return isFabMenuOpen;
    }

}
