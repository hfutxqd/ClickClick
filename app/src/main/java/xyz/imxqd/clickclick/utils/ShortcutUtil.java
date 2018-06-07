package xyz.imxqd.clickclick.utils;

import android.content.Intent;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.ui.NoDisplayActivity;

public class ShortcutUtil {

    public static final String ACTION_RUN = BuildConfig.APPLICATION_ID + ".run";

    public static void createRunFunc(long funcId, String name) {
        Intent shortcutIntent = new Intent(ACTION_RUN);
        shortcutIntent.setClass(App.get(), NoDisplayActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shortcutIntent.putExtra(NoDisplayActivity.ARG_FUNC_ID, funcId);

        ShortcutInfoCompat infoCompat = new ShortcutInfoCompat.Builder(App.get(), String.valueOf(funcId))
                .setIcon(IconCompat.createWithResource(App.get(), R.mipmap.ic_launcher))
                .setIntent(shortcutIntent)
                .setShortLabel(name)
                .setLongLabel(name)
                .build();

        ShortcutManagerCompat.requestPinShortcut(App.get(), infoCompat, null);
    }
}
