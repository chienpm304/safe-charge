package com.chienpm.safecharge;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnSetupPassword;
    TextView tvVoltage, tvTemperature, tvBatteryLevel;


    RunInBackgroundService mService;
    Intent mServiceIntent;

    //Todo: ReDefine layout to fit all kind of device

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        mService = new RunInBackgroundService(this);
        mServiceIntent = new Intent(this, mService.getClass());


        if (!isMyServiceRunning(mService.getClass())) {
            startService(mServiceIntent);
        }

        MyUtils.updateSavedLanguage(this);
        updateUiAdaptedToLanguage();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MyUtils.updateSavedLanguage(this);
        updateUiAdaptedToLanguage();
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
        MyUtils.updateSavedLanguage(this);
        updateUiAdaptedToLanguage();
        checkPasswordStatus();
        super.onResume();
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
    protected void onDestroy() {
        stopService(mServiceIntent);
        unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
    }



    private void initViews() {
        tvBatteryLevel = findViewById(R.id.battery_level);
        tvTemperature = findViewById(R.id.temperature_level);
        tvVoltage = findViewById(R.id.voltage_level);
        checkPasswordStatus();
    }

    private void updateUiAdaptedToLanguage() {
        tvBatteryLevel.setText(R.string.battery_level);
        tvTemperature.setText(R.string.battery_temperature);
        tvVoltage.setText(R.string.battery_voltage);
        setTitle(getString(R.string.app_name));
    }

    private void checkPasswordStatus() {
        if(MyUtils.isEmptyPassword(this)){
            Intent intent = new Intent(getApplicationContext(), LockscreenActivity.class);
            intent.putExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_SETUP_PASSWORD);
            startActivity(intent);
        }
    }

   private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int voltage = intent.getIntExtra("voltage", 0);
            int temperature = intent.getIntExtra("temperature", 0);
            tvBatteryLevel.setText(getString(R.string.battery_level, level));
            tvVoltage.setText(getString(R.string.battery_voltage, voltage));
            double temps = (double)temperature / 10;
            tvTemperature.setText(getString(R.string.battery_temperature, (int)temps));

        }
    };
}
