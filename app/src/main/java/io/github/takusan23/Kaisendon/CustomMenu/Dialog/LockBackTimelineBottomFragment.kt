package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

class LockBackTimelineBottomFragment : BottomSheetDialogFragment() {

    private var pref_setting: SharedPreferences? = null;
    private var recyclerView: RecyclerView? = null;
    private var recyclerViewList: ArrayList<ArrayList<*>>? = null
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private var customMenuRecyclerViewAdapter: CustomMenuRecyclerViewAdapter? = null
    private var id: String = ""
    private var url: String = ""
    private var token: String = ""
    private var instance: String = ""
    private var json_data: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.timeback_timeline, null)
    }

    /*はじっこを丸くする*/
    override fun getTheme(): Int {
        var theme = R.style.BottomSheetDialogThemeAppTheme
        val darkModeSupport = DarkModeSupport(context!!)
        if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES){
            theme =  R.style.BottomSheetDialogThemeDarkTheme
        }
        return theme
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)

        recyclerView = view.findViewById(R.id.lockbackRecyclerView);

        id = arguments?.getString("status_id", "0") ?: "0"
        url = arguments?.getString("lockback_url", "") ?: ""
        instance = arguments?.getString("instance", "") ?: ""
        token = arguments?.getString("token", "") ?: ""
        json_data = arguments?.getString("setting") ?: ""

        recyclerViewList = ArrayList()
        //ここから下三行必須
        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = mLayoutManager
        customMenuRecyclerViewAdapter = CustomMenuRecyclerViewAdapter(recyclerViewList!!)
        recyclerView!!.adapter = customMenuRecyclerViewAdapter
        recyclerViewLayoutManager = recyclerView!!.layoutManager

        //ダークモード
        val darkModeSupport = DarkModeSupport(context!!)
        darkModeSupport.setLayoutAllThemeColor(view as LinearLayout)

        getBeforeTimeline()
    }

    //指定IDより前のTL取得
    private fun getBeforeTimeline() {
        if (id != "0") {
            val builder = HttpUrl.parse("https://$instance$url")!!.newBuilder()
            builder.addQueryParameter("limit", "40")
            builder.addQueryParameter("access_token", token)
            builder.addQueryParameter("since_id", id)
            callAPI(builder.build().toString(), true, false)
        }
    }

    //指定IDよりも後ろのTL取得
    private fun getAfterTimeline() {
        if (id != "0") {
            val builder = HttpUrl.parse("https://$instance$url")!!.newBuilder()
            builder.addQueryParameter("limit", "40")
            builder.addQueryParameter("access_token", token)
            builder.addQueryParameter("max_id", id)
            callAPI(builder.build().toString(), false, false)
        }
    }

    //まとめたやつ
    /**
     * @param url URLいれてね
     * @param isBefore 指定IDより前のTLを取得する場合はtrue
     * @param isOriginal 指定したIDの投稿をパースするときtrue
     * */
    private fun callAPI(url: String, isBefore: Boolean, isOriginal: Boolean) {
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity!!.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            override fun onResponse(call: Call, response: Response) {
                var response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗時
                    activity!!.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    if (!isOriginal) {
                        var jsonArray = JSONArray(response_string)
                        for (i in 0 until jsonArray.length()) {
                            val toot_jsonObject = jsonArray.getJSONObject(i)
                            if (activity != null && isAdded) {
                                //配列を作成
                                val Item = ArrayList<String>()
                                //メモとか通知とかに
                                Item.add("CustomMenu Local")
                                //内容
                                Item.add(CustomMenuTimeLine.url!!)
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
                                Item.add(instance!!)
                                Item.add(token!!)
                                //設定ファイルJSON
                                Item.add(json_data!!)
                                //画像表示、こんてんとわーにんぐ
                                Item.add("false")
                                Item.add("false")
                                recyclerViewList!!.add(Item)
                            }
                        }
                    } else {
                        val toot_jsonObject = JSONObject(response_string)
                        if (activity != null && isAdded) {
                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add("CustomMenu Local")
                            //内容
                            Item.add(CustomMenuTimeLine.url!!)
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
                            Item.add(instance!!)
                            Item.add(token!!)
                            //設定ファイルJSON
                            Item.add(json_data!!)
                            //画像表示、こんてんとわーにんぐ
                            Item.add("false")
                            Item.add("false")
                            recyclerViewList!!.add(Item)
                        }
                    }
                    //RecyclerView更新（notifyItemInserted使うべき？）
                    activity!!.runOnUiThread {
                        customMenuRecyclerViewAdapter!!.notifyDataSetChanged()
                    }
                    //前の投稿取得→元トゥート取得→後ろの投稿取得
                    if (isBefore && !isOriginal) {
                        getOriginalToot()
                    } else if (!isBefore && isOriginal) {
                        getAfterTimeline()
                    }
                }
            }
        })
    }

    //前後だけだと自身のトゥートがでないので
    fun getOriginalToot() {
        val builder = HttpUrl.parse("https://${instance}/api/v1/statuses/${id}")!!.newBuilder()
        callAPI(builder.build().toString(), false, true)
    }
}

