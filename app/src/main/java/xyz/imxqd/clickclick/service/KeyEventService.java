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

import java.util.List;

import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public class KeyEventService extends AccessibilityService {

    private Toast mToast;

    @Override
    protected void onServiceConnected() {

        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_service_success));
        }
        AppEventManager.getInstance().attachToAccessibilityService(this);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source == null) {
                Log.d("onAccessibilityEvent", "source was null for: " + event);
            } else {
                source.refresh();
                if (source != source.getParent() && source.getParent() != null && !TextUtils.equals(source.getPackageName(), "com.android.systemui")) {
                    AccessibilityNodeInfo p = source;
                    do {
                        p = p.getParent();
                    } while (p.getParent() != null);
                    Log.d("onAccessibilityEvent", "root parent is " + p.getPackageName());
                }
                String viewIdResourceName = source.getViewIdResourceName();
                Log.d("onAccessibilityEvent", "viewid: " + viewIdResourceName);
            }

        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
        }
    }

    @Override
    public void onInterrupt() {
        Logger.d("onInterrupt");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_service_interrupt));
        }
        AppEventManager.getInstance().detachFromAccessibilityService();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return AppEventManager.getInstance().shouldInterrupt(event);
    }


    private void showToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        mToast.show();
    }
}
