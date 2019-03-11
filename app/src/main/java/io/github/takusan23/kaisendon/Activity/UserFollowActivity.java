package io.github.takusan23.kaisendon.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.sys1yagi.mastodon4j.api.entity.Attachment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.SimpleAdapter;
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

    //追加読み込み制御
    private boolean scroll = false;
    private Snackbar snackbar;

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
        snackbar = Snackbar.make(view, snackber_text, Snackbar.LENGTH_INDEFINITE);
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


        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(UserFollowActivity.this, R.layout.timeline_item, toot_list);
        SimpleAdapter simpleAdapter = new SimpleAdapter(UserFollowActivity.this, R.layout.timeline_item, toot_list);

        if (follow_follower == 1) {
            //ふぉろー
            LoadFollow(Instance, AccessToken, simpleAdapter, false, false, null);
            setTitle(getString(R.string.follow));
        }
        if (follow_follower == 2) {
            //ふぉろわー
            LoadFollow(Instance, AccessToken, simpleAdapter, false, true, null);
            setTitle(getString(R.string.follower));
        }

        if (follow_follower == 3) {
            //toot
            LoadUserToot(Instance, AccessToken, adapter, null);
            setTitle(getString(R.string.toot));
        }

    }


    /**
     * @param customURL URL指定できる。nullを入力すると既定値になるよ。
     */
    public void LoadUserToot(String Instance, String AccessToken, HomeTimeLineAdapter adapter, String customURL) {
        //くるくる
        snackbar.show();
        String url = "https://" + Instance + "/api/v1/accounts/" + account_id + "/statuses/?access_token=" + AccessToken;
        //カスタムURL

        //直接URLを指定
        if (customURL != null) {
            url = customURL + "&access_token=" + AccessToken;
        } else {
            url = "https://" + Instance + "/api/v1/accounts/" + account_id + "/statuses/?access_token=" + AccessToken;
        }

        //作成
        Request max_id_request = new Request.Builder()
                .url(url)
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
                //追加読み込み
                String header_url = response.headers().get("link");
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
                                ListView listView = (ListView) findViewById(R.id.follow_follower_list);
                                listView.setAdapter(adapter);

                                listView.setSelectionFromTop(position, y);
                                scroll = false;
                                //おわり
                                snackbar.dismiss();

                                //追加読み込み
                                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                    @Override
                                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                                        position = listView.getFirstVisiblePosition();
                                        y = listView.getChildAt(0).getTop();
                                    }

                                    @Override
                                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                        if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                                            scroll = true;
                                            //１個以上で動くように
                                            //URLを正規表現で取る？
                                            String url = null;
                                            ArrayList<String> url_list = new ArrayList<>();
                                            //正規表現実行
                                            //判定するパターンを生成
                                            Pattern p = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+");
                                            Matcher m = p.matcher(header_url);
                                            //正規表現で取り出す
                                            //ループ
                                            while (m.find()) {
                                                url_list.add(m.group());
                                            }

                                            //max_idを配列から探す
                                            //ないときは-1を返すのでちぇっく
                                            if (url_list.get(0).contains("max_id")) {
                                                url = url_list.get(0);
                                                System.out.println("max_id りんく : " + url);
                                                //実行
                                                if (url != null) {
                                                    LoadUserToot(Instance, AccessToken, adapter, url);
                                                }
                                            }
                                        }
                                    }
                                });


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

    /**
     * @param customURL URLを直接入力する。nullで無効にできます
     */
    public void LoadFollow(String Instance, String AccessToken, SimpleAdapter adapter, boolean addLoad, boolean follower, String customURL) {
        //くるくる
        snackbar.show();

        String url = null;
        //直接URLを指定
        if (customURL != null) {
            url = customURL + "&access_token=" + AccessToken;
        } else {
            if (follower) {
                //フォロワー
                url = "https://" + Instance + "/api/v1/accounts/" + account_id + "/followers/?access_token=" + AccessToken + "&limit=80";
            } else {
                //フォロー
                url = "https://" + Instance + "/api/v1/accounts/" + account_id + "/following/?access_token=" + AccessToken + "&limit=80";
            }
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

                        //追加読み込み
                        String header_url = response.headers().get("link");


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

                                listView.setSelectionFromTop(position, y);
                                scroll = false;
                                //おわり
                                snackbar.dismiss();

                                //追加読み込み
                                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                    @Override
                                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                                        position = listView.getFirstVisiblePosition();
                                        y = listView.getChildAt(0).getTop();
                                    }

                                    @Override
                                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                        if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                                            scroll = true;
                                            //１個以上で動くように
                                            //URLを正規表現で取る？
                                            String url = null;
                                            ArrayList<String> url_list = new ArrayList<>();
                                            //正規表現実行
                                            //判定するパターンを生成
                                            Pattern p = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+");
                                            Matcher m = p.matcher(header_url);
                                            //正規表現で取り出す
                                            //ループ
                                            while (m.find()) {
                                                url_list.add(m.group());
                                            }

                                            //max_idを配列から探す
                                            //ないときは-1を返すのでちぇっく
                                            if (url_list.get(0).contains("max_id")) {
                                                url = url_list.get(0);
                                                System.out.println("max_id りんく : " + url);
                                                //実行
                                                if (url != null) {
                                                    LoadFollow(Instance, AccessToken, adapter, false, false, url);
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }

/*                    //最後のIDを更新する
                    JSONObject last_toot = jsonArray.getJSONObject(79);
                    max_id = last_toot.getString("id");*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

}
