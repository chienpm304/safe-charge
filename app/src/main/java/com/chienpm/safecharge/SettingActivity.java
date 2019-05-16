package com.chienpm.safecharge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    //Todo: Change setting icon to white

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initViews();
    }


    private void initViews() {
        ((TextView)findViewById(R.id.setting_change_password)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LockscreenActivity.class);
                intent.putExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_CHANGE_PASSWORD);
                startActivity(intent);
            }
        });
    }
}
