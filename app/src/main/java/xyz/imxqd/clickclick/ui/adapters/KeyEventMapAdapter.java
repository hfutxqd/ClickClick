package xyz.imxqd.clickclick.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.imxqd.clickclick.R;

/**
 * Created by imxqd on 2017/11/26.
 */

public class KeyEventMapAdapter extends RecyclerView.Adapter<KeyEventMapAdapter.KeyMapHolder> {

    @Override
    public KeyEventMapAdapter.KeyMapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_key_event_map, parent, false);
        return new KeyMapHolder(v);
    }

    @Override
    public void onBindViewHolder(KeyEventMapAdapter.KeyMapHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 50;
    }

    static class KeyMapHolder extends RecyclerView.ViewHolder {

        public KeyMapHolder(View itemView) {
            super(itemView);
        }
    }
}
