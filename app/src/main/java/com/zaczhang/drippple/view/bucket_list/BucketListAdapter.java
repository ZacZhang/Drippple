package com.zaczhang.drippple.view.bucket_list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zaczhang.drippple.R;
import com.zaczhang.drippple.model.Bucket;
import com.zaczhang.drippple.view.shot_list.ShotListAdapter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


public class BucketListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_BUCKET = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private List<Bucket> data;
    private LoadMoreListener loadMoreListener;
    private boolean showLoading;
    private boolean isChoosingMode;

    public BucketListAdapter(@NonNull List<Bucket> data,
                             @NonNull LoadMoreListener loadMoreListener,
                             boolean isChoosingMode) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.isChoosingMode = isChoosingMode;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // note the warning for "final int position", it's for recycler view drag and drop
        // after drag and drop onBindViewHolder will not be called again with the new position
        // that's why you should not assume this position is always fixed

        // in our case, we do not support drag and drop in bucket list because Dribbble API
        // doesn't support reordering buckets, so using "final int position" is fine

        final int viewType = getItemViewType(position);

        if (viewType == VIEW_TYPE_LOADING) {
            loadMoreListener.onLoadMore();
        } else {
            final Bucket bucket = data.get(position);
            BucketViewHolder bucketViewHolder = (BucketViewHolder) holder;
            Context context = holder.itemView.getContext();

            // 0 -> 0 shot
            // 1 -> 1 shot
            // 2 -> 2 shots
            String bucketShotCountString = MessageFormat.format(
                    holder.itemView.getContext().getResources().getString(R.string.shot_count), bucket.shots_count);


            bucketViewHolder.bucketName.setText(bucket.name);
            bucketViewHolder.bucketShotCount.setText(bucketShotCountString);

            if (isChoosingMode) {
                bucketViewHolder.bucketChosen.setVisibility(View.VISIBLE);

                if (bucket.isChoosing) {
                    bucketViewHolder.bucketChosen.setImageDrawable(ContextCompat
                            .getDrawable(context, R.drawable.ic_check_box_black_24dp));
                } else {
                    bucketViewHolder.bucketChosen.setImageDrawable(ContextCompat
                            .getDrawable(context, R.drawable.ic_check_box_outline_blank_black_24dp));
                }

                bucketViewHolder.bucketLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bucket.isChoosing = !bucket.isChoosing;
                        notifyItemChanged(position);
                    }
                });
            } else {
                bucketViewHolder.bucketChosen.setVisibility(View.GONE);

                bucketViewHolder.bucketLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // if not in choosing mode, we need to open a new Activity to show
                        // what shots are in this bucket, we will need ShotListFragment here
                    }
                });
            }
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

    public ArrayList<String> getSelectedBucketIDs() {
        ArrayList<String> selectedBucketIDs = new ArrayList<>();
        for (Bucket bucket : data) {
            if (bucket.isChoosing) {
                selectedBucketIDs.add(bucket.id);
            }
        }
        return selectedBucketIDs;
    }
}
