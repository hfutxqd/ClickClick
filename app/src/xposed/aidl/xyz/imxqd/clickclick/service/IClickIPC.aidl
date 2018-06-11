// ClickIPC.aidl
package xyz.imxqd.clickclick.service;

import android.view.KeyEvent;
import xyz.imxqd.clickclick.service.IClickCallback;

interface IClickIPC {

    void hello();

    boolean onKeyEvent(in KeyEvent event);

    void registerCallback(IClickCallback callback);

    void unregisterCallback();

}
