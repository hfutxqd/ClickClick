package xyz.imxqd.clickclick.func;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public abstract class AbstractFunction implements IFunction {
    private String funcData;

    public AbstractFunction(String funcData) {
        this.funcData = funcData;
    }

    public String getPrefix() {
        int pos = funcData.indexOf(':');
        return funcData.substring(0, pos);
    }

    public String getArgs() {
        int pos = funcData.indexOf(':');
        return funcData.substring(pos + 1);
    }

    public void toast(final String str) {
        App.get().showToast(str);
    }

    @Override
    public boolean exec() {
        try {
            doFunction(getArgs());
            return true;
        } catch (Exception e) {
            Logger.e("exec error " + e.getMessage());
            if (SettingsUtil.displayDebug()) {
                App.get().showToast(e.getMessage(), true);
            }
            return false;
        }

    }
}
