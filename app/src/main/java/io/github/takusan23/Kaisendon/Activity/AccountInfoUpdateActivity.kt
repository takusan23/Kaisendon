package io.github.takusan23.Kaisendon.Activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.CalenderDialog
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.Preference_ApplicationContext.Companion.context
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SnackberProgress
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class AccountInfoUpdateActivity : AppCompatActivity() {

    internal var AccessToken: String? = null
    internal var Instance: String? = null
    internal lateinit var display_name: String
    internal lateinit var note: String
    internal var fields_name_1: String? = null
    internal var fields_name_2: String? = null
    internal var fields_name_3: String? = null
    internal var fields_name_4: String? = null
    internal var fields_value_1: String? = null
    internal var fields_value_2: String? = null
    internal var fields_value_3: String? = null
    internal var fields_value_4: String? = null
    internal lateinit var avatar_url: String
    internal lateinit var header_url: String
    internal var image_url: String? = null
    internal var image_uri: String? = null
    internal var image_name: String? = null
    internal var header_post_url: String? = null
    internal var header_post_uri: String? = null
    internal var header_post_name: String? = null
    internal var avatar_post_path: String? = null
    internal var avatar_post_uri: String? = null
    internal var avatar_post_name: String? = null
    internal var avatar_extn: String? = null
    internal var header_extn: String? = null
    internal var avatar_file: File? = null
    internal var header_file: File? = null
    internal var avatar = false
    internal var header = false

    private var fields_attributes_1_edittext_name: EditText? = null
    private var fields_attributes_2_edittext_name: EditText? = null
    private var fields_attributes_3_edittext_name: EditText? = null
    private var fields_attributes_4_edittext_name: EditText? = null
    private var fields_attributes_1_edittext_value: EditText? = null
    private var fields_attributes_2_edittext_value: EditText? = null
    private var fields_attributes_3_edittext_value: EditText? = null
    private var fields_attributes_4_edittext_value: EditText? = null

    private var avater_image_post_message_textview: TextView? = null
    private var avater_image_imageview: ImageView? = null
    private var header_image_imageview: ImageView? = null
    private var avatar_button: Button? = null
    private var header_button: Button? = null
    private var fab: FloatingActionButton? = null

    private var displayname_textview: TextView? = null
    private var displayname_edittext: EditText? = null
    private var note_textview: TextView? = null
    private var note_edittext: TextView? = null
    private val snackbar: Snackbar? = null
    private var snackbar_loading: Snackbar? = null
    private var pref_setting: SharedPreferences? = null

    private var place_EditText: EditText? = null
    private var cat_Switch: Switch? = null

    private var misskey_avatar_id: String? = null
    private var misskey_banner_id: String? = null

    private var avatar_Uri: Uri? = null
    private var header_Uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

        //ダークテーマに切り替える機能
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)

        if (intent.getBooleanExtra("Misskey", false)) {
            setContentView(R.layout.misskey_account_update_layout)
        } else {
            setContentView(R.layout.activity_account_info_update)
        }

        val accessToken_boomelan = pref_setting!!.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {
            AccessToken = pref_setting!!.getString("pref_mastodon_accesstoken", "")
            Instance = pref_setting!!.getString("pref_mastodon_instance", "")
        } else {
            AccessToken = pref_setting!!.getString("main_token", "")
            Instance = pref_setting!!.getString("main_instance", "")
        }


        //くるくる
        val view = findViewById<View>(android.R.id.content)
        val snackbar = arrayOf(Snackbar.make(view, getString(R.string.loading_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE))
        val snackBer_viewGrop = snackbar[0].view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 2
        //複数行対応させたおかげでずれたので修正
        val progressBar = ProgressBar(this@AccountInfoUpdateActivity)
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBer_layoutParams
        snackBer_viewGrop.addView(progressBar, 0)
        snackbar[0].show()

        //Misskey
        if (intent.getBooleanExtra("Misskey", false)) {

            displayname_edittext = findViewById(R.id.account_update_display_name_edittext)
            note_edittext = findViewById(R.id.account_update_note_name_edittext)
            avater_image_imageview = findViewById(R.id.account_update_avatar_imageview)
            header_image_imageview = findViewById(R.id.account_update_header_imageview)
            avatar_button = findViewById(R.id.account_update_avatar_button)
            header_button = findViewById(R.id.account_update_header_button)
            place_EditText = findViewById(R.id.place_edittext)
            birthday_Button = findViewById(R.id.birthday_button)
            cat_Switch = findViewById(R.id.cat_switch)
            fab = findViewById(R.id.fab)
            //タイトル
            setTitle(R.string.update_userinfo_title)

            //Misskey Account 取得
            getMisskeyAccount()

            //プロフィール更新用
            fab!!.setOnClickListener { updateMisskeyProfile() }

            birthday_Button.setOnClickListener {
                val calenderDialog = CalenderDialog()
                val bundle = Bundle()
                bundle.putString("type", "birthday")
                calenderDialog.arguments = bundle
                calenderDialog.show(supportFragmentManager, "calender_dialog")
            }

            //画像のアップロード
            //権限があるか確認
            avatar_button!!.setOnClickListener {
                clickMediaSelect()
                avatar = true
            }
            header_button!!.setOnClickListener {
                clickMediaSelect()
                header = true
            }

        } else {

            //find
            displayname_textview = findViewById(R.id.account_update_display_name_textview)
            displayname_edittext = findViewById(R.id.account_update_display_name_edittext)
            note_textview = findViewById(R.id.account_update_note_name_textview)
            note_edittext = findViewById(R.id.account_update_note_name_edittext)

            /*
        TextView fields_attributes_1_textview = findViewById(R.id.account_update_fields_attributes_1_textview);
        TextView fields_attributes_2_textview = findViewById(R.id.account_update_fields_attributes_2_textview);
        TextView fields_attributes_3_textview = findViewById(R.id.account_update_fields_attributes_3_textview);
        TextView fields_attributes_4_textview = findViewById(R.id.account_update_fields_attributes_4_textview);
*/

            fields_attributes_1_edittext_name = findViewById(R.id.account_update_fields_attributes_1_edittext_name)
            fields_attributes_2_edittext_name = findViewById(R.id.account_update_fields_attributes_2_edittext_name)
            fields_attributes_3_edittext_name = findViewById(R.id.account_update_fields_attributes_3_edittext_name)
            fields_attributes_4_edittext_name = findViewById(R.id.account_update_fields_attributes_4_edittext_name)

            fields_attributes_1_edittext_value = findViewById(R.id.account_update_fields_attributes_1_edittext_value)
            fields_attributes_2_edittext_value = findViewById(R.id.account_update_fields_attributes_2_edittext_value)
            fields_attributes_3_edittext_value = findViewById(R.id.account_update_fields_attributes_3_edittext_value)
            fields_attributes_4_edittext_value = findViewById(R.id.account_update_fields_attributes_4_edittext_value)

            avater_image_post_message_textview = findViewById(R.id.account_update_avatar_textview_title)
            avater_image_imageview = findViewById(R.id.account_update_avatar_imageview)
            header_image_imageview = findViewById(R.id.account_update_header_imageview)
            avatar_button = findViewById(R.id.account_update_avatar_button)
            header_button = findViewById(R.id.account_update_header_button)


            //説明文
            avater_image_post_message_textview!!.text = getString(R.string.upload_avater_header) + "\r\n" + getString(R.string.image_upload_storage_permisson)


            //EditTextにヒントを入れる
            val label = getString(R.string.label)
            val context = getString(R.string.content)
            fields_attributes_1_edittext_name!!.hint = label + "1"
            fields_attributes_2_edittext_name!!.hint = label + "2"
            fields_attributes_3_edittext_name!!.hint = label + "3"
            fields_attributes_4_edittext_name!!.hint = label + "4"

            fields_attributes_1_edittext_value!!.hint = context + "1"
            fields_attributes_2_edittext_value!!.hint = context + "2"
            fields_attributes_3_edittext_value!!.hint = context + "3"
            fields_attributes_4_edittext_value!!.hint = context + "4"

            //タイトル
            setTitle(R.string.update_userinfo_title)

            //画像のアップロード
            //権限があるか確認
            avatar_button!!.setOnClickListener {
                clickMediaSelect()
                avatar = true
            }
            header_button!!.setOnClickListener {
                clickMediaSelect()
                header = true
            }


            //編集前の内容にする！！
            //パラメータを設定
            val url = "https://$Instance/api/v1/accounts/verify_credentials/?access_token=$AccessToken"
            val builder = HttpUrl.parse(url)!!.newBuilder()
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
                    try {
                        val jsonObject = JSONObject(responce_string)
                        display_name = jsonObject.getString("display_name")
                        note = jsonObject.getString("note")
                        avatar_url = jsonObject.getString("avatar")
                        header_url = jsonObject.getString("header")

                        //補足情報
                        val fields_array = jsonObject.getJSONArray("fields")
                        //System.out.println("数" + String.valueOf(fields_array.length()));
                        if (0 < fields_array.length()) {
                            val fields_object = fields_array.getJSONObject(0)
                            fields_name_1 = fields_object.getString("name")
                            fields_value_1 = fields_object.getString("value")
                        }
                        if (1 < fields_array.length()) {
                            val fields_object = fields_array.getJSONObject(1)
                            fields_name_2 = fields_object.getString("name")
                            fields_value_2 = fields_object.getString("value")
                        }
                        if (2 < fields_array.length()) {
                            val fields_object = fields_array.getJSONObject(2)
                            fields_name_3 = fields_object.getString("name")
                            fields_value_3 = fields_object.getString("value")
                        }
                        if (3 < fields_array.length()) {
                            val fields_object = fields_array.getJSONObject(3)
                            fields_name_4 = fields_object.getString("name")
                            fields_value_4 = fields_object.getString("value")
                        }

                        //System.out.println(fields_name_1 + fields_value_1);

                        //UI更新
                        runOnUiThread {
                            displayname_edittext!!.setText(display_name)
                            note_edittext!!.text = Html.fromHtml(note, Html.FROM_HTML_MODE_COMPACT)

                            setTextNullChack(fields_attributes_1_edittext_name, fields_name_1)
                            setTextNullChack(fields_attributes_2_edittext_name, fields_name_2)
                            setTextNullChack(fields_attributes_3_edittext_name, fields_name_3)
                            setTextNullChack(fields_attributes_4_edittext_name, fields_name_4)

                            setTextNullChack(fields_attributes_1_edittext_value, fields_value_1)
                            setTextNullChack(fields_attributes_2_edittext_value, fields_value_2)
                            setTextNullChack(fields_attributes_3_edittext_value, fields_value_3)
                            setTextNullChack(fields_attributes_4_edittext_value, fields_value_4)

                            Glide.with(this@AccountInfoUpdateActivity)
                                    .load(avatar_url)
                                    .into(avater_image_imageview!!)
                            Glide.with(this@AccountInfoUpdateActivity)
                                    .load(header_url)
                                    .into(header_image_imageview!!)

                            try {
                                supportActionBar!!.setSubtitle(display_name + " ( @" + jsonObject.getString("acct") + " / " + Instance + " )")
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        snackbar[0].dismiss()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            })


            fab = findViewById<View>(R.id.fab) as FloatingActionButton

            //情報を更新する
            //全部入力されてるか確認
            if (displayname_edittext!!.text.toString() != null && note_edittext!!.text.toString() != null) {
                fab!!.setOnClickListener { view ->
                    val replace_snackber = pref_setting!!.getBoolean("pref_one_hand_mode", false)
                    if (replace_snackber) {
                        snackbar[0] = Snackbar.make(view, R.string.upload_info, Snackbar.LENGTH_LONG)
                        snackbar[0].setAction(R.string.update) {
                            //くるくる
                            val view = findViewById<View>(android.R.id.content)
                            snackbar_loading = Snackbar.make(view, getString(R.string.upload_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE)
                            val snackBer_viewGrop = snackbar_loading!!.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
                            //SnackBerを複数行対応させる
                            val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
                            snackBer_textView.maxLines = 2
                            //複数行対応させたおかげでずれたので修正
                            val progressBar = ProgressBar(this@AccountInfoUpdateActivity)
                            val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            progressBer_layoutParams.gravity = Gravity.CENTER
                            progressBar.layoutParams = progressBer_layoutParams
                            snackBer_viewGrop.addView(progressBar, 0)
                            snackbar_loading!!.show()

                            uploadProfile()
                        }.show()
                    } else {
                        //ダイアログ
                        val alertDialog = AlertDialog.Builder(this@AccountInfoUpdateActivity)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(R.string.upload_info)
                        alertDialog.setPositiveButton(R.string.update) { dialog, which ->
                            val view = findViewById<View>(android.R.id.content)
                            snackbar_loading = Snackbar.make(view, getString(R.string.upload_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE)
                            val snackBer_viewGrop = snackbar_loading!!.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
                            //SnackBerを複数行対応させる
                            val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
                            snackBer_textView.maxLines = 2
                            //複数行対応させたおかげでずれたので修正
                            val progressBar = ProgressBar(this@AccountInfoUpdateActivity)
                            val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            progressBer_layoutParams.gravity = Gravity.CENTER
                            progressBar.layoutParams = progressBer_layoutParams
                            snackBer_viewGrop.addView(progressBar, 0)
                            snackbar_loading!!.show()

                            uploadProfile()
                        }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    }
                }
            } else {
                Toast.makeText(this@AccountInfoUpdateActivity, R.string.fillItem, Toast.LENGTH_SHORT).show()
            }
        }
    }

    //画像をアップロードすつ
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val avater_image_imageview = findViewById<ImageView>(R.id.account_update_avatar_imageview)
        val header_image_imageview = findViewById<ImageView>(R.id.account_update_header_imageview)
        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data!!.data

                val filePath = getFileNameUri(selectedImage)
                val file_extn = filePath!!.substring(filePath.lastIndexOf(".") + 1)
                if (file_extn == "img" || file_extn == "jpg" || file_extn == "jpeg" || file_extn == "gif" || file_extn == "png") {
                    if (avatar) {
                        avater_image_imageview.setImageURI(selectedImage)
                        avatar_Uri = selectedImage
                        avatar_post_name = getFileNameUri(selectedImage)
                        avatar_extn = file_extn
                        //Misskey POST
                        if (intent.getBooleanExtra("Misskey", false)) {
                            postMisskeyPhotoPOST(avatar_extn!!, selectedImage, false)
                        }
                    } else {
                        header_image_imageview.setImageURI(selectedImage)
                        header_Uri = selectedImage
                        //header_file = file;
                        header_post_name = getFileNameUri(selectedImage)
                        header_extn = file_extn
                        //Misskey POST
                        if (intent.getBooleanExtra("Misskey", false)) {
                            postMisskeyPhotoPOST(header_extn!!, selectedImage, true)
                        }
                    }
                }
            }

        header = false
        avatar = false
    }


    /**
     * Uri→FileName
     */
    private fun getFileNameUri(uri: Uri?): String? {
        var file_name: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                file_name = cursor.getString(0)
            }
        }
        return file_name
    }


    /**
     * Misskeyアカウント情報
     */
    private fun getMisskeyAccount() {
        val instance = pref_setting!!.getString("misskey_main_instance", "")
        val token = pref_setting!!.getString("misskey_main_token", "")
        val username = pref_setting!!.getString("misskey_main_username", "")
        val url = "https://$instance/api/i"
        SnackberProgress.showProgressSnackber(displayname_edittext, this@AccountInfoUpdateActivity, getString(R.string.loading) + "\n" + url)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
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
                runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@AccountInfoUpdateActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    runOnUiThread {
                        try {
                            val jsonObject = JSONObject(response_string)
                            displayname_edittext!!.setText(jsonObject.getString("name"))
                            note_edittext!!.text = jsonObject.getString("description")
                            val profile = jsonObject.getJSONObject("profile")
                            //null check
                            if (!profile.isNull("birthday")) {
                                birthday_Button.text = profile.getString("birthday")
                            }
                            if (!profile.isNull("location")) {
                                place_EditText!!.setText(profile.getString("location"))
                            }
                            cat_Switch!!.isChecked = java.lang.Boolean.valueOf(jsonObject.getString("isCat"))
                            //画像
                            Glide.with(this@AccountInfoUpdateActivity).load(jsonObject.getString("avatarUrl")).into(avater_image_imageview!!)
                            Glide.with(this@AccountInfoUpdateActivity).load(jsonObject.getString("bannerUrl")).into(header_image_imageview!!)
                            //SubTitle
                            supportActionBar!!.setSubtitle(jsonObject.getString("name") + " ( @" + jsonObject.getString("username") + " / " + instance + " )")
                            //avatar / banner
                            misskey_avatar_id = jsonObject.getString("avatarId")
                            misskey_banner_id = jsonObject.getString("bannerId")

                            SnackberProgress.closeProgressSnackber()

                        } catch (e: JSONException) {

                        }
                    }

                }
            }
        })
    }

    /**
     * Misskey Profile Update
     */
    private fun updateMisskeyProfile() {
        //必須項目が埋まってるかチェック
        if (displayname_edittext!!.text.toString() != null && note_edittext!!.text.toString() != null) {
            Snackbar.make(displayname_edittext!!, R.string.upload_info, Snackbar.LENGTH_LONG).setAction(getString(R.string.update)) {
                //画像アップロードは受け取りのところでやった
                postMisskeyProfile()
            }.show()
        }
    }

    /**
     * Misskey プロフィール更新
     */
    private fun postMisskeyProfile() {
        val instance = pref_setting!!.getString("misskey_main_instance", "")
        val token = pref_setting!!.getString("misskey_main_token", "")
        val username = pref_setting!!.getString("misskey_main_username", "")
        val url = "https://$instance/api/i/update"
        //くるくる
        SnackberProgress.showProgressSnackber(displayname_edittext, this@AccountInfoUpdateActivity, getString(R.string.loading) + "\n" + url)
        //ぱらめーたー
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("name", displayname_edittext!!.text.toString())
            jsonObject.put("description", note_edittext!!.text.toString())
            jsonObject.put("location", place_EditText!!.text.toString())
            jsonObject.put("bannerId", misskey_banner_id)
            jsonObject.put("avatarId", misskey_avatar_id)
            jsonObject.put("birthday", birthday_Button.text.toString())
            jsonObject.put("isCat", cat_Switch!!.isChecked)
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
                runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@AccountInfoUpdateActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    if (!response.isSuccessful) {
                        //失敗
                        runOnUiThread { Toast.makeText(this@AccountInfoUpdateActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@AccountInfoUpdateActivity, R.string.successful, Toast.LENGTH_SHORT).show()
                            SnackberProgress.closeProgressSnackber()
                        }
                    }
                }

            }
        })
    }

    /**
     * Misskey avatarUrl bannerUrl POST
     *
     * @param banner avatarUrlはfalse、bannerはtrue
     */
    private fun postMisskeyPhotoPOST(file_extn_post: String, uri: Uri?, banner: Boolean) {
        val instance = pref_setting!!.getString("misskey_main_instance", "")
        val token = pref_setting!!.getString("misskey_main_token", "")
        val username = pref_setting!!.getString("misskey_main_username", "")
        val url = "https://$instance/api/drive/files/create"
        //Uri → Bitmap → Byte
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val baos = ByteArrayOutputStream()
        bitmap!!.compress(getImageType(file_extn_post), 100, baos)
        val imageBytes = baos.toByteArray()
        //くるくる
        SnackberProgress.showProgressSnackber(displayname_edittext, this@AccountInfoUpdateActivity, getString(R.string.loading) + "\n" + url)
        //ぱらめーたー
        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", getFileNameUri(uri), RequestBody.create(MediaType.parse("image/$file_extn_post"), imageBytes))
                .addFormDataPart("i", token!!)
                .addFormDataPart("force", "true")
                .build()
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
                runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@AccountInfoUpdateActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        val jsonObject = JSONObject(response_string)
                        val media_id_long = jsonObject.getString("id")
                        if (!banner) {
                            //avatar
                            misskey_avatar_id = media_id_long
                        } else {
                            //banner
                            misskey_banner_id = media_id_long
                        }
                        SnackberProgress.closeProgressSnackber()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

            }
        })
    }


    private fun setTextNullChack(id: EditText?, string: String?) {
        if (string != null) {
            id!!.setText(Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT))
        }
    }

    private fun clickMediaSelect() {
        //ストレージ読み込みの権限があるか確認
        //許可してないときは許可を求める
        val REQUEST_PERMISSION = 1000
        if (ContextCompat.checkSelfPermission(this@AccountInfoUpdateActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this@AccountInfoUpdateActivity)
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
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, 1)
        }

    }

    /**
     * context://→file://へ変換する
     */
    fun getPath(uri: Uri): String {
        //uri.getLastPathSegment();
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val column_index = cursor!!
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val imagePath = cursor.getString(column_index)
        cursor.close()
        return imagePath
    }


    /**
     * PNG / JPEG
     */
    private fun getImageType(extn: String): Bitmap.CompressFormat {
        var format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
        when (extn) {
            "jpg" -> format = Bitmap.CompressFormat.JPEG
            "jpeg" -> format = Bitmap.CompressFormat.JPEG
            "png" -> format = Bitmap.CompressFormat.PNG
        }
        return format
    }


    fun uploadProfile() {
        //非同期処理
        Thread(Runnable {
            //編集前の内容にする！！
            //パラメータを設定
            val url = "https://$Instance/api/v1/accounts/update_credentials/?access_token=$AccessToken"
            val builder = HttpUrl.parse(url)!!.newBuilder()
            val final_url = builder.build().toString()

            val form = MultipartBody.Builder()
            form.setType(MultipartBody.FORM)
            form.addFormDataPart("display_name", displayname_edittext!!.text.toString())
            form.addFormDataPart("note", note_edittext!!.text.toString())

            if (fields_attributes_1_edittext_name!!.text.toString() != null && fields_attributes_1_edittext_value!!.text.toString() != null) {
                form.addFormDataPart("fields_attributes[0][name]", fields_attributes_1_edittext_name!!.text.toString())
                form.addFormDataPart("fields_attributes[0][value]", fields_attributes_1_edittext_value!!.text.toString())
            }
            if (fields_attributes_2_edittext_name!!.text.toString() != null && fields_attributes_2_edittext_value!!.text.toString() != null) {
                form.addFormDataPart("fields_attributes[1][name]", fields_attributes_2_edittext_name!!.text.toString())
                form.addFormDataPart("fields_attributes[1][value]", fields_attributes_2_edittext_value!!.text.toString())
            }
            if (fields_attributes_3_edittext_name!!.text.toString() != null && fields_attributes_3_edittext_value!!.text.toString() != null) {
                form.addFormDataPart("fields_attributes[2][name]", fields_attributes_3_edittext_name!!.text.toString())
                form.addFormDataPart("fields_attributes[2][value]", fields_attributes_3_edittext_value!!.text.toString())
            }
            if (fields_attributes_4_edittext_name!!.text.toString() != null && fields_attributes_4_edittext_value!!.text.toString() != null) {
                form.addFormDataPart("fields_attributes[3][name]", fields_attributes_4_edittext_name!!.text.toString())
                form.addFormDataPart("fields_attributes[3][value]", fields_attributes_4_edittext_value!!.text.toString())
            }

            //画像を投げる
            try {
                if (avatar_Uri != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, avatar_Uri)
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(getImageType(avatar_extn!!), 100, baos)
                    val imageBytes = baos.toByteArray()
                    form.addFormDataPart("avatar", getFileNameUri(avatar_Uri), RequestBody.create(MediaType.parse("image/" + avatar_extn!!), imageBytes))
                }
                if (header_Uri != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, header_Uri)
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(getImageType(header_extn!!), 100, baos)
                    val imageBytes = baos.toByteArray()
                    form.addFormDataPart("header", getFileNameUri(header_Uri), RequestBody.create(MediaType.parse("image/" + header_extn!!), imageBytes))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            //ぱらめーたー
            val requestBody = form.build()

            //作成
            val request = Request.Builder()
                    .url(final_url)
                    .patch(requestBody)
                    .build()

            //GETリクエスト
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@AccountInfoUpdateActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                        snackbar_loading!!.dismiss()
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@AccountInfoUpdateActivity, getString(R.string.successful), Toast.LENGTH_SHORT).show()
                            snackbar_loading!!.dismiss()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@AccountInfoUpdateActivity, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show()
                            snackbar_loading!!.dismiss()
                        }
                    }
                }
            })
        }).start()

    }

    companion object {
        lateinit var birthday_Button: Button
    }


}
