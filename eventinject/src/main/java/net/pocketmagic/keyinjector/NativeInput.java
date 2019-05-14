package net.pocketmagic.keyinjector;

import java.lang.reflect.Method;

public class NativeInput {
    int m_fd;
    final static int EV_KEY = 0x01;

    public NativeInput() {
        intEnableDebug(1);
        m_fd = intCreate("/dev/input/event3", 1, 0);
    }

    public static int chmod(String path, int mode) throws Exception {
        Class fileUtils = Class.forName("android.os.FileUtils");
        Method setPermissions =
                fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
        return (Integer) setPermissions.invoke(null, path, mode, -1, -1);
    }
    public int SendKey(int key, boolean state) {
        if (state)
            return intSendEvent(m_fd, EV_KEY, key, 1); //key down
        else
            return intSendEvent(m_fd, EV_KEY, key, 0); //key up
    }



    native int		intEnableDebug(int enabled); 	//1 will output to logcat, 0 will disable
    //
    native int 		intCreate(String dev, int kb, int mouse);
    native void		intClose(int fd);
    native int		intSendEvent(int fd, int type, int code, int value);

    static {
        System.loadLibrary("input");
    }
}