package io.github.takusan23.Kaisendon

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Handler
import com.sys1yagi.mastodon4j.api.entity.Notification
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Streaming
import okhttp3.OkHttpClient
import java.util.*

class PublicTimeLine : AppCompatActivity() {


    internal var toot_text: String? = null
    internal var user: String? = null
    internal var user_name: String? = null
    internal var user_use_client: String? = null
    internal var toot_id: Long = 0
    internal var toot_id_string: String? = null
    internal var user_avater_url: String? = null
    internal var toot_time: String? = null
    internal var account_id: Long = 0


    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //設定のプリファレンス
        val pref_setting = getDefaultSharedPreferences(this)

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        val dark_mode = pref_setting.getBoolean("pref_dark_theme", false)
        if (dark_mode) {
            setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar)
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        val oled_mode = pref_setting.getBoolean("pref_oled_mode", false)
        if (oled_mode) {
            setTheme(R.style.OLED_Theme)
        } else {
            //なにもない
        }

        setContentView(R.layout.activity_publc_time_line)

        val handler_1 = android.os.Handler()


        //スリープ無効？
        val setting_sleep = pref_setting.getBoolean("pref_no_sleep_timeline", false)
        if (setting_sleep) {
            //常時点灯
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            //常時点灯しない
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }


        val toot_list = ArrayList<ListItem>()

        //アクセストークンを変更してる場合のコード
        //アクセストークン
        val pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        var AccessToken: String? = null

        //インスタンス
        var Instance: String? = null

        val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
            Instance = pref_setting.getString("pref_mastodon_instance", "")

        } else {

            AccessToken = pref_setting.getString("main_token", "")
            Instance = pref_setting.getString("main_instance", "")

        }


        //くるくる
        //くるくる
        dialog = ProgressDialog(this@PublicTimeLine)
        dialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog!!.setMessage("ローカルタイムラインを取得中")
        dialog!!.show()

        //Button nicoru = (Button) findViewById(R.id.nicoru);

        val finalAccessToken = AccessToken

        val finalInstance = Instance

        val asyncTask = object : AsyncTask<String, Void, String>() {


            override fun doInBackground(vararg string: String): String? {

                val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                        .accessToken(finalAccessToken!!)
                        .useStreamingApi()
                        .build()
                val handler = object : Handler {

                    override fun onStatus(status: com.sys1yagi.mastodon4j.api.entity.Status) {
                        //System.out.println(status.getContent());
                        toot_text = status.content
                        user = status.account!!.userName
                        user_name = status.account!!.displayName
                        user_use_client = status.application!!.name
                        toot_id = status.id
                        toot_id_string = toot_id.toString()
                        toot_time = status.createdAt
                        account_id = status.account!!.id


                        //ユーザーのアバター取得
                        user_avater_url = status.account!!.avatar

                        val bmp: Bitmap? = null
                        //BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);  // 今回はサンプルなのでデフォルトのAndroid Iconを利用
                        val nicoru_button: ImageButton? = null
                        //ListItem listItem = new ListItem(null, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + toot_time, toot_id_string, user_avater_url, account_id, user, null);


                        //toot_list.add(listItem);


                        handler_1.post {
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
                    }

                    override fun onNotification(notification: Notification) {/* no op */
                    }

                    override fun onDelete(id: Long) {/* no op */
                    }
                }

                val streaming = Streaming(client)
                try {
                    val shutdownable = streaming.localPublic(handler)
                    Thread.sleep(10000L)
                    //shutdownable.shutdown();
                } catch (e: Mastodon4jRequestException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                return toot_text
            }

            override fun onPostExecute(result: String) {

                val listView = findViewById<View>(R.id.public_time_line_list) as ListView

                //くるくる終了
                dialog!!.dismiss()
                return
            }


        }
        asyncTask.execute(toot_text)


        //簡易トゥート
        val toot_text_edit = findViewById<TextView>(R.id.toot_text_public)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val finalAccessToken1 = AccessToken

        /*
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

*/


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



