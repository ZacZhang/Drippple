package com.zaczhang.drippple.view.shot_detail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zaczhang.drippple.R;
import com.zaczhang.drippple.model.Shot;
import com.zaczhang.drippple.utils.ImageUtils;

// ShotAdapter is used to display a Shot object as items in RecyclerView
public class ShotAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_SHOT_IMAGE = 0;
    private static final int VIEW_TYPE_SHOT_INFO = 1;
    private static final int VIEW_TYPE_SHOT_COMMENT = 2;

    private final ShotFragment shotFragment;
    private final Shot shot;


    public ShotAdapter(@NonNull ShotFragment shotFragment, @NonNull Shot shot) {
        this.shotFragment = shotFragment;
        this.shot = shot;
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

            case VIEW_TYPE_SHOT_COMMENT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_item_comment, parent, false);
                return new CommentViewHolder(view);

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
                ImageUtils.loadShotImage(shot, ((ImageViewHolder) holder).image);
                break;

            case VIEW_TYPE_SHOT_INFO:
                final InfoViewHolder infoViewHolder = (InfoViewHolder) holder;

                infoViewHolder.title.setText(shot.title);
                infoViewHolder.authorName.setText(shot.user.name);

                if (shot.description != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        infoViewHolder.description.setText(Html.fromHtml(shot.description, Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        infoViewHolder.description.setText(Html.fromHtml(shot.description));
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        infoViewHolder.description.setText(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        infoViewHolder.description.setText(Html.fromHtml(""));
                    }
                }

                // 设置文本可滚动
                infoViewHolder.description.setMovementMethod(LinkMovementMethod.getInstance());

                // infoViewHolder.authorPicture.setImageURI(Uri.parse(shot.user.avatar_url));
                infoViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
                infoViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
                infoViewHolder.viewCount.setText(String.valueOf(shot.views_count));

                ImageUtils.loadUserPicture(getContext(), infoViewHolder.authorPicture, shot.user.avatar_url);

                infoViewHolder.likeCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "Like count clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                infoViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shotFragment.like(shot.id, !shot.liked);
                    }
                });

                infoViewHolder.bucketCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "Bucket count clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                // 启动新的activity（选择要添加的bucket）
                infoViewHolder.bucketButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shotFragment.bucket();
                    }
                });

                // 分享
                infoViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shotFragment.share();
                    }
                });

                Drawable likeDrawable;
                if (shot.liked) {
                    likeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_pink_18dp);
                } else {
                    likeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_black_18dp);
                }
                infoViewHolder.likeButton.setImageDrawable(likeDrawable);

                Drawable bucketDrawable;
                if (shot.bucketed) {
                    bucketDrawable = ContextCompat.getDrawable(infoViewHolder.itemView.getContext(),
                            R.drawable.ic_move_to_inbox_pink_18dp);
                } else {
                    bucketDrawable = ContextCompat.getDrawable(infoViewHolder.itemView.getContext(),
                            R.drawable.ic_move_to_inbox_black_18dp);
                }
                infoViewHolder.bucketButton.setImageDrawable(bucketDrawable);

                break;

            case VIEW_TYPE_SHOT_COMMENT:
                CommentViewHolder commentViewHolder = (CommentViewHolder) holder;

                // commentViewHolder.comment.setText(shot.comment.get(0));
                commentViewHolder.comment.setText("come on");
                break;
        }
    }

    @Override
    public int getItemCount() {
        // three viewTypes
        // return shot.comment.size() + 2;
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        // mapping from position to ViewType
        switch (position) {
            case 0:
                return VIEW_TYPE_SHOT_IMAGE;
            case 1:
                return VIEW_TYPE_SHOT_INFO;
//            case 2:
//                return VIEW_TYPE_SHOT_COMMENT;
            default:
                return VIEW_TYPE_SHOT_COMMENT;
        }

//        if (position == 0) {
//            return VIEW_TYPE_SHOT_IMAGE;
//        } else {
//            return VIEW_TYPE_SHOT_INFO;
//        }
    }

    @NonNull
    private Context getContext() {
        return shotFragment.getContext();
    }
}
