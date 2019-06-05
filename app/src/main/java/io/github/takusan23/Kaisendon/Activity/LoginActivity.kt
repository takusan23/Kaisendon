package io.github.takusan23.Kaisendon.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.textfield.TextInputLayout
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SnackberProgress
import okhttp3.*
import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginActivity : AppCompatActivity() {
    private var pref_setting: SharedPreferences? = null

    private var client_id: String? = null
    private var client_secret: String? = null
    private val client_name: String? = null
    private var redirect_url: String? = null
    private val callback_code: String? = null

    private var client_name_EditText: EditText? = null
    private var client_name_TextInputEditText: TextInputLayout? = null
    private var instance_name_EditText: EditText? = null
    private var access_token_Switch: Switch? = null
    private var login_Button: Button? = null
    private var access_token_LinearLayout: LinearLayout? = null

    private val accesstoken_imput = false

    //マルチアカウント
    internal var multi_account_count: Int = 0

    internal var swich: Int = 0

    private val dialog: ProgressDialog? = null

    //Misskeyモード
    private var misskey_login_Switch: Switch? = null
    private val misskey_code: String? = null
    private var editor: SharedPreferences.Editor? = null
    //強制保存モード
    private val error_mode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //設定プリファレンス
        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        editor = pref_setting!!.edit()

        //ダークテーマに切り替える機能
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)
        setContentView(R.layout.activity_login)

        //ログイン画面再構築
        client_name_EditText = findViewById(R.id.client_name_textbox_textbox)
        client_name_TextInputEditText = findViewById(R.id.client_name_textbox)
        instance_name_EditText = findViewById(R.id.instance_name_editText)
        access_token_Switch = findViewById(R.id.login_access_token_swich)
        login_Button = findViewById(R.id.login_button)
        access_token_LinearLayout = findViewById(R.id.access_token_linearLayout)
        misskey_login_Switch = findViewById(R.id.misskey_login_switch)

        //クライアント名をグレーアウトしない
        client_name_TextInputEditText!!.isEnabled = true
        access_token_LinearLayout!!.removeAllViews()
        //認証開始
        //onResumeにアクセストークン取得部分が書いてあります
        login_Button!!.setOnClickListener {
            //Step1:client_id,client_secretを取得せよ
            //Misskeyと分ける
            if (misskey_login_Switch!!.isChecked) {
                getMisskeyApp()
            } else {
                getClientIDSecret()
            }
        }

        //アクセストークン手打ちモード
        access_token_Switch!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //Misskeyモード
                if (misskey_login_Switch!!.isChecked) {
                    client_name_TextInputEditText!!.hint = "Username"
                    client_name_EditText!!.setText("")
                    //レイアウトを取り込む
                    layoutInflater.inflate(R.layout.textinput_edittext, access_token_LinearLayout)
                    //Hint
                    (layoutInflater.inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById<View>(R.id.name_TextInputLayout) as TextInputLayout).hint = getString(R.string.setting_mastodon_accesstoken)
                    //保存
                    login_Button!!.setOnClickListener {
                        //アクセストークン検証あんd保存
                        checkMisskeyAccount(client_name_EditText!!.text.toString(), (layoutInflater.inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById<View>(R.id.name_editText) as EditText).text.toString())
                    }
                    //強制保存モード(非推奨)
                    //一回失敗しないと利用できないように
                    login_Button!!.setOnLongClickListener {
                        saveMisskeyAccount(instance_name_EditText!!.text.toString(), (layoutInflater.inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById<View>(R.id.name_editText) as EditText).text.toString(), client_name_EditText!!.text.toString())
                        false
                    }
                } else {
                    //クライアント名をグレーアウトする
                    client_name_TextInputEditText!!.isEnabled = false
                    //レイアウトを取り込む
                    layoutInflater.inflate(R.layout.textinput_edittext, access_token_LinearLayout)
                    //Hint
                    (layoutInflater.inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById<View>(R.id.name_TextInputLayout) as TextInputLayout).hint = getString(R.string.setting_mastodon_accesstoken)
                    //保存
                    login_Button!!.setOnClickListener {
                        //アクセストークン検証あんd保存
                        checkAccount((layoutInflater.inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById<View>(R.id.name_editText) as EditText).text.toString())
                    }
                }
            } else {
                //クライアント名をグレーアウトしない
                client_name_TextInputEditText!!.isEnabled = true
                client_name_TextInputEditText!!.hint = getString(R.string.setting_client_name)
                client_name_EditText!!.setText("Kaisendon")
                access_token_LinearLayout!!.removeAllViews()
                //認証開始
                //onResumeにアクセストークン取得部分が書いてあります
                login_Button!!.setOnClickListener {
                    //Step1:client_id,client_secretを取得せよ
                    //Misskeyと分ける
                    if (misskey_login_Switch!!.isChecked) {
                        val instance = instance_name_EditText!!.text.toString()
                        getMisskeyApp()
                    } else {
                        getClientIDSecret()
                    }
                }
            }
        }
    }

    /*
     * ClientID / ClientSecret 取得
     * */
    private fun getClientIDSecret() {
        val url = "https://" + instance_name_EditText!!.text.toString() + "/api/v1/apps"
        //SnackberProgress
        SnackberProgress.showProgressSnackber(instance_name_EditText, this@LoginActivity, getString(R.string.loading) + "\n" + url)
        //OkHttp
        //ぱらめーたー
        val requestBody = FormBody.Builder()
                .add("client_name", client_name_EditText!!.text.toString())
                .add("redirect_uris", "https://takusan23.github.io/Kaisendon-Callback-Website/")
                .add("scopes", "read write follow")
                .add("website", "https://play.google.com/store/apps/details?id=io.github.takusan23.kaisendon")
                .build()
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@LoginActivity, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    //エラー
                    runOnUiThread { Toast.makeText(this@LoginActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    val response_string = response.body()!!.string()
                    try {
                        val jsonObject = JSONObject(response_string)
                        //ぱーす
                        client_id = jsonObject.getString("client_id")
                        client_secret = jsonObject.getString("client_secret")
                        redirect_url = jsonObject.getString("redirect_uri")
                        //アクセストークン取得のときにActivity再起動されるので保存しておく
                        val editor = pref_setting!!.edit()
                        editor.putString("client_id", client_id)
                        editor.putString("client_secret", client_secret)
                        editor.putString("redirect_uri", redirect_url)
                        //リダイレクト時にインスタンス名飛ぶので保存
                        editor.putString("register_instance", instance_name_EditText!!.text.toString())
                        editor.apply()
                        //Step2:認証画面を表示させる
                        showApplicationRequest()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })
        SnackberProgress.closeProgressSnackber()
    }

    /**
     * 認証画面表示
     */
    private fun showApplicationRequest() {
        //PINを生成する
        val url = Uri.parse("https://" + instance_name_EditText!!.text.toString() + "/oauth/authorize?client_id=" + client_id + "&redirect_uri=" + redirect_url + "&response_type=code&scope=read%20write%20follow")
        val chrome_custom_tabs = pref_setting!!.getBoolean("pref_chrome_custom_tabs", true)
        //戻るアイコン
        val back_icon = BitmapFactory.decodeResource(this@LoginActivity.applicationContext.resources, R.drawable.ic_action_arrow_back)
        //CutomTabを使うかどうか
        //有効
        if (chrome_custom_tabs) {
            val custom = CustomTabsHelper.getPackageNameToUse(this@LoginActivity)
            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(custom)
            customTabsIntent.launchUrl(this@LoginActivity as Activity, url)
        } else {
            //無効
            val intent = Intent(Intent.ACTION_VIEW, url)
            this@LoginActivity.startActivity(intent)
        }
    }

    /**
     * アクセストークン取得
     */
    private fun getAccessToken(code: String) {
        val url = "https://" + pref_setting!!.getString("register_instance", "") + "/oauth/token"
        //SnackberProgress
        SnackberProgress.showProgressSnackber(instance_name_EditText, this@LoginActivity, getString(R.string.loading) + "\n" + url)
        //OkHttp
        //ぱらめーたー
        val requestBody = FormBody.Builder()
                .add("client_id", pref_setting!!.getString("client_id", "")!!)
                .add("client_secret", pref_setting!!.getString("client_secret", "")!!)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", pref_setting!!.getString("redirect_uri", "")!!)
                .build()
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@LoginActivity, R.string.error, Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    //エラー
                    //失敗
                    runOnUiThread { Toast.makeText(this@LoginActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    //成功
                    val response_string = response.body()!!.string()
                    try {
                        val jsonObject = JSONObject(response_string)
                        val access_token = jsonObject.getString("access_token")
                        //保存
                        saveAccount(pref_setting!!.getString("register_instance", ""), access_token)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })
    }

    /**
     * アクセストークン取得
     */
    override fun onResume() {
        super.onResume()
        //認証最後の仕事、アクセストークン取得
        //URLスキーマからの起動のときの処理
        if (intent.data != null) {
            //code URLパース
            val code = intent.data!!.getQueryParameter("code")
            //Step3:アクセストークン取得
            //onResume()が呼ばれます
            //MastodonかMisskeyか
            if (code != null) {
                getAccessToken(code)
            } else {
                val token = intent.data!!.getQueryParameter("token")
                getMisskeyAccessToken(token)
            }
        }
    }

    /**
     * アカウント保存
     */
    private fun saveAccount(instance: String?, access_token: String) {
        val editor = pref_setting!!.edit()
        //祝　マルチアカウント対応
        //ここから
        //配列を使えば幸せになれそう！！！
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()
        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting!!.getString("instance_list", "")
        val account_instance_string = pref_setting!!.getString("access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance.add(instance_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        multi_account_instance.add(instance!!)
        multi_account_access_token.add(access_token)

        //Preferenceに配列は保存できないのでJSON化して保存する
        //Write
        val instance_array = JSONArray()
        val access_array = JSONArray()
        for (i in multi_account_instance.indices) {
            instance_array.put(multi_account_instance[i])
        }
        for (i in multi_account_access_token.indices) {
            access_array.put(multi_account_access_token[i])
        }

        //書き込む
        editor.putString("instance_list", instance_array.toString())
        editor.putString("access_list", access_array.toString())
        editor.apply()

        //ログインできたらとりあえずそれにする
        editor.putString("main_token", access_token)
        editor.putString("main_instance", instance)
        editor.apply()

        //HomeCardへ画面を戻す
        editor.putBoolean("pref_dark_theme", false)
        editor.putBoolean("pref_oled_mode", false)
        editor.commit()
        val homecard = Intent(this@LoginActivity, Home::class.java)
        startActivity(homecard)
    }

    /**
     * Misskeyアカウント保存
     */
    private fun saveMisskeyAccount(instance: String?, access_token: String, username: String) {
        //上記Mastodonアカウント保存とだいたいおなじ
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()
        val multi_account_username = ArrayList<String>()
        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting!!.getString("misskey_instance_list", "")
        val account_instance_string = pref_setting!!.getString("misskey_access_list", "")
        val account_username_string = pref_setting!!.getString("misskey_username_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                val username_array = JSONArray(account_username_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance.add(instance_array.getString(i))
                    multi_account_username.add(username_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        multi_account_instance.add(instance!!)
        multi_account_access_token.add(access_token)
        multi_account_username.add(username)
        //Preferenceに配列は保存できないのでJSON化して保存する
        //Write
        val instance_array = JSONArray()
        val access_array = JSONArray()
        val username_array = JSONArray()
        for (i in multi_account_instance.indices) {
            instance_array.put(multi_account_instance[i])
        }
        for (i in multi_account_access_token.indices) {
            access_array.put(multi_account_access_token[i])
        }
        for (i in multi_account_username.indices) {
            username_array.put(multi_account_username[i])
        }
        //書き込む
        editor!!.putString("misskey_instance_list", instance_array.toString())
        editor!!.putString("misskey_access_list", access_array.toString())
        editor!!.putString("misskey_username_list", username_array.toString())
        editor!!.apply()
        //Misskeyはカスタムメニュー限定要素の予定なので
        /*
        editor.putString("main_token", access_token);
        editor.putString("main_instance", instance);
        editor.apply();
*/

        //HomeCardへ画面を戻す
        editor!!.putBoolean("pref_dark_theme", false)
        editor!!.putBoolean("pref_oled_mode", false)
        editor!!.commit()
        val homecard = Intent(this@LoginActivity, Home::class.java)
        startActivity(homecard)
    }

    /**
     * アクセストークン手打ち
     * アカウントチェック
     */
    private fun checkMisskeyAccount(username: String, access_token: String) {
        val url = "https://" + instance_name_EditText!!.text.toString() + "/api/users/show"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("username", username)
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
                runOnUiThread { runOnUiThread { Toast.makeText(this@LoginActivity, getString(R.string.error), Toast.LENGTH_SHORT).show() } }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@LoginActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {

                }
                try {
                    val jsonObject = JSONObject(response_string)
                    val name = jsonObject.getString("name")
                    if (name.length != 0) {
                        saveMisskeyAccount(instance_name_EditText!!.text.toString(), access_token, username)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * アカウント検証
     */
    private fun checkAccount(access_token: String) {
        //アクセストークンがあってるかユーザー情報を取得して確認する
        //最後のトゥートIDを持ってくる
        //もういい！okhttpで実装する！！
        val url = "https://" + instance_name_EditText!!.text.toString() + "/api/v1/accounts/verify_credentials/?access_token=" + access_token
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject = JSONObject(response.body()!!.string())
                    val username = jsonObject.getString("display_name")
                    runOnUiThread { Toast.makeText(this@LoginActivity, username, Toast.LENGTH_SHORT).show() }
                    //何もなければ保存
                    if (username != null) {
                        saveAccount(instance_name_EditText!!.text.toString(), access_token)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * Misskeyにアプリ登録
     */
    private fun getMisskeyApp() {
        //SnackberProgress
        val test = instance_name_EditText!!.text.toString()
        //System.out.println("テキストボックス:" + test);
        if (test != null) {
            val url = "https://$test/api/app/create/"
            //System.out.println("りんく:" + url);
            SnackberProgress.showProgressSnackber(instance_name_EditText, this@LoginActivity, getString(R.string.loading) + "\n" + url)

            //アクセストークン取得の前準備
            //Permissionは一覧無いけどこれが全てだと思います
            val `object` = "{\n" +
                    "\"name\": \"" + client_name_EditText!!.text.toString() + "\",\n" +
                    "\"description\": \"Android Mastodon/Misskey Client\",\n" +
                    "\"callbackUrl\": \"https://takusan23.github.io/Kaisendon-Callback-Website/\",\n" +
                    "\"permission\":[\"account-read\",\n" +
                    "\"account-write\",\n" +
                    "\"account/read\",\n" +
                    "\"account/write\",\n" +
                    "\"note-write\",\n" +
                    "\"reaction-write\",\n" +
                    "\"drive-read\",\n" +
                    "\"drive-write\",\n" +
                    "\"favorite-write\",\n" +
                    "\"favorites-read\",\n" +
                    "\"following-write\",\n" +
                    "\"following-read\",\n" +
                    "\"messaging-read\",\n" +
                    "\"messaging-write\",\n" +
                    "\"notification-read\",\n" +
                    "\"notification-write\"\n" +
                    "]\n" +
                    "}"
            val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), `object`)
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
                    runOnUiThread { Toast.makeText(this@LoginActivity, R.string.error, Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val response_string = response.body()!!.string()
                    //System.out.println(response_string);
                    if (!response.isSuccessful) {
                        //失敗
                        runOnUiThread { Toast.makeText(this@LoginActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        try {
                            val jsonObject = JSONObject(response_string)
                            editor!!.putString("misskey_secret", jsonObject.getString("secret"))
                            //インスタンス名一時保存
                            editor!!.putString("misskey_instance_tmp", instance_name_EditText!!.text.toString())
                            editor!!.apply()
                            getMisskeyLogin(jsonObject.getString("secret"))
                            //くるくる終了
                            runOnUiThread { SnackberProgress.closeProgressSnackber() }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }
            })

        }
    }

    /**
     * 認証画面に飛ばす
     */
    private fun getMisskeyLogin(secretKey: String) {
        val url = "https://" + instance_name_EditText!!.text.toString() + "/api/auth/session/generate"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("appSecret", secretKey)
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
                runOnUiThread { Toast.makeText(this@LoginActivity, R.string.error, Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@LoginActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        val jsonObject = JSONObject(response_string)
                        val url = jsonObject.getString("url")
                        //CutomTabを使うかどうか
                        //有効
                        val chrome_custom_tabs = pref_setting!!.getBoolean("pref_chrome_custom_tabs", true)
                        val back_icon = BitmapFactory.decodeResource(this@LoginActivity.applicationContext.resources, R.drawable.ic_action_arrow_back)
                        if (chrome_custom_tabs) {
                            val custom = CustomTabsHelper.getPackageNameToUse(this@LoginActivity)
                            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                            val customTabsIntent = builder.build()
                            customTabsIntent.intent.setPackage(custom)
                            customTabsIntent.launchUrl(this@LoginActivity as Activity, Uri.parse(url))
                        } else {
                            //無効
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            this@LoginActivity.startActivity(intent)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })
    }

    /**
     * アクセストークン生成
     */
    private fun getMisskeyAccessToken(token: String?) {
        val url = "https://misskey.m544.net/api/auth/session/userkey"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("appSecret", pref_setting!!.getString("misskey_secret", ""))
            jsonObject.put("token", token)
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
                runOnUiThread { Toast.makeText(this@LoginActivity, R.string.error, Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val resopnse_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@LoginActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        val jsonObject = JSONObject(resopnse_string)
                        val secret = pref_setting!!.getString("misskey_secret", "")
                        val token = jsonObject.getString("accessToken")
                        val text = token + secret!!
                        val digest = MessageDigest.getInstance("SHA-256")
                        digest.reset()
                        digest.update(text.toByteArray(charset("utf8")))
                        val access_token_sha_256 = String.format("%040x", BigInteger(1, digest.digest()))
                        //アクセストークンでアカウント情報がわかるAPIがMisskeyに無いのでここでユーザーネーム保存しておく
                        val user_name = jsonObject.getJSONObject("user").getString("username")
                        //保存すりゅ
                        //System.out.println(access_token_sha_256);
                        saveMisskeyAccount(pref_setting!!.getString("misskey_instance_tmp", ""), access_token_sha_256, user_name)

                        //アカウント確認？
                        val name = jsonObject.getJSONObject("user").getString("name")
                        runOnUiThread { Toast.makeText(this@LoginActivity, name, Toast.LENGTH_SHORT).show() }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: NoSuchAlgorithmException) {
                        e.printStackTrace()
                    }

                }
            }
        })
    }

    /**
     * ためしにAPI叩いた
     */
    private fun testMisskey() {
        val url = "https://misskey.m544.net/api/notes/timeline"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", "26e301f8dbc6ca77bc2029e7296ddad806e3f695b5a5f0da84a90f9dc24a3ed6")
            jsonObject.put("limit", 100)
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

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //System.out.println(response.body().string());
            }
        })
    }

}
