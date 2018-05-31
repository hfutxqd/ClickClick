package xyz.imxqd.clickclick.func;

import android.app.Notification;
import android.app.PendingIntent;
import android.text.TextUtils;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.utils.AlertUtil;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.ResourceUtl;

public class NotificationFunction extends AbstractFunction {
    public static final String PREFIX = "notification";

    private static final String REGEX_PACKAGE = "([a-zA-Z0-9_]+\\.{1})+[a-zA-Z0-9_]+";
    private static final String REGEX_RESOURCE_ID = "@id/([a-zA-Z_]+)";
    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile(REGEX_RESOURCE_ID);

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

    @Override
    public void doFunction(String args) throws Exception {

        NotificationCollectorService service = AppEventManager.getInstance().getNotificationService();
        if (service != null) {
            Matcher matcher = RESOURCE_ID_PATTERN.matcher(getPackageArgs(args));
            if (matcher.matches()) {
                Logger.d("notification : id mode");
                matcher.reset();
                String idName = "playNotificationStar";
                if (matcher.find()) {
                    if (matcher.groupCount() != 1) {
                        throw new RuntimeException("Syntax Error");
                    }
                    idName = matcher.group(1);
                }
                int viewId = ResourceUtl.getIdByName(getPackageName(args), idName);

                List<Notification> notifications = service.getNotificationsByPackage(getPackageName(args));
                if (notifications.size() == 0) {
                    throw new RuntimeException("There are no notifications of " + getPackageName(args));
                }
                PendingIntent intent;
                intent = NotificationAccessUtil.getPendingIntentByViewId(notifications.get(0).bigContentView, viewId);
                if (intent == null) {
                    intent = NotificationAccessUtil.getPendingIntentByViewId(notifications.get(0).contentView, viewId);
                }
                intent.send();
            } else {
                Logger.d("notification : order mode");
                List<Notification> notifications = service.getNotificationsByPackage(getPackageName(args));
                if (notifications.size() == 0) {
                    throw new RuntimeException("There are no notifications of " + getPackageName(args));
                }
                List<PendingIntent> intents = NotificationAccessUtil.getPendingIntents(notifications.get(0));
                int order = getOrder(args);
                if (order > 0 && intents.size() > order) {
                    intents.get(order).send();
                }
            }

        } else {
            AlertUtil.show(App.get().getString(R.string.notification_service_error));
            throw new RuntimeException(App.get().getString(R.string.notification_service_error));
        }
    }
}
