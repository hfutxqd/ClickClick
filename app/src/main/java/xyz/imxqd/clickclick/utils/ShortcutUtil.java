package xyz.imxqd.clickclick.utils;

import android.content.Intent;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.ui.NaviActivity;
import xyz.imxqd.clickclick.ui.NoDisplayActivity;

public class ShortcutUtil {

    public static final String ACTION_RUN = "xyz.imxqd.clicklick.run";

    public static void createRunFunc(long funcId, String name) {
        Intent shortcutIntent = new Intent(ACTION_RUN);
        shortcutIntent.setClass(App.get(), NoDisplayActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shortcutIntent.putExtra(NoDisplayActivity.ARG_FUNC_ID, funcId);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(App.get(), R.mipmap.ic_launcher));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        App.get().sendBroadcast(addIntent);
    }
}
