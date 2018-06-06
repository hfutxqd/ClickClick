package xyz.imxqd.clickclick.utils;

import android.content.Intent;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.ui.AlertDialogActivity;

public class AlertUtil {

    public static void showNotify(String packageName, String id) {
        Intent intent = new Intent(App.get(), AlertDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlertDialogActivity.ARG_TYPE, AlertDialogActivity.TYPE_NOTIFY);
        intent.putExtra(AlertDialogActivity.ARG_NOTIFY_PACKAGE, packageName);
        intent.putExtra(AlertDialogActivity.ARG_NOTIFY_VIEW_ID, id);
        App.get().startActivity(intent);
    }


    public static void show(String message) {
        Intent intent = new Intent(App.get(), AlertDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlertDialogActivity.ARG_TYPE, AlertDialogActivity.TYPE_NORMAL);
        intent.putExtra(AlertDialogActivity.ARG_NORMAL_MESSAGE, message);
        App.get().startActivity(intent);
    }
}
