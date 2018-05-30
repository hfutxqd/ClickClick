package xyz.imxqd.clickclick.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import xyz.imxqd.clickclick.App;

public class PackageUtil {
    public static String getAppName(String packageName) throws PackageManager.NameNotFoundException {
        ApplicationInfo info = App.get().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        return App.get().getPackageManager().getApplicationLabel(info).toString();
    }
}
