package xyz.imxqd.clickclick.func;



import com.udojava.evalex.Expression;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellInterpreter {

    private static final String REGEX_RESOURCE_ID = "@id/([a-zA-Z_]+)";
    private static final String REGEX_FUNC = "([a-z_]+)\\((.*)\\)";
    private static final String REGEX_ASSIGN = "^([a-z_]+)={1}([a-z_]+)\\((.*)\\)";
    private static final String REGEX_COMMENT = ";.*$";

    private static final Pattern FUNC_PATTERN = Pattern.compile(REGEX_FUNC);
    private static final Pattern ASSIGN_PATTERN = Pattern.compile(REGEX_ASSIGN);
    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile(REGEX_RESOURCE_ID);

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(new File("test.click"));
        ShellInterpreter main = new ShellInterpreter();
        while (scanner.hasNextLine()) {
            main.readLine(scanner.nextLine());
        }
        main.exec();
    }

    private List<String> mCommends = new ArrayList<>();
    private Map<String, Object> mValues = new HashMap<>();
    private int mLoopStartLine = -1;
    private int mCurrentExecLine = 0;
    private Object mResult = null;

    public void readLine(String line) {
        mCommends.add(line);
    }

    public void release() {
        mCommends.clear();
        mValues.clear();
        mLoopStartLine = -1;
        mCurrentExecLine = 0;
        mResult = null;
    }

    public void exec() throws Exception {
        for (; mCurrentExecLine < mCommends.size();) {
            String commend = mCommends.get(mCurrentExecLine);
            if (commend.trim().matches(REGEX_COMMENT)) {
                // comment line skip
                mCurrentExecLine++;
            } else {
                // remove comment from commend line
                commend = commend.replaceAll(REGEX_COMMENT, "").trim();
                if (commend.matches(REGEX_FUNC)) {
                    Matcher matcher = FUNC_PATTERN.matcher(commend);
                    matcher.find();
                    if (matcher.groupCount() == 1) {
                        String name = matcher.group(1);
                        invoke(name, "");
                    } else if (matcher.groupCount() == 2) {
                        String name = matcher.group(1);
                        String funcArgs = matcher.group(2);
                        invoke(name, funcArgs);
                    }
                    mCurrentExecLine++;
                } else if (commend.trim().matches(REGEX_ASSIGN)) {
                    assign(commend);
                    mCurrentExecLine++;
                } else if (commend.trim().equals("pool")){
                    mCurrentExecLine = mLoopStartLine - 1;
                } else if (commend.trim().equals("fi")) {
                    mCurrentExecLine++;
                } else if (commend.trim().equals("shell")){
                    mCurrentExecLine++;
                } else {
                    throw new RuntimeException("syntax error: " + commend + " , line " + (mCurrentExecLine + 1));
                }
            }
        }
    }

    private void invoke(String name, String args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.getClass().getMethod(name, String.class).invoke(this, args);
    }

    public void start_app(String str) {
        System.out.println("start_app:" + str);
    }

    public void start_intent(String str) throws URISyntaxException {
        System.out.println("start_intent:" + str);
    }

    public void key_event(String str) {
        System.out.println("key_event:" + str);
    }

    public void input_text(String str) {
        System.out.println("input_text:" + str);
    }

    public void assign(String str) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Matcher matcher = ASSIGN_PATTERN.matcher(str.trim());
        matcher.find();
        String key = matcher.group(1);
        String func = matcher.group(2);
        String funcArgs = matcher.group(3);
        invoke(func, funcArgs);
        mValues.put(key, mResult);
        System.out.println("assign:" + str.trim());
    }

    // All -> Null
    public void set_null(String str) {
        System.out.println("value_null: " + str);
        mResult = null;
        mValues.put(str.trim(), null);
    }

    public void intVal(String str) {
        System.out.println("intVal: " + str);
        mResult = Integer.valueOf(str.trim());
    }

    // Null -> NotNull  NotNull -> Null
    public void not(String str) {
        Object o = mValues.get(str.trim());
        if (o == null) {
            mResult = Boolean.TRUE;
        } else {
            mResult = null;
        }
    }

    public void eq(String str) {
        System.out.println("eq: " + str);
    }

    public void lower(String str) {
        System.out.println("lower: " + str);
    }

    public void higher(String str) {
        System.out.println("higher: " + str);
    }

    public void add(String str) {
        System.out.println("add: " + str);
    }

    public void find_id(String str) {
        System.out.println("find_id:" + str);
        mResult = str;
    }

    public void find_text(String str) {
        System.out.println("find_text:" + str);
        mResult = str;
    }

    private void expression_eval(String str) {
        Expression expression = new Expression(str);
        expression.eval();
    }

    public void if_do(String str) {
        if(mValues.get(str) != null) {
            System.out.println("if_do: " + str + " : true");
        } else {
            System.out.println("if_do: " + str + " : false");
            for (;mCurrentExecLine < mCommends.size(); mCurrentExecLine++) {
                if (mCommends.get(mCurrentExecLine).trim().equals("fi")) {
                    break;
                }
            }
        }
    }

    public void loop_do(String str) throws Exception {
        if(mValues.get(str) != null) {
            mLoopStartLine = mCurrentExecLine;
            exec();
            System.out.println("loop_do: " + str + " : true");
        } else {
            System.out.println("loop_do: " + str + " : false");
            for (;mCurrentExecLine < mCommends.size(); mCurrentExecLine++) {
                if (mCommends.get(mCurrentExecLine).trim().equals("pool")) {
                    break;
                }
            }
        }
    }

    public void click(String str) {
        System.out.println("click:" + str);
    }

    public void sleep(String str) throws InterruptedException {
        System.out.println("sleep:" + str);
        Thread.sleep(Long.valueOf(str.trim()));
    }

    public void tap(String str) {
        System.out.println("tap:" + str);
    }

    public void toast(String str) {
        System.out.println("toast:" + str);
    }

    public void tone(String str) {
        System.out.println("tone:" + str);
    }

    public void vibrate(String str) {
        System.out.println("vibrate:" + str);
    }

    public void exit_shell(String str) {
        System.out.println("exit_shell:" + str);
        release();
    }
}

