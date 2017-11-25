package xyz.imxqd.mediacontroller.model;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

import xyz.imxqd.mediacontroller.App;
import xyz.imxqd.mediacontroller.R;

/**
 * Created by imxqd on 2017/11/25.
 */
@SuppressLint("UseSparseArrays")
public abstract class Function implements IFunction {

    protected Map<Integer, String> mFunctionMap;

    protected int mFunctionKey;

    public Function(int mFunctionKey) {
        this.mFunctionKey = mFunctionKey;
    }

    @Override
    public String getFunctionName() {
        if (mFunctionMap == null) {
            mFunctionMap = new HashMap<>();
            String[] names = App.get().getResources().getStringArray(R.array.key_functions);
            int[] keys = App.get().getResources().getIntArray(R.array.key_values);
            for (int i = 0; i < keys.length; i++) {
                mFunctionMap.put(keys[i], names[i]);
            }
        }
        return mFunctionMap.get(mFunctionKey);
    }
}
