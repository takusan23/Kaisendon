package io.github.takusan23.Kaisendon.Activity

import androidx.appcompat.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.text.util.Linkify
import android.view.Display
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar

import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.PicassoImageGetter
import io.github.takusan23.Kaisendon.Preference_ApplicationContext
import io.github.takusan23.Kaisendon.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class UserActivity : AppCompatActivity() {
    //private TextView mTv;
    internal var AccessToken: String? = null
    internal var Instance: String? = null


    //がんばってレイアウト作り直すわ
    private var user_activity_LinearLayout: LinearLayout? = null

    internal var account_id: String? = null

    private var display_name: String? = null
    private var user_account_id: String? = null
    private var userId: String? = null
    private var avater_url: String? = null
    private var header_url: String? = null
    private var profile: String? = null
    private var create_at: String? = null
    private var remote: String? = null

    //カスタム絵文字表示に使う配列
    private val emojis_shortcode = ArrayList<String>()
    private val emojis_url = ArrayList<String>()
    private var emojis_show = false

    internal var custom_emoji_src: String? = null

    internal var profile_textview: TextView? = null

    internal var follow_id: Long = 0

    internal var user_url: String? = null
    internal var final_toot_text: String? = null

    internal var follow: Int = 0
    internal var follower: Int = 0
    internal var status_count: Int = 0

    private var fieldsJsonArray: JSONArray? = null
    private var nico_url: String? = null

    private val dialog: ProgressDialog? = null

    //フォロー/フォローしてない
    internal var following = false

    private var pref_setting: SharedPreferences? = null

    private val snackbar: Snackbar? = null

    private var followButton: Button? = null
    private var headerImageView: ImageView? = null
    private var display_name_avatar_LinearLayout: LinearLayout? = null
    private var simpleDateFormat: SimpleDateFormat? = null
    private var japanDateFormat: SimpleDateFormat? = null
    private var darkModeSupport: DarkModeSupport? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

        //ダークテーマに切り替える機能
        darkModeSupport = DarkModeSupport(this)
        darkModeSupport!!.setActivityTheme(this)
        setContentView(R.layout.activity_user)

        val handler_1 = android.os.Handler()

        val intent = intent
        account_id = intent.getStringExtra("Account_ID")


        AccessToken = pref_setting!!.getString("main_token", "")
        Instance = pref_setting!!.getString("main_instance", "")

        //背景
        val background_imageView = findViewById<ImageView>(R.id.user_activity_background_imageview)

/*
        if (pref_setting!!.getBoolean("background_image", true)) {
            val uri = Uri.parse(pref_setting!!.getString("background_image_path", ""))
            Glide.with(this@UserActivity)
                    .load(uri)
                    .into(background_imageView)
        }
*/


        // Backボタンを有効にする
        //        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //        getSupportActionBar().setHomeButtonEnabled(true);

        //くるくる
        /*
        dialog = new ProgressDialog(UserActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("ユーザー情報を取得中 \r\n /api/v1/accounts");
*/
        //        dialog.show();

        followButton = Button(this@UserActivity)

        //アカウント情報読み込み
        //Misskeyと分ける
        if (getIntent().getBooleanExtra("Misskey", false)) {
            loadMisskeyAccount()
        } else {
            loadAccount()
        }

    }

    /**
     * アカウント情報を取得する
     */
    private fun loadAccount() {
        val view = findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(view, getString(R.string.loading_user_info) + "\r\n /api/v1/accounts", Snackbar.LENGTH_INDEFINITE)
        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 2
        //複数行対応させたおかげでずれたので修正
        val progressBar = ProgressBar(this@UserActivity)
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBer_layoutParams
        snackBer_viewGrop.addView(progressBar, 0)
        snackbar.show()
        //APIを叩く
        val url = "https://$Instance/api/v1/accounts/$account_id"
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        //GETリクエスト
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                //System.out.println(response_string);
                try {
                    val jsonObject = JSONObject(response_string)
                    display_name = jsonObject.getString("display_name")
                    user_account_id = jsonObject.getString("acct")
                    profile = jsonObject.getString("note")
                    avater_url = jsonObject.getString("avatar")
                    header_url = jsonObject.getString("header")
                    create_at = jsonObject.getString("created_at")
                    remote = jsonObject.getString("acct")
                    userId = jsonObject.getString("id")

                    follow = jsonObject.getInt("following_count")
                    follower = jsonObject.getInt("followers_count")
                    status_count = jsonObject.getInt("statuses_count")

                    //nico_url
                    if (!jsonObject.isNull("nico_url")) {
                        nico_url = jsonObject.getString("nico_url")
                    }

                    fieldsJsonArray = jsonObject.getJSONArray("fields")

                    //emojisをパースして配列に入れる
                    val emojis = jsonObject.getJSONArray("emojis")
                    for (i in 0 until emojis.length()) {
                        val emojiObject = emojis.getJSONObject(i)
                        emojis_shortcode.add(emojiObject.getString("shortcode"))
                        emojis_url.add(emojiObject.getString("url"))
                    }
                    if (!jsonObject.isNull("profile_emojis")) {
                        //profile_emojis
                        val profile_emojis = jsonObject.getJSONArray("profile_emojis")
                        for (i in 0 until profile_emojis.length()) {
                            val emojiObject = profile_emojis.getJSONObject(i)
                            emojis_shortcode.add(emojiObject.getString("shortcode"))
                            emojis_url.add(emojiObject.getString("url"))
                        }
                    }

                    //Wi-Fi接続状況確認
                    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

                    //カスタム絵文字有効時
                    if (pref_setting!!.getBoolean("pref_custom_emoji", true)) {
                        if (pref_setting!!.getBoolean("pref_avater_wifi", true)) {
                            //WIFIのみ表示有効時
                            if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                //WIFI
                                setCustomEmoji()
                                emojis_show = true
                            }
                        } else {
                            //WIFI/MOBILE DATA 関係なく表示
                            setCustomEmoji()
                            emojis_show = true
                        }
                    }

                    //レイアウト構成
                    runOnUiThread {
                        setLayout()
                        snackbar.dismiss()
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * Misskey 用
     */
    private fun loadMisskeyAccount() {
        val instance = pref_setting!!.getString("misskey_main_instance", "")
        val token = pref_setting!!.getString("misskey_main_token", "")
        val username = pref_setting!!.getString("misskey_main_username", "")
        val url = "https://$instance/api/users/show"
        //読み込み中お知らせ
        val view = findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(view, getString(R.string.loading_user_info) + "\r\n" + url, Snackbar.LENGTH_INDEFINITE)
        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 2
        //複数行対応させたおかげでずれたので修正
        val progressBar = ProgressBar(this@UserActivity)
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBer_layoutParams
        snackBer_viewGrop.addView(progressBar, 0)
        snackbar.show()
        //Request
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("userId", intent.getStringExtra("Account_ID"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
        //作成
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失敗
                runOnUiThread { Toast.makeText(this@UserActivity, getString(R.string.error), Toast.LENGTH_SHORT).show() }

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@UserActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    snackbar.dismiss()
                    // System.out.println(response_string);
                    try {
                        val jsonObject = JSONObject(response_string)
                        display_name = jsonObject.getString("name")
                        user_account_id = jsonObject.getString("username")
                        profile = jsonObject.getString("description")
                        avater_url = jsonObject.getString("avatarUrl")
                        create_at = jsonObject.getString("createdAt")
                        //ここusername+インスタンス名ね
                        remote = jsonObject.getString("host")
                        if (remote!!.isEmpty()) {
                            remote = "$user_account_id@$remote"
                        } else {
                            //nullじゃなくてなにもない文字にする
                            remote = user_account_id
                        }
                        if (!jsonObject.isNull("bannerUrl")) {
                            header_url = jsonObject.getString("bannerUrl")
                        } else {
                            header_url = ""
                        }
                        account_id = jsonObject.getString("id")

                        follow = jsonObject.getInt("followingCount")
                        follower = jsonObject.getInt("followersCount")
                        status_count = jsonObject.getInt("notesCount")

                        //fieldsJsonArray = jsonObject.getJSONArray("fields");

                        //emojisをパースして配列に入れる
                        val emojis = jsonObject.getJSONArray("emojis")
                        for (i in 0 until emojis.length()) {
                            val emojiObject = emojis.getJSONObject(i)
                            emojis_shortcode.add(emojiObject.getString("name"))
                            emojis_url.add(emojiObject.getString("url"))
                        }

                        runOnUiThread {
                            //display_nameはすでにカスタム絵文字用に置き換えてるので直接
                            try {
                                title = jsonObject.getString("name") + " @" + username + "@" + remote
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        //Wi-Fi接続状況確認
                        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

                        //カスタム絵文字有効時
                        if (pref_setting!!.getBoolean("pref_custom_emoji", true)) {
                            if (pref_setting!!.getBoolean("pref_avater_wifi", true)) {
                                //WIFIのみ表示有効時
                                if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                    //WIFI
                                    setCustomEmoji()
                                    emojis_show = true
                                }
                            } else {
                                //WIFI/MOBILE DATA 関係なく表示
                                setCustomEmoji()
                                emojis_show = true
                            }
                        }

                        runOnUiThread {
                            setLayout()
                            //フォロー中ってテキスト変更
                            try {
                                if (jsonObject.getBoolean("isFollowing")) {
                                    followButton!!.text = resources.getString(R.string.following)
                                    followButton!!.setPadding(10, 10, 10, 10)
                                    followButton!!.setTextColor(Color.parseColor("#2196f3"))
                                    val favIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done_black_24dp, null)
                                    favIcon!!.setTint(Color.parseColor("#2196f3"))
                                    followButton!!.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null)
                                }
                                //フォローされている
                                if (jsonObject.getBoolean("isFollowed")) {
                                    followButton!!.text = followButton!!.text.toString() + "\n" + resources.getString(R.string.follow_back)
                                    followButton!!.setTextColor(Color.parseColor("#2196f3"))
                                    val favIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done_all_black_24dp, null)
                                    favIcon!!.setTint(Color.parseColor("#2196f3"))
                                    followButton!!.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null)
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })
    }


    /**
     * レイアウト<br></br>
     * 必ず、<font color="red">UIスレッド</font>で呼べよ
     */
    private fun setLayout() {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //カードUIの用な感じに
        val top_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val cardView = top_linearLayout.findViewById<View>(R.id.cardview) as CardView
        val textView = top_linearLayout.findViewById<View>(R.id.cardview_textview) as TextView
        //ここについか
        val main_LinearLayout = top_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)

        //名前とかアバター画像とか
        display_name_avatar_LinearLayout = LinearLayout(this@UserActivity)
        display_name_avatar_LinearLayout!!.orientation = LinearLayout.VERTICAL
        display_name_avatar_LinearLayout!!.gravity = Gravity.CENTER
        display_name_avatar_LinearLayout!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        display_name_avatar_LinearLayout!!.background = getDrawable(R.drawable.button_style_white)


        val title_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_create_black_24dp_black, null)
        title_icon!!.setBounds(0, 0, title_icon.intrinsicWidth, title_icon.intrinsicHeight)
        //ImageViewとか
        headerImageView = ImageView(this@UserActivity)
        //今はどっちもMATCH_PARENTになっているけどレイアウトが完成したときにサイズを調整するからおｋ
        headerImageView!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        Glide.with(this@UserActivity).load(header_url).into(headerImageView!!)


        val avatarImageView = ImageView(this@UserActivity)
        avatarImageView.setPadding(10, 10, 10, 10)
        val display_name_TextView = TextView(this@UserActivity)
        display_name_TextView.setPadding(10, 10, 10, 10)
        val frameLayout = FrameLayout(this@UserActivity)
        //真ん中
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER
        frameLayout.layoutParams = layoutParams

        //TextView
        //カスタム絵文字サポート
        val imageGetter = PicassoImageGetter(display_name_TextView)
        display_name_TextView.text = Html.fromHtml(display_name, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
        display_name_TextView.append("\n@" + remote!!)
        display_name_TextView.append(" / " + userId!!)
        display_name_TextView.setTextColor(Color.parseColor("#000000"))
        display_name_TextView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        display_name_TextView.gravity = Gravity.CENTER
        //ImageView
        Glide.with(this@UserActivity).load(avater_url).apply(RequestOptions().override(200)).into(avatarImageView)
        avatarImageView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //Button
        followButton!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        followButton!!.background = getDrawable(R.drawable.button_style)
        followButton!!.text = getString(R.string.follow)
        followButton!!.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_person_add_black_24dp), null, null, null)
        //自分の場合は編集ボタンを出す
        if (intent.getBooleanExtra("my", false)) {
            followButton!!.text = getString(R.string.edit)
            followButton!!.setTextColor(Color.parseColor("#000000"))
            followButton!!.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_create_black_24dp_black), null, null, null)
            followButton!!.setOnClickListener {
                val intent = Intent(this@UserActivity, AccountInfoUpdateActivity::class.java)
                intent.putExtra("Misskey", getIntent().getBooleanExtra("Misskey", false))
                startActivity(intent)
            }
        } else {
            //フォロー状態取得
            //MisskeyはすでにあるのでMastodonだけ
            if (!CustomMenuTimeLine.isMisskeyMode) {
                getFollowInfo()
            }
            //クリックイベント
            followButton!!.setOnClickListener {
                //フォローしてるかで条件分岐
                if (!following) {
                    if (pref_setting!!.getBoolean("pref_follow_dialog", true)) {
                        var url = ""
                        if (CustomMenuTimeLine.isMisskeyMode) {
                            url = "/api/following/create"
                        } else {
                            url = "/api/v1/follows"
                        }
                        val alertDialog = AlertDialog.Builder(this@UserActivity)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(display_name + "(@ " + user_account_id + ")" + getString(R.string.follow_message) + "\r\n " + url + "(" + remote + ")")
                        alertDialog.setPositiveButton(R.string.follow) { dialog, which ->
                            //Misskey分岐
                            if (CustomMenuTimeLine.isMisskeyMode) {
                                postMisskeyFF("/api/following/create", getString(R.string.follow_ok))
                            } else {
                                //リモートフォローかそうじゃないか
                                //@があるかどうか
                                if (remote!!.contains("@")) {
                                    //remote follow
                                    remoteFollow(getString(R.string.follow_ok))
                                } else {
                                    follow("follow", getString(R.string.follow_ok))
                                }
                            }
                        }.show()
                    } else {
                        //Misskey分岐
                        if (CustomMenuTimeLine.isMisskeyMode) {
                            postMisskeyFF("/api/following/create", getString(R.string.follow_ok))
                        } else {
                            //リモートフォローかそうじゃないか
                            //@があるかどうか
                            if (remote!!.contains("@")) {
                                //remote follow
                                remoteFollow(getString(R.string.follow_ok))
                            } else {
                                follow("follow", getString(R.string.follow_ok))
                            }
                        }
                    }
                } else {
                    //ふぉろーはずし
                    if (pref_setting!!.getBoolean("pref_follow_dialog", true)) {
                        var url = ""
                        if (CustomMenuTimeLine.isMisskeyMode) {
                            url = "/api/following/delete"
                        } else {
                            url = "/api/v1/follows"
                        }
                        val alertDialog = AlertDialog.Builder(this@UserActivity)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(display_name + "(@ " + user_account_id + ")" + getString(R.string.unfollow_message) + "\r\n " + url + "(" + remote + ")")
                        alertDialog.setPositiveButton(R.string.follow) { dialog, which ->
                            //Misskey分岐
                            if (CustomMenuTimeLine.isMisskeyMode) {
                                postMisskeyFF("/api/following/delete", getString(R.string.unfollow_ok))
                            } else {
                                follow("unfollow", getString(R.string.unfollow_ok))
                            }
                        }.show()
                    } else {
                        //Misskey分岐
                        if (CustomMenuTimeLine.isMisskeyMode) {
                            postMisskeyFF("api/following/delete", getString(R.string.unfollow_ok))
                        } else {
                            follow("unfollow", getString(R.string.unfollow_ok))
                        }
                    }
                }
            }
        }

        display_name_avatar_LinearLayout!!.addView(avatarImageView)
        display_name_avatar_LinearLayout!!.addView(display_name_TextView)
        display_name_avatar_LinearLayout!!.addView(followButton)
        headerImageSize()

        //nico_url Button
        if (nico_url != null) {
            val nicoButton = Button(this@UserActivity)
            nicoButton.setPadding(10, 10, 10, 10)
            nicoButton.text = "niconico"
            nicoButton.background = getDrawable(R.drawable.button_style)
            nicoButton.setTextColor(Color.parseColor("#000000"))
            nicoButton.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            nicoButton.setOnClickListener {
                val chrome_custom_tabs = pref_setting!!.getBoolean("pref_chrome_custom_tabs", true)
                //戻るアイコン
                val back_icon = BitmapFactory.decodeResource(resources, R.drawable.ic_action_arrow_back)
                //有効
                if (chrome_custom_tabs) {
                    val custom = CustomTabsHelper.getPackageNameToUse(this@UserActivity)
                    val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                    val customTabsIntent = builder.build()
                    customTabsIntent.intent.setPackage(custom)
                    customTabsIntent.launchUrl(this@UserActivity, Uri.parse(nico_url))
                    //無効
                } else {
                    val uri = Uri.parse(nico_url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            display_name_avatar_LinearLayout!!.addView(nicoButton)
            headerImageSize()
        }

        //FrameLayoutに入れる
        frameLayout.addView(display_name_avatar_LinearLayout)


        //今回はタイトルを使ってないので消す
        (top_linearLayout.findViewById<View>(R.id.cardview_linearLayout) as LinearLayout).removeView(textView)


        //追加
        main_LinearLayout.addView(headerImageView)
        cardView.addView(frameLayout)


        //ふぉろーふぉろわーすてーたす
        //なげえし分けるわ
        val menuCardLinearLayout = followMenuCard()
        //説明文
        val noteCardLinearLayout = noteCard()
        //作成時
        val create_atCard = create_atCard()
        //CardView追加
        user_activity_LinearLayout!!.addView(top_linearLayout)
        user_activity_LinearLayout!!.addView(menuCardLinearLayout)
        user_activity_LinearLayout!!.addView(create_atCard)
        user_activity_LinearLayout!!.addView(noteCardLinearLayout)
        //補足情報
        //Misskeyは飛ばす
        if (!CustomMenuTimeLine.isMisskeyMode && fieldsJsonArray != null) {
            val fieldsLinearLayout = fieldsCard()
            if (!fieldsJsonArray!!.isNull(0)) {
                user_activity_LinearLayout!!.addView(fieldsLinearLayout)
            }
        }

        //snackbar.dismiss();

    }


    /**
     * フォローしている等状況を取得する
     */
    private fun getFollowInfo() {
        val url = "https://$Instance/api/v1/accounts/relationships/?stream=user&access_token=$AccessToken"

        //パラメータを設定
        val builder = HttpUrl.parse(url)!!.newBuilder()
        builder.addQueryParameter("id", account_id.toString())
        val final_url = builder.build().toString()

        //作成
        val request = Request.Builder()
                .url(final_url)
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //JSON化
                //System.out.println("レスポンス : " + response.body().string());
                val response_string = response.body()!!.string()
                try {
                    val jsonArray = JSONArray(response_string)
                    val jsonObject = jsonArray.getJSONObject(0)

                    runOnUiThread {
                        //フォロー中ってテキスト変更
                        try {
                            if (jsonObject.getBoolean("following")) {
                                followButton!!.text = resources.getString(R.string.following)
                                followButton!!.setTextColor(Color.parseColor("#2196f3"))
                                val favIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done_black_24dp, null)
                                favIcon!!.setTint(Color.parseColor("#2196f3"))
                                followButton!!.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null)
                            }
                            //フォローされている
                            if (jsonObject.getBoolean("followed_by")) {
                                followButton!!.text = followButton!!.text.toString() + "\n" + resources.getString(R.string.follow_back)
                                followButton!!.setTextColor(Color.parseColor("#2196f3"))
                                val favIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done_all_black_24dp, null)
                                favIcon!!.setTint(Color.parseColor("#2196f3"))
                                followButton!!.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null)
                            }
                            //背景のImageViewの大きさを変更する
                            //なんかボタンのテキストが改行されて二行になると下に白あまりができるので
                            headerImageSize()

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }


                    val followed_by = jsonObject.getBoolean("followed_by")
                    val blocking = jsonObject.getBoolean("blocking")
                    val muting = jsonObject.getBoolean("muting")
                    following = jsonObject.getBoolean("following")


                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * ボタンを押したときに移動するやつを指定。
     *
     * @param i １でstatuses、２でfollowing、３でfollower
     */
    private fun clickAcitvityMode(i: Int) {
        val follow_intent = Intent(this@UserActivity, UserFollowActivity::class.java)
        follow_intent.putExtra("account_id", account_id)
        follow_intent.putExtra("count", follow)
        //Misskey？
        if (CustomMenuTimeLine.isMisskeyMode) {
            follow_intent.putExtra("misskey", true)
        }
        when (i) {
            1 -> {
                follow_intent.putExtra("follow_follower", 3)
                startActivity(follow_intent)
            }
            2 -> {
                follow_intent.putExtra("follow_follower", 1)
                startActivity(follow_intent)
            }
            3 -> {
                follow_intent.putExtra("follow_follower", 2)
                startActivity(follow_intent)
            }
        }
    }

    /**
     * フォローする（同じインスタンスの場合）
     *
     * @param followUrl "follow"または"unfollow"
     * @param message   POST終わったときに表示するメッセージ + account_id のToast
     */
    private fun follow(followUrl: String, message: String) {
        val url = "https://" + Instance + "/api/v1/accounts/" + account_id.toString() + "/" + followUrl + "?access_token=" + AccessToken
        //ぱらめーたー
        val requestBody = FormBody.Builder()
                .build()
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

        //POST
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread { Toast.makeText(this@UserActivity, message + " : " + account_id.toString(), Toast.LENGTH_SHORT).show() }
            }
        })
    }

    /**
     * Misskey ふぉろー
     *
     * @param api_url api/xxxx　のこと
     * @param message Toast メッセージ
     */
    private fun postMisskeyFF(api_url: String, message: String) {
        val instance = pref_setting!!.getString("misskey_main_instance", "")
        val token = pref_setting!!.getString("misskey_main_token", "")
        val username = pref_setting!!.getString("misskey_main_username", "")
        val url = "https://$instance$api_url"
        //Request
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("userId", intent.getStringExtra("Account_ID"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
        //作成
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失敗
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@UserActivity, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@UserActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    runOnUiThread { Toast.makeText(this@UserActivity, "$message : $display_name", Toast.LENGTH_SHORT).show() }
                }
            }
        })
    }

    /**
     * リモートフォロー（違うインスタンスでのフォロー）
     *
     * @param message POST終わったときに表示するメッセージ
     */
    private fun remoteFollow(message: String) {
        val url = "https://$Instance/api/v1/follows?access_token=$AccessToken"
        //ぱらめーたー
        val requestBody = FormBody.Builder()
                .add("uri", remote!!)
                .build()
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

        //POST
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread { Toast.makeText(this@UserActivity, message + " : " + account_id.toString(), Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun followMenuCard(): LinearLayout {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //カードUIの用な感じに
        val top_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val cardView = top_linearLayout.findViewById<View>(R.id.cardview) as CardView
        val textView = top_linearLayout.findViewById<View>(R.id.cardview_textview) as TextView
        //名前とか
        textView.text = getString(R.string.follow) + "/" + getString(R.string.follower)
        textView.setPadding(10, 10, 10, 10)
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_person_black_24dp), null, null, null)

        //ここについか
        val main_LinearLayout = top_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)
        //めにゅー
        val menuList = arrayOf(getString(R.string.toot_text) + " : " + status_count.toString(), getString(R.string.follow) + " : " + follow.toString(), getString(R.string.follower) + " : " + follower.toString())
        val drawableList = arrayOf<Drawable>(getDrawable(R.drawable.ic_create_black_24dp_black), getDrawable(R.drawable.ic_done_black_24dp_2), getDrawable(R.drawable.ic_done_all_black_24dp_2))
        //forで回すか
        for (i in 0..2) {
            val menuTextView = TextView(this@UserActivity)
            //menuTextView.setCompoundDrawableTintList(getResources().getColorStateList(android.R.color.white, getTheme()));
            menuTextView.setPadding(10, 10, 10, 10)
            menuTextView.text = menuList[i]
            menuTextView.textSize = 24f
            menuTextView.setCompoundDrawablesWithIntrinsicBounds(drawableList[i], null, null, null)
            menuTextView.background = getDrawable(R.drawable.button_style)
            menuTextView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            main_LinearLayout.addView(menuTextView)
            //クリックイベント
            menuTextView.setOnClickListener { clickAcitvityMode(i + 1) }
        }
        //ダークモード
        return top_linearLayout
    }


    private fun noteCard(): LinearLayout {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //カードUIの用な感じに
        val top_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val cardView = top_linearLayout.findViewById<View>(R.id.cardview) as CardView
        val textView = top_linearLayout.findViewById<View>(R.id.cardview_textview) as TextView
        //名前とか
        textView.text = getString(R.string.note)
        textView.setPadding(10, 10, 10, 10)
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_create_black_24dp_black), null, null, null)
        //ここについか
        val main_LinearLayout = top_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)
        //note
        val noteTextView = TextView(this@UserActivity)
        val imageGetter = PicassoImageGetter(noteTextView)
        noteTextView.setPadding(10, 10, 10, 10)
        noteTextView.autoLinkMask = Linkify.WEB_URLS
        noteTextView.text = Html.fromHtml(profile, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
        main_LinearLayout.addView(noteTextView)

        return top_linearLayout
    }

    private fun create_atCard(): LinearLayout {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //カードUIの用な感じに
        val top_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val cardView = top_linearLayout.findViewById<View>(R.id.cardview) as CardView
        val textView = top_linearLayout.findViewById<View>(R.id.cardview_textview) as TextView
        //名前とか
        textView.setPadding(10, 10, 10, 10)
        textView.text = getString(R.string.create_at_date)
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_date_range_black_24dp), null, null, null)
        //ここについか
        val main_LinearLayout = top_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)
        val dateTextView = TextView(this@UserActivity)
        dateTextView.text = dateFormat(create_at)
        dateTextView.setPadding(10, 10, 10, 10)
        dateTextView.textSize = 18f

        main_LinearLayout.addView(dateTextView)

        return top_linearLayout
    }

    private fun dateFormat(dateText: String?): String {
        var dateText = dateText
        //フォーマットを規定の設定にする？
        //ここtrueにした
        if (pref_setting!!.getBoolean("pref_custom_time_format", true)) {
            //時差計算？
            if (simpleDateFormat == null && japanDateFormat == null) {
                simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                simpleDateFormat!!.timeZone = TimeZone.getTimeZone("UTC")
                //日本用フォーマット
                japanDateFormat = SimpleDateFormat(pref_setting!!.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!)
                japanDateFormat!!.timeZone = TimeZone.getTimeZone(TimeZone.getDefault().id)
                //calendar = Calendar.getInstance();
            }
            try {
                val date = simpleDateFormat!!.parse(dateText!!)
                val calendar = Calendar.getInstance()
                calendar.time = date!!
                //タイムゾーンを設定
                //calendar.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
                //calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                dateText = japanDateFormat!!.format(calendar.time)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }
        return dateText as String
    }

    private fun fieldsCard(): LinearLayout {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //カードUIの用な感じに
        val top_linearLayout = inflater.inflate(R.layout.cardview_layout, null) as LinearLayout
        val cardView = top_linearLayout.findViewById<View>(R.id.cardview) as CardView
        val textView = top_linearLayout.findViewById<View>(R.id.cardview_textview) as TextView
        //名前とか
        textView.text = getString(R.string.fields_attributes)
        //darkModeSupport.setTextViewThemeColor(textView);
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_playlist_add_black_24dp), null, null, null)
        //ここについか
        val main_LinearLayout = top_linearLayout.findViewById<LinearLayout>(R.id.cardview_lineaLayout_main)
        //forで回す
        for (i in 0 until fieldsJsonArray!!.length()) {
            //ぱーす
            try {
                var name = fieldsJsonArray!!.getJSONObject(i).getString("name")
                var value = fieldsJsonArray!!.getJSONObject(i).getString("value")
                //カスタム絵文字
                if (emojis_show) {
                    //forで回す
                    for (e in emojis_shortcode.indices) {
                        val emoji_name = emojis_shortcode[e]
                        val emoji_url = emojis_url[e]
                        val custom_emoji_src = "<img src=\'$emoji_url\'>"
                        //name
                        if (name.contains(emojis_shortcode[e])) {
                            //あったよ
                            name = name.replace(":$emoji_name:", custom_emoji_src)
                        }
                        //value
                        if (value.contains(emojis_shortcode[e])) {
                            //あったよ
                            value = value.replace(":$emoji_name:", custom_emoji_src)
                        }
                    }
                }
                //LinearLayout
                val fieldsLinearLayout = LinearLayout(this@UserActivity)
                fieldsLinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                fieldsLinearLayout.orientation = LinearLayout.VERTICAL
                fieldsLinearLayout.background = getDrawable(R.drawable.button_style)
                //TextView
                val nameTextView = TextView(this@UserActivity)
                val valueTextView = TextView(this@UserActivity)
                val created_atTextView = TextView(this@UserActivity)
                nameTextView.setPadding(10, 10, 10, 10)
                valueTextView.setPadding(10, 10, 10, 10)
                created_atTextView.setPadding(10, 10, 10, 10)
                nameTextView.textSize = 18f
                valueTextView.textSize = 18f
                valueTextView.autoLinkMask = Linkify.WEB_URLS
                valueTextView.text = Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT)
                val value_imageGetter = PicassoImageGetter(valueTextView)
                val name_imageGetter = PicassoImageGetter(nameTextView)
                valueTextView.text = Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY, value_imageGetter, null)
                nameTextView.text = Html.fromHtml(name, Html.FROM_HTML_MODE_LEGACY, name_imageGetter, null)
                //入れる
                fieldsLinearLayout.addView(nameTextView)
                //認証済みのときはverified_atの値を取得する
                if (!fieldsJsonArray!!.getJSONObject(i).isNull("verified_at")) {
                    val verified_at_string = fieldsJsonArray!!.getJSONObject(i).getString("verified_at")
                    fieldsLinearLayout.background = getDrawable(R.drawable.button_style_green)
                    //今までの方法だと使えない形なのでちょっと手を加える
                    created_atTextView.text = getString(R.string.verification_text) + " : " + timeFormat(verified_at_string.replace("+00:00", "Z"))
                    created_atTextView.textSize = 18f
                    //ちぇっくまーく
                    val doneIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done_black_24dp, null)
                    doneIcon!!.setTint(Color.parseColor("#3c8e37"))
                    created_atTextView.setCompoundDrawablesWithIntrinsicBounds(doneIcon, null, null, null)
                    //入れる
                    fieldsLinearLayout.addView(created_atTextView)
                }
                fieldsLinearLayout.addView(valueTextView)
                main_LinearLayout.addView(fieldsLinearLayout)

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        return top_linearLayout
    }

    private fun headerImageSize() {
        //背景のImageViewの大きさを変更する
        //なんかボタンのテキストが改行されて二行になると下に白あまりができるので
        //高さ調整で使う
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val config = resources.configuration
        //headerImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        headerImageView!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, point.y / 4)
        headerImageView!!.scaleType = ImageView.ScaleType.CENTER_CROP
        headerImageView!!.invalidate()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    //時差計算
    private fun timeFormat(time: String): String {
        var timeReturn = time
        val japan_timeSetting = pref_setting!!.getBoolean("pref_custom_time_format", false)
        if (japan_timeSetting) {
            //時差計算？
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            //日本用フォーマット
            val japanDateFormat = SimpleDateFormat(pref_setting!!.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!, Locale.JAPAN)
            try {
                val date = simpleDateFormat.parse(time)
                val calendar = Calendar.getInstance()
                calendar.time = date!!
                //9時間足して日本時間へ
                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting?.getString("pref_time_add", "9") ?: "9"))
                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                timeReturn = japanDateFormat.format(calendar.time)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        } else {
            timeReturn = time
        }
        return timeReturn
    }


    /**
     * DisplayName等にカスタム絵文字が含まれている場合は
     * <img src=""></img>を入れる
     */
    private fun setCustomEmoji() {
        //DisplayName/note/fieldsにカスタム絵文字が入ってるか回して確認
        for (i in emojis_shortcode.indices) {
            val emoji_name = emojis_shortcode[i]
            val emoji_url = emojis_url[i]
            val custom_emoji_src = "<img src=\'$emoji_url\'>"
            //display_name
            if (display_name!!.contains(emojis_shortcode[i])) {
                //あったよ
                display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
            }
            //note
            if (profile!!.contains(emojis_shortcode[i])) {
                //あったよ
                profile = profile!!.replace(":$emoji_name:", custom_emoji_src)
            }
            //fields
            //fields Cardのところに書く
        }
    }

    companion object {
        private val TAG = "TestImageGetter"
    }

}
