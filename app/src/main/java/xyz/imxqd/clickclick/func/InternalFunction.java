package xyz.imxqd.clickclick.func;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioTrack;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
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
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.model.FlymeSmartTouchHelper;
import xyz.imxqd.clickclick.service.KeyEventService;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.utils.AlertUtil;
import xyz.imxqd.clickclick.utils.Flash;
import xyz.imxqd.clickclick.utils.GestureUtil;
import xyz.imxqd.clickclick.utils.PackageUtil;
import xyz.imxqd.clickclick.utils.ResourceUtl;
import xyz.imxqd.clickclick.utils.RomUtil;
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
    public void doFunction(String args) throws Throwable {
        Matcher matcher = FUNC_PATTERN.matcher(args);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Syntax Error");
        }
        matcher.reset();
        if (matcher.find()) {
            try {
                if (matcher.groupCount() == 1) {
                    String name = matcher.group(1);
                    this.getClass().getMethod(name, String.class).invoke(this, "");
                } else if (matcher.groupCount() == 2) {
                    String name = matcher.group(1);
                    String funcArgs = matcher.group(2);
                    this.getClass().getMethod(name, String.class).invoke(this, funcArgs);
                }
            } catch (IllegalAccessException | NoSuchMethodException e) {
                LogUtils.e(e.getMessage());
                throw new IllegalArgumentException("Syntax Error");
            } catch (InvocationTargetException e) {
                LogUtils.e(e.getMessage());
                throw e.getCause();
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

    @SuppressLint("WrongConstant")
    public void do_not_disturb(String str) {
        NotificationManager notificationManager = (NotificationManager) App.get().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            throw new RuntimeException("NotificationManager is null");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.get().startActivity(intent);
            } else {
                int filter = 1;
                if (TextUtils.isEmpty(str)) {
                    filter = notificationManager.getCurrentInterruptionFilter();
                    filter = filter + 1 > 4 ? 1 : filter + 1;
                    switch (filter) {
                        case NotificationManager.INTERRUPTION_FILTER_ALL:
                            App.get().showToast(R.string.notification_filter_all, false, true);
                            break;
                        case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                            App.get().showToast(R.string.notification_filter_priority, false, true);
                            break;
                        case NotificationManager.INTERRUPTION_FILTER_NONE:
                            App.get().showToast(R.string.notification_filter_none, false, true);
                            break;
                        case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                            App.get().showToast(R.string.notification_filter_alarms, false, true);
                            break;
                        default:
                    }
                } else {
                    filter = Integer.valueOf(str);
                    if (filter < 1 || filter > 4) {
                        throw new RuntimeException("do_not_disturb: value is from 1 to 4");
                    }
                }
                notificationManager.setInterruptionFilter(filter);
            }
        }
    }

    public void flash_light(String str) throws Exception {
        if (PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
            try {
                if (TextUtils.isEmpty(str)) {
                    if (Flash.isFlashOn()) {
                        Flash.get().off();
                        Flash.get().close();
                    } else {
                        Flash.get().on();
                    }
                } else {
                    int enable = Integer.valueOf(str);
                    if (enable == 1) {
                        Flash.get().on();
                    } else if (enable == 0) {
                        Flash.get().off();
                        Flash.get().close();
                    } else {
                        throw new RuntimeException("flash_light: value is from 0 to 1");
                    }
                }
            } catch (Exception e) {
                LogUtils.e(e.getMessage());
                throw new RuntimeException("Open camera failed.");
            }
        } else {
            App.get().showToast(App.get().getString(R.string.request_camera), true, true);
            PackageUtil.showInstalledAppDetails(App.get().getPackageName());
            throw new RuntimeException("no permission");
        }

    }

    public void smart_touch(String str) throws RemoteException {
        if (!RomUtil.isMeizuFlyme()) {
            throw new RuntimeException("This is not a flyme os");
        }
        if (TextUtils.isEmpty(str)) {
            if (FlymeSmartTouchHelper.get().isShowing()) {
                FlymeSmartTouchHelper.get().hide();
            } else {
                FlymeSmartTouchHelper.get().show();
            }
        } else {
            int show = Integer.valueOf(str);
            if (show == 0) {
                FlymeSmartTouchHelper.get().hide();
            } else if (show == 1) {
                FlymeSmartTouchHelper.get().show();
            } else {
                throw new RuntimeException("smart_touch: value is from 0 to 1");
            }
        }
    }

    public void show_notifications(String str) throws Exception {
        KeyEventService service = AppEventManager.getInstance().getService();
        if (service == null) {
            AlertUtil.show(App.get().getString(R.string.accessibility_error));
            throw new RuntimeException(App.get().getString(R.string.accessibility_error));
        }
        if (TextUtils.isEmpty(str)) {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
        } else {
            throw new RuntimeException("show_notification: value is no need");
        }
    }

    private static final String REGEX_TAP_ARGS = "([0-9]+)\\s*,\\s*([0-9]+)";
    private static final Pattern TAP_ARGS_PATTERN = Pattern.compile(REGEX_TAP_ARGS);

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void tap(String str) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            throw new UnsupportedOperationException();
        }
        Matcher matcher = TAP_ARGS_PATTERN.matcher(str);
        if (matcher.find()) {
            String numX = matcher.group(1).trim();
            String numY = matcher.group(2).trim();
            GestureDescription description = GestureUtil.makeTap(Float.valueOf(numX), Float.valueOf(numY));
            KeyEventService service = AppEventManager.getInstance().getService();
            if (service != null) {
                service.dispatchGesture(description, null, null);
            } else {
                AlertUtil.show(App.get().getString(R.string.accessibility_error));
                throw new RuntimeException(App.get().getString(R.string.accessibility_error));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static final String REGEX_SWIPE_ARGS = "([0-9]+)\\s*,\\s*([0-9]+)\\s*([0-9]+)\\s*,\\s*([0-9]+),\\s*([0-9]+)";
    private static final Pattern SWIPE_ARGS_PATTERN = Pattern.compile(REGEX_SWIPE_ARGS);
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void swipe(String str) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            throw new UnsupportedOperationException();
        }
        Matcher matcher = SWIPE_ARGS_PATTERN.matcher(str);
        if (matcher.find()) {
            String numX = matcher.group(1).trim();
            String numY = matcher.group(2).trim();
            String numX2 = matcher.group(3).trim();
            String numY2 = matcher.group(4).trim();
            GestureDescription description = GestureUtil.makeSwipe(Float.valueOf(numX), Float.valueOf(numY), Float.valueOf(numX2), Float.valueOf(numY2));
            KeyEventService service = AppEventManager.getInstance().getService();
            if (service != null) {
                service.dispatchGesture(description, null, null);
            } else {
                AlertUtil.show(App.get().getString(R.string.accessibility_error));
                throw new RuntimeException(App.get().getString(R.string.accessibility_error));
            }
        } else {
            throw new IllegalArgumentException();
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
