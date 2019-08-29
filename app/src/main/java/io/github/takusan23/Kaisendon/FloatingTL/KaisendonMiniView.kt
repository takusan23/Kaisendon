package io.github.takusan23.Kaisendon.FloatingTL

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.github.takusan23.Kaisendon.APICall.MastodonTimelineAPICall
import io.github.takusan23.Kaisendon.APIJSONParse.CustomMenuJSONParse
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.Fragment.HelloFragment
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SnackberProgress
import kotlinx.android.synthetic.main.overlay_player_layout.view.*
import okhttp3.*
import org.java_websocket.client.WebSocketClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList

class KaisendonMiniView(val context: Context, val jsonString: String) {

    var popupView: View = (context as Home).popupView
    val activity = (context as AppCompatActivity)
    //RecyclerView
    val recyclerViewList: ArrayList<ArrayList<*>>? = arrayListOf()
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private var customMenuRecyclerViewAdapter: KaisendonMiniRecyclerViewAdapter? = null

    val customMenuJSONParse = CustomMenuJSONParse(jsonString)

    val misskey = customMenuJSONParse.misskey
    val isMisskeyMode = java.lang.Boolean.valueOf(misskey)
    var content = customMenuJSONParse.content
    val instance = customMenuJSONParse.instance
    val access_token = customMenuJSONParse.access_token
    val isReadOnly = customMenuJSONParse.isReadOnly.toBoolean()

    lateinit var webSocket: WebSocketClient

    //かいせんどんミニ表示
    fun showKaisendonMini() {
        val windowManager =
                (context as AppCompatActivity).windowManager
        val width = 600
        val height = 800
        //レイアウト読み込み
        val layoutInflater = LayoutInflater.from(context)

        // オーバーレイViewの設定をする
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                    width,
                    height,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                    width,
                    height,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
            )
        }
        //popupView = layoutInflater.inflate(R.layout.overlay_player_layout, null)
        //表示
        windowManager.addView(popupView, params)

        //移動
        //画面サイズ
        val displaySize: Point by lazy {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            size
        }
        //https://qiita.com/farman0629/items/ce547821dd2e16e4399e
        popupView.kaisendon_mini_drag_button.setOnLongClickListener {
            //長押し判定
            popupView.kaisendon_mini_drag_button.setOnTouchListener { view, motionEvent ->
                // タップした位置を取得する
                val x = motionEvent.rawX.toInt()
                val y = motionEvent.rawY.toInt()

                when (motionEvent.action) {
                    // Viewを移動させてるときに呼ばれる
                    MotionEvent.ACTION_MOVE -> {
                        // 中心からの座標を計算する
                        val centerX = x - (displaySize.x / 2)
                        val centerY = y - (displaySize.y / 2)

                        // オーバーレイ表示領域の座標を移動させる
                        params.x = centerX
                        params.y = centerY

                        // 移動した分を更新する
                        windowManager.updateViewLayout(popupView, params)
                    }
                }
                false
            }
            true//OnclickListener呼ばないようにtrue
        }

        //閉じるボタン
        popupView.kaisendon_mini_close_button.setOnClickListener {

            windowManager.removeView(popupView)

            if (this@KaisendonMiniView::webSocket.isInitialized) {
                //初期化済みなら閉じる
                webSocket.close()
            }
        }

        //ここから下三行必須
        popupView.kaisendon_mini_recyclerview.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        popupView.kaisendon_mini_recyclerview.layoutManager = mLayoutManager
        customMenuRecyclerViewAdapter = KaisendonMiniRecyclerViewAdapter(recyclerViewList!!)
        popupView.kaisendon_mini_recyclerview.adapter = customMenuRecyclerViewAdapter
        recyclerViewLayoutManager = popupView.kaisendon_mini_recyclerview.layoutManager

        //RecyclerViewに区切り線引く
        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        popupView.kaisendon_mini_recyclerview.addItemDecoration(itemDecoration)

        //MastodonのAPI叩く
        val mastodonTimelineAPICall = MastodonTimelineAPICall(activity)
        //設定
        mastodonTimelineAPICall.itemList = recyclerViewList
        mastodonTimelineAPICall.recyclerView = popupView.kaisendon_mini_recyclerview
        mastodonTimelineAPICall.recyclerViewLayoutManager = recyclerViewLayoutManager as LinearLayoutManager
        mastodonTimelineAPICall.accessToken = access_token
        mastodonTimelineAPICall.instance = instance
        mastodonTimelineAPICall.customMenuJSON = customMenuJSONParse.json_data

        //投稿
        popupView.kaisendon_mini_post_button.setOnClickListener {
            mastodonStatusPOST()
        }
        //更新
        val url = "https://$instance/$content"
        popupView.kaisendon_mini_update_button.setOnClickListener {
            recyclerViewList.clear()
            //普通のAPIも叩く
            mastodonTimelineAPICall.callMastodonTLAPI(url)
        }
        //ストリーミングAPI
        popupView.kaisendon_mini_streaming_button.setOnClickListener {
            //StreamingAPI
            if (this@KaisendonMiniView::webSocket.isInitialized) {
                connectionStreaming(mastodonTimelineAPICall, url)
            } else {
                if (webSocket.isClosed) {
                    disconnectStreaming()
                } else {
                    connectionStreaming(mastodonTimelineAPICall, url)
                }
            }
        }
    }

    //ストリーミング接続
    fun connectionStreaming(mastodonTimelineAPICall: MastodonTimelineAPICall, url: String) {
        Toast.makeText(context, context.getString(R.string.streaming_connect), Toast.LENGTH_SHORT).show()
        val streamingLink = mastodonTimelineAPICall.restAPIURLToWebSocketURL(url, null)
        webSocket = mastodonTimelineAPICall.useStreamingAPI(streamingLink)
    }

    //ストリーミング切断
    fun disconnectStreaming() {
        Toast.makeText(context, context.getString(R.string.streaming_disconnect), Toast.LENGTH_SHORT).show()
        webSocket.close()
    }

    /*MastodonStatusPOST*/
    private fun mastodonStatusPOST() {
        try {
            val url = "https://$instance/api/v1/statuses/?access_token=$access_token"
            val postJsonObject = JSONObject()
            postJsonObject.put("status", popupView.kaisendon_mini_edittext.text.toString())
            val requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postJsonObject.toString())
            val request = Request.Builder()
                    .url(url)
                    .post(requestBody_json)
                    .build()
            //POST
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    activity.runOnUiThread { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        //失敗
                        activity.runOnUiThread { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(context, context.getString(R.string.toot_ok), Toast.LENGTH_SHORT).show()
                            popupView.kaisendon_mini_edittext.setText("")
                        }
                    }
                }
            })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


}