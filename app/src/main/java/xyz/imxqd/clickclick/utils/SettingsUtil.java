package xyz.imxqd.clickclick.utils;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;

/**
 * Created by imxqd on 2017/11/26.
 */

public class SettingsUtil {
    public static boolean displayDebug() {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getBoolean(ResUtil.getString(R.string.pref_key_app_debug), false);
    }

    public static boolean isServiceOn() {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getBoolean(ResUtil.getString(R.string.pref_key_app_switch), true);
    }


    public static int getQuickClickTime() {
        String quickTime = SettingsUtil.getStringVal(App.get().getString(R.string.pref_key_quick_click_time),
                App.get().getString(R.string.quick_click_speed_time_default));
        return Integer.valueOf(quickTime);
    }

    public static int getLongClickTime() {
        String longTime = SettingsUtil.getStringVal(App.get().getString(R.string.pref_key_long_click_time),
                App.get().getString(R.string.long_click_speed_time_default));
        return Integer.valueOf(longTime);
    }

    public static String getStringVal(String key, String def) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getString(key, def);
    }

    public static int getIntVal(String key, int def) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getInt(key, def);
    }
}
