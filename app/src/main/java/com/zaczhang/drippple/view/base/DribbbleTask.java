package com.zaczhang.drippple.view.base;


import android.os.AsyncTask;

import com.zaczhang.drippple.dribbble.DribbbleException;

public abstract class DribbbleTask<Params, Progress, Result>
        extends AsyncTask<Params, Progress, Result> {

    private DribbbleException exception;

    // 非UI线程
    protected abstract Result doJob(Params... params) throws DribbbleException;

    // UI线程
    protected abstract void onSuccess(Result result);

    protected abstract void onFailed(DribbbleException e);

    @Override
    protected Result doInBackground(Params... params) {
        try {
            return doJob(params);
        } catch (DribbbleException e) {
            e.printStackTrace();
            exception =  e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (exception != null) {
            onFailed(exception);
        } else {
            onSuccess(result);
        }
    }
}
