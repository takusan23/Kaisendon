package io.github.takusan23.Kaisendon.Activity

import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter
import io.github.takusan23.Kaisendon.Theme.DarkModeSupport
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SnackberProgress
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class UserFollowActivity : AppCompatActivity() {

    internal var account_id: String? = null

    private val dialog: ProgressDialog? = null

    internal var toot_time: String? = null

    private var AccessToken: String? = null
    private var Instance: String? = null

    //private ListView listView;
    //private ArrayList<ListItem> toot_list;
    //private HomeTimeLineAdapter adapter;
    //private SimpleAdapter simpleAdapter;

    private var snackber_text: String? = null

    //メディア
    internal var media_url_1: String? = null
    internal var media_url_2: String? = null
    internal var media_url_3: String? = null
    internal var media_url_4: String? = null

    internal lateinit var pref_setting: SharedPreferences

    internal var max_id: String? = null

    internal var position: Int = 0
    internal var y: Int = 0

    private var isMisskey = false

    //追加読み込み制御
    private var scroll = false
    private val snackbar: Snackbar? = null

    internal var max_count: Int = 0
    /**
     * Mastodon URL
     */
    private var mastodonUrl = ""
    /**
     * Misskey 追加読み込み用
     */
    private var nextID = ""
    /**
     * Misskey URL
     */
    private var misskeyUrl = ""

    //RecyclerView
    private var recyclerViewList: ArrayList<ArrayList<*>>? = null
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var customMenuRecyclerViewAdapter: CustomMenuRecyclerViewAdapter? = null
    //固定トゥーとを読み込み済みの場合はtrue
    private var loadedPinnedStatus = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)

        //ダークテーマに切り替える機能
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)
        setContentView(R.layout.activity_user_follow)


        //設定を読み込み
        //Misskey
        if (isMisskey) {
            AccessToken = pref_setting.getString("misskey_main_token", "")
            Instance = pref_setting.getString("misskey_instance", "")
        } else {
            AccessToken = pref_setting.getString("main_token", "")
            Instance = pref_setting.getString("main_instance", "")
        }

        recyclerView = findViewById(R.id.follow_follower_list)

        recyclerViewList = ArrayList()
        //ここから下三行必須
        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = mLayoutManager
        customMenuRecyclerViewAdapter = CustomMenuRecyclerViewAdapter(recyclerViewList!!)
        recyclerView!!.adapter = customMenuRecyclerViewAdapter
        recyclerViewLayoutManager = recyclerView!!.layoutManager

        //account_idを受け取る
        val intent = intent
        account_id = intent.getStringExtra("account_id")

        //カウントも受け取る
        max_count = intent.getIntExtra("count", 0)

        //フォローかフォロワーかを分ける
        val follow_follower = intent.getIntExtra("follow_follower", 0)

        //Misskeyか分ける
        if (intent.getBooleanExtra("misskey", false)) {
            isMisskey = true
        }
        // dialog.show();
        if (follow_follower == 1) {
            snackber_text = getString(R.string.loading_follow) + "\r\n /api/v1/accounts/:id/followers"
        }
        if (follow_follower == 2) {
            snackber_text = getString(R.string.loading_follower) + "\r\n /api/v1/accounts/:id/following"
        }
        if (follow_follower == 3) {
            snackber_text = getString(R.string.loading_toot) + "\r\n /api/v1/accounts/:id/statuses"
        }

        //URL
        when (getIntent().getIntExtra("follow_follower", 0)) {
            1 -> {
                misskeyUrl = "/api/users/following"
                mastodonUrl = "https://$Instance/api/v1/accounts/$account_id/following"
            }
            2 -> {
                misskeyUrl = "/api/users/followers"
                mastodonUrl = "https://$Instance/api/v1/accounts/$account_id/followers"
            }
            3 -> {
                misskeyUrl = "/api/users/notes"
                mastodonUrl = "https://$Instance/api/v1/accounts/$account_id/statuses"
            }
        }


        if (isMisskey) {
            postMisskeyAPI(misskeyUrl, null)
            //最後までスクロール
            recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val visibleItemCount = (recyclerView.layoutManager as LinearLayoutManager).childCount
                    val totalItemCount = (recyclerView.layoutManager as LinearLayoutManager).itemCount
                    //最後までスクロールしたときの処理
                    if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                        position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        y = recyclerView.getChildAt(0).top
                        if (recyclerViewList!!.size >= 20) {
                            SnackberProgress.showProgressSnackber(recyclerView, this@UserFollowActivity, getString(R.string.loading) + "\n" + misskeyUrl)
                            scroll = true
                            postMisskeyAPI(misskeyUrl, nextID)
                        }
                    }
                }
            })

        } else {
            getMastodonAPI(mastodonUrl, null)
            //最後までスクロール
            recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val visibleItemCount = (recyclerView.layoutManager as LinearLayoutManager).childCount
                    val totalItemCount = (recyclerView.layoutManager as LinearLayoutManager).itemCount
                    //最後までスクロールしたときの処理
                    if (firstVisibleItem + visibleItemCount == totalItemCount && !scroll) {
                        position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        y = recyclerView.getChildAt(0).top
                        if (recyclerViewList!!.size >= 20) {
                            SnackberProgress.showProgressSnackber(recyclerView, this@UserFollowActivity, getString(R.string.loading) + "\n" + misskeyUrl)
                            scroll = true
                            getMastodonAPI(mastodonUrl, nextID)
                        }
                    }
                }
            })
        }
    }

    /**
     * APIを叩くところだけ
     * パースはそれぞれ
     *
     * @param api_url /api/xxxの部分
     */
    private fun postMisskeyAPI(api_url: String, untilId: String?) {
        //くるくる
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance$api_url"
        SnackberProgress.showProgressSnackber(recyclerView, this, getString(R.string.loading) + "\n" + url)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("limit", 100)
            jsonObject.put("userId", intent.getStringExtra("account_id"))
            //追加読み込み
            //Follow/Followerに関してはJSONArrayの最後にIDがあるのでそれ利用
            if (untilId != null) {
                jsonObject.put("untilId", untilId)
            }
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
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@UserFollowActivity, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@UserFollowActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        when (intent.getIntExtra("follow_follower", 0)) {
                            1 -> {
                                loadMisskeyFollowFollower(response_string)
                                nextID = JSONObject(response_string).getString("next")
                            }
                            2 -> {
                                loadMisskeyFollowFollower(response_string)
                                nextID = JSONObject(response_string).getString("next")
                            }
                            3 -> {
                                loadMisskeyNote(response_string)
                                nextID = JSONArray(response_string).getJSONObject(99).getString("id")
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
     * Mastodon API叩くところ
     *
     * @param api_url 全部
     * @param max_id  使わないなら**null**で
     */
    private fun getMastodonAPI(api_url: String, max_id: String?) {
        //作成
        SnackberProgress.showProgressSnackber(recyclerView, this, getString(R.string.loading) + "\n" + api_url)
        //パラメータを設定
        val builder = HttpUrl.parse(api_url)!!.newBuilder()
        builder.addQueryParameter("limit", "80")
        builder.addQueryParameter("access_token", AccessToken)
        if (max_id != null) {
            builder.addQueryParameter("max_id", max_id)
        }
        //固定トゥートを読み込む
        if (!loadedPinnedStatus) {
            builder.addQueryParameter("pinned", "true")
        }
        val final_url = builder.build().toString()
        val request = Request.Builder()
                .url(final_url)
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@UserFollowActivity, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@UserFollowActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        when (intent.getIntExtra("follow_follower", 0)) {
                            1 -> {
                                loadMastodonFollowFollower(response_string)
                                nextID = JSONArray(response_string).getJSONObject(79).getString("id")
                            }
                            2 -> {
                                loadMastodonFollowFollower(response_string)
                                nextID = JSONArray(response_string).getJSONObject(79).getString("id")
                            }
                            3 -> {
                                loadMastodonToot(response_string)
                                //固定トゥートを読み込んだらそれ以外を読み込むのでもう一回叩く
                                if (!loadedPinnedStatus) {
                                    loadedPinnedStatus = true
                                    getMastodonAPI(mastodonUrl, null)
                                }
                                nextID = JSONArray(response_string).getJSONObject(39).getString("id")
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
     * Mastodon status Parse
     */
    fun loadMastodonToot(response_string: String) {
        try {
            val jsonArray = JSONArray(response_string)
            //max_count = jsonArray.length();
            for (i in 0 until jsonArray.length()) {
                val toot_jsonObject = jsonArray.getJSONObject(i)
                //配列を作成
                val Item = ArrayList<String>()
                //メモとか通知とかに
                Item.add("CustomMenu Mastodon Toot")
                //内容
                Item.add(mastodonUrl)
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
                Item.add(Instance.toString())
                Item.add(AccessToken.toString())
                //設定ファイルJSON
                Item.add("")
                //画像表示、こんてんとわーにんぐ
                Item.add("false")
                Item.add("false")

                recyclerViewList!!.add(Item)
                runOnUiThread {
                    if (recyclerViewLayoutManager != null) {
                        (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    //recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                    SnackberProgress.closeProgressSnackber()
                    scroll = false
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /**
     * Mastodon Follow Follower JSON Parse
     */
    fun loadMastodonFollowFollower(response_string: String) {
        try {
            val jsonArray = JSONArray(response_string)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                //配列を作成
                val Item = ArrayList<String>()
                //メモとか通知とかに
                Item.add("CustomMenu Mastodon Follow")
                //内容
                Item.add(mastodonUrl)
                //ユーザー名
                Item.add("")
                //JSONObject
                Item.add(jsonObject.toString())
                //ぶーすとした？
                Item.add("false")
                //ふぁぼした？
                Item.add("false")
                //Mastodon / Misskey
                Item.add("Mastodon")
                //Insatnce/AccessToken
                Item.add(Instance.toString())
                Item.add(AccessToken.toString())
                //設定ファイルJSON
                Item.add("")
                //画像表示、こんてんとわーにんぐ
                Item.add("false")
                Item.add("false")

                recyclerViewList!!.add(Item)
                runOnUiThread {
                    if (recyclerViewLayoutManager != null) {
                        (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                    }
                    //recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                    SnackberProgress.closeProgressSnackber()
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /**
     * Misskey users/notes
     */
    private fun loadMisskeyNote(response_string: String) {
        try {
            val jsonArray = JSONArray(response_string)
            //max_count = jsonArray.length();
            for (i in 0 until jsonArray.length()) {
                val toot_jsonObject = jsonArray.getJSONObject(i)
                //配列を作成
                val Item = ArrayList<String>()
                //メモとか通知とかに
                Item.add("CustomMenu Misskey Notes")
                //内容
                Item.add(misskeyUrl)
                //ユーザー名
                Item.add("")
                //JSONObject
                Item.add(toot_jsonObject.toString())
                //ぶーすとした？
                Item.add("false")
                //ふぁぼした？
                Item.add("false")
                //Mastodon / Misskey
                Item.add("Misskey")
                //Insatnce/AccessToken
                Item.add(Instance.toString())
                Item.add(AccessToken.toString())
                //設定ファイルJSON
                Item.add("")
                //画像表示、こんてんとわーにんぐ
                Item.add("false")
                Item.add("false")

                recyclerViewList!!.add(Item)
                runOnUiThread {
                    if (recyclerViewLayoutManager != null) {
                        (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    //recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                    SnackberProgress.closeProgressSnackber()
                    scroll = false
                }
            }
            //最後のIDを更新する
            val last_toot = jsonArray.getJSONObject(99)
            max_id = last_toot.getString("id")
            //scrollPosition += 30;


        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /**
     * Misskey Follow/Follower取得
     */
    private fun loadMisskeyFollowFollower(response_string: String) {
        try {
            val first_Object = JSONObject(response_string)
            val jsonArray = first_Object.getJSONArray("users")
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                //配列を作成
                val Item = ArrayList<String>()
                //メモとか通知とかに
                Item.add("CustomMenu Misskey Follow")
                //内容
                Item.add(mastodonUrl)
                //ユーザー名
                Item.add("")
                //JSONObject
                Item.add(jsonObject.toString())
                //ぶーすとした？
                Item.add("false")
                //ふぁぼした？
                Item.add("false")
                //Mastodon / Misskey
                Item.add("Misskey")
                //Insatnce/AccessToken
                Item.add(Instance.toString())
                Item.add(AccessToken.toString())
                //設定ファイルJSON
                Item.add("")
                //画像表示、こんてんとわーにんぐ
                Item.add("false")
                Item.add("false")

                recyclerViewList!!.add(Item)
                runOnUiThread {
                    if (recyclerViewLayoutManager != null) {
                        (recyclerViewLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, y)
                    }
                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                    //recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                    SnackberProgress.closeProgressSnackber()
                    scroll = false
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

}
