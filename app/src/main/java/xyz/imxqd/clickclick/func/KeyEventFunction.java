package xyz.imxqd.clickclick.func;

import xyz.imxqd.clickclick.utils.KeyEventUtil;

public class KeyEventFunction extends AbstractFunction {
    public static final String PREFIX = "keyevent";

    public KeyEventFunction(String funcData) {
        super(funcData);
    }

    private int getKeyCode(String args) {
        return Integer.valueOf(args);
    }

    @Override
    public void doFunction(String args) throws Exception {
        int keyCode = getKeyCode(args);
        KeyEventUtil.sendKeyEvent(keyCode);
    }
}
