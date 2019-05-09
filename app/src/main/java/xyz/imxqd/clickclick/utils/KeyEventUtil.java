package xyz.imxqd.clickclick.utils;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.os.Process;
import android.os.SystemClock;
import android.view.KeyEvent;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;

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
            LogUtils.e(e.getMessage());
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

    public static void sendKeyEvent(int keyCode) {
        AccessibilityService service = AppEventManager.getInstance().getService();
        AudioManager audioManager = AppEventManager.getInstance().getAudioManager();
        LogUtils.i("keyevent:" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (service != null) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                } else {
                    AlertUtil.show(App.get().getString(R.string.accessibility_error));
                    throw new RuntimeException(App.get().getString(R.string.accessibility_error));
                }
                break;
            case KeyEvent.KEYCODE_HOME:
                if (service != null) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                } else {
                    AlertUtil.show(App.get().getString(R.string.accessibility_error));
                    throw new RuntimeException(App.get().getString(R.string.accessibility_error));
                }
                break;
            case KeyEvent.KEYCODE_APP_SWITCH:
                if (service != null) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                } else {
                    AlertUtil.show(App.get().getString(R.string.accessibility_error));
                    throw new RuntimeException(App.get().getString(R.string.accessibility_error));
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MUTE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_RECORD:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_CLOSE:
            case KeyEvent.KEYCODE_MEDIA_EJECT:
            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
                if (audioManager != null) {
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    KeyEvent[] events = KeyEventUtil.makeKeyEventGroup(keyCode);
                    audioManager.dispatchMediaKeyEvent(events[0]);
                    audioManager.dispatchMediaKeyEvent(events[1]);
                } else {
                    throw new RuntimeException("AudioManager Error");
                }
                break;
            default:
                if (!KeyEventUtil.sendKeyEventByShell(keyCode)) {
                    throw new RuntimeException("KeyEvent commend exec error");
                }
        }
    }

    public static boolean sendKeyEventByShell(int keyCode) {
        boolean success  = true;
        try {
            if (ShellUtil.isSuAvailable()) {
                ShellUtil.runCommand("input keyevent " + keyCode);
            } else {
                Runtime.getRuntime()
                        .exec("input keyevent " + keyCode);
            }
        } catch (IOException e) {
            success = false;
            LogUtils.e(e.getMessage());
        }
        return success;
    }
}
