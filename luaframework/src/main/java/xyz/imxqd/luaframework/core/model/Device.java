package xyz.imxqd.luaframework.core.model;

import android.os.Build;

import cn.vimfung.luascriptcore.LuaExportType;

public class Device implements LuaExportType {
    public Device(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final int width;
    public final int height;
    public final String buildId = Build.ID;
    public final String broad = Build.BOARD;
    public final String brand = Build.BRAND;
    public final String device = Build.DEVICE;
    public final String model = Build.MODEL;
    public final String product = Build.PRODUCT;
    public final String bootloader = Build.BOOTLOADER;
    public final String hardware = Build.HARDWARE;
    public final String fingerprint = Build.FINGERPRINT;
    public final int sdkInt = Build.VERSION.SDK_INT;
    public final String incremental = Build.VERSION.INCREMENTAL;
    public final String release = Build.VERSION.RELEASE;

}
