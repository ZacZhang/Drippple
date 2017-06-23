package com.zaczhang.drippple.dribbble.auth;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.zaczhang.drippple.R;

import butterknife.BindView;
import butterknife.ButterKnife;

// 获取临时令牌
public class AuthActivity extends AppCompatActivity {

    public static final String KEY_URL = "url";

    public static final String KEY_CODE = "code";

    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.webview) WebView webView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(getString(R.string.auth_activity_title));

        progressBar.setMax(100);

        webView.setWebViewClient(new WebViewClient() {


            // 当werView要加载新的url时，这个函数就会被调用
            // 捕捉到这个跳转的事件
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                // 当整个url以redirect_uri开头时，获取后部分的临时令牌(code)
                String url = request.getUrl().toString();
                if (url.startsWith(Auth.REDIRECT_URI)) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent();
                    intent.putExtra(KEY_CODE, uri.getQueryParameter(KEY_CODE));
                    setResult(RESULT_OK, intent);
                    finish();
                }

                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                // 刚开始加载时，进度条为0
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 加载完成后，进度条消失
                progressBar.setVisibility(View.GONE);
            }
        });

        // 更新进度条
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });

        String url = getIntent().getStringExtra(KEY_URL);
        // 加载网页
        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
