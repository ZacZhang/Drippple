package com.zaczhang.drippple.dribbble.auth;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Auth {

    public static final int REQ_CODE = 100;

    public static final String KEY_CODE = "code";

    private static final String KEY_CLIENT_ID = "client_id";

    private static final String KEY_CLIENT_SECRET = "client_secret";

    private static final String KEY_REDIRECT_URI = "redirect_uri";

    private static final String KEY_SCOPE = "scope";

    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static final String CLIENT_ID = "26ce1ced9560cfdfccebde37d3c43bf170470f1048b5adae7a26bdfaa9d4ae8e";

    private static final String CLIENT_SECRET = "083cc64752f230b7c9b76b9e3008b60d1c9e4068fdfeb89e741e478764562f95";

    private static final String SCOPE = "public+write";

    private static final String URI_AUTHORIZE = "https://dribbble.com/oauth/authorize";

    private static final String URI_TOKEN = "https://dribbble.com/oauth/token";

    public static final String REDIRECT_URI = "http://www.drippple.com";


    private static String getAuthorizeUrl() {
        String url = Uri.parse(URI_AUTHORIZE)
                .buildUpon()
                .appendQueryParameter(KEY_CLIENT_ID, CLIENT_ID)
                .build()
                .toString();

        // fix encode issue
        url = url + "&" + KEY_REDIRECT_URI + "=" + REDIRECT_URI;
        url = url + "&" + KEY_SCOPE + "=" + SCOPE;

        return url;
    }

    private static String getTokenUrl(String authCode) {
        return Uri.parse(URI_TOKEN)
                .buildUpon()
                .appendQueryParameter(KEY_CLIENT_ID, CLIENT_ID)
                .appendQueryParameter(KEY_CLIENT_SECRET, CLIENT_SECRET)
                .appendQueryParameter(KEY_CODE, authCode)
                .appendQueryParameter(KEY_REDIRECT_URI, REDIRECT_URI)
                .build()
                .toString();
    }

    public static void openAuthActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, AuthActivity.class);
        intent.putExtra(AuthActivity.KEY_URL, getAuthorizeUrl());

        activity.startActivityForResult(intent, REQ_CODE);
    }

    public static String fetchAccessToken(String authCode) throws IOException{
        OkHttpClient client = new OkHttpClient();
        RequestBody postBody = new FormBody.Builder()
                .add(KEY_CLIENT_ID, CLIENT_ID)
                .add(KEY_CLIENT_SECRET, CLIENT_SECRET)
                .add(KEY_CODE, authCode)
                .add(KEY_REDIRECT_URI, REDIRECT_URI)
                .build();

        Request request = new Request.Builder()
                .url(URI_TOKEN)
                .post(postBody)
                .build();

        Response response = client.newCall(request).execute();

        String responseString = response.body().string();

        try {
            // 先转成JSON，再取出key为access_token的最终令牌
            JSONObject object = new JSONObject(responseString);
            return object.getString(KEY_ACCESS_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
