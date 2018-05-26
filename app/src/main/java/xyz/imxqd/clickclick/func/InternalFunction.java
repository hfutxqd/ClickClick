package xyz.imxqd.clickclick.func;

import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.utils.Shocker;
import xyz.imxqd.clickclick.utils.ToneUtil;

public class InternalFunction extends AbstractFunction {
    public static final String PREFIX = "internal";

    private static final String REGEX_FUNC = "([a-z_]+)\\((.*)\\)";

    private static final Pattern FUNC_PATTERN = Pattern.compile(REGEX_FUNC);

    public InternalFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) {
        Matcher matcher = match(args);
        if (!matcher.matches()) {
            return;
        }
        matcher.reset();
        if (matcher.find()) {
            if (matcher.groupCount() != 2) {
                return;
            }
            String name = matcher.group(1);
            String funcArgs = matcher.group(2);
            try {
                this.getClass().getMethod(name, String.class).invoke(this, funcArgs);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private Matcher match(String args) {
        return FUNC_PATTERN.matcher(args);
    }

    public void cloudMusicLike() {
        // TODO: 2018/5/23  
    }

    public void qqMusicLike() {
        // TODO: 2018/5/23
    }

    public void toast(String str) {
        Toast.makeText(App.get(), str, Toast.LENGTH_LONG).show();
    }

    public void vibrate(String str) {
        Gson gson = new Gson();
        try {
            long[] times = gson.fromJson(str, long[].class);
            Shocker.shock(times);
        } catch (Exception e) {

        }
    }

    private ToneUtil.Tone getTone(String tone) {
        ToneUtil.Tone t = new ToneUtil.Tone();
        t.freq = Integer.valueOf(tone.substring(0, tone.indexOf(':')));
        if (t.freq == 0) {
            t.freq = 1;
        }
        t.duration = Integer.valueOf(tone.substring(tone.indexOf(':') + 1));
        return t;
    }

    public void tone(String str) {
        try {
            String[] tones = str.split(",");
            List<ToneUtil.Tone> list = new ArrayList<>(tones.length);
            for (String tone : tones) {
                list.add(getTone(tone));
            }
            ToneUtil.genAudio(list).play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
