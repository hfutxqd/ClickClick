package xyz.imxqd.clickclick.func;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShellFunction extends AbstractFunction {
    public static final String PREFIX = "shell";

    public List<String> mCommends = new ArrayList<>();
    public Map<String, Object> mValues = new HashMap<>();

    public ShellFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) throws Exception {

    }

}