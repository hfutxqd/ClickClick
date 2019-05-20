package xyz.imxqd.clickclick.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.service.KeyEventService;

/**
 * Created by imxqd on 2017/11/26.
 */

public class SettingsUtil {

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + KeyEventService.class.getName();
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            LogUtils.d("Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        accessibilityFound = true;
                    }
                }
            }
        }
        return accessibilityFound;
    }


    public static void startInstalledAppDetailsActivity(String pkg) {
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + pkg));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        App.get().startActivity(i);
    }

    public static void startAccessibilitySettings(Context context) {
        try {
            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } catch (Throwable t) {
            App.get().showToast(R.string.open_accessibility_error);
            LogUtils.e(t.getMessage());
        }
    }

    public static boolean displayDebug() {
        if(BuildConfig.DEBUG) {
            return true;
        }
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getBoolean(ResUtil.getString(R.string.pref_key_app_debug), false);
    }

    public static boolean isServiceOn() {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getBoolean(ResUtil.getString(R.string.pref_key_app_switch), false);
    }

    public static boolean isNotificationOn() {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getBoolean(ResUtil.getString(R.string.pref_key_notification_switch), false);
    }

    public static boolean isNotificationWorking() {
        return AppEventManager.getInstance().getNotificationService() != null;
    }

    public static boolean isShockOn() {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
        return shp.getBoolean(ResUtil.getString(R.string.pref_key_shock_after_run), true);
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
