package xyz.imxqd.clickclick.utils;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.log.LogUtils;

public class ResourceUtl {
    public static int getIdByName(String packageName, String idName) {
        try {
            PackageManager manager = MyApp.get().getPackageManager();
            Resources mApk1Resources = manager.getResourcesForApplication(packageName);

            int id = mApk1Resources.getIdentifier(idName, "id",packageName);
            LogUtils.d(packageName + "@" + idName + "=" + id);
            return id;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Drawable getDrawableById(String packageName, int resId) {
        try {
            PackageManager manager = MyApp.get().getPackageManager();
            Resources mApk1Resources = manager.getResourcesForApplication(packageName);
            return mApk1Resources.getDrawable(resId);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
