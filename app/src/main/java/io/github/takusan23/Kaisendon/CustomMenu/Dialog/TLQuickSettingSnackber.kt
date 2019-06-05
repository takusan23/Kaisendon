package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

import org.chromium.customtabsclient.shared.CustomTabsHelper

import java.util.ArrayList

import io.github.takusan23.Kaisendon.Activity.KonoAppNiTuite
import io.github.takusan23.Kaisendon.Activity.LoginActivity
import io.github.takusan23.Kaisendon.Activity.UserActivity
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuLoadSupport
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSQLiteHelper
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.DesktopTL.DesktopFragment
import io.github.takusan23.Kaisendon.FloatingTL.FloatingTL
import io.github.takusan23.Kaisendon.Fragment.AccountListFragment
import io.github.takusan23.Kaisendon.Fragment.ActivityPubViewer
import io.github.takusan23.Kaisendon.Fragment.Bookmark_Frament
import io.github.takusan23.Kaisendon.Fragment.License_Fragment
import io.github.takusan23.Kaisendon.Fragment.SettingFragment
import io.github.takusan23.Kaisendon.Fragment.WearFragment
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.Omake.KaisendonLife
import io.github.takusan23.Kaisendon.R

class TLQuickSettingSnackber(private val context: Activity?, view: View) {
    private val view: View? = null
    /*SnackBer*/
    //表示
    val snackbar: Snackbar

    private var bottomNavigationView: BottomNavigationView? = null
    private var tts_Switch: Switch? = null
    private var color_EditText: EditText? = null
    //private FragmentTransaction transaction;
    private var pref_setting: SharedPreferences? = null
    private var navigationView: NavigationView? = null
    private val helper: CustomMenuSQLiteHelper? = null
    private val db: SQLiteDatabase? = null
    private var customMenuLoadSupport: CustomMenuLoadSupport? = null
    private var switch_LinearLayout: LinearLayout? = null
    private var list: ArrayList<String>? = null
    private var darkModeSupport: DarkModeSupport? = null
    private var icon: ImageView? = null
    private val tl_qs_color_edittext_linearlayout: LinearLayout? = null


    /*読み上げするかを返す*/
    val timelineTTS: Boolean
        get() {
            var `is` = false
            if (tts_Switch != null && tts_Switch!!.isChecked) {
                `is` = true
            }
            return `is`
        }

    /*ハイライトする文字を返す*/
    val highlightText: String
        get() {
            var text = ""
            if (color_EditText != null) {
                text = color_EditText!!.text.toString()
            }
            return text
        }

    init {
        this.snackbar = setSnackBer()
    }

    private fun setSnackBer(): Snackbar {
        val view = context!!.findViewById<View>(R.id.container_public)
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)
        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //TextViewを非表示にする
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.visibility = View.INVISIBLE
        //Linearlayout
        val main_LinearLayout = LinearLayout(context)
        main_LinearLayout.orientation = LinearLayout.VERTICAL
        main_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.tl_quick_settings_bottom_fragment, main_LinearLayout)
        snackBer_viewGrop.addView(main_LinearLayout, 0)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        bottomNavigationView = main_LinearLayout.findViewById(R.id.tl_qs_menu)
        tts_Switch = main_LinearLayout.findViewById(R.id.tl_qs_tts_switch)
        color_EditText = main_LinearLayout.findViewById(R.id.tl_qs_color_edittext)
        icon = main_LinearLayout.findViewById(R.id.imageView2)
        navigationView = context.findViewById(R.id.nav_view)
        customMenuLoadSupport = CustomMenuLoadSupport(context, navigationView!!)
        switch_LinearLayout = main_LinearLayout.findViewById(R.id.tl_qs_switch_linearlayout)

        darkModeSupport = DarkModeSupport(context)
        darkModeSupport!!.setBottomNavigationBerThemeColor(bottomNavigationView!!)

        setClickEvent()
        setDarkmodeSwitch()

        return snackbar
    }

    private fun setClickEvent() {
        //trueでアニメーションされる？
        bottomNavigationView!!.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.tl_qs_account -> setAccountPopupMenu()
                R.id.tl_qs_bookmark -> setBookmark()
                R.id.tl_qs_desktop_mode -> setDesktopMode()
                R.id.tl_qs_floating_tl -> showFloatingMenu()
                R.id.tl_qs_sonota -> setSonotaMenu()
            }
            true
        }
    }

    /*Floating TL*/
    private fun showFloatingTL(isPiP: Boolean) {
        if (context != null) {
            val fragment = (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.container_container)
            if (fragment is CustomMenuTimeLine) {
                val floatingTL = FloatingTL(context, fragment.arguments!!.getString("json")!!)
                floatingTL.setNotification(isPiP)
            } else {
                Toast.makeText(context, context.getString(R.string.floating_tl_error_custom_tl), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*デスクトップモード*/
    private fun setDesktopMode() {
        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_container, DesktopFragment(), "desktop")
        transaction.commit()
    }

    /*ぶっくまーく*/
    private fun setBookmark() {
        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_container, Bookmark_Frament())
        transaction.commit()
    }

    /*ポップアップメニュー*/
    @SuppressLint("RestrictedApi")
    private fun setAccountPopupMenu() {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(context!!)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.tl_qs_account_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, bottomNavigationView!!)
        optionsMenu.setForceShowIcon(true)
        //表示
        optionsMenu.show()
        //押したときの反応
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.home_menu_login -> {
                        val login = Intent(context, LoginActivity::class.java)
                        context.startActivity(login)
                    }
                    R.id.home_menu_account_list -> {
                        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.container_container, AccountListFragment())
                        transaction.commit()
                    }
                    R.id.home_menu_account -> {
                        val intent = Intent(context, UserActivity::class.java)
                        if (list != null) {
                            if (CustomMenuTimeLine.isMisskeyMode) {
                                intent.putExtra("Account_ID", CustomMenuTimeLine.account_id)
                            } else {
                                intent.putExtra("Account_ID", list!![0])
                            }
                            intent.putExtra("my", true)
                            context.startActivity(intent)
                        }
                    }
                }
                return false
            }

            override fun onMenuModeChange(menuBuilder: MenuBuilder) {

            }
        })
    }


    /*その他のメニュー*/
    @SuppressLint("RestrictedApi")
    private fun setSonotaMenu() {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(context!!)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.tl_qs_sonota_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, bottomNavigationView!!)
        optionsMenu.setForceShowIcon(true)
        //表示
        optionsMenu.show()
        //押したときの反応
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                when (menuItem.itemId) {
                    R.id.home_menu_activity_pub_viewer -> {
                        transaction.replace(R.id.container_container, ActivityPubViewer())
                        transaction.commit()
                    }
                    R.id.home_menu_reload_menu -> {
                        //再読み込み
                        navigationView!!.menu.clear()
                        navigationView!!.inflateMenu(R.menu.custom_menu)
                        customMenuLoadSupport!!.loadCustomMenu(null)
                    }
                    R.id.home_menu_setting -> {
                        transaction.replace(R.id.container_container, SettingFragment())
                        transaction.commit()
                    }
                    R.id.home_menu_license -> {
                        transaction.replace(R.id.container_container, License_Fragment())
                        transaction.commit()
                    }
                    R.id.home_menu_thisapp -> {
                        val thisApp = Intent(context, KonoAppNiTuite::class.java)
                        context.startActivity(thisApp)
                    }
                    R.id.home_menu_privacy_policy -> showPrivacyPolicy()
                    R.id.home_menu_wear -> {
                        transaction.replace(R.id.container_container, WearFragment())
                        transaction.commit()
                    }
                    R.id.home_menu_instance_info -> {
                        val instanceInfoBottomFragment = InstanceInfoBottomFragment()
                        instanceInfoBottomFragment.show(transaction, "instance_info")
                    }
                    R.id.home_menu_kaisendon_life -> {
                        val intent = Intent(bottomNavigationView!!.context, KaisendonLife::class.java)
                        bottomNavigationView!!.context.startActivity(intent)
                    }
                }
                return false
            }

            override fun onMenuModeChange(menuBuilder: MenuBuilder) {

            }
        })
    }

    /*フローティングメニュー*/
    @SuppressLint("RestrictedApi")
    private fun showFloatingMenu() {
        if (Build.VERSION.CODENAME.contains("Q")) {
            //QのユーザーはBubbleかPiPか選べるように
            //ポップアップメニュー作成
            val menuBuilder = MenuBuilder(context!!)
            val inflater = MenuInflater(context)
            inflater.inflate(R.menu.floating_tl_menu, menuBuilder)
            val optionsMenu = MenuPopupHelper(context, menuBuilder, bottomNavigationView!!)
            optionsMenu.setForceShowIcon(true)
            //表示
            optionsMenu.show()
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.home_menu_floating_pip -> showFloatingTL(true)
                        R.id.home_menu_floating_bubble -> showFloatingTL(false)
                    }
                    return false
                }

                override fun onMenuModeChange(menu: MenuBuilder) {

                }
            })
        } else {
            //PiPで起動
            showFloatingTL(true)
        }
    }


    /**
     * プライバシーポリシー
     */
    private fun showPrivacyPolicy() {
        val githubUrl = "https://github.com/takusan23/Kaisendon/blob/master/kaisendon-privacy-policy.md"
        if (pref_setting!!.getBoolean("pref_chrome_custom_tabs", true)) {
            val back_icon = BitmapFactory.decodeResource(context!!.resources, R.drawable.ic_action_arrow_back)
            val custom = CustomTabsHelper.getPackageNameToUse(context)
            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(custom)
            customTabsIntent.launchUrl(context, Uri.parse(githubUrl))
        } else {
            val uri = Uri.parse(githubUrl)
            val browser = Intent(Intent.ACTION_VIEW, uri)
            context!!.startActivity(browser)
        }
    }

    /*Android Pie以前ユーザー用にダークモードスイッチを用意する*/
    private fun setDarkmodeSwitch() {
        if (Build.VERSION.CODENAME != "Q") {
            val sw = Switch(context)
            sw.text = context!!.getText(R.string.darkmode)
            sw.setTextColor(context.getColor(R.color.white))
            sw.isChecked = pref_setting!!.getBoolean("darkmode", false)
            val editor = pref_setting!!.edit()
            sw.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    editor.putBoolean("darkmode", true)
                } else {
                    editor.putBoolean("darkmode", false)
                }
                context.startActivity(Intent(context, Home::class.java))
                editor.apply()
            }
            switch_LinearLayout!!.addView(sw)
        }
    }

    fun showSnackBer() {
        //表示
        snackbar.show()
    }

    fun dismissSnackBer() {
        //表示
        snackbar.dismiss()
    }

    /*配列を設定*/
    fun setList(list: ArrayList<String>) {
        this.list = list
    }

}
