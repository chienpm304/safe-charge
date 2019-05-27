package com.chienpm.safecharge;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

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
//        MobileAds.initialize(context, context.getString(R.string.admob_app_id));

        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E69ED7FEB84433CA8B103E6CD89C1133")
                .build();
    }

    public static AdListener createAdListener() {
        AdListener mListenner =  new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d("chienpm_ads_log", "Ads loaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d("chienpm_ads_log", "Ads failed to load "+i);
            }

            @Override
            public void onAdClosed() {
                Log.d("chienpm_ads_log", "Ads closed");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d("chienpm_ads_log", "Ads left app");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d("chienpm_ads_log", "Ads opened");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d("chienpm_ads_log", "Ads clicked");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d("chienpm_ads_log", "Ads impression");
            }

        };
        return mListenner;
    }

    public static InterstitialAd createInterstitialAd(Context context) {
//        MobileAds.initialize(context, context.getString(R.string.admob_app_id));
        InterstitialAd mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getString(R.string.interstitial_full_screen));
        return mInterstitialAd;
    }


    public static int getBatteryImageWithLevel(int level) {
        switch (level / 10) {
            case 0:
                return R.drawable.ic_battery_0;
            case 1:
                return R.drawable.ic_battery_10;
            case 2:
                return R.drawable.ic_battery_20;
            case 3:
                return R.drawable.ic_battery_30;
            case 4:
                return R.drawable.ic_battery_40;
            case 5:
                return R.drawable.ic_battery_50;
            case 6:
                return R.drawable.ic_battery_60;
            case 7:
                return R.drawable.ic_battery_70;
            case 8:
                return R.drawable.ic_battery_80;
            case 9:
                return R.drawable.ic_battery_90;
        }
        return R.drawable.ic_battery_100;
    }

    public static boolean checkIfMainActivityIsActive(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Definition.PREF_KEY_FILE, Activity.MODE_PRIVATE);
        Boolean isActive = pref.getBoolean(Definition.PREF_MAIN_ACTIVITY_STATUS, false);
        return isActive;
    }

    /*
     * Set flag to check if the Main Activity is running or not
     * status: true - running, false - not running
     */
    public static void setMainAtivityStatusToActive(Context context, boolean status){
        SharedPreferences pref = context.getSharedPreferences(Definition.PREF_KEY_FILE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Definition.PREF_MAIN_ACTIVITY_STATUS, status);
        editor.apply();
        editor.commit();
    }
}
