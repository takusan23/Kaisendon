package io.github.takusan23.Kaisendon

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.snackbar.Snackbar
import io.github.takusan23.Kaisendon.Omake.KaisendonLife
import io.github.takusan23.Kaisendon.Zyanken.ZyankenMenu
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

object CommandCode {


    //コマンドをここに書いていくと思うよ

    /**
     * @param editText          　値が正しいか確認
     * @param toot_LinearLayout 　コマンド実行ボタン生成
     * @param command_Button    　コマンド実行ボタン
     * @param commandText       　コマンド実行文
     * @param prefKey           　プリファレンスに保存する値
     */
    fun commandSet(activity: Activity, editText: EditText, toot_LinearLayout: LinearLayout, command_Button: Button, commandText: String, prefKey: String) {
        //コマンド機能
        val pref_setting = getDefaultSharedPreferences(editText.context)
        if (editText.text.toString().contains(commandText)) {
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            command_Button.layoutParams = layoutParams
            command_Button.setText(R.string.command_run)
            command_Button.setOnClickListener { v ->
                //クローズでソフトキーボード非表示
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm != null) {
                    if (activity.currentFocus != null) {
                        imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
                    }
                }

                //スナックばー
                Snackbar.make(v, R.string.command_run_message, Snackbar.LENGTH_SHORT).setAction(R.string.run) {
                    //preference書かないコマンド
                    //設定変更
                    val editor = pref_setting.edit()
                    //モード切替
                    if (pref_setting.getBoolean(prefKey, false)) {
                        //ONのときはOFFにする
                        editor.putBoolean(prefKey, false)
                        editor.apply()
                    } else {
                        //OFFのときはONにする
                        editor.putBoolean(prefKey, true)
                        editor.apply()
                    }
                }.show()
            }
            toot_LinearLayout.addView(command_Button, 0)
        } else {
            toot_LinearLayout.removeView(command_Button)
        }
    }

    /**
     * @param context           いろいろ
     * @param editText          　値が正しいか確認
     * @param toot_LinearLayout 　コマンド実行ボタン生成
     * @param command_Button    　コマンド実行ボタン
     * @param commandText       　コマンド実行文
     * @param commandType       コマンド詳細（fav/btコマンドはタイムライン名）
     */
    fun commandSetNotPreference(activity: Activity, context: Context, editText: EditText, toot_LinearLayout: LinearLayout, command_Button: Button, commandText: String, commandType: String) {
        //コマンド機能
        if (editText.text.toString().contains(commandText)) {
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            command_Button.layoutParams = layoutParams
            command_Button.setText(R.string.command_run)
            command_Button.setOnClickListener { v ->
                //スナックばー
                Snackbar.make(v, R.string.command_run_message, Snackbar.LENGTH_SHORT).setAction(R.string.run) {
                    //クローズでソフトキーボード非表示
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (imm != null) {
                        if (activity.currentFocus != null) {
                            imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
                        }
                    }
                    //レートリミット
                    if (commandType.contains("rate-limit")) {
                        getMyRateLimit(context, editText)
                    }
                    //じゃんけん
                    if (commandType.contains("じゃんけん")) {
                        val intent = Intent(context, ZyankenMenu::class.java)
                        context.startActivity(intent)
                    }
                    //favコマンド
                    if (commandType.contains("home")) {
                        favCommand("home", editText)
                    }
                    if (commandType.contains("local")) {
                        favCommand("local", editText)
                    }
                    //進捗機能
                    if (commandType.contains("progress")) {
                        saveSharedPreference(context, editText, "progress_mode")
                    }
                    //起動カウント
                    if (commandType.contains("lunch_bonus")) {
                        saveSharedPreference(context, editText, "lunch_bonus_mode")
                    }
                    //Life機能
                    if (commandType.contains("life")) {
                        context.startActivity(Intent(context, KaisendonLife::class.java))
                    }
                    //戻す
                    editText.setText("")
                    toot_LinearLayout.removeView(command_Button)
                }.show()
            }
            toot_LinearLayout.addView(command_Button, 0)
        } else {
            //toot_LinearLayout.removeView(command_Button);
        }
    }

    /*保存*/
    fun saveSharedPreference(context: Context, editText: EditText, name: String) {
        val pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val editor = pref_setting.edit()
        if (!pref_setting.getBoolean(name, false)) {
            editor.putBoolean(name, true)
        } else {
            editor.putBoolean(name, false)
        }
        editor.apply()
        editText.setText("")
    }

    //れーとりみっとかくにん

    /**
     * @param editText トゥートテキストボックス
     */
    private fun getMyRateLimit(context: Context, editText: EditText) {
        val pref_setting = getDefaultSharedPreferences(editText.context)
        //アクセストークンがあってるかユーザー情報を取得して確認する
        val AccessToken: String?
        val instance: String?
        val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
            instance = pref_setting.getString("pref_mastodon_instance", "")
        } else {
            AccessToken = pref_setting.getString("main_token", "")
            instance = pref_setting.getString("main_instance", "")
        }
        val url = "https://$instance/api/v1/accounts/verify_credentials/?access_token=$AccessToken"
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
                //レスポンスヘッダー
                val headers = response.headers()
                //残機確認
                val rateLimit = headers.get("x-ratelimit-limit")
                val rateLimit_nokori = headers.get("x-ratelimit-remaining")
                val rateLimit_time = headers.get("x-ratelimit-reset")
                (context as AppCompatActivity).runOnUiThread {
                    editText.append("\n")
                    editText.append(context.getString(R.string.ratelimit_limit) + "(x-ratelimit-limit) : " + rateLimit + "\n")
                    editText.append(context.getString(R.string.ratelimit_remaining) + "(x-ratelimit-remaining) : " + rateLimit_nokori + "\n")
                    editText.append(context.getString(R.string.ratelimit_reset) + "(x-ratelimit-reset) : " + rateLimit_time + "\n")
                }
            }
        })
    }

    //ふぁぼる
    private fun favCommand(timeline: String, editText: EditText) {
        val pref_setting = getDefaultSharedPreferences(editText.context)
        val AccessToken: String?
        val instance: String?
        val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
            instance = pref_setting.getString("pref_mastodon_instance", "")
        } else {
            AccessToken = pref_setting.getString("main_token", "")
            instance = pref_setting.getString("main_instance", "")
        }

        var url = "https://$instance/api/v1/timelines/$timeline/?access_token=$AccessToken&limit=40"
        //ローカルTL
        if (timeline.contains("local")) {
            url = "https://$instance/api/v1/timelines/public/?access_token=$AccessToken&limit=40&local=true"
        }
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
                    val response_string = response.body()!!.string()
                    //JSONArray
                    val jsonArray = JSONArray(response_string)
                    //ぱーす
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        //ID取得
                        val id = jsonObject.getString("id")
                        //System.out.println("れすぽんす : " + id);
                        //Favouriteする
                        val url = "https://$instance/api/v1/statuses/$id/favourite/?access_token=$AccessToken"
                        //ぱらめーたー
                        val requestBody = FormBody.Builder()
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

                            }
                        })

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }


}
