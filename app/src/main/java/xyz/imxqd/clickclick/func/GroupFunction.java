package xyz.imxqd.clickclick.func;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GroupFunction extends AbstractFunction {

    public static final String PREFIX = "group";

    public GroupFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(final String args) throws RuntimeException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                List<String> groupItemsStr = gson.fromJson(args, List.class);
                try {
                    List<GroupItem> groupItems = parseGroupItems(groupItemsStr);
                    for (GroupItem item : groupItems) {
                        Thread.sleep(item.timeBefore);
                        FunctionFactory.getFunc(item.funcData).exec();
                        Thread.sleep(item.timeAfter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, PREFIX).start();
    }

    public static List<GroupItem> parseGroupItems(List<String> groupItemStrList) throws Exception {
        List<GroupItem> list = new ArrayList<>(groupItemStrList.size());
        for (String str : groupItemStrList) {
            int pos1 = str.indexOf('|');
            int pos2 = str.lastIndexOf('|');
            if (pos1 != -1 && pos2 != -1 && pos1 != pos2) {
                GroupItem item = new GroupItem();
                if (pos1 > 0) {
                    item.timeBefore = Integer.valueOf(str.substring(0, pos1));
                }
                if (pos2 < str.length() - 1) {
                    item.timeAfter = Integer.valueOf(str.substring(pos2 + 1));
                }
                item.funcData = str.substring(pos1 + 1, pos2);
                list.add(item);
            } else {
                return null;
            }
        }
        return list;
    }

    public static class GroupItem {
        int timeBefore = 0;
        int timeAfter = 0;
        String funcData;
    }
}
