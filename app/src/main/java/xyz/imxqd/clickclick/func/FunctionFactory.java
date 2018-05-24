package xyz.imxqd.clickclick.func;

import com.raizlabs.android.dbflow.sql.language.Select;

import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.dao.DefinedFunction_Table;

public class FunctionFactory {

    private static String getPrefix(String funcData) {
        int pos = funcData.indexOf(':');
        return funcData.substring(0, pos);
    }

    private static String getArgs(String funcData) {
        int pos = funcData.indexOf(':');
        return funcData.substring(pos + 1);
    }


    public static IFunction getFunc(String data) {
        switch (getPrefix(data)) {
            case InternalFunction.PREFIX:
                break;
            case MediaFunction.PREFIX:
                break;
            case UrlFunction.PREFIX:
                return new UrlFunction(data);
            case KeyEventFunction.PREFIX:
                break;
            case ActionFunction.PREFIX:
                return new ActionFunction(data);
            default:

        }
        return null;
    }

    public static IFunction getFuncById(long id) {
        DefinedFunction function = new Select()
                .from(DefinedFunction.class)
                .where(DefinedFunction_Table.id.eq(id))
                .querySingle();
        if (function == null) {
            return null;
        } else {
            return getFunc(function.body);
        }
    }
}
