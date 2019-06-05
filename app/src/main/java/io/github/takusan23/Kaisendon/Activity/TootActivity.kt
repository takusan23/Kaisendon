package io.github.takusan23.Kaisendon.Activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.AsyncTask
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Accounts
import com.sys1yagi.mastodon4j.api.method.Statuses

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.IOException
import java.util.ArrayList

import io.github.takusan23.Kaisendon.Kaisendon_NowPlaying_Service
import io.github.takusan23.Kaisendon.Preference_ApplicationContext.Companion.context
import io.github.takusan23.Kaisendon.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class TootActivity : AppCompatActivity() {

    private val preferences: SharedPreferences? = null
    private val REQUEST_PERMISSION = 1000

    internal var display_name: String? = null
    internal var user_id: String? = null
    internal var user_avater: String? = null

    internal var image_id: Long = 0
    internal var image_url: String? = null

    internal var NowPlaying_Text: String? = null

    internal var notificationList = ArrayList<String>()

    internal var intentFilter = IntentFilter()

    internal lateinit var NowPlaying_broadcastReceiver: BroadcastReceiver

    internal var media_ids = ArrayList<Long>()

    internal var visibility: Status.Visibility = Status.Visibility.Private

    internal var contact: String? = null

    internal lateinit var toot_LinearLayout: LinearLayout
    internal lateinit var toot_textbox: TextView
    internal lateinit var command_Button: Button
    internal lateinit var pref_setting: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var AccessToken: String? = null
        var instance: String? = null
        //設定のプリファレンス
        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        val dark_mode = pref_setting.getBoolean("pref_dark_theme", false)
        if (dark_mode) {
            setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar)
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        val oled_mode = pref_setting.getBoolean("pref_oled_mode", false)
        if (oled_mode) {
            setTheme(R.style.OLED_Theme)
        } else {
            //なにもない
        }

        setContentView(R.layout.activity_toot)


        contact = intent.getStringExtra("contact")

        toot_textbox = findViewById(R.id.toot_text_public)
        val toot_count = findViewById<TextView>(R.id.toot_count)
        val toot_button = findViewById<Button>(R.id.toot)
        val nya = findViewById<Button>(R.id.nya_n)
        val device_info = findViewById<Button>(R.id.device_info_button)

        toot_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_create_black_24dp_black, 0, 0, 0)

        val pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        //設定のプリファレンス
        val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
            instance = pref_setting.getString("pref_mastodon_instance", "")

        } else {

            AccessToken = pref_setting.getString("main_token", "")
            instance = pref_setting.getString("main_instance", "")

        }

        //find
        val account_name = findViewById<TextView>(R.id.account_name)
        val account_id = findViewById<TextView>(R.id.account_id)
        val avater_imageView = findViewById<ImageView>(R.id.avater_imageview)

        //image
        val add_image_button = findViewById<Button>(R.id.add_image_button)
        val now_playing_button = findViewById<Button>(R.id.nowplaying_button)

        //すぴなー
        val spinner = findViewById<Spinner>(R.id.visibility_spinner)

        //LinearLayou
        toot_LinearLayout = findViewById(R.id.toot_LinearLayout)


        //作者に連絡
        try {
            toot_textbox.append(contact)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }

        //背景
        val background_imageView = findViewById<ImageView>(R.id.activity_toot_background_imageview)

        if (pref_setting.getBoolean("background_image", true)) {
            val uri = Uri.parse(pref_setting.getString("background_image_path", ""))
            Glide.with(this@TootActivity)
                    .load(uri)
                    .into(background_imageView)
        }

        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f).toDouble() != 0.1) {
            background_imageView.alpha = pref_setting.getFloat("transparency", 1.0f)
        }


        //画像付き
        //とりあえず画像を選べるアクティビティへー
        add_image_button.setOnClickListener {
            //ストレージ読み込みの権限があるか確認
            //許可してないときは許可を求める
            if (ContextCompat.checkSelfPermission(this@TootActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this@TootActivity)
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


        //Wi-Fi接続状況確認
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false)
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)

        //アバター画像と名前
        val finalInstance1 = instance
        val finalAccessToken1 = AccessToken
        object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg string: String): String? {
                val client = MastodonClient.Builder(finalInstance1!!, OkHttpClient.Builder(), Gson()).accessToken(finalAccessToken1!!).build()

                try {
                    val account = Accounts(client).getVerifyCredentials().execute()

                    display_name = account.displayName
                    user_id = account.userName

                    user_avater = account.avatar

                    //UIを変更するために別スレッド呼び出し
                    runOnUiThread {
                        //表示設定
                        if (setting_avater_hidden) {

                            avater_imageView.setImageResource(R.drawable.ic_person_black_24dp)

                        }
                        //Wi-Fi
                        if (setting_avater_wifi) {
                            if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                                if (setting_avater_gif) {

                                    //GIFアニメ再生させない
                                    Picasso.get()
                                            .load(user_avater)
                                            .resize(100, 100)
                                            .placeholder(R.drawable.ic_refresh_black_24dp)
                                            .into(avater_imageView)

                                } else {

                                    //GIFアニメを再生
                                    Glide.with(applicationContext)
                                            .load(user_avater)
                                            .apply(RequestOptions().override(100, 100).placeholder(R.drawable.ic_refresh_black_24dp))
                                            .into(avater_imageView)
                                }
                            }

                        } else {

                            avater_imageView.setImageResource(R.drawable.ic_person_black_24dp)

                        }


                        account_name.text = display_name
                        account_id.text = "@$user_id@$finalInstance1"
                    }


                } catch (e: Mastodon4jRequestException) {
                    e.printStackTrace()
                }


                return null
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        //公開範囲設定
        val visibility_list = arrayOf(getString(R.string.visibility_public), getString(R.string.visibility_unlisted), getString(R.string.visibility_private), getString(R.string.visibility_direct))
        val spineer_adapter = ArrayAdapter(this@TootActivity, android.R.layout.simple_spinner_item, visibility_list)
        spineer_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = spineer_adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    visibility = Status.Visibility.Public
                } else if (position == 1) {
                    visibility = Status.Visibility.Unlisted
                } else if (position == 2) {
                    visibility = Status.Visibility.Private
                } else if (position == 3) {
                    visibility = Status.Visibility.Direct
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }


        val finalAccessToken = AccessToken
        val finalInstance = instance

        //にゃーん
        nya.setOnClickListener { toot_textbox.append("にゃーん") }
        toot_button.setOnClickListener {
            val toot_text = toot_textbox.text.toString()

            //ダイアログ出すかどうか
            val accessToken_boomelan = pref_setting.getBoolean("pref_toot_dialog", false)
            if (accessToken_boomelan) {

                val alertDialog = AlertDialog.Builder(this@TootActivity)
                alertDialog.setTitle(R.string.confirmation)
                alertDialog.setMessage(R.string.toot_dialog)
                alertDialog.setPositiveButton(R.string.toot) { dialog, which ->
                    //トゥートああああ

                    object : AsyncTask<String, Void, String>() {

                        override fun doInBackground(vararg params: String): String {
                            val accessToken = AccessToken()
                            accessToken.accessToken = finalAccessToken!!

                            val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson()).accessToken(accessToken.accessToken).build()
                            try {
                                Statuses(client).postStatus(toot_text, null, null, false, null, visibility).execute()
                            } catch (e: Mastodon4jRequestException) {
                                e.printStackTrace()
                            }

                            return toot_text
                        }

                        override fun onPostExecute(result: String) {
                            Toast.makeText(applicationContext, "トゥートしました : $result", Toast.LENGTH_SHORT).show()
                        }

                    }.execute()
                    toot_textbox.text = "" //投稿した後に入力フォームを空にする
                }
                alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                alertDialog.create().show()

            } else {

                //トゥートああああ
                object : AsyncTask<String, Void, String>() {

                    override fun doInBackground(vararg params: String): String {
                        val accessToken = AccessToken()
                        accessToken.accessToken = finalAccessToken

                        val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson()).accessToken(accessToken.accessToken).build()
                        try {
                            Statuses(client).postStatus(toot_text, null, null, false, null, visibility).execute()
                        } catch (e: Mastodon4jRequestException) {
                            e.printStackTrace()
                        }

                        return toot_text
                    }

                    override fun onPostExecute(result: String) {
                        Toast.makeText(applicationContext, "トゥートしました : $result", Toast.LENGTH_SHORT).show()
                    }

                }.execute()
                toot_textbox.text = "" //投稿した後に入力フォームを空にする
            }


            //                Toast.makeText(getApplicationContext(),token,Toast.LENGTH_SHORT).show();

            stopNowPlayingService()
        }


        toot_count.text = "文字数カウント : " + "0/500"


        //文字カウント
        //コマンド機能？
        command_Button = Button(this@TootActivity)
        toot_textbox.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var textColor = Color.GRAY

                // 入力文字数の表示
                val txtLength = s.length
                toot_count.text = "文字数カウント : " + Integer.toString(txtLength) + "/500"

                // 指定文字数オーバーで文字色を赤くする
                if (txtLength > 500) {
                    textColor = Color.RED
                }
                toot_count.setTextColor(textColor)


                //コマンド機能
                commandSet("/sushi", "command_sushi")
                commandSetNotPreference("/fav-home", "fav-home")
                commandSetNotPreference("/rate-limit", "rate-limit")

            }

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        })


        //新しいトゥート画面に
        //お引越し
        /*
        //共有メニューから押したときの処理
        Intent share_intent = getIntent();
        String share_string = share_intent.getAction();
        try {
            if (share_string.equals(Intent.ACTION_SEND)) {
                Bundle bundle = share_intent.getExtras();
                if (bundle != null) {
                    CharSequence text = bundle.getCharSequence(Intent.EXTRA_TEXT);
                    if (text != null) {
                        toot_textbox.append(text);
                    }
                }
            }
        } catch (NullPointerException e) {

        }
*/

        // thinking
        toot_button.setOnLongClickListener {
            toot_textbox.append("🤔")
            false
        }


        //NowPlaying
        now_playing_button.text = getString(R.string.NowPlaying)
        now_playing_button.setOnClickListener {
            //通知リスナーの権限がある確認
            val cn = ComponentName(this@TootActivity, Kaisendon_NowPlaying_Service::class.java)
            val flat = Settings.Secure.getString(this@TootActivity.contentResolver, "enabled_notification_listeners")
            val enabled = flat != null && flat.contains(cn.flattenToString())
            if (enabled) {

                //ダイアログ
                val editText_App_Name = EditText(this@TootActivity)
                editText_App_Name.append(pref_setting.getString("Now_Playing_AppName", ""))
                val alertDialog_editTranspatency = AlertDialog.Builder(this@TootActivity)
                alertDialog_editTranspatency.setView(editText_App_Name)
                alertDialog_editTranspatency.setTitle(getString(R.string.Now_Playing_Dialog_title))
                        .setMessage(getString(R.string.Now_Playing_Dialog_message))
                        .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                            val editAppName = editText_App_Name.text.toString()
                            val editor = pref_setting.edit()
                            editor.putString("Now_Playing_AppName", editAppName)
                            editor.apply()

                            //ブロードキャスト受信
                            NowPlaying_broadcastReceiver = object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent) {
                                    var title: String? = null
                                    //ArrayList<String> notificationList = new ArrayList<String>();
                                    val bundle = intent.extras
                                    title = bundle!!.getString("title")
                                    // System.out.println("通知 : " + title);

                                    //テキストボックスにいれる

                                    //if (!toot_textbox.getText().toString().contains(title)) {
                                    toot_textbox.append(title)
                                    //}

                                    unregisterReceiver(NowPlaying_broadcastReceiver)
                                    //stopNowPlayingService();


                                    val stop_service = Intent()
                                    stop_service.action = "Stop_Now_Playing"
                                    sendBroadcast(stop_service)


                                }
                            }
                            //ブロードキャスト関係
                            intentFilter.addAction("Now_Playing")
                            registerReceiver(NowPlaying_broadcastReceiver, intentFilter)
                            val intent = Intent(this@TootActivity, Kaisendon_NowPlaying_Service::class.java)
                            //startService(intent);
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()

            } else {
                //権限を取りに行く
                AlertDialog.Builder(this@TootActivity)
                        .setTitle(getString(R.string.listenerservice_dialog_title))
                        .setMessage(getString(R.string.listenerservice_dialog_message))
                        .setPositiveButton("OK") { dialog, which ->
                            val intent = Intent()
                            intent.action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                            startActivity(intent)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
            }
        }

        //長押しで設定画面に飛ばす
        now_playing_button.setOnLongClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            startActivity(intent)

            false
        }


        //端末情報
        val device_name = booleanArrayOf(false)
        val android_version = booleanArrayOf(false)
        val maker = booleanArrayOf(false)
        val sdk_version = booleanArrayOf(false)
        val code_name = booleanArrayOf(false)
        val battery = booleanArrayOf(false)

        var codeName: String? = null

        //コードネーム変換（手動

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            codeName = "Nougat"
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            codeName = "Oreo"
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            codeName = "Pie"
        }


        //ダイアログ生成
        val finalCodeName = codeName
        device_info.setOnClickListener {
            //ダイアログを出す
            val items = arrayOf(getString(R.string.device_name), getString(R.string.android_version), getString(R.string.maker), getString(R.string.sdk_version), getString(R.string.codename), getString(R.string.battery_level))
            val checkedItems = ArrayList<Int>()
            AlertDialog.Builder(this@TootActivity)
                    .setTitle(R.string.device_info)
                    .setMultiChoiceItems(items, null) { dialog, which, isChecked ->
                        if (which == 0 && isChecked) {
                            device_name[0] = true
                        } else if (which == 0 && !isChecked) {
                            device_name[0] = false
                        } else if (which == 1 && isChecked) {
                            android_version[0] = true
                        } else if (which == 1 && !isChecked) {
                            android_version[0] = false
                        } else if (which == 2 && isChecked) {
                            maker[0] = true
                        } else if (which == 2 && !isChecked) {
                            maker[0] = false
                        } else if (which == 3 && isChecked) {
                            sdk_version[0] = true
                        } else if (which == 3 && !isChecked) {
                            sdk_version[0] = false
                        } else if (which == 4 && isChecked) {
                            code_name[0] = true
                        } else if (which == 4 && !isChecked) {
                            code_name[0] = false
                        } else if (which == 5 && isChecked) {
                            battery[0] = true
                        } else if (which == 5 && !isChecked) {
                            battery[0] = false
                        }
                    }
                    .setPositiveButton(R.string.ok) { dialog, which ->
                        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                        //テキストボックスに入れる
                        if (device_name[0]) {
                            toot_textbox.append(Build.MODEL)
                            toot_textbox.append("\r\n")
                        }
                        if (android_version[0]) {
                            toot_textbox.append(Build.VERSION.RELEASE)
                            toot_textbox.append("\r\n")
                        }
                        if (maker[0]) {
                            toot_textbox.append(Build.BRAND)
                            toot_textbox.append("\r\n")
                        }
                        if (sdk_version[0]) {
                            toot_textbox.append(Build.VERSION.SDK_INT.toString())
                            toot_textbox.append("\r\n")
                        }
                        if (code_name[0]) {
                            toot_textbox.append(finalCodeName)
                            toot_textbox.append("\r\n")
                        }
                        if (battery[0]) {
                            toot_textbox.append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toString() + "%")
                            toot_textbox.append("\r\n")
                        }

                        //falseに戻す
                        device_name[0] = false
                        android_version[0] = false
                        maker[0] = false
                        sdk_version[0] = false
                        code_name[0] = false
                        battery[0] = false

                        for (i in checkedItems) {
                            // item_i checked
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()

            //toot_textbox.append(Build.MODEL + Build.BRAND + Build.VERSION.RELEASE);
        }

    }


    /*

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

            LinearLayout add_image_linearLayout = findViewById(R.id.media_linearLayout);


            String AccessToken = null;
            String instance = null;
            //設定のプリファレンス
            SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
            boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
            if (accessToken_boomelan) {

                AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
                instance = pref_setting.getString("pref_mastodon_instance", "");

            } else {

                AccessToken = pref_setting.getString("main_token", "");
                instance = pref_setting.getString("main_instance", "");

            }
            final TextView toot_textbox = findViewById(R.id.toot_text_public);
            final TextView toot_count = findViewById(R.id.toot_count);
            Button toot_button = findViewById(R.id.toot);
            Button nya = findViewById(R.id.nya_n);


            if (requestCode == RESULT_PICK_IMAGEFILE && resultCode == Activity.RESULT_OK) {
                if (resultData.getData() != null) {
                    ParcelFileDescriptor pfDescriptor = null;
                    try {
                        Uri uri = resultData.getData();

    */
    /*
                    if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                        //ギャラリーからの場合
                        String id = DocumentsContract.getDocumentId(resultData.getData());
                        String selection = "_id=?";
                        String[] selectionArgs = new String[]{id.split(":")[1]};

                        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns.DATA}, selection, selectionArgs, null);

                        if (cursor.moveToFirst()) {
                            File file = new File(cursor.getString(0));
                            System.out.println("ディレクトリ : " + file);
                            // fileから写真を読み込む
                        }

                    }
*//*


                    pfDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                    if (pfDescriptor != null) {
                        FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
                        Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        pfDescriptor.close();

                        //動的にレイアウト作成
                        ImageView add_image_imageview = new ImageView(this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
                        add_image_imageview.setLayoutParams(layoutParams);
                        add_image_linearLayout.addView(add_image_imageview);

                        add_image_imageview.setImageURI(uri);

                        System.out.println("LINK : " + uri);

                        //がぞうをアップロード
                        String finalInstance = instance;
                        String finalAccessToken = AccessToken;

                        System.out.println(String.valueOf("イメージID : " + uri.toString()));

                        //URI > File
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];
                        if ("primary".equalsIgnoreCase(type)) {
                            String test =  Environment.getExternalStorageDirectory() + "/" + split[1];
                            System.out.println("てすお : " + test);
                        }else {
                            String test= "/stroage/" + type +  "/" + split[1];
                            System.out.println("てすよ : " + test);
                        }

                        new AsyncTask<String, Void, String>() {

                            @Override
                            protected String doInBackground(String... string) {
                                File file = new File(uri.toString());
                                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken).build();
                                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                //MultipartBody.Part part = MultipartBody.Part.createFormData("multipart/form-data", path);
                                MultipartBody.Part part = MultipartBody.Part.createFormData("multipart/form-data", "file:///storage/emulated/0/Sketch/test.png");

                                try {
                                    Attachment media = new Media(client).postMedia(part).execute();

                                    image_id = media.getId();
                                    String url = media.getPreviewUrl();

                                    System.out.println(String.valueOf("イメージID : " + image_id));
                                    System.out.println(String.valueOf("リンクID : " + url));


                                } catch (Mastodon4jRequestException e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }

                            //もしかしたらだめかも？
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (pfDescriptor != null) {
                            pfDescriptor.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }
*/
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)

        val add_image_linearLayout = findViewById<LinearLayout>(R.id.media_linearLayout)
        val background_imageView = findViewById<ImageView>(R.id.activity_toot_background_imageview)

        if (media_ids.size < 4) {
            if (requestCode == 1)
                if (resultCode == Activity.RESULT_OK) {
                    val selectedImage = data!!.data

                    val filePath = getPath(selectedImage)
                    val file_extn = filePath.substring(filePath.lastIndexOf(".") + 1)
                    val file = File(filePath)
                    val finalPath = "file:\\\\$filePath"

                    //image_name_tv.setText(filePath);

                    if (file_extn == "img" || file_extn == "jpg" || file_extn == "jpeg" || file_extn == "gif" || file_extn == "png") {
                        //System.out.println("パス : " + finalPath.replaceAll("\\\\", "/"));
                        //System.out.println("拡張子 : " + file_extn);
                        //System.out.println("ファイル名 : " + file.getName());

                        //動的にレイアウト作成
                        val add_image_imageview = ImageView(this)
                        val layoutParams = LinearLayout.LayoutParams(200, 200)
                        add_image_imageview.layoutParams = layoutParams
                        add_image_linearLayout.addView(add_image_imageview)
                        add_image_imageview.setImageURI(selectedImage)


                        //画像を投げるだけ
                        val asyncTask = object : AsyncTask<String, Void, String>() {
                            override fun doInBackground(vararg string: String): String? {
                                var AccessToken: String? = null
                                var instance: String? = null
                                //設定のプリファレンス
                                val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
                                val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
                                if (accessToken_boomelan) {
                                    AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
                                    instance = pref_setting.getString("pref_mastodon_instance", "")
                                } else {
                                    AccessToken = pref_setting.getString("main_token", "")
                                    instance = pref_setting.getString("main_instance", "")
                                }

                                val view = findViewById<View>(android.R.id.content)
                                val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                                /*
                                 *  ここから　MultiPartBody/FormDataをつかった画像POST
                                 *  念願の画像POSTです！！！！！！！！！！！
                                 *
                                 */

                                //くるくる

                                val snackbar = Snackbar.make(view, getString(R.string.upload_image) + "\r\n /api/v1/media", Snackbar.LENGTH_INDEFINITE)
                                val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
                                //SnackBerを複数行対応させる
                                val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
                                snackBer_textView.maxLines = 2
                                //複数行対応させたおかげでずれたので修正
                                val progressBar = ProgressBar(this@TootActivity)
                                progressBer_layoutParams.gravity = Gravity.CENTER
                                progressBar.layoutParams = progressBer_layoutParams
                                snackBer_viewGrop.addView(progressBar, 0)
                                snackbar.show()


                                val okHttpClient = OkHttpClient()

                                val url_link = "https://$instance/api/v1/media/"

                                val requestBody = MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("image/$file_extn"), file))
                                        .addFormDataPart("access_token", AccessToken!!)
                                        .build()

                                val request = Request.Builder()
                                        .url(url_link)
                                        .post(requestBody)
                                        .build()

                                val request_string = ""

                                okHttpClient.newCall(request).enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {

                                    }

                                    @Throws(IOException::class)
                                    override fun onResponse(call: Call, response: Response) {
                                        val response_string = response.body()!!.string()
                                        //System.out.println("レスポンス : " + response_string);

                                        try {
                                            val jsonObject = JSONObject(response_string)
                                            val media_id_long = jsonObject.getLong("id")
                                            media_ids.add(media_id_long)
                                            runOnUiThread {
                                                snackbar.dismiss()
                                                add_image_imageview.tag = media_ids.size
                                            }
                                        } catch (e: JSONException) {
                                            e.printStackTrace()
                                        }

                                    }
                                })

                                val toot_button = findViewById<Button>(R.id.toot)
                                val toot_textbox = findViewById<TextView>(R.id.toot_text_public)
                                val client = MastodonClient.Builder(instance!!, OkHttpClient.Builder(), Gson()).accessToken(AccessToken).build()

                                //くるくる
                                val snackbar_status = Snackbar.make(view, "トゥート！ \r\n/api/v1/statuses", Snackbar.LENGTH_INDEFINITE)
                                val snackBer_viewGrop_status = snackbar_status.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
                                //SnackBerを複数行対応させる
                                val snackBer_textView_status = snackBer_viewGrop_status.findViewById<View>(R.id.snackbar_text) as TextView
                                snackBer_textView_status.maxLines = 2
                                //複数行対応させたおかげでずれたので修正
                                val progressBar_status = ProgressBar(this@TootActivity)
                                val progressBer_layoutParams_status = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                progressBer_layoutParams_status.gravity = Gravity.CENTER
                                progressBar_status.layoutParams = progressBer_layoutParams
                                snackBer_viewGrop_status.addView(progressBar_status, 0)

                                toot_button.setOnClickListener {
                                    //ダイアログ出すかどうか
                                    val accessToken_boomelan = pref_setting.getBoolean("pref_toot_dialog", false)
                                    if (accessToken_boomelan) {
                                        val alertDialog = AlertDialog.Builder(this@TootActivity)
                                        alertDialog.setTitle(R.string.confirmation)
                                        alertDialog.setMessage(R.string.toot_dialog)
                                        alertDialog.setPositiveButton(R.string.toot) { dialog, which ->
                                            object : AsyncTask<String, String, String>() {
                                                override fun doInBackground(vararg params: String): String {
                                                    try {
                                                        Statuses(client).postStatus(toot_textbox.text.toString(), null, media_ids, false, null, visibility).execute()
                                                    } catch (e: Mastodon4jRequestException) {
                                                        e.printStackTrace()
                                                    }

                                                    return toot_textbox.text.toString()
                                                }

                                                override fun onPostExecute(result: String) {
                                                    Toast.makeText(applicationContext, "トゥートしました : $result", Toast.LENGTH_SHORT).show()
                                                }
                                            }.execute()
                                        }
                                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                                        alertDialog.create().show()
                                    } else {
                                        object : AsyncTask<String, String, String>() {
                                            override fun doInBackground(vararg params: String): String {
                                                try {
                                                    Statuses(client).postStatus(toot_textbox.text.toString(), null, media_ids, false, null, visibility).execute()
                                                } catch (e: Mastodon4jRequestException) {
                                                    e.printStackTrace()
                                                }

                                                return toot_textbox.text.toString()
                                            }

                                            override fun onPostExecute(result: String) {
                                                Toast.makeText(applicationContext, "トゥートしました : $result", Toast.LENGTH_SHORT).show()
                                            }
                                        }.execute()
                                    }
                                }


                                // System.out.println("=====" + client.post("statuses", requestBody));


                                /*
                            try {

                                Attachment media = new Media(client).postMedia(part).execute();

                                image_id = media.getId();
                                image_url = media.getPreviewUrl();

                                System.out.println("イメージID : " + String.valueOf(image_id));
                                System.out.println("イメージURL : " + image_url);

                                long aa = 6517780;

                                List<Long> media_list = new ArrayList<Long>();
                                media_list.add(aa);


                                //com.sys1yagi.mastodon4j.api.entity.Status statuses = new Statuses(client).postStatus("テスト",null,media_list,false, "").execute();


                            } catch (Mastodon4jRequestException e) {
                                e.printStackTrace();
                            }

*/


                                return null
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


                        //画像を消す
                        add_image_imageview.setOnClickListener {
                            //Toast.makeText(getContext(), String.valueOf((int) add_image_imageview.getTag()), Toast.LENGTH_SHORT).show();
                            val media_number = add_image_imageview.tag as Int

                            AlertDialog.Builder(this@TootActivity)
                                    .setTitle(R.string.confirmation)
                                    .setMessage(R.string.media_delete)
                                    .setPositiveButton(R.string.delete_ok) { dialog, which ->
                                        // OK button pressed
                                        try {
                                            if (media_ids.size >= media_number) {
                                                media_ids.removeAt(media_number - 1)
                                                add_image_linearLayout.removeViewAt(media_number - 1)
                                            }
                                        } catch (e: IndexOutOfBoundsException) {
                                            media_ids.clear()
                                            runOnUiThread { add_image_linearLayout.removeViewAt(2) }
                                        }
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                        }


                        //FINE
                    } else {
                        //NOT IN REQUIRED FORMAT
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopNowPlayingService()
        try {
            unregisterReceiver(NowPlaying_broadcastReceiver)
        } catch (e: RuntimeException) {

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

    /*
    private static class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // ブロードキャストを受け取った時の処理を記述
            // 今回はログを出力しています。
            Log.i(getClass().getSimpleName(), intent.getStringExtra("KEY"));
        }
    }
*/

    private fun stopNowPlayingService() {
        //        Intent intent_stop = new Intent(TootActivity.this, Kaisendon_NowPlaying_Service.class);
        //        stopService(intent_stop);
    }

    private fun commandSet(commandText: String, prefKey: String) {
        //コマンド機能
        if (toot_textbox.text.toString().contains(commandText)) {
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            command_Button.layoutParams = layoutParams
            command_Button.setText(R.string.command_run)
            command_Button.setOnClickListener { v ->
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
            toot_LinearLayout.addView(command_Button)
        } else {
            toot_LinearLayout.removeView(command_Button)
        }
    }


    private fun commandSetNotPreference(commandText: String, commandType: String) {
        //コマンド機能
        if (toot_textbox.text.toString().contains(commandText)) {
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            command_Button.layoutParams = layoutParams
            command_Button.setText(R.string.command_run)
            command_Button.setOnClickListener { v ->
                //スナックばー
                Snackbar.make(v, R.string.command_run_message, Snackbar.LENGTH_SHORT).setAction(R.string.run) {
                    //レートリミット
                    if (commandType.contains("rate-limit")) {
                        getMyRateLimit()
                    }
                    //favコマンド
                    if (commandType.contains("fav-home")) {
                        favCommand("home")
                    }
                }.show()
            }
            toot_LinearLayout.addView(command_Button)
        } else {
            //toot_LinearLayout.removeView(command_Button);
        }
    }

    //れーとりみっとかくにん
    private fun getMyRateLimit() {
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

                //UI すれっどから動かす
                runOnUiThread {
                    toot_textbox.append("\n")
                    toot_textbox.append(getString(R.string.ratelimit_limit) + "(x-ratelimit-limit) : " + rateLimit + "\n")
                    toot_textbox.append(getString(R.string.ratelimit_remaining) + "(x-ratelimit-remaining) : " + rateLimit_nokori + "\n")
                    toot_textbox.append(getString(R.string.ratelimit_reset) + "(x-ratelimit-reset) : " + rateLimit_time + "\n")
                }


            }
        })
    }

    //ふぁぼる
    fun favCommand(timeline: String) {
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

        val url = "https://$instance/api/v1/timelines/$timeline/?access_token=$AccessToken"
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
                        println("れすぽんす : $id")
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
                        /*
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                            }
                        });
*/

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    companion object {
        private val RESULT_PICK_IMAGEFILE = 1001
    }


}
