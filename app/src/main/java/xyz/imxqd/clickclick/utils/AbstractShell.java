package xyz.imxqd.clickclick.utils;

import android.content.Context;


public abstract class AbstractShell {


    public static class Result {
        public int code = -1;
        public String error;
        public String result;

        @Override
        public String toString() {
            return "ShellResult{" +
                    "code=" + code +
                    ", error='" + error + '\'' +
                    ", result='" + result + '\'' +
                    '}';
        }
    }

    protected static final String COMMAND_SU = "su";
    protected static final String COMMAND_SH = "sh";
    protected static final String COMMAND_EXIT = "exit\n";
    protected static final String COMMAND_LINE_END = "\n";


    private boolean mRoot;
    protected Context mContext;

    public AbstractShell() {
        this(false);
    }

    public AbstractShell(boolean root) {
        this(null, root);
    }

    public AbstractShell(Context context, boolean root) {
        mContext = context;
        mRoot = root;
        init(root ? COMMAND_SU : COMMAND_SH);
    }

    public boolean isRoot() {
        return mRoot;
    }

    protected abstract void init(String initialCommand);

    public abstract void exec(String command);

    public abstract void exit();

    public void KeyCode(int keyCode) {
        exec("input keyevent " + keyCode);
    }

    public void Input(String text) {
        exec("input text " + text);
    }

    public void Text(String text) {
        Input(text);
    }

    public abstract void exitAndWaitFor();

    public void sleep(long i) {
        exec("sleep " + i);
    }

    public void usleep(long l) {
        exec("usleep " + l);
    }
}
