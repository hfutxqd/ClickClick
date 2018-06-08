package xyz.imxqd.clickclick.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.log.LogUtils;

public class SystemSettingsUtl {

    public static boolean canWrite() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return Settings.System.canWrite(App.get());
        } else {
            return true;
        }
    }

    public static void startPackageSettings() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + App.get().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.get().startActivity(intent);
        }
    }

    public static boolean switchAutoRotation(int enable) {
        if (canWrite()) {
            if (enable == 0) {
                App.get().showToast(App.get().getString(R.string.auto_rotation_turn_off), false, true);
            } else if (enable == 1) {
                App.get().showToast(App.get().getString(R.string.auto_rotation_turn_on), false, true);
            }
            Settings.System.putInt(App.get().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enable);
            return true;
        }
        return false;
    }

    public static boolean switchAutoRotation() {
        if (canWrite()) {
            try {
                if (Settings.System.getInt(App.get().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1) {
                    Display defaultDisplay = ((WindowManager) App.get().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    Settings.System.putInt(App.get().getContentResolver(), Settings.System.USER_ROTATION, defaultDisplay.getRotation());
                    switchAutoRotation(0);
                } else {
                    switchAutoRotation(1);
                }
                return true;
            } catch (Settings.SettingNotFoundException e) {
                LogUtils.e(e.getMessage());
                return false;
            }
        }
        return false;
    }

    @IntDef({ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Rotation {}

    /**
     * Rotation constant: 0 degree rotation (natural orientation)
     */
    public static final int ROTATION_0 = 0;

    /**
     * Rotation constant: 90 degree rotation.
     */
    public static final int ROTATION_90 = 1;

    /**
     * Rotation constant: 180 degree rotation.
     */
    public static final int ROTATION_180 = 2;

    /**
     * Rotation constant: 270 degree rotation.
     */
    public static final int ROTATION_270 = 3;

    public static boolean switchRotation(@Rotation int rotation) {
        if (canWrite()) {
            Settings.System.putInt(App.get().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
            Settings.System.putInt(App.get().getContentResolver(), Settings.System.USER_ROTATION, rotation);
            return true;
        }
        return false;
    }
}
