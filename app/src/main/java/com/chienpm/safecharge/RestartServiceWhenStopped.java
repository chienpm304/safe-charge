package com.chienpm.safecharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartServiceWhenStopped extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, RunInBackgroundService.class));
        Log.d("chienpm_log_tag: ", "The service is restart by using broadcast receiver");
    }
}
