package xyz.imxqd.clickclick.model;

import android.accessibilityservice.AccessibilityService;
import android.app.Application;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.utils.KeyEventHandler;
import xyz.imxqd.clickclick.utils.KeyEventUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;
import static android.content.Context.AUDIO_SERVICE;

public class AppEventManager implements KeyEventHandler.Callback {

    private static AppEventManager sInstance;

    private AudioManager mAudioManager;
    private KeyEventHandler mKeyEventHandler;
    private AccessibilityService mService;
    private Toast mToast;

    private AppEventManager() {
    }

    public static AppEventManager getInstance() {
        if (sInstance == null) {
            synchronized (AppEventManager.class) {
                sInstance = new AppEventManager();
            }
            return sInstance;
        } else {
            return sInstance;
        }
    }

    private boolean init = false;

    public void init(Application application) {
        synchronized (AppEventManager.class) {
            if (init) {
                return;
            }
            mAudioManager = (AudioManager) application.getSystemService(AUDIO_SERVICE);
            mKeyEventHandler = new KeyEventHandler();
            mKeyEventHandler.mLongClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
            mKeyEventHandler.mLongClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
//            mKeyEventHandler.mSingleClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
//            mKeyEventHandler.mSingleClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
            mKeyEventHandler.mDoubleClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
            mKeyEventHandler.mDoubleClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
            mKeyEventHandler.mTripleClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
            mKeyEventHandler.mTripleClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
            mKeyEventHandler.setCallback(this);
            init = true;
        }
    }

    public void updateKeyEventData() {

    }

    public boolean shouldInterrupt(KeyEvent event) {
        if (App.get().isServiceOn && SettingsUtil.isServiceOn()) {
            return mKeyEventHandler.inputKeyEvent(event);
        } else {
            return false;
        }
    }


    public static String makeAppKeyEventData(int keyCode, int deviceId, AppKeyEventType type) {
        return String.format(Locale.getDefault(), "%d:%d:%s", keyCode, deviceId, type.getName());
    }

    public void onEvent(int keyCode, int deviceId, AppKeyEventType type) {
        String eventData = makeAppKeyEventData(keyCode, deviceId, type);
    }

    public void attachToAccessibilityService(AccessibilityService service) {
        mService = service;
    }

    public void dettachFromAccessibilityService() {
        mService = null;
    }

    @Override
    public void onNormalKeyEvent(KeyEvent event) {
        if (SettingsUtil.displayDebug()) {
            showToast("normal :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mService != null) {
                mService.performGlobalAction(GLOBAL_ACTION_BACK);
            }
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        } else {
            KeyEventUtil.sendKeyEventByShell(event.getKeyCode());
        }
    }

    @Override
    public void onLongClick(KeyEvent event) {
        Logger.d(event);
        if (SettingsUtil.displayDebug()) {
            showToast("onLongClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDeviceId(), AppKeyEventType.LongClick);
    }

    @Override
    public void onSingleClick(KeyEvent event) {
        Logger.d(event);
        if (SettingsUtil.displayDebug()) {
            showToast("onSingleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDeviceId(), AppKeyEventType.SingleClick);
    }

    @Override
    public void onDoubleClick(KeyEvent event) {
        Logger.d(event);
        if (SettingsUtil.displayDebug()) {
            showToast("onDoubleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDeviceId(), AppKeyEventType.DoubleClick);
    }

    @Override
    public void onTripleClick(KeyEvent event) {
        Logger.d(event);
        if (SettingsUtil.displayDebug()) {
            showToast("onTripleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDeviceId(), AppKeyEventType.TripleClick);

    }

    private void showToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(App.get(), str, Toast.LENGTH_LONG);
        mToast.show();
    }
}
