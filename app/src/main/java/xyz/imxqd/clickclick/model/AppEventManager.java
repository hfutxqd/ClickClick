package xyz.imxqd.clickclick.model;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;
import xyz.imxqd.clickclick.dao.KeyMappingEvent_Table;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;
import xyz.imxqd.clickclick.service.KeyEventService;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.utils.KeyEventHandler;
import xyz.imxqd.clickclick.utils.KeyEventUtil;
import xyz.imxqd.clickclick.log.LogUtils;
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
    private ButtonHandler mButtonHandler = new ButtonHandler();

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

    public AudioManager getAudioManager() {
        return mAudioManager;
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
        mKeyEventHandler.mSingleClickKeyCodes.clear();
        mKeyEventHandler.mDoubleClickKeyCodes.clear();
        mKeyEventHandler.mTripleClickKeyCodes.clear();
        mKeyEventData.clear();

        List<KeyMappingEvent> keyMappingEvents = KeyMappingEvent.getEnabledNormalItems();
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
            LogUtils.e("function id not found.");
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
        LogUtils.d(event.toString());
        if (SettingsUtil.displayDebug()) {
            showToast("onLongClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDeviceId(), AppKeyEventType.LongClick);
    }

    @Override
    public void onSingleClick(KeyEvent event) {
        LogUtils.d(event.toString());
        if (SettingsUtil.displayDebug()) {
            showToast("onSingleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDeviceId(), AppKeyEventType.SingleClick);
    }

    @Override
    public void onDoubleClick(KeyEvent event) {
        LogUtils.d(event.toString());
        if (SettingsUtil.displayDebug()) {
            showToast("onDoubleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        onEvent(event.getKeyCode(), event.getDeviceId(), AppKeyEventType.DoubleClick);
    }

    @Override
    public void onTripleClick(KeyEvent event) {
        LogUtils.d(event.toString());
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

    public IFunction getHomeDoubleClickFunction(int button) {
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

    public IFunction getHomeTripleClickFunction(int button) {
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
                IFunction f = getHomeDoubleClickFunction(button);
                if (f != null) {
                    f.exec();
                }
            }

            @Override
            public void onTripleClick(int button) {
                LogUtils.d("onTripleClick " + button);
                IFunction f = getHomeTripleClickFunction(button);
                if (f != null) {
                    f.exec();
                }
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
                if (hasOnlyDoubleClick() && count == 2) {
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

        public boolean hasOnlyDoubleClick() {
            return false;
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
