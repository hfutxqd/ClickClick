package xyz.imxqd.clickclick.utils;

import android.app.Service;
import android.os.Vibrator;

import xyz.imxqd.clickclick.App;

/**
 * Created by imxqd on 17-4-2.
 */

public class Shocker {
    public static void shock(long[] ms) throws Exception {
        Vibrator vibrator = (Vibrator) App.get().getSystemService(Service.VIBRATOR_SERVICE);
        if (vibrator == null) {
            throw new Exception("no vibrator found");
        }
        vibrator.vibrate(ms, -1);
    }

    public static void shock()  throws Exception {
        Vibrator vibrator = (Vibrator) App.get().getSystemService(Service.VIBRATOR_SERVICE);
        if (vibrator == null) {
            throw new Exception("no vibrator found");
        }
        vibrator.vibrate(60000);
    }

    public static void cancal() throws Exception {
        Vibrator vibrator = (Vibrator) App.get().getSystemService(Service.VIBRATOR_SERVICE);
        if (vibrator == null) {
            throw new Exception("no vibrator found");
        }
        vibrator.cancel();
    }
}