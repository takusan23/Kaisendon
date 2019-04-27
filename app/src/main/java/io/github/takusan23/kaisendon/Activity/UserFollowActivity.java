package io.github.takusan23.kaisendon.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.SnackberProgress;
import okhttp3.Call;
import okhttp3.Callback;
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

    //private ListView listView;
    //private ArrayList<ListItem> toot_list;
    //private HomeTimeLineAdapter adapter;
    //private SimpleAdapter simpleAdapter;

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

    //RecyclerView
    private ArrayList<ArrayList> recyclerViewList;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private RecyclerView recyclerView;
    private CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter;


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

        recyclerView = findViewById(R.id.follow_follower_list);

        recyclerViewList = new ArrayList<>();
        //ここから下三行必須
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
        recyclerView.setAdapter(customMenuRecyclerViewAdapter);
        recyclerViewLayoutManager = recyclerView.getLayoutManager();

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
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    int visibleItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getChildCount();
                    int totalItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getItemCount();
                    //最後までスクロールしたときの処理
                    if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                        position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        y = recyclerView.getChildAt(0).getTop();
                        if (recyclerViewList.size() >= 20) {
                            SnackberProgress.showProgressSnackber(recyclerView, getContext(), getString(R.string.loading) + "\n" + misskeyUrl);
                            scroll = true;
                            postMisskeyAPI(misskeyUrl, nextID);
                        }
                    }
                }
            });

        } else {
            postMastodonAPI(mastodonUrl, null);
            //最後までスクロール
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    int visibleItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getChildCount();
                    int totalItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getItemCount();
                    //最後までスクロールしたときの処理
                    if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                        position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        y = recyclerView.getChildAt(0).getTop();
                        if (recyclerViewList.size() >= 20) {
                            SnackberProgress.showProgressSnackber(recyclerView, getContext(), getString(R.string.loading) + "\n" + misskeyUrl);
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
        SnackberProgress.showProgressSnackber(recyclerView, getContext(), getString(R.string.loading) + "\n" + url);
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
        SnackberProgress.showProgressSnackber(recyclerView, getContext(), getString(R.string.loading) + "\n" + api_url);
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
                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add("CustomMenu Mastodon Toot");
                //内容
                Item.add(mastodonUrl);
                //ユーザー名
                Item.add("");
                //JSONObject
                Item.add(toot_jsonObject.toString());
                //ぶーすとした？
                Item.add("false");
                //ふぁぼした？
                Item.add("false");
                //Mastodon / Misskey
                Item.add("Mastodon");
                //Insatnce/AccessToken
                Item.add(Instance);
                Item.add(AccessToken);

                recyclerViewList.add(Item);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recyclerViewLayoutManager != null) {
                            ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                        }
                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                        //recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                        SnackberProgress.closeProgressSnackber();
                        scroll = false;
                    }
                });
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
                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add("CustomMenu Mastodon Follow");
                //内容
                Item.add(mastodonUrl);
                //ユーザー名
                Item.add("");
                //JSONObject
                Item.add(jsonObject.toString());
                //ぶーすとした？
                Item.add("false");
                //ふぁぼした？
                Item.add("false");
                //Mastodon / Misskey
                Item.add("Mastodon");
                //Insatnce/AccessToken
                Item.add(Instance);
                Item.add(AccessToken);

                recyclerViewList.add(Item);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recyclerViewLayoutManager != null) {
                            ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                        }
                        //recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);recyclerView.setAdapter(customMenuRecyclerViewAdapter);
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
                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add("CustomMenu Misskey Notes");
                //内容
                Item.add(misskeyUrl);
                //ユーザー名
                Item.add("");
                //JSONObject
                Item.add(toot_jsonObject.toString());
                //ぶーすとした？
                Item.add("false");
                //ふぁぼした？
                Item.add("false");
                //Mastodon / Misskey
                Item.add("Misskey");
                //Insatnce/AccessToken
                Item.add(Instance);
                Item.add(AccessToken);

                recyclerViewList.add(Item);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recyclerViewLayoutManager != null) {
                            ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                        }
                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                        //recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                        SnackberProgress.closeProgressSnackber();
                        scroll = false;
                    }
                });
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
                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add("CustomMenu Misskey Follow");
                //内容
                Item.add(mastodonUrl);
                //ユーザー名
                Item.add("");
                //JSONObject
                Item.add(jsonObject.toString());
                //ぶーすとした？
                Item.add("false");
                //ふぁぼした？
                Item.add("false");
                //Mastodon / Misskey
                Item.add("Misskey");
                //Insatnce/AccessToken
                Item.add(Instance);
                Item.add(AccessToken);

                recyclerViewList.add(Item);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recyclerViewLayoutManager != null) {
                            ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                        }
                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                        //recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                        SnackberProgress.closeProgressSnackber();
                        scroll = false;
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
