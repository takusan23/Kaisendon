package io.github.takusan23.Kaisendon.Zyanken

import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Handler
import com.sys1yagi.mastodon4j.api.Shutdownable
import com.sys1yagi.mastodon4j.api.entity.Notification
import com.sys1yagi.mastodon4j.api.method.Streaming
import io.github.takusan23.Kaisendon.ListItem
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SimpleAdapter
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Zyanken : AppCompatActivity() {

    //じゃんけんの状態
    //自分
    internal var zyanken_String = "ぐー"
    //相手
    internal var zyanken_String_2: String? = null
    //勝ったのは？
    internal var zyanken_final: String? = null

    //主催側
    internal var acct: String? = null
    //String content;
    //主催側が文字列を時刻だけにするのに自分のacctを使う
    internal lateinit var myAccountacct: String
    //Host/Client共に使う時間
    internal lateinit var sendTime: String
    //正しく結果が出るように
    internal var timeTemp: String? = null

    //じぶん、あいて切り替え
    //false 自分
    //true あいて
    internal var player = false
    //TextView zyanken_TextView;
    internal lateinit var listView: ListView
    //状況
    internal lateinit var zyanken_TextView_info: TextView
    internal var zyanken_info: String? = null
    internal var myCount = 0
    internal var OpponentCount = 0
    internal var totalCount = 0
    internal var aiko = 0
    internal var errorCount = 0

    //自分、相手のIDを控える
    internal lateinit var myName: String
    internal lateinit var opponentName: String
    internal var opponentID: Long = 0

    //runningCheck
    internal var runningCheck = false

    internal lateinit var rock_Button: Button
    internal lateinit var caesar_Button: Button
    internal lateinit var paper_Button: Button


    internal lateinit var pref_setting: SharedPreferences

    //mode
    internal var host_boo = false
    internal var client_boo = false
    internal var ok = false

    internal var startCount = 0
    internal var shutdownable: Shutdownable? = null
    internal lateinit var task: TimerTask

    internal lateinit var adapter: SimpleAdapter

    //終わりボタン
    internal lateinit var finish_Button: Button
    internal lateinit var share_Button: Button

    internal lateinit var timer: Timer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zyanken)

        pref_setting = getDefaultSharedPreferences(this)

        //アクセストークン
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

        //もーど
        if (intent.getStringExtra("mode")!!.contains("host")) {
            host_boo = true
        }
        //もーど
        if (intent.getStringExtra("mode")!!.contains("client")) {
            client_boo = true
        }

        val toot_list = ArrayList<ListItem>()

        adapter = SimpleAdapter(this@Zyanken, R.layout.timeline_item, toot_list)

        //find
        //zyanken_TextView = findViewById(R.id.zyanken_TextView);
        listView = findViewById(R.id.zyanken_listView)
        zyanken_TextView_info = findViewById(R.id.zyanken_textView_info)
        //scrollView = findViewById(R.id.zyanken_scrollView);
        rock_Button = findViewById(R.id.rock)
        caesar_Button = findViewById(R.id.caesar)
        paper_Button = findViewById(R.id.paper)

        finish_Button = findViewById(R.id.zyanken_finish)
        share_Button = findViewById(R.id.zyanken_share)

        //共有ボタンで共有できるようにする
        share_Button.setOnClickListener {
            val builder = ShareCompat.IntentBuilder.from(this@Zyanken)
            //ダイアログの名前
            builder.setChooserTitle("結果を共有")
            //シェアするときのタイトル
            builder.setSubject("じゃんけん結果")
            //本文
            builder.setText(zyanken_TextView_info.text.toString())
            //今回は文字なので
            builder.setType("text/plain")
            //ダイアログ
            builder.startChooser()
        }

        //終了ボタン
        finish_Button.setOnClickListener {
            if (acct != null) {
                //ストリーミング終了
                shutdownable!!.shutdown()
                //Timerも終了させる
                task.cancel()
                //DMで終わり通知
                val finalMessage = "@$acct //じゃんけん//\nおわりだよ。またねー"
                sendDirectMessage(finalMessage)
                //UIスレッドで実行
                runOnUiThread {
                    //ListView
                    //配列を作成
                    val Item = ArrayList<String>()
                    //メモとか通知とかに
                    Item.add("じゃんけん")
                    //内容
                    Item.add("終了だって<br>" + zyanken_TextView_info.text.toString())
                    //ユーザー名
                    Item.add(opponentName)
                    //時間、クライアント名等
                    Item.add(null!!)
                    //Toot ID 文字列版
                    Item.add(null!!)
                    //アバターURL
                    Item.add("じゃんけん おわり")
                    //アカウントID
                    Item.add(opponentID.toString())
                    //ユーザーネーム
                    Item.add("")
                    //メディア
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    //カード
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    val listItem = ListItem(Item)

                    adapter.add(listItem)
                    adapter.notifyDataSetChanged()
                    listView.adapter = adapter

                    //ListView下にスクロール
                    listView.setSelection(listView.count - 1)
                }
            }
        }

        //@ID@Instance取得
        //自分の名前も取ってくる
        getMyUser()

        //通知取得
        val finalInstance = Instance
        val finalAccessToken = AccessToken
        object : AsyncTask<Void, Void, Void>() {

            override fun doInBackground(vararg aVoid: Void): Void? {

                val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                        .accessToken(finalAccessToken!!)
                        .useStreamingApi()
                        .build()
                val handler = object : Handler {
                    override fun onStatus(status: com.sys1yagi.mastodon4j.api.entity.Status) {}

                    override fun onNotification(notification: Notification) {
                        if (notification.type.contains("mention")) {
                            val content = notification.status!!.content
                            opponentName = notification.status!!.account!!.displayName + " @" + notification.status!!.account!!.acct
                            acct = notification.account!!.acct
                            opponentID = notification.status!!.account!!.id
                            //timeTemp = notification.getStatus().getCreatedAt();
                            //参加
                            //最初のゲームの開始時刻を一緒に送信する
                            if (client_boo) {
                                //ゲーム開始はDM送信から10秒後とする
                                val date = Date()
                                //10秒加算するのでCalendarを使う
                                val calendar = Calendar.getInstance()
                                calendar.time = date
                                //10秒追加しまーす（大物Youtubeｒ）
                                calendar.add(Calendar.SECOND, +10)
                                //Dateに戻しまーす
                                val finalDate = Date(calendar.timeInMillis)
                                //Stringへ
                                val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                                val dateString = simpleDateFormat.format(finalDate)
                                //送信時間確定
                                //もしかして：見えない改行エスケープ文字が存在する？
                                //参加側だけが改行されなかったのでなんとなく
                                sendTime = dateString + "\n"
                                startZyanken()

                                //DM送信
                                val finalMessage = "@$acct //じゃんけん//\nいいよ$dateString"
                                sendDirectMessage(finalMessage)
                                runOnUiThread {
                                    //zyanken_TextView.append("準備完了/メッセージ送信完了\n");
                                    Toast.makeText(this@Zyanken, "準備完了/メッセージ送信完了", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(this@Zyanken, "ぐー・ちょき・ぱー\n選んでね！10秒後に勝負だよ！", Toast.LENGTH_SHORT).show()
                                }


                            }
                            //企画
                            //参加側から送られてきた時刻をDateへ
                            if (host_boo) {
                                if (content.contains("いいよ") && content.contains("//じゃんけん//")) {
                                    //Date以外を取り除く
                                    val htmlRemoveContent = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT).toString()
                                    val finalContent = htmlRemoveContent.replace("@$myAccountacct //じゃんけん//\nいいよ", "")
                                    //送信時間確定
                                    sendTime = finalContent
                                    startZyanken()
                                    runOnUiThread {
                                        //zyanken_TextView.append("相手準備完了\n");
                                        Toast.makeText(this@Zyanken, "相手準備完了", Toast.LENGTH_SHORT).show()
                                        Toast.makeText(this@Zyanken, "ぐー・ちょき・ぱー\n選んでね！10秒後に勝負だよ！", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            //タイトルバー
                            runOnUiThread { supportActionBar!!.setTitle("相手 : $opponentName") }

                            //正負ちぇっく
                            //最初の動作確認は動かさないようにする
                            if (runningCheck) {
                                syoubu(zyanken_String, content)
                            }
                            //最初か判断する
                            runningCheck = true
                            host_boo = false
                            client_boo = false

                            //おわらせる
                            //終わりメッセージを受け取る
                            if (content.contains("おわりだよ。またねー") && content.contains("//じゃんけん//")) {
                                //ストリーミング終了
                                shutdownable!!.shutdown()
                                //Timerも終了させる
                                task.cancel()
                                //UIスレッドで実行
                                runOnUiThread {
                                    //ListView
                                    //配列を作成
                                    val Item = ArrayList<String>()
                                    //メモとか通知とかに
                                    Item.add("じゃんけん")
                                    //内容
                                    Item.add("終了だって<br>" + zyanken_TextView_info.text.toString())
                                    //ユーザー名
                                    Item.add(opponentName)
                                    //時間、クライアント名等
                                    Item.add(null!!)
                                    //Toot ID 文字列版
                                    Item.add(null!!)
                                    //アバターURL
                                    Item.add("じゃんけん おわり")
                                    //アカウントID
                                    Item.add(opponentID.toString())
                                    //ユーザーネーム
                                    Item.add("")
                                    //メディア
                                    Item.add(null!!)
                                    Item.add(null!!)
                                    Item.add(null!!)
                                    Item.add(null!!)
                                    //カード
                                    Item.add(null!!)
                                    Item.add(null!!)
                                    Item.add(null!!)
                                    Item.add(null!!)
                                    val listItem = ListItem(Item)

                                    adapter.add(listItem)
                                    adapter.notifyDataSetChanged()
                                    listView.adapter = adapter

                                    //ListView下にスクロール
                                    listView.setSelection(listView.count - 1)
                                }
                            }
                        }
                    }

                    override fun onDelete(l: Long) {

                    }
                }

                val streaming = Streaming(client)
                try {
                    shutdownable = streaming.user(handler)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return null
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        //用意できた
        //if (zyanken_TextView.getText().toString().length() == 0) {
        //10秒間隔でジャンケン勝負を行う
        sendButton(rock_Button)

        sendButton(caesar_Button)

        sendButton(paper_Button)
        //}
    }


    override fun onDestroy() {
        super.onDestroy()
        task.cancel()
        if (shutdownable != null) {
            shutdownable!!.shutdown()
        }
    }


    //ボタンの動作
    private fun sendButton(button: Button) {
        val dore = arrayOf("")
        button.setOnClickListener {
            startCount++
            //判断
            if (button.text.toString().contains("✊")) {
                dore[0] = "ぐー"
            }
            if (button.text.toString().contains("✌")) {
                dore[0] = "ちょき"
            }
            if (button.text.toString().contains("✋")) {
                dore[0] = "ぱー"
            }
            zyanken_String = dore[0]

            Toast.makeText(this@Zyanken, "$zyanken_String　を選びました", Toast.LENGTH_SHORT).show()
        }
    }


    //DMをPOSTする
    //定期的に動かす？
    private fun zyankenPost() {
        val finalMessage = "@$acct //じゃんけん//\n$zyanken_String"
        sendDirectMessage(finalMessage)
        runOnUiThread {
            Toast.makeText(this@Zyanken, "送信したよ！", Toast.LENGTH_SHORT).show()
            //zyanken_TextView.append("送信したよ！\n");
        }
    }

    //最初に使う
    private fun startZyanken() {
        //UIすれっど
        runOnUiThread {
            //zyanken_TextView.append(sendTime);
        }

        //if (zyanken_TextView.getText().toString().length() == 0) {

        //定期実行
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        timer = Timer(true)
        task = object : TimerTask() {
            override fun run() {
                //POST
                zyankenPost()
            }
        }
        //第一引数　TimerTast
        //第二引数　タイマー開始時間指定
        //第三引数　実行間隔
        try {
            timer.schedule(task, simpleDateFormat.parse(sendTime), 10000)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        // }

    }

    //結果
    //毎回呼ばれる

    /**
     * @param my      自分の選んだ手
     * @param player1 相手の選んだ手
     */
    private fun syoubu(my: String?, player1: String?) {
        var player_string = ""
        //NullCheck
        if (my != null && player1 != null) {
            //ぐー
            //自分
            if (my.contains("ぐー")) {
                //ぱーが勝ち
                //相手
                if (player1.contains("ぱー")) {
                    zyanken_final = "負けました"
                    player_string = "ぱー"
                    //相手のカウント
                    OpponentCount++
                }
                if (player1.contains("ちょき")) {
                    zyanken_final = "勝ちました"
                    player_string = "ちょき"
                    //勝カウント
                    myCount++
                }
                if (player1.contains("ぐー")) {
                    zyanken_final = "あいこだぜ"
                    player_string = "ぐー"
                    aiko++
                }
            }
            //きょき
            if (my.contains("ちょき")) {
                //ぱーが勝ち
                if (player1.contains("ぐー")) {
                    zyanken_final = "負けました"
                    player_string = "ぐー"
                    //相手のカウント
                    OpponentCount++
                }
                if (player1.contains("ぱー")) {
                    zyanken_final = "勝ちました"
                    player_string = "ぱー"
                    //勝カウント
                    myCount++
                }
                if (player1.contains("ちょき")) {
                    zyanken_final = "あいこだぜ"
                    player_string = "ちょき"
                    aiko++
                }
            }
            //ぱー
            if (my.contains("ぱー")) {
                //ぱーが勝ち
                if (player1.contains("きょき")) {
                    zyanken_final = "負けました"
                    player_string = "ちょき"
                    //相手のカウント
                    OpponentCount++
                }
                if (player1.contains("ぐー")) {
                    zyanken_final = "勝ちました"
                    player_string = "ぐー"
                    //勝カウント
                    myCount++
                }
                if (player1.contains("ぱー")) {
                    zyanken_final = "あいこだぜ"
                    player_string = "ぱー"
                    aiko++
                }
            }
            //総ゲーム数カウント
            totalCount++

            //自分の選んだものを表示
            //なーんかNullがあるっぽい？
            if (zyanken_final != null) {
                val finalPlayer_string = player_string
                runOnUiThread {
                    //zyanken_TextView.append("自分 : " + myName + " / " + "相手 : " + opponentName + "\n");
                    //zyanken_TextView.append("自分 : " + zyanken_String + " / " + "相手 : " + finalPlayer_string + "\n");
                    //zyanken_TextView.append(zyanken_final + "\n");

                    var imageString: String? = null
                    //画像選択
                    if (zyanken_final!!.contains("勝ちました")) {
                        imageString = "じゃんけん " + "勝ちました"
                    }
                    if (zyanken_final!!.contains("負けました")) {
                        imageString = "じゃんけん " + "負けました"
                    }
                    if (zyanken_final!!.contains("あいこだぜ")) {
                        imageString = "じゃんけん " + "あいこだぜ"
                    }

                    //ListView
                    //配列を作成
                    val Item = ArrayList<String>()
                    //メモとか通知とかに
                    Item.add("じゃんけん")
                    //内容
                    Item.add("自分 : $zyanken_String / 相手 : $finalPlayer_string<br>$zyanken_final")
                    //ユーザー名
                    Item.add(opponentName)
                    //時間、クライアント名等
                    Item.add(null!!)
                    //Toot ID 文字列版
                    Item.add(null!!)
                    //アバターURL
                    Item.add(imageString!!)
                    //アカウントID
                    Item.add(opponentID.toString())
                    //ユーザーネーム
                    Item.add("")
                    //メディア
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    //カード
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    val listItem = ListItem(Item)

                    adapter.add(listItem)
                    adapter.notifyDataSetChanged()
                    listView.adapter = adapter

                    //状態を更新する
                    setZyankenInfo()

                    //ListView下にスクロール
                    listView.setSelection(listView.count - 1)
                }
            } else {
                //えらーめせーじ
                val finalPlayer_string1 = player_string
                errorCount++
                runOnUiThread {
                    val finalPlayer_string = finalPlayer_string1
                    var imageString: String? = null
                    imageString = "じゃんけん " + "えらー"

                    //ListView
                    //配列を作成
                    val Item = ArrayList<String>()
                    //メモとか通知とかに
                    Item.add("じゃんけん")
                    //内容
                    Item.add("相手の情報が取れなかったよ")
                    //ユーザー名
                    Item.add(opponentName)
                    //時間、クライアント名等
                    Item.add(null!!)
                    //Toot ID 文字列版
                    Item.add(null!!)
                    //アバターURL
                    Item.add(imageString!!)
                    //アカウントID
                    Item.add(opponentID.toString())
                    //ユーザーネーム
                    Item.add("")
                    //メディア
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    //カード
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    Item.add(null!!)
                    val listItem = ListItem(Item)

                    adapter.add(listItem)
                    adapter.notifyDataSetChanged()
                    listView.adapter = adapter
                    //状態を更新する
                    setZyankenInfo()

                    //ListView下にスクロール
                    listView.setSelection(listView.count - 1)

                    //zyanken_TextView.append("相手の情報が取れなかったよ \n");
                }

            }
        }
    }


    private fun kati_make(player_1: String, player_2: String) {
        //ぐー
        if (player_1.contains("ぐー")) {
            //ぱーが勝ち
            if (player_2.contains("ぱー")) {
                zyanken_final = "２番目の勝ちです"
            }
            if (player_2.contains("ちょき")) {
                zyanken_final = "１番目の勝ちです"
            }
            if (player_2.contains("ぐー")) {
                zyanken_final = "あいこです"
            }
        }
        //きょき
        if (player_1.contains("ちょき")) {
            //ぱーが勝ち
            if (player_2.contains("ぐー")) {
                zyanken_final = "２番目の勝ちです"
            }
            if (player_2.contains("ぱー")) {
                zyanken_final = "１番目の勝ちです"
            }
            if (player_2.contains("ちょき")) {
                zyanken_final = "あいこです"
            }
        }
        //ぱー
        if (player_1.contains("ぱー")) {
            //ぱーが勝ち
            if (player_2.contains("きょき")) {
                zyanken_final = "２番目の勝ちです"
            }
            if (player_2.contains("ぐー")) {
                zyanken_final = "１番目の勝ちです"
            }
            if (player_2.contains("ぱー")) {
                zyanken_final = "あいこです"
            }
        }
    }

    //じゃんけんの状態
    private fun setZyankenInfo() {
        //定型文
        val player_my = "自分勝ち "
        val player_pair = "相手勝ち "
        val total = "ゲーム数 "
        val aikoString = "あいこ "
        val eroor = "エラー "
        zyanken_TextView_info.text = total + intToString(totalCount) + " / " + player_my + intToString(myCount) + " / " + player_pair + intToString(OpponentCount) + " / " + aikoString + intToString(aiko) + " / " + eroor + intToString(errorCount)
    }

    //int→String
    private fun intToString(convert: Int): String {
        return convert.toString()
    }

    private fun sendDirectMessage(message: String) {
        //アクセストークン
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

        val url = "https://$Instance/api/v1/statuses/?access_token=$AccessToken"
        //ぱらめーたー
        val requestBody = FormBody.Builder()
                .add("status", message)
                .add("visibility", "direct")
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
                runOnUiThread { Toast.makeText(this@Zyanken, "送信しました", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun getMyUser() {
        //アクセストークン
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
        val url = "https://$Instance/api/v1/accounts/verify_credentials/?access_token=$AccessToken"
        //作成
        val request = Request.Builder()
                .url(url)
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
                try {
                    val jsonObject = JSONObject(response_string)
                    myAccountacct = jsonObject.getString("acct")
                    //名前（DisplayName+acct）
                    myName = jsonObject.getString("display_name") + " @" + myAccountacct
                    runOnUiThread {
                        //Titleber
                        supportActionBar!!.subtitle = "自分 : $myName"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

}
