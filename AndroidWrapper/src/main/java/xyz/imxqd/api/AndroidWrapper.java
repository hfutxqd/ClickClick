package xyz.imxqd.api;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.ArraySet;

public class AndroidWrapper {

    public static ArraySet<PendingIntent> getAllPendingIntent(Notification n) {
        return n.allPendingIntents;
    }

    public static Intent getIntent(PendingIntent pendingIntent) {
        return pendingIntent.getIntent();
    }

    public static void startActivityAsUserId(Context context, Intent intent, int uid) {
        context.startActivityAsUser(intent, new UserHandle(uid));
    }
}
