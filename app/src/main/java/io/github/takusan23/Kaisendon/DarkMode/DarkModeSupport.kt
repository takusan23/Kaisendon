package io.github.takusan23.Kaisendon.DarkMode

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.*
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.github.takusan23.Kaisendon.R

class DarkModeSupport/*テキストビューの染色だけならここからどうぞ*/
(internal var context: Context) {
    internal var nightMode: Int = 0
    internal lateinit var activity: Activity
    internal var pref_setting: SharedPreferences

    init {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        setDarkmode()
    }

    /*背景ダークモード*/
    fun setBackgroundDarkMode(linearLayout: LinearLayout) {
        setDarkmode()
        if (linearLayout is LinearLayout) {
            when (nightMode) {
                Configuration.UI_MODE_NIGHT_NO -> linearLayout.setBackgroundColor(context.resources.getColor(android.R.color.white, context.theme))
                Configuration.UI_MODE_NIGHT_YES -> linearLayout.setBackgroundColor(context.resources.getColor(android.R.color.black, context.theme))
            }
        }
    }


    /*テーマを適用する*/
    fun setActivityTheme(activity: Activity) {
        this.activity = activity
        setDarkmode()
        //テーマ切り替え
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> activity.setTheme(R.style.AppTheme)
            Configuration.UI_MODE_NIGHT_YES -> activity.setTheme(R.style.OLED_ActionBer)
        }
    }

    //ダークモード設定
    private fun setDarkmode() {
        //ダークモード処理
        val conf = context.resources.configuration
        nightMode = conf.uiMode and Configuration.UI_MODE_NIGHT_MASK
        //Android Q以前の場合はメニューから切り替えを行う
        if (Build.VERSION.SDK_INT <= 28 && Build.VERSION.CODENAME != "Q") {
            if (pref_setting.getBoolean("darkmode", false)) {
                nightMode = Configuration.UI_MODE_NIGHT_YES
            } else {
                nightMode = Configuration.UI_MODE_NIGHT_NO
            }
        }
    }

    //染色する
    fun setTextViewThemeColor(textView: TextView) {
        setDarkmode()
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                textView.setTextColor(context.getColor(android.R.color.tab_indicator_text))
                textView.compoundDrawableTintList = context.resources.getColorStateList(android.R.color.black, context.theme)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                textView.setTextColor(context.getColor(android.R.color.white))
                textView.compoundDrawableTintList = context.resources.getColorStateList(android.R.color.white, context.theme)
            }
        }
    }

    //ダークモードかどうか判断（手動）
    fun setIsDarkModeSelf(darkmode: Int): Int {
        var darkmode = darkmode
        //Android Pie以前端末用
        if (Build.VERSION.SDK_INT <= 28 && Build.VERSION.CODENAME != "Q") {
            if (pref_setting.getBoolean("darkmode", false)) {
                darkmode = Configuration.UI_MODE_NIGHT_YES
            } else {
                darkmode = Configuration.UI_MODE_NIGHT_NO
            }
        }
        return darkmode
    }

    //すいっち
    fun setSwitchThemeColor(sw: Switch) {
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> sw.compoundDrawableTintList = context.resources.getColorStateList(android.R.color.black, context.theme)
            Configuration.UI_MODE_NIGHT_YES -> sw.compoundDrawableTintList = context.resources.getColorStateList(android.R.color.white, context.theme)
        }
    }

    //すいっち
    fun setViewThemeColor(v: View) {
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> v.setBackgroundColor(context.resources.getColor(android.R.color.white, context.theme))
            Configuration.UI_MODE_NIGHT_YES -> v.setBackgroundColor(context.resources.getColor(android.R.color.black, context.theme))
        }
    }


    //Linearlayoutから子View全取得してTextViewなら染色を行う
    fun setLayoutAllThemeColor(layout: LinearLayout) {
        setBackgroundDarkMode(layout)
        for (i in 0 until layout.childCount) {
            //System.out.println(layout.getChildCount());
            //setViewThemeColor(layout.getChildAt(i));
            if (layout.getChildAt(i) is LinearLayout) {
                setLayoutAllThemeColor(layout.getChildAt(i) as LinearLayout)
            }
            if (layout.getChildAt(i) is EditText) {
                setEditTextThemeColor(layout.getChildAt(i) as EditText)
            }
            if (layout.getChildAt(i) is TextView) {
                setTextViewThemeColor(layout.getChildAt(i) as TextView)
            }
            if (layout.getChildAt(i) is Switch) {
                setSwitchThemeColor(layout.getChildAt(i) as Switch)
            }
            if (layout.getChildAt(i) is BottomNavigationView) {
                setBottomNavigationBerThemeColor(layout.getChildAt(i) as BottomNavigationView)
            }
            if (layout.getChildAt(i) is TextInputLayout) {
                //setTextInputLayoutThemeColor((TextInputLayout) layout.getChildAt(i));
            }

        }
    }

    /*Drawableに染色する*/
    private fun setDrawableColor(drawable: Drawable): Drawable {
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> drawable.setTint(Color.parseColor("#ffffff"))
            Configuration.UI_MODE_NIGHT_YES -> drawable.setTint(Color.parseColor("#000000"))
        }
        return drawable
    }

    /*BottomNavigationBer*/
    fun setBottomNavigationBerThemeColor(view: BottomNavigationView) {
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                //view.setItemBackgroundResource(R.color.white);
                view.itemTextColor = ColorStateList.valueOf(context.resources.getColor(R.color.colorAccent, null))
                view.itemIconTintList = ColorStateList.valueOf(context.resources.getColor(R.color.colorAccent, null))
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                //view.setItemBackgroundResource(R.color.black);
                view.itemTextColor = ColorStateList.valueOf(context.resources.getColor(R.color.white, null))
                view.itemIconTintList = null
            }
        }
    }

    /*Snackberの色*/
    fun setSnackBerThemeColor(snackbar: Snackbar) {
        snackbar.view.rootView.setBackgroundColor(context.getColor(R.color.black))
    }


    /*TextInoutLayout*/
    fun setTextInputLayoutThemeColor(view: TextInputLayout) {
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                view.boxStrokeColor = context.resources.getColor(R.color.white, null)
                view.boxBackgroundColor = context.resources.getColor(R.color.white, null)
                view.setBoxBackgroundMode(context.resources.getColor(R.color.white, null))
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                view.boxStrokeColor = context.resources.getColor(R.color.black, null)
                view.boxBackgroundColor = context.resources.getColor(R.color.black, null)
                view.setBoxBackgroundMode(context.resources.getColor(R.color.black, null))
            }
        }
    }

    /*EditText*/
    fun setEditTextThemeColor(view: EditText) {
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> view.setHintTextColor(context.resources.getColor(android.R.color.darker_gray, context.theme))
            Configuration.UI_MODE_NIGHT_YES -> {
                view.setHintTextColor(context.resources.getColor(android.R.color.darker_gray, context.theme))
                view.backgroundTintList = context.resources.getColorStateList(android.R.color.white, context.theme)
            }
        }//view.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
    }

    /*ImageView*/
    fun setImageViewThemeColor(view: ImageView) {
        when (nightMode) {
            Configuration.UI_MODE_NIGHT_NO -> view.imageTintList = ColorStateList.valueOf(context.getColor(R.color.black))
            Configuration.UI_MODE_NIGHT_YES -> view.imageTintList = ColorStateList.valueOf(context.getColor(R.color.white))
        }
    }

}
