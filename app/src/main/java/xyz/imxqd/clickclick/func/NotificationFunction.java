package xyz.imxqd.clickclick.func;

import android.app.Notification;
import android.app.PendingIntent;
import android.text.TextUtils;

import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.utils.AlertUtil;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.ResourceUtl;

public class NotificationFunction extends AbstractFunction {
    public static final String PREFIX = "notification";

    private static final String REGEX_PACKAGE = "([a-zA-Z0-9_]+\\.{1})+[a-zA-Z0-9_]+";
    private static final String REGEX_RESOURCE_ID = "@id/([a-zA-Z_]+)";
    private static final String REGEX_ID = "@id/(([a-zA-Z0-9_]+){1}(\\.{1}[a-zA-Z0-9_]+)*):id/([a-zA-Z0-9_]+)";
    private static final String REGEX_ACTION = "@action/([0-9]+)";
    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile(REGEX_RESOURCE_ID);
    private static final Pattern ID_PATTERN = Pattern.compile(REGEX_ID);
    private static final Pattern ACTION_PATTERN = Pattern.compile(REGEX_ACTION);

    public NotificationFunction(String funcData) {
        super(funcData);
    }

    private String getPackageName(String args) {
        if (TextUtils.isEmpty(args)) {
            return null;
        }
        int pos = args.indexOf(':');
        if (pos <= 0) {
            throw new RuntimeException("Syntax Error");
        }
        String packageName = args.substring(0, pos);
        Pattern pattern = Pattern.compile(REGEX_PACKAGE);
        Matcher matcher = pattern.matcher(packageName);
        if (matcher.matches()) {
            return packageName;
        } else {
            throw new RuntimeException("Syntax Error");
        }
    }


    public String getPackageArgs(String args) {
        int pos = args.indexOf(':');
        if (pos <= 0) {
            throw new RuntimeException("Syntax Error");
        }
        return args.substring(pos + 1);
    }


    public int getOrder(String args) {
        if (TextUtils.isEmpty(args)) {
            return -1;
        }
        int pos = args.indexOf(':');
        if (pos <= 0) {
            throw new RuntimeException("Syntax Error");
        }
        if (pos >= args.length() - 1) {
            throw new RuntimeException("Syntax Error");
        }
        try {
            int order = Integer.valueOf(args.substring(pos + 1));
            return order;
        } catch (Exception e) {
            throw new RuntimeException("Syntax Error" );
        }
    }

    private static TreeMap<String, PendingIntent> mCacheIntents = new TreeMap<>();

    @Override
    public void doFunction(String args) throws Exception {
        if (NotificationCollectorService.getNotificationsByPackage(getPackageName(args)).size() > 0) {
            Matcher matcher = RESOURCE_ID_PATTERN.matcher(getPackageArgs(args));
            Matcher matcher2 = ID_PATTERN.matcher(getPackageArgs(args));
            Matcher matcher3 = ACTION_PATTERN.matcher(getPackageArgs(args));
            if (matcher.matches()) {
                LogUtils.d("notification : id mode");
                matcher.reset();
                String idName = "playNotificationStar";
                if (matcher.find()) {
                    if (matcher.groupCount() != 1) {
                        throw new RuntimeException("Syntax Error");
                    }
                    idName = matcher.group(1);
                }
                int viewId = ResourceUtl.getIdByName(getPackageName(args), idName);

                List<Notification> notifications = NotificationCollectorService.getNotificationsByPackage(getPackageName(args));
                if (notifications.size() == 0) {
                    throw new RuntimeException("There are no notifications of " + getPackageName(args));
                }
                PendingIntent intent;
                intent = NotificationAccessUtil.getPendingIntentByViewId(notifications.get(0).bigContentView, viewId);
                if (intent == null) {
                    intent = NotificationAccessUtil.getPendingIntentByViewId(notifications.get(0).contentView, viewId);
                }
                if (intent != null) {
                    mCacheIntents.put(args, intent);
                } else  {
                    intent = mCacheIntents.get(args);
                }
                if (intent == null) {
                    intent = notifications.get(0).contentIntent;
                }
                intent.send();
            } else if (matcher2.matches()) {
                LogUtils.d("notification : id mode2");
                matcher2.reset();
                String packageArgs = getPackageArgs(args);
                String idName = getIdName(packageArgs);
                String idPackageName = getIdPackageName(packageArgs);
                int viewId = ResourceUtl.getIdByName(idPackageName, idName);
                List<Notification> notifications = NotificationCollectorService.getNotificationsByPackage(getPackageName(args));
                if (notifications.size() == 0) {
                    throw new RuntimeException("There are no notifications of " + getPackageName(args));
                }
                PendingIntent intent = null;
                if (notifications.get(0).bigContentView != null) {
                    intent = NotificationAccessUtil.getPendingIntentByViewId(notifications.get(0).bigContentView, viewId);
                }
                if (intent == null && notifications.get(0).contentView != null) {
                    intent = NotificationAccessUtil.getPendingIntentByViewId(notifications.get(0).contentView, viewId);
                }
                if (intent != null) {
                    mCacheIntents.put(args, intent);
                } else  {
                    intent = mCacheIntents.get(args);
                }
                if (intent == null) {
                    intent = notifications.get(0).contentIntent;
                }
                intent.send();

            } else if (matcher3.matches()) {
                matcher3.reset();
                int actionOrder = 0;
                if (matcher3.find()) {
                    actionOrder = Integer.valueOf(matcher3.group(1));
                    List<Notification> notifications = NotificationCollectorService.getNotificationsByPackage(getPackageName(args));
                    if (notifications.size() == 0) {
                        throw new RuntimeException("There are no notifications of " + getPackageName(args));
                    }
                    Notification.Action[] actions = notifications.get(0).actions;
                    PendingIntent intent = null;
                    if (actions != null) {
                        intent = actions[actionOrder].actionIntent;
                        if (intent != null) {
                            mCacheIntents.put(args, intent);
                            intent.send();
                        } else {
                            intent = mCacheIntents.get(args);
                            intent.send();
                        }
                    } else {
                        throw new RuntimeException("There are no notification actions for " + getPackageName(args));
                    }
                }

            } else {
                LogUtils.d("notification : order mode");
                List<Notification> notifications = NotificationCollectorService.getNotificationsByPackage(getPackageName(args));
                if (notifications.size() == 0) {
                    throw new RuntimeException("There are no notifications of " + getPackageName(args));
                }
                List<PendingIntent> intents = NotificationAccessUtil.getPendingIntents(notifications.get(0));
                int order = getOrder(args);
                if (order > 0 && intents.size() > order) {
                    intents.get(order).send();
                }
            }

        } else if (AppEventManager.getInstance().getNotificationService() == null){
            AlertUtil.show(App.get().getString(R.string.notification_service_error));
            throw new RuntimeException(App.get().getString(R.string.notification_service_error));
        } else {
            throw new RuntimeException("There are no notification actions for " + getPackageName(args));
        }
    }

    private static String getIdPackageName(String id) {
        Matcher matcher = ID_PATTERN.matcher(id);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String getIdName(String id) {
        Matcher matcher = ID_PATTERN.matcher(id);
        if (matcher.find()) {
            return matcher.group(matcher.groupCount());
        }
        return null;
    }

}
