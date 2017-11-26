package xyz.imxqd.mediacontroller.utils;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by imxqd on 2017/11/25.
 */
@SuppressLint("UseSparseArrays")
public class KeyEventUtil {

    public static final Map<Integer, String> mKeyName;
    static {
        mKeyName = new HashMap<>();
        Field[] fields = KeyEvent.class.getFields();
        try {
            for (Field field : fields) {
                if (field.getName().startsWith("KEYCODE_")) {
                    mKeyName.put(field.getInt(null),
                            field.getName().replaceFirst("KEYCODE_", ""));
                }
            }
        } catch (IllegalAccessException e) {
            Logger.e(e.getMessage());
        }

    }

    public static String getKeyName(int keyCode) {
        if (mKeyName.containsKey(keyCode)) {
            return mKeyName.get(keyCode);
        } else {
            return "UNKNOWN(" + keyCode + ")";
        }
    }

    public static KeyEvent[] makeKeyEventGroup(int keyCode) {
        KeyEvent down = new KeyEvent(SystemClock.uptimeMillis() - 100,
                SystemClock.uptimeMillis()  - 100,
                KeyEvent.ACTION_DOWN,  keyCode, 0);
        KeyEvent up = new KeyEvent(SystemClock.uptimeMillis() - 100,
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP,  keyCode, 0);
        return new KeyEvent[]{down, up};
    }

    public static boolean sendKeyEventByShell(int keyCode) {
        boolean success  = true;
        try {
            Runtime.getRuntime()
                    .exec("input keyevent " + keyCode);
        } catch (IOException e) {
            success = false;
            Logger.e(e.getMessage());
        }
        return success;
    }
}
