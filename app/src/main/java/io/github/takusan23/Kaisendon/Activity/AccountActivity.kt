package io.github.takusan23.Kaisendon.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.net.Uri
import android.os.AsyncTask
import android.preference.PreferenceManager
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Space
import android.widget.TextView

import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.squareup.picasso.Picasso
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Accounts

import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import io.github.takusan23.Kaisendon.Preference_ApplicationContext
import io.github.takusan23.Kaisendon.R
import okhttp3.OkHttpClient

class AccountActivity : AppCompatActivity() {

    internal var account_id: Long = 0

    internal var display_name: String? = null
    internal var user_account_id: String? = null
    internal var avater_url: String? = null
    internal var heander_url: String? = null
    internal var profile: String? = null
    internal var create_at: String? = null


    //カスタム絵文字関係
    internal var final_toot_text: String? = null
    internal var custom_emoji_src: String? = null
    internal var avater_emoji = false
    internal var avater_custom_emoji_src: String? = null


    internal var account_id_button: Long = 0

    internal var follow: Int = 0
    internal var follower: Int = 0

    private val dialog: ProgressDialog? = null

    internal lateinit var profile_textview: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

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


        setContentView(R.layout.activity_user_old)

        val handler_1 = android.os.Handler()


        //先に
        val displayname_textview = findViewById<TextView>(R.id.username)
        val id_textview = findViewById<TextView>(R.id.account_id)
        profile_textview = findViewById(R.id.profile)

        //画像
        val avater = findViewById<ImageView>(R.id.avater_user)
        val header = findViewById<ImageView>(R.id.header_user)

        //ボタン
        val follower_button = findViewById<Button>(R.id.follower_button)
        val follow_button = findViewById<Button>(R.id.follow_button)
        val toot_button = findViewById<Button>(R.id.toot_button)
        val follow_request_button = findViewById<Button>(R.id.follow_request_button)
        follow_request_button.setText(R.string.edit)


        //補足情報
        val fields_attributes_linearLayout = findViewById<LinearLayout>(R.id.fields_attributes_linearLayout)


        //プリファレンス
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

        // Backボタンを有効にする
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //くるくる
        val view = findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(view, getString(R.string.loading_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE)
        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 2
        //複数行対応させたおかげでずれたので修正
        val progressBar = ProgressBar(this@AccountActivity)
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBer_layoutParams
        snackBer_viewGrop.addView(progressBar, 0)
        snackbar.show()


        //非同期通信でアカウント情報を取得
        val finalInstance = Instance
        val finalAccessToken = AccessToken

        //Icon
        val back_icon = BitmapFactory.decodeResource(resources, R.drawable.baseline_arrow_back_black_24dp)

        //どうでもいい
        //
        //        SpannableString spannableString = new SpannableString("アイコンテスト : ");
        //        spannableString.setSpan(new ImageSpan(AccountActivity.this, R.mipmap.ic_launcher), 7, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //        displayname_textview.setText(spannableString);
        //
        //ImageGetter
        //カスタム絵文字
        val toot_imageGetter = Html.ImageGetter { source ->
            val d = LevelListDrawable()
            val empty = resources.getDrawable(R.drawable.ic_refresh_black_24dp)
            d.addLevel(0, 0, empty)
            d.setBounds(0, 0, empty.intrinsicWidth, empty.intrinsicHeight)

            LoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, source, d)

            d
        }


        val asyncTask = object : AsyncTask<String, Void, String>() {
            override fun doInBackground(vararg string: String): String? {

                val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                        .accessToken(finalAccessToken!!)
                        .build()

                try {

                    val account = Accounts(client).getVerifyCredentials().execute()

                    display_name = account.displayName
                    user_account_id = account.userName
                    profile = account.note
                    avater_url = account.avatar
                    heander_url = account.header
                    //create_at = account.getCreatedAt();

                    follow = account.followingCount
                    follower = account.followersCount

                    account_id_button = account.id


                    //時刻表記を直す
                    val japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false)
                    if (japan_timeSetting) {
                        //時差計算？
                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                        //日本用フォーマット
                        val japanDateFormat = SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!, Locale.JAPAN)
                        try {
                            val date = simpleDateFormat.parse(account.createdAt)
                            val calendar = Calendar.getInstance()
                            calendar.time = date!!
                            //9時間足して日本時間へ
                            calendar.add(Calendar.HOUR, pref_setting.getString("pref_time_add", "9")!!.toInt())
                            //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                            create_at = japanDateFormat.format(calendar.time)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }

                    } else {
                        create_at = account.createdAt
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
                } catch (e1: Mastodon4jRequestException) {
                    e1.printStackTrace()
                }

                handler_1.post {
                    displayname_textview.text = display_name
                    displayname_textview.textSize = 20f
                    id_textview.text = "@$user_account_id@$finalInstance\r\n$create_at"
                    if (pref_setting.getBoolean("pref_custom_emoji", true)) {
                        try {
                            profile_textview.text = Html.fromHtml(profile, toot_imageGetter, null)
                        } catch (e: NullPointerException) {
                            profile_textview.text = Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT)
                        }

                    } else {
                        profile_textview.text = Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT)
                    }


                    follow_button.text = "フォロー : $follow"
                    follower_button.text = "フォロワー : $follower"

                    //タイトル
                    supportActionBar!!.setTitle(display_name)
                    supportActionBar!!.setSubtitle("@$user_account_id@$finalInstance")

                    val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)
                    if (setting_avater_gif) {

                        //GIFアニメ再生させない
                        Picasso.get()
                                .load(avater_url)
                                .into(avater)

                        Picasso.get()
                                .load(heander_url)
                                .into(header)

                    } else {

                        //GIFアニメを再生
                        Glide.with(this@AccountActivity)
                                .load(avater_url)
                                .into(avater)

                        Glide.with(this@AccountActivity)
                                .load(heander_url)
                                .into(header)
                    }
                }


                //friends.nicoモードかな？
                val frenico_mode = pref_setting.getBoolean("setting_friends_nico_mode", true)
                //Chrome Custom Tab
                val chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true)

                val nico_url = arrayOf<String>()

                //Json解析
                try {
                    val account_nico_url = Accounts(client).getVerifyCredentials().doOnJson { jsonString ->
                        //System.out.println(jsonString);
                        //String string_ = "{\"int array\":[100,200,300],\"boolean\":true,\"string\":\"string\",\"object\":{\"object_1\":1,\"object_3\":3,\"object_2\":2},\"null\":null,\"array\":[1,2,3],\"long\":18000305032230531,\"int\":100,\"double\":10.5}";
                        val parser = JsonParser()
                        var jsonObject: JSONObject? = null
                        try {
                            jsonObject = JSONObject(jsonString)

                            nico_url[0] = jsonObject.getString("nico_url")

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }


                    }.execute()
                } catch (e1: Mastodon4jRequestException) {
                    e1.printStackTrace()
                }


                //URLあるよ
                if (frenico_mode && nico_url[0] != null) {
                    //ニコニコURLへ
                    val button = findViewById<Button>(R.id.button3)
                    runOnUiThread { button.text = "ニコニコ" }

                    button.setOnClickListener {
                        if (chrome_custom_tabs) {

                            val custom = CustomTabsHelper.getPackageNameToUse(this@AccountActivity)

                            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                            val customTabsIntent = builder.build()
                            customTabsIntent.intent.setPackage(custom)
                            customTabsIntent.launchUrl(this@AccountActivity as Activity, Uri.parse(nico_url[0]))
                            //無効
                        } else {
                            val uri = Uri.parse(nico_url[0])
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }
                    }
                    //URLなかった
                } else {
                    val button = findViewById<Button>(R.id.button3)
                    runOnUiThread { button.text = "Web" }
                    button.setOnClickListener {
                        if (chrome_custom_tabs) {

                            val custom = CustomTabsHelper.getPackageNameToUse(this@AccountActivity)

                            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                            val customTabsIntent = builder.build()
                            customTabsIntent.intent.setPackage(custom)
                            customTabsIntent.launchUrl(this@AccountActivity as Activity, Uri.parse(nico_url[0]))
                            //無効
                        } else {
                            val uri = Uri.parse(nico_url[0])
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }
                    }
                }

                //補足情報
                //Json解析
                try {
                    val fields_attributes_account = Accounts(client).getVerifyCredentials().doOnJson { fields_attributes_account_jsonString ->
                        var jsonObject: JSONObject? = null
                        try {
                            jsonObject = JSONObject(fields_attributes_account_jsonString)

                            //補足情報取得

                            //補足情報まで案内
                            val source = jsonObject!!.getJSONObject("source")
                            val fields = source.getJSONArray("fields")
                            //同じコードを書きたくない？のでwhileつかう
                            var count = 0
                            while (count <= fields.length()) {

                                val fields_attributes_account_jsonObject = fields.getJSONObject(count)
                                //名前を取得
                                val name = fields_attributes_account_jsonObject.getString("name")
                                //情報
                                val value = fields_attributes_account_jsonObject.getString("value")
                                //レイアウトをつくる
                                //調子悪いのでUIスレッドで
                                runOnUiThread {
                                    val fields_attributes_content = LinearLayout(this@AccountActivity)
                                    fields_attributes_content.orientation = LinearLayout.VERTICAL
                                    //テキストビュー
                                    val fields_attributes_name_textview = TextView(this@AccountActivity)
                                    val fields_attributes_value_textview = TextView(this@AccountActivity)
                                    val fields_attributes_params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                    fields_attributes_params.weight = 1f
                                    val fields_attributes_params_2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                    fields_attributes_params_2.weight = 2f
                                    //名前
                                    fields_attributes_name_textview.autoLinkMask = Linkify.ALL
                                    fields_attributes_name_textview.text = Html.fromHtml(name, Html.FROM_HTML_MODE_COMPACT)
                                    fields_attributes_name_textview.textSize = 18f
                                    //fields_attributes_name_textview.setBackgroundColor(Color.parseColor("#999999"));
                                    fields_attributes_name_textview.layoutParams = fields_attributes_params_2
                                    //説明
                                    fields_attributes_value_textview.autoLinkMask = Linkify.ALL
                                    fields_attributes_value_textview.text = Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT)
                                    fields_attributes_value_textview.textSize = 18f
                                    //fields_attributes_value_textview.setBackgroundColor(Color.parseColor("#cccccc"));
                                    fields_attributes_value_textview.layoutParams = fields_attributes_params
                                    //空白
                                    val sp = Space(this@AccountActivity)
                                    //枠
                                    fields_attributes_content.background = getDrawable(R.drawable.button_style)
                                    //セット
                                    fields_attributes_content.addView(fields_attributes_name_textview)
                                    fields_attributes_content.addView(fields_attributes_value_textview)
                                    fields_attributes_linearLayout.addView(fields_attributes_content)
                                    fields_attributes_linearLayout.addView(sp, LinearLayout.LayoutParams(20, 1))
                                }
                                count++
                            }


                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }


                    }.execute()
                } catch (e1: Mastodon4jRequestException) {
                    e1.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(result: String) {
                snackbar.dismiss()
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        //ボタンクリック
        follow_button.setOnClickListener {
            val follow = Intent(this@AccountActivity, UserFollowActivity::class.java)
            follow.putExtra("account_id", account_id_button)
            follow.putExtra("follow_follower", 1)
            startActivity(follow)
        }

        follower_button.setOnClickListener {
            val follower = Intent(this@AccountActivity, UserFollowActivity::class.java)
            follower.putExtra("account_id", account_id_button)
            follower.putExtra("follow_follower", 2)
            startActivity(follower)
        }

        toot_button.setOnClickListener {
            val follower = Intent(this@AccountActivity, UserFollowActivity::class.java)
            follower.putExtra("account_id", account_id_button)
            follower.putExtra("follow_follower", 3)
            startActivity(follower)
        }

        //アカウント情報を更新する
        follow_request_button.setOnClickListener {
            val account_info_update = Intent(this@AccountActivity, AccountInfoUpdateActivity::class.java)
            startActivity(account_info_update)
        }


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    internal inner class LoadImage : AsyncTask<Any, Void, Bitmap>() {

        private var mDrawable: LevelListDrawable? = null

        override fun doInBackground(vararg params: Any): Bitmap? {
            val source = params[0] as String
            mDrawable = params[1] as LevelListDrawable
            Log.d(TAG, "doInBackground $source")
            try {
                val `is` = URL(source).openStream()
                return BitmapFactory.decodeStream(`is`)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            Log.d(TAG, "onPostExecute drawable " + mDrawable!!)
            Log.d(TAG, "onPostExecute bitmap " + bitmap!!)
            if (bitmap != null) {
                val d = BitmapDrawable(bitmap)
                mDrawable!!.addLevel(1, 1, d)
                mDrawable!!.setBounds(0, 0, 40, 40)
                mDrawable!!.level = 1
                // i don't know yet a better way to refresh TextView
                // mTv.invalidate() doesn't work as expected
                val t = profile_textview.text
                profile_textview.text = t
            }
        }
    }

    companion object {
        private val TAG = "TestImageGetter"
    }


}
