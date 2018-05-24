package xyz.imxqd.clickclick.func;

import android.content.Intent;
import android.net.Uri;

import com.orhanobut.logger.Logger;

import xyz.imxqd.clickclick.App;

public class UrlFunction extends AbstractFunction {
    public static final String PREFIX = "url";

    public UrlFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) {
        if (PREFIX.equals(getPrefix())) {
            String url = getArgs();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            App.get().startActivity(intent);
        } else {
            Logger.e("function prefix not match");
        }

    }
}
