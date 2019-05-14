package com.chienpm.safecharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.support.annotation.Nullable;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        updateBatteryInfo();
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

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int voltage = intent.getIntExtra("voltage", 0);
            int temperature = intent.getIntExtra("temperature", 0);
            tvBatteryLevel.setText("Battery Status: " + String.valueOf(level) + "%");
            tvVoltage.setText("Battery Voltage: " + String.valueOf(voltage));
            double temps = (double)temperature / 10;
            tvTemperature.setText("Battery Temperature: " + String.valueOf(temps));
        }
    };
}
