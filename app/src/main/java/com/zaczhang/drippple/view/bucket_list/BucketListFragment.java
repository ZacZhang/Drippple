package com.zaczhang.drippple.view.bucket_list;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.os.TraceCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.imagepipeline.producers.AddImageTransformMetaDataProducer;
import com.google.gson.JsonSyntaxException;
import com.zaczhang.drippple.R;
import com.zaczhang.drippple.dribbble.Dribbble;
import com.zaczhang.drippple.dribbble.DribbbleException;
import com.zaczhang.drippple.model.Bucket;
import com.zaczhang.drippple.view.base.DribbbleTask;
import com.zaczhang.drippple.view.base.InfiniteAdapter;
import com.zaczhang.drippple.view.base.SpaceItemDecoration;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Random;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BucketListFragment extends Fragment {

    public static final int REQ_CODE_NEW_BUCKET = 100;

    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_CHOOSING_MODE = "choosing_mode";
    public static final String KEY_CHOSEN_BUCKET_IDS = "chosen_bucket_ids";
    public static final String KEY_COLLECTED_BUCKET_IDS = "collected_bucket_ids";

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_container) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fab) FloatingActionButton fab;

    private BucketListAdapter adapter;

    private String userID;
    private boolean isChoosingMode;
    private Set<String> collectedBucketIDSet;

    private InfiniteAdapter.LoadMoreListener onLoadMore = new InfiniteAdapter.LoadMoreListener() {
        @Override
        public void onLoadMore() {
            AsyncTaskCompat.executeParallel(new LoadBucketsTask(false));
        }
    };

    public static BucketListFragment newInstance(@NonNull String userID,
                                                 boolean isChoosingMode,
                                                 @Nullable ArrayList<String> chosenBucketIDs) {
        Bundle args = new Bundle();
        args.putString(KEY_USER_ID, userID);
        args.putBoolean(KEY_CHOOSING_MODE, isChoosingMode);
        args.putStringArrayList(KEY_COLLECTED_BUCKET_IDS, chosenBucketIDs);

        BucketListFragment fragment = new BucketListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe_fab_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // get arguments
        final Bundle args = getArguments();
        userID = args.getString(KEY_USER_ID);
        isChoosingMode = args.getBoolean(KEY_CHOOSING_MODE);

        if (isChoosingMode) {
            List<String> chosenBucketIDList = args.getStringArrayList(KEY_COLLECTED_BUCKET_IDS);
            if (chosenBucketIDList != null) {
                collectedBucketIDSet = new HashSet<>(chosenBucketIDList);
            }
        } else {
            collectedBucketIDSet = new HashSet<>();
        }

        // init UI
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncTaskCompat.executeParallel(new LoadBucketsTask(true));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        adapter = new BucketListAdapter(getContext(), new ArrayList<Bucket>(), onLoadMore, isChoosingMode);
        recyclerView.setAdapter(adapter);

        // 点击之后弹出添加新bucket的对话框
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewBucketDialogFragment dialogFragment = NewBucketDialogFragment.newInstance();

                // 把 TargetFragment 设置成主调fragment (即BucketListFragment)
                dialogFragment.setTargetFragment(BucketListFragment.this, REQ_CODE_NEW_BUCKET);
                dialogFragment.show(getFragmentManager(), NewBucketDialogFragment.TAG);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isChoosingMode) {
            inflater.inflate(R.menu.bucket_list_choose_mode_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 当save被点击时
        if (item.getItemId() == R.id.save) {
            ArrayList<String> chosenBucketIDs = new ArrayList<>();
            for (Bucket bucket : adapter.getData()) {
                if (bucket.isChoosing) {
                    chosenBucketIDs.add(bucket.id);
                }
            }

            Intent intent = new Intent();
            intent.putStringArrayListExtra(KEY_CHOSEN_BUCKET_IDS, chosenBucketIDs);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }


    // 获取新建Bucket之后传回来的数据
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_NEW_BUCKET && resultCode == Activity.RESULT_OK) {
            String bucketName = data.getStringExtra(NewBucketDialogFragment.KEY_BUCKET_NAME);
            String bucketDescription = data.getStringExtra(NewBucketDialogFragment.KEY_BUCKET_DESCRIPTION);
            // 如果是空的name，就什么都不做。否则就启动新的AsyncTask发送网络请求来完成新建bucket的工作
            if (!TextUtils.isEmpty(bucketName)) {
                AsyncTaskCompat.executeParallel(new NewBucketTask(bucketName, bucketDescription));
            }
        }
    }



    // 加载已有的buckets
    private class LoadBucketsTask extends DribbbleTask<Void, Void, List<Bucket>> {

        private boolean refresh;

        public LoadBucketsTask(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected List<Bucket> doJob(Void... params) throws DribbbleException {
            final int page;
            if (refresh) {
                page = 1;
            } else {
                page = adapter.getData().size() / Dribbble.COUNT_PER_PAGE + 1;
            }

            if (userID == null) {
                // 如果没有userID，则直接用access token的形式获得用户的收藏夹
                return Dribbble.getUserBuckets(page);
            } else {
                // 如果有userID，则用userID的形式获得用户的收藏夹
                return Dribbble.getUserBuckets(userID, page);
            }
        }

        @Override
        protected void onSuccess(List<Bucket> buckets) {
            // 如果buckets的个数大于等于12个，说明可能还会有更多的，故显示loading animation。
            // 其实这里应该用 ==，因为dribbble不会返回超过12个
            adapter.setShowLoading(buckets.size() >= Dribbble.COUNT_PER_PAGE);

            for (Bucket bucket : buckets) {
                // 如果已选择的buckets中包含当前bucket，则将isChoosing更新为true(将CheckBox勾上)
                if (collectedBucketIDSet != null && collectedBucketIDSet.contains(bucket.id)) {
                    bucket.isChoosing = true;
                }
            }

            if (refresh) {
                adapter.setData(buckets);
                swipeRefreshLayout.setRefreshing(false);
            } else {
                // 将更新为true的bucket添加到界面上
                adapter.append(buckets);
            }

            swipeRefreshLayout.setEnabled(true);
        }

        @Override
        protected void onFailed(DribbbleException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    // 新建bucket(网络请求)
    private class NewBucketTask extends DribbbleTask<Void, Void, Bucket> {

        private String name;
        private String description;

        private NewBucketTask(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        protected Bucket doJob(Void... params) throws DribbbleException {
            return Dribbble.newBucket(name, description);
        }

        @Override
        protected void onSuccess(Bucket bucket) {
            // 新建的bucket默认是选中的
            bucket.isChoosing = true;
            // 更新bucket列表，prepend是把新建的bucket加到最前面
            adapter.prepend(Collections.singletonList(bucket));
        }

        @Override
        protected void onFailed(DribbbleException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }
}
