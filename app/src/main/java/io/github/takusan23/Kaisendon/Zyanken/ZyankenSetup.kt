package io.github.takusan23.Kaisendon.Zyanken

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ZyankenSetup : AppCompatActivity() {

    internal var follower: Button? = null
    internal var follow: Button? = null
    internal lateinit var connection: Button

    internal lateinit var textView: TextView
    internal lateinit var editText: EditText

    internal var listView: ListView? = null

    internal lateinit var pref_setting: SharedPreferences

    internal lateinit var acct: String
    internal lateinit var display_name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zyanken__setup)
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

        //find
        connection = findViewById(R.id.zyanken_setup_connection)
        textView = findViewById(R.id.zyanken_setup_textView)
        editText = findViewById(R.id.zyanken_setup_editText)

        supportActionBar!!.setTitle("招待")

        //Intentでデータを受け取る
        val mode = intent.getStringExtra("mode")
        //ホスト側
        if (mode!!.contains("host")) {
            //テキスト変更
            textView.text = "@相手のID@インスタンス名\nを入力してください"
            //EditTextの内容
            //ボタンを押したらDM送信
            //ユーザー情報取得
            //getMyUser();
            //String finalMessage = "//じゃんけん//\n" + display_name + " さんから招待";
            //あいてはWebSocketで常時接続状態になっていることが必須
            connection.setOnClickListener {
                //DM送信
                val userID = editText.text.toString()
                val finalMessage = "$userID //じゃんけん//\n招待だよ"
                sendDirectMessage(finalMessage)
                //Activity移動
                val intent = Intent(this@ZyankenSetup, Zyanken::class.java)
                intent.putExtra("mode", "host")
                startActivity(intent)
            }
        }
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
                runOnUiThread { Toast.makeText(this@ZyankenSetup, "送信しました", Toast.LENGTH_SHORT).show() }
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
                    acct = jsonObject.getString("acct")
                    display_name = jsonObject.getString("display_name")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }
}
