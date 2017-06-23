package com.zaczhang.drippple.view.bucket_list;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.zaczhang.drippple.view.base.SingleFragmentActivity;

import java.util.ArrayList;


public class BucketListActivity extends SingleFragmentActivity {

    @NonNull
    @Override
    protected Fragment newFragment() {

        boolean isChoosingMode = getIntent().getExtras().getBoolean(BucketListFragment.KEY_CHOOSING_MODE);

        // 通过Activity把数据传给BucketListFragment
        // 获得原本已经被选择的buckets，并将其传给ShotListFragment
        ArrayList<String> chosenBucketIDs = getIntent().getExtras().getStringArrayList(
                BucketListFragment.KEY_COLLECTED_BUCKET_IDS);

        return BucketListFragment.newInstance(null, isChoosingMode, chosenBucketIDs);
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return "Choose bucket";
    }
}
