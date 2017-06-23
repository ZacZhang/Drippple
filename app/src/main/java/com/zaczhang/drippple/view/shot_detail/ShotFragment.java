package com.zaczhang.drippple.view.shot_detail;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.zaczhang.drippple.R;
import com.zaczhang.drippple.dribbble.Dribbble;
import com.zaczhang.drippple.dribbble.DribbbleException;
import com.zaczhang.drippple.model.Bucket;
import com.zaczhang.drippple.model.Shot;
import com.zaczhang.drippple.utils.ModelUtils;
import com.zaczhang.drippple.view.base.DribbbleTask;
import com.zaczhang.drippple.view.bucket_list.BucketListActivity;
import com.zaczhang.drippple.view.bucket_list.BucketListFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ShotFragment extends Fragment {

    public static final String KEY_SHOT = "shot";
    public static final int REQ_CODE_BUCKET = 100;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private Shot shot;
    private boolean isLiking;
    private ArrayList<String> collectedBucketIDs;

    // 接收传进来的整个bundle，设置成setArguments, 然后return这个fragment
    // 通过这种方式完成ShotActivity到ShotFragment的数据传递
    public static ShotFragment newInstance(@NonNull Bundle args) {
        ShotFragment fragment = new ShotFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shot, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        // json解序列化
        // getArguments()得到传进来的bundle，然后取出key为shot的json字符串，解序列化为shot对象
        shot = ModelUtils.toObject(getArguments().getString(KEY_SHOT), new TypeToken<Shot>() {});

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ShotAdapter(this, shot));

        isLiking = true;
        AsyncTaskCompat.executeParallel(new CheckLikeTask());
        AsyncTaskCompat.executeParallel(new LoadBucketsTask());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_BUCKET && resultCode == Activity.RESULT_OK) {
            List<String> chosenBucketIDs = data.getStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS);
            List<String> addedBucketIDs = new ArrayList<>();
            List<String> removedBucketIDs = new ArrayList<>();

            // 拿到界面上被选中的bucket id, 然后和刚进入的的bucket id对比，多出来就加上
            // chosenBucketIDs是选择结束后的，collectedBucketIDs是刚进去时的bucketIDs
            for (String chosenBucketID : chosenBucketIDs) {
                // 刚进界面时没有的就是多出来的
                if (!collectedBucketIDs.contains(chosenBucketID)) {
                    addedBucketIDs.add(chosenBucketID);
                }
            }

            // 拿到界面上被选中的bucket id, 然后和刚进入的bucket id对比，少了就减去
            for (String collectedBucketID : collectedBucketIDs) {
                // 选中之后没有的就是被删掉的
                if (!chosenBucketIDs.contains(collectedBucketID)) {
                    removedBucketIDs.add(collectedBucketID);
                }
            }

            AsyncTaskCompat.executeParallel(new UpdateBucketTask(addedBucketIDs, removedBucketIDs));
        }
    }

    private void setResult() {
        Intent intent = new Intent();
        intent.putExtra(KEY_SHOT, ModelUtils.toString(shot, new TypeToken<Shot>() {}));
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    public void like(@NonNull String shotID, boolean like) {
        if (!isLiking) {
            isLiking = true;
            AsyncTaskCompat.executeParallel(new LikeTask(shotID, like));
        }
    }

    // 点击bucket，启动新的activity，并传入数据
    public void bucket() {
        if (collectedBucketIDs == null) {
            Snackbar.make(getView(), R.string.shot_detail_loading_buckets, Snackbar.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(getContext(), BucketListActivity.class);
            intent.putExtra(BucketListFragment.KEY_CHOOSING_MODE, true);
            // 把已经加入过的buckets传进去
            intent.putStringArrayListExtra(BucketListFragment.KEY_COLLECTED_BUCKET_IDS, collectedBucketIDs);
            startActivityForResult(intent, REQ_CODE_BUCKET);
        }
    }

    public void share() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shot.title + " " + shot.html_url);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_shot)));
    }



    private class LikeTask extends DribbbleTask<Void, Void, Void> {

        private String id;
        private boolean like;

        public LikeTask(String id, boolean like) {
            this.id = id;
            this.like = like;
        }

        @Override
        protected Void doJob(Void... params) throws DribbbleException {
            if (like) {
                Dribbble.likeShot(id);
            } else {
                Dribbble.unlikeShot(id);
            }
            return null;
        }

        @Override
        protected void onSuccess(Void aVoid) {
            isLiking = false;

            shot.liked = like;
            if (like) {
                shot.likes_count++;
            } else {
                shot.likes_count--;
            }

            recyclerView.getAdapter().notifyDataSetChanged();

            setResult();
        }

        @Override
        protected void onFailed(DribbbleException e) {
            isLiking = false;
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }



    private class CheckLikeTask extends DribbbleTask<Void, Void, Boolean> {

        // 检查是否喜欢当前shot，如果like，点亮爱心
        @Override
        protected Boolean doJob(Void... params) throws DribbbleException {
            return Dribbble.isLikingShot(shot.id);
        }

        @Override
        protected void onSuccess(Boolean result) {
            isLiking = false;
            shot.liked = result;
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        @Override
        protected void onFailed(DribbbleException e) {
            isLiking = false;
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }



    private class LoadBucketsTask extends DribbbleTask<Void, Void, List<String>> {

        @Override
        protected List<String> doJob(Void... params) throws DribbbleException {
            // 当前shot被哪些buckets收藏了
            List<Bucket> shotBuckets = Dribbble.getShotBuckets(shot.id);

            // 用户自己有哪些收藏夹
            List<Bucket> userBuckets = Dribbble.getUserBuckets();

            // 取交集就是当前shot被放入了当前用户的哪些收藏夹
            Set<String> userBucketIDs = new HashSet<>();

            // 先把userBuckets放到HashSet中
            for (Bucket userBucket : userBuckets) {
                userBucketIDs.add(userBucket.id);
            }

            // 刚进入时被哪些buckets收藏了
            List<String> collectedBucketIDs = new ArrayList<>();
            for (Bucket shotBucket : shotBuckets) {
                if (userBucketIDs.contains(shotBucket.id)) {
                    collectedBucketIDs.add(shotBucket.id);
                }
            }
            return collectedBucketIDs;
        }

        @Override
        protected void onSuccess(List<String> result) {
            collectedBucketIDs = new ArrayList<>(result);

            // 大于0表示至少被一个bucket收藏了
            if (result.size() > 0) {
                shot.bucketed = true;
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }

        @Override
        protected void onFailed(DribbbleException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }



    private class UpdateBucketTask extends DribbbleTask<Void, Void, Void> {

        private List<String> added;
        private List<String> removed;

        private UpdateBucketTask(@NonNull List<String> added, @NonNull List<String> removed) {
            this.added = added;
            this.removed = removed;
        }

        @Override
        protected Void doJob(Void... params) throws DribbbleException {
            for (String addedID : added) {
                // 把shot.id添加到addedID收藏夹
                Dribbble.addBucketShot(addedID, shot.id);
            }

            for (String removedID : removed) {
                // 把shot.id从removeID收藏夹移除
                Dribbble.removeBucketShot(removedID, shot.id);
            }
            return null;
        }

        // 更新bucketsID
        @Override
        protected void onSuccess(Void aVoid) {
            collectedBucketIDs.addAll(added);
            collectedBucketIDs.removeAll(removed);

            shot.bucketed = !collectedBucketIDs.isEmpty();
            shot.buckets_count = shot.buckets_count + added.size() - removed.size();

            recyclerView.getAdapter().notifyDataSetChanged();

            setResult();
        }

        @Override
        protected void onFailed(DribbbleException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }
}