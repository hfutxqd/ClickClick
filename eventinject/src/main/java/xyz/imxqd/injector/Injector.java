package xyz.imxqd.injector;

import android.util.Log;

public class Injector {
    private static Events events;
    public static void sendKeyEvent(int keycode) {
        if (events == null) {
            synchronized (Injector.class) {
                events = new Events();
                events.Init();
            }
        }
        for (Events.InputDevice device : events.m_Devs) {
            Log.i("Injector", device.toString());
            if (device.getPath().equals("/dev/input/event2")) {
                device.Open(true);
                device.SendKey(keycode, true);
                device.SendKey(keycode, false);
                device.Close();
            }

        }

    }
}
