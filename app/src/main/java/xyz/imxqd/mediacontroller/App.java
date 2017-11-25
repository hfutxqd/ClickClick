package xyz.imxqd.mediacontroller;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created by imxqd on 2017/11/24.
 */

public class App extends Application {

    public boolean isServiceOn = true;

    private static App mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        Logger.addLogAdapter(new AndroidLogAdapter());

    }

    public static App get() {
        return mApp;
    }


}
