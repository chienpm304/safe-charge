package com.chienpm.safecharge;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    TextView tvVoltage, tvTemperature, tvBatteryLevel;
    AdView mAdView;

    RunInBackgroundService mService;
    Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        registerServices();

        MyUtils.updateSavedLanguage(this);

        updateUI();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(MyUtils.updateSavedLanguage(this))
            updateUI();
    }

    private void registerServices() {
        registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mService = new RunInBackgroundService(this);
        mServiceIntent = new Intent(this, mService.getClass());
        if (!isMyServiceRunning(mService.getClass())) {
            startService(mServiceIntent);
        }
    }

    private void unregisterServices() {
        if (mServiceIntent != null) {
            stopService(mServiceIntent);
            mServiceIntent = null;
        }
        if(mBatInfoReceiver != null) {
            unregisterReceiver(mBatInfoReceiver);
            mBatInfoReceiver = null;
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onResume() {
        if(MyUtils.updateSavedLanguage(this))
            updateUI();
        if (mAdView != null) {
            mAdView.resume();
        }
        registerServices();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        checkPasswordStatus();
        if(MyUtils.updateSavedLanguage(this))
            updateUI();
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_item_setting){
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }



    @Override
    protected void onDestroy() {
        unregisterServices();
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }



    private void initViews() {
        //Init Admod
        Log.d("chienpm_ads_log", "inited admod");
        MobileAds.initialize(this, getString(R.string.admob_app_id));


        Log.d("chienpm_ads_log", "start init adview");
        mAdView = (AdView)findViewById(R.id.adView);

        AdRequest adRequest = MyUtils.createAdRequest(this);
        AdListener adListener = MyUtils.createAdListener();

        mAdView.setAdListener(adListener);
        mAdView.loadAd(adRequest);


        Log.d("chienpm_ads_log", "end init adview");

        tvBatteryLevel = findViewById(R.id.battery_level);
        tvTemperature = findViewById(R.id.temperature_level);
        tvVoltage = findViewById(R.id.voltage_level);
        checkPasswordStatus();
    }

    private void updateUI() {
        tvBatteryLevel.setText(getString(R.string.battery_level, level));
        tvVoltage.setText(getString(R.string.battery_voltage, voltage));
        tvTemperature.setText(getString(R.string.battery_temperature, (int)temperature/10));
        setTitle(getString(R.string.app_name));
    }

    private void checkPasswordStatus() {
        if(MyUtils.isEmptyPassword(this)){
            Intent intent = new Intent(getApplicationContext(), LockscreenActivity.class);
            intent.putExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_SETUP_PASSWORD);
            startActivity(intent);
            Log.d("chienpm_log", "START password activity");
        }
    }
    int level, voltage, temperature;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            voltage = intent.getIntExtra("voltage", 0);
            temperature = intent.getIntExtra("temperature", 0);
            updateUI();
        }
    };
}
