package io.github.takusan23.Kaisendon.Theme

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomappbar.BottomAppBar
import io.github.takusan23.Kaisendon.APIJSONParse.CustomMenuJSONParse
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.bottom_bar_layout.*

/* JSONデータからステータスバー、ナビゲーションバーの色などを変更できる*/
class ColorThemeSupport(val activity: AppCompatActivity, val customMenuJSONParse: CustomMenuJSONParse) {
    var darkModeSupport: DarkModeSupport = DarkModeSupport(activity)

    init {
        //ダークモードでも利用するか
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
        setStatusBarColor()
        setNavBarColor()
        setToolBarColor()
        setFabColor()
        setBackgroundColor()
        setTootBackgroundColor()
        setIconTextColor()
    }

    /*すてーたすばーの色*/
    fun setStatusBarColor() {
        if (customMenuJSONParse.theme_status_bar_color.isNotEmpty()) {
            activity.window?.statusBarColor = convertColorInt(customMenuJSONParse.theme_status_bar_color)
        } else {
            //未設定
            activity.window?.statusBarColor = activity.getColor(android.R.color.transparent)
        }
    }

    /*なびばーの色*/
    fun setNavBarColor() {
        if (customMenuJSONParse.theme_nav_bar_color.isNotEmpty()) {
            activity.window?.navigationBarColor = convertColorInt(customMenuJSONParse.theme_nav_bar_color)
        } else {
            //未設定
            when (darkModeSupport.nightMode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    //ダークモード有効時
                    activity.window?.navigationBarColor = activity.getColor(R.color.black)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    //無効時
                    activity.window?.navigationBarColor = activity.getColor(R.color.black)
                }
            }
        }
    }

    /*つーるばーの色*/
    fun setToolBarColor() {
        if (customMenuJSONParse.theme_tool_bar_color.isNotEmpty()) {
            if (activity is Home) {
                //ホームActivity
                if (activity.bottomAppBar != null) {
                    val bottomAppBar = activity.bottomAppBar
                    bottomAppBar.backgroundTint = convertColorStateTintList(customMenuJSONParse.theme_tool_bar_color)
                } else {
                    val actionBar = activity.supportActionBar
                    actionBar?.setBackgroundDrawable(ColorDrawable(convertColorInt(customMenuJSONParse.theme_tool_bar_color)))
                }
            } else {
                val actionBar = activity.supportActionBar
                actionBar?.setBackgroundDrawable(ColorDrawable(convertColorInt(customMenuJSONParse.theme_tool_bar_color)))
            }
        } else {
            var color = activity.getColor(R.color.colorPrimary)
            //未設定
            when (darkModeSupport.nightMode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    //ダークモード有効時
                    color = activity.getColor(R.color.black)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    //無効時
                    color = activity.getColor(R.color.colorPrimary)
                }
            }
            if (activity is Home) {
                //ホームActivity
                if (activity.bottomAppBar != null) {
                    val bottomAppBar = activity.bottomAppBar
                    bottomAppBar.backgroundTint = ColorStateList.valueOf(color)
                    //BottomAppBarはナビバーの色も変える
                    activity.window.navigationBarColor = color
                } else {
                    val actionBar = activity.supportActionBar
                    actionBar?.setBackgroundDrawable(ColorDrawable(color))
                }
            } else {
                val actionBar = activity.supportActionBar
                actionBar?.setBackgroundDrawable(ColorDrawable(color))
            }
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
        //Fragment取得
        val fragment = activity.supportFragmentManager.findFragmentById(R.id.container_container)
        if (fragment is CustomMenuTimeLine) {
            if (customMenuJSONParse.theme_toot_background_color.isNotEmpty()) {
                //FragmentがCustomMenuTimeLineだったら動く
                fragment.customMenuRecyclerViewAdapter?.toot_backgroundColor = customMenuJSONParse.theme_toot_background_color
            } else {
                fragment.customMenuRecyclerViewAdapter?.toot_backgroundColor = ""
            }
        }
    }

    /*あいこん、てきすとの色*/
    fun setIconTextColor() {
        //Fragment取得
        val fragment = activity.supportFragmentManager.findFragmentById(R.id.container_container)
        if (fragment is CustomMenuTimeLine) {
            if (customMenuJSONParse.theme_toot_background_color.isNotEmpty()) {
                //FragmentがCustomMenuTimeLineだったら動く
                fragment.customMenuRecyclerViewAdapter?.iconColor = customMenuJSONParse.theme_text_icon_color
            } else {
                fragment.customMenuRecyclerViewAdapter?.iconColor = ""
            }
        }
    }

    /*Fabの背景色+アイコンの色*/
    fun setFabColor() {
        if (customMenuJSONParse.theme_post_button_background_color.isNotEmpty()) {
            if (activity is Home) {
                activity.fab.backgroundTintList = convertColorStateTintList(customMenuJSONParse.theme_post_button_background_color)
            }
        } else {
            if (activity is Home) {
                activity.fab.backgroundTintList = ColorStateList.valueOf(activity.getColor(R.color.colorPrimaryDark))
            }
        }
        if (customMenuJSONParse.theme_post_button_icon_color.isNotEmpty()) {
            if (activity is Home) {
                activity.fab.drawable.setTintList(convertColorStateTintList(customMenuJSONParse.theme_post_button_icon_color))
            }
        } else {
            if (activity is Home) {
                activity.fab.drawable.setTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")))
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