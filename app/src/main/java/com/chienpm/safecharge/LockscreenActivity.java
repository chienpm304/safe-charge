package com.chienpm.safecharge;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import org.w3c.dom.Text;

import java.util.List;

public class LockscreenActivity extends AppCompatActivity {
    RunInBackgroundService mService;
    Intent mServiceIntent;

    private int mMode;
    private int mStep = 0;
    final int CHANGE_PASSWORD = 9;

    final int SETUP_STEP1_INIT = 10;
    final int SETUP_STEP1_DRAWING = 11;
    final int SETUP_STEP1_DRAWN = 12;

    final int SETUP_STEP2_INIT = 13;
    final int SETUP_STEP2_DRAWING = 14;
    final int SETUP_STEP2_DRAWN = 15;




    Button leftButton, rightButton;
    TextView tvTittle, tvWarrning;
    CountDownTimer timer;
    MediaPlayer mediaPlayer;

    View.OnClickListener mCancelListener, mClearListener, mNextListener, mConfirmListener;
    private String mPrevPattern="";
    private String mNextPattern="";

    //Todo: Change button layout

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
        initListener();
        updateScreenLayoutMode();

        if(mMode == Definition.LOCKSCREEN_UNLOCK){
            //TODO: lock the phone i want disable all physical button when in unlock mode

            startCountDown();
        }

    }

    private void initListener() {
        mClearListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPatternLockView.clearPattern();
                if (mStep == SETUP_STEP1_DRAWING || mStep == SETUP_STEP1_DRAWN) {
                    mStep = SETUP_STEP1_INIT;
                } else if(mStep == SETUP_STEP2_DRAWING || mStep == SETUP_STEP2_DRAWN) {
                    mStep = SETUP_STEP2_INIT;
                }
                updateNavigationUI();
            }
        };

        mCancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };

        mNextListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStep == SETUP_STEP1_DRAWN){
                    mStep = SETUP_STEP2_INIT;
                    mPatternLockView.clearPattern();
                    updateNavigationUI();
                }
            }
        };

        mConfirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.equals(mPrevPattern, mNextPattern)){
                    SharedPreferences pref = getSharedPreferences(Definition.PREF_KEY_FILE, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Definition.PREF_PASSWORD, mNextPattern);
                    editor.commit();
                    finish();
                }
            }
        };
    }

    //Deny Back, Volume button when in Unlock mode
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mMode == Definition.LOCKSCREEN_UNLOCK){
            Log.d("chienpm_log_tag", "Key captured, mode = " + mMode + "key = "+keyCode);
            Toast.makeText(this, "Aka, who know how to escape :))", Toast.LENGTH_LONG);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mServiceIntent); //stop service to invoke the sevice's destroy and restart service immediately
    }

    private PatternLockView mPatternLockView;

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            if(mMode == Definition.LOCKSCREEN_SETUP_PASSWORD)
                tvWarrning.setText("");
            if(mStep == SETUP_STEP1_INIT)
            {
                mStep = SETUP_STEP1_DRAWING; updateNavigationUI();
            }
            else if(mStep == SETUP_STEP2_INIT){
                mStep = SETUP_STEP2_DRAWING; updateNavigationUI();
            }
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {

            String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
            Log.d("chienpm_log_tag", "drawn: "+ patternString);

            if(patternString.length() < Definition.MIN_PATTERN_LENGTH)
            {
                tvWarrning.setText("Connect at least 4 dots. Try again");
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
                            tvWarrning.setText("Wrong pattern");
                            pattern.clear();
                        }

                        break;
                    case Definition.LOCKSCREEN_CHANGE_PASSWORD:
                        //check pass
                        if(isCorrectPattern(patternString)){
                            mMode = Definition.LOCKSCREEN_SETUP_PASSWORD;
                            mStep = SETUP_STEP1_INIT;
                            pattern.clear();
                            reloadUI();
                            updateScreenLayoutMode();
                        }else{
                            tvWarrning.setText("Wrong pattern");
                        }
                        break;
                    case Definition.LOCKSCREEN_SETUP_PASSWORD:
                        if(mStep == SETUP_STEP1_DRAWING){
                            mPrevPattern = patternString;
                            mStep = SETUP_STEP1_DRAWN;
                        }
                        else if(mStep == SETUP_STEP2_DRAWING){
                            mNextPattern = patternString;
                            if(TextUtils.equals(mPrevPattern, mNextPattern))
                                mStep++;
                            else{
                                tvWarrning.setText("Wrong pattern");
                                pattern.clear();
                                mStep = SETUP_STEP2_INIT;
                                mNextPattern = "";
                            }
                        }
                        updateNavigationUI();
                        break;
                }
            }
        }

        @Override
        public void onCleared() {

        }
    };

    private void reloadUI() {
        setContentView(R.layout.activity_lockscreen);
        initViews();
        initListener();
        updateScreenLayoutMode();
    }

    private boolean isCorrectPattern(String patternString) {
        String password = getSharedPreferences(Definition.PREF_KEY_FILE, MODE_PRIVATE).getString(Definition.PREF_PASSWORD, "");
        return TextUtils.equals(patternString, password);
    }

    private void startCountDown() {
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                tvWarrning.setText("Alert in " + String.valueOf(l/1000) +"s");
            }

            @Override
            public void onFinish() {
                tvWarrning.setText("Damn, Robber!");
                setMaximumVolume();
                mediaPlayer.start();
            }
        }.start();
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
        mediaPlayer.setVolume(1.0f, 1.0f);
    }

    private void setMaximumVolume() {
        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int media_max_volume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        manager.setStreamVolume(
                AudioManager.STREAM_MUSIC, // Stream type
                media_max_volume, // Index
                AudioManager.FLAG_VIBRATE // Flags
        );
    }


    private void updateScreenLayoutMode() {
        switch (mMode){
            case Definition.LOCKSCREEN_UNLOCK:
                setTitle("Safe Charge");
                tvTittle.setText("Drawn unlock pattern");
                leftButton.setVisibility(View.GONE);
                rightButton.setVisibility(View.GONE);

                break;
            case Definition.LOCKSCREEN_CHANGE_PASSWORD:
                setTitle("Change password");
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.GONE);
                leftButton.setText("Cancel");
                leftButton.setOnClickListener(mCancelListener);
                mStep = CHANGE_PASSWORD;
                break;
            case Definition.LOCKSCREEN_SETUP_PASSWORD:
                setTitle("Choose your pattern");
                mStep = SETUP_STEP1_INIT;
                break;
        }
        updateNavigationUI();
    }

    private void updateNavigationUI() {
        switch (mStep){
            case SETUP_STEP1_INIT:
                leftButton.setEnabled(true);
                leftButton.setText("Cancel");
                leftButton.setOnClickListener(mCancelListener);

                rightButton.setText("Next");
                rightButton.setEnabled(false);

                tvTittle.setText("Draw an unlock pattern");
                break;

            case SETUP_STEP1_DRAWING:
            case SETUP_STEP2_DRAWING:
                leftButton.setText("Cancel");
                leftButton.setEnabled(false);
                leftButton.setOnClickListener(mCancelListener);
                rightButton.setEnabled(false);
                tvTittle.setText("Release finger when done");
                break;

            case SETUP_STEP1_DRAWN:
                leftButton.setText("Clear");
                leftButton.setEnabled(true);
                leftButton.setOnClickListener(mClearListener);

                rightButton.setText("Next");
                rightButton.setEnabled(true);
                rightButton.setOnClickListener(mNextListener);
                tvTittle.setText("Pattern recored");
                break;


            case SETUP_STEP2_INIT:
                leftButton.setText("Cancel");
                rightButton.setText("Confirm");
                leftButton.setEnabled(true);
                rightButton.setEnabled(false);
                tvTittle.setText("Draw pattern again to confirm");
                break;

            case SETUP_STEP2_DRAWN:
                leftButton.setText("Cancel");
                rightButton.setText("Confirm");
                leftButton.setEnabled(true);
                rightButton.setEnabled(true);
                leftButton.setOnClickListener(mCancelListener);
                rightButton.setOnClickListener(mConfirmListener);
                tvTittle.setText("Your new unlock pattern");
                break;

            case CHANGE_PASSWORD:
                tvTittle.setText("Cofirm your pattern");
                break;
        }
    }
}
