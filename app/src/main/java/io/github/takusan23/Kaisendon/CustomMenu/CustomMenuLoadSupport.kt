package io.github.takusan23.Kaisendon.CustomMenu

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import io.github.takusan23.Kaisendon.R
import org.json.JSONException
import org.json.JSONObject

class CustomMenuLoadSupport(private val context: Context, //private FragmentTransaction transaction;
                            private val navigationView: NavigationView) {
    private var helper: CustomMenuSQLiteHelper? = null
    private var db: SQLiteDatabase? = null
    private val pref_setting: SharedPreferences

    init {
        this.pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        //transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
    }


    /**
     * カスタムメニュー読み込み
     *
     * @param search 最後に開いた（ｒｙを使うときに使う。**使わないときはnullを入れてね**
     */
    fun loadCustomMenu(search: String?) {
        //SQLite
        if (helper == null) {
            helper = CustomMenuSQLiteHelper(context)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            db!!.disableWriteAheadLogging()
        }
        //SQLite読み込み
        val cursor: Cursor
        //検索するかどうか
        if (search != null) {
            cursor = db!!.query(
                    "custom_menudb",
                    arrayOf("name", "setting", "_id"),
                    "name=?",
                    arrayOf(search), null, null, null
            )
        } else {
            cursor = db!!.query(
                    "custom_menudb",
                    arrayOf("name", "setting"), null, null, null, null, null
            )
        }

        var misskey = ""
        var name = ""
        var content = ""
        var instance = ""
        var access_token = ""
        var image_load = ""
        var dialog = ""
        var dark_mode = ""
        var position = ""
        var streaming = ""
        var subtitle = ""
        var image_url = ""
        var background_transparency = ""
        var background_screen_fit = ""
        var quick_profile = ""
        var toot_counter = ""
        var custom_emoji = ""
        var gif = ""
        var font = ""
        var one_hand = ""
        var misskey_username = ""
        var setting = ""
        val no_fav_icon = ""
        val yes_fav_icon = ""
        var json = ""
        var read_only = ""
        cursor.moveToFirst()

        //最後に開く機能使うか？
        if (search != null) {
            try {
                val jsonObject = JSONObject(cursor.getString(1))
                json = jsonObject.toString()
                name = jsonObject.getString("name")
                content = jsonObject.getString("content")
                instance = jsonObject.getString("instance")
                access_token = jsonObject.getString("access_token")
                image_load = jsonObject.getString("image_load")
                dialog = jsonObject.getString("dialog")
                //dark_mode = jsonObject.getString("dark_mode")
                position = jsonObject.getString("position")
                streaming = jsonObject.getString("streaming")
                subtitle = jsonObject.getString("subtitle")
                image_url = jsonObject.getString("image_url")
                background_transparency = jsonObject.getString("background_transparency")
                background_screen_fit = jsonObject.getString("background_screen_fit")
                quick_profile = jsonObject.getString("quick_profile")
                toot_counter = jsonObject.getString("toot_counter")
                custom_emoji = jsonObject.getString("custom_emoji")
                gif = jsonObject.getString("gif")
                font = jsonObject.getString("font")
                one_hand = jsonObject.getString("one_hand")
                misskey = jsonObject.getString("misskey")
                misskey_username = jsonObject.getString("misskey_username")
                setting = jsonObject.getString("setting")
                read_only = getStringNullCheck(jsonObject, "read_only", "false")
                val bundle = Bundle()

                bundle.putString("setting_json",json)
                bundle.putString("content", content)

                val customMenuTimeLine = CustomMenuTimeLine()
                customMenuTimeLine.arguments = bundle
                //名前控える
                saveLastOpenCustomMenu(name)
                //チェックつける
                val item = navigationView.menu.getItem(cursor.getInt(2))
                item?.isChecked = true
                //置き換え
                val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container_container, customMenuTimeLine)
                transaction.commit()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        } else {
            for (i in 0 until cursor.count) {
                try {
                    val jsonObject = JSONObject(cursor.getString(1))
                    json = jsonObject.toString()
                    name = jsonObject.getString("name")
                    content = jsonObject.getString("content")
                    instance = jsonObject.getString("instance")
                    access_token = jsonObject.getString("access_token")
                    image_load = jsonObject.getString("image_load")
                    dialog = jsonObject.getString("dialog")
                    //dark_mode = jsonObject.getString("dark_mode")
                    position = jsonObject.getString("position")
                    streaming = jsonObject.getString("streaming")
                    subtitle = jsonObject.getString("subtitle")
                    image_url = jsonObject.getString("image_url")
                    background_transparency = jsonObject.getString("background_transparency")
                    background_screen_fit = jsonObject.getString("background_screen_fit")
                    quick_profile = jsonObject.getString("quick_profile")
                    toot_counter = jsonObject.getString("toot_counter")
                    custom_emoji = jsonObject.getString("custom_emoji")
                    gif = jsonObject.getString("gif")
                    font = jsonObject.getString("font")
                    one_hand = jsonObject.getString("one_hand")
                    misskey = jsonObject.getString("misskey")
                    misskey_username = jsonObject.getString("misskey_username")
                    setting = jsonObject.getString("setting")
                    read_only = getStringNullCheck(jsonObject, "read_only", "false")
                    //メニュー追加
                    val finalName = name
                    val finalContent = content
                    val finalInstance = instance
                    val finalAccess_token = access_token
                    val finalImage_load = image_load
                    val finalDialog = dialog
                    val finalDark_mode = dark_mode
                    val finalPosition = position
                    val finalStreaming = streaming
                    val finalSubtitle = subtitle
                    val finalImage_url = image_url
                    val finalBackground_transparency = background_transparency
                    val finalBackground_screen_fit = background_screen_fit
                    val finalQuick_profile = quick_profile
                    val finalToot_counter = toot_counter
                    val finalCustom_emoji = custom_emoji
                    val finalGif = gif
                    val finalFont = font
                    val finalOne_hand = one_hand
                    val finalSetting = setting
                    val finalMisskey = misskey
                    val finalMisskey_username = misskey_username
                    val finalReadOnly = read_only
                    val finalJson = json
                    val item = navigationView.menu.add(getDrawerMenuTitle(content, name)).setIcon(urlToContent(content))
                    item.setOnMenuItemClickListener {
                        //チェック外し
                        for (count in 0..navigationView.menu.size() - 1) {
                            val unCheckItem = navigationView.menu.getItem(count)
                            unCheckItem.isChecked = false
                        }
                        //チェックつける
                        item.isChecked = true
                        //Fragment切り替え
                        //受け渡す
                        val bundle = Bundle()
                        bundle.putString("setting_json",json)
                        val customMenuTimeLine = CustomMenuTimeLine()
                        customMenuTimeLine.arguments = bundle
                        //名前控える
                        saveLastOpenCustomMenu(finalName)
                        //置き換え
                        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.container_container, customMenuTimeLine)
                        transaction.commit()
                        false
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    //なくてもとりあえず追加する
                    //メニュー追加
                    val finalName = name
                    val finalContent = content
                    val finalInstance = instance
                    val finalAccess_token = access_token
                    val finalImage_load = image_load
                    val finalDialog = dialog
                    val finalDark_mode = dark_mode
                    val finalPosition = position
                    val finalStreaming = streaming
                    val finalSubtitle = subtitle
                    val finalImage_url = image_url
                    val finalBackground_transparency = background_transparency
                    val finalBackground_screen_fit = background_screen_fit
                    val finalQuick_profile = quick_profile
                    val finalToot_counter = toot_counter
                    val finalCustom_emoji = custom_emoji
                    val finalGif = gif
                    val finalFont = font
                    val finalOne_hand = one_hand
                    val finalSetting = setting
                    val finalMisskey = misskey
                    val finalMisskey_username = misskey_username
                    val finalReadOnly = read_only
                    val finalJson = json
                    val item = navigationView.menu.add(getDrawerMenuTitle(content, name)).setIcon(urlToContent(content))
                    item.setOnMenuItemClickListener {
                        //チェック外し
                        for (count in 0..navigationView.menu.size()) {
                            val unCheckItem = navigationView.menu.getItem(count)
                            unCheckItem.isChecked = false
                        }
                        //チェックつける
                        item.isChecked = true
                        //Fragment切り替え
                        //受け渡す
                        val bundle = Bundle()
                        bundle.putString("setting_json",json)
                        val customMenuTimeLine = CustomMenuTimeLine()
                        customMenuTimeLine.arguments = bundle
                        //名前控える
                        saveLastOpenCustomMenu(finalName)
                        //置き換え
                        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.container_container, customMenuTimeLine)
                        transaction.commit()
                        false
                    }
                }

                cursor.moveToNext()

            }
        }
        cursor.close()
    }

    /**
     * 最後に開いたカスタムメニュー保存
     */
    private fun saveLastOpenCustomMenu(name: String) {
        val editor = pref_setting.edit()
        editor.putString("custom_menu_last", name)
        editor.apply()
    }

    /*ViewPager用にFragment配列を返します*/
    fun loadMenuViewPager(): ArrayList<Fragment> {

        val argumentList = arrayListOf<Fragment>()

        //SQLite
        if (helper == null) {
            helper = CustomMenuSQLiteHelper(context)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            db!!.disableWriteAheadLogging()
        }
        //SQLite読み込み
        val cursor: Cursor
        cursor = db!!.query(
                "custom_menudb",
                arrayOf("name", "setting"), null, null, null, null, null
        )


        var misskey = ""
        var name = ""
        var content = ""
        var instance = ""
        var access_token = ""
        var image_load = ""
        var dialog = ""
        var dark_mode = ""
        var position = ""
        var streaming = ""
        var subtitle = ""
        var image_url = ""
        var background_transparency = ""
        var background_screen_fit = ""
        var quick_profile = ""
        var toot_counter = ""
        var custom_emoji = ""
        var gif = ""
        var font = ""
        var one_hand = ""
        var misskey_username = ""
        var setting = ""
        val no_fav_icon = ""
        val yes_fav_icon = ""
        var read_only = ""
        var json = ""

        cursor.moveToFirst()

        for (i in 0 until cursor.count) {
            try {
                val jsonObject = JSONObject(cursor.getString(1))
                json = jsonObject.toString()
                name = jsonObject.getString("name")
                content = jsonObject.getString("content")
                instance = jsonObject.getString("instance")
                access_token = jsonObject.getString("access_token")
                image_load = jsonObject.getString("image_load")
                dialog = jsonObject.getString("dialog")
                //dark_mode = jsonObject.getString("dark_mode")
                position = jsonObject.getString("position")
                streaming = jsonObject.getString("streaming")
                subtitle = jsonObject.getString("subtitle")
                image_url = jsonObject.getString("image_url")
                background_transparency = jsonObject.getString("background_transparency")
                background_screen_fit = jsonObject.getString("background_screen_fit")
                quick_profile = jsonObject.getString("quick_profile")
                toot_counter = jsonObject.getString("toot_counter")
                custom_emoji = jsonObject.getString("custom_emoji")
                gif = jsonObject.getString("gif")
                font = jsonObject.getString("font")
                one_hand = jsonObject.getString("one_hand")
                misskey = jsonObject.getString("misskey")
                misskey_username = jsonObject.getString("misskey_username")
                //                    no_fav_icon = jsonObject.getString("no_fav_icon");
                //                    yes_fav_icon = jsonObject.getString("yes_fav_icon");
                read_only = getStringNullCheck(jsonObject, "read_only", "false")
                setting = jsonObject.getString("setting")
                //メニュー追加
                val finalName = name
                val finalContent = content
                val finalInstance = instance
                val finalAccess_token = access_token
                val finalImage_load = image_load
                val finalDialog = dialog
                val finalDark_mode = dark_mode
                val finalPosition = position
                val finalStreaming = streaming
                val finalSubtitle = subtitle
                val finalImage_url = image_url
                val finalBackground_transparency = background_transparency
                val finalBackground_screen_fit = background_screen_fit
                val finalQuick_profile = quick_profile
                val finalToot_counter = toot_counter
                val finalCustom_emoji = custom_emoji
                val finalGif = gif
                val finalFont = font
                val finalOne_hand = one_hand
                val finalSetting = setting
                val finalMisskey = misskey
                val finalMisskey_username = misskey_username
                val finalReadOnly = read_only
                val finalJson = json
                //Fragment切り替え
                //受け渡す
                val bundle = Bundle()
                bundle.putString("setting_json",json)
                val customMenuTimeLine = CustomMenuTimeLine()
                customMenuTimeLine.arguments = bundle
                argumentList.add(customMenuTimeLine)
            } catch (e: JSONException) {
                e.printStackTrace()
                //なくてもとりあえず追加する
                //メニュー追加
                val finalName = name
                val finalContent = content
                val finalInstance = instance
                val finalAccess_token = access_token
                val finalImage_load = image_load
                val finalDialog = dialog
                val finalDark_mode = dark_mode
                val finalPosition = position
                val finalStreaming = streaming
                val finalSubtitle = subtitle
                val finalImage_url = image_url
                val finalBackground_transparency = background_transparency
                val finalBackground_screen_fit = background_screen_fit
                val finalQuick_profile = quick_profile
                val finalToot_counter = toot_counter
                val finalCustom_emoji = custom_emoji
                val finalGif = gif
                val finalFont = font
                val finalOne_hand = one_hand
                val finalSetting = setting
                val finalMisskey = misskey
                val finalMisskey_username = misskey_username
                val finalReadOnly = read_only
                val finalJson = json
                //Fragment切り替え
                //受け渡す
                val bundle = Bundle()
                bundle.putString("setting_json",json)
                val customMenuTimeLine = CustomMenuTimeLine()
                customMenuTimeLine.arguments = bundle
                argumentList.add(customMenuTimeLine)
            }
            cursor.moveToNext()
        }
        cursor.close()
        return argumentList
    }

    /*アイコンを返す*/
    private fun urlToContent(url: String): Drawable? {
        var drawable = context.getDrawable(R.drawable.ic_home_black_24dp)
        when (url) {
            "/api/v1/timelines/home" -> drawable = context.getDrawable(R.drawable.ic_home_black_24dp)
            "/api/v1/notifications" -> drawable = context.getDrawable(R.drawable.ic_notifications_black_24dp)
            "/api/v1/timelines/public?local=true" -> drawable = context.getDrawable(R.drawable.ic_train_black_24dp)
            "/api/v1/timelines/public" -> drawable = context.getDrawable(R.drawable.ic_flight_black_24dp)
            "/api/v1/timelines/direct" -> drawable = context.getDrawable(R.drawable.ic_assignment_ind_black_24dp)
            "/api/v1/favourites" -> drawable = context.getDrawable(R.drawable.ic_star_black_24dp)
            "/api/v1/scheduled_statuses" -> drawable = context.getDrawable(R.drawable.ic_access_alarm_black_24dp)
            "/api/v1/suggestions" -> drawable = context.getDrawable(R.drawable.ic_person_add_black_24dp)
            "/api/v1/timelines/tag/" -> drawable = context.getDrawable(R.drawable.ic_label_outline_black_24dp)
            "/api/v1/timelines/tag/?local=true" -> drawable = context.getDrawable(R.drawable.ic_label_outline_black_24dp)
            "/api/notes/timeline" -> drawable = context.getDrawable(R.drawable.ic_home_black_24dp)
            "/api/i/notifications" -> drawable = context.getDrawable(R.drawable.ic_notifications_black_24dp)
            "/api/notes/local-timeline" -> drawable = context.getDrawable(R.drawable.ic_train_black_24dp)
            "/api/notes/global-timeline" -> drawable = context.getDrawable(R.drawable.ic_flight_black_24dp)
        }
        return drawable
    }

    /*ハッシュタグ（＃）を入れる*/
    private fun getDrawerMenuTitle(context: String, title: String): String {
        var title = title
        if (context.contains("/api/v1/timelines/tag/") || context.contains("/api/v1/timelines/tag/?local=true")) {
            title = "#$title"
        }
        return title
    }

    /**Nullチェック付きgetString()
     * @param key 名前
     * @param default Nullのとき
     * */
    private fun getStringNullCheck(jsonObject: JSONObject, key: String, default: String): String {
        var value = default
        if (jsonObject.has(key)) {
            value = jsonObject.getString(key)
        } else {
            value = default
        }
        return value
    }

}
