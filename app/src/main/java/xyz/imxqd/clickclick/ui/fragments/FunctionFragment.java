package xyz.imxqd.clickclick.ui.fragments;


import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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


public class FunctionFragment extends BaseFragment implements FunctionAdapter.EventCallback {

    private volatile static FunctionFragment mInstance;

    @BindView(android.R.id.list)
    RecyclerView vList;

    @BindView(R.id.function_state)
    TextView vState;

    FunctionAdapter mAdapter;
    ItemTouchHelper mItemTouchHelper;

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

    ItemTouchHelper.Callback mCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN|ItemTouchHelper.UP, 0) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            mAdapter.swap(fromPosition, toPosition);
            mAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                ViewCompat.animate(viewHolder.itemView)
                        .setDuration(100)
                        .translationZ(15f)
                        .start();
            }

        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            ViewCompat.animate(viewHolder.itemView)
                    .translationZ(0f)
                    .setDuration(150)
                    .start();

        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }


    };

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
        mAdapter.setOnStartDragCallback(this);
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
        mItemTouchHelper = new ItemTouchHelper(mCallback);
        mItemTouchHelper.attachToRecyclerView(vList);
        initStateText();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInstance = null;
    }

    private void initStateText() {
        int count = mAdapter.getItemCount();
        String state = getString(R.string.function_current_state, count);
        CharSequence text = getBigNumberText(state);
        vState.setText(text);
    }

    @OnClick(R.id.action_add)
    public void onAddClick() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onStartDrag(FunctionAdapter.FunctionHolder holder) {
        mItemTouchHelper.startDrag(holder);
    }

    @Override
    public void onDataChanged() {
        initStateText();
    }
}
