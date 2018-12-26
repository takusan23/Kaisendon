package io.github.takusan23.kaisendon;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Streaming;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import okhttp3.OkHttpClient;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String toot_text = null;
    String user = null;

    String display_name = null;
    String user_id = null;
    String user_avater = null;
    String user_header = null;

    long account_id;
    private ProgressDialog dialog;
    AlertDialog alertDialog;

    private Snackbar snackbar;

    boolean nicoru = false;

    int test = 0;

    TextToSpeech textToSpeech;

    BroadcastReceiver networkChangeBroadcast;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

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


        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        if (dark_mode) {
            setTheme(R.style.DayNightNoActionBarClearStatusBer);
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        //この画面用にNoActionBerなダークテーマを指定している
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            setTheme(R.style.OLED_Theme_Home);
        } else {
            //なにもない
        }

        //setTheme(R.style.Emoji);


        setContentView(R.layout.activity_home);

        //System.out.println("アクセストークン : " + pref_setting.getString("token", ""));

        //ログイン情報があるか
        //アクセストークンがない場合はログイン画面へ飛ばす
        if (pref_setting.getString("main_token", "").equals("")) {
            Intent login = new Intent(this, LoginActivity.class);
            //login.putExtra("first_applunch", true);
            startActivity(login);
        }


        //起動時の
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
                FragmentChange(new Start_Fragment());
            }
            if (start_fragment.equals("Streaming")) {
                FragmentChange(new CustomStreamingFragment());
            }

            //何もなかった場合（初期状態）
            if (start_fragment.equals("")) {
                FragmentChange(new HomeCrad_Fragment());
            }
        }


/*
        //ウィジェットから起動
        try {
            if (getIntent().getBooleanExtra("Home",false)){
                FragmentChange(new Home_Fragment());
            }
            if (getIntent().getBooleanExtra("Notification",false)){
                FragmentChange(new Notification_Fragment());
            }
            if (getIntent().getBooleanExtra("Local",false)){
                FragmentChange(new Public_TimeLine_Fragment());
            }
            if (getIntent().getBooleanExtra("Federated",false)){
                FragmentChange(new Federated_TimeLine_Fragment());
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
*/

/*
        //絵文字用SharedPreferences
        SharedPreferences pref_emoji = Preference_ApplicationContext.getContext().getSharedPreferences("preferences_emoji", Context.MODE_PRIVATE);
        Toast.makeText(Home.this,pref_setting.getString("emoji",""),Toast.LENGTH_LONG).show();

*/

/*
        boolean homecard = pref_setting.getBoolean("pref_home_homecard", true);
        if (homecard) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new HomeCrad_Fragment());
            transaction.commit();
        }
*/

        //App Shortcutから起動
        if (getIntent().getBooleanExtra("home_card", false)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_container, new HomeCrad_Fragment());
            transaction.commit();
        }

        //ネットワークが切り替わったらトースト表示
        if (pref_setting.getBoolean("pref_networkchange", false)) {
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
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toot = new Intent(Home.this, TootActivity.class);
                startActivity(toot);
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //アクセストークン
        String AccessToken = null;

        //インスタンス
        String Instance = null;

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        String finalInstance = Instance;
        String finalAccessToken = AccessToken;


        //どろわーのイメージとか文字とか
        View navHeaderView = navigationView.getHeaderView(0);
        //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.nav_header_home_linearlayout);
        ImageView avater_imageView = navHeaderView.findViewById(R.id.icon_image);
        ImageView header_imageView = navHeaderView.findViewById(R.id.drawer_header);
        //ImageView header_imageView = navHeaderView
        TextView user_account_textView = navHeaderView.findViewById(R.id.drawer_account);
        TextView user_id_textView = navHeaderView.findViewById(R.id.drawer_id);
//        user_id_textView.setText(user_id);
//        user_account_textView.setText(display_name);

        Menu drawer_menu = navigationView.getMenu();
        MenuItem favourite_menu = drawer_menu.findItem(R.id.favourite_list);
        //ニコるかお気に入りか
        boolean nicoru_favourite_check = pref_setting.getBoolean("pref_friends_nico_mode", false);
        if (!nicoru_favourite_check) {
            favourite_menu.setTitle(R.string.favourite_list);
        } else {
            favourite_menu.setTitle("ニコったリスト");
        }


        //あかうんとじょうほう
        String finalInstance1 = Instance;
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken).build();

                try {
                    Account account = new Accounts(client).getVerifyCredentials().execute();

                    display_name = account.getDisplayName();
                    user_id = account.getUserName();

                    user_avater = account.getAvatar();
                    user_header = account.getHeader();

                    account_id = account.getId();

                    //UIを変更するために別スレッド呼び出し
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
                                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                                    if (setting_avater_gif) {

                                        //GIFアニメ再生させない
                                        Picasso.get()
                                                .load(user_avater)
                                                .resize(100, 100)
                                                .into(avater_imageView);

                                        Picasso.get()
                                                .load(user_header)
                                                .into(header_imageView);

                                    } else {

                                        //GIFアニメを再生
                                        Glide.with(getApplicationContext())
                                                .load(user_avater)
                                                .apply(new RequestOptions().override(100, 100))
                                                .into(avater_imageView);

                                        Glide.with(getApplicationContext())
                                                .load(user_header)
                                                //.apply(new RequestOptions().override(1000, 500))
                                                .into(header_imageView);

                                    }
                                }


                            } else {

                                avater_imageView.setImageResource(R.drawable.ic_person_black_24dp);
                                header_imageView.setBackgroundColor(Color.parseColor("#c8c8c8"));

                            }


                            user_account_textView.setText(display_name);
                            user_id_textView.setText("@" + user_id + "@" + finalInstance);

                            if (pref_setting.getBoolean("pref_subtitle_show", true)) {
                                //サブタイトルに名前を入れる
                                getSupportActionBar().setSubtitle(display_name + " ( @" + user_id + " / " + finalInstance + " )");
                            }

                        }
                    });


                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }


                return null;
            }

        }.execute();

        boolean friends_nico_check_box = pref_setting.getBoolean("pref_friends_nico_mode", false);


        //通知
        boolean notification_toast = pref_setting.getBoolean("pref_notification_toast", true);
        boolean notification_vibrate = pref_setting.getBoolean("pref_notification_vibrate", true);

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (notification_toast) {
            //Streaming Notification
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... string) {
                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken).useStreamingApi().build();

                    Handler handler = new Handler() {

                        @Override
                        public void onStatus(com.sys1yagi.mastodon4j.api.entity.Status status) {
                        }

                        //これ通知
                        @Override
                        public void onNotification(Notification notification) {

                            String user = notification.getAccount().getAcct();
                            String user_name = notification.getAccount().getDisplayName();
                            String notification_type = notification.getType();

                            Locale locale = Locale.getDefault();

                            Drawable drawable = getDrawable(R.drawable.ic_android_black_24dp);

                            if (notification_type.equals("mention") && locale.equals(Locale.JAPAN)) {
                                notification_type = "返信し";
                                drawable = getDrawable(R.drawable.notification_to_mention);
                            }
                            if (notification_type.equals("reblog") && locale.equals(Locale.JAPAN)) {
                                notification_type = "ブーストし";
                                drawable = getDrawable(R.drawable.notification_to_boost);
                            }
                            if (notification_type.equals("favourite") && locale.equals(Locale.JAPAN)) {
                                if (friends_nico_check_box) {
                                    notification_type = "お気に入りし";
                                    drawable = getDrawable(R.drawable.notification_to_favourite);
                                } else {
                                    notification_type = "二コり";
                                    drawable = getDrawable(R.drawable.nicoru);
                                    nicoru = true;
                                }
                            }
                            if (notification_type.equals("follow") && locale.equals(Locale.JAPAN)) {
                                notification_type = "フォローし";
                                drawable = getDrawable(R.drawable.notification_to_person);
                            }


                            String notification_string = user_name + "(" + user + ")" + "さんが" + notification_type + "ました";


                            Drawable finalDrawable = drawable;
                            String finalNotification_type = notification_type;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //カスタムトースト
                                    Toast toast = new Toast(getApplicationContext());
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.notification_toast_layout, null);
                                    TextView toast_text = layout.findViewById(R.id.notification_text);
                                    toast_text.setText(notification_string);
                                    AppCompatImageView toast_imageview = layout.findViewById(R.id.notification_icon);
                                    toast_imageview.setImageDrawable(finalDrawable);
                                    try {
                                        Animatable animatable = (Animatable) finalDrawable;
                                        animatable.start();
                                    } catch (ClassCastException e) {

                                    }
                                    toast.setView(layout);
                                    toast.setDuration(Toast.LENGTH_LONG);
                                    toast.show();

                                    //Toast.makeText(getApplicationContext(), notification_string, Toast.LENGTH_LONG).show();
                                }
                            });

                            if (notification_vibrate) {
                                long[] pattern = {100, 100, 100, 100};
                                vibrator.vibrate(pattern, -1);
                            }

                        }

                        @Override
                        public void onDelete(long l) {

                        }
                    };
                    Streaming streaming = new Streaming(client);
                    try {
                        Shutdownable shutdownable = streaming.user(handler);
                        //Thread.sleep(10000L);
                        //shutdownable.shutdown();
                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }
                    return toot_text;
                }

            }.execute();
        } else {

        }


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
/*
                                    Toast toast = new Toast(getApplicationContext());
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.notification_toast_layout, null);
                                    LinearLayout toast_linearLayout = layout.findViewById(R.id.toast_layout_root);
                                    TextView toast_text = layout.findViewById(R.id.notification_text);
                                    toast_text.setText(Html.fromHtml(user_name + "\r\n" + toot_text, Html.FROM_HTML_MODE_COMPACT));
                                    ImageView toast_imageview = layout.findViewById(R.id.notification_icon);
                                    //toast_imageview.setImageDrawable(finalDrawable);
*/

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
                        new NotificationCompat.Builder(this,"Kaisendon_1")
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

            new BroadcastReceiver(){

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getBooleanExtra("TTS",false)){
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

                            if (0 < toot.length()) {

                                textToSpeech.speak(finaltoot, textToSpeech.QUEUE_ADD, null, "messageID");

                            }
                        }

                        @Override
                        public void onNotification(@NotNull Notification notification) {/* no op */}

                        @Override
                        public void onDelete(long id) {/* no op */}
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





/*


        if (setting_avater_hidden) {

            //avater_imageView.setImageResource(R.drawable.nicoru);

        }
        //Wi-Fi
        if (setting_avater_wifi) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                if (setting_avater_gif) {

*/
/*                    //GIFアニメ再生させない
                    //アバター画像
                    Picasso.get()
                            .load(user_avater)
                            .into(avater_imageView);

                    //へっだー
                    Picasso.get()
                            .load(user_header)
                            .into(header_imageView);


                    //GIFアニメを再生
                    //アバター画像
                    Glide.with(getApplicationContext())
                            .load(user_avater)
                            .into(avater_imageView);


                    //へっだー
                    Glide.with(getApplicationContext())
                            .load(user_header)
                            .into(header_imageView);

*//*

                }
            }

        } else {
            // avater_imageView.setImageResource(R.drawable.nicoru);
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


/*
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        View progress = getLayoutInflater().inflate(R.layout.progress_ber, null);
        getSupportActionBar().setCustomView(progress, new ActionBar.LayoutParams(100,100));
*/
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

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }
*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            Intent login = new Intent(this, AccountActivity.class);
            login.putExtra("Account_ID", account_id);
            startActivity(login);

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
        } else if (id == R.id.konoAppmenu) {
            Intent login = new Intent(this, KonoAppNiTuite.class);
            startActivity(login);
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


    @Override
    protected void onStop() {
        if (networkChangeBroadcast != null){
            unregisterReceiver(networkChangeBroadcast);
        }
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
    }

}
