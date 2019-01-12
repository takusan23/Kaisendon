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
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Card;
import com.sys1yagi.mastodon4j.api.entity.Emoji;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Notifications;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Streaming;

import org.jetbrains.annotations.NotNull;
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
    String user_name = null;
    String type = null;
    String toot_text = null;
    String toot_text_time = null;
    String user_avater_url = null;
    String user_id = null;
    String user = null;
    String max_id = null;

    String layout_type = null;

    long toot_text_id;

    long account_id;

    private ProgressDialog dialog;

    View view;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;

    Shutdownable shutdownable;

    SharedPreferences pref_setting;

    int scrollPosition = 30;

    int position;
    int y;

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

        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }


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

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_notification);

        ListView listView = view.findViewById(R.id.notifications_list);

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


        ArrayList<ListItem> toot_text_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_text_list);

        String finalAccessToken = AccessToken;

        String finalInstance = Instance;


        //ストリーミングAPI
        if (pref_setting.getBoolean("pref_streaming_api", true)) {
            //引っ張って更新するやつ無効
            swipeRefreshLayout.setEnabled(false);

            String finalInstance1 = Instance;
            String finalAccessToken1 = AccessToken;
            new AsyncTask<String, String, String>() {

                @Override
                protected String doInBackground(String... string) {

                    MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken1)
                            .useStreamingApi()
                            .build();

                    Handler handler = new Handler() {

                        @Override
                        public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {

                        }

                        @Override
                        public void onNotification(@NotNull Notification notification) {
                            user_name = notification.getAccount().getDisplayName();
                            type = notification.getType();
                            //toot_text_time = status.getCreatedAt();
                            user_avater_url = notification.getAccount().getAvatar();
                            user_id = notification.getAccount().getUserName();
                            user = notification.getAccount().getAcct();

                            account_id = notification.getAccount().getId();

                            String toot_text_id_string = null;

                            //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                            try {
                                toot_text = notification.getStatus().getContent();
                                toot_text_id = notification.getStatus().getId();
                                toot_text_id_string = String.valueOf(toot_text_id);
                            } catch (NullPointerException e) {
                                toot_text = "";
                                toot_text_id = 0;
                                toot_text_id_string = String.valueOf(toot_text_id);
                            }

                            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                            if (japan_timeSetting) {
                                //時差計算？
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                //日本用フォーマット
                                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                try {
                                    Date date = simpleDateFormat.parse(notification.getCreatedAt());
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    //9時間足して日本時間へ
                                    calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                    //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                    toot_text_time = japanDateFormat.format(calendar.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                toot_text_time = notification.getCreatedAt();
                            }

                            //カスタム絵文字
                            if (pref_setting.getBoolean("pref_custom_emoji", false)) {

                                try {
                                    //本文
                                    List<Emoji> emoji_List = notification.getStatus().getEmojis();
                                    emoji_List.forEach(emoji -> {
                                        String emoji_name = emoji.getShortcode();
                                        String emoji_url = emoji.getUrl();
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                    });

                                } catch (NullPointerException e) {
                                    toot_text = "";
                                    toot_text_id = 0;
                                    toot_text_id_string = String.valueOf(toot_text_id);
                                }

                                //DisplayNameのほう
                                List<Emoji> account_emoji_List = notification.getAccount().getEmojis();
                                account_emoji_List.forEach(emoji -> {
                                    String emoji_name = emoji.getShortcode();
                                    String emoji_url = emoji.getUrl();
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                });
                            }

                            Locale locale = Locale.getDefault();
                            boolean jp = locale.equals(Locale.JAPAN);

                            if (type.equals("mention")) {
                                if (jp) {
                                    type = " さんが返信しました";
                                } else {
                                    type = " mentioned";
                                }
                                layout_type = "Notification_mention";
                            }
                            if (type.equals("reblog")) {
                                if (jp) {
                                    type = " さんがブーストしました";
                                } else {
                                    type = " boosted";
                                }
                                layout_type = "Notification_reblog";
                            }
                            if (type.equals("favourite")) {
                                if (jp) {
                                    if (friends_nico_check_box) {
                                        type = " さんがお気に入りしました";
                                    } else {
                                        type = " さんが二コりました";
                                    }
                                } else {
                                    type = "favourited";
                                }
                                layout_type = "Notification_favourite";
                            }

                            if (type.equals("follow")) {
                                if (jp) {
                                    type = " さんがフォローしました";
                                } else {
                                    type = " followed";
                                }
                                layout_type = "Notification_follow";
                            }

                            String[] mediaURL = {null, null, null, null};
                            //めでぃあ
                            //配列に入れる形で
                            try {
                                final int[] i = {0};
                                List<Attachment> list = notification.getStatus().getMediaAttachments();
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


                            //Card
                            ArrayList<String> card = new ArrayList<>();
                            String cardTitle = null;
                            String cardURL = null;
                            String cardDescription = null;
                            String cardImage = null;

                            try {
                                Card statuses = new Statuses(client).getCard(toot_text_id).execute();
                                if (!statuses.getUrl().isEmpty()) {
                                    cardTitle = statuses.getTitle();
                                    cardURL = statuses.getUrl();
                                    cardDescription = statuses.getDescription();
                                    cardImage = statuses.getImage();

                                    card.add(statuses.getTitle());
                                    card.add(statuses.getUrl());
                                    card.add(statuses.getDescription());
                                    card.add(statuses.getImage());
                                }
                            } catch (Mastodon4jRequestException e) {
                                e.printStackTrace();
                            }
                            //配列を作成
                            ArrayList<String> Item = new ArrayList<>();
                            //メモとか通知とかに
                            Item.add(layout_type);
                            //内容
                            Item.add(toot_text);
                            //ユーザー名
                            Item.add(user_name + " @" + user + type);
                            //時間、クライアント名等
                            Item.add("トゥートID : " + toot_text_id_string + " / " + getString(R.string.time) + " : " + toot_text_time);
                            //Toot ID 文字列版
                            Item.add(toot_text_id_string);
                            //アバターURL
                            Item.add(user_avater_url);
                            //アカウントID
                            Item.add(String.valueOf(account_id));
                            //ユーザーネーム
                            Item.add(user);
                            //メディア
                            Item.add(media_url_1);
                            Item.add(media_url_2);
                            Item.add(media_url_3);
                            Item.add(media_url_4);
                            //カード
                            Item.add(cardTitle);
                            Item.add(cardURL);
                            Item.add(cardDescription);
                            Item.add(cardImage);


                            ListItem listItem = new ListItem(Item);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.insert(listItem, 0);
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
                        }


                        @Override
                        public void onDelete(long l) {

                        }
                    };
                    Streaming streaming = new Streaming(client);
                    try {
                        shutdownable = streaming.user(handler);
                        //Thread.sleep(10000L);
                        //shutdownable.shutdown();
                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //ストリーミング前の通知取得
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
                            JSONObject toot_text_jsonObject = jsonArray.getJSONObject(i);
                            JSONObject toot_text_account = toot_text_jsonObject.getJSONObject("account");
                            user_id = toot_text_account.getString("username");
                            user_name = toot_text_account.getString("display_name");
                            toot_text_time = toot_text_jsonObject.getString("created_at");
                            type = toot_text_jsonObject.getString("type");
                            String toot_text_id_string = null;
                            toot_text = "";

                            JSONObject toot_text_status = null;
                            user_avater_url = toot_text_account.getString("avatar");

                            account_id = toot_text_account.getLong("id");

                            user = toot_text_account.getString("acct");

                            List<Attachment> attachment = Collections.singletonList(new Attachment());

                            //カード情報
                            String cardTitle = null;
                            String cardURL = null;
                            String cardDescription = null;
                            String cardImage = null;

                            if (toot_text_jsonObject.has("status")) {
                                toot_text_status = toot_text_jsonObject.getJSONObject("status");
                                toot_text = toot_text_status.getString("content");
                                toot_text_id_string = toot_text_status.getString("id");

                                JSONArray media_array = toot_text_status.getJSONArray("media_attachments");
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
                                    JSONArray emoji = toot_text_status.getJSONArray("emojis");
                                    for (int e = 0; e < emoji.length(); e++) {
                                        JSONObject jsonObject = emoji.getJSONObject(e);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                    }

                                    //アバター絵文字
                                    try {
                                        JSONArray avater_emoji = toot_text_jsonObject.getJSONArray("profile_emojis");
                                        for (int a = 0; a < avater_emoji.length(); a++) {
                                            JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                            String emoji_name = jsonObject.getString("shortcode");
                                            String emoji_url = jsonObject.getString("url");
                                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                            toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                            user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                        }

                                        //ユーザーネームの方のアバター絵文字
                                        JSONArray account_avater_emoji = toot_text_account.getJSONArray("profile_emojis");
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
                                    JSONArray account_emoji = toot_text_account.getJSONArray("emojis");
                                    for (int e = 0; e < account_emoji.length(); e++) {
                                        JSONObject jsonObject = account_emoji.getJSONObject(e);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                    }
                                }

                                //カード情報
                                if (!toot_text_status.isNull("card")) {
                                    JSONObject cardObject = toot_text_status.getJSONObject("card");
                                    cardURL = cardObject.getString("url");
                                    cardTitle = cardObject.getString("title");
                                    cardDescription = cardObject.getString("description");
                                    cardImage = cardObject.getString("image");
                                }

                            }

                            final String[] medias = new String[1];

                            final String[] media_url = {null};

                            Locale locale = Locale.getDefault();
                            boolean jp = locale.equals(Locale.JAPAN);

                            if (type.equals("mention")) {
                                if (jp) {
                                    type = " さんが返信しました";
                                } else {
                                    type = " mentioned";
                                }
                                layout_type = "Notification_mention";
                            }
                            if (type.equals("reblog")) {
                                if (jp) {
                                    type = " さんがブーストしました";
                                } else {
                                    type = " boosted";
                                }
                                layout_type = "Notification_reblog";
                            }
                            if (type.equals("favourite")) {
                                if (jp) {
                                    if (friends_nico_check_box) {
                                        type = " さんがお気に入りしました";
                                    } else {
                                        type = " さんが二コりました";
                                    }
                                } else {
                                    type = "favourited";
                                }
                                layout_type = "Notification_favourite";
                            }

                            if (type.equals("follow")) {
                                if (jp) {
                                    type = " さんがフォローしました";
                                } else {
                                    type = " followed";
                                }
                                layout_type = "Notification_follow";
                            }


                            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                            if (japan_timeSetting) {
                                //時差計算？
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                //日本用フォーマット
                                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                try {
                                    Date date = simpleDateFormat.parse(toot_text_jsonObject.getString("created_at"));
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    //9時間足して日本時間へ
                                    calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                    //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                    toot_text_time = japanDateFormat.format(calendar.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                toot_text_time = toot_text_jsonObject.getString("created_at");
                            }

                            //配列を作成
                            ArrayList<String> Item = new ArrayList<>();
                            //メモとか通知とかに
                            Item.add(layout_type);
                            //内容
                            Item.add(toot_text);
                            //ユーザー名
                            Item.add(user_name + " @" + user + type);
                            //時間、クライアント名等
                            Item.add("トゥートID : " + toot_text_id_string + " / " + getString(R.string.time) + " : " + toot_text_time);
                            //Toot ID 文字列版
                            Item.add(toot_text_id_string);
                            //アバターURL
                            Item.add(user_avater_url);
                            //アカウントID
                            Item.add(String.valueOf(account_id));
                            //ユーザーネーム
                            Item.add(user);
                            //メディア
                            Item.add(media_url_1);
                            Item.add(media_url_2);
                            Item.add(media_url_3);
                            Item.add(media_url_4);
                            //カード
                            Item.add(cardTitle);
                            Item.add(cardURL);
                            Item.add(cardDescription);
                            Item.add(cardImage);


                            if (getActivity() != null) {
                                ListItem listItem = new ListItem(Item);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                        listView.setAdapter(adapter);
                                        snackbar.dismiss();
                                        //listView.setSelection(scrollPosition);
                                    }
                                });
                            }

                            media_url_1 = null;
                            media_url_2 = null;
                            media_url_3 = null;
                            media_url_4 = null;
                            layout_type = null;

                        }
                        //最後のIDを更新する
                        JSONObject last_toot_text = jsonArray.getJSONObject(29);
                        max_id = last_toot_text.getString("id");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
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

                            user_name = status.getAccount().getDisplayName();
                            type = status.getType();
                            //toot_text_time = status.getCreatedAt();
                            user_avater_url = status.getAccount().getAvatar();
                            user_id = status.getAccount().getUserName();
                            user = status.getAccount().getAcct();

                            account_id = status.getAccount().getId();

                            String toot_text_id_string = null;

                            //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                            try {
                                toot_text = status.getStatus().getContent();
                                toot_text_id = status.getStatus().getId();
                                toot_text_id_string = String.valueOf(toot_text_id);
                            } catch (NullPointerException e) {
                                toot_text = "";
                                toot_text_id = 0;
                                toot_text_id_string = String.valueOf(toot_text_id);
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
                                    toot_text_time = japanDateFormat.format(calendar.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                toot_text_time = status.getCreatedAt();
                            }

                            //カスタム絵文字
                            if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                try {
                                    //本文
                                    List<Emoji> emoji_List = status.getStatus().getEmojis();
                                    emoji_List.forEach(emoji -> {
                                        String emoji_name = emoji.getShortcode();
                                        String emoji_url = emoji.getUrl();
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                    });

                                } catch (NullPointerException e) {
                                    toot_text = "";
                                    toot_text_id = 0;
                                    toot_text_id_string = String.valueOf(toot_text_id);
                                }

                                //DisplayNameのほう
                                List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                                account_emoji_List.forEach(emoji -> {
                                    String emoji_name = emoji.getShortcode();
                                    String emoji_url = emoji.getUrl();
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                });
                            }

                            Locale locale = Locale.getDefault();
                            boolean jp = locale.equals(Locale.JAPAN);

                            if (type.equals("mention")) {
                                if (jp) {
                                    type = " さんが返信しました";
                                } else {
                                    type = " mentioned";
                                }
                                layout_type = "Notification_mention";
                            }
                            if (type.equals("reblog")) {
                                if (jp) {
                                    type = " さんがブーストしました";
                                } else {
                                    type = " boosted";
                                }
                                layout_type = "Notification_reblog";
                            }
                            if (type.equals("favourite")) {
                                if (jp) {
                                    if (friends_nico_check_box) {
                                        type = " さんがお気に入りしました";
                                    } else {
                                        type = " さんが二コりました";
                                    }
                                } else {
                                    type = "favourited";
                                }
                                layout_type = "Notification_favourite";
                            }

                            if (type.equals("follow")) {
                                if (jp) {
                                    type = " さんがフォローしました";
                                } else {
                                    type = " followed";
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

                            //Card
                            ArrayList<String> card = new ArrayList<>();
                            String cardTitle = null;
                            String cardURL = null;
                            String cardDescription = null;
                            String cardImage = null;

                            try {
                                Card card_status = new Statuses(client).getCard(toot_text_id).execute();
                                if (!card_status.getUrl().isEmpty()) {
                                    cardTitle = card_status.getTitle();
                                    cardURL = card_status.getUrl();
                                    cardDescription = card_status.getDescription();
                                    cardImage = card_status.getImage();
                                }
                            } catch (Mastodon4jRequestException e) {
                                e.printStackTrace();
                            }


                            //配列を作成
                            ArrayList<String> Item = new ArrayList<>();
                            //メモとか通知とかに
                            Item.add(layout_type);
                            //内容
                            Item.add(toot_text);
                            //ユーザー名
                            Item.add(user_name + " @" + user + type);
                            //時間、クライアント名等
                            Item.add("トゥートID : " + toot_text_id_string + " / " + getString(R.string.time) + " : " + toot_text_id_string);
                            //Toot ID 文字列版
                            Item.add(toot_text_id_string);
                            //アバターURL
                            Item.add(user_avater_url);
                            //アカウントID
                            Item.add(String.valueOf(account_id));
                            //ユーザーネーム
                            Item.add(user);
                            //メディア
                            Item.add(media_url_1);
                            Item.add(media_url_2);
                            Item.add(media_url_3);
                            Item.add(media_url_4);
                            //カード
                            Item.add(cardTitle);
                            Item.add(cardURL);
                            Item.add(cardDescription);
                            Item.add(cardImage);


                            if (getActivity() != null) {
                                ListItem listItem = new ListItem(Item);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                        ListView listView = (ListView) view.findViewById(R.id.notifications_list);

                                        listView.setAdapter(adapter);
                                    }
                                });
                            }

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

                    HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_text_list);
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //引っ張って更新するやつ
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

                                    user_name = status.getAccount().getDisplayName();
                                    type = status.getType();
                                    toot_text_time = status.getCreatedAt();
                                    user_avater_url = status.getAccount().getAvatar();
                                    user_id = status.getAccount().getUserName();
                                    user = status.getAccount().getAcct();

                                    account_id = status.getAccount().getId();

                                    String toot_text_id_string = null;

                                    //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                                    try {
                                        toot_text = status.getStatus().getContent();
                                        toot_text_id = status.getStatus().getId();
                                        toot_text_id_string = String.valueOf(toot_text_id);
                                    } catch (NullPointerException e) {
                                        toot_text = "";
                                        toot_text_id = 0;
                                        toot_text_id_string = String.valueOf(toot_text_id);
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
                                            toot_text_time = japanDateFormat.format(calendar.getTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        toot_text_time = status.getCreatedAt();
                                    }


                                    //カスタム絵文字
                                    if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                        try {
                                            List<Emoji> emoji_List = status.getStatus().getEmojis();
                                            emoji_List.forEach(emoji -> {
                                                String emoji_name = emoji.getShortcode();
                                                String emoji_url = emoji.getUrl();
                                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                            });
                                        } catch (NullPointerException e) {
                                            toot_text = "";
                                            toot_text_id = 0;
                                            toot_text_id_string = String.valueOf(toot_text_id);
                                        }

                                        //DisplayNameのほう
                                        List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                                        account_emoji_List.forEach(emoji -> {
                                            String emoji_name = emoji.getShortcode();
                                            String emoji_url = emoji.getUrl();
                                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                            user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                        });
                                    }


                                    Locale locale = Locale.getDefault();
                                    boolean jp = locale.equals(Locale.JAPAN);

                                    if (type.equals("mention")) {
                                        if (jp) {
                                            type = " さんが返信しました";
                                        } else {
                                            type = " mentioned";
                                        }
                                        layout_type = "Notification_mention";
                                    }
                                    if (type.equals("reblog")) {
                                        if (jp) {
                                            type = " さんがブーストしました";
                                        } else {
                                            type = " boosted";
                                        }
                                        layout_type = "Notification_reblog";
                                    }
                                    if (type.equals("favourite")) {
                                        if (jp) {
                                            if (friends_nico_check_box) {
                                                type = " さんがお気に入りしました";
                                            } else {
                                                type = " さんが二コりました";
                                            }
                                        } else {
                                            type = "favourited";
                                        }
                                        layout_type = "Notification_favourite";
                                    }

                                    if (type.equals("follow")) {
                                        if (jp) {
                                            type = " さんがフォローしました";
                                        } else {
                                            type = " followed";
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

                                    //Card
                                    ArrayList<String> card = new ArrayList<>();
                                    String cardTitle = null;
                                    String cardURL = null;
                                    String cardDescription = null;
                                    String cardImage = null;

                                    try {
                                        Card card_status = new Statuses(client).getCard(toot_text_id).execute();
                                        if (!card_status.getUrl().isEmpty()) {
                                            cardTitle = card_status.getTitle();
                                            cardURL = card_status.getUrl();
                                            cardDescription = card_status.getDescription();
                                            cardImage = card_status.getImage();

                                            card.add(card_status.getTitle());
                                            card.add(card_status.getUrl());
                                            card.add(card_status.getDescription());
                                            card.add(card_status.getImage());
                                        }
                                    } catch (Mastodon4jRequestException e) {
                                        e.printStackTrace();
                                    }


                                    //配列を作成
                                    ArrayList<String> Item = new ArrayList<>();
                                    //メモとか通知とかに
                                    Item.add(layout_type);
                                    //内容
                                    Item.add(toot_text);
                                    //ユーザー名
                                    Item.add(user_name + " @" + user + type);
                                    //時間、クライアント名等
                                    Item.add("トゥートID : " + toot_text_id_string + " / " + getString(R.string.time) + " : " + toot_text_time);
                                    //Toot ID 文字列版
                                    Item.add(toot_text_id_string);
                                    //アバターURL
                                    Item.add(user_avater_url);
                                    //アカウントID
                                    Item.add(String.valueOf(account_id));
                                    //ユーザーネーム
                                    Item.add(user);
                                    //メディア
                                    Item.add(media_url_1);
                                    Item.add(media_url_2);
                                    Item.add(media_url_3);
                                    Item.add(media_url_4);
                                    //カード
                                    Item.add(cardTitle);
                                    Item.add(cardURL);
                                    Item.add(cardDescription);
                                    Item.add(cardImage);


                                    if (getActivity() != null) {
                                        ListItem listItem = new ListItem(Item);

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.add(listItem);
                                                adapter.notifyDataSetChanged();
                                                ListView listView = (ListView) view.findViewById(R.id.notifications_list);

                                                listView.setAdapter(adapter);
                                            }
                                        });
                                    }
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

        }


        //非同期通信
        //通知を取得
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
                        // System.out.println("最後だよ");

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
                                    JSONObject last_toot_text_jsonObject = jsonArray.getJSONObject(29);
                                    max_id = last_toot_text_jsonObject.getString("id");
//                                    System.out.println("最後" + max_id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        if (max_id != null) {

                            //SnackBer表示
                            Snackbar maxid_snackbar = Snackbar.make(view, "通知を取得中 \r\n /api/v1/notifications \r\n max_id=" + max_id, Snackbar.LENGTH_INDEFINITE);
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
                                            JSONObject toot_text_jsonObject = jsonArray.getJSONObject(i);
                                            JSONObject toot_text_account = toot_text_jsonObject.getJSONObject("account");
                                            user_id = toot_text_account.getString("username");
                                            user_name = toot_text_account.getString("display_name");
                                            toot_text_time = toot_text_jsonObject.getString("created_at");
                                            type = toot_text_jsonObject.getString("type");
                                            String toot_text_id_string = null;
                                            toot_text = "";

                                            JSONObject toot_text_status = null;
                                            user_avater_url = toot_text_account.getString("avatar");

                                            account_id = toot_text_account.getLong("id");

                                            user = toot_text_account.getString("acct");

                                            List<Attachment> attachment = Collections.singletonList(new Attachment());

                                            //カード情報
                                            String cardTitle = null;
                                            String cardURL = null;
                                            String cardDescription = null;
                                            String cardImage = null;

                                            if (toot_text_jsonObject.has("status")) {
                                                toot_text_status = toot_text_jsonObject.getJSONObject("status");
                                                toot_text = toot_text_status.getString("content");
                                                toot_text_id_string = toot_text_status.getString("id");

                                                JSONArray media_array = toot_text_status.getJSONArray("media_attachments");
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
                                                    JSONArray emoji = toot_text_status.getJSONArray("emojis");
                                                    for (int e = 0; e < emoji.length(); e++) {
                                                        JSONObject jsonObject = emoji.getJSONObject(e);
                                                        String emoji_name = jsonObject.getString("shortcode");
                                                        String emoji_url = jsonObject.getString("url");
                                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                    }

                                                    //アバター絵文字
                                                    try {
                                                        JSONArray avater_emoji = toot_text_jsonObject.getJSONArray("profile_emojis");
                                                        for (int a = 0; a < avater_emoji.length(); a++) {
                                                            JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                                            String emoji_name = jsonObject.getString("shortcode");
                                                            String emoji_url = jsonObject.getString("url");
                                                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                            toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                            user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                        }

                                                        //ユーザーネームの方のアバター絵文字
                                                        JSONArray account_avater_emoji = toot_text_account.getJSONArray("profile_emojis");
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
                                                    JSONArray account_emoji = toot_text_account.getJSONArray("emojis");
                                                    for (int e = 0; e < account_emoji.length(); e++) {
                                                        JSONObject jsonObject = account_emoji.getJSONObject(e);
                                                        String emoji_name = jsonObject.getString("shortcode");
                                                        String emoji_url = jsonObject.getString("url");
                                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                    }
                                                }

                                                //カード情報
                                                if (!toot_text_status.isNull("card")) {
                                                    JSONObject cardObject = toot_text_status.getJSONObject("card");
                                                    cardURL = cardObject.getString("url");
                                                    cardTitle = cardObject.getString("title");
                                                    cardDescription = cardObject.getString("description");
                                                    cardImage = cardObject.getString("image");
                                                }

                                            }

                                            final String[] medias = new String[1];

                                            final String[] media_url = {null};

                                            Locale locale = Locale.getDefault();
                                            boolean jp = locale.equals(Locale.JAPAN);

                                            if (type.equals("mention")) {
                                                if (jp) {
                                                    type = " さんが返信しました";
                                                } else {
                                                    type = " mentioned";
                                                }
                                                layout_type = "Notification_mention";
                                            }
                                            if (type.equals("reblog")) {
                                                if (jp) {
                                                    type = " さんがブーストしました";
                                                } else {
                                                    type = " boosted";
                                                }
                                                layout_type = "Notification_reblog";
                                            }
                                            if (type.equals("favourite")) {
                                                if (jp) {
                                                    if (friends_nico_check_box) {
                                                        type = " さんがお気に入りしました";
                                                    } else {
                                                        type = " さんが二コりました";
                                                    }
                                                } else {
                                                    type = "favourited";
                                                }
                                                layout_type = "Notification_favourite";
                                            }

                                            if (type.equals("follow")) {
                                                if (jp) {
                                                    type = " さんがフォローしました";
                                                } else {
                                                    type = " followed";
                                                }
                                                layout_type = "Notification_follow";
                                            }

                                            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                                            if (japan_timeSetting) {
                                                //時差計算？
                                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                                //日本用フォーマット
                                                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                                try {
                                                    Date date = simpleDateFormat.parse(toot_text_jsonObject.getString("created_at"));
                                                    Calendar calendar = Calendar.getInstance();
                                                    calendar.setTime(date);
                                                    //9時間足して日本時間へ
                                                    calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                                    //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                                    toot_text_time = japanDateFormat.format(calendar.getTime());
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                toot_text_time = toot_text_jsonObject.getString("created_at");
                                            }

                                            //配列を作成
                                            ArrayList<String> Item = new ArrayList<>();
                                            //メモとか通知とかに
                                            Item.add(layout_type);
                                            //内容
                                            Item.add(toot_text);
                                            //ユーザー名
                                            Item.add(user_name + " @" + user + type);
                                            //時間、クライアント名等
                                            Item.add("トゥートID : " + toot_text_id_string + " / " + getString(R.string.time) + " : " + toot_text_time);
                                            //Toot ID 文字列版
                                            Item.add(toot_text_id_string);
                                            //アバターURL
                                            Item.add(user_avater_url);
                                            //アカウントID
                                            Item.add(String.valueOf(account_id));
                                            //ユーザーネーム
                                            Item.add(user);
                                            //メディア
                                            Item.add(media_url_1);
                                            Item.add(media_url_2);
                                            Item.add(media_url_3);
                                            Item.add(media_url_4);
                                            //カード
                                            Item.add(cardTitle);
                                            Item.add(cardURL);
                                            Item.add(cardDescription);
                                            Item.add(cardImage);


                                            if (getActivity() != null) {
                                                ListItem listItem = new ListItem(Item);

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        adapter.add(listItem);
                                                        adapter.notifyDataSetChanged();
                                                        listView.setAdapter(adapter);
                                                        snackbar.dismiss();
                                                        //listView.setSelection(scrollPosition);
                                                    }
                                                });
                                            }

                                            media_url_1 = null;
                                            media_url_2 = null;
                                            media_url_3 = null;
                                            media_url_4 = null;
                                            layout_type = null;

                                        }
                                        //最後のIDを更新する
                                        JSONObject last_toot_text = jsonArray.getJSONObject(29);
                                        max_id = last_toot_text.getString("id");

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

    //フラグメントが外されたときに呼ばれる
    @Override
    public void onDetach() {
        super.onDetach();
        //System.out.println("終了");
//        asyncTask.cancel(true);
        //ストリーミング終了
        if (shutdownable != null) {
            shutdownable.shutdown();
        }
    }
}