package com.zaczhang.drippple.view.shot_detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.zaczhang.drippple.R;
import com.zaczhang.drippple.model.Shot;

// ShotAdapter is used to display a Shot object as items in RecyclerView
public class ShotAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_SHOT_IMAGE = 0;
    private static final int VIEW_TYPE_SHOT_INFO = 1;

    private final Shot shot;

    public ShotAdapter(@NonNull Shot shot) {
        this.shot = shot;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SHOT_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_item_image, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_item_info, parent, false);
            return new InfoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_SHOT_IMAGE) {
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(shot.getImageUrl()))
                    .setAutoPlayAnimations(true)
                    .build();

            ((ImageViewHolder) holder).image.setController(controller);
        } else {
            InfoViewHolder infoViewHolder = (InfoViewHolder) holder;

            infoViewHolder.title.setText(shot.title);
            infoViewHolder.authorName.setText(shot.user.name);
            infoViewHolder.description.setText(shot.description);
            infoViewHolder.authorPicture.setImageURI(Uri.parse(shot.user.avatar_url));
            infoViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
            infoViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
            infoViewHolder.viewCount.setText(String.valueOf(shot.views_count));

            infoViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    share(view.getContext());
                }
            });
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

    private void share(Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
        intent.setType("text/plain");
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_shot)));
    }
}
