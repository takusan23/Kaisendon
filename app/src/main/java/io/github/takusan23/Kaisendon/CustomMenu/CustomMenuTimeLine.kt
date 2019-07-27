package io.github.takusan23.Kaisendon.CustomMenu


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.text.Html
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Shutdownable
import com.sys1yagi.mastodon4j.api.entity.Notification
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Statuses
import io.github.takusan23.Kaisendon.*
import io.github.takusan23.Kaisendon.APIJSONParse.CustomMenuJSONParse
import io.github.takusan23.Kaisendon.APIJSONParse.GlideSupport
import io.github.takusan23.Kaisendon.APIJSONParse.MastodonTLAPIJSONParse
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.TLQuickSettingSnackber
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.DesktopTL.DesktopFragment
import okhttp3.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


/**
 * A simple [Fragment] subclass.
 */
class CustomMenuTimeLine : Fragment() {

    lateinit var pref_setting: SharedPreferences

    private var parent_linearlayout: LinearLayout? = null

    private var misskey: String? = null
    private var instance: String? = null
    private var access_token: String? = null
    private var json_data: String? = null
    private var dialog: String? = null
    private var image_load: String? = null
    private val dark_mode: String? = null
    private val setting: String? = null
    private var streaming: String? = null
    private var name: String? = null
    private var subtitle: String? = null
    private var image_url: String? = null
    private var background_transparency: String? = null
    private var quick_profile: String? = null
    private var toot_counter: String? = null
    private var custom_emoji: String? = null
    private var gif: String? = null
    private var font: String? = null
    private var one_hand: String? = null

    private var background_screen_fit: Boolean? = null
    private val dark_theme = false

    private var max_id: String? = null

    private var linearLayout: LinearLayout? = null
    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var adapter: HomeTimeLineAdapter? = null
    private var imageView: ImageView? = null
    private var avater_imageView: ImageView? = null
    private var header_imageView: ImageView? = null
    private var user_account_textView: TextView? = null
    private var user_id_textView: TextView? = null

    private var scroll = false
    private val streaming_mode: Boolean = false

    private var position: Int = 0
    private var y: Int = 0
    private val shutdownable: Shutdownable? = null

    //通知フィルター
    private var fav_filter = true
    private var bt_filter = true
    private var mention_filter = true
    private var follow_filter = true
    private var vote_filter = true

    private val client: MastodonClient? = null

    //トゥートカウンター
    private var akeome_count: Int = 0
    private var count_text: String? = null
    private var countTextView: TextView? = null

    private val helper: CustomMenuSQLiteHelper? = null
    private val db: SQLiteDatabase? = null

    //最初だけ通知しない
    private var network_count = 0


    //WebSocket
    private var webSocketClient: WebSocketClient? = null
    private var notification_WebSocketClient: WebSocketClient? = null

    //名前
    private var display_name: String? = null
    private var username: String? = null
    private var follow: String? = null
    private var follower: String? = null
    private val statuses: String? = null
    private var note: String? = null
    private var account_JsonObject: JSONObject? = null
    private val emojis_show = false
    private var untilId: String? = null

    //RecyclerView
    private var recyclerViewList: ArrayList<ArrayList<*>>? = null
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null

    //通知
    private var vibrator: Vibrator? = null
    //時間指定投稿待ち一覧モード
    private var isScheduled_statuses = false
    private var customMenuRecyclerViewAdapter: CustomMenuRecyclerViewAdapter? = null
    //フォロー推奨ユーザー表示モード
    private var isFollowSuggestions = false

    //時間
    private var simpleDateFormat: SimpleDateFormat? = null
    private var japanDateFormat: SimpleDateFormat? = null
    private var calendar: Calendar? = null
    private val desktop_url = ""

    private var no_fav_icon: String? = ""
    private var yes_fav_icon: String? = ""

    private var darkModeSupport: DarkModeSupport? = null
    //isDesktopMode
    private var isDesktopMode = false
    //TTS
    private var tts: TextToSpeech? = null
    //クイック設定
    private var tlQuickSettingSnackber: TLQuickSettingSnackber? = null

    //Streaming APIに接続できないときにインスタンス情報APIからurlを取ってくる
    //あくまで既定でつながらなった場合のみ
    private var instance_api_streaming_api_link = ""

    //読み取り専用モード？
    private var isReadOnly = "false"

/*
    //ストリーミングのときに同じ内容が増えないようにするために
    //追加前とこの変数と比較して、内容が増えてればnotifyItemInserted(0)を呼ぶことにする
    private var tootListCount = 0
*/

    /**
     * 変数 : url
     * これCustomMenuTimeLine単体だと動くけどDesktopModeだとおかしくなるのでこのメゾット使って
     */
    private//最終的なURL(static使いまくったらDesktopMode実装で困った（）
    val desktopModeURL: String
        get() = "https://" + arguments?.getString("instance") + arguments?.getString("content")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //ダークモードを有効にするか
        return inflater.inflate(R.layout.fragment_custom_menu_time_line, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        parent_linearlayout = view.findViewById(R.id.custom_menu_parent_linearlayout)
        linearLayout = view.findViewById(R.id.custom_menu_fragment_linearlayout)
        recyclerView = view.findViewById(R.id.custom_menu_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.custom_menu_swipe_refresh)
        imageView = view.findViewById(R.id.custom_tl_background_imageview)

        //データ受け取り
        misskey = arguments!!.getString("misskey")
        isMisskeyMode = java.lang.Boolean.valueOf(misskey)
        url = arguments!!.getString("content")
        instance = arguments!!.getString("instance")
        access_token = arguments!!.getString("access_token")
        json_data = arguments!!.getString("json")
        streaming = arguments!!.getString("streaming")
        subtitle = arguments!!.getString("subtitle")
        dialog = arguments!!.getString("dialog")
        image_load = arguments!!.getString("image_load")
        image_url = arguments!!.getString("image_url")
        background_transparency = arguments!!.getString("background_transparency")
        background_screen_fit = java.lang.Boolean.valueOf(arguments!!.getString("background_screen_fit"))
        quick_profile = arguments!!.getString("quick_profile")
        toot_counter = arguments!!.getString("toot_counter")
        custom_emoji = arguments!!.getString("custom_emoji")
        gif = arguments!!.getString("gif")
        font = arguments!!.getString("font")
        misskey_username = arguments!!.getString("misskey_username")
        one_hand = arguments!!.getString("one_hand")
        name = arguments!!.getString("name")
        no_fav_icon = arguments!!.getString("no_fav_icon")
        yes_fav_icon = arguments!!.getString("yes_fav_icon")
        isReadOnly = arguments?.getString("read_only") ?: "false"

        // onOptionsItemSelectedが呼ばれない対策
        setHasOptionsMenu(true)

        //Navication Drawer
        if (activity != null) {
            val navigationView = activity!!.findViewById<NavigationView>(R.id.nav_view)
            if (navigationView != null) {
                //どろわーのイメージとか文字とか
                val navHeaderView = navigationView.getHeaderView(0)
                avater_imageView = navHeaderView.findViewById(R.id.icon_image)
                header_imageView = navHeaderView.findViewById(R.id.drawer_header)
                user_account_textView = navHeaderView.findViewById(R.id.drawer_account)
                user_id_textView = navHeaderView.findViewById(R.id.drawer_id)
                avater_imageView!!.imageTintList = null
                header_imageView!!.imageTintList = null
            }
        }
        //インスタンス、アクセストークン変更
        //Misskeyは設定しないように
        //デスクトップモード時も設定しないように
        //読み取り専用モード時も設定しないように
        if (activity!!.supportFragmentManager.findFragmentById(R.id.container_container) is DesktopFragment || isReadOnly.toBoolean()) {
            //タイトル
            isDesktopMode = true
            (context as AppCompatActivity).title = getString(R.string.desktop_mode)
        } else {
            //タイトル
            (context as AppCompatActivity).title = setName(url!!, arguments!!.getString("name"))
            if (!isMisskeyMode) {
                val editor = pref_setting!!.edit()
                editor.putString("main_instance", instance)
                editor.putString("main_token", access_token)
                editor.apply()
            } else {
                val editor = pref_setting!!.edit()
                editor.putString("misskey_main_instance", instance)
                editor.putString("misskey_main_token", access_token)
                editor.putString("misskey_main_username", misskey_username)
                editor.apply()
            }
        }

        //トゥートカウンター
        countTextView = TextView(context)

        //フォント設定。一回設定したら使い回すようにする
        //staticってこれで使い方あってんの
        val file = File(font!!)
        if (file.exists()) {
            font_Typeface = Typeface.createFromFile(font)
        } else {
            font_Typeface = TextView(context).typeface
        }

        //透明度設定は背景画像利用時のみ利用できるようにする
        if (image_url!!.length == 0) {
            background_transparency = ""
        }

        //OLED
        //ダークモード処理
        val conf = resources.configuration
        var currecntNightMode = conf.uiMode and Configuration.UI_MODE_NIGHT_MASK
        darkModeSupport = DarkModeSupport(context!!)
        currecntNightMode = darkModeSupport!!.setIsDarkModeSelf(currecntNightMode)
        when (currecntNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> linearLayout!!.setBackgroundColor(Color.parseColor("#" + background_transparency + "ffffff"))
            Configuration.UI_MODE_NIGHT_YES -> linearLayout!!.setBackgroundColor(Color.parseColor("#" + background_transparency + "000000"))
        }

        //最終的なURL
        url = "https://$instance$url"

        val toot_list = ArrayList<ListItem>()
        adapter = HomeTimeLineAdapter(context!!, R.layout.timeline_item, toot_list)

        //背景画像セット
        if (image_url!!.length != 0) {
            //URI画像を入れる
            Glide.with(context!!).load(image_url).into(imageView!!)
            //画面に合わせる設定
            if (background_screen_fit!!) {
                imageView!!.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }

        //片手モード
        if (java.lang.Boolean.valueOf(one_hand)) {
            one_hand_mode()
        }

        //ToolBerをクリックしたら一番上に移動するようにする
        if (pref_setting!!.getBoolean("pref_listview_top", true)) {
            try {
                (activity as Home).toolBer.setOnClickListener {
                    //これ一番上に移動するやつ
                    recyclerView?.smoothScrollToPosition(0)
                }
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
        }

        //TLQuickSettings
        if (activity is Home) {
            tlQuickSettingSnackber = (activity as Home).tlQuickSettingSnackber
        }

        //トゥートカウンター
        if (java.lang.Boolean.valueOf(toot_counter)) {
            setTootCounterLayout()
        }

        //予約投稿（時間指定投稿）待ち一覧モードかを判断する
        if (url!!.contains("/api/v1/scheduled_statuses")) {
            isScheduled_statuses = true
        }
        //フォロー推奨ユーザー読み込みモード
        if (url!!.contains("/api/v1/suggestions")) {
            isFollowSuggestions = true
        }


        recyclerViewList = ArrayList()
        //ここから下三行必須
        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = mLayoutManager
        customMenuRecyclerViewAdapter = CustomMenuRecyclerViewAdapter(recyclerViewList!!)
        recyclerView!!.adapter = customMenuRecyclerViewAdapter
        recyclerViewLayoutManager = recyclerView!!.layoutManager

        addNavigationOpen()

        //TL読み込み
        //APIがTL取得のみに
        //TL と Favourite List
        if (!isScheduled_statuses && !isFollowSuggestions) {
            //Misskey
            if (isMisskeyMode) {
                loadMisskeyAccountName()
                //くるくる
                SnackberProgress.showProgressSnackber(view, context!!, getString(R.string.loading) + "\n" + arguments!!.getString("content"))
                //通知以外
                if (!url!!.contains("notifications")) {
                    //普通にAPI叩く
                    loadMisskeyTimeline(null, false)
                } else {
                    //通知レイアウト読み込み
                    notificationLayout()
                    loadMisskeyTimeline(null, true)
                }

                //引っ張って更新
                swipeRefreshLayout!!.setOnRefreshListener {
                    adapter!!.clear()
                    recyclerViewList!!.clear()
                    //トゥートカウンター
                    countTextView!!.text = ""
                    akeome_count = 0
                    SnackberProgress.showProgressSnackber(view, context!!, getString(R.string.loading) + "\n" + arguments!!.getString("content"))
                    //通知以外
                    if (!url!!.contains("notifications")) {
                        //普通にAPI叩く
                        loadMisskeyTimeline(null, false)
                    } else {
                        loadMisskeyTimeline(null, true)
                    }
                }


                //最後までスクロール
                recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        //Favのときはしない
                        if (recyclerViewLayoutManager != null) {
                            val firstVisibleItem = (recyclerViewLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            val visibleItemCount = (recyclerViewLayoutManager as LinearLayoutManager).childCount
                            val totalItemCount = (recyclerViewLayoutManager as LinearLayoutManager).itemCount
                            //最後までスクロールしたときの処理
                            if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                                position = (recyclerViewLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                                y = recyclerView.getChildAt(0).top
                                if (recyclerViewList!!.size >= 20) {
                                    SnackberProgress.showProgressSnackber(view, context!!, getString(R.string.loading) + "\n" + arguments!!.getString("content"))
                                    scroll = true
                                    //通知以外
                                    if (!url!!.contains("notifications")) {
                                        //普通にAPI叩く
                                        loadMisskeyTimeline(this@CustomMenuTimeLine.untilId, false)
                                    } else {
                                        loadMisskeyTimeline(null, true)
                                    }
                                }
                            }

                        }
                    }
                })
            } else {
                //名前表示
                //サブタイトル更新

                //読み取り専用あんどAppBarを下にしたときは利用しない
                if (!isReadOnly()) {
                    loadAccountName()
                }

                //ストリーミングAPI。本来は無効のときチェックを付けてるけど保存時に反転してるのでおっけ
                //無効・有効
                SnackberProgress.showProgressSnackber(view, context!!, getString(R.string.loading) + "\n" + arguments!!.getString("content"))
                if (java.lang.Boolean.valueOf(streaming)) {
                    //有効
                    //引っ張って更新無効
                    swipeRefreshLayout!!.isEnabled = false
                    if (!url!!.contains("/api/v1/notifications")) {
                        loadTimeline("")
                        //ストリーミング
                        useStreamingAPI()
                    } else {
                        notificationLayout()
                        //普通にAPI叩く
                        loadNotification("")
                        //ストリーミング
                        useStreamingAPI()
                    }
                } else {
                    //無効
                    //引っ張って更新有効
                    swipeRefreshLayout!!.isEnabled = true
                    //通知以外
                    if (url!!.contains("/api/v1/notifications")) {
                        //通知用レイアウト呼ぶ
                        notificationLayout()
                        //普通にAPI叩く
                        loadNotification("")
                    } else {
                        //通常読み込み
                        loadTimeline("")
                    }
                }
                //引っ張って更新
                swipeRefreshLayout!!.setOnRefreshListener {

                    //customMenuRecyclerViewAdapter!!.clear()
                    recyclerViewList!!.clear()
                    customMenuRecyclerViewAdapter!!.notifyDataSetChanged()

                    //位置リセット
                    position = 0
                    y = 0

                    //トゥートカウンター
                    countTextView!!.text = ""
                    akeome_count = 0
                    SnackberProgress.showProgressSnackber(view, context!!, getString(R.string.loading) + "\n" + arguments!!.getString("content"))
                    //通知以外
                    if (!url!!.contains("/api/v1/notifications")) {
                        //普通にAPI叩く
                        loadTimeline("")
                    } else {
                        loadNotification("")
                    }
                }

                //最後までスクロール
                recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!url!!.contains("/api/v1/favourites")) {
                            val firstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                            val visibleItemCount = (recyclerView.layoutManager as LinearLayoutManager).childCount
                            val totalItemCount = (recyclerView.layoutManager as LinearLayoutManager).itemCount
                            //最後までスクロールしたときの処理
                            if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                                position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                                y = recyclerView.getChildAt(0).top
                                if (recyclerViewList!!.size >= 20) {
                                    SnackberProgress.showProgressSnackber(view, context!!, getString(R.string.loading) + "\n" + arguments!!.getString("content"))
                                    scroll = true
                                    //通知以外
                                    if (!this@CustomMenuTimeLine.arguments!!.getString("content")!!.contains("/api/v1/notifications")) {
                                        //普通にAPI叩く
                                        loadTimeline(max_id)
                                    } else {
                                        loadNotification(max_id!!)
                                    }
                                }
                            }
                        }
                    }
                })

                //通知
                if (pref_setting!!.getBoolean("pref_notification_toast", true)) {
                    setStreamingNotification()
                }
            }
        } else if (isScheduled_statuses) {
            //予約リスト
            //引っ張って更新無効
            swipeRefreshLayout!!.isEnabled = false
            //アカウント情報
            //読み取り専用利用しない
            if (!isReadOnly()) {
                loadAccountName()
            }
            //時間指定待ち一覧を読み込む
            loadScheduled_statuses(view)
        } else if (isFollowSuggestions) {
            //フォロー一覧
            //引っ張って更新無効
            swipeRefreshLayout!!.isEnabled = false
            //アカウント情報
            //読み取り専用は利用しない
            if (!isReadOnly()) {
                loadAccountName()
            }
            //フォロー推奨ユーザーを読み込む
            loadFollowSuggestions(view)
        }
        //ネットワーク変更を検知する
        setNetworkChangeCallback()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.bottom_app_bar_menu_scroll) {
            // BottomAppBarのときはTextView無いので指定のボタンを押したときに動くようにしてます
            recyclerView!!.smoothScrollToPosition(0)
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * タイムラインを読み込む
     * 通知はこれでは読み込めない
     *
     * @param max_id_id 追加読み込み。無いときは""でも
     */
    private fun loadTimeline(max_id_id: String?) {
        //パラメータを設定
        //最終的なURL(static使いまくったらDesktopMode実装で困った（）
        //ハッシュタグはそのままURLが利用できないので修正
        if (url?.contains("/api/v1/timelines/tag/") == true) {
            if (url == "?local=true") {
                url = "https://" + instance + "/api/v1/timelines/tag/" + arguments?.getString("name") + "?local=true"
            } else {
                url = " https://" + instance + "/api/v1/timelines/tag/" + arguments?.getString("name")
            }
        } else {
            //ハッシュタグ以外はここから取れる
            url = desktopModeURL
        }
        val builder = HttpUrl.parse(url)?.newBuilder()
        builder?.addQueryParameter("limit", "40")
        //読み取り専用？
        if (!isReadOnly()) {
            builder?.addQueryParameter("access_token", arguments?.getString("access_token"))
        }
        if (max_id_id != null) {
            if (max_id_id.length != 0) {
                builder?.addQueryParameter("max_id", max_id_id)
            }
        }
        val max_id_final_url = builder?.build().toString()
        //作成
        val request = Request.Builder()
                .url(max_id_final_url)
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //成功時
                if (response.isSuccessful) {
                    val response_string = response.body()?.string()
                    var jsonArray: JSONArray? = null
                    try {
                        jsonArray = JSONArray(response_string)
                        for (i in 0 until jsonArray.length()) {
                            val toot_jsonObject = jsonArray.getJSONObject(i)
                            if (activity != null && isAdded) {
                                //配列を作成
                                val Item = ArrayList<String>()
                                //メモとか通知とかに
                                Item.add("CustomMenu Local")
                                //内容
                                Item.add(url ?: "")
                                //ユーザー名
                                Item.add("")
                                //JSONObject
                                Item.add(toot_jsonObject.toString())
                                //ぶーすとした？
                                Item.add("false")
                                //ふぁぼした？
                                Item.add("false")
                                //Mastodon / Misskey
                                Item.add("Mastodon")
                                //Insatnce/AccessToken
                                Item.add(instance ?: "")
                                Item.add(access_token ?: "")
                                //設定ファイルJSON
                                Item.add(json_data ?: "")
                                //画像表示、こんてんとわーにんぐ
                                Item.add("false")
                                Item.add("false")

                                //ListItem listItem = new ListItem(Item);
                                recyclerViewList?.add(Item)
                            }
                        }
                        //Adapter更新
                        activity?.runOnUiThread {
                            if (recyclerViewLayoutManager != null) {
                                (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                            }
                            //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                            //recyclerView?.adapter = customMenuRecyclerViewAdapter
                            customMenuRecyclerViewAdapter?.notifyDataSetChanged()
                            SnackberProgress.closeProgressSnackber()
                            scroll = false
                        }
                        //最後のIDを更新する
                        val last_toot = jsonArray.getJSONObject(39)
                        max_id = last_toot.getString("id")
                        if (swipeRefreshLayout?.isRefreshing == true) {
                            activity?.runOnUiThread { swipeRefreshLayout?.isRefreshing = false }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    //失敗時
                    activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                }
            }
        })
    }

    /**
     * Misskey ユーザー情報取得
     */
    private fun loadMisskeyAccountName() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("username", misskey_username)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
        //作成
        val request = Request.Builder()
                .url("https://$instance/api/users/show")
                .post(requestBody)
                .build()
        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失敗時
                activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()?.string()
                if (!response.isSuccessful) {
                    //失敗時
                    activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        val jsonObject = JSONObject(response_string)
                        var name = jsonObject.getString("name")
                        val title_name = jsonObject.getString("name")
                        val username = jsonObject.getString("username")
                        account_id = jsonObject.getString("id")
                        val avatarUrl = jsonObject.getString("avatarUrl")
                        val avatarUrlnotGif = jsonObject.getString("avatarUrl")
                        val bannerUrl = jsonObject.getString("bannerUrl")
                        if (pref_setting?.getBoolean("pref_custom_emoji", true) != false || java.lang.Boolean.valueOf(custom_emoji)) {
                            val emoji = jsonObject.getJSONArray("emojis")
                            for (e in 0 until emoji.length()) {
                                val emoji_jsonObject = emoji.getJSONObject(e)
                                val emoji_name = emoji_jsonObject.getString("name")
                                val emoji_url = emoji_jsonObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                name = name.replace(":$emoji_name:", custom_emoji_src)
                            }
                        }
                        val finalName = name

                        if (activity != null) {
                            activity?.runOnUiThread {
                                //ドロワーに反映
                                setDrawerImageText(avatarUrl, bannerUrl, finalName, "@$username@$instance")
                                if (context != null && (context as AppCompatActivity).supportActionBar != null) {
                                    //サブタイトル更新
                                    if (subtitle?.length ?: 0 >= 1) {
                                        (context as AppCompatActivity).supportActionBar?.subtitle = subtitle
                                    } else {
                                        (context as AppCompatActivity).supportActionBar?.subtitle = "$title_name( @$username / $instance )"
                                    }
                                }
                            }
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })

    }

    //DisplayName + ID　が出るようにする
    private fun loadAccountName() {
        //パラメータを設定
        val builder = HttpUrl.parse("https://$instance/api/v1/accounts/verify_credentials")?.newBuilder()
        builder?.addQueryParameter("access_token", access_token)
        val url = builder?.build().toString()
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失敗時
                activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val jsonObject = JSONObject(response.body()?.string())

                        val name = jsonObject.getString("display_name")
                        val id = jsonObject.getString("acct")
                        username = jsonObject.getString("username")
                        display_name = jsonObject.getString("display_name")
                        follow = jsonObject.getString("following_count")
                        follower = jsonObject.getString("followers_count")
                        note = jsonObject.getString("note")
                        var avatar = jsonObject.getString("avatar")
                        val avatarNotGif = jsonObject.getString("avatar_static")
                        var header = jsonObject.getString("header")
                        val headerNotGif = jsonObject.getString("header_static")
                        account_JsonObject = jsonObject
                        //カスタム絵文字
                        if (java.lang.Boolean.valueOf(custom_emoji) || pref_setting?.getBoolean("pref_custom_emoji", true) != false) {
                            val emojis = jsonObject.getJSONArray("emojis")
                            for (i in 0 until emojis.length()) {
                                val emojiObject = emojis.getJSONObject(i)
                                val emoji_name = emojiObject.getString("shortcode")
                                val emoji_url = emojiObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                //display_name
                                if (display_name?.contains(emoji_name) == true) {
                                    //あったよ
                                    display_name = display_name?.replace(":$emoji_name:", custom_emoji_src)
                                }
                            }
                        }
                        if (activity != null) {
                            activity?.runOnUiThread {
                                //ドロワー
                                if (pref_setting?.getBoolean("pref_avater_gif", true) != false) {
                                    avatar = avatarNotGif
                                    header = headerNotGif
                                }
                                setDrawerImageText(avatar, header, display_name, "@$username@$instance")
                                //サブタイトル更新
                                if (isDesktopMode) {
                                    (context as AppCompatActivity).supportActionBar?.subtitle = ""
                                } else {
                                    if (context != null && (context as AppCompatActivity).supportActionBar != null) {
                                        if (subtitle?.length ?: 0 >= 1) {
                                            (context as AppCompatActivity).supportActionBar?.subtitle = subtitle
                                        } else {
                                            (context as AppCompatActivity).supportActionBar?.subtitle = "$name( @$id / $instance )"
                                        }
                                    }
                                }
                            }
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    //失敗時
                    if (isAdded) {
                        activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        })
    }

    /**
     * ストリーミングAPI
     */
    private fun useStreamingAPI() {
        //接続先設定
        var link = ""
        //通知
        var notification = false
        //DM
        var direct = false

        /*
         * もし/api/v1/instanceからurls.streaming_apiが違ったときに動く
         * isEmpty()でfalseのときは別リンクが設定されてる
         * */
        if (instance_api_streaming_api_link.isEmpty()) {
            //既定
            when (arguments?.getString("content")) {
                "/api/v1/timelines/home" -> link = "wss://$instance/api/v1/streaming/?stream=user"
                "/api/v1/notifications" -> {
                    notification = true
                    link = "wss://$instance/api/v1/streaming/?stream=user:notification"
                }
                "/api/v1/timelines/public?local=true" -> link = "wss://$instance/api/v1/streaming/?stream=public:local"
                "/api/v1/timelines/public" -> link = "wss://$instance/api/v1/streaming/?stream=public"
                "/api/v1/timelines/direct" -> {
                    direct = true
                    link = "wss://$instance/api/v1/streaming/?stream=direct&"
                }
                "/api/v1/timelines/tag/" -> link = "wss://" + instance + "/api/v1/streaming/hashtag/?hashtag=" + arguments!!.getString("name")
                "/api/v1/timelines/tag/?local=true" -> link = "wss://" + instance + "/api/v1/streaming/hashtag/local?hashtag=" + arguments!!.getString("name")
            }
            //読み取り専用でもローカルタイムラインなら接続可能？
            //HttpUrl.Builderはwssスキームがつかえないんだって。しらんかった
            if (!isReadOnly()) {
                link += "&access_token$access_token"
            }
        } else {
            //特別リンクが設定されてる時
            when (arguments?.getString("content")) {
                "/api/v1/timelines/home" -> link = "$instance_api_streaming_api_link/api/v1/streaming/?stream=user"
                "/api/v1/notifications" -> {
                    notification = true
                    link = "$instance_api_streaming_api_link/api/v1/streaming/?stream=user:notification"
                }
                "/api/v1/timelines/public?local=true" -> link = "$instance_api_streaming_api_link/api/v1/streaming/?stream=public:local"
                "/api/v1/timelines/public" -> link = "$instance_api_streaming_api_link/api/v1/streaming/?stream=public"
                "/api/v1/timelines/direct" -> {
                    direct = true
                    link = "$instance_api_streaming_api_link/api/v1/streaming/?stream=direct"
                }
                "/api/v1/timelines/tag/" -> link = "$instance_api_streaming_api_link/api/v1/streaming/hashtag/?hashtag=" + arguments!!.getString("name")
                "/api/v1/timelines/tag/?local=true" -> link = "$instance_api_streaming_api_link/api/v1/streaming/hashtag/local?hashtag=" + arguments!!.getString("name")
            }

            //読み取り専用でもローカルタイムラインなら接続可能？
            if (!isReadOnly()) {
                link += "&access_token$access_token"
            }
        }
        if (Build.PRODUCT.contains("sdk")) {
            // エミュレータの場合はIPv6を無効    ----1
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false")
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true")
        }
        try {
            val uri = URI(link)

            //WebSocket
            val finalNotification = notification
            val finalDirect = direct
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake) {
                    println("おーぷん")
                }

                override fun onMessage(message: String) {
                    //JSONParse
                    try {
                        if (!finalNotification && !finalDirect) {
                            val jsonObject = JSONObject(message)
                            //一回文字列として取得してから再度JSONObjectにする
                            val payload = jsonObject.getString("payload")
                            //updateのイベントだけ受け付ける
                            //長年悩んだトゥートが増えるバグは新しいトゥート以外の内容でもRecyclerViewの０番目を更新するやつ呼んでたのが原因
                            val event = jsonObject.getString("event")
                            if (event.contains("update")) {
                                val toot_jsonObject = JSONObject(payload)
                                //これでストリーミング有効・無効でもJSONパースになるので楽になる（？）
                                timelineJSONParse(toot_jsonObject, true)
                            }

                        } else if (finalNotification) {
                            val jsonObject = JSONObject(message)
                            val payload = jsonObject.getString("payload")
                            val toot_text_jsonObject = JSONObject(payload)
                            val toot_text_account = toot_text_jsonObject.getJSONObject("account")
                            //Type!!!!!!!!
                            val type = toot_text_jsonObject.getString("type")
                            //振り分け
                            if (fav_filter) {
                                if (type.contains("favourite")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true)
                                }
                            }
                            if (bt_filter) {
                                if (type.contains("reblog")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true)
                                }
                            }
                            if (mention_filter) {
                                if (type.contains("mention")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true)
                                }
                            }
                            if (follow_filter) {
                                if (type.contains("follow")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true)
                                }
                            }
                            if (vote_filter) {
                                if (type.contains("poll")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, true)
                                }
                            }
                        } else if (finalDirect) {
                            //DM
                            val jsonObject = JSONObject(message)
                            val payload = jsonObject.getString("payload")
                            val toot_text_jsonObject = JSONObject(payload)
                            streamingAPIDirect(toot_text_jsonObject)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

                override fun onClose(code: Int, reason: String, remote: Boolean) {

                }

                override fun onError(ex: Exception) {
                    //失敗時
                    activity?.runOnUiThread {
                        if (isAdded) {
                            Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
                        }
                    }


                    //404エラーは再接続？
                    //何回もAPI叩かれると困る
                    if (instance_api_streaming_api_link.isEmpty()) {
                        getInstanceUrlsStreamingAPI()
                        useStreamingAPI()
                    }

                }
            }
            //接続
            webSocketClient?.connect()

        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    /*
    * /api/v1/instanceを叩いてurls.streaming_apiを取得すりゅ
    * */
    fun getInstanceUrlsStreamingAPI() {
        //APIを叩く
        val url = "https://$instance/api/v1/instance"
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        //GETリクエスト
        val okHttpClient = OkHttpClient()
        val response = okHttpClient.newCall(request).execute()
        //JSONパース
        if (response.isSuccessful) {
            val jsonObject = JSONObject(response.body()?.string())
            instance_api_streaming_api_link = jsonObject.getJSONObject("urls").getString("streaming_api")
        } else {
            activity?.runOnUiThread {
                Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 通知
     *
     * @param max_id_id 追加読み込み。無いときは""でも
     */
    private fun loadNotification(max_id_id: String) {
        //パラメータを設定
        val builder = HttpUrl.parse(desktopModeURL)?.newBuilder()
        builder?.addQueryParameter("limit", "40")
        builder?.addQueryParameter("access_token", access_token)
        if (max_id_id.length != 0) {
            builder?.addQueryParameter("max_id", max_id_id)
        }

        val max_id_final_url = builder?.build().toString()

        //作成
        val request = Request.Builder()
                .url(max_id_final_url)
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (context != null) {
                    activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val response_string = response.body()?.string()
                    var jsonArray: JSONArray? = null
                    try {
                        jsonArray = JSONArray(response_string)
                        for (i in 0 until jsonArray.length()) {
                            val toot_text_jsonObject = jsonArray.getJSONObject(i)
                            val toot_text_account = toot_text_jsonObject.getJSONObject("account")
                            //Type!!!!!!!!
                            val type = toot_text_jsonObject.getString("type")
                            //振り分け
                            if (fav_filter) {
                                if (type.contains("favourite")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false)
                                }
                            }
                            if (bt_filter) {
                                if (type.contains("reblog")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false)
                                }
                            }
                            if (mention_filter) {
                                if (type.contains("mention")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false)
                                }
                            }
                            if (follow_filter) {
                                if (type.contains("follow")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false)
                                }
                            }
                            if (vote_filter) {
                                if (type.contains("poll")) {
                                    notificationJSONPase(toot_text_account, toot_text_jsonObject, type, false)
                                }
                            }
                        }
                        //最後のIDを更新する
                        val last_toot_text = jsonArray.getJSONObject(29)
                        max_id = last_toot_text.getString("id")
                        //わんちゃんJSONすべてがフィルターにかかって０件の場合があるのでそのときは２０個以上になるまで叩き続ける
/*
                        if (adapter?.count < 20) {
                            //loadNotification(max_id);
                        }
*/

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    //失敗時
                    if (context != null) {
                        activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        })
    }

    /**
     * 通知メニューレイアウト
     */
    private fun notificationLayout() {
        if (context != null) {
            //追加
            //新しいLinearlayout
            val notificationLinearLayout = LinearLayout(context)
            notificationLinearLayout.orientation = LinearLayout.HORIZONTAL
            notificationLinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            //配列で勘弁して
            val icon = arrayOf<Drawable>(context!!.getDrawable(R.drawable.ic_star_black_24dp)!!, context!!.getDrawable(R.drawable.ic_repeat_black_24dp)!!, context!!.getDrawable(R.drawable.ic_announcement_black_24dp)!!, context!!.getDrawable(R.drawable.ic_person_add_black_24dp)!!, context!!.getDrawable(R.drawable.ic_baseline_how_to_vote_24px)!!)
            val tag = arrayOf("fav_filter", "bt_filter", "mention_filter", "follow_filter", "vote_filter")
            //背景
            var background = "ffffff"
            if (java.lang.Boolean.valueOf(dark_mode)) {
                background = "000000"
            }

            for (i in 0..4) {
                val sw = Switch(context)
                darkModeSupport?.setSwitchThemeColor(sw)
                sw.setCompoundDrawablesWithIntrinsicBounds(icon[i], null, null, null)
                sw.tag = tag[i]
                sw.isChecked = true
                notificationLinearLayout.addView(sw)
                //切り替え
                sw.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        notificationFilterBoolean(tag[i], true)
                    } else {
                        notificationFilterBoolean(tag[i], false)
                    }
                    //通知更新
                    recyclerViewList?.clear()
                    if (context != null) {
                        SnackberProgress.showProgressSnackber(sw, context!!, getString(R.string.loading) + "\n" + arguments?.getString("content"))
                    }
                    if (java.lang.Boolean.valueOf(misskey)) {
                        loadMisskeyTimeline(null, true)
                    } else {
                        loadNotification("")
                    }
                }
            }
            //ついか
            linearLayout?.addView(notificationLinearLayout, 0)
        }
    }

    /**
     * 通知で使う
     *
     * @param type fav,bt,mention,followのいずれか
     */
    private fun notificationFilterBoolean(type: String, isChecked: Boolean) {
        if (isChecked) {
            when (type) {
                "fav_filter" -> fav_filter = true
                "bt_filter" -> bt_filter = true
                "mention_filter" -> mention_filter = true
                "follow_filter" -> follow_filter = true
                "vote_filter" -> vote_filter = true
            }
        } else {
            when (type) {
                "fav_filter" -> fav_filter = false
                "bt_filter" -> bt_filter = false
                "mention_filter" -> mention_filter = false
                "follow_filter" -> follow_filter = false
                "vote_filter" -> vote_filter = false
            }
        }
    }

    /**
     * ホーム、ローカル、連合のJSONParse
     *
     * @param streaming ストリーミングAPIのときはtrueにしてね（一番上に追加するため）
     */
    private fun timelineJSONParse(toot_jsonObject: JSONObject, streaming: Boolean) {
        val toot_account: JSONObject? = null
        if (activity != null && isAdded) {

            //配列を作成
            val Item = ArrayList<String>()
            //メモとか通知とかに
            Item.add("CustomMenu")
            //内容
            Item.add(url ?: "")
            //ユーザー名
            Item.add("")
            //時間、クライアント名等
            Item.add(toot_jsonObject.toString())
            //ぶーすとした？
            Item.add("false")
            //ふぁぼした？
            Item.add("false")
            //Mastodon / Misskey
            Item.add("Mastodon")
            //Insatnce/AccessToken
            Item.add(instance ?: "")
            Item.add(access_token ?: "")
            //設定ファイルJSON
            Item.add(json_data ?: "")
            //画像表示、こんてんとわーにんぐ
            Item.add("false")
            Item.add("false")
            //数字控える
            if (streaming) {
                recyclerViewList?.add(0, Item)
            } else {
                recyclerViewList?.add(Item)
            }
            activity?.runOnUiThread {
                //カウンター
                if (java.lang.Boolean.valueOf(toot_counter)) {
                    if (count_text != null) {
                        //含んでいるか
                        try {
                            if (toot_jsonObject.getString("content").contains(count_text!!)) {
                                val count_template = " : "
                                akeome_count++
                                countTextView?.text = count_text + count_template + akeome_count.toString()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }

                if (recyclerViewLayoutManager as LinearLayoutManager? != null) {
                    // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                    val pos = (recyclerViewLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    var top = 0
                    if ((recyclerViewLayoutManager as LinearLayoutManager).childCount > 0) {
                        top = (recyclerViewLayoutManager as LinearLayoutManager).getChildAt(0)!!.top
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    if (streaming) {
                        //一番上にアイテムが追加されたことを通知する？
                        //notifyDataSetChanged()と違って追加時にアニメーションされる
                        customMenuRecyclerViewAdapter?.notifyItemInserted(0)
                    } else {
                        customMenuRecyclerViewAdapter?.notifyDataSetChanged()
                    }
                    //一番上なら追いかける
                    if (pos == 0) {
                        recyclerView?.post {
                            //scrollToPosition()に置き換えた。アニメーションされるようになった
                            recyclerView?.scrollToPosition(0)
                        }
                    } else {
                        (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(pos + 1, top)
                    }
                }
/*
                */
/*TTS*//*

                if (tlQuickSettingSnackber != null && tlQuickSettingSnackber?.timelineTTS == true) {
                    //インスタンス生成
                    if (tts == null) {
                        tts = TextToSpeech(context, TextToSpeech.OnInitListener { i ->
                            //初期化
                            if (i == TextToSpeech.SUCCESS) {
                                Toast.makeText(context, getString(R.string.text_to_speech_preparation), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        tts?.setSpeechRate(java.lang.Float.valueOf(pref_setting?.getString("pref_speech_rate", "1.0f")!!))
                        val setting = CustomMenuJSONParse(json_data ?: "")
                        val api = MastodonTLAPIJSONParse(context!!, toot_jsonObject.toString(), setting, 0)
                        //正規表現でURL消す
                        var text = Html.fromHtml(api.toot_text, Html.FROM_HTML_MODE_COMPACT).toString()
                        if (pref_setting?.getBoolean("pref_speech_url", true) != false) {
                            text = text.replace("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+".toRegex(), "URL省略")
                        }
                        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "tts")
                    }
                } else {
                    if (tts != null) {
                        tts?.stop()
                        tts?.shutdown()
                    }
                }
*/
            }
        }
    }


    /**
     * 通知のJSONParse
     *
     * @param streaming ストリーミングAPIの場合はtrue
     */
    private fun notificationJSONPase(toot_text_account: JSONObject, toot_text_jsonObject: JSONObject, type: String, streaming: Boolean) {

        if (activity != null && isAdded) {

            //配列を作成
            val Item = ArrayList<String>()
            //メモとか通知とかに
            Item.add(url ?: "")
            //内容
            Item.add("")
            //ユーザー名
            Item.add("")
            //時間、クライアント名等
            Item.add(toot_text_jsonObject.toString())
            //ぶーすとした？
            Item.add("false")
            //ふぁぼした？
            Item.add("false")
            //Mastodon / Misskey
            Item.add("Mastodon")
            //Insatnce/AccessToken
            Item.add(instance ?: "")
            Item.add(access_token ?: "")
            //設定ファイルJSON
            Item.add(json_data ?: "")
            //画像表示、こんてんとわーにんぐ
            Item.add("false")
            Item.add("false")

            if (streaming) {
                recyclerViewList?.add(0, Item)
            } else {
                recyclerViewList?.add(Item)
            }

            activity?.runOnUiThread {
                if (recyclerViewLayoutManager != null) {
                    (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                }
                //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                recyclerView?.adapter = customMenuRecyclerViewAdapter
                SnackberProgress.closeProgressSnackber()
                scroll = false

                /*
                    if (streaming) {
                        adapter.insert(listItem, 0);
                        // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                        int pos = listView.getFirstVisiblePosition();
                        int top = 0;
                        if (listView.getChildCount() > 0) {
                            top = listView.getChildAt(0).getTop();
                        }
                        listView.setAdapter(adapter);
                        // 要素追加前の状態になるようセットする
                        adapter.notifyDataSetChanged();
                        //一番上なら追いかける
                        if (pos == 0) {
                            listView.post(new Runnable() {
                                @Override
                                public void run() {
                                    listView.smoothScrollToPosition(0);
                                }
                            });
                        } else {
                            listView.setSelectionFromTop(pos + 1, top);
                        }
                    } else {
                        adapter.add(listItem);
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);
                        SnackberProgress.closeProgressSnackber();
                        listView.setSelectionFromTop(position, y);
                        scroll = false;
                    }
*/
            }
        }
    }

    /**
     * ダイレクトメッセージ（ストリーミングAPI）
     */
    private fun streamingAPIDirect(jsonObject: JSONObject) {

        if (activity != null && isAdded) {

            //配列を作成
            val Item = ArrayList<String>()
            //メモとか通知とかに
            Item.add("CustomMenu")
            //内容
            Item.add(url ?: "")
            //ユーザー名
            Item.add("")
            //時間、クライアント名等
            Item.add(jsonObject.toString())
            //ぶーすとした？
            Item.add("false")
            //ふぁぼした？
            Item.add("false")
            //Mastodon / Misskey
            Item.add("Mastodon")
            //Insatnce/AccessToken
            Item.add(instance ?: "")
            Item.add(access_token ?: "")
            //設定ファイルJSON
            Item.add(json_data ?: "")
            //画像表示、こんてんとわーにんぐ
            Item.add("false")
            Item.add("false")

            recyclerViewList?.add(0, Item)

            activity?.runOnUiThread {
                if (recyclerViewLayoutManager != null) {
                    (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                }
                //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                recyclerView?.adapter = customMenuRecyclerViewAdapter
                /*
                    adapter.insert(listItem, 0);
                    // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                    int pos = listView.getFirstVisiblePosition();
                    int top = 0;
                    if (listView.getChildCount() > 0) {
                        top = listView.getChildAt(0).getTop();
                    }
                    listView.setAdapter(adapter);
                    // 要素追加前の状態になるようセットする
                    adapter.notifyDataSetChanged();
                    //一番上なら追いかける
                    if (pos == 0) {
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                listView.smoothScrollToPosition(0);
                            }
                        });
                    } else {
                        listView.setSelectionFromTop(pos + 1, top);
                    }

                    //カウンター
                    if (Boolean.valueOf(toot_counter)) {
                        if (count_text != null) {
                            //含んでいるか
                            if (finalToot_text.contains(count_text)) {
                                String count_template = " : ";
                                akeome_count++;
                                countTextView.setText(count_text + count_template + String.valueOf(akeome_count));
                            }
                        }
                    }
*/
            }
        }
    }

    /**
     * MisskeyAPI
     * note/timeline
     *
     * @param id           追加読み込み時に利用。<br></br>追加読込しない場合は**null**を入れてね
     * @param notification 通知の場合は**true**
     */
    private fun loadMisskeyTimeline(id: String?, notification: Boolean) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", access_token)
            jsonObject.put("limit", 100)
            if (id != null) {
                jsonObject.put("untilId", id)
            }
            //TLで自分の投稿を見れるように
            if (url?.contains("timeline") != false) {
                jsonObject.put("includeLocalRenotes", true)
                jsonObject.put("includeMyRenotes", true)
                jsonObject.put("includeRenotedMyNotes", true)
            }
            if (notification) {
                //通知フィルター機能
                val filter = JSONArray()
                if (fav_filter) {
                    filter.put("reaction")
                }
                if (bt_filter) {
                    filter.put("renote")
                }
                if (mention_filter) {
                    filter.put("mention")
                }
                if (follow_filter) {
                    filter.put("follow")
                }
                if (vote_filter) {
                    filter.put("poll_vote")
                }
                jsonObject.put("includeTypes", filter)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //System.out.println(jsonObject.toString());
        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
        //作成
        val request = Request.Builder()
                .url(url ?: "")
                .post(requestBody)
                .build()
        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()?.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗時
                    if (activity != null) {
                        activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    }
                } else {
                    try {
                        val jsonArray = JSONArray(response_string)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            if (!notification) {
                                setMisskeyTLParse(jsonObject)
                            } else {
                                setMisskeyNotification(jsonObject)
                            }
                        }
                        if (activity != null) {
                            activity?.runOnUiThread { swipeRefreshLayout?.isRefreshing = false }
                        }
                        //最後のIDを保存
                        val last = jsonArray.getJSONObject(99)
                        this@CustomMenuTimeLine.untilId = last.getString("id")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })
    }

    /**
     * Misskey JSON TL Parse
     *
     * @param jsonObject JSONオブジェクト
     */
    private fun setMisskeyTLParse(jsonObject: JSONObject) {
        //AppCompatActivity activity = (AppCompatActivity)getContext();

        if (activity != null && isAdded) {
            //配列を作成
            val Item = ArrayList<String>()
            //メモとか通知とかに
            Item.add("CustomMenu")
            //内容
            Item.add(url ?: "")
            //ユーザー名
            Item.add("")
            //時間、クライアント名等
            Item.add(jsonObject.toString())
            //ぶーすとした？
            Item.add("false")
            //ふぁぼした？
            Item.add("")
            //Mastodon / Misskey
            Item.add("Misskey")
            //Insatnce/AccessToken
            Item.add(instance ?: "")
            Item.add(access_token ?: "")
            //設定ファイルJSON
            Item.add(json_data ?: "")
            //画像表示、こんてんとわーにんぐ
            Item.add("false")
            Item.add("false")

            recyclerViewList?.add(Item)

            activity?.runOnUiThread {
                if (recyclerViewLayoutManager != null) {
                    (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                }
                //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                recyclerView?.adapter = customMenuRecyclerViewAdapter
                SnackberProgress.closeProgressSnackber()
                scroll = false
                /*
                    adapter.add(listItem);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                    //くるくる終了
                    SnackberProgress.closeProgressSnackber();
                    listView.setSelectionFromTop(position, y);
                    scroll = false;
*/
            }
        }
    }

    /**
     * Misskey通知
     */
    private fun setMisskeyNotification(jsonObject: JSONObject) {
        if (activity != null && isAdded) {

            //配列を作成
            val Item = ArrayList<String>()
            //メモとか通知とかに
            Item.add(url ?: "")
            //内容
            Item.add("")
            //ユーザー名
            Item.add("")
            //時間、クライアント名等
            Item.add(jsonObject.toString())
            //ぶーすとした？
            Item.add("false")
            //ふぁぼした？
            Item.add("")
            //Mastodon / Misskey
            Item.add("Misskey")
            //Insatnce/AccessToken
            Item.add(instance ?: "")
            Item.add(access_token ?: "")
            //設定ファイルJSON
            Item.add(json_data ?: "")
            //画像表示、こんてんとわーにんぐ
            Item.add("false")
            Item.add("false")

            recyclerViewList?.add(Item)

            activity?.runOnUiThread {
                if (recyclerViewLayoutManager != null) {
                    (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                }
                //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                recyclerView?.adapter = customMenuRecyclerViewAdapter
                SnackberProgress.closeProgressSnackber()
                scroll = false
                /*
                    adapter.add(listItem);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                    //くるくる終了
                    SnackberProgress.closeProgressSnackber();
                    listView.setSelectionFromTop(position, y);
                    scroll = false;
*/
            }
        }

    }


    /**
     * ストリーミングAPI版通知Parse
     */
    private fun streamingNotificationParse(notification: Notification) {
        val user_name = arrayOf(notification.account!!.displayName)
        var type = notification.type
        val user_avater_url = notification.account!!.avatar
        val user_id = notification.account!!.userName
        val user = notification.account!!.acct

        val account_id = notification.account!!.id

        var toot_text_id_string: String? = null

        val toot_text = arrayOf<String>()
        var toot_text_time: String? = null
        var layout_type: String? = null
        var toot_text_id: Long = 0

        when (type) {
            "mention" -> {
                type = getString(R.string.notification_mention)
                layout_type = "Notification_mention"
            }
            "reblog" -> {
                type = getString(R.string.notification_Boost)
                layout_type = "Notification_reblog"
            }
            "favourite" -> {
                type = getString(R.string.notification_favourite)
                layout_type = "Notification_favourite"
            }
            "follow" -> {
                type = getString(R.string.notification_followed)
                layout_type = "Notification_follow"
            }
        }
        //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
        try {
            toot_text[0] = notification.status!!.content
            toot_text_id = notification.status!!.id
            toot_text_id_string = toot_text_id.toString()
        } catch (e: NullPointerException) {
            toot_text[0] = ""
            toot_text_id = 0
            toot_text_id_string = toot_text_id.toString()
        }

        //時間フォーマット
        toot_text_time = getCreatedAtFormat(toot_text_time)

        //カスタム絵文字
        if (pref_setting!!.getBoolean("pref_custom_emoji", true) || java.lang.Boolean.valueOf(custom_emoji)) {

            try {
                //本文
                val emoji_List = notification.status!!.emojis
                emoji_List.forEach { emoji ->
                    val emoji_name = emoji.shortcode
                    val emoji_url = emoji.url
                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                    toot_text[0] = toot_text[0].replace(":$emoji_name:", custom_emoji_src)
                }

            } catch (e: NullPointerException) {
                toot_text[0] = ""
                toot_text_id = 0
                toot_text_id_string = toot_text_id.toString()
            }

            //DisplayNameのほう
            val account_emoji_List = notification.account!!.emojis
            account_emoji_List.forEach { emoji ->
                val emoji_name = emoji.shortcode
                val emoji_url = emoji.url
                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                user_name[0] = user_name[0].replace(":$emoji_name:", custom_emoji_src)
            }
        }


        val mediaURL = arrayOf<String>(null!!, null!!, null!!, null!!)
        var media_url_1: String? = null
        var media_url_2: String? = null
        var media_url_3: String? = null
        var media_url_4: String? = null
        //めでぃあ
        //配列に入れる形で
        try {
            val i = intArrayOf(0)
            val list = notification.status!!.mediaAttachments
            list.forEach { media ->
                mediaURL[i[0]] = media.url
                i[0]++
            }
            //配列から文字列に
            media_url_1 = mediaURL[0]
            media_url_2 = mediaURL[1]
            media_url_3 = mediaURL[2]
            media_url_4 = mediaURL[3]
        } catch (e: NullPointerException) {
            //配列から文字列に
            media_url_1 = null
            media_url_2 = null
            media_url_3 = null
            media_url_4 = null
        }


        //Card
        val card = ArrayList<String>()
        var cardTitle: String? = null
        var cardURL: String? = null
        var cardDescription: String? = null
        var cardImage: String? = null

        try {
            val statuses = Statuses(client!!).getCard(toot_text_id).execute()
            if (!statuses.url.isEmpty()) {
                cardTitle = statuses.title
                cardURL = statuses.url
                cardDescription = statuses.description
                cardImage = statuses.image

                card.add(statuses.title)
                card.add(statuses.url)
                card.add(statuses.description)
                card.add(statuses.image!!)
            }
        } catch (e: Mastodon4jRequestException) {
            e.printStackTrace()
        }

        if (activity != null && isAdded) {

            //配列を作成
            val Item = ArrayList<String>()
            //メモとか通知とかに
            Item.add(layout_type!!)
            //内容
            Item.add(toot_text[0])
            //ユーザー名
            Item.add(user_name[0] + " @" + user + type)
            //時間、クライアント名等
            Item.add("トゥートID : " + toot_text_id_string + " / " + getString(R.string.time) + " : " + toot_text_time)
            //Toot ID 文字列版
            Item.add(toot_text_id_string!!)
            //アバターURL
            Item.add(user_avater_url)
            //アカウントID
            Item.add(account_id.toString())
            //ユーザーネーム
            Item.add(user)
            //メディア
            Item.add(media_url_1!!)
            Item.add(media_url_2!!)
            Item.add(media_url_3!!)
            Item.add(media_url_4!!)
            //カード
            Item.add(cardTitle!!)
            Item.add(cardURL!!)
            Item.add(cardDescription!!)
            Item.add(cardImage!!)
            recyclerViewList!!.add(0, Item)

            activity!!.runOnUiThread {
                if (recyclerViewLayoutManager != null) {
                    (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                }
                //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                recyclerView!!.adapter = customMenuRecyclerViewAdapter
                /*
                    position = listView.getFirstVisiblePosition();
                    y = listView.getChildAt(0).getTop();
                    adapter.insert(listItem, 0);
                    listView.setAdapter(adapter);
                    //System.out.println("TOP == " + top);
                    // 要素追加前の状態になるようセットする
                    adapter.notifyDataSetChanged();
                    listView.setSelectionFromTop(position, y);
                    //ストリーミングAPI前のStatus取得
                    //loadNotification(max_id);
*/
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shutdownable?.shutdown()
        if (webSocketClient != null) {
            //終了
            webSocketClient?.close()
        }
        if (notification_WebSocketClient != null) {
            notification_WebSocketClient?.close()
        }
        if (tts != null) {
            tts!!.shutdown()
        }
        //OLEDとかかかわらず戻す
        //getActivity().setTheme(R.style.AppTheme);
        //((Home) getActivity()).getToolBer().setBackgroundColor(Color.parseColor("#2196f3"));
    }

    /**
     * トゥートカウンターようれいあうと
     */
    private fun setTootCounterLayout() {
        //カウンターようレイアウト
        val LayoutlayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val countLinearLayout = LinearLayout(context)
        countLinearLayout.orientation = LinearLayout.HORIZONTAL
        countLinearLayout.layoutParams = LayoutlayoutParams
        linearLayout?.addView(countLinearLayout, 0)
        //いろいろ
        val countEditText = EditText(context)
        val countButton = Button(context)
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.weight = 1f
        countTextView?.layoutParams = layoutParams
        countEditText.layoutParams = layoutParams
        countButton.background = context?.getDrawable(R.drawable.button_style_white)
        countButton.text = ">"
        countEditText.hint = getString(R.string.toot_count_hint)
        //背景
        var background = "ffffff"
        if (java.lang.Boolean.valueOf(dark_mode)) {
            background = "000000"
        }

        countLinearLayout.addView(countEditText)
        countLinearLayout.addView(countButton)
        countLinearLayout.addView(countTextView)

        //テキストを決定
        activity?.runOnUiThread {
            countButton.setOnClickListener {
                count_text = countEditText.text.toString()
                akeome_count = 0
                countTextView?.text = "$count_text : $akeome_count"
            }
            //長押しでコピー
            countTextView?.setOnLongClickListener {
                val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", akeome_count.toString()))
                Toast.makeText(context, R.string.copy, Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    /**
     * 片手モード
     */
    private fun one_hand_mode() {
        val one_hand_LinearLayout = LinearLayout(context)
        val one_hand_layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        one_hand_LinearLayout.orientation = LinearLayout.VERTICAL
        one_hand_layoutParams.weight = 1f
        one_hand_LinearLayout.layoutParams = one_hand_layoutParams
        one_hand_LinearLayout.gravity = Gravity.CENTER
        //使いみち誰か（）
        //TL領域を広げるとかする
        val textView = TextView(context)
        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textView.textSize = 18f
        textView.text = getString(R.string.custom_menu_tl_up)
        //領域広げる
        one_hand_LinearLayout.setOnClickListener {
            if (textView.text.toString().contains(getString(R.string.custom_menu_tl_up))) {
                textView.text = getString(R.string.custom_menu_tl_down)
                one_hand_layoutParams.weight = 2f
            } else {
                textView.text = getString(R.string.custom_menu_tl_up)
                one_hand_layoutParams.weight = 1f
            }
        }
        //タイトルも
        val title_TextView = TextView(context)
        title_TextView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        title_TextView.text = arguments?.getString("name")
        title_TextView.textSize = 24f
        //追加
        one_hand_LinearLayout.addView(title_TextView)
        one_hand_LinearLayout.addView(textView)
        //ダークモード対応
        if (dark_theme) {
            title_TextView.setTextColor(Color.parseColor("#ffffff"))
            textView.setTextColor(Color.parseColor("#ffffff"))
            one_hand_LinearLayout.setBackgroundColor(Color.parseColor("#000000"))
        }
        //半分
        parent_linearlayout?.addView(one_hand_LinearLayout, 0)
    }

    /**
     * ドロワーの画像、文字を変更する
     */
    private fun setDrawerImageText(avatarUrl: String, headerUri: String, display_name: String?, username: String) {

        //ImageViewのサイズ変更
        val layoutParams = LinearLayout.LayoutParams(200, 200)
        avater_imageView?.layoutParams = layoutParams
        val glideSupport = GlideSupport()

        //Wi-Fi接続状況確認
        if (context != null && user_account_textView != null) {
            val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            //一応Nullチェック
            if (header_imageView != null) {
                //画像読み込むか
                if (pref_setting?.getBoolean("pref_drawer_avater", false) == true) {
                    //読み込まない
                    avater_imageView?.setImageResource(R.drawable.ic_person_black_24dp)
                    header_imageView?.setBackgroundColor(Color.parseColor("#c8c8c8"))
                }
                //Wi-Fi時は読み込む
                if (pref_setting?.getBoolean("pref_avater_wifi", true) != false) {
                    //既定でGIFは再生しない方向で
                    //GIF/GIFじゃないは引数に入れる前から判断してる
                    glideSupport.loadGlide(avatarUrl, avater_imageView!!)
                    glideSupport.loadGlide(headerUri, header_imageView!!)
                } else {
                    glideSupport.loadGlideReadFromCache(avatarUrl, avater_imageView!!)
                    glideSupport.loadGlideReadFromCache(headerUri, header_imageView!!)
                }
                //UserName
                val imageGetter = PicassoImageGetter(user_account_textView!!)
                user_account_textView?.text = Html.fromHtml(display_name, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
                user_id_textView?.text = username

            }
        }
    }

    /**
     * 時刻をフォーマットして返す
     */
    private fun getCreatedAtFormat(createdAt: String?): String {
        var createdAt = createdAt
        //フォーマットを規定の設定にする？
        //ここtrueにした
        if (pref_setting!!.getBoolean("pref_custom_time_format", true)) {
            //時差計算？
            if (simpleDateFormat == null && japanDateFormat == null && calendar == null) {
                simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                simpleDateFormat?.timeZone = TimeZone.getTimeZone("UTC")
                //日本用フォーマット
                japanDateFormat = SimpleDateFormat(pref_setting!!.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!)
                japanDateFormat?.timeZone = TimeZone.getTimeZone(TimeZone.getDefault().id)
                calendar = Calendar.getInstance()
            }
            try {
                val date = simpleDateFormat!!.parse(createdAt!!)
                calendar?.time = date!!
                //タイムゾーンを設定
                //calendar.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
                calendar?.add(Calendar.HOUR, +Integer.valueOf(pref_setting!!.getString("pref_time_add", "9")!!))
                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                createdAt = japanDateFormat?.format(calendar!!.time)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }
        return createdAt as String
    }


    /**
     * 通知（どん
     * *
     */
    private fun setStreamingNotification() {
        //StreamingAPIのLink違う時
        val url = "wss://$instance/api/v1/streaming/?stream=user:notification&access_token=$access_token"
        if (context != null) {
            vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        try {
            notification_WebSocketClient = object : WebSocketClient(URI(url)) {
                override fun onOpen(handshakedata: ServerHandshake) {
                    System.out.println("通知おーぷん")
                }

                override fun onMessage(message: String) {
                    try {
                        val jsonObject = JSONObject(message)
                        //if (jsonObject.getString("type").equals("notification")) {
                        val `object` = jsonObject.getString("payload")
                        val payload_JsonObject = JSONObject(`object`)
                        val type = payload_JsonObject.getString("type")
                        val account = payload_JsonObject.getJSONObject("account")
                        var display_name = account.getString("display_name")
                        val acct = account.getString("acct")
                        //カスタム絵文字
                        if (java.lang.Boolean.valueOf(custom_emoji)) {
                            val emojis = account.getJSONArray("emojis")
                            for (e in 0 until emojis.length()) {
                                val emoji = emojis.getJSONObject(e)
                                val emoji_name = emoji.getString("shortcode")
                                val emoji_url = emoji.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                display_name = display_name.replace(":$emoji_name:", custom_emoji_src)
                            }
                        }
                        //トースト出す
                        val finalDisplay_name = display_name

                        //通知RecyclerView
                        if (activity is Home) {
                            val home = activity as Home
                            //RecyclerViewで使うの
                            val notificationList = home.tlQuickSettingSnackber?.recyclerViewList
                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add("TLQSNotification")
                            //内容
                            Item.add(url)
                            //ユーザー名
                            Item.add("")
                            //時間、クライアント名等
                            Item.add(payload_JsonObject.toString())
                            //ぶーすとした？
                            Item.add("false")
                            //ふぁぼした？
                            Item.add("false")
                            //Mastodon / Misskey
                            Item.add("Mastodon")
                            //Insatnce/AccessToken
                            Item.add(instance ?: "")
                            Item.add(access_token ?: "")
                            //設定ファイルJSON
                            Item.add(json_data ?: "")
                            //画像表示、こんてんとわーにんぐ
                            Item.add("false")
                            Item.add("false")
                            notificationList?.add(Item)
                        }

                        activity?.runOnUiThread {

                            //カスタムトースト
                            val toast = Toast(context)
                            val inflater = layoutInflater
                            val layout = inflater.inflate(R.layout.notification_toast_layout, null)
                            //文字
                            val toast_text = layout.findViewById<TextView>(R.id.notification_toast_textView)
                            val picassoImageGetter = PicassoImageGetter(toast_text)
                            toast_text.text = Html.fromHtml(CustomMenuRecyclerViewAdapter.toNotificationType(context, type) + "<br>" + finalDisplay_name + "@" + acct, Html.FROM_HTML_MODE_COMPACT, picassoImageGetter, null)
                            val toast_imageview = layout.findViewById<AppCompatImageView>(R.id.notification_toast_icon_imageView)
                            //アイコン
                            toast_imageview.setImageDrawable(getNotificationIcon(type))
                            //レイアウト適用
                            toast.view = layout
                            toast.duration = Toast.LENGTH_LONG
                            toast.show()

                            if (pref_setting!!.getBoolean("pref_notification_vibrate", true) && vibrator != null) {
                                val pattern = longArrayOf(100, 100, 100, 100)
                                //バイブなんか非推奨になってた（）書き直した
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(
                                            VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE)
                                    )
                                } else {
                                    vibrator?.vibrate(pattern, -1)
                                }
                            }

                        }
                        // }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

                override fun onClose(code: Int, reason: String, remote: Boolean) {

                }

                override fun onError(ex: Exception) {
                    //404エラーは再接続？
                    //何回もAPI叩かれると困る
                    if (instance_api_streaming_api_link.isEmpty()) {
                        getInstanceUrlsStreamingAPI()
                        useStreamingAPI()
                    }
                }
            }
            //接続
            notification_WebSocketClient?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

    }

    /**
     * 時間指定投稿（予約投稿）一覧読み込み
     */
    private fun loadScheduled_statuses(view: View) {
        //作成
        val url = url + "?access_token=" + access_token
        SnackberProgress.showProgressSnackber(view, view.context, getString(R.string.loading) + "\n" + arguments?.getString("content"))
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()?.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗時
                    activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        val jsonArray = JSONArray(response_string)
                        //無いとき
                        if (jsonArray.length() == 0) {
                            activity?.runOnUiThread {
                                SnackberProgress.closeProgressSnackber()
                                Toast.makeText(context, getString(R.string.not_fount_time_post), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            for (i in 0 until jsonArray.length()) {
                                val toot_jsonObject = jsonArray.getJSONObject(i)
                                if (activity != null && isAdded) {
                                    //配列を作成
                                    val Item = ArrayList<String>()
                                    //メモとか通知とかに
                                    Item.add("CustomMenu 時間指定投稿")
                                    //内容
                                    Item.add(url)
                                    //ユーザー名
                                    Item.add("")
                                    //JSONObject
                                    Item.add(toot_jsonObject.toString())
                                    //ぶーすとした？
                                    Item.add("false")
                                    //ふぁぼした？
                                    Item.add("false")
                                    //Mastodon / Misskey
                                    Item.add("Mastodon")
                                    //Insatnce/AccessToken
                                    Item.add(instance ?: "")
                                    Item.add(access_token ?: "")
                                    //設定ファイルJSON
                                    Item.add(json_data ?: "")
                                    //画像表示、こんてんとわーにんぐ
                                    Item.add("false")
                                    Item.add("false")

                                    recyclerViewList?.add(Item)
                                    activity?.runOnUiThread {
                                        if (recyclerViewLayoutManager != null) {
                                            (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                                        }
                                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                                        recyclerView?.adapter = customMenuRecyclerViewAdapter
                                        SnackberProgress.closeProgressSnackber()
                                    }
                                }
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
     * お気に入り一覧を取得
     */
    private fun loadFollowSuggestions(view: View) {
        //作成
        val url = url + "?access_token=" + access_token
        SnackberProgress.showProgressSnackber(view, view.context, getString(R.string.loading) + "\n" + arguments?.getString("content"))
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()?.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗時
                    activity?.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        val jsonArray = JSONArray(response_string)
                        for (i in 0 until jsonArray.length()) {
                            val toot_jsonObject = jsonArray.getJSONObject(i)
                            if (activity != null && isAdded) {
                                //配列を作成
                                val Item = ArrayList<String>()
                                //メモとか通知とかに
                                Item.add("CustomMenu フォロー推奨")
                                //内容
                                Item.add(url)
                                //ユーザー名
                                Item.add("")
                                //JSONObject
                                Item.add(toot_jsonObject.toString())
                                //ぶーすとした？
                                Item.add("false")
                                //ふぁぼした？
                                Item.add("false")
                                //Mastodon / Misskey
                                Item.add("Mastodon")
                                //Insatnce/AccessToken
                                Item.add(instance ?: "")
                                Item.add(access_token ?: "")
                                //設定ファイルJSON
                                Item.add(json_data ?: "")
                                //画像表示、こんてんとわーにんぐ
                                Item.add("false")
                                Item.add("false")

                                recyclerViewList?.add(0, Item)
                                activity?.runOnUiThread {
                                    if (recyclerViewLayoutManager != null) {
                                        (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                                    }
                                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                                    SnackberProgress.closeProgressSnackber()
                                }
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
     * 通知アイコン
     */
    private fun getNotificationIcon(type: String): Drawable? {
        var drawable = context!!.getDrawable(R.drawable.ic_notifications_black_24dp)
        when (type) {
            "follow" -> drawable = context!!.getDrawable(R.drawable.ic_person_add_black_24dp)
            "favourite" -> drawable = context!!.getDrawable(R.drawable.ic_star_border_black_24dp)
            "reblog" -> drawable = context!!.getDrawable(R.drawable.ic_repeat_black_24dp)
            "mention" -> drawable = context!!.getDrawable(R.drawable.ic_announcement_black_24dp)
            "reaction" -> drawable = context!!.getDrawable(R.drawable.ic_audiotrack_black_24dp)
        }
        return drawable
    }


    /**
     * Android 10の新しいジェスチャーで戻るジェスチャーとドロワー開くジェスチャーをかぶらないようにする
     * 端からスワイプ以外でも動作するようにする
     */
    private fun addNavigationOpen() {
        //すたーと
        val start = floatArrayOf(0f)
        val end = floatArrayOf(0f)
        val y_start = floatArrayOf(0f)
        val y_end = floatArrayOf(0f)
        recyclerView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    start[0] = event.x
                    y_start[0] = event.y
                }
                MotionEvent.ACTION_UP -> {
                    end[0] = event.x
                    y_end[0] = event.y
                    //System.out.println("end : " + y_end[0]);
                    //System.out.println("final : " + (y_start[0] - y_end[0]));
                    //両方揃ったら比較開始
                    if (start[0] != end[0]) {
                        //なんとなく400以上の誤差がないとうごかないように　と　縦スクロールが大きいと動作しないようにする（100から-100までのみ）
                        if (end[0] - start[0] > 400 && y_start[0] - y_end[0] < 100 && y_start[0] - y_end[0] > -100) {
                            //ドロワー開く。getActivity()あってよかた
                            val drawer = activity?.findViewById<View>(R.id.drawer_layout) as DrawerLayout
                            drawer.openDrawer(Gravity.LEFT)
                        }
                    }
                }
            }//System.out.println("start : " + y_start[0]);

            false
        }
    }

    /*ネットワークの変更を検知する*/
    private fun setNetworkChangeCallback() {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        //
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        connectivityManager.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                //最初は無視する
                if (network_count > 0) {
                    //変更されたら
                    if (webSocketClient != null || notification_WebSocketClient != null && recyclerView != null) {
                        //なんとなくスナックバー
                        Snackbar.make(recyclerView!!, R.string.network_change, Snackbar.LENGTH_SHORT).show()
                        //5秒後にストリーミングAPIに再接続する
                        val timer = Timer()
                        val timerTask = object : TimerTask() {
                            override fun run() {
                                if (webSocketClient != null) {
                                    webSocketClient?.close()
                                    //通知以外
                                    if (url?.contains("/api/v1/notifications") == false) {
                                        loadTimeline("")
                                        //ストリーミング
                                        useStreamingAPI()
                                    } else {
                                        activity?.runOnUiThread {
                                            notificationLayout()
                                        }
                                        //普通にAPI叩く
                                        loadNotification("")
                                        //ストリーミング
                                        useStreamingAPI()
                                    }
                                }
                                if (notification_WebSocketClient != null) {
                                    notification_WebSocketClient?.close()
                                    setStreamingNotification()
                                }
                            }
                        }
                        timer.schedule(timerTask, 5000)

                    }
                }
                network_count += 1
            }
        })
    }

    /*ハッシュタグ（＃）を入れる*/
    private fun setName(context: String, title: String?): String {
        var title = title
        if (context.contains("/api/v1/timelines/tag/") || context.contains("/api/v1/timelines/tag/?local=true")) {
            title = "#" + title!!
        }
        return title as String
    }

    //インスタンス名を返す
    fun getInstance(): String {
        return instance.toString()
    }

    //CustomMenuの名前を返す
    fun getCustomMenuName(): String {
        return name.toString()
    }

    //読み取り専用かどうかを返す
    fun isReadOnly(): Boolean {
        return isReadOnly.toBoolean()
    }


    override fun onStop() {
        super.onStop()
        //アプリが後ろに移動したらストリーミングAPI切る
        //設定を読み込む
        if (!pref_setting.getBoolean("pref_timeline_streaming_background", false)) {
            if (webSocketClient?.isClosed == false) {
                webSocketClient?.close()
            }
        }
        //通知Ver
        if (!pref_setting.getBoolean("pref_notification_streaming_background", false)) {
            if (notification_WebSocketClient?.isClosed == false) {
                notification_WebSocketClient?.close()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        //アプリを表示させたらストリーミングAPI接続する
        if (webSocketClient?.isClosed == true) {
            useStreamingAPI()
            //Snackbar.make(view!!, "タイムラインのストリーミングAPIへ再接続しました。", Snackbar.LENGTH_SHORT).show()
        }
        if (notification_WebSocketClient?.isClosed == true) {
            setStreamingNotification()
            //Snackbar.make(view!!, "通知のストリーミングAPIへ再接続しました。", Snackbar.LENGTH_SHORT).show()
        }
    }

    //画像表示させるか
    fun getImageLoad(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        //設定からアバター画像読み込まないの場合
        if (pref_setting?.getBoolean("setting_avater_get", false) == true) {
            return false
        }
        //Wi-Fi接続時
        if (pref_setting?.getBoolean("setting_avater_wifi_get_info", true) != false) {
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
        //CustomMenuの設定で有効?
        //今回はデフォで有効にしている。
        if (image_load?.toBoolean() ?: true) {
            return true
        }
        return false
    }


    companion object {


        var url: String? = null
            private set

        //フォント
        /**
         * フォント設定
         */
        lateinit var font_Typeface: Typeface

        //misskey
        /**
         * Misskeyモードかどうか
         */
        var isMisskeyMode = false
            private set
        private var misskey_username: String? = ""
        var account_id = ""
            private set

        /**
         * CustomMenu利用中かどうかを返す
         */
        val isUseCustomMenu: Boolean
            get() = true

        /**
         * 通知かどうか
         */
        val isNotification: Boolean
            get() {
                var mode = false
                if (url!!.contains("/api/v1/notifications")) {
                    mode = true
                }
                return mode
            }
    }
}
