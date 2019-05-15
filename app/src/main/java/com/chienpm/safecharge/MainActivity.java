package com.chienpm.safecharge;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnSetupPassword;
    TextView tvVoltage, tvTemperature, tvBatteryLevel;


    RunInBackgroundService mService;
    Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mService = new RunInBackgroundService(this);
        mServiceIntent = new Intent(this, mService.getClass());


        if (!isMyServiceRunning(mService.getClass())) {
            startService(mServiceIntent);
        }

        updateBatteryInfo();

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
        super.onDestroy();
    }

    private void updateBatteryInfo() {

    }

    private void initViews() {
        tvBatteryLevel = findViewById(R.id.battery_level);
        tvTemperature = findViewById(R.id.temperature_level);
        tvVoltage = findViewById(R.id.voltage_level);
        btnSetupPassword = findViewById(R.id.btnSetupPassword);
        btnSetupPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LockscreenActivity.class);
                intent.putExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_SETUP_PASSWORD);
                startActivity(intent);
            }
        });
        checkPasswordStatus();

    }

    private void checkPasswordStatus() {
        if(isEmptyPassword()){
            btnSetupPassword.setVisibility(View.VISIBLE);
        }
        else{
            btnSetupPassword.setVisibility(View.GONE);
        }
    }

    private boolean isEmptyPassword() {
        SharedPreferences pref = getSharedPreferences(Definition.PREF_KEY_FILE, MODE_PRIVATE);
        String password = pref.getString(Definition.PREF_PASSWORD, "");
        Log.d("chienpm_log_tag", "password: "+password);
        return (TextUtils.isEmpty(password));
    }

}
