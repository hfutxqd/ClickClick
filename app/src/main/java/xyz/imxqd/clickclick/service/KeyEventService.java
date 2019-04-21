package xyz.imxqd.clickclick.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
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
        info.notificationTimeout = 0;
        setServiceInfo(info);
    }

    private void exitTouchExplorationMode() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        info.notificationTimeout = 0;
        setServiceInfo(info);
    }

    private boolean isNotificationWidget(AccessibilityNodeInfo source) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }
        source.refresh();
        if (source != source.getParent() && source.getParent() != null) {
            AccessibilityNodeInfo p = source;
            do {
                p = p.getParent();
                if (TextUtils.equals(p.getViewIdResourceName(), "com.android.systemui:id/notification_stack_scroller")
                         || TextUtils.equals(p.getViewIdResourceName(), "android:id/status_bar_latest_event_content")
                        || TextUtils.equals(p.getViewIdResourceName(), "com.android.systemui:id/expanded_notifications") ) {
                    return true;
                }
            } while (p.getParent() != null);

        } else {
            return false;
        }
        return false;
    }

    private boolean isNotificationAction(AccessibilityNodeInfo source) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }
        if (mClickCallbacks.size() > 0) {
            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        }
        if (TextUtils.equals(source.getViewIdResourceName(), "android:id/action0")) {
            return true;
        }
        AccessibilityNodeInfo parent = source.getParent();
        if (parent == null) {
            return false;
        }
        if (TextUtils.equals(parent.getViewIdResourceName(), "android:id/media_actions")) {
            return true;
        }

        AccessibilityNodeInfo parent2 = parent.getParent();
        if (parent2 == null) {
            return false;
        }
        if (TextUtils.equals(parent.getViewIdResourceName(), "android:id/actions")
                && TextUtils.equals(parent2.getViewIdResourceName(), "android:id/actions_container")) {
            return true;
        }
        return false;
    }

    private int getNotificationActionIndex(AccessibilityNodeInfo source) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return -1;
        }
        AccessibilityNodeInfo parent = source.getParent();
        if (TextUtils.equals(parent.getViewIdResourceName(), "android:id/actions") ||
                TextUtils.equals(parent.getViewIdResourceName(), "android:id/media_actions") ||
                TextUtils.equals(source.getViewIdResourceName(), "android:id/action0")) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (parent.getChild(i).equals(source)) {
                    return i;
                }
            }
        }
        return -1;
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED || event.getEventType() == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED) {
                AccessibilityNodeInfo source = event.getSource();
                if (source == null) {
                    LogUtils.d( "source was null for: " + event);
                } else {
                    if (isNotificationAction(source)) {
                        for (OnNotificationWidgetClick c : mClickCallbacks) {
                            c.onNotificationActionClick(source.getPackageName().toString(), getNotificationActionIndex(source));
                        }
                        LogUtils.d("notification action order : " + getNotificationActionIndex(source));
                    } else if (isNotificationWidget(source)) {
                        for (OnNotificationWidgetClick c : mClickCallbacks) {
                            if (TextUtils.equals("android:id/action0", source.getViewIdResourceName())) {
                                c.onNotificationActionClick(source.getPackageName().toString(), getNotificationActionIndex(source) + 1);
                            } else {
                                c.onNotificationWidgetClick(source.getPackageName().toString(), source.getViewIdResourceName());
                            }
                        }
                        String viewIdResourceName = source.getViewIdResourceName();
                        LogUtils.d( "viewid: " + viewIdResourceName);
                    } else {
                        LogUtils.d( "unknown");
                    }
                }

            }
//            else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) {
//                AccessibilityNodeInfo source = event.getSource();
//                if (source == null) {
//                    LogUtils.d( "source was null for: " + event);
//                } else {
//                    if (isNotificationAction(source)) {
//                        LogUtils.d("notification action order : " + getNotificationActionIndex(source));
//                    } else if (isNotificationWidget(source)) {
//                        for (OnNotificationWidgetClick c : mClickCallbacks) {
//                            c.onNotificationWidgetClick(source.getPackageName().toString(), source.getViewIdResourceName());
//                        }
//                        String viewIdResourceName = source.getViewIdResourceName();
//                        LogUtils.d( "viewid: " + viewIdResourceName);
//                    }
//                }
//            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) {
//                event.getSource().performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
//            }
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
        void onNotificationActionClick(String packageName, int index);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return AppEventManager.getInstance().shouldInterrupt(event);
    }

}
