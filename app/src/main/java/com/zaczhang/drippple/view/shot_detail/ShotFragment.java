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
import com.zaczhang.drippple.model.Bucket;
import com.zaczhang.drippple.model.Shot;
import com.zaczhang.drippple.utils.ModelUtils;
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

    private ShotAdapter adapter;
    private Shot shot;

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
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        // json解序列化
        shot = ModelUtils.toObject(getArguments().getString(KEY_SHOT), new TypeToken<Shot>(){});

        adapter = new ShotAdapter(this, shot);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        AsyncTaskCompat.executeParallel(new LoadCollectedBucketIDsTask());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_BUCKET && requestCode == Activity.RESULT_OK) {
            List<String> chosenBucketIDs = data.getStringArrayListExtra(BucketListFragment.KEY_CHOSEN_BUCKET_IDS);
            List<String> addedBucketIDs = new ArrayList<>();
            List<String> removedBucketIDs = new ArrayList<>();
            List<String> collectedBucketIDs = adapter.getReadOnlyCollectedBucketIDs();

            // 拿到界面上被选中的bucket id, 然后和之前的bucket id对比，多出来就加上
            for (String chosenBucketID : chosenBucketIDs) {
                if (!collectedBucketIDs.contains(chosenBucketID)) {
                    addedBucketIDs.add(chosenBucketID);
                }
            }

            // 拿到界面上被选中的bucket id, 然后和之前的bucket id对比，少了就减去
            for (String collectedBucketID : collectedBucketIDs) {
                if (!chosenBucketIDs.contains(collectedBucketID)) {
                    removedBucketIDs.add(collectedBucketID);
                }
            }

            AsyncTaskCompat.executeParallel(new UpdateBucketTask(addedBucketIDs, removedBucketIDs));
        }
    }

    private class LoadCollectedBucketIDsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                // all the buckets which a certain shot has been put into
                List<Bucket> shotBuckets = Dribbble.getShotBuckets(shot.id);

                // all the buckets for the logged in user
                List<Bucket> userBuckets = Dribbble.getUserBuckets();

                Set<String> userBucketIDs = new HashSet<>();
                for (Bucket userBucket : userBuckets) {
                    userBucketIDs.add(userBucket.id);
                }

                List<String> collectedBucketIDs = new ArrayList<>();
                for (Bucket shotBucket : shotBuckets) {
                    if (userBucketIDs.contains(shotBucket.id)) {
                        collectedBucketIDs.add(shotBucket.id);
                    }
                }
                return collectedBucketIDs;
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> collectedBucketIDs) {
            adapter.updateCollectedBucketIDs(collectedBucketIDs);
        }
    }

    private class UpdateBucketTask extends AsyncTask<Void, Void, Void> {

        private List<String> added;
        private List<String> removed;
        private Exception e;

        private UpdateBucketTask(@NonNull List<String> added, @NonNull List<String> removed) {
            this.added = added;
            this.removed = removed;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                for (String addedID : added) {
                    Dribbble.addBucketShot(addedID, shot.id);
                }

                for (String removedID : removed) {
                    Dribbble.removeBucketShot(removedID, shot.id);
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                this.e = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (e == null) {
                adapter.updateCollectedBucketIDs(added, removed);
            } else {
                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
