package io.github.takusan23.kaisendon;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Emoji;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Favourites;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Favourites_List_Fragment extends Fragment {
    String display_name = null;
    String account_id_string = null;
    String avater_url = null;
    String nicoru_favourite = null;
    String toot = null;
    String time = null;
    String user_name = null;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;

    String max_id = null;

    long account_id;

    private ProgressDialog dialog;

    int position;
    int y;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.activity_home_timeline, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        String AccessToken = null;
        String Instance = null;

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        //ニコるかお気に入りか
        boolean nicoru_favourite_check = pref_setting.getBoolean("pref_friends_nico_mode", false);
        if (nicoru_favourite_check) {
            nicoru_favourite = getString(R.string.dialog_favorite);
            getActivity().setTitle(getString(R.string.favourite_list));
        } else {
            nicoru_favourite = "ニコった";
            getActivity().setTitle("ニコったリスト");
        }


        //背景
        ImageView background_imageView = view.findViewById(R.id.hometimeline_background_imageview);

        if (pref_setting.getBoolean("background_image", true)) {
            Uri uri = Uri.parse(pref_setting.getString("background_image_path", ""));
            Glide.with(getContext())
                    .load(uri)
                    .into(background_imageView);
        }

        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f) != 0.1) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        //くるくる
/*
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(nicoru_favourite + "リストを取得中 \r\n /api/v1/favourites");
        dialog.show();
*/

        //くるくる
        //ProgressDialog API 26から非推奨に
        Snackbar snackbar = Snackbar.make(view, nicoru_favourite + "リストを取得中 \r\n /api/v1/favourites", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(getContext());
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar.show();


        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);


        //非同期通信
        String finalInstance = Instance;
        String finalAccessToken = AccessToken;
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {
                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken).useStreamingApi().build();

                try {
                    Pageable<com.sys1yagi.mastodon4j.api.entity.Status> favourites = new Favourites(client).getFavourites(new Range(null, null, 40)).execute();

                    favourites.getPart().forEach(status -> {

                        user_name = status.getAccount().getDisplayName();
                        String user_id = status.getAccount().getUserName();
                        toot = status.getContent();
                        long toot_id = status.getId();
                        long account_id = status.getAccount().getId();
                        String toot_id_string = String.valueOf(toot_id);
                        time = status.getCreatedAt();
                        String user_avater_url = status.getAccount().getAvatar();

                        boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                        if (japan_timeSetting) {
                            //時差計算？
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                            //日本用フォーマット
                            SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                            try {
                                Date date = simpleDateFormat.parse(status.getCreatedAt());
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                //9時間足して日本時間へ
                                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                time = japanDateFormat.format(calendar.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            time = status.getCreatedAt();
                        }


                        //カスタム絵文字
                        if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                            List<Emoji> emoji_List = status.getEmojis();
                            emoji_List.forEach(emoji -> {
                                String emoji_name = emoji.getShortcode();
                                String emoji_url = emoji.getUrl();
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                toot = toot.replace(":" + emoji_name + ":", custom_emoji_src);
                            });

                            //DisplayNameカスタム絵文字
                            List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                            account_emoji_List.forEach(emoji -> {
                                String emoji_name = emoji.getShortcode();
                                String emoji_url = emoji.getUrl();
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                            });
                        }


                        ListItem listItem = new ListItem(null, toot, user_name + " @" + user_id, "トゥートID : " + toot_id_string + " / " + time, toot_id_string, user_avater_url, account_id, user_id, null, null, null, null);

                        if (getActivity() == null)
                            return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.add(listItem);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        //UI変更
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() != null) {

                                    ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                    listView.setAdapter(adapter);
                                }
                            }
                        });
                    });

                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                //くるくるを終了
                //dialog.dismiss();
                snackbar.dismiss();
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //引っ張って更新するやつ
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                dialog.show();
                toot_list.clear();
                AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... string) {
                        MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken).useStreamingApi().build();

                        try {
                            Pageable<com.sys1yagi.mastodon4j.api.entity.Status> favourites = new Favourites(client).getFavourites(new Range(null, null, 40)).execute();

                            favourites.getPart().forEach(status -> {

                                user_name = status.getAccount().getDisplayName();
                                String user_id = status.getAccount().getUserName();
                                toot = status.getContent();
                                long toot_id = status.getId();
                                long account_id = status.getAccount().getId();
                                String toot_id_string = String.valueOf(toot_id);
                                time = status.getCreatedAt();
                                String user_avater_url = status.getAccount().getAvatar();

                                //カスタム絵文字
                                if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                    List<Emoji> emoji_List = status.getEmojis();
                                    emoji_List.forEach(emoji -> {
                                        String emoji_name = emoji.getShortcode();
                                        String emoji_url = emoji.getUrl();
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot = toot.replace(":" + emoji_name + ":", custom_emoji_src);
                                    });

                                    //DisplayNameカスタム絵文字
                                    List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                                    account_emoji_List.forEach(emoji -> {
                                        String emoji_name = emoji.getShortcode();
                                        String emoji_url = emoji.getUrl();
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                    });
                                }


                                boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                                if (japan_timeSetting) {
                                    //時差計算？
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                    //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                    //日本用フォーマット
                                    SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                    try {
                                        Date date = simpleDateFormat.parse(status.getCreatedAt());
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(date);
                                        //9時間足して日本時間へ
                                        calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                        //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                        time = japanDateFormat.format(calendar.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    time = status.getCreatedAt();
                                }

                                ListItem listItem = new ListItem(null, toot, user_name + " @" + user_id, "トゥートID : " + toot_id_string + " / " + time, toot_id_string, user_avater_url, account_id, user_id, null, null, null, null);

                                if (getActivity() == null)
                                    return;

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                                //UI変更
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (getActivity() != null) {

                                            ListView listView = (ListView) view.findViewById(R.id.home_timeline);

                                            listView.setAdapter(adapter);
                                        }
                                    }
                                });
                            });

                        } catch (Mastodon4jRequestException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        //くるくるを終了
                        //dialog.dismiss();
                        snackbar.dismiss();
                    }

                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });


        ListView listView = (ListView) view.findViewById(R.id.home_timeline);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount != 0 && totalItemCount == firstVisibleItem + visibleItemCount) {
                    position = listView.getFirstVisiblePosition();
                    y = listView.getChildAt(0).getTop();

                    Snackbar snackbar_ = Snackbar.make(view, R.string.add_loading, Snackbar.LENGTH_LONG);
                    snackbar_.show();
                    if (snackbar_.isShown()) {
                        System.out.println("最後だよ");

                        //scrollPosition = scrollPosition + 40;


                        //最後のトゥートIDを持ってくる
                        //もういい！okhttpで実装する！！
                        String url = "https://" + finalInstance + "/api/v1/timelines/home/?access_token=" + finalAccessToken;
                        //パラメータを設定
                        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
                        builder.addQueryParameter("limit", "40");
                        String final_url = builder.build().toString();

                        //作成
                        Request request = new Request.Builder()
                                .url(final_url)
                                .get()
                                .build();

                        //GETリクエスト
                        OkHttpClient client_1 = new OkHttpClient();
                        client_1.newCall(request).enqueue(new Callback() {

                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String response_string = response.body().string();
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = new JSONArray(response_string);
                                    JSONObject last_toot_jsonObject = jsonArray.getJSONObject(39);
                                    max_id = last_toot_jsonObject.getString("id");
//                                    System.out.println("最後" + max_id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        if (max_id != null) {

                            //SnackBer表示
                            Snackbar maxid_snackbar = Snackbar.make(view, getString(R.string.loading_home) + "\r\n /api/v1/timelines/home \r\n max_id=" + max_id, Snackbar.LENGTH_INDEFINITE);
                            ViewGroup snackBer_viewGrop = (ViewGroup) maxid_snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                            //SnackBerを複数行対応させる
                            TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
                            snackBer_textView.setMaxLines(3);
                            //複数行対応させたおかげでずれたので修正
                            ProgressBar progressBar = new ProgressBar(getContext());
                            LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            progressBer_layoutParams.gravity = Gravity.CENTER;
                            progressBar.setLayoutParams(progressBer_layoutParams);
                            snackBer_viewGrop.addView(progressBar, 0);
                            maxid_snackbar.show();


                            //最後のトゥートIDを持ってくる
                            //もういい！okhttpで実装する！！
                            String max_id_url = "https://" + finalInstance + "/api/v1/timelines/home/?access_token=" + finalAccessToken;
                            //パラメータを設定
                            HttpUrl.Builder max_id_builder = HttpUrl.parse(max_id_url).newBuilder();
                            max_id_builder.addQueryParameter("limit", "40");
                            max_id_builder.addQueryParameter("max_id", max_id);
                            String max_id_final_url = max_id_builder.build().toString();

                            //作成
                            Request max_id_request = new Request.Builder()
                                    .url(max_id_final_url)
                                    .get()
                                    .build();

                            //GETリクエスト
                            OkHttpClient max_id_client = new OkHttpClient();
                            max_id_client.newCall(max_id_request).enqueue(new Callback() {

                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String response_string = response.body().string();
                                    JSONArray jsonArray = null;
                                    try {
                                        jsonArray = new JSONArray(response_string);
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                                            JSONObject toot_account = toot_jsonObject.getJSONObject("account");
                                            String user_name = toot_account.getString("display_name");
                                            String user_id = toot_account.getString("username");
                                            String toot = toot_jsonObject.getString("content");
                                            String user_use_client = null;


                                            //クライアント名がある？ない？
                                            try {
                                                JSONObject application = toot_jsonObject.getJSONObject("application");
                                                user_use_client = application.getString("name");
                                            } catch (JSONException e) {
                                                user_use_client = toot_jsonObject.getString("application");
                                            }


                                            //                       user_use_client = status.getApplication().getName();
                                            //toot_id = toot_jsonObject.getString("id");
                                            String toot_id_string = toot_jsonObject.getString("id");

                                            String user_avater_url = toot_account.getString("avatar");

                                            account_id = toot_account.getInt("id");

                                            List<Attachment> attachment = Collections.singletonList(new Attachment());


                                            final String[] medias = new String[1];

                                            final String[] media_url = {null};

                                            JSONArray media_array = toot_jsonObject.getJSONArray("media_attachments");
                                            if (!media_array.isNull(0)) {
                                                media_url_1 = media_array.getJSONObject(0).getString("url");
                                            }
                                            if (!media_array.isNull(1)) {
                                                media_url_2 = media_array.getJSONObject(1).getString("url");
                                            }
                                            if (!media_array.isNull(2)) {
                                                media_url_3 = media_array.getJSONObject(2).getString("url");
                                            }
                                            if (!media_array.isNull(3)) {
                                                media_url_4 = media_array.getJSONObject(3).getString("url");
                                            }


                                            //絵文字
                                            if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                                JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                                                for (int e = 0; e < emoji.length(); e++) {
                                                    JSONObject jsonObject = emoji.getJSONObject(e);
                                                    String emoji_name = jsonObject.getString("shortcode");
                                                    String emoji_url = jsonObject.getString("url");
                                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                    toot = toot.replace(":" + emoji_name + ":", custom_emoji_src);
                                                }

                                                //アバター絵文字
                                                try {
                                                    JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                                                    for (int a = 0; a < avater_emoji.length(); a++) {
                                                        JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                                        String emoji_name = jsonObject.getString("shortcode");
                                                        String emoji_url = jsonObject.getString("url");
                                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                        toot = toot.replace(":" + emoji_name + ":", custom_emoji_src);
                                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                    }

                                                    //ユーザーネームの方のアバター絵文字
                                                    JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                                                    for (int a = 0; a < account_avater_emoji.length(); a++) {
                                                        JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                                                        String emoji_name = jsonObject.getString("shortcode");
                                                        String emoji_url = jsonObject.getString("url");
                                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                    }
                                                } catch (JSONException e) {

                                                }

                                                //ユーザーネームの方の絵文字
                                                JSONArray account_emoji = toot_account.getJSONArray("emojis");
                                                for (int e = 0; e < account_emoji.length(); e++) {
                                                    JSONObject jsonObject = account_emoji.getJSONObject(e);
                                                    String emoji_name = jsonObject.getString("shortcode");
                                                    String emoji_url = jsonObject.getString("url");
                                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                }
                                            }


                                            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                                            if (japan_timeSetting) {
                                                //時差計算？
                                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                                //日本用フォーマット
                                                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                                try {
                                                    Date date = simpleDateFormat.parse(toot_jsonObject.getString("created_at"));
                                                    Calendar calendar = Calendar.getInstance();
                                                    calendar.setTime(date);
                                                    //9時間足して日本時間へ
                                                    calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                                    //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                                    time = japanDateFormat.format(calendar.getTime());
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                time = toot_jsonObject.getString("created_at");
                                            }

                                            ListItem listItem = new ListItem(null, toot, user_name + " @" + user_id, "トゥートID : " + toot_id_string + " / " + time, toot_id_string, user_avater_url, account_id, user_id, null, null, null, null);


                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    adapter.add(listItem);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });

                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                                    listView.setAdapter(adapter);
                                                    maxid_snackbar.dismiss();
                                                    //listView.setSelection(scrollPosition);
                                                    listView.setSelectionFromTop(position, y);

                                                    //listView.setSelection(scrollPosition);
                                                }
                                            });
                                            media_url_1 = null;
                                            media_url_2 = null;
                                            media_url_3 = null;
                                            media_url_4 = null;
                                        }
                                        //最後のIDを更新する
                                        JSONObject last_toot = jsonArray.getJSONObject(39);
                                        max_id = last_toot.getString("id");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }

}
