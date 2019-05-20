package com.chienpm.safecharge;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> mListSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);



        mListView = findViewById(R.id.listViewSettings);

        mAdapter = new ArrayAdapter<String>(this, R.layout.list_text_item);
        mListView.setAdapter(mAdapter);

        MyUtils.updateSavedLanguage(this);
        updateUI();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 1: //change password
                        Intent changPasswordIntent = new Intent(getApplicationContext(), LockscreenActivity.class);
                        changPasswordIntent.putExtra(Definition.LOCKSCREEN_MODE, Definition.LOCKSCREEN_CHANGE_PASSWORD);
                        startActivity(changPasswordIntent);
                        break;
                    case 0://language
                        Intent changeLanguageIntent = new Intent(getApplicationContext(), LanguageActivity.class);
                        startActivity(changeLanguageIntent);
                        break;
                }
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(MyUtils.updateSavedLanguage(this))
            updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MyUtils.updateSavedLanguage(this))
            updateUI();
        Log.d("chienpm_log", "onResume Setting activity");
    }

    private void updateUI() {
        setTitle(R.string.settings);
        //get string array adapted to language
        mAdapter.clear();
        mListSettings = Arrays.asList(getResources().getStringArray(R.array.list_settings));
        mAdapter.addAll(mListSettings);
    }

}
