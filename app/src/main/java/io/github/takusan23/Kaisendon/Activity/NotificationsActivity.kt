package io.github.takusan23.Kaisendon.Activity

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Range
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Notifications
import io.github.takusan23.Kaisendon.HomeTimeLineAdapter
import io.github.takusan23.Kaisendon.ListItem
import io.github.takusan23.Kaisendon.R
import okhttp3.OkHttpClient
import java.util.*

class NotificationsActivity : AppCompatActivity() {

    //通知
    internal var account: String? = null
    internal var type: String? = null
    internal var toot: String? = null
    internal var time: String? = null
    internal var avater_url: String? = null
    internal var user_id: String? = null

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
            setTheme(R.style.Theme_AppCompat_DayNight)
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        //この画面用にNoActionBerなダークテーマを指定している
        val oled_mode = pref_setting.getBoolean("pref_oled_mode", false)
        if (oled_mode) {
            setTheme(R.style.OLED_Theme)
        } else {
            //なにもない
        }


        setContentView(R.layout.activity_notifications)


        val handler_1 = android.os.Handler()


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


        dialog = ProgressDialog(this@NotificationsActivity)
        dialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog!!.setMessage("通知を取得中")
        dialog!!.show()


        val toot_list = ArrayList<ListItem>()

        val finalAccessToken = AccessToken

        val finalInstance = Instance

        //非同期通信
        //通知を取得
        val asyncTask = object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg string: String): String? {

                val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                        .accessToken(finalAccessToken!!)
                        .useStreamingApi()
                        .build()

                val notifications = Notifications(client)


                try {

                    val statuses = notifications.getNotifications(Range(null, null, 30), null).execute()

                    statuses.part.forEach { status ->

                        account = status.account!!.displayName
                        type = status.type
                        time = status.createdAt
                        avater_url = status.account!!.avatar
                        user_id = status.account!!.userName

                        account_id = status.account!!.id

                        //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                        try {
                            toot = status.status!!.content
                        } catch (e: NullPointerException) {
                            toot = ""
                        }

                        //ListItem listItem = new ListItem(null, toot, account + " / " + type, time, null, avater_url,account_id, user_id, null,null,null,null,null);

                        //toot_list.add(listItem);


                        //UI変更
                        handler_1.post {
                            val adapter = HomeTimeLineAdapter(this@NotificationsActivity, R.layout.timeline_item, toot_list)
                            val listView = findViewById<View>(R.id.notifications_list) as ListView
                            listView.adapter = adapter
                        }

                    }

                } catch (e: Mastodon4jRequestException) {

                    e.printStackTrace()

                }



                return null
            }

            override fun onPostExecute(result: String) {

                dialog!!.dismiss()

            }

        }.execute()


        //引っ張って更新するやつ
        val swipeRefreshLayout = findViewById<View>(R.id.swipe_refresh_notification) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            val asyncTask = object : AsyncTask<String, Void, String>() {

                override fun doInBackground(vararg string: String): String? {

                    val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                            .accessToken(finalAccessToken!!)
                            .useStreamingApi()
                            .build()

                    val notifications = Notifications(client)

                    notifications.getNotifications(Range(null, null, 30))

                    try {

                        val statuses = notifications.getNotifications(Range(null, null, 30), null).execute()

                        statuses.part.forEach { status ->

                            account = status.account!!.displayName
                            type = status.type
                            time = status.createdAt
                            avater_url = status.account!!.avatar
                            user_id = status.account!!.userName

                            account_id = status.account!!.id

                            //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                            try {
                                toot = status.status!!.content
                            } catch (e: NullPointerException) {
                                toot = ""
                            }

                            //ListItem listItem = new ListItem(null, toot, account + " / " + type, time, null, avater_url,account_id, user_id, null,null,null,null,null);

                            // toot_list.add(listItem);


                            //UI変更
                            handler_1.post {
                                val adapter = HomeTimeLineAdapter(this@NotificationsActivity, R.layout.timeline_item, toot_list)
                                val listView = findViewById<View>(R.id.notifications_list) as ListView
                                listView.adapter = adapter
                            }

                        }

                    } catch (e: Mastodon4jRequestException) {

                        e.printStackTrace()

                    }


                    return null
                }

                override fun onPostExecute(result: String) {


                }

            }.execute()
            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}
