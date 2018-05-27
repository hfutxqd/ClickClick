package xyz.imxqd.clickclick.func;

import android.annotation.SuppressLint;
import android.os.Looper;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;

public class GroupFunction extends AbstractFunction {

    public static final String PREFIX = "group";

    public GroupFunction(String funcData) {
        super(funcData);
    }

    @SuppressLint("CheckResult")
    @Override
    public void doFunction(final String args) throws Exception {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                Gson gson = new Gson();
                String[] groupItemsStr = gson.fromJson(args, String[].class);
                List<GroupItem> groupItems = parseGroupItems(Arrays.asList(groupItemsStr));
                for (GroupItem item : groupItems) {
                    Thread.sleep(item.timeBefore);
                    boolean success = FunctionFactory.getFunc(item.funcData).exec();
                    Thread.sleep(item.timeAfter);
                    if (!success) {
                        emitter.onNext(false);
                        break;
                    }
                }
                emitter.onNext(true);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) throws Exception {
                if (!success) {
                    toast(App.get().getString(R.string.run_failed));
                }
            }
        });
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
                throw new RuntimeException("Syntax Error");
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
