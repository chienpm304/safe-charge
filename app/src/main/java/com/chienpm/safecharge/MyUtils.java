package com.chienpm.safecharge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

public class MyUtils {
    public static void saveLocale(Locale locale, Context context) {
        SharedPreferences pref = context.getSharedPreferences(Definition.PREF_KEY_FILE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Definition.PREF_LANGUAGE, locale.getLanguage());
        editor.putString(Definition.PREF_COUNTRY, locale.getCountry());
        editor.apply();
    }

    public static void changeLanguage(Locale locale, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        Configuration config = new Configuration();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(locale);
        }
        else{
            config.locale = locale;
        }
        Locale.setDefault(locale);
        context.getResources().updateConfiguration(config, displayMetrics);

    }

    public static boolean isEmptyPassword(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Definition.PREF_KEY_FILE, Activity.MODE_PRIVATE);
        String password = pref.getString(Definition.PREF_PASSWORD, "");
        Log.d("chienpm_log_tag", "password: "+password);
        return (TextUtils.isEmpty(password));
    }

    @SuppressWarnings("deprecation")
    public static Locale getCurrentLocale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return configuration.getLocales().get(0);
        }
        else
            return configuration.locale;
    }

    public static void updateSavedLanguage(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Definition.PREF_KEY_FILE, Activity.MODE_PRIVATE);

        String savedLangCode = pref.getString(Definition.PREF_LANGUAGE, "");
        String savedLangCountry = pref.getString(Definition.PREF_COUNTRY, "");

        Locale currentLocale = MyUtils.getCurrentLocale(context);

        if(TextUtils.isEmpty(savedLangCode))
        {
            MyUtils.saveLocale(currentLocale, context);
            return;
        }
        if(!currentLocale.getLanguage().equals(savedLangCode)){
            //need to change language
            Locale locale = new Locale(savedLangCode, savedLangCountry);
            MyUtils.changeLanguage(locale, context);

        }

    }
}
