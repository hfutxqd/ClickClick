package xyz.imxqd.clickclick.func;

public class IfThenFunction extends AbstractFunction {

    private static final String REGEX_SUB_FUNC = "[a-zA-Z0-9_$]+\\([a-zA-Z0-9_$]*(\\([a-zA-Z0-9_$]*\\))*[a-zA-Z0-9_$]*\\)";


    public IfThenFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) throws Exception {

    }
}

// if:and()
//
// if:or()
//
// if:nor()
