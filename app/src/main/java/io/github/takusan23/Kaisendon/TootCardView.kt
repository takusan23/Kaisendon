package io.github.takusan23.Kaisendon

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.sys1yagi.mastodon4j.api.entity.Card
import io.github.takusan23.Kaisendon.APIJSONParse.GlideSupport
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.MisskeyDriveBottomDialog
import io.github.takusan23.Kaisendon.CustomMenu.UriToByte
import io.github.takusan23.Kaisendon.Omake.ShinchokuLayout
import io.github.takusan23.Kaisendon.PaintPOST.PaintPOSTActivity
import kotlinx.android.synthetic.main.activity_toot.view.*
import kotlinx.android.synthetic.main.carview_toot_layout.view.*
import kotlinx.android.synthetic.main.toot_vote_layout.view.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*
import kotlin.concurrent.timerTask

class TootCardView(val context: Context, val isMisskey: Boolean) {

    val linearLayout = LinearLayout(context)
    lateinit var cardView: CardView
    val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)

    //添付画像
    val attachMediaList = arrayListOf<Uri>()
    //画像アップロードして出来たIDを入れる配列
    val postMediaList = arrayListOf<String>()

    //公開範囲
    var mastodonVisibility = "public"
    var misskeyVisibility = "public"

    //予約投稿
    var isScheduledPOST = false
    var scheduledDate = ""
    var scheduledTime = ""
    //アンケート
    var isVotePOST = false

    lateinit var tootEditText: TextView

    var isShow = false

    //オマケ機能
    val shinchokuLayout = ShinchokuLayout(context)

    init {
        //初期化
        val laytoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        laytoutInflater.inflate(R.layout.carview_toot_layout, linearLayout)
        tootEditText = linearLayout.toot_card_textinput
        cardView = linearLayout.toot_card_parent_cardview
        cardView.visibility = View.GONE

        setClickEvent()

        //Misskey/Mastodon
        if (isMisskey) {
            getMisskeyAccount()
            linearLayout.toot_card_vote_button.visibility = View.GONE
            linearLayout.toot_card_time_button.visibility = View.GONE
            linearLayout.toot_card_misskey_drive.visibility = View.VISIBLE
        } else {
            getAccount()
            linearLayout.toot_card_misskey_drive.visibility = View.GONE
            linearLayout.toot_card_vote_button.visibility = View.VISIBLE
            linearLayout.toot_card_time_button.visibility = View.VISIBLE
        }

        setTextLengthCount()

        setOmake()
    }

    //おまけきのう
    private fun setOmake() {
        if (pref_setting.getBoolean("life_mode", false) && !isMisskey) {
            val sinchokuLL = shinchokuLayout.layout
            linearLayout.toot_cardview_progress.addView(sinchokuLL)
        }
    }

    fun cardViewShow() {
        //表示アニメーションつける
        val showAnimation =
                AnimationUtils.loadAnimation(context, R.anim.tootcard_show_animation);
        //Visibility変更
        cardView.startAnimation(showAnimation)
        cardView.visibility = View.VISIBLE
        isShow = true
    }

    fun cardViewHide() {
        //非表示アニメーションつける
        val hideAnimation =
                AnimationUtils.loadAnimation(context, R.anim.tootcard_hide_animation);
        cardView.startAnimation(hideAnimation)
        Timer().schedule(timerTask {
            cardView.post {
                cardView.visibility = View.GONE
            }
            this.cancel()
        }, 500)
        //非表示
        isShow = false
    }

    fun setClickEvent() {
        linearLayout.toot_card_attach_image.setOnClickListener {
            setAttachImage()
        }
        linearLayout.toot_card_device_button.setOnClickListener {
            showDeviceInfo()
        }
        linearLayout.toot_card_visibility_button.setOnClickListener {
            showVisibility()
        }
        linearLayout.toot_card_vote_button.setOnClickListener {
            showVote()
        }
        linearLayout.toot_card_time_button.setOnClickListener {
            showScheduled()
        }
        linearLayout.toot_card_paint_post_button.setOnClickListener {
            showPaint()
        }
        linearLayout.toot_card_post_button.setOnClickListener {
            postStatus()
        }
        //Misskeyドライブ
        linearLayout.toot_card_misskey_drive.setOnClickListener {
            showMisskeyDrive()
        }
    }

    //文字数カウント
    fun setTextLengthCount() {
        linearLayout.toot_card_textinput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //文字数
                if (p0?.length ?: 0 < 0) {
                    val text = "${p0?.length
                            ?: 0}/500 ${context.getString(R.string.toot_text)}"
                    linearLayout.toot_card_post_button.text = text
                } else {
                    linearLayout.toot_card_post_button.text = context.getString(R.string.toot_text)
                }
            }

        })
    }

    private fun showMisskeyDrive() {
        //Misskey Drive API を叩く
        val dialogFragment = MisskeyDriveBottomDialog()
        dialogFragment.show((context as AppCompatActivity).supportFragmentManager, "misskey_drive_dialog")
    }

    fun setAttachImage() {
        //キーボード隠す
        closeKeyboard()
        val REQUEST_PERMISSION = 1000
        //onActivityResultで受け取れる
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        (context as AppCompatActivity).startActivityForResult(photoPickerIntent, 1)
    }

    fun showVisibility() {
        if (isMisskey) {
            showMisskeyVisibilityMenu()
        } else {
            showMastodonVisibilityMenu()
        }
    }

    fun showDeviceInfo() {
        val view = linearLayout.toot_card_device_button
        //ポップアップメニュー作成
        val device_menuBuilder = MenuBuilder(context)
        val device_inflater = MenuInflater(context)
        device_inflater.inflate(R.menu.device_info_menu, device_menuBuilder)
        val device_optionsMenu = MenuPopupHelper(context, device_menuBuilder, view)
        device_optionsMenu.setForceShowIcon(true)
        //コードネーム変換（手動
        var codeName = ""
        when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.N -> {
                codeName = "Nougat"
            }
            Build.VERSION_CODES.O -> {
                codeName = "Oreo"
            }
            Build.VERSION_CODES.P -> {
                codeName = "Pie"
            }
            Build.VERSION_CODES.Q -> {
                codeName = "10"
            }
        }

        device_optionsMenu.show()

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        device_menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                //名前
                if (menuItem.title.toString().contains(context.getString(R.string.device_name))) {
                    tootEditText.append(Build.MODEL)
                    tootEditText.append("\r\n")
                }
                //Androidバージョン
                if (menuItem.title.toString().contains(context.getString(R.string.android_version))) {
                    tootEditText.append(Build.VERSION.RELEASE)
                    tootEditText.append("\r\n")
                }
                //めーかー
                if (menuItem.title.toString().contains(context.getString(R.string.maker))) {
                    tootEditText.append(Build.BRAND)
                    tootEditText.append("\r\n")
                }
                //SDKバージョン
                if (menuItem.title.toString().contains(context.getString(R.string.sdk_version))) {
                    tootEditText.append(Build.VERSION.SDK_INT.toString())
                    tootEditText.append("\r\n")
                }
                //コードネーム
                if (menuItem.title.toString().contains(context.getString(R.string.codename))) {
                    tootEditText.append(codeName)
                    tootEditText.append("\r\n")
                }
                //バッテリーレベル
                if (menuItem.title.toString().contains(context.getString(R.string.battery_level))) {
                    tootEditText.append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toString() + "%")
                    tootEditText.append("\r\n")
                }
                return false
            }

            override fun onMenuModeChange(menuBuilder: MenuBuilder) {

            }
        })
    }

    fun showScheduled() {
        if (linearLayout.toot_card_scheduled_linearlayout.visibility == View.GONE) {
            isScheduledPOST = true
            linearLayout.toot_card_scheduled_linearlayout.visibility = View.VISIBLE
        } else {
            isScheduledPOST = false
            linearLayout.toot_card_scheduled_linearlayout.visibility = View.GONE
        }
        linearLayout.toot_card_scheduled_time_button.setOnClickListener {
            //時間ピッカー
            showTimePicker(linearLayout.toot_card_scheduled_time_textview)
        }
        linearLayout.toot_card_scheduled_date_button.setOnClickListener {
            //日付ピッカー
            showDatePicker(linearLayout.toot_card_scheduled_date_textview)
        }
    }

    fun showVote() {
        if (linearLayout.toot_card_vote_linearlayout.visibility == View.GONE) {
            isVotePOST = true
            linearLayout.toot_card_vote_linearlayout.visibility = View.VISIBLE
        } else {
            isVotePOST = false
            linearLayout.toot_card_vote_linearlayout.visibility = View.GONE
        }
    }

    fun showPaint() {
        //キーボード隠す
        closeKeyboard()
        //開発中メッセージ
        val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.paintPost))
                .setMessage(context.getString(R.string.paint_post_description))
                .setPositiveButton(context.getString(R.string.open_painit_post)) { dialogInterface, i ->
                    //お絵かきアクティビティへ移動
                    val intent = Intent(context, PaintPOSTActivity::class.java)
                    context.startActivity(intent)
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
        //https://stackoverflow.com/questions/9467026/changing-position-of-the-dialog-on-screen-android
        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.gravity = Gravity.BOTTOM
        layoutParams?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window?.attributes = layoutParams
    }

    fun postStatus() {
        //感触フィードバックをつける？
        if (pref_setting.getBoolean("pref_post_haptics", false)) {
            linearLayout.toot_card_paint_post_button.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
        if (attachMediaList.isEmpty()) {
            //画像投稿無し
            //文字数チェック
            if (linearLayout.toot_card_textinput.text?.length ?: 0 > 0) {
                //時間指定投稿
                val message: String
                if (isScheduledPOST) {
                    message = context.getString(R.string.time_post_post_button)
                } else {
                    message = context.getString(R.string.note_create_message)
                }
                //Tootする
                //確認SnackBer
                Snackbar.make(linearLayout.toot_card_textinput, message, Snackbar.LENGTH_SHORT).setAction(R.string.toot_text) {
                    //Mastodon / Misskey
                    if (isMisskey) {
                        misskeyNoteCreatePOST()
                    } else {
                        mastodonStatusesPOST()
                    }
                }.setAnchorView(linearLayout.toot_card_post_button).show()
            }
        } else {
            //画像アップロードから
            attachMediaList.forEach {
                if (isMisskey) {
                    uploadDrivePhoto(it)
                } else {
                    uploadMastodonPhoto(it)
                }
            }
        }
    }

    /**
     * Mastodon 画像POST
     */
    private fun uploadMastodonPhoto(uri: Uri) {
        val AccessToken = pref_setting.getString("main_token", "")
        val Instance = pref_setting.getString("main_instance", "")
        //えんどぽいんと
        val url = "https://$Instance/api/v1/media/"
        //ぱらめーたー
        val requestBody = MultipartBody.Builder()
        requestBody.setType(MultipartBody.FORM)
        //requestBody.addFormDataPart("file", file_post.getName(), RequestBody.create(MediaType.parse("image/" + file_extn_post), file_post));
        requestBody.addFormDataPart("access_token", AccessToken!!)
        //くるくる
        SnackberProgress.showProgressSnackber(linearLayout.toot_card_textinput, context, context.getString(R.string.loading) + "\n" + url)
        //Android Qで動かないのでUriからバイトに変換してPOSTしてます
        //重いから非同期処理
        Thread(Runnable {
            val uri_byte = UriToByte(context);
            try {
                // file:// と content:// でわける
                if (uri.scheme?.contains("file") == true) {
                    val file_name = getFileSchemeFileName(uri)
                    val extn = getFileSchemeFileExtension(uri)
                    requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn!!), uri_byte.getByte(uri)))
                } else {
                    val file_name = getFileNameUri(uri)
                    val extn = context.contentResolver.getType(uri)
                    requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn!!), uri_byte.getByte(uri)))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            //じゅんび
            val request = Request.Builder()
                    .url(url)
                    .post(requestBody.build())
                    .build()
            //画像Upload
            val okHttpClient = OkHttpClient()
            //POST実行
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    //失敗
                    e.printStackTrace()
                    (context as AppCompatActivity).runOnUiThread { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val response_string = response.body()!!.string()
                    //System.out.println("画像POST : " + response_string);
                    if (!response.isSuccessful) {
                        //失敗
                        (context as AppCompatActivity).runOnUiThread { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        try {
                            val jsonObject = JSONObject(response_string)
                            val media_id_long = jsonObject.getString("id")
                            //配列に格納
                            postMediaList.add(media_id_long)
                            //確認SnackBer
                            //数確認
                            if (postMediaList.size == attachMediaList.size) {
                                Snackbar.make(linearLayout.toot_card_textinput, R.string.note_create_message, Snackbar.LENGTH_INDEFINITE).setAction(R.string.toot_text) { mastodonStatusesPOST() }.show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }
            })
        }).start()
    }

    /**
     * Misskey 画像POST
     */
    private fun uploadDrivePhoto(uri: Uri) {
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance/api/drive/files/create"
        //くるくる
        SnackberProgress.showProgressSnackber(linearLayout.toot_card_textinput, context, context.getString(R.string.loading) + "\n" + url)
        //ぱらめーたー
        val requestBody = MultipartBody.Builder()
        requestBody.setType(MultipartBody.FORM)
        //requestBody.addFormDataPart("file", file_post.getName(), RequestBody.create(MediaType.parse("image/" + file_extn_post), file_post));
        requestBody.addFormDataPart("i", token!!)
        requestBody.addFormDataPart("force", "true")
        //Android Qで動かないのでUriからBitmap変換してそれをバイトに変換してPOSTしてます
        //お絵かき投稿Misskey対応?
        //重いから非同期処理
        Thread(Runnable {
            val uri_byte = UriToByte(context);
            try {
                // file:// と content:// でわける
                if (uri.scheme?.contains("file") == true) {
                    val file_name = getFileSchemeFileName(uri)
                    val extn = getFileSchemeFileExtension(uri)
                    requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn!!), uri_byte.getByte(uri)))
                } else {
                    val file_name = getFileNameUri(uri)
                    val extn = context.contentResolver.getType(uri)
                    requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn!!), uri_byte.getByte(uri)))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            //じゅんび
            val request = Request.Builder()
                    .url(url)
                    .post(requestBody.build())
                    .build()
            //画像Upload
            val okHttpClient = OkHttpClient()
            //POST実行
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    //失敗
                    e.printStackTrace()
                    (context as AppCompatActivity).runOnUiThread { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val response_string = response.body()!!.string()
                    //System.out.println("画像POST : " + response_string);
                    if (!response.isSuccessful) {
                        //失敗
                        (context as AppCompatActivity).runOnUiThread { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        try {
                            val jsonObject = JSONObject(response_string)
                            val media_id_long = jsonObject.getString("id")
                            //配列に格納
                            postMediaList.add(media_id_long)
                            //確認SnackBer
                            //数確認
                            if (postMediaList.size == attachMediaList.size) {
                                Snackbar.make(linearLayout.toot_card_textinput, R.string.note_create_message, Snackbar.LENGTH_INDEFINITE).setAction(R.string.toot_text) { misskeyNoteCreatePOST() }.show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }
            })
        }).start()
    }


    /**
     * Uri→FileName
     */
    private fun getFileNameUri(uri: Uri): String? {
        var file_name: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                file_name = cursor.getString(0)
            }
        }
        return file_name
    }

    /*
    * Uri→FileName
    * Fileスキーム限定
    * */
    fun getFileSchemeFileName(uri: Uri): String? {
        //file://なので使える
        val file = File(uri.path)
        return file.name
    }

    /*
    * Uri→Extension
    * 拡張子取得。Kotlinだと楽だね！
    * */
    fun getFileSchemeFileExtension(uri: Uri): String? {
        val file = File(uri.path)
        return file.extension
    }


    private fun mastodonStatusesPOST() {
        val AccessToken = pref_setting.getString("main_token", "")
        val Instance = pref_setting.getString("main_instance", "")
        val url = "https://$Instance/api/v1/statuses/?access_token=$AccessToken"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("status", linearLayout.toot_card_textinput.text.toString())
            jsonObject.put("visibility", mastodonVisibility)
            //時間指定
            if (linearLayout.toot_card_scheduled_switch.isChecked) {
                //System.out.println(post_date + "/" + post_time);
                //nullCheck
                if (scheduledDate.isNotEmpty() && scheduledTime.isNotEmpty()) {
                    jsonObject.put("scheduled_at", scheduledDate + scheduledTime)
                }
            }
            //画像
            if (postMediaList.size != 0) {
                val media = JSONArray()
                for (i in postMediaList) {
                    media.put(i)
                }
                jsonObject.put("media_ids", media)
            }
            //投票機能
            if (isVotePOST) {
                jsonObject.put("poll", createMastodonVote())
            }
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
                e.printStackTrace()
                (context as AppCompatActivity).runOnUiThread { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    (context as AppCompatActivity).runOnUiThread { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    (context as AppCompatActivity).runOnUiThread {
                        //予約投稿・通常投稿でトースト切り替え
                        if (linearLayout.toot_card_scheduled_switch.isChecked) {
                            linearLayout.toot_card_scheduled_switch.isChecked = false
                            Toast.makeText(context, context.getString(R.string.time_post_ok), Toast.LENGTH_SHORT).show()
                            //予約投稿を無効化
                            isScheduledPOST = false
                        } else {
                            Toast.makeText(context, context.getString(R.string.toot_ok), Toast.LENGTH_SHORT).show()
                        }
                        //投票
                        if (isVotePOST) {
                            isVotePOST = false
                            linearLayout.toot_card_vote_use_switch.isChecked = false
                        }
                        //EditTextを空にする
                        linearLayout.toot_card_textinput.setText("")
                        //tootTextCount = 0
                        //TootCard閉じる
                        cardView.visibility = View.GONE
                        //配列を空にする
                        attachMediaList.clear()
                        Home.post_media_id.clear()
                        linearLayout.toot_card_attach_linearlayout.removeAllViews()

                        //目標更新
                        shinchokuLayout.setTootChallenge()
                        //JSONParseしてトゥート数変更する
                        val jsonObject = JSONObject(response_string)
                        if (jsonObject.has("account")) {
                            val toot_count = jsonObject.getJSONObject("account").getInt("statuses_count").toString()
                            shinchokuLayout.setStatusProgress(toot_count)
                        }
                    }
                }
            }
        })
    }

    private fun misskeyNoteCreatePOST() {
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance/api/notes/create"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("visibility", misskeyVisibility)
            jsonObject.put("text", linearLayout.toot_card_textinput.text.toString())
            jsonObject.put("viaMobile", true)//スマホからなので一応
            //添付メディア
            if (postMediaList.size >= 1) {
                val jsonArray = JSONArray()
                for (i in postMediaList) {
                    jsonArray.put(i)
                }
                jsonObject.put("fileIds", jsonArray)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //System.out.println(jsonObject.toString());
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
                (context as AppCompatActivity).runOnUiThread { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗
                    (context as AppCompatActivity).runOnUiThread { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    (context as AppCompatActivity).runOnUiThread {
                        //EditTextを空にする
                        linearLayout.toot_card_textinput.setText("")
                        //tootTextCount = 0
                        //TootCard閉じる
                        cardView.visibility = View.GONE
                        //配列を空にする
                        attachMediaList.clear()
                        postMediaList.clear()
                        linearLayout.toot_card_attach_linearlayout.removeAllViews()
                    }
                }
            }
        })
    }


    /**
     * DatePicker
     */
    private fun showDatePicker(textView: TextView) {
        val date = arrayOf("")

        val calendar = Calendar.getInstance()
        val dateBuilder = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            var month = month
            var month_string = ""
            var day_string = ""
            //1-9月は前に0を入れる
            if (month++ <= 9) {
                month_string = "0" + month++.toString()
            } else {
                month_string = month++.toString()
            }
            //1-9日も前に0を入れる
            if (dayOfMonth <= 9) {
                day_string = "0$dayOfMonth"
            } else {
                day_string = dayOfMonth.toString()
            }
            scheduledDate = year.toString() + month_string + day_string + "T"
            textView.text = year.toString() + month_string + day_string + "T"
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        dateBuilder.show()
    }

    /**
     * TimePicker
     */
    private fun showTimePicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            var hourOfDay = hourOfDay
            var hour_string = ""
            var minute_string = ""
            //1-9月は前に0を入れる
            if (hourOfDay <= 9) {
                hour_string = "0" + hourOfDay++.toString()
            } else {
                hour_string = hourOfDay++.toString()
            }
            //1-9日も前に0を入れる
            if (minute <= 9) {
                minute_string = "0$minute"
            } else {
                minute_string = minute.toString()
            }
            scheduledTime = hour_string + minute_string + "00" + "+0900"
            textView.text = hour_string + minute_string + "00" + "+0900"
        }, hour, minute, true)
        dialog.show()
    }

    fun closeKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if ((context as AppCompatActivity).currentFocus != null) {
            imm.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
        }
    }

    fun showMastodonVisibilityMenu() {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.toot_area_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, linearLayout.toot_card_visibility_button)
        optionsMenu.setForceShowIcon(true)
        //ポップアップメニューを展開する
        //表示
        optionsMenu.show()
        //押したときの反応
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                //公開（全て）
                if (menuItem.title.toString().contains(context.getString(R.string.visibility_public))) {
                    mastodonVisibility = "public"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_public_black_24dp))
                }
                //未収載（TL公開なし・誰でも見れる）
                if (menuItem.title.toString().contains(context.getString(R.string.visibility_unlisted))) {
                    mastodonVisibility = "unlisted"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_done_all_black_24dp))
                }
                //非公開（フォロワー限定）
                if (menuItem.title.toString().contains(context.getString(R.string.visibility_private))) {
                    mastodonVisibility = "private"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_lock_open_black_24dp))
                }
                //ダイレクト（指定したアカウントと自分）
                if (menuItem.title.toString().contains(context.getString(R.string.visibility_direct))) {
                    mastodonVisibility = "direct"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_assignment_ind_black_24dp))
                }
                return false
            }

            override fun onMenuModeChange(menuBuilder: MenuBuilder) {

            }
        })
    }

    fun showMisskeyVisibilityMenu() {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.misskey_visibility_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, linearLayout.toot_card_visibility_button)
        optionsMenu.setForceShowIcon(true)
        //ポップアップメニューを展開する
        //表示
        optionsMenu.show()
        //押したときの反応
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                //公開（全て）
                if (menuItem.title.toString().contains(context.getString(R.string.misskey_public))) {
                    misskeyVisibility = "public"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_public_black_24dp))
                }
                //ホーム
                if (menuItem.title.toString().contains(context.getString(R.string.misskey_home))) {
                    misskeyVisibility = "home"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_home_black_24dp))
                }
                //フォロワー限定
                if (menuItem.title.toString().contains(context.getString(R.string.misskey_followers))) {
                    misskeyVisibility = "followers"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_person_add_black_24dp))
                }
                //ダイレクト（指定したアカウントと自分）
                if (menuItem.title.toString().contains(context.getString(R.string.misskey_specified))) {
                    misskeyVisibility = "specified"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_assignment_ind_black_24dp))
                }
                //公開（ローカルのみ）
                if (menuItem.title.toString().contains(context.getString(R.string.misskey_private))) {
                    misskeyVisibility = "private"
                    linearLayout.toot_card_visibility_button.setImageDrawable(context.getDrawable(R.drawable.ic_public_black_24dp))
                }

                return false
            }

            override fun onMenuModeChange(menuBuilder: MenuBuilder) {

            }
        })
    }

    fun createMastodonVote(): JSONObject {
        val `object` = JSONObject()
        try {
            //配列
            val jsonArray = JSONArray()
            if (linearLayout.toot_card_vote_editText_1.text.toString() != null) {
                jsonArray.put(linearLayout.toot_card_vote_editText_1.text.toString())
            }
            if (linearLayout.toot_card_vote_editText_2.text.toString() != null) {
                jsonArray.put(linearLayout.toot_card_vote_editText_2.text.toString())
            }
            if (linearLayout.toot_card_vote_editText_3.text.toString() != null) {
                jsonArray.put(linearLayout.toot_card_vote_editText_3.text.toString())
            }
            if (linearLayout.toot_card_vote_editText_4.text.toString() != null) {
                jsonArray.put(linearLayout.toot_card_vote_editText_4.text.toString())
            }
            `object`.put("options", jsonArray)
            `object`.put("expires_in", linearLayout.toot_card_vote_editText_time.text.toString())
            `object`.put("multiple", linearLayout.toot_card_vote_multi_switch.isChecked)
            `object`.put("hide_totals", linearLayout.toot_card_vote_hide_switch.isChecked);
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return `object`
    }

    //自分の情報を手に入れる
    private fun getAccount() {
        //Wi-Fi接続状況確認
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        //カスタム絵文字有効/無効
        var isEmojiShow = false
        if (pref_setting.getBoolean("pref_custom_emoji", true)) {
            if (pref_setting.getBoolean("pref_avater_wifi", true)) {
                //WIFIのみ表示有効時
                //ネットワーク未接続時はnullか出る
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        //WIFI
                        isEmojiShow = true
                    }
                }
            } else {
                //WIFI/MOBILE DATA 関係なく表示
                isEmojiShow = true
            }
        }


        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false)
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)
        val AccessToken = pref_setting.getString("main_token", "")
        val Instance = pref_setting.getString("main_instance", "")

        val glideSupport = GlideSupport()

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
                    var display_name = jsonObject.getString("display_name")
                    val user_id = jsonObject.getString("acct")
                    val toot_count = jsonObject.getString("statuses_count")
                    //カスタム絵文字適用
                    if (isEmojiShow) {
                        //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
                        val emojis = jsonObject.getJSONArray("emojis")
                        for (i in 0 until emojis.length()) {
                            val emojiObject = emojis.getJSONObject(i)
                            val emoji_name = emojiObject.getString("shortcode")
                            val emoji_url = emojiObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            //display_name
                            if (display_name.contains(emoji_name)) {
                                //あったよ
                                display_name = display_name.replace(":$emoji_name:", custom_emoji_src)
                            }
                        }
                        if (!jsonObject.isNull("profile_emojis")) {
                            val profile_emojis = jsonObject.getJSONArray("profile_emojis")
                            for (i in 0 until profile_emojis.length()) {
                                val emojiObject = profile_emojis.getJSONObject(i)
                                val emoji_name = emojiObject.getString("shortcode")
                                val emoji_url = emojiObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                //display_name
                                if (display_name.contains(emoji_name)) {
                                    //あったよ
                                    display_name = display_name.replace(":$emoji_name:", custom_emoji_src)
                                }
                            }
                        }
                    }
                    val accountText = "@$user_id@$Instance"
                    val snackber_Avatar = jsonObject.getString("avatar")
                    val snackber_Avatar_notGif = jsonObject.getString("avatar_static")
                    //UIスレッド
                    (context as AppCompatActivity).runOnUiThread {
                        //画像を入れる
                        //表示設定
                        if (setting_avater_hidden) {
                            linearLayout.toot_card_account_imageview.setImageResource(R.drawable.ic_person_black_24dp)
                        }
                        //GIF再生するか
                        var url = snackber_Avatar
                        if (setting_avater_gif) {
                            //再生しない
                            url = snackber_Avatar_notGif
                        }
                        //読み込む
                        if (setting_avater_wifi && networkCapabilities != null) {
                            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                //角丸設定込み
                                glideSupport.loadGlide(url, linearLayout.toot_card_account_imageview)
                            } else {
                                //キャッシュで読み込む
                                glideSupport.loadGlideReadFromCache(url, linearLayout.toot_card_account_imageview)
                            }
                        } else {
                            //キャッシュで読み込む
                            glideSupport.loadGlideReadFromCache(url, linearLayout.toot_card_account_imageview)
                        }
                        //テキストビューに入れる
                        val imageGetter = PicassoImageGetter(linearLayout.toot_card_account_textview)
                        linearLayout.toot_card_account_textview.text = Html.fromHtml(display_name, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
                        linearLayout.toot_card_account_textview.append("\n" + accountText)
                        //裏機能？
                        shinchokuLayout.setStatusProgress(toot_count)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    //添付画像を消せるようにする
    fun setAttachImageLinearLayout() {
        linearLayout.toot_card_attach_linearlayout.removeAllViews()
        //取得
        attachMediaList.forEach {
            val uri = it
            val pos = attachMediaList.indexOf(it)
            val imageView = ImageView(context)
            val params = ViewGroup.LayoutParams(200, 200)
            imageView.layoutParams = params
            imageView.tag = pos
            imageView.setImageURI(it)
            //長押ししたら削除
            imageView.setOnClickListener {
                Toast.makeText(context, "長押しで削除できます。", Toast.LENGTH_SHORT).show()
            }
            imageView.setOnLongClickListener {
                //削除
                val pos = imageView.tag as Int
                if (attachMediaList.contains(uri)) {
                    //けす
                    attachMediaList.removeAt(pos)
                    //添付画像のLinearLayout作り直す
                    setAttachImageLinearLayout()
                }
                true
            }
            //追加
            println(attachMediaList)
            linearLayout.toot_card_attach_linearlayout.addView(imageView)
        }
    }


    //自分の情報を手に入れる Misskey版
    private fun getMisskeyAccount() {

        //Wi-Fi接続状況確認
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        //カスタム絵文字有効/無効
        var isEmojiShow = false
        if (pref_setting.getBoolean("pref_custom_emoji", true)) {
            if (pref_setting.getBoolean("pref_avater_wifi", true)) {
                //WIFIのみ表示有効時
                //ネットワーク未接続時はnullか出る
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        //WIFI
                        isEmojiShow = true
                    }
                }
            } else {
                //WIFI/MOBILE DATA 関係なく表示
                isEmojiShow = true
            }
        }

        val glideSupport = GlideSupport()

        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false)
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)


        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance/api/users/show"
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
        val client_1 = OkHttpClient()
        client_1.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                try {
                    val jsonObject = JSONObject(response_string)
                    val display_name = jsonObject.getString("name")
                    //toot_count = jsonObject.getString("notesCount")
                    val user_id = jsonObject.getString("username")
                    var snackber_DisplayName = display_name
                    //カスタム絵文字適用
                    if (isEmojiShow) {
                        //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
                        val emojis = jsonObject.getJSONArray("emojis")
                        for (i in 0 until emojis.length()) {
                            val emojiObject = emojis.getJSONObject(i)
                            val emoji_name = emojiObject.getString("name")
                            val emoji_url = emojiObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            //display_name
                            if (snackber_DisplayName.contains(emoji_name)) {
                                //あったよ
                                snackber_DisplayName = snackber_DisplayName.replace(":$emoji_name:", custom_emoji_src)
                            }
                        }
                        if (!jsonObject.isNull("profile_emojis")) {
                            val profile_emojis = jsonObject.getJSONArray("profile_emojis")
                            for (i in 0 until profile_emojis.length()) {
                                val emojiObject = profile_emojis.getJSONObject(i)
                                val emoji_name = emojiObject.getString("name")
                                val emoji_url = emojiObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                //display_name
                                if (snackber_DisplayName.contains(emoji_name)) {
                                    //あったよ
                                    snackber_DisplayName = snackber_DisplayName.replace(":$emoji_name:", custom_emoji_src)
                                }
                            }
                        }
                    }
                    val snackber_Name = "@$username@$instance"
                    val snackber_Avatar = jsonObject.getString("avatarUrl")
                    //UIスレッド
                    (context as AppCompatActivity).runOnUiThread {
                        //画像を入れる
                        //表示設定
                        if (setting_avater_hidden) {
                            linearLayout.toot_card_account_imageview.setImageResource(R.drawable.ic_person_black_24dp)
                            //linearLayout.toot_card_account_imageview.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                        }
/*
                        Misskeyのときは静止画像は取れないっぽい？

                        //GIF再生するか
                        var url = snackber_Avatar
                        if (setting_avater_gif) {
                            //再生しない
                            url = snackber_Avatar_notGif
                        }
*/
                        //読み込む
                        if (setting_avater_wifi && networkCapabilities != null) {
                            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                //角丸設定込み
                                glideSupport.loadGlide(snackber_Avatar, linearLayout.toot_card_account_imageview)
                            } else {
                                //キャッシュで読み込む
                                glideSupport.loadGlideReadFromCache(snackber_Avatar, linearLayout.toot_card_account_imageview)
                            }
                        } else {
                            //キャッシュで読み込む
                            glideSupport.loadGlideReadFromCache(snackber_Avatar, linearLayout.toot_card_account_imageview)
                        }
                        //テキストビューに入れる
                        val imageGetter = PicassoImageGetter(linearLayout.toot_card_account_textview)
                        linearLayout.toot_card_account_textview.text = Html.fromHtml(display_name, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
                        linearLayout.toot_card_account_textview.append("\n" + user_id)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }


}