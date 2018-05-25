package xyz.imxqd.clickclick.func;

import android.content.Intent;
import android.text.TextUtils;

import org.json.JSONObject;

import java.net.URISyntaxException;

import xyz.imxqd.clickclick.App;

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

    private boolean isPureIntent(String args) {
        if (TextUtils.isEmpty(args) || args.length() < 10) {
            return false;
        }
        return args.startsWith("intent://");
    }

    public Intent getPureIntent(String args) {
        try {
            return Intent.parseUri(args.substring(9), 0);
        } catch (URISyntaxException e) {
            return new Intent();
        }
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

    @Override
    public void doFunction(String args) {
        Intent intent = null;
        if (isActionIntent(args)) {
            intent = getActionIntent(args);
        } else if (isPureIntent(args)) {
            intent = getPureIntent(args);
        }
        if (intent != null) {
            App.get().startActivity(intent);
        }
    }
}
