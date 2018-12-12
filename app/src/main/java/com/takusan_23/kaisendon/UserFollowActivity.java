package com.takusan_23.kaisendon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;

public class UserFollowActivity extends AppCompatActivity {

    long account_id;

    private ProgressDialog dialog;

    String toot_time;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;

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

        setContentView(R.layout.activity_user_follow);

        final android.os.Handler handler_1 = new android.os.Handler();

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


        //account_idを受け取る
        Intent intent = getIntent();
        account_id = intent.getLongExtra("account_id", 0);


        String finalInstance = Instance;
        String finalAccessToken = AccessToken;

        //フォローかフォロワーかを分ける
        int follow_follower = intent.getIntExtra("follow_follower", 0);

        //くるくる
        dialog = new ProgressDialog(UserFollowActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (follow_follower == 1) {
            dialog.setMessage("フォロー取得中 \r\n /api/v1/accounts/:id/followers");
        }
        if (follow_follower == 2) {
            dialog.setMessage("フォロワー取得中 \r\n /api/v1/accounts/:id/following");
        }
        if (follow_follower == 3) {
            dialog.setMessage("トゥート取得中 \r\n /api/v1/accounts/:id/statuses");
        }

        dialog.show();


        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(UserFollowActivity.this, R.layout.timeline_item, toot_list);



        if (follow_follower == 1) {
            //非同期通信
            //ふぉろー

            //Toast.makeText(this, String.valueOf(account_id), Toast.LENGTH_SHORT).show();

            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... string) {

                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken)
                            .build();

                    try {
                        Pageable<Account> accounts = new Accounts(client).getFollowing(account_id, new Range(null, null, 80)).execute();
                        accounts.getPart().forEach(status -> {

                            //アカウント情報ー
                            String display_name = status.getDisplayName();
                            String id = status.getUserName();
                            String avater_url = status.getAvatar();
                            String account_info = status.getNote();
                            long account_id_follow = status.getId();

                            ListItem listItem = new ListItem(null, account_info, display_name + " @" + id, null, "", avater_url, account_id_follow, display_name, null,null,null,null);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(listItem);
                                    adapter.notifyDataSetChanged();
                                }
                            });

                            handler_1.post(new Runnable() {
                                @Override
                                public void run() {

                                    ListView listView = (ListView) findViewById(R.id.follow_follower_list);
                                    listView.setAdapter(adapter);
                                    setTitle(getString(R.string.follow));

                                }
                            });
                        });
                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(String result) {
                    dialog.dismiss();
                    return;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }
        if (follow_follower == 2){
            //非同期通信
            //ふぉろわー
            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... string) {

                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken)
                            .build();

                    ArrayList<ListItem> toot_list = new ArrayList<>();


                    try {
                        Pageable<Account> accounts = new Accounts(client).getFollowers(account_id, new Range(null, null, 80)).execute();
                        accounts.getPart().forEach(status -> {

                            //アカウント情報ー
                            String display_name = status.getDisplayName();
                            String id = status.getUserName();
                            String avater_url = status.getAvatar();
                            String account_info = status.getNote();
                            long account_id_follower = status.getId();

                            ListItem listItem = new ListItem(null, account_info, display_name + " @" + id, null, "", avater_url, account_id_follower, display_name, null,null,null,null);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(listItem);
                                    adapter.notifyDataSetChanged();
                                }
                            });

                            handler_1.post(new Runnable() {
                                @Override
                                public void run() {
                                    TextView account_name = findViewById(R.id.account_name);
                                    ListView listView = (ListView) findViewById(R.id.follow_follower_list);
                                    listView.setAdapter(adapter);

                                    setTitle(getString(R.string.follower));
                                }
                            });
                        });
                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(String result) {
                    dialog.dismiss();
                    return;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        }

        //toot
        if (follow_follower == 3){
            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... string) {

                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken)
                            .build();

                    ArrayList<ListItem> toot_list = new ArrayList<>();


                    try {
                        Pageable<com.sys1yagi.mastodon4j.api.entity.Status> accounts = new Accounts(client).getStatuses(account_id,false,false,false,new Range(null,null,40)).execute();
                        accounts.getPart().forEach(status -> {
                            String toot_text = status.getContent();
                            String user = status.getAccount().getAcct();
                            String user_name = status.getAccount().getDisplayName();
                            //toot_time = status.getCreatedAt();

                            long toot_id = status.getId();
                            String toot_id_string = String.valueOf(toot_id);

                            account_id = status.getAccount().getId();

                            String user_avater_url = status.getAccount().getAvatar();

                            final String[] medias = {null,null,null,null};
                            //めでぃあ
                            List<Attachment> list = status.getMediaAttachments();
                            final int[] count = {0};
                            list.forEach(media -> {
                                medias[count[0]] = media.getUrl();
                                count[0]++;
                            });

                            media_url_1 = medias[0];
                            media_url_2 = medias[1];
                            media_url_3 = medias[2];
                            media_url_4 = medias[3];

                            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                            if (japan_timeSetting) {
                                //時差計算？
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                //日本用フォーマット
                                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text","yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                try {
                                    Date date = simpleDateFormat.parse(status.getCreatedAt());
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    //9時間足して日本時間へ
                                    calendar.add(Calendar.HOUR, + Integer.valueOf(pref_setting.getString("pref_time_add","9")));
                                    //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                    toot_time = japanDateFormat.format(calendar.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                toot_time = status.getCreatedAt();
                            }


                            ListItem listItem = new ListItem(null, toot_text, user_name + " @" + user, "クライアント : " + "null" + " / " + "トゥートID : " + toot_id_string + " / " +  getString(R.string.time)+ " : " + toot_time, toot_id_string, user_avater_url, account_id, user, media_url_1,media_url_2,media_url_3,media_url_4);

                            //通知が行くように
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(listItem);
                                    adapter.notifyDataSetChanged();
                                }
                            });

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ListView listView = (ListView) findViewById(R.id.follow_follower_list);
                                    listView.setAdapter(adapter);
                                    setTitle(R.string.toot);
                                }
                            });

                        });
                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(String result) {

                    dialog.dismiss();
                    return;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }


    }
}
