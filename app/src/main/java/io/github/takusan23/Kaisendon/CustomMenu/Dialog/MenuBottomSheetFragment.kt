package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.APIJSONParse.CustomMenuJSONParse
import io.github.takusan23.Kaisendon.Activity.KonoAppNiTuite
import io.github.takusan23.Kaisendon.Activity.LoginActivity
import io.github.takusan23.Kaisendon.Activity.UserActivity
import io.github.takusan23.Kaisendon.CustomMenu.AddCustomMenuBottomFragment
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSwipeSwitch
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.DesktopTL.DesktopFragment
import io.github.takusan23.Kaisendon.FloatingTL.FloatingTL
import io.github.takusan23.Kaisendon.FloatingTL.KaisendonMiniView
import io.github.takusan23.Kaisendon.Fragment.*
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.Omake.KaisendonLife
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.Theme.DarkModeSupport
import kotlinx.android.synthetic.main.bottom_fragment_menu.*
import org.chromium.customtabsclient.shared.CustomTabsHelper

class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    lateinit var pref_setting: SharedPreferences
    var navColor: Int? = null

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
        return inflater.inflate(R.layout.bottom_fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navColor = activity?.window?.navigationBarColor
        activity?.window?.navigationBarColor = Color.parseColor("#ffffff")

        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)

        menu_bottom_sheet_bottom_nav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tl_qs_account -> setAccountPopupMenu()
                R.id.tl_qs_bookmark -> setBookmark()
                R.id.tl_qs_edit -> showCustomMenuEditor()
                R.id.tl_qs_display_method -> showDisplayMethodMenu()
                R.id.tl_qs_sonota -> setSonotaMenu()
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (navColor != null) {
            activity?.window?.navigationBarColor = navColor as Int
        }
    }

    /*編集画面出す*/
    private fun showCustomMenuEditor() {
        val fragment = (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.container_container)
        if (fragment is CustomMenuTimeLine) {
            val customMenuBottomFragment = AddCustomMenuBottomFragment()
            val bundle = Bundle()
            bundle.putBoolean("delete_button", true)
            bundle.putString("name", fragment.customMenuJSONParse.name)
            customMenuBottomFragment.arguments = bundle
            customMenuBottomFragment.show((context as AppCompatActivity).supportFragmentManager, "add_custom_menu")
        }
    }

    /*ポップアップメニュー*/
    @SuppressLint("RestrictedApi")
    private fun setAccountPopupMenu() {
        if (context != null) {
            //ポップアップメニュー作成
            val menuBuilder = MenuBuilder(context!!)
            val inflater = MenuInflater(context)
            inflater.inflate(R.menu.tl_qs_account_menu, menuBuilder)
            val optionsMenu = MenuPopupHelper(context!!, menuBuilder, menu_bottom_sheet_bottom_nav)
            optionsMenu.setForceShowIcon(true)
            //表示
            optionsMenu.show()
            //押したときの反応
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.home_menu_login -> {
                            val login = Intent(context, LoginActivity::class.java)
                            context?.startActivity(login)
                        }
                        R.id.home_menu_account_list -> {
                            val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                            transaction.replace(R.id.container_container, AccountListFragment())
                            transaction.commit()
                        }
                        R.id.home_menu_account -> {
                            val intent = Intent(context, UserActivity::class.java)
                            if (CustomMenuTimeLine.isMisskeyMode) {
                                intent.putExtra("Account_ID", arguments?.getString("account"))
                            } else {
                                intent.putExtra("Account_ID", arguments?.getString("account"))
                            }
                            intent.putExtra("my", true)
                            context?.startActivity(intent)
                        }
                    }
                    return false
                }

                override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                }
            })
        }
    }


    /*その他のメニュー*/
    @SuppressLint("RestrictedApi")
    private fun setSonotaMenu() {
        if (context != null) {
            //ポップアップメニュー作成
            val menuBuilder = MenuBuilder(context!!)
            val inflater = MenuInflater(context)
            inflater.inflate(R.menu.tl_qs_sonota_menu, menuBuilder)
            val optionsMenu = MenuPopupHelper(context!!, menuBuilder, menu_bottom_sheet_bottom_nav)
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
/*
                            //再読み込み
                            navigationView!!.menu.clear()
                            navigationView!!.inflateMenu(R.menu.custom_menu)
                            customMenuLoadSupport!!.loadCustomMenu(null)
*/
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
                            context?.startActivity(thisApp)
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
                            val intent = Intent(context, KaisendonLife::class.java)
                            context?.startActivity(intent)
                        }
                        R.id.home_menu_guide -> {
                            showDocument()
                        }
                    }
                    return false
                }

                override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                }
            })
        }
    }

    /*Floating TL*/
    private fun showFloatingTL(isPiP: Boolean) {
        if (context != null) {
            val fragment = (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.container_container)
            if (fragment is CustomMenuTimeLine) {
                val floatingTL = FloatingTL(activity as AppCompatActivity, fragment.arguments!!.getString("setting_json")!!)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    floatingTL.setNotification(isPiP)
                }
            } else {
                Toast.makeText(context, context?.getString(R.string.floating_tl_error_custom_tl), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*かいせんどんミニのパーミッション*/
    fun showKaisendonMiniPermission() {
        val fragment = (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.container_container)
        val json = fragment?.arguments!!.getString("setting_json")
        val customMenuJSONParse = CustomMenuJSONParse(json ?: "")
        //ポップアップ再生。コメント付き
        if (!Settings.canDrawOverlays(context)) {
            //RuntimePermissionに対応させる
            // 権限取得
            val intent =
                    Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context?.getPackageName()}")
                    );
            activity?.startActivityForResult(intent, 114)
        } else {
            if (json != null) {
                //Mastodon限定
                if (!customMenuJSONParse.misskey.toBoolean()) {
                    val kaisendonMiniView = KaisendonMiniView(activity as AppCompatActivity, json)
                    kaisendonMiniView.showKaisendonMini()
                }
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


    /*
    * 説明書だす
    * */
    fun showDocument() {
        val documentUrl = "https://github.com/takusan23/Kaisendon/wiki"
        val chrome_custom_tabs = pref_setting!!.getBoolean("pref_chrome_custom_tabs", true)
        if (chrome_custom_tabs) {
            val back_icon = BitmapFactory.decodeResource(context?.resources, R.drawable.ic_action_arrow_back)
            val custom = CustomTabsHelper.getPackageNameToUse(context)
            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(custom)
            customTabsIntent.launchUrl(context, Uri.parse(documentUrl))
        } else {
            val uri = Uri.parse(documentUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context?.startActivity(intent)
        }
    }

    /*フローティングメニュー*/
    @SuppressLint("RestrictedApi")
    private fun showFloatingMenu() {
        //QのユーザーはBubbleかPiPか選べるように
        //ポップアップメニュー作成
        if (context != null) {
            val menuBuilder = MenuBuilder(context!!)
            val inflater = MenuInflater(context)
            inflater.inflate(R.menu.floating_tl_menu, menuBuilder)
            val optionsMenu = MenuPopupHelper(context!!, menuBuilder, menu_bottom_sheet_bottom_nav)
            optionsMenu.setForceShowIcon(true)
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.home_menu_floating_pip -> showFloatingTL(true)
                        R.id.home_menu_floating_bubble -> showFloatingTL(false)
                        R.id.home_menu_floating_kaisendon_mini -> showKaisendonMiniPermission()
                    }
                    return false
                }

                override fun onMenuModeChange(menu: MenuBuilder) {

                }
            })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //表示
                optionsMenu.show()
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                //Android Pie以前ではBubbleのAPIが無いのでメニュー非表示
                menuBuilder.findItem(R.id.home_menu_floating_bubble).isVisible = false
                //表示
                optionsMenu.show()
            } else {
                showKaisendonMiniPermission()
            }
        }
    }

    /*表示方法メニュー*/
    @SuppressLint("RestrictedApi")
    private fun showDisplayMethodMenu() {
        if (context != null) {
            val menuBuilder = MenuBuilder(context!!)
            val inflater = MenuInflater(context)
            inflater.inflate(R.menu.display_method_menu, menuBuilder)
            val optionsMenu = MenuPopupHelper(context!!, menuBuilder, menu_bottom_sheet_bottom_nav)
            optionsMenu.setForceShowIcon(true)
            //表示
            optionsMenu.show()
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.home_menu_desktop_mode -> setDesktopMode()
                        R.id.home_menu_floating_mode -> showFloatingMenu()
                        R.id.home_menu_view_pager_mode -> showSwipeSwitchingMode()
                    }
                    return false
                }

                override fun onMenuModeChange(menu: MenuBuilder) {

                }
            })
        }
    }

    /*スワイプ切り替えモード（開発中）*/
    private fun showSwipeSwitchingMode() {
        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_container, CustomMenuSwipeSwitch(), "swipe_switch")
        transaction.commit()
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
        if (context != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val sw = Switch(context)
                sw.text = context!!.getText(R.string.darkmode)
                sw.setTextColor(context!!.getColor(R.color.white))
                sw.isChecked = pref_setting!!.getBoolean("darkmode", false)
                val editor = pref_setting!!.edit()
                sw.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        editor.putBoolean("darkmode", true)
                    } else {
                        editor.putBoolean("darkmode", false)
                    }
                    context?.startActivity(Intent(context, Home::class.java))
                    editor.apply()
                }
                switch_LinearLayout.addView(sw)
            }
        }
    }


}