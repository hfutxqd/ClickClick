package xyz.imxqd.clickclick.utils;

import android.app.Service;
import android.os.Vibrator;

import xyz.imxqd.clickclick.App;

/**
 * Created by imxqd on 17-4-2.
 */

public class Shocker {
    public static void shock(long[] ms) {
        Vibrator vibrator = (Vibrator) App.get().getSystemService(Service.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        vibrator.vibrate(ms, -1);
    }

    public static void shock() {
        Vibrator vibrator = (Vibrator) App.get().getSystemService(Service.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        vibrator.vibrate(60000);
    }

    public static void cancal() {
        Vibrator vibrator = (Vibrator) App.get().getSystemService(Service.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        vibrator.cancel();
    }
}