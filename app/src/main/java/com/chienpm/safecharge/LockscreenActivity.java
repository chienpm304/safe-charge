package com.chienpm.safecharge;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.service.autofill.TextValueSanitizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class LockscreenActivity extends AppCompatActivity {
    RunInBackgroundService mService;
    Intent mServiceIntent;

    private int mMode;

    private PatternLockView mPatternLockView;

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            tvTittle.setText("Please draw the unlock pattern");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            tvTittle.setText("Release your finger when its done");
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {

            String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
            Log.d("chienpm_log_tag", "drawn: "+ patternString);

            if(patternString.length() < Definition.MIN_PATTERN_LENGTH)
            {
                tvWarrning.setText("You must connect at least 4 dots. Please try again");
                pattern.clear();
            }
            else{
                switch (mMode){
                    case Definition.LOCKSCREEN_UNLOCK:
                        if(isCorrectPattern(patternString)){
                            mediaPlayer.stop();
                            finish();
                        }
                        else{
                            pattern.clear();
                        }

                        break;
                    case Definition.LOCKSCREEN_CHANGE_PASSWORD:


                        break;
                    case Definition.LOCKSCREEN_SETUP_PASSWORD:
                        setTitle("Setup password");
                        SharedPreferences pref = getSharedPreferences(Definition.PREF_KEY_FILE, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(Definition.PREF_PASSWORD, patternString);
                        editor.commit();
                        finish();
                        break;
                }
            }
        }

        @Override
        public void onCleared() {

        }
    };

    private boolean isCorrectPattern(String patternString) {
        String password = getSharedPreferences(Definition.PREF_KEY_FILE, MODE_PRIVATE).getString(Definition.PREF_PASSWORD, "");
        return TextUtils.equals(patternString, password);
    }

    Button leftButton, rightButton;
    TextView tvTittle, tvWarrning;
    CountDownTimer timer;
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockscreen);

        Intent intent = getIntent();
        mMode = intent.getIntExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_UNLOCK);

        mService = new RunInBackgroundService(this);
        mServiceIntent = new Intent(this, mService.getClass());


        if (!isMyServiceRunning(mService.getClass())) {
            startService(mServiceIntent);
        }

        initViews();

        updateScreenMode();

        if(mMode == Definition.LOCKSCREEN_UNLOCK){
            //TODO: lock the phone

            //TODO: start service countdown to alarm:
            timer = new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long l) {
                    tvWarrning.setText("Alert in " + String.valueOf(l/1000) +"s");
                }

                @Override
                public void onFinish() {
                    //TODO: open alarm
                    tvWarrning.setText("Damn, Robber!");
                    mediaPlayer.start();
                }
            }.start();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mServiceIntent); //stop service to invoke the sevice's destroy and restart service immediately
    }

    private boolean isMyServiceRunning(Class<? extends RunInBackgroundService> serviceClass) {
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

    private void initViews() {
        mPatternLockView = (PatternLockView)findViewById(R.id.patter_lock_view);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);

        tvTittle = findViewById(R.id.tvTitle);
        tvWarrning = findViewById(R.id.tvWarrning);
        leftButton = findViewById(R.id.btnLeft);
        rightButton = findViewById(R.id.btnRight);
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(1, 1);
    }


    private void updateScreenMode() {
        switch (mMode){
            case Definition.LOCKSCREEN_UNLOCK:
                setTitle("Draw the pattern");
                leftButton.setVisibility(View.GONE);
                rightButton.setVisibility(View.GONE);

                break;
            case Definition.LOCKSCREEN_CHANGE_PASSWORD:
                setTitle("Change password");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.VISIBLE);

                break;
            case Definition.LOCKSCREEN_SETUP_PASSWORD:
                setTitle("Setup password");

                break;
        }
    }
}
