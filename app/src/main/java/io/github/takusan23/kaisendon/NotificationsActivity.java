package io.github.takusan23.kaisendon;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Favourites;
import com.sys1yagi.mastodon4j.api.method.Notifications;

import java.util.ArrayList;
import java.util.Collections;

import okhttp3.OkHttpClient;

public class NotificationsActivity extends AppCompatActivity {

    //通知
    String account = null;
    String type = null;
    String toot = null;
    String time = null;
    String avater_url = null;
    String user_id = null;

    long account_id;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //設定のプリファレンス
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


        setContentView(R.layout.activity_notifications);


        final android.os.Handler handler_1 = new android.os.Handler();


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


        //くるくる
        //くるくる


        dialog = new ProgressDialog(NotificationsActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("通知を取得中");
        dialog.show();




        ArrayList<ListItem> toot_list = new ArrayList<>();

        String finalAccessToken = AccessToken;

        String finalInstance = Instance;

        //非同期通信
        //通知を取得
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();

                Notifications notifications = new Notifications(client);


                try {

                    Pageable<Notification> statuses = notifications.getNotifications(new Range(null,null,30), null).execute();

                    statuses.getPart().forEach(status -> {

                        account = status.getAccount().getDisplayName();
                        type = status.getType();
                        time = status.getCreatedAt();
                        avater_url = status.getAccount().getAvatar();
                        user_id = status.getAccount().getUserName();

                        account_id = status.getAccount().getId();

                        //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                        try {
                            toot = status.getStatus().getContent();
                        }catch (NullPointerException e) {
                            toot = "";
                        }

                        //ListItem listItem = new ListItem(null, toot, account + " / " + type, time, null, avater_url,account_id, user_id, null,null,null,null,null);

                        //toot_list.add(listItem);


                        //UI変更
                        handler_1.post(new Runnable() {
                            @Override
                            public void run() {

                                HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(NotificationsActivity.this, R.layout.timeline_item, toot_list);
                                ListView listView = (ListView) findViewById(R.id.notifications_list);
                                listView.setAdapter(adapter);

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

            }

        }.execute();


        //引っ張って更新するやつ
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_notification);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... string) {

                        MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                                .accessToken(finalAccessToken)
                                .useStreamingApi()
                                .build();

                        Notifications notifications = new Notifications(client);

                        notifications.getNotifications(new Range(null,null,30));

                        try {

                            Pageable<Notification> statuses = notifications.getNotifications(new Range(null,null,30), null).execute();

                            statuses.getPart().forEach(status -> {

                                account = status.getAccount().getDisplayName();
                                type = status.getType();
                                time = status.getCreatedAt();
                                avater_url = status.getAccount().getAvatar();
                                user_id = status.getAccount().getUserName();

                                account_id = status.getAccount().getId();

                                //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                                try {
                                    toot = status.getStatus().getContent();
                                }catch (NullPointerException e) {
                                    toot = "";
                                }

                                //ListItem listItem = new ListItem(null, toot, account + " / " + type, time, null, avater_url,account_id, user_id, null,null,null,null,null);

                               // toot_list.add(listItem);


                                //UI変更
                                handler_1.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(NotificationsActivity.this, R.layout.timeline_item, toot_list);
                                        ListView listView = (ListView) findViewById(R.id.notifications_list);
                                        listView.setAdapter(adapter);

                                    }

                                });

                            });

                        } catch (Mastodon4jRequestException e) {

                            e.printStackTrace();

                        }


                        return null;
                    }

                    protected void onPostExecute(String result) {


                    }

                }.execute();
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

        });
    }
}
