package com.github.ahmadaghazadeh.editor.processor.language;

import com.github.ahmadaghazadeh.editor.processor.utils.text.ArrayUtils;

import java.util.regex.Pattern;

public class LuaLanguage extends Language {
    private static final Pattern SYNTAX_NUMBERS = Pattern.compile("(\\b(\\d*[.]?\\d+)\\b)");

    public final Pattern getSyntaxNumbers() {
        return SYNTAX_NUMBERS;
    }

    private static final Pattern SYNTAX_SYMBOLS = Pattern.compile(
            "(!|\\+|-|\\*|<|>|=|\\?|\\||:|%|&)");

    public final Pattern getSyntaxSymbols() {
        return SYNTAX_SYMBOLS;
    }

    private static final Pattern SYNTAX_BRACKETS = Pattern.compile("(\\(|\\)|\\{|\\}|\\[|\\])");

    public final Pattern getSyntaxBrackets() {
        return SYNTAX_BRACKETS;
    }

    private static final Pattern SYNTAX_KEYWORDS = Pattern.compile(
            "(?<=\\b)((and)|(break)|(do)|(else)|(elseif)|(end)|(false)|(for)" +
                    "|(function)|(if)|(in)|(local)|(nil)|(not)|(or)|(repeat)" +
                    "|(return)|(then)|(true)|(until)|(while)|(null)" +
                    ")(?=\\b)"); // CASE_INSENSITIVE

    public final Pattern getSyntaxKeywords() {
        return SYNTAX_KEYWORDS;
    }

    private static final Pattern SYNTAX_METHODS = Pattern.compile(
            "(?<=(function) )(\\w+)", Pattern.CASE_INSENSITIVE);

    public final Pattern getSyntaxMethods() {
        return SYNTAX_METHODS;
    }

    private static final Pattern SYNTAX_STRINGS = Pattern.compile("\"(.*?)\"|'(.*?)'");

    public final Pattern getSyntaxStrings() {
        return SYNTAX_STRINGS;
    }

    private static final Pattern SYNTAX_COMMENTS = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*");

    public final Pattern getSyntaxComments() {
        return SYNTAX_COMMENTS;
    }

    private static final char[] LANGUAGE_BRACKETS = new char[]{'{', '[', '(', '}', ']', ')'}; //do not change

    public final char[] getLanguageBrackets() {
        return LANGUAGE_BRACKETS;
    }


    private static final String[] GLOBAL_VALUES = new String[] {
            "app", "runtime", "device", "media"
    };

    private static final String[] APP_FUNCTIONS = new String[] {
            "app.launch()", "app.getAppName()", "app.openAppSetting()", "app.viewFile()", "app.editFile()",
            "app.uninstall()", "app.openUrl()", "app.sendEmail()", "app.startActivity()", "app.sendBroadcast()",
            "app.startService()", "app.parseUri()", "app.getUriForFile()"
    };

    private static final String[] DEVICE_FUNCTIONS = new String[] {
            "device.width", "device.height", "device.buildId", "device.broad", "device.brand",
            "device.device", "deivce.model", "device.product", "device.bootloader", "device.hardware",
            "device.fingerprint", "device.sdkInt", "device.incremental", "device.release",
            "device.isScreenOn()", "device.vibrate()", "device.cancelVibration()"
    };

    private static final String[] GLOBAL_FUNCTIONS = new String[] {
            "currentPackage()", "currentActivity()", "waitForPackage()", "waitForActivity()",
            "random()", "toast()", "print()", "requiresApi()", "exit()", "sleep()", "requiresLuaCoreVersion()"
    };

    private static final String[] ALL_KEYWORDS = ArrayUtils.join(String.class, GLOBAL_VALUES,
            APP_FUNCTIONS, DEVICE_FUNCTIONS, GLOBAL_FUNCTIONS);

    public final String[] getAllCompletions() {
        return ALL_KEYWORDS;
    }
}
