package xyz.imxqd.clickclick.func;

import com.orhanobut.logger.Logger;

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

    @Override
    public void exec() {
        try {
            doFunction(getArgs());
        } catch (Exception e) {
            Logger.e("exec error " + e.getMessage());
        }

    }
}
