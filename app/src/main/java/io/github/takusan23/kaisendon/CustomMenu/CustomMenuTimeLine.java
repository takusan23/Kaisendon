package io.github.takusan23.kaisendon.CustomMenu;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Card;
import com.sys1yagi.mastodon4j.api.entity.Emoji;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.PicassoImageGetter;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomMenuTimeLine extends Fragment {

    private SharedPreferences pref_setting;

    private LinearLayout parent_linearlayout;

    private String misskey;
    private String url;
    private static String instance;
    private static String access_token;
    private String dialog;
    private String image_load;
    private String dark_mode;
    private String setting;
    private String streaming;
    private String subtitle;
    private String image_url;
    private String background_transparency;
    private String quick_profile;
    private String toot_counter;
    private String custom_emoji;
    private String gif;
    private String font;
    private String one_hand;

    private Boolean background_screen_fit;
    private boolean dark_theme = false;

    private String max_id;

    private LinearLayout linearLayout;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeTimeLineAdapter adapter;
    private ImageView imageView;
    private ImageView avater_imageView;
    private ImageView header_imageView;
    private TextView user_account_textView;
    private TextView user_id_textView;

    private boolean scroll = false;
    private boolean streaming_mode;

    private int position;
    private int y;
    private Shutdownable shutdownable;

    //通知フィルター
    private boolean fav_filter = true;
    private boolean bt_filter = true;
    private boolean mention_filter = true;
    private boolean follow_filter = true;

    private MastodonClient client;

    //トゥートカウンター
    private int akeome_count;
    private String count_text;
    private TextView countTextView;

    private CustomMenuSQLiteHelper helper = null;
    private SQLiteDatabase db = null;


    //WebSocket
    private WebSocketClient webSocketClient;

    //フォント
    public static Typeface font_Typeface;

    //名前
    private String display_name;
    private String username;
    private String follow;
    private String follower;
    private String statuses;
    private String note;
    private JSONObject account_JsonObject;
    private boolean emojis_show = false;

    //misskey
    private static boolean misskey_mode = false;
    private static String misskey_username = "";
    private static String account_id = "";
    private String untilId = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //OLED
        if (Boolean.valueOf(getArguments().getString("dark_mode"))) {
            //フォントのため
            getActivity().setTheme(R.style.OLED_Theme);
        } else {
            //フォントのため
            getActivity().setTheme(R.style.AppTheme);
        }
        return inflater.inflate(R.layout.fragment_custom_menu_time_line, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        parent_linearlayout = view.findViewById(R.id.custom_menu_parent_linearlayout);
        linearLayout = view.findViewById(R.id.custom_menu_fragment_linearlayout);
        listView = view.findViewById(R.id.custom_menu_listview);
        swipeRefreshLayout = view.findViewById(R.id.custom_menu_swipe_refresh);
        imageView = view.findViewById(R.id.custom_tl_background_imageview);

        //データ受け取り
        misskey = getArguments().getString("misskey");
        misskey_mode = Boolean.valueOf(misskey);
        url = getArguments().getString("content");
        instance = getArguments().getString("instance");
        access_token = getArguments().getString("access_token");
        streaming = getArguments().getString("streaming");
        subtitle = getArguments().getString("subtitle");
        dialog = getArguments().getString("dialog");
        image_load = getArguments().getString("image_load");
        image_url = getArguments().getString("image_url");
        background_transparency = getArguments().getString("background_transparency");
        background_screen_fit = Boolean.valueOf(getArguments().getString("background_screen_fit"));
        quick_profile = getArguments().getString("quick_profile");
        toot_counter = getArguments().getString("toot_counter");
        custom_emoji = getArguments().getString("custom_emoji");
        gif = getArguments().getString("gif");
        font = getArguments().getString("font");
        misskey_username = getArguments().getString("misskey_username");
        one_hand = getArguments().getString("one_hand");
        //Navication Drawer
        if (getActivity() != null) {
            NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
            //どろわーのイメージとか文字とか
            View navHeaderView = navigationView.getHeaderView(0);
            avater_imageView = navHeaderView.findViewById(R.id.icon_image);
            header_imageView = navHeaderView.findViewById(R.id.drawer_header);
            user_account_textView = navHeaderView.findViewById(R.id.drawer_account);
            user_id_textView = navHeaderView.findViewById(R.id.drawer_id);
        }
        //インスタンス、アクセストークン変更
        //Misskeyは設定しないように
        if (!misskey_mode) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putString("main_instance", instance);
            editor.putString("main_token", access_token);
            editor.apply();
        } else {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putString("misskey_main_instance", instance);
            editor.putString("misskey_main_token", access_token);
            editor.putString("misskey_main_username", misskey_username);
            editor.apply();
        }

        //トゥートカウンター
        countTextView = new TextView(getContext());

        //フォント設定。一回設定したら使い回すようにする
        //staticってこれで使い方あってんの
        File file = new File(font);
        if (file.exists()) {
            font_Typeface = Typeface.createFromFile(font);
        } else {
            font_Typeface = new TextView(getContext()).getTypeface();
        }

        //透明度設定は背景画像利用時のみ利用できるようにする
        if (image_url.length() == 0) {
            background_transparency = "";
        }

        //OLEDは背景を黒にする
        if (Boolean.valueOf(getArguments().getString("dark_mode"))) {
            linearLayout.setBackgroundColor(Color.parseColor("#" + background_transparency + "000000"));
            ((Home) getActivity()).getToolBer().setBackgroundColor(Color.parseColor("#000000"));
            dark_theme = true;
        } else {
            //黒にしなくていい
            linearLayout.setBackgroundColor(Color.parseColor("#" + background_transparency + "ffffff"));
            //てーま
            ((Home) getActivity()).getToolBer().setBackgroundColor(Color.parseColor("#2196f3"));
        }

        //最終的なURL
        url = "https://" + instance + url;
        //タイトル
        ((AppCompatActivity) getContext()).setTitle(getArguments().getString("name"));

        ArrayList<ListItem> toot_list = new ArrayList<>();
        adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

        //背景画像セット
        if (image_url.length() != 0) {
            //URI画像を入れる
            Glide.with(getContext()).load(image_url).into(imageView);
            //画面に合わせる設定
            if (background_screen_fit) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }

        //片手モード
        if (Boolean.valueOf(one_hand)) {
            one_hand_mode();
        }

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


        //トゥートカウンター
        if (Boolean.valueOf(toot_counter)) {
            setTootCounterLayout();
        }

        //Misskey
        if (misskey_mode) {
            loadMisskeyAccountName();
            //くるくる
            SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
            //通知以外
            if (!url.contains("notifications")) {
                //普通にAPI叩く
                loadMisskeyTimeline(null, false);
            } else {
                //通知レイアウト読み込み
                notificationLayout();
                loadMisskeyTimeline(null, true);
            }

            //引っ張って更新
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    adapter.clear();
                    //トゥートカウンター
                    countTextView.setText("");
                    akeome_count = 0;
                    SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
                    //通知以外
                    if (!url.contains("notifications")) {
                        //普通にAPI叩く
                        loadMisskeyTimeline(null, false);
                    } else {
                        loadMisskeyTimeline(null, true);
                    }
                }
            });

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
                            if (!url.contains("notifications")) {
                                //普通にAPI叩く
                                loadMisskeyTimeline(CustomMenuTimeLine.this.untilId, false);
                            } else {
                                loadMisskeyTimeline(null, true);
                            }
                        }
                    }
                }
            });
        } else {
            //名前表示
            //サブタイトル更新
            //片手モード有効時の処理
            loadAccountName();

            //ストリーミングAPI。本来は無効のときチェックを付けてるけど保存時に反転してるのでおっけ
            //無効・有効
            SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
            if (Boolean.valueOf(streaming)) {
                //有効
                //引っ張って更新無効
                swipeRefreshLayout.setEnabled(false);
                //通知以外
                if (!url.contains("/api/v1/notifications")) {
                    loadTimeline("");
                    //ストリーミング
                    useStreamingAPI(false);
                } else {
                    notificationLayout();
                    //普通にAPI叩く
                    loadNotification("");
                    //ストリーミング
                    useStreamingAPI(true);
                }
            } else {
                //無効
                //引っ張って更新有効
                swipeRefreshLayout.setEnabled(true);
                //通知以外
                if (url.contains("/api/v1/notifications")) {
                    //通知用レイアウト呼ぶ
                    notificationLayout();
                    //普通にAPI叩く
                    loadNotification("");
                } else {
                    //通常読み込み
                    loadTimeline("");
                }
            }
            //引っ張って更新
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    adapter.clear();
                    //トゥートカウンター
                    countTextView.setText("");
                    akeome_count = 0;
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
                            String user = toot_account.getString("acct");
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
                            if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(custom_emoji)) {
                                JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                                for (int e = 0; e < emoji.length(); e++) {
                                    JSONObject jsonObject = emoji.getJSONObject(e);
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

                                //Pawooで取れない？
                                if (!toot_jsonObject.isNull("profile_emojis")) {
                                    //アバター絵文字
                                    JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                                    for (int a = 0; a < avater_emoji.length(); a++) {
                                        JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
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
                                Item.add("CustomMenu");
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
                                //警告テキスト
                                //TODO 警告文実装する
                                Item.add("");
                                //カスタムメニュー用
                                Item.add(dialog);
                                Item.add(image_load);
                                Item.add(quick_profile);
                                Item.add(custom_emoji);
                                Item.add(gif);
                                Item.add(font);

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

    /**
     * Misskey ユーザー情報取得
     */
    private void loadMisskeyAccountName() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", misskey_username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        //作成
        Request request = new Request.Builder()
                .url("https://" + instance + "/api/users/show")
                .post(requestBody)
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
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                if (!response.isSuccessful()) {
                    //失敗時
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response_string);
                        String name = jsonObject.getString("name");
                        String username = jsonObject.getString("username");
                        account_id = jsonObject.getString("id");
                        String avatarUrl = jsonObject.getString("avatarUrl");
                        String bannerUrl = jsonObject.getString("bannerUrl");
                        if (pref_setting.getBoolean("pref_custom_emoji",true)|| Boolean.valueOf(custom_emoji)){
                            JSONArray emoji = jsonObject.getJSONArray("emojis");
                            for (int e = 0; e < emoji.length(); e++) {
                                JSONObject emoji_jsonObject = emoji.getJSONObject(e);
                                String emoji_name = emoji_jsonObject.getString("name");
                                String emoji_url = emoji_jsonObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                name = name.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                        }
                        if (getActivity() != null) {
                            String finalName = name;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //ドロワーに反映
                                    setDrawerImageText(avatarUrl, bannerUrl, finalName, "@" + username + "@" + instance);
                                    //サブタイトル更新
                                    if (subtitle.length() >= 1) {
                                        ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(subtitle);
                                    } else {
                                        ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(finalName + "( @" + username + " / " + instance + " )");
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
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
                        username = jsonObject.getString("username");
                        display_name = jsonObject.getString("display_name");
                        follow = jsonObject.getString("following_count");
                        follower = jsonObject.getString("followers_count");
                        note = jsonObject.getString("note");
                        String avatar = jsonObject.getString("avatar");
                        String header = jsonObject.getString("header");
                        account_JsonObject = jsonObject;
                        //カスタム絵文字
                        if (Boolean.valueOf(custom_emoji) || pref_setting.getBoolean("pref_custom_emoji",true)){
                            JSONArray emojis = jsonObject.getJSONArray("emojis");
                            for (int i = 0; i < emojis.length(); i++) {
                                JSONObject emojiObject = emojis.getJSONObject(i);
                                String emoji_name = emojiObject.getString("shortcode");
                                String emoji_url = emojiObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                //display_name
                                if (display_name.contains(emoji_name)) {
                                    //あったよ
                                    display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                }
                            }
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //ドロワー
                                    setDrawerImageText(avatar, header, display_name, "@" + username + "@" + instance);
                                    //サブタイトル更新
                                    if (subtitle.length() >= 1) {
                                        ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(subtitle);
                                    } else {
                                        ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(name + "( @" + id + " / " + instance + " )");
                                    }
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
                            Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
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
        //接続先設定
        String link = "";
        //通知
        boolean notification = false;
        //DM
        boolean direct = false;
        switch (getArguments().getString("content")) {
            case "/api/v1/timelines/home":
                link = "wss://" + instance + "/api/v1/streaming/?stream=user&access_token=" + access_token;
                break;
            case "/api/v1/notifications":
                notification = true;
                link = "wss://" + instance + "/api/v1/streaming/?stream=user:notification&access_token=" + access_token;
                break;
            case "/api/v1/timelines/public?local=true":
                link = "wss://" + instance + "/api/v1/streaming/?stream=public:local&access_token=" + access_token;
                break;
            case "/api/v1/timelines/public":
                link = "wss://" + instance + "/api/v1/streaming/?stream=public&access_token=" + access_token;
                break;
            case "/api/v1/timelines/direct":
                direct = true;
                link = "wss://" + instance + "/api/v1/streaming/?stream=direct&access_token=" + access_token;
                break;
        }
        if ("sdk".equals(Build.PRODUCT)) {
            // エミュレータの場合はIPv6を無効    ----1
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        }
        try {
            URI uri = new URI(link);

            //WebSocket
            boolean finalNotification = notification;
            boolean finalDirect = direct;
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("おーぷん");
                }

                @Override
                public void onMessage(String message) {
                    //JSONParse
                    try {
                        if (!finalNotification && !finalDirect) {
                            JSONObject jsonObject = new JSONObject(message);
                            //一回文字列として取得してから再度JSONObjectにする
                            String payload = jsonObject.getString("payload");
                            JSONObject toot_jsonObject = new JSONObject(payload);
                            //これでストリーミング有効・無効でもJSONパースになるので楽になる（？）
                            timelineJSONParse(toot_jsonObject, true);
                        } else if (finalNotification) {
                            JSONObject jsonObject = new JSONObject(message);
                            String payload = jsonObject.getString("payload");
                            JSONObject toot_text_jsonObject = new JSONObject(payload);
                            JSONObject toot_text_account = toot_text_jsonObject.getJSONObject("account");
                            //Type!!!!!!!!
                            String type = toot_text_jsonObject.getString("type");
                            //振り分け
                            if (fav_filter) {
                                if (type.contains("favourite")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true);
                                }
                            }
                            if (bt_filter) {
                                if (type.contains("reblog")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true);
                                }
                            }
                            if (mention_filter) {
                                if (type.contains("mention")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true);
                                }
                            }
                            if (follow_filter) {
                                if (type.contains("follow")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true);
                                }
                            }
                        } else if (finalDirect) {
                            //DM
                            JSONObject jsonObject = new JSONObject(message);
                            String payload = jsonObject.getString("payload");
                            JSONObject toot_text_jsonObject = new JSONObject(payload);
                            streamingAPIDirect(toot_text_jsonObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    //失敗時
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error) + "\n" + reason, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {

                }
            };
            //接続
            webSocketClient.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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
                            //Type!!!!!!!!
                            String type = toot_text_jsonObject.getString("type");
                            //振り分け
                            if (fav_filter) {
                                if (type.contains("favourite")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false);
                                }
                            }
                            if (bt_filter) {
                                if (type.contains("reblog")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false);
                                }
                            }
                            if (mention_filter) {
                                if (type.contains("mention")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false);
                                }
                            }
                            if (follow_filter) {
                                if (type.contains("follow")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false);
                                }
                            }
                        }
                        //最後のIDを更新する
                        JSONObject last_toot_text = jsonArray.getJSONObject(29);
                        max_id = last_toot_text.getString("id");
                        //わんちゃんJSONすべてがフィルターにかかって０件の場合があるのでそのときは２０個以上になるまで叩き続ける
                        if (adapter.getCount() < 20) {
                            loadNotification(max_id);
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

    /**
     * 通知メニューレイアウト
     */
    private void notificationLayout() {
        //追加
        //新しいLinearlayout
        LinearLayout notificationLinearLayout = new LinearLayout(getContext());
        notificationLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        notificationLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //配列で勘弁して
        Drawable icon[] = {getContext().getDrawable(R.drawable.ic_star_black_24dp),
                getContext().getDrawable(R.drawable.ic_repeat_black_24dp),
                getContext().getDrawable(R.drawable.ic_announcement_black_24dp),
                getContext().getDrawable(R.drawable.ic_person_add_black_24dp)};
        String tag[] = {"fav_filter", "bt_filter", "mention_filter", "follow_filter"};
        //背景
        String background = "ffffff";
        if (Boolean.valueOf(dark_mode)) {
            background = "000000";
        }

        for (int i = 0; i < 4; i++) {
            Switch sw = new Switch(getContext());
            sw.setCompoundDrawablesWithIntrinsicBounds(icon[i], null, null, null);
            sw.setTag(tag[i]);
            sw.setChecked(true);
            notificationLinearLayout.addView(sw);
            //切り替え
            int finalI = i;
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        notificationFilterBoolean(tag[finalI], true);
                    } else {
                        notificationFilterBoolean(tag[finalI], false);
                    }
                    //通知更新
                    adapter.clear();
                    SnackberProgress.showProgressSnackber(sw, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
                    if (misskey_mode) {
                        loadMisskeyTimeline(null, true);
                    } else {
                        loadNotification("");
                    }
                }
            });
        }


        //メインLinearLayoutに追加
        linearLayout.addView(notificationLinearLayout, 0);
    }

    /**
     * 通知で使う
     *
     * @param type fav,bt,mention,followのいずれか
     */
    private void notificationFilterBoolean(String type, boolean isChecked) {
        if (isChecked) {
            switch (type) {
                case "fav_filter":
                    fav_filter = true;
                    break;
                case "bt_filter":
                    bt_filter = true;
                    break;
                case "mention_filter":
                    mention_filter = true;
                    break;
                case "follow_filter":
                    follow_filter = true;
                    break;
            }
        } else {
            switch (type) {
                case "fav_filter":
                    fav_filter = false;
                    break;
                case "bt_filter":
                    bt_filter = false;
                    break;
                case "mention_filter":
                    mention_filter = false;
                    break;
                case "follow_filter":
                    follow_filter = false;
                    break;
            }
        }
    }

    /**
     * ホーム、ローカル、連合のJSONParse
     *
     * @param streaming ストリーミングAPIのときはtrueにしてね（一番上に追加するため）
     */
    private void timelineJSONParse(JSONObject toot_jsonObject, boolean streaming) {
        JSONObject toot_account = null;
        try {
            toot_account = toot_jsonObject.getJSONObject("account");
            String toot_text = toot_jsonObject.getString("content");
            String user = toot_account.getString("acct");
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
                //Streaming APIだとこれら取れないっぽい
                if (!streaming) {
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
            if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(custom_emoji)) {
                JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                for (int e = 0; e < emoji.length(); e++) {
                    JSONObject jsonObject = emoji.getJSONObject(e);
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

                //Pawooだとprofile_emojisない
                if (!toot_jsonObject.isNull("profile_emojis")) {
                    //アバター絵文字
                    JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                    for (int a = 0; a < avater_emoji.length(); a++) {
                        JSONObject jsonObject = avater_emoji.getJSONObject(a);
                        String emoji_name = jsonObject.getString("shortcode");
                        String emoji_url = jsonObject.getString("url");
                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
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
                Item.add("CustomMenu");
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
                //警告テキスト
                //TODO 警告文実装する
                Item.add("");
                //カスタムメニュー用
                Item.add(dialog);
                Item.add(image_load);
                Item.add(quick_profile);
                Item.add(custom_emoji);
                Item.add(gif);
                Item.add(font);

                ListItem listItem = new ListItem(Item);

                String finalToot_text = toot_text;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (streaming) {
                            adapter.insert(listItem, 0);
                            // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                            int pos = listView.getFirstVisiblePosition();
                            int top = 0;
                            if (listView.getChildCount() > 0) {
                                top = listView.getChildAt(0).getTop();
                            }
                            listView.setAdapter(adapter);
                            // 要素追加前の状態になるようセットする
                            adapter.notifyDataSetChanged();
                            //一番上なら追いかける
                            if (pos == 0) {
                                listView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listView.smoothScrollToPosition(0);
                                    }
                                });
                            } else {
                                listView.setSelectionFromTop(pos + 1, top);
                            }
                        } else {
                            adapter.add(listItem);
                            adapter.notifyDataSetChanged();
                            listView.setAdapter(adapter);
                            //くるくる終了
                            SnackberProgress.closeProgressSnackber();
                            listView.setSelectionFromTop(position, y);
                            scroll = false;
                        }
                        //カウンター
                        if (Boolean.valueOf(toot_counter)) {
                            if (count_text != null) {
                                //含んでいるか
                                if (finalToot_text.contains(count_text)) {
                                    String count_template = " : ";
                                    akeome_count++;
                                    countTextView.setText(count_text + count_template + String.valueOf(akeome_count));
                                }
                            }
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通知のJSONParse
     *
     * @param streaming ストリーミングAPIの場合はtrue
     */
    private void notificationJSONPase(JSONObject toot_text_account, JSONObject toot_text_jsonObject, String type, boolean streaming) throws JSONException {
        try {
            String user_id = toot_text_account.getString("username");

            String user_name = toot_text_account.getString("display_name");
            String toot_text_time = toot_text_jsonObject.getString("created_at");
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
                if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(custom_emoji)) {
                    JSONArray emoji = toot_text_status.getJSONArray("emojis");
                    for (int e = 0; e < emoji.length(); e++) {
                        JSONObject jsonObject = emoji.getJSONObject(e);
                        String emoji_name = jsonObject.getString("shortcode");
                        String emoji_url = jsonObject.getString("url");
                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
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

                    //アバター絵文字
                    //
                    if (!toot_text_jsonObject.isNull("profile_emojis")) {
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
                        if (streaming) {
                            adapter.insert(listItem, 0);
                            // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                            int pos = listView.getFirstVisiblePosition();
                            int top = 0;
                            if (listView.getChildCount() > 0) {
                                top = listView.getChildAt(0).getTop();
                            }
                            listView.setAdapter(adapter);
                            // 要素追加前の状態になるようセットする
                            adapter.notifyDataSetChanged();
                            //一番上なら追いかける
                            if (pos == 0) {
                                listView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listView.smoothScrollToPosition(0);
                                    }
                                });
                            } else {
                                listView.setSelectionFromTop(pos + 1, top);
                            }
                        } else {
                            adapter.add(listItem);
                            adapter.notifyDataSetChanged();
                            listView.setAdapter(adapter);
                            SnackberProgress.closeProgressSnackber();
                            listView.setSelectionFromTop(position, y);
                            scroll = false;
                        }

                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ダイレクトメッセージ（ストリーミングAPI）
     */
    private void streamingAPIDirect(JSONObject jsonObject) {
        //絵文字の配列
        ArrayList<String> emoji_shortcode = new ArrayList<>();
        ArrayList<String> emoji_url = new ArrayList<>();
        try {
            //配列
            JSONArray jsonArray = jsonObject.getJSONArray("accounts");
            JSONObject toot_JsonObject = jsonObject.getJSONObject("last_status");
            JSONObject account_JsonObject = toot_JsonObject.getJSONObject("account");
            JSONArray media_JsonArray = toot_JsonObject.getJSONArray("media_attachments");
            //emojis
            JSONArray emojis = toot_JsonObject.getJSONArray("emojis");
            JSONArray profile_emojis = toot_JsonObject.getJSONArray("profile_emojis");
            //Account
            JSONArray account_emojis = account_JsonObject.getJSONArray("emojis");
            JSONArray account_profile_emojis = account_JsonObject.getJSONArray("profile_emojis");
            //last_status
            JSONArray laststatus_emojis = jsonArray.getJSONObject(0).getJSONArray("emojis");
            JSONArray laststatus_profile_emojis = jsonArray.getJSONObject(0).getJSONArray("profile_emojis");

            String user = account_JsonObject.getString("acct");
            String user_name = account_JsonObject.getString("display_name");
            String toot_id_string = account_JsonObject.getString("id");
            String user_avater_url = account_JsonObject.getString("avatar");
            int account_id = account_JsonObject.getInt("id");

            String toot_text = toot_JsonObject.getString("content");

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
            if (!toot_JsonObject.isNull("application")) {
                JSONObject application = toot_JsonObject.getJSONObject("application");
                user_use_client = application.getString("name");
            }

            //ブーストかも
            //ブーストあったよ
            String boost_content = null;
            String boost_user_name = null;
            String boost_user = null;
            String boost_avater_url = null;
            long boost_account_id = 0;

            //かうんと
            favCount = String.valueOf(toot_JsonObject.getInt("favourites_count"));

            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
            if (japan_timeSetting) {
                //時差計算？
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                //日本用フォーマット
                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                try {
                    Date date = simpleDateFormat.parse(toot_JsonObject.getString("created_at"));
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
                toot_time = toot_JsonObject.getString("created_at");
            }

            if (!media_JsonArray.isNull(0)) {
                media_url_1 = media_JsonArray.getJSONObject(0).getString("url");
            }
            if (!media_JsonArray.isNull(1)) {
                media_url_2 = media_JsonArray.getJSONObject(1).getString("url");
            }
            if (!media_JsonArray.isNull(2)) {
                media_url_3 = media_JsonArray.getJSONObject(2).getString("url");
            }
            if (!media_JsonArray.isNull(3)) {
                media_url_4 = media_JsonArray.getJSONObject(3).getString("url");
            }


            //絵文字
            if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(custom_emoji)) {
                //絵文字のデータが色んな所（emojis,accountsのemoji,laststatusのemoji）を一旦配列に入れる
                for (int e = 0; e < emojis.length(); e++) {
                    JSONObject emoji_jsonObject = emojis.getJSONObject(e);
                    emoji_shortcode.add(emoji_jsonObject.getString("shortcode"));
                    emoji_url.add(emoji_jsonObject.getString("url"));
                }
                for (int e = 0; e < profile_emojis.length(); e++) {
                    JSONObject emoji_jsonObject = profile_emojis.getJSONObject(e);
                    emoji_shortcode.add(emoji_jsonObject.getString("shortcode"));
                    emoji_url.add(emoji_jsonObject.getString("url"));
                }
                for (int e = 0; e < account_emojis.length(); e++) {
                    JSONObject emoji_jsonObject = account_emojis.getJSONObject(e);
                    emoji_shortcode.add(emoji_jsonObject.getString("shortcode"));
                    emoji_url.add(emoji_jsonObject.getString("url"));
                }
                for (int e = 0; e < account_profile_emojis.length(); e++) {
                    JSONObject emoji_jsonObject = account_profile_emojis.getJSONObject(e);
                    emoji_shortcode.add(emoji_jsonObject.getString("shortcode"));
                    emoji_url.add(emoji_jsonObject.getString("url"));
                }
                for (int e = 0; e < laststatus_emojis.length(); e++) {
                    JSONObject emoji_jsonObject = laststatus_emojis.getJSONObject(e);
                    emoji_shortcode.add(emoji_jsonObject.getString("shortcode"));
                    emoji_url.add(emoji_jsonObject.getString("url"));
                }
                for (int e = 0; e < laststatus_profile_emojis.length(); e++) {
                    JSONObject emoji_jsonObject = laststatus_profile_emojis.getJSONObject(e);
                    emoji_shortcode.add(emoji_jsonObject.getString("shortcode"));
                    emoji_url.add(emoji_jsonObject.getString("url"));
                }

                //絵文字対応
                //配列をすべて回す
                for (int i = 0; i < emoji_shortcode.size(); i++) {
                    String name = emoji_shortcode.get(i);
                    String url = emoji_url.get(i);
                    String custom_emoji_src = "<img src=\'" + url + "\'>";
                    //一致で置き換え
                    toot_text = toot_text.replace(":" + name + ":", custom_emoji_src);
                    user_name = user_name.replace(":" + name + ":", custom_emoji_src);
                }
            }

            //カード情報
            String cardTitle = null;
            String cardURL = null;
            String cardDescription = null;
            String cardImage = null;

            if (!jsonObject.isNull("card")) {
                JSONObject cardObject = jsonObject.getJSONObject("card");
                cardURL = cardObject.getString("url");
                cardTitle = cardObject.getString("title");
                cardDescription = cardObject.getString("description");
                cardImage = cardObject.getString("image");
            }

            if (getActivity() != null && isAdded()) {

                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add("CustomMenu");
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
                //警告テキスト
                //TODO 警告文実装する
                Item.add("");
                //カスタムメニュー用
                Item.add(dialog);
                Item.add(image_load);
                Item.add(quick_profile);
                Item.add(custom_emoji);
                Item.add(gif);
                Item.add(font);

                ListItem listItem = new ListItem(Item);

                String finalToot_text = toot_text;
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
                        // 要素追加前の状態になるようセットする
                        adapter.notifyDataSetChanged();
                        //一番上なら追いかける
                        if (pos == 0) {
                            listView.post(new Runnable() {
                                @Override
                                public void run() {
                                    listView.smoothScrollToPosition(0);
                                }
                            });
                        } else {
                            listView.setSelectionFromTop(pos + 1, top);
                        }

                        //カウンター
                        if (Boolean.valueOf(toot_counter)) {
                            if (count_text != null) {
                                //含んでいるか
                                if (finalToot_text.contains(count_text)) {
                                    String count_template = " : ";
                                    akeome_count++;
                                    countTextView.setText(count_text + count_template + String.valueOf(akeome_count));
                                }
                            }
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * MisskeyAPI
     * note/timeline
     *
     * @param id           追加読み込み時に利用。<br>追加読込しない場合は<b>null</b>を入れてね
     * @param notification 通知の場合は<b>true</b>
     */
    private void loadMisskeyTimeline(String id, boolean notification) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("i", access_token);
            jsonObject.put("limit", 100);
            if (id != null) {
                jsonObject.put("untilId", id);
            }
            //TLで自分の投稿を見れるように
            if (url.contains("timeline")) {
                jsonObject.put("includeLocalRenotes", true);
                jsonObject.put("includeMyRenotes", true);
                jsonObject.put("includeRenotedMyNotes", true);
            }
            if (notification) {
                //通知フィルター機能
                JSONArray filter = new JSONArray();
                if (fav_filter) {
                    filter.put("reaction");
                }
                if (bt_filter) {
                    filter.put("renote");
                }
                if (mention_filter) {
                    filter.put("mention");
                }
                if (follow_filter) {
                    filter.put("follow");
                }
                jsonObject.put("includeTypes", filter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //System.out.println(jsonObject.toString());
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                //System.out.println(response_string);
                if (!response.isSuccessful()) {
                    //失敗時
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(response_string);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (!notification) {
                                setMisskeyTLParse(jsonObject);
                            } else {
                                setMisskeyNotification(jsonObject);
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                        //最後のIDを保存
                        JSONObject last = jsonArray.getJSONObject(99);
                        CustomMenuTimeLine.this.untilId = last.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Misskey JSON TL Parse
     *
     * @param jsonObject JSONオブジェクト
     */
    private void setMisskeyTLParse(JSONObject jsonObject) {
        try {
            JSONObject account_JsonObject = jsonObject.getJSONObject("user");
            String toot_text = "";
            toot_text = jsonObject.getString("text");
            String name = account_JsonObject.getString("name");
            String username = account_JsonObject.getString("username");
            String note_id = jsonObject.getString("id");
            String avatar = account_JsonObject.getString("avatarUrl");
            String user_Id = account_JsonObject.getString("id");
            String renote_count = jsonObject.getString("renoteCount");
            String createdAt = jsonObject.getString("createdAt");
            String host = "";
            String client = null;
            //media
            String media_1 = null;
            String media_2 = null;
            String media_3 = null;
            String media_4 = null;
            //card
            String cardTitle = null;
            String cardURL = null;
            String cardDescription = null;
            String cardImage = null;
            //renote
            String renote_content = null;
            String renote_user_name = null;
            String renote_user = null;
            String renote_avater_url = null;
            String renote_account_id = null;
            //クライアント名
            if (!jsonObject.isNull("app")) {
                client = jsonObject.getJSONObject("app").getString("name");
            }
            //host (acct的な)
            if (!account_JsonObject.isNull("host")) {
                host = account_JsonObject.getString("host");
                host = "@" + host;
            }
            //添付メディア
            if (!jsonObject.isNull("media")) {
                JSONArray media = jsonObject.getJSONArray("media");
                if (!media.isNull(0)) {
                    media_1 = media.getJSONObject(0).getString("url");
                }
                if (!media.isNull(1)) {
                    media_2 = media.getJSONObject(1).getString("url");
                }
                if (!media.isNull(2)) {
                    media_3 = media.getJSONObject(2).getString("url");
                }
                if (!media.isNull(3)) {
                    media_4 = media.getJSONObject(3).getString("url");
                }
            }

            //ブースト　ふぁぼ
            //本家Misskeyに自分がRenoteしたかどうかを取得する値が無いらしい（めいめいMisskeyにはあるっぽい）
            String isBoost = "no";
            String isFav = "";
            String boostCount = "0";
            final String[] favCount = {""};
            boostCount = jsonObject.getString("renoteCount");

            //renote (Mastodonで言うboost)
            if (!jsonObject.isNull("renote")) {
                JSONObject renote_JsonObject = jsonObject.getJSONObject("renote");
                JSONObject user_JsonObject = renote_JsonObject.getJSONObject("user");
                renote_content = renote_JsonObject.getString("text");
                renote_user_name = user_JsonObject.getString("name");
                renote_user = user_JsonObject.getString("username");
                renote_avater_url = user_JsonObject.getString("avatarUrl");
                renote_account_id = user_JsonObject.getString("id");
            }
            //自分のアクション内容
            if (!jsonObject.isNull("myReaction")) {
                isFav = jsonObject.getString("myReaction");
            }
            //MastodonでFavのところはMisskeyリアクション一覧の配列を渡す
            JSONObject reaction_Object = jsonObject.getJSONObject("reactionCounts");
            //名前を取り出す？
            reaction_Object.keys().forEachRemaining(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    try {
                        //カウントを表示
                        String index = reaction_Object.getString(s);
                        favCount[0] = favCount[0] + " " + HomeTimeLineAdapter.toReactionEmoji(s) + ":" + index + "  ";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


            //絵文字
            if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(custom_emoji)) {
                JSONArray emoji = jsonObject.getJSONArray("emojis");
                for (int e = 0; e < emoji.length(); e++) {
                    JSONObject emoji_jsonObject = emoji.getJSONObject(e);
                    String emoji_name = emoji_jsonObject.getString("name");
                    String emoji_url = emoji_jsonObject.getString("url");
                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                    toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                }

                //ユーザーネームの方の絵文字
                if (!account_JsonObject.isNull("emojis")) {
                    JSONArray account_emoji = account_JsonObject.getJSONArray("emojis");
                    for (int e = 0; e < account_emoji.length(); e++) {
                        JSONObject emoji_jsonObject = account_emoji.getJSONObject(e);
                        String emoji_name = emoji_jsonObject.getString("name");
                        String emoji_url = emoji_jsonObject.getString("url");
                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                        name = name.replace(":" + emoji_name + ":", custom_emoji_src);
                    }
                }
            }
            if (getActivity() != null && isAdded()) {
                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add("CustomMenu");
                //内容
                Item.add(toot_text);
                //ユーザー名
                Item.add(name + " @" + username + " " + host);
                //時間、クライアント名等
                Item.add("クライアント : " + client + " / " + "ID : " + note_id + " / " + getString(R.string.time) + " : " + createdAt);
                //Toot ID 文字列版
                Item.add(note_id);
                //アバターURL
                Item.add(avatar);
                //アカウントID
                Item.add(user_Id);
                //ユーザーネーム
                Item.add(host);
                //メディア
                Item.add(media_1);
                Item.add(media_2);
                Item.add(media_3);
                Item.add(media_4);
                //カード
                Item.add(cardTitle);
                Item.add(cardURL);
                Item.add(cardDescription);
                Item.add(cardImage);
                //ブースト、ふぁぼしたか・ブーストカウント・ふぁぼかうんと
                Item.add(isBoost);
                Item.add(isFav);
                Item.add(boostCount);
                Item.add(favCount[0]);
                //Reblog ブースト用
                Item.add(renote_content);
                Item.add(renote_user_name + " @" + renote_user);
                Item.add(renote_avater_url);
                Item.add(renote_account_id);
                //警告テキスト
                Item.add("");
                //カスタムメニュー用
                Item.add(dialog);
                Item.add(image_load);
                Item.add(quick_profile);
                Item.add(custom_emoji);
                Item.add(gif);
                Item.add(font);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Misskey通知
     **/
    private void setMisskeyNotification(JSONObject jsonObject) {
        try {
            JSONObject user_JsonObject = jsonObject.getJSONObject("user");
            String layout_type = "";
            String text = "";
            String note_id = "";
            String id = jsonObject.getString("id");
            String type = jsonObject.getString("type");
            String name = user_JsonObject.getString("name");
            String username = user_JsonObject.getString("username");
            String createdAt = jsonObject.getString("createdAt");
            String avatar = user_JsonObject.getString("avatarUrl");
            String account_id = user_JsonObject.getString("id");
            String host = "";
            //画像各位
            String media_1 = null;
            String media_2 = null;
            String media_3 = null;
            String media_4 = null;

            //noteがあるか確認
            if (!jsonObject.isNull("note")) {
                JSONObject note_JsonObject = jsonObject.getJSONObject("note");
                text = note_JsonObject.getString("text");
                note_id = note_JsonObject.getString("id");
                //添付メディア
                if (!note_JsonObject.isNull("media")) {
                    JSONArray media = note_JsonObject.getJSONArray("media");
                    if (!media.isNull(0)) {
                        media_1 = media.getJSONObject(0).getString("url");
                    }
                    if (!media.isNull(1)) {
                        media_2 = media.getJSONObject(1).getString("url");
                    }
                    if (!media.isNull(2)) {
                        media_3 = media.getJSONObject(2).getString("url");
                    }
                    if (!media.isNull(3)) {
                        media_4 = media.getJSONObject(3).getString("url");
                    }
                }
            }
            //リアクション取る
            String reaction = "";
            if (!jsonObject.isNull("reaction")) {
                reaction = jsonObject.getString("reaction");
                //絵文字変換
                type = HomeTimeLineAdapter.toReactionEmoji(reaction);
            }
            //card
            String cardTitle = null;
            String cardURL = null;
            String cardDescription = null;
            String cardImage = null;
            //acct
            if (!user_JsonObject.isNull("host")) {
                host = user_JsonObject.getString("host");
                host = "@" + host;
            }
            //添付メディア
            if (!jsonObject.isNull("media")) {
                JSONArray media = jsonObject.getJSONArray("media");
                if (!media.isNull(0)) {
                    media_1 = media.getJSONObject(0).getString("url");
                }
                if (!media.isNull(1)) {
                    media_2 = media.getJSONObject(1).getString("url");
                }
                if (!media.isNull(2)) {
                    media_3 = media.getJSONObject(2).getString("url");
                }
                if (!media.isNull(3)) {
                    media_4 = media.getJSONObject(3).getString("url");
                }
            }

            if (getActivity() != null && isAdded()) {

                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add(layout_type);
                //内容
                Item.add(text);
                //ユーザー名
                Item.add(name + " @" + username + host + " / " + type);
                //時間、クライアント名等
                Item.add("ID : " + note_id + " / " + getString(R.string.time) + " : " + createdAt);
                //Toot ID 文字列版
                Item.add(note_id);
                //アバターURL
                Item.add(avatar);
                //アカウントID
                Item.add(account_id);
                //ユーザーネーム
                Item.add(username + host);
                //メディア
                Item.add(media_1);
                Item.add(media_2);
                Item.add(media_3);
                Item.add(media_4);
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
                        //くるくる終了
                        SnackberProgress.closeProgressSnackber();
                        listView.setSelectionFromTop(position, y);
                        scroll = false;
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * ストリーミングAPI版通知Parse
     */
    private void streamingNotificationParse(Notification notification) {
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
        if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(custom_emoji)) {

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
                    //loadNotification(max_id);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (shutdownable != null) {
            shutdownable.shutdown();
        }
        if (webSocketClient != null) {
            //終了
            webSocketClient.close();
        }
        //OLEDとかかかわらず戻す
        getActivity().setTheme(R.style.AppTheme);
        ((Home) getActivity()).getToolBer().setBackgroundColor(Color.parseColor("#2196f3"));
    }

    /**
     * replaceしたときに最後に呼ばれるところ
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * トゥートカウンターようれいあうと
     */
    private void setTootCounterLayout() {
        //カウンターようレイアウト
        LinearLayout.LayoutParams LayoutlayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout countLinearLayout = new LinearLayout(getContext());
        countLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        countLinearLayout.setLayoutParams(LayoutlayoutParams);
        linearLayout.addView(countLinearLayout, 0);
        //いろいろ
        EditText countEditText = new EditText(getContext());
        Button countButton = new Button(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        countTextView.setLayoutParams(layoutParams);
        countEditText.setLayoutParams(layoutParams);
        countButton.setBackground(getContext().getDrawable(R.drawable.button_style_white));
        countButton.setText(">");
        countEditText.setHint(getString(R.string.toot_count_hint));
        //背景
        String background = "ffffff";
        if (Boolean.valueOf(dark_mode)) {
            background = "000000";
        }

        countLinearLayout.addView(countEditText);
        countLinearLayout.addView(countButton);
        countLinearLayout.addView(countTextView);

        //テキストを決定
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                countButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        count_text = countEditText.getText().toString();
                        akeome_count = 0;
                        countTextView.setText(count_text + " : " + String.valueOf(akeome_count));
                    }
                });
                //長押しでコピー
                countTextView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        assert clipboardManager != null;
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("", String.valueOf(akeome_count)));
                        Toast.makeText(getContext(), R.string.copy, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });
    }

    /**
     * 片手モード
     */
    private void one_hand_mode() {
        LinearLayout one_hand_LinearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams one_hand_layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        one_hand_LinearLayout.setOrientation(LinearLayout.VERTICAL);
        one_hand_layoutParams.weight = 1;
        one_hand_LinearLayout.setLayoutParams(one_hand_layoutParams);
        one_hand_LinearLayout.setGravity(Gravity.CENTER);
        //使いみち誰か（）
        //TL領域を広げるとかする
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setTextSize(18);
        textView.setText(getString(R.string.custom_menu_tl_up));
        //領域広げる
        one_hand_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getText().toString().contains(getString(R.string.custom_menu_tl_up))) {
                    textView.setText(getString(R.string.custom_menu_tl_down));
                    one_hand_layoutParams.weight = 2;
                } else {
                    textView.setText(getString(R.string.custom_menu_tl_up));
                    one_hand_layoutParams.weight = 1;
                }
            }
        });
        //タイトルも
        TextView title_TextView = new TextView(getContext());
        title_TextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        title_TextView.setText(getArguments().getString("name"));
        title_TextView.setTextSize(24);
        //追加
        one_hand_LinearLayout.addView(title_TextView);
        one_hand_LinearLayout.addView(textView);
        //ダークモード対応
        if (dark_theme) {
            title_TextView.setTextColor(Color.parseColor("#ffffff"));
            textView.setTextColor(Color.parseColor("#ffffff"));
            one_hand_LinearLayout.setBackgroundColor(Color.parseColor("#000000"));
        }
        //半分
        parent_linearlayout.addView(one_hand_LinearLayout, 0);
    }

    /**
     * ドロワーの画像、文字を変更する
     */
    private void setDrawerImageText(String avatarUrl, String headerUri, String display_name, String username) {
        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        //一応Nullチェック
        if (header_imageView != null) {
            //画像読み込むか
            if (pref_setting.getBoolean("pref_drawer_avater", false)) {
                //読み込まない
                avater_imageView.setImageResource(R.drawable.ic_person_black_24dp);
                header_imageView.setBackgroundColor(Color.parseColor("#c8c8c8"));
            }
            //Wi-Fi時は読み込む
            if (pref_setting.getBoolean("pref_avater_wifi", true)) {
                //既定でGIFは再生しない方向で
                if (pref_setting.getBoolean("pref_avater_gif", true)) {
                    //GIFアニメ再生させない
                    Picasso.get().load(avatarUrl).resize(100, 100).into(avater_imageView);
                    Picasso.get().load(headerUri).into(header_imageView);
                } else {
                    //GIFアニメを再生
                    Glide.with(getContext()).load(avatarUrl).apply(new RequestOptions().override(100, 100)).into(avater_imageView);
                    Glide.with(getContext()).load(headerUri).into(header_imageView);
                }
            } else {
                avater_imageView.setImageResource(R.drawable.ic_person_black_24dp);
                header_imageView.setBackgroundColor(Color.parseColor("#c8c8c8"));
            }
            //UserName
            PicassoImageGetter imageGetter = new PicassoImageGetter(user_account_textView);
            user_account_textView.setText(Html.fromHtml(display_name, Html.FROM_HTML_MODE_LEGACY, imageGetter, null));
            user_id_textView.setText(username);

        }
    }


    /**
     * フォント設定
     */
    public static Typeface getFont_Typeface() {
        return font_Typeface;
    }

    /**
     * Misskeyモードかどうか
     */
    public static boolean isMisskeyMode() {
        return misskey_mode;
    }

    /**
     * CustomMenu利用中かどうかを返す
     */
    public static boolean isUseCustomMenu() {
        return true;
    }

    /**
     * Instance
     */
    public static String getInstance() {
        return instance;
    }

    public static String getAccess_token() {
        return access_token;
    }

    public static String getUsername() {
        return misskey_username;
    }

    public static String getAccount_id() {
        return account_id;
    }
}
