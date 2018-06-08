package xyz.imxqd.clickclick.func;

import android.content.Intent;
import android.media.AudioTrack;
import android.text.TextUtils;

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
import xyz.imxqd.clickclick.service.KeyEventService;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.utils.AlertUtil;
import xyz.imxqd.clickclick.utils.ResourceUtl;
import xyz.imxqd.clickclick.utils.Shocker;
import xyz.imxqd.clickclick.utils.SystemSettingsUtl;
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
            if (matcher.groupCount() == 1) {
                String name = matcher.group(1);
                try {
                    this.getClass().getMethod(name, String.class).invoke(this, "");
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new Exception("error");
                }
            } else if (matcher.groupCount() == 2) {
                String name = matcher.group(1);
                String funcArgs = matcher.group(2);
                try {
                    this.getClass().getMethod(name, String.class).invoke(this, funcArgs);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new Exception("error");
                }
            }
        }
    }

    public void notify_helper(String args) {

        final KeyEventService service = AppEventManager.getInstance().getService();
        if (service != null) {
            App.get().showToast(App.get().getString(R.string.please_click_the_notification_item), true, true);
            final KeyEventService.OnNotificationWidgetClick callback = new KeyEventService.OnNotificationWidgetClick() {
                @Override
                public void onNotificationWidgetClick(String packageName, String viewId) {
                    AlertUtil.showNotify(packageName, viewId);
                    service.removeOnNotificationWidgetClickCallback(this);
                }
            };
            service.addOnNotificationWidgetClickCallback(callback);
            App.get().getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    service.removeOnNotificationWidgetClickCallback(callback);
                }
            }, 15000);
        } else {
            AlertUtil.show(App.get().getString(R.string.accessibility_error));
            throw new RuntimeException(App.get().getString(R.string.accessibility_error));
        }
    }

    public void cloud_music_like(String args) {
        final NotificationCollectorService service = AppEventManager.getInstance().getNotificationService();
        String idName = "playNotificationStar";
        if (service == null) {
            AlertUtil.show(App.get().getString(R.string.notification_service_error));
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
        IFunction f = new NotificationFunction("notification:com.netease.cloudmusic:@id/" + idName);
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

    public void qq_music_like(String str) {
        Intent intent = new Intent("com.tencent.qqmusic.ACTION_SERVICE_FAVORATE_SONG.QQMusicPhone");
        App.get().sendBroadcast(intent);
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

    public void auto_rotation(String enable) {
        boolean res = false;
        if (TextUtils.isEmpty(enable)) {
            res = SystemSettingsUtl.switchAutoRotation();
        } else {
            try {
                int r = Integer.valueOf(enable);
                if (r == 0 || r == 1) {
                    res = SystemSettingsUtl.switchAutoRotation(r);
                } else {
                    throw new RuntimeException("auto_rotation value is wrong");
                }
            } catch (Exception e) {
                throw new RuntimeException("auto_rotation value is wrong");
            }
        }

        if(!res) {
            App.get().showToast(App.get().getString(R.string.request_write_settings), true, true);
            SystemSettingsUtl.startPackageSettings();
        }
    }

    public void rotation(String rotation) {
        try {
            int r = Integer.valueOf(rotation);
            int rota = 0;
            switch (r) {
                case 0:
                    rota = 0;
                    break;
                case 90:
                    rota = 1;
                    break;
                case 180:
                    rota = 2;
                    break;
                case 270:
                    rota = 3;
                    break;
                default:
                    throw new RuntimeException("rotation wrong");
            }
            if (SystemSettingsUtl.switchRotation(rota)) {
            } else {
                App.get().showToast(App.get().getString(R.string.request_write_settings), true, true);
                SystemSettingsUtl.startPackageSettings();
            }
        } catch (Exception e) {
            throw new RuntimeException("rotation wrong");
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
