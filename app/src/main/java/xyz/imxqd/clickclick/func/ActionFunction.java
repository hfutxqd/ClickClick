package xyz.imxqd.clickclick.func;

import android.content.Intent;

import org.json.JSONObject;

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

    @Override
    public void doFunction(String args) {
        Intent intent = new Intent(getAction(args));
        App.get().startActivity(intent);

    }
}
