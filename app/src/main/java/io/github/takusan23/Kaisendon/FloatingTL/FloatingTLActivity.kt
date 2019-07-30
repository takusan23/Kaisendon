package io.github.takusan23.Kaisendon.FloatingTL

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.Theme.DarkModeSupport
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class FloatingTLActivity : AppCompatActivity() {

    private var editText: EditText? = null
    private var postImageButton: ImageButton? = null
    private var jsonObject: JSONObject? = null
    private var isMisskey: String? = null
    private var pip_mode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.float_tl_layout)

        editText = findViewById(R.id.floating_tl_edittext)
        postImageButton = findViewById(R.id.floating_tl_post_button)
        pip_mode = intent.getBooleanExtra("pip", false)

        //ダークテーマに切り替える機能
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)

        //Fragmentにわたすやつ
        try {
            jsonObject = JSONObject(intent.getStringExtra("json")!!)
            val json = jsonObject!!.toString()
            val name = jsonObject!!.getString("name")
            val content = jsonObject!!.getString("content")
            val instance = jsonObject!!.getString("instance")
            val access_token = jsonObject!!.getString("access_token")
            val image_load = jsonObject!!.getString("image_load")
            val dialog = jsonObject!!.getString("dialog")
            //val dark_mode = jsonObject!!.getString("dark_mode")
            val position = jsonObject!!.getString("position")
            val streaming = jsonObject!!.getString("streaming")
            val subtitle = jsonObject!!.getString("subtitle")
            val image_url = jsonObject!!.getString("image_url")
            val background_transparency = jsonObject!!.getString("background_transparency")
            val background_screen_fit = jsonObject!!.getString("background_screen_fit")
            val quick_profile = jsonObject!!.getString("quick_profile")
            val toot_counter = jsonObject!!.getString("toot_counter")
            val custom_emoji = jsonObject!!.getString("custom_emoji")
            val gif = jsonObject!!.getString("gif")
            val font = jsonObject!!.getString("font")
            val one_hand = jsonObject!!.getString("one_hand")
            val misskey = jsonObject!!.getString("misskey")
            val misskey_username = jsonObject!!.getString("misskey_username")
            val setting = jsonObject!!.getString("setting")
            //Fragmentに詰める
            val bundle = Bundle()
            bundle.putString("setting_json",json)
            val customMenuTimeLine = CustomMenuTimeLine()
            customMenuTimeLine.arguments = bundle
            //Fragmentおく？
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            var tag = ""
            //PiPモードで簡略表示するため
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || pip_mode) {
                tag = "pip_fragment"
            }
            fragmentTransaction.replace(R.id.float_tl_linearlayout, customMenuTimeLine, tag)
            fragmentTransaction.commit()
            isMisskey = jsonObject!!.getString("misskey")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //投稿
        setStatusPOST()

        //Android 10以前の端末はPictureInPictureモードで起動する
        //Nougatユーザーは神バージョンだけど使えないよ😢
        //TLQuickSettingSnackbarの中にQ以外の場合はpipがtrueになることになる。
        if (pip_mode) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                //1:1の大きさにする設定
                val aspectRatio = Rational(1, 1)
                var PIPParamsBuilder: PictureInPictureParams.Builder? = null
                PIPParamsBuilder = PictureInPictureParams.Builder()
                PIPParamsBuilder.setAspectRatio(aspectRatio).build()
                //Mastodonのときは投稿ボタンを表示する
                if (!java.lang.Boolean.valueOf(isMisskey)) {
                    val toot_RemoteAction = ArrayList<RemoteAction>()
                    val intent = Intent(this, PiPBroadcastReciver::class.java)
                    val remoteAction = RemoteAction(Icon.createWithResource(this, R.drawable.ic_create_black_24dp), getString(R.string.toot), "Toot", PendingIntent.getBroadcast(this, 114, intent, 0))
                    toot_RemoteAction.add(remoteAction)
                    PIPParamsBuilder.setActions(toot_RemoteAction)
                }
                enterPictureInPictureMode(PIPParamsBuilder.build())
                //投稿ボタンを消す
                val edit_LinearLayout = postImageButton!!.parent as LinearLayout
                (edit_LinearLayout.parent as LinearLayout).removeView(edit_LinearLayout)
            }
        }
    }

    /*とうこう*/
    private fun setStatusPOST() {
        postImageButton!!.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.note_create_message), Snackbar.LENGTH_LONG).setAction(getString(R.string.toot_text)) {
                try {
                    if (java.lang.Boolean.valueOf(jsonObject!!.getString("misskey"))) {
                        misskeyStatusPOST()
                    } else {
                        mastodonStatusPOST()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }.show()
        }
    }

    /*Misskey*/
    private fun misskeyStatusPOST() {
        try {
            val token = jsonObject!!.getString("access_token")
            val instance = jsonObject!!.getString("instance")
            val url = "https://$instance/api/notes/create"
            val jsonObject = JSONObject()
            try {
                jsonObject.put("i", token)
                jsonObject.put("text", editText!!.text.toString())
                jsonObject.put("viaMobile", true)//スマホからなので一応
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
            val request = Request.Builder()
                    .url(url)
                    .post(requestBody_json)
                    .build()
            //POST
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread { Toast.makeText(this@FloatingTLActivity, getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        //失敗
                        runOnUiThread { Toast.makeText(this@FloatingTLActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@FloatingTLActivity, getString(R.string.toot_ok), Toast.LENGTH_SHORT).show()
                            editText!!.setText("")
                        }
                    }
                }
            })
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /*MastodonStatusPOST*/
    private fun mastodonStatusPOST() {
        var AccessToken: String? = null
        var Instance: String? = null
        try {
            AccessToken = jsonObject!!.getString("access_token")
            Instance = jsonObject!!.getString("instance")
            val url = "https://$Instance/api/v1/statuses/?access_token=$AccessToken"
            val postJsonObject = JSONObject()
            postJsonObject.put("status", editText!!.text.toString())
            val requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postJsonObject.toString())
            val request = Request.Builder()
                    .url(url)
                    .post(requestBody_json)
                    .build()
            //POST
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread { Toast.makeText(this@FloatingTLActivity, getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        //失敗
                        runOnUiThread { Toast.makeText(this@FloatingTLActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@FloatingTLActivity, getString(R.string.toot_ok), Toast.LENGTH_SHORT).show()
                            editText!!.setText("")
                        }
                    }
                }
            })
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /*PiPからアプリ起動したときの処理*/
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        if (!isInPictureInPictureMode) {
            finishAndRemoveTask()
            startActivity(Intent(this@FloatingTLActivity, Home::class.java))
        }
    }

}
