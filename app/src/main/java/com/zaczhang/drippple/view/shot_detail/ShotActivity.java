package com.zaczhang.drippple.view.shot_detail;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.zaczhang.drippple.view.base.SingleFragmentActivity;


public class ShotActivity extends SingleFragmentActivity {

    public static final String KEY_SHOT_TITLE = "shot_title";

    @NonNull
    @Override
    protected Fragment newFragment() {
        // 把Intent里面的所有数据取出来，也就是一个bundle，然后传给ShotFragment
        return ShotFragment.newInstance(getIntent().getExtras());
    }

    @NonNull
    @Override
    protected String getActivityTitle() {
        return getIntent().getStringExtra(KEY_SHOT_TITLE);
    }
}
