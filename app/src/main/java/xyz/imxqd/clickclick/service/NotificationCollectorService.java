package xyz.imxqd.clickclick.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.LogUtils;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

/**
 * Created by imxqd on 2016/12/11.
 */

public class NotificationCollectorService extends NotificationListenerService {

    private static final String TAG = "NotificationService";
    private Toast mToast;

    private boolean isConnected = false;

    private ArrayList<Feedback> mFeedbackList = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isConnected) {
            return START_STICKY_COMPATIBILITY;
        }
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onListenerConnected() {
        LogUtils.d( "onListenerConnected");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_notification_service_success));
        }
        isConnected = true;
        AppEventManager.getInstance().attachToNotificationService(this);
    }

    @Override
    public void onListenerDisconnected() {
        LogUtils.d("onListenerDisconnected");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_notification_service_disconnected));
        }
        isConnected = false;
        AppEventManager.getInstance().detachFromNotificationService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d( "onListenerDisconnected");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_notification_service_disconnected));
        }
        isConnected = false;
        AppEventManager.getInstance().detachFromNotificationService();
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        LogUtils.d(sbn.getPackageName());
        for (Feedback feedback : mFeedbackList) {
            if (sbn.getPackageName().equals(feedback.packageName)) {
                Notification n = sbn.getNotification();
                if (n == null) {
                    return;
                }
                ArrayList list = new ArrayList();
                if (n.bigContentView != null) {
                    list.addAll(NotificationAccessUtil.getReflectionActions(n.bigContentView, feedback.methodName, feedback.viewId));
                }
                if (n.contentView != null) {
                    list.addAll(NotificationAccessUtil.getReflectionActions(n.contentView, feedback.methodName, feedback.viewId));
                }
                if (list.size() == 0) {
                    return;
                }
                if (feedback.callback != null) {
                    feedback.callback.onNotificationPosted(NotificationAccessUtil.getReflectionActionValue(list.get(0)));
                }
            }
        }

    }

    public List<Notification> getNotificationsByPackage(String packageName) {
        List<Notification> notifications = new ArrayList<>();
        StatusBarNotification[] ns = getActiveNotifications();
        if (ns == null) {
            return notifications;
        }
        for (StatusBarNotification sn : ns) {
            if (sn.getPackageName().equals(packageName)) {
                notifications.add(sn.getNotification());
            }
        }
        return notifications;
    }


    private boolean doAction(PendingIntent intent) {
        try {
            intent.send();
            return true;
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        LogUtils.d( "onNotificationRemoved");
    }

    private void showToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        mToast.show();
    }

    public void addFeedback(Feedback feedback) {
        mFeedbackList.add(feedback);
    }

    public void removeFeedback(Feedback feedback) {
        if (mFeedbackList.contains(feedback)) {
            mFeedbackList.remove(feedback);
        }
    }

    public static class Feedback {
        public String packageName;
        public String methodName;
        public int viewId;

        public Callback callback;

        public interface Callback {
            void onNotificationPosted(Object val);
        }

    }

}
