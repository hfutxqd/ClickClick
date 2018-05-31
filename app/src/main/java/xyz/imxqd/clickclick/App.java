package xyz.imxqd.clickclick;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.raizlabs.android.dbflow.config.FlowManager;

import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.LogUtils;
import xyz.imxqd.clickclick.utils.SettingsUtil;

/**
 * Created by imxqd on 2017/11/24.
 */

public class App extends Application {

    private static final String TAG = "ClickClick";

    public boolean isServiceOn = true;

    private static App mApp;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        FlowManager.init(this);
        AppEventManager.getInstance().init(this);
        initLogger();
    }

    public void initLogger() {
        Log.d(TAG, "initLogger");
        LogUtils.allowD = SettingsUtil.displayDebug();
        LogUtils.allowI = SettingsUtil.displayDebug();
        LogUtils.allowV = SettingsUtil.displayDebug();
    }

    public static App get() {
        return mApp;
    }

    private Toast mToast;

    public void showToast(@StringRes final int str) {
        showToast(getString(str), false);
    }

    public void showToast(final String str, final boolean center) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(App.get(), str, Toast.LENGTH_LONG);
                if (center) {
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                }
                mToast.show();
            }
        });

    }

    public void showToast(final String str, final boolean show, final boolean center) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    Toast t = Toast.makeText(App.get(), str, Toast.LENGTH_LONG);
                    if (center) {
                        t.setGravity(Gravity.CENTER, 0, 0);
                    }
                    t.show();
                } else {
                    showToast(str, center);
                }

            }
        });

    }

    public void toastCenter(final String str) {

    }

    public void toastImage(final Drawable drawable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = new Toast(App.get());
                ImageView imageView = new ImageView(App.get());
                imageView.setImageDrawable(drawable);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                int width = getResources().getDimensionPixelSize(R.dimen.dimen_24_dp);
                int height = getResources().getDimensionPixelSize(R.dimen.dimen_24_dp);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
                imageView.setLayoutParams(params);

                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setView(imageView);
                toast.show();
            }
        });

    }

    public Handler getHandler() {
        return mHandler;
    }
}
