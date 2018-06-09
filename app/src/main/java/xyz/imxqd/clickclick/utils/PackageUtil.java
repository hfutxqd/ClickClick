package xyz.imxqd.clickclick.utils;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

import xyz.imxqd.clickclick.App;

public class PackageUtil {
    public static String getAppName(String packageName) throws PackageManager.NameNotFoundException {
        ApplicationInfo info = App.get().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        return App.get().getPackageManager().getApplicationLabel(info).toString();
    }

    public static boolean checkIntentForActivity(Intent intent) {
        List<ResolveInfo> list =  App.get().getPackageManager().queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }

    public static boolean checkIntentForService(Intent intent) {
        List<ResolveInfo> list =  App.get().getPackageManager().queryIntentServices(intent,PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }

    public static boolean checkIntentForBroadcastReceiver(Intent intent) {
        List<ResolveInfo> list = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            list = App.get().getPackageManager().queryBroadcastReceivers(intent, PackageManager.MATCH_ALL);
        } else {
            list =  App.get().getPackageManager().queryBroadcastReceivers(intent,PackageManager.MATCH_DEFAULT_ONLY);
        }
        return list != null && list.size() > 0;
    }
}
