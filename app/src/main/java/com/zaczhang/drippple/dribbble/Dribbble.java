package com.zaczhang.drippple.dribbble;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.zaczhang.drippple.model.Bucket;
import com.zaczhang.drippple.model.Shot;
import com.zaczhang.drippple.model.User;
import com.zaczhang.drippple.utils.ModelUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Dribbble {

    private static final String TAG = "Dribbble API";

    // Dribbble loads everything in a 12-per-page manner
    public static final int COUNT_PER_PAGE = 12;

    private static final String API_URL = "https://api.dribbble.com/v1/";
    private static final String USER_END_POINT = API_URL + "user";
    private static final String SHOT_END_POINT = API_URL + "shots";
    private static final String BUCKETS_END_POINT = API_URL + "buckets";

    private static final String SP_AUTH = "auth";

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER = "user";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_SHOT_ID = "shot_id";

    private static final TypeToken<User> USER_TYPE = new TypeToken<User>() {};
    private static final TypeToken<Bucket> BUCKET_TYPE = new TypeToken<Bucket>() {};
    private static final TypeToken<List<Shot>> SHOT_LIST_TYPE = new TypeToken<List<Shot>>() {};
    private static final TypeToken<List<Bucket>> BUCKET_LIST_TYPE = new TypeToken<List<Bucket>>() {};

    private static OkHttpClient client = new OkHttpClient();
    private static String accessToken;
    private static User user;


    private static Request.Builder authRequestBuilder(String url) {
        return new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(url);
    }

    private static Response makeRequest(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        Log.d(TAG, response.header("X-RateLimit-Remaining"));
        return response;
    }

    private static Response makeGetRequest(String url) throws IOException {
        Request request = authRequestBuilder(url).build();
        return makeRequest(request);
    }

    private static Response makePostRequest(String url, RequestBody requestBody) throws IOException {
        Request request = authRequestBuilder(url)
                .post(requestBody)
                .build();
        return makeRequest(request);
    }

    private static Response makePutRequest(String url, RequestBody requestBody) throws IOException {
        Request request = authRequestBuilder(url)
                .put(requestBody)
                .build();
        return makeRequest(request);
    }

    private static Response makeDeleteRequest(String url, RequestBody requestBody) throws IOException {
        Request request = authRequestBuilder(url)
                .delete(requestBody)
                .build();
        return makeRequest(request);
    }

    private static <T> T parseResponse(Response response, TypeToken<T> typeToken)
            throws IOException, JsonSyntaxException {
        String responseString = response.body().string();
        Log.d(TAG, responseString);
        return ModelUtils.toObject(responseString, typeToken);
    }

    private static void checkStatusCode(Response response, int statusCode) throws IOException {
        if (response.code() != statusCode) {
            throw new IOException(response.message());
        }
    }

    // end of basic http methods

    // 读取之前已经保存的accessToken,能读出来则说明已经登录过了，否则读取新用户
    public static void init(@NonNull Context context) {
        accessToken = loadAccessToken(context);
        if (accessToken != null) {
            user = loadUser(context);
        }
    }

    // accessToken不为空说明已经登录过了
    public static boolean isLoggedIn() {
        return accessToken != null;
    }

    public static void login(@NonNull Context context, @NonNull String accessToken)
            throws IOException, JsonSyntaxException{
        Dribbble.accessToken = accessToken;
        storeAccessToken(context, accessToken);

        Dribbble.user = getUser();
        storeUser(context, user);
    }

    public static void logout(@NonNull Context context) {
        storeAccessToken(context, null);
        storeUser(context, null);

        accessToken = null;
        user = null;
    }



    public static User getCurrentUser() {
        return user;
    }

    public static User getUser() throws IOException, JsonSyntaxException {
        return parseResponse(makeGetRequest(USER_END_POINT), USER_TYPE);
    }

    public static void storeUser(@NonNull Context context, @NonNull User user) {
        ModelUtils.save(context, KEY_USER, user);
    }

    public static User loadUser(@NonNull Context context) {
        return ModelUtils.read(context, KEY_USER, new TypeToken<User>(){});
    }

    public static void storeAccessToken(@NonNull Context context, @NonNull String token) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SP_AUTH, context.MODE_PRIVATE);
        sp.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public static String loadAccessToken(@NonNull Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SP_AUTH, context.MODE_PRIVATE);
        return sp.getString(KEY_ACCESS_TOKEN, null);
    }

    public static List<Shot> getShots(int page) throws IOException, JsonSyntaxException {
        String url = SHOT_END_POINT + "?page=" + page;
        return parseResponse(makeGetRequest(url), SHOT_LIST_TYPE);
    }

    /**
     * @return all the buckets for the logged in user
     * @throws IOException
     * @throws JsonSyntaxException
     */
    public static List<Bucket> getUserBuckets() throws IOException, JsonSyntaxException {
        String url = USER_END_POINT + "/" + "buckets?per_page=" + Integer.MAX_VALUE;
        return parseResponse(makeGetRequest(url), BUCKET_LIST_TYPE);
    }

    public static List<Bucket> getUserBuckets(int page) throws IOException, JsonSyntaxException {
        String url = USER_END_POINT + "/" + "buckets?page=" + page;
        return parseResponse(makeGetRequest(url), BUCKET_LIST_TYPE);
    }

    /**
     * @param shotID
     * @return all the buckets which a certain shot has been put into
     * @throws IOException
     * @throws JsonSyntaxException
     */
    public static List<Bucket> getShotBuckets(@NonNull String shotID) throws IOException, JsonSyntaxException {
        String url = SHOT_END_POINT + "/" + shotID + "/buckets?per_page=" + Integer.MAX_VALUE;
        return parseResponse(makeGetRequest(url), BUCKET_LIST_TYPE);
    }

    public static Bucket newBucket(@NonNull String name, @NonNull String description)
        throws IOException, JsonSyntaxException {
        FormBody formBody = new FormBody.Builder()
                .add(KEY_NAME, name)
                .add(KEY_DESCRIPTION, description)
                .build();

        return parseResponse(makePostRequest(BUCKETS_END_POINT, formBody), BUCKET_TYPE);
    }


    /**
     * Add a shot to a bucket
     * @param bucketID
     * @param shotID
     * @throws IOException
     * @throws JsonSyntaxException
     */
    public static void addBucketShot(@NonNull String bucketID,
                                     @NonNull String shotID) throws IOException, JsonSyntaxException {
        String url = BUCKETS_END_POINT + "/" + bucketID + "/shots";
        FormBody formBody = new FormBody.Builder()
                .add(KEY_SHOT_ID, shotID)
                .build();

        Response response = makePutRequest(url, formBody);
        checkStatusCode(response, HttpURLConnection.HTTP_NO_CONTENT);
    }

    public static void removeBucketShot(@NonNull String bucketID,
                                        @NonNull String shotID) throws IOException, JsonSyntaxException {
        String url = BUCKETS_END_POINT + "/" + bucketID + "/shots";
        FormBody formBody = new FormBody.Builder()
                .add(KEY_SHOT_ID, shotID)
                .build();

        Response response = makeDeleteRequest(url, formBody);
        checkStatusCode(response, HttpURLConnection.HTTP_NO_CONTENT);
    }
}
