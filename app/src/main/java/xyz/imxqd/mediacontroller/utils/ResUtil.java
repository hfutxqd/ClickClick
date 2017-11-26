package xyz.imxqd.mediacontroller.utils;

import android.support.annotation.StringRes;

import xyz.imxqd.mediacontroller.App;

/**
 * Created by imxqd on 2017/11/26.
 */

public class ResUtil {
    public static String getString(@StringRes int id) {
        return App.get().getResources().getString(id);
    }
}
