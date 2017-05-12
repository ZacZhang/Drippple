package com.zaczhang.drippple.view.bucket_list;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.zaczhang.drippple.R;
import com.zaczhang.drippple.view.base.SingleFragmentActivity;

import java.util.ArrayList;


public class ChooseBucketActivity extends SingleFragmentActivity {

    @NonNull
    @Override
    protected Fragment newFragment() {
        ArrayList<String> chosenBucketIDs = getIntent().getStringArrayListExtra(
                BucketListFragment.KEY_CHOSEN_BUCKET_IDS);
        return BucketListFragment.newInstance(true, chosenBucketIDs);
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getString(R.string.choose_bucket);
    }
}
