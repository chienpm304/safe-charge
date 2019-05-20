package com.chienpm.safecharge;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;

public class SafeChargingApplication extends Application {
    Intent mServiceIntent = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("chienpm_log", "I am ApplicationClass, I was created firstly");
        // initialize the AdMob appy
//        MobileAds.initialize(this, getString(R.string.admob_app_id));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("chienpm_log", "onTerminate called");
        if(mServiceIntent != null)
            stopService(mServiceIntent);
    }
}
