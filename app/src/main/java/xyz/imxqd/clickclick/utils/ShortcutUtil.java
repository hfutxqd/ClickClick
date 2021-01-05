package xyz.imxqd.clickclick.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Process;
import androidx.annotation.RequiresApi;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.ArrayList;
import java.util.List;

import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.ui.NoDisplayActivity;

import static android.content.pm.LauncherApps.ShortcutQuery.*;

public class ShortcutUtil {

    public static final String ACTION_RUN = BuildConfig.APPLICATION_ID + ".run";

    public static void createRunFunc(long funcId, String name) {
        Intent shortcutIntent = new Intent(ACTION_RUN);
        shortcutIntent.setClass(MyApp.get(), NoDisplayActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shortcutIntent.putExtra(NoDisplayActivity.ARG_FUNC_ID, funcId);

        ShortcutInfoCompat infoCompat = new ShortcutInfoCompat.Builder(MyApp.get(), String.valueOf(funcId))
                .setIcon(IconCompat.createWithResource(MyApp.get(), R.mipmap.ic_launcher))
                .setIntent(shortcutIntent)
                .setShortLabel(name)
                .setLongLabel(name)
                .build();

        ShortcutManagerCompat.requestPinShortcut(MyApp.get(), infoCompat, null);
    }



    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static List<ShortcutInfo> getShortcuts(String packageName) {
        LauncherApps launcherApps = (LauncherApps) MyApp.get().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
        query.setQueryFlags(FLAG_MATCH_DYNAMIC | FLAG_MATCH_MANIFEST | FLAG_MATCH_PINNED);
        query.setPackage(packageName);
        try {
            return launcherApps.getShortcuts(query, Process.myUserHandle());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
