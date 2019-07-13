package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSQLiteHelper
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.create_dafault_custommenu_bottom_fragment_layout.*
import org.json.JSONException
import org.json.JSONObject

class CreateDefaultCustomMenuBottomFragment : BottomSheetDialogFragment() {

    /*はじっこを丸くする*/
    override fun getTheme(): Int {
        var theme = R.style.BottomSheetDialogThemeAppTheme
        val darkModeSupport = DarkModeSupport(context!!)
        if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES){
            theme =  R.style.BottomSheetDialogThemeDarkTheme
        }
        return theme
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.create_dafault_custommenu_bottom_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Activity切り替え
        create_custommenu_create_button.setOnClickListener {
            //カスタムメニュー作成
            setCreateCustomMenu()
            val homecard = Intent(context, Home::class.java)
            startActivity(homecard)
        }
    }

    fun setCreateCustomMenu() {
        val instance = arguments?.getString("name")
        val token = arguments?.getString("token")

        val custom_menu_name = arrayListOf<String>()
        val custom_menu_url = arrayListOf<String>()

        if (create_default_custom_menu_home_switch.isChecked) {
            custom_menu_name.add("${getString(R.string.home)} : ")
            custom_menu_url.add("/api/v1/timelines/home")
        }
        if (create_default_custom_menu_notifications_switch.isChecked) {
            custom_menu_name.add("${getString(R.string.notifications)} : ")
            custom_menu_url.add("/api/v1/notifications")
        }
        if (create_default_custom_menu_local_switch.isChecked) {
            custom_menu_name.add("${getString(R.string.public_time_line)} : ")
            custom_menu_url.add("/api/v1/timelines/public?local=true")
        }
        if (create_default_custom_menu_federated_switch.isChecked) {
            custom_menu_name.add("${getString(R.string.federated_timeline)} : ")
            custom_menu_url.add("/api/v1/timelines/public")
        }


        if (context != null) {
            val customMenuSQLiteHelper = CustomMenuSQLiteHelper(context!!)
            val db = customMenuSQLiteHelper.writableDatabase
            db.disableWriteAheadLogging()
            val values = ContentValues()
            for (i in custom_menu_name) {
                //起動するFragment指定
                if (custom_menu_name.indexOf(i) == 0){
                    val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
                    val editor = pref_setting.edit()
                    editor.putString("custom_menu_last", "${i}${instance}")
                    editor.apply()
                }
                //JSON化
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("misskey", "false")
                    jsonObject.put("name", "${i}${instance}")
                    jsonObject.put("memo", "")
                    jsonObject.put("content", custom_menu_url[custom_menu_name.indexOf(i)])
                    jsonObject.put("instance", instance)
                    jsonObject.put("access_token", token)
                    jsonObject.put("image_load", "false")
                    jsonObject.put("dialog", "false")
                    jsonObject.put("dark_mode", "false")
                    jsonObject.put("position", "")
                    jsonObject.put("streaming", "true") //反転させてONのときStereaming有効に
                    jsonObject.put("subtitle", "")
                    jsonObject.put("image_url", "")
                    jsonObject.put("background_transparency", "")
                    jsonObject.put("background_screen_fit", "false")
                    jsonObject.put("quick_profile", "true")
                    jsonObject.put("toot_counter", "false")
                    jsonObject.put("custom_emoji", "true")
                    jsonObject.put("gif", "false")
                    jsonObject.put("font", "")
                    jsonObject.put("one_hand", "false")
                    jsonObject.put("misskey_username", "")
                    jsonObject.put("no_fav_icon", null)
                    jsonObject.put("yes_fav_icon", null)
                    jsonObject.put("setting", "")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                values.put("name", "${i}${instance}")
                values.put("setting", jsonObject.toString())
                db.insert("custom_menudb", null, values)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val homecard = Intent(context, Home::class.java)
        startActivity(homecard)
    }
}