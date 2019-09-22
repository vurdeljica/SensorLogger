package rs.ac.bg.etf.rti.sensorlogger.presentation.home;

import android.content.Context;
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
import rs.ac.bg.etf.rti.sensorlogger.workers.PeriodicTriggerWorker;
import rs.ac.bg.etf.rti.sensorlogger.workers.StoreLocationInFileWorker;
import rs.ac.bg.etf.rti.sensorlogger.workers.StoreSensorDataInFileWorker;

public class HomeViewModel extends BaseObservable {
    public static final String IS_LISTENING_KEY = "isListeningKey";
    public static final String WORK_TAG = "StoreWorkTag";
    private static final String PERIODIC_TASK_ID = "PeriodicTrigger";

    private boolean listening;
    private Context context;
    private ObservableBoolean isFabMenuOpen = new ObservableBoolean();
    private Consumer<Void> openMenu;
    private Consumer<Void> closeMenu;

    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, on) -> {
        SharedPreferences sharedPref = context.getSharedPreferences(SensorLoggerApplication.SHARED_PREFERENCES_ID, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(IS_LISTENING_KEY, on).apply();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(WORK_TAG);
        if (!on) {
            OneTimeWorkRequest storeLocationWorkRequest = new OneTimeWorkRequest.Builder(StoreLocationInFileWorker.class)
                    .addTag(WORK_TAG)
                    .build();
            workManager.enqueue(storeLocationWorkRequest);
            OneTimeWorkRequest storeSensorDataWorkRequest = new OneTimeWorkRequest.Builder(StoreSensorDataInFileWorker.class)
                    .addTag(WORK_TAG)
                    .build();
            workManager.enqueue(storeSensorDataWorkRequest);
        } else {
            PeriodicWorkRequest periodicTriggerWorker = new PeriodicWorkRequest.Builder(PeriodicTriggerWorker.class, 15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
                    .addTag(WORK_TAG)
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
        DatabaseManager.getInstance().deleteSensorDataBefore(timestamp);
    }

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

    private void transferDataToServer(ServerInfo serverInfo) {
        Thread thread = new Thread(new Runnable() {
            boolean transferred = false;

            @Override
            public void run() {
                try {
                    PersistenceManager.getInstance().saveDailyActivity();

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
