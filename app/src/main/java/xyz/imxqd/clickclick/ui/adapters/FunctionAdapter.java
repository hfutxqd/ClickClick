package xyz.imxqd.clickclick.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by imxqd on 2017/11/26.
 */

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionHolder> {

    @Override
    public FunctionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(FunctionHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class FunctionHolder extends RecyclerView.ViewHolder {

        public FunctionHolder(View itemView) {
            super(itemView);
        }
    }
}
