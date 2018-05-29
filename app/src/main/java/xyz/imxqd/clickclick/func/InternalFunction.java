package xyz.imxqd.clickclick.func;

import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.OnClick;
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
        new NotificationFunction("notification:com.netease.cloudmusic:4").exec();

        final NotificationCollectorService.Feedback feedback = new NotificationCollectorService.Feedback();
        feedback.packageName = "com.netease.cloudmusic";
        feedback.methodName = "setImageResource";
        feedback.viewId = ResourceUtl.getIdByName(feedback.packageName, idName);
        feedback.callback = new NotificationCollectorService.Feedback.Callback() {
            @Override
            public void onNotificationPosted(Object val) {
                if (val instanceof  Integer) {
                    Toast toast = new Toast(App.get());
                    ImageView imageView = new ImageView(App.get());
                    imageView.setImageDrawable(ResourceUtl.getDrawableById(feedback.packageName, (Integer) val));
                    toast.setView(imageView);
                    toast.show();
                }
                service.removeFeedback(feedback);
            }
        };
        service.addFeedback(feedback);
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
}
