package io.github.takusan23.kaisendon.CustomMenu;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.SnackberProgress;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomMenuTimeLine extends Fragment {

    private SharedPreferences pref_setting;

    private String url;
    private String instance;
    private String access_token;
    private String dialog;
    private String image_load;
    private String dark_mode;
    private String setting;

    private String max_id;

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeTimeLineAdapter adapter;

    private boolean scroll = false;

    private int position;
    private int y;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_menu_time_line, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        listView = view.findViewById(R.id.custom_menu_listview);
        swipeRefreshLayout = view.findViewById(R.id.custom_menu_swipe_refresh);

        //データ受け取り
        url = getArguments().getString("content");
        instance = getArguments().getString("instance");
        access_token = getArguments().getString("access_token");
        //最終的なURL
        url = "https://" + instance + url;
        //タイトル
        ((AppCompatActivity) getContext()).setTitle(getArguments().getString("name"));

        ArrayList<ListItem> toot_list = new ArrayList<>();
        adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

        //サブタイトル更新
        loadAccountName();

        //タイムラインを読み込む
        //通知とDM以外のURL
        if (!url.contains("/api/v1/notifications")) {
            SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
            loadTimeline("");
        }

        //引っ張って更新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
                loadTimeline("");
            }
        });

        //ToolBerをクリックしたら一番上に移動するようにする
        if (pref_setting.getBoolean("pref_listview_top", true)) {
            ((Home) getActivity()).getToolBer().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //これ一番上に移動するやつ
                    listView.smoothScrollToPosition(0);
                }
            });
        }

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
                        SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
                        scroll = true;
                        loadTimeline(max_id);
                    }
                }
            }
        });

    }

    /**
     * タイムラインを読み込む
     * 通知はこれでは読み込めない
     *
     * @param max_id_id 追加読み込み。無いときは""でも
     */
    private void loadTimeline(String max_id_id) {
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("limit", "40");
        builder.addQueryParameter("access_token", access_token);
        if (max_id_id.length() != 0) {
            builder.addQueryParameter("max_id", max_id_id);
        }

        String max_id_final_url = builder.build().toString();

        //作成
        Request request = new Request.Builder()
                .url(max_id_final_url)
                .get()
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //成功時
                if (response.isSuccessful()) {
                    String response_string = response.body().string();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(response_string);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                            JSONObject toot_account = toot_jsonObject.getJSONObject("account");
                            String toot_text = toot_jsonObject.getString("content");
                            String user = toot_account.getString("username");
                            String user_name = toot_account.getString("display_name");
                            String toot_id_string = toot_jsonObject.getString("id");
                            String user_avater_url = toot_account.getString("avatar");
                            int account_id = toot_account.getInt("id");
                            String toot_time = "";
                            //ブースト　ふぁぼ
                            String isBoost = "no";
                            String isFav = "no";
                            String boostCount = "0";
                            String favCount = "0";
                            //画像各位
                            String media_url_1 = null;
                            String media_url_2 = null;
                            String media_url_3 = null;
                            String media_url_4 = null;
                            //クライアント
                            String user_use_client = "";
                            //頑張ってApplication取ろう
                            if (!toot_jsonObject.isNull("application")) {
                                JSONObject application = toot_jsonObject.getJSONObject("application");
                                user_use_client = application.getString("name");
                            }

                            //ブーストかも
                            //ブーストあったよ
                            String boost_content = null;
                            String boost_user_name = null;
                            String boost_user = null;
                            String boost_avater_url = null;
                            long boost_account_id = 0;

                            if (!toot_jsonObject.isNull("reblog")) {
                                JSONObject reblogJsonObject = toot_jsonObject.getJSONObject("reblog");
                                JSONObject reblogAccountJsonObject = reblogJsonObject.getJSONObject("account");
                                boost_content = reblogJsonObject.getString("content");
                                boost_user_name = reblogAccountJsonObject.getString("display_name");
                                boost_user = reblogAccountJsonObject.getString("username");
                                boost_avater_url = reblogAccountJsonObject.getString("avatar");
                                boost_account_id = reblogAccountJsonObject.getLong("id");
                                if (reblogJsonObject.getBoolean("reblogged")) {
                                    isBoost = "reblogged";
                                }
                                if (reblogJsonObject.getBoolean("favourited")) {
                                    isFav = "favourited";
                                }
                                //かうんと
                                boostCount = String.valueOf(reblogJsonObject.getInt("reblogs_count"));
                                favCount = String.valueOf(reblogJsonObject.getInt("favourites_count"));
                            } else {
                                if (toot_jsonObject.getBoolean("reblogged")) {
                                    isBoost = "reblogged";
                                }
                                if (toot_jsonObject.getBoolean("favourited")) {
                                    isFav = "favourited";
                                }
                                //かうんと
                                boostCount = String.valueOf(toot_jsonObject.getInt("reblogs_count"));
                                favCount = String.valueOf(toot_jsonObject.getInt("favourites_count"));
                            }


                            //かうんと
                            boostCount = String.valueOf(toot_jsonObject.getInt("reblogs_count"));
                            favCount = String.valueOf(toot_jsonObject.getInt("favourites_count"));


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

                            if (getActivity() != null && isAdded()) {

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
                                //ブースト、ふぁぼしたか・ブーストカウント・ふぁぼかうんと
                                Item.add(isBoost);
                                Item.add(isFav);
                                Item.add(boostCount);
                                Item.add(favCount);
                                //Reblog ブースト用
                                Item.add(boost_content);
                                Item.add(boost_user_name + " @" + boost_user);
                                Item.add(boost_avater_url);
                                Item.add(String.valueOf(boost_account_id));

                                ListItem listItem = new ListItem(Item);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                        listView.setAdapter(adapter);
                                        //くるくる終了
                                        SnackberProgress.closeProgressSnackber();
                                        listView.setSelectionFromTop(position, y);
                                        scroll = false;
                                    }
                                });
                            }
                        }
                        //最後のIDを更新する
                        JSONObject last_toot = jsonArray.getJSONObject(39);
                        max_id = last_toot.getString("id");
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //失敗時
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), R.string.error + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    //DisplayName + ID　が出るようにする
    private void loadAccountName() {
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse("https://" + instance + "/api/v1/accounts/verify_credentials").newBuilder();
        builder.addQueryParameter("access_token", access_token);
        String url = builder.build().toString();
        //作成
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //失敗時
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        String name = jsonObject.getString("display_name");
                        String id = jsonObject.getString("acct");
                        //サブタイトル更新
                        //TODO  いつか設定できるようにしたい
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(name + "( @" + id + " / " + instance + " )");
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //失敗時
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), R.string.error + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}
