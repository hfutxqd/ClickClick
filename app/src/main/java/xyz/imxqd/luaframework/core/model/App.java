package xyz.imxqd.luaframework.core.model;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import cn.vimfung.luascriptcore.LuaExportType;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.luaframework.LuaEngine;

public class App implements LuaExportType {

    private static App sApp;

    private App() {
    }

    public static App getInstance() {
        if (sApp == null) {
            synchronized (App.class) {
                sApp = new App();
            }
        }
        return sApp;
    }

    public boolean launch(String pkg) {
        Log.i("lua:app", "launch:" + pkg);
        PackageManager pm = LuaEngine.getGlobalContext().getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(pkg);
        if (intent != null) {
            LuaEngine.getGlobalContext().startActivity(intent);
            return true;
        } else {
            Toast.makeText(LuaEngine.getGlobalContext(), R.string.pkg_not_found, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public boolean openAppSetting(String pkg) {
        Log.i("lua:app", "openAppSetting:" + pkg);
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + pkg));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            LuaEngine.getGlobalContext().startActivity(i);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean openFile(String path) {
        Log.i("lua:app", "openFile:" + path);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.fromFile(new File(path)));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            LuaEngine.getGlobalContext().startActivity(i);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean uninstall(String pkg) {
        return false;
    }

    public boolean openUrl(String url) {
        return false;
    }

    public boolean sendEmail(String email, String title, String content) {
        return false;
    }

    public boolean startActivity(Option intent) {
        return false;
    }

    public boolean sendBroadcast(Option intent) {
        return false;
    }

    public boolean startService(Option intent) {
        return false;
    }

    public String getAppName(String pkg) {
        return null;
    }

}
