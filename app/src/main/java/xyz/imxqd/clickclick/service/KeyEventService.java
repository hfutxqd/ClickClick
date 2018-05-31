package xyz.imxqd.clickclick.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public class KeyEventService extends AccessibilityService {

    private static final String TAG = "KeyEventService";

    private Set<OnNotificationWidgetClick> mClickCallbacks = new HashSet<>();

    @Override
    protected void onServiceConnected() {

        if (SettingsUtil.displayDebug()) {
            App.get().showToast(getString(R.string.open_service_success), true);
        }
        AppEventManager.getInstance().attachToAccessibilityService(this);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                AccessibilityNodeInfo source = event.getSource();
                if (source == null) {
                    Log.d(TAG, "source was null for: " + event);
                } else {
                    source.refresh();
                    if (source != source.getParent() && source.getParent() != null && !TextUtils.equals(source.getPackageName(), "com.android.systemui")) {
                        AccessibilityNodeInfo p = source;
                        do {
                            p = p.getParent();
                        } while (p.getParent() != null);
                        Log.d(TAG, "root parent is " + p.getPackageName());
                        if (TextUtils.equals(p.getPackageName(), "com.android.systemui")) {
                            for (OnNotificationWidgetClick c : mClickCallbacks) {
                                c.onNotificationWidgetClick(source.getPackageName().toString(), source.getViewIdResourceName());
                            }
                            String viewIdResourceName = source.getViewIdResourceName();
                            Log.d(TAG, "viewid: " + viewIdResourceName);
                        }
                    }
                }

            } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        Logger.d("onInterrupt");
        if (SettingsUtil.displayDebug()) {
            App.get().showToast(getString(R.string.open_service_interrupt), true);
        }
        AppEventManager.getInstance().detachFromAccessibilityService();
    }

    public void addOnNotificationWidgetClickCallback(OnNotificationWidgetClick callback) {
        if (callback != null) {
            mClickCallbacks.add(callback);
        }
    }

    public void removeOnNotificationWidgetClickCallback(OnNotificationWidgetClick callback) {
        if (callback != null && mClickCallbacks.contains(callback)) {
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
