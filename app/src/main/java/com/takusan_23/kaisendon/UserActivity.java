package com.takusan_23.kaisendon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Relationship;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Follows;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserActivity extends AppCompatActivity {
    private final static String TAG = "TestImageGetter";
    //private TextView mTv;

    long account_id;

    String display_name = null;
    String user_account_id = null;
    String avater_url = null;
    String heander_url = null;
    String profile = null;
    String create_at = null;
    String remote = null;

    String custom_emoji_src = null;

    TextView profile_textview;

    long follow_id;

    String user_url = null;
    String final_toot_text = null;

    int follow;
    int follower;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

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


        setContentView(R.layout.activity_user);

        final android.os.Handler handler_1 = new android.os.Handler();

        Intent intent = getIntent();
        account_id = intent.getLongExtra("Account_ID", 0);

        //先に
        TextView displayname_textview = findViewById(R.id.username);
        TextView id_textview = findViewById(R.id.account_id);
        profile_textview = findViewById(R.id.profile);
        TextView user_info_textView = findViewById(R.id.user_info_textview);

        //画像
        ImageView avater = findViewById(R.id.avater_user);
        ImageView header = findViewById(R.id.header_user);

        //ボタン
        Button follower_button = findViewById(R.id.follower_button);
        Button follow_button = findViewById(R.id.follow_button);
        Button follow_request_button = findViewById(R.id.follow_request_button);
        Button toot_button = findViewById(R.id.toot_button);
        //補足情報
        LinearLayout fields_attributes_linearLayout = findViewById(R.id.fields_attributes_linearLayout);


        //設定を読み込み
        SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
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

        //背景
        ImageView background_imageView = findViewById(R.id.user_activity_background_imageview);

        if (pref_setting.getBoolean("background_image", true)) {
            Uri uri = Uri.parse(pref_setting.getString("background_image_path", ""));
            Glide.with(UserActivity.this)
                    .load(uri)
                    .into(background_imageView);
        }


        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f) != 0.0) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        // Backボタンを有効にする
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //くるくる
/*
        dialog = new ProgressDialog(UserActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("ユーザー情報を取得中 \r\n /api/v1/accounts");
*/
//        dialog.show();

        View view = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(view, "ユーザー情報を取得中 \r\n /api/v1/accounts", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(UserActivity.this);
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar,0);
        snackbar.show();


        //非同期通信でアカウント情報を取得
        String finalInstance = Instance;
        String finalAccessToken = AccessToken;

        //Icon
        Bitmap back_icon = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_arrow_back_black_24dp);


        //ImageGetter
        //カスタム絵文字
        Html.ImageGetter toot_imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                LevelListDrawable d = new LevelListDrawable();
                Drawable empty = getResources().getDrawable(R.drawable.ic_refresh_black_24dp);
                d.addLevel(0, 0, empty);
                d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());

                new LoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,source,d);

                return d;
            }
        };

        String finalInstance2 = Instance;
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .build();

                try {

                    Account accounts = new Accounts(client).getAccount(account_id).execute();

                    display_name = accounts.getDisplayName();
                    user_account_id = accounts.getUserName();
                    profile = accounts.getNote();
                    avater_url = accounts.getAvatar();
                    heander_url = accounts.getHeader();
                    //create_at = accounts.getCreatedAt();
                    remote = accounts.getAcct();

                    follow = accounts.getFollowingCount();
                    follower = accounts.getFollowersCount();


                    //時刻表記を直す
                    boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                    if (japan_timeSetting) {
                        //時差計算？
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                        //日本用フォーマット
                        SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                        try {
                            Date date = simpleDateFormat.parse(accounts.getCreatedAt());
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            //9時間足して日本時間へ
                            calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                            //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                            create_at = japanDateFormat.format(calendar.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        create_at = accounts.getCreatedAt();
                    }


                    String profile_text = Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT).toString();
                    System.out.println("でーん : " + final_toot_text);
                    String first_profile_text = null;
                    String second_profile_text = null;
                    Pattern pattern = Pattern.compile("\\:.+?\\:");
                    Matcher matcher = pattern.matcher(profile_text);
                    if (matcher.find()) {
                        first_profile_text = matcher.group();
                        second_profile_text = first_profile_text.replaceAll("\\:", "");
                        System.out.println("できた！！！ : " + second_profile_text);


                        String finalFirst_profile_text = profile;
                        String finalSecond_profile_text = second_profile_text;

                        //@マークが有るかどうかでカスタム絵文字・そうでないかを分ける
                        //Matcher attoma_ku = Pattern.compile(toot_text).matcher("[@]");



                        //カスタム絵文字
                        Account account_emoji = new Accounts(client).getAccount(account_id).doOnJson(jsonAccount -> {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonAccount);
                                JSONArray emojis = jsonObject.getJSONArray("emojis");
                                for (int i = 0; i < emojis.length(); i++) {
                                    JSONObject emojisJSONObject = emojis.getJSONObject(i);
                                    String emoji_url = emojisJSONObject.getString("url");
                                    custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    System.out.println("リンク : " + custom_emoji_src);
                                    //final_toot_text = toot_text.replaceAll("\\:", custom_emoji_src);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (custom_emoji_src != null) {
                                final_toot_text = finalFirst_profile_text.replaceAll("\\:.+?\\:", "<img src=\'" + "https://d2zoeobnny43zx.cloudfront.net/custom_emojis/images/000/002/580/original/74da2858dcb13aa9.png" + "\'>");
                            }
                            System.out.println("でーん : " + final_toot_text);
                            custom_emoji_src = null;
                        }).execute();



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayname_textview.setText(display_name);
                                displayname_textview.setTextSize(20);
                                id_textview.setText("@" + remote + "\r\n" + create_at);
                                if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                    try {
                                        profile_textview.setText((Html.fromHtml(final_toot_text, toot_imageGetter, null)));
                                    } catch (NullPointerException e) {
                                        profile_textview.setText(Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT));
                                    }
                                } else {
                                    profile_textview.setText(Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT));
                                }

                                follow_button.setText("フォロー : " + String.valueOf(follow));
                                follower_button.setText("フォロワー : " + String.valueOf(follower));

                                //タイトル
                                setTitle(display_name);

                                boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);
                                if (setting_avater_gif) {

                                    //GIFアニメ再生させない
                                    Picasso.get()
                                            .load(avater_url)
                                            .into(avater);

                                    Picasso.get()
                                            .load(heander_url)
                                            .into(header);

                                } else {

                                    //GIFアニメを再生
                                    Glide.with(UserActivity.this)
                                            .load(avater_url)
                                            .into(avater);

                                    Glide.with(UserActivity.this)
                                            .load(heander_url)
                                            .into(header);
                                }
                            }
                        });
                    }

                    //正規表現にマッチしなかった場合
                    if (!matcher.find()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayname_textview.setText(display_name);
                                displayname_textview.setTextSize(20);
                                id_textview.setText("@" + remote + "\r\n" + create_at);
                                if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                    try {
                                        profile_textview.setText((Html.fromHtml(final_toot_text, toot_imageGetter, null)));
                                    } catch (NullPointerException e) {
                                        profile_textview.setText(Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT));
                                    }
                                } else {
                                    profile_textview.setText(Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT));
                                }

                                follow_button.setText("フォロー : " + String.valueOf(follow));
                                follower_button.setText("フォロワー : " + String.valueOf(follower));

                                //タイトル
                                setTitle(display_name);

                                boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);
                                if (setting_avater_gif) {

                                    //GIFアニメ再生させない
                                    Picasso.get()
                                            .load(avater_url)
                                            .into(avater);

                                    Picasso.get()
                                            .load(heander_url)
                                            .into(header);

                                } else {

                                    //GIFアニメを再生
                                    Glide.with(UserActivity.this)
                                            .load(avater_url)
                                            .into(avater);

                                    Glide.with(UserActivity.this)
                                            .load(heander_url)
                                            .into(header);
                                }
                            }
                        });
                    }
                        //friends.nicoモードかな？
                        boolean frenico_mode = pref_setting.getBoolean("setting_friends_nico_mode", true);
                        //Chrome Custom Tab
                        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);

                        final String[] nico_url = {null};

                        //Json解析して"nico_url"取得
                        Account account_nico_url = new Accounts(client).getAccount(account_id).doOnJson(jsonString -> {
                                    //System.out.println(jsonString);
                                    //String string_ = "{\"int array\":[100,200,300],\"boolean\":true,\"string\":\"string\",\"object\":{\"object_1\":1,\"object_3\":3,\"object_2\":2},\"null\":null,\"array\":[1,2,3],\"long\":18000305032230531,\"int\":100,\"double\":10.5}";
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(jsonString);

                                        nico_url[0] = jsonObject.getString("nico_url");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                        ).execute();


                        try {
                            //URLあるよ
                            if (frenico_mode && !nico_url[0].equals("null")) {
                                //ニコニコURLへ
                                Button button = findViewById(R.id.button3);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        button.setText("ニコニコ");
                                    }
                                });

                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (chrome_custom_tabs) {

                                            String custom = CustomTabsHelper.getPackageNameToUse(UserActivity.this);

                                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                            CustomTabsIntent customTabsIntent = builder.build();
                                            customTabsIntent.intent.setPackage(custom);
                                            customTabsIntent.launchUrl((Activity) UserActivity.this, Uri.parse(nico_url[0]));
                                            //無効
                                        } else {
                                            Uri uri = Uri.parse(nico_url[0]);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                            //URLなかった
                        } catch (RuntimeException e) {
                            Button button = findViewById(R.id.button3);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button.setText("Web");
                                }
                            });
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (chrome_custom_tabs) {

                                        String custom = CustomTabsHelper.getPackageNameToUse(UserActivity.this);

                                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                        CustomTabsIntent customTabsIntent = builder.build();
                                        customTabsIntent.intent.setPackage(custom);
                                        customTabsIntent.launchUrl((Activity) UserActivity.this, Uri.parse("https://" + finalInstance + "/" + "@" + user_account_id));
                                        //無効
                                    } else {
                                        Uri uri = Uri.parse(nico_url[0]);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + finalInstance + "/" + "@" + user_account_id));
                                        startActivity(intent);
                                    }
                                }
                            });
                        }


                        //補足情報
                        //Json解析
                        Account fields_attributes_account = new Accounts(client).getAccount(account_id).doOnJson(fields_attributes_account_jsonString -> {
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(fields_attributes_account_jsonString);

                                        //補足情報取得
                                        //補足情報まで案内
                                        JSONArray fields = jsonObject.getJSONArray("fields");
                                        //同じコードを書きたくない？のでwhileつかう
                                        int count = 0;
                                        while (count <= fields.length()) {

                                            JSONObject fields_attributes_account_jsonObject = fields.getJSONObject(count);
                                            //名前を取得
                                            String name = fields_attributes_account_jsonObject.getString("name");
                                            //情報
                                            String value = fields_attributes_account_jsonObject.getString("value");
                                            //レイアウトをつくる
                                            //調子悪いのでUIスレッドで
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    LinearLayout fields_attributes_content = new LinearLayout(UserActivity.this);
                                                    fields_attributes_content.setOrientation(LinearLayout.HORIZONTAL);
                                                    //テキストビュー
                                                    TextView fields_attributes_name_textview = new TextView(UserActivity.this);
                                                    TextView fields_attributes_value_textview = new TextView(UserActivity.this);
                                                    LinearLayout.LayoutParams fields_attributes_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                                    fields_attributes_params.weight = 1;
                                                    LinearLayout.LayoutParams fields_attributes_params_2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                                    fields_attributes_params_2.weight = 2;
                                                    //名前
                                                    fields_attributes_name_textview.setAutoLinkMask(Linkify.ALL);
                                                    fields_attributes_name_textview.setText(Html.fromHtml(name, Html.FROM_HTML_MODE_COMPACT));
                                                    fields_attributes_name_textview.setTextSize(18);
                                                    //fields_attributes_name_textview.setBackgroundColor(Color.parseColor("#999999"));
                                                    fields_attributes_name_textview.setLayoutParams(fields_attributes_params_2);
                                                    //説明
                                                    fields_attributes_value_textview.setAutoLinkMask(Linkify.ALL);
                                                    fields_attributes_value_textview.setText(Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT));
                                                    fields_attributes_value_textview.setTextSize(18);
                                                    //fields_attributes_value_textview.setBackgroundColor(Color.parseColor("#cccccc"));
                                                    fields_attributes_value_textview.setLayoutParams(fields_attributes_params);
                                                    //空白
                                                    Space sp = new Space(UserActivity.this);
                                                    //セット
                                                    fields_attributes_content.addView(fields_attributes_name_textview);
                                                    fields_attributes_content.addView(fields_attributes_value_textview);
                                                    fields_attributes_linearLayout.addView(fields_attributes_content);
                                                    fields_attributes_linearLayout.addView(sp, new LinearLayout.LayoutParams(20, 1));
                                                }
                                            });
                                            count++;
                                        }


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                        ).execute();

                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                snackbar.dismiss();
                //dialog.dismiss();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //ふぉろーされてるか
        AsyncTask<String, Void, String> asyncTask_follow_check = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                String url = "https://" + finalInstance + "/api/v1/accounts/relationships/?stream=user&access_token=" + finalAccessToken;

                //パラメータを設定
                HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
                builder.addQueryParameter("id", String.valueOf(account_id));
                String final_url = builder.build().toString();

                //作成
                Request request = new Request.Builder()
                        .url(final_url)
                        .get()
                        .build();

                //GETリクエスト
                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //JSON化
                        //System.out.println("レスポンス : " + response.body().string());
                        String response_string = response.body().string();
                        try {
                            JSONArray jsonArray = new JSONArray(response_string);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            boolean followed_by = jsonObject.getBoolean("followed_by");
                            boolean blocking = jsonObject.getBoolean("blocking");
                            boolean muting = jsonObject.getBoolean("muting");

                            String follow = null;
                            String block = null;
                            String mute = null;


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    user_info_textView.setText("フォロバ : " + yes_no_check(followed_by, follow, UserActivity.this) + "\r\n" + "ブロック : " + yes_no_check(blocking, block, UserActivity.this) + " / " + "ミュート : " + yes_no_check(muting, mute, UserActivity.this));
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //ボタンクリック
        follow_button.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Intent follow = new Intent(UserActivity.this, UserFollowActivity.class);
                follow.putExtra("account_id", account_id);
                follow.putExtra("follow_follower", 1);
                startActivity(follow);
            }
        });

        follower_button.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Intent follower = new Intent(UserActivity.this, UserFollowActivity.class);
                follower.putExtra("account_id", account_id);
                follower.putExtra("follow_follower", 2);
                startActivity(follower);
            }
        });

        toot_button.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Intent follower = new Intent(UserActivity.this, UserFollowActivity.class);
                follower.putExtra("account_id", account_id);
                follower.putExtra("follow_follower", 3);
                startActivity(follower);
            }
        });


        //フォローする
        String finalInstance1 = Instance;
        String finalAccessToken1 = AccessToken;
        follow_request_button.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                //フォローダイアログを出す？
                boolean follow_dialog = pref_setting.getBoolean("pref_follow_dialog", false);

                if (follow_dialog) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserActivity.this);
                    alertDialog.setTitle(R.string.confirmation);
                    alertDialog.setMessage(display_name + "(@ " + user_account_id + ")" + "をフォローしますか？ \r\n /api/v1/follows" + "(" + remote + ")");
                    alertDialog.setPositiveButton(R.string.follow, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                                @Override
                                protected String doInBackground(String... string) {
                                    com.sys1yagi.mastodon4j.api.entity.auth.AccessToken accessToken = new AccessToken();
                                    accessToken.setAccessToken(finalAccessToken);

                                    //非同期通信
                                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                                            .accessToken(finalAccessToken)
                                            .build();


                                    //リモートフォロー用のURL取得

                                    try {
                                        Account accounts = new Accounts(client).getAccount(account_id).execute();

                                        remote = accounts.getAcct();
                                        follow_id = accounts.getId();
                                        display_name = accounts.getDisplayName();

                                        //Account follow = new Follows(client).postRemoteFollow("takusan_23@pawoo.net").execute();
                                    } catch (Mastodon4jRequestException e) {
                                        e.printStackTrace();
                                    }

                                    //リモートフォローかそうじゃないか
                                    //@があるかどうか
                                    Matcher m = Pattern.compile("[@]").matcher(remote);
                                    if (m.find()) {
                                        System.out.println("@@@@@@@@@@@@@");

                                        try {
                                            Account follow = new Follows(client).postRemoteFollow(remote).execute();
                                        } catch (Mastodon4jRequestException e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        System.out.println("-------------------");


                                        try {
                                            Relationship accounts = new Accounts(client).postFollow(follow_id).execute();
                                        } catch (Mastodon4jRequestException e) {
                                            e.printStackTrace();
                                        }

                                    }


                                    return display_name;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    Toast.makeText(UserActivity.this, "フォローしました : " + result, Toast.LENGTH_SHORT).show();
                                }

                            }.execute();


                        }
                    });
                    //フォローしない
                    alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.create().show();

                } else {

                    AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                        @Override
                        protected String doInBackground(String... string) {
                            //非同期通信
                            MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken).build();


                            //リモートフォロー用のURL取得
                            try {

                                Account accounts = new Accounts(client).getAccount(account_id).execute();
                                remote = accounts.getAcct();
                                follow_id = accounts.getId();

                            } catch (Mastodon4jRequestException e) {
                                e.printStackTrace();
                            }

                            //リモートフォローかそうじゃないか
                            //@があるかどうか
                            Matcher m = Pattern.compile(remote).matcher("[@]");
                            if (m.find()) {
                                try {
                                    Account follow = new Follows(client).postRemoteFollow(remote).execute();
                                } catch (Mastodon4jRequestException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    Relationship accounts = new Accounts(client).postFollow(follow_id).execute();
                                } catch (Mastodon4jRequestException e) {
                                    e.printStackTrace();
                                }
                            }

                            return display_name;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            Toast.makeText(UserActivity.this, "フォローしました : " + result, Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static String yes_no_check(Boolean boomelan, String string, Context context) {
        if (boomelan) {
            string = context.getString(R.string.yes);
        } else {
            string = context.getString(R.string.no);
        }
        return string;
    }

    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];
            Log.d(TAG, "doInBackground " + source);
            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute drawable " + mDrawable);
            Log.d(TAG, "onPostExecute bitmap " + bitmap);
            if (bitmap != null) {
                BitmapDrawable d = new BitmapDrawable(bitmap);
                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, 40, 40);
                mDrawable.setLevel(1);
                // i don't know yet a better way to refresh TextView
                // mTv.invalidate() doesn't work as expected
                CharSequence t = profile_textview.getText();
                profile_textview.setText(t);
            }
        }
    }

}
