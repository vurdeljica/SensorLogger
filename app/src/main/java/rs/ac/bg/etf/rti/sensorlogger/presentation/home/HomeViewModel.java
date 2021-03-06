package rs.ac.bg.etf.rti.sensorlogger.presentation.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;

import androidx.core.util.Consumer;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableBoolean;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.config.SensorLoggerApplication;
import rs.ac.bg.etf.rti.sensorlogger.network.NetworkManager;
import rs.ac.bg.etf.rti.sensorlogger.network.ServerInfo;
import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.persistency.PersistenceManager;
import rs.ac.bg.etf.rti.sensorlogger.presentation.settings.SettingsActivity;
import rs.ac.bg.etf.rti.sensorlogger.workers.TriggerWorker;

/**
 * View model of the Home fragment
 */
public class HomeViewModel extends BaseObservable {
    /**
     * Key for accessing the data collection status from the shared preferences
     */
    public static final String IS_LISTENING_KEY = "isListeningKey";
    /**
     * Tag for store data work requests
     */
    public static final String WORK_TAG = "StoreWorkTag";
    /**
     * Id of the work request that periodically triggers the data storage
     */
    private static final String PERIODIC_TASK_ID = "PeriodicTrigger";

    /**
     * Sensor data collection status
     */
    private boolean listening;
    private Context context;

    private ObservableBoolean isFabMenuOpen = new ObservableBoolean();
    private Consumer<Void> openMenu;
    private Consumer<Void> closeMenu;

    /**
     * Listener called when sensor data collection status changes
     */
    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, on) -> {
        SharedPreferences sharedPref = context.getSharedPreferences(SensorLoggerApplication.SHARED_PREFERENCES_ID, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(IS_LISTENING_KEY, on).apply();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(WORK_TAG);
        if (!on) {
            Log.d(WORK_TAG, "One time trigger worker enqueued");
            OneTimeWorkRequest storeAll = new OneTimeWorkRequest.Builder(TriggerWorker.class)
                    .addTag(WORK_TAG)
                    .build();
            workManager.enqueue(storeAll);
        } else {
            Log.d(WORK_TAG, "Periodic trigger worker enqueued");
            PeriodicWorkRequest periodicTriggerWorker = new PeriodicWorkRequest.Builder(TriggerWorker.class, 15, TimeUnit.MINUTES)
                    .addTag(WORK_TAG)
                    .setInitialDelay(15, TimeUnit.MINUTES)
                    .build();
            workManager.enqueueUniquePeriodicWork(PERIODIC_TASK_ID, ExistingPeriodicWorkPolicy.KEEP, periodicTriggerWorker);
        }
    };

    private NetworkManager networkManager;

    HomeViewModel(Context context, Consumer<Void> openMenu, Consumer<Void> closeMenu) {
        this.context = context;
        this.openMenu = openMenu;
        this.closeMenu = closeMenu;

        listening = context.getSharedPreferences(SensorLoggerApplication.SHARED_PREFERENCES_ID, Context.MODE_PRIVATE).getBoolean(IS_LISTENING_KEY, false);
        networkManager = NetworkManager.getInstance(context);
    }

    /**
     * Shows the confirmation dialog for data deletion
     */
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

    /**
     * Deletes the last four hours of data collected from all devices
     */
    private void deleteLastFourHoursOfData() {
        long timestamp = System.currentTimeMillis();
        PersistenceManager.getInstance().deleteLastFourHoursOfSensorData(timestamp);
        DatabaseManager.getInstance().deleteGPSDataBefore(timestamp);
        DatabaseManager.getInstance().deleteSensorDataBefore(timestamp);
    }

    /**
     * Shows the transfer dialog with the receiving server list that is refreshed automatically
     */
    public void showAutomaticTransferDialog() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.AlertDialogStyle)
                .setTitle(context.getString(R.string.send_alert_title)).setNegativeButton(context.getString(android.R.string.cancel),
                        (dialog, which) -> dialog.dismiss());
        ServerInfoListAdapter adapter = new ServerInfoListAdapter(context, android.R.layout.simple_list_item_single_choice, new ArrayList<>());
        materialAlertDialogBuilder.setSingleChoiceItems(adapter, -1, (dialogInterface, i) -> {
            transferDataToServer(adapter.getServerInfoAt(i));
            dialogInterface.dismiss();
        })
                .setNeutralButton(R.string.manual, (dialogInterface, i) -> {
                    showManualTransferDialog();
                    dialogInterface.dismiss();
                });

        materialAlertDialogBuilder.show();
    }

    /**
     * Shows the transfer dialog where the user can manually enter receiving server data
     */
    private void showManualTransferDialog() {
        LayoutInflater li = context.getSystemService(LayoutInflater.class);
        View v = li.inflate(R.layout.dialog_manual_server_entry, null);
        new MaterialAlertDialogBuilder(context, R.style.AlertDialogStyle)
                .setTitle(context.getString(R.string.send_alert_title))
                .setNegativeButton(context.getString(android.R.string.cancel),
                        (dialog, which) -> dialog.dismiss())
                .setNeutralButton(R.string.automatic, (dialogInterface, i) -> {
                    showAutomaticTransferDialog();
                    dialogInterface.dismiss();
                })
                .setPositiveButton(context.getString(R.string.send), (dialogInterface, i) -> {
                    String ipAddress = "";
                    int port = 8000;

                    TextInputEditText ipAddressEditText = v.findViewById(R.id.ip_address_et);
                    TextInputEditText portEditText = v.findViewById(R.id.port_et);
                    if (ipAddressEditText != null && ipAddressEditText.getText() != null) {
                        ipAddress = ipAddressEditText.getText().toString();
                    }
                    if (portEditText != null && portEditText.getText() != null) {
                        try {
                            port = Integer.valueOf(portEditText.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    transferDataToServer(new ServerInfo("", ipAddress, port, ""));
                    dialogInterface.dismiss();
                })
                .setView(v)
                .show();
    }

    /**
     * Transfers collected data to the server
     * @param serverInfo information about the receiving server
     */
    private void transferDataToServer(ServerInfo serverInfo) {
        Thread thread = new Thread(new Runnable() {
            boolean transferred = false;

            @Override
            public void run() {
                try {
                    File dirP = new File(Environment.getExternalStorageDirectory() + "/testDirectory");

                    networkManager.uploadDirectoryContentToServer(serverInfo, dirP);

                    networkManager.removeLocalServerInfo(serverInfo.getInstanceName());

                    transferred = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void openSettings() {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    /**
     * Listener that shows and hides the fab menu
     */
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
