package com.chienpm.safecharge;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

public class RunInBackgroundService extends Service {
    Context mContext;
    public RunInBackgroundService(Context context) {
        mContext = context;
    }

    public RunInBackgroundService(){
        //default contructor for system call
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private static IntentFilter intentFilter;

    @Override
    public void onCreate() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(this.mBatInfoReceiver, intentFilter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("chienpm_log_tag", "Service was killed");
        super.onDestroy();

        Intent broadcastIntent = new Intent(this, RestartServiceWhenStopped.class);
        sendBroadcast(broadcastIntent);

        unregisterReceiver(mBatInfoReceiver);

    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//            int voltage = intent.getIntExtra("voltage", 0);
//            int temperature = intent.getIntExtra("temperature", 0);
//            double temps = (double)temperature / 10;

            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                Log.d("chienpm_log_tag", "Power connected!");
//                Intent intent1 = new Intent(getApplicationContext(), CharingActivity.class);
//                startActivity(intent1);
            }
            else if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                Log.d("chienpm_log_tag", "Power disconnected!");
                Intent intent2 = new Intent(getApplicationContext(), LockscreenActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_UNLOCK);
                startActivity(intent2);
            }
        }
    };
}
