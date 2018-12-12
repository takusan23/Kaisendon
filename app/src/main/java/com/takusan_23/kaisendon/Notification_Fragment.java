package com.takusan_23.kaisendon;

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
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Notifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class Notification_Fragment extends Fragment {

    //通知
    String account = null;
    String type = null;
    String toot = null;
    String time = null;
    String avater_url = null;
    String user_id = null;
    String user_acct = null;
    String max_id = null;

    String layout_type = null;

    long toot_id;

    long account_id;

    private ProgressDialog dialog;

    View view;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.activity_notifications, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());


        final android.os.Handler handler_1 = new android.os.Handler();


        String AccessToken = null;

        //インスタンス
        String Instance = null;

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        getActivity().setTitle(R.string.notifications);


        //背景
        ImageView background_imageView = view.findViewById(R.id.notification_background_imageview);

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

        boolean friends_nico_check_box = pref_setting.getBoolean("pref_friends_nico_mode", false);

        //くるくる
/*
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("通知を取得中 \r\n /api/v1/notifications");
        dialog.show();
*/
        //くるくる
        //ProgressDialog API 26から非推奨に

        Snackbar snackbar = Snackbar.make(view, "通知を取得中 \r\n /api/v1/notifications", Snackbar.LENGTH_INDEFINITE);
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

        String finalAccessToken = AccessToken;

        String finalInstance = Instance;

        //非同期通信
        //通知を取得
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();

                Notifications notifications = new Notifications(client);


                try {

                    Pageable<Notification> statuses = notifications.getNotifications(new Range(null, null, 30), null).execute();

                    statuses.getPart().forEach(status -> {

                        account = status.getAccount().getDisplayName();
                        type = status.getType();
                        //time = status.getCreatedAt();
                        avater_url = status.getAccount().getAvatar();
                        user_id = status.getAccount().getUserName();
                        user_acct = status.getAccount().getAcct();

                        account_id = status.getAccount().getId();

                        String toot_id_string = null;

                        //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                        try {
                            toot = status.getStatus().getContent();
                            toot_id = status.getStatus().getId();
                            toot_id_string = String.valueOf(toot_id);
                        } catch (NullPointerException e) {
                            toot = "";
                            toot_id = 0;
                            toot_id_string = String.valueOf(toot_id);
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

                        //カスタム絵文字
                        try {
                            //本文
                            List<Emoji> emoji_List = status.getStatus().getEmojis();
                            emoji_List.forEach(emoji -> {
                                String emoji_name = emoji.getShortcode();
                                String emoji_url = emoji.getUrl();
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                toot = toot.replace(":" + emoji_name + ":", custom_emoji_src);
                            });

                        } catch (NullPointerException e) {
                            toot = "";
                            toot_id = 0;
                            toot_id_string = String.valueOf(toot_id);
                        }

                        //DisplayNameのほう
                        List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                        account_emoji_List.forEach(emoji -> {
                            String emoji_name = emoji.getShortcode();
                            String emoji_url = emoji.getUrl();
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            account = account.replace(":" + emoji_name + ":", custom_emoji_src);
                        });


                        Locale locale = Locale.getDefault();
                        boolean jp = locale.equals(Locale.JAPAN);

                        if (type.equals("mention")) {
                            if (jp){
                                type = "返信しました";
                            }
                            layout_type = "Notification_mention";
                        }
                        if (type.equals("reblog")) {
                            if (jp){
                                type = "ブーストしました";
                            }
                            layout_type = "Notification_reblog";
                        }
                        if (type.equals("favourite")) {
                            if (jp) {
                                if (friends_nico_check_box) {
                                    type = "お気に入りしました";
                                } else {
                                    type = "二コりました";
                                }
                                layout_type = "Notification_favourite";
                            }
                        }
                        if (type.equals("follow")) {
                            if (jp){
                                type = "フォローしました";
                            }
                            layout_type = "Notification_follow";
                        }

                        String[] mediaURL = {null, null, null, null};
                        //めでぃあ
                        //配列に入れる形で
                        try {
                            final int[] i = {0};
                            List<Attachment> list = status.getStatus().getMediaAttachments();
                            list.forEach(media -> {
                                mediaURL[i[0]] = media.getUrl();
                                i[0]++;
                            });
                            //配列から文字列に
                            media_url_1 = mediaURL[0];
                            media_url_2 = mediaURL[1];
                            media_url_3 = mediaURL[2];
                            media_url_4 = mediaURL[3];
                        } catch (NullPointerException e) {
                            //配列から文字列に
                            media_url_1 = null;
                            media_url_2 = null;
                            media_url_3 = null;
                            media_url_4 = null;
                        }


                        ListItem listItem = new ListItem(layout_type, toot, account + " @" + user_acct + " / " + type, "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + time, toot_id_string, avater_url, account_id, user_id, media_url_1, media_url_2, media_url_3, media_url_4);

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

                                    ListView listView = (ListView) view.findViewById(R.id.notifications_list);

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

            protected void onPostExecute(String result) {

                //くるくるを終了
                //dialog.dismiss();
                snackbar.dismiss();

                HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //引っ張って更新するやつ
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_notification);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                adapter.clear();
                snackbar.show();
                AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... string) {

                        MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                                .accessToken(finalAccessToken)
                                .useStreamingApi()
                                .build();

                        Notifications notifications = new Notifications(client);

                        notifications.getNotifications(new Range(null, null, 30));

                        try {

                            Pageable<Notification> statuses = notifications.getNotifications(new Range(null, null, 30), null).execute();

                            statuses.getPart().forEach(status -> {

                                account = status.getAccount().getDisplayName();
                                type = status.getType();
                                time = status.getCreatedAt();
                                avater_url = status.getAccount().getAvatar();
                                user_id = status.getAccount().getUserName();
                                user_acct = status.getAccount().getAcct();

                                account_id = status.getAccount().getId();

                                String toot_id_string = null;

                                //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                                try {
                                    toot = status.getStatus().getContent();
                                    toot_id = status.getStatus().getId();
                                    toot_id_string = String.valueOf(toot_id);
                                } catch (NullPointerException e) {
                                    toot = "";
                                    toot_id = 0;
                                    toot_id_string = String.valueOf(toot_id);
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


                                //カスタム絵文字
                                try {
                                    List<Emoji> emoji_List = status.getStatus().getEmojis();
                                    emoji_List.forEach(emoji -> {
                                        String emoji_name = emoji.getShortcode();
                                        String emoji_url = emoji.getUrl();
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot = toot.replace(":" + emoji_name + ":", custom_emoji_src);
                                    });
                                } catch (NullPointerException e) {
                                    toot = "";
                                    toot_id = 0;
                                    toot_id_string = String.valueOf(toot_id);
                                }

                                //DisplayNameのほう
                                List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                                account_emoji_List.forEach(emoji -> {
                                    String emoji_name = emoji.getShortcode();
                                    String emoji_url = emoji.getUrl();
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    account = account.replace(":" + emoji_name + ":", custom_emoji_src);
                                });

                                Locale locale = Locale.getDefault();
                                boolean jp = locale.equals(Locale.JAPAN);

                                if (type.equals("mention")) {
                                    if (jp){
                                        type = "返信しました";
                                    }
                                    layout_type = "Notification_mention";
                                }
                                if (type.equals("reblog")) {
                                    if (jp){
                                        type = "ブーストしました";
                                    }
                                    layout_type = "Notification_reblog";
                                }
                                if (type.equals("favourite")) {
                                    if (jp) {
                                        if (friends_nico_check_box) {
                                            type = "お気に入りしました";
                                        } else {
                                            type = "二コりました";
                                        }
                                        layout_type = "Notification_favourite";
                                    }
                                }
                                if (type.equals("follow")) {
                                    if (jp){
                                        type = "フォローしました";
                                    }
                                    layout_type = "Notification_follow";
                                }

                                String[] mediaURL = {null, null, null, null};
                                //めでぃあ
                                //配列に入れる形で
                                try {
                                    final int[] i = {0};
                                    List<Attachment> list = status.getStatus().getMediaAttachments();
                                    list.forEach(media -> {
                                        mediaURL[i[0]] = media.getUrl();
                                        i[0]++;
                                    });
                                    //配列から文字列に
                                    media_url_1 = mediaURL[0];
                                    media_url_2 = mediaURL[1];
                                    media_url_3 = mediaURL[2];
                                    media_url_4 = mediaURL[3];
                                } catch (NullPointerException e) {
                                    //配列から文字列に
                                    media_url_1 = null;
                                    media_url_2 = null;
                                    media_url_3 = null;
                                    media_url_4 = null;
                                }


                                ListItem listItem = new ListItem(layout_type, toot, account + " @" + user_acct + " / " + type, "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + time, toot_id_string, avater_url, account_id, user_id, media_url_1, media_url_2, media_url_3, media_url_4);

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

                                        ListView listView = (ListView) view.findViewById(R.id.notifications_list);

                                        listView.setAdapter(adapter);
                                    }


                                });

                            });

                        } catch (Mastodon4jRequestException e) {

                            e.printStackTrace();

                        }


                        return null;
                    }

                    protected void onPostExecute(String result) {
                        snackbar.dismiss();
                    }

                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

        });


        ListView listView = view.findViewById(R.id.notifications_list);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount != 0 && totalItemCount == firstVisibleItem + visibleItemCount) {
                    Snackbar snackbar_ = Snackbar.make(view, "追加読み込み準備中", Snackbar.LENGTH_LONG);
                    snackbar_.show();
                    if (snackbar_.isShown()) {
                        System.out.println("最後だよ");

                        //scrollPosition = scrollPosition + 40;


                        //最後のトゥートIDを持ってくる
                        //もういい！okhttpで実装する！！
                        String url = "https://" + finalInstance + "/api/v1/notifications/?access_token=" + finalAccessToken;
                        //パラメータを設定
                        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
                        builder.addQueryParameter("limit", "30");
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
                                    JSONObject last_toot_jsonObject = jsonArray.getJSONObject(29);
                                    max_id = last_toot_jsonObject.getString("id");
//                                    System.out.println("最後" + max_id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        if (max_id != null) {

                            //SnackBer表示
                            Snackbar maxid_snackbar = Snackbar.make(view, "ホームを取得中 \r\n /api/v1/notifications \r\n max_id=" + max_id, Snackbar.LENGTH_INDEFINITE);
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
                            String max_id_url = "https://" + finalInstance + "/api/v1/notifications/?access_token=" + finalAccessToken;
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
                                            JSONObject toot_status = toot_jsonObject.getJSONObject("status");
                                            toot = toot_status.getString("content");
                                            user_id = toot_account.getString("username");
                                            account = toot_account.getString("display_name");
                                            time = toot_jsonObject.getString("created_at");
                                            type = toot_jsonObject.getString("type");
                                            String toot_id_string = null;
                                            toot_id_string = toot_status.getString("id");

                                            avater_url = toot_account.getString("avatar");

                                            account_id = toot_account.getLong("id");

                                            List<Attachment> attachment = Collections.singletonList(new Attachment());


                                            final String[] medias = new String[1];

                                            final String[] media_url = {null};

                                            Locale locale = Locale.getDefault();
                                            boolean jp = locale.equals(Locale.JAPAN);

                                            if (type.equals("mention")) {
                                                if (jp){
                                                    type = "返信しました";
                                                }
                                                layout_type = "Notification_mention";
                                            }
                                            if (type.equals("reblog")) {
                                                if (jp){
                                                    type = "ブーストしました";
                                                }
                                                layout_type = "Notification_reblog";
                                            }
                                            if (type.equals("favourite")) {
                                                if (jp) {
                                                    if (friends_nico_check_box) {
                                                        type = "お気に入りしました";
                                                    } else {
                                                        type = "二コりました";
                                                    }
                                                    layout_type = "Notification_favourite";
                                                }
                                            }
                                            if (type.equals("follow")) {
                                                if (jp){
                                                    type = "フォローしました";
                                                }
                                                layout_type = "Notification_follow";
                                            }


                                            JSONArray media_array = toot_status.getJSONArray("media_attachments");
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
                                            JSONArray emoji = toot_status.getJSONArray("emojis");
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
                                                    account = account.replace(":" + emoji_name + ":", custom_emoji_src);
                                                }

                                                //ユーザーネームの方のアバター絵文字
                                                JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                                                for (int a = 0; a < account_avater_emoji.length(); a++) {
                                                    JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                                                    String emoji_name = jsonObject.getString("shortcode");
                                                    String emoji_url = jsonObject.getString("url");
                                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                    account = account.replace(":" + emoji_name + ":", custom_emoji_src);
                                                }
                                            }catch (JSONException e){

                                            }

                                            //ユーザーネームの方の絵文字
                                            JSONArray account_emoji = toot_account.getJSONArray("emojis");
                                            for (int e = 0; e < account_emoji.length(); e++) {
                                                JSONObject jsonObject = account_emoji.getJSONObject(e);
                                                String emoji_name = jsonObject.getString("shortcode");
                                                String emoji_url = jsonObject.getString("url");
                                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                account = account.replace(":" + emoji_name + ":", custom_emoji_src);
                                            }



                                            ListItem listItem = new ListItem(layout_type, toot, account + " @" + user_acct + " / " + type, "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + time, toot_id_string, avater_url, account_id, user_id, media_url_1, media_url_2, media_url_3, media_url_4);


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
                                                    listView.setAdapter(adapter);
                                                    maxid_snackbar.dismiss();
                                                    //listView.setSelection(scrollPosition);
                                                }
                                            });
                                            media_url_1 = null;
                                            media_url_2 = null;
                                            media_url_3 = null;
                                            media_url_4 = null;
                                            layout_type = null;

                                        }
                                        //最後のIDを更新する
                                        JSONObject last_toot = jsonArray.getJSONObject(29);
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
