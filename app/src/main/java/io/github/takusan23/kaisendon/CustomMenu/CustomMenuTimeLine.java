package io.github.takusan23.kaisendon.CustomMenu;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.github.takusan23.kaisendon.APIJSONParse.CustomMenuJSONParse;
import io.github.takusan23.kaisendon.APIJSONParse.MastodonTLAPIJSONParse;
import io.github.takusan23.kaisendon.CustomMenu.Dialog.TLQuickSettingSnackber;
import io.github.takusan23.kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.kaisendon.DesktopTL.DesktopFragment;
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
    private static String url;
    private String instance;
    private String access_token;
    private String json_data;
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
    private RecyclerView recyclerView;
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
    private boolean vote_filter = true;

    private MastodonClient client;

    //トゥートカウンター
    private int akeome_count;
    private String count_text;
    private TextView countTextView;

    private CustomMenuSQLiteHelper helper = null;
    private SQLiteDatabase db = null;


    //WebSocket
    private WebSocketClient webSocketClient;
    private WebSocketClient notification_WebSocketClient;

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

    //RecyclerView
    private ArrayList<ArrayList> recyclerViewList;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    //通知
    private Vibrator vibrator;
    //時間指定投稿待ち一覧モード
    private boolean isScheduled_statuses = false;
    private CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter;
    //フォロー推奨ユーザー表示モード
    private boolean isFollowSuggestions = false;

    //時間
    private SimpleDateFormat simpleDateFormat;
    private SimpleDateFormat japanDateFormat;
    private Calendar calendar;
    private String desktop_url = "";

    private String no_fav_icon = "";
    private String yes_fav_icon = "";

    private DarkModeSupport darkModeSupport;
    //isDesktopMode
    private boolean isDesktopMode = false;
    //TTS
    private TextToSpeech tts;
    //クイック設定
    private TLQuickSettingSnackber tlQuickSettingSnackber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //ダークモードを有効にするか
        return inflater.inflate(R.layout.fragment_custom_menu_time_line, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        parent_linearlayout = view.findViewById(R.id.custom_menu_parent_linearlayout);
        linearLayout = view.findViewById(R.id.custom_menu_fragment_linearlayout);
        recyclerView = view.findViewById(R.id.custom_menu_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.custom_menu_swipe_refresh);
        imageView = view.findViewById(R.id.custom_tl_background_imageview);

        //データ受け取り
        misskey = getArguments().getString("misskey");
        misskey_mode = Boolean.valueOf(misskey);
        url = getArguments().getString("content");
        instance = getArguments().getString("instance");
        access_token = getArguments().getString("access_token");
        json_data = getArguments().getString("json");
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
        no_fav_icon = getArguments().getString("no_fav_icon");
        yes_fav_icon = getArguments().getString("yes_fav_icon");

        //Navication Drawer
        if (getActivity() != null) {
            NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
            if (navigationView != null) {
                //どろわーのイメージとか文字とか
                View navHeaderView = navigationView.getHeaderView(0);
                avater_imageView = navHeaderView.findViewById(R.id.icon_image);
                header_imageView = navHeaderView.findViewById(R.id.drawer_header);
                user_account_textView = navHeaderView.findViewById(R.id.drawer_account);
                user_id_textView = navHeaderView.findViewById(R.id.drawer_id);
                avater_imageView.setImageTintList(null);
                header_imageView.setImageTintList(null);
            }
        }
        //インスタンス、アクセストークン変更
        //Misskeyは設定しないように
        //デスクトップモード時も設定しないように
        if (getActivity().getSupportFragmentManager().findFragmentById(R.id.container_container) instanceof DesktopFragment) {
            //タイトル
            isDesktopMode = true;
            ((AppCompatActivity) getContext()).setTitle(getString(R.string.desktop_mode));
        } else {
            //タイトル
            ((AppCompatActivity) getContext()).setTitle(getArguments().getString("name"));
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

        //OLED
        //ダークモード処理
        Configuration conf = getResources().getConfiguration();
        int currecntNightMode = conf.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        darkModeSupport = new DarkModeSupport(getContext());
        currecntNightMode = darkModeSupport.setIsDarkModeSelf(currecntNightMode);
        switch (currecntNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                linearLayout.setBackgroundColor(Color.parseColor("#" + background_transparency + "ffffff"));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                linearLayout.setBackgroundColor(Color.parseColor("#" + background_transparency + "000000"));
                break;
        }

/*
        //OLED
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            linearLayout.setBackgroundColor(Color.parseColor("#" + background_transparency + "000000"));
        }else{
            linearLayout.setBackgroundColor(Color.parseColor("#" + background_transparency + "ffffff"));
        }
*/


/*
        //OLEDは背景を黒にする
        if (Boolean.valueOf(getArguments().getString("dark_mode"))) {
            linearLayout.setBackgroundColor(Color.parseColor("#" + background_transparency + "000000"));
            //((Home) getActivity()).getToolBer().setBackgroundColor(Color.parseColor("#000000"));
            dark_theme = true;
        } else {
            //黒にしなくていい
            linearLayout.setBackgroundColor(Color.parseColor("#" + background_transparency + "ffffff"));
            //てーま
            //((Home) getActivity()).getToolBer().setBackgroundColor(Color.parseColor("#2196f3"));
        }
*/

        //最終的なURL
        url = "https://" + instance + url;

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
            try {
                ((Home) getActivity()).getToolBer().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //これ一番上に移動するやつ
                        recyclerView.smoothScrollToPosition(0);
                    }
                });
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        //TLQuickSettings
        if (getActivity() instanceof Home){
            tlQuickSettingSnackber = ((Home) getActivity()).getTlQuickSettingSnackber();
        }

        //トゥートカウンター
        if (Boolean.valueOf(toot_counter)) {
            setTootCounterLayout();
        }

        //予約投稿（時間指定投稿）待ち一覧モードかを判断する
        if (url.contains("/api/v1/scheduled_statuses")) {
            isScheduled_statuses = true;
        }
        //フォロー推奨ユーザー読み込みモード
        if (url.contains("/api/v1/suggestions")) {
            isFollowSuggestions = true;
        }


        recyclerViewList = new ArrayList<>();
        //ここから下三行必須
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
        recyclerView.setAdapter(customMenuRecyclerViewAdapter);
        recyclerViewLayoutManager = recyclerView.getLayoutManager();


        addNavigationOpen();

        //TL読み込み
        //APIがTL取得のみに
        //TL と Favourite List
        if (!isScheduled_statuses && !isFollowSuggestions) {
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
                        recyclerViewList.clear();
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
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        //Favのときはしない
                        if (recyclerViewLayoutManager != null) {
                            int firstVisibleItem = ((LinearLayoutManager) recyclerViewLayoutManager).findFirstVisibleItemPosition();
                            int visibleItemCount = ((LinearLayoutManager) recyclerViewLayoutManager).getChildCount();
                            int totalItemCount = ((LinearLayoutManager) recyclerViewLayoutManager).getItemCount();
                            //最後までスクロールしたときの処理
                            if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                                position = ((LinearLayoutManager) recyclerViewLayoutManager).findFirstVisibleItemPosition();
                                y = recyclerView.getChildAt(0).getTop();
                                if (recyclerViewList.size() >= 20) {
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
                        recyclerViewList.clear();
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
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (!url.contains("/api/v1/favourites")) {
                            int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                            int visibleItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getChildCount();
                            int totalItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getItemCount();
                            //最後までスクロールしたときの処理
                            if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                                position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                                y = recyclerView.getChildAt(0).getTop();
                                if (recyclerViewList.size() >= 20) {
                                    SnackberProgress.showProgressSnackber(view, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
                                    scroll = true;
                                    //通知以外
                                    if (!CustomMenuTimeLine.this.getArguments().getString("content").contains("/api/v1/notifications")) {
                                        //普通にAPI叩く
                                        loadTimeline(max_id);
                                    } else {
                                        loadNotification(max_id);
                                    }
                                }
                            }
                        }
                    }
                });

                //通知
                if (pref_setting.getBoolean("pref_notification_toast", true)) {
                    setStreamingNotification();
                }
            }
        } else if (isScheduled_statuses) {
            //予約リスト
            //引っ張って更新無効
            swipeRefreshLayout.setEnabled(false);
            //アカウント情報
            loadAccountName();
            //時間指定待ち一覧を読み込む
            loadScheduled_statuses(view);
        } else if (isFollowSuggestions) {
            //フォロー一覧
            //引っ張って更新無効
            swipeRefreshLayout.setEnabled(false);
            //アカウント情報
            loadAccountName();
            //フォロー推奨ユーザーを読み込む
            loadFollowSuggestions(view);
        }
    }

    /**
     * 変数 : url
     * これCustomMenuTimeLine単体だと動くけどDesktopModeだとおかしくなるのでこのメゾット使って
     */
    private String getDesktopModeURL() {
        //最終的なURL(static使いまくったらDesktopMode実装で困った（）
        return "https://" + getArguments().getString("instance") + getArguments().getString("content");
    }


    /**
     * タイムラインを読み込む
     * 通知はこれでは読み込めない
     *
     * @param max_id_id 追加読み込み。無いときは""でも
     */
    private void loadTimeline(String max_id_id) {
        //パラメータを設定
        //最終的なURL(static使いまくったらDesktopMode実装で困った（）
        url = getDesktopModeURL();
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("limit", "40");
        builder.addQueryParameter("access_token", getArguments().getString("access_token"));
        if (max_id_id != null) {
            if (max_id_id.length() != 0) {
                builder.addQueryParameter("max_id", max_id_id);
            }
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
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
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
                            if (getActivity() != null && isAdded()) {
                                //配列を作成
                                ArrayList<String> Item = new ArrayList<>();
                                //メモとか通知とかに
                                Item.add("CustomMenu Local");
                                //内容
                                Item.add(url);
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
                                Item.add(instance);
                                Item.add(access_token);
                                //設定ファイルJSON
                                Item.add(json_data);

                                //ListItem listItem = new ListItem(Item);
                                recyclerViewList.add(Item);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (recyclerViewLayoutManager != null) {
                                            ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                                        }
                                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                                        recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                                        SnackberProgress.closeProgressSnackber();
                                        scroll = false;

/*
                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                        listView.setAdapter(adapter);
                                        //くるくる終了
*/
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
                            Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
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
                        String title_name = jsonObject.getString("name");
                        String username = jsonObject.getString("username");
                        account_id = jsonObject.getString("id");
                        String avatarUrl = jsonObject.getString("avatarUrl");
                        String bannerUrl = jsonObject.getString("bannerUrl");
                        if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(custom_emoji)) {
                            JSONArray emoji = jsonObject.getJSONArray("emojis");
                            for (int e = 0; e < emoji.length(); e++) {
                                JSONObject emoji_jsonObject = emoji.getJSONObject(e);
                                String emoji_name = emoji_jsonObject.getString("name");
                                String emoji_url = emoji_jsonObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                name = name.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                        }
                        String finalName = name;

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //ドロワーに反映
                                    setDrawerImageText(avatarUrl, bannerUrl, finalName, "@" + username + "@" + instance);
                                    //サブタイトル更新
                                    if (subtitle.length() >= 1) {
                                        ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(subtitle);
                                    } else {
                                        ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(title_name + "( @" + username + " / " + instance + " )");
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
                        if (Boolean.valueOf(custom_emoji) || pref_setting.getBoolean("pref_custom_emoji", true)) {
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
                                    if (isDesktopMode) {
                                        ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle("");
                                    } else {
                                        if (getContext() != null && ((AppCompatActivity) getContext()).getSupportActionBar() != null) {
                                            if (subtitle.length() >= 1) {
                                                ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(subtitle);
                                            } else {
                                                ((AppCompatActivity) getContext()).getSupportActionBar().setSubtitle(name + "( @" + id + " / " + instance + " )");
                                            }
                                        }
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
                            String type = jsonObject.getString("event");
                            //通知はParseしない
                            if (!type.contains("notification")) {
                                timelineJSONParse(toot_jsonObject, true);
                            }
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
                            if (vote_filter) {
                                if (type.contains("poll")) {
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
                            if (isAdded()) {
                                Toast.makeText(getContext(), getString(R.string.error) + "\n" + reason, Toast.LENGTH_SHORT).show();
                            }
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
        HttpUrl.Builder builder = HttpUrl.parse(getDesktopModeURL()).newBuilder();
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
                if (getContext() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
                            if (vote_filter) {
                                if (type.contains("poll")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false);
                                }
                            }
                        }
                        //最後のIDを更新する
                        JSONObject last_toot_text = jsonArray.getJSONObject(29);
                        max_id = last_toot_text.getString("id");
                        //わんちゃんJSONすべてがフィルターにかかって０件の場合があるのでそのときは２０個以上になるまで叩き続ける
                        if (adapter.getCount() < 20) {
                            //loadNotification(max_id);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    //失敗時
                    if (getContext() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * 通知メニューレイアウト
     */
    private void notificationLayout() {
        if (getContext() != null) {
            //追加
            //新しいLinearlayout
            LinearLayout notificationLinearLayout = new LinearLayout(getContext());
            notificationLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            notificationLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //配列で勘弁して
            Drawable icon[] = {getContext().getDrawable(R.drawable.ic_star_black_24dp),
                    getContext().getDrawable(R.drawable.ic_repeat_black_24dp),
                    getContext().getDrawable(R.drawable.ic_announcement_black_24dp),
                    getContext().getDrawable(R.drawable.ic_person_add_black_24dp),
                    getContext().getDrawable(R.drawable.ic_move_to_inbox_black_24dp)
            };
            String tag[] = {"fav_filter", "bt_filter", "mention_filter", "follow_filter", "vote_filter"};
            //背景
            String background = "ffffff";
            if (Boolean.valueOf(dark_mode)) {
                background = "000000";
            }

            for (int i = 0; i < 5; i++) {
                Switch sw = new Switch(getContext());
                darkModeSupport.setSwitchThemeColor(sw);
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
                        recyclerViewList.clear();
                        SnackberProgress.showProgressSnackber(sw, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
                        if (Boolean.valueOf(misskey)) {
                            loadMisskeyTimeline(null, true);
                        } else {
                            loadNotification("");
                        }
                    }
                });
            }
            //ついか
            linearLayout.addView(notificationLinearLayout, 0);
        }
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
                case "vote_filter":
                    vote_filter = true;
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
                case "vote_filter":
                    vote_filter = false;
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
        if (getActivity() != null && isAdded()) {

            //配列を作成
            ArrayList<String> Item = new ArrayList<>();
            //メモとか通知とかに
            Item.add("CustomMenu");
            //内容
            Item.add(url);
            //ユーザー名
            Item.add("");
            //時間、クライアント名等
            Item.add(toot_jsonObject.toString());
            //ぶーすとした？
            Item.add("false");
            //ふぁぼした？
            Item.add("false");
            //Mastodon / Misskey
            Item.add("Mastodon");
            //Insatnce/AccessToken
            Item.add(instance);
            Item.add(access_token);
            //設定ファイルJSON
            Item.add(json_data);

            if (streaming) {
                recyclerViewList.add(0, Item);
            } else {
                recyclerViewList.add(Item);
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //カウンター
                    if (Boolean.valueOf(toot_counter)) {
                        if (count_text != null) {
                            //含んでいるか
                            try {
                                if (toot_jsonObject.getString("content").contains(count_text)) {
                                    String count_template = " : ";
                                    akeome_count++;
                                    countTextView.setText(count_text + count_template + String.valueOf(akeome_count));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (((LinearLayoutManager) recyclerViewLayoutManager) != null) {
                        // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                        int pos = ((LinearLayoutManager) recyclerViewLayoutManager).findFirstVisibleItemPosition();
                        int top = 0;
                        if (((LinearLayoutManager) recyclerViewLayoutManager).getChildCount() > 0) {
                            top = ((LinearLayoutManager) recyclerViewLayoutManager).getChildAt(0).getTop();
                        }
                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                        if (streaming) {
                            //一番上にアイテムが追加されたことを通知する？
                            //notifyDataSetChanged()と違って追加時にアニメーションされる
                            customMenuRecyclerViewAdapter.notifyItemInserted(0);
                        } else {
                            customMenuRecyclerViewAdapter.notifyDataSetChanged();
                        }
                        //一番上なら追いかける
                        if (pos == 0) {
                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    //scrollToPosition()に置き換えた。アニメーションされるようになった
                                    recyclerView.scrollToPosition(0);
                                }
                            });
                        } else {
                            ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(pos + 1, top);
                        }
                    }
                    /*TTS*/
                    if (tlQuickSettingSnackber != null && tlQuickSettingSnackber.getTimelineTTS()) {
                        //インスタンス生成
                        if (tts == null) {
                            tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int i) {
                                    //初期化
                                    if (i == TextToSpeech.SUCCESS) {
                                        Toast.makeText(getContext(), getString(R.string.text_to_speech_preparation), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            tts.setSpeechRate(Float.valueOf(pref_setting.getString("pref_speech_rate", "1.0f")));
                            CustomMenuJSONParse setting = new CustomMenuJSONParse(json_data);
                            MastodonTLAPIJSONParse api = new MastodonTLAPIJSONParse(getContext(), toot_jsonObject.toString(), setting);
                            //正規表現でURL消す
                            String text = Html.fromHtml(api.getToot_text(), Html.FROM_HTML_MODE_COMPACT).toString();
                            if (pref_setting.getBoolean("pref_speech_url", true)) {
                                text = text.replaceAll("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", "URL省略");
                            }
                            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "tts");
                        }
                    } else {
                        if (tts != null) {
                            tts.stop();
                            tts.shutdown();
                        }
                    }
                }
            });
        }
    }


    /**
     * 通知のJSONParse
     *
     * @param streaming ストリーミングAPIの場合はtrue
     */
    private void notificationJSONPase(JSONObject toot_text_account, JSONObject toot_text_jsonObject, String type, boolean streaming) {

        if (getActivity() != null && isAdded()) {

            //配列を作成
            ArrayList<String> Item = new ArrayList<>();
            //メモとか通知とかに
            Item.add(url);
            //内容
            Item.add("");
            //ユーザー名
            Item.add("");
            //時間、クライアント名等
            Item.add(toot_text_jsonObject.toString());
            //ぶーすとした？
            Item.add("false");
            //ふぁぼした？
            Item.add("false");
            //Mastodon / Misskey
            Item.add("Mastodon");
            //Insatnce/AccessToken
            Item.add(instance);
            Item.add(access_token);
            //設定ファイルJSON
            Item.add(json_data);

            if (streaming) {
                recyclerViewList.add(0, Item);
            } else {
                recyclerViewList.add(Item);
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (recyclerViewLayoutManager != null) {
                        ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                    SnackberProgress.closeProgressSnackber();
                    scroll = false;

/*
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
*/

                }
            });
        }
    }

    /**
     * ダイレクトメッセージ（ストリーミングAPI）
     */
    private void streamingAPIDirect(JSONObject jsonObject) {

        if (getActivity() != null && isAdded()) {

            //配列を作成
            ArrayList<String> Item = new ArrayList<>();
            //メモとか通知とかに
            Item.add("CustomMenu");
            //内容
            Item.add(url);
            //ユーザー名
            Item.add("");
            //時間、クライアント名等
            Item.add(jsonObject.toString());
            //ぶーすとした？
            Item.add("false");
            //ふぁぼした？
            Item.add("false");
            //Mastodon / Misskey
            Item.add("Mastodon");
            //Insatnce/AccessToken
            Item.add(instance);
            Item.add(access_token);
            //設定ファイルJSON
            Item.add(json_data);

            recyclerViewList.add(0, Item);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (recyclerViewLayoutManager != null) {
                        ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    recyclerView.setAdapter(customMenuRecyclerViewAdapter);
/*
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
*/
                }
            });
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
                if (vote_filter) {
                    filter.put("poll_vote");
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
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
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
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
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
        //AppCompatActivity activity = (AppCompatActivity)getContext();

        if (getActivity() != null && isAdded()) {
            //配列を作成
            ArrayList<String> Item = new ArrayList<>();
            //メモとか通知とかに
            Item.add("CustomMenu");
            //内容
            Item.add(url);
            //ユーザー名
            Item.add("");
            //時間、クライアント名等
            Item.add(jsonObject.toString());
            //ぶーすとした？
            Item.add("false");
            //ふぁぼした？
            Item.add("");
            //Mastodon / Misskey
            Item.add("Misskey");
            //Insatnce/AccessToken
            Item.add(instance);
            Item.add(access_token);
            //設定ファイルJSON
            Item.add(json_data);

            recyclerViewList.add(Item);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (recyclerViewLayoutManager != null) {
                        ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                    SnackberProgress.closeProgressSnackber();
                    scroll = false;
/*
                    adapter.add(listItem);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                    //くるくる終了
                    SnackberProgress.closeProgressSnackber();
                    listView.setSelectionFromTop(position, y);
                    scroll = false;
*/
                }
            });
        }
    }

    /**
     * Misskey通知
     **/
    private void setMisskeyNotification(JSONObject jsonObject) {
        if (getActivity() != null && isAdded()) {

            //配列を作成
            ArrayList<String> Item = new ArrayList<>();
            //メモとか通知とかに
            Item.add(url);
            //内容
            Item.add("");
            //ユーザー名
            Item.add("");
            //時間、クライアント名等
            Item.add(jsonObject.toString());
            //ぶーすとした？
            Item.add("false");
            //ふぁぼした？
            Item.add("");
            //Mastodon / Misskey
            Item.add("Misskey");
            //Insatnce/AccessToken
            Item.add(instance);
            Item.add(access_token);
            //設定ファイルJSON
            Item.add(json_data);

            recyclerViewList.add(Item);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (recyclerViewLayoutManager != null) {
                        ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                    SnackberProgress.closeProgressSnackber();
                    scroll = false;
/*
                    adapter.add(listItem);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                    //くるくる終了
                    SnackberProgress.closeProgressSnackber();
                    listView.setSelectionFromTop(position, y);
                    scroll = false;
*/
                }
            });
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

        //時間フォーマット
        toot_text_time = getCreatedAtFormat(toot_text_time);

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
            recyclerViewList.add(0, Item);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (recyclerViewLayoutManager != null) {
                        ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    recyclerView.setAdapter(customMenuRecyclerViewAdapter);
/*
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
*/
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
        if (notification_WebSocketClient != null) {
            notification_WebSocketClient.close();
        }
        if (tts != null) {
            tts.shutdown();
        }
        //OLEDとかかかわらず戻す
        //getActivity().setTheme(R.style.AppTheme);
        //((Home) getActivity()).getToolBer().setBackgroundColor(Color.parseColor("#2196f3"));
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
        if (getContext() != null && user_account_textView != null) {
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
    }

    /**
     * 時刻をフォーマットして返す
     */
    private String getCreatedAtFormat(String createdAt) {
        //フォーマットを規定の設定にする？
        //ここtrueにした
        if (pref_setting.getBoolean("pref_custom_time_format", true)) {
            //時差計算？
            if (simpleDateFormat == null && japanDateFormat == null && calendar == null) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                //日本用フォーマット
                japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"));
                japanDateFormat.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
                calendar = Calendar.getInstance();
            }
            try {
                Date date = simpleDateFormat.parse(createdAt);
                calendar.setTime(date);
                //タイムゾーンを設定
                //calendar.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                createdAt = japanDateFormat.format(calendar.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return createdAt;
    }

    /**
     * 通知（どん
     * *
     */
    private void setStreamingNotification() {
        String url = "wss://" + instance + "/api/v1/streaming/?stream=user&access_token=" + access_token;
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        try {
            notification_WebSocketClient = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {

                }

                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        //if (jsonObject.getString("type").equals("notification")) {
                        String object = jsonObject.getString("payload");
                        JSONObject payload_JsonObject = new JSONObject(object);
                        String type = payload_JsonObject.getString("type");
                        JSONObject account = payload_JsonObject.getJSONObject("account");
                        String display_name = account.getString("display_name");
                        String acct = account.getString("acct");
                        //カスタム絵文字
                        if (Boolean.valueOf(custom_emoji)) {
                            JSONArray emojis = account.getJSONArray("emojis");
                            for (int e = 0; e < emojis.length(); e++) {
                                JSONObject emoji = emojis.getJSONObject(e);
                                String emoji_name = emoji.getString("shortcode");
                                String emoji_url = emoji.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                        }
                        //トースト出す
                        String finalDisplay_name = display_name;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //カスタムトースト
                                Toast toast = new Toast(getContext());
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.notification_toast_layout, null);
                                TextView toast_text = layout.findViewById(R.id.notification_text);
                                PicassoImageGetter picassoImageGetter = new PicassoImageGetter(toast_text);
                                toast_text.setText(Html.fromHtml(CustomMenuRecyclerViewAdapter.toNotificationType(getContext(), type) + "<br>" + finalDisplay_name, Html.FROM_HTML_MODE_COMPACT, picassoImageGetter, null));
                                AppCompatImageView toast_imageview = layout.findViewById(R.id.notification_icon);
                                toast_imageview.setImageDrawable(getNotificationIcon(type));
                                toast.setView(layout);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.show();
                                if (pref_setting.getBoolean("pref_notification_vibrate", true)) {
                                    long[] pattern = {100, 100, 100, 100};
                                    vibrator.vibrate(pattern, -1);
                                }
                            }
                        });
                        // }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                }

                @Override
                public void onError(Exception ex) {

                }
            };
            //接続
            notification_WebSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 時間指定投稿（予約投稿）一覧読み込み
     **/
    private void loadScheduled_statuses(View view) {
        //作成
        String url = this.url + "?access_token=" + access_token;
        SnackberProgress.showProgressSnackber(view, view.getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
        Request request = new Request.Builder()
                .url(url)
                .get()
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
                        //無いとき
                        if (jsonArray.length() == 0) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SnackberProgress.closeProgressSnackber();
                                    Toast.makeText(getContext(), getString(R.string.not_fount_time_post), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                                if (getActivity() != null && isAdded()) {
                                    //配列を作成
                                    ArrayList<String> Item = new ArrayList<>();
                                    //メモとか通知とかに
                                    Item.add("CustomMenu 時間指定投稿");
                                    //内容
                                    Item.add(url);
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
                                    Item.add(instance);
                                    Item.add(access_token);
                                    //設定ファイルJSON
                                    Item.add(json_data);

                                    recyclerViewList.add(Item);
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (recyclerViewLayoutManager != null) {
                                                ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                                            }
                                            //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                                            recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                                            SnackberProgress.closeProgressSnackber();
                                        }
                                    });
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * お気に入り一覧を取得
     */
    private void loadFollowSuggestions(View view) {
        //作成
        String url = this.url + "?access_token=" + access_token;
        SnackberProgress.showProgressSnackber(view, view.getContext(), getString(R.string.loading) + "\n" + getArguments().getString("content"));
        Request request = new Request.Builder()
                .url(url)
                .get()
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
                            JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                            if (getActivity() != null && isAdded()) {
                                //配列を作成
                                ArrayList<String> Item = new ArrayList<>();
                                //メモとか通知とかに
                                Item.add("CustomMenu フォロー推奨");
                                //内容
                                Item.add(url);
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
                                Item.add(instance);
                                Item.add(access_token);
                                //設定ファイルJSON
                                Item.add(json_data);

                                recyclerViewList.add(Item);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (recyclerViewLayoutManager != null) {
                                            ((LinearLayoutManager) recyclerViewLayoutManager).scrollToPositionWithOffset(position, y);
                                        }
                                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                                        SnackberProgress.closeProgressSnackber();
                                    }
                                });
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * 通知アイコン
     */
    private Drawable getNotificationIcon(String type) {
        Drawable drawable = getContext().getDrawable(R.drawable.ic_notifications_black_24dp);
        switch (type) {
            case "follow":
                drawable = getContext().getDrawable(R.drawable.ic_person_add_black_24dp);
                break;
            case "favourite":
                drawable = getContext().getDrawable(R.drawable.ic_star_black_24dp);
                break;
            case "reblog":
                drawable = getContext().getDrawable(R.drawable.ic_repeat_black_24dp);
                break;
            case "mention":
                drawable = getContext().getDrawable(R.drawable.ic_announcement_black_24dp);
                break;
            case "reaction":
                drawable = getContext().getDrawable(R.drawable.ic_audiotrack_black_24dp);
                break;
        }
        return drawable;
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
     * 通知かどうか
     */
    public static boolean isNotification() {
        boolean mode = false;
        if (url.contains("/api/v1/notifications")) {
            mode = true;
        }
        return mode;
    }


    public static String getAccount_id() {
        return account_id;
    }

    public static String getUrl() {
        return url;
    }


    /**
     * Android 10の新しいジェスチャーで戻るジェスチャーとドロワー開くジェスチャーをかぶらないようにする
     * 端からスワイプ以外でも動作するようにする
     */
    private void addNavigationOpen() {
        //すたーと
        final float[] start = {0};
        final float[] end = {0};
        final float[] y_start = {0};
        final float[] y_end = {0};
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        start[0] = event.getX();
                        y_start[0] = event.getY();
                        //System.out.println("start : " + y_start[0]);
                        break;
                    case MotionEvent.ACTION_UP:
                        end[0] = event.getX();
                        y_end[0] = event.getY();
                        //System.out.println("end : " + y_end[0]);
                        //System.out.println("final : " + (y_start[0] - y_end[0]));
                        //両方揃ったら比較開始
                        if (start[0] != end[0]) {
                            //なんとなく400以上の誤差がないとうごかないように　と　縦スクロールが大きいと動作しないようにする（100から-100までのみ）
                            if (end[0] - start[0] > 400 && y_start[0] - y_end[0] < 100 && y_start[0] - y_end[0] > -100) {
                                //ドロワー開く。getActivity()あってよかた
                                DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                                if (drawer != null) {
                                    drawer.openDrawer(Gravity.LEFT);
                                }
                            }
                        }
                        break;
                }

                return false;
            }
        });
    }

}
