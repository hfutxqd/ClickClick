package xyz.imxqd.clickclick.func;

import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.utils.PackageUtil;

public class ActionFunction extends AbstractFunction {
    public static final String PREFIX = "action";

    public ActionFunction(String funcData) {
        super(funcData);
    }

    private String getAction(String args) {
        return args;
    }

    private JSONObject getIntentArgs(String args) {
        return new JSONObject();
    }


    public Intent getIntent(String prefix, String args) {
        try {
            Matcher matcher = Pattern.compile(prefix + "(.+)").matcher(args);
            if (matcher.find()) {
                return Intent.parseUri(matcher.group(1), 0);
            } else {
                throw new RuntimeException("intent uri: Syntax Error");
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("intent uri: Syntax Error");
        }
    }

    private boolean isPureIntent(String args) {
        if (TextUtils.isEmpty(args) || args.length() < 10) {
            return false;
        }
        return args.startsWith("intent://");
    }

    public Intent getPureIntent(String args) {
        return getIntent("intent://", args);
    }

    private boolean isActionIntent(String args) {
        if (TextUtils.isEmpty(args) || args.length() < 10) {
            return false;
        }
        return args.startsWith("action://");
    }

    private Intent getActionIntent(String args) {
        return new Intent(args.substring(9));
    }

    private boolean isBroadcastIntent(String args) {
        if (TextUtils.isEmpty(args) || args.length() < 10) {
            return false;
        }
        return args.startsWith("broadcast://");
    }

    private Intent getBroadcastIntent(String args) {
        return getIntent("broadcast://", args);
    }

    private boolean isServiceIntent(String args) {
        if (TextUtils.isEmpty(args) || args.length() < 10) {
            return false;
        }
        return args.startsWith("service://");
    }

    private Intent getServiceIntent(String args) {
        return getIntent("service://", args);
    }

    @Override
    public void doFunction(String args) throws Exception {
        Intent intent = null;
        if (isActionIntent(args)) {
            intent = getActionIntent(args);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!PackageUtil.checkIntentForActivity(intent)) {
                throw new RuntimeException("no activity found");
            }
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(App.get(), UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                throw new RuntimeException("start activity failed. : " + e.getMessage());
            }
        } else if (isPureIntent(args)) {
            intent = getPureIntent(args);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!PackageUtil.checkIntentForActivity(intent)) {
                throw new RuntimeException("no activity found");
            }
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(App.get(), UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                throw new RuntimeException("start activity failed. : " + e.getMessage());
            }
        } else if (isBroadcastIntent(args)) {
            intent = getBroadcastIntent(args);
            if (!PackageUtil.checkIntentForBroadcastReceiver(intent)) {
                throw new RuntimeException("no receiver found");
            }
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(App.get(), UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send();
            } catch (Exception e) {
                throw new RuntimeException("start broadcast failed. : " + e.getMessage());
            }
        } else if (isServiceIntent(args)) {
            intent = getServiceIntent(args);
            if (!PackageUtil.checkIntentForService(intent)) {
                throw new RuntimeException("no service found");
            }
            PendingIntent pendingIntent =
                    PendingIntent.getService(App.get(), UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send();
            } catch (Exception e) {
                throw new RuntimeException("start service failed. : " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Syntax Error");
        }
    }
}
