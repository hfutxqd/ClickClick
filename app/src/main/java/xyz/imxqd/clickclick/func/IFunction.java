package xyz.imxqd.clickclick.func;

/**
 * Created by imxqd on 2017/11/25.
 */

public interface IFunction {
    void doFunction(String args) throws Throwable;

    Throwable getErrorInfo();

    boolean exec();
}
