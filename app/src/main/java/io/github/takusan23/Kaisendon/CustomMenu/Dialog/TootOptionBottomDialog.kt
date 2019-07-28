package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.annotation.SuppressLint
import android.content.*
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.util.DisplayMetrics
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import io.github.takusan23.Kaisendon.Activity.UserActivity
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuLoadSupport
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSQLiteHelper
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.TootBookmark_SQLite
import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern


class TootOptionBottomDialog : BottomSheetDialogFragment() {

    private var pref_setting: SharedPreferences? = null
    private var view_: View? = null
    private var toot_option_LinearLayout: LinearLayout? = null
    private var account_Button: TextView? = null
    private var bookmark_Button: TextView? = null
    private var copy_TextView: TextView? = null
    private var copy_toot_id_TextView: TextView? = null
    private var browser_TextView: TextView? = null
    private var lockback_TextView: TextView? = null
    private val favourite_TextView: TextView? = null
    private val boost_TextView: TextView? = null
    private val padding = 35

    //BookMarkDB
    private var tootBookmark_sqLite: TootBookmark_SQLite? = null
    private var db: SQLiteDatabase? = null
    private var customMenuSQLiteHelper: CustomMenuSQLiteHelper? = null
    private var custom_SqLiteDatabase: SQLiteDatabase? = null


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
        return inflater.inflate(R.layout.toot_option_button_dialog_layout, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.view_ = view
        val darkModeSupport = DarkModeSupport(context!!)
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context!!)
        account_Button = view.findViewById(R.id.toot_option_account_button)
        bookmark_Button = view.findViewById(R.id.toot_option_bookmark_button)
        copy_TextView = view.findViewById(R.id.toot_option_copy_button)
        copy_toot_id_TextView = view.findViewById(R.id.toot_option_toot_id_copy)
        browser_TextView = view.findViewById(R.id.toot_option_browser)
        toot_option_LinearLayout = view.findViewById(R.id.toot_option_linearlayout)
        lockback_TextView = view.findViewById(R.id.lockback_timeline_textview)

        //SQLite
        if (customMenuSQLiteHelper == null) {
            customMenuSQLiteHelper = CustomMenuSQLiteHelper(context!!)
        }
        if (custom_SqLiteDatabase == null) {
            custom_SqLiteDatabase = customMenuSQLiteHelper!!.writableDatabase
            custom_SqLiteDatabase!!.disableWriteAheadLogging()
        }
        //ハッシュタグ
        setHashtagButton()
        //ニコニコ大百科
        setNicoNicoPedia()

        //だーくもーど
        darkModeSupport.setLayoutAllThemeColor(view as LinearLayout)


        //クリック
        account_Button!!.setOnClickListener {
            val intent = Intent(account_Button!!.context, UserActivity::class.java)
            //IDを渡す
            if (CustomMenuTimeLine.isMisskeyMode) {
                intent.putExtra("Misskey", true)
            }
            intent.putExtra("Account_ID", arguments!!.getString("user_id"))
            account_Button!!.context.startActivity(intent)
        }
        //クリップボード
        copy_TextView!!.setOnClickListener {
            if (activity != null) {
                val clipboardManager = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("とぅーとこぴー", arguments!!.getString("status_text")))
                    Toast.makeText(context, getString(R.string.copy) + " : " + arguments!!.getString("status_text"), Toast.LENGTH_SHORT).show()
                }
                dismiss()
            }
        }
        //ブックマーク
        bookmark_Button!!.setOnClickListener {
            if (tootBookmark_sqLite == null) {
                tootBookmark_sqLite = TootBookmark_SQLite(context!!)
            }
            if (db == null) {
                db = tootBookmark_sqLite!!.writableDatabase
                db!!.disableWriteAheadLogging()
            }
            //DBに入れる
            val json = arguments!!.getString("json")
            val instance = arguments!!.getString("instance")
            val values = ContentValues()
            values.put("json", json)
            values.put("instance", instance)
            db!!.insert("tootbookmarkdb", null, values)
            Toast.makeText(context, getString(R.string.add_Bookmark), Toast.LENGTH_SHORT).show()
            dismiss()
        }
        //トゥートID
        copy_toot_id_TextView!!.setOnClickListener {
            if (activity != null) {
                val clipboardManager = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(arguments!!.getString("status_id"), "copy"))
                    Toast.makeText(context, getString(R.string.copy) + " : " + arguments!!.getString("status_id"), Toast.LENGTH_SHORT).show()
                }
                dismiss()
            }
        }
        //ブラウザで開く
        browser_TextView!!.setOnClickListener {
            val url = "https://" + arguments!!.getString("instance") + "/@" + arguments!!.getString("user_name") + "/" + arguments!!.getString("status_id")
            if (pref_setting!!.getBoolean("pref_chrome_custom_tabs", true)) {
                val back_icon = BitmapFactory.decodeResource(context!!.resources, R.drawable.ic_action_arrow_back)
                val custom = CustomTabsHelper.getPackageNameToUse(context)
                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                val customTabsIntent = builder.build()
                customTabsIntent.intent.setPackage(custom)
                customTabsIntent.launchUrl(context, Uri.parse(url))
            } else {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        //振り返りタイムライン
        //Mastodonのみ対応？
        if (!arguments!!.getString("instance")!!.contains("misskey")) {
            lockback_TextView!!.setOnClickListener {
                val dialog = LockBackTimelineBottomFragment()
                var bundle = arguments
                //ポップアップメニュー作成
                val menuBuilder = MenuBuilder(context!!)
                val inflater = MenuInflater(context)
                inflater.inflate(R.menu.lockback_tl_menu, menuBuilder)
                val optionsMenu = MenuPopupHelper(context!!, menuBuilder, lockback_TextView!!)
                optionsMenu.setForceShowIcon(true)
                //表示
                optionsMenu.show()
                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    override fun onMenuItemSelected(menu: MenuBuilder?, item: MenuItem?): Boolean {
                        //URL設定
                        when (item!!.itemId) {
                            R.id.lockback_tl_home -> {
                                bundle!!.putString("lockback_url", "/api/v1/timelines/home")
                            }
                            R.id.lockback_tl_local -> {
                                bundle!!.putString("lockback_url", "/api/v1/timelines/public?local=true")
                            }
                        }
                        dialog.arguments = bundle
                        fragmentManager?.let { it1 -> dialog.show(it1, "lockback_tl") }
                        return false
                    }

                    override fun onMenuModeChange(menu: MenuBuilder?) {
                    }
                })


            }
        } else {
            (lockback_TextView!!.parent as LinearLayout).removeView(lockback_TextView!!)
        }

    }

    //ハッシュタグ
    private fun setHashtagButton() {
        val spannableString = SpannableString(arguments!!.getString("status_text"))
        //正規表現
        //パクった→https://qiita.com/corin8823/items/75309761833d823cac6f
        val hashtag_Matcher = Pattern.compile("#([Ａ-Ｚａ-ｚA-Za-z一-鿆0-9０-９ぁ-ヶｦ-ﾟー]+)").matcher(spannableString)
        val nicoID_Matcher = Pattern.compile("(sm|nw)([0-9]+)").matcher(spannableString)
        while (hashtag_Matcher.find()) {
            val textView = TextView(context)
            textView.setPadding(padding, padding, padding, padding)
            textView.text = hashtag_Matcher.group()
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_label_outline_black_24dp, activity!!.theme), null, null, null)
            toot_option_LinearLayout!!.addView(textView)
            //押したらメニュー追加
            setHashtagMenu(hashtag_Matcher.group(), textView)
        }
        var temp_id: String? = null
        while (nicoID_Matcher.find()) {
            //動画リンクと#smがあると2つ生成されるので回避する
            if (temp_id == null) {
                temp_id = nicoID_Matcher.group()
                setニコニコで開く(nicoID_Matcher.group())
            } else {
                if (!temp_id.contains(nicoID_Matcher.group())) {
                    //かぶってなかったら生成
                    setニコニコで開く(nicoID_Matcher.group())
                } else {
                    //同じだったらnullにしとく
                    temp_id = null
                }
            }
        }
    }

    //ニコニコ動画で開く
    private fun setニコニコで開く(title: String) {
        var title = title
        val id = title
        if (title.contains("nw")) {
            setニコニコニュースで開く(title)
        } else {
            //かぶってなかったら生成
            val textView = TextView(context)
            //日本語と英語でわける。英語わけわかめ
            if (getString(R.string.open_nicovideo).contains("をニコニコ動画で開く")) {
                title = title + getString(R.string.open_nicovideo)
            } else {
                title = "Open" + title + getString(R.string.open_nicovideo)
            }
            textView.setPadding(padding, padding, padding, padding)
            textView.text = title
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_open_in_new_black_24dp, activity!!.theme), null, null, null)
            toot_option_LinearLayout!!.addView(textView)
            //押したらリンク
            setニコニコ動画ID(id, textView)
        }
    }

    //ニコニコニュースで開く
    private fun setニコニコニュースで開く(title: String) {
        var title = title
        val id = title
        //かぶってなかったら生成
        val textView = TextView(context)
        title = title + "をニコニコニュースで開く"
        textView.setPadding(padding, padding, padding, padding)
        textView.text = title
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_open_in_new_black_24dp, activity!!.theme), null, null, null)
        toot_option_LinearLayout!!.addView(textView)
        //押したらリンク
        setニコニコニュース(id, textView)
    }


    //メニュー作成
    @SuppressLint("RestrictedApi")
    private fun setHashtagMenu(name: String, view: View) {
        val menuBuilder = MenuBuilder(context!!)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.add_hashtag_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context!!, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)
        view.setOnClickListener {
            //表示
            optionsMenu.show()
            //押したときの反応
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                    val id = name.replace("#", "")
                    when (item.itemId) {
                        R.id.add_hashtag_tl_local -> insertDB(id, "/api/v1/timelines/tag/?local=true")
                        R.id.add_hashtag_tl_public -> insertDB(id, "/api/v1/timelines/tag/")
                    }
                    return false
                }

                override fun onMenuModeChange(menu: MenuBuilder) {

                }
            })
        }
    }

    //動画ID？
    private fun setニコニコ動画ID(id: String, view: View) {
        view.setOnClickListener {
            var base_url = "https://www.nicovideo.jp/watch/"
            base_url += id
            useCustomTabs(base_url)
        }
    }

    //ニコニコニュース
    private fun setニコニコニュース(id: String, view: View) {
        view.setOnClickListener {
            var base_url = "https://news.nicovideo.jp/watch/"
            base_url += id
            useCustomTabs(base_url)
        }
    }

    //データベース追加
    private fun insertDB(title: String, contentUrl: String) {
        if (arguments!!.getString("cmtl_name") != null) {
            val cmtl_name = arguments!!.getString("cmtl_name")
            val cursor = custom_SqLiteDatabase!!.query(
                    "custom_menudb",
                    arrayOf("setting"),
                    "name=?",
                    arrayOf<String>(cmtl_name!!), null, null, null
            )
            cursor.moveToFirst()
            for (i in 0 until cursor.count) {
                try {
                    val jsonObject = JSONObject(cursor.getString(0))
                    jsonObject.put("name", title)
                    jsonObject.put("content", contentUrl)
                    val values = ContentValues()
                    values.put("name", title)
                    values.put("setting", jsonObject.toString())
                    custom_SqLiteDatabase!!.insert("custom_menudb", null, values)
                    reLoadMenu()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                cursor.moveToNext()
            }
            cursor.close()
        }
    }

    /**
     * CustomTab
     */
    private fun useCustomTabs(url: String) {
        val chrome_custom_tabs = pref_setting!!.getBoolean("pref_chrome_custom_tabs", true)
        //カスタムタグ有効
        if (chrome_custom_tabs) {
            val back_icon = BitmapFactory.decodeResource(context!!.resources, R.drawable.ic_action_arrow_back)
            val custom = CustomTabsHelper.getPackageNameToUse(context)
            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(custom)
            customTabsIntent.launchUrl(context, Uri.parse(url))
            //無効
        } else {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context!!.startActivity(intent)
        }
    }

    /*めにゅー再生成*/
    private fun reLoadMenu() {
        val navigationView = activity!!.findViewById<NavigationView>(R.id.nav_view)
        val customMenuLoadSupport = CustomMenuLoadSupport(context!!, navigationView)
        //再読み込み
        navigationView.menu.clear()
        navigationView.inflateMenu(R.menu.custom_menu)
        customMenuLoadSupport.loadCustomMenu(null)
    }

    //ニコニコ大百科にも対応させる？
    private fun setNicoNicoPedia() {
        val status = arguments!!.getString("status_text")
        if (status!!.contains("https://dic.nicovideo.jp/")) {
            //正規表現でURL取り出し
            val pattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+")
            val matcher = pattern.matcher(status)
            if (matcher.find()) {
                //かぶってなかったら生成
                val textView = TextView(context)
                //タイトルだけ取得するのでそれ以外を消す
                val title = getNicoNicoPediaTitle(status, matcher.group())
                textView.setPadding(padding, padding, padding, padding)
                textView.text = title
                textView.gravity = Gravity.CENTER_VERTICAL
                textView.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_open_in_new_black_24dp, activity!!.theme), null, null, null)
                toot_option_LinearLayout!!.addView(textView)
                //押したらリンク
                textView.setOnClickListener { useCustomTabs(matcher.group()) }
            }
        }
    }

    //ニコニコ大百科のタイトルだけ取得
    private fun getNicoNicoPediaTitle(title: String, url: String): String {
        var title = title
        title = title.replace("- ニコ百", "")
        title = title.replace("#nicopedia", "")
        title = title.replace(url, "")
        title = title.replace("\n", "")
        if (getString(R.string.open_niconico_pedia).contains("をニコニコ大百科で開く")) {
            title = title + getString(R.string.open_niconico_pedia)
        } else {
            title = "Open " + title + getString(R.string.open_niconico_pedia)
        }
        return title
    }

    /*
    * 幅を設定する
    * */
    override fun onResume() {
        super.onResume()
        // https://stackoverflow.com/questions/38436130/how-to-set-max-width-for-bottomsheetdialogfragment/38477466
        val windowManager = activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val width = if (displayMetrics.widthPixels < 1280) displayMetrics.widthPixels else 1280
        val height = -1 // MATCH_PARENT
        dialog?.getWindow()?.setLayout(width, height)
    }

}
