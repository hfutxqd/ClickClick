package xyz.imxqd.clickclick.utils;

import java.lang.reflect.Field;

import xyz.imxqd.clickclick.log.LogUtils;

public class ProcessUtils {

    public static int getProcessPid(Process process) {
        try {
            Field pid = process.getClass().getDeclaredField("pid");
            pid.setAccessible(true);
            return (int) pid.get(process);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage());
            return -1;
        }
    }
}
