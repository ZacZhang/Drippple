package com.zaczhang.drippple.view.shot_list;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.zaczhang.drippple.R;
import com.zaczhang.drippple.model.Shot;
import com.zaczhang.drippple.utils.ImageUtils;
import com.zaczhang.drippple.utils.ModelUtils;
import com.zaczhang.drippple.view.base.BaseViewHolder;
import com.zaczhang.drippple.view.base.InfiniteAdapter;
import com.zaczhang.drippple.view.shot_detail.ShotActivity;
import com.zaczhang.drippple.view.shot_detail.ShotFragment;

import java.util.List;



public class ShotListAdapter extends InfiniteAdapter<Shot> {

    private final ShotListFragment shotListFragment;


    // adapter接收一个callback，当需要加载更多数据的时候，调用这个callback。adapter只负责把数据显示到界面上。
    public ShotListAdapter(@NonNull ShotListFragment shotListFragment,
                           @NonNull List<Shot> data,
                           @NonNull LoadMoreListener loadMoreListener) {
        super(shotListFragment.getContext(), data, loadMoreListener);
        this.shotListFragment = shotListFragment;
    }

    @Override
    protected BaseViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_shot, parent, false);
        return new ShotViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(BaseViewHolder holder, int position) {
        ShotViewHolder shotViewHolder = (ShotViewHolder) holder;

        final Shot shot = getData().get(position);

        // 进入具体的shot
        // launch ShotActivity and pass the Shot data to it
        shotViewHolder.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ShotActivity.class);
                intent.putExtra(ShotFragment.KEY_SHOT, ModelUtils.toString(shot, new TypeToken<Shot>() {}));
                // 单独传一个title是因为ShotActivity接收到Shot数据，原封不动的传给ShotFragment。
                // 为了ShotActivity取到标题时，不需要解序列化，这里单独再传一个title
                intent.putExtra(ShotActivity.KEY_SHOT_TITLE, shot.title);
                shotListFragment.startActivityForResult(intent,ShotListFragment.REQ_CODE_SHOT);
            }
        });

        shotViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
        shotViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
        shotViewHolder.viewCount.setText(String.valueOf(shot.views_count));

        ImageUtils.loadShotImage(shot, shotViewHolder.image);
    }
}
