package io.github.takusan23.Kaisendon.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Range
import com.sys1yagi.mastodon4j.api.Shutdownable
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Accounts
import com.sys1yagi.mastodon4j.api.method.Notifications
import com.sys1yagi.mastodon4j.api.method.Public
import com.sys1yagi.mastodon4j.api.method.Timelines
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.HomeTimeLineAdapter
import io.github.takusan23.Kaisendon.ListItem
import io.github.takusan23.Kaisendon.R
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class HomeCrad_Fragment : Fragment() {
    internal lateinit var view: View

    internal var shutdownable: Shutdownable? = null

    internal var select_color: Int = 0

    internal lateinit var pref_setting: SharedPreferences

    private val REQUEST_PERMISSION = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        view = inflater.inflate(R.layout.activity_homecard, container, false)

        return view
    }


    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //設定
        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        //アクセストークンを変更してる場合のコード
        //アクセストークン
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

        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)) {
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }


        val linearLayout_ = view.findViewById<View>(R.id.cardview_linear) as LinearLayout
        linearLayout_.removeAllViews()
        val cardSize = 1 // カードの枚数

        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        //アカウント
        val account_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val account_main = account_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)
        val account_textview = account_linearLayout.findViewById<TextView>(R.id.cardview_textview)

        val account_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_sentiment_neutral_black_24dp, null)
        account_icon!!.setBounds(0, 0, account_icon.intrinsicWidth, account_icon.intrinsicHeight)

        account_textview.setText(R.string.account)
        account_textview.setCompoundDrawables(account_icon, null, null, null)

        val progressBar_account = ProgressBar(context)
        account_main.addView(progressBar_account)
        //ここにいれる
        val account_layout = LinearLayout(account_linearLayout.context)
        account_main.addView(account_layout)
        //ふぉよー・ふぉよわー
        val follow_layout = LinearLayout(account_linearLayout.context)
        follow_layout.orientation = LinearLayout.HORIZONTAL
        account_main.addView(follow_layout)

        val account_text_textview = TextView(account_linearLayout.context)
        val account_avater_imageview = ImageView(account_linearLayout.context)

        val account_follow_textview = TextView(account_linearLayout.context)
        val account_follower_textview = TextView(account_linearLayout.context)

        //取得
        //Wi-Fi接続状況確認
        val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false)
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)

        val finalInstance2 = Instance
        val finalAccessToken1 = AccessToken
        object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg string: String): String? {

                val client = MastodonClient.Builder(finalInstance2!!, OkHttpClient.Builder(), Gson()).accessToken(finalAccessToken1!!).build()

                try {
                    val account = Accounts(client).getVerifyCredentials().execute()

                    val display_name = account.displayName
                    val user_id = account.userName
                    val profile = account.note

                    val follow = account.followingCount
                    val follower = account.followersCount

                    val user_avater = account.avatar
                    val user_header = account.header

                    if (activity != null) {

                        //UIを変更するために別スレッド呼び出し
                        activity!!.runOnUiThread {
                            //表示設定
                            if (setting_avater_hidden) {
                                account_avater_imageview.setImageResource(R.drawable.ic_person_black_24dp)
                            }
                            //Wi-Fi
                            if (setting_avater_wifi) {
                                if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                                    if (setting_avater_gif) {
                                        //GIFアニメ再生させない
                                        Picasso.get()
                                                .load(user_avater)
                                                .resize(100, 100)
                                                .into(account_avater_imageview)
                                    } else {
                                        //GIFアニメを再生
                                        Glide.with(context!!)
                                                .load(user_avater)
                                                .apply(RequestOptions().override(100, 100))
                                                .into(account_avater_imageview)
                                    }
                                }
                            } else {
                                account_avater_imageview.setImageResource(R.drawable.ic_person_black_24dp)
                            }

                            account_main.removeView(progressBar_account)

                            val follow_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done_black_24dp, null)
                            follow_icon!!.setBounds(0, 0, follow_icon.intrinsicWidth, follow_icon.intrinsicHeight)
                            val follower_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done_all_black_24dp, null)
                            follower_icon!!.setBounds(0, 0, follower_icon.intrinsicWidth, follower_icon.intrinsicHeight)


                            val user_strnig = "$display_name\r\n@$user_id@$finalInstance2\r\n\r\n$profile"
                            account_text_textview.text = Html.fromHtml(user_strnig, Html.FROM_HTML_MODE_COMPACT)
                            account_follow_textview.text = getString(R.string.follow) + " : " + follow.toString()
                            account_follow_textview.setCompoundDrawablesWithIntrinsicBounds(follow_icon, null, null, null)
                            account_follower_textview.text = getString(R.string.follower) + " : " + follower.toString()
                            account_follower_textview.setCompoundDrawablesWithIntrinsicBounds(follower_icon, null, null, null)
                        }
                    }
                } catch (e: Mastodon4jRequestException) {
                    e.printStackTrace()
                }

                return null
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        //AsyncTackを並列実行

        account_layout.addView(account_avater_imageview)
        account_layout.addView(account_text_textview)
        follow_layout.addView(account_follow_textview)
        follow_layout.addView(account_follower_textview)


        //Toot
        val linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val cardView = linearLayout.findViewById<View>(R.id.cardview) as CardView
        val textBox = linearLayout.findViewById<View>(R.id.cardview_textview) as TextView
        //ここについか
        val main_toot = linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)

        val title_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_create_black_24dp_black, null)
        title_icon!!.setBounds(0, 0, title_icon.intrinsicWidth, title_icon.intrinsicHeight)

        val editText = EditText(linearLayout.context)
        val button = Button(linearLayout.context)

        editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        editText.setHint(R.string.imananisiteru)

        val layoutParams_button = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //Gravity setGravityは中の文字に反映されてしまう
        layoutParams_button.gravity = Gravity.END
        button.layoutParams = layoutParams_button
        button.setText(R.string.toot)
        button.setCompoundDrawables(title_icon, null, null, null)


        val finalAccessToken = AccessToken
        val finalInstance1 = Instance
        button.setOnClickListener {
            val toot_text = editText.text.toString()

            //ダイアログ出すかどうか
            val accessToken_boomelan = pref_setting.getBoolean("pref_toot_dialog", false)
            if (accessToken_boomelan) {

                val alertDialog = AlertDialog.Builder(context!!)
                alertDialog.setTitle(R.string.confirmation)
                alertDialog.setMessage(R.string.toot_dialog)
                alertDialog.setPositiveButton(R.string.toot) { dialog, which ->
                    //トゥートああああ

                    object : AsyncTask<String, Void, String>() {

                        override fun doInBackground(vararg params: String): String {
                            val accessToken = AccessToken()
                            accessToken.accessToken = finalAccessToken!!

                            val client = MastodonClient.Builder(finalInstance1!!, OkHttpClient.Builder(), Gson()).accessToken(accessToken.accessToken).build()

                            val requestBody = FormBody.Builder()
                                    .add("status", toot_text)
                                    .build()

                            println("=====" + client.post("statuses", requestBody))


                            return toot_text
                        }

                        override fun onPostExecute(result: String) {
                            Toast.makeText(context, "トゥートしました : $result", Toast.LENGTH_SHORT).show()
                        }

                    }.execute()
                    editText.setText("") //投稿した後に入力フォームを空にする
                }
                alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                alertDialog.create().show()

            } else {
                //トゥートああああ
                object : AsyncTask<String, Void, String>() {

                    override fun doInBackground(vararg params: String): String {
                        val accessToken = AccessToken()
                        accessToken.accessToken = finalAccessToken!!

                        val client = MastodonClient.Builder(finalInstance1!!, OkHttpClient.Builder(), Gson()).accessToken(accessToken.accessToken).build()

                        val requestBody = FormBody.Builder()
                                .add("status", toot_text)
                                .build()

                        println("=====" + client.post("statuses", requestBody))


                        return toot_text
                    }

                    override fun onPostExecute(result: String) {
                        Toast.makeText(context, "トゥートしました : $result", Toast.LENGTH_SHORT).show()
                    }

                }.execute()
                editText.setText("") //投稿した後に入力フォームを空にする
            }
        }

        main_toot.addView(editText)
        main_toot.addView(button)

        textBox.setText(R.string.toot)
        textBox.setCompoundDrawables(title_icon, null, null, null)


        //インスタンス情報
        val inflater_ = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val instance_linearLayout = inflater_.inflate(R.layout.cardview_layout, null) as LinearLayout
        val instance_title = instance_linearLayout.findViewById<TextView>(R.id.cardview_textview)
        //ここに追加
        val main_instance = instance_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)

        val instance_title_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_public_black_24dp, null)
        instance_title_icon!!.setBounds(0, 0, instance_title_icon.intrinsicWidth, instance_title_icon.intrinsicHeight)

        instance_title.setText(R.string.instance_info)
        instance_title.setCompoundDrawables(instance_title_icon, null, null, null)

        //このカードで使うTextView
        val instance_title_textiew = TextView(instance_linearLayout.context)
        val instance_description_textview = TextView(instance_linearLayout.context)
        val instance_total_user_textview = TextView(instance_linearLayout.context)
        val instance_total_toot_textview = TextView(instance_linearLayout.context)

        //activeもだすわ
        val instance_active_title_textview = TextView(instance_linearLayout.context)
        val active_LinearLayout = LinearLayout(main_instance.context)
        active_LinearLayout.orientation = LinearLayout.HORIZONTAL
        val instance_active_status_textview = TextView(main_instance.context)
        val instance_active_logins_textview = TextView(main_instance.context)
        val instance_active_registrations = TextView(main_instance.context)

        //読み込み中
        //くるくる
        val progressBar_info = ProgressBar(context)
        instance_title_textiew.setText(R.string.loading)
        main_instance.addView(progressBar_info)

        //Mastodon 統計APIを叩いて
        //JSONを解析
        val finalInstance = Instance
        val asyncTask = object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg string: String): String? {

                var url: URL? = null
                var connection: HttpURLConnection? = null
                val url_link = "https://$finalInstance/api/v1/instance/"

                try {

                    url = URL(url_link)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    val `in` = connection.inputStream
                    var encoding: String? = connection.contentEncoding
                    if (null == encoding) {
                        encoding = "UTF-8"
                    }
                    val inReader = InputStreamReader(`in`, encoding)
                    val bufReader = BufferedReader(inReader)
                    val response = StringBuilder()
                    var line: String? = null
                    // 1行ずつ読み込む
/*
                    while ((line = bufReader.readLine()) != null) {
                        response.append(line)
                    }
*/
                    bufReader.close()
                    inReader.close()
                    `in`.close()

                    // 受け取ったJSON文字列をパース
                    val jsonObject = JSONObject(response.toString())
                    //Status
                    val stats = jsonObject.getJSONObject("stats")

                    //タイトル
                    val title = jsonObject.getString("title")
                    //説明
                    val description = jsonObject.getString("description")
                    //バージョン？
                    val version = jsonObject.getString("version")
                    //ユーザー数
                    val user_total = stats.getString("user_count")
                    //トータルトゥート
                    val toot_total = stats.getString("status_count")

                    if (activity != null) {
                        //UI変更
                        activity!!.runOnUiThread {
                            //ICON
                            val title_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_public_black_24dp, null)
                            title_icon!!.setBounds(0, 0, title_icon.intrinsicWidth, title_icon.intrinsicHeight)
                            val description_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_description_black_24dp, null)
                            description_icon!!.setBounds(0, 0, description_icon.intrinsicWidth, description_icon.intrinsicHeight)
                            val user_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_people_black_24dp, null)
                            user_icon!!.setBounds(0, 0, user_icon.intrinsicWidth, user_icon.intrinsicHeight)
                            val toot_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_create_black_24dp_black, null)
                            toot_icon!!.setBounds(0, 0, toot_icon.intrinsicWidth, toot_icon.intrinsicHeight)

                            //入れる
                            instance_title_textiew.text = getString(R.string.instance_name) + " : " + title + " (" + version + ")"
                            instance_title_textiew.setCompoundDrawables(title_icon, null, null, null)
                            instance_description_textview.text = getString(R.string.instance_description) + " : " + Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
                            instance_description_textview.setCompoundDrawables(description_icon, null, null, null)
                            instance_total_user_textview.text = getString(R.string.instance_user_count) + " : " + user_total
                            instance_total_user_textview.setCompoundDrawables(user_icon, null, null, null)
                            instance_total_toot_textview.text = getString(R.string.instance_status_count) + " : " + toot_total
                            instance_total_toot_textview.setCompoundDrawables(toot_icon, null, null, null)
                            //Gravity
                            instance_title_textiew.gravity = Gravity.CENTER_VERTICAL
                            instance_description_textview.gravity = Gravity.CENTER_VERTICAL
                            instance_total_user_textview.gravity = Gravity.CENTER_VERTICAL
                            instance_total_toot_textview.gravity = Gravity.CENTER_VERTICAL

                            //レイアウトに入れる
                            main_instance.addView(instance_title_textiew)
                            main_instance.addView(instance_description_textview)
                            main_instance.addView(instance_total_user_textview)
                            main_instance.addView(instance_total_toot_textview)
                        }
                    }
                    return null
                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                } catch (e: JSONException) {
                    e.printStackTrace()
                    return null
                } finally {
                    connection?.disconnect()
                }
            }

            override fun onPostExecute(result: String) {

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        //Activeもだすぜ
        val asyncTask_active = object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg string: String): String? {

                var url: URL? = null
                var connection: HttpURLConnection? = null
                val url_link = "https://$finalInstance/api/v1/instance/activity"

                try {

                    url = URL(url_link)
                    connection = url!!.openConnection() as HttpURLConnection
                    connection!!.connect()

                    val `in` = connection!!.inputStream
                    var encoding: String? = connection!!.contentEncoding
                    if (null == encoding) {
                        encoding = "UTF-8"
                    }
                    val inReader = InputStreamReader(`in`, encoding!!)
                    val bufReader = BufferedReader(inReader)
                    val response = StringBuilder()
                    var line: String? = null
/*
                    // 1行ずつ読み込む
                    while ((line = bufReader.readLine()) != null) {
                        response.append(line)
                    }
*/
                    bufReader.close()
                    inReader.close()
                    `in`.close()

                    // 受け取ったJSON文字列をパース
                    //                    JSONObject jsonObject = new JSONObject(response.toString());
                    val datas = JSONArray(response.toString())
                    //Status
                    val stats = datas.getJSONObject(0)

                    //すてーたす
                    val toot_total = stats.getString("statuses")
                    //ログイン
                    val user_login = stats.getString("logins")
                    //登録?
                    val registrations = stats.getString("registrations")


                    if (activity != null) {
                        //UI変更
                        activity!!.runOnUiThread {
                            //読み込みくるくる終了
                            main_instance.removeView(progressBar_info)

                            val title_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_timeline_black_24dp, null)
                            title_icon!!.setBounds(0, 0, title_icon.intrinsicWidth, title_icon.intrinsicHeight)
                            val description_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_create_black_24dp_black, null)
                            description_icon!!.setBounds(0, 0, description_icon.intrinsicWidth, description_icon.intrinsicHeight)
                            val user_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_people_black_24dp, null)
                            user_icon!!.setBounds(0, 0, user_icon.intrinsicWidth, user_icon.intrinsicHeight)
                            val active_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_whatshot_black_24dp, null)
                            active_icon!!.setBounds(0, 0, active_icon.intrinsicWidth, active_icon.intrinsicHeight)
                            val people_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_person_add_black_24dp, null)
                            people_icon!!.setBounds(0, 0, people_icon.intrinsicWidth, people_icon.intrinsicHeight)

                            val textview_params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            textview_params.weight = 1f

                            instance_active_title_textview.text = getString(R.string.instance_statistics) + "(" + getString(R.string.this_week) + ")"
                            instance_active_title_textview.setCompoundDrawables(title_icon, null, null, null)
                            instance_active_title_textview.layoutParams = textview_params
                            instance_active_status_textview.text = getString(R.string.instance_statistics_status) + "\r\n" + toot_total
                            instance_active_status_textview.setCompoundDrawables(user_icon, null, null, null)
                            instance_active_status_textview.layoutParams = textview_params
                            instance_active_logins_textview.text = getString(R.string.instance_statistics_login) + "\r\n" + user_login
                            instance_active_logins_textview.setCompoundDrawables(active_icon, null, null, null)
                            instance_active_logins_textview.layoutParams = textview_params
                            instance_active_registrations.text = getString(R.string.instance_statistics_registrations) + "\r\n" + registrations
                            instance_active_registrations.setCompoundDrawables(people_icon, null, null, null)
                            instance_active_registrations.layoutParams = textview_params

                            //Gravity
                            instance_active_title_textview.gravity = Gravity.CENTER_VERTICAL
                            instance_active_status_textview.gravity = Gravity.CENTER_VERTICAL
                            instance_active_logins_textview.gravity = Gravity.CENTER_VERTICAL
                            instance_active_registrations.gravity = Gravity.CENTER_VERTICAL

                            //レイアウトに入れる
                            main_instance.addView(instance_active_title_textview)
                            main_instance.addView(active_LinearLayout)
                            active_LinearLayout.addView(instance_active_status_textview)
                            active_LinearLayout.addView(instance_active_logins_textview)
                            active_LinearLayout.addView(instance_active_registrations)
                        }
                    }

                    return null

                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                } catch (e: JSONException) {
                    e.printStackTrace()
                    return null
                } finally {
                    if (connection != null) {
                        connection!!.disconnect()
                    }
                }
            }

            override fun onPostExecute(result: String) {

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        //取得、解析後にaddViewする
        /*
        main_instance.addView(instance_description_textview);
        main_instance.addView(instance_total_user_textview);
        main_instance.addView(instance_total_toot_textview);
*/


        //Home ?
        val linearLayout_home = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val textView_home = linearLayout_home.findViewById<TextView>(R.id.cardview_textview)
        //ここにViewを動的追加する
        val main_home = linearLayout_home.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)

        val home_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_home_black_24dp, null)
        home_icon!!.setBounds(0, 0, home_icon.intrinsicWidth, home_icon.intrinsicHeight)

        /*
        TextView textView = new TextView(main_home.getContext());
        textView.setText("てしゅと");
        main_home.addView(textView);
*/

        //ListView
        val home_listview = ListView(main_home.context)
        val home_listview_layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 500)
        home_listview.layoutParams = home_listview_layoutParams
        main_home.addView(home_listview)
        //ScrollViewの中のListViewのスクロールができるように
        home_listview.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        val toot_list = ArrayList<ListItem>()

        val adapter = HomeTimeLineAdapter(context!!, R.layout.timeline_item, toot_list)

        //非同期通信
        object : AsyncTask<String, Void, String>() {
            override fun doInBackground(vararg string: String): String? {

                val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                        .accessToken(finalAccessToken!!)
                        .useStreamingApi()
                        .build()

                val timelines = Timelines(client)
                val range = Range(null, null, 40)
                val toot_list = ArrayList<ListItem>()
                try {
                    val statuses = timelines.getHome(range)
                            .execute()
                    statuses.part.forEach { status ->

                        val toot_text = status.content
                        val user = status.account!!.acct
                        val user_name = status.account!!.displayName
                        var toot_time: String? = null

                        val toot_id = status.id
                        val toot_id_string = toot_id.toString()

                        val account_id = status.account!!.id

                        val user_avater_url = status.account!!.avatar

                        val medias = arrayOfNulls<String>(1)
                        val media_url = arrayOf<String>()
                        //めでぃあ
                        val list = status.mediaAttachments
                        list.forEach { media ->

                            media_url[0] = media.url

                        }

                        val japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false)
                        if (japan_timeSetting) {
                            //時差計算？
                            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                            //日本用フォーマット
                            val japanDateFormat = SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!, Locale.JAPAN)
                            try {
                                val date = simpleDateFormat.parse(status.createdAt)
                                val calendar = Calendar.getInstance()
                                calendar.time = date!!
                                //9時間足して日本時間へ
                                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")!!))
                                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                toot_time = japanDateFormat.format(calendar.time)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                        } else {
                            toot_time = status.createdAt
                        }

                        var listItem: ListItem? = null

                        if (activity != null && isAdded) {
                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add("")
                            //内容
                            Item.add(toot_text)
                            //ユーザー名
                            Item.add("$user_name @$user")
                            //時間、クライアント名等
                            Item.add("トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time)
                            //Toot ID 文字列版
                            Item.add(toot_id_string)
                            //アバターURL
                            Item.add(user_avater_url)
                            //アカウントID
                            Item.add(account_id.toString())
                            //ユーザーネーム
                            Item.add(user)
                            //メディア
                            Item.add(media_url[0])
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)
                            //カード
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)

                            listItem = ListItem(Item)
                            val finalListItem = listItem
                            activity!!.runOnUiThread {
                                adapter.add(finalListItem)
                                adapter.notifyDataSetChanged()
                                //ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                home_listview.adapter = adapter
                            }
                        }
                    }
                } catch (e: Mastodon4jRequestException) {
                    e.printStackTrace()
                }

                return null
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        textView_home.setText(R.string.home)
        textView_home.setCompoundDrawables(home_icon, null, null, null)


        //ローカルタイムライン
        val localtimeline_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val localtime_textview = localtimeline_linearLayout.findViewById<TextView>(R.id.cardview_textview)
        //追加するレイアウト
        val localtimeline_main = localtimeline_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)

        val localtimeline_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_train_black_24dp, null)
        localtimeline_icon!!.setBounds(0, 0, localtimeline_icon.intrinsicWidth, localtimeline_icon.intrinsicHeight)

        localtime_textview.setText(R.string.public_time_line)
        localtime_textview.setCompoundDrawables(localtimeline_icon, null, null, null)

        //ListView
        val localtime_listview = ListView(localtimeline_main.context)
        val localtime_listview_layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 500)
        localtime_listview.layoutParams = localtime_listview_layoutParams
        localtimeline_main.addView(localtime_listview)
        //ScrollViewの中のListViewのスクロールができるように
        localtime_listview.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        val local_timeline_toot_list = ArrayList<ListItem>()

        val local_timeline_adapter = HomeTimeLineAdapter(context!!, R.layout.timeline_item, local_timeline_toot_list)

        //非同期通信
        val localtimeline_asyncTask = object : AsyncTask<String, Void, String>() {
            override fun doInBackground(vararg string: String): String? {

                val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                        .accessToken(finalAccessToken!!)
                        .useStreamingApi()
                        .build()


                val public_timeline = Public(client)

                try {
                    val statuses = public_timeline.getLocalPublic(Range(null, null, 40)).execute()
                    statuses.part.forEach { status ->
                        //System.out.println(status.getContent());
                        val toot_text = status.content
                        val user = status.account!!.userName
                        val user_name = status.account!!.displayName
                        val toot_id = status.id
                        val toot_id_string = toot_id.toString()
                        var toot_time: String? = null
                        val account_id = status.account!!.id

                        //ユーザーのアバター取得
                        val user_avater_url = status.account!!.avatar

                        val media_url = arrayOf<String>()
                        //めでぃあ
                        val list = status.mediaAttachments
                        list.forEach { media -> media_url[0] = media.url }

                        val japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false)
                        if (japan_timeSetting) {
                            //時差計算？
                            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                            //日本用フォーマット
                            val japanDateFormat = SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!, Locale.JAPAN)
                            try {
                                val date = simpleDateFormat.parse(status.createdAt)
                                val calendar = Calendar.getInstance()
                                calendar.time = date!!
                                //9時間足して日本時間へ
                                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")!!))
                                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                toot_time = japanDateFormat.format(calendar.time)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                        } else {
                            toot_time = status.createdAt
                        }

                        if (activity != null && isAdded) {
                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add("")
                            //内容
                            Item.add(toot_text)
                            //ユーザー名
                            Item.add("$user_name @$user")
                            //時間、クライアント名等
                            Item.add("トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time)
                            //Toot ID 文字列版
                            Item.add(toot_id_string)
                            //アバターURL
                            Item.add(user_avater_url)
                            //アカウントID
                            Item.add(account_id.toString())
                            //ユーザーネーム
                            Item.add(user)
                            //メディア
                            Item.add(media_url[0])
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)
                            //カード
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)

                            var listItem: ListItem? = null

                            listItem = ListItem(Item)
                            val finalListItem = listItem
                            activity!!.runOnUiThread {
                                //ListView listView = (ListView) view.findViewById(R.id.public_time_line_list);
                                local_timeline_adapter.insert(finalListItem, 0)
                                var y = 0
                                localtime_listview.adapter = local_timeline_adapter
                                val position = localtime_listview.firstVisiblePosition
                                if (localtime_listview.childCount > 0) {
                                    y = localtime_listview.getChildAt(0).top
                                }
                                localtime_listview.setSelectionFromTop(position, y)
                            }
                        }
                    }
                } catch (e: Mastodon4jRequestException) {
                    e.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(result: String) {}
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        //設定カード
        val setting_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val setting_textview = setting_linearLayout.findViewById<TextView>(R.id.cardview_textview)
        //View追加用
        val setting_main = setting_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)
        setting_main.orientation = LinearLayout.HORIZONTAL
        val mainLayout = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setting_main.layoutParams = mainLayout
        //タイトル
        val setting_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_settings_black_24dp, null)
        setting_icon!!.setBounds(0, 0, setting_icon.intrinsicWidth, setting_icon.intrinsicHeight)
        setting_textview.setText(R.string.quick_setting)
        setting_textview.setCompoundDrawables(setting_icon, null, null, null)

        //画像のサイズ
        val image_size = LinearLayout.LayoutParams(200, 200)
        //テキストサイズ
        val textsize = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //Layout
        val menuLayout = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        menuLayout.weight = 1f

        //ボタンを追加
        val setting_theme_linearLayout = LinearLayout(setting_linearLayout.context)
        setting_theme_linearLayout.orientation = LinearLayout.VERTICAL
        setting_theme_linearLayout.layoutParams = menuLayout
        //setting_linearLayout.setLayoutParams(textsize);
        val setting_theme_imageView = ImageView(setting_linearLayout.context)
        setting_theme_imageView.setImageResource(R.drawable.ic_format_paint_black_24dp)
        setting_theme_imageView.layoutParams = image_size
        //テキスト
        val setting_theme_textview = TextView(setting_linearLayout.context)
        setting_theme_textview.layoutParams = textsize
        setting_theme_textview.gravity = Gravity.CENTER

        //追加
        setting_theme_linearLayout.addView(setting_theme_imageView)
        setting_theme_linearLayout.addView(setting_theme_textview)

        //テーマ変更
        //現在の条件を取得
        val setting_theme = pref_setting.getBoolean("pref_dark_theme", true)
        val setting_theme_oled = pref_setting.getBoolean("pref_oled_mode", true)
        //ここで判断
        val theme_image = intArrayOf(1)
        if (setting_theme) {
            theme_image[0] = 1
        }
        if (setting_theme_oled) {
            theme_image[0] = 2
        }
        if (!setting_theme && !setting_theme_oled) {
            theme_image[0] = 3
        }
        //System.out.println("現在　：　" + String.valueOf(theme_image[0]));
        //ダークモードへ
        if (theme_image[0] == 1) {
            val editor = pref_setting.edit()
            editor.putBoolean("pref_dark_theme", true)
            editor.apply()
            setting_theme_imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN)
            setting_theme_textview.setText(R.string.setting_app_theme_dark_theme)
            theme_image[0]++
        } else if (theme_image[0] == 2) {
            val editor = pref_setting.edit()
            editor.putBoolean("pref_dark_theme", false)
            editor.putBoolean("pref_oled_mode", true)
            editor.apply()
            setting_theme_imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN)
            setting_theme_textview.setText(R.string.setting_app_theme_oled_theme)
            theme_image[0]++
        } else if (theme_image[0] == 3) {
            val editor = pref_setting.edit()
            editor.putBoolean("pref_dark_theme", false)
            editor.putBoolean("pref_oled_mode", false)
            editor.apply()
            setting_theme_imageView.setColorFilter(Color.parseColor("#2196f3"), PorterDuff.Mode.SRC_IN)
            setting_theme_textview.setText(R.string.nomal)
            theme_image[0] = 1
        }//標準テーマへ
        //有機ELモードへ
        setting_theme_imageView.setOnClickListener {
            //ダークモードへ
            if (theme_image[0] == 1) {
                val editor = pref_setting.edit()
                editor.putBoolean("pref_dark_theme", true)
                editor.apply()
                setting_theme_imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN)
                setting_theme_textview.setText(R.string.setting_app_theme_dark_theme)
                //Toast.makeText(getContext(), R.string.setting_app_theme, Toast.LENGTH_SHORT).show();
                theme_image[0]++
            } else if (theme_image[0] == 2) {
                val editor = pref_setting.edit()
                editor.putBoolean("pref_dark_theme", false)
                editor.putBoolean("pref_oled_mode", true)
                editor.apply()
                setting_theme_imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN)
                setting_theme_textview.setText(R.string.setting_app_theme_oled_theme)
                //Toast.makeText(getContext(), R.string.setting_app_theme, Toast.LENGTH_SHORT).show();
                theme_image[0]++
            } else if (theme_image[0] == 3) {
                val editor = pref_setting.edit()
                editor.putBoolean("pref_dark_theme", false)
                editor.putBoolean("pref_oled_mode", false)
                editor.apply()
                setting_theme_imageView.setColorFilter(Color.parseColor("#2196f3"), PorterDuff.Mode.SRC_IN)
                setting_theme_textview.setText(R.string.nomal)
                //Toast.makeText(getContext(), R.string.setting_app_theme, Toast.LENGTH_SHORT).show();
                theme_image[0] = 1
            }//標準テーマへ
            //有機ELモードへ

            //押したときにActivityを再生成する
            // アクティビティ再起動
            RestartActivity()
        }


        //ボタンを追加 通知
        val setting_notification_linearLayout = LinearLayout(setting_linearLayout.context)
        setting_notification_linearLayout.orientation = LinearLayout.VERTICAL
        setting_notification_linearLayout.layoutParams = menuLayout
        val setting_notification_imageView = ImageView(setting_linearLayout.context)
        setting_notification_imageView.setImageResource(R.drawable.ic_notifications_black_24dp)
        setting_notification_imageView.layoutParams = image_size
        val setting_notification_textview = TextView(setting_linearLayout.context)
        setting_notification_textview.gravity = Gravity.CENTER
        setting_notification_textview.setText(R.string.notifications)

        setting_notification_linearLayout.addView(setting_notification_imageView)
        setting_notification_linearLayout.addView(setting_notification_textview)

        //現在の条件を取得
        val setting_notification_toast = pref_setting.getBoolean("pref_notification_toast", true)
        val setting_notification_vibrate = pref_setting.getBoolean("pref_notification_vibrate", true)
        var notificaiton_count = 1
        if (setting_notification_toast) {
            //両方
            notificaiton_count = 1
        } else {
            //無効化
            notificaiton_count = 2
        }
        //System.out.println("ああああ" + String.valueOf(notificaiton_count));

        //設定をアイコン、テキストに反映させる
        //すべて
        if (notificaiton_count == 1) {
            setting_notification_imageView.setImageResource(R.drawable.ic_notifications_active_black_24dp)
            setting_notification_textview.setText(R.string.notifications)
        }
        //無効
        if (notificaiton_count == 2) {
            setting_notification_imageView.setImageResource(R.drawable.ic_notifications_off_black_24dp)
            setting_notification_textview.setText(R.string.mute)
        }

        //画像が押されたとき
        val finalNotificaiton_count = intArrayOf(notificaiton_count)
        setting_notification_imageView.setOnClickListener {
            if (finalNotificaiton_count[0] == 1) {
                val editor = pref_setting.edit()
                editor.putBoolean("pref_notification_toast", true)
                editor.putBoolean("pref_notification_vibrate", true)
                editor.apply()
                setting_notification_imageView.setImageResource(R.drawable.ic_notifications_active_black_24dp)
                setting_notification_textview.setText(R.string.notifications)
                finalNotificaiton_count[0]++
            } else if (finalNotificaiton_count[0] == 2) {
                val editor = pref_setting.edit()
                editor.putBoolean("pref_notification_toast", false)
                editor.putBoolean("pref_notification_vibrate", false)
                editor.apply()
                setting_notification_imageView.setImageResource(R.drawable.ic_notifications_off_black_24dp)
                setting_notification_textview.setText(R.string.mute)
                finalNotificaiton_count[0] = 1
            }//無効
        }


        //タイムライントースト
        val timeline_toast_linearLayout = LinearLayout(setting_linearLayout.context)
        timeline_toast_linearLayout.orientation = LinearLayout.VERTICAL
        timeline_toast_linearLayout.layoutParams = menuLayout
        val timeline_toast_imageView = ImageView(setting_linearLayout.context)
        timeline_toast_imageView.setImageResource(R.drawable.ic_rate_review_black_24dp)
        timeline_toast_imageView.layoutParams = image_size
        val timeline_toast_textView = TextView(setting_linearLayout.context)
        timeline_toast_textView.gravity = Gravity.CENTER
        timeline_toast_textView.setText(R.string.timeline_toast_disable)

        timeline_toast_linearLayout.addView(timeline_toast_imageView)
        timeline_toast_linearLayout.addView(timeline_toast_textView)

        val timeline_toast_count = IntArray(1)

        //変更
        if (pref_setting.getInt("timeline_toast_check", 0) == 1) {
            timeline_toast_count[0] = 1
        } else if (pref_setting.getInt("timeline_toast_check", 0) == 0) {
            timeline_toast_count[0] = 0
        }

        //画像を変更する
        if (timeline_toast_count[0] == 0) {
            timeline_toast_textView.setText(R.string.timeline_toast_disable)
        } else if (timeline_toast_count[0] == 1) {
            timeline_toast_textView.setText(R.string.notification_timeline)
        }

        timeline_toast_imageView.setOnClickListener {
            val listener: SharedPreferences.OnSharedPreferenceChangeListener

            listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                //見つける
                if (key == "timeline_toast_check") {
                    //System.out.println("よんだ！");
                }
            }
            pref_setting.registerOnSharedPreferenceChangeListener(listener)

            //画像を変更する
            if (timeline_toast_count[0] == 0) {
                val editor = pref_setting.edit()
                editor.putInt("timeline_toast_check", 0)
                editor.commit()
                timeline_toast_textView.setText(R.string.timeline_toast_disable)
                timeline_toast_count[0] = 1
            } else if (timeline_toast_count[0] == 1) {
                val editor = pref_setting.edit()
                editor.putInt("timeline_toast_check", 1)
                editor.commit()
                timeline_toast_textView.setText(R.string.notification_timeline)
                timeline_toast_count[0] = 0

            }
        }


        //背景画像

        //背景ImageView
        val background_imageView = view.findViewById<ImageView>(R.id.homecard_background_imageview)

        val background_LinearLayout = LinearLayout(setting_linearLayout.context)
        background_LinearLayout.orientation = LinearLayout.VERTICAL
        background_LinearLayout.layoutParams = menuLayout
        val background_setting_ImageView = ImageView(setting_linearLayout.context)
        background_setting_ImageView.setImageResource(R.drawable.ic_brush_black_24dp)
        background_setting_ImageView.layoutParams = image_size
        val background_settig_TextView = TextView(setting_linearLayout.context)
        background_settig_TextView.gravity = Gravity.CENTER
        background_settig_TextView.setText(R.string.setting_background_image)

        background_LinearLayout.addView(background_setting_ImageView)
        background_LinearLayout.addView(background_settig_TextView)

        var background_mode = ""
        var background_fit_image = ""

        //有効・無効のとき
        if (pref_setting.getBoolean("background_image", false)) {
            //有効
            background_mode = getString(R.string.background_image_off)

        } else {
            //無効
            background_mode = getString(R.string.background_image_on)
        }


        if (pref_setting.getBoolean("background_image", true)) {
            val uri = Uri.parse(pref_setting.getString("background_image_path", ""))
            Glide.with(context!!)
                    .load(uri)
                    .into(background_imageView)
        }


        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_fit_image = getString(R.string.background_imageview_center_not)
            background_imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            //無効
            background_fit_image = getString(R.string.background_imageview_center)
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f).toDouble() != 0.1) {
            background_imageView.alpha = pref_setting.getFloat("transparency", 1.0f)
        }


        val finalBackground_mode = background_mode
        val editor = pref_setting.edit()
        val finalBackground_fit_image = background_fit_image
        background_setting_ImageView.setOnClickListener {
            //ストレージ読み込みの権限があるか確認
            //許可してないときは許可を求める
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(activity!!)
                        .setTitle(getString(R.string.permission_dialog_titile))
                        .setMessage(getString(R.string.permission_dialog_message))
                        .setPositiveButton(getString(R.string.permission_ok)) { dialog, which ->
                            //権限をリクエストする
                            ActivityCompat.requestPermissions(activity!!,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    REQUEST_PERMISSION)
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
            }

            //許可
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


                val items = arrayOf(finalBackground_mode, getString(R.string.background_image_change), getString(R.string.background_image_delete), finalBackground_fit_image, getString(R.string.background_image_transparency_titile))
                AlertDialog.Builder(activity!!)
                        .setTitle(R.string.setting_background_image)
                        .setItems(items) { dialog, which ->
                            //有効無効
                            if (which == 0) {
                                if (pref_setting.getBoolean("background_image", true)) {
                                    editor.putBoolean("background_image", false)
                                    editor.commit()
                                } else {
                                    editor.putBoolean("background_image", true)
                                    editor.commit()
                                }
                            }
                            //選択
                            if (which == 1) {
                                //画像選択
                                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                                photoPickerIntent.type = "image/*"
                                startActivityForResult(photoPickerIntent, 1)
                                //onActivityResultで処理
                            }
                            //削除
                            if (which == 2) {
                                editor.putString("background_image_path", null)
                                editor.apply()
                            }
                            //画面に合わせる
                            if (which == 3) {
                                if (pref_setting.getBoolean("background_fit_image", true)) {
                                    editor.putBoolean("background_fit_image", false)
                                    editor.commit()
                                } else {
                                    editor.putBoolean("background_fit_image", true)
                                    editor.commit()
                                }
                            }
                            //透明度
                            if (which == 4) {
                                //ダイアログ
                                val editText_Transparency = EditText(context)
                                editText_Transparency.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                                val alertDialog_editTranspatency = AlertDialog.Builder(activity!!)
                                alertDialog_editTranspatency.setView(editText_Transparency)
                                alertDialog_editTranspatency.setTitle(getString(R.string.background_image_transparency_titile))
                                        .setMessage(getString(R.string.background_image_transparency_message))
                                        .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                                            val editTranspatency = editText_Transparency.text.toString()
                                            val transparency = java.lang.Float.parseFloat(editTranspatency)
                                            editor.putFloat("transparency", transparency)
                                            editor.apply()
                                        }
                                        .setNegativeButton(getString(R.string.cancel), null)
                                        .show()
                            }
                        }.show()
            }
        }


        //アカウント切り替え
        val multiaccount_LinearLayout = LinearLayout(setting_linearLayout.context)
        multiaccount_LinearLayout.orientation = LinearLayout.VERTICAL
        multiaccount_LinearLayout.layoutParams = menuLayout
        val multiaccount_setting_ImageView = ImageView(setting_linearLayout.context)
        multiaccount_setting_ImageView.setImageResource(R.drawable.ic_transfer_within_a_station_black_24dp)
        multiaccount_setting_ImageView.layoutParams = image_size
        val multiaccount_settig_TextView = TextView(setting_linearLayout.context)
        multiaccount_settig_TextView.gravity = Gravity.CENTER
        multiaccount_settig_TextView.textSize = 13f
        multiaccount_settig_TextView.setText(R.string.account_chenge)

        multiaccount_LinearLayout.addView(multiaccount_setting_ImageView)
        multiaccount_LinearLayout.addView(multiaccount_settig_TextView)

        //ポップアップメニューを展開する
        val menuBuilder = MenuBuilder(context!!)
        val popupMenu = PopupMenu(context, multiaccount_LinearLayout)

        //menuBuilder.add("にゃーん");
        val optionsMenu = MenuPopupHelper(context!!, menuBuilder, multiaccount_LinearLayout)
        optionsMenu.setForceShowIcon(true)

        //マルチアカウントを取ってくる
        //マルチアカウント
        //配列を使えば幸せになれそう！！！
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()

        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting.getString("instance_list", "")
        val account_instance_string = pref_setting.getString("access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance.add(instance_array.getString(i))
                }
            } catch (e: Exception) {

            }

        }

        if (multi_account_instance.size >= 1) {
            for (count in multi_account_instance.indices) {
                val multi_instance = multi_account_instance[count]
                val multi_access_token = multi_account_access_token[count]
                //読み込みってテキスト変更
                multiaccount_settig_TextView.setText(R.string.loading)
                object : AsyncTask<String, Void, String>() {
                    override fun doInBackground(vararg string: String): String? {
                        val client = MastodonClient.Builder(multi_instance, OkHttpClient.Builder(), Gson())
                                .accessToken(multi_access_token)
                                .build()

                        try {
                            val main_accounts = Accounts(client).getVerifyCredentials().execute()

                            val account_id = main_accounts.id
                            val display_name = main_accounts.displayName
                            val account_id_string = main_accounts.userName
                            val profile = main_accounts.note
                            val avater_url = main_accounts.avatar

                            //menuBuilder.add(display_name + "(" + account_id_string + " / " + multi_instance + ")");
                            //第二引数　ID　にカウントを渡している
                            menuBuilder.add(0, count, 0, "$display_name($account_id_string / $multi_instance)")

                        } catch (e: Mastodon4jRequestException) {
                            e.printStackTrace()
                        }

                        return null
                    }

                    override fun onPostExecute(result: String) {
                        //UIスレッドに戻ったらテキストを変更する
                        multiaccount_settig_TextView.setText(R.string.account_chenge)
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }


            //押したら表示
            multiaccount_LinearLayout.setOnClickListener {
                //追加中に押したら落ちるから回避
                if (menuBuilder.size() == multi_account_instance.size) {
                    optionsMenu.show()
                    menuBuilder.setCallback(object : MenuBuilder.Callback {
                        override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {

                            //ItemIdにマルチアカウントのカウントを入れている
                            val position = menuItem.itemId

                            val multi_instance = multi_account_instance[position]
                            val multi_access_token = multi_account_access_token[position]

                            val editor = pref_setting.edit()
                            editor.putString("main_instance", multi_instance)
                            editor.putString("main_token", multi_access_token)
                            editor.apply()

                            //アプリ再起動
                            val intent = Intent(context, Home::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)

                            return false
                        }

                        override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                        }
                    })

                } else {
                    Toast.makeText(context, R.string.loading, Toast.LENGTH_SHORT).show()
                }
            }
            setting_main.addView(multiaccount_LinearLayout)
        }


        //読み上げ
        val speech_LinearLayout = LinearLayout(setting_linearLayout.context)
        speech_LinearLayout.orientation = LinearLayout.VERTICAL
        speech_LinearLayout.layoutParams = menuLayout
        val speech_setting_ImageView = ImageView(setting_linearLayout.context)
        speech_setting_ImageView.setImageResource(R.drawable.ic_volume_off_black_24dp)
        speech_setting_ImageView.layoutParams = image_size
        val speech_TextView = TextView(setting_linearLayout.context)
        speech_TextView.gravity = Gravity.CENTER
        //speech_TextView.setTextSize(13);
        speech_TextView.setText(R.string.timeline_toast_disable)

        speech_LinearLayout.addView(speech_setting_ImageView)
        speech_LinearLayout.addView(speech_TextView)

        //現在の状態を確認
        if (pref_setting.getBoolean("pref_speech", false)) {
            speech_TextView.setText(R.string.speech_timeline)
            speech_setting_ImageView.setImageResource(R.drawable.ic_volume_up_black_24dp)
        } else {
            speech_TextView.setText(R.string.timeline_toast_disable)
            speech_setting_ImageView.setImageResource(R.drawable.ic_volume_off_black_24dp)
        }

        //クリック
        speech_LinearLayout.setOnClickListener {
            if (pref_setting.getBoolean("pref_speech", false)) {
                speech_TextView.setText(R.string.timeline_toast_disable)
                speech_setting_ImageView.setImageResource(R.drawable.ic_volume_off_black_24dp)
                val editor = pref_setting.edit()
                editor.putBoolean("pref_speech", false)
                editor.apply()
            } else {
                speech_TextView.setText(R.string.speech_timeline)
                speech_setting_ImageView.setImageResource(R.drawable.ic_volume_up_black_24dp)
                val editor = pref_setting.edit()
                editor.putBoolean("pref_speech", true)
                editor.apply()
            }
        }


        //ボタン一覧にいれる
        setting_main.addView(setting_theme_linearLayout)
        setting_main.addView(setting_notification_linearLayout)
        setting_main.addView(timeline_toast_linearLayout)
        setting_main.addView(speech_LinearLayout)
        setting_main.addView(background_LinearLayout)


        //通知カード
        val notification_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val notification_textview = notification_linearLayout.findViewById<TextView>(R.id.cardview_textview)
        //追加するレイアウト
        val notification_main = notification_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)
        val notification_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_notifications_black_24dp, null)
        notification_icon!!.setBounds(0, 0, localtimeline_icon.intrinsicWidth, localtimeline_icon.intrinsicHeight)
        notification_textview.setText(R.string.notifications)
        notification_textview.setCompoundDrawables(notification_icon, null, null, null)
        //ListView
        val notification_listview = ListView(localtimeline_main.context)
        val notification_listview_layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 500)
        localtime_listview.layoutParams = notification_listview_layoutParams
        notification_main.addView(notification_listview)
        //ScrollViewの中のListViewのスクロールができるように
        notification_listview.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        val notification_toot_list = ArrayList<ListItem>()

        val notification_adapter = HomeTimeLineAdapter(context!!, R.layout.timeline_item, notification_toot_list)

        object : AsyncTask<String, String, String>() {
            override fun doInBackground(vararg string: String): String? {
                val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                        .accessToken(finalAccessToken!!)
                        .useStreamingApi()
                        .build()

                val notifications = Notifications(client)


                try {

                    val statuses = notifications.getNotifications(Range(null, null, 30), null).execute()

                    statuses.part.forEach { status ->

                        val account = arrayOf(status.account!!.displayName)
                        var type = status.type
                        //time = status.getCreatedAt();
                        val avater_url = status.account!!.avatar
                        val user_id = status.account!!.userName
                        val user_acct = status.account!!.acct
                        val account_id = status.account!!.id
                        var toot_id_string: String? = null
                        val toot = arrayOf<String>()
                        var toot_id: Long = 0
                        var time: String? = null
                        var layout_type: String? = null

                        //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                        try {
                            toot[0] = status.status!!.content
                            toot_id = status.status!!.id
                            toot_id_string = toot_id.toString()
                        } catch (e: NullPointerException) {
                            toot[0] = ""
                            toot_id = 0
                            toot_id_string = toot_id.toString()
                        }

                        val japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false)
                        if (japan_timeSetting) {
                            //時差計算？
                            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                            //日本用フォーマット
                            val japanDateFormat = SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!, Locale.JAPAN)
                            try {
                                val date = simpleDateFormat.parse(status.createdAt)
                                val calendar = Calendar.getInstance()
                                calendar.time = date!!
                                //9時間足して日本時間へ
                                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")!!))
                                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                time = japanDateFormat.format(calendar.time)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                        } else {
                            time = status.createdAt
                        }

                        //カスタム絵文字
                        try {
                            //本文
                            val emoji_List = status.status!!.emojis
                            emoji_List.forEach { emoji ->
                                val emoji_name = emoji.shortcode
                                val emoji_url = emoji.url
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                toot[0] = toot[0].replace(":$emoji_name:", custom_emoji_src)
                            }

                        } catch (e: NullPointerException) {
                            toot[0] = ""
                            toot_id = 0
                            toot_id_string = toot_id.toString()
                        }

                        //DisplayNameのほう
                        val account_emoji_List = status.account!!.emojis
                        account_emoji_List.forEach { emoji ->
                            val emoji_name = emoji.shortcode
                            val emoji_url = emoji.url
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            account[0] = account[0].replace(":$emoji_name:", custom_emoji_src)
                        }


                        val locale = Locale.getDefault()
                        val jp = locale == Locale.JAPAN
                        val friends_nico_check_box = pref_setting.getBoolean("pref_friends_nico_mode", false)

                        if (type == "mention") {
                            if (jp) {
                                type = "返信しました"
                            }
                            layout_type = "Notification_mention"
                        }
                        if (type == "reblog") {
                            if (jp) {
                                type = "ブーストしました"
                            }
                            layout_type = "Notification_reblog"
                        }
                        if (type == "favourite") {
                            if (jp) {
                                if (friends_nico_check_box) {
                                    type = "お気に入りしました"
                                } else {
                                    type = "二コりました"
                                }
                                layout_type = "Notification_favourite"
                            }
                        }
                        if (type == "follow") {
                            if (jp) {
                                type = "フォローしました"
                            }
                            layout_type = "Notification_follow"
                        }

                        if (activity != null && isAdded) {
                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add(layout_type!!)
                            //内容
                            Item.add(toot[0])
                            //ユーザー名
                            Item.add(account[0] + " @" + user_acct + " / " + type)
                            //時間、クライアント名等
                            Item.add("トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + time)
                            //Toot ID 文字列版
                            Item.add(toot_id_string!!)
                            //アバターURL
                            Item.add(avater_url)
                            //アカウントID
                            Item.add(account_id.toString())
                            //ユーザーネーム
                            Item.add(avater_url)
                            //メディア
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)
                            //カード
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)
                            Item.add(null!!)

                            var listItem: ListItem? = null
                            listItem = ListItem(Item)
                            val finalListItem = listItem
                            activity!!.runOnUiThread {
                                notification_adapter.add(finalListItem)
                                notification_adapter.notifyDataSetChanged()
                                notification_listview.adapter = notification_adapter
                            }
                        }
                    }
                } catch (e: Mastodon4jRequestException) {
                    e.printStackTrace()
                }

                return null
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        //最後

        linearLayout_.addView(account_linearLayout, 0)

        linearLayout_.addView(linearLayout, 1)

        linearLayout_.addView(setting_linearLayout, 2)

        linearLayout_.addView(instance_linearLayout, 3)

        linearLayout_.addView(linearLayout_home, 4)

        linearLayout_.addView(notification_linearLayout, 5)

        linearLayout_.addView(localtimeline_linearLayout, 6)


    }


    //権限承認されたかな？
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val toast = Toast.makeText(context, R.string.permission_ok, Toast.LENGTH_SHORT)
                toast.show()
            } else {
                // それでも拒否された時の対応
                val toast = Toast.makeText(context, R.string.permission_block, Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }


    //画像処理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val background_imageView = view.findViewById<ImageView>(R.id.homecard_background_imageview)

        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data!!.data
                //完全パス取得
                val get_Path = getPath(selectedImage)
                val image_Path = "file:\\\\$get_Path"
                //置き換え
                val final_Path = image_Path.replace("\\\\".toRegex(), "/")

                //URI画像を入れる
                Glide.with(context!!)
                        .load(get_Path)
                        .into(background_imageView)
                //System.out.println("パス？ : " + final_Path);

                //ファイルパスを保存
                //file://の形じゃないとGIFに対応できない？（要検証
                val editor = pref_setting.edit()
                editor.putString("background_image_path", final_Path)
                editor.apply()

            }
    }


    override fun onDetach() {
        super.onDetach()
        //System.out.println("終了");
        //ストリーミング終了
        if (shutdownable != null) {
            shutdownable!!.shutdown()
        }
    }


    fun getPath(uri: Uri?): String {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = context!!.contentResolver.query(uri!!, projection, null, null, null)
        val column_index = cursor!!
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        cursor.moveToFirst()
        val imagePath = cursor.getString(column_index)

        return cursor.getString(column_index)
    }

    fun RestartActivity() {
        //押したときにActivityを再生成する
        // アクティビティ再起動
        val intent = Intent(context, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK // 起動しているActivityをすべて削除し、新しいタスクでMainActivityを起動する
        startActivity(intent)

    }

    companion object {

        private val RESULT_PICK_IMAGEFILE = 1001
    }


}