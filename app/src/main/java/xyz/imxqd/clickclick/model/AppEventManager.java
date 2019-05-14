package xyz.imxqd.clickclick.model;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.KeyEvent;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;
import xyz.imxqd.clickclick.dao.KeyMappingEvent_Table;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.service.KeyEventService;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.utils.KeyEventHandler;
import xyz.imxqd.clickclick.utils.KeyEventUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

import static android.content.Context.AUDIO_SERVICE;

public class AppEventManager implements KeyEventHandler.Callback {

    private static AppEventManager sInstance;

    private AudioManager mAudioManager;
    private KeyEventHandler mKeyEventHandler;
    private KeyEventService mService;
    private NotificationCollectorService mNotification;
    private ButtonHandler mButtonHandler = new ButtonHandler();

    private Map<String, Long> mKeyEventData = new HashMap<>();

    private boolean isInputMode = false;

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
            initHomeButtonListener();
            updateClickTime();
            init = true;
        }
    }

    public KeyEventService getService() {
        if (SettingsUtil.isServiceOn()) {
            return mService;
        } else {
            return null;
        }
    }

    public void stopSelf() {
        if (mService != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mService.disableSelf();
            } else {
                mService.stopSelf();
            }
            mService = null;
        }

    }

    public AudioManager getAudioManager() {
        return mAudioManager;
    }

    public void refreshKeyMappingEvents() {
        updateKeyEventData();
        initHomeButtonListener();
    }

    public NotificationCollectorService getNotificationService() {
        if (SettingsUtil.isNotificationOn()) {
            return mNotification;
        } else {
            return null;
        }
    }

    public void updateClickTime() {
        KeyEventHandler.initClickTimes(SettingsUtil.getQuickClickTime(), SettingsUtil.getLongClickTime());
    }

    public void updateKeyEventData() {
        mKeyEventHandler.mLongClickKeyCodes.clear();
        mKeyEventHandler.mLongClickIgdKeyCodes.clear();
        mKeyEventHandler.mLongClickDevices.clear();

        mKeyEventHandler.mSingleClickKeyCodes.clear();
        mKeyEventHandler.mSingleClickIgdKeyCodes.clear();
        mKeyEventHandler.mSingleClickDevices.clear();

        mKeyEventHandler.mDoubleClickKeyCodes.clear();
        mKeyEventHandler.mDoubleClickIgdKeyCodes.clear();
        mKeyEventHandler.mDoubleClickDevices.clear();

        mKeyEventHandler.mTripleClickKeyCodes.clear();
        mKeyEventHandler.mTripleClickIgdKeyCodes.clear();
        mKeyEventHandler.mTripleClickDevices.clear();

        mKeyEventHandler.mInputModeKeyCodes.clear();
        mKeyEventHandler.mInputModeIgdKeyCodes.clear();
        mKeyEventHandler.mInputModeDevices.clear();


        mKeyEventData.clear();

        List<KeyMappingEvent> keyMappingEvents = KeyMappingEvent.getEnabledNormalItems();
        for (KeyMappingEvent event : keyMappingEvents) {
            if (event.eventType == AppKeyEventType.SingleClick) {
                if (event.ignoreDevice) {
                    mKeyEventHandler.mSingleClickIgdKeyCodes.add(event.keyCode);
                } else {
                    mKeyEventHandler.mSingleClickKeyCodes.add(event.keyCode);
                    mKeyEventHandler.mSingleClickDevices.add(event.deviceName);
                }

            } else if (event.eventType == AppKeyEventType.LongClick) {
                if (event.ignoreDevice) {
                    mKeyEventHandler.mLongClickIgdKeyCodes.add(event.keyCode);
                } else {
                    mKeyEventHandler.mLongClickKeyCodes.add(event.keyCode);
                    mKeyEventHandler.mLongClickDevices.add(event.deviceName);
                }

            } else if (event.eventType == AppKeyEventType.DoubleClick) {
                if (event.ignoreDevice) {
                    mKeyEventHandler.mDoubleClickIgdKeyCodes.add(event.keyCode);
                } else {
                    mKeyEventHandler.mDoubleClickKeyCodes.add(event.keyCode);
                    mKeyEventHandler.mDoubleClickDevices.add(event.deviceName);
                }

            } else if (event.eventType == AppKeyEventType.TripleClick) {
                if (event.ignoreDevice) {
                    mKeyEventHandler.mTripleClickIgdKeyCodes.add(event.keyCode);
                } else {
                    mKeyEventHandler.mTripleClickKeyCodes.add(event.keyCode);
                    mKeyEventHandler.mTripleClickDevices.add(event.deviceName);
                }

            }

            if (event.ignoreDevice) {
                String key = makeAppKeyEventData(event.keyCode, "*", event.eventType);
                mKeyEventData.put(key, event.funcId);
            } else {
                String key = makeAppKeyEventData(event.keyCode, event.deviceName, event.eventType);
                mKeyEventData.put(key, event.funcId);
            }

        }
        List<KeyMappingEvent> inputModeEvents = KeyMappingEvent.getEnabledInputModeItems();
        for (KeyMappingEvent event : inputModeEvents) {
            if (event.ignoreDevice) {
                mKeyEventHandler.mInputModeIgdKeyCodes.add(event.keyCode);
            } else {
                mKeyEventHandler.mInputModeKeyCodes.add(event.keyCode);
                mKeyEventHandler.mInputModeDevices.add(event.deviceName);
            }

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

    public void toggleInputMode() {
        this.isInputMode = !this.isInputMode;
        if (this.isInputMode) {
            App.get().showToast(R.string.input_mode_turn_on);
        } else {
            App.get().showToast(R.string.input_mode_turn_off);
        }

    }

    public boolean isInputMode() {
        return isInputMode;
    }

    public static String makeAppKeyEventData(int keyCode, String deviceName, AppKeyEventType type) {
        return String.format(Locale.getDefault(), "%d:%s:%s", keyCode, deviceName, type.getName());
    }

    public void onEvent(int keyCode, InputDevice device, AppKeyEventType type) {
        String eventKey = makeAppKeyEventData(keyCode, device.getName(), type);
        String eventKey2 = makeAppKeyEventData(keyCode, "*", type);
        String key = null;
        if (mKeyEventData.containsKey(eventKey)) {
            key = eventKey;
        } else if (mKeyEventData.containsKey(eventKey2)) {
            key = eventKey2;
        }
        if (key == null) {
            LogUtils.i("ignore " + keyCode);
            return;
        }
        LogUtils.i(key);
        Long funcId = mKeyEventData.get(key);
        if (funcId == null) {
            LogUtils.e("function id not found.");
            return;
        }
        if (funcId == -2) {
            toggleInputMode();
            return;
        }
        IFunction function = FunctionFactory.getFuncById(funcId);
        if (function != null) {
            function.exec();
        } else {

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
        try {
            KeyEventUtil.sendKeyEvent(event.getKeyCode());
        } catch (Throwable t) {
            LogUtils.e(t.getMessage());
        }
    }

    @Override
    public void onLongClick(KeyEvent event) {
        LogUtils.d(event.toString());
        if (SettingsUtil.displayDebug()) {
            showToast("onLongClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDevice(), AppKeyEventType.LongClick);
    }

    @Override
    public void onSingleClick(KeyEvent event) {
        LogUtils.d(event.toString());
        if (SettingsUtil.displayDebug()) {
            showToast("onSingleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDevice(), AppKeyEventType.SingleClick);
    }

    @Override
    public void onDoubleClick(KeyEvent event) {
        LogUtils.d(event.toString());
        if (SettingsUtil.displayDebug()) {
            showToast("onDoubleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDevice(), AppKeyEventType.DoubleClick);
    }

    @Override
    public void onTripleClick(KeyEvent event) {
        LogUtils.d(event.toString());
        if (SettingsUtil.displayDebug()) {
            showToast("onTripleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDevice(), AppKeyEventType.TripleClick);

    }

    private void showToast(String str) {
        App.get().showToast(str, false);
    }

    public static IFunction getHomeDoubleClickFunction(int button) {
        KeyMappingEvent event = new Select()
                .from(KeyMappingEvent.class)
                .where(KeyMappingEvent_Table.enable.eq(true))
                .and(KeyMappingEvent_Table.device_id.eq(-1))
                .and(KeyMappingEvent_Table.event_type.eq(AppKeyEventType.DoubleClick))
                .and(KeyMappingEvent_Table.key_code.eq(button))
                .querySingle();
        if (event != null) {
            return FunctionFactory.getFuncById(event.funcId);
        }
        return null;
    }

    public static IFunction getHomeTripleClickFunction(int button) {
        KeyMappingEvent event = new Select()
                .from(KeyMappingEvent.class)
                .where(KeyMappingEvent_Table.enable.eq(true))
                .and(KeyMappingEvent_Table.device_id.eq(-1))
                .and(KeyMappingEvent_Table.event_type.eq(AppKeyEventType.TripleClick))
                .and(KeyMappingEvent_Table.key_code.eq(button))
                .querySingle();
        if (event != null) {
            return FunctionFactory.getFuncById(event.funcId);
        }
        return null;
    }


    private void initHomeButtonListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        App.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TextUtils.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS, action)) {
                    String reason = intent.getStringExtra("reason");
                    if (reason != null) {
                        if (reason.equals("homekey")) {
                            // Home
                            LogUtils.d("homekey");
                            mButtonHandler.handle(KeyEvent.KEYCODE_HOME);
                        } else if (reason.equals("recentapps")) {
                            // Recent
                            LogUtils.d("recentapps");
                            mButtonHandler.handle(KeyEvent.KEYCODE_APP_SWITCH);
                        }
                    }
                }
            }
        }, filter);
        mButtonHandler.setCallback(new ButtonHandler.Callback() {
            @Override
            public void onDoubleClick(int button) {
                LogUtils.d("onDoubleClick " + button);
                App.get().getHandler().post(() -> {
                    IFunction f = getHomeDoubleClickFunction(button);
                    if (f != null) {
                        f.exec();
                    }
                });
            }

            @Override
            public void onTripleClick(int button) {
                LogUtils.d("onTripleClick " + button);
                App.get().getHandler().post(() -> {
                    IFunction f = getHomeTripleClickFunction(button);
                    if (f != null) {
                        f.exec();
                    }
                });
            }
        });
    }

    public static class ButtonHandler {
        private static final int WHAT_BUTTON_HANDLER = 1223;

        private Callback callback;
        private int lastButton = 0;
        private int count = 0;
        public void handle(int button) {
            if (lastButton == button) {
                count++;
                if (hasOnlyDoubleClick(button) && count == 2) {
                    App.get().getHandler().removeMessages(WHAT_BUTTON_HANDLER);
                    if (callback != null) {
                        callback.onDoubleClick(lastButton);
                    }
                    reset();
                } else if (count == 3) {
                    App.get().getHandler().removeMessages(WHAT_BUTTON_HANDLER);
                    if (callback != null) {
                        callback.onTripleClick(lastButton);
                    }
                    reset();
                }
            } else {
                count = 1;
                lastButton = button;
                App.get().getHandler().removeMessages(WHAT_BUTTON_HANDLER);
                Message msg = Message.obtain(App.get().getHandler(), () -> {
                    App.get().getHandler().removeMessages(WHAT_BUTTON_HANDLER);
                    if (callback != null) {
                        if (count == 2) {
                            callback.onDoubleClick(lastButton);
                        } else if (count == 3) {
                            callback.onTripleClick(lastButton);
                        }
                    }
                    reset();
                });
                msg.what = WHAT_BUTTON_HANDLER;
                App.get().getHandler().sendMessageDelayed(msg, SettingsUtil.getQuickClickTime());
            }
        }

        public boolean hasOnlyDoubleClick(int button) {
            return getHomeTripleClickFunction(button) == null;
        }

        private void reset() {
            count = 0;
            lastButton = 0;
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }

        public interface Callback {
            void onDoubleClick(int button);
            void onTripleClick(int button);
        }
    }
}
