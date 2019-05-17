package io.github.takusan23.Kaisendon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.github.takusan23.Kaisendon.Activity.KonoAppNiTuite;
import io.github.takusan23.Kaisendon.Activity.LoginActivity;
import io.github.takusan23.Kaisendon.Activity.UserActivity;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuLoadSupport;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSQLiteHelper;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSettingFragment;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.MisskeyDriveBottomDialog;
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.TLQuickSettingSnackber;
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.TLQuickSettingsBottomFragment;
import io.github.takusan23.Kaisendon.CustomMenu.DirectMessage_Fragment;
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.Kaisendon.DesktopTL.DesktopFragment;
import io.github.takusan23.Kaisendon.Fragment.Bookmark_Frament;
import io.github.takusan23.Kaisendon.Fragment.CustomStreamingFragment;
import io.github.takusan23.Kaisendon.Fragment.Favourites_List_Fragment;
import io.github.takusan23.Kaisendon.Fragment.Federated_TimeLine_Fragment;
import io.github.takusan23.Kaisendon.Fragment.Follow_Suggestions_Fragment;
import io.github.takusan23.Kaisendon.Fragment.HelloFragment;
import io.github.takusan23.Kaisendon.Fragment.HomeCrad_Fragment;
import io.github.takusan23.Kaisendon.Fragment.Home_Fragment;
import io.github.takusan23.Kaisendon.Fragment.InstanceInfo_Fragment;
import io.github.takusan23.Kaisendon.Fragment.License_Fragment;
import io.github.takusan23.Kaisendon.Fragment.MultiAccountList_Fragment;
import io.github.takusan23.Kaisendon.Fragment.MultiPain_UI_Fragment;
import io.github.takusan23.Kaisendon.Fragment.Notification_Fragment;
import io.github.takusan23.Kaisendon.Fragment.Public_TimeLine_Fragment;
import io.github.takusan23.Kaisendon.Fragment.SettingFragment;
import io.github.takusan23.Kaisendon.Fragment.WearFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.github.takusan23.Kaisendon.Preference_ApplicationContext.getContext;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String toot_text = null;
    String user = null;

    private CustomMenuSQLiteHelper helper;
    private SQLiteDatabase db;


    String display_name = null;
    String user_id = null;
    String user_avater = null;
    String user_header = null;

    String account_id;
    private ProgressDialog dialog;
    AlertDialog alertDialog;

    private Snackbar snackbar;

    boolean nicoru = false;

    int test = 0;

    TextToSpeech textToSpeech;

    BroadcastReceiver networkChangeBroadcast;

    Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawer;

    Snackbar toot_snackbar;
    Snackbar newNote_Snackbar;

    SharedPreferences pref_setting;
    FloatingActionButton fab;
    LinearLayout media_LinearLayout;
    Button post_button;
    ImageButton toot_area_Button;
    EditText toot_EditText;
    //公開範囲
    String toot_area = "public";
    //名前とか
    String snackber_DisplayName;
    String snackber_Name = "";
    String Instance;
    String AccessToken;
    String snackber_Avatar;
    ImageView snackberAccountAvaterImageView;
    TextView snackberAccount_TextView;
    MenuBuilder account_menuBuilder;
    MenuPopupHelper account_optionsMenu;
    MenuBuilder misskey_account_menuBuilder;
    MenuPopupHelper misskey_account_optionMenu;
    LinearLayout account_LinearLayout;
    LinearLayout misskey_account_LinearLayout;
    ImageButton misskey_drive_Button;
    //時間指定投稿
    ImageButton mastodon_time_post_Button;
    String post_date;
    String post_time;
    Switch time_post_Switch;
    boolean isTimePost = false;
    boolean isMastodon_time_post;
    //投票
    ImageButton mastodon_vote_Button;
    LinearLayout toot_Button_LinearLayout;
    boolean isMastodon_vote = false;
    boolean isMastodon_vote_layout = false;
    EditText vote_1;
    EditText vote_2;
    EditText vote_3;
    EditText vote_4;
    EditText vote_time;
    Switch vote_use_Switch;
    Switch vote_multi_Switch;
    Switch vote_hide_Switch;
    //マルチアカウント読み込み用
    ArrayList<String> multi_account_instance;
    ArrayList<String> multi_account_access_token;
    ArrayList<String> misskey_multi_account_instance;
    ArrayList<String> misskey_multi_account_access_token;
    //添付メディア
    public static ArrayList<String> post_media_id;
    public static ArrayList<String> misskey_media_url;
    //文字数カウント
    int tootTextCount = 0;
    //カスタム絵文字表示に使う配列
    private boolean emojis_show = false;

    //最後に開いたカスタムメニューを保管（）
    private String lastMenuName = "";

    //DesktomMode用Mastodon Misskey分岐
    private boolean isDesktoopMisskeyMode = false;
    private Switch misskey_switch;
    private boolean isDesktop = false;

    private TLQuickSettingsBottomFragment dialogFragment;
    private TLQuickSettingSnackber tlQuickSettingSnackber;

    private CustomMenuLoadSupport customMenuLoadSupport;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //設定のプリファレンス
        pref_setting = PreferenceManager.getDefaultSharedPreferences(Home.this);

        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        //通信量節約
        boolean setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false);
        //Wi-Fi接続時は有効？
        boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
        //GIFを再生するか？
        boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);


        //ダークモード処理
        Configuration conf = getResources().getConfiguration();
        int currecntNightMode = conf.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        DarkModeSupport darkModeSupport = new DarkModeSupport(this);
        currecntNightMode = darkModeSupport.setIsDarkModeSelf(currecntNightMode);
        switch (currecntNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.AppTheme_NoActionBar);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.OLED_Theme);
                break;
        }

        setContentView(R.layout.activity_home);

        navigationView = findViewById(R.id.nav_view);

        customMenuLoadSupport = new CustomMenuLoadSupport(this, navigationView);

        //ログイン情報があるか
        //アクセストークンがない場合はログイン画面へ飛ばす
        if (pref_setting.getString("main_token", "").equals("") && pref_setting.getString("misskey_instance_list", "").equals("")) {
            Intent login = new Intent(this, LoginActivity.class);
            //login.putExtra("first_applunch", true);
            startActivity(login);
        }

        //SQLite
        if (helper == null) {
            helper = new CustomMenuSQLiteHelper(getContext());
        }
        if (db == null) {
            db = helper.getWritableDatabase();
            db.disableWriteAheadLogging();
        }

        //起動時の
        FragmentChange(new HelloFragment());
        //最後に開いたメニューを開くようにする
        String lastName = pref_setting.getString("custom_menu_last", null);
        //メニュー入れ替え
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.custom_menu);
        customMenuLoadSupport.loadCustomMenu(null);
        if (lastName != null) {
            try {
                customMenuLoadSupport.loadCustomMenu(lastName);
            } catch (CursorIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        //デスクトップモード時で再生成されないようにする
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_container);
        if (fragment != null && fragment instanceof DesktopFragment) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container_container, new DesktopFragment(), "desktop");
            fragmentTransaction.commit();
            isDesktop = true;
        } else {
            isDesktop = false;
        }

        dialogFragment = new TLQuickSettingsBottomFragment();



/*
        String start_fragment = pref_setting.getString("pref_startFragment", "HomeCard");
        if (savedInstanceState == null) {
            if (start_fragment.equals("HomeCard")) {
                FragmentChange(new HomeCrad_Fragment());
            }
            if (start_fragment.equals("Home")) {
                FragmentChange(new Home_Fragment());
            }
            if (start_fragment.equals("Notification")) {
                FragmentChange(new Notification_Fragment());
            }
            if (start_fragment.equals("Local")) {
                FragmentChange(new Public_TimeLine_Fragment());
            }
            if (start_fragment.equals("Federated")) {
                FragmentChange(new Federated_TimeLine_Fragment());
            }
            if (start_fragment.equals("Start")) {
            }
            if (start_fragment.equals("Streaming")) {
                FragmentChange(new CustomStreamingFragment());
            }

            //何もなかった場合（初期状態）
            if (start_fragment.equals("")) {
                FragmentChange(new HomeCrad_Fragment());
            }
        }
*/


        //カスタム絵文字有効/無効
        if (pref_setting.getBoolean("pref_custom_emoji", true)) {
            if (pref_setting.getBoolean("pref_avater_wifi", true)) {
                //WIFIのみ表示有効時
                //ネットワーク未接続時はnullか出る
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        //WIFI
                        emojis_show = true;
                    }
                }
            } else {
                //WIFI/MOBILE DATA 関係なく表示
                emojis_show = true;
            }
        }


        //アクセストークン
        AccessToken = null;
        //インスタンス
        Instance = null;

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        //カスタムメニューの場合は追加処理
        if (pref_setting.getBoolean("custom_menu", false)) {
            //メニュー入れ替え
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.custom_menu);
            customMenuLoadSupport.loadCustomMenu(null);
        }

        //ネットワークが切り替わったらトースト表示
        if (pref_setting.getBoolean("pref_networkchange", false)) {
            /*API 28で以下のコードは廃止されて動かないのでこれからはフラグメントの方に書きます*/
/*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                networkChangeBroadcast = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        //何故かアプリ起動時にもネットワーク変更ブロードキャストが飛んでくるので
                        //カウントアップして起動時は表示しないように
                        test++;
                        //System.out.println("カウント : " + String.valueOf(test));
                        if (test >= 2) {
                            View view = findViewById(android.R.id.content);
                            Snackbar.make(view, R.string.network_change, Snackbar.LENGTH_SHORT).setAction(R.string.ReStart, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //押したときにActivityを再生成する
                                    // アクティビティ再起動
                                    Intent intent = new Intent(Home.this, Home.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 起動しているActivityをすべて削除し、新しいタスクでMainActivityを起動する
                                    startActivity(intent);

                                }
                            }).show();
                        }
                    }
                };
                registerReceiver(networkChangeBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }
*/
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        //TootSnackBerのコードがクソ長いのでメゾット化
        //Misskey
        //setNewNote_Snackber();
        tootSnackBer();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTootShortcut();
            }
        });


        //共有を受け取る
        Intent intent = getIntent();
        String action_string = intent.getAction();
        System.out.println(action_string);
        if (Intent.ACTION_SEND.equals(action_string)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                //URL
                CharSequence text = extras.getCharSequence(Intent.EXTRA_TEXT);
                //タイトル
                CharSequence title = extras.getCharSequence(Intent.EXTRA_SUBJECT);
                //画像URI
                Uri uri = extras.getParcelable(Intent.EXTRA_STREAM);
                //EXTRA TEXTにタイトルが含まれているかもしれない？
                //含まれているときは消す
                if (text != null) {
                    if (title != null) {
                        text = text.toString().replace(title, "");
                        toot_EditText.append(title);
                        toot_EditText.append("\n");
                    }
                    toot_EditText.append(text);
                }
                //画像
                if (uri != null) {
                    media_uri_list.add(uri);
                    ImageViewClick();
                }
                //0.5秒後に起動するように
                Timer timer = new Timer(false);
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showTootShortcut();
                                timer.cancel();
                            }
                        });
                    }
                };
                timer.schedule(task, 500);
            }
        }

        //App Shortcutから起動
        if (getIntent().getBooleanExtra("toot", false)) {
            //0.5秒後に起動するように
            Timer timer = new Timer(false);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTootShortcut();
                            timer.cancel();
                        }
                    });
                }
            };
            timer.schedule(task, 500);
        }


/*
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //従来のTootActivityへー
                Intent toot = new Intent(Home.this, TestDragListView.class);
                startActivity(toot);
                return false;
            }
        });
*/


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String finalInstance = Instance;
        String finalAccessToken = AccessToken;

        //どろわーのイメージとか文字とか
        //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.nav_header_home_linearlayout);
        View navHeaderView = navigationView.getHeaderView(0);
        ImageView avater_imageView = navHeaderView.findViewById(R.id.icon_image);
        ImageView header_imageView = navHeaderView.findViewById(R.id.drawer_header);
        //ImageView header_imageView = navHeaderView
        TextView user_account_textView = navHeaderView.findViewById(R.id.drawer_account);
        TextView user_id_textView = navHeaderView.findViewById(R.id.drawer_id);
        if (!CustomMenuTimeLine.isMisskeyMode()) {
            String url = "https://" + Instance + "/api/v1/accounts/verify_credentials/?access_token=" + AccessToken;
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        //成功時
                        String response_string = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(response_string);
                            display_name = jsonObject.getString("display_name");
                            user_id = jsonObject.getString("username");
                            user_avater = jsonObject.getString("avatar");
                            user_header = jsonObject.getString("header");
                            account_id = jsonObject.getString("id");

                            //カスタム絵文字適用
                            if (emojis_show) {
                                //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
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
                                if (!jsonObject.isNull("profile_emojis")) {
                                    JSONArray profile_emojis = jsonObject.getJSONArray("profile_emojis");
                                    for (int i = 0; i < profile_emojis.length(); i++) {
                                        JSONObject emojiObject = profile_emojis.getJSONObject(i);
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
                            }

                            //UI Thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //表示設定
                                    if (setting_avater_hidden) {
                                        avater_imageView.setImageResource(R.drawable.ic_person_black_24dp);
                                        header_imageView.setBackgroundColor(Color.parseColor("#c8c8c8"));
                                    }
                                    //Wi-Fi
                                    if (setting_avater_wifi) {
                                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                            if (setting_avater_gif) {
                                                //GIFアニメ再生させない
                                                Picasso.get().load(user_avater).resize(100, 100).into(avater_imageView);
                                                Picasso.get().load(user_header).into(header_imageView);
                                            } else {
                                                //GIFアニメを再生
                                                Glide.with(getApplicationContext()).load(user_avater).apply(new RequestOptions().override(100, 100)).into(avater_imageView);
                                                Glide.with(getApplicationContext()).load(user_header).into(header_imageView);
                                            }
                                        }
                                    } else {
                                        avater_imageView.setImageResource(R.drawable.ic_person_black_24dp);
                                        header_imageView.setBackgroundColor(Color.parseColor("#c8c8c8"));
                                    }
                                    PicassoImageGetter imageGetter = new PicassoImageGetter(user_account_textView);
                                    user_account_textView.setText(Html.fromHtml(display_name, Html.FROM_HTML_MODE_LEGACY, imageGetter, null));
                                    user_id_textView.setText("@" + user_id + "@" + finalInstance);
                                    if (pref_setting.getBoolean("pref_subtitle_show", true)) {
                                        //サブタイトルに名前を入れる
                                        try {
                                            getSupportActionBar().setSubtitle(jsonObject.getString("display_name") + " ( @" + user_id + " / " + finalInstance + " )");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        //失敗時
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

        /*クイック設定*/
        setTimelinQuickSettings();
        tlQuickSettingSnackber = new TLQuickSettingSnackber(Home.this, navigationView);

/*
        //ローカルタイムライントースト
        if (pref_setting.getInt("timeline_toast_check", 0) == 1) {

            String channel = "Kaisendon_1";
            String channel_1 = "notification_TL";

            //通知
            NotificationManager notificationManager = (NotificationManager) Home.this.getSystemService(Context.NOTIFICATION_SERVICE);

            //Android Oからは通知の仕様が変わった
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(channel, "LocalTimeline Toot", NotificationManager.IMPORTANCE_DEFAULT);

                notificationChannel.setDescription(getString(R.string.notification_LocalTimeline_Toast));
                notificationChannel.setName(getString(R.string.app_name));

                notificationManager.createNotificationChannel(notificationChannel);

                NotificationCompat.Builder notification_nougat = new NotificationCompat.Builder(Home.this, channel)
                        .setSmallIcon(R.drawable.ic_rate_review_white_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_LocalTimeline_Toast));

                Intent resultIntent = new Intent(Home.this, Home.class);

                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(Home.this);

                taskStackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        taskStackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                notification_nougat.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager_nougat.notify(R.string.app_name, notification_nougat.build());


            } else {

                //ブロードキャストを送信
                //ブロードキャスト先を指定（明示的ブロードキャスト）
                //マニフェストにも記入しないと動かないので注意
                Intent notification_Intent = new Intent(getApplicationContext(), BroadcastRecever_Notification_Button.class);

                //ボタンを追加する

                NotificationCompat.Action action = new NotificationCompat.Action(
                        R.drawable.ic_rate_review_black_24dp,
                        getString(R.string.timeline_toast_disable),
                        PendingIntent.getBroadcast(this, 0, notification_Intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );


                NotificationCompat.Builder notification_nougat = new NotificationCompat.Builder(Home.this)
                        .setSmallIcon(R.drawable.ic_rate_review_white_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_LocalTimeline_Toast)
                        );

                notification_nougat.addAction(action);

                Intent resultIntent = new Intent(Home.this, Home.class);

                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(Home.this);

                taskStackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        taskStackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                notification_nougat.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager_nougat.notify(R.string.app_name, notification_nougat.build());

            }

            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                @Override
                protected String doInBackground(String... string) {

                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken)
                            .useStreamingApi()
                            .build();

                    Handler handler = new Handler() {
                        @Override
                        public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {
                            String user_name = status.getAccount().getDisplayName();
                            String toot_text = status.getContent();
                            String user_avater_url = status.getAccount().getAvatar();
                            long toot_id = status.getId();

                            Spanned spanned = Html.fromHtml(toot_text, Html.FROM_HTML_MODE_LEGACY);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    //トースト表示
*/
/*
                                    Toast toast = new Toast(getApplicationContext());
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.notification_toast_layout, null);
                                    LinearLayout toast_linearLayout = layout.findViewById(R.id.toast_layout_root);
                                    TextView toast_text = layout.findViewById(R.id.notification_text);
                                    toast_text.setText(Html.fromHtml(user_name + "\r\n" + toot_text, Html.FROM_HTML_MODE_COMPACT));
                                    ImageView toast_imageview = layout.findViewById(R.id.notification_icon);
                                    //toast_imageview.setImageDrawable(finalDrawable);
*//*


                                    //ブロードキャストを送信
                                    //ブロードキャスト先を指定（明示的ブロードキャスト）

                                    //途中で変更がある可能性があるので再度確認
                                    if (pref_setting.getInt("timeline_toast_check", 0) == 1) {


                                        NotificationChannel notificationChannel = null;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            notificationChannel = new NotificationChannel(channel_1, "Notification TL", NotificationManager.IMPORTANCE_HIGH);
                                            notificationChannel.setDescription(getString(R.string.notification_timeline));
                                            notificationChannel.setName(getString(R.string.notification_timeline));

                                            notificationManager.createNotificationChannel(notificationChannel);

                                            //トゥートようブロードキャスト
                                            String Type = "Type";

                                            Intent notification_localtimeline_toot = new Intent(getApplicationContext(), BroadcastReceiver_Notification_Timeline.class);
                                            notification_localtimeline_toot.putExtra(Type, "Toot");
                                            PendingIntent notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT);


                                            //マニフェストにも記入しないと動かないので注意
                                            Intent notififation_localtimeline_favouite = new Intent(getApplicationContext(), BroadcastReceiver_Notification_Timeline.class);
                                            notififation_localtimeline_favouite.putExtra(Type, "Favourite");
                                            notififation_localtimeline_favouite.putExtra("ID", toot_id);


                                            long[] pattern = {100};

                                            //通知
                                            NotificationCompat.Builder notification_localtimeline = new NotificationCompat.Builder(Home.this, channel_1)
                                                    .setSmallIcon(R.drawable.ic_rate_review_white_24dp)
                                                    .setContentTitle(user_name)
                                                    .setContentText(spanned)
                                                    .setPriority(2)
                                                    .setVibrate(pattern);

                                            //お気に入りボタン
                                            NotificationCompat.Action notification_favourite_action = new NotificationCompat.Action(
                                                    R.drawable.ic_star_black_24dp,
                                                    getString(R.string.favoutire),
                                                    PendingIntent.getBroadcast(Home.this, 0, notififation_localtimeline_favouite, PendingIntent.FLAG_UPDATE_CURRENT)
                                            );


                                            //トゥート
                                            RemoteInput remoteInput = new RemoteInput.Builder("Toot_Text")
                                                    .setLabel("今なにしてる？")
                                                    .build();

                                            NotificationCompat.Action notification_toot_action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send
                                                    , "今なにしてる？", notification_localtimeline_pendingIntent)
                                                    .addRemoteInput(remoteInput)
                                                    .build();

                                            notification_localtimeline.addAction(notification_toot_action);


                                            notification_localtimeline.addAction(notification_favourite_action);

                                            NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager_nougat.notify(R.string.notification_LocalTimeline_Notification, notification_localtimeline.build());


                                        } else {
                                            //トゥートようブロードキャスト
                                            String Type = "Type";

                                            Intent notification_localtimeline_toot = new Intent(getApplicationContext(), BroadcastReceiver_Notification_Timeline.class);
                                            notification_localtimeline_toot.putExtra(Type, "Toot");
                                            PendingIntent notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT);


                                            //マニフェストにも記入しないと動かないので注意
                                            Intent notififation_localtimeline_favouite = new Intent(getApplicationContext(), BroadcastReceiver_Notification_Timeline.class);
                                            notififation_localtimeline_favouite.putExtra(Type, "Favourite");
                                            notififation_localtimeline_favouite.putExtra("ID", toot_id);


                                            long[] pattern = {100};

                                            //通知
                                            NotificationCompat.Builder notification_localtimeline = new NotificationCompat.Builder(Home.this)
                                                    .setSmallIcon(R.drawable.ic_rate_review_white_24dp)
                                                    .setContentTitle(user_name)
                                                    .setContentText(spanned)
                                                    .setPriority(1)
                                                    .setVibrate(pattern);

                                            //お気に入りボタン
                                            NotificationCompat.Action notification_favourite_action = new NotificationCompat.Action(
                                                    R.drawable.ic_star_black_24dp,
                                                    getString(R.string.dialog_favorite),
                                                    PendingIntent.getBroadcast(Home.this, 0, notififation_localtimeline_favouite, PendingIntent.FLAG_UPDATE_CURRENT)
                                            );


                                            //トゥート
                                            RemoteInput remoteInput = new RemoteInput.Builder("Toot_Text")
                                                    .setLabel("今なにしてる？")
                                                    .build();

                                            NotificationCompat.Action notification_toot_action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send
                                                    , "今なにしてる？", notification_localtimeline_pendingIntent)
                                                    .addRemoteInput(remoteInput)
                                                    .build();

                                            notification_localtimeline.addAction(notification_toot_action);


                                            notification_localtimeline.addAction(notification_favourite_action);

                                            NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager_nougat.notify(R.string.notification_LocalTimeline_Notification, notification_localtimeline.build());

                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onNotification(@NotNull Notification notification) {

                        }

                        @Override
                        public void onDelete(long l) {

                        }
                    };
                    Streaming streaming = new Streaming(client);
                    try {
                        if (pref_setting.getString("pref_notification_timeline", "Home").contains("Home")) {
                            Shutdownable shutdownable = streaming.user(handler);
                        }
                        if (pref_setting.getString("pref_notification_timeline", "Home").contains("Local")) {
                            Shutdownable shutdownable = streaming.localPublic(handler);
                        }
                        if (pref_setting.getString("pref_notification_timeline", "Home").contains("Federated")) {
                            Shutdownable shutdownable = streaming.federatedPublic(handler);
                        }

                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }


        //読み上げ　TTS
        if (pref_setting.getBoolean("pref_speech", false)) {
            NotificationManager notificationManager = (NotificationManager) Home.this.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel notificationChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel("Kaisendon_1", "LocalTimeline Toot", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription(getString(R.string.notification_timeline));
                notificationChannel.setName(getString(R.string.notification_timeline));

                notificationManager.createNotificationChannel(notificationChannel);

                NotificationCompat.Builder ttsNotification =
                        new NotificationCompat.Builder(this, "Kaisendon_1")
                                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                                .setContentTitle(getString(R.string.speech_timeline))
                                .setContentText(getString(R.string.notification_speech_timeline));
                Intent tts_intent = new Intent(getApplicationContext(), BroadcastRecever_Notification_Button.class);
                tts_intent.putExtra("TTS", true);
                NotificationCompat.Action ttsAction = new NotificationCompat.Action(
                        R.drawable.ic_volume_off_black_24dp,
                        getString(R.string.timeline_toast_disable),
                        PendingIntent.getBroadcast(getApplicationContext(), 0, tts_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );
                ttsNotification.addAction(ttsAction);
                NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager_nougat.notify(R.string.speech_timeline, ttsNotification.build());

            } else {
                NotificationCompat.Builder ttsNotification =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                                .setContentTitle(getString(R.string.speech_timeline))
                                .setContentText(getString(R.string.notification_speech_timeline));
                Intent tts_intent = new Intent(getApplicationContext(), BroadcastRecever_Notification_Button.class);
                tts_intent.putExtra("TTS", true);
                NotificationCompat.Action ttsAction = new NotificationCompat.Action(
                        R.drawable.ic_volume_off_black_24dp,
                        getString(R.string.timeline_toast_disable),
                        PendingIntent.getBroadcast(getApplicationContext(), 0, tts_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );
                ttsNotification.addAction(ttsAction);
                NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager_nougat.notify(R.string.speech_timeline, ttsNotification.build());
            }

            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getBooleanExtra("TTS", false)) {
                        SharedPreferences.Editor editor = pref_setting.edit();
                        editor.putBoolean("pref_speech", false);
                        editor.apply();
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(R.string.speech_timeline);
                    }
                }
            };

            textToSpeech = new TextToSpeech(Home.this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (TextToSpeech.SUCCESS == status) {
                        //初期化　使えるよ！
                        textToSpeech.setSpeechRate(Float.valueOf(pref_setting.getString("pref_speech_rate", "1.0f")));
                        Toast.makeText(Home.this, R.string.text_to_speech_preparation, Toast.LENGTH_SHORT).show();
                    } else {
                        //使えないよ
                    }
                }
            });

            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... string) {
                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken)
                            .useStreamingApi()
                            .build();
                    Handler handler = new Handler() {
                        @Override
                        public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {


                            String toot = status.getContent();
                            String finaltoot = Html.fromHtml(toot, Html.FROM_HTML_MODE_COMPACT).toString();


                            // 正規表現
                            finaltoot = Html.fromHtml(finaltoot, Html.FROM_HTML_MODE_COMPACT).toString();
                            String final_toot_1 = finaltoot;
                            //URL省略
                            if (pref_setting.getBoolean("pref_speech_url", true)) {
                                final_toot_1 = finaltoot.replaceAll("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", "URL省略");
                                System.out.println(final_toot_1);
                            } else {
                                final_toot_1 = finaltoot;
                            }

                            if (0 < toot.length()) {
                                textToSpeech.speak(final_toot_1, textToSpeech.QUEUE_ADD, null, "messageID");
                            }
                        }

                        @Override
                        public void onNotification(@NotNull Notification notification) {*/
        /* no op *//*
}

                        @Override
                        public void onDelete(long id) {*/
        /* no op *//*
}
                    };

                    Streaming streaming = new Streaming(client);
                    try {
                        if (pref_setting.getString("pref_speech_timeline_type", "Home").contains("Home")) {
                            Shutdownable shutdownable = streaming.user(handler);
                        }
                        if (pref_setting.getString("pref_speech_timeline_type", "Home").contains("Local")) {
                            Shutdownable shutdownable = streaming.localPublic(handler);
                        }
                        if (pref_setting.getString("pref_speech_timeline_type", "Home").contains("Federated")) {
                            Shutdownable shutdownable = streaming.federatedPublic(handler);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
*/
        //スリープ無効？
        boolean setting_sleep = pref_setting.getBoolean("pref_no_sleep", false);
        if (setting_sleep) {
            //常時点灯
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            //常時点灯しない
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    //画像POST
    int count = 0;
    ArrayList<String> media_list = new ArrayList<>();
    ArrayList<Uri> media_uri_list = new ArrayList<>();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                System.out.println("値 : " + String.valueOf(resultCode) + " / " + data.getData());

                //ファイルパスとか
                String filePath = getPath(selectedImage);
                String file_extn = filePath.substring(filePath.lastIndexOf(".") + 1);
                File file = new File(filePath);
                String finalPath = "file:\\\\" + filePath;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);

                if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
                    //配列に入れる
                    media_uri_list.add(data.getData());
                    media_LinearLayout.removeAllViews();
                    //配列に入れた要素をもとにImageViewを生成する
                    for (int i = 0; i < media_uri_list.size(); i++) {
                        ImageView imageView = new ImageView(Home.this);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setImageURI(media_uri_list.get(i));
                        imageView.setTag(i);
                        media_LinearLayout.addView(imageView);
                        //押したとき
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(Home.this, "位置 : " + String.valueOf((Integer) imageView.getTag()), Toast.LENGTH_SHORT).show();
                                //要素の削除
                                //media_list.remove(0);
                                //再生成
                                ImageViewClick();
                            }
                        });
                    }
                    //画像アップロード
                    post_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //クローズでソフトキーボード非表示
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                if (Home.this.getCurrentFocus() != null) {
                                    imm.hideSoftInputFromWindow(Home.this.getCurrentFocus().getWindowToken(), 0);
                                }
                            }
/*
                            //通常POST・画像つきPOST
                            if (!media_uri_list.isEmpty()) {
                                //配列からUriを取り出す
                                for (int i = 0; i < media_uri_list.size(); i++) {
                                    //ひつようなやつ
                                    Uri uri = media_uri_list.get(i);
                                    if (CustomMenuTimeLine.isMisskeyMode()) {
                                        uploadDrivePhoto(uri);
                                    } else {
                                        uploadMastodonPhoto(uri);
                                    }
                                }
                            } else {
                                //画像添付トゥートは別に書くよ
                                if (media_uri_list.isEmpty() || media_uri_list == null || media_uri_list.get(0) == null) {
                                    //FABのアイコン戻す
                                    fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp));
                                    //時間指定投稿（予約投稿）を送信するね！メッセージ
                                    String message;
                                    if (isTimePost) {
                                        message = getString(R.string.time_post_post_button);
                                    } else {
                                        message = getString(R.string.note_create_message);
                                    }
                                    //Tootする
                                    //確認SnackBer
                                    Snackbar.make(v, message, Snackbar.LENGTH_SHORT).setAction(R.string.toot_text, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //FABのアイコン戻す
                                            fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp));
                                            //Mastodon / Misskey
                                            if (CustomMenuTimeLine.isMisskeyMode()) {
                                                misskeyNoteCreatePOST();
                                            } else {
                                                mastodonStatusesPOST();
                                            }
                                        }
                                    }).show();
                                }
                            }
*/
                        }
                    });
                }
            }
    }

    /**
     * context://→file://へ変換する
     */
    public String getPath(Uri uri) {
        //uri.getLastPathSegment();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);
        cursor.close();
        //Android Q から追加された Scoped Storage に一時的に対応
        //なにそれ→アプリごとにストレージサンドボックスが作られて、今まであったWRITE_EXTERNAL_STORAGEなしで扱える他
        //他のアプリからはアクセスできないようになってる。
        //<I>いやでも今までのfile://スキーマ変換が使えないのはクソクソクソでは</I>
        //今までのやつをAndroid Qで動かすと
        //Q /mnt/content/media ～
        //Pie /storage/emulated/0 ～
        //もう一回かけてようやくfile://スキーマのリンク取れた
        //Android Q
        if (Build.VERSION.CODENAME.equals("Q")) {
            // /mnt/content/が邪魔なので取って、そこにcontent://スキーマをつける
            // Google Photoからしか動かねーわまあPixel以外にもQが配信される頃には情報がわさわさ出てくることでしょう。
            String content_text = imagePath.replace("/mnt/content/", "content://");
            //System.out.println(imagePath);
            //try-catch
            //実機で確認できず
            //imagePath = getPathAndroidQ(Home.this, Uri.parse(content_text));
        }
        System.out.println(imagePath);
        return imagePath;
    }

    public static String getPathAndroidQ(Context context, Uri contentUri) {
        Cursor cursor = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = 0;
        String path = "";
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        }
        return path;
    }

    private void ImageViewClick() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
        media_LinearLayout.removeAllViews();
        //配列に入れた要素をもとにImageViewを生成する
        for (int i = 0; i < media_uri_list.size(); i++) {
            ImageView imageView = new ImageView(Home.this);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageURI(media_uri_list.get(i));
            imageView.setTag(i);
            media_LinearLayout.addView(imageView);
            //押したとき
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Home.this, "位置 : " + String.valueOf((Integer) imageView.getTag()), Toast.LENGTH_SHORT).show();
                    //要素の削除
                    //なんだこのくそｇｍコードは
                    //removeにgetTagそのまま書くとなんかだめなんだけど何これ意味不
                    if ((Integer) imageView.getTag() == 0) {
                        media_uri_list.remove(0);
                    } else if ((Integer) imageView.getTag() == 1) {
                        media_uri_list.remove(1);
                    } else if ((Integer) imageView.getTag() == 2) {
                        media_uri_list.remove(2);
                    } else if ((Integer) imageView.getTag() == 3) {
                        media_uri_list.remove(3);
                    }
                    //再生成
                    ImageViewClick();
                }
            });
        }
    }

    /**
     * Misskey Driveの画像を表示させる
     */
    private void setMisskeyDrivePhoto() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
        media_LinearLayout.removeAllViews();
        //配列に入れた要素をもとにImageViewを生成する
        for (int i = 0; i < misskey_media_url.size(); i++) {
            ImageView imageView = new ImageView(Home.this);
            //Glide
            Glide.with(Home.this).load(misskey_media_url.get(i)).into(imageView);
            imageView.setLayoutParams(layoutParams);
            imageView.setTag(i);
            media_LinearLayout.addView(imageView);
            //押したとき
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Home.this, "位置 : " + String.valueOf((Integer) imageView.getTag()), Toast.LENGTH_SHORT).show();
                    //要素の削除
                    //なんだこのくそｇｍコードは
                    //removeにgetTagそのまま書くとなんかだめなんだけど何これ意味不
                    if ((Integer) imageView.getTag() == 0) {
                        misskey_media_url.remove(0);
                        post_media_id.remove(0);
                    } else if ((Integer) imageView.getTag() == 1) {
                        misskey_media_url.remove(1);
                        post_media_id.remove(1);
                    } else if ((Integer) imageView.getTag() == 2) {
                        misskey_media_url.remove(2);
                        post_media_id.remove(2);
                    } else if ((Integer) imageView.getTag() == 3) {
                        misskey_media_url.remove(3);
                        post_media_id.remove(3);
                    }
                    //再生成
                    setMisskeyDrivePhoto();
                }
            });
        }
    }


    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

/*
        //noinspection SimplifiableIfStatement
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (id == R.id.home_menu_quick_setting) {
            Bundle bundle = new Bundle();
            bundle.putString("account_id", account_id);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "quick_setting");
        }
*/

/*
            case R.id.home_menu_login:
                Intent login = new Intent(this, LoginActivity.class);
                startActivity(login);
                break;
            case R.id.home_menu_account_list:
                transaction.replace(R.id.container_container, new AccountListFragment());
                transaction.commit();
                break;
            case R.id.home_menu_account:
                Intent intent = new Intent(this, UserActivity.class);
                if (CustomMenuTimeLine.isMisskeyMode()) {
                    intent.putExtra("Account_ID", CustomMenuTimeLine.getAccount_id());
                } else {
                    intent.putExtra("Account_ID", account_id);
                }
                intent.putExtra("my", true);
                startActivity(intent);
                break;
            case R.id.home_menu_desktop_mode:
                DesktopFragment desktopFragment = new DesktopFragment();
                transaction.replace(R.id.container_container, new DesktopFragment(), "desktop");
                transaction.commit();
                break;
            case R.id.home_menu_setting:
                transaction.replace(R.id.container_container, new SettingFragment());
                transaction.commit();
                break;
            case R.id.home_menu_license:
                transaction.replace(R.id.container_container, new License_Fragment());
                transaction.commit();
                break;
            case R.id.home_menu_thisapp:
                Intent thisApp = new Intent(this, KonoAppNiTuite.class);
                startActivity(thisApp);
                break;
            case R.id.home_menu_privacy_policy:
                showPrivacyPolicy();
                break;
            case R.id.home_menu_wear:
                transaction.replace(R.id.container_container, new WearFragment());
                transaction.commit();
                break;
            case R.id.home_menu_bookmark:
                transaction.replace(R.id.container_container, new Bookmark_Frament());
                transaction.commit();
                break;
            case R.id.home_menu_activity_pub_viewer:
                transaction.replace(R.id.container_container, new ActivityPubViewer());
                transaction.commit();
                break;
            case R.id.home_menu_reload_menu:
                //再読み込み
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.custom_menu);
                customMenuLoadSupport.loadCustomMenu(null);
                break;
            case R.id.home_menu_old_drawer:
                navigationView.inflateMenu(R.menu.activity_home_drawer);
                break;
            case R.id.home_menu_flowlt:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_container);
                if (fragment != null && fragment instanceof CustomMenuTimeLine) {
                    FloatingTL floatingTL = new FloatingTL(this, fragment.getArguments().getString("json"));
                    floatingTL.setNotification();
                }
                break;
            case R.id.home_menu_quick_setting:
                Bundle bundle = new Bundle();
                bundle.putString("account_id", account_id);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "quick_setting");
                break;
            case R.id.home_menu_dark:
                //これはAndroid Qを搭載しない端末向け設定
                if (Build.VERSION.SDK_INT <= 28 && !Build.VERSION.CODENAME.equals("Q")) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    if (pref_setting.getBoolean("darkmode", false)) {
                        editor.putBoolean("darkmode", false);
                        editor.apply();
                    } else {
                        editor.putBoolean("darkmode", true);
                        editor.apply();
                    }
                    //Activity再起動
                    startActivity(new Intent(this, Home.class));
                } else {
                    Toast.makeText(this, getString(R.string.darkmode_error_os), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        if (id == R.id.action_settings) {
            return true;
        }
*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * プライバシーポリシー
     */
    private void showPrivacyPolicy() {
        String githubUrl = "https://github.com/takusan23/Kaisendon/blob/master/kaisendon-privacy-policy.md";
        if (pref_setting.getBoolean("pref_chrome_custom_tabs", true)) {
            Bitmap back_icon = BitmapFactory.decodeResource(Home.this.getResources(), R.drawable.ic_action_arrow_back);
            String custom = CustomTabsHelper.getPackageNameToUse(Home.this);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(custom);
            customTabsIntent.launchUrl(Home.this, Uri.parse(githubUrl));
        } else {
            Uri uri = Uri.parse(githubUrl);
            Intent browser = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(browser);
        }
    }

    /*クイック設定を返すやつ*/
    public TLQuickSettingSnackber getTlQuickSettingSnackber() {
        return tlQuickSettingSnackber;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        if (id == R.id.home_timeline) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new Home_Fragment());
            transaction.commit();

        } else if (id == R.id.login_menu) {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);

        } else if (id == R.id.account_menu) {
            Intent intent = new Intent(this, UserActivity.class);
            intent.putExtra("Account_ID", account_id);
            intent.putExtra("my", true);
            startActivity(intent);

        } else if (id == R.id.notifications) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new Notification_Fragment());
            transaction.commit();

        } else if (id == R.id.public_time_line_menu) {

            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new Public_TimeLine_Fragment());
            transaction.commit();

        } else if (id == R.id.federated_time_line_menu) {

            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new Federated_TimeLine_Fragment());
            transaction.commit();

        } else if (id == R.id.instance_info_menu) {

//            Intent setting = new Intent(this, SettingsActivity.class);
//            startActivity(setting);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new InstanceInfo_Fragment());
            transaction.commit();
/*

        } else if (id == R.id.search_menu) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new SearchFragment());
            transaction.commit();


*/
        } else if (id == R.id.setting) {

//            Intent setting = new Intent(this, SettingsActivity.class);
//            startActivity(setting);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new SettingFragment());
            transaction.commit();

        } else if (id == R.id.multipain_ui) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", true);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new MultiPain_UI_Fragment());
            transaction.commit();

        } else if (id == R.id.homecard_menu) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new HomeCrad_Fragment());
            transaction.commit();

        } else if (id == R.id.follow_suggestions) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new Follow_Suggestions_Fragment());
            transaction.commit();

        } else if (id == R.id.favourite_list) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new Favourites_List_Fragment());
            transaction.commit();

        } else if (id == R.id.bookmark) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new Bookmark_Frament());
            transaction.commit();

        } else if (id == R.id.multi_account_menu) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new MultiAccountList_Fragment());
            transaction.commit();

        } else if (id == R.id.custom_streaming_nemu) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new CustomStreamingFragment());
            transaction.commit();

        } else if (id == R.id.licence_menu) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new License_Fragment());
            transaction.commit();
        } else if (id == R.id.direct_message_menu) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new DirectMessage_Fragment());
            transaction.commit();
        } else if (id == R.id.menu_wear) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("app_multipain_ui", false);
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new WearFragment());
            transaction.commit();
        } else if (id == R.id.konoAppmenu) {
            Intent login = new Intent(this, KonoAppNiTuite.class);
            startActivity(login);
        } else if (id == R.id.custom_menu_mode_menu) {
            //モード切替
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.getMenu().clear();
            SharedPreferences.Editor editor = pref_setting.edit();
            if (pref_setting.getBoolean("custom_menu", false)) {
                editor.putBoolean("custom_menu", false);
                //メニュー切り替え
                navigationView.inflateMenu(R.menu.activity_home_drawer);
            } else {
                editor.putBoolean("custom_menu", true);
                //メニュー切り替え
                navigationView.inflateMenu(R.menu.custom_menu);
                //適用処理
                customMenuLoadSupport.loadCustomMenu(null);
            }
            editor.apply();
        } else if (id == R.id.custom_menu_setting_menu) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new CustomMenuSettingFragment());
            transaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //終了しますか？
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_local_hotel_black_12dp)
                        .setTitle("終了確認")
                        .setMessage("アプリを終了しますか？")
                        .setNegativeButton("いいえ", null)
                        .setPositiveButton("はい",

                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }
                        )
                        .show();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void FragmentChange(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_container, fragment);
        transaction.commit();
    }

/*
    @Override
    protected void onResume() {
        super.onResume();
        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        pref_setting.registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        pref_setting.unregisterOnSharedPreferenceChangeListener(this);
    }
*/


    //TextViewがどうしても欲しかった
    //TextViewを押す→ListViewの一番上に移動←これがしたかった
    /*
    https://stackoverflow.com/questions/31428086/access-toolbar-textview-from-fragment-in-android
    */
    public Toolbar getToolBer() {
        return toolbar;
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        SharedPreferences.Editor editor = pref_setting.edit();
        editor.putBoolean("app_multipain_ui", false);
        editor.apply();
        if (pref_setting.getBoolean("pref_speech", false)) {
            textToSpeech.shutdown();
        }
        //レジーバー解除
        if (pref_setting.getBoolean("pref_networkchange", false)) {
            if (networkChangeBroadcast != null) {
                unregisterReceiver(networkChangeBroadcast);
            }
        }
    }


    @SuppressLint("RestrictedApi")
    public void tootSnackBer() {
        //画像ID配列
        post_media_id = new ArrayList<>();
        misskey_media_url = new ArrayList<>();

        String AccessToken = pref_setting.getString("main_token", "");
        String Instance = pref_setting.getString("main_instance", "");

        View view = Home.this.findViewById(R.id.container_public);
        toot_snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);
        //Snackber生成
        ViewGroup snackBer_viewGrop = (ViewGroup) toot_snackbar.getView().findViewById(R.id.snackbar_text).getParent();
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        //LinearLayout動的に生成
        LinearLayout snackber_LinearLayout = new LinearLayout(Home.this);
        snackber_LinearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams warp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        snackber_LinearLayout.setLayoutParams(warp);
        //スワイプで消せないようにする
        toot_snackbar.getView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                toot_snackbar.getView().getViewTreeObserver().removeOnPreDrawListener(this);
                ((CoordinatorLayout.LayoutParams) toot_snackbar.getView().getLayoutParams()).setBehavior(null);
                return true;
            }
        });

        //テキストボックス
        //Materialふうに
        LinearLayout toot_textBox_LinearLayout = new LinearLayout(Home.this);
        //レイアウト読み込み
        getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout);
        toot_EditText = getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_editText);
        //ヒント
        ((TextInputLayout) getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_TextInputLayout)).setHint(getString(R.string.imananisiteru));
        //色
        ((TextInputLayout) getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_TextInputLayout)).setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));
        ((TextInputLayout) getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_TextInputLayout)).setBoxStrokeColor(Color.parseColor("#ffffff"));
        toot_EditText.setTextColor(Color.parseColor("#ffffff"));
        toot_EditText.setHintTextColor(Color.parseColor("#ffffff"));
        //ボタン追加用LinearLayout
        toot_Button_LinearLayout = new LinearLayout(Home.this);
        toot_Button_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        toot_Button_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //Button
        //画像追加
        ImageButton add_image_Button = new ImageButton(Home.this);
        add_image_Button.setPadding(20, 20, 20, 20);
        add_image_Button.setBackgroundColor(Color.parseColor("#00000000"));
        add_image_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        add_image_Button.setImageDrawable(getDrawable(R.drawable.ic_image_black_24dp));
        add_image_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int REQUEST_PERMISSION = 1000;
                //ストレージ読み込みの権限があるか確認
                //許可してないときは許可を求める
                if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(Home.this)
                            .setTitle(getString(R.string.permission_dialog_titile))
                            .setMessage(getString(R.string.image_upload_storage_permisson))
                            .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //権限をリクエストする
                                    requestPermissions(
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            REQUEST_PERMISSION);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                } else {
                    //onActivityResultで受け取れる
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                }
            }
        });

        //公開範囲選択用Button
        toot_area_Button = new ImageButton(Home.this);
        toot_area_Button.setPadding(20, 20, 20, 20);
        toot_area_Button.setBackgroundColor(Color.parseColor("#00000000"));
        toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp));
        toot_area_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        //toot_area_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0);
        //メニューセット
        if (CustomMenuTimeLine.isMisskeyMode()) {
            setMisskeyVisibilityMenu(toot_area_Button);
        } else {
            setMastodonVisibilityMenu(toot_area_Button);
        }

        //投稿用LinearLayout
        LinearLayout toot_LinearLayout = new LinearLayout(Home.this);
        toot_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams toot_button_LayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toot_button_LayoutParams.gravity = Gravity.RIGHT;
        toot_LinearLayout.setLayoutParams(toot_button_LayoutParams);

        //投稿用Button
        post_button = new Button(Home.this, null, 0, R.style.Widget_AppCompat_Button_Borderless);
        post_button.setText(String.valueOf(tootTextCount) + "/" + "500 " + getString(R.string.toot_text));
        post_button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        post_button.setTextColor(Color.parseColor("#ffffff"));
        Drawable toot_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_create_black_24dp, null);
        post_button.setCompoundDrawablesWithIntrinsicBounds(toot_icon, null, null, null);
        //POST statuses
        String finalAccessToken = AccessToken;
        String finalInstance = Instance;
        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //クローズでソフトキーボード非表示
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    if (Home.this.getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(Home.this.getCurrentFocus().getWindowToken(), 0);
                    }
                }
                //画像添付なしのときはここを利用して、
                //画像添付トゥートは別に書くよ
                if (media_uri_list.isEmpty() || media_uri_list == null || media_uri_list.get(0) == null) {
                    //FABのアイコン戻す
                    fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp));
                    //時間指定投稿（予約投稿）を送信するね！メッセージ
                    String message;
                    if (isTimePost) {
                        message = getString(R.string.time_post_post_button);
                    } else {
                        message = getString(R.string.note_create_message);
                    }
                    //Tootする
                    //確認SnackBer
                    Snackbar.make(v, message, Snackbar.LENGTH_SHORT).setAction(R.string.toot_text, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //FABのアイコン戻す
                            fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp));
                            //Mastodon / Misskey
                            if (CustomMenuTimeLine.isMisskeyMode()) {
                                misskeyNoteCreatePOST();
                            } else {
                                mastodonStatusesPOST();
                            }
                        }
                    }).show();
                } else {
                    //画像投稿する
                    for (int i = 0; i < media_uri_list.size(); i++) {
                        //ひつようなやつ
                        Uri uri = media_uri_list.get(i);
                        if (CustomMenuTimeLine.isMisskeyMode()) {
                            uploadDrivePhoto(uri);
                        } else {
                            uploadMastodonPhoto(uri);
                        }
                    }
                }
            }
        });

        //端末情報とぅーと
        ImageButton device_Button = new ImageButton(Home.this);
        device_Button.setPadding(20, 20, 20, 20);
        device_Button.setBackgroundColor(Color.parseColor("#00000000"));
        device_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        device_Button.setImageDrawable(getDrawable(R.drawable.ic_perm_device_information_black_24dp));
        //ポップアップメニュー作成
        MenuBuilder device_menuBuilder = new MenuBuilder(Home.this);
        MenuInflater device_inflater = new MenuInflater(Home.this);
        device_inflater.inflate(R.menu.device_info_menu, device_menuBuilder);
        MenuPopupHelper device_optionsMenu = new MenuPopupHelper(Home.this, device_menuBuilder, device_Button);
        device_optionsMenu.setForceShowIcon(true);
        //コードネーム変換（手動
        String codeName = "";
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            codeName = "Nougat";
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            codeName = "Oreo";
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            codeName = "Pie";
        }
        String finalCodeName = codeName;
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        device_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device_optionsMenu.show();
                device_menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        //名前
                        if (menuItem.getTitle().toString().contains(getString(R.string.device_name))) {
                            toot_EditText.append(Build.MODEL);
                            toot_EditText.append("\r\n");
                        }
                        //Androidバージョン
                        if (menuItem.getTitle().toString().contains(getString(R.string.android_version))) {
                            toot_EditText.append(Build.VERSION.RELEASE);
                            toot_EditText.append("\r\n");
                        }
                        //めーかー
                        if (menuItem.getTitle().toString().contains(getString(R.string.maker))) {
                            toot_EditText.append(Build.BRAND);
                            toot_EditText.append("\r\n");
                        }
                        //SDKバージョン
                        if (menuItem.getTitle().toString().contains(getString(R.string.sdk_version))) {
                            toot_EditText.append(String.valueOf(Build.VERSION.SDK_INT));
                            toot_EditText.append("\r\n");
                        }
                        //コードネーム
                        if (menuItem.getTitle().toString().contains(getString(R.string.codename))) {
                            toot_EditText.append(finalCodeName);
                            toot_EditText.append("\r\n");
                        }
                        //バッテリーレベル
                        if (menuItem.getTitle().toString().contains(getString(R.string.battery_level))) {
                            toot_EditText.append(String.valueOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) + "%");
                            toot_EditText.append("\r\n");
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });
            }
        });

        //Misskey Driveボタン
        misskey_drive_Button = new ImageButton(Home.this);
        //misskey_drive_Button.setBackground(getDrawable(R.drawable.button_clear));
        misskey_drive_Button.setPadding(20, 20, 20, 20);
        misskey_drive_Button.setBackgroundColor(Color.parseColor("#00000000"));
        misskey_drive_Button.setImageDrawable(getDrawable(R.drawable.ic_cloud_queue_white_24dp));
        misskey_drive_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Misskey Drive API を叩く
                MisskeyDriveBottomDialog dialogFragment = new MisskeyDriveBottomDialog();
                dialogFragment.show(getSupportFragmentManager(), "misskey_drive_dialog");
                setMisskeyDrivePhoto();
            }
        });

        //時間投稿ボタン
        //高さ調整
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LinearLayout mastodon_time_post_LinearLayout = new LinearLayout(Home.this);
        getLayoutInflater().inflate(R.layout.mastodon_time_post_layout, mastodon_time_post_LinearLayout);
        mastodon_time_post_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mastodon_time_post_Button = new ImageButton(Home.this);
        mastodon_time_post_Button.setPadding(20, 20, 20, 20);
        mastodon_time_post_Button.setBackgroundColor(Color.parseColor("#00000000"));
        mastodon_time_post_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        mastodon_time_post_Button.setImageDrawable(getDrawable(R.drawable.ic_timer_black_24dp));
        mastodon_time_post_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //2番めに出す
                if (!isMastodon_time_post) {
                    isMastodon_time_post = true;
                    snackber_LinearLayout.addView(mastodon_time_post_LinearLayout, 2);
                    //設定ボタン等
                    Button day_setting_Button = snackber_LinearLayout.findViewById(R.id.time_post_button);
                    time_post_Switch = snackber_LinearLayout.findViewById(R.id.time_post_switch);
                    TextView date_TextView = snackber_LinearLayout.findViewById(R.id.time_post_textview);
                    Button time_setting_Button = snackber_LinearLayout.findViewById(R.id.time_post_time_button);
                    TextView time_TextView = snackber_LinearLayout.findViewById(R.id.time_post_time_textview);
                    //日付設定画面
                    day_setting_Button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDatePicker(date_TextView);
                        }
                    });
                    //時間設定画面
                    time_setting_Button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showTimePicker(time_TextView);
                        }
                    });
                    //有効無効
                    time_post_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            //入れておく
                            isTimePost = isChecked;
                            //色を変えとく？
                            if (isChecked) {
                                post_button.setText(getString(R.string.time_post_post_button));
                                mastodon_time_post_Button.setColorFilter(Color.parseColor("#0069c0"), PorterDuff.Mode.SRC_IN);
                            } else {
                                post_button.setText(getString(R.string.toot_text));
                                mastodon_time_post_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            }
                        }
                    });

                } else {
                    //消す
                    isMastodon_time_post = false;
                    snackber_LinearLayout.removeView(mastodon_time_post_LinearLayout);
                }
            }
        });

        //投票
        //ここから
        LinearLayout vote_LinearLayout = new LinearLayout(Home.this);
        getLayoutInflater().inflate(R.layout.toot_vote_layout, vote_LinearLayout);
        vote_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size.y / 2));
        mastodon_vote_Button = new ImageButton(Home.this);
        mastodon_vote_Button.setPadding(20, 20, 20, 20);
        mastodon_vote_Button.setBackgroundColor(Color.parseColor("#00000000"));
        mastodon_vote_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        mastodon_vote_Button.setImageDrawable(getDrawable(R.drawable.ic_move_to_inbox_black_24dp));
        mastodon_vote_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMastodon_vote_layout) {
                    isMastodon_vote_layout = true;
                    snackber_LinearLayout.addView(vote_LinearLayout, 2);
                    vote_use_Switch = snackber_LinearLayout.findViewById(R.id.vote_use_switch);
                    vote_multi_Switch = snackber_LinearLayout.findViewById(R.id.vote_multi_switch);
                    vote_hide_Switch = snackber_LinearLayout.findViewById(R.id.vote_hide_switch);
                    vote_1 = snackber_LinearLayout.findViewById(R.id.vote_editText_1);
                    vote_2 = snackber_LinearLayout.findViewById(R.id.vote_editText_2);
                    vote_3 = snackber_LinearLayout.findViewById(R.id.vote_editText_3);
                    vote_4 = snackber_LinearLayout.findViewById(R.id.vote_editText_4);
                    vote_time = snackber_LinearLayout.findViewById(R.id.vote_editText_time);
                    ((TextInputLayout) snackber_LinearLayout.findViewById(R.id.vote_textInputLayout_1)).setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    ((TextInputLayout) snackber_LinearLayout.findViewById(R.id.vote_textInputLayout_2)).setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    ((TextInputLayout) snackber_LinearLayout.findViewById(R.id.vote_textInputLayout_3)).setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    ((TextInputLayout) snackber_LinearLayout.findViewById(R.id.vote_textInputLayout_4)).setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    ((TextInputLayout) snackber_LinearLayout.findViewById(R.id.vote_textInputLayout_time)).setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    //有効無効
                    vote_use_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            //入れておく
                            isMastodon_vote = isChecked;
                            //色を変えとく？
                            if (isChecked) {
                                mastodon_vote_Button.setColorFilter(Color.parseColor("#0069c0"), PorterDuff.Mode.SRC_IN);
                            } else {
                                mastodon_vote_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            }
                        }
                    });
                } else {
                    isMastodon_vote_layout = false;
                    snackber_LinearLayout.removeView(vote_LinearLayout);
                }
            }
        });

        //コマンド実行ボタン
        Button command_Button = new Button(Home.this, null, 0, R.style.Widget_AppCompat_Button_Borderless);
        command_Button.setText(R.string.command_run);
        command_Button.setTextColor(Color.parseColor("#ffffff"));
        //EditTextを監視する
        toot_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //コマンド実行メゾット？
                //CommandCode.commandSet(Home.this, toot_EditText, toot_LinearLayout, command_Button, "/sushi", "command_sushi");
                CommandCode.commandSet(Home.this, toot_EditText, toot_LinearLayout, command_Button, "/friends.nico", "pref_friends_nico_mode");
                CommandCode.commandSetNotPreference(Home.this, Home.this, toot_EditText, toot_LinearLayout, command_Button, "/rate-limit", "rate-limit");
                CommandCode.commandSetNotPreference(Home.this, Home.this, toot_EditText, toot_LinearLayout, command_Button, "/fav-home", "home");
                CommandCode.commandSetNotPreference(Home.this, Home.this, toot_EditText, toot_LinearLayout, command_Button, "/fav-local", "local");
                CommandCode.commandSetNotPreference(Home.this, Home.this, toot_EditText, toot_LinearLayout, command_Button, "/じゃんけん", "じゃんけん");
                //カウント
                tootTextCount = toot_EditText.getText().toString().length();
                //投稿ボタンの文字
                String buttonText;
                if (isTimePost) {
                    buttonText = getString(R.string.time_post_post_button);
                } else {
                    buttonText = getString(R.string.toot_text);
                }
                post_button.setText(String.valueOf(tootTextCount) + "/" + "500 " + buttonText);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //アカウント切り替えとか
        account_LinearLayout = new LinearLayout(this);
        account_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams center_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        center_layoutParams.gravity = Gravity.CENTER;
        //ImageView
        snackberAccountAvaterImageView = new ImageView(this);
        snackberAccountAvaterImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        snackberAccountAvaterImageView.setLayoutParams(center_layoutParams);
        //TextView
        snackberAccount_TextView = new TextView(this);
        snackberAccount_TextView.setTextSize(14);
        snackberAccount_TextView.setTextColor(Color.parseColor("#ffffff"));
        snackberAccount_TextView.setLayoutParams(center_layoutParams);
        //アカウント情報を取得するところにテキスト設定とか書いたで
        if (CustomMenuTimeLine.isMisskeyMode()) {
            getMisskeyAccount();
        } else {
            getAccount();
        }
        //アカウント切り替えポップアップ
        //ポップアップメニューを展開する
        account_menuBuilder = new MenuBuilder(this);
        account_optionsMenu = new MenuPopupHelper(this, account_menuBuilder, account_LinearLayout);
        misskey_account_menuBuilder = new MenuBuilder(this);
        misskey_account_optionMenu = new MenuPopupHelper(this, misskey_account_menuBuilder, account_LinearLayout);
        //マルチアカウント読み込み
        //押したときの処理とかもこっち
        //カスタムメニュー時は無効（）

        //LinearLayoutに入れる
        account_LinearLayout.addView(snackberAccountAvaterImageView);
        account_LinearLayout.addView(snackberAccount_TextView);


        //画像追加用LinearLayout
        media_LinearLayout = new LinearLayout(Home.this);
        media_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        media_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //LinearLayoutに追加
        //メイン

        snackber_LinearLayout.addView(account_LinearLayout);
        snackber_LinearLayout.addView(toot_textBox_LinearLayout);
        snackber_LinearLayout.addView(toot_Button_LinearLayout);
        snackber_LinearLayout.addView(media_LinearLayout);
        snackber_LinearLayout.addView(toot_LinearLayout);
        //ボタン追加
        toot_Button_LinearLayout.addView(add_image_Button);
        toot_Button_LinearLayout.addView(toot_area_Button);
        toot_Button_LinearLayout.addView(device_Button);

        //Toot LinearLayout
        toot_LinearLayout.addView(post_button);

        //SnackBerに追加
        snackBer_viewGrop.addView(snackber_LinearLayout);
    }

    public Snackbar getToot_snackbar() {
        return toot_snackbar;
    }

    //自分の情報を手に入れる
    private void getAccount() {
        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        //通信量節約
        boolean setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false);
        //Wi-Fi接続時は有効？
        boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
        //GIFを再生するか？
        boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);
        String AccessToken = pref_setting.getString("main_token", "");
        String Instance = pref_setting.getString("main_instance", "");

/*
        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");
        }
*/
        String url = "https://" + Instance + "/api/v1/accounts/verify_credentials/?access_token=" + AccessToken;
        //作成
        Request request = new Request.Builder()
                .url(url)
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
                try {
                    JSONObject jsonObject = new JSONObject(response_string);
                    String display_name = jsonObject.getString("display_name");
                    String user_id = jsonObject.getString("acct");
                    snackber_DisplayName = display_name;
                    //カスタム絵文字適用
                    if (emojis_show) {
                        //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
                        JSONArray emojis = jsonObject.getJSONArray("emojis");
                        for (int i = 0; i < emojis.length(); i++) {
                            JSONObject emojiObject = emojis.getJSONObject(i);
                            String emoji_name = emojiObject.getString("shortcode");
                            String emoji_url = emojiObject.getString("url");
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            //display_name
                            if (snackber_DisplayName.contains(emoji_name)) {
                                //あったよ
                                snackber_DisplayName = snackber_DisplayName.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                        }
                        if (!jsonObject.isNull("profile_emojis")) {
                            JSONArray profile_emojis = jsonObject.getJSONArray("profile_emojis");
                            for (int i = 0; i < profile_emojis.length(); i++) {
                                JSONObject emojiObject = profile_emojis.getJSONObject(i);
                                String emoji_name = emojiObject.getString("shortcode");
                                String emoji_url = emojiObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                //display_name
                                if (snackber_DisplayName.contains(emoji_name)) {
                                    //あったよ
                                    snackber_DisplayName = snackber_DisplayName.replace(":" + emoji_name + ":", custom_emoji_src);
                                }
                            }
                        }
                    }
                    snackber_Name = "@" + user_id + "@" + Instance + "";
                    snackber_Avatar = jsonObject.getString("avatar");
                    //UIスレッド
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //画像を入れる
                            //表示設定
                            if (setting_avater_hidden) {
                                snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp);
                                snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            }
                            //Wi-Fi
                            if (setting_avater_wifi && networkCapabilities != null) {
                                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                    if (setting_avater_gif) {
                                        //GIFアニメ再生させない
                                        Picasso.get()
                                                .load(snackber_Avatar)
                                                .resize(100, 100)
                                                .placeholder(R.drawable.ic_refresh_black_24dp)
                                                .into(snackberAccountAvaterImageView);
                                    } else {
                                        //GIFアニメを再生
                                        Glide.with(getApplicationContext())
                                                .load(snackber_Avatar)
                                                .apply(new RequestOptions().override(100, 100).placeholder(R.drawable.ic_refresh_black_24dp))
                                                .into(snackberAccountAvaterImageView);
                                    }
                                }
                            } else {
                                snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp);
                                snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            }
                            //テキストビューに入れる
                            PicassoImageGetter imageGetter = new PicassoImageGetter(snackberAccount_TextView);
                            snackberAccount_TextView.setText(Html.fromHtml(snackber_DisplayName, Html.FROM_HTML_MODE_LEGACY, imageGetter, null));
                            snackberAccount_TextView.append("\n" + snackber_Name);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void showMultiAccount() {
        //押したときの処理
        account_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //そもそも呼ばれてない説
                if (multi_account_instance == null) {
                    //一度だけ取得する
                    readMultiAccount();
                } else {
                    //追加中に押したら落ちるから回避
                    if (account_menuBuilder.size() == multi_account_instance.size()) {
                        account_optionsMenu.show();
                        account_menuBuilder.setCallback(new MenuBuilder.Callback() {
                            @Override
                            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                                //ItemIdにマルチアカウントのカウントを入れている
                                int position = menuItem.getItemId();
                                String multi_instance = multi_account_instance.get(position);
                                String multi_access_token = multi_account_access_token.get(position);
                                AccessToken = multi_access_token;
                                Instance = multi_instance;
                                SharedPreferences.Editor editor = pref_setting.edit();
                                editor.putString("main_instance", multi_instance);
                                editor.putString("main_token", multi_access_token);
                                editor.apply();
                                //アカウント情報更新
                                getAccount();
                                return false;
                            }

                            @Override
                            public void onMenuModeChange(MenuBuilder menuBuilder) {

                            }
                        });
                    } else {
                        Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void showMisskeyMultiAccount() {
        //押したときの処理
        account_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //そもそも呼ばれてない説
                if (misskey_multi_account_instance == null) {
                    //一度だけ取得する
                    readMisskeyMultiAccount();
                } else {
                    //追加中に押したら落ちるから回避
                    if (misskey_account_menuBuilder.size() == misskey_multi_account_instance.size()) {
                        misskey_account_optionMenu.show();
                        misskey_account_menuBuilder.setCallback(new MenuBuilder.Callback() {
                            @Override
                            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                                //ItemIdにマルチアカウントのカウントを入れている
                                int position = menuItem.getItemId();
                                String multi_instance = misskey_multi_account_instance.get(position);
                                String multi_access_token = misskey_multi_account_instance.get(position);
                                AccessToken = multi_access_token;
                                Instance = multi_instance;
                                SharedPreferences.Editor editor = pref_setting.edit();
                                editor.putString("misskey_main_instance", multi_instance);
                                editor.putString("misskey_main_token", multi_access_token);
                                editor.apply();
                                //アカウント情報更新
                                getMisskeyAccount();
                                return false;
                            }

                            @Override
                            public void onMenuModeChange(MenuBuilder menuBuilder) {

                            }
                        });
                    } else {
                        Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void readMultiAccount() {
        multi_account_instance = new ArrayList<>();
        multi_account_access_token = new ArrayList<>();
        misskey_multi_account_instance = new ArrayList<>();
        misskey_multi_account_access_token = new ArrayList<>();
        //とりあえずPreferenceに書き込まれた値を
        String instance_instance_string = pref_setting.getString("instance_list", "");
        String account_instance_string = pref_setting.getString("access_list", "");
        String misskey_instance_instance_string = pref_setting.getString("misskey_instance_list", "");
        String misskey_account_instance_string = pref_setting.getString("misskey_access_list", "");
        if (!instance_instance_string.equals("")) {
            try {
                JSONArray instance_array = new JSONArray(instance_instance_string);
                JSONArray access_array = new JSONArray(account_instance_string);
                for (int i = 0; i < instance_array.length(); i++) {
                    multi_account_access_token.add(access_array.getString(i));
                    multi_account_instance.add(instance_array.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!misskey_instance_instance_string.equals("")) {
            try {
                JSONArray instance_array = new JSONArray(misskey_account_instance_string);
                JSONArray access_array = new JSONArray(misskey_account_instance_string);
                for (int i = 0; i < instance_array.length(); i++) {
                    misskey_multi_account_access_token.add(access_array.getString(i));
                    misskey_multi_account_instance.add(instance_array.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (multi_account_instance.size() >= 1) {
            for (int count = 0; count < multi_account_instance.size(); count++) {
                String multi_instance = multi_account_instance.get(count);
                String multi_access_token = multi_account_access_token.get(count);
                int finalCount = count;
                //GetAccount
                String url = "https://" + multi_instance + "/api/v1/accounts/verify_credentials/?access_token=" + multi_access_token;
                //作成
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                //GETリクエスト
                OkHttpClient client_1 = new OkHttpClient();
                String finalInstance = Instance;
                client_1.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String response_string = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(response_string);
                            String display_name = jsonObject.getString("display_name");
                            String user_id = jsonObject.getString("acct");
                            //スナックバー更新
                            snackber_Name = "@" + user_id + "@" + finalInstance + "";
                            snackber_Avatar = jsonObject.getString("avatar");
                            account_menuBuilder.add(0, finalCount, 0, display_name + "(" + user_id + " / " + multi_instance + ")");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }


    /**
     * マルチアカウント読み込み　Misskey
     */
    @SuppressLint("RestrictedApi")
    private void readMisskeyMultiAccount() {
        misskey_multi_account_instance = new ArrayList<>();
        misskey_multi_account_access_token = new ArrayList<>();
        //とりあえずPreferenceに書き込まれた値を
        String instance_instance_string = pref_setting.getString("misskey_instance_list", "");
        String account_instance_string = pref_setting.getString("misskey_access_list", "");
        if (!instance_instance_string.equals("")) {
            try {
                JSONArray instance_array = new JSONArray(instance_instance_string);
                JSONArray access_array = new JSONArray(account_instance_string);
                for (int i = 0; i < instance_array.length(); i++) {
                    misskey_multi_account_instance.add(instance_array.getString(i));
                    misskey_multi_account_access_token.add(access_array.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (misskey_multi_account_instance.size() >= 1) {
            for (int count = 0; count < misskey_multi_account_instance.size(); count++) {
                String multi_instance = misskey_multi_account_instance.get(count);
                String multi_access_token = misskey_multi_account_access_token.get(count);
                int finalCount = count;
                //GetAccount
                String url = "https://" + multi_instance + "/api/i";
                //JSON
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("i", multi_access_token);
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
                OkHttpClient client_1 = new OkHttpClient();
                client_1.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String response_string = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(response_string);
                            String display_name = jsonObject.getString("name");
                            String user_id = jsonObject.getString("username");
                            misskey_account_menuBuilder.add(0, finalCount, 0, display_name + "(" + user_id + " / " + multi_instance + ")");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }


    //自分の情報を手に入れる Misskey版
    private void getMisskeyAccount() {
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        //通信量節約
        boolean setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false);
        //Wi-Fi接続時は有効？
        boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
        //GIFを再生するか？
        boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);
        String url = "https://" + instance + "/api/users/show";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
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
        OkHttpClient client_1 = new OkHttpClient();
        String finalInstance = Instance;
        client_1.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(response_string);
                    String display_name = jsonObject.getString("name");
                    snackber_DisplayName = display_name;
                    //カスタム絵文字適用
                    if (emojis_show) {
                        //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
                        JSONArray emojis = jsonObject.getJSONArray("emojis");
                        for (int i = 0; i < emojis.length(); i++) {
                            JSONObject emojiObject = emojis.getJSONObject(i);
                            String emoji_name = emojiObject.getString("name");
                            String emoji_url = emojiObject.getString("url");
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            //display_name
                            if (snackber_DisplayName.contains(emoji_name)) {
                                //あったよ
                                snackber_DisplayName = snackber_DisplayName.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                        }
                        if (!jsonObject.isNull("profile_emojis")) {
                            JSONArray profile_emojis = jsonObject.getJSONArray("profile_emojis");
                            for (int i = 0; i < profile_emojis.length(); i++) {
                                JSONObject emojiObject = profile_emojis.getJSONObject(i);
                                String emoji_name = emojiObject.getString("name");
                                String emoji_url = emojiObject.getString("url");
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                //display_name
                                if (snackber_DisplayName.contains(emoji_name)) {
                                    //あったよ
                                    snackber_DisplayName = snackber_DisplayName.replace(":" + emoji_name + ":", custom_emoji_src);
                                }
                            }
                        }
                    }
                    snackber_Name = "@" + username + "@" + instance + "";
                    snackber_Avatar = jsonObject.getString("avatarUrl");
                    //UIスレッド
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //画像を入れる
                            //表示設定
                            if (setting_avater_hidden) {
                                snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp);
                                snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            }
                            //Wi-Fi
                            if (setting_avater_wifi) {
                                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                    if (setting_avater_gif) {
                                        //GIFアニメ再生させない
                                        Picasso.get()
                                                .load(snackber_Avatar)
                                                .resize(100, 100)
                                                .placeholder(R.drawable.ic_refresh_black_24dp)
                                                .into(snackberAccountAvaterImageView);
                                    } else {
                                        //GIFアニメを再生
                                        Glide.with(getApplicationContext())
                                                .load(snackber_Avatar)
                                                .apply(new RequestOptions().override(100, 100).placeholder(R.drawable.ic_refresh_black_24dp))
                                                .into(snackberAccountAvaterImageView);
                                    }
                                }
                            } else {
                                snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp);
                                snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            }
                            //テキストビューに入れる
                            PicassoImageGetter imageGetter = new PicassoImageGetter(snackberAccount_TextView);
                            snackberAccount_TextView.setText(Html.fromHtml(snackber_DisplayName, Html.FROM_HTML_MODE_LEGACY, imageGetter, null));
                            snackberAccount_TextView.append("\n" + snackber_Name);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Misskey notes/create POST
     */
    private void misskeyNoteCreatePOST() {
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        String url = "https://" + instance + "/api/notes/create";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("i", token);
            jsonObject.put("visibility", toot_area);
            jsonObject.put("text", toot_EditText.getText().toString());
            jsonObject.put("viaMobile", true);//スマホからなので一応
            //添付メディア
            if (post_media_id.size() >= 1) {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < post_media_id.size(); i++) {
                    jsonArray.put(post_media_id.get(i));
                }
                jsonObject.put("fileIds", jsonArray);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                System.out.println(response_string);
                if (!response.isSuccessful()) {
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Home.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //EditTextを空にする
                            toot_EditText.setText("");
                            tootTextCount = 0;
                            //TootSnackber閉じる
                            toot_snackbar.dismiss();
                            //配列を空にする
                            media_uri_list.clear();
                            post_media_id.clear();
                            media_LinearLayout.removeAllViews();
                        }
                    });
                }
            }
        });
    }

    /**
     * Mastodon statuses POST
     */
    private void mastodonStatusesPOST() {
        String AccessToken = pref_setting.getString("main_token", "");
        String Instance = pref_setting.getString("main_instance", "");
        String url = "https://" + Instance + "/api/v1/statuses/?access_token=" + AccessToken;
/*
        //ぱらめーたー
        FormBody.Builder requestBody = new FormBody.Builder();
        requestBody.add("status", toot_EditText.getText().toString());
        requestBody.add("visibility", toot_area);

        //時間指定
        if (isTimePost) {
            //System.out.println(post_date + "/" + post_time);
            //nullCheck
            if (post_date != null && post_time != null) {
                requestBody.add("scheduled_at", post_date + post_time);
            }
        }
        //画像
        for (int i = 0; i < post_media_id.size(); i++) {
            requestBody.add("media_ids[]", post_media_id.get(i));
        }
        //投票機能
        if (isMastodon_vote) {
            requestBody.add("poll", createMastodonVote().toString());
        }
        requestBody.build();
*/

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", toot_EditText.getText().toString());
            jsonObject.put("visibility", toot_area);
            //時間指定
            if (isTimePost) {
                //System.out.println(post_date + "/" + post_time);
                //nullCheck
                if (post_date != null && post_time != null) {
                    jsonObject.put("scheduled_at", post_date + post_time);
                }
            }
            //画像
            if (post_media_id.size() != 0) {
                JSONArray media = new JSONArray();
                for (int i = 0; i < post_media_id.size(); i++) {
                    media.put(post_media_id.get(i));
                }
                jsonObject.put("media_ids", media);
            }
            //投票機能
            if (isMastodon_vote) {
                jsonObject.put("poll", createMastodonVote());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonObject.toString());
        RequestBody requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        //System.out.println(jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody_json)
                .build();

        //POST
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
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
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Home.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //予約投稿・通常投稿でトースト切り替え
                            if (time_post_Switch != null) {
                                time_post_Switch.setChecked(false);
                                Toast.makeText(Home.this, getString(R.string.time_post_ok), Toast.LENGTH_SHORT).show();
                                //予約投稿を無効化
                                isTimePost = false;
                                mastodon_time_post_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            } else {
                                Toast.makeText(Home.this, getString(R.string.toot_ok), Toast.LENGTH_SHORT).show();
                            }
                            //投票
                            if (isMastodon_vote) {
                                isMastodon_vote = false;
                                vote_use_Switch.setChecked(false);
                            }
                            //EditTextを空にする
                            toot_EditText.setText("");
                            tootTextCount = 0;
                            //TootSnackber閉じる
                            toot_snackbar.dismiss();
                            //配列を空にする
                            media_uri_list.clear();
                            post_media_id.clear();
                            media_LinearLayout.removeAllViews();
                        }
                    });
                }
            }
        });
    }

    /**
     * Misskey 画像POST
     */
    private void uploadDrivePhoto(Uri uri) {
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        String url = "https://" + instance + "/api/drive/files/create";
        //くるくる
        SnackberProgress.showProgressSnackber(toot_EditText, Home.this, getString(R.string.loading) + "\n" + url);
        //ぱらめーたー
        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        requestBody.setType(MultipartBody.FORM);
        //requestBody.addFormDataPart("file", file_post.getName(), RequestBody.create(MediaType.parse("image/" + file_extn_post), file_post));
        requestBody.addFormDataPart("i", token);
        requestBody.addFormDataPart("force", "true");
        //Android Qで動かないのでUriからBitmap変換してそれをバイトに変換してPOSTしてます
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            String file_name = getFileNameUri(uri);
            String extn = getContentResolver().getType(uri);
            requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn), byteBuffer.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        requestBody.build();
        //作成
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody.build())
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
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                //System.out.println(response_string);
                if (!response.isSuccessful()) {
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Home.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response_string);
                        String media_id_long = jsonObject.getString("id");
                        //配列に格納
                        post_media_id.add(media_id_long);
                        //確認SnackBer
                        //数確認
                        if (media_uri_list.size() == post_media_id.size()) {
                            View view = findViewById(R.id.container_public);
                            Snackbar.make(view, R.string.note_create_message, Snackbar.LENGTH_SHORT).setAction(R.string.toot_text, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    misskeyNoteCreatePOST();
                                }
                            }).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * PNG / JPEG
     */
    private Bitmap.CompressFormat getImageType(String extn) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        switch (extn) {
            case "jpg":
                format = Bitmap.CompressFormat.JPEG;
                break;
            case "jpeg":
                format = Bitmap.CompressFormat.JPEG;
                break;
            case "png":
                format = Bitmap.CompressFormat.PNG;
                break;
        }
        return format;
    }

    /**
     * Mastodon 画像POST
     */
    private void uploadMastodonPhoto(Uri uri) {
        //えんどぽいんと
        String url = "https://" + Instance + "/api/v1/media/";
        //ぱらめーたー
        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        requestBody.setType(MultipartBody.FORM);
        //requestBody.addFormDataPart("file", file_post.getName(), RequestBody.create(MediaType.parse("image/" + file_extn_post), file_post));
        requestBody.addFormDataPart("access_token", AccessToken);
        //くるくる
        SnackberProgress.showProgressSnackber(toot_EditText, Home.this, getString(R.string.loading) + "\n" + url);
        //Android Qで動かないのでUriからバイトに変換してPOSTしてます
        //重いから非同期処理
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }
                    String file_name = getFileNameUri(uri);
                    String extn = getContentResolver().getType(uri);
                    requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn), byteBuffer.toByteArray()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //じゅんび
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody.build())
                        .build();
                //画像Upload
                OkHttpClient okHttpClient = new OkHttpClient();
                //POST実行
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        //失敗
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Home.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String response_string = response.body().string();
                        //System.out.println("画像POST : " + response_string);
                        if (!response.isSuccessful()) {
                            //失敗
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Home.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(response_string);
                                String media_id_long = jsonObject.getString("id");
                                //配列に格納
                                post_media_id.add(media_id_long);
                                //確認SnackBer
                                //数確認
                                if (media_uri_list.size() == post_media_id.size()) {
                                    View view = findViewById(R.id.container_public);
                                    Snackbar.make(view, R.string.note_create_message, Snackbar.LENGTH_INDEFINITE).setAction(R.string.toot_text, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mastodonStatusesPOST();
                                        }
                                    }).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * Uri→FileName
     */
    private String getFileNameUri(Uri uri) {
        String file_name = null;
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                file_name = cursor.getString(0);
            }
        }
        return file_name;
    }


    /**
     * Mastodon 投票
     */
    private JSONObject createMastodonVote() {
        JSONObject object = new JSONObject();
        try {
            //配列
            JSONArray jsonArray = new JSONArray();
            if (vote_1.getText().toString() != null) {
                jsonArray.put(vote_1.getText().toString());
            }
            if (vote_2.getText().toString() != null) {
                jsonArray.put(vote_2.getText().toString());
            }
            if (vote_3.getText().toString() != null) {
                jsonArray.put(vote_3.getText().toString());
            }
            if (vote_4.getText().toString() != null) {
                jsonArray.put(vote_4.getText().toString());
            }
            object.put("options", jsonArray);
            object.put("expires_in", vote_time.getText().toString());
            object.put("multiple", vote_multi_Switch.isChecked());
            //object.put("hide_totals", vote_hide_Switch.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }


    /**
     * Mastodon 公開範囲
     */
    @SuppressLint("RestrictedApi")
    private void setMastodonVisibilityMenu(ImageButton button) {
        //ポップアップメニュー作成
        MenuBuilder menuBuilder = new MenuBuilder(Home.this);
        MenuInflater inflater = new MenuInflater(Home.this);
        inflater.inflate(R.menu.toot_area_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(Home.this, menuBuilder, button);
        optionsMenu.setForceShowIcon(true);
        //ポップアップメニューを展開する
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //表示
                optionsMenu.show();
                //押したときの反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        //公開（全て）
                        if (menuItem.getTitle().toString().contains(getString(R.string.visibility_public))) {
                            toot_area = "public";
                            button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp));
                        }
                        //未収載（TL公開なし・誰でも見れる）
                        if (menuItem.getTitle().toString().contains(getString(R.string.visibility_unlisted))) {
                            toot_area = "unlisted";
                            button.setImageDrawable(getDrawable(R.drawable.ic_done_all_black_24dp));
                        }
                        //非公開（フォロワー限定）
                        if (menuItem.getTitle().toString().contains(getString(R.string.visibility_private))) {
                            toot_area = "private";
                            button.setImageDrawable(getDrawable(R.drawable.ic_lock_open_black_24dp));
                        }
                        //ダイレクト（指定したアカウントと自分）
                        if (menuItem.getTitle().toString().contains(getString(R.string.visibility_direct))) {
                            toot_area = "direct";
                            button.setImageDrawable(getDrawable(R.drawable.ic_assignment_ind_black_24dp));
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });

            }
        });
    }

    /**
     * Misskey 公開範囲
     */
    @SuppressLint("RestrictedApi")
    private void setMisskeyVisibilityMenu(ImageButton button) {
        //ポップアップメニュー作成
        MenuBuilder menuBuilder = new MenuBuilder(Home.this);
        MenuInflater inflater = new MenuInflater(Home.this);
        inflater.inflate(R.menu.misskey_visibility_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(Home.this, menuBuilder, button);
        optionsMenu.setForceShowIcon(true);
        //ポップアップメニューを展開する
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //表示
                optionsMenu.show();
                //押したときの反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        //公開（全て）
                        if (menuItem.getTitle().toString().contains(getString(R.string.misskey_public))) {
                            toot_area = "public";
                            button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp));
                        }
                        //ホーム
                        if (menuItem.getTitle().toString().contains(getString(R.string.misskey_home))) {
                            toot_area = "home";
                            button.setImageDrawable(getDrawable(R.drawable.ic_home_black_24dp));
                        }
                        //フォロワー限定
                        if (menuItem.getTitle().toString().contains(getString(R.string.misskey_followers))) {
                            toot_area = "followers";
                            button.setImageDrawable(getDrawable(R.drawable.ic_person_add_black_24dp));
                        }
                        //ダイレクト（指定したアカウントと自分）
                        if (menuItem.getTitle().toString().contains(getString(R.string.misskey_specified))) {
                            toot_area = "specified";
                            button.setImageDrawable(getDrawable(R.drawable.ic_assignment_ind_black_24dp));
                        }
                        //公開（ローカルのみ）
                        if (menuItem.getTitle().toString().contains(getString(R.string.misskey_private))) {
                            toot_area = "private";
                            button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp));
                        }

                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });

            }
        });
    }

    /**
     * DatePicker
     */
    private void showDatePicker(TextView textView) {
        final String[] date = {""};

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dateBuilder = new DatePickerDialog(Home.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String month_string = "";
                String day_string = "";
                //1-9月は前に0を入れる
                if (month++ <= 9) {
                    month_string = "0" + String.valueOf(month++);
                } else {
                    month_string = String.valueOf(month++);
                }
                //1-9日も前に0を入れる
                if (dayOfMonth <= 9) {
                    day_string = "0" + String.valueOf(dayOfMonth);
                } else {
                    day_string = String.valueOf(dayOfMonth);
                }
                post_date = year + month_string + day_string + "T";
                textView.setText(post_date);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        dateBuilder.show();
    }

    /**
     * TimePicker
     */
    private void showTimePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(Home.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hour_string = "";
                String minute_string = "";
                //1-9月は前に0を入れる
                if (hourOfDay <= 9) {
                    hour_string = "0" + String.valueOf(hourOfDay++);
                } else {
                    hour_string = String.valueOf(hourOfDay++);
                }
                //1-9日も前に0を入れる
                if (minute <= 9) {
                    minute_string = "0" + String.valueOf(minute);
                } else {
                    minute_string = String.valueOf(minute);
                }
                post_time = hour_string + minute_string + "00" + "+0900";
                textView.setText(post_time);
            }
        }, hour, minute, true);
        dialog.show();
    }

    /**
     * TootShortcutShow
     */
    private void showTootShortcut() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_container);
        if (fragment != null && fragment instanceof DesktopFragment) {
            //DesktopModeはPopupMenuからMastodon/Misskeyを選ぶ
            //Misskeyアカウントが登録されていなければ話にならない
            if (!pref_setting.getString("misskey_instance_list", "").equals("")) {
                //ポップアップメニュー作成
                PopupMenu sns_PopupMenu = new PopupMenu(this, fab);
                sns_PopupMenu.inflate(R.menu.desktop_mode_sns_menu);
                //クリックイベント
                sns_PopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.desktop_mode_menu_mastodon:
                                getAccount();
                                setMastodonVisibilityMenu(toot_area_Button);
                                toot_Button_LinearLayout.removeView(misskey_drive_Button);
                                toot_Button_LinearLayout.removeView(mastodon_time_post_Button);
                                toot_Button_LinearLayout.removeView(mastodon_vote_Button);
                                toot_Button_LinearLayout.addView(mastodon_time_post_Button);
                                toot_Button_LinearLayout.addView(mastodon_vote_Button);
                                //デスクトップモード利用時はマルチアカウント表示できるように
                                if (fragment != null && fragment instanceof DesktopFragment) {
                                    showMultiAccount();
                                }
                                break;
                            case R.id.desktop_mode_menu_misskey:
                                if (!pref_setting.getString("misskey_instance_list", "").equals("")) {
                                    getMisskeyAccount();
                                    setMisskeyVisibilityMenu(toot_area_Button);
                                    toot_Button_LinearLayout.removeView(misskey_drive_Button);
                                    toot_Button_LinearLayout.removeView(mastodon_time_post_Button);
                                    toot_Button_LinearLayout.removeView(mastodon_vote_Button);
                                    toot_Button_LinearLayout.addView(misskey_drive_Button);
                                    //デスクトップモード利用時はマルチアカウント表示できるように
                                    if (fragment != null && fragment instanceof DesktopFragment) {
                                        showMisskeyMultiAccount();
                                    }
                                }
                                break;
                        }
                        toot_snackbar.show();
                        //ふぉーかす
                        toot_EditText.requestFocus();
                        //キーボード表示
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }
                        return false;
                    }
                });
                //すでにTootSnackberが表示されている場合は消して、ポップアップメニューを表示する
                if (toot_snackbar.isShown()) {
                    toot_snackbar.dismiss();
                    //キーボード非表示
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        if (Home.this.getCurrentFocus() != null) {
                            imm.hideSoftInputFromWindow(Home.this.getCurrentFocus().getWindowToken(), 0);
                        }
                    }
                    sns_PopupMenu.show();
                } else {
                    sns_PopupMenu.show();
                }
            } else {
                //Mastodonのみ表示
                getAccount();
                setMastodonVisibilityMenu(toot_area_Button);
                toot_Button_LinearLayout.removeView(misskey_drive_Button);
                toot_Button_LinearLayout.removeView(mastodon_time_post_Button);
                toot_Button_LinearLayout.removeView(mastodon_vote_Button);
                toot_Button_LinearLayout.addView(mastodon_time_post_Button);
                toot_Button_LinearLayout.addView(mastodon_vote_Button);
                //デスクトップモード利用時はマルチアカウント表示できるように
                if (fragment != null && fragment instanceof DesktopFragment) {
                    showMultiAccount();
                }
                if (!toot_snackbar.isShown()) {
                    toot_snackbar.show();
                    //ふぉーかす
                    toot_EditText.requestFocus();
                    //キーボード表示
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                } else {
                    toot_snackbar.dismiss();
                    //クローズでソフトキーボード非表示
                    fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp));
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        if (Home.this.getCurrentFocus() != null) {
                            imm.hideSoftInputFromWindow(Home.this.getCurrentFocus().getWindowToken(), 0);
                        }
                    }
                }
            }
        } else {
            //ユーザー情報を取得
            //MisskeyモードでMisskeyアカウントが登録されれいるときのみ表示
            //避けたかったけどどうしてもisMisskeyMode()必要だから使う
            if (CustomMenuTimeLine.isMisskeyMode() && !pref_setting.getString("misskey_instance_list", "").equals("")) {
                getMisskeyAccount();
                setMisskeyVisibilityMenu(toot_area_Button);
                toot_Button_LinearLayout.removeView(misskey_drive_Button);
                toot_Button_LinearLayout.removeView(mastodon_time_post_Button);
                toot_Button_LinearLayout.removeView(mastodon_vote_Button);
                toot_Button_LinearLayout.addView(misskey_drive_Button);
            } else {
                getAccount();
                setMastodonVisibilityMenu(toot_area_Button);
                toot_Button_LinearLayout.removeView(misskey_drive_Button);
                toot_Button_LinearLayout.removeView(mastodon_time_post_Button);
                toot_Button_LinearLayout.removeView(mastodon_vote_Button);
                toot_Button_LinearLayout.addView(mastodon_time_post_Button);
                toot_Button_LinearLayout.addView(mastodon_vote_Button);
            }
            if (!toot_snackbar.isShown()) {
                toot_snackbar.show();
                //ふぉーかす
                toot_EditText.requestFocus();
                //キーボード表示
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            } else {
                toot_snackbar.dismiss();
                //クローズでソフトキーボード非表示
                fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp));
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    if (Home.this.getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(Home.this.getCurrentFocus().getWindowToken(), 0);
                    }
                }
            }
        }
    }

    /*タイムラインクイック設定ボタン生成*/
    private void setTimelinQuickSettings() {
        ImageView qs = new ImageView(this);
        qs.setImageResource(R.drawable.tl_quick_setting_icon);
        qs.setPadding(50, 10, 50, 10);
        qs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable = qs.getDrawable();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                    ((Animatable) drawable).start();
                }
                ArrayList<String> list = new ArrayList<>();
                list.add(account_id);
                tlQuickSettingSnackber.setList(list);
                if (tlQuickSettingSnackber.getSnackbar().isShown()) {
                    tlQuickSettingSnackber.dismissSnackBer();
                } else {
                    tlQuickSettingSnackber.showSnackBer();
                }
            }
        });
        //追加されてなければ追加
        toolbar.addView(qs);
    }
}
