package xyz.imxqd.clickclick.ui.adapters;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.imxqd.clickclick.R;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder>{

    List<AppInfo> packages = new ArrayList<>();

    private OnAppSelectedCallback mCallback;


    public void setOnAppSelectedCallback(OnAppSelectedCallback callback) {
        mCallback = callback;
    }


    public void setData(List<AppInfo> data) {
        packages.clear();
        packages.addAll(data);
    }

    @NonNull
    @Override
    public AppListAdapter.AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_app, parent, false);
        return new AppViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppListAdapter.AppViewHolder holder, int position) {
        AppInfo info = packages.get(position);
        holder.name.setText(info.name);
        holder.packageName.setText(info.packageName);
        holder.icon.setImageDrawable(info.icon);
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    public class AppViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.app_icon)
        ImageView icon;
        @BindView(R.id.app_name)
        TextView name;
        @BindView(R.id.app_package)
        TextView packageName;

        public AppViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                if (mCallback != null) {
                    AppInfo appInfo = packages.get(getAdapterPosition());
                    mCallback.onAppSelect(appInfo);
                }
            });
        }
    }

    public static class AppInfo {
        public String name;
        public Drawable icon;
        public String packageName;
    }

    public interface OnAppSelectedCallback {
        void onAppSelect(AppInfo appInfo);
    }
}
