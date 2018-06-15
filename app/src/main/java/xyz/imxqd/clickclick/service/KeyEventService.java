package xyz.imxqd.clickclick.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashSet;
import java.util.Set;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public class KeyEventService extends AccessibilityService {

    private static final String TAG = "KeyEventService";

    private Set<OnNotificationWidgetClick> mClickCallbacks = new HashSet<>();

    @Override
    protected void onServiceConnected() {

        LogUtils.d("time = " + System.currentTimeMillis());
        if (SettingsUtil.displayDebug()) {
            App.get().showToast(getString(R.string.open_service_success), true);
        }
        exitTouchExplorationMode();
        AppEventManager.getInstance().attachToAccessibilityService(this);
    }

    private void enterTouchExplorationMode() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }

    private void exitTouchExplorationMode() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }

    private boolean isNotificationWidget(AccessibilityNodeInfo source) {
        source.refresh();
        if (source != source.getParent() && source.getParent() != null && !TextUtils.equals(source.getPackageName(), "com.android.systemui")) {
            AccessibilityNodeInfo p = source;
            do {
                LogUtils.d(p.getViewIdResourceName());
                p = p.getParent();
            } while (p.getParent() != null);
            LogUtils.d( "root parent is " + p.getPackageName());
            if (TextUtils.equals(p.getPackageName(), "com.android.systemui")) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                AccessibilityNodeInfo source = event.getSource();
                if (source == null) {
                    LogUtils.d( "source was null for: " + event);
                } else {
                    if (isNotificationWidget(source)) {
                        for (OnNotificationWidgetClick c : mClickCallbacks) {
                            c.onNotificationWidgetClick(source.getPackageName().toString(), source.getViewIdResourceName());
                        }
                        String viewIdResourceName = source.getViewIdResourceName();
                        LogUtils.d( "viewid: " + viewIdResourceName);
                    }
                }

            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) {
                AccessibilityNodeInfo source = event.getSource();
                if (source == null) {
                    LogUtils.d( "source was null for: " + event);
                } else {
                    if (isNotificationWidget(source)) {
                        for (OnNotificationWidgetClick c : mClickCallbacks) {
                            c.onNotificationWidgetClick(source.getPackageName().toString(), source.getViewIdResourceName());
                        }
                        String viewIdResourceName = source.getViewIdResourceName();
                        LogUtils.d( "viewid: " + viewIdResourceName);
                        exitTouchExplorationMode();
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
        }
    }

    @Override
    public void onInterrupt() {
        LogUtils.d("time = " + System.currentTimeMillis());
        if (SettingsUtil.displayDebug()) {
            App.get().showToast(getString(R.string.open_service_interrupt), true);
        }
        AppEventManager.getInstance().detachFromAccessibilityService();
        stopSelf();
    }

    public void addOnNotificationWidgetClickCallback(OnNotificationWidgetClick callback) {
        if (callback != null) {
//            enterTouchExplorationMode();
            mClickCallbacks.add(callback);
        }
    }

    public void removeOnNotificationWidgetClickCallback(OnNotificationWidgetClick callback) {
        if (callback != null && mClickCallbacks.contains(callback)) {
//            exitTouchExplorationMode();
            mClickCallbacks.remove(callback);
        }
    }

    public interface OnNotificationWidgetClick {
        void onNotificationWidgetClick(String packageName, String viewId);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return AppEventManager.getInstance().shouldInterrupt(event);
    }

}
