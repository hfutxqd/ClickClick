package xyz.imxqd.clickclick.func;

import android.media.AudioTrack;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.utils.ResourceUtl;
import xyz.imxqd.clickclick.utils.Shocker;
import xyz.imxqd.clickclick.utils.ToneUtil;

public class InternalFunction extends AbstractFunction {
    public static final String PREFIX = "internal";

    private static final String REGEX_FUNC = "([a-z_]+)\\((.*)\\)";
    private static final String REGEX_RESOURCE = "@(id|drawable|string|array|color)/([a-zA-Z_]+)";
    private static final String REGEX_RESOURCE_ID = "@id/([a-zA-Z_]+)";

    private static final Pattern FUNC_PATTERN = Pattern.compile(REGEX_FUNC);
    private static final Pattern RESOURCE_PATTERN = Pattern.compile(REGEX_RESOURCE);
    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile(REGEX_RESOURCE_ID);

    public InternalFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) throws Exception {
        Matcher matcher = FUNC_PATTERN.matcher(args);
        if (!matcher.matches()) {
            throw new Exception("Syntax Error");
        }
        matcher.reset();
        if (matcher.find()) {
            if (matcher.groupCount() != 2) {
                throw new Exception("Syntax Error");
            }
            String name = matcher.group(1);
            String funcArgs = matcher.group(2);
            try {
                this.getClass().getMethod(name, String.class).invoke(this, funcArgs);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new Exception("error");
            }
        }
    }

    public void cloud_music_like(String args) {
        final NotificationCollectorService service = AppEventManager.getInstance().getNotificationService();
        String idName = "playNotificationStar";
        if (service == null) {
            throw new RuntimeException(App.get().getString(R.string.notification_service_error));
        }
        Matcher matcher = RESOURCE_ID_PATTERN.matcher(args);
        if (!matcher.matches()) {
            throw new RuntimeException("Syntax Error");
        }
        matcher.reset();
        if (matcher.find()) {
            if (matcher.groupCount() != 1) {
                throw new RuntimeException("Syntax Error");
            }
            idName = matcher.group(1);
        }
        IFunction f = new NotificationFunction("notification:com.netease.cloudmusic:4");
        if (!f.exec()) {
            throw new RuntimeException("Failed");
        }

        final NotificationCollectorService.Feedback feedback = new NotificationCollectorService.Feedback();
        feedback.packageName = "com.netease.cloudmusic";
        feedback.methodName = "setImageResource";
        feedback.viewId = ResourceUtl.getIdByName(feedback.packageName, idName);
        feedback.callback = new NotificationCollectorService.Feedback.Callback() {
            @Override
            public void onNotificationPosted(Object val) {
                if (val instanceof  Integer) {
                    App.get().toastImage(ResourceUtl.getDrawableById(feedback.packageName, (Integer) val));
                }
                service.removeFeedback(feedback);
            }
        };
        service.addFeedback(feedback);
        App.get().getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                service.removeFeedback(feedback);
            }
        }, 1000);
    }

    public void qq_music_like() {
        // TODO: 2018/5/23
    }

    @Override
    public void toast(final String str) {
        super.toast(str);
    }

    public void vibrate(String str) throws Exception{
        Gson gson = new Gson();
        long[] times = gson.fromJson(str, long[].class);
        Shocker.shock(times);
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
        String[] tones = str.split(",");
        List<ToneUtil.Tone> list = new ArrayList<>(tones.length);
        for (String tone : tones) {
            list.add(getTone(tone));
        }
        AudioTrack track = ToneUtil.genAudio(list);
        track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {

            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
                track.release();
            }
        });
        track.play();
    }

    public static HashMap<Character, String> map = new HashMap<>();

    public static void main(String ... args) {
        map.put('1', "256:400");
        map.put('2', "288:400");
        map.put('3', "320:400");
        map.put('4', "341:400");
        map.put('5', "384:400");
        map.put('6', "426:400");
        map.put('7', "480:400");
        map.put('8', "512:400");
        map.put('-', "0:800");
        map.put(' ', "0:200");

        StringBuilder str = new StringBuilder();
        str.append("internal:tone(");
        String sss = "1 1 5 5 6 6 5-4 4 3 3 2 2 1-5 5 4 4 3 3 2-5 5 4 4 3 3 2-1 1 5 5 6 6 5-4 4 3 3 2 2 1";
        for (int i = 0; i < sss.length() - 1; i++) {
            str.append(map.get(sss.charAt(i)));
            str.append(',');
        }
        str.append(map.get(sss.charAt(sss.length() - 1)));
        str.append(")");
        System.out.println(str);
    }
}
