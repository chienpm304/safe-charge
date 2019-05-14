package com.chienpm.safecharge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class LockscreenActivity extends AppCompatActivity {

    private int mMode;

    private PatternLockView mPatternLockView;

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {

        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {

            String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
            Log.d("chienpm_log_tag", "drawn: "+ patternString);
            pattern.clear();
        }

        @Override
        public void onCleared() {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockscreen);

        Intent intent = getIntent();
        mMode = intent.getIntExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_UNLOCK);

        initViews();

        updateScreenMode();
    }

    private void initViews() {
        mPatternLockView = (PatternLockView)findViewById(R.id.patter_lock_view);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);
    }


    private void updateScreenMode() {
        switch (mMode){
            case Definition.LOCKSCREEN_UNLOCK:
                setTitle("Draw the pattern");

                break;
            case Definition.LOCKSCREEN_CHANGE_PASSWORD:
                setTitle("Change password");

                break;
            case Definition.LOCKSCREEN_SETUP_PASSWORD:
                setTitle("Setup password");
                SharedPreferences pref = getSharedPreferences(Definition.PREF_KEY_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(Definition.PREF_PASSWORD, "chienfx");
                editor.commit();

                break;
        }
    }
}
