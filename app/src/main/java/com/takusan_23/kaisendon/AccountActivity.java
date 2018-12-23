package com.takusan_23.kaisendon;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AccountActivity extends AppCompatActivity {
    private final static String TAG = "TestImageGetter";

    long account_id;

    String display_name = null;
    String user_account_id = null;
    String avater_url = null;
    String heander_url = null;
    String profile = null;
    String create_at = null;


    //カスタム絵文字関係
    String final_toot_text;
    String custom_emoji_src;
    boolean avater_emoji = false;
    String avater_custom_emoji_src;


    long account_id_button;

    int follow;
    int follower;

    private ProgressDialog dialog;

    TextView profile_textview;


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

        //先に
        TextView displayname_textview = findViewById(R.id.username);
        TextView id_textview = findViewById(R.id.account_id);
        profile_textview = findViewById(R.id.profile);

        //画像
        ImageView avater = findViewById(R.id.avater_user);
        ImageView header = findViewById(R.id.header_user);

        //ボタン
        Button follower_button = findViewById(R.id.follower_button);
        Button follow_button = findViewById(R.id.follow_button);
        Button toot_button = findViewById(R.id.toot_button);
        Button follow_request_button = findViewById(R.id.follow_request_button);
        follow_request_button.setText(R.string.edit);



        //補足情報
        LinearLayout fields_attributes_linearLayout = findViewById(R.id.fields_attributes_linearLayout);


        //プリファレンス
        SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
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

        // Backボタンを有効にする
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //くるくる
        View view = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(view, getString(R.string.loading_user_info)+"\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(AccountActivity.this);
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar.show();


        //非同期通信でアカウント情報を取得
        String finalInstance = Instance;
        String finalAccessToken = AccessToken;

        //Icon
        Bitmap back_icon = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_arrow_back_black_24dp);

        //どうでもいい
//
//        SpannableString spannableString = new SpannableString("アイコンテスト : ");
//        spannableString.setSpan(new ImageSpan(AccountActivity.this, R.mipmap.ic_launcher), 7, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        displayname_textview.setText(spannableString);
        //
        //ImageGetter
        //カスタム絵文字
        Html.ImageGetter toot_imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                LevelListDrawable d = new LevelListDrawable();
                Drawable empty = getResources().getDrawable(R.drawable.ic_refresh_black_24dp);
                d.addLevel(0, 0, empty);
                d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());

                new LoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, source, d);

                return d;
            }
        };


        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .build();

                try {

                    Account account = new Accounts(client).getVerifyCredentials().execute();

                    display_name = account.getDisplayName();
                    user_account_id = account.getUserName();
                    profile = account.getNote();
                    avater_url = account.getAvatar();
                    heander_url = account.getHeader();
                    //create_at = account.getCreatedAt();

                    follow = account.getFollowingCount();
                    follower = account.getFollowersCount();

                    account_id_button = account.getId();



                    //時刻表記を直す
                    boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                    if (japan_timeSetting) {
                        //時差計算？
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                        //日本用フォーマット
                        SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                        try {
                            Date date = simpleDateFormat.parse(account.getCreatedAt());
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
                        create_at = account.getCreatedAt();
                    }


                    //@マークが有るかどうかでカスタム絵文字・そうでないかを分ける
                    //Matcher attoma_ku = Pattern.compile(toot_text).matcher("[@]");
 /*                       if (finalSecond_profile_text != null) {
                            Request request = new Request.Builder()
                                    .url("https://" + finalInstance + "/api/v1/accounts/" + String.valueOf(account_id))
                                    .get()
                                    .build();

                            //GETリクエスト
                            OkHttpClient client_account = new OkHttpClient();
                            client_account.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    //JSON化
                                    //System.out.println("レスポンス : " + response.body().string());
                                    String response_string = response.body().string();
                                    System.out.println("レスポンス : " + response_string);
                                    try {
                                        JSONObject jsonObject = new JSONObject(response_string);
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
                                }
                            });
                            if (custom_emoji_src != null) {
                                final_toot_text = finalFirst_profile_text.replaceAll("\\:.+?\\:", custom_emoji_src);
                            }
                            //final_toot_text = finalFirst_profile_text.replaceAll("\\:.+?\\:", "<img src=\'" + "https://img.pawoo.net/custom_emojis/images/000/000/021/original/15e4392aa8b03dda.png" + "\'>");
                            custom_emoji_src = null;

                        }*/
                } catch (Mastodon4jRequestException e1) {
                    e1.printStackTrace();
                }

                handler_1.post(new Runnable() {
                    @Override
                    public void run() {
                        displayname_textview.setText(display_name);
                        displayname_textview.setTextSize(20);
                        id_textview.setText("@" + user_account_id + "@" + finalInstance + "\r\n" + create_at);
                        if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                            try {
                                profile_textview.setText((Html.fromHtml(profile, toot_imageGetter, null)));
                            } catch (NullPointerException e) {
                                profile_textview.setText(Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT));
                            }
                        } else {
                            profile_textview.setText(Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT));
                        }


                        follow_button.setText("フォロー : " + String.valueOf(follow));
                        follower_button.setText("フォロワー : " + String.valueOf(follower));

                        //タイトル
                        getSupportActionBar().setTitle(display_name);
                        getSupportActionBar().setSubtitle("@" + user_account_id + "@" + finalInstance);

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
                            Glide.with(AccountActivity.this)
                                    .load(avater_url)
                                    .into(avater);

                            Glide.with(AccountActivity.this)
                                    .load(heander_url)
                                    .into(header);
                        }
                    }
                });


                //friends.nicoモードかな？
                boolean frenico_mode = pref_setting.getBoolean("setting_friends_nico_mode", true);
                //Chrome Custom Tab
                boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);

                final String[] nico_url = {null};

                //Json解析
                try {
                    Account account_nico_url = new Accounts(client).getVerifyCredentials().doOnJson(jsonString -> {
                                //System.out.println(jsonString);
                                //String string_ = "{\"int array\":[100,200,300],\"boolean\":true,\"string\":\"string\",\"object\":{\"object_1\":1,\"object_3\":3,\"object_2\":2},\"null\":null,\"array\":[1,2,3],\"long\":18000305032230531,\"int\":100,\"double\":10.5}";
                                JsonParser parser = new JsonParser();
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(jsonString);

                                    nico_url[0] = jsonObject.getString("nico_url");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                    ).execute();
                } catch (Mastodon4jRequestException e1) {
                    e1.printStackTrace();
                }


                //URLあるよ
                if (frenico_mode && nico_url[0] != null) {
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

                                String custom = CustomTabsHelper.getPackageNameToUse(AccountActivity.this);

                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.intent.setPackage(custom);
                                customTabsIntent.launchUrl((Activity) AccountActivity.this, Uri.parse(nico_url[0]));
                                //無効
                            } else {
                                Uri uri = Uri.parse(nico_url[0]);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        }
                    });
                    //URLなかった
                } else {
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

                                String custom = CustomTabsHelper.getPackageNameToUse(AccountActivity.this);

                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.intent.setPackage(custom);
                                customTabsIntent.launchUrl((Activity) AccountActivity.this, Uri.parse(nico_url[0]));
                                //無効
                            } else {
                                Uri uri = Uri.parse(nico_url[0]);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        }
                    });
                }

                //補足情報
                //Json解析
                try {
                    Account fields_attributes_account = new Accounts(client).getVerifyCredentials().doOnJson(fields_attributes_account_jsonString -> {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(fields_attributes_account_jsonString);

                                    //補足情報取得

                                    //補足情報まで案内
                                    JSONObject source = jsonObject.getJSONObject("source");
                                    JSONArray fields = source.getJSONArray("fields");
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
                                                LinearLayout fields_attributes_content = new LinearLayout(AccountActivity.this);
                                                fields_attributes_content.setOrientation(LinearLayout.HORIZONTAL);
                                                //テキストビュー
                                                TextView fields_attributes_name_textview = new TextView(AccountActivity.this);
                                                TextView fields_attributes_value_textview = new TextView(AccountActivity.this);
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
                                                Space sp = new Space(AccountActivity.this);
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
                } catch (Mastodon4jRequestException e1) {
                    e1.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                snackbar.dismiss();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //ボタンクリック
        follow_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent follow = new Intent(AccountActivity.this, UserFollowActivity.class);
                follow.putExtra("account_id", account_id_button);
                follow.putExtra("follow_follower", 1);
                startActivity(follow);
            }
        });

        follower_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent follower = new Intent(AccountActivity.this, UserFollowActivity.class);
                follower.putExtra("account_id", account_id_button);
                follower.putExtra("follow_follower", 2);
                startActivity(follower);
            }
        });

        toot_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent follower = new Intent(AccountActivity.this, UserFollowActivity.class);
                follower.putExtra("account_id", account_id_button);
                follower.putExtra("follow_follower", 3);
                startActivity(follower);
            }
        });

        //アカウント情報を更新する
        follow_request_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent account_info_update = new Intent(AccountActivity.this,AccountInfoUpdateActivity.class);
                startActivity(account_info_update);
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
