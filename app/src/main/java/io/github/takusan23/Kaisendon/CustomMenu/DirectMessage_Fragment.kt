package io.github.takusan23.Kaisendon.CustomMenu

import android.app.ProgressDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.sys1yagi.mastodon4j.api.Shutdownable
import com.sys1yagi.mastodon4j.api.entity.Attachment
import io.github.takusan23.Kaisendon.HomeTimeLineAdapter
import io.github.takusan23.Kaisendon.ListItem
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DirectMessage_Fragment : Fragment() {

    internal var toot_text: String? = null
    internal var user: String? = null
    internal var user_name: String? = null
    internal var user_use_client: String? = null
    internal var toot_id: Long = 0
    internal var toot_id_string: String? = null
    internal var user_avater_url: String? = null
    internal var toot_boost: String? = null
    internal var toot_time: String? = null
    internal var media_url: String? = null
    internal var account_id: Long = 0
    private val dialog: ProgressDialog? = null
    internal lateinit var view: View

    internal var max_id: String? = null
    internal var min_id: String? = null

    //メディア
    internal var media_url_1: String? = null
    internal var media_url_2: String? = null
    internal var media_url_3: String? = null
    internal var media_url_4: String? = null

    internal var count = 0

    internal var scrollPosition = 0

    internal var shutdownable: Shutdownable? = null

    internal var position: Int = 0
    internal var y: Int = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        view = inflater.inflate(R.layout.activity_home_timeline, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //設定のプリファレンス
        val pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)

        //UIスレッド
        val handler_1 = android.os.Handler()

        val toot_list = ArrayList<ListItem>()

        val adapter = HomeTimeLineAdapter(context!!, R.layout.timeline_item, toot_list)

        //アクセストークンを変更してる場合のコード
        //アクセストークン
        var AccessToken: String? = null

        //インスタンス
        var Instance: String? = null

        //getView().setBackgroundColor(Color.parseColor("#E687CEEB"));

        val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
            Instance = pref_setting.getString("pref_mastodon_instance", "")

        } else {

            AccessToken = pref_setting.getString("main_token", "")
            Instance = pref_setting.getString("main_instance", "")

        }

        activity!!.setTitle(R.string.direct_message)


        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)) {
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }


        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)

        //背景
        val background_imageView = view.findViewById<ImageView>(R.id.hometimeline_background_imageview)

        if (pref_setting.getBoolean("background_image", true)) {
            val uri = Uri.parse(pref_setting.getString("background_image_path", ""))
            Glide.with(context!!)
                    .load(uri)
                    .into(background_imageView)
        }


        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f).toDouble() != 0.0) {
            background_imageView.alpha = pref_setting.getFloat("transparency", 1.0f)
        }


        val snackbar = Snackbar.make(view, getString(R.string.loading_direct_message) + "\r\n /api/v1/timelines/direct", Snackbar.LENGTH_INDEFINITE)
        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 2
        //複数行対応させたおかげでずれたので修正
        val progressBar = ProgressBar(context)
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBer_layoutParams
        snackBer_viewGrop.addView(progressBar, 0)
        snackbar.show()


        val finalAccessToken = AccessToken

        val finalInstance = Instance

        val url = "https://$finalInstance/api/v1/timelines/direct/?access_token=$finalAccessToken"
        //パラメータを設定
        val builder = HttpUrl.parse(url)!!.newBuilder()
        builder.addQueryParameter("limit", "40")
        val final_url = builder.build().toString()
        //作成
        val max_id_request = Request.Builder()
                .url(final_url)
                .get()
                .build()

        //GETリクエスト
        val max_id_client = OkHttpClient()
        max_id_client.newCall(max_id_request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                var jsonArray: JSONArray? = null
                try {
                    jsonArray = JSONArray(response_string)
                    for (i in 0 until jsonArray.length()) {
                        val toot_jsonObject = jsonArray.getJSONObject(i)
                        val toot_account = toot_jsonObject.getJSONObject("account")
                        val media_array = toot_jsonObject.getJSONArray("media_attachments")
                        toot_text = toot_jsonObject.getString("content")
                        user = toot_account.getString("username")
                        user_name = toot_account.getString("display_name")
                        toot_time = toot_jsonObject.getString("created_at")

                        //クライアント名がある？ない？
                        try {
                            val application = toot_jsonObject.getJSONObject("application")
                            user_use_client = application.getString("name")
                        } catch (e: JSONException) {
                            user_use_client = toot_jsonObject.getString("application")
                        }

                        //                       user_use_client = status.getApplication().getName();
                        //toot_id = toot_jsonObject.getString("id");
                        toot_id_string = toot_jsonObject.getString("id")

                        user_avater_url = toot_account.getString("avatar")

                        account_id = toot_account.getInt("id").toLong()

                        val attachment = listOf(Attachment())


                        val medias = arrayOfNulls<String>(1)

                        val media_url = arrayOf<String>()

                        if (!media_array.isNull(0)) {
                            media_url_1 = media_array.getJSONObject(0).getString("url")
                        }
                        if (!media_array.isNull(1)) {
                            media_url_2 = media_array.getJSONObject(1).getString("url")
                        }
                        if (!media_array.isNull(2)) {
                            media_url_3 = media_array.getJSONObject(2).getString("url")
                        }
                        if (!media_array.isNull(3)) {
                            media_url_4 = media_array.getJSONObject(3).getString("url")
                        }

                        //System.out.println("これかあ！ ： " + media_url_1 + " / " + media_url_2  + " / " + media_url_3 + " / " + media_url_4);
                        val japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false)
                        if (japan_timeSetting) {
                            //時差計算？
                            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                            //日本用フォーマット
                            val japanDateFormat = SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!, Locale.JAPAN)
                            try {
                                val date = simpleDateFormat.parse(toot_jsonObject.getString("created_at"))
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
                            toot_time = toot_jsonObject.getString("created_at")
                        }

                        //絵文字
                        if (pref_setting.getBoolean("pref_custom_emoji", true)) {
                            val emoji = toot_jsonObject.getJSONArray("emojis")
                            for (e in 0 until emoji.length()) {
                                val jsonObject = emoji.getJSONObject(e)
                                val emoji_name = jsonObject.getString("shortcode")
                                val emoji_url = jsonObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                            }

                            //アバター絵文字
                            val avater_emoji = toot_jsonObject.getJSONArray("profile_emojis")
                            for (a in 0 until avater_emoji.length()) {
                                val jsonObject = avater_emoji.getJSONObject(a)
                                val emoji_name = jsonObject.getString("shortcode")
                                val emoji_url = jsonObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                            }

                            //ユーザーネームの方の絵文字
                            val account_emoji = toot_account.getJSONArray("emojis")
                            for (e in 0 until account_emoji.length()) {
                                val jsonObject = account_emoji.getJSONObject(e)
                                val emoji_name = jsonObject.getString("shortcode")
                                val emoji_url = jsonObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                user_name = user_name!!.replace(":$emoji_name:", custom_emoji_src)
                            }

                            //ユーザーネームの方のアバター絵文字
                            val account_avater_emoji = toot_account.getJSONArray("profile_emojis")
                            for (a in 0 until account_avater_emoji.length()) {
                                val jsonObject = account_avater_emoji.getJSONObject(a)
                                val emoji_name = jsonObject.getString("shortcode")
                                val emoji_url = jsonObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                user_name = user_name!!.replace(":$emoji_name:", custom_emoji_src)
                            }
                        }

                        //カード情報
                        var cardTitle: String? = null
                        var cardURL: String? = null
                        var cardDescription: String? = null
                        var cardImage: String? = null

                        if (!toot_jsonObject.isNull("card")) {
                            val cardObject = toot_jsonObject.getJSONObject("card")
                            cardURL = cardObject.getString("url")
                            cardTitle = cardObject.getString("title")
                            cardDescription = cardObject.getString("description")
                            cardImage = cardObject.getString("image")
                        }

                        //配列を作成
                        val Item = ArrayList<String>()
                        //メモとか通知とかに
                        Item.add("")
                        //内容
                        Item.add(toot_text!!)
                        //ユーザー名
                        Item.add("$user_name @$user")
                        //時間、クライアント名等
                        Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time)
                        //Toot ID 文字列版
                        Item.add(toot_id_string!!)
                        //アバターURL
                        Item.add(user_avater_url!!)
                        //アカウントID
                        Item.add(account_id.toString())
                        //ユーザーネーム
                        Item.add(user!!)
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

                        if (activity != null) {
                            val listItem = ListItem(Item)

                            activity!!.runOnUiThread {
                                adapter.add(listItem)
                                adapter.notifyDataSetChanged()
                                val listView = view.findViewById<View>(R.id.home_timeline) as ListView
                                listView.adapter = adapter
                                snackbar.dismiss()
                                //maxid_snackbar.dismiss();
                                //listView.setSelection(scrollPosition);
                            }
                        }

                        media_url_1 = null
                        media_url_2 = null
                        media_url_3 = null
                        media_url_4 = null


                    }
                    //最後のIDを更新する
                    val last_toot = jsonArray.getJSONObject(39)
                    max_id = last_toot.getString("id")

                    //更新用に最初のIDを控える
                    val first_toot = jsonArray.getJSONObject(0)
                    min_id = first_toot.getString("id")

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })


        /*
        //非同期通信
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();

                Timelines timelines = new Timelines(client);
                Range range = new Range(null, null, 40);

                ArrayList<ListItem> toot_list = new ArrayList<>();

                try {
                    Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statuses = timelines.getHome(range)
                            .execute();
                    statuses.getPart().forEach(status -> {



*/
        /*
                        System.out.println("=============");
                        System.out.println(status.getAccount().getDisplayName());
                        System.out.println(status.getContent());
                        System.out.println(status.isReblogged());
*//*



                        toot_text = status.getContent();
                        user = status.getAccount().getAcct();
                        user_name = status.getAccount().getDisplayName();
                        try {
                            user_use_client = status.getApplication().getName();
                        } catch (NullPointerException e) {
                            user_use_client = null;
                        }
                        //toot_time = status.getCreatedAt();

                        toot_id = status.getId();
                        toot_id_string = String.valueOf(toot_id);

                        account_id = status.getAccount().getId();

                        user_avater_url = status.getAccount().getAvatar();

                        String[] medias = {null, null, null, null};
*/
        /*
                        medias[0] = "https://www.google.co.jp/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";
                        medias[1] = "https://pbs.twimg.com/profile_images/1026695015140012032/ZT1or2k5_400x400.jpg";
                        medias[2] = "https://pbs.twimg.com/media/Djmc9cJUcAEoNJ-.jpg";
                        medias[3] = "https://pbs.twimg.com/tweet_video_thumb/Di6kwc8U4AEOXtf.jpg";
*//*


                        final String[] media_url = {null};
                        //めでぃあ
                        List<Attachment> list = status.getMediaAttachments();
                        list.forEach(media -> {

                            if (media_url_1 != null) {
                                media_url_1 = media.getUrl();
                            } else if (media_url_2 != null) {
                                media_url_2 = media.getUrl();
                            } else if (media_url_3 != null) {
                                media_url_3 = media.getUrl();
                            } else if (media_url_4 != null) {
                                media_url_4 = media.getUrl();
                            }

                            int i = 0;
                            //medias[i] = media.getUrl();
                            i++;
                            media_url[0] = media.getUrl();
                        });
                        System.out.println("画像リンク : " + media_url_1);
                        System.out.println("画像リンク : " + media_url_2);
                        System.out.println("画像リンク : " + media_url_3);
                        System.out.println("画像リンク : " + media_url_4);

                        boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                        if (japan_timeSetting) {
                            //時差計算？
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                            //日本用フォーマット
                            SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                            try {
                                Date date = simpleDateFormat.parse(status.getCreatedAt());
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                //9時間足して日本時間へ
                                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                toot_time = japanDateFormat.format(calendar.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            toot_time = status.getCreatedAt();
                        }


                        //ListItem listItem = new ListItem(null, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, medias);

                        //通知が行くように
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //adapter.add(listItem);
                                adapter.notifyDataSetChanged();
                            }
                        });

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                ListView listView = (ListView) view.findViewById(R.id.home_timeline);

                                listView.setAdapter(adapter);

                            }
                        });

                    });

                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String result) {

                //くるくるを終了
                //dialog.dismiss();
                snackbar.dismiss();

                HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

                //これを書かないとListViewがエラーNullだよって言うから書こうね
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }


                return;
            }
        };
*/


        //引っ張って更新するやつ
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#BBDEFB"), Color.parseColor("#90CAF9"), Color.parseColor("#42A5F5"), Color.parseColor("#1565C0"))
        swipeRefreshLayout.setOnRefreshListener {
            adapter.clear()
            snackbar.show()

            //最後のトゥートIDを持ってくる
            //もういい！okhttpで実装する！！
            val max_id_url = "https://$finalInstance/api/v1/timelines/direct/?access_token=$finalAccessToken"
            //パラメータを設定
            val max_id_builder = HttpUrl.parse(max_id_url)!!.newBuilder()
            max_id_builder.addQueryParameter("limit", "40")
            //max_id_builder.addQueryParameter("since_id", min_id);
            val max_id_final_url = max_id_builder.build().toString()

            //作成
            val max_id_request = Request.Builder()
                    .url(max_id_final_url)
                    .get()
                    .build()

            //GETリクエスト
            val max_id_client = OkHttpClient()
            max_id_client.newCall(max_id_request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val response_string = response.body()!!.string()
                    var jsonArray: JSONArray? = null
                    try {
                        jsonArray = JSONArray(response_string)
                        for (i in 0 until jsonArray!!.length()) {
                            val toot_jsonObject = jsonArray!!.getJSONObject(i)
                            val toot_account = toot_jsonObject.getJSONObject("account")
                            toot_text = toot_jsonObject.getString("content")
                            user = toot_account.getString("username")
                            user_name = toot_account.getString("display_name")
                            toot_time = toot_jsonObject.getString("created_at")

                            //クライアント名がある？ない？
                            try {
                                val application = toot_jsonObject.getJSONObject("application")
                                user_use_client = application.getString("name")
                            } catch (e: JSONException) {
                                user_use_client = toot_jsonObject.getString("application")
                            }

                            //                       user_use_client = status.getApplication().getName();
                            //toot_id = toot_jsonObject.getString("id");
                            toot_id_string = toot_jsonObject.getString("id")

                            user_avater_url = toot_account.getString("avatar")

                            account_id = toot_account.getInt("id").toLong()

                            val attachment = listOf(Attachment())


                            val medias = arrayOfNulls<String>(1)

                            val media_url = arrayOf<String>()

                            val media_array = toot_jsonObject.getJSONArray("media_attachments")
                            if (!media_array.isNull(0)) {
                                media_url_1 = media_array.getJSONObject(0).getString("url")
                            }
                            if (!media_array.isNull(1)) {
                                media_url_2 = media_array.getJSONObject(1).getString("url")
                            }
                            if (!media_array.isNull(2)) {
                                media_url_3 = media_array.getJSONObject(2).getString("url")
                            }
                            if (!media_array.isNull(3)) {
                                media_url_4 = media_array.getJSONObject(3).getString("url")
                            }

                            //System.out.println("これかあ！ ： " + media_url_1 + " / " + media_url_2  + " / " + media_url_3 + " / " + media_url_4);
                            val japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false)
                            if (japan_timeSetting) {
                                //時差計算？
                                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                //日本用フォーマット
                                val japanDateFormat = SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!, Locale.JAPAN)
                                try {
                                    val date = simpleDateFormat.parse(toot_jsonObject.getString("created_at"))
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
                                toot_time = toot_jsonObject.getString("created_at")
                            }

                            //絵文字
                            if (pref_setting.getBoolean("pref_custom_emoji", true)) {
                                val emoji = toot_jsonObject.getJSONArray("emojis")
                                for (e in 0 until emoji.length()) {
                                    val jsonObject = emoji.getJSONObject(e)
                                    val emoji_name = jsonObject.getString("shortcode")
                                    val emoji_url = jsonObject.getString("url")
                                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                    toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                                }

                                //アバター絵文字
                                val avater_emoji = toot_jsonObject.getJSONArray("profile_emojis")
                                for (a in 0 until avater_emoji.length()) {
                                    val jsonObject = avater_emoji.getJSONObject(a)
                                    val emoji_name = jsonObject.getString("shortcode")
                                    val emoji_url = jsonObject.getString("url")
                                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                    toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                                }

                                //ユーザーネームの方の絵文字
                                val account_emoji = toot_account.getJSONArray("emojis")
                                for (e in 0 until account_emoji.length()) {
                                    val jsonObject = account_emoji.getJSONObject(e)
                                    val emoji_name = jsonObject.getString("shortcode")
                                    val emoji_url = jsonObject.getString("url")
                                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                    user_name = user_name!!.replace(":$emoji_name:", custom_emoji_src)
                                }

                                //ユーザーネームの方のアバター絵文字
                                val account_avater_emoji = toot_account.getJSONArray("profile_emojis")
                                for (a in 0 until account_avater_emoji.length()) {
                                    val jsonObject = account_avater_emoji.getJSONObject(a)
                                    val emoji_name = jsonObject.getString("shortcode")
                                    val emoji_url = jsonObject.getString("url")
                                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                    user_name = user_name!!.replace(":$emoji_name:", custom_emoji_src)
                                }
                            }

                            //カード情報
                            var cardTitle: String? = null
                            var cardURL: String? = null
                            var cardDescription: String? = null
                            var cardImage: String? = null

                            if (!toot_jsonObject.isNull("card")) {
                                val cardObject = toot_jsonObject.getJSONObject("card")
                                cardURL = cardObject.getString("url")
                                cardTitle = cardObject.getString("title")
                                cardDescription = cardObject.getString("description")
                                cardImage = cardObject.getString("image")
                            }

                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add("")
                            //内容
                            Item.add(toot_text!!)
                            //ユーザー名
                            Item.add("$user_name @$user")
                            //時間、クライアント名等
                            Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time)
                            //Toot ID 文字列版
                            Item.add(toot_id_string!!)
                            //アバターURL
                            Item.add(user_avater_url!!)
                            //アカウントID
                            Item.add(account_id.toString())
                            //ユーザーネーム
                            Item.add(user!!)
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


                            if (activity != null) {
                                val listItem = ListItem(Item)

                                activity!!.runOnUiThread {
                                    adapter.add(listItem)
                                    adapter.notifyDataSetChanged()
                                    val listView = view.findViewById<View>(R.id.home_timeline) as ListView
                                    listView.adapter = adapter
                                    snackbar.dismiss()
                                    //listView.setSelection(scrollPosition);
                                }
                            }

                            media_url_1 = null
                            media_url_2 = null
                            media_url_3 = null
                            media_url_4 = null

                        }

                        //最後のIDを更新する
                        val last_toot = jsonArray!!.getJSONObject(39)
                        max_id = last_toot.getString("id")


                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            })


            /*
                AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... string) {

                        snackbar.show();

                        MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                                .accessToken(finalAccessToken)
                                .build();

                        Timelines timelines = new Timelines(client);

                        //limitに40を指定して40件取得できるように
                        Range range = new Range(null, null, 40);

                        ArrayList<ListItem> toot_list = new ArrayList<>();


                        try {
                            Pageable<com.sys1yagi.mastodon4j.api.entity.Status> statuses = timelines.getHome(range)
                                    .execute();
                            statuses.getPart().forEach(status -> {

*/
            /*

                                System.out.println("=============");
                                System.out.println(status.getAccount().getDisplayName());
                                System.out.println(status.getContent());
                                System.out.println(status.isReblogged());

*//*


                                toot_text = status.getContent();
                                user = status.getAccount().getUserName();
                                user_name = status.getAccount().getDisplayName();
                                //toot_time = status.getCreatedAt();

                                //                       user_use_client = status.getApplication().getName();
                                toot_id = status.getId();
                                toot_id_string = String.valueOf(toot_id);

                                user_avater_url = status.getAccount().getAvatar();

                                account_id = status.getAccount().getId();

                                List<Attachment> attachment = Collections.singletonList(new Attachment());


                                final String[] medias = new String[1];

                                final String[] media_url = {null};
                                //めでぃあ
                                List<Attachment> list = status.getMediaAttachments();
                                list.forEach(media -> {

                                    media_url[0] = media.getUrl();

                                });

                                boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                                if (japan_timeSetting) {
                                    //時差計算？
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                    //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                    //日本用フォーマット
                                    SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                    try {
                                        Date date = simpleDateFormat.parse(status.getCreatedAt());
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(date);
                                        //9時間足して日本時間へ
                                        calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                        //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                        toot_time = japanDateFormat.format(calendar.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    toot_time = status.getCreatedAt();
                                }

                                //ListItem listItem = new ListItem(null, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, null);

                                //メディア用
                                ListMediaItem listMediaItem = new ListMediaItem(media_url[0], media_url[0], media_url[0], media_url[0]);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                        listView.setAdapter(adapter);
                                    }
                                });

                            });
                        } catch (Mastodon4jRequestException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    protected void onPostExecute(String result) {

                        //くるくるを終了
                        //dialog.dismiss();
                        snackbar.dismiss();

*/
            /*
                        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

                        //これを書かないとListViewがエラーNullだよって言うから書こうね
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
*//*


                        return;
                    }
                };
*/

            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }
        }


        val listView = view.findViewById<View>(R.id.home_timeline) as ListView
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {

            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if (totalItemCount != 0 && totalItemCount == firstVisibleItem + visibleItemCount) {
                    position = listView.firstVisiblePosition
                    y = listView.getChildAt(0).top

                    val snackbar_ = Snackbar.make(view, R.string.add_loading, Snackbar.LENGTH_LONG)
                    snackbar_.show()
                    if (snackbar_.isShown) {
                        println("最後だよ")

                        //scrollPosition = scrollPosition + 40;


                        //最後のトゥートIDを持ってくる
                        //もういい！okhttpで実装する！！
                        val url = "https://$finalInstance/api/v1/timelines/direct/?access_token=$finalAccessToken"
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
                        val client_1 = OkHttpClient()
                        client_1.newCall(request).enqueue(object : Callback {

                            override fun onFailure(call: Call, e: IOException) {

                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response: Response) {
                                val response_string = response.body()!!.string()
                                var jsonArray: JSONArray? = null
                                try {
                                    jsonArray = JSONArray(response_string)
                                    val last_toot_jsonObject = jsonArray!!.getJSONObject(39)
                                    max_id = last_toot_jsonObject.getString("id")
                                    //                                    System.out.println("最後" + max_id);
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }

                            }
                        })

                        if (max_id != null) {

                            //SnackBer表示
                            val maxid_snackbar = Snackbar.make(view, getString(R.string.loading_direct_message) + "\r\n /api/v1/timelines/direct \r\n max_id=" + max_id, Snackbar.LENGTH_INDEFINITE)
                            val snackBer_viewGrop = maxid_snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
                            //SnackBerを複数行対応させる
                            val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
                            snackBer_textView.maxLines = 3
                            //複数行対応させたおかげでずれたので修正
                            val progressBar = ProgressBar(context)
                            val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            progressBer_layoutParams.gravity = Gravity.CENTER
                            progressBar.layoutParams = progressBer_layoutParams
                            snackBer_viewGrop.addView(progressBar, 0)
                            maxid_snackbar.show()


                            //最後のトゥートIDを持ってくる
                            //もういい！okhttpで実装する！！
                            val max_id_url = "https://$finalInstance/api/v1/timelines/direct/?access_token=$finalAccessToken"
                            //パラメータを設定
                            val max_id_builder = HttpUrl.parse(max_id_url)!!.newBuilder()
                            max_id_builder.addQueryParameter("limit", "40")
                            max_id_builder.addQueryParameter("max_id", max_id)
                            val max_id_final_url = max_id_builder.build().toString()

                            //作成
                            val max_id_request = Request.Builder()
                                    .url(max_id_final_url)
                                    .get()
                                    .build()

                            //GETリクエスト
                            val max_id_client = OkHttpClient()
                            max_id_client.newCall(max_id_request).enqueue(object : Callback {

                                override fun onFailure(call: Call, e: IOException) {

                                }

                                @Throws(IOException::class)
                                override fun onResponse(call: Call, response: Response) {
                                    val response_string = response.body()!!.string()
                                    var jsonArray: JSONArray? = null
                                    try {
                                        jsonArray = JSONArray(response_string)
                                        for (i in 0 until jsonArray!!.length()) {
                                            val toot_jsonObject = jsonArray!!.getJSONObject(i)
                                            val toot_account = toot_jsonObject.getJSONObject("account")
                                            toot_text = toot_jsonObject.getString("content")
                                            user = toot_account.getString("username")
                                            user_name = toot_account.getString("display_name")
                                            toot_time = toot_jsonObject.getString("created_at")

                                            //クライアント名がある？ない？
                                            try {
                                                val application = toot_jsonObject.getJSONObject("application")
                                                user_use_client = application.getString("name")
                                            } catch (e: JSONException) {
                                                user_use_client = toot_jsonObject.getString("application")
                                            }

                                            //                       user_use_client = status.getApplication().getName();
                                            //toot_id = toot_jsonObject.getString("id");
                                            toot_id_string = toot_jsonObject.getString("id")

                                            user_avater_url = toot_account.getString("avatar")

                                            account_id = toot_account.getInt("id").toLong()

                                            val attachment = listOf(Attachment())


                                            val medias = arrayOfNulls<String>(1)

                                            val media_url = arrayOf<String>()

                                            val media_array = toot_jsonObject.getJSONArray("media_attachments")
                                            if (!media_array.isNull(0)) {
                                                media_url_1 = media_array.getJSONObject(0).getString("url")
                                            }
                                            if (!media_array.isNull(1)) {
                                                media_url_2 = media_array.getJSONObject(1).getString("url")
                                            }
                                            if (!media_array.isNull(2)) {
                                                media_url_3 = media_array.getJSONObject(2).getString("url")
                                            }
                                            if (!media_array.isNull(3)) {
                                                media_url_4 = media_array.getJSONObject(3).getString("url")
                                            }

                                            //絵文字
                                            if (pref_setting.getBoolean("pref_custom_emoji", true)) {
                                                val emoji = toot_jsonObject.getJSONArray("emojis")
                                                for (e in 0 until emoji.length()) {
                                                    val jsonObject = emoji.getJSONObject(e)
                                                    val emoji_name = jsonObject.getString("shortcode")
                                                    val emoji_url = jsonObject.getString("url")
                                                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                                    toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                                                }

                                                //アバター絵文字
                                                val avater_emoji = toot_jsonObject.getJSONArray("profile_emojis")
                                                for (a in 0 until avater_emoji.length()) {
                                                    val jsonObject = avater_emoji.getJSONObject(a)
                                                    val emoji_name = jsonObject.getString("shortcode")
                                                    val emoji_url = jsonObject.getString("url")
                                                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                                    toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                                                }

                                                //ユーザーネームの方の絵文字
                                                val account_emoji = toot_account.getJSONArray("emojis")
                                                for (e in 0 until account_emoji.length()) {
                                                    val jsonObject = account_emoji.getJSONObject(e)
                                                    val emoji_name = jsonObject.getString("shortcode")
                                                    val emoji_url = jsonObject.getString("url")
                                                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                                    user_name = user_name!!.replace(":$emoji_name:", custom_emoji_src)
                                                }

                                                //ユーザーネームの方のアバター絵文字
                                                val account_avater_emoji = toot_account.getJSONArray("profile_emojis")
                                                for (a in 0 until account_avater_emoji.length()) {
                                                    val jsonObject = account_avater_emoji.getJSONObject(a)
                                                    val emoji_name = jsonObject.getString("shortcode")
                                                    val emoji_url = jsonObject.getString("url")
                                                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                                    user_name = user_name!!.replace(":$emoji_name:", custom_emoji_src)
                                                }
                                            }

                                            val japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false)
                                            if (japan_timeSetting) {
                                                //時差計算？
                                                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                                //日本用フォーマット
                                                val japanDateFormat = SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS")!!, Locale.JAPAN)
                                                try {
                                                    val date = simpleDateFormat.parse(toot_jsonObject.getString("created_at"))
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
                                                toot_time = toot_jsonObject.getString("created_at")
                                            }

                                            //カード情報
                                            var cardTitle: String? = null
                                            var cardURL: String? = null
                                            var cardDescription: String? = null
                                            var cardImage: String? = null

                                            if (!toot_jsonObject.isNull("card")) {
                                                val cardObject = toot_jsonObject.getJSONObject("card")
                                                cardURL = cardObject.getString("url")
                                                cardTitle = cardObject.getString("title")
                                                cardDescription = cardObject.getString("description")
                                                cardImage = cardObject.getString("image")
                                            }

                                            //配列を作成
                                            val Item = ArrayList<String>()
                                            //メモとか通知とかに
                                            Item.add("")
                                            //内容
                                            Item.add(toot_text!!)
                                            //ユーザー名
                                            Item.add("$user_name @$user")
                                            //時間、クライアント名等
                                            Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time)
                                            //Toot ID 文字列版
                                            Item.add(toot_id_string!!)
                                            //アバターURL
                                            Item.add(user_avater_url!!)
                                            //アカウントID
                                            Item.add(account_id.toString())
                                            //ユーザーネーム
                                            Item.add(user!!)
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


                                            if (activity != null) {
                                                val listItem = ListItem(Item)

                                                activity!!.runOnUiThread {
                                                    adapter.add(listItem)
                                                    adapter.notifyDataSetChanged()
                                                    val listView = view.findViewById<View>(R.id.home_timeline) as ListView
                                                    listView.adapter = adapter
                                                    maxid_snackbar.dismiss()
                                                    listView.setSelectionFromTop(position, y)

                                                    //listView.setSelection(scrollPosition);
                                                }
                                            }

                                            media_url_1 = null
                                            media_url_2 = null
                                            media_url_3 = null
                                            media_url_4 = null

                                        }
                                        //最後のIDを更新する
                                        val last_toot = jsonArray!!.getJSONObject(39)
                                        max_id = last_toot.getString("id")

                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }

                                }
                            })
                        }
                    }
                }
            }
        })

    }


}
