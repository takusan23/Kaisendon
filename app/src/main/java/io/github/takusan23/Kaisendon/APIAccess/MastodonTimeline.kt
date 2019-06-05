package io.github.takusan23.Kaisendon.APIAccess

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SnackberProgress
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.*

class MastodonTimeline(url: String, instance: String, access_token: String, recyclerViewList: ArrayList<ArrayList<*>>, customMenuRecyclerViewAdapter: CustomMenuRecyclerViewAdapter, recyclerView: RecyclerView) {
    private val context: Context
    private val pref_setting: SharedPreferences

    init {
        context = recyclerView.context
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        getMastodonTimeline(url, instance, access_token, recyclerViewList, customMenuRecyclerViewAdapter, recyclerView)
    }

    fun getMastodonTimeline(api_url: String, instance: String, access_token: String, recyclerViewList: ArrayList<ArrayList<*>>, customMenuRecyclerViewAdapter: CustomMenuRecyclerViewAdapter, recyclerView: RecyclerView) {
        val handler = Handler(Looper.getMainLooper())
        //パラメータを設定
        val builder = HttpUrl.parse(api_url)!!.newBuilder()
        builder.addQueryParameter("limit", "40")
        builder.addQueryParameter("access_token", access_token)
        val max_id_final_url = builder.build().toString()

        //作成
        val request = Request.Builder()
                .url(max_id_final_url)
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //成功時
                if (response.isSuccessful) {
                    val response_string = response.body()!!.string()
                    var jsonArray: JSONArray? = null
                    try {
                        jsonArray = JSONArray(response_string)
                        for (i in 0 until jsonArray.length()) {
                            val toot_jsonObject = jsonArray.getJSONObject(i)
                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add("CustomMenu Local")
                            //内容
                            Item.add("")
                            //ユーザー名
                            Item.add("")
                            //JSONObject
                            Item.add(toot_jsonObject.toString())
                            //ぶーすとした？
                            Item.add("false")
                            //ふぁぼした？
                            Item.add("false")

                            //ListItem listItem = new ListItem(Item);
                            recyclerViewList.add(Item)

                            handler.post {
                                val recyclerViewLayoutManager = recyclerView.layoutManager
                                //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                                recyclerView.adapter = customMenuRecyclerViewAdapter
                                SnackberProgress.closeProgressSnackber()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    //失敗時
                    handler.post { Toast.makeText(context, R.string.error.toString() + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                }
            }
        })

    }

}
