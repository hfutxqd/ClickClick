package xyz.imxqd.clickclick.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

/**
 * Created by imxqd on 2016/12/11.
 */

public class NotificationCollectorService extends NotificationListenerService {

    private static final String TAG = "NotificationService";
    private Toast mToast;

    private boolean isConnected = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isConnected) {
            return START_STICKY_COMPATIBILITY;
        }
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "onListenerConnected");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_notification_service_success));
        }
        isConnected = true;
        AppEventManager.getInstance().attachToNotificationService(this);
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "onListenerDisconnected");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_notification_service_disconnected));
        }
        isConnected = false;
        AppEventManager.getInstance().detachFromNotificationService();
    }


    // 0 暂停
    // 1 下一曲
    // 2 关闭
    // 3 歌词
    // 4 喜爱
    //测试版本
    //网易云音乐 5.2.0.437608
    //
    //喜爱按钮
    //mix2s: id 2131822537
    //
    //oneplus3 : id 2131822537
    //
    //2130838697 未喜爱
    //2130838699 喜爱
    //
    //
    //播放按钮
    //id 2131822539
    //正在播放（暂停按钮） 2130838709
    //暂停状态（播放按钮） 2130838712



    @Override
    public void onNotificationPosted(StatusBarNotification sbn) { Log.d(TAG, "onNotificationPosted : " + sbn.getPackageName());
        System.out.println(NotificationAccessUtil.getReflectionActions(sbn.getNotification().bigContentView, "setImageResource", 2131822537));
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
        Log.d(TAG, "onNotificationRemoved");
    }

    private void showToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        mToast.show();
    }

}
