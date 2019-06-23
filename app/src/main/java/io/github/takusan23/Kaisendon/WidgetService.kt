package io.github.takusan23.Kaisendon

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Html
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.concurrent.ExecutionException

class WidgetService : RemoteViewsService() {

    internal var AccessToken: String? = null
    internal var Instance: String? = null

    private var jsonArray = JSONArray()

    private var pref_setting: SharedPreferences? = null


    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return WidgetFactory()
    }

    private inner class WidgetFactory : RemoteViewsService.RemoteViewsFactory {


        override fun onCreate() {
            //Log.v(TAG, "[onCreate]");

        }

        override fun onDataSetChanged() {
            //Log.v(TAG, "[onDataSetChanged]");
            //タイムライン読み込み
            getTimeLineJson()
        }

        override fun onDestroy() {}

        override fun getViewAt(position: Int): RemoteViews {
            //Log.e(TAG, "[getViewAt]: " + position);

            //ここでListViewに追加する
            var remoteViews: RemoteViews? = null
            remoteViews = RemoteViews(applicationContext.packageName, R.layout.widget_listview_layout)
            //pref_setting = getDefaultSharedPreferences(Preference_ApplicationContext.context)
            //URL
            var toot_url: String? = null
            //画像を表示するかの判断]
            var avater_show = false
            //通信量節約
            val setting_avater_hidden = pref_setting!!.getBoolean("pref_avater", false)
            val setting_avater_wifi = pref_setting!!.getBoolean("pref_avater_wifi", true)
            //Wi-Fi接続状況確認
            val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            var networkCapabilities: NetworkCapabilities? = null
            if (connectivityManager != null) {
                networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            }
            //Wi-Fi
            if (setting_avater_wifi) {
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        avater_show = true
                    } else {
                        avater_show = false
                    }//Wi-Fi no Connection
                }
            } else {
                avater_show = false
            }


            //ここで追加
            try {
                //Htmlなんとか～はJSONの中のトゥートにHTMLタグがついてるため
                if (pref_setting!!.getString("WidgetTLType", "Home")!!.contains("Notification")) {
                    val type = jsonArray.getJSONObject(position).getString("type")
                    val content = jsonArray.getJSONObject(position).getJSONObject("status").getString("content")
                    val account = jsonArray.getJSONObject(position).getJSONObject("account").getString("acct")
                    val display_name = jsonArray.getJSONObject(position).getJSONObject("account").getString("display_name")
                    val avater_url = jsonArray.getJSONObject(position).getJSONObject("account").getString("avatar")
                    toot_url = jsonArray.getJSONObject(position).getJSONObject("status").getString("url")

                    remoteViews.setTextViewText(R.id.widget_listview_layout_textview, Html.fromHtml("$type\r\n$display_name / @$account\r\n$content", Html.FROM_HTML_MODE_COMPACT))


                    //Glideは神！！！！！！！！！！！！！！！！！！！！！！！！！！！
                    if (avater_show) {
                        try {
                            //アバター
                            val bitmap = Glide.with(applicationContext).asBitmap().load(avater_url).submit(100, 100).get()
                            remoteViews.setImageViewBitmap(R.id.widget_listview_layout_imageview, bitmap)
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }

                } else {
                    val content = jsonArray.getJSONObject(position).getString("content")
                    val account = jsonArray.getJSONObject(position).getJSONObject("account").getString("acct")
                    val display_name = jsonArray.getJSONObject(position).getJSONObject("account").getString("display_name")
                    val avater_url = jsonArray.getJSONObject(position).getJSONObject("account").getString("avatar")
                    toot_url = jsonArray.getJSONObject(position).getString("url")

                    remoteViews.setTextViewText(R.id.widget_listview_layout_textview, Html.fromHtml("$display_name / @$account\r\n$content", Html.FROM_HTML_MODE_COMPACT))

                    //Glideは神！！！！！！！！！！！！！！！！！！！！！！！！！！！
                    if (avater_show) {
                        try {
                            val bitmap = Glide.with(applicationContext).asBitmap().load(avater_url).submit(100, 100).get()
                            remoteViews.setImageViewBitmap(R.id.widget_listview_layout_imageview, bitmap)
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }
                }


                //ListViewの項目をクリックできるようにする
                val btnClickIntent = Intent(applicationContext, NewAppWidget::class.java)
                btnClickIntent.putExtra("URL", toot_url)
                btnClickIntent.putExtra("ListViewClick", true)

                remoteViews.setOnClickFillInIntent(R.id.widget_listview_layout_textview, btnClickIntent)


            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return remoteViews
        }

        override fun getItemId(position: Int): Long {
            //Log.v(TAG, "[getItemId]: " + position);

            return 0
        }

        override fun getCount(): Int {
            //Log.v(TAG, "[getCount]");

            //returnにListViewの合計数を入れる
            //今回はJSONの配列の数
            return jsonArray.length()
        }

        override fun getLoadingView(): RemoteViews? {
            //Log.v(TAG, "[getLoadingView]");

            return null
        }


        override fun getViewTypeCount(): Int {
            //Log.v(TAG, "[getViewTypeCount]");

            return 1
        }

        override fun hasStableIds(): Boolean {
            //Log.v(TAG, "[hasStableIds]");

            return true
        }


        //JSON取得
        internal fun getTimeLineJson() {
            //Log.v(TAG, "[Request]");

            //pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.context)

            val accessToken_boomelan = pref_setting?.getBoolean("pref_advanced_setting_instance_change", false)
            if (accessToken_boomelan ?: false) {
                AccessToken = pref_setting?.getString("pref_mastodon_accesstoken", "")
                Instance = pref_setting?.getString("pref_mastodon_instance", "")
            } else {
                AccessToken = pref_setting?.getString("main_token", "")
                Instance = pref_setting?.getString("main_instance", "")
            }

            //URL設定
            var url = "https://$Instance/api/v1/timelines/public/?local=true"

            if (pref_setting!!.getString("WidgetTLType", "Home")!!.contains("Home")) {
                url = "https://$Instance/api/v1/timelines/home/?access_token=$AccessToken"
            }
            if (pref_setting!!.getString("WidgetTLType", "Home")!!.contains("Local")) {
                url = "https://$Instance/api/v1/timelines/public/?local=true"
            }
            if (pref_setting!!.getString("WidgetTLType", "Home")!!.contains("Notification")) {
                url = "https://$Instance/api/v1/notifications?access_token=$AccessToken"
            }
            if (pref_setting!!.getString("WidgetTLType", "Home")!!.contains("Federated")) {
                url = "https://$Instance/api/v1/timelines/public/"
            }

            //パラメータを設定
            val builder = HttpUrl.parse(url)!!.newBuilder()
            builder.addQueryParameter("limit", "40")
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
                    val responce_string = response.body()!!.string()
                    //System.out.println(responce_string);
                    try {
                        jsonArray = JSONArray(responce_string)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            })
        }
    }
}
