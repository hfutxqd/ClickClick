package xyz.imxqd.luaframework.core.model;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import cn.vimfung.luascriptcore.LuaExportType;
import xyz.imxqd.luaframework.LuaEngine;
import xyz.imxqd.luaframework.R;

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

    public void openAppSetting(String pkg) {

    }

    public void openFile(String path) {

    }

    public void editFile(String path) {

    }

    public void uninstall(String pkg) {

    }

    public void openUrl(String url) {

    }

    public void sendEmail(String email, String title, String content) {

    }

    public void startActivity(Option intent) {

    }

    public void sendBroadcast(Option intent) {

    }

    public void startService(Option intent) {

    }

    public String getAppName(String pkg) {
        return null;
    }

}
