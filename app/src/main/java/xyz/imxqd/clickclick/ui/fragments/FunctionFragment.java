package xyz.imxqd.clickclick.ui.fragments;


import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.ui.adapters.FunctionAdapter;
import xyz.imxqd.clickclick.utils.ScreenUtl;


public class FunctionFragment extends BaseFragment {

    private volatile static FunctionFragment mInstance;

    @BindView(android.R.id.list)
    RecyclerView vList;

    @BindView(R.id.function_state)
    TextView vState;

    FunctionAdapter mAdapter;

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
        View v = inflater.inflate(R.layout.fragment_function, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new FunctionAdapter();
        vList.setLayoutManager(new LinearLayoutManager(getContext()));
        vList.setAdapter(mAdapter);
        vList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int pos = parent.getChildAdapterPosition(view);
                if (pos == 0) {
                    outRect.top = ScreenUtl.dp2px(5);
                }
            }
        });
        vState.setText(getStateText());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInstance = null;
    }

    private CharSequence getStateText() {
        int count = mAdapter.getItemCount();
        String state = getString(R.string.function_current_state, count);
        return getBigNumberText(state);
    }

    @OnClick(R.id.action_add)
    public void onAddClick() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
