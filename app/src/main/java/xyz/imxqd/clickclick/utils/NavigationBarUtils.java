package xyz.imxqd.clickclick.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import xyz.imxqd.clickclick.log.LogUtils;

public class NavigationBarUtils {
    private static int NAVIGATION_BAR_TRANSIENT = -1;
    private static int NAVIGATION_BAR_TRANSLUCENT = -1;
    private static int NAVIGATION_BAR_TRANSPARENT = -1;
    private static Method hasNavigationBar;
    private static Method getInitialDisplayDensity;
    private static Class<?> WindowManagerGlobal;
    private static Method getWindowManagerService;

    static {
        try {
            Class<?> IWindowManager = Class.forName("android.view.IWindowManager");
            hasNavigationBar = IWindowManager.getDeclaredMethod("hasNavigationBar");
            getInitialDisplayDensity = IWindowManager.getDeclaredMethod("getInitialDisplayDensity", int.class);
            WindowManagerGlobal = Class.forName("android.view.WindowManagerGlobal");
            getWindowManagerService = WindowManagerGlobal.getDeclaredMethod("getWindowManagerService");
        } catch (Exception e) {
            LogUtils.w(String.valueOf(e));
        }
    }

    private static int getFieldInt(String name) {
        try {
            Field field = View.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.getInt(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void init() {
        if (NAVIGATION_BAR_TRANSPARENT == -1) {
            NAVIGATION_BAR_TRANSIENT = getFieldInt("NAVIGATION_BAR_TRANSIENT");
            NAVIGATION_BAR_TRANSLUCENT = getFieldInt("NAVIGATION_BAR_TRANSLUCENT");
            NAVIGATION_BAR_TRANSPARENT = getFieldInt("NAVIGATION_BAR_TRANSPARENT");
        }
    }

    public static void hideNavigationBar(@NonNull Activity activity) {
        if (activity == null) {
            return;
        }
        int visibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        int flag = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        activity.getWindow().getDecorView().setSystemUiVisibility(visibility | flag);
    }

    public static void showNavigationBar(@NonNull Activity activity) {
        if (activity == null) {
            return;
        }
        int visibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        int flag = ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        activity.getWindow().getDecorView().setSystemUiVisibility(visibility & flag);
    }


    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }


    public static void setNavigationBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color);
        }
    }

    /**
     * 从WMS判断是否带有导航栏
     *
     * @return
     */
    public static boolean hasNavigationBar() {
        if (WindowManagerGlobal != null && getWindowManagerService != null) {
            try {
                Object windowManager = getWindowManagerService.invoke(null);
                return (boolean) hasNavigationBar.invoke(windowManager);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogUtils.e(e.toString());
            }
        }
        return false;
    }


    /**
     * 判断底部navigator是否已经显示
     * @param windowManager
     * @return
     */
    private static boolean displayNavigationBar(WindowManager windowManager){
        Display d = windowManager.getDefaultDisplay();


        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);


        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;


        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);


        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;


        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    /**
     * 设置导航栏完全透明
     *
     * @param window
     */
    public static void setTransparent(Window window) {
        if (Build.VERSION.SDK_INT > 23) {
            init();
            View decorView = window.getDecorView();
            int flag = decorView.getSystemUiVisibility();
            flag |= NAVIGATION_BAR_TRANSPARENT;
            flag &= ~NAVIGATION_BAR_TRANSIENT;
            flag &= ~NAVIGATION_BAR_TRANSLUCENT;
            decorView.setSystemUiVisibility(flag);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
