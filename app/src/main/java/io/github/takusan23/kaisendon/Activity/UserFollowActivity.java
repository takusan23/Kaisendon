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
import android.widget.Toast;

import com.sys1yagi.mastodon4j.api.entity.Attachment;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.takusan23.kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.SimpleAdapter;
import io.github.takusan23.kaisendon.SnackberProgress;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.github.takusan23.kaisendon.Preference_ApplicationContext.getContext;

public class UserFollowActivity extends AppCompatActivity {

    String account_id;

    private ProgressDialog dialog;

    String toot_time;

    private String AccessToken = null;
    private String Instance = null;

    private ListView listView;
    private ArrayList<ListItem> toot_list;
    private HomeTimeLineAdapter adapter;
    private SimpleAdapter simpleAdapter;

    private String snackber_text = null;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;

    SharedPreferences pref_setting;

    String max_id = null;

    int position;
    int y;

    private boolean isMisskey = false;

    //追加読み込み制御
    private boolean scroll = false;
    private Snackbar snackbar;

    int max_count;
    /**
     * Mastodon URL
     */
    private String mastodonUrl = "";
    /**
     * Misskey 追加読み込み用
     */
    private String nextID = "";
    /**
     * Misskey URL
     */
    private String misskeyUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        if (dark_mode) {
            setTheme(R.style.Theme_AppCompat_DayNight);
        } 
        //OLEDように真っ暗のテーマも使えるように
        //この画面用にNoActionBerなダークテーマを指定している
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            setTheme(R.style.OLED_Theme);
        }

        setContentView(R.layout.activity_user_follow);


        //設定を読み込み
        //Misskey
        if (isMisskey) {
            AccessToken = pref_setting.getString("misskey_main_token", "");
            Instance = pref_setting.getString("misskey_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");
        }

        toot_list = new ArrayList<>();
        adapter = new HomeTimeLineAdapter(UserFollowActivity.this, R.layout.timeline_item, toot_list);
        simpleAdapter = new SimpleAdapter(UserFollowActivity.this, R.layout.timeline_item, toot_list);
        listView = findViewById(R.id.follow_follower_list);

        //account_idを受け取る
        Intent intent = getIntent();
        account_id = intent.getStringExtra("account_id");

        //カウントも受け取る
        max_count = intent.getIntExtra("count", 0);

        //フォローかフォロワーかを分ける
        int follow_follower = intent.getIntExtra("follow_follower", 0);

        //Misskeyか分ける
        if (intent.getBooleanExtra("misskey", false)) {
            isMisskey = true;
        }
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

        //URL
        switch (getIntent().getIntExtra("follow_follower", 0)) {
            case 1:
                misskeyUrl = "/api/users/following";
                mastodonUrl = "https://" + Instance + "/api/v1/accounts/" + account_id + "/following";
                break;
            case 2:
                misskeyUrl = "/api/users/followers";
                mastodonUrl = "https://" + Instance + "/api/v1/accounts/" + account_id + "/followers";
                break;
            case 3:
                misskeyUrl = "/api/users/notes";
                mastodonUrl = "https://" + Instance + "/api/v1/accounts/" + account_id + "/statuses";
                break;
        }


        if (isMisskey) {
            postMisskeyAPI(misskeyUrl, null);
            //最後までスクロール
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount != 0 && totalItemCount == firstVisibleItem + visibleItemCount && !scroll) {
                        position = listView.getFirstVisiblePosition();
                        y = listView.getChildAt(0).getTop();
                        if (adapter.getCount() >= 20) {
                            SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + misskeyUrl);
                            scroll = true;
                            postMisskeyAPI(misskeyUrl, nextID);
                        }
                    }
                }
            });
        } else {
            postMastodonAPI(mastodonUrl, null);
            //最後までスクロール
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount != 0 && totalItemCount == firstVisibleItem + visibleItemCount && !scroll) {
                        position = listView.getFirstVisiblePosition();
                        y = listView.getChildAt(0).getTop();
                        if (adapter.getCount() >= 20) {
                            SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + misskeyUrl);
                            scroll = true;
                            postMastodonAPI(mastodonUrl, nextID);
                        }
                    }
                }
            });
        }
    }

    /**
     * APIを叩くところだけ
     * パースはそれぞれ
     *
     * @param api_url /api/xxxの部分
     */
    private void postMisskeyAPI(String api_url, String untilId) {
        //くるくる
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        String url = "https://" + instance + api_url;
        SnackberProgress.showProgressSnackber(listView, getContext(), getString(R.string.loading) + "\n" + url);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("i", token);
            jsonObject.put("limit", 100);
            jsonObject.put("userId", getIntent().getStringExtra("account_id"));
            //追加読み込み
            //Follow/Followerに関してはJSONArrayの最後にIDがあるのでそれ利用
            if (untilId != null) {
                jsonObject.put("untilId", untilId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        //作成
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserFollowActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                if (!response.isSuccessful()) {
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserFollowActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        switch (getIntent().getIntExtra("follow_follower", 0)) {
                            case 1:
                                loadMisskeyFollowFollower(response_string);
                                nextID = new JSONObject(response_string).getString("next");
                                break;
                            case 2:
                                loadMisskeyFollowFollower(response_string);
                                nextID = new JSONObject(response_string).getString("next");
                                break;
                            case 3:
                                loadMisskeyNote(response_string);
                                nextID = new JSONArray(response_string).getJSONObject(99).getString("id");
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Mastodon API叩くところ
     *
     * @param api_url 全部
     * @param max_id  使わないなら<b>null</b>で
     */
    private void postMastodonAPI(String api_url, String max_id) {
        //作成
        SnackberProgress.showProgressSnackber(listView, getContext(), getString(R.string.loading) + "\n" + api_url);
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(api_url).newBuilder();
        builder.addQueryParameter("limit", "80");
        builder.addQueryParameter("access_token", AccessToken);
        if (max_id != null) {
            builder.addQueryParameter("max_id", max_id);
        }
        String final_url = builder.build().toString();
        Request request = new Request.Builder()
                .url(final_url)
                .get()
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserFollowActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                if (!response.isSuccessful()) {
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserFollowActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        switch (getIntent().getIntExtra("follow_follower", 0)) {
                            case 1:
                                loadMastodonFollowFollower(response_string);
                                nextID = new JSONArray(response_string).getJSONObject(79).getString("id");
                                break;
                            case 2:
                                loadMastodonFollowFollower(response_string);
                                nextID = new JSONArray(response_string).getJSONObject(79).getString("id");
                                break;
                            case 3:
                                loadMastodonToot(response_string);
                                nextID = new JSONArray(response_string).getJSONObject(39).getString("id");
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    /**
     * Mastodon status Parse
     */
    public void loadMastodonToot(String response_string) {
        try {
            JSONArray jsonArray = new JSONArray(response_string);
            //max_count = jsonArray.length();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                JSONObject toot_account = toot_jsonObject.getJSONObject("account");
                JSONArray media_array = toot_jsonObject.getJSONArray("media_attachments");
                String toot_text = toot_jsonObject.getString("content");
                String user = toot_account.getString("username");
                String user_name = toot_account.getString("display_name");
                toot_time = toot_jsonObject.getString("created_at");
                String type = "";
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

                account_id = toot_account.getString("id");

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
                        listView.setAdapter(adapter);
                        listView.setSelectionFromTop(position, y);
                        scroll = false;
                        //おわり
                        SnackberProgress.closeProgressSnackber();
                    }
                });
                media_url_1 = null;
                media_url_2 = null;
                media_url_3 = null;
                media_url_4 = null;
                type = null;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mastodon Follow Follower JSON Parse
     */
    public void loadMastodonFollowFollower(String response_string) {
        try {
            JSONArray jsonArray = new JSONArray(response_string);
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
                Item.add("");
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
                        simpleAdapter.add(listItem);
                        simpleAdapter.notifyDataSetChanged();
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(simpleAdapter);
                        listView.setSelectionFromTop(position, y);
                        scroll = false;
                        //おわり
                        SnackberProgress.closeProgressSnackber();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Misskey users/notes
     */
    private void loadMisskeyNote(String response_string) {
        try {
            JSONArray jsonArray = new JSONArray(response_string);
            //max_count = jsonArray.length();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                JSONObject toot_account = toot_jsonObject.getJSONObject("user");
                JSONArray media_array = toot_jsonObject.getJSONArray("media");
                String toot_text = toot_jsonObject.getString("text");
                String user = toot_account.getString("username");
                String user_name = toot_account.getString("name");
                toot_time = toot_jsonObject.getString("createdAt");
                String type = "";
                String user_use_client = null;

                //user_use_client = status.getApplication().getName();
                //toot_id = toot_jsonObject.getString("id");
                String toot_id_string = toot_jsonObject.getString("id");

                String user_avater_url = toot_account.getString("avatarUrl");

                account_id = toot_account.getString("id");

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
                    String emoji_name = jsonObject.getString("name");
                    String emoji_url = jsonObject.getString("url");
                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                    toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                }
                //ユーザーネームの方の絵文字
                JSONArray account_emoji = toot_account.getJSONArray("emojis");
                for (int e = 0; e < account_emoji.length(); e++) {
                    JSONObject jsonObject = account_emoji.getJSONObject(e);
                    String emoji_name = jsonObject.getString("name");
                    String emoji_url = jsonObject.getString("url");
                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                }
                //カード情報
                String cardTitle = null;
                String cardURL = null;
                String cardDescription = null;
                String cardImage = null;

/*
                        if (!toot_jsonObject.isNull("card")) {
                            JSONObject cardObject = toot_jsonObject.getJSONObject("card");
                            cardURL = cardObject.getString("url");
                            cardTitle = cardObject.getString("title");
                            cardDescription = cardObject.getString("description");
                            cardImage = cardObject.getString("image");
                        }
*/
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
                        listView.setAdapter(adapter);

                        listView.setSelectionFromTop(position, y);
                        scroll = false;
                        //おわり
                        SnackberProgress.closeProgressSnackber();
                    }
                });
                media_url_1 = null;
                media_url_2 = null;
                media_url_3 = null;
                media_url_4 = null;
                type = null;

            }
            //最後のIDを更新する
            JSONObject last_toot = jsonArray.getJSONObject(99);
            max_id = last_toot.getString("id");
            //scrollPosition += 30;


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Misskey Follow/Follower取得
     */
    private void loadMisskeyFollowFollower(String response_string) {
        try {
            JSONObject first_Object = new JSONObject(response_string);
            JSONArray jsonArray = first_Object.getJSONArray("users");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //アカウント情報ー
                String display_name = jsonObject.getString("name");
                String id = jsonObject.getString("username");
                String avater_url = jsonObject.getString("avatarUrl");
                String account_info = jsonObject.getString("description");
                String account_id_follow = jsonObject.getString("id");
                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add("");
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
                Item.add(account_id_follow);
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
                        simpleAdapter.add(listItem);
                        simpleAdapter.notifyDataSetChanged();
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(simpleAdapter);
                        listView.setSelectionFromTop(position, y);
                        scroll = false;
                        //おわり
                        SnackberProgress.closeProgressSnackber();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
