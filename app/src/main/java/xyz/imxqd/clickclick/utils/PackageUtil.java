package xyz.imxqd.clickclick.utils;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

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

    public static int getPackageVersionCode(String packageName) {
        PackageInfo info = null;
        try {
            info = App.get().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }


    private static final String SCHEME = "package";

    public static void showInstalledAppDetails(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(SCHEME, packageName, null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.get().startActivity(intent);
    }

}
