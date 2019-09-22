package rs.ac.bg.etf.rti.sensorlogger.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import rs.ac.bg.etf.rti.sensorlogger.presentation.home.HomeViewModel;

public class PeriodicTriggerWorker extends Worker {

    private Context context;

    public PeriodicTriggerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        WorkManager workManager = WorkManager.getInstance(context);
        OneTimeWorkRequest storeLocationWorkRequest = new OneTimeWorkRequest.Builder(StoreLocationInFileWorker.class)
                .addTag(HomeViewModel.WORK_TAG)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
        workManager.enqueue(storeLocationWorkRequest);

        OneTimeWorkRequest storeSensorDataWorkRequest = new OneTimeWorkRequest.Builder(StoreSensorDataInFileWorker.class)
                .addTag(HomeViewModel.WORK_TAG)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build();
        workManager.enqueue(storeSensorDataWorkRequest);
        return Result.success();
    }

}
