package io.github.takusan23.Kaisendon.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import io.github.takusan23.Kaisendon.CommandCode
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class TootSnackberActivity : AppCompatActivity() {

    internal lateinit var toot_snackbar: Snackbar
    internal lateinit var pref_setting: SharedPreferences
    internal lateinit var media_LinearLayout: LinearLayout
    internal lateinit var post_button: Button
    internal lateinit var toot_EditText: EditText
    //公開範囲
    internal var toot_area = "public"
    //画像
    internal var count = 0
    internal var media_list: ArrayList<String>? = ArrayList()
    internal var post_media_id = ArrayList<String>()
    //名前とか
    internal var snackber_Name = ""
    internal var Instance: String? = null
    internal lateinit var snackber_Avatar: String
    internal lateinit var snackberAccountAvaterImageView: ImageView
    internal lateinit var snackberAccount_TextView: TextView
    internal lateinit var account_menuBuilder: MenuBuilder
    internal lateinit var account_optionsMenu: MenuPopupHelper
    internal lateinit var account_LinearLayout: LinearLayout
    //マルチアカウント読み込み用
    internal lateinit var multi_account_instance: ArrayList<String>
    internal lateinit var multi_account_access_token: ArrayList<String>
    //文字数カウント
    internal var tootTextCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot_snackber)

        //設定のプリファレンス
        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)
        //スナックバー生成
        tootSnackBer()
        //表示
        toot_snackbar.show()
        //ふぉーかす
        toot_EditText.requestFocus()
        //キーボード表示
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)


        //共有受け取る
        val intent = intent
        val action = intent.action
        if (Intent.ACTION_SEND == action) {
            val extras = intent.extras
            if (extras != null && toot_snackbar.isShown) {
                toot_EditText.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                //URL
                var text = extras.getCharSequence(Intent.EXTRA_TEXT)
                //タイトル
                val title = extras.getCharSequence(Intent.EXTRA_SUBJECT)
                //EXTRA TEXTにタイトルが含まれているかもしれない？
                //含まれているときは消す
                text = text!!.toString().replace(title.toString(), "", ignoreCase = false)
                if (title != null) {
                    toot_EditText.append(title)
                }
                if (text != null) {
                    toot_EditText.append("\n")
                    toot_EditText.append(text)
                }
            }
        }

        //作者に連絡
        try {
            toot_EditText.append(getIntent().getStringExtra("contact"))
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }


        //FABで閉じれるようにする
        val fab = findViewById<FloatingActionButton>(R.id.toot_snackber_fab)
        fab.setOnClickListener {
            finishAndRemoveTask()
            //クローズでソフトキーボード非表示
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm != null) {
                if (this@TootSnackberActivity.currentFocus != null) {
                    imm.hideSoftInputFromWindow(this@TootSnackberActivity.currentFocus!!.windowToken, 0)
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data!!.data

                //ファイルパスとか
                val filePath = getPath(selectedImage)
                val file_extn = filePath.substring(filePath.lastIndexOf(".") + 1)
                val file = File(filePath)
                val finalPath = "file:\\\\$filePath"
                val layoutParams = LinearLayout.LayoutParams(200, 200)

                if (file_extn == "img" || file_extn == "jpg" || file_extn == "jpeg" || file_extn == "gif" || file_extn == "png") {
                    //配列に入れる
                    media_list!!.add(selectedImage!!.toString())
                    media_LinearLayout.removeAllViews()
                    //配列に入れた要素をもとにImageViewを生成する
                    for (i in media_list!!.indices) {
                        val imageView = ImageView(this@TootSnackberActivity)
                        imageView.layoutParams = layoutParams
                        imageView.setImageURI(Uri.parse(media_list!![i]))
                        imageView.tag = i
                        media_LinearLayout.addView(imageView)
                        //押したとき
                        imageView.setOnClickListener {
                            //Toast.makeText(TootSnackberActivity.this, "位置 : " + String.valueOf((Integer) imageView.getTag()), Toast.LENGTH_SHORT).show();
                            //要素の削除
                            //media_list.remove(0);
                            //再生成
                            ImageViewClick()
                        }
                    }

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
                    //とぅーとする
                    val finalAccessToken = AccessToken
                    val finalInstance = Instance
                    post_button.setOnClickListener { v ->
                        //クローズでソフトキーボード非表示
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        if (imm != null) {
                            if (this@TootSnackberActivity.currentFocus != null) {
                                imm.hideSoftInputFromWindow(this@TootSnackberActivity.currentFocus!!.windowToken, 0)
                            }
                        }
                        //配列からUriを取り出す
                        for (i in media_list!!.indices) {
                            //ひつようなやつ
                            val filePath_post = getPath(Uri.parse(media_list!![i]))
                            val file_extn_post = filePath_post.substring(filePath_post.lastIndexOf(".") + 1)
                            val file_post = File(filePath_post)

                            //画像Upload
                            val okHttpClient = OkHttpClient()
                            //えんどぽいんと
                            val url_link = "https://$finalInstance/api/v1/media/"
                            //ぱらめーたー
                            val requestBody = MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("file", file_post.name, RequestBody.create(MediaType.parse("image/$file_extn_post"), file_post))
                                    .addFormDataPart("access_token", finalAccessToken!!)
                                    .build()
                            //じゅんび
                            val request = Request.Builder()
                                    .url(url_link)
                                    .post(requestBody)
                                    .build()
                            //POST実行
                            okHttpClient.newCall(request).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {

                                }

                                @Throws(IOException::class)
                                override fun onResponse(call: Call, response: Response) {
                                    val response_string = response.body()!!.string()
                                    //System.out.println("画像POST : " + response_string);

                                    try {
                                        val jsonObject = JSONObject(response_string)
                                        val media_id_long = jsonObject.getString("id")
                                        //配列に格納
                                        post_media_id.add(media_id_long)
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }

                                }
                            })
                        }
                        //画像がPOSTできたらトゥート実行
                        //Tootする
                        //確認SnackBer
                        Snackbar.make(v, R.string.toot_dialog, Snackbar.LENGTH_SHORT).setAction(R.string.toot) {
                            //なんかアップロードしてないときある？
                            if (media_list!!.size == post_media_id.size) {
                                val url = "https://$finalInstance/api/v1/statuses/?access_token=$finalAccessToken"
                                //ぱらめーたー
                                val form = MultipartBody.Builder()
                                form.addFormDataPart("status", toot_EditText.text.toString())
                                form.addFormDataPart("visibility", toot_area)
                                //画像
                                for (i in post_media_id.indices) {
                                    form.addFormDataPart("media_ids[]", post_media_id[i])
                                }
                                form.build()
                                //ぱらめーたー
                                val requestBody = form.build()
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
                                        //System.out.println("レスポンス : " + response.body().string());
                                        toot_snackbar.dismiss()
                                        //EditTextを空にする
                                        toot_EditText.setText("")
                                        tootTextCount = 0
                                        //配列を空にする
                                        media_list!!.clear()
                                        post_media_id.clear()
                                        media_LinearLayout.removeAllViews()
                                        //アプリを閉じる
                                        finishAndRemoveTask()
                                    }
                                })
                            }
                        }.show()
                    }
                }
            }
    }

    fun getPath(uri: Uri?): String {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        val column_index = cursor!!
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        cursor.moveToFirst()
        val imagePath = cursor.getString(column_index)

        return cursor.getString(column_index)
    }

    private fun ImageViewClick() {
        val layoutParams = LinearLayout.LayoutParams(200, 200)
        media_LinearLayout.removeAllViews()
        //配列に入れた要素をもとにImageViewを生成する
        for (i in media_list!!.indices) {
            val imageView = ImageView(this@TootSnackberActivity)
            imageView.layoutParams = layoutParams
            imageView.setImageURI(Uri.parse(media_list!![i]))
            imageView.tag = i
            media_LinearLayout.addView(imageView)
            //押したとき
            imageView.setOnClickListener {
                //Toast.makeText(TootSnackberActivity.this, "位置 : " + String.valueOf((Integer) imageView.getTag()), Toast.LENGTH_SHORT).show();
                //要素の削除
                //なんだこのくそｇｍコードは
                //removeにgetTagそのまま書くとなんかだめなんだけど何これ意味不
                if (imageView.tag as Int == 0) {
                    media_list!!.removeAt(0)
                } else if (imageView.tag as Int == 1) {
                    media_list!!.removeAt(1)
                } else if (imageView.tag as Int == 2) {
                    media_list!!.removeAt(2)
                } else if (imageView.tag as Int == 3) {
                    media_list!!.removeAt(3)
                }
                //再生成
                ImageViewClick()
            }
        }
    }


    @SuppressLint("RestrictedApi")
    private fun tootSnackBer() {

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

        val view = findViewById<View>(R.id.toot_snackber_coordinator)
        toot_snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)
        //Snackber生成
        val snackBer_viewGrop = toot_snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        //LinearLayout動的に生成
        val snackber_LinearLayout = LinearLayout(this@TootSnackberActivity)
        snackber_LinearLayout.orientation = LinearLayout.VERTICAL
        val warp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        snackber_LinearLayout.layoutParams = warp
        //テキストボックス
        //Materialふうに
        val toot_textBox_LinearLayout = LinearLayout(this@TootSnackberActivity)
        //レイアウト読み込み
        layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout)
        toot_EditText = layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_editText)
        //ヒント
        (layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById<View>(R.id.name_TextInputLayout) as TextInputLayout).hint = getString(R.string.imananisiteru)
        //色
        (layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById<View>(R.id.name_TextInputLayout) as TextInputLayout).defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))
        (layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById<View>(R.id.name_TextInputLayout) as TextInputLayout).boxStrokeColor = Color.parseColor("#ffffff")
        toot_EditText.setTextColor(Color.parseColor("#ffffff"))
        toot_EditText.setHintTextColor(Color.parseColor("#ffffff"))

        //ボタン追加用LinearLayout
        val toot_Button_LinearLayout = LinearLayout(this@TootSnackberActivity)
        toot_Button_LinearLayout.orientation = LinearLayout.HORIZONTAL
        toot_Button_LinearLayout.layoutParams = warp

        //Button
        //画像追加
        val add_image_Button = ImageButton(this@TootSnackberActivity, null, 0, R.style.Widget_AppCompat_Button_Borderless)
        add_image_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        add_image_Button.setImageDrawable(getDrawable(R.drawable.ic_image_black_24dp))
        add_image_Button.setOnClickListener {
            val REQUEST_PERMISSION = 1000
            //ストレージ読み込みの権限があるか確認
            //許可してないときは許可を求める
            if (ContextCompat.checkSelfPermission(this@TootSnackberActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this@TootSnackberActivity)
                        .setTitle(getString(R.string.permission_dialog_titile))
                        .setMessage(getString(R.string.image_upload_storage_permisson))
                        .setPositiveButton(getString(R.string.permission_ok)) { dialog, which ->
                            //権限をリクエストする
                            requestPermissions(
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    REQUEST_PERMISSION)
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
            } else {
                //onActivityResultで受け取れる
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, 1)
            }
        }

        //公開範囲選択用Button
        val toot_area_Button = ImageButton(this@TootSnackberActivity, null, 0, R.style.Widget_AppCompat_Button_Borderless)
        toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp))
        toot_area_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        //toot_area_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0);

        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(this@TootSnackberActivity)
        val inflater = MenuInflater(this@TootSnackberActivity)
        inflater.inflate(R.menu.toot_area_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(this@TootSnackberActivity, menuBuilder, toot_area_Button)
        optionsMenu.setForceShowIcon(true)

        //ポップアップメニューを展開する
        toot_area_Button.setOnClickListener {
            //表示
            optionsMenu.show()
            //押したときの反応
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                    //公開（全て）
                    if (menuItem.title.toString().contains(getString(R.string.visibility_public))) {
                        toot_area = "public"
                        toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp))
                    }
                    //未収載（TL公開なし・誰でも見れる）
                    if (menuItem.title.toString().contains(getString(R.string.visibility_unlisted))) {
                        toot_area = "unlisted"
                        toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_done_all_black_24dp))
                    }
                    //非公開（フォロワー限定）
                    if (menuItem.title.toString().contains(getString(R.string.visibility_private))) {
                        toot_area = "private"
                        toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_lock_open_black_24dp))
                    }
                    //ダイレクト（指定したアカウントと自分）
                    if (menuItem.title.toString().contains(getString(R.string.visibility_direct))) {
                        toot_area = "direct"
                        toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_assignment_ind_black_24dp))
                    }

                    return false
                }

                override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                }
            })
        }


        //投稿用LinearLayout
        val toot_LinearLayout = LinearLayout(this@TootSnackberActivity)
        toot_LinearLayout.orientation = LinearLayout.HORIZONTAL
        val toot_button_LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        toot_button_LayoutParams.gravity = Gravity.RIGHT
        toot_LinearLayout.layoutParams = toot_button_LayoutParams

        //投稿用Button
        post_button = Button(this@TootSnackberActivity, null, 0, R.style.Widget_AppCompat_Button_Borderless)
        post_button.text = tootTextCount.toString() + "/" + "500 " + getString(R.string.toot)
        post_button.setTextColor(Color.parseColor("#ffffff"))
        val toot_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_create_black_24dp, null)
        post_button.setCompoundDrawablesWithIntrinsicBounds(toot_icon, null, null, null)
        //POST statuses
        val finalAccessToken = AccessToken
        val finalInstance = Instance
        post_button.setOnClickListener { v ->
            //クローズでソフトキーボード非表示
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm != null) {
                if (this@TootSnackberActivity.currentFocus != null) {
                    imm.hideSoftInputFromWindow(this@TootSnackberActivity.currentFocus!!.windowToken, 0)
                }
            }
            //画像添付なしのときはここを利用して、
            //画像添付トゥートは別に書くよ
            if (media_list!!.isEmpty() || media_list == null || media_list!![0] == null) {
                //Tootする
                //確認SnackBer
                Snackbar.make(v, R.string.toot_dialog, Snackbar.LENGTH_SHORT).setAction(R.string.toot) {
                    val url = "https://$finalInstance/api/v1/statuses/?access_token=$finalAccessToken"
                    //ぱらめーたー
                    val requestBody = FormBody.Builder()
                            .add("status", toot_EditText.text.toString())
                            .add("visibility", toot_area)
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
                            runOnUiThread {
                                //EditTextを空にする
                                toot_EditText.setText("")
                                tootTextCount = 0
                                //アプリを閉じる
                                finishAndRemoveTask()
                                toot_snackbar.dismiss()
                            }
                        }
                    })
                }.show()
            }
        }

        //端末情報とぅーと
        val device_Button = ImageButton(this@TootSnackberActivity, null, 0, R.style.Widget_AppCompat_Button_Borderless)
        device_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        device_Button.setImageDrawable(getDrawable(R.drawable.ic_perm_device_information_black_24dp))
        //ポップアップメニュー作成
        val device_menuBuilder = MenuBuilder(this@TootSnackberActivity)
        val device_inflater = MenuInflater(this@TootSnackberActivity)
        device_inflater.inflate(R.menu.device_info_menu, device_menuBuilder)
        val device_optionsMenu = MenuPopupHelper(this@TootSnackberActivity, device_menuBuilder, device_Button)
        device_optionsMenu.setForceShowIcon(true)
        //コードネーム変換（手動
        var codeName = ""
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            codeName = "Nougat"
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            codeName = "Oreo"
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            codeName = "Pie"
        }
        val finalCodeName = codeName
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        device_Button.setOnClickListener {
            device_optionsMenu.show()
            device_menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                    //名前
                    if (menuItem.title.toString().contains(getString(R.string.device_name))) {
                        toot_EditText.append(Build.MODEL)
                        toot_EditText.append("\r\n")
                    }
                    //Androidバージョン
                    if (menuItem.title.toString().contains(getString(R.string.android_version))) {
                        toot_EditText.append(Build.VERSION.RELEASE)
                        toot_EditText.append("\r\n")
                    }
                    //めーかー
                    if (menuItem.title.toString().contains(getString(R.string.maker))) {
                        toot_EditText.append(Build.BRAND)
                        toot_EditText.append("\r\n")
                    }
                    //SDKバージョン
                    if (menuItem.title.toString().contains(getString(R.string.sdk_version))) {
                        toot_EditText.append(Build.VERSION.SDK_INT.toString())
                        toot_EditText.append("\r\n")
                    }
                    //コードネーム
                    if (menuItem.title.toString().contains(getString(R.string.codename))) {
                        toot_EditText.append(finalCodeName)
                        toot_EditText.append("\r\n")
                    }
                    //バッテリーレベル
                    if (menuItem.title.toString().contains(getString(R.string.battery_level))) {
                        toot_EditText.append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toString() + "%")
                        toot_EditText.append("\r\n")
                    }
                    return false
                }

                override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                }
            })
        }


        //コマンド実行ボタン
        val command_Button = Button(this@TootSnackberActivity, null, 0, R.style.Widget_AppCompat_Button_Borderless)
        command_Button.setText(R.string.command_run)
        command_Button.setTextColor(Color.parseColor("#ffffff"))
        //EditTextを監視する
        toot_EditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //コマンド実行メゾット？
                //CommandCode.commandSet(TootSnackberActivity.this, toot_EditText, toot_LinearLayout, command_Button, "/sushi", "command_sushi");
                CommandCode.commandSet(this@TootSnackberActivity, toot_EditText, toot_LinearLayout, command_Button, "/friends.nico", "pref_friends_nico_mode")
                CommandCode.commandSetNotPreference(this@TootSnackberActivity, this@TootSnackberActivity, toot_EditText, toot_LinearLayout, command_Button, "/rate-limit", "rate-limit")
                CommandCode.commandSetNotPreference(this@TootSnackberActivity, this@TootSnackberActivity, toot_EditText, toot_LinearLayout, command_Button, "/fav-home", "home")
                CommandCode.commandSetNotPreference(this@TootSnackberActivity, this@TootSnackberActivity, toot_EditText, toot_LinearLayout, command_Button, "/fav-local", "local")
                CommandCode.commandSetNotPreference(this@TootSnackberActivity, this@TootSnackberActivity, toot_EditText, toot_LinearLayout, command_Button, "/じゃんけん", "じゃんけん")
                //カウント
                tootTextCount = toot_EditText.text.toString().length
                post_button.text = tootTextCount.toString() + "/" + "500 " + getString(R.string.toot)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        //アカウント切り替えとか
        account_LinearLayout = LinearLayout(this)
        account_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val center_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        center_layoutParams.gravity = Gravity.CENTER
        //ImageView
        snackberAccountAvaterImageView = ImageView(this)
        snackberAccountAvaterImageView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        snackberAccountAvaterImageView.layoutParams = center_layoutParams
        //TextView
        snackberAccount_TextView = TextView(this)
        snackberAccount_TextView.textSize = 14f
        snackberAccount_TextView.setTextColor(Color.parseColor("#ffffff"))
        snackberAccount_TextView.layoutParams = center_layoutParams
        //アカウント情報を取得するところにテキスト設定とか書いたで
        getAccount()
        //アカウント切り替えポップアップ
        //ポップアップメニューを展開する
        account_menuBuilder = MenuBuilder(this)
        account_optionsMenu = MenuPopupHelper(this, account_menuBuilder, account_LinearLayout)
        optionsMenu.setForceShowIcon(true)
        //マルチアカウント読み込み
        //押したときの処理とかもこっち
        readMultiAccount()

        //LinearLayoutに入れる
        account_LinearLayout.addView(snackberAccountAvaterImageView)
        account_LinearLayout.addView(snackberAccount_TextView)


        //画像追加用LinearLayout
        media_LinearLayout = LinearLayout(this@TootSnackberActivity)
        media_LinearLayout.orientation = LinearLayout.HORIZONTAL
        media_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        //LinearLayoutに追加
        //メイン
        snackber_LinearLayout.addView(account_LinearLayout)
        snackber_LinearLayout.addView(toot_textBox_LinearLayout)
        snackber_LinearLayout.addView(toot_Button_LinearLayout)
        snackber_LinearLayout.addView(media_LinearLayout)
        snackber_LinearLayout.addView(toot_LinearLayout)
        //ボタン追加
        toot_Button_LinearLayout.addView(add_image_Button)
        toot_Button_LinearLayout.addView(toot_area_Button)
        toot_Button_LinearLayout.addView(device_Button)
        //Toot LinearLayout
        toot_LinearLayout.addView(post_button)
        //SnackBerに追加
        snackBer_viewGrop.addView(snackber_LinearLayout)
    }


    //自分の情報を手に入れる
    private fun getAccount() {
        //Wi-Fi接続状況確認
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false)
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)
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
        val finalInstance = Instance
        client_1.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                try {
                    val jsonObject = JSONObject(response_string)
                    val display_name = jsonObject.getString("display_name")
                    val user_id = jsonObject.getString("acct")
                    //スナックバー更新
                    snackber_Name = "$display_name ( @$user_id / $finalInstance )"
                    snackber_Avatar = jsonObject.getString("avatar")
                    //UIスレッド
                    runOnUiThread {
                        //画像を入れる
                        //表示設定
                        if (setting_avater_hidden) {
                            snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp)
                            snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                        }
                        //Wi-Fi
                        if (setting_avater_wifi) {
                            if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                if (setting_avater_gif) {
                                    //GIFアニメ再生させない
                                    Picasso.get()
                                            .load(snackber_Avatar)
                                            .resize(100, 100)
                                            .placeholder(R.drawable.ic_refresh_black_24dp)
                                            .into(snackberAccountAvaterImageView)
                                } else {
                                    //GIFアニメを再生
                                    Glide.with(applicationContext)
                                            .load(snackber_Avatar)
                                            .apply(RequestOptions().override(100, 100).placeholder(R.drawable.ic_refresh_black_24dp))
                                            .into(snackberAccountAvaterImageView)
                                }
                            }
                        } else {
                            snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp)
                            snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                        }
                        //テキストビューに入れる
                        snackberAccount_TextView.text = snackber_Name
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }


    @SuppressLint("RestrictedApi")
    private fun readMultiAccount() {
        multi_account_instance = ArrayList()
        multi_account_access_token = ArrayList()
        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting.getString("instance_list", "")
        val account_instance_string = pref_setting.getString("access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance.add(instance_array.getString(i))
                }
            } catch (e: Exception) {

            }

        }

        if (multi_account_instance.size >= 1) {
            for (count in multi_account_instance.indices) {
                val multi_instance = multi_account_instance[count]
                val multi_access_token = multi_account_access_token[count]
                val finalCount = count
                //GetAccount
                val url = "https://$multi_instance/api/v1/accounts/verify_credentials/?access_token=$multi_access_token"
                //作成
                val request = Request.Builder()
                        .url(url)
                        .get()
                        .build()

                //GETリクエスト
                val client_1 = OkHttpClient()
                val finalInstance = Instance
                client_1.newCall(request).enqueue(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val response_string = response.body()!!.string()
                        try {
                            val jsonObject = JSONObject(response_string)
                            val display_name = jsonObject.getString("display_name")
                            val user_id = jsonObject.getString("acct")
                            //スナックバー更新
                            snackber_Name = "$display_name ( @$user_id / $finalInstance )"
                            snackber_Avatar = jsonObject.getString("avatar")
                            account_menuBuilder.add(0, finalCount, 0, "$display_name($user_id / $multi_instance)")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                })
            }
        }

        //押したときの処理
        account_LinearLayout.setOnClickListener {
            //追加中に押したら落ちるから回避
            if (account_menuBuilder.size() == multi_account_instance.size) {
                account_optionsMenu.show()
                account_menuBuilder.setCallback(object : MenuBuilder.Callback {
                    override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {

                        //ItemIdにマルチアカウントのカウントを入れている
                        val position = menuItem.itemId

                        val multi_instance = multi_account_instance[position]
                        val multi_access_token = multi_account_access_token[position]

                        val editor = pref_setting.edit()
                        editor.putString("main_instance", multi_instance)
                        editor.putString("main_token", multi_access_token)
                        editor.apply()

                        //アプリ再起動
                        val intent = Intent(this@TootSnackberActivity, Home::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                        return false
                    }

                    override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                    }
                })

            } else {
                Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show()
            }
        }

    }


}
