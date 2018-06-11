package xyz.imxqd.clickclick.func;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public abstract class AbstractFunction implements IFunction {
    private String funcData;
    private Throwable error = new Exception("no error");

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
        App.get().showToast(str, true, false);
    }

    @Override
    public boolean exec() {
        try {
            doFunction(getArgs());
            return true;
        } catch (Throwable e) {
            error = e;
            LogUtils.e("exec error " + e.getMessage());
            if (SettingsUtil.displayDebug()) {
                App.get().showToast(e.getMessage(), true, true);
            }
            return false;
        }

    }

    @Override
    public Throwable getErrorInfo() {
        return error;
    }
}
