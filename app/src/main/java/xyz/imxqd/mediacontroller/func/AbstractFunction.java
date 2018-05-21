package xyz.imxqd.mediacontroller.func;

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

    public void exec() {
        doFunction(getArgs());
    }
}
