package xyz.imxqd.clickclick.model;

import android.accessibilityservice.AccessibilityService;
import android.app.Application;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;
import xyz.imxqd.clickclick.service.KeyEventService;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.utils.KeyEventHandler;
import xyz.imxqd.clickclick.utils.KeyEventUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;
import static android.content.Context.AUDIO_SERVICE;

public class AppEventManager implements KeyEventHandler.Callback {

    private static AppEventManager sInstance;

    private AudioManager mAudioManager;
    private KeyEventHandler mKeyEventHandler;
    private KeyEventService mService;
    private NotificationCollectorService mNotification;
    private Toast mToast;

    private Map<String, Long> mKeyEventData = new HashMap<>();

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
            updateKeyEventData();
            mKeyEventHandler.setCallback(this);
            updateClickTime();
            init = true;
        }
    }

    public KeyEventService getService() {
        return mService;
    }

    public AudioManager getAudioManager() {
        return mAudioManager;
    }

    public NotificationCollectorService getNotificationService() {
        return mNotification;
    }

    public void updateClickTime() {
        KeyEventHandler.initClickTimes(SettingsUtil.getQuickClickTime(), SettingsUtil.getLongClickTime());
    }

    public void updateKeyEventData() {
        mKeyEventHandler.mLongClickKeyCodes.clear();
        mKeyEventHandler.mSingleClickKeyCodes.clear();
        mKeyEventHandler.mDoubleClickKeyCodes.clear();
        mKeyEventHandler.mTripleClickKeyCodes.clear();
        mKeyEventData.clear();

        List<KeyMappingEvent> keyMappingEvents = KeyMappingEvent.getEnabledItems();
        for (KeyMappingEvent event : keyMappingEvents) {
            if (event.eventType == AppKeyEventType.SingleClick) {
                mKeyEventHandler.mSingleClickKeyCodes.add(event.keyCode);
            } else if (event.eventType == AppKeyEventType.LongClick) {
                mKeyEventHandler.mLongClickKeyCodes.add(event.keyCode);
            } else if (event.eventType == AppKeyEventType.DoubleClick) {
                mKeyEventHandler.mDoubleClickKeyCodes.add(event.keyCode);
            } else if (event.eventType == AppKeyEventType.TripleClick) {
                mKeyEventHandler.mTripleClickKeyCodes.add(event.keyCode);
            }
            String key =makeAppKeyEventData(event.keyCode, event.deviceId, event.eventType);
            mKeyEventData.put(key, event.funcId);
        }
    }

    public boolean shouldInterrupt(KeyEvent event) {
        try {
            if (App.get().isServiceOn && SettingsUtil.isServiceOn()) {
                return mKeyEventHandler.inputKeyEvent(event);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    public static String makeAppKeyEventData(int keyCode, int deviceId, AppKeyEventType type) {
        return String.format(Locale.getDefault(), "%d:%d:%s", keyCode, deviceId, type.getName());
    }

    public void onEvent(int keyCode, int deviceId, AppKeyEventType type) {
        String eventData = makeAppKeyEventData(keyCode, deviceId, type);
        if (mKeyEventData.containsKey(eventData)) {
            long funcId = mKeyEventData.get(eventData);
            IFunction function = FunctionFactory.getFuncById(funcId);
            if (function != null) {
                function.exec();
            }
        } else {
            Logger.e("function id not found.");
        }
    }

    public void attachToAccessibilityService(KeyEventService service) {
        mService = service;
    }

    public void detachFromAccessibilityService() {
        mService = null;
    }

    public void attachToNotificationService(NotificationCollectorService service) {
        mNotification = service;
    }

    public void detachFromNotificationService() {
        mNotification = null;
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
