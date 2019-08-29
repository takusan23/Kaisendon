package io.github.takusan23.Kaisendon.APICall

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.util.ArrayList

class MastodonTimelineAPICall(val activity: AppCompatActivity) {

    /*
    * アカウント設定
    * */
    var accessToken = ""
    var instance = ""

    /*
    * CustomMenu設定
    * */
    var customMenuJSON = ""


    /*
    * RecyclerView
    * */
    lateinit var recyclerView: RecyclerView
    lateinit var recyclerViewLayoutManager: LinearLayoutManager

    /*
    * リスト
    * */
    lateinit var itemList: ArrayList<ArrayList<*>>

    //max_id
    var max_id = ""

    //通知フィルター
    var notificationFilter = ""
    var favourite = "fav"
    var reblog = "reblog"
    var mention = "mention"
    var follow = "follow"
    var vote = "vote"


    /*
    * max_id指定用
    * */
    fun setMaxIdParameters(url: String): String {
        val builder = HttpUrl.parse(url)?.newBuilder()
        builder?.addQueryParameter("max_id", max_id)
        return builder?.build().toString()
    }


    /*
    * タイムライン取得。max_idはnullの場合は指定なしになります。
    * */
    fun callMastodonTLAPI(url: String) {

        //limit+access_token
        val builder = HttpUrl.parse(url)?.newBuilder()
        builder?.addQueryParameter("limit", "40")
        builder?.addQueryParameter("access_token", accessToken)

        //作成
        val request = Request.Builder()
                .url(builder?.build().toString())
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread { Toast.makeText(activity, activity.getString(R.string.error), Toast.LENGTH_SHORT).show() }
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
                            timelineJSONParse(toot_jsonObject, false)
                        }
                        //最後のIDを更新する
                        val last_toot = jsonArray.getJSONObject(39)
                        max_id = last_toot.getString("id")

                        //更新
                        activity.runOnUiThread {
                            if (this@MastodonTimelineAPICall::recyclerView.isInitialized) {
                                recyclerView.adapter?.notifyDataSetChanged()
                            }
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    //失敗時
                    activity.runOnUiThread { Toast.makeText(activity, activity.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                }
            }
        })
    }


    fun loadNotification(url: String) {

        //limit+access_token
        val builder = HttpUrl.parse(url)?.newBuilder()
        builder?.addQueryParameter("limit", "40")
        builder?.addQueryParameter("access_token", accessToken)

        //作成
        val request = Request.Builder()
                .url(builder?.build().toString())
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread { Toast.makeText(activity, activity.getString(R.string.error), Toast.LENGTH_SHORT).show() }
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
                            if (notificationFilter.contains(favourite)) {
                                if (type.contains("favourite")) {
                                    notificationJSONPase(toot_text_jsonObject, false)
                                }
                            }
                            if (notificationFilter.contains(reblog)) {
                                if (type.contains("reblog")) {
                                    notificationJSONPase(toot_text_jsonObject, false)
                                }
                            }
                            if (notificationFilter.contains(mention)) {
                                if (type.contains("mention")) {
                                    notificationJSONPase(toot_text_jsonObject, false)
                                }
                            }
                            if (notificationFilter.contains(follow)) {
                                if (type.contains("follow")) {
                                    notificationJSONPase(toot_text_jsonObject, false)
                                }
                            }
                            if (notificationFilter.contains(vote)) {
                                if (type.contains("poll")) {
                                    notificationJSONPase(toot_text_jsonObject, false)
                                }
                            }
                        }
                        //最後のIDを更新する
                        val last_toot_text = jsonArray.getJSONObject(29)
                        max_id = last_toot_text.getString("id")
                        //更新
                        activity.runOnUiThread {
                            if (this@MastodonTimelineAPICall::recyclerView.isInitialized) {
                                recyclerView.adapter?.notifyDataSetChanged()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    //失敗時
                    activity.runOnUiThread { Toast.makeText(activity, activity.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                }
            }
        })
    }

    fun useStreamingAPI(url: String): WebSocketClient {

        val uri = URI(url)

        //WebSocket
        val webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                println("おーぷん")
            }

            override fun onMessage(message: String) {
                //JSONParse
                try {
                    if (!url.contains("direct") || !url.contains("notification")) {
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

                    } else if (url.contains("notification")) {
                        val jsonObject = JSONObject(message)
                        val payload = jsonObject.getString("payload")
                        val toot_text_jsonObject = JSONObject(payload)
                        val toot_text_account = toot_text_jsonObject.getJSONObject("account")
                        //Type!!!!!!!!
                        val type = toot_text_jsonObject.getString("type")
                        //振り分け
                        if (notificationFilter.contains(favourite)) {
                            if (type.contains("favourite")) {
                                notificationJSONPase(toot_text_jsonObject, false)
                            }
                        }
                        if (notificationFilter.contains(reblog)) {
                            if (type.contains("reblog")) {
                                notificationJSONPase(toot_text_jsonObject, true)
                            }
                        }
                        if (notificationFilter.contains(mention)) {
                            if (type.contains("mention")) {
                                notificationJSONPase(toot_text_jsonObject, true)
                            }
                        }
                        if (notificationFilter.contains(follow)) {
                            if (type.contains("follow")) {
                                notificationJSONPase(toot_text_jsonObject, true)
                            }
                        }
                        if (notificationFilter.contains(vote)) {
                            if (type.contains("poll")) {
                                notificationJSONPase(toot_text_jsonObject, true)
                            }
                        }
                    } else if (url.contains("notification")) {
                        //DM
                        val jsonObject = JSONObject(message)
                        val payload = jsonObject.getString("payload")
                        val toot_text_jsonObject = JSONObject(payload)
                        streamingAPIDirect(toot_text_jsonObject, true)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {

            }

            override fun onError(ex: Exception) {
                //失敗時
                activity.runOnUiThread {
                    Toast.makeText(activity, activity.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
        //接続
        webSocketClient.connect()

        return webSocketClient
    }


    fun notificationJSONPase(toot_text_jsonObject: JSONObject, streaming: Boolean) {
        //配列を作成
        val Item = ArrayList<String>()
        //メモとか通知とかに
        Item.add("")
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
        Item.add(instance)
        Item.add(accessToken)
        //設定ファイルJSON
        Item.add(customMenuJSON)
        //画像表示、こんてんとわーにんぐ
        Item.add("false")
        Item.add("false")
        //Streaming ?
        if (streaming) {
            itemList.add(0, Item)
            if (recyclerViewLayoutManager as LinearLayoutManager? != null) {
                // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                val pos = recyclerViewLayoutManager.findFirstVisibleItemPosition()
                var top = 0
                if (recyclerViewLayoutManager.childCount > 0) {
                    top = recyclerViewLayoutManager.getChildAt(0)!!.top
                }
                //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                if (streaming) {
                    //一番上にアイテムが追加されたことを通知する？
                    //notifyDataSetChanged()と違って追加時にアニメーションされる
                    recyclerView.adapter?.notifyItemInserted(0)
                } else {
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                //一番上なら追いかける
                recyclerView.post {
                    if (pos == 0) {
                        //scrollToPosition()に置き換えた。アニメーションされるようになった
                        recyclerView.scrollToPosition(0)
                    } else {
                        recyclerViewLayoutManager.scrollToPositionWithOffset(pos + 1, top)
                    }
                }
            }
        } else {
            itemList.add(Item)
            activity.runOnUiThread {
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }


    fun timelineJSONParse(toot_jsonObject: JSONObject, streaming: Boolean) {
        //配列を作成
        val Item = ArrayList<String>()
        //メモとか通知とかに
        Item.add("CustomMenu")
        //内容
        Item.add("")
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
        Item.add(instance)
        Item.add(accessToken)
        //設定ファイルJSON
        Item.add(customMenuJSON)
        //画像表示、こんてんとわーにんぐ
        Item.add("false")
        Item.add("false")
        if (streaming) {
            itemList.add(0, Item)
            if (recyclerViewLayoutManager as LinearLayoutManager? != null) {
                // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                val pos = recyclerViewLayoutManager.findFirstVisibleItemPosition()
                var top = 0
                if (recyclerViewLayoutManager.childCount > 0) {
                    top = recyclerViewLayoutManager.getChildAt(0)!!.top
                }
                recyclerView.post {
                    if (streaming) {
                        //一番上にアイテムが追加されたことを通知する？
                        //notifyDataSetChanged()と違って追加時にアニメーションされる
                        recyclerView.adapter?.notifyItemInserted(0)
                    } else {
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    //一番上なら追いかける
                    if (pos == 0) {
                        //scrollToPosition()に置き換えた。アニメーションされるようになった
                        recyclerView.scrollToPosition(0)
                    } else {
                        recyclerViewLayoutManager.scrollToPositionWithOffset(pos + 1, top)
                    }
                }
            }
        } else {
            itemList.add(Item)
            activity.runOnUiThread {
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun streamingAPIDirect(jsonObject: JSONObject, streaming: Boolean) {
        //配列を作成
        val Item = ArrayList<String>()
        //メモとか通知とかに
        Item.add("CustomMenu")
        //内容
        Item.add("")
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
        Item.add(instance)
        Item.add(accessToken)
        //設定ファイルJSON
        Item.add(customMenuJSON)
        //画像表示、こんてんとわーにんぐ
        Item.add("false")
        Item.add("false")
        //Streaming ?
        if (streaming) {
            itemList.add(0, Item)
            if (recyclerViewLayoutManager as LinearLayoutManager? != null) {
                // 画面上で最上部に表示されているビューのポジションとTopを記録しておく
                val pos = recyclerViewLayoutManager.findFirstVisibleItemPosition()
                var top = 0
                if (recyclerViewLayoutManager.childCount > 0) {
                    top = recyclerViewLayoutManager.getChildAt(0)!!.top
                }
                //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                if (streaming) {
                    //一番上にアイテムが追加されたことを通知する？
                    //notifyDataSetChanged()と違って追加時にアニメーションされる
                    recyclerView.adapter?.notifyItemInserted(0)
                } else {
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                //一番上なら追いかける
                recyclerView.post {
                    if (pos == 0) {
                        //scrollToPosition()に置き換えた。アニメーションされるようになった
                        recyclerView.scrollToPosition(0)
                    } else {
                        recyclerViewLayoutManager.scrollToPositionWithOffset(pos + 1, top)
                    }
                }
            }
        } else {
            itemList.add(Item)
            activity.runOnUiThread {
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    /**
     * RESTAPIのリンクをストリーミングAPIへ変換する
     * @param hashtagName ハッシュタグ以外はnullで構いません。
     * */
    fun restAPIURLToWebSocketURL(url: String, hashtagName: String?): String {
        //既定
        if (url.contains("/api/v1/timelines/home")) {
            return "wss://$instance/api/v1/streaming/?stream=user"
        }
        if (url.contains("/api/v1/notifications")) {
            return "wss://$instance/api/v1/streaming/?stream=user:notification"
        }
        if (url.contains("/api/v1/timelines/public?local=true")) {
            return "wss://$instance/api/v1/streaming/?stream=public:local"
        }
        if (url.contains("/api/v1/timelines/public")) {
            return "wss://$instance/api/v1/streaming/?stream=public"
        }
        if (url.contains("/api/v1/timelines/direct")) {
            return "wss://$instance/api/v1/streaming/?stream=direct&"
        }
        if (url.contains("/api/v1/timelines/tag/")) {
            return "wss://$instance/api/v1/streaming/hashtag/?hashtag=$hashtagName"
        }
        if (url.contains("/api/v1/timelines/tag/?local=true")) {
            return "wss://$instance/api/v1/streaming/hashtag/local?hashtag=$hashtagName"
        }
        return "wss://$instance/api/v1/streaming/?stream=public:local"
    }

}