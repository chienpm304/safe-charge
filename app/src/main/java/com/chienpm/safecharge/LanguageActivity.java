package com.chienpm.safecharge;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {


    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> mLanguagesDisplay;
    private List<String> mLanguagesCountry;
    private List<String> mLanguagesKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        MyUtils.updateSavedLanguage(this);
        setTitle(R.string.language);

        mLanguagesDisplay = Arrays.asList(getResources().getStringArray(R.array.list_lang_displays));
        mLanguagesKey = Arrays.asList(getResources().getStringArray(R.array.list_lang_keys));
        mLanguagesCountry = Arrays.asList(getResources().getStringArray(R.array.list_lang_countrys));


        mListView = findViewById(R.id.listviewLanguages);
        mAdapter = new ArrayAdapter<String>(this, R.layout.list_text_item);
        mListView.setAdapter(mAdapter);
        mAdapter.addAll(mLanguagesDisplay);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Locale locale = new Locale(mLanguagesKey.get(position), mLanguagesCountry.get(position));
                MyUtils.saveLocale(locale, getApplicationContext());
                finish();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MyUtils.updateSavedLanguage(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        MyUtils.updateSavedLanguage(this);
    }
}
