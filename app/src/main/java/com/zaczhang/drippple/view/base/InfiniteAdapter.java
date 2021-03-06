package com.zaczhang.drippple.view.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zaczhang.drippple.R;

import java.util.List;


public abstract class InfiniteAdapter<M> extends RecyclerView.Adapter<BaseViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private List<M> data;
    private final Context context;

    private final LoadMoreListener loadMoreListener;
    private boolean showLoading;

    public InfiniteAdapter(@NonNull Context context,
                           @NonNull List<M> data,
                           @NonNull LoadMoreListener loadMoreListener) {
        this.context = context;
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.showLoading = true;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_loading, parent, false);
            return new BaseViewHolder(view);
        } else {
            return onCreateItemViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == TYPE_LOADING) {
            loadMoreListener.onLoadMore();
        } else {
            onBindItemViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        if (showLoading) {
            return data.size() + 1;
        } else {
            return data.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading) {
            if (position == data.size()) {
                return TYPE_LOADING;
            } else {
                return TYPE_ITEM;
            }
        } else {
            return TYPE_ITEM;
        }
    }

    // 加到最后
    public void append(@NonNull List<M> data) {
        // 更新数据
        this.data.addAll(data);
        // 更新界面
        notifyDataSetChanged();
    }

    // 加到最前
    public void prepend(@NonNull List<M> data) {
        this.data.addAll(0, data);
        notifyDataSetChanged();
    }

    public void setData(@NonNull List<M> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    public List<M> getData() {
        return data;
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    protected Context getContext() {
        return context;
    }

    protected abstract BaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);

    protected abstract void onBindItemViewHolder(BaseViewHolder holder, int position);

    public interface LoadMoreListener {
        void onLoadMore();
    }
}
