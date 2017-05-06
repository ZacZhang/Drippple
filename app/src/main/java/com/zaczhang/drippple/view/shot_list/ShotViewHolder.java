package com.zaczhang.drippple.view.shot_list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zaczhang.drippple.R;
import com.zaczhang.drippple.view.base.BaseViewHolder;

import butterknife.BindView;


public class ShotViewHolder extends BaseViewHolder {

    @BindView(R.id.shot_clickable_cover) View cover;
    @BindView(R.id.shot_like_count) TextView likeCount;
    @BindView(R.id.shot_bucket_count) TextView bucketCount;
    @BindView(R.id.shot_view_count) TextView viewCount;
    @BindView(R.id.shot_image) ImageView image;


    public ShotViewHolder(View itemView) {
        super(itemView);
    }
}
