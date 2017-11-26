package xyz.imxqd.mediacontroller.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.imxqd.mediacontroller.R;


public class FunctionFragment extends Fragment {
    private static volatile FunctionFragment mInstance;

    public FunctionFragment() {
        // Required empty public constructor
    }


    public static FunctionFragment getInstance() {
        if (mInstance == null ) {
            synchronized (FunctionFragment.class) {
                mInstance = new FunctionFragment();
            }
        }
        return mInstance;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_function, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInstance = null;
    }
}
