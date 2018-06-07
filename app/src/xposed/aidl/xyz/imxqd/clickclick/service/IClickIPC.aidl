// ClickIPC.aidl
package xyz.imxqd.clickclick.service;

import android.view.KeyEvent;

interface IClickIPC {

    void hello(in String str);

    boolean onKeyEvent(in KeyEvent event);

}
