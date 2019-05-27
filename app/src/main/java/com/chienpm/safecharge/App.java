package com.chienpm.safecharge;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.squareup.leakcanary.LeakCanary;

public class App extends Application {
    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        MobileAds.initialize(this, this.getString(R.string.admob_app_id));
    }

}
