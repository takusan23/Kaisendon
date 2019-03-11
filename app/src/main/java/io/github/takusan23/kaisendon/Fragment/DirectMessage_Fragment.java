package io.github.takusan23.kaisendon.Fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
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

import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DirectMessage_Fragment extends Fragment {

    String toot_text = null;
    String user = null;
    String user_name = null;
    String user_use_client = null;
    long toot_id;
    String toot_id_string = null;
    String user_avater_url = null;
    String toot_boost = null;
    String toot_time = null;
    String media_url = null;
    long account_id;
    private ProgressDialog dialog;
    View view;

    String max_id = null;
    String min_id = null;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;

    int count = 0;

    int scrollPosition = 0;

    Shutdownable shutdownable;

    int position;
    int y;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.activity_home_timeline, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //UIスレッド
        final android.os.Handler handler_1 = new android.os.Handler();

        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

        //アクセストークンを変更してる場合のコード
        //アクセストークン
        String AccessToken = null;

        //インスタンス
        String Instance = null;

        //getView().setBackgroundColor(Color.parseColor("#E687CEEB"));

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        getActivity().setTitle(R.string.direct_message);


        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)){
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }


        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

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
        if (pref_setting.getFloat("transparency", 1.0f) != 0.0) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        Snackbar snackbar = Snackbar.make(view, getString(R.string.loading_direct_message)+"\r\n /api/v1/timelines/direct", Snackbar.LENGTH_INDEFINITE);
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




        String finalAccessToken = AccessToken;

        String finalInstance = Instance;

        String url = "https://" + finalInstance + "/api/v1/timelines/direct/?access_token=" + finalAccessToken;
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("limit", "40");
        String final_url = builder.build().toString();
        //作成
        Request max_id_request = new Request.Builder()
                .url(final_url)
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
                        JSONArray media_array = toot_jsonObject.getJSONArray("media_attachments");
                        toot_text = toot_jsonObject.getString("content");
                        user = toot_account.getString("username");
                        user_name = toot_account.getString("display_name");
                        toot_time = toot_jsonObject.getString("created_at");

                        //クライアント名がある？ない？
                        try {
                            JSONObject application = toot_jsonObject.getJSONObject("application");
                            user_use_client = application.getString("name");
                        } catch (JSONException e) {
                            user_use_client = toot_jsonObject.getString("application");
                        }

                        //                       user_use_client = status.getApplication().getName();
                        //toot_id = toot_jsonObject.getString("id");
                        toot_id_string = toot_jsonObject.getString("id");

                        user_avater_url = toot_account.getString("avatar");

                        account_id = toot_account.getInt("id");

                        List<Attachment> attachment = Collections.singletonList(new Attachment());


                        final String[] medias = new String[1];

                        final String[] media_url = {null};

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

                        //System.out.println("これかあ！ ： " + media_url_1 + " / " + media_url_2  + " / " + media_url_3 + " / " + media_url_4);
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
                                toot_time = japanDateFormat.format(calendar.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            toot_time = toot_jsonObject.getString("created_at");
                        }

                        //絵文字
                        if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                            JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                            for (int e = 0; e < emoji.length(); e++) {
                                JSONObject jsonObject = emoji.getJSONObject(e);
                                String emoji_name = jsonObject.getString("shortcode");
                                String emoji_url = jsonObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                            }

                            //アバター絵文字
                            JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                            for (int a = 0; a < avater_emoji.length(); a++) {
                                JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                String emoji_name = jsonObject.getString("shortcode");
                                String emoji_url = jsonObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
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

                            //ユーザーネームの方のアバター絵文字
                            JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                            for (int a = 0; a < account_avater_emoji.length(); a++) {
                                JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                                String emoji_name = jsonObject.getString("shortcode");
                                String emoji_url = jsonObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                        }

                        //カード情報
                        String cardTitle = null;
                        String cardURL = null;
                        String cardDescription = null;
                        String cardImage = null;

                        if (!toot_jsonObject.isNull("card")) {
                            JSONObject cardObject = toot_jsonObject.getJSONObject("card");
                            cardURL = cardObject.getString("url");
                            cardTitle = cardObject.getString("title");
                            cardDescription = cardObject.getString("description");
                            cardImage = cardObject.getString("image");
                        }

                        //配列を作成
                        ArrayList<String> Item = new ArrayList<>();
                        //メモとか通知とかに
                        Item.add("");
                        //内容
                        Item.add(toot_text);
                        //ユーザー名
                        Item.add(user_name + " @" + user);
                        //時間、クライアント名等
                        Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time);
                        //Toot ID 文字列版
                        Item.add(toot_id_string);
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

                        if (getActivity() != null){
                            ListItem listItem = new ListItem(Item);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(listItem);
                                    adapter.notifyDataSetChanged();
                                    ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                    listView.setAdapter(adapter);
                                    snackbar.dismiss();
                                    //maxid_snackbar.dismiss();
                                    //listView.setSelection(scrollPosition);
                                }
                            });
                        }

                        media_url_1 = null;
                        media_url_2 = null;
                        media_url_3 = null;
                        media_url_4 = null;


                    }
                    //最後のIDを更新する
                    JSONObject last_toot = jsonArray.getJSONObject(39);
                    max_id = last_toot.getString("id");

                    //更新用に最初のIDを控える
                    JSONObject first_toot = jsonArray.getJSONObject(0);
                    min_id = first_toot.getString("id");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


/*
        //非同期通信
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();

                Timelines timelines = new Timelines(client);
                Range range = new Range(null, null, 40);

                ArrayList<ListItem> toot_list = new ArrayList<>();

                try {
                    Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statuses = timelines.getHome(range)
                            .execute();
                    statuses.getPart().forEach(status -> {



*/
/*
                        System.out.println("=============");
                        System.out.println(status.getAccount().getDisplayName());
                        System.out.println(status.getContent());
                        System.out.println(status.isReblogged());
*//*



                        toot_text = status.getContent();
                        user = status.getAccount().getAcct();
                        user_name = status.getAccount().getDisplayName();
                        try {
                            user_use_client = status.getApplication().getName();
                        } catch (NullPointerException e) {
                            user_use_client = null;
                        }
                        //toot_time = status.getCreatedAt();

                        toot_id = status.getId();
                        toot_id_string = String.valueOf(toot_id);

                        account_id = status.getAccount().getId();

                        user_avater_url = status.getAccount().getAvatar();

                        String[] medias = {null, null, null, null};
*/
/*
                        medias[0] = "https://www.google.co.jp/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";
                        medias[1] = "https://pbs.twimg.com/profile_images/1026695015140012032/ZT1or2k5_400x400.jpg";
                        medias[2] = "https://pbs.twimg.com/media/Djmc9cJUcAEoNJ-.jpg";
                        medias[3] = "https://pbs.twimg.com/tweet_video_thumb/Di6kwc8U4AEOXtf.jpg";
*//*


                        final String[] media_url = {null};
                        //めでぃあ
                        List<Attachment> list = status.getMediaAttachments();
                        list.forEach(media -> {

                            if (media_url_1 != null) {
                                media_url_1 = media.getUrl();
                            } else if (media_url_2 != null) {
                                media_url_2 = media.getUrl();
                            } else if (media_url_3 != null) {
                                media_url_3 = media.getUrl();
                            } else if (media_url_4 != null) {
                                media_url_4 = media.getUrl();
                            }

                            int i = 0;
                            //medias[i] = media.getUrl();
                            i++;
                            media_url[0] = media.getUrl();
                        });
                        System.out.println("画像リンク : " + media_url_1);
                        System.out.println("画像リンク : " + media_url_2);
                        System.out.println("画像リンク : " + media_url_3);
                        System.out.println("画像リンク : " + media_url_4);

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
                                toot_time = japanDateFormat.format(calendar.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            toot_time = status.getCreatedAt();
                        }


                        //ListItem listItem = new ListItem(null, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, medias);

                        //通知が行くように
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //adapter.add(listItem);
                                adapter.notifyDataSetChanged();
                            }
                        });

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                ListView listView = (ListView) view.findViewById(R.id.home_timeline);

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

                //くるくるを終了
                //dialog.dismiss();
                snackbar.dismiss();

                HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

                //これを書かないとListViewがエラーNullだよって言うから書こうね
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }


                return;
            }
        };
*/


        //引っ張って更新するやつ
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#BBDEFB"), Color.parseColor("#90CAF9"), Color.parseColor("#42A5F5"), Color.parseColor("#1565C0"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                adapter.clear();
                snackbar.show();

                //最後のトゥートIDを持ってくる
                //もういい！okhttpで実装する！！
                String max_id_url = "https://" + finalInstance + "/api/v1/timelines/direct/?access_token=" + finalAccessToken;
                //パラメータを設定
                HttpUrl.Builder max_id_builder = HttpUrl.parse(max_id_url).newBuilder();
                max_id_builder.addQueryParameter("limit", "40");
                //max_id_builder.addQueryParameter("since_id", min_id);
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
                                toot_text = toot_jsonObject.getString("content");
                                user = toot_account.getString("username");
                                user_name = toot_account.getString("display_name");
                                toot_time = toot_jsonObject.getString("created_at");

                                //クライアント名がある？ない？
                                try {
                                    JSONObject application = toot_jsonObject.getJSONObject("application");
                                    user_use_client = application.getString("name");
                                } catch (JSONException e) {
                                    user_use_client = toot_jsonObject.getString("application");
                                }

                                //                       user_use_client = status.getApplication().getName();
                                //toot_id = toot_jsonObject.getString("id");
                                toot_id_string = toot_jsonObject.getString("id");

                                user_avater_url = toot_account.getString("avatar");

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

                                //System.out.println("これかあ！ ： " + media_url_1 + " / " + media_url_2  + " / " + media_url_3 + " / " + media_url_4);
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
                                        toot_time = japanDateFormat.format(calendar.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    toot_time = toot_jsonObject.getString("created_at");
                                }

                                //絵文字
                                if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                    JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                                    for (int e = 0; e < emoji.length(); e++) {
                                        JSONObject jsonObject = emoji.getJSONObject(e);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                    }

                                    //アバター絵文字
                                    JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                                    for (int a = 0; a < avater_emoji.length(); a++) {
                                        JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
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

                                    //ユーザーネームの方のアバター絵文字
                                    JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                                    for (int a = 0; a < account_avater_emoji.length(); a++) {
                                        JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                    }
                                }

                                //カード情報
                                String cardTitle = null;
                                String cardURL = null;
                                String cardDescription = null;
                                String cardImage = null;

                                if (!toot_jsonObject.isNull("card")) {
                                    JSONObject cardObject = toot_jsonObject.getJSONObject("card");
                                    cardURL = cardObject.getString("url");
                                    cardTitle = cardObject.getString("title");
                                    cardDescription = cardObject.getString("description");
                                    cardImage = cardObject.getString("image");
                                }

                                //配列を作成
                                ArrayList<String> Item = new ArrayList<>();
                                //メモとか通知とかに
                                Item.add("");
                                //内容
                                Item.add(toot_text);
                                //ユーザー名
                                Item.add(user_name + " @" + user);
                                //時間、クライアント名等
                                Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time);
                                //Toot ID 文字列版
                                Item.add(toot_id_string);
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


                                if (getActivity() != null){
                                    ListItem listItem = new ListItem(Item);

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.add(listItem);
                                            adapter.notifyDataSetChanged();
                                            ListView listView = (ListView) view.findViewById(R.id.home_timeline);
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

                            }

                            //最後のIDを更新する
                            JSONObject last_toot = jsonArray.getJSONObject(39);
                            max_id = last_toot.getString("id");


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });




/*
                AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... string) {

                        snackbar.show();

                        MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                                .accessToken(finalAccessToken)
                                .build();

                        Timelines timelines = new Timelines(client);

                        //limitに40を指定して40件取得できるように
                        Range range = new Range(null, null, 40);

                        ArrayList<ListItem> toot_list = new ArrayList<>();


                        try {
                            Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statuses = timelines.getHome(range)
                                    .execute();
                            statuses.getPart().forEach(status -> {

*/
/*

                                System.out.println("=============");
                                System.out.println(status.getAccount().getDisplayName());
                                System.out.println(status.getContent());
                                System.out.println(status.isReblogged());

*//*


                                toot_text = status.getContent();
                                user = status.getAccount().getUserName();
                                user_name = status.getAccount().getDisplayName();
                                //toot_time = status.getCreatedAt();

                                //                       user_use_client = status.getApplication().getName();
                                toot_id = status.getId();
                                toot_id_string = String.valueOf(toot_id);

                                user_avater_url = status.getAccount().getAvatar();

                                account_id = status.getAccount().getId();

                                List<Attachment> attachment = Collections.singletonList(new Attachment());


                                final String[] medias = new String[1];

                                final String[] media_url = {null};
                                //めでぃあ
                                List<Attachment> list = status.getMediaAttachments();
                                list.forEach(media -> {

                                    media_url[0] = media.getUrl();

                                });

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
                                        toot_time = japanDateFormat.format(calendar.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    toot_time = status.getCreatedAt();
                                }

                                //ListItem listItem = new ListItem(null, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, null);

                                //メディア用
                                ListMediaItem listMediaItem = new ListMediaItem(media_url[0], media_url[0], media_url[0], media_url[0]);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ListView listView = (ListView) view.findViewById(R.id.home_timeline);
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

                        //くるくるを終了
                        //dialog.dismiss();
                        snackbar.dismiss();

*/
/*
                        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

                        //これを書かないとListViewがエラーNullだよって言うから書こうね
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
*//*


                        return;
                    }
                };
*/

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
                        String url = "https://" + finalInstance + "/api/v1/timelines/direct/?access_token=" + finalAccessToken;
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
                            Snackbar maxid_snackbar = Snackbar.make(view, getString(R.string.loading_direct_message)+"\r\n /api/v1/timelines/direct \r\n max_id=" + max_id, Snackbar.LENGTH_INDEFINITE);
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
                            String max_id_url = "https://" + finalInstance + "/api/v1/timelines/direct/?access_token=" + finalAccessToken;
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
                                            toot_text = toot_jsonObject.getString("content");
                                            user = toot_account.getString("username");
                                            user_name = toot_account.getString("display_name");
                                            toot_time = toot_jsonObject.getString("created_at");

                                            //クライアント名がある？ない？
                                            try {
                                                JSONObject application = toot_jsonObject.getJSONObject("application");
                                                user_use_client = application.getString("name");
                                            } catch (JSONException e) {
                                                user_use_client = toot_jsonObject.getString("application");
                                            }

                                            //                       user_use_client = status.getApplication().getName();
                                            //toot_id = toot_jsonObject.getString("id");
                                            toot_id_string = toot_jsonObject.getString("id");

                                            user_avater_url = toot_account.getString("avatar");

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
                                                    toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                }

                                                //アバター絵文字
                                                JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                                                for (int a = 0; a < avater_emoji.length(); a++) {
                                                    JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                                    String emoji_name = jsonObject.getString("shortcode");
                                                    String emoji_url = jsonObject.getString("url");
                                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                    toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
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

                                                //ユーザーネームの方のアバター絵文字
                                                JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                                                for (int a = 0; a < account_avater_emoji.length(); a++) {
                                                    JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
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
                                                    toot_time = japanDateFormat.format(calendar.getTime());
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                toot_time = toot_jsonObject.getString("created_at");
                                            }

                                            //カード情報
                                            String cardTitle = null;
                                            String cardURL = null;
                                            String cardDescription = null;
                                            String cardImage = null;

                                            if (!toot_jsonObject.isNull("card")) {
                                                JSONObject cardObject = toot_jsonObject.getJSONObject("card");
                                                cardURL = cardObject.getString("url");
                                                cardTitle = cardObject.getString("title");
                                                cardDescription = cardObject.getString("description");
                                                cardImage = cardObject.getString("image");
                                            }

                                            //配列を作成
                                            ArrayList<String> Item = new ArrayList<>();
                                            //メモとか通知とかに
                                            Item.add("");
                                            //内容
                                            Item.add(toot_text);
                                            //ユーザー名
                                            Item.add(user_name + " @" + user);
                                            //時間、クライアント名等
                                            Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time);
                                            //Toot ID 文字列版
                                            Item.add(toot_id_string);
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


                                            if (getActivity() != null){
                                                ListItem listItem = new ListItem(Item);

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        adapter.add(listItem);
                                                        adapter.notifyDataSetChanged();
                                                        ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                                        listView.setAdapter(adapter);
                                                        maxid_snackbar.dismiss();
                                                        listView.setSelectionFromTop(position, y);

                                                        //listView.setSelection(scrollPosition);
                                                    }
                                                });
                                            }

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
