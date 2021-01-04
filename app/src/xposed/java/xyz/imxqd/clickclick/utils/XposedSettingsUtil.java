package xyz.imxqd.clickclick.utils;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;

public class XposedSettingsUtil extends SettingsUtil {
    public static boolean isWorkScreenOff() {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getBoolean(ResUtil.getString(R.string.pref_key_work_screen_off), false);
    }
}
