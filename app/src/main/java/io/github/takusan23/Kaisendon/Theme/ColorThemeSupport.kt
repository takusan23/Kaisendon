package io.github.takusan23.Kaisendon.Theme

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import io.github.takusan23.Kaisendon.APIJSONParse.CustomMenuJSONParse
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.R

/* JSONデータからステータスバー、ナビゲーションバーの色などを変更できる*/
class ColorThemeSupport(val activity: AppCompatActivity, val customMenuJSONParse: CustomMenuJSONParse) {

    init {
        //ダークモードでも利用するか
        val darkModeSupport = DarkModeSupport(activity)
        if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES) {
            if (customMenuJSONParse.theme_darkmode.toBoolean()) {
                setActivityTheme()
            }
        } else {
            //のーまる
            setActivityTheme()
        }
    }

    fun setActivityTheme() {
        //テーマの設定があるか
        if (customMenuJSONParse.theme_data) {
            setStatusBarColor()
            setNavBarColor()
            setToolBarColor()
            setBackgroundColor()
            setTootBackgroundColor()
            setIconTextColor()
        }
    }

    /*すてーたすばーの色*/
    fun setStatusBarColor() {
        if (customMenuJSONParse.theme_status_bar_color.isNotEmpty()) {
            activity.window?.statusBarColor = convertColorInt(customMenuJSONParse.theme_status_bar_color)
        }
    }

    /*なびばーの色*/
    fun setNavBarColor() {
        if (customMenuJSONParse.theme_nav_bar_color.isNotEmpty()) {
            activity.window?.navigationBarColor = convertColorInt(customMenuJSONParse.theme_nav_bar_color)
        }
    }

    /*つーるばーの色*/
    fun setToolBarColor() {
        if (customMenuJSONParse.theme_tool_bar_color.isNotEmpty()) {
            val actionBar = activity.supportActionBar
            actionBar?.setBackgroundDrawable(ColorDrawable(convertColorInt(customMenuJSONParse.theme_tool_bar_color)))
        }
    }

    /*背景色*/
    fun setBackgroundColor() {
        if (customMenuJSONParse.theme_background_color.isNotEmpty()) {
            //背景画像が優先される
            if (customMenuJSONParse.image_url.isEmpty()) {
                //Fragment取得
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.container_container)
                if (fragment is CustomMenuTimeLine) {
                    //FragmentがCustomMenuTimeLineだったら動く
                    fragment.recyclerView?.background = ColorDrawable(convertColorInt(customMenuJSONParse.theme_background_color))
                }
            }
        }
    }

    /*トゥート背景色*/
    fun setTootBackgroundColor() {
        if (customMenuJSONParse.theme_toot_background_color.isNotEmpty()) {
            //Fragment取得
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.container_container)
            if (fragment is CustomMenuTimeLine) {
                //FragmentがCustomMenuTimeLineだったら動く
                fragment.customMenuRecyclerViewAdapter?.toot_backgroundColor = customMenuJSONParse.theme_toot_background_color
            }
        }
    }

    /*あいこん、てきすとの色*/
    fun setIconTextColor() {
        if (customMenuJSONParse.theme_toot_background_color.isNotEmpty()) {
            //Fragment取得
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.container_container)
            if (fragment is CustomMenuTimeLine) {
                //FragmentがCustomMenuTimeLineだったら動く
                fragment.customMenuRecyclerViewAdapter?.iconColor = customMenuJSONParse.theme_text_icon_color
            }
        }
    }

    fun convertColorStateTintList(colorCode: String): ColorStateList {
        return ColorStateList.valueOf(Color.parseColor(colorCode))
    }

    fun convertColorInt(colorCode: String): Int {
        return Color.parseColor(colorCode)
    }

}