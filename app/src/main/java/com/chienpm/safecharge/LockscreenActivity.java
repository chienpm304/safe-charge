package com.chienpm.safecharge;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

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
    AdView mAdView;
    InterstitialAd mInterstitialAd;


    View.OnClickListener mCancelListener, mClearListener, mNextListener, mConfirmListener;
    private String mPrevPattern="";
    private String mNextPattern="";
    private int wrongCount = 5;

    /*
     * Activity lifecyle implementation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockscreen);

        Intent intent = getIntent();
        mMode = intent.getIntExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_UNLOCK);

        initServices();

        initViews();

        if(mMode == Definition.LOCKSCREEN_UNLOCK){
            startCountDown();
            mAdView.setVisibility(View.VISIBLE);
        }
        else
        {
            mAdView.setVisibility(View.GONE);
        }


        initBannerAdmod();
        initInterstitalAd();
        initListeners();
        updateScreenLayoutMode();
        MyUtils.updateSavedLanguage(this);
    }

    @Override
    protected void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
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
        if (mAdView != null) {
            mAdView.destroy();
        }
        unregisterListeners();
        super.onDestroy();
        if(mServiceIntent != null) {
            stopService(mServiceIntent); //stop service to invoke the sevice's destroy and restart service immediately
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MyUtils.updateSavedLanguage(this);
    }

    //Deny Back, Volume button when in Unlock mode
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mMode == Definition.LOCKSCREEN_UNLOCK){
            Log.d("chienpm_log_tag", "Key captured, mode = " + mMode + "key = "+keyCode);
            Toast.makeText(this, R.string.do_not_escape, Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /*
     * Initialization
     */
    private void initViews() {
        mPatternLockView = (PatternLockView)findViewById(R.id.patter_lock_view);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);
        mAdView = findViewById(R.id.lockScreenAdview);
        tvTittle = findViewById(R.id.tvTitle);
        tvWarrning = findViewById(R.id.tvWarrning);
        leftButton = findViewById(R.id.btnLeft);
        rightButton = findViewById(R.id.btnRight);

        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(1.0f, 1.0f);
    }

    private void initServices() {
        mService = new RunInBackgroundService(this.getApplicationContext());
        mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());


        if (!isMyServiceRunning(mService.getClass())) {
            startService(mServiceIntent);
        }
    }

    private void initBannerAdmod() {
        //Admod
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        AdRequest adRequest = MyUtils.createAdRequest(this);
        AdListener adListener = MyUtils.createAdListener();

        mAdView.setAdListener(adListener);
        mAdView.loadAd(adRequest);

    }

    private void initInterstitalAd() {
        AdRequest adRequest = MyUtils.createAdRequest(this);
        AdListener adListener = MyUtils.createAdListener();

        mInterstitialAd =  MyUtils.createInterstitialAd(this);
        mInterstitialAd.setAdListener(adListener);
        mInterstitialAd.loadAd(adRequest);

    }

    private void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void initListeners() {
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
                showInterstitial();
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
                    MyUtils.savedNewPassword(getApplicationContext(), mNextPattern);
                    showInterstitial();
                    finish();
                }
            }
        };
    }

    private void unregisterListeners() {
        leftButton.setOnClickListener(null);
        rightButton.setOnClickListener(null);
    }

    /*
     * Update UI functions
     */

    private void reloadUI() {
        setContentView(R.layout.activity_lockscreen);
        initViews();
        initListeners();
        updateScreenLayoutMode();
    }

    private void updateScreenLayoutMode() {
        switch (mMode){
            case Definition.LOCKSCREEN_UNLOCK:
                setTitle(R.string.app_name);
                tvTittle.setText(R.string.draw_unlock_pattern);
                leftButton.setVisibility(View.GONE);
                rightButton.setVisibility(View.GONE);

                break;
            case Definition.LOCKSCREEN_CHANGE_PASSWORD:
                setTitle(R.string.change_password);
                leftButton.setVisibility(View.VISIBLE);
                rightButton.setVisibility(View.GONE);
                leftButton.setText(R.string.cancel);
                leftButton.setOnClickListener(mCancelListener);
                mStep = CHANGE_PASSWORD;
                break;

            case Definition.LOCKSCREEN_SETUP_PASSWORD:
                setTitle(R.string.choose_your_pattern);
                mStep = SETUP_STEP1_INIT;
                break;
        }
        updateNavigationUI();
    }

    private void updateNavigationUI() {
        switch (mStep){
            case SETUP_STEP1_INIT:
                leftButton.setEnabled(true);
                leftButton.setText(R.string.cancel);
                leftButton.setOnClickListener(mCancelListener);

                rightButton.setText(R.string.next);
                rightButton.setEnabled(false);

                tvTittle.setText(R.string.draw_unlock_pattern);
                break;

            case SETUP_STEP1_DRAWING:
            case SETUP_STEP2_DRAWING:
                leftButton.setText(R.string.cancel);
                leftButton.setEnabled(false);
                leftButton.setOnClickListener(mCancelListener);
                rightButton.setEnabled(false);
                tvTittle.setText(R.string.release_finger);
                break;

            case SETUP_STEP1_DRAWN:
                leftButton.setText(R.string.clear);
                leftButton.setEnabled(true);
                leftButton.setOnClickListener(mClearListener);

                rightButton.setText(R.string.next);
                rightButton.setEnabled(true);
                rightButton.setOnClickListener(mNextListener);
                tvTittle.setText(R.string.pattern_recorded);
                break;


            case SETUP_STEP2_INIT:
                leftButton.setText(R.string.cancel);
                rightButton.setText(R.string.confirm);
                leftButton.setEnabled(true);
                rightButton.setEnabled(false);
                tvTittle.setText(R.string.draw_pattern_again);
                break;

            case SETUP_STEP2_DRAWN:
                leftButton.setText(R.string.cancel);
                rightButton.setText(R.string.confirm);
                leftButton.setEnabled(true);
                rightButton.setEnabled(true);
                leftButton.setOnClickListener(mCancelListener);
                rightButton.setOnClickListener(mConfirmListener);
                tvTittle.setText(R.string.your_new_pattern);
                break;

            case CHANGE_PASSWORD:
                tvTittle.setText(R.string.confirm_pattern);
                break;
        }
    }


    /*
     * Drawing pattern processing
     */
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

            if(PatternLockUtils.patternToString(mPatternLockView, pattern).length() < Definition.MIN_PATTERN_LENGTH)
            {
                tvWarrning.setText(R.string.pattern_error);
                wrongCount--;
                pattern.clear();
            }
            else{
                String patternString = PatternLockUtils.patternToMD5(mPatternLockView, pattern);
                Log.d("chienpm_log_tag", "drawn: "+ patternString);
                switch (mMode){
                    case Definition.LOCKSCREEN_UNLOCK:
                        if(isCorrectPattern(patternString)){
                            mediaPlayer.stop();
                            finish();
                            Intent mainAct = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(mainAct);
                            showInterstitial();
                        }
                        else{
                            tvWarrning.setText(R.string.pattern_wrong);
                            pattern.clear();
                        }

                        break;
                    case Definition.LOCKSCREEN_CHANGE_PASSWORD:
                        //check pass
                        if(isCorrectPattern(patternString)){
                            mMode = Definition.LOCKSCREEN_SETUP_PASSWORD;
                            mStep = SETUP_STEP1_INIT;
                            pattern.clear();
                            unregisterListeners();
                            reloadUI();
                            updateScreenLayoutMode();
                        }else{
                            tvWarrning.setText(R.string.pattern_wrong);
                            wrongCount--;
                            if(wrongCount < 4)
                                tvWarrning.setText(getString(R.string.pattern_attempts_left, wrongCount));
                            if(wrongCount < 1)
                                finish();
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
                                tvWarrning.setText(R.string.pattern_wrong);
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


    /*
     * Helper functions
     */

    private boolean isCorrectPattern(String patternString) {
        String password = getSharedPreferences(Definition.PREF_KEY_FILE, MODE_PRIVATE).getString(Definition.PREF_PASSWORD, "");
        return TextUtils.equals(patternString, password);
    }

    private void startCountDown() {
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                tvWarrning.setText(getString(R.string.alert_in, l/1000));
            }

            @Override
            public void onFinish() {
                tvWarrning.setText(R.string.thief_detected);
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

    private void setMaximumVolume() {
        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int media_max_volume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        manager.setStreamVolume(
                AudioManager.STREAM_MUSIC, // Stream type
                media_max_volume, // Index
                AudioManager.FLAG_VIBRATE // Flags
        );
    }

}
