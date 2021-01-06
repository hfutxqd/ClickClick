package xyz.imxqd.clickclick.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.log.LogUtils;
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

    private List<Notification> findNotificationsByPackage(String packageName) {
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

    public static List<Notification> getNotificationsByPackage(String packageName) {
        NotificationCollectorService service =  AppEventManager.getInstance().getNotificationService();
        if (service != null) {
            return service.findNotificationsByPackage(packageName);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            NotificationManager notificationManager = (NotificationManager) MyApp.get().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager == null) {
                return new ArrayList<>();
            }
            StatusBarNotification[] ns = notificationManager.getActiveNotifications();
            List<Notification> notifications = new ArrayList<>();
            if (ns == null) {
                return notifications;
            }
            for (StatusBarNotification sn : ns) {
                if (sn.getPackageName().equals(packageName)) {
                    notifications.add(sn.getNotification());
                }
            }
            return notifications;
        } else {
            return new ArrayList<>();
        }
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

    }

    private void showToast(String str) {
        if (Looper.myLooper() == null) {
            return;
        }
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
