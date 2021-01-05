package xyz.imxqd.clickclick;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.StringRes;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.HashSet;
import java.util.Set;

import me.weishu.reflection.Reflection;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.SettingsUtil;
import xyz.imxqd.luaframework.LuaEngine;

/**
 * Created by imxqd on 2017/11/24.
 */
public class MyApp extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "ClickClick";

    public static final int EVENT_WHAT_REFRESH_UI = 1;
    public static final int EVENT_WHAT_APP_SWITCH_CHANGED = 2;

    public boolean isServiceOn = true;

    private static MyApp mApp;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Set<AppEventCallback> mAppEventCallbacks = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(this, "11f099c63f", false);
        }
        FlowManager.init(this);
        LuaEngine.init(this);
        AppEventManager.getInstance().init(this);
        initLogger();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            Reflection.unseal(base);
        } catch (Throwable ignore) {}
    }

    public void initLogger() {
        Log.d(TAG, "initLogger");
        LogUtils.allowD = SettingsUtil.displayDebug();
        LogUtils.allowI = SettingsUtil.displayDebug();
        LogUtils.allowV = SettingsUtil.displayDebug();
    }

    public static MyApp get() {
        return mApp;
    }

    private Toast mToast;

    public void showToast(@StringRes final int str) {
        showToast(getString(str), false);
    }

    public void showToast(@StringRes final int str, final boolean show, final boolean center) {
        showToast(getString(str), show, center);
    }

    public void showToast(final String str, final boolean center) {
        mHandler.post(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(MyApp.get(), str, Toast.LENGTH_LONG);
            if (center) {
                mToast.setGravity(Gravity.CENTER, 0, 0);
            }
            mToast.show();
        });

    }

    public void showToast(final String str, final boolean show, final boolean center) {
        mHandler.post(() -> {
            if (show) {
                Toast t = Toast.makeText(MyApp.get(), str, Toast.LENGTH_LONG);
                if (center) {
                    t.setGravity(Gravity.CENTER, 0, 0);
                }
                t.show();
            } else {
                showToast(str, center);
            }

        });

    }

    public void toastCenter(final String str) {
        showToast(str, true);
    }

    public void toastImage(final Drawable drawable) {
        mHandler.post(() -> {
            Toast toast = new Toast(MyApp.get());
            ImageView imageView = new ImageView(MyApp.get());
            imageView.setImageDrawable(drawable);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            int width = getResources().getDimensionPixelSize(R.dimen.dimen_24_dp);
            int height = getResources().getDimensionPixelSize(R.dimen.dimen_24_dp);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
            imageView.setLayoutParams(params);

            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setView(imageView);
            toast.show();
        });

    }

    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof AppEventCallback) {
            mAppEventCallbacks.add((AppEventCallback) activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof AppEventCallback) {
            mAppEventCallbacks.remove(activity);
        }
    }


    public void post(int what, Object data) {
        for (AppEventCallback callback : mAppEventCallbacks) {
            callback.onEvent(what, data);
        }
    }

    public interface AppEventCallback {
        void onEvent(int what, Object data);
    }
}
