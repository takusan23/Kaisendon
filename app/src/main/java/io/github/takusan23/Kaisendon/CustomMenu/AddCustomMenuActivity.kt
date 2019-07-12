package io.github.takusan23.Kaisendon.CustomMenu

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class AddCustomMenuActivity : AppCompatActivity() {
    private val noFavCode = 2525
    private val yesFavCode = 25


    private var account_menuBuilder: MenuBuilder? = null
    private var account_optionsMenu: MenuPopupHelper? = null
    private var helper: CustomMenuSQLiteHelper? = null
    private var db: SQLiteDatabase? = null

    private var misskey_Switch: Switch? = null

    private var linearLayout: LinearLayout? = null
    private var name_EditText: EditText? = null
    private var load_Button: Button? = null
    private var account_Button: Button? = null
    private var background_image_set_Button: Button? = null
    private var background_image_reset_Button: Button? = null
    private var background_image_ImageView: ImageView? = null
    private var dialog_Switch: Switch? = null
    private var image_Switch: Switch? = null
    private var dark_Switch: Switch? = null
    private var streaming_Switch: Switch? = null
    private var quickprofile_Switch: Switch? = null
    private var tootcounter_Switch: Switch? = null
    private var custom_emoji_Switch: Switch? = null
    private var gif_Switch: Switch? = null
    private var one_hand_Switch: Switch? = null
    private var subtitle_EditText: EditText? = null
    private var background_screen_fit_Switch: Switch? = null
    private var background_transparency: EditText? = null
    private var font_Button: Button? = null
    private var font_reset_Button: Button? = null
    private var font_TextView: TextView? = null
    private var fab: FloatingActionButton? = null

    /*
    private ImageView no_favourite_ImageView;
    private Button no_favourite_Button;
    private Button no_favourite_Delete_Button;
    private ImageView yes_favourite_ImageView;
    private Button yes_favourite_Button;
    private Button yes_favourite_Delete_Button;
*/

    private var pref_setting: SharedPreferences? = null

    private var typeface: Typeface? = null

    private var load_url: String? = null
    private var instance: String? = null
    private var access_token: String? = null

    //画像のURL
    private var image_url = ""
    //ふぉんとのパス
    private var font_path = ""
    //misskey
    private var misskey_username = ""
    //お気に入りボタン
    private var no_fav_icon_path: String? = null
    private var yes_fav_icon_path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ダークモード設定
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)
        setContentView(R.layout.activity_add_custom_menu)
        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

        setTitle(R.string.custom_menu_add)

        fab = findViewById(R.id.custom_menu_add_fab)
        linearLayout = findViewById(R.id.add_custom_menu_linearlayout)
        name_EditText = findViewById(R.id.custom_menu_name_edittext_edittext)
        load_Button = findViewById(R.id.custom_menu_load)
        account_Button = findViewById(R.id.custom_menu_account)
        dialog_Switch = findViewById(R.id.custom_menu_dialog)
        image_Switch = findViewById(R.id.custom_menu_image)
        dark_Switch = findViewById(R.id.custom_menu_darkmode)
        streaming_Switch = findViewById(R.id.custom_menu_streaming)
        quickprofile_Switch = findViewById(R.id.custom_menu_quickprofile)
        tootcounter_Switch = findViewById(R.id.custom_menu_tootcounter)
        custom_emoji_Switch = findViewById(R.id.custom_menu_custom_emoji)
        gif_Switch = findViewById(R.id.custom_menu_gif)
        subtitle_EditText = findViewById(R.id.custom_menu_subtitle_edittext_edittext)
        background_image_set_Button = findViewById(R.id.custom_background_image_button)
        background_image_reset_Button = findViewById(R.id.custom_background_image_reset_button)
        background_image_ImageView = findViewById(R.id.custom_background_image_imageview)
        background_screen_fit_Switch = findViewById(R.id.custom_menu_background_screen_fit_switch)
        background_transparency = findViewById(R.id.custom_menu_background_transoarency_edittext_edittext)
        font_Button = findViewById(R.id.custom_menu_font)
        font_TextView = findViewById(R.id.font_textView)
        font_reset_Button = findViewById(R.id.custom_menu_font_reset)
        one_hand_Switch = findViewById(R.id.custom_menu_one_hand)
        misskey_Switch = findViewById(R.id.misskey_switch)
        /*
        no_favourite_Button = findViewById(R.id.no_favouriteButton);
        no_favourite_ImageView = findViewById(R.id.no_favourite_imageview);
        no_favourite_Delete_Button = findViewById(R.id.no_favouriteDeleteButton);
        yes_favourite_Button = findViewById(R.id.yes_favouriteButton);
        yes_favourite_ImageView = findViewById(R.id.yes_favourite_imageview);
        yes_favourite_Delete_Button = findViewById(R.id.yes_favouriteDeleteButton);
*/
        //クイックプロフィール、カスタム絵文字を既定で有効
        quickprofile_Switch!!.isChecked = true
        custom_emoji_Switch!!.isChecked = true

        //てーま
        darkModeSupport.setSwitchThemeColor(dialog_Switch!!)
        darkModeSupport.setSwitchThemeColor(image_Switch!!)
        darkModeSupport.setSwitchThemeColor(dark_Switch!!)
        darkModeSupport.setSwitchThemeColor(streaming_Switch!!)
        darkModeSupport.setSwitchThemeColor(quickprofile_Switch!!)
        darkModeSupport.setSwitchThemeColor(tootcounter_Switch!!)
        darkModeSupport.setSwitchThemeColor(custom_emoji_Switch!!)
        darkModeSupport.setSwitchThemeColor(one_hand_Switch!!)
        darkModeSupport.setSwitchThemeColor(gif_Switch!!)

        //SQLite
        if (helper == null) {
            helper = CustomMenuSQLiteHelper(applicationContext)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            db!!.disableWriteAheadLogging()
        }

        //削除ボタン
        //ListViewから来たとき
        if (intent.getBooleanExtra("delete_button", false)) {
            //タイトル変更
            setTitle(R.string.custom_menu_update_title)
            val name = intent.getStringExtra("name")
            //ボタンを動的に生成
            val delete_Button = Button(this)
            delete_Button.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            delete_Button.background = getDrawable(R.drawable.button_style)
            delete_Button.setText(R.string.custome_menu_delete)
            delete_Button.setOnClickListener { v ->
                Snackbar.make(v, R.string.custom_setting_delete_message, Snackbar.LENGTH_SHORT).setAction(R.string.delete_ok) {
                    db!!.delete("custom_menudb", "name=?", arrayOf(name))
                    startActivity(Intent(this@AddCustomMenuActivity, Home::class.java))
                }.show()
            }
            loadSQLite(name)
            linearLayout!!.addView(delete_Button)
        }


        //メニュー
        setLoadMenu()

        //Misskeyモード?
        misskey_Switch!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //チェックした
                setLoadMisskeyMenu()
            } else {
                //チェックしてない
                setLoadMenu()
            }
        }


        fab!!.setOnClickListener { v ->
            //更新・新規作成
            if (!intent.getBooleanExtra("delete_button", false)) {
                //新規作成
                //SnackBer
                Snackbar.make(v, R.string.custom_add_message, Snackbar.LENGTH_SHORT).setAction(R.string.register) {
                    saveSQLite()
                    //戻る
                    startActivity(Intent(this, Home::class.java))
                }.show()
            } else {
                //SnackBer
                Snackbar.make(v, R.string.custom_menu_update, Snackbar.LENGTH_SHORT).setAction(R.string.update) {
                    //更新
                    val name = intent.getStringExtra("name")
                    updateSQLite(name)
                    //戻る
                    startActivity(Intent(this, Home::class.java))
                }.show()
            }
        }

        //背景画像
        background_setting()
        //フォント
        font_setting()
        //お気に入りボタン
        //favButtonSetting()
    }

    /**
     * 画像受け取り
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (resultData!!.data != null) {
                val selectedImage = resultData.data
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    //完全パス取得
                    val get_Path = getPath(selectedImage)
                    val image_Path = "file:\\\\$get_Path"
                    //置き換え
                    val final_Path = image_Path.replace("\\\\".toRegex(), "/")
                    image_url = final_Path
                    //いれておく？
                    background_image_set_Button!!.text = image_url
                    //URI画像を入れる
                    Glide.with(this)
                            .load(get_Path)
                            .into(background_image_ImageView!!)
                } else {
                    //Scoped StorageのせいでURL取得できなくなったのでURIで管理する
                    image_url = selectedImage!!.toString()
                    //いれておく？
                    background_image_set_Button!!.text = image_url
                    //URI画像を入れる
                    Glide.with(this)
                            .load(image_url)
                            .into(background_image_ImageView!!)
                }
            }
        }
        if (requestCode == 4545 && resultCode == Activity.RESULT_OK) {
            val uri = resultData!!.data
            //String変換（非正規ルート？）
            val path = uri!!.path
            //Android QのScoped Storageのおかげで使えなくなった。
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                //Android Pie以前
                //「/document/raw:」を消す
                //「/document/primary:」を消す
                if (path!!.contains("/document/raw:")) {
                    font_path = path.replace("/document/raw:", "")
                }
                if (path.contains("/document/primary:")) {
                    font_path = path.replace("/document/primary:", "storage/emulated/0/")
                }
            }
            //content://からfile://へ変換する
            typeface = Typeface.createFromFile(File(font_path))
            font_TextView!!.setTypeface(typeface)
            font_Button!!.text = font_path
        }
        /*
        //お気に入りボタン変更
        if (resultData.getData() != null) {
            Uri get_Path = resultData.getData();
            //String image_Path = "file:\\\\" + get_Path;
            //置き換え
            //String final_Path = image_Path.replaceAll("\\\\", "/");
            //image_url = final_Path;
            if (requestCode == noFavCode && resultCode == Activity.RESULT_OK) {
                no_favourite_Button.setText(get_Path.toString());
                no_fav_icon_path = get_Path.toString();
                //URI画像を入れる
                Glide.with(getContext())
                        .load(get_Path)
                        .into(no_favourite_ImageView);
            } else if (requestCode == yesFavCode && resultCode == Activity.RESULT_OK) {
                yes_favourite_Button.setText(get_Path.toString());
                yes_fav_icon_path = get_Path.toString();
                //URI画像を入れる
                Glide.with(getContext())
                        .load(get_Path)
                        .into(yes_favourite_ImageView);
            }
        }
*/
    }

//    /**
//     * ふぁぼボタン変更
//     */
//    private fun favButtonSetting() {
//        //画像選択画面に飛ばす
//       no_favourite_Button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //ストレージ読み込みの権限があるか確認
//                //許可してないときは許可を求める
//                if (ContextCompat.checkSelfPermission(AddCustomMenuActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    new AlertDialog.Builder(AddCustomMenuActivity.this)
//                            .setTitle(getString(R.string.permission_dialog_titile))
//                            .setMessage(getString(R.string.permission_dialog_message))
//                            .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    //権限をリクエストする
//                                    ActivityCompat.requestPermissions(AddCustomMenuActivity.this,
//                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                                            1000);
//                                }
//                            })
//                            .setNegativeButton(getString(R.string.cancel), null)
//                            .show();
//                } else {
//                    //画像選択
//                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                    photoPickerIntent.setType("image/*");
//                    startActivityForResult(photoPickerIntent, noFavCode);
//                    //onActivityResultで処理
//                }
//            }
//        });
//        //画像選択画面に飛ばす
//        yes_favourite_Button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //ストレージ読み込みの権限があるか確認
//                //許可してないときは許可を求める
//                if (ContextCompat.checkSelfPermission(AddCustomMenuActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    new AlertDialog.Builder(AddCustomMenuActivity.this)
//                            .setTitle(getString(R.string.permission_dialog_titile))
//                            .setMessage(getString(R.string.permission_dialog_message))
//                            .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    //権限をリクエストする
//                                    ActivityCompat.requestPermissions(AddCustomMenuActivity.this,
//                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                                            1000);
//                                }
//                            })
//                            .setNegativeButton(getString(R.string.cancel), null)
//                            .show();
//                } else {
//                    //画像選択
//                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                    photoPickerIntent.setType("image/*");
//                    startActivityForResult(photoPickerIntent, yesFavCode);
//                    //onActivityResultで処理
//                }
//            }
//        });
//        //リセットボタン
//        no_favourite_Delete_Button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //リンクをリセット
//                no_fav_icon_path = "";
//                //URI画像を入れる
//                Glide.with(getContext()).load("").into(no_favourite_ImageView);
//                no_favourite_Button.setText(R.string.custom_setting_background_image);
//            }
//        });
//        //リセットボタン
//        yes_favourite_Delete_Button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //リンクをリセット
//                yes_fav_icon_path = "";
//                //URI画像を入れる
//                Glide.with(getContext()).load("").into(yes_favourite_ImageView);
//                yes_favourite_Button.setText(R.string.custom_setting_background_image);
//            }
//        });
//**/
//    }

    /**
     * 背景画像のボタンクリックイベントとか
     */
    private fun background_setting() {
        //画像選択画面に飛ばす
        background_image_set_Button!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //ストレージ読み込みの権限があるか確認
                //許可してないときは許可を求める
                if (ContextCompat.checkSelfPermission(this@AddCustomMenuActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder(this@AddCustomMenuActivity)
                            .setTitle(getString(R.string.permission_dialog_titile))
                            .setMessage(getString(R.string.permission_dialog_message))
                            .setPositiveButton(getString(R.string.permission_ok), object : DialogInterface.OnClickListener {
                                public override fun onClick(dialog: DialogInterface, which: Int) {
                                    //権限をリクエストする
                                    ActivityCompat.requestPermissions(this@AddCustomMenuActivity,
                                            arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                                            1000)
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show()
                } else {
                    //画像選択
                    val photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.setType("image/*")
                    startActivityForResult(photoPickerIntent, 1)
                    //onActivityResultで処理
                }
            }
        })
        //リセットボタン
        background_image_reset_Button!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                //リンクをリセット
                image_url = ""
                //URI画像を入れる
                Glide.with(this@AddCustomMenuActivity).load("").into(background_image_ImageView!!)
                background_image_set_Button!!.setText(R.string.custom_setting_background_image)
            }
        })
    }

    /**
     * フォント設定
     */
    private fun font_setting() {
        //Android Q
        var popupMenu: PopupMenu? = null
        var file_404 = false
        var files: Array<File>? = null
        var path = ""
        //Scoped Storageのせいで基本このアプリのサンドボックスしかあくせすできないので
        //ちなみにScoped Storageだと権限はいらない
        //ぱす
        var kaisendon_path = ""
        //Scoped Storage に対応させる
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            kaisendon_path = getExternalFilesDir(null)?.path + "/Kaisendon"
        } else {
            kaisendon_path = Environment.getExternalStorageDirectory().path + "/Kaisendon"
        }
        val kaisendon_file = File(kaisendon_path)
        kaisendon_file.mkdir()
        path = kaisendon_path + "/kaisendon_fonts"
        val font_folder = File(path)
        //存在チェック
        if (font_folder.exists()) {
            //存在するときはフォルダの中身を表示させる
            files = font_folder.listFiles()
            if (files != null) {
                //PopupMenu
                popupMenu = PopupMenu(this@AddCustomMenuActivity, font_Button)
                val menu = popupMenu.getMenu()
                //ディレクトリの中0個
                if (files.size == 0) {
                    file_404 = true
                } else {
                    for (i in files.indices) {
                        //追加
                        //ItemIDに配列の番号を入れる
                        menu.add(0, i, 0, files[i].getName())
                    }
                }
            }
        } else {
            //存在しないときはディレクトリ作成
            font_folder.mkdir()
            file_404 = true
        }
        //クリックイベント
        val finalFiles = files
        if (popupMenu != null) {
            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                public override fun onMenuItemClick(item: MenuItem): Boolean {
                    font_Button!!.setText(finalFiles!![item.getItemId()].getPath())
                    font_path = finalFiles!![item.getItemId()].getPath()
                    typeface = Typeface.createFromFile(File(font_path))
                    font_TextView?.setTypeface(typeface)
                    return false
                }
            })
        }

        val finalPopupMenu = popupMenu
        val finalFile_40 = file_404
        val finalPath = path
        font_Button!!.setOnClickListener {
            // Android 9 以前の端末でもフォントをディレクトリに入れてもらう
            //フォント用ディレクトリがない・ディレクトリの中身が無いときにToastを出す
            if (finalFile_40) {
                Toast.makeText(this@AddCustomMenuActivity, getString(R.string.font_directory_not_found) + "\n" + finalPath, Toast.LENGTH_LONG).show()
            } else {
                finalPopupMenu?.show()
            }
        }


        font_reset_Button!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                font_path = ""
                font_Button!!.setText("")
                font_TextView!!.setTypeface(TextView(this@AddCustomMenuActivity).getTypeface())
            }
        })
    }


    /**
     * SQLiteに保存する
     */
    private fun saveSQLite() {
        val values = ContentValues()
        //JSON化
        val jsonObject = JSONObject()
        try {
            jsonObject.put("misskey", (misskey_Switch!!.isChecked()).toString())
            jsonObject.put("name", name_EditText!!.getText().toString())
            jsonObject.put("memo", "")
            jsonObject.put("content", load_url)
            jsonObject.put("instance", instance)
            jsonObject.put("access_token", access_token)
            jsonObject.put("image_load", (image_Switch!!.isChecked()).toString())
            jsonObject.put("dialog", (dialog_Switch!!.isChecked()).toString())
            jsonObject.put("dark_mode", (dark_Switch!!.isChecked()).toString())
            jsonObject.put("position", "")
            jsonObject.put("streaming", (!streaming_Switch!!.isChecked()).toString()) //反転させてONのときStereaming有効に
            jsonObject.put("subtitle", subtitle_EditText!!.getText().toString())
            jsonObject.put("image_url", image_url)
            jsonObject.put("background_transparency", background_transparency!!.getText().toString())
            jsonObject.put("background_screen_fit", (background_screen_fit_Switch!!.isChecked()).toString())
            jsonObject.put("quick_profile", (quickprofile_Switch!!.isChecked()).toString())
            jsonObject.put("toot_counter", (tootcounter_Switch!!.isChecked()).toString())
            jsonObject.put("custom_emoji", (custom_emoji_Switch!!.isChecked()).toString())
            jsonObject.put("gif", (gif_Switch!!.isChecked()).toString())
            jsonObject.put("font", (font_path).toString())
            jsonObject.put("one_hand", (one_hand_Switch!!.isChecked()).toString())
            jsonObject.put("misskey_username", misskey_username)
            jsonObject.put("no_fav_icon", no_fav_icon_path)
            jsonObject.put("yes_fav_icon", yes_fav_icon_path)
            jsonObject.put("setting", "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        values.put("name", name_EditText!!.getText().toString())
        values.put("setting", jsonObject.toString())
        db!!.insert("custom_menudb", null, values)
    }

    /**
     * SQLite更新
     */
    private fun updateSQLite(name: String?) {
        val values = ContentValues()
        //JSON化
        val jsonObject = JSONObject()
        try {
            jsonObject.put("misskey", (misskey_Switch!!.isChecked()).toString())
            jsonObject.put("name", name_EditText!!.getText().toString())
            jsonObject.put("memo", "")
            jsonObject.put("content", load_url)
            jsonObject.put("instance", instance)
            jsonObject.put("access_token", access_token)
            jsonObject.put("image_load", (image_Switch!!.isChecked()).toString())
            jsonObject.put("dialog", (dialog_Switch!!.isChecked()).toString())
            jsonObject.put("dark_mode", (dark_Switch!!.isChecked()).toString())
            jsonObject.put("position", "")
            jsonObject.put("streaming", (!streaming_Switch!!.isChecked()).toString()) //反転させてONのときStereaming有効に
            jsonObject.put("subtitle", subtitle_EditText!!.getText().toString())
            jsonObject.put("image_url", image_url)
            jsonObject.put("background_transparency", background_transparency!!.getText().toString())
            jsonObject.put("background_screen_fit", (background_screen_fit_Switch!!.isChecked()).toString())
            jsonObject.put("quick_profile", (quickprofile_Switch!!.isChecked()).toString())
            jsonObject.put("toot_counter", (tootcounter_Switch!!.isChecked()).toString())
            jsonObject.put("custom_emoji", (custom_emoji_Switch!!.isChecked()).toString())
            jsonObject.put("gif", (gif_Switch!!.isChecked()).toString())
            jsonObject.put("font", (font_path).toString())
            jsonObject.put("one_hand", (one_hand_Switch!!.isChecked()).toString())
            jsonObject.put("misskey_username", misskey_username)
            jsonObject.put("no_fav_icon", no_fav_icon_path)
            jsonObject.put("yes_fav_icon", yes_fav_icon_path)
            jsonObject.put("setting", "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        values.put("name", name_EditText!!.getText().toString())
        values.put("setting", jsonObject.toString())
        db!!.update("custom_menudb", values, "name=?", arrayOf<String>(name!!))
    }


    //ListViewにあった場合は
    //読み込む

    /**
     * 読み込む
     */
    private fun loadSQLite(name: String?) {
        val cursor = db!!.query(
                "custom_menudb",
                arrayOf<String>("setting"),
                "name=?",
                arrayOf<String>(name!!), null, null, null
        )
        cursor.moveToFirst()
        for (i in 0 until cursor.getCount()) {
            try {
                val jsonObject = JSONObject(cursor.getString(0))
                misskey_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("misskey")))
                name_EditText!!.setText(jsonObject.getString("name"))
                urlToContent(jsonObject.getString("content"))
                instance = jsonObject.getString("instance")
                account_Button!!.setText(instance)
                access_token = jsonObject.getString("access_token")
                image_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("image_load")))
                dark_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("dark_mode")))
                streaming_Switch!!.setChecked(!java.lang.Boolean.valueOf(jsonObject.getString("streaming")))
                dialog_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("dialog")))
                subtitle_EditText!!.setText(jsonObject.getString("subtitle"))
                background_image_set_Button!!.setText(jsonObject.getString("image_url"))
                image_url = jsonObject.getString("image_url")
                background_image_set_Button!!.setText(image_url)
                Glide.with(this).load(jsonObject.getString("image_url")).into(background_image_ImageView!!)
                background_transparency!!.setText(jsonObject.getString("background_transparency"))
                background_screen_fit_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("background_screen_fit")))
                quickprofile_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("quick_profile")))
                tootcounter_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("toot_counter")))
                custom_emoji_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("custom_emoji")))
                gif_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("gif")))
                one_hand_Switch!!.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("one_hand")))
                font_Button!!.setText(jsonObject.getString("font"))
                font_path = jsonObject.getString("font")
                no_fav_icon_path = jsonObject.getString("no_fav_icon")
                yes_fav_icon_path = jsonObject.getString("yes_fav_icon")
                val file = File(font_path)
                misskey_username = jsonObject.getString("misskey_username")
                if (file.exists()) {
                    typeface = Typeface.createFromFile(File(font_path))
                    font_TextView!!.setTypeface(typeface)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            cursor.moveToNext()
        }
        cursor.close()
    }


    //ポップアップメニュー
    @SuppressLint("RestrictedApi")
    private fun setLoadMenu() {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(this@AddCustomMenuActivity)
        val inflater = MenuInflater(this@AddCustomMenuActivity)
        inflater.inflate(R.menu.custom_menu_load_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(this@AddCustomMenuActivity, menuBuilder, load_Button!!)
        optionsMenu.setForceShowIcon(true)

        load_Button!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                //表示
                optionsMenu.show()
                //押したときの反応
                menuBuilder.setCallback(object : MenuBuilder.Callback {

                    public override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                        when (menuItem.getItemId()) {
                            R.id.custom_menu_load_home -> {
                                load_url = "/api/v1/timelines/home"
                                load_Button!!.setText(R.string.home)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_notification -> {
                                load_url = "/api/v1/notifications"
                                load_Button!!.setText(R.string.notifications)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_local -> {
                                load_url = "/api/v1/timelines/public?local=true"
                                load_Button!!.setText(R.string.public_time_line)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_federated -> {
                                load_url = "/api/v1/timelines/public"
                                load_Button!!.setText(R.string.federated_timeline)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_direct -> {
                                load_url = "/api/v1/timelines/direct"
                                load_Button!!.setText(R.string.direct_message)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_scheduled_statuses -> {
                                load_url = "/api/v1/scheduled_statuses"
                                load_Button!!.setText(R.string.scheduled_statuses)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_alarm_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_favourite_list -> {
                                load_url = "/api/v1/favourites"
                                load_Button!!.setText(R.string.favourite_list)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_follow_suggestions -> {
                                load_url = "/api/v1/suggestions"
                                load_Button!!.setText(R.string.follow_suggestions)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_hastag_tl_local -> {
                                showHashtagMessageToast()
                                load_url = "/api/v1/timelines/tag/?local=true"
                                load_Button!!.setText(getString(R.string.hash_tag_tl_local))
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_label_outline_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_hastag_tl_public -> {
                                showHashtagMessageToast()
                                load_url = "/api/v1/timelines/tag/"
                                load_Button!!.setText(getString(R.string.hash_tag_tl_public))
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_label_outline_black_24dp, 0, 0, 0)
                            }
                        }
                        return false
                    }

                    public override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                    }
                })

            }
        })


        account_menuBuilder = MenuBuilder(this)
        account_optionsMenu = MenuPopupHelper(this, account_menuBuilder!!, account_Button!!)
        optionsMenu.setForceShowIcon(true)
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

            }

        }

        if (multi_account_instance.size >= 1) {
            for (count in multi_account_instance.indices) {
                val multi_instance = multi_account_instance.get(count)
                val multi_access_token = multi_account_access_token.get(count)
                val finalCount = count
                //GetAccount
                val url = "https://" + multi_instance + "/api/v1/accounts/verify_credentials/?access_token=" + multi_access_token
                //作成
                val request = Request.Builder()
                        .url(url)
                        .get()
                        .build()

                //GETリクエスト
                val client_1 = OkHttpClient()
                client_1.newCall(request).enqueue(object : Callback {

                    public override fun onFailure(call: Call, e: IOException) {

                    }

                    @Throws(IOException::class)
                    public override fun onResponse(call: Call, response: Response) {
                        val response_string = response.body()!!.string()
                        try {
                            val jsonObject = JSONObject(response_string)
                            val display_name = jsonObject.getString("display_name")
                            val user_id = jsonObject.getString("acct")
                            account_menuBuilder?.add(0, finalCount, 0, display_name + "(" + user_id + " / " + multi_instance + ")")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                })
            }
        }

        //押したときの処理
        account_Button!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                //追加中に押したら落ちるから回避
                //Knzk.meなどの終了した鯖があると絶対動かないので一個以上あれば動くように修正
                if (account_menuBuilder?.size() ?: 0 >= 1) {
                    account_optionsMenu?.show()
                    account_menuBuilder?.setCallback(object : MenuBuilder.Callback {
                        public override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {

                            //ItemIdにマルチアカウントのカウントを入れている
                            val position = menuItem.getItemId()

                            instance = multi_account_instance.get(position)
                            access_token = multi_account_access_token.get(position)
                            account_Button!!.setText(instance)
                            return false
                        }

                        public override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                        }
                    })

                } else {
                    Toast.makeText(this@AddCustomMenuActivity, R.string.loading, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /*ハッシュタグ用の警告。名前欄にハッシュタグを入れてねっていうメッセージ*/
    private fun showHashtagMessageToast() {
        Toast.makeText(this, getString(R.string.hashtag_tl_toast_message), Toast.LENGTH_SHORT).show()
    }

    /**
     * Misskey用メニュー
     */
    @SuppressLint("RestrictedApi")
    private fun setLoadMisskeyMenu() {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(this@AddCustomMenuActivity)
        val inflater = MenuInflater(this@AddCustomMenuActivity)
        inflater.inflate(R.menu.custom_menu_misskey_load_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(this@AddCustomMenuActivity, menuBuilder, load_Button!!)
        optionsMenu.setForceShowIcon(true)
        load_Button!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                //表示
                optionsMenu.show()
                //押したときの反応
                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    public override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                        when (menuItem.getItemId()) {
                            R.id.misskey_custom_menu_load_home -> {
                                load_url = "/api/notes/timeline"
                                load_Button!!.setText(R.string.home)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0)
                            }
                            R.id.misskey_custom_menu_load_notification -> {
                                load_url = "/api/i/notifications"
                                load_Button!!.setText(R.string.notifications)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0)
                            }
                            R.id.misskey_custom_menu_load_local -> {
                                load_url = "/api/notes/local-timeline"
                                load_Button!!.setText(R.string.public_time_line)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0)
                            }
                            R.id.misskey_custom_menu_load_federated -> {
                                load_url = "/api/notes/global-timeline"
                                load_Button!!.setText(R.string.federated_timeline)
                                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0)
                            }
                        }
                        return false
                    }

                    public override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                    }
                })

            }
        })


        account_menuBuilder = MenuBuilder(this)
        account_optionsMenu = MenuPopupHelper(this, account_menuBuilder!!, account_Button!!)
        optionsMenu.setForceShowIcon(true)
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()
        val multi_account_username = ArrayList<String>()
        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting!!.getString("misskey_instance_list", "")
        val account_instance_string = pref_setting!!.getString("misskey_access_list", "")
        val username_instance_string = pref_setting!!.getString("misskey_username_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                val username_array = JSONArray(username_instance_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance.add(instance_array.getString(i))
                    multi_account_username.add(username_array.getString(i))
                }
            } catch (e: Exception) {

            }

        }

        if (multi_account_instance.size >= 1) {
            for (count in multi_account_instance.indices) {
                val multi_instance = multi_account_instance.get(count)
                val multi_access_token = multi_account_access_token.get(count)
                val multi_username = multi_account_username.get(count)
                val finalCount = count
                //GetAccount
                val url = "https://" + multi_instance + "/api/users/show"
                //JSON
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("username", multi_username)
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
                    public override fun onFailure(call: Call, e: IOException) {

                    }

                    @Throws(IOException::class)
                    public override fun onResponse(call: Call, response: Response) {
                        val response_string = response.body()!!.string()
                        try {
                            val jsonObject = JSONObject(response_string)
                            val display_name = jsonObject.getString("name")
                            val user_id = jsonObject.getString("username")
                            account_menuBuilder!!.add(0, finalCount, 0, display_name + "(" + user_id + " / " + multi_instance + ")")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                })
            }
        }

        //押したときの処理
        account_Button!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                //追加中に押したら落ちるから回避
                if (account_menuBuilder!!.size() == multi_account_instance.size) {
                    account_optionsMenu!!.show()
                    account_menuBuilder!!.setCallback(object : MenuBuilder.Callback {
                        public override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {

                            //ItemIdにマルチアカウントのカウントを入れている
                            val position = menuItem.getItemId()
                            instance = multi_account_instance.get(position)
                            access_token = multi_account_access_token.get(position)
                            account_Button!!.setText(instance)
                            misskey_username = multi_account_username.get(position)
                            return false
                        }

                        public override fun onMenuModeChange(menuBuilder: MenuBuilder) {}
                    })
                } else {
                    Toast.makeText(this@AddCustomMenuActivity, R.string.loading, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * URL→こんてんと
     */
    private fun urlToContent(url: String) {
        when (url) {
            "/api/v1/timelines/home" -> {
                load_url = "/api/v1/timelines/home"
                load_Button!!.setText(R.string.home)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0)
            }
            "/api/v1/notifications" -> {
                load_url = "/api/v1/notifications"
                load_Button!!.setText(R.string.notifications)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/public?local=true" -> {
                load_url = "/api/v1/timelines/public?local=true"
                load_Button!!.setText(R.string.public_time_line)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/public" -> {
                load_url = "/api/v1/timelines/public"
                load_Button!!.setText(R.string.federated_timeline)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/direct" -> {
                load_url = "/api/v1/timelines/direct"
                load_Button!!.setText(R.string.direct_message)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0)
            }
            "/api/v1/scheduled_statuses" -> {
                load_url = "/api/v1/scheduled_statuses"
                load_Button!!.setText(R.string.scheduled_statuses)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_alarm_black_24dp, 0, 0, 0)
            }
            "/api/v1/favourites" -> {
                load_url = "/api/v1/favourites"
                load_Button!!.setText(R.string.favourite_list)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black_24dp, 0, 0, 0)
            }
            "/api/v1/suggestions" -> {
                load_url = "/api/v1/suggestions"
                load_Button!!.setText(R.string.follow_suggestions)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/tag/?local=true" -> {
                load_url = "/api/v1/timelines/tag/?local=true"
                load_Button!!.setText(R.string.hash_tag_tl_local)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_label_outline_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/tag/" -> {
                load_url = "api/v1/timelines/tag/"
                load_Button!!.setText(R.string.hash_tag_tl_public)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_label_outline_black_24dp, 0, 0, 0)
            }
            "/api/notes/timeline" -> {
                load_url = "/api/notes/timeline"
                load_Button!!.setText(R.string.home)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0)
            }
            "/api/i/notifications" -> {
                load_url = "/api/i/notifications"
                load_Button!!.setText(R.string.notifications)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0)
            }
            "/api/notes/local-timeline" -> {
                load_url = "/api/notes/local-timeline"
                load_Button!!.setText(R.string.public_time_line)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0)
            }
            "/api/notes/global-timeline" -> {
                load_url = "/api/notes/global-timeline"
                load_Button!!.setText(R.string.global)
                load_Button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0)
            }
        }
    }

    /*
    * 閉じたときに保存する
    * */
    override fun onDestroy() {
        super.onDestroy()
        //更新・新規作成
        if (!intent.getBooleanExtra("delete_button", false)) {
            //新規作成
            saveSQLite()
        } else {
            //更新
            val name = intent.getStringExtra("name")
            updateSQLite(name)
        }
    }

    fun getPath(uri: Uri?): String {
        val projection = arrayOf<String>(MediaStore.Images.Media.DATA)
        val cursor = getContentResolver().query(uri!!, projection, null, null, null)
        val column_index = cursor!!
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor!!.moveToFirst()
        var imagePath = cursor!!.getString(column_index)
        cursor!!.close()
        //Android Q から追加された Scoped Storage に一時的に対応
        //なにそれ→アプリごとにストレージサンドボックスが作られて、今まであったWRITE_EXTERNAL_STORAGEなしで扱える他
        //他のアプリからはアクセスできないようになってる。
        //<I>いやでも今までのfile://スキーマ変換が使えないのはクソクソクソでは</I>
        //今までのやつをAndroid Qで動かすと
        //Q /mnt/content/media ～
        //Pie /storage/emulated/0 ～
        //もう一回かけてようやくfile://スキーマのリンク取れた
        //Android Q
        if (Build.VERSION.CODENAME == "Q") {
            // /mnt/content/が邪魔なので取って、そこにcontent://スキーマをつける
            val content_text = imagePath.replace("/mnt/content/", "content://")
            //もう一回目ゾッと呼ぶので制御用にtrue
            imagePath = Home.getPathAndroidQ(this, Uri.parse(content_text))
        }
        println(imagePath)
        return imagePath
    }

    private fun getPath_Q(uri: Uri): String {
        val path = ""
        return path
    }

}
