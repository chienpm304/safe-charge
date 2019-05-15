package com.chienpm.safecharge;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ChargingDetectorService extends JobService {
    public ChargingDetectorService() {

    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("chienpm_log_tag", "job stated");
        Intent intent = new Intent(this, CharingActivity.class);
        intent.putExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_UNLOCK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        jobFinished(jobParameters, true);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
            Log.d("chienpm_log_tag", "job stopped");
//            Intent intent = new Intent(this, LockscreenActivity.class);
//            intent.putExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_UNLOCK);
//            startActivity(intent);

        return false;
    }
}
