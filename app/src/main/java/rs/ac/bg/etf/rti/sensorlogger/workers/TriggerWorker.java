package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rs.ac.bg.etf.rti.sensorlogger.persistency.DatabaseManager;
import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel;

public class TriggerWorker extends Worker {

    private Context context;

    public TriggerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(HomeViewModel.WORK_TAG, "Trigger worker started");
        WorkManager workManager = WorkManager.getInstance(context);
        OneTimeWorkRequest storeLocationWorkRequest = new OneTimeWorkRequest.Builder(StoreLocationInFileWorker.class)
                .addTag(HomeViewModel.WORK_TAG)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
        workManager.enqueue(storeLocationWorkRequest);

        DatabaseManager dbManager = DatabaseManager.getInstance();
        List<String> nodeIds = dbManager.getNodeIds();
        for (String nodeId : nodeIds) {
            Data.Builder dataBuilder = new Data.Builder();
            dataBuilder.putString("nodeId", nodeId);
            OneTimeWorkRequest storeSensorDataWorkRequest = new OneTimeWorkRequest.Builder(StoreSensorDataInFileWorker.class)
                    .addTag(HomeViewModel.WORK_TAG)
                    .setInitialDelay(10, TimeUnit.SECONDS)
                    .setInputData(dataBuilder.build())
                    .build();

            workManager.enqueue(storeSensorDataWorkRequest);

        }

        return Result.success();
    }

}
