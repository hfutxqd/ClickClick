package xyz.imxqd.clickclick.ui.adapters;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;

/**
 * Created by imxqd on 2017/11/26.
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.KeyMapHolder> {

    List<KeyMappingEvent> mEvents;
    ProfileChangeCallback mCallback;
    ObservableEmitter<Object> savePositionEmitter;

    @SuppressLint("CheckResult")
    public ProfileAdapter() {
        mEvents = KeyMappingEvent.getOrderedAll();
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                savePositionEmitter = emitter;
            }
        }).debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        for (int i = 0; i < mEvents.size(); i++) {
                            mEvents.get(i).order = i + 1;
                            mEvents.get(i).save();
                        }
                    }
                });
    }

    public void setCheckChangeCallback(ProfileChangeCallback callback) {
        mCallback = callback;
    }

    public int getEnableCount() {
        int count = 0;
        for (KeyMappingEvent e : mEvents) {
            if (e.enable) {
                count++;
            }
        }
        return count;
    }

    public KeyMappingEvent getItem(int pos) {
        return mEvents.get(pos);
    }

    public void refreshData() {
        List<KeyMappingEvent> events = KeyMappingEvent.getOrderedAll();
        mEvents.clear();
        mEvents.addAll(events);
    }

    public void swap(int index1, int index2) {
        Collections.swap(mEvents, index1, index2);
    }

    public void savePosition() {
        if (savePositionEmitter != null) {
            savePositionEmitter.onNext(new Object());
        }
    }

    @Override
    public ProfileAdapter.KeyMapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_profile, parent, false);
        return new KeyMapHolder(v);
    }

    @Override
    public void onBindViewHolder(ProfileAdapter.KeyMapHolder holder, int position) {
        KeyMappingEvent event = mEvents.get(position);
        holder.title.setText(event.funcName);
        holder.subTitle.setText(event.keyName + "  " + event.eventType.getName());
        holder.enable.setChecked(event.enable);
        holder.deleteAlpha(0f);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class KeyMapHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.profile_drag_handle)
        ImageView handle;
        @BindView(R.id.profile_title)
        TextView title;
        @BindView(R.id.profile_sub_title)
        TextView subTitle;
        @BindView(R.id.profile_switch)
        SwitchCompat enable;
        @BindView(R.id.profile_delete_hint)
        TextView deleteHint;
        @BindView(R.id.profile_item_content)
        View content;
        public KeyMapHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            enable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = getAdapterPosition();
                mEvents.get(pos).enable = isChecked;
                mEvents.get(pos).save();
                if (mCallback != null) {
                    mCallback.onCheckedChanged(isChecked);
                }
            });
            handle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mCallback != null) {
                        mCallback.onStartDrag(KeyMapHolder.this);
                    }
                }
                return true;
            });
        }

        public void deleteAlpha(float alpha) {
            deleteHint.setAlpha(alpha);
            content.setAlpha(1 - alpha);
        }
    }

    public interface ProfileChangeCallback {
        void onCheckedChanged(boolean isChecked);
        void onStartDrag(RecyclerView.ViewHolder holder);
    }
}
