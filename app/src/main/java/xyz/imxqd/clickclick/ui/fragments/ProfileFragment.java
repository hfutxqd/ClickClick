package xyz.imxqd.clickclick.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.ui.AddKeyEventActivity;
import xyz.imxqd.clickclick.ui.adapters.ProfileAdapter;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.ScreenUtl;


public class ProfileFragment extends BaseFragment implements ProfileAdapter.ProfileChangeCallback {

    private static final int REQUEST_CODE_ADD_KEY_EVENT = 1;

    @BindView(android.R.id.list)
    RecyclerView vList;

    @BindView(R.id.profile_state)
    TextView vState;

    @BindView(R.id.empty_view)
    FrameLayout mEmpty;

    ProfileAdapter mAdapter;
    ItemTouchHelper itemTouchHelper;

    public ProfileFragment() {
        Logger.d("ProfileFragment new instance");
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    ItemTouchHelper.Callback mCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN|ItemTouchHelper.UP, ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            mAdapter.swap(fromPosition, toPosition);
            mAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            long id = mAdapter.getItem(viewHolder.getAdapterPosition()).id;
            KeyMappingEvent.deleteById(id);
            mAdapter.refreshData();
            mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            mAdapter.savePosition();
            initStateText();
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            ProfileAdapter.KeyMapHolder holder = (ProfileAdapter.KeyMapHolder) viewHolder;
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                if (dX > 200f) {
                    dX = 200f;
                }
                holder.deleteAlpha(dX / 200f);
            } else {
                holder.deleteAlpha(0f);
            }
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
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
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new ProfileAdapter();
        mAdapter.setCheckChangeCallback(this);
        vList.setLayoutManager(new LinearLayoutManager(getContext()));
        vList.setAdapter(mAdapter);
        itemTouchHelper = new ItemTouchHelper(mCallback);
        itemTouchHelper.attachToRecyclerView(vList);
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

        assert getActivity() != null;
        assert getContext() != null;

        if (!NotificationAccessUtil.isEnabled(getContext())) {
            Logger.d("NotificationAccess is disabled.");
        } else {
            getActivity().startService(new Intent(getActivity(), NotificationCollectorService.class));
        }
        initStateText();
    }

    private void initStateText() {
        AppEventManager.getInstance().updateKeyEventData();
        int count = mAdapter.getEnableCount();
        String state = getString(R.string.profile_current_state, count);
        vState.setText(getBigNumberText(state));
        if (mAdapter.getItemCount() == 0) {
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            mEmpty.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.action_add)
    public void onAddClick() {
        Intent intent = new Intent(getActivity(), AddKeyEventActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivityForResult(intent, REQUEST_CODE_ADD_KEY_EVENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            mAdapter.refreshData();
            initStateText();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCheckedChanged(boolean isChecked) {
        initStateText();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);
    }
}
