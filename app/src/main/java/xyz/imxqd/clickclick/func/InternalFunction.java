package xyz.imxqd.clickclick.func;

import android.content.ComponentName;
import android.content.Intent;

import xyz.imxqd.clickclick.App;

public class InternalFunction extends AbstractFunction {
    public static final String PREFIX = "internal";

    public InternalFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) {
        if ("wechat_scan".equals(args)) {
            startWeChatScan();
        }
    }

    public void cloudMusicLike() {
        // TODO: 2018/5/23  
    }

    public void qqMusicLike() {
        // TODO: 2018/5/23
    }

    public void startWeChatScan() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            intent.setAction(Intent.ACTION_VIEW);
            App.get().startActivity(intent);
        } catch (Exception e) {
        }
    }
}
