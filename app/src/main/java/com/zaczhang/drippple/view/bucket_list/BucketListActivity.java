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

        ArrayList<String> chosenBucketIDs = getIntent().getExtras().getStringArrayList(
                BucketListFragment.KEY_CHOSEN_BUCKET_IDS);

        return BucketListFragment.newInstance(null, isChoosingMode, chosenBucketIDs);
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return "Choose bucket";
    }
}
