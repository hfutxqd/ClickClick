package xyz.imxqd.clickclick;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.raizlabs.android.dbflow.config.FlowManager;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

import java.util.HashSet;
import java.util.Set;

import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.SettingsUtil;
import xyz.imxqd.clickclick.utils.SystemSettingsUtl;

/**
 * Created by imxqd on 2017/11/24.
 */
@AcraCore(buildConfigClass = BuildConfig.class, reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraMailSender(mailTo = "ahtlxqd@gmail.com", reportFileName = "clickclick_app_crash_log.txt", resSubject = R.string.app_crashed)
@AcraDialog(resTitle = R.string.app_crashed,resText = R.string.app_crashed_dialog_text,resCommentPrompt = R.string.app_crashed_dialog_comment)
public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "ClickClick";

    public static final int EVENT_WHAT_REFRESH_UI = 1;

    public boolean isServiceOn = true;

    private static App mApp;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Set<AppEventCallback> mAppEventCallbacks = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        FlowManager.init(this);
        AppEventManager.getInstance().init(this);
        initLogger();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
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

    public void showToast(@StringRes final int str, final boolean show, final boolean center) {
        showToast(getString(str), show, center);
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
        if (activity instanceof AppEventCallback && mAppEventCallbacks.contains(activity)) {
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
