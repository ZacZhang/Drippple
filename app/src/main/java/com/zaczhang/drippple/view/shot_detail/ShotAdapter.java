package com.zaczhang.drippple.view.shot_detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.zaczhang.drippple.R;
import com.zaczhang.drippple.model.Shot;
import com.zaczhang.drippple.view.bucket_list.BucketListFragment;
import com.zaczhang.drippple.view.bucket_list.ChooseBucketActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// ShotAdapter is used to display a Shot object as items in RecyclerView
public class ShotAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_SHOT_IMAGE = 0;
    private static final int VIEW_TYPE_SHOT_INFO = 1;

    private final ShotFragment shotFragment;
    private final Shot shot;

    private ArrayList<String> collectedBucketIDs;

    public ShotAdapter(@NonNull ShotFragment shotFragment, @NonNull Shot shot) {
        this.shotFragment = shotFragment;
        this.shot = shot;
        this.collectedBucketIDs = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case VIEW_TYPE_SHOT_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_item_image, parent, false);
                return new ImageViewHolder(view);

            case VIEW_TYPE_SHOT_INFO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_item_info, parent, false);
                return new InfoViewHolder(view);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);

        switch (viewType) {
            case VIEW_TYPE_SHOT_IMAGE:
                // play gif automatically
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse(shot.getImageUrl()))
                        .setAutoPlayAnimations(true)
                        .build();

                ((ImageViewHolder) holder).image.setController(controller);
                break;

            case VIEW_TYPE_SHOT_INFO:
                InfoViewHolder infoViewHolder = (InfoViewHolder) holder;

                infoViewHolder.title.setText(shot.title);
                infoViewHolder.authorName.setText(shot.user.name);
                infoViewHolder.description.setText(shot.description);
                infoViewHolder.authorPicture.setImageURI(Uri.parse(shot.user.avatar_url));
                infoViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
                infoViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
                infoViewHolder.viewCount.setText(String.valueOf(shot.views_count));

                Drawable bucketDrawable;
                if (shot.bucketed) {
                    bucketDrawable = ContextCompat.getDrawable(infoViewHolder.itemView.getContext(),
                            R.drawable.ic_move_to_inbox_pink_18dp);
                } else {
                    bucketDrawable = ContextCompat.getDrawable(infoViewHolder.itemView.getContext(),
                            R.drawable.ic_move_to_inbox_black_18dp);
                }
                infoViewHolder.bucketButton.setImageDrawable(bucketDrawable);

                infoViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        share(view.getContext());
                    }
                });

                infoViewHolder.bucketButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        // 启动新的activity（选择要添加的bucket）
                        bucket(view.getContext());
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        // two viewTypes
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_SHOT_IMAGE;
        } else {
            return VIEW_TYPE_SHOT_INFO;
        }
    }

    public List<String> getReadOnlyCollectedBucketIDs() {
        // 获取刚进去时的bucket id
        return Collections.unmodifiableList(collectedBucketIDs);
    }

    public void updateCollectedBucketIDs(@NonNull List<String> bucketIDs) {
        if (collectedBucketIDs == null) {
            collectedBucketIDs = new ArrayList<>();
        }

        collectedBucketIDs.clear();
        collectedBucketIDs.addAll(bucketIDs);

        shot.bucketed = !bucketIDs.isEmpty();
        notifyDataSetChanged();
    }

    public void updateCollectedBucketIDs(@NonNull List<String> addedIDs,
                                         @NonNull List<String> removedIDs) {
        if (collectedBucketIDs == null) {
            collectedBucketIDs = new ArrayList<>();
        }

        collectedBucketIDs.addAll(addedIDs);
        collectedBucketIDs.removeAll(removedIDs);

        shot.bucketed = !collectedBucketIDs.isEmpty();
        shot.buckets_count = shot.buckets_count + addedIDs.size() - removedIDs.size();
        notifyDataSetChanged();
    }

    private void share(Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
        intent.setType("text/plain");
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_shot)));
    }

    private void bucket(Context context) {
        if (collectedBucketIDs != null) {
            // collectedBucketIDs == null means we're still loading
            Intent intent = new Intent(context, ChooseBucketActivity.class);
            intent.putStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS, collectedBucketIDs);
            shotFragment.startActivityForResult(intent, ShotFragment.REQ_CODE_BUCKET);
        }
    }
}
