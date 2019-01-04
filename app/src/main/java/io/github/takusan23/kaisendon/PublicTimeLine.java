package io.github.takusan23.kaisendon;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Streaming;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class PublicTimeLine extends AppCompatActivity {


    String toot_text = null;
    String user = null;
    String user_name = null;
    String user_use_client = null;
    long toot_id;
    String toot_id_string = null;
    String user_avater_url = null;
    String toot_time = null;
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
            setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar);
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            setTheme(R.style.OLED_Theme);
        } else {
            //なにもない
        }

        setContentView(R.layout.activity_publc_time_line);

        final android.os.Handler handler_1 = new android.os.Handler();


        //スリープ無効？
        boolean setting_sleep = pref_setting.getBoolean("pref_no_sleep_timeline", false);
        if (setting_sleep) {
            //常時点灯
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            //常時点灯しない
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }


        ArrayList<ListItem> toot_list = new ArrayList<>();

        //アクセストークンを変更してる場合のコード
        //アクセストークン
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
        dialog = new ProgressDialog(PublicTimeLine.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("ローカルタイムラインを取得中");
        dialog.show();

        //Button nicoru = (Button) findViewById(R.id.nicoru);

        String finalAccessToken = AccessToken;

        String finalInstance = Instance;

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
                        //System.out.println(status.getContent());
                        toot_text = status.getContent();
                        user = status.getAccount().getUserName();
                        user_name = status.getAccount().getDisplayName();
                        user_use_client = status.getApplication().getName();
                        toot_id = status.getId();
                        toot_id_string = String.valueOf(toot_id);
                        toot_time = status.getCreatedAt();
                        account_id = status.getAccount().getId();


                        //ユーザーのアバター取得
                        user_avater_url = status.getAccount().getAvatar();

                        Bitmap bmp = null;
                        //BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);  // 今回はサンプルなのでデフォルトのAndroid Iconを利用
                        ImageButton nicoru_button = null;
                        //ListItem listItem = new ListItem(null, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + toot_time, toot_id_string, user_avater_url, account_id, user, null);


                        //toot_list.add(listItem);


                        handler_1.post(new Runnable() {
                            @Override
                            public void run() {

/*
                                //toot_list.add( "----------" + "\r\n" + "User : " + user +"\r\n" + "Toot : " + toot_text + "\r\n" + "----------");
                                TimeLineAdapter adapter = new TimeLineAdapter(PublicTimeLine.this, R.layout.timeline_item, toot_list);
                                ListView listView = (ListView) findViewById(R.id.public_time_line_list);

                                int position = listView.getFirstVisiblePosition();
                                int y = 0;
                                if (listView.getChildCount() > 0) {
                                    y = listView.getChildAt(0).getTop();
                                }

                                listView.setAdapter(adapter);

                                listView.setSelectionFromTop(position, y);

                                listView.invalidateViews();
                                adapter.notifyDataSetChanged();
                                listView.deferNotifyDataSetChanged();
*/


                            }
                        });
                    }

                    @Override
                    public void onNotification(@NotNull Notification notification) {/* no op */}

                    @Override
                    public void onDelete(long id) {/* no op */}
                };

                Streaming streaming = new Streaming(client);
                try {
                    Shutdownable shutdownable = streaming.localPublic(handler);
                    Thread.sleep(10000L);
                    //shutdownable.shutdown();
                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return toot_text;
            }

            protected void onPostExecute(String result) {

                ListView listView = (ListView) findViewById(R.id.public_time_line_list);

                //くるくる終了
                dialog.dismiss();
                return;
            }


        };
        asyncTask.execute(toot_text);


        //簡易トゥート
        TextView toot_text_edit = findViewById(R.id.toot_text_public);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        String finalAccessToken1 = AccessToken;

        //テキストボックス長押し
        toot_text_edit.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                //設定でダイアログを出すかどうか
                boolean instant_toot_dialog = pref_setting.getBoolean("pref_timeline_toot_dialog", false);

                if (instant_toot_dialog) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(PublicTimeLine.this);
                    alertDialog.setTitle(R.string.confirmation);
                    alertDialog.setMessage(R.string.toot_dialog);
                    alertDialog.setPositiveButton(R.string.toot, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            final String toot_text = toot_text_edit.getText().toString();


                            new AsyncTask<String, Void, String>() {

                                @Override
                                protected String doInBackground(String... params) {
                                    AccessToken accessToken = new AccessToken();
                                    accessToken.setAccessToken(finalAccessToken1);

                                    MastodonClient client = new MastodonClient.Builder("friends.nico", new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                    RequestBody requestBody = new FormBody.Builder()
                                            .add("status", toot_text)
                                            .build();

                                   // System.out.println("=====" + client.post("statuses", requestBody));

                                    return toot_text;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    Toast.makeText(getApplicationContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                                }

                            }.execute();
                            toot_text_edit.setText(""); //投稿した後に入力フォームを空にする


                        }
                    });
                    alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.create().show();

                } else {

                    final String toot_text = toot_text_edit.getText().toString();

                    new AsyncTask<String, Void, String>() {

                        @Override
                        protected String doInBackground(String... params) {
                            AccessToken accessToken = new AccessToken();
                            accessToken.setAccessToken(finalAccessToken1);

                            MastodonClient client = new MastodonClient.Builder("friends.nico", new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                            RequestBody requestBody = new FormBody.Builder()
                                    .add("status", toot_text)
                                    .build();

                            //System.out.println("=====" + client.post("statuses", requestBody));

                            return toot_text;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            Toast.makeText(getApplicationContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                        }

                    }.execute();
                    toot_text_edit.setText(""); //投稿した後に入力フォームを空にする
                }

                return false;
            }
        });



/*



        toot_text_edit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String toot_text = toot_text_edit.getText().toString();



                return true;
            }
        });


*/


/*
        Button nicoru = findViewById(R.id.nicoru);
        nicoru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String toot_text = toot_text_edit.getText().toString();


                Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();


                new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        AccessToken accessToken = new AccessToken();
                        accessToken.setAccessToken(AccessToken);

                        MastodonClient client = new MastodonClient.Builder("friends.nico", new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                        RequestBody requestBody = new FormBody.Builder()
                                //.add(":id" , toot_id_string)
                                .build();

                        System.out.println("=====" + client.post("statuses/100673569983698220/favourite", requestBody));

                        return toot_text;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        Toast.makeText(getApplicationContext(), "ニコった！ : " + result, Toast.LENGTH_SHORT).show();
                    }

                }.execute();
                toot_text_edit.setText(""); //投稿した後に入力フォームを空にする



            }
        });
*/


    }

}



