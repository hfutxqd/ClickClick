package xyz.imxqd.clickclick.utils;

import android.os.Build;

public class RomUtil {

    public static boolean isMeizuFlyme() {
        return Build.MANUFACTURER.toLowerCase().contains("meizu") || Build.FINGERPRINT.toLowerCase().contains("flyme") ||
                Build.FINGERPRINT.toLowerCase().contains("meizu");
    }
}
