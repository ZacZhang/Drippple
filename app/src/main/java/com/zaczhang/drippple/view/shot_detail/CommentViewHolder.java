package com.zaczhang.drippple.view.shot_detail;

import android.view.View;
import android.widget.TextView;

import com.zaczhang.drippple.R;
import com.zaczhang.drippple.view.base.BaseViewHolder;

import butterknife.BindView;


public class CommentViewHolder extends BaseViewHolder {

    @BindView(R.id.shot_comment) TextView comment;

    public CommentViewHolder(View itemView) {
        super(itemView);
    }
}
