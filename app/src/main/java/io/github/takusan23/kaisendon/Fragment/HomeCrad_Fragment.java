package io.github.takusan23.kaisendon.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Card;
import com.sys1yagi.mastodon4j.api.entity.Emoji;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Notifications;
import com.sys1yagi.mastodon4j.api.method.Public;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Streaming;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class HomeCrad_Fragment extends Fragment {
    View view;

    Shutdownable shutdownable;

    private static final int RESULT_PICK_IMAGEFILE = 1001;

    int select_color;

    SharedPreferences pref_setting;

    private final int REQUEST_PERMISSION = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.activity_homecard, container, false);

        return view;
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //設定
        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        //アクセストークンを変更してる場合のコード
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

        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }


        LinearLayout linearLayout_ = (LinearLayout) view.findViewById(R.id.cardview_linear);
        linearLayout_.removeAllViews();
        int cardSize = 1; // カードの枚数

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        //アカウント
        LinearLayout account_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        LinearLayout account_main = account_linearLayout.findViewById(R.id.cardview_lineaLayout_main);
        TextView account_textview = account_linearLayout.findViewById(R.id.cardview_textview);

        Drawable account_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sentiment_neutral_black_24dp, null);
        account_icon.setBounds(0, 0, account_icon.getIntrinsicWidth(), account_icon.getIntrinsicHeight());

        account_textview.setText(R.string.account);
        account_textview.setCompoundDrawables(account_icon, null, null, null);

        ProgressBar progressBar_account = new ProgressBar(getContext());
        account_main.addView(progressBar_account);
        //ここにいれる
        LinearLayout account_layout = new LinearLayout(account_linearLayout.getContext());
        account_main.addView(account_layout);
        //ふぉよー・ふぉよわー
        LinearLayout follow_layout = new LinearLayout(account_linearLayout.getContext());
        follow_layout.setOrientation(LinearLayout.HORIZONTAL);
        account_main.addView(follow_layout);

        TextView account_text_textview = new TextView(account_linearLayout.getContext());
        ImageView account_avater_imageview = new ImageView(account_linearLayout.getContext());

        TextView account_follow_textview = new TextView(account_linearLayout.getContext());
        TextView account_follower_textview = new TextView(account_linearLayout.getContext());

        //取得
        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        //通信量節約
        boolean setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false);
        //Wi-Fi接続時は有効？
        boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
        //GIFを再生するか？
        boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);

        String finalInstance2 = Instance;
        String finalAccessToken1 = AccessToken;
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance2, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken1).build();

                try {
                    Account account = new Accounts(client).getVerifyCredentials().execute();

                    String display_name = account.getDisplayName();
                    String user_id = account.getUserName();
                    String profile = account.getNote();

                    int follow = account.getFollowingCount();
                    int follower = account.getFollowersCount();

                    String user_avater = account.getAvatar();
                    String user_header = account.getHeader();

                    if (getActivity() != null) {

                        //UIを変更するために別スレッド呼び出し
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //表示設定
                                if (setting_avater_hidden) {
                                    account_avater_imageview.setImageResource(R.drawable.ic_person_black_24dp);
                                }
                                //Wi-Fi
                                if (setting_avater_wifi) {
                                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                                        if (setting_avater_gif) {
                                            //GIFアニメ再生させない
                                            Picasso.get()
                                                    .load(user_avater)
                                                    .resize(100, 100)
                                                    .into(account_avater_imageview);
                                        } else {
                                            //GIFアニメを再生
                                            Glide.with(getContext())
                                                    .load(user_avater)
                                                    .apply(new RequestOptions().override(100, 100))
                                                    .into(account_avater_imageview);
                                        }
                                    }
                                } else {
                                    account_avater_imageview.setImageResource(R.drawable.ic_person_black_24dp);
                                }

                                account_main.removeView(progressBar_account);

                                Drawable follow_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done_black_24dp, null);
                                follow_icon.setBounds(0, 0, follow_icon.getIntrinsicWidth(), follow_icon.getIntrinsicHeight());
                                Drawable follower_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done_all_black_24dp, null);
                                follower_icon.setBounds(0, 0, follower_icon.getIntrinsicWidth(), follower_icon.getIntrinsicHeight());


                                String user_strnig = display_name + "\r\n" + "@" + user_id + "@" + finalInstance2 + "\r\n\r\n" + profile;
                                account_text_textview.setText(Html.fromHtml(user_strnig, Html.FROM_HTML_MODE_COMPACT));
                                account_follow_textview.setText(getString(R.string.follow) + " : " + String.valueOf(follow));
                                account_follow_textview.setCompoundDrawablesWithIntrinsicBounds(follow_icon, null, null, null);
                                account_follower_textview.setText(getString(R.string.follower) + " : " + String.valueOf(follower));
                                account_follower_textview.setCompoundDrawablesWithIntrinsicBounds(follower_icon, null, null, null);


                            }
                        });
                    }
                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //AsyncTackを並列実行

        account_layout.addView(account_avater_imageview);
        account_layout.addView(account_text_textview);
        follow_layout.addView(account_follow_textview);
        follow_layout.addView(account_follower_textview);


        //Toot
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        CardView cardView = (CardView) linearLayout.findViewById(R.id.cardview);
        TextView textBox = (TextView) linearLayout.findViewById(R.id.cardview_textview);
        //ここについか
        LinearLayout main_toot = linearLayout.findViewById(R.id.cardview_lineaLayout_main);

        Drawable title_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_create_black_24dp_black, null);
        title_icon.setBounds(0, 0, title_icon.getIntrinsicWidth(), title_icon.getIntrinsicHeight());

        EditText editText = new EditText(linearLayout.getContext());
        Button button = new Button(linearLayout.getContext());

        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setHint(R.string.imananisiteru);

        LinearLayout.LayoutParams layoutParams_button = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //Gravity setGravityは中の文字に反映されてしまう
        layoutParams_button.gravity = Gravity.END;
        button.setLayoutParams(layoutParams_button);
        button.setText(R.string.toot);
        button.setCompoundDrawables(title_icon, null, null, null);


        String finalAccessToken = AccessToken;
        String finalInstance1 = Instance;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String toot_text = editText.getText().toString();

                //ダイアログ出すかどうか
                boolean accessToken_boomelan = pref_setting.getBoolean("pref_toot_dialog", false);
                if (accessToken_boomelan)

                {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setTitle(R.string.confirmation);
                    alertDialog.setMessage(R.string.toot_dialog);
                    alertDialog.setPositiveButton(R.string.toot, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //トゥートああああ

                            new AsyncTask<String, Void, String>() {

                                @Override
                                protected String doInBackground(String... params) {
                                    AccessToken accessToken = new AccessToken();
                                    accessToken.setAccessToken(finalAccessToken);

                                    MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                    RequestBody requestBody = new FormBody.Builder()
                                            .add("status", toot_text)
                                            .build();

                                    System.out.println("=====" + client.post("statuses", requestBody));


                                    return toot_text;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    Toast.makeText(getContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                                }

                            }.execute();
                            editText.setText(""); //投稿した後に入力フォームを空にする

                        }
                    });
                    alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.create().show();

                } else {
                    //トゥートああああ
                    new AsyncTask<String, Void, String>() {

                        @Override
                        protected String doInBackground(String... params) {
                            AccessToken accessToken = new AccessToken();
                            accessToken.setAccessToken(finalAccessToken);

                            MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                            RequestBody requestBody = new FormBody.Builder()
                                    .add("status", toot_text)
                                    .build();

                            System.out.println("=====" + client.post("statuses", requestBody));


                            return toot_text;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            Toast.makeText(getContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                        }

                    }.execute();
                    editText.setText(""); //投稿した後に入力フォームを空にする
                }
            }
        });

        main_toot.addView(editText);
        main_toot.addView(button);

        textBox.setText(R.string.toot);
        textBox.setCompoundDrawables(title_icon, null, null, null);


        //インスタンス情報
        LayoutInflater inflater_ = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout instance_linearLayout = (LinearLayout) inflater_.inflate(R.layout.cardview_layout, null);
        TextView instance_title = instance_linearLayout.findViewById(R.id.cardview_textview);
        //ここに追加
        LinearLayout main_instance = instance_linearLayout.findViewById(R.id.cardview_lineaLayout_main);

        Drawable instance_title_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_public_black_24dp, null);
        instance_title_icon.setBounds(0, 0, instance_title_icon.getIntrinsicWidth(), instance_title_icon.getIntrinsicHeight());

        instance_title.setText(R.string.instance_info);
        instance_title.setCompoundDrawables(instance_title_icon, null, null, null);

        //このカードで使うTextView
        TextView instance_title_textiew = new TextView(instance_linearLayout.getContext());
        TextView instance_description_textview = new TextView(instance_linearLayout.getContext());
        TextView instance_total_user_textview = new TextView(instance_linearLayout.getContext());
        TextView instance_total_toot_textview = new TextView(instance_linearLayout.getContext());

        //activeもだすわ
        TextView instance_active_title_textview = new TextView(instance_linearLayout.getContext());
        LinearLayout active_LinearLayout = new LinearLayout(main_instance.getContext());
        active_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView instance_active_status_textview = new TextView(main_instance.getContext());
        TextView instance_active_logins_textview = new TextView(main_instance.getContext());
        TextView instance_active_registrations = new TextView(main_instance.getContext());

        //読み込み中
        //くるくる
        ProgressBar progressBar_info = new ProgressBar(getContext());
        instance_title_textiew.setText(R.string.loading);
        main_instance.addView(progressBar_info);

        //Mastodon 統計APIを叩いて
        //JSONを解析
        String finalInstance = Instance;
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                URL url = null;
                HttpURLConnection connection = null;
                String url_link = "https://" + finalInstance + "/api/v1/instance/";

                try {

                    url = new URL(url_link);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    final InputStream in = connection.getInputStream();
                    String encoding = connection.getContentEncoding();
                    if (null == encoding) {
                        encoding = "UTF-8";
                    }
                    final InputStreamReader inReader = new InputStreamReader(in, encoding);
                    final BufferedReader bufReader = new BufferedReader(inReader);
                    StringBuilder response = new StringBuilder();
                    String line = null;
                    // 1行ずつ読み込む
                    while ((line = bufReader.readLine()) != null) {
                        response.append(line);
                    }
                    bufReader.close();
                    inReader.close();
                    in.close();

                    // 受け取ったJSON文字列をパース
                    JSONObject jsonObject = new JSONObject(response.toString());
                    //Status
                    JSONObject stats = jsonObject.getJSONObject("stats");

                    //タイトル
                    String title = jsonObject.getString("title");
                    //説明
                    String description = jsonObject.getString("description");
                    //バージョン？
                    String version = jsonObject.getString("version");
                    //ユーザー数
                    String user_total = stats.getString("user_count");
                    //トータルトゥート
                    String toot_total = stats.getString("status_count");

                    if (getActivity() != null) {
                        //UI変更
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //ICON
                                Drawable title_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_public_black_24dp, null);
                                title_icon.setBounds(0, 0, title_icon.getIntrinsicWidth(), title_icon.getIntrinsicHeight());
                                Drawable description_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_description_black_24dp, null);
                                description_icon.setBounds(0, 0, description_icon.getIntrinsicWidth(), description_icon.getIntrinsicHeight());
                                Drawable user_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_people_black_24dp, null);
                                user_icon.setBounds(0, 0, user_icon.getIntrinsicWidth(), user_icon.getIntrinsicHeight());
                                Drawable toot_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_create_black_24dp_black, null);
                                toot_icon.setBounds(0, 0, toot_icon.getIntrinsicWidth(), toot_icon.getIntrinsicHeight());

                                //入れる
                                instance_title_textiew.setText(getString(R.string.instance_name) + " : " + title + " (" + version + ")");
                                instance_title_textiew.setCompoundDrawables(title_icon, null, null, null);
                                instance_description_textview.setText(getString(R.string.instance_description) + " : " + description);
                                instance_description_textview.setCompoundDrawables(description_icon, null, null, null);
                                instance_total_user_textview.setText(getString(R.string.instance_user_count) + " : " + user_total);
                                instance_total_user_textview.setCompoundDrawables(user_icon, null, null, null);
                                instance_total_toot_textview.setText(getString(R.string.instance_status_count) + " : " + toot_total);
                                instance_total_toot_textview.setCompoundDrawables(toot_icon, null, null, null);
                                //Gravity
                                instance_title_textiew.setGravity(Gravity.CENTER_VERTICAL);
                                instance_description_textview.setGravity(Gravity.CENTER_VERTICAL);
                                instance_total_user_textview.setGravity(Gravity.CENTER_VERTICAL);
                                instance_total_toot_textview.setGravity(Gravity.CENTER_VERTICAL);

                                //レイアウトに入れる
                                main_instance.addView(instance_title_textiew);
                                main_instance.addView(instance_description_textview);
                                main_instance.addView(instance_total_user_textview);
                                main_instance.addView(instance_total_toot_textview);

                            }
                        });
                    }
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //Activeもだすぜ
        AsyncTask<String, Void, String> asyncTask_active = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                URL url = null;
                HttpURLConnection connection = null;
                String url_link = "https://" + finalInstance + "/api/v1/instance/activity";

                try {

                    url = new URL(url_link);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    final InputStream in = connection.getInputStream();
                    String encoding = connection.getContentEncoding();
                    if (null == encoding) {
                        encoding = "UTF-8";
                    }
                    final InputStreamReader inReader = new InputStreamReader(in, encoding);
                    final BufferedReader bufReader = new BufferedReader(inReader);
                    StringBuilder response = new StringBuilder();
                    String line = null;
                    // 1行ずつ読み込む
                    while ((line = bufReader.readLine()) != null) {
                        response.append(line);
                    }
                    bufReader.close();
                    inReader.close();
                    in.close();

                    // 受け取ったJSON文字列をパース
//                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray datas = new JSONArray(response.toString());
                    //Status
                    JSONObject stats = datas.getJSONObject(0);

                    //すてーたす
                    String toot_total = stats.getString("statuses");
                    //ログイン
                    String user_login = stats.getString("logins");
                    //登録?
                    String registrations = stats.getString("registrations");


                    if (getActivity() != null) {
                        //UI変更
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //読み込みくるくる終了
                                main_instance.removeView(progressBar_info);

                                Drawable title_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_timeline_black_24dp, null);
                                title_icon.setBounds(0, 0, title_icon.getIntrinsicWidth(), title_icon.getIntrinsicHeight());
                                Drawable description_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_create_black_24dp_black, null);
                                description_icon.setBounds(0, 0, description_icon.getIntrinsicWidth(), description_icon.getIntrinsicHeight());
                                Drawable user_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_people_black_24dp, null);
                                user_icon.setBounds(0, 0, user_icon.getIntrinsicWidth(), user_icon.getIntrinsicHeight());
                                Drawable active_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_whatshot_black_24dp, null);
                                active_icon.setBounds(0, 0, active_icon.getIntrinsicWidth(), active_icon.getIntrinsicHeight());
                                Drawable people_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_person_add_black_24dp, null);
                                people_icon.setBounds(0, 0, people_icon.getIntrinsicWidth(), people_icon.getIntrinsicHeight());

                                LinearLayout.LayoutParams textview_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                textview_params.weight = 1;

                                instance_active_title_textview.setText(getString(R.string.instance_statistics) + "(" + getString(R.string.this_week) + ")");
                                instance_active_title_textview.setCompoundDrawables(title_icon, null, null, null);
                                instance_active_title_textview.setLayoutParams(textview_params);
                                instance_active_status_textview.setText(getString(R.string.instance_statistics_status) + "\r\n" + toot_total);
                                instance_active_status_textview.setCompoundDrawables(user_icon, null, null, null);
                                instance_active_status_textview.setLayoutParams(textview_params);
                                instance_active_logins_textview.setText(getString(R.string.instance_statistics_login) + "\r\n" + user_login);
                                instance_active_logins_textview.setCompoundDrawables(active_icon, null, null, null);
                                instance_active_logins_textview.setLayoutParams(textview_params);
                                instance_active_registrations.setText(getString(R.string.instance_statistics_registrations) + "\r\n" + registrations);
                                instance_active_registrations.setCompoundDrawables(people_icon, null, null, null);
                                instance_active_registrations.setLayoutParams(textview_params);

                                //Gravity
                                instance_active_title_textview.setGravity(Gravity.CENTER_VERTICAL);
                                instance_active_status_textview.setGravity(Gravity.CENTER_VERTICAL);
                                instance_active_logins_textview.setGravity(Gravity.CENTER_VERTICAL);
                                instance_active_registrations.setGravity(Gravity.CENTER_VERTICAL);

                                //レイアウトに入れる
                                main_instance.addView(instance_active_title_textview);
                                main_instance.addView(active_LinearLayout);
                                active_LinearLayout.addView(instance_active_status_textview);
                                active_LinearLayout.addView(instance_active_logins_textview);
                                active_LinearLayout.addView(instance_active_registrations);


                            }
                        });
                    }

                    return null;

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //取得、解析後にaddViewする
/*
        main_instance.addView(instance_description_textview);
        main_instance.addView(instance_total_user_textview);
        main_instance.addView(instance_total_toot_textview);
*/


        //Home ?
        LinearLayout linearLayout_home = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        TextView textView_home = linearLayout_home.findViewById(R.id.cardview_textview);
        //ここにViewを動的追加する
        LinearLayout main_home = linearLayout_home.findViewById(R.id.cardview_lineaLayout_main);

        Drawable home_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_home_black_24dp, null);
        home_icon.setBounds(0, 0, home_icon.getIntrinsicWidth(), home_icon.getIntrinsicHeight());

/*
        TextView textView = new TextView(main_home.getContext());
        textView.setText("てしゅと");
        main_home.addView(textView);
*/

        //ListView
        ListView home_listview = new ListView(main_home.getContext());
        LinearLayout.LayoutParams home_listview_layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 500);
        home_listview.setLayoutParams(home_listview_layoutParams);
        main_home.addView(home_listview);
        //ScrollViewの中のListViewのスクロールができるように
        home_listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

        //非同期通信
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();

                Timelines timelines = new Timelines(client);
                Range range = new Range(null, null, 40);
                ArrayList<ListItem> toot_list = new ArrayList<>();
                try {
                    Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statuses = timelines.getHome(range)
                            .execute();
                    statuses.getPart().forEach(status -> {

                        String toot_text = status.getContent();
                        String user = status.getAccount().getAcct();
                        String user_name = status.getAccount().getDisplayName();
                        String toot_time = null;

                        long toot_id = status.getId();
                        String toot_id_string = String.valueOf(toot_id);

                        long account_id = status.getAccount().getId();

                        String user_avater_url = status.getAccount().getAvatar();

                        final String[] medias = new String[1];
                        final String[] media_url = {null};
                        //めでぃあ
                        List<Attachment> list = status.getMediaAttachments();
                        list.forEach(media -> {

                            media_url[0] = media.getUrl();

                        });

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

                        ListItem listItem = null;

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
                            Item.add("トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time);
                            //Toot ID 文字列版
                            Item.add(toot_id_string);
                            //アバターURL
                            Item.add(user_avater_url);
                            //アカウントID
                            Item.add(String.valueOf(account_id));
                            //ユーザーネーム
                            Item.add(user);
                            //メディア
                            Item.add(media_url[0]);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            //カード
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);

                            listItem = new ListItem(Item);
                            ListItem finalListItem = listItem;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(finalListItem);
                                    adapter.notifyDataSetChanged();
                                    //ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                    home_listview.setAdapter(adapter);
                                }
                            });
                        }
                    });
                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        textView_home.setText(R.string.home);
        textView_home.setCompoundDrawables(home_icon, null, null, null);


        //ローカルタイムライン
        LinearLayout localtimeline_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        TextView localtime_textview = localtimeline_linearLayout.findViewById(R.id.cardview_textview);
        //追加するレイアウト
        LinearLayout localtimeline_main = localtimeline_linearLayout.findViewById(R.id.cardview_lineaLayout_main);

        Drawable localtimeline_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_train_black_24dp, null);
        localtimeline_icon.setBounds(0, 0, localtimeline_icon.getIntrinsicWidth(), localtimeline_icon.getIntrinsicHeight());

        localtime_textview.setText(R.string.public_time_line);
        localtime_textview.setCompoundDrawables(localtimeline_icon, null, null, null);

        //ListView
        ListView localtime_listview = new ListView(localtimeline_main.getContext());
        LinearLayout.LayoutParams localtime_listview_layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 500);
        localtime_listview.setLayoutParams(localtime_listview_layoutParams);
        localtimeline_main.addView(localtime_listview);
        //ScrollViewの中のListViewのスクロールができるように
        localtime_listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        ArrayList<ListItem> local_timeline_toot_list = new ArrayList<>();

        HomeTimeLineAdapter local_timeline_adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, local_timeline_toot_list);

        //非同期通信
        AsyncTask<String, Void, String> localtimeline_asyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();


                Public public_timeline = new Public(client);

                try {
                    Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statuses = public_timeline.getLocalPublic(new Range(null, null, 40)).execute();
                    statuses.getPart().forEach(status -> {
                        //System.out.println(status.getContent());
                        String toot_text = status.getContent();
                        String user = status.getAccount().getUserName();
                        String user_name = status.getAccount().getDisplayName();
                        String user_use_client = status.getApplication().getName();
                        long toot_id = status.getId();
                        String toot_id_string = String.valueOf(toot_id);
                        String toot_time = null;
                        long account_id = status.getAccount().getId();

                        //ユーザーのアバター取得
                        String user_avater_url = status.getAccount().getAvatar();

                        final String[] media_url = {null};
                        //めでぃあ
                        List<Attachment> list = status.getMediaAttachments();
                        list.forEach(media -> {
                            media_url[0] = media.getUrl();
                        });

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
                            Item.add("トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time);
                            //Toot ID 文字列版
                            Item.add(toot_id_string);
                            //アバターURL
                            Item.add(user_avater_url);
                            //アカウントID
                            Item.add(String.valueOf(account_id));
                            //ユーザーネーム
                            Item.add(user);
                            //メディア
                            Item.add(media_url[0]);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            //カード
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);

                            ListItem listItem = null;

                            listItem = new ListItem(Item);
                            ListItem finalListItem = listItem;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //ListView listView = (ListView) view.findViewById(R.id.public_time_line_list);
                                    local_timeline_adapter.insert(finalListItem, 0);
                                    int y = 0;
                                    localtime_listview.setAdapter(local_timeline_adapter);
                                    int position = localtime_listview.getFirstVisiblePosition();
                                    if (localtime_listview.getChildCount() > 0) {
                                        y = localtime_listview.getChildAt(0).getTop();
                                    }
                                    localtime_listview.setSelectionFromTop(position, y);

                                }
                            });
                        }
                    });
                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String result) {
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //設定カード
        LinearLayout setting_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        TextView setting_textview = setting_linearLayout.findViewById(R.id.cardview_textview);
        //View追加用
        LinearLayout setting_main = setting_linearLayout.findViewById(R.id.cardview_lineaLayout_main);
        setting_main.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams mainLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setting_main.setLayoutParams(mainLayout);
        //タイトル
        Drawable setting_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_settings_black_24dp, null);
        setting_icon.setBounds(0, 0, setting_icon.getIntrinsicWidth(), setting_icon.getIntrinsicHeight());
        setting_textview.setText(R.string.quick_setting);
        setting_textview.setCompoundDrawables(setting_icon, null, null, null);

        //画像のサイズ
        LinearLayout.LayoutParams image_size = new LinearLayout.LayoutParams(200, 200);
        //テキストサイズ
        LinearLayout.LayoutParams textsize = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //Layout
        LinearLayout.LayoutParams menuLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        menuLayout.weight = 1;

        //ボタンを追加
        LinearLayout setting_theme_linearLayout = new LinearLayout(setting_linearLayout.getContext());
        setting_theme_linearLayout.setOrientation(LinearLayout.VERTICAL);
        setting_theme_linearLayout.setLayoutParams(menuLayout);
        //setting_linearLayout.setLayoutParams(textsize);
        ImageView setting_theme_imageView = new ImageView(setting_linearLayout.getContext());
        setting_theme_imageView.setImageResource(R.drawable.ic_format_paint_black_24dp);
        setting_theme_imageView.setLayoutParams(image_size);
        //テキスト
        TextView setting_theme_textview = new TextView(setting_linearLayout.getContext());
        setting_theme_textview.setLayoutParams(textsize);
        setting_theme_textview.setGravity(Gravity.CENTER);

        //追加
        setting_theme_linearLayout.addView(setting_theme_imageView);
        setting_theme_linearLayout.addView(setting_theme_textview);

        //テーマ変更
        //現在の条件を取得
        final boolean setting_theme = pref_setting.getBoolean("pref_dark_theme", true);
        boolean setting_theme_oled = pref_setting.getBoolean("pref_oled_mode", true);
        //ここで判断
        final int[] theme_image = {1};
        if (setting_theme) {
            theme_image[0] = 1;
        }
        if (setting_theme_oled) {
            theme_image[0] = 2;
        }
        if (!setting_theme && !setting_theme_oled) {
            theme_image[0] = 3;
        }
        //System.out.println("現在　：　" + String.valueOf(theme_image[0]));
        //ダークモードへ
        if (theme_image[0] == 1) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("pref_dark_theme", true);
            editor.apply();
            setting_theme_imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
            setting_theme_textview.setText(R.string.setting_app_theme_dark_theme);
            theme_image[0]++;
        }
        //有機ELモードへ
        else if (theme_image[0] == 2) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("pref_dark_theme", false);
            editor.putBoolean("pref_oled_mode", true);
            editor.apply();
            setting_theme_imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
            setting_theme_textview.setText(R.string.setting_app_theme_oled_theme);
            theme_image[0]++;
        }
        //標準テーマへ
        else if (theme_image[0] == 3) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putBoolean("pref_dark_theme", false);
            editor.putBoolean("pref_oled_mode", false);
            editor.apply();
            setting_theme_imageView.setColorFilter(Color.parseColor("#2196f3"), PorterDuff.Mode.SRC_IN);
            setting_theme_textview.setText(R.string.nomal);
            theme_image[0] = 1;
        }
        setting_theme_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ダークモードへ
                if (theme_image[0] == 1) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_dark_theme", true);
                    editor.apply();
                    setting_theme_imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
                    setting_theme_textview.setText(R.string.setting_app_theme_dark_theme);
                    //Toast.makeText(getContext(), R.string.setting_app_theme, Toast.LENGTH_SHORT).show();
                    theme_image[0]++;
                }
                //有機ELモードへ
                else if (theme_image[0] == 2) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_dark_theme", false);
                    editor.putBoolean("pref_oled_mode", true);
                    editor.apply();
                    setting_theme_imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
                    setting_theme_textview.setText(R.string.setting_app_theme_oled_theme);
                    //Toast.makeText(getContext(), R.string.setting_app_theme, Toast.LENGTH_SHORT).show();
                    theme_image[0]++;
                }
                //標準テーマへ
                else if (theme_image[0] == 3) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_dark_theme", false);
                    editor.putBoolean("pref_oled_mode", false);
                    editor.apply();
                    setting_theme_imageView.setColorFilter(Color.parseColor("#2196f3"), PorterDuff.Mode.SRC_IN);
                    setting_theme_textview.setText(R.string.nomal);
                    //Toast.makeText(getContext(), R.string.setting_app_theme, Toast.LENGTH_SHORT).show();
                    theme_image[0] = 1;
                }

                //押したときにActivityを再生成する
                // アクティビティ再起動
                RestartActivity();
            }
        });


        //ボタンを追加 通知
        LinearLayout setting_notification_linearLayout = new LinearLayout(setting_linearLayout.getContext());
        setting_notification_linearLayout.setOrientation(LinearLayout.VERTICAL);
        setting_notification_linearLayout.setLayoutParams(menuLayout);
        ImageView setting_notification_imageView = new ImageView(setting_linearLayout.getContext());
        setting_notification_imageView.setImageResource(R.drawable.ic_notifications_black_24dp);
        setting_notification_imageView.setLayoutParams(image_size);
        TextView setting_notification_textview = new TextView(setting_linearLayout.getContext());
        setting_notification_textview.setGravity(Gravity.CENTER);
        setting_notification_textview.setText(R.string.notifications);

        setting_notification_linearLayout.addView(setting_notification_imageView);
        setting_notification_linearLayout.addView(setting_notification_textview);

        //現在の条件を取得
        boolean setting_notification_toast = pref_setting.getBoolean("pref_notification_toast", true);
        boolean setting_notification_vibrate = pref_setting.getBoolean("pref_notification_vibrate", true);
        int notificaiton_count = 1;
        if (setting_notification_toast) {
            //両方
            notificaiton_count = 1;
        } else {
            //無効化
            notificaiton_count = 2;
        }
        //System.out.println("ああああ" + String.valueOf(notificaiton_count));

        //設定をアイコン、テキストに反映させる
        //すべて
        if (notificaiton_count == 1) {
            setting_notification_imageView.setImageResource(R.drawable.ic_notifications_active_black_24dp);
            setting_notification_textview.setText(R.string.notifications);
        }
        //無効
        if (notificaiton_count == 2) {
            setting_notification_imageView.setImageResource(R.drawable.ic_notifications_off_black_24dp);
            setting_notification_textview.setText(R.string.mute);
        }

        //画像が押されたとき
        final int[] finalNotificaiton_count = {notificaiton_count};
        setting_notification_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalNotificaiton_count[0] == 1) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_notification_toast", true);
                    editor.putBoolean("pref_notification_vibrate", true);
                    editor.apply();
                    setting_notification_imageView.setImageResource(R.drawable.ic_notifications_active_black_24dp);
                    setting_notification_textview.setText(R.string.notifications);
                    finalNotificaiton_count[0]++;
                }
                //無効
                else if (finalNotificaiton_count[0] == 2) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_notification_toast", false);
                    editor.putBoolean("pref_notification_vibrate", false);
                    editor.apply();
                    setting_notification_imageView.setImageResource(R.drawable.ic_notifications_off_black_24dp);
                    setting_notification_textview.setText(R.string.mute);
                    finalNotificaiton_count[0] = 1;
                }
            }
        });


        //タイムライントースト
        LinearLayout timeline_toast_linearLayout = new LinearLayout(setting_linearLayout.getContext());
        timeline_toast_linearLayout.setOrientation(LinearLayout.VERTICAL);
        timeline_toast_linearLayout.setLayoutParams(menuLayout);
        ImageView timeline_toast_imageView = new ImageView(setting_linearLayout.getContext());
        timeline_toast_imageView.setImageResource(R.drawable.ic_rate_review_black_24dp);
        timeline_toast_imageView.setLayoutParams(image_size);
        TextView timeline_toast_textView = new TextView(setting_linearLayout.getContext());
        timeline_toast_textView.setGravity(Gravity.CENTER);
        timeline_toast_textView.setText(R.string.timeline_toast_disable);

        timeline_toast_linearLayout.addView(timeline_toast_imageView);
        timeline_toast_linearLayout.addView(timeline_toast_textView);

        final int[] timeline_toast_count = new int[1];

        //変更
        if (pref_setting.getInt("timeline_toast_check", 0) == 1) {
            timeline_toast_count[0] = 1;
        } else if (pref_setting.getInt("timeline_toast_check", 0) == 0) {
            timeline_toast_count[0] = 0;
        }

        //画像を変更する
        if (timeline_toast_count[0] == 0) {
            timeline_toast_textView.setText(R.string.timeline_toast_disable);
        } else if (timeline_toast_count[0] == 1) {
            timeline_toast_textView.setText(R.string.notification_timeline);
        }

        timeline_toast_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.OnSharedPreferenceChangeListener listener;

                listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        //見つける
                        if (key.equals("timeline_toast_check")) {
                            //System.out.println("よんだ！");
                        }
                    }
                };
                pref_setting.registerOnSharedPreferenceChangeListener(listener);

                //画像を変更する
                if (timeline_toast_count[0] == 0) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putInt("timeline_toast_check", 0);
                    editor.commit();
                    timeline_toast_textView.setText(R.string.timeline_toast_disable);
                    timeline_toast_count[0] = 1;
                } else if (timeline_toast_count[0] == 1) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putInt("timeline_toast_check", 1);
                    editor.commit();
                    timeline_toast_textView.setText(R.string.notification_timeline);
                    timeline_toast_count[0] = 0;

                }

            }
        });


        //背景画像

        //背景ImageView
        ImageView background_imageView = view.findViewById(R.id.homecard_background_imageview);

        LinearLayout background_LinearLayout = new LinearLayout(setting_linearLayout.getContext());
        background_LinearLayout.setOrientation(LinearLayout.VERTICAL);
        background_LinearLayout.setLayoutParams(menuLayout);
        ImageView background_setting_ImageView = new ImageView(setting_linearLayout.getContext());
        background_setting_ImageView.setImageResource(R.drawable.ic_brush_black_24dp);
        background_setting_ImageView.setLayoutParams(image_size);
        TextView background_settig_TextView = new TextView(setting_linearLayout.getContext());
        background_settig_TextView.setGravity(Gravity.CENTER);
        background_settig_TextView.setText(R.string.setting_background_image);

        background_LinearLayout.addView(background_setting_ImageView);
        background_LinearLayout.addView(background_settig_TextView);

        String background_mode = "";
        String background_fit_image = "";

        //有効・無効のとき
        if (pref_setting.getBoolean("background_image", false)) {
            //有効
            background_mode = getString(R.string.background_image_off);

        } else {
            //無効
            background_mode = getString(R.string.background_image_on);
        }


        if (pref_setting.getBoolean("background_image", true)) {
            Uri uri = Uri.parse(pref_setting.getString("background_image_path", ""));
            Glide.with(getContext())
                    .load(uri)
                    .into(background_imageView);
        }


        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_fit_image = getString(R.string.background_imageview_center_not);
            background_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            //無効
            background_fit_image = getString(R.string.background_imageview_center);
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f) != 0.1) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        String finalBackground_mode = background_mode;
        SharedPreferences.Editor editor = pref_setting.edit();
        String finalBackground_fit_image = background_fit_image;
        background_setting_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ストレージ読み込みの権限があるか確認
                //許可してないときは許可を求める
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.permission_dialog_titile))
                            .setMessage(getString(R.string.permission_dialog_message))
                            .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //権限をリクエストする
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            REQUEST_PERMISSION);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                }

                //許可
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


                    final String[] items = {finalBackground_mode, getString(R.string.background_image_change), getString(R.string.background_image_delete), finalBackground_fit_image, getString(R.string.background_image_transparency_titile)};
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.setting_background_image)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //有効無効
                                    if (which == 0) {
                                        if (pref_setting.getBoolean("background_image", true)) {
                                            editor.putBoolean("background_image", false);
                                            editor.commit();
                                        } else {
                                            editor.putBoolean("background_image", true);
                                            editor.commit();
                                        }
                                    }
                                    //選択
                                    if (which == 1) {
                                        //画像選択
                                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                        photoPickerIntent.setType("image/*");
                                        startActivityForResult(photoPickerIntent, 1);
                                        //onActivityResultで処理
                                    }
                                    //削除
                                    if (which == 2) {
                                        editor.putString("background_image_path", null);
                                        editor.apply();
                                    }
                                    //画面に合わせる
                                    if (which == 3) {
                                        if (pref_setting.getBoolean("background_fit_image", true)) {
                                            editor.putBoolean("background_fit_image", false);
                                            editor.commit();
                                        } else {
                                            editor.putBoolean("background_fit_image", true);
                                            editor.commit();
                                        }
                                    }
                                    //透明度
                                    if (which == 4) {
                                        //ダイアログ
                                        EditText editText_Transparency = new EditText(getContext());
                                        editText_Transparency.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                        AlertDialog.Builder alertDialog_editTranspatency = new AlertDialog.Builder(getActivity());
                                        alertDialog_editTranspatency.setView(editText_Transparency);
                                        alertDialog_editTranspatency.setTitle(getString(R.string.background_image_transparency_titile))
                                                .setMessage(getString(R.string.background_image_transparency_message))
                                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        String editTranspatency = editText_Transparency.getText().toString();
                                                        float transparency = Float.parseFloat(editTranspatency);
                                                        editor.putFloat("transparency", transparency);
                                                        editor.apply();
                                                    }
                                                })
                                                .setNegativeButton(getString(R.string.cancel), null)
                                                .show();
                                    }
                                }
                            }).show();
                }
            }
        });


        //アカウント切り替え
        LinearLayout multiaccount_LinearLayout = new LinearLayout(setting_linearLayout.getContext());
        multiaccount_LinearLayout.setOrientation(LinearLayout.VERTICAL);
        multiaccount_LinearLayout.setLayoutParams(menuLayout);
        ImageView multiaccount_setting_ImageView = new ImageView(setting_linearLayout.getContext());
        multiaccount_setting_ImageView.setImageResource(R.drawable.ic_transfer_within_a_station_black_24dp);
        multiaccount_setting_ImageView.setLayoutParams(image_size);
        TextView multiaccount_settig_TextView = new TextView(setting_linearLayout.getContext());
        multiaccount_settig_TextView.setGravity(Gravity.CENTER);
        multiaccount_settig_TextView.setTextSize(13);
        multiaccount_settig_TextView.setText(R.string.account_chenge);

        multiaccount_LinearLayout.addView(multiaccount_setting_ImageView);
        multiaccount_LinearLayout.addView(multiaccount_settig_TextView);

        //ポップアップメニューを展開する
        MenuBuilder menuBuilder = new MenuBuilder(getContext());
        PopupMenu popupMenu = new PopupMenu(getContext(), multiaccount_LinearLayout);

        //menuBuilder.add("にゃーん");
        MenuPopupHelper optionsMenu = new MenuPopupHelper(getContext(), menuBuilder, multiaccount_LinearLayout);
        optionsMenu.setForceShowIcon(true);

        //マルチアカウントを取ってくる
        //マルチアカウント
        //配列を使えば幸せになれそう！！！
        ArrayList<String> multi_account_instance = new ArrayList<>();
        ArrayList<String> multi_account_access_token = new ArrayList<>();

        //とりあえずPreferenceに書き込まれた値を
        String instance_instance_string = pref_setting.getString("instance_list", "");
        String account_instance_string = pref_setting.getString("access_list", "");
        if (!instance_instance_string.equals("")) {
            try {
                JSONArray instance_array = new JSONArray(instance_instance_string);
                JSONArray access_array = new JSONArray(account_instance_string);
                for (int i = 0; i < instance_array.length(); i++) {
                    multi_account_access_token.add(access_array.getString(i));
                    multi_account_instance.add(instance_array.getString(i));
                }
            } catch (Exception e) {

            }
        }

        if (multi_account_instance.size() >= 1) {
            for (int count = 0; count < multi_account_instance.size(); count++) {
                String multi_instance = multi_account_instance.get(count);
                String multi_access_token = multi_account_access_token.get(count);
                //読み込みってテキスト変更
                multiaccount_settig_TextView.setText(R.string.loading);
                int finalCount = count;
                new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... string) {
                        MastodonClient client = new MastodonClient.Builder(multi_instance, new OkHttpClient.Builder(), new Gson())
                                .accessToken(multi_access_token)
                                .build();

                        try {
                            Account main_accounts = new Accounts(client).getVerifyCredentials().execute();

                            long account_id = main_accounts.getId();
                            String display_name = main_accounts.getDisplayName();
                            String account_id_string = main_accounts.getUserName();
                            String profile = main_accounts.getNote();
                            String avater_url = main_accounts.getAvatar();

                            //menuBuilder.add(display_name + "(" + account_id_string + " / " + multi_instance + ")");
                            //第二引数　ID　にカウントを渡している
                            menuBuilder.add(0, finalCount, 0, display_name + "(" + account_id_string + " / " + multi_instance + ")");

                        } catch (Mastodon4jRequestException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    protected void onPostExecute(String result) {
                        //UIスレッドに戻ったらテキストを変更する
                        multiaccount_settig_TextView.setText(R.string.account_chenge);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }


            //押したら表示
            multiaccount_LinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //追加中に押したら落ちるから回避
                    if (menuBuilder.size() == multi_account_instance.size()) {
                        optionsMenu.show();
                        menuBuilder.setCallback(new MenuBuilder.Callback() {
                            @Override
                            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {

                                //ItemIdにマルチアカウントのカウントを入れている
                                int position = menuItem.getItemId();

                                String multi_instance = multi_account_instance.get(position);
                                String multi_access_token = multi_account_access_token.get(position);

                                SharedPreferences.Editor editor = pref_setting.edit();
                                editor.putString("main_instance", multi_instance);
                                editor.putString("main_token", multi_access_token);
                                editor.apply();

                                //アプリ再起動
                                Intent intent = new Intent(getContext(), Home.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

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
            });
            setting_main.addView(multiaccount_LinearLayout);
        }


        //読み上げ
        LinearLayout speech_LinearLayout = new LinearLayout(setting_linearLayout.getContext());
        speech_LinearLayout.setOrientation(LinearLayout.VERTICAL);
        speech_LinearLayout.setLayoutParams(menuLayout);
        ImageView speech_setting_ImageView = new ImageView(setting_linearLayout.getContext());
        speech_setting_ImageView.setImageResource(R.drawable.ic_volume_off_black_24dp);
        speech_setting_ImageView.setLayoutParams(image_size);
        TextView speech_TextView = new TextView(setting_linearLayout.getContext());
        speech_TextView.setGravity(Gravity.CENTER);
        //speech_TextView.setTextSize(13);
        speech_TextView.setText(R.string.timeline_toast_disable);

        speech_LinearLayout.addView(speech_setting_ImageView);
        speech_LinearLayout.addView(speech_TextView);

        //現在の状態を確認
        if (pref_setting.getBoolean("pref_speech", false)) {
            speech_TextView.setText(R.string.speech_timeline);
            speech_setting_ImageView.setImageResource(R.drawable.ic_volume_up_black_24dp);
        } else {
            speech_TextView.setText(R.string.timeline_toast_disable);
            speech_setting_ImageView.setImageResource(R.drawable.ic_volume_off_black_24dp);
        }

        //クリック
        speech_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref_setting.getBoolean("pref_speech", false)) {
                    speech_TextView.setText(R.string.timeline_toast_disable);
                    speech_setting_ImageView.setImageResource(R.drawable.ic_volume_off_black_24dp);
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_speech", false);
                    editor.apply();
                } else {
                    speech_TextView.setText(R.string.speech_timeline);
                    speech_setting_ImageView.setImageResource(R.drawable.ic_volume_up_black_24dp);
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_speech", true);
                    editor.apply();
                }
            }
        });


        //ボタン一覧にいれる
        setting_main.addView(setting_theme_linearLayout);
        setting_main.addView(setting_notification_linearLayout);
        setting_main.addView(timeline_toast_linearLayout);
        setting_main.addView(speech_LinearLayout);
        setting_main.addView(background_LinearLayout);


        //通知カード
        LinearLayout notification_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        TextView notification_textview = notification_linearLayout.findViewById(R.id.cardview_textview);
        //追加するレイアウト
        LinearLayout notification_main = notification_linearLayout.findViewById(R.id.cardview_lineaLayout_main);
        Drawable notification_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_notifications_black_24dp, null);
        notification_icon.setBounds(0, 0, localtimeline_icon.getIntrinsicWidth(), localtimeline_icon.getIntrinsicHeight());
        notification_textview.setText(R.string.notifications);
        notification_textview.setCompoundDrawables(notification_icon, null, null, null);
        //ListView
        ListView notification_listview = new ListView(localtimeline_main.getContext());
        LinearLayout.LayoutParams notification_listview_layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 500);
        localtime_listview.setLayoutParams(notification_listview_layoutParams);
        notification_main.addView(notification_listview);
        //ScrollViewの中のListViewのスクロールができるように
        notification_listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        ArrayList<ListItem> notification_toot_list = new ArrayList<>();

        HomeTimeLineAdapter notification_adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, notification_toot_list);

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... string) {
                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();

                Notifications notifications = new Notifications(client);


                try {

                    Pageable<Notification> statuses = notifications.getNotifications(new Range(null, null, 30), null).execute();

                    statuses.getPart().forEach(status -> {

                        final String[] account = {status.getAccount().getDisplayName()};
                        String type = status.getType();
                        //time = status.getCreatedAt();
                        String avater_url = status.getAccount().getAvatar();
                        String user_id = status.getAccount().getUserName();
                        String user_acct = status.getAccount().getAcct();
                        long account_id = status.getAccount().getId();
                        String toot_id_string = null;
                        final String[] toot = {null};
                        long toot_id = 0;
                        String time = null;
                        String layout_type = null;

                        //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                        try {
                            toot[0] = status.getStatus().getContent();
                            toot_id = status.getStatus().getId();
                            toot_id_string = String.valueOf(toot_id);
                        } catch (NullPointerException e) {
                            toot[0] = "";
                            toot_id = 0;
                            toot_id_string = String.valueOf(toot_id);
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
                                time = japanDateFormat.format(calendar.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            time = status.getCreatedAt();
                        }

                        //カスタム絵文字
                        try {
                            //本文
                            List<Emoji> emoji_List = status.getStatus().getEmojis();
                            emoji_List.forEach(emoji -> {
                                String emoji_name = emoji.getShortcode();
                                String emoji_url = emoji.getUrl();
                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                toot[0] = toot[0].replace(":" + emoji_name + ":", custom_emoji_src);
                            });

                        } catch (NullPointerException e) {
                            toot[0] = "";
                            toot_id = 0;
                            toot_id_string = String.valueOf(toot_id);
                        }

                        //DisplayNameのほう
                        List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                        account_emoji_List.forEach(emoji -> {
                            String emoji_name = emoji.getShortcode();
                            String emoji_url = emoji.getUrl();
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            account[0] = account[0].replace(":" + emoji_name + ":", custom_emoji_src);
                        });


                        Locale locale = Locale.getDefault();
                        boolean jp = locale.equals(Locale.JAPAN);
                        boolean friends_nico_check_box = pref_setting.getBoolean("pref_friends_nico_mode", false);

                        if (type.equals("mention")) {
                            if (jp) {
                                type = "返信しました";
                            }
                            layout_type = "Notification_mention";
                        }
                        if (type.equals("reblog")) {
                            if (jp) {
                                type = "ブーストしました";
                            }
                            layout_type = "Notification_reblog";
                        }
                        if (type.equals("favourite")) {
                            if (jp) {
                                if (friends_nico_check_box) {
                                    type = "お気に入りしました";
                                } else {
                                    type = "二コりました";
                                }
                                layout_type = "Notification_favourite";
                            }
                        }
                        if (type.equals("follow")) {
                            if (jp) {
                                type = "フォローしました";
                            }
                            layout_type = "Notification_follow";
                        }

                        if (getActivity() != null && isAdded()) {
                            //配列を作成
                            ArrayList<String> Item = new ArrayList<>();
                            //メモとか通知とかに
                            Item.add(layout_type);
                            //内容
                            Item.add(toot[0]);
                            //ユーザー名
                            Item.add(account[0] + " @" + user_acct + " / " + type);
                            //時間、クライアント名等
                            Item.add("トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + time);
                            //Toot ID 文字列版
                            Item.add(toot_id_string);
                            //アバターURL
                            Item.add(avater_url);
                            //アカウントID
                            Item.add(String.valueOf(account_id));
                            //ユーザーネーム
                            Item.add(avater_url);
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

                            ListItem listItem = null;
                            listItem = new ListItem(Item);
                            ListItem finalListItem = listItem;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notification_adapter.add(finalListItem);
                                    notification_adapter.notifyDataSetChanged();
                                    notification_listview.setAdapter(notification_adapter);

                                }
                            });
                        }
                    });
                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //最後

        linearLayout_.addView(account_linearLayout, 0);

        linearLayout_.addView(linearLayout, 1);

        linearLayout_.addView(setting_linearLayout, 2);

        linearLayout_.addView(instance_linearLayout, 3);

        linearLayout_.addView(linearLayout_home, 4);

        linearLayout_.addView(notification_linearLayout, 5);

        linearLayout_.addView(localtimeline_linearLayout, 6);


    }


    //権限承認されたかな？
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast toast = Toast.makeText(getContext(), R.string.permission_ok, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(getContext(), R.string.permission_block, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    //画像処理
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView background_imageView = view.findViewById(R.id.homecard_background_imageview);

        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                //完全パス取得
                String get_Path = getPath(selectedImage);
                String image_Path = "file:\\\\" + get_Path;
                //置き換え
                String final_Path = image_Path.replaceAll("\\\\", "/");

                //URI画像を入れる
                Glide.with(getContext())
                        .load(get_Path)
                        .into(background_imageView);
                //System.out.println("パス？ : " + final_Path);

                //ファイルパスを保存
                //file://の形じゃないとGIFに対応できない？（要検証
                SharedPreferences.Editor editor = pref_setting.edit();
                editor.putString("background_image_path", final_Path);
                editor.apply();

            }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        //System.out.println("終了");
        //ストリーミング終了
        if (shutdownable != null) {
            shutdownable.shutdown();
        }
    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

    public void RestartActivity() {
        //押したときにActivityを再生成する
        // アクティビティ再起動
        Intent intent = new Intent(getContext(), Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 起動しているActivityをすべて削除し、新しいタスクでMainActivityを起動する
        startActivity(intent);

    }


}