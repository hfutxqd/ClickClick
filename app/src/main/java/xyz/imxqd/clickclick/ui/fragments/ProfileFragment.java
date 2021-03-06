package xyz.imxqd.clickclick.ui.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.ui.AddHomeEventActivity;
import xyz.imxqd.clickclick.ui.AddKeyEventActivity;
import xyz.imxqd.clickclick.ui.adapters.ProfileAdapter;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.ScreenUtl;


public class ProfileFragment extends BaseFragment implements ProfileAdapter.ProfileChangeCallback, OnRefreshUI {

    private static final int REQUEST_CODE_ADD_KEY_EVENT = 1;

    @BindView(android.R.id.list)
    RecyclerView vList;

    @BindView(R.id.profile_state)
    TextView vState;

    @BindView(R.id.empty_view)
    FrameLayout mEmpty;

    ProfileAdapter mAdapter = new ProfileAdapter();
    ItemTouchHelper itemTouchHelper;
    ArrayAdapter<String> mMenuAdapter;

    public ProfileFragment() {
        LogUtils.d("ProfileFragment new instance");
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
            mAdapter.savePosition();
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
    public void onResume() {
        super.onResume();
        mAdapter.refreshData();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getActivity() != null;
        assert getContext() != null;

        mAdapter.setCheckChangeCallback(this);
        vList.setLayoutManager(new LinearLayoutManager(getContext()));
        vList.setAdapter(mAdapter);

        List<String> list = new ArrayList<>();
        list.add(getString(R.string.add_key_event_normal));
        list.add(getString(R.string.add_key_event_home));
        mMenuAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);

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

        vList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));



        if (!NotificationAccessUtil.isEnabled(getContext())) {
            LogUtils.d("NotificationAccess is disabled.");
        } else if (AppEventManager.getInstance().getNotificationService() == null) {
            try {
                getActivity().startService(new Intent(getActivity(), NotificationCollectorService.class));
            } catch (Throwable t) {
            }
        }
        initStateText();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.destroy();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void initStateText() {
        AppEventManager.getInstance().updateKeyEventData();
        if (isAdded() && !isDetached() && getActivity() != null) {
            int count = mAdapter.getEnableCount();
            String state = getString(R.string.profile_current_state, count);
            vState.setText(getBigNumberText(state));
            if (mAdapter.getItemCount() == 0) {
                mEmpty.setVisibility(View.VISIBLE);
            } else {
                mEmpty.setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.action_add)
    public void onAddClick() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        new AlertDialog.Builder(context)
                .setAdapter(mMenuAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startAddKeyEventActivity();
                            break;
                        case 1:
                            startAddHomeEventActivity();
                            break;
                        default:
                    }
                })
                .show();
    }

    private void startAddKeyEventActivity() {
        Intent intent = new Intent(getActivity(), AddKeyEventActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivityForResult(intent, REQUEST_CODE_ADD_KEY_EVENT);
    }

    private void startAddHomeEventActivity() {
        Intent intent = new Intent(getActivity(), AddHomeEventActivity.class);
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
            mAdapter.savePosition();
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

    @Override
    public void onRefreshUI() {
        if (isAdded() && !isDetached() && getActivity() != null) {
            mAdapter.refreshData();
            mAdapter.notifyDataSetChanged();
            initStateText();
            mAdapter.savePosition();
        }
    }
}
