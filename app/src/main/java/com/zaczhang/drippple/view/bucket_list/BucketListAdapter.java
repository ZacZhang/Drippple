package com.zaczhang.drippple.view.bucket_list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zaczhang.drippple.R;
import com.zaczhang.drippple.model.Bucket;
import com.zaczhang.drippple.view.shot_list.ShotListAdapter;

import java.text.MessageFormat;
import java.util.List;


public class BucketListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_BUCKET = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private List<Bucket> data;
    private LoadMoreListener loadMoreListener;
    private boolean showLoading;

    public BucketListAdapter(@NonNull List<Bucket> data, @NonNull LoadMoreListener loadMoreListener) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.showLoading = true;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_BUCKET) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_bucket, parent, false);
            return new BucketViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_bucket, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_LOADING) {
            loadMoreListener.onLoadMore();
        } else {
            Bucket bucket = data.get(position);

            // 0 -> 0 shot
            // 1 -> 1 shot
            // 2 -> 2 shots
            String bucketShotCountString = MessageFormat.format(
                    holder.itemView.getContext().getResources().getString(R.string.shot_count), bucket.shots_count);

            BucketViewHolder bucketViewHolder = (BucketViewHolder) holder;
            bucketViewHolder.bucketName.setText(bucket.name);
            bucketViewHolder.bucketShotCount.setText(bucketShotCountString);

            bucketViewHolder.bucketChosen.setVisibility(View.GONE);
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
        if (position < data.size()) {
            return VIEW_TYPE_BUCKET;
        } else {
            return VIEW_TYPE_LOADING;
        }
    }

    public void append(@NonNull List<Bucket> moreBuckets) {
        data.addAll(moreBuckets);
        notifyDataSetChanged();
    }

    public void prepend(@NonNull List<Bucket> data) {
        this.data.addAll(0, data);
        notifyDataSetChanged();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    public interface LoadMoreListener{
        void onLoadMore();
    }
}
