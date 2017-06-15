package com.zaczhang.drippple.view.bucket_list;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.zaczhang.drippple.view.base.SingleFragmentActivity;
import com.zaczhang.drippple.view.shot_list.ShotListFragment;

public class BucketShotListActivity extends SingleFragmentActivity {

    public static final String KEY_BUCKET_NAME = "bucketName";

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getIntent().getStringExtra(KEY_BUCKET_NAME);
    }

    @NonNull
    @Override
    protected Fragment newFragment() {
        String bucketID = getIntent().getStringExtra(ShotListFragment.KEY_BUCKET_ID);
        if (bucketID == null) {
            return ShotListFragment.newInstance(ShotListFragment.LIST_TYPE_POPULAR);
        } else {
            return ShotListFragment.newBucketListInstance(bucketID);
        }
    }
}
