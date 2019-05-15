package com.chienpm.safecharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class RestartServiceWhenStopped extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, RunInBackgroundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceIntent.setAction(RunInBackgroundService.ACTION_START_FOREGROUND_SERVICE);
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        Log.d("chienpm_log_tag: ", "The service is restart by using broadcast receiver");
    }
}
