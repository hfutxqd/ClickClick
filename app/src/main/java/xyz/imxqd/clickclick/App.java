package xyz.imxqd.clickclick;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.raizlabs.android.dbflow.config.FlowManager;

import xyz.imxqd.clickclick.model.AppEventManager;

/**
 * Created by imxqd on 2017/11/24.
 */

public class App extends Application {

    public boolean isServiceOn = true;

    private static App mApp;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        FlowManager.init(this);
        AppEventManager.getInstance().init(this);
        Logger.addLogAdapter(new AndroidLogAdapter());

    }

    public static App get() {
        return mApp;
    }

    private Toast mToast;

    public void showToast(@StringRes final int str) {
        showToast(getString(str));
    }

    public void showToast(final String str) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(App.get(), str, Toast.LENGTH_LONG);
                mToast.show();
            }
        });

    }

    public void showToast(final String str, final boolean show) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    Toast.makeText(App.get(), str, Toast.LENGTH_LONG).show();
                } else {
                    showToast(str);
                }

            }
        });

    }
}
