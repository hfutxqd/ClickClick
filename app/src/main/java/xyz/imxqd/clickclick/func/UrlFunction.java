package xyz.imxqd.clickclick.func;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;

import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.utils.PackageUtil;

public class UrlFunction extends AbstractFunction {
    public static final String PREFIX = "url";

    public UrlFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) throws Exception {
        if (PREFIX.equals(getPrefix())) {
            String url = getArgs();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            if (!PackageUtil.checkIntentForActivity(intent)) {
                throw new RuntimeException("no activity found");
            }
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(MyApp.get(), 0, intent, 0);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                LogUtils.e(e.getMessage());
                throw new RuntimeException("start activity failed. : " + e.getMessage());
            }
        } else {
            throw new RuntimeException("function prefix not match");
        }

    }
}
