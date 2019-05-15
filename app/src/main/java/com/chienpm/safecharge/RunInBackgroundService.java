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

    private static final int NOTIFICATION_ID = 304;
    private static final String NOTIFICATIONS_CHANNEL = "NOTIFICATON_CHANNEL_NAME_IS_SAFE_CHARGE";
    Context mContext;
    private final String NOTIFICATION_CHANNEL_ID = "channel_id";
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
    private NotificationUtils mNotificationUtils;

    @Override
    public void onCreate() {
        mNotificationUtils = new NotificationUtils(this);

        Log.d("chienpm_log_tag", "Service created, Im going call startForeground");

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(this.mBatInfoReceiver, intentFilter);

        super.onCreate();
    }

    private void startForegroundService() {
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
