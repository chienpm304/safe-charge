package com.chienpm.safecharge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

public class RunInBackgroundService extends Service {
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    Context mContext;

    public RunInBackgroundService(Context context) {
        mContext = context;
    }

    public RunInBackgroundService(){
        //default contructor for system call
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private static IntentFilter intentFilter;
    private NotificationUtils mNotificationUtils = null;

    @Override
    public void onCreate() {


        Log.d("chienpm_log_tag", "Service created, Im going call startForeground");

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(this.mBatInfoReceiver, intentFilter);

        super.onCreate();
    }

    private void startForegroundService() {
        if(mNotificationUtils == null)
            mNotificationUtils = new NotificationUtils(this);

        Notification.Builder nb = mNotificationUtils.
                getAndroidChannelNotification("Waiting for charging", "By " + "Chienpm");

        mNotificationUtils.getManager().notify(101, nb.build());
        startForeground(101, nb.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if(TextUtils.equals(action, ACTION_START_FOREGROUND_SERVICE))
            startForegroundService();

        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.d("chienpm_log_tag", "Service was killed");

        super.onDestroy();

        Intent broadcastIntent = new Intent(this, RestartServiceWhenStoppedReceiver.class);
        sendBroadcast(broadcastIntent);

        unregisterReceiver(mBatInfoReceiver);
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                Log.d("chienpm_log_tag", "Power connected!");
                Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
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
