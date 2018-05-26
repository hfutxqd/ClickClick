package xyz.imxqd.clickclick.func;

import android.accessibilityservice.AccessibilityService;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.widget.Toast;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.KeyEventUtil;

public class KeyEventFunction extends AbstractFunction {
    public static final String PREFIX = "keyevent";

    public KeyEventFunction(String funcData) {
        super(funcData);
    }

    private int getKeyCode(String args) {
        try {
            return Integer.valueOf(args);
        } catch (Exception e) {
            return 0;
        }
    }

    private void toastAccessibilityError() {
        Toast.makeText(App.get(), R.string.accessibility_error, Toast.LENGTH_LONG).show();
    }


    @Override
    public void doFunction(String args) {
        int keyCode = getKeyCode(args);
        AccessibilityService service = AppEventManager.getInstance().getService();
        AudioManager audioManager = AppEventManager.getInstance().getAudioManager();
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (service != null) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                } else {
                    toastAccessibilityError();
                }
                break;
            case KeyEvent.KEYCODE_HOME:
                if (service != null) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                } else {
                    toastAccessibilityError();
                }
                break;
            case KeyEvent.KEYCODE_APP_SWITCH:
                if (service != null) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                } else {
                    toastAccessibilityError();
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
                    KeyEvent[] events = KeyEventUtil.makeKeyEventGroup(keyCode);
                    audioManager.dispatchMediaKeyEvent(events[0]);
                    audioManager.dispatchMediaKeyEvent(events[1]);
                }
                break;
             default:
                    KeyEventUtil.sendKeyEventByShell(keyCode);
        }
    }
}
