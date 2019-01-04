package io.github.takusan23.kaisendon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslCertificate;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserFollowActivity extends AppCompatActivity {

    long account_id;

    private ProgressDialog dialog;

    String toot_time;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;

    SharedPreferences pref_setting;

    String max_id = null;

    int position;
    int y;

    //↓これ！！！！はもう内容が無いのに取得してしまうのを抑えるために使うよ！！！！！！！！！！！！！！！！！
    int max_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        if (dark_mode) {
            setTheme(R.style.Theme_AppCompat_DayNight);
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        //この画面用にNoActionBerなダークテーマを指定している
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            setTheme(R.style.OLED_Theme);
        } else {
            //なにもない
        }

        setContentView(R.layout.activity_user_follow);

        final android.os.Handler handler_1 = new android.os.Handler();

        //設定を読み込み
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


        //account_idを受け取る
        Intent intent = getIntent();
        account_id = intent.getLongExtra("account_id", 0);

        //カウントも受け取る
        max_count = intent.getIntExtra("count", 0);

        System.out.println("渡された : " + String.valueOf(max_count));

        String finalInstance = Instance;
        String finalAccessToken = AccessToken;

        //フォローかフォロワーかを分ける
        int follow_follower = intent.getIntExtra("follow_follower", 0);

        //くるくる
/*
        dialog = new ProgressDialog(UserFollowActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
*/
        String snackber_text = null;

        // dialog.show();
        if (follow_follower == 1) {
            snackber_text = getString(R.string.loading_follow) + "\r\n /api/v1/accounts/:id/followers";
        }
        if (follow_follower == 2) {
            snackber_text = getString(R.string.loading_follower) + "\r\n /api/v1/accounts/:id/following";
        }
        if (follow_follower == 3) {
            snackber_text = getString(R.string.loading_toot) + "\r\n /api/v1/accounts/:id/statuses";
        }


        View view = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(view, snackber_text, Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(UserFollowActivity.this);
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar.show();


        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(UserFollowActivity.this, R.layout.timeline_item, toot_list);
        SimpleAdapter simpleAdapter = new SimpleAdapter(UserFollowActivity.this, R.layout.timeline_item, toot_list);

        if (follow_follower == 1) {
            //ふぉろー
            LoadFollow(Instance, AccessToken, simpleAdapter, false, false);
            snackbar.dismiss();
/*
            //追加
            ListView listView = (ListView) findViewById(R.id.follow_follower_list);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount > 79 && totalItemCount == firstVisibleItem + visibleItemCount && simpleAdapter.getCount() != max_count) {
                        Snackbar snackbar_ = Snackbar.make(view, R.string.add_loading, Snackbar.LENGTH_LONG);
                        snackbar_.show();
                        if (snackbar_.isShown()) {
                            System.out.println("渡された : " + String.valueOf(simpleAdapter.getCount()));
                            snackbar.show();
                            position = listView.getFirstVisiblePosition();
                            y = listView.getChildAt(0).getTop();
                            LoadFollow(finalInstance, finalAccessToken, simpleAdapter, true, false);
                            snackbar.dismiss();
                        }
                    }
                }
            });
*/
        }
        if (follow_follower == 2) {

            //ふぉろわー
            LoadFollow(Instance, AccessToken, simpleAdapter, false, true);
            snackbar.dismiss();
/*
            //追加
            ListView listView = (ListView) findViewById(R.id.follow_follower_list);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount > 79 && totalItemCount == firstVisibleItem + visibleItemCount && totalItemCount != max_count) {
                        Snackbar snackbar_ = Snackbar.make(view, R.string.add_loading, Snackbar.LENGTH_LONG);
                        snackbar_.show();
                        if (snackbar_.isShown()) {
                            snackbar.show();
                            position = listView.getFirstVisiblePosition();
                            y = listView.getChildAt(0).getTop();
                            LoadFollow(finalInstance, finalAccessToken, simpleAdapter, true, true);
                            snackbar.dismiss();
                        }
                    }
                }
            });
*/
        }

        //toot
        if (follow_follower == 3) {
            LoadUserToot(Instance, AccessToken, adapter, false);
            snackbar.dismiss();
            //追加
            ListView listView = (ListView) findViewById(R.id.follow_follower_list);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount > 39 && totalItemCount == firstVisibleItem + visibleItemCount && totalItemCount != max_count) {
                        Snackbar snackbar_ = Snackbar.make(view, R.string.add_loading, Snackbar.LENGTH_LONG);
                        snackbar_.show();
                        if (snackbar_.isShown()) {
                            snackbar.show();
                            position = listView.getFirstVisiblePosition();
                            y = listView.getChildAt(0).getTop();
                            LoadUserToot(finalInstance, finalAccessToken, adapter, true);
                            snackbar.dismiss();
                        }
                    }
                }
            });
        }

    }


    public void LoadUserToot(String Instance, String AccessToken, HomeTimeLineAdapter adapter, boolean addLoad) {
        String url = "https://" + Instance + "/api/v1/accounts/" + account_id + "/statuses/?access_token=" + AccessToken;
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("limit", "40");
        if (addLoad) {
            builder.addQueryParameter("max_id", max_id);
        }
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
                    //max_count = jsonArray.length();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                        JSONObject toot_account = toot_jsonObject.getJSONObject("account");
                        JSONArray media_array = toot_jsonObject.getJSONArray("media_attachments");
                        String toot_text = toot_jsonObject.getString("content");
                        String user = toot_account.getString("username");
                        String user_name = toot_account.getString("display_name");
                        toot_time = toot_jsonObject.getString("created_at");
                        String type = null;
                        String user_use_client = null;

                        //クライアント名がある？ない？
                        try {
                            JSONObject application = toot_jsonObject.getJSONObject("application");
                            user_use_client = application.getString("name");
                        } catch (JSONException e) {
                            user_use_client = toot_jsonObject.getString("application");
                        }

                        //user_use_client = status.getApplication().getName();
                        //toot_id = toot_jsonObject.getString("id");
                        String toot_id_string = toot_jsonObject.getString("id");

                        String user_avater_url = toot_account.getString("avatar");

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

                        //絵文字
                        JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                        for (int e = 0; e < emoji.length(); e++) {
                            JSONObject jsonObject = emoji.getJSONObject(e);
                            String emoji_name = jsonObject.getString("shortcode");
                            String emoji_url = jsonObject.getString("url");
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                            user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                        }

                        //アバター絵文字
                        try {
                            JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                            for (int a = 0; a < avater_emoji.length(); a++) {
                                JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                String emoji_name = jsonObject.getString("shortcode");
                                String emoji_url = jsonObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
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
                        Item.add(type);
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

                        ListItem listItem = new ListItem(Item);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.add(listItem);
                                adapter.notifyDataSetChanged();
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ListView listView = (ListView) findViewById(R.id.follow_follower_list);
                                listView.setAdapter(adapter);
                                //listView.setSelection(scrollPosition);
                                if (addLoad) {
                                    listView.setSelectionFromTop(position, y);
                                }

                                //snackbar.dismiss();
                                //maxid_snackbar.dismiss();
                                //listView.setSelection(scrollPosition);
                            }
                        });
                        media_url_1 = null;
                        media_url_2 = null;
                        media_url_3 = null;
                        media_url_4 = null;
                        type = null;

                    }
                    //最後のIDを更新する
                    JSONObject last_toot = jsonArray.getJSONObject(39);
                    max_id = last_toot.getString("id");
                    //scrollPosition += 30;


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void LoadFollow(String Instance, String AccessToken, SimpleAdapter adapter, boolean addLoad, boolean follower) {
        String url = null;
        if (follower) {
            //フォロワー
            url = "https://" + Instance + "/api/v1/accounts/" + account_id + "/followers/?access_token=" + AccessToken;
        } else {
            //フォロー
            url = "https://" + Instance + "/api/v1/accounts/" + account_id + "/following/?access_token=" + AccessToken;
        }
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("limit", "80");
        if (addLoad) {
            builder.addQueryParameter("max_id", max_id);
        }
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

                Headers headers = response.headers();

                //System.out.println("リンクヘッダー？ : " + headers.get("link"));

                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(response_string);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        //アカウント情報ー
                        String display_name = jsonObject.getString("display_name");
                        String id = jsonObject.getString("acct");
                        String avater_url = jsonObject.getString("avatar");
                        String account_info = jsonObject.getString("note");
                        long account_id_follow = jsonObject.getLong("id");



                        //配列を作成
                        ArrayList<String> Item = new ArrayList<>();
                        //メモとか通知とかに
                        Item.add(null);
                        //内容
                        Item.add(account_info);
                        //ユーザー名
                        Item.add(display_name + " @" + id);
                        //時間、クライアント名等
                        Item.add(null);
                        //Toot ID 文字列版
                        Item.add("");
                        //アバターURL
                        Item.add(avater_url);
                        //アカウントID
                        Item.add(String.valueOf(account_id_follow));
                        //ユーザーネーム
                        Item.add(display_name);
                        //メディア
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        //カード
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);

                        ListItem listItem = new ListItem(Item);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.add(listItem);
                                adapter.notifyDataSetChanged();
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ListView listView = (ListView) findViewById(R.id.follow_follower_list);
                                listView.setAdapter(adapter);
                                setTitle(getString(R.string.follow));
                                if (addLoad) {
                                    listView.setSelectionFromTop(position, y);
                                }
                            }
                        });
                    }

                    int getmax_id_int;

                    if (79 < jsonArray.length()) {
                        getmax_id_int = 79;
                    } else {
                        getmax_id_int = jsonArray.length();
                    }


                    //最後のIDを更新する
                    JSONObject last_toot = jsonArray.getJSONObject(79);
                    max_id = last_toot.getString("id");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

}
