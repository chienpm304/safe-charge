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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

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

    public static void savedNewPassword(Context context, String newPassword) {
        SharedPreferences pref = context.getSharedPreferences(Definition.PREF_KEY_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Definition.PREF_PASSWORD, newPassword);
        editor.apply();
        editor.commit();
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

    /*Check and update saved locale
    * Return TRUE if need to update UI
    * Return FALSE if not need to update UI
    */
    public static boolean updateSavedLanguage(Context context) {

        SharedPreferences pref = context.getSharedPreferences(Definition.PREF_KEY_FILE, Activity.MODE_PRIVATE);

        String savedLangCode = pref.getString(Definition.PREF_LANGUAGE, "");
        String savedLangCountry = pref.getString(Definition.PREF_COUNTRY, "");

        Locale currentLocale = MyUtils.getCurrentLocale(context);

        if(TextUtils.isEmpty(savedLangCode))
        {
            Log.d("chienpm_log", "I was localed because savedLangCode is empty");
            MyUtils.saveLocale(currentLocale, context);
            return true;
        }
        if(!currentLocale.getLanguage().equals(savedLangCode)){
            //need to change language
            Locale locale = new Locale(savedLangCode, savedLangCountry);
            MyUtils.changeLanguage(locale, context);
            Log.d("chienpm_log", "I was localed because savedLangCode changed");
            return true;
        }
        return false;
    }

    public static AdRequest createAdRequest(Context context) {
        MobileAds.initialize(context, context.getString(R.string.admob_app_id));
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E69ED7FEB84433CA8B103E6CD89C1133")
                .build();
    }

    public static InterstitialAd createInterstitialAd(Context context) {
        MobileAds.initialize(context, context.getString(R.string.admob_app_id));
        InterstitialAd mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getString(R.string.interstitial_full_screen));


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
            }

        });
        return mInterstitialAd;
    }
}
