package io.github.takusan23.Kaisendon.CustomMenu

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import io.github.takusan23.Kaisendon.APIJSONParse.CustomMenuJSONParse
import io.github.takusan23.Kaisendon.Theme.DarkModeSupport
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.*
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_background_image_button
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_background_image_imageview
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_background_image_reset_button
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_account
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_account_linearlayout
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_background_screen_fit_switch
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_background_transoarency_edittext_edittext
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_custom_emoji
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_font
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_font_reset
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_gif
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_image
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_load
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_one_hand
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_quickprofile
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_read_only_instance_switch
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_read_only_instance_textinput
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_streaming
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_subtitle_edittext_edittext
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.custom_menu_tootcounter
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.font_textView
import kotlinx.android.synthetic.main.bottom_fragment_add_custom_menu.misskey_switch
import kotlinx.android.synthetic.main.fragment_custom_menu_setting.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.ArrayList

class AddCustomMenuBottomFragment : BottomSheetDialogFragment() {

    lateinit var pref_setting: SharedPreferences
    lateinit var account_menuBuilder: MenuBuilder
    lateinit var account_optionsMenu: MenuPopupHelper
    lateinit var helper: CustomMenuSQLiteHelper
    lateinit var db: SQLiteDatabase

    private var load_url: String? = null
    private var instance: String? = null
    private var access_token: String? = null
    private var isReadOnly = false

    //画像のURL
    private var image_url = ""
    //ふぉんとのパス
    private var font_path = ""
    private var typeface: Typeface? = null

    //misskey
    private var misskey_username = ""


    /*はじっこを丸くする*/
    override fun getTheme(): Int {
        var theme = R.style.BottomSheetDialogThemeAppTheme
        val darkModeSupport = DarkModeSupport(context!!)
        if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES) {
            theme = R.style.BottomSheetDialogThemeDarkTheme
        }
        return theme
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_fragment_add_custom_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        //ダークモード対応
        val darkModeSupport = DarkModeSupport(context!!)
        darkModeSupport.setLayoutAllThemeColor(view as LinearLayout)
        if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES) {
            add_custom_menu_tablayout.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
            darkModeSupport.setAllChildViewSwitchColor(custom_menu_edit_parent_linearlayout)
        }

        //データベース
        //SQLite
        helper = CustomMenuSQLiteHelper(context!!)
        db = helper.writableDatabase
        db.disableWriteAheadLogging()

        //タブ切り替え
        add_custom_menu_tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setAllGoneVisibility()
                when (tab?.text) {
                    getString(R.string.custom_menu_editor_required) -> {
                        required_linearlayout.visibility = View.VISIBLE
                    }
                    getString(R.string.custom_menu_editor_theme) -> {
                        appearance_linearlayout.visibility = View.VISIBLE
                    }
                    getString(R.string.custom_menu_editor_details) -> {
                        advance_linearlayout.visibility = View.VISIBLE
                    }
                }
            }
        })


        //削除ボタン
        //ListViewから来たとき
        if (arguments?.getBoolean("delete_button", false) == true) {
            loadSQLite(arguments!!.getString("name"))
            custom_menu_title_textview.text = context?.getString(R.string.custom_menu_update_title)
            custom_menu_save_button.text = context?.getString(R.string.apply)
        }


        //メニュー
        setLoadMenu()


        //Misskey
        misskey_switch.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                //チェックした
                setLoadMisskeyMenu()
            } else {
                //チェックしてない
                setLoadMenu()
            }
        }

        custom_menu_save_button.setOnClickListener { v ->
            val delete = arguments?.getBoolean("delete_button") ?: false
            //更新・新規作成
            if (!delete) {
                //新規作成
                //Dialog
                val dialog = AlertDialog.Builder(context!!)
                        .setTitle(R.string.custom_menu_add)
                        .setMessage(R.string.custom_add_message)
                        .setPositiveButton(R.string.register) { dialogInterface, i ->
                            saveSQLite()
                            //戻る
                            startActivity(Intent(context, Home::class.java))
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                //https://stackoverflow.com/questions/9467026/changing-position-of-the-dialog-on-screen-android
                val window = dialog.window
                val layoutParams = window?.attributes
                layoutParams?.gravity = Gravity.BOTTOM
                layoutParams?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                window?.attributes = layoutParams
            } else {
                val dialog = AlertDialog.Builder(context!!)
                        .setTitle(R.string.custom_menu_update_title)
                        .setMessage(R.string.custom_menu_update)
                        .setPositiveButton(R.string.update) { dialogInterface, i ->
                            //更新
                            val name = arguments?.getString("name")
                            updateSQLite(name)
                            //戻る
                            startActivity(Intent(context, Home::class.java))
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                //https://stackoverflow.com/questions/9467026/changing-position-of-the-dialog-on-screen-android
                val window = dialog.window
                val layoutParams = window?.attributes
                layoutParams?.gravity = Gravity.BOTTOM
                layoutParams?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                window?.attributes = layoutParams
            }
        }


        //背景画像
        background_setting()
        //フォント
        font_setting()

    }

    /*VisibilityをすべてGONEにする*/
    fun setAllGoneVisibility() {
        required_linearlayout.visibility = View.GONE
        appearance_linearlayout.visibility = View.GONE
        advance_linearlayout.visibility = View.GONE
    }


    //ポップアップメニュー
    @SuppressLint("RestrictedApi")
    private fun setLoadMenu() {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.custom_menu_load_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context!!, menuBuilder, custom_menu_load)
        optionsMenu.setForceShowIcon(true)

        custom_menu_load.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //表示
                optionsMenu.show()
                //押したときの反応
                menuBuilder.setCallback(object : MenuBuilder.Callback {

                    override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {

                        //読み取り専用レイアウトけす
                        custom_menu_account_linearlayout.visibility = View.GONE

                        when (menuItem.itemId) {
                            R.id.custom_menu_load_home -> {
                                load_url = "/api/v1/timelines/home"
                                custom_menu_load.setText(R.string.home)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_notification -> {
                                load_url = "/api/v1/notifications"
                                custom_menu_load.setText(R.string.notifications)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_local -> {
                                load_url = "/api/v1/timelines/public?local=true"
                                custom_menu_load.setText(R.string.public_time_line)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0)
                                //ローカルTLのみ読み取り専用を許可する
                                custom_menu_account_linearlayout.visibility = View.VISIBLE
                            }
                            R.id.custom_menu_load_federated -> {
                                load_url = "/api/v1/timelines/public"
                                custom_menu_load.setText(R.string.federated_timeline)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_direct -> {
                                load_url = "/api/v1/timelines/direct"
                                custom_menu_load.setText(R.string.direct_message)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_scheduled_statuses -> {
                                load_url = "/api/v1/scheduled_statuses"
                                custom_menu_load.setText(R.string.scheduled_statuses)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_alarm_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_favourite_list -> {
                                load_url = "/api/v1/favourites"
                                custom_menu_load.setText(R.string.favourite_list)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_follow_suggestions -> {
                                load_url = "/api/v1/suggestions"
                                custom_menu_load.setText(R.string.follow_suggestions)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_hastag_tl_local -> {
                                showHashtagMessageToast()
                                load_url = "/api/v1/timelines/tag/?local=true"
                                custom_menu_load.text = getString(R.string.hash_tag_tl_local)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_label_outline_black_24dp, 0, 0, 0)
                            }
                            R.id.custom_menu_load_hastag_tl_public -> {
                                showHashtagMessageToast()
                                load_url = "/api/v1/timelines/tag/"
                                custom_menu_load.text = getString(R.string.hash_tag_tl_public)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_label_outline_black_24dp, 0, 0, 0)
                            }
                        }
                        return false
                    }

                    override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                    }
                })

            }
        })


        account_menuBuilder = MenuBuilder(context)
        account_optionsMenu = MenuPopupHelper(context!!, account_menuBuilder, custom_menu_account)
        optionsMenu.setForceShowIcon(true)
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()
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

                    override fun onFailure(call: Call, e: IOException) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val response_string = response.body()!!.string()
                        try {
                            val jsonObject = JSONObject(response_string)
                            val display_name = jsonObject.getString("display_name")
                            val user_id = jsonObject.getString("acct")
                            account_menuBuilder.add(0, finalCount, 0, display_name + "(" + user_id + " / " + multi_instance + ")")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                })
            }
        }

        //押したときの処理
        custom_menu_account.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //追加中に押したら落ちるから回避
                //Knzk.meなどの終了した鯖があると絶対動かないので一個以上あれば動くように修正
                if (account_menuBuilder.size() >= 1) {
                    account_optionsMenu.show()
                    account_menuBuilder.setCallback(object : MenuBuilder.Callback {
                        override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {

                            //ItemIdにマルチアカウントのカウントを入れている
                            val position = menuItem.itemId

                            instance = multi_account_instance.get(position)
                            access_token = multi_account_access_token.get(position)
                            custom_menu_account.text = instance
                            return false
                        }

                        override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                        }
                    })

                } else {
                    Toast.makeText(context, R.string.loading, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /*ハッシュタグ用の警告。名前欄にハッシュタグを入れてねっていうメッセージ*/
    private fun showHashtagMessageToast() {
        Toast.makeText(context, getString(R.string.hashtag_tl_toast_message), Toast.LENGTH_SHORT).show()
    }

    /**
     * Misskey用メニュー
     */
    @SuppressLint("RestrictedApi")
    private fun setLoadMisskeyMenu() {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.custom_menu_misskey_load_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context!!, menuBuilder, custom_menu_load)
        optionsMenu.setForceShowIcon(true)
        custom_menu_load.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //表示
                optionsMenu.show()
                //押したときの反応
                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                        when (menuItem.itemId) {
                            R.id.misskey_custom_menu_load_home -> {
                                load_url = "/api/notes/timeline"
                                custom_menu_load.setText(R.string.home)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0)
                            }
                            R.id.misskey_custom_menu_load_notification -> {
                                load_url = "/api/i/notifications"
                                custom_menu_load.setText(R.string.notifications)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0)
                            }
                            R.id.misskey_custom_menu_load_local -> {
                                load_url = "/api/notes/local-timeline"
                                custom_menu_load.setText(R.string.public_time_line)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0)
                            }
                            R.id.misskey_custom_menu_load_federated -> {
                                load_url = "/api/notes/global-timeline"
                                custom_menu_load.setText(R.string.federated_timeline)
                                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0)
                            }
                        }
                        return false
                    }

                    override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                    }
                })

            }
        })


        account_menuBuilder = MenuBuilder(context)
        account_optionsMenu = MenuPopupHelper(context!!, account_menuBuilder, custom_menu_load)
        optionsMenu.setForceShowIcon(true)
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()
        val multi_account_username = ArrayList<String>()
        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting.getString("misskey_instance_list", "")
        val account_instance_string = pref_setting.getString("misskey_access_list", "")
        val username_instance_string = pref_setting.getString("misskey_username_list", "")
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
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val response_string = response.body()!!.string()
                        try {
                            val jsonObject = JSONObject(response_string)
                            val display_name = jsonObject.getString("name")
                            val user_id = jsonObject.getString("username")
                            account_menuBuilder.add(0, finalCount, 0, display_name + "(" + user_id + " / " + multi_instance + ")")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                })
            }
        }

        //押したときの処理
        custom_menu_account.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //追加中に押したら落ちるから回避
                if (account_menuBuilder.size() == multi_account_instance.size) {
                    account_optionsMenu.show()
                    account_menuBuilder.setCallback(object : MenuBuilder.Callback {
                        override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {

                            //ItemIdにマルチアカウントのカウントを入れている
                            val position = menuItem.itemId
                            instance = multi_account_instance.get(position)
                            access_token = multi_account_access_token.get(position)
                            custom_menu_account.text = instance
                            misskey_username = multi_account_username.get(position)
                            return false
                        }

                        override fun onMenuModeChange(menuBuilder: MenuBuilder) {}
                    })
                } else {
                    Toast.makeText(context, R.string.loading, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /*インスタンス名取得？、読み取り専用の場合と登録済みから取るやつ。*/
    private fun getInstanceName(): String {
        if (isReadOnly || custom_menu_read_only_instance_switch.isChecked) {
            instance = custom_menu_read_only_instance_textinput.text.toString()
        }
        return instance.toString()
    }

    /**
     * URL→こんてんと
     */
    private fun urlToContent(url: String) {
        when (url) {
            "/api/v1/timelines/home" -> {
                load_url = "/api/v1/timelines/home"
                custom_menu_load.setText(R.string.home)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0)
            }
            "/api/v1/notifications" -> {
                load_url = "/api/v1/notifications"
                custom_menu_load.setText(R.string.notifications)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/public?local=true" -> {
                load_url = "/api/v1/timelines/public?local=true"
                custom_menu_load.setText(R.string.public_time_line)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/public" -> {
                load_url = "/api/v1/timelines/public"
                custom_menu_load.setText(R.string.federated_timeline)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/direct" -> {
                load_url = "/api/v1/timelines/direct"
                custom_menu_load.setText(R.string.direct_message)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0)
            }
            "/api/v1/scheduled_statuses" -> {
                load_url = "/api/v1/scheduled_statuses"
                custom_menu_load.setText(R.string.scheduled_statuses)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_alarm_black_24dp, 0, 0, 0)
            }
            "/api/v1/favourites" -> {
                load_url = "/api/v1/favourites"
                custom_menu_load.setText(R.string.favourite_list)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black_24dp, 0, 0, 0)
            }
            "/api/v1/suggestions" -> {
                load_url = "/api/v1/suggestions"
                custom_menu_load.setText(R.string.follow_suggestions)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/tag/?local=true" -> {
                load_url = "/api/v1/timelines/tag/?local=true"
                custom_menu_load.setText(R.string.hash_tag_tl_local)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_label_outline_black_24dp, 0, 0, 0)
            }
            "/api/v1/timelines/tag/" -> {
                load_url = "api/v1/timelines/tag/"
                custom_menu_load.setText(R.string.hash_tag_tl_public)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_label_outline_black_24dp, 0, 0, 0)
            }
            "/api/notes/timeline" -> {
                load_url = "/api/notes/timeline"
                custom_menu_load.setText(R.string.home)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0)
            }
            "/api/i/notifications" -> {
                load_url = "/api/i/notifications"
                custom_menu_load.setText(R.string.notifications)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0)
            }
            "/api/notes/local-timeline" -> {
                load_url = "/api/notes/local-timeline"
                custom_menu_load.setText(R.string.public_time_line)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0)
            }
            "/api/notes/global-timeline" -> {
                load_url = "/api/notes/global-timeline"
                custom_menu_load.setText(R.string.global)
                custom_menu_load.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data!!.data != null) {
                val selectedImage = data.data
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    //完全パス取得
                    val get_Path = getPath(selectedImage)
                    val image_Path = "file:\\\\$get_Path"
                    //置き換え
                    val final_Path = image_Path.replace("\\\\".toRegex(), "/")
                    image_url = final_Path
                    //いれておく？
                    custom_background_image_button.text = image_url
                    //URI画像を入れる
                    Glide.with(this)
                            .load(get_Path)
                            .into(custom_background_image_imageview)
                } else {
                    //Scoped StorageのせいでURL取得できなくなったのでURIで管理する
                    image_url = selectedImage!!.toString()
                    //いれておく？
                    custom_background_image_button.text = image_url
                    //URI画像を入れる
                    Glide.with(this)
                            .load(image_url)
                            .into(custom_background_image_imageview)
                }
            }
        }
        if (requestCode == 4545 && resultCode == Activity.RESULT_OK) {
            val uri = data!!.data
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
            font_textView.typeface = typeface
            custom_menu_font.text = font_path
        }
    }

    /**
     * 背景画像のボタンクリックイベントとか
     */
    private fun background_setting() {
        //画像選択画面に飛ばす
        custom_background_image_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (context != null) {
                    //ストレージ読み込みの権限があるか確認
                    //許可してないときは許可を求める
                    if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        AlertDialog.Builder(context!!)
                                .setTitle(getString(R.string.permission_dialog_titile))
                                .setMessage(getString(R.string.permission_dialog_message))
                                .setPositiveButton(getString(R.string.permission_ok)) { dialog, which ->
                                    if (activity != null) {
                                        //権限をリクエストする
                                        ActivityCompat.requestPermissions(activity!!,
                                                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                                                1000)
                                    }
                                }
                                .setNegativeButton(getString(R.string.cancel), null)
                                .show()
                    } else {
                        //画像選択
                        val photoPickerIntent = Intent(Intent.ACTION_PICK)
                        photoPickerIntent.type = "image/*"
                        startActivityForResult(photoPickerIntent, 1)
                        //onActivityResultで処理
                    }
                }
            }
        })
        //リセットボタン
        custom_background_image_reset_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //リンクをリセット
                image_url = ""
                //URI画像を入れる
                Glide.with(custom_background_image_imageview).load("").into(custom_background_image_imageview)
                custom_background_image_reset_button.setText(R.string.custom_setting_background_image)
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
            kaisendon_path = context?.getExternalFilesDir(null)?.path + "/Kaisendon"
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
                popupMenu = PopupMenu(context, custom_menu_font)
                val menu = popupMenu.menu
                //ディレクトリの中0個
                if (files.size == 0) {
                    file_404 = true
                } else {
                    for (i in files.indices) {
                        //追加
                        //ItemIDに配列の番号を入れる
                        menu.add(0, i, 0, files[i].name)
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
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    custom_menu_font.text = finalFiles!![item.itemId].path
                    font_path = finalFiles[item.itemId].path
                    typeface = Typeface.createFromFile(File(font_path))
                    font_textView.typeface = typeface
                    return false
                }
            })
        }

        val finalPopupMenu = popupMenu
        val finalFile_40 = file_404
        val finalPath = path
        custom_menu_font.setOnClickListener {
            // Android 9 以前の端末でもフォントをディレクトリに入れてもらう
            //フォント用ディレクトリがない・ディレクトリの中身が無いときにToastを出す
            if (finalFile_40) {
                Toast.makeText(context, getString(R.string.font_directory_not_found) + "\n" + finalPath, Toast.LENGTH_LONG).show()
            } else {
                finalPopupMenu?.show()
            }
        }


        custom_menu_font_reset.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                font_path = ""
                custom_menu_font.text = ""
                font_textView.typeface = TextView(context).typeface
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

            //アクセストークンが無くてもローカルTL（読み取り専用）なら許可
            var isReadOnly = false
            if (access_token?.isEmpty() ?: false && load_url?.contains("/api/v1/timelines/public?local=true") ?: false) {
                isReadOnly = true
            }

            //必須項目が埋まってるか確認
            if (custom_menu_name_edittext_edittext.text.toString().isNotEmpty() && load_url?.isNotEmpty() == true || isReadOnly) {
                jsonObject.put("misskey", (misskey_switch.isChecked).toString())
                jsonObject.put("name", custom_menu_name_edittext_edittext.text.toString())
                jsonObject.put("memo", "")
                jsonObject.put("content", load_url)
                jsonObject.put("instance", getInstanceName())
                jsonObject.put("access_token", access_token ?: "")
                jsonObject.put("image_load", (custom_menu_image.isChecked).toString())
                jsonObject.put("dialog", (custom_menu_dialog.isChecked).toString())
                jsonObject.put("position", "")
                jsonObject.put("streaming", (!custom_menu_streaming.isChecked).toString()) //反転させてONのときStereaming有効に
                jsonObject.put("subtitle", custom_menu_subtitle_edittext_edittext.text.toString())
                jsonObject.put("image_url", image_url)
                jsonObject.put("background_transparency", custom_menu_background_transoarency_edittext_edittext.text.toString())
                jsonObject.put("background_screen_fit", (custom_menu_background_screen_fit_switch.isChecked).toString())
                jsonObject.put("quick_profile", (custom_menu_quickprofile.isChecked).toString())
                jsonObject.put("toot_counter", (custom_menu_tootcounter.isChecked).toString())
                jsonObject.put("custom_emoji", (custom_menu_custom_emoji.isChecked).toString())
                jsonObject.put("gif", (custom_menu_gif.isChecked).toString())
                jsonObject.put("font", (font_path).toString())
                jsonObject.put("one_hand", (custom_menu_one_hand.isChecked).toString())
                jsonObject.put("misskey_username", misskey_username)
                jsonObject.put("read_only", custom_menu_read_only_instance_switch.isChecked.toString())
                jsonObject.put("setting", "")
                //テーマ用JSONオブジェクト
                val themeJsonObject = JSONObject()
                themeJsonObject.put("theme_darkmode", theme_darkmode_switch.isChecked.toString())
                themeJsonObject.put("theme_status_bar_color", theme_status_bar_color_edittext.text.toString())
                themeJsonObject.put("theme_nav_bar_color", theme_nav_bar_color_edittext.text.toString())
                themeJsonObject.put("theme_tool_bar_color", theme_tool_bar_color_edittext.text.toString())
                themeJsonObject.put("theme_background_color", theme_background_color_edittext.text.toString())
                themeJsonObject.put("theme_toot_background_color", theme_toot_background_color_edittext.text.toString())
                themeJsonObject.put("theme_text_icon_color", theme_text_icon_color_edittext.text.toString())
                themeJsonObject.put("theme_post_button_background_color", theme_post_button_background_color_edittext.text.toString())
                themeJsonObject.put("theme_post_button_icon_color", theme_post_button_icon_color_edittext.text.toString())
                jsonObject.put("theme", themeJsonObject)

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        values.put("name", custom_menu_name_edittext_edittext.text.toString())
        values.put("setting", jsonObject.toString())
        db.insert("custom_menudb", null, values)
    }

    /**
     * SQLite更新
     */
    private fun updateSQLite(name: String?) {
        val values = ContentValues()
        //JSON化
        val jsonObject = JSONObject()
        try {
            jsonObject.put("misskey", (misskey_switch.isChecked).toString())
            jsonObject.put("name", custom_menu_name_edittext_edittext.text.toString())
            jsonObject.put("memo", "")
            jsonObject.put("content", load_url)
            jsonObject.put("instance", getInstanceName())
            jsonObject.put("access_token", access_token ?: "")
            jsonObject.put("image_load", (custom_menu_image.isChecked).toString())
            jsonObject.put("dialog", (custom_menu_dialog.isChecked).toString())
            //jsonObject.put("dark_mode", (custom_menu_darkmode.isChecked()).toString())
            jsonObject.put("position", "")
            jsonObject.put("streaming", (!custom_menu_streaming.isChecked).toString()) //反転させてONのときStereaming有効に
            jsonObject.put("subtitle", custom_menu_subtitle_edittext_edittext.text.toString())
            jsonObject.put("image_url", image_url)
            jsonObject.put("background_transparency", custom_menu_background_transoarency_edittext_edittext.text.toString())
            jsonObject.put("background_screen_fit", (custom_menu_background_screen_fit_switch.isChecked).toString())
            jsonObject.put("quick_profile", (custom_menu_quickprofile.isChecked).toString())
            jsonObject.put("toot_counter", (custom_menu_tootcounter.isChecked).toString())
            jsonObject.put("custom_emoji", (custom_menu_custom_emoji.isChecked).toString())
            jsonObject.put("gif", (custom_menu_gif.isChecked).toString())
            jsonObject.put("font", (font_path).toString())
            jsonObject.put("one_hand", (custom_menu_one_hand.isChecked).toString())
            jsonObject.put("misskey_username", misskey_username)
            //jsonObject.put("no_fav_icon", no_fav_icon_path)
            //jsonObject.put("yes_fav_icon", yes_fav_icon_path)
            jsonObject.put("read_only", custom_menu_read_only_instance_switch.isChecked.toString())
            jsonObject.put("setting", "")
            //テーマ用JSONオブジェクト
            val themeJsonObject = JSONObject()
            themeJsonObject.put("theme_darkmode", theme_darkmode_switch.isChecked.toString())
            themeJsonObject.put("theme_status_bar_color", theme_status_bar_color_edittext.text.toString())
            themeJsonObject.put("theme_nav_bar_color", theme_nav_bar_color_edittext.text.toString())
            themeJsonObject.put("theme_tool_bar_color", theme_tool_bar_color_edittext.text.toString())
            themeJsonObject.put("theme_background_color", theme_background_color_edittext.text.toString())
            themeJsonObject.put("theme_toot_background_color", theme_toot_background_color_edittext.text.toString())
            themeJsonObject.put("theme_text_icon_color", theme_text_icon_color_edittext.text.toString())
            themeJsonObject.put("theme_post_button_background_color", theme_post_button_background_color_edittext.text.toString())
            themeJsonObject.put("theme_post_button_icon_color", theme_post_button_icon_color_edittext.text.toString())
            jsonObject.put("theme", themeJsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        values.put("name", custom_menu_name_edittext_edittext.text.toString())
        values.put("setting", jsonObject.toString())
        db.update("custom_menudb", values, "name=?", arrayOf<String>(name!!))
    }


    //ListViewにあった場合は
    //読み込む

    /**
     * 読み込む
     */
    private fun loadSQLite(name: String?) {
        val cursor = db.query(
                "custom_menudb",
                arrayOf<String>("setting"),
                "name=?",
                arrayOf<String>(name!!), null, null, null
        )
        cursor.moveToFirst()
        for (i in 0 until cursor.count) {
            try {
                val jsonObject = JSONObject(cursor.getString(0))
                val customMenuJSONParse = CustomMenuJSONParse(jsonObject.toString())
                misskey_switch.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.misskey)
                custom_menu_name_edittext_edittext.setText(customMenuJSONParse.name)
                urlToContent(customMenuJSONParse.content)
                instance = customMenuJSONParse.instance
                custom_menu_account.text = instance
                access_token = customMenuJSONParse.access_token
                custom_menu_image.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.image_load)
                //custom_menu_darkmode.setChecked(java.lang.Boolean.valueOf(jsonObject.getString("dark_mode")))
                custom_menu_streaming.isChecked = !java.lang.Boolean.valueOf(customMenuJSONParse.streaming)
                custom_menu_dialog.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.dialog)
                custom_menu_subtitle_edittext_edittext.setText(customMenuJSONParse.subtitle)
                custom_background_image_button.text = customMenuJSONParse.image_url
                image_url = customMenuJSONParse.image_url
                custom_background_image_button.text = image_url
                Glide.with(this).load(customMenuJSONParse.image_url).into(custom_background_image_imageview)
                custom_menu_background_transoarency_edittext_edittext.setText(customMenuJSONParse.background_transparency)
                custom_menu_background_screen_fit_switch.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.background_screen_fit)
                custom_menu_quickprofile.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.quick_profile)
                custom_menu_tootcounter.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.toot_counter)
                custom_menu_custom_emoji.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.custom_emoji)
                custom_menu_gif.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.gif)
                custom_menu_one_hand.isChecked = java.lang.Boolean.valueOf(customMenuJSONParse.one_hand)
                custom_menu_font.text = customMenuJSONParse.font
                font_path = customMenuJSONParse.font
                isReadOnly = customMenuJSONParse.isReadOnly.toBoolean()
                custom_menu_read_only_instance_switch.isChecked = isReadOnly
                custom_menu_read_only_instance_textinput.setText(instance)
                //テーマ
                theme_darkmode_switch.isChecked = customMenuJSONParse.theme_darkmode.toBoolean()
                theme_status_bar_color_edittext.setText(customMenuJSONParse.theme_status_bar_color)
                theme_nav_bar_color_edittext.setText(customMenuJSONParse.theme_nav_bar_color)
                theme_tool_bar_color_edittext.setText(customMenuJSONParse.theme_tool_bar_color)
                theme_background_color_edittext.setText(customMenuJSONParse.theme_background_color)
                theme_toot_background_color_edittext.setText(customMenuJSONParse.theme_toot_background_color)
                theme_text_icon_color_edittext.setText(customMenuJSONParse.theme_text_icon_color)
                theme_post_button_background_color_edittext.setText(customMenuJSONParse.theme_post_button_background_color)
                theme_post_button_icon_color_edittext.setText(customMenuJSONParse.theme_post_button_icon_color)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            cursor.moveToNext()
        }
        cursor.close()
    }


    fun getPath(uri: Uri?): String {
        val projection = arrayOf<String>(MediaStore.Images.Media.DATA)
        val cursor = context?.contentResolver?.query(uri!!, projection, null, null, null)
        val column_index = cursor!!
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val imagePath = cursor.getString(column_index)
        cursor.close()
        return imagePath
    }

    private fun dpToPx(dp: Int): Int {
        // https://developer.android.com/guide/practices/screens_support.html#dips-pels
        val density = Resources.getSystem().displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }


    /* 幅を設定する*/
    override fun onResume() {
        super.onResume()
        // https://stackoverflow.com/questions/38436130/how-to-set-max-width-for-bottomsheetdialogfragment/38477466
        val windowManager = activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val width = if (displayMetrics.widthPixels < 1980) displayMetrics.widthPixels else 1980
        val height = -1 // MATCH_PARENT
        dialog?.window?.setLayout(width, height)
    }


}