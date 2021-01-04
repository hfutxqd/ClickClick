package xyz.imxqd.clickclick.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * Created by imxqd on 2017/11/28.
 */

public class ShellUtil {
    private static final String TAG = "ShellUtil";

    private static String shellSU;

    // uid=0(root) gid=0(root)
    private static final Pattern UID_PATTERN = Pattern.compile("^uid=(\\d+).*?");

    enum OUTPUT {
        STDOUT,
        STDERR,
        BOTH
    }

    private static final String EXIT = "exit\n";

    private static final String[] SU_COMMANDS = new String[]{
            "su",
            "xbin/su",
            "/system/xbin/su",
            "/system/bin/su"
    };

    private static final String[] TEST_COMMANDS = new String[]{
            "whoami"
    };

    public static synchronized boolean isSuAvailable() {
        if (shellSU == null) {
            checkSu();
        }
        return shellSU != null;
    }

    public static synchronized void setShellSU(String shellSU) {
        ShellUtil.shellSU = shellSU;
    }

    private static boolean checkSu() {
        for (String command : SU_COMMANDS) {
            shellSU = command;
            if (isRootUid()) return true;
        }
        shellSU = null;
        return false;
    }

    private static boolean isRootUid() {
        String out = null;
        for (String command : TEST_COMMANDS) {
            out = getProcessOutput(command);
            if (out != null && out.length() > 0) break;
        }
        if (out == null || out.length() == 0) return false;
        if (TextUtils.isEmpty(out)) {
            return false;
        }
        if (TextUtils.equals("root", out.trim())) {
            return true;
        }
        return false;
    }

    public static String getProcessOutput(String command) {
        try {
            return _runCommand(command, OUTPUT.STDERR);
        } catch (IOException ignored) {
            return null;
        }
    }

    public static String runCommand(String command) {
        try {
            return _runCommand(command, OUTPUT.BOTH);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static String _runCommand(String command, OUTPUT o) throws IOException {
        DataOutputStream os = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(shellSU);
            os = new DataOutputStream(process.getOutputStream());
            InputStreamHandler sh = sinkProcessOutput(process, o);
            os.writeBytes(command + '\n');
            os.flush();
            os.writeBytes(EXIT);
            os.flush();
            process.waitFor();
            if (sh != null) {
                return sh.getOutput();
            } else {
                return null;
            }
        } catch (Exception e) {
            final String msg = e.getMessage();
            Log.e(TAG, "runCommand error: " + msg);
            throw new IOException(msg);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception ignored) {}
        }
    }

    public static InputStreamHandler sinkProcessOutput(Process p, OUTPUT o) {
        InputStreamHandler output = null;
        switch (o) {
            case STDOUT:
                output = new InputStreamHandler(p.getErrorStream(), false);
                new InputStreamHandler(p.getInputStream(), true);
                break;
            case STDERR:
                output = new InputStreamHandler(p.getInputStream(), false);
                new InputStreamHandler(p.getErrorStream(), true);
                break;
            case BOTH:
                output = new InputStreamHandler(p.getInputStream(), false);
                new InputStreamHandler(p.getErrorStream(), true);
                break;
        }
        return output;
    }

    private static class InputStreamHandler extends Thread {
        private final InputStream stream;
        private final boolean     sink;
        StringBuffer output;

        public String getOutput() {
            return output.toString();
        }

        InputStreamHandler(InputStream stream, boolean sink) {
            this.sink = sink;
            this.stream = stream;
            start();
        }

        @Override
        public void run() {
            try {
                if (sink) {
                    while (stream.read() != -1) {}
                } else {
                    output = new StringBuffer();
                    BufferedReader b = new BufferedReader(new InputStreamReader(stream));
                    String s;
                    while ((s = b.readLine()) != null) {
                        output.append(s).append("\n");
                    }
                }
            } catch (IOException ignored) {}
        }
    }
}