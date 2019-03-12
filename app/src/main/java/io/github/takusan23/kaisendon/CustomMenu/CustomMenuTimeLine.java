package io.github.takusan23.kaisendon.CustomMenu;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Card;
import com.sys1yagi.mastodon4j.api.entity.Emoji;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
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
    private String streaming;
    private String subtitle;

    private String max_id;

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeTimeLineAdapter adapter;

    private boolean scroll = false;
    private boolean streaming_mode;

    private int position;
    private int y;
    private Shutdownable shutdownable;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //OLED
        if (Boolean.valueOf(getArguments().getString("dark_mode"))) {
            getActivity().setTheme(R.style.OLED_Theme);
        }
        return inflater.inflate(R.layout.fragment_custom_menu_time_line, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        listView = view.findViewById(R.id.custom_menu_listview);
        swipeRefreshLayout = view.findViewById(R.id.custom_menu_swipe_refresh);

        //OLEDは背景を黒にする（一時的
        if (Boolean.valueOf(getArguments().getString("dark_mode"))) {
            listView.setBackgroundColor(Color.parseColor("#000000"));
        }


        //データ受け取り
        url = getArguments().getString("content");
        instance = getArguments().getString("instance");
        access_token = getArguments().getString("access_token");
        streaming = getArguments().getString("streaming");
        subtitle = getArguments().getString("subtitle");

        //最終的なURL
        url = "https://" + instance + url;
        //タイトル
        ((AppCompatActivity) getContext()).setTitle(getArguments().getString("name"));

        ArrayList<ListItem> toot_list = new ArrayList<>();
        adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);


        //サブタイトル更新
        //サブタイトルが空とかの処理
        if (subtitle.length() >= 1) {
            //サブタイトルはEditTextの値
            ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(subtitle);
        } else {
            //名前表示
            loadAccountName();
        }

        //ストリーミングAPI。本来は無効のときチェックを付けてるけど保存時に反転してるのでおっけ
        //無効・有効
        if (Boolean.valueOf(streaming)) {
            //有効
            //スワイプ無効
            swipeRefreshLayout.setEnabled(false);
            //通知以外
            if (!url.contains("/api/v1/notifications")) {
                //ストリーミング
                useStreamingAPI(false);
            } else {
                //ストリーミング
                useStreamingAPI(true);
            }
        } else {
            //無効
            //スワイプ有効
            swipeRefreshLayout.setEnabled(true);
            //通知以外
            if (!url.contains("/api/v1/notifications")) {
                //普通にAPI叩く
                loadNotification("");
            } else {
                //通常読み込み
                loadTimeline("");
            }
        }

        //タイムラインを読み込む
        //通知以外のURL
        if (!url.contains("/api/v1/notifications")) {
            SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
            loadTimeline("");
        } else {
            //通知
            SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
            loadNotification("");
        }

        //引っ張って更新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
                //通知以外
                if (!url.contains("/api/v1/notifications")) {
                    //普通にAPI叩く
                    loadTimeline("");
                } else {
                    loadNotification("");
                }
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
                        //通知以外
                        if (!url.contains("/api/v1/notifications")) {
                            //普通にAPI叩く
                            loadTimeline(max_id);
                        } else {
                            loadNotification(max_id);
                        }
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            });
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

    /**
     * ストリーミングAPI
     */
    private void useStreamingAPI(boolean notification_mode) {
        MastodonClient client = new MastodonClient.Builder(instance, new OkHttpClient.Builder(), new Gson())
                .accessToken(access_token)
                .useStreamingApi()
                .build();
        //AsyncTask
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... aVoid) {
                Handler handler = new Handler() {

                    @Override
                    public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {
                        //通知以外
                        if (!notification_mode) {
                            final String[] toot_text = {status.getContent()};
                            String user = status.getAccount().getUserName();
                            final String[] user_name = {status.getAccount().getDisplayName()};
                            String user_use_client = null;
                            long toot_id = status.getId();
                            String toot_id_string = String.valueOf(toot_id);
                            //toot_time = status.getCreatedAt();
                            long account_id = status.getAccount().getId();
                            //ブースト　ふぁぼ
                            //Reblogかな？
                            String isBoost = "no";
                            String isFav = "no";
                            String boostCount = "0";
                            String favCount = "0";
                            //ブーストあったよ
                            String boost_content = null;
                            String boost_user_name = null;
                            String boost_user = null;
                            String boost_avater_url = null;
                            long boost_account_id = 0;
                            String toot_time = null;

                            try {
                                boost_content = status.getReblog().getContent();
                                boost_user_name = status.getReblog().getAccount().getDisplayName();
                                boost_user = status.getReblog().getAccount().getUserName();
                                boost_avater_url = status.getReblog().getAccount().getAvatar();
                                boost_account_id = status.getReblog().getId();
                                //BTしたTootのばあいがあるね
                                if (status.getReblog().isReblogged()) {
                                    isBoost = "reblogged";
                                }
                                if (status.getReblog().isFavourited()) {
                                    isFav = "favourited";
                                }
                                //かうんと
                                boostCount = String.valueOf(status.getReblog().getReblogsCount());
                                favCount = String.valueOf(status.getReblog().getFavouritesCount());
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                //BTなかった
                                if (status.isReblogged()) {
                                    isBoost = "reblogged";
                                }
                                if (status.isFavourited()) {
                                    isFav = "favourited";
                                }
                                //かうんと
                                boostCount = String.valueOf(status.getReblogsCount());
                                favCount = String.valueOf(status.getFavouritesCount());
                            }

                            //ユーザーのアバター取得
                            String user_avater_url = status.getAccount().getAvatar();

                            //一番最初のIDを控える
                            if (max_id == null) {
                                max_id = toot_id_string;
                            }

                            //クライアント名
                            try {
                                user_use_client = status.getApplication().getName();
                            } catch (NullPointerException e) {
                                user_use_client = null;
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
                                    toot_time = japanDateFormat.format(calendar.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                toot_time = status.getCreatedAt();
                            }

                            String[] mediaURL = {null, null, null, null};
                            //めでぃあ
                            //配列に入れる形で
                            final int[] i = {0};
                            List<Attachment> list = status.getMediaAttachments();
                            list.forEach(media -> {
                                mediaURL[i[0]] = media.getUrl();
                                i[0]++;
                            });

                            //System.out.println("配列 : " + Arrays.asList(mediaURL));

                            //配列から文字列に
                            String media_url_1 = mediaURL[0];
                            String media_url_2 = mediaURL[1];
                            String media_url_3 = mediaURL[2];
                            String media_url_4 = mediaURL[3];


                            if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                //カスタム絵文字
                                List<Emoji> emoji_List = status.getEmojis();
                                emoji_List.forEach(emoji -> {
                                    String emoji_name = emoji.getShortcode();
                                    String emoji_url = emoji.getUrl();
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    toot_text[0] = toot_text[0].replace(":" + emoji_name + ":", custom_emoji_src);
                                });

                                //DisplayNameカスタム絵文字
                                List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                                account_emoji_List.forEach(emoji -> {
                                    String emoji_name = emoji.getShortcode();
                                    String emoji_url = emoji.getUrl();
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    user_name[0] = user_name[0].replace(":" + emoji_name + ":", custom_emoji_src);
                                });
                            }

                            Bitmap bmp = null;
                            //BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);  // 今回はサンプルなのでデフォルトのAndroid Iconを利用
                            ImageButton nicoru_button = null;


                            //Card
                            ArrayList<String> card = new ArrayList<>();
                            String cardTitle = null;
                            String cardURL = null;
                            String cardDescription = null;
                            String cardImage = null;

                            try {
                                Card statuses = new Statuses(client).getCard(toot_id).execute();
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

                            if (getActivity() != null && isAdded()) {

                                //配列を作成
                                ArrayList<String> Item = new ArrayList<>();
                                //メモとか通知とかに
                                Item.add("");
                                //内容
                                Item.add(toot_text[0]);
                                //ユーザー名
                                Item.add(user_name[0] + " @" + user);
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

                                        adapter.insert(listItem, 0);
                                        // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                                        int pos = listView.getFirstVisiblePosition();
                                        int top = 0;
                                        if (listView.getChildCount() > 0) {
                                            top = listView.getChildAt(0).getTop();
                                        }
                                        listView.setAdapter(adapter);
                                        //System.out.println("TOP == " + top);
                                        // 要素追加前の状態になるようセットする
                                        adapter.notifyDataSetChanged();
                                        //一番上なら追いかける
                                        if (pos == 0) {
                                            listView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    listView.smoothScrollToPosition(0);
                                                    //listView.setSelectionFromTop(index, top_);
                                                }
                                            });
                                            //System.out.println("ねてた");
                                        } else {
                                            listView.setSelectionFromTop(pos + 1, top);
                                        }
                                        //ストリーミングAPI前のStatus取得
                                        loadNotification(max_id);
/*
                            //カウンター
                            if (count_text != null && pref_setting.getBoolean("pref_toot_count", false)) {
                                //含んでいるか
                                if (toot_text.contains(count_text)) {
                                    String count_template = "　を含んだトゥート数 : ";
                                    akeome_count++;
                                    countTextView.setText(count_text + count_template + String.valueOf(akeome_count));
                                }
                            }
*/

                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onNotification(@NotNull Notification notification) {
                        //通知のみ
                        if (notification_mode) {
                            final String[] user_name = {notification.getAccount().getDisplayName()};
                            String type = notification.getType();
                            String user_avater_url = notification.getAccount().getAvatar();
                            String user_id = notification.getAccount().getUserName();
                            String user = notification.getAccount().getAcct();

                            long account_id = notification.getAccount().getId();

                            String toot_text_id_string = null;

                            final String[] toot_text = {null};
                            String toot_text_time = null;
                            String layout_type = null;
                            long toot_text_id = 0;

                            switch (type) {
                                case "mention":
                                    type = getString(R.string.notification_mention);
                                    layout_type = "Notification_mention";
                                    break;
                                case "reblog":
                                    type = getString(R.string.notification_Boost);
                                    layout_type = "Notification_reblog";
                                    break;
                                case "favourite":
                                    type = getString(R.string.notification_favourite);
                                    layout_type = "Notification_favourite";
                                    break;
                                case "follow":
                                    type = getString(R.string.notification_followed);
                                    layout_type = "Notification_follow";
                                    break;
                            }
                            //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                            try {
                                toot_text[0] = notification.getStatus().getContent();
                                toot_text_id = notification.getStatus().getId();
                                toot_text_id_string = String.valueOf(toot_text_id);
                            } catch (NullPointerException e) {
                                toot_text[0] = "";
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
                                        toot_text[0] = toot_text[0].replace(":" + emoji_name + ":", custom_emoji_src);
                                    });

                                } catch (NullPointerException e) {
                                    toot_text[0] = "";
                                    toot_text_id = 0;
                                    toot_text_id_string = String.valueOf(toot_text_id);
                                }

                                //DisplayNameのほう
                                List<Emoji> account_emoji_List = notification.getAccount().getEmojis();
                                account_emoji_List.forEach(emoji -> {
                                    String emoji_name = emoji.getShortcode();
                                    String emoji_url = emoji.getUrl();
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    user_name[0] = user_name[0].replace(":" + emoji_name + ":", custom_emoji_src);
                                });
                            }


                            String[] mediaURL = {null, null, null, null};
                            String media_url_1 = null;
                            String media_url_2 = null;
                            String media_url_3 = null;
                            String media_url_4 = null;
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

                            if (getActivity() != null && isAdded()) {

                                //配列を作成
                                ArrayList<String> Item = new ArrayList<>();
                                //メモとか通知とかに
                                Item.add(layout_type);
                                //内容
                                Item.add(toot_text[0]);
                                //ユーザー名
                                Item.add(user_name[0] + " @" + user + type);
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
                                        position = listView.getFirstVisiblePosition();
                                        y = listView.getChildAt(0).getTop();
                                        adapter.insert(listItem, 0);
                                        listView.setAdapter(adapter);
                                        //System.out.println("TOP == " + top);
                                        // 要素追加前の状態になるようセットする
                                        adapter.notifyDataSetChanged();
                                        listView.setSelectionFromTop(position, y);
                                        //ストリーミングAPI前のStatus取得
                                        loadNotification(max_id);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onDelete(long l) {

                    }
                };

                Streaming streaming = new Streaming(client);
                try {
                    switch (getArguments().getString("content")) {
                        case "/api/v1/timelines/home":
                            shutdownable = streaming.user(handler);
                            break;
                        case "/api/v1/notifications":
                            shutdownable = streaming.user(handler);
                            break;
                        case "/api/v1/timelines/public?local=true":
                            shutdownable = streaming.localPublic(handler);
                            break;
                        case "/api/v1/timelines/public":
                            shutdownable = streaming.federatedPublic(handler);
                            break;
                        case "/api/v1/timelines/direct":
                            break;
                    }
                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 通知
     *
     * @param max_id_id 追加読み込み。無いときは""でも
     */
    private void loadNotification(String max_id_id) {
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
                if (response.isSuccessful()) {
                    String response_string = response.body().string();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(response_string);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject toot_text_jsonObject = jsonArray.getJSONObject(i);
                            JSONObject toot_text_account = toot_text_jsonObject.getJSONObject("account");
                            String user_id = toot_text_account.getString("username");
                            String user_name = toot_text_account.getString("display_name");
                            String toot_text_time = toot_text_jsonObject.getString("created_at");
                            String type = toot_text_jsonObject.getString("type");
                            String toot_text_id_string = null;
                            String toot_text = "";

                            JSONObject toot_text_status = null;
                            String user_avater_url = toot_text_account.getString("avatar");

                            long account_id = toot_text_account.getLong("id");

                            String user = toot_text_account.getString("acct");

                            List<Attachment> attachment = Collections.singletonList(new Attachment());

                            //カード情報
                            String cardTitle = null;
                            String cardURL = null;
                            String cardDescription = null;
                            String cardImage = null;
                            //メディア
                            String media_url_1 = null;
                            String media_url_2 = null;
                            String media_url_3 = null;
                            String media_url_4 = null;

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

                            String layout_type = null;
                            switch (type) {
                                case "mention":
                                    type = getString(R.string.notification_mention);
                                    layout_type = "Notification_mention";
                                    break;
                                case "reblog":
                                    type = getString(R.string.notification_Boost);
                                    layout_type = "Notification_reblog";
                                    break;
                                case "favourite":
                                    type = getString(R.string.notification_favourite);
                                    layout_type = "Notification_favourite";
                                    break;
                                case "follow":
                                    type = getString(R.string.notification_followed);
                                    layout_type = "Notification_follow";
                                    break;
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

                            if (getActivity() != null && isAdded()) {

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
                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                        listView.setAdapter(adapter);
                                        SnackberProgress.closeProgressSnackber();
                                        //listView.setSelection(scrollPosition);
                                    }
                                });
                            }
                        }
                        //最後のIDを更新する
                        JSONObject last_toot_text = jsonArray.getJSONObject(29);
                        max_id = last_toot_text.getString("id");

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


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (shutdownable != null) {
            shutdownable.shutdown();
        }
    }
}
