package xyz.imxqd.clickclick.xposed;

import de.robv.android.xposed.XposedBridge;

public class Log {

    public static void d(String tag, String message) {
        XposedBridge.log(tag + ":\t" + message);
    }

}
