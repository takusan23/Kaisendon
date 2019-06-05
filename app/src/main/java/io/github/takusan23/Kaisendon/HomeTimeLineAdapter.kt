package io.github.takusan23.Kaisendon

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken
import io.github.takusan23.Kaisendon.Activity.UserActivity
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.CustomTabURL.LinkTransformationMethod
import okhttp3.*
import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutionException

class HomeTimeLineAdapter
/**
 * コンストラクタ
 *
 * @param context  コンテキスト
 * @param resource リソースID
 * @param items    リストビューの要素
 */
(context: Context, private val mResource: Int, private val mItems: List<ListItem>) : ArrayAdapter<ListItem>(context, mResource, mItems) {
    private val mTv: TextView? = null

    //String AccessToken = null;
    //インスタンス
    internal var Instance: String? = null
    private val mInflater: LayoutInflater
    private val layoutId: Int = 0
    private val visibleSet = HashSet<Int>()
    internal val handler_1 = android.os.Handler()
    private var sqLite: TootBookmark_SQLite? = null
    private var sqLiteDatabase: SQLiteDatabase? = null
    //カスタム絵文字関係
    internal var final_toot_text: String? = null
    internal var custom_emoji_src: String? = null
    internal var avater_emoji = false
    internal var avater_custom_emoji_src: String? = null
    internal var nicoru_text: String? = null
    internal var emoji_name: String? = null
    internal var emoji_name_list = ArrayList<String>()

    //メディア
    internal var media_url_1: String? = null
    internal var media_url_2: String? = null
    internal var media_url_3: String? = null
    internal var media_url_4: String? = null


    //ViewHolder
    private var holder: ViewHolder? = null

    //絵文字表示するか
    private var emojis_show: Boolean = false

    //ブックマークのボタンの動作決定部分
    internal var bookmark_delete = false

    //通知のフラグメントのときは画像非表示モードでもレイアウトを消さないように
    internal var notification_layout = false

    //カスタムメニュー用
    private var dialog_not_show = false    //ダイアログ出さない
    private var image_show = false         //強制画像表示
    private var quick_profile = false      //クイックプロフィール有効
    private val custom_emoji = false       //トゥートカウンターを有効
    private var gif_notPlay = false                    //GIFアニメ有効
    private var font_path = ""                  //フォントのパス

    //settingのプリファレンスをとる
    internal var pref_setting = getDefaultSharedPreferences(context)

    internal var AccessToken: String? = null

    //一度だけ実行するように
    private val one = false
    private val reaction_text = ""

    init {
        mInflater = LayoutInflater.from(context)
        //this.layoutId = layoutId

    }

    @SuppressLint("RestrictedApi")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView

        //Emoji
        val config = BundledEmojiCompatConfig(context)
        config.setReplaceAll(true)
        EmojiCompat.init(config)
        val compat = EmojiCompat.get()

        //メディア

        //データの再利用を許さない！！！！！！！！！！！！！！！！！！！
        if (convertView == null) {

            view = mInflater.inflate(R.layout.timeline_item, parent, false)

            holder = ViewHolder()

            holder!!.linearLayoutMediaButton = view!!.findViewById(R.id.linearlayout_mediaButton)
            holder!!.linearLayoutMedia = view.findViewById(R.id.linearlayout_media)
            holder!!.linearLayoutMedia2 = view.findViewById(R.id.linearlayout_media2)
            holder!!.linearLayoutEnquate = view.findViewById(R.id.linearlayout_enquate)
            holder!!.vw1 = view.findViewById(R.id.vw1)
            holder!!.toot_linearLayout = view.findViewById(R.id.toot_linearlayout)
            holder!!.button_linearLayout = view.findViewById(R.id.button_layout)
            holder!!.avaterImageview_linearLayout = view.findViewById(R.id.avater_imageview_linearlayout)

            //Card
            holder!!.card_linearLayout = view.findViewById(R.id.linearlayout_card)
            holder!!.cardImageView = ImageView(context)
            holder!!.cardTextView = TextView(context)

            //添付メディア
            holder!!.media_imageview_1 = ImageView(context)
            holder!!.media_imageview_2 = ImageView(context)
            holder!!.media_imageview_3 = ImageView(context)
            holder!!.media_imageview_4 = ImageView(context)
            holder!!.imageButton = Button(context)
            holder!!.notification_icon = ImageView(context)

            holder!!.avater_imageview = view.findViewById(R.id.thumbnail)

            holder!!.user_textview = view.findViewById(R.id.user)
            holder!!.tile_textview = view.findViewById(R.id.tile_)
            holder!!.client_textview = view.findViewById(R.id.client)
            holder!!.bookmark_button = view.findViewById(R.id.bookmark)

            holder!!.nicoru_button = view.findViewById(R.id.nicoru)
            holder!!.boost_button = view.findViewById(R.id.boost)
            holder!!.web_button = view.findViewById(R.id.web)
            holder!!.misskey_Reaction = view.findViewById(R.id.misskey_reaction_textView)

            view.tag = holder

        } else {
            holder = view!!.tag as ViewHolder
        }


        val item = mItems[position]
        val listItem = item.listItem


        //mTv = view.findViewById(R.id.tile_);


        //設定を取得
        AccessToken = pref_setting.getString("main_token", "")
        Instance = pref_setting.getString("main_instance", "")


        val nicoru = holder!!.nicoru_button
        val boost = holder!!.boost_button

        //カスタムメニュー用設定
        if (listItem!![0].contains("CustomMenu")) {
            //ダイアログ出さない
            if (java.lang.Boolean.valueOf(listItem[25])) {
                dialog_not_show = true
            }
            //強制画像表示
            if (java.lang.Boolean.valueOf(listItem[26])) {
                image_show = true
            }
            //クイックプロフィール
            if (java.lang.Boolean.valueOf(listItem[27])) {
                quick_profile = true
            }
            //カスタム絵文字
            if (java.lang.Boolean.valueOf(listItem[28])) {
                emojis_show = true
            }
            //GIFアニメ trueで有効
            if (java.lang.Boolean.valueOf(listItem[29])) {
                gif_notPlay = true
            }
            //フォントパス
            font_path = listItem[30]
        }


        //Wi-Fi接続状況確認
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        //カスタム絵文字有効/無効
        if (pref_setting.getBoolean("pref_custom_emoji", true)) {
            if (pref_setting.getBoolean("pref_avater_wifi", true)) {
                //WIFIのみ表示有効時
                if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    //WIFI
                    emojis_show = true
                }
            } else {
                //WIFI/MOBILE DATA 関係なく表示
                emojis_show = true
            }
        }


        //ニコるをお気に入りに変更 設定次第
        //メッセージも変更できるようにする
        val friends_nico_check_box = pref_setting.getBoolean("pref_friends_nico_mode", false)
        if (!friends_nico_check_box) {

            nicoru!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black_24dp, 0, 0, 0)

            val locale = Locale.getDefault()
            if (locale == Locale.JAPAN) {
                //nicoru.setText("お気に入り");
                nicoru_text = "お気に入りに登録しました : "
            } else {
                //nicoru.setText("Favorite");
                nicoru_text = "add Favorite"
            }
        } else {
            val nicoru_image = ContextCompat.getDrawable(context, R.drawable.nicoru)
            nicoru_image!!.setBounds(0, 0, 64, 47)
            nicoru!!.setCompoundDrawables(nicoru_image, null, null, null)
            nicoru_text = "ニコった！ : "
        }

        //ニコる
        val finalNicoru_text = nicoru_text
        val id_string = item.listItem!![4]
        val media_url = listItem[8]

        // ふぁぼった、ぶーすとした
        val favClick = booleanArrayOf(false)
        val boostClick = booleanArrayOf(false)

        //ホームのみ　ぶーすとのとき用
        //BoostしたTootのとき　ホーム用
        var reblogToot = false
        var boostFavCount = false
        if (item.listItem!!.size >= 21) {
            reblogToot = true
        }
        if (item.listItem!!.size >= 17) {
            boostFavCount = true
        }

        //ブースト　それ以外
        //ブーストの要素がnullだったらそのまま
        var avater_url: String? = null
        if (reblogToot && listItem[20] != null) {
            avater_url = listItem[22]
        } else {
            //要素があったとき
            avater_url = listItem[5]
        }


        //カード　配列管理
        val card_title = listItem[12]
        val card_url = listItem[13]
        val card_description = listItem[14]
        val card_image = listItem[15]

        //ArrayList<String> arrayList = item.getStringList();
        if (card_title != null) {

            //System.out.println("カード" + card_title);

            val linearLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val imageLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val textLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            imageLayoutParams.weight = 4f
            textLayoutParams.weight = 1f

            //カード実装
            if (holder!!.cardImageView!!.parent != null) {
                (holder!!.cardImageView!!.parent as ViewGroup).removeView(holder!!.cardImageView)
            }
            //カード実装
            if (holder!!.cardTextView!!.parent != null) {
                (holder!!.cardTextView!!.parent as ViewGroup).removeView(holder!!.cardTextView)
            }

            ImageViewClickCustomTab_LinearLayout(holder!!.card_linearLayout, card_url)

            //タイムラインに画像を表示
            //動的に画像を追加するよ
            //LinearLayout linearLayout = (LinearLayout) holder.linearLayout;
            //Wi-Fi接続時は有効？
            val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
            val toot_media = pref_setting.getBoolean("pref_toot_media", false)

            //タイムラインに画像を表示
            if (card_url != null) {
                //System.out.println("にゃーん :" + media_url_2);
                //Wi-Fi接続時
                if (setting_avater_wifi) {
                    if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        holder!!.card_linearLayout!!.addView(holder!!.cardImageView)
                        Glide.with(context).load(card_image).into(holder!!.cardImageView!!)
                    }
                } else if (!toot_media || image_show) {
                    holder!!.card_linearLayout!!.addView(holder!!.cardImageView)
                    Glide.with(context).load(card_image).into(holder!!.cardImageView!!)
                }
            }

            holder!!.card_linearLayout!!.layoutParams = linearLayoutParams
            holder!!.card_linearLayout!!.addView(holder!!.cardTextView)
            holder!!.cardTextView!!.layoutParams = textLayoutParams
            holder!!.cardTextView!!.text = card_title + "\n" + card_description
            holder!!.cardTextView!!.textSize = Integer.valueOf(pref_setting.getString("pref_fontsize_card", "10")!!).toFloat()
            holder!!.cardImageView!!.layoutParams = imageLayoutParams
            //Cardくそ見にくいから枠つけるか
            holder!!.card_linearLayout!!.background = context.getDrawable(R.drawable.button_style)
        }


        //背景色を変える機能
        //ブックマーク削除など

        //SVG許可
        val svgAnimation = pref_setting.getBoolean("pref_svg_animation", false)

        val type = listItem[0]

        if (type != null) {
            if (type.contains("custom_notification")) {
                holder!!.vw1!!.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_notification_color", "#1A008000")))
            }
            if (type.contains("custom_home")) {
                holder!!.vw1!!.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_home_color", "#1Aff0000")))
            }
            if (type.contains("custom_local")) {
                holder!!.vw1!!.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_local_color", "#1A0000ff")))
            }
            if (type.contains("bookmark")) {
                bookmark_delete = true
            }
            //ブースト
            if (type.contains("Notification_reblog")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder)
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true
                //アニメーションアイコン
                holder!!.notification_icon!!.setImageResource(R.drawable.ic_repeat_black_24dp)
            }
            //お気に入り
            if (type.contains("Notification_favourite")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder)
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true
                //アニメーションアイコン
                //friends.nicoモードかな？
                if (!friends_nico_check_box) {
                    holder!!.notification_icon!!.setImageResource(R.drawable.ic_star_black_24dp)
                } else {
                    holder!!.notification_icon!!.setImageResource(R.drawable.nicoru)
                }
            }
            //ふぉろー
            if (type.contains("Notification_follow")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder)
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true
                //アニメーションアイコン
                holder!!.notification_icon!!.setImageResource(R.drawable.ic_person_add_black_24dp)
            }
            //めんしょん
            if (type.contains("Notification_mention")) {
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true
                //アニメーションアイコン
                holder!!.notification_icon!!.setImageResource(R.drawable.ic_announcement_black_24dp)
            }

            if (holder!!.notification_icon!!.parent != null) {
                (holder!!.notification_icon!!.parent as ViewGroup).removeView(holder!!.notification_icon)
            }
            holder!!.avaterImageview_linearLayout!!.addView(holder!!.notification_icon, 0)
        }


        //メッセージ
        //設定で分けるように
        var favorite_message: String? = null
        var favorite_title: String? = null

        val nicoru_dialog_chack = pref_setting.getBoolean("pref_friends_nico_mode", false)
        if (!nicoru_dialog_chack) {
            favorite_message = context.getString(R.string.favoutire)
            favorite_title = context.getString(R.string.favourite_add_message)
        } else {
            favorite_message = "ニコる"
            favorite_title = "ニコりますか"
        }


        //ニコるダイアログ
        val finalFavorite_message = favorite_message
        val finalFavorite_title = favorite_title
        val finalInstance1 = Instance
        val finalAccessToken = AccessToken
        val finalAccessToken1 = AccessToken
        val finalConvertView1 = view
        val finalView1 = view
        val finalBoostFavCount = boostFavCount
        nicoru.setOnClickListener { v ->
            //Misskeyと分ける
            if (CustomMenuTimeLine.isMisskeyMode) {
                //リアクション登録、外す
                if (nicoru.text.toString().length == 0) {
                    showMisskeyReaction(id_string, nicoru, item)
                } else {
                    //外す
                    Snackbar.make(v, context.getString(R.string.reaction_delete_message), Snackbar.LENGTH_SHORT).setAction(context.getText(R.string.reaction_delete)) {
                        postMisskeyReaction("delete", "", id_string)
                        nicoru.text = ""
                    }.show()
                }
            } else {
                //もってくる
                var apiURL = "favourite"
                //Snackber Text
                var snackberTitle = finalFavorite_title
                var snackberButton = finalFavorite_message
                //配列の範囲内にするため
                if (finalBoostFavCount) {
                    val isFav = item.listItem!![17]
                    //すでにFav済みの場合は外すAPIを叩く
                    if (isFav.contains("favourited") || favClick[0]) {
                        apiURL = "unfavourite"
                        snackberTitle = context.getString(R.string.delete_fav)
                        snackberButton = context.getString(R.string.delete_text)
                    }
                }
                val finalApiURL = apiURL

                val favorite = pref_setting.getBoolean("pref_nicoru_dialog", true)
                val replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", true)
                //ダイアログ表示する？
                if (favorite && !dialog_not_show) {
                    if (replace_snackber) {

                        val favourite_snackbar: Snackbar
                        favourite_snackbar = Snackbar.make(finalView1, snackberTitle.toString(), Snackbar.LENGTH_SHORT)
                        favourite_snackbar.setAction(snackberButton) {
                            TootAction(id_string, finalApiURL, nicoru)
                            favClick[0] = true
                            if (finalBoostFavCount) {
                                item.listItem!![17] = "favourited"
                            }
                        }
                        favourite_snackbar.show()
                    } else {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(snackberTitle)
                        alertDialog.setPositiveButton(snackberButton) { dialog, which ->
                            TootAction(id_string, finalApiURL, nicoru)
                            favClick[0] = true
                            if (finalBoostFavCount) {
                                item.listItem!![17] = "favourited"
                            }
                        }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    }
                    //テキストボックが未選択
                } else {
                    TootAction(id_string, finalApiURL, nicoru)
                    favClick[0] = true
                    if (finalBoostFavCount) {
                        item.listItem!![17] = "favourited"
                    }
                }
            }
        }


        //ブースト
        boost!!.setOnClickListener {
            //すでにブースト済みの場合は外すAPIにする
            var apiURL = "reblog"
            //Snackber
            var snackberTitle = context.getString(R.string.dialog_boost_info)
            var snackberButton = context.getString(R.string.dialog_boost)

            //MisskeyはRenoteなのでメッセージを変える
            if (CustomMenuTimeLine.isMisskeyMode) {
                //Renoteしますかメッセージ
                if (finalBoostFavCount) {
                    snackberTitle = context.getString(R.string.renote_message)
                    snackberButton = context.getString(R.string.renote)
                } else {
                    snackberTitle = context.getString(R.string.renote_delete_message)
                    snackberButton = context.getString(R.string.delete_renote)
                }
            } else {
                //Boost外しますかメッセージ
                if (finalBoostFavCount) {
                    val isBoost = item.listItem!![16]
                    if (isBoost.contains("reblogged") || boostClick[0]) {
                        apiURL = "unreblog"
                        snackberTitle = context.getString(R.string.delete_bt)
                        snackberButton = context.getString(R.string.delete_text)
                    }
                }
            }

            val finalApiURL = apiURL
            //設定でダイアログをだすかどうか
            val boost_dialog = pref_setting.getBoolean("pref_boost_dialog", true)
            val replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", true)
            //ダイアログ表示する？
            if (boost_dialog && !dialog_not_show) {
                if (replace_snackber) {
                    val snackbar: Snackbar
                    snackbar = Snackbar.make(finalView1, snackberTitle, Snackbar.LENGTH_SHORT)
                    snackbar.setAction(snackberButton) {
                        //Misskey
                        if (CustomMenuTimeLine.isMisskeyMode) {
                            postMisskeyRenote("/api/notes/create", id_string, "home")
                        } else {
                            TootAction(id_string, finalApiURL, boost)
                        }
                        boostClick[0] = true
                        if (finalBoostFavCount) {
                            item.listItem!![16] = "reblogged"
                        }
                    }
                    snackbar.show()
                } else {
                    //ダイアログ
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle(R.string.confirmation)
                    alertDialog.setMessage(snackberTitle)
                    alertDialog.setPositiveButton(snackberButton) { dialog, which ->
                        TootAction(id_string, finalApiURL, boost)
                        boostClick[0] = true
                        if (finalBoostFavCount) {
                            item.listItem!![16] = "reblogged"
                        }
                    }
                    alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                    alertDialog.create().show()
                }

                //チェックボックスが未チェックだったとき
            } else {
                TootAction(id_string, finalApiURL, boost)
                boostClick[0] = true
                if (finalBoostFavCount) {
                    item.listItem!![16] = "reblogged"
                }
            }
        }

        //Fav+BT機能
        //Misskeyでは使わない
        if (!CustomMenuTimeLine.isMisskeyMode) {
            nicoru.setOnLongClickListener {
                //面倒なので事前に調べたりはしない
                //設定でダイアログをだすかどうか
                val fav_bt = pref_setting.getBoolean("pref_fav_and_bt_dialog", true)
                val replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", true)
                if (fav_bt && !dialog_not_show) {
                    if (replace_snackber) {
                        val snackbar: Snackbar
                        snackbar = Snackbar.make(finalView1, R.string.favAndBT, Snackbar.LENGTH_SHORT)
                        snackbar.setAction("Fav+BT") {
                            TootAction(id_string, "favourite", boost)
                            TootAction(id_string, "reblog", boost)
                            boostClick[0] = true
                            if (finalBoostFavCount) {
                                item.listItem!![16] = "reblogged"
                                item.listItem!![17] = "favourited"
                            }
                        }
                        snackbar.show()
                    } else {
                        //ダイアログ
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(R.string.favAndBT)
                        alertDialog.setPositiveButton("Fav+BT") { dialog, which ->
                            TootAction(id_string, "favourite", boost)
                            TootAction(id_string, "reblog", boost)
                            boostClick[0] = true
                            if (finalBoostFavCount) {
                                item.listItem!![16] = "reblogged"
                                item.listItem!![17] = "favourited"
                            }
                        }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    }
                    //チェックボックスが未チェックだったとき
                } else {
                    TootAction(id_string, "favourite", boost)
                    TootAction(id_string, "reblog", boost)
                    boostClick[0] = true
                    if (finalBoostFavCount) {
                        item.listItem!![16] = "reblogged"
                        item.listItem!![17] = "favourited"
                    }
                }
                //OnClickListenerが呼ばれないようにする
                true
            }
        }

        //ブーストボタンにアイコンつける
        val boost_button = holder!!.boost_button
        boost_button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_black_24dp, 0, 0, 0)


        //ブラウザ、他クライアントで開くボタン設置
        val web_button = holder!!.web_button
        web_button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_more_vert_black_24dp, 0, 0, 0)


        val chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true)
        val back_icon = BitmapFactory.decodeResource(context.applicationContext.resources, R.drawable.ic_action_arrow_back)

        //ポップアップメニューを展開する
        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.timeline_popup_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, web_button)
        optionsMenu.setForceShowIcon(true)


        val finalInstance = Instance
        val user_id = listItem[7]
        web_button.setOnClickListener { v ->
            if (!CustomMenuTimeLine.isMisskeyMode) {
                val account = java.lang.Long.valueOf(listItem[6])
                // Display the menu
                optionsMenu.show()

                //押したときの反応
                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                        //アカウント
                        if (item.toString().contains(context.getString(R.string.account))) {
                            //読み込み
                            //Quick Profile
                            if (pref_setting.getBoolean("pref_quick_profile", false) || quick_profile) {
                                //クイックプロフィーる
                                if (CustomMenuTimeLine.isMisskeyMode) {
                                    showMisskeyQuickProfile(v, listItem[6])
                                } else {
                                    quickProfileSnackber(v, listItem[6])
                                }
                            } else {
                                val intent = Intent(context, UserActivity::class.java)
                                if (CustomMenuTimeLine.isMisskeyMode) {
                                    intent.putExtra("Misskey", true)
                                }
                                intent.putExtra("Account_ID", account)
                                //IDを渡す
                                context.startActivity(intent)

                                /*
                                    //画面分割用
                                    boolean multipain_ui_mode = pref_setting.getBoolean("app_multipain_ui", false);
                                    if (multipain_ui_mode) {
                                        FragmentTransaction ft = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
                                        Fragment fragment = new User_Fragment();
                                        long account_id = account;
                                        Bundle bundle = new Bundle();
                                        bundle.putLong("Account_ID", account_id);
                                        fragment.setArguments(bundle);
                                        ft.replace(R.id.fragment3, fragment).commit();
                                    } else {
                                        long account_id = account;
                                        Intent intent = new Intent(getContext(), UserActivity.class);
                                        //IDを渡す
                                        intent.putExtra("Account_ID", account_id);
                                        getContext().startActivity(intent);
                                    }
*/
                            }
                        }
                        //ブラウザ
                        if (item.toString().contains(context.getString(R.string.browser))) {
                            //有効
                            if (chrome_custom_tabs) {
                                val custom = CustomTabsHelper.getPackageNameToUse(context)
                                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                                val customTabsIntent = builder.build()
                                customTabsIntent.intent.setPackage(custom)
                                customTabsIntent.launchUrl(context as Activity, Uri.parse("https://$finalInstance/@$user_id/$id_string"))
                                //無効
                            } else {
                                val uri = Uri.parse("https://$finalInstance/@$user_id/$id_string")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            }
                        }
                        //コピー
                        if (item.toString().contains(context.getString(R.string.copy))) {
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("", holder!!.tile_textview!!.text.toString()))

                            Toast.makeText(context, context.getString(R.string.copy) + " : " + holder!!.tile_textview!!.text.toString(), Toast.LENGTH_SHORT).show()
                        }

                        return false
                    }

                    override fun onMenuModeChange(menu: MenuBuilder) {}
                })
            } else {
            }
        }

        //タイムラインに画像を表示
        //動的に画像を追加するよ
        //LinearLayout linearLayout = (LinearLayout) holder.linearLayout;
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)

        val toot_media = pref_setting.getBoolean("pref_toot_media", false)


        media_url_1 = listItem[8]
        media_url_2 = listItem[9]
        media_url_3 = listItem[10]
        media_url_4 = listItem[11]


        if (media_url_1 != null) {
            //System.out.println("にゃーん :" + media_url_2);
            //非表示
            if (toot_media || image_show) {
                holder!!.imageButton!!.setOnClickListener {
                    if (setting_avater_gif || !gif_notPlay) {
                        //GIFアニメ再生させない / カスタムメニューで無効化
                        ImageViewSetting(holder!!)
                        //表示
                        addMediaPicasso(media_url_1, holder!!.media_imageview_1, holder!!.linearLayoutMedia)
                        addMediaPicasso(media_url_2, holder!!.media_imageview_2, holder!!.linearLayoutMedia)
                        addMediaPicasso(media_url_3, holder!!.media_imageview_3, holder!!.linearLayoutMedia2)
                        addMediaPicasso(media_url_4, holder!!.media_imageview_4, holder!!.linearLayoutMedia2)

                    } else {
                        ImageViewSetting(holder!!)
                        //表示
                        //Glide.with(getContext()).load(media_url).into(holder.media_imageview_1);
                        addMediaGlide(media_url_1, holder!!.media_imageview_1, holder!!.linearLayoutMedia)
                        addMediaGlide(media_url_2, holder!!.media_imageview_2, holder!!.linearLayoutMedia)
                        addMediaGlide(media_url_3, holder!!.media_imageview_3, holder!!.linearLayoutMedia2)
                        addMediaGlide(media_url_4, holder!!.media_imageview_4, holder!!.linearLayoutMedia2)
                    }
                }
            }

            //Wi-Fi接続時　か　強制画像表示
            if (setting_avater_wifi || image_show) {
                if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                    if (setting_avater_gif || !gif_notPlay) {
                        //GIFアニメ再生させない / カスタムメニューで無効化
                        ImageViewSetting(holder!!)
                        //表示
                        addMediaPicasso(media_url_1, holder!!.media_imageview_1, holder!!.linearLayoutMedia)
                        addMediaPicasso(media_url_2, holder!!.media_imageview_2, holder!!.linearLayoutMedia)
                        addMediaPicasso(media_url_3, holder!!.media_imageview_3, holder!!.linearLayoutMedia2)
                        addMediaPicasso(media_url_4, holder!!.media_imageview_4, holder!!.linearLayoutMedia2)
                    } else {
                        ImageViewSetting(holder!!)
                        //画像を取ってくる
                        //表示
                        addMediaGlide(media_url_1, holder!!.media_imageview_1, holder!!.linearLayoutMedia)
                        addMediaGlide(media_url_2, holder!!.media_imageview_2, holder!!.linearLayoutMedia)
                        addMediaGlide(media_url_3, holder!!.media_imageview_3, holder!!.linearLayoutMedia2)
                        addMediaGlide(media_url_4, holder!!.media_imageview_4, holder!!.linearLayoutMedia2)
                    }
                }

                //Wi-Fi未接続
            } else {
                holder!!.imageButton!!.setText(R.string.show_image)
                holder!!.imageButton!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_image_black_24dp, 0, 0, 0)
                val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.weight = 1f
                holder!!.imageButton!!.layoutParams = layoutParams
                if (holder!!.imageButton!!.parent != null) {
                    (holder!!.imageButton!!.parent as ViewGroup).removeView(holder!!.imageButton)
                }
                holder!!.linearLayoutMediaButton!!.addView(holder!!.imageButton)

                //クリックしてイメージ表示
                val finalMedia_url1 = media_url
                holder!!.imageButton!!.setOnClickListener {
                    if (setting_avater_gif || !gif_notPlay) {
                        //GIFアニメ再生させない / カスタムメニューで無効化
                        ImageViewSetting(holder!!)
                        //表示
                        addMediaPicasso(media_url_1, holder!!.media_imageview_1, holder!!.linearLayoutMedia)
                        addMediaPicasso(media_url_2, holder!!.media_imageview_2, holder!!.linearLayoutMedia)
                        addMediaPicasso(media_url_3, holder!!.media_imageview_3, holder!!.linearLayoutMedia2)
                        addMediaPicasso(media_url_4, holder!!.media_imageview_4, holder!!.linearLayoutMedia2)

                    } else {
                        ImageViewSetting(holder!!)
                        //画像を取ってくる
                        //表示
                        addMediaGlide(media_url_1, holder!!.media_imageview_1, holder!!.linearLayoutMedia)
                        addMediaGlide(media_url_2, holder!!.media_imageview_2, holder!!.linearLayoutMedia)
                        addMediaGlide(media_url_3, holder!!.media_imageview_3, holder!!.linearLayoutMedia2)
                        addMediaGlide(media_url_4, holder!!.media_imageview_4, holder!!.linearLayoutMedia2)
                    }
                }
            }
        }

        //サムネイル画像を設定
        val thumbnail = holder!!.avater_imageview
        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_avater", false)

        if (setting_avater_hidden) {
            //thumbnail.setImageBitmap(item.getThumbnail());
        }
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                thumbnail.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }).start();
*/
        //Wi-Fi か　強制画像表示
        if (setting_avater_wifi || image_show) {
            if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                if (setting_avater_gif || !gif_notPlay) {
                    //GIFアニメ再生させない / カスタムメニューで無効化
                    Picasso.get().load(avater_url).into(thumbnail)
                } else {
                    //GIFアニメを再生
                    Glide.with(thumbnail!!).load(avater_url).into(thumbnail)
                }
            } else {
                //レイアウトを消す
                if (!notification_layout) {
                    holder!!.vw1!!.removeView(holder!!.avaterImageview_linearLayout)
                }
            }//Wi-Fi no Connection
        } else {
            //レイアウトを消す
            if (!notification_layout) {
                holder!!.vw1!!.removeView(holder!!.avaterImageview_linearLayout)
            }
        }

        //ブーストの要素がnullだったらそのまま
        var account_id: Long = 0
        if (!CustomMenuTimeLine.isMisskeyMode) {
            if (reblogToot && listItem[20] != null) {
                account_id = java.lang.Long.valueOf(listItem[23])
            } else {
                account_id = java.lang.Long.valueOf(listItem[6])
            }
        }


        /*
        //ユーザー情報
        FragmentTransaction ft = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
        Fragment fragment = new User_Fragment();
        View finalConvertView = convertView;
        long finalAccount_id = account_id;
        thumbnail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //読み込み
                boolean multipain_ui_mode = pref_setting.getBoolean("app_multipain_ui", false);

                if (pref_setting.getBoolean("pref_quick_profile", false) || quick_profile) {
                    //クイックプロフィーる
                    if (CustomMenuTimeLine.isMisskeyMode()) {
                        showMisskeyQuickProfile(v, listItem.get(6));
                    } else {
                        quickProfileSnackber(v, listItem.get(6));
                    }
                } else {
                    Intent intent = new Intent(getContext(), UserActivity.class);
                    if (CustomMenuTimeLine.isMisskeyMode()) {
                        intent.putExtra("Misskey", true);
                    }
                    intent.putExtra("Account_ID", finalAccount_id);
                    //IDを渡す
                    getContext().startActivity(intent);

*/
        /*
                    if (multipain_ui_mode) {
                        Bundle bundle = new Bundle();
                        bundle.putLong("Account_ID", finalAccount_id);
                        fragment.setArguments(bundle);
                        ft.replace(R.id.fragment3, fragment).commit();
                    } else {
                    }
*//*

                }
            }
        });
*/


        //ブックマーク関係
        val replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", true)
        holder!!.bookmark_button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark_border_black_24dp, 0, 0, 0)
        holder!!.bookmark_button!!.setOnClickListener {
            if (bookmark_delete) {
                //消去
                if (replace_snackber) {
                    val snackbar: Snackbar
                    snackbar = Snackbar.make(finalView1, R.string.bookmark_delete_title, Snackbar.LENGTH_SHORT)
                    snackbar.setAction(R.string.ok) {
                        //読み込み
                        if (sqLite == null) {
                            sqLite = TootBookmark_SQLite(context)
                        }

                        if (sqLiteDatabase == null) {
                            sqLiteDatabase = sqLite!!.readableDatabase
                        }
                        val info = holder!!.client_textview!!.text.toString()
                        sqLiteDatabase!!.delete("tootbookmarkdb", "info=?", arrayOf(info))
                        Toast.makeText(context, R.string.delete, Toast.LENGTH_SHORT).show()
                    }
                    snackbar.show()
                } else {
                    //ダイアログ
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle(R.string.bookmark_delete_title)
                    alertDialog.setMessage(R.string.bookmark_delete_message)
                    alertDialog.setPositiveButton(R.string.ok) { dialog, which ->
                        //読み込み
                        if (sqLite == null) {
                            sqLite = TootBookmark_SQLite(context)
                        }

                        if (sqLiteDatabase == null) {
                            sqLiteDatabase = sqLite!!.readableDatabase
                        }
                        val info = holder!!.client_textview!!.text.toString()
                        sqLiteDatabase!!.delete("tootbookmarkdb", "info=?", arrayOf(info))
                        Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show()
                    }
                    alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                    alertDialog.create().show()
                }

                //書き込み
            } else {
                val favorite_dialog = pref_setting.getBoolean("pref_bookmark_dialog", true)
                val replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", false)
                if (favorite_dialog) {
                    if (replace_snackber) {
                        val snackbar: Snackbar
                        snackbar = Snackbar.make(finalView1, R.string.dialog_bookmark_info, Snackbar.LENGTH_SHORT)
                        snackbar.setAction(R.string.bookmark) {
                            SQLitePut(item)
                            Toast.makeText(context, R.string.add_Bookmark, Toast.LENGTH_SHORT).show()
                        }
                        snackbar.show()

                    } else {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(R.string.dialog_bookmark_info)
                        alertDialog.setPositiveButton(R.string.bookmark) { dialog, which ->
                            SQLitePut(item)
                            Toast.makeText(context, R.string.add_Bookmark, Toast.LENGTH_SHORT).show()
                        }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    }
                } else {
                    SQLitePut(item)
                    Toast.makeText(context, R.string.add_Bookmark, Toast.LENGTH_SHORT).show()
                }
            }
        }


        //friends.nicoようにアンケートも実装するぞ！
        //アンケートっぽいトゥートを見つける

        if (listItem[1] != null && listItem[1].contains("friends.nico アンケート")) {
            //System.out.println("アンケート発見 : " + String.valueOf(item.getID()));

            //!で条件を反転させる
            if (!listItem[1].contains("friends.nico アンケート(結果)")) {

                val button_params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                button_params.weight = 1f

                //imageLinearLayoutにボタンを入れる
                val enquete_1 = Button(context)
                enquete_1.text = "1"
                enquete_1.layoutParams = button_params
                val enquete_2 = Button(context)
                enquete_2.text = "2"
                enquete_2.layoutParams = button_params
                val enquete_3 = Button(context)
                enquete_3.text = "3"
                enquete_3.layoutParams = button_params
                val enquete_4 = Button(context)
                enquete_4.text = "4"
                enquete_4.layoutParams = button_params
                val enquete_5 = Button(context)
                enquete_5.text = "🤔"
                enquete_5.layoutParams = button_params
                holder!!.linearLayoutEnquate!!.addView(enquete_1)
                holder!!.linearLayoutEnquate!!.addView(enquete_2)
                holder!!.linearLayoutEnquate!!.addView(enquete_3)
                holder!!.linearLayoutEnquate!!.addView(enquete_4)
                holder!!.linearLayoutEnquate!!.addView(enquete_5)

                //何個目か?
                val enquete_select = IntArray(1)

                enquete_1.setOnClickListener {
                    enquete_select[0] = 1

                    val enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false)
                    if (enquate_dialog) {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(R.string.enquate_dialog)
                        alertDialog.setPositiveButton(R.string.vote) { dialog, which -> FriendsNicoEnquate(id_string, "0", "１番目に投票しました : ") }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    } else {
                        FriendsNicoEnquate(id_string, "0", "１番目に投票しました : ")
                    }
                }

                enquete_2.setOnClickListener {
                    enquete_select[0] = 2
                    val enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false)
                    if (enquate_dialog) {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(R.string.enquate_dialog)
                        alertDialog.setPositiveButton(R.string.vote) { dialog, which -> FriendsNicoEnquate(id_string, "1", "２番目に投票しました : ") }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    } else {
                        FriendsNicoEnquate(id_string, "1", "２番目に投票しました : ")
                    }
                }

                enquete_3.setOnClickListener {
                    enquete_select[0] = 3
                    val enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false)
                    if (enquate_dialog) {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(R.string.enquate_dialog)
                        alertDialog.setPositiveButton(R.string.vote) { dialog, which -> FriendsNicoEnquate(id_string, "2", "３番目に投票しました : ") }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    } else {
                        FriendsNicoEnquate(id_string, "2", "３番目に投票しました : ")
                    }
                }

                enquete_4.setOnClickListener {
                    enquete_select[0] = 4
                    val enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false)
                    if (enquate_dialog) {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(R.string.enquate_dialog)
                        alertDialog.setPositiveButton(R.string.vote) { dialog, which -> FriendsNicoEnquate(id_string, "3", "４番目に投票しました : ") }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    } else {
                        FriendsNicoEnquate(id_string, "3", "４番目に投票しました : ")
                    }
                }

                enquete_5.setOnClickListener {
                    enquete_select[0] = 5
                    val enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false)
                    if (enquate_dialog) {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle(R.string.confirmation)
                        alertDialog.setMessage(R.string.enquate_dialog)
                        alertDialog.setPositiveButton(R.string.vote) { dialog, which -> FriendsNicoEnquate(id_string, "4", "５番目に投票しました : ") }
                        alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                        alertDialog.create().show()
                    } else {
                        FriendsNicoEnquate(id_string, "4", "５番目に投票しました : ")
                    }
                }
            }
        }

        /*
        //ImageGetter
        //カスタム絵文字
        Html.ImageGetter toot_imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                final LevelListDrawable[] d = {new LevelListDrawable()};
                //final Drawable[] d = {null};
                Drawable empty = getContext().getResources().getDrawable(R.drawable.ic_refresh_black_24dp);
                d[0].addLevel(0, 0, empty);
                d[0].setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
                new LoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, source, d[0]);
                return d[0];
            }
        };*/


        // トゥート
        val title = holder!!.tile_textview
        //title.setText(Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT));
        title!!.textSize = 10f
        //フォントサイズの変更
        val toot_textsize = pref_setting.getString("pref_fontsize_timeline", "10")
        title.textSize = Integer.parseInt(toot_textsize.toString()).toFloat()

        // ユーザー名
        val user = holder!!.user_textview
        //user.setText(item.getUser());
        user!!.textSize = 10f
        //フォントサイズの変更
        val username_textsize = pref_setting.getString("pref_fontsize_user", "10")
        user.textSize = Integer.parseInt(username_textsize.toString()).toFloat()

        //クライアント
        val client = holder!!.client_textview
        //client.setText(item.getClient());
        client!!.textSize = 10f
        //フォントサイズ変更
        val client_textsize = pref_setting.getString("pref_fontsize_client", "10")
        client.textSize = Integer.parseInt(client_textsize.toString()).toFloat()

        //各アイコンはトゥートサイズに合わせる
        val button_textsize = pref_setting.getString("pref_fontsize_button", "10")
        nicoru.textSize = Integer.parseInt(button_textsize.toString()).toFloat()
        boost.textSize = Integer.parseInt(button_textsize.toString()).toFloat()
        holder!!.bookmark_button!!.textSize = Integer.parseInt(button_textsize.toString()).toFloat()
        web_button.textSize = Integer.parseInt(button_textsize.toString()).toFloat()

        //フォント指定
        title.setTypeface(CustomMenuTimeLine.font_Typeface)
        user.setTypeface(CustomMenuTimeLine.font_Typeface)
        client.setTypeface(CustomMenuTimeLine.font_Typeface)
        holder!!.cardTextView!!.setTypeface(CustomMenuTimeLine.font_Typeface)
        boost_button.setTypeface(CustomMenuTimeLine.font_Typeface)
        web_button.setTypeface(CustomMenuTimeLine.font_Typeface)
        holder!!.bookmark_button!!.setTypeface(CustomMenuTimeLine.font_Typeface)
        holder!!.nicoru_button!!.setTypeface(CustomMenuTimeLine.font_Typeface)
        //フォントの色設定
        val font_setting_swich = pref_setting.getBoolean("pref_fontcolor_setting", false)
        if (font_setting_swich) {
            //ゆーざー
            val user_font_color = pref_setting.getString("pref_fontcolor", "#000000")
            user.setTextColor(Color.parseColor(user_font_color))

            //たいむらいん
            val toot_font_color = pref_setting.getString("pref_fontcolor_toot", "#000000")
            title.setTextColor(Color.parseColor(toot_font_color))
            //くらいあんと
            val client_font_color = pref_setting.getString("pref_fontcolor_client", "#000000")
            client.setTextColor(Color.parseColor(client_font_color))

        }

        //絵文字強制
        val emoji_compatibility = pref_setting.getBoolean("pref_emoji_compatibility", false)
        //ブースト　それ以外
        var titleString: String? = null
        var userString: String? = null
        //ブーストの要素がnullだったらそのまま
        if (reblogToot && listItem[20] != null) {
            titleString = listItem[20]
            userString = listItem[21] + "<br>" + listItem[2] + " " + context.getString(R.string.reblog)
            //アイコンつける
            val drawable = context.getDrawable(R.drawable.ic_repeat_black_24dp_2)
            drawable!!.setTint(Color.parseColor("#008000"))
            user.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            //色つける
            user.setTextColor(Color.parseColor("#008000"))
        } else {
            titleString = listItem[1]
            userString = listItem[2]
        }

        /**
         * 内容を表示する部分
         * カスタム絵文字もほぼ動くように←これ重要
         * ちなみに最新の絵文字サポート機能は削りましたいる？
         *
         *
         * https://medium.com/@rajeefmk/android-textview-and-image-loading-from-url-part-1-a7457846abb6
         *
         */

        val title_imageGetter = PicassoImageGetter(title)
        val user_imageGetter = PicassoImageGetter(user)
        var toot_html: Spannable
        var user_html: Spannable

        if (title != null) {

            if (pref_setting.getBoolean("pref_custom_emoji", true)) {
                //カスタム絵文字有効時
                if (setting_avater_wifi) {
                    //WIFIのみ表示有効時
                    if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        //WIFI接続中か確認
                        //接続中
                        try {
                            user_html = Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null) as Spannable
                            toot_html = Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null) as Spannable
                            title.text = toot_html
                            user.text = user_html
                        } catch (e: NullPointerException) {
                            user_html = Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null) as Spannable
                            toot_html = Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null) as Spannable
                            title.text = toot_html
                            user.text = user_html
                        }

                    } else {
                        //確認したけどWIFI接続確認できなかった
                        title.text = Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT)
                        user.text = Html.fromHtml(userString, Html.FROM_HTML_MODE_COMPACT)
                    }
                } else {
                    //WIFIのみ表示無効時
                    //そのまま表示させる
                    try {
                        user_html = Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null) as Spannable
                        toot_html = Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null) as Spannable
                        title.text = toot_html
                        user.text = user_html
                    } catch (e: NullPointerException) {
                        user_html = Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null) as Spannable
                        toot_html = Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null) as Spannable
                        title.text = toot_html
                        user.text = user_html
                    }

                }
            } else {
                title.text = Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT)
                user.text = Html.fromHtml(userString, Html.FROM_HTML_MODE_COMPACT)
            }
        }

        //強制的に表示
        if (emojis_show) {
            try {
                user_html = Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null) as Spannable
                toot_html = Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null) as Spannable
                title.text = toot_html
                user.text = user_html
            } catch (e: NullPointerException) {
                user_html = Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null) as Spannable
                toot_html = Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null) as Spannable
                title.text = toot_html
                user.text = user_html
            }

        }


        //title.setText((Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT)));
        client.text = listItem[3]


        //URLをCustomTabで開くかどうか
        if (chrome_custom_tabs) {
            holder!!.tile_textview!!.transformationMethod = LinkTransformationMethod()
            holder!!.tile_textview!!.movementMethod = LinkMovementMethod.getInstance()
        } else {
            holder!!.tile_textview!!.autoLinkMask = Linkify.WEB_URLS
        }


        //アイコンオンリー
        val button_icon = pref_setting.getBoolean("pref_button_icon", false)
        if (button_icon) {
            boost_button.text = ""
            nicoru.text = ""
            web_button.text = ""
            holder!!.bookmark_button!!.text = ""
        }

        //Fダークモード、OLEDモード時にアイコンが見えない問題
        //どちらかが有効の場合
        //↑これ廃止ね。代わりに利用中のテーマを取得して変更する仕様にするからよろー
        //Theme比較わからんから変わりにToolberの背景が黒だったら動くように
        //なんか落ちる（要検証）
        try {
            if (((context as Home).toolBer.background as ColorDrawable).color == Color.parseColor("#000000")) {
                boost_button.setTextColor(Color.parseColor("#ffffff"))
                nicoru.setTextColor(Color.parseColor("#ffffff"))
                web_button.setTextColor(Color.parseColor("#ffffff"))
                holder!!.bookmark_button!!.setTextColor(Color.parseColor("#ffffff"))

                //アイコンを取得
                val boost_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_repeat_black_24dp, null)
                val web_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_more_vert_black_24dp, null)
                val bookmark_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_bookmark_border_black_24dp, null)
                val favourite_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_star_black_24dp, null)
                //Misskey
                val reaction_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_add_black_24dp, null)

                //染色
                boost_icon_white!!.setTint(Color.parseColor("#ffffff"))
                web_icon_white!!.setTint(Color.parseColor("#ffffff"))
                bookmark_icon_white!!.setTint(Color.parseColor("#ffffff"))
                favourite_icon_white!!.setTint(Color.parseColor("#ffffff"))
                reaction_icon_white!!.setTint(Color.parseColor("#ffffff"))

                //入れる
                boost_button.setCompoundDrawablesWithIntrinsicBounds(boost_icon_white, null, null, null)
                web_button.setCompoundDrawablesWithIntrinsicBounds(web_icon_white, null, null, null)
                holder!!.bookmark_button!!.setCompoundDrawablesWithIntrinsicBounds(bookmark_icon_white, null, null, null)
                holder!!.nicoru_button!!.setCompoundDrawablesWithIntrinsicBounds(favourite_icon_white, null, null, null)
                if (CustomMenuTimeLine.isMisskeyMode) {
                    holder!!.nicoru_button!!.setCompoundDrawablesWithIntrinsicBounds(reaction_icon_white, null, null, null)
                }

                //ニコるをお気に入りに変更 設定次第
                //メッセージも変更できるようにする
                if (friends_nico_check_box) {
                    //Misskey
                    holder!!.nicoru_button!!.setCompoundDrawablesWithIntrinsicBounds(favourite_icon_white, null, null, null)
                }
            } else {
                //アイコンを取得
                val boost_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_repeat_black_24dp, null)
                val web_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_more_vert_black_24dp, null)
                val bookmark_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_bookmark_border_black_24dp, null)
                val favourite_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_star_black_24dp, null)
                //Misskey
                val reaction_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_add_black_24dp, null)

                //染色
                boost_icon_white!!.setTint(Color.parseColor("#000000"))
                web_icon_white!!.setTint(Color.parseColor("#000000"))
                bookmark_icon_white!!.setTint(Color.parseColor("#000000"))
                favourite_icon_white!!.setTint(Color.parseColor("#000000"))
                reaction_icon_white!!.setTint(Color.parseColor("#000000"))

                //入れる
                boost_button.setCompoundDrawablesWithIntrinsicBounds(boost_icon_white, null, null, null)
                web_button.setCompoundDrawablesWithIntrinsicBounds(web_icon_white, null, null, null)
                holder!!.bookmark_button!!.setCompoundDrawablesWithIntrinsicBounds(bookmark_icon_white, null, null, null)
                holder!!.nicoru_button!!.setCompoundDrawablesWithIntrinsicBounds(favourite_icon_white, null, null, null)
                if (CustomMenuTimeLine.isMisskeyMode) {
                    holder!!.nicoru_button!!.setCompoundDrawablesWithIntrinsicBounds(reaction_icon_white, null, null, null)
                }
            }
        } catch (e: ClassCastException) {
            //アイコンを取得
            val boost_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_repeat_black_24dp, null)
            val web_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_more_vert_black_24dp, null)
            val bookmark_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_bookmark_border_black_24dp, null)
            val favourite_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_star_black_24dp, null)
            //Misskey
            val reaction_icon_white = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_add_black_24dp, null)

            //染色
            boost_icon_white!!.setTint(Color.parseColor("#000000"))
            web_icon_white!!.setTint(Color.parseColor("#000000"))
            bookmark_icon_white!!.setTint(Color.parseColor("#000000"))
            favourite_icon_white!!.setTint(Color.parseColor("#000000"))
            reaction_icon_white!!.setTint(Color.parseColor("#000000"))

            //入れる
            boost_button.setCompoundDrawablesWithIntrinsicBounds(boost_icon_white, null, null, null)
            web_button.setCompoundDrawablesWithIntrinsicBounds(web_icon_white, null, null, null)
            holder!!.bookmark_button!!.setCompoundDrawablesWithIntrinsicBounds(bookmark_icon_white, null, null, null)
            holder!!.nicoru_button!!.setCompoundDrawablesWithIntrinsicBounds(favourite_icon_white, null, null, null)
            if (CustomMenuTimeLine.isMisskeyMode) {
                holder!!.nicoru_button!!.setCompoundDrawablesWithIntrinsicBounds(reaction_icon_white, null, null, null)
            }
        }

        //自分、ブーストいいですか？
        //とりあえず要素数で
        if (boostFavCount) {

            //もってくる
            val isBoost = item.listItem!![16]
            val isFav = item.listItem!![17]
            val boostCount = item.listItem!![18]
            val favCount = item.listItem!![19]

            //ブースト、Renoteカウンター
            boost_button.text = boostCount
            //りぶろぐした・りぶろぐおしたとき
            if (isBoost.contains("reblogged") || boostClick[0]) {
                val boostIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_repeat_black_24dp_2, null)
                boostIcon!!.setTint(Color.parseColor("#008000"))
                boost_button.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null)
            }

            //ふぁぼした、ふぁぼおした
            //Mastodon限定
            if (!CustomMenuTimeLine.isMisskeyMode) {
                if (isFav.contains("favourited") || favClick[0]) {
                    val favIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_star_black_24dp_1, null)
                    favIcon!!.setTint(Color.parseColor("#ffd700"))
                    nicoru.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null)
                }
            }

            //Misskeyはリアクション、Mastodonはカウントを入れる
            if (CustomMenuTimeLine.isMisskeyMode) {
                nicoru.text = toReactionEmoji(isFav)
                //addView
                holder!!.misskey_Reaction!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                holder!!.misskey_Reaction!!.text = favCount
                holder!!.misskey_Reaction!!.textSize = Integer.valueOf(pref_setting.getString("pref_fontsize_button", "10")!!).toFloat()
                holder!!.misskey_Reaction!!.background = context.getDrawable(R.drawable.button_corners)
            } else {
                nicoru.text = favCount
            }

        }

        //ボタン非表示モード
        val button_hidden = pref_setting.getBoolean("pref_timeline_button", false)
        if (button_hidden) {

            val button_layout = holder!!.button_linearLayout
            button_layout!!.removeView(nicoru)
            button_layout.removeView(boost)
            button_layout.removeView(web_button)
            button_layout.removeView(holder!!.bookmark_button)

            val toot_layout = holder!!.toot_linearLayout

            //めにゅー
            val finalFavorite_title1 = favorite_message
            val finalView = view
            val finalAccount_id1 = account_id
            toot_layout!!.setOnClickListener {
                //Toast.makeText(getContext(), "Test", Toast.LENGTH_SHORT).show();

                val items = arrayOf(finalFavorite_title1, finalView.context.getString(R.string.boost_button), "Web", finalView.context.getString(R.string.bookmark), finalView.context.getString(R.string.account))
                AlertDialog.Builder(context)
                        .setTitle(finalView.context.getString(R.string.menu))
                        .setItems(items) { dialog, which ->
                            //whichは番号
                            //                                    Toast.makeText(getContext(), String.valueOf(which), Toast.LENGTH_SHORT).show();

                            //Favorite
                            if (which == 0) {
                                val favorite = pref_setting.getBoolean("pref_nicoru_dialog", false)
                                if (favorite) {

                                    val alertDialog = AlertDialog.Builder(context)
                                    alertDialog.setTitle(R.string.confirmation)
                                    alertDialog.setMessage(finalFavorite_title)
                                    alertDialog.setPositiveButton(finalFavorite_message) { dialog, which ->
                                        object : AsyncTask<String, Void, String>() {

                                            override fun doInBackground(vararg params: String): String {
                                                val accessToken = AccessToken()
                                                accessToken.accessToken = finalAccessToken!!

                                                val client = MastodonClient.Builder(finalInstance1!!, OkHttpClient.Builder(), Gson()).accessToken(accessToken.accessToken).build()

                                                val requestBody = FormBody.Builder()
                                                        //.add(":id" , toot_id_string)
                                                        .build()

                                                println("=====" + client.post("statuses/$id_string/favourite", requestBody))

                                                return id_string
                                            }

                                            override fun onPostExecute(result: String) {
                                                Toast.makeText(context, finalNicoru_text!! + result, Toast.LENGTH_SHORT).show()
                                            }

                                        }.execute()
                                    }
                                    alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                                    alertDialog.create().show()

                                } else {

                                    object : AsyncTask<String, Void, String>() {

                                        override fun doInBackground(vararg params: String): String {
                                            val accessToken = AccessToken()
                                            accessToken.accessToken = finalAccessToken1!!

                                            val client = MastodonClient.Builder(finalInstance1!!, OkHttpClient.Builder(), Gson()).accessToken(accessToken.accessToken).build()

                                            val requestBody = FormBody.Builder()
                                                    //.add(":id" , toot_id_string)
                                                    .build()

                                            println("=====" + client.post("statuses/$id_string/favourite", requestBody))

                                            return id_string
                                        }

                                        override fun onPostExecute(result: String) {
                                            Toast.makeText(context, finalNicoru_text!! + result, Toast.LENGTH_SHORT).show()
                                        }

                                    }.execute()
                                }
                            }
                            //Boost
                            if (which == 1) {

                                val favorite = pref_setting.getBoolean("pref_nicoru_dialog", false)
                                if (favorite) {

                                    val alertDialog = AlertDialog.Builder(context)
                                    alertDialog.setTitle(R.string.confirmation)
                                    alertDialog.setMessage(R.string.dialog_boost_info)
                                    alertDialog.setPositiveButton(R.string.boost_button) { dialog, which ->
                                        object : AsyncTask<String, Void, String>() {

                                            override fun doInBackground(vararg params: String): String {
                                                val accessToken = AccessToken()
                                                accessToken.accessToken = finalAccessToken!!

                                                val client = MastodonClient.Builder(finalInstance1!!, OkHttpClient.Builder(), Gson()).accessToken(accessToken.accessToken).build()

                                                val requestBody = FormBody.Builder()
                                                        //.add(":id" , toot_id_string)
                                                        .build()

                                                println("=====" + client.post("statuses/$id_string/reblog", requestBody))

                                                return id_string
                                            }

                                            override fun onPostExecute(result: String) {
                                                Toast.makeText(context, finalNicoru_text!! + result, Toast.LENGTH_SHORT).show()
                                            }

                                        }.execute()
                                    }
                                    alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                                    alertDialog.create().show()

                                } else {

                                    object : AsyncTask<String, Void, String>() {

                                        override fun doInBackground(vararg params: String): String {
                                            val accessToken = AccessToken()
                                            accessToken.accessToken = finalAccessToken1!!

                                            val client = MastodonClient.Builder(finalInstance1!!, OkHttpClient.Builder(), Gson()).accessToken(accessToken.accessToken).build()

                                            val requestBody = FormBody.Builder()
                                                    //.add(":id" , toot_id_string)
                                                    .build()

                                            println("=====" + client.post("statuses/$id_string/reblog", requestBody))

                                            return id_string
                                        }

                                        override fun onPostExecute(result: String) {
                                            Toast.makeText(context, finalNicoru_text!! + result, Toast.LENGTH_SHORT).show()
                                        }

                                    }.execute()

                                }

                            }
                            //Web
                            if (which == 2) {
                                val chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true)

                                //戻るアイコン
                                val back_icon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_action_arrow_back)

                                //有効
                                if (chrome_custom_tabs) {

                                    val custom = CustomTabsHelper.getPackageNameToUse(context)

                                    val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                                    val customTabsIntent = builder.build()
                                    customTabsIntent.intent.setPackage(custom)
                                    customTabsIntent.launchUrl(context as Activity, Uri.parse("https://" + Instance + "/" + "@" + listItem[7] + "/" + id_string))
                                    //無効
                                } else {
                                    val uri = Uri.parse("https://" + Instance + "/" + "@" + listItem[7] + "/" + id_string)
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)

                                }
                            }
                            //ブックマーク
                            if (which == 3) {
                                val favorite = pref_setting.getBoolean("pref_nicoru_dialog", false)
                                if (favorite) {

                                    val alertDialog = AlertDialog.Builder(context)
                                    alertDialog.setTitle(R.string.confirmation)
                                    alertDialog.setMessage(R.string.dialog_boost_info)
                                    alertDialog.setPositiveButton(R.string.boost_button) { dialog, which ->
                                        if (sqLite == null) {
                                            sqLite = TootBookmark_SQLite(context)
                                        }

                                        if (sqLiteDatabase == null) {
                                            sqLiteDatabase = sqLite!!.writableDatabase
                                        }

                                        val toot_sq = listItem[1]
                                        val id_sq = listItem[4]

                                        val contentValues = ContentValues()
                                        contentValues.put("toot", toot_sq)
                                        contentValues.put("id", id_sq)
                                        sqLiteDatabase!!.insert("tootbookmarkdb", null, contentValues)

                                        Toast.makeText(context, R.string.add_Bookmark, Toast.LENGTH_SHORT).show()
                                    }
                                    alertDialog.setNegativeButton(R.string.cancel) { dialog, which -> }
                                    alertDialog.create().show()

                                } else {
                                    if (sqLite == null) {
                                        sqLite = TootBookmark_SQLite(context)
                                    }

                                    if (sqLiteDatabase == null) {
                                        sqLiteDatabase = sqLite!!.writableDatabase
                                    }

                                    val toot_sq = listItem[1]
                                    val id_sq = listItem[4]

                                    val contentValues = ContentValues()
                                    contentValues.put("toot", toot_sq)
                                    contentValues.put("id", id_sq)
                                    sqLiteDatabase!!.insert("tootbookmarkdb", null, contentValues)
                                    Toast.makeText(context, R.string.add_Bookmark, Toast.LENGTH_SHORT).show()
                                }

                            }

                            //Account
                            if (which == 4) {
                                //読み込み
                                val multipain_ui_mode = pref_setting.getBoolean("app_multipain_ui", false)

                                if (multipain_ui_mode) {

                                    val bundle = Bundle()
                                    bundle.putLong("Account_ID", finalAccount_id1)
                                    /*
                                            fragment.setArguments(bundle);

                                            ft.replace(R.id.fragment3, fragment).commit();
*/

                                } else {

                                    val intent = Intent(context, UserActivity::class.java)
                                    //IDを渡す
                                    intent.putExtra("Account_ID", finalAccount_id1)
                                    context.startActivity(intent)
                                }

                            }
                        }.show()
            }

        }

        return view
    }
    //ニコる
    //    ImageButton nicoru = (ImageButton) view.findViewById(R.id.nicoru);


    inner class ViewHolder {
        internal var media_imageview_1: ImageView? = null
        internal var media_imageview_2: ImageView? = null
        internal var media_imageview_3: ImageView? = null
        internal var media_imageview_4: ImageView? = null
        internal var notification_icon: ImageView? = null

        internal var avater_imageview: ImageView? = null

        internal var user_textview: TextView? = null
        internal var tile_textview: TextView? = null
        internal var client_textview: TextView? = null

        internal var nicoru_button: TextView? = null
        internal var boost_button: TextView? = null
        internal var bookmark_button: TextView? = null
        internal var web_button: TextView? = null
        internal var misskey_Reaction: TextView? = null

        internal var cardTextView: TextView? = null
        internal var cardImageView: ImageView? = null

        internal var linearLayoutMediaButton: LinearLayout? = null
        internal var linearLayoutMedia: LinearLayout? = null
        internal var linearLayoutMedia2: LinearLayout? = null
        internal var linearLayoutEnquate: LinearLayout? = null
        internal var vw1: LinearLayout? = null
        internal var toot_linearLayout: LinearLayout? = null
        internal var button_linearLayout: LinearLayout? = null
        internal var avaterImageview_linearLayout: LinearLayout? = null
        internal var card_linearLayout: LinearLayout? = null

        internal var imageButton: Button? = null
    }

    override fun getViewTypeCount(): Int {

        return count
    }

    override fun getItemViewType(position: Int): Int {

        return position
    }

    internal inner class LoadImage : AsyncTask<Any, Void, Bitmap>() {

        private var mDrawable: LevelListDrawable? = null

        override fun doInBackground(vararg params: Any): Bitmap? {
            val source = params[0] as String
            val bitmap = arrayOf<Bitmap>()
            val drawable = arrayOf<Drawable>()
            mDrawable = params[1] as LevelListDrawable
            Log.d(TAG, "doInBackground $source")


            try {
                val `is` = URL(source).openStream()
                return Glide.with(context).asBitmap().load(source).submit(100, 100).get()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            return null

        }

        override fun onPostExecute(bitmap: Bitmap?) {
            Log.d(TAG, "onPostExecute drawable " + mDrawable!!)
            Log.d(TAG, "onPostExecute bitmap " + bitmap!!)
            if (bitmap != null) {
                val d = BitmapDrawable(context.resources, bitmap)
                mDrawable!!.addLevel(1, 1, d)
                mDrawable!!.setBounds(0, 0, 40, 40)
                mDrawable!!.level = 1
                // i don't know yet a better way to refresh TextView
                // mTv.invalidate() doesn't work as expected
                val t = mTv!!.text
                mTv.text = t
                holder!!.tile_textview!!.invalidate()
                holder!!.tile_textview!!.postInvalidate()
                holder!!.tile_textview!!.refreshDrawableState()
            }
        }

    }


    private fun addMediaGlide(mediaURL: String?, ImageView: ImageView?, linearLayout: LinearLayout?) {
        //画像、動画チェック
        if (mediaURL != null) {
            if (mediaURL.contains(".mp4")) {
                ImageView!!.setImageDrawable(context.getDrawable(R.drawable.ic_movie_black_24dp))
                //呼び出し（こっわ
                if (ImageView.parent != null) {
                    (ImageView.parent as ViewGroup).removeView(ImageView)
                }
                //表示
                ImageViewClickCustomTab(ImageView, mediaURL)
                linearLayout!!.addView(ImageView)
            } else {
                //画像を取ってくる
                Glide.with(context)
                        .load(mediaURL)
                        //Overrideはサイズ、placeholderは読み込み中アイコン
                        .apply(RequestOptions()
                                //.override(500, 500)
                                .placeholder(R.drawable.ic_sync_black_24dp))
                        .into(ImageView!!)
                //呼び出し（こっわ
                if (ImageView.parent != null) {
                    (ImageView.parent as ViewGroup).removeView(ImageView)
                }
                //表示
                ImageViewClickCustomTab(ImageView, mediaURL)
                linearLayout!!.addView(ImageView)
            }
        }
    }

    private fun addMediaPicasso(mediaURL: String?, ImageView: ImageView?, linearLayout: LinearLayout?) {
        //画像、動画チェック
        if (mediaURL != null) {
            if (mediaURL.contains(".mp4")) {
                ImageView!!.setImageDrawable(context.getDrawable(R.drawable.ic_movie_black_24dp))
                //呼び出し（こっわ
                if (ImageView.parent != null) {
                    (ImageView.parent as ViewGroup).removeView(ImageView)
                }
                //表示
                ImageViewClickCustomTab(ImageView, mediaURL)
                linearLayout!!.addView(ImageView)
            } else {
                //画像を取ってくる
                Picasso.get()
                        .load(mediaURL)
                        //.resize(500, 500)
                        .placeholder(R.drawable.ic_sync_black_24dp)
                        .into(ImageView)
                //呼び出し（こっわ
                if (ImageView!!.parent != null) {
                    (ImageView.parent as ViewGroup).removeView(ImageView)
                }
                //表示
                ImageViewClickCustomTab(ImageView, mediaURL)
                linearLayout!!.addView(ImageView)
            }
        }
    }

    private fun ImageViewSetting(holder: ViewHolder) {
        //適当にサイズ
        val layoutParams = LinearLayout.LayoutParams(200, 200)
        layoutParams.weight = 1f
        holder.media_imageview_1!!.layoutParams = layoutParams
        holder.media_imageview_2!!.layoutParams = layoutParams
        holder.media_imageview_3!!.layoutParams = layoutParams
        holder.media_imageview_4!!.layoutParams = layoutParams
        /*
        holder.media_imageview_1.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_2.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_3.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_4.setScaleType(ImageView.ScaleType.CENTER);
*/

    }

    fun ImageViewClickCustomTab_LinearLayout(linearLayout: LinearLayout?, mediaURL: String?) {
        linearLayout!!.setOnClickListener {
            val chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true)
            //カスタムタグ有効
            if (chrome_custom_tabs) {
                val custom = CustomTabsHelper.getPackageNameToUse(context)

                val builder = CustomTabsIntent.Builder().setShowTitle(true)
                val customTabsIntent = builder.build()
                customTabsIntent.intent.setPackage(custom)
                customTabsIntent.launchUrl(context as Activity, Uri.parse(mediaURL))

                //無効
            } else {
                val uri = Uri.parse(mediaURL)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                linearLayout.context.startActivity(intent)
            }
        }
    }

    fun ImageViewClickCustomTab(ImageView: ImageView, mediaURL: String) {
        ImageView.setOnClickListener {
            val chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true)
            //カスタムタグ有効
            if (chrome_custom_tabs) {
                val back_icon = BitmapFactory.decodeResource(context.applicationContext.resources, R.drawable.ic_action_arrow_back)
                val custom = CustomTabsHelper.getPackageNameToUse(context)
                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                val customTabsIntent = builder.build()
                customTabsIntent.intent.setPackage(custom)
                customTabsIntent.launchUrl(context, Uri.parse(mediaURL))
                //無効
            } else {
                val uri = Uri.parse(mediaURL)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
        }
    }

    private fun TootAction(id: String, endPoint: String, textView: TextView?) {
        val url = "https:$Instance/api/v1/statuses/$id/$endPoint/?access_token=$AccessToken"
        val requestBody = FormBody.Builder()
                .build()
        //作成
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                textView!!.post { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    textView!!.post { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    //UI Thread
                    textView!!.post {
                        if (endPoint.contains("reblog")) {
                            Toast.makeText(context, context.getString(R.string.boost_ok) + " : " + id, Toast.LENGTH_SHORT).show()
                            val boostIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_repeat_black_24dp_2, null)
                            boostIcon!!.setTint(Color.parseColor("#008000"))
                            textView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null)
                        }
                        if (endPoint.contains("favourite")) {
                            Toast.makeText(context, nicoru_text!! + id, Toast.LENGTH_SHORT).show()
                            val favIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_star_black_24dp_1, null)
                            favIcon!!.setTint(Color.parseColor("#ffd700"))
                            textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null)
                        }
                        if (endPoint.contains("unfavourite")) {
                            Toast.makeText(context, context.getString(R.string.delete_fav_toast) + id, Toast.LENGTH_SHORT).show()
                            val favIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_star_black_24dp_1, null)
                            favIcon!!.setTint(Color.parseColor("#000000"))
                            textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null)
                        }
                        if (endPoint.contains("unreblog")) {
                            Toast.makeText(context, context.getString(R.string.delete_bt_toast) + id, Toast.LENGTH_SHORT).show()
                            val favIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_repeat_black_24dp_2, null)
                            favIcon!!.setTint(Color.parseColor("#000000"))
                            textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null)
                        }
                    }
                }
            }
        })
    }

    private fun FriendsNicoEnquate(id_string: String, number: String, ToastMessage: String) {
        object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg params: String): String {
                val client = MastodonClient.Builder(Instance!!, OkHttpClient.Builder(), Gson()).accessToken(AccessToken!!).build()

                val requestBody = FormBody.Builder()
                        .add("item_index", number)
                        .build()

                println("=====" + client.post("votes/$id_string", requestBody))

                return id_string
            }

            override fun onPostExecute(result: String) {
                Toast.makeText(context, ToastMessage + result, Toast.LENGTH_SHORT).show()
            }
        }.execute()
    }

    /**
     * Misskey Renote / delete
     *
     * @param api_url  "/api/notes/create" か "/api/notes/delete"
     * @param renoteId noteIdを入れて
     */
    private fun postMisskeyRenote(api_url: String, renoteId: String, visibility: String) {
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance$api_url"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("visibility", visibility)
            jsonObject.put("renoteId", renoteId)
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
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                holder!!.boost_button!!.post {
                    e.printStackTrace()
                    Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗時
                    holder!!.boost_button!!.post { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    val boostIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_repeat_black_24dp_2, null)
                    holder!!.boost_button!!.post {
                        when (api_url) {
                            "/api/notes/create" -> {
                                Toast.makeText(context, context.getString(R.string.renote_ok) + " : " + renoteId, Toast.LENGTH_SHORT).show()
                                boostIcon!!.setTint(Color.parseColor("#008000"))
                                holder!!.boost_button!!.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null)
                            }
                            "/api/notes/delete" -> {
                                Toast.makeText(context, context.getString(R.string.renote_delete_ok) + " : " + renoteId, Toast.LENGTH_SHORT).show()
                                boostIcon!!.setTint(Color.parseColor("#000000"))
                                holder!!.boost_button!!.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun quickProfileSnackber(v: View, accountID: String) {
        //読み込み中お知らせ
        val snackbar = Snackbar.make(v, context.getString(R.string.loading_user_info) + "\r\n /api/v1/accounts/" + accountID, Snackbar.LENGTH_INDEFINITE)
        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 2
        //複数行対応させたおかげでずれたので修正
        val progressBar = ProgressBar(context)
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBer_layoutParams
        snackBer_viewGrop.addView(progressBar, 0)
        snackbar.show()

        //APIを叩く
        val url = "https://$Instance/api/v1/accounts/$accountID"
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        //GETリクエスト
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject = JSONObject(response.body()!!.string())

                    var display_name = jsonObject.getString("display_name")
                    val username = jsonObject.getString("acct")
                    var profile_note = jsonObject.getString("note")
                    val avater_url = jsonObject.getString("avatar")
                    val follow = jsonObject.getString("following_count")
                    val follower = jsonObject.getString("followers_count")

                    //カスタム絵文字適用
                    if (emojis_show) {
                        //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
                        val emojis = jsonObject.getJSONArray("emojis")
                        for (i in 0 until emojis.length()) {
                            val emojiObject = emojis.getJSONObject(i)
                            val emoji_name = emojiObject.getString("shortcode")
                            val emoji_url = emojiObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            //display_name
                            if (display_name.contains(emoji_name)) {
                                //あったよ
                                display_name = display_name.replace(":$emoji_name:", custom_emoji_src)
                            }
                            //note
                            if (profile_note.contains(emoji_name)) {
                                //あったよ
                                profile_note = profile_note.replace(":$emoji_name:", custom_emoji_src)
                            }
                        }
                        if (!jsonObject.isNull("profile_emojis")) {
                            val profile_emojis = jsonObject.getJSONArray("profile_emojis")
                            for (i in 0 until profile_emojis.length()) {
                                val emojiObject = profile_emojis.getJSONObject(i)
                                val emoji_name = emojiObject.getString("shortcode")
                                val emoji_url = emojiObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                //display_name
                                if (display_name.contains(emoji_name)) {
                                    //あったよ
                                    display_name = display_name.replace(":$emoji_name:", custom_emoji_src)
                                }
                                //note
                                if (profile_note.contains(emoji_name)) {
                                    //あったよ
                                    profile_note = profile_note.replace(":$emoji_name:", custom_emoji_src)
                                }
                            }
                        }
                    }


                    //フォローされているか（無駄にAPI叩いてね？）
                    val follow_back = arrayOf(context.getString(R.string.follow_back_not))
                    val follow_url = "https://$Instance/api/v1/accounts/relationships/?stream=user&access_token=$AccessToken"

                    //パラメータを設定
                    val builder = HttpUrl.parse(follow_url)!!.newBuilder()
                    builder.addQueryParameter("id", accountID)
                    val final_url = builder.build().toString()

                    //作成
                    val request = Request.Builder()
                            .url(final_url)
                            .get()
                            .build()

                    //GETリクエスト
                    val client = OkHttpClient()
                    val finalProfile_note = profile_note
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {

                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            //JSON化
                            //System.out.println("レスポンス : " + response.body().string());
                            val response_string = response.body()!!.string()
                            try {
                                val jsonArray = JSONArray(response_string)
                                val jsonObject = jsonArray.getJSONObject(0)
                                val followed_by = jsonObject.getBoolean("followed_by")
                                if (followed_by) {
                                    follow_back[0] = context.getString(R.string.follow_back)
                                }
                                val bitmap = Glide.with(context).asBitmap().load(avater_url).submit(100, 100).get()

                                v.post {
                                    val snackbar = Snackbar.make(v, "", Snackbar.LENGTH_SHORT)
                                    val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
                                    val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                    progressBer_layoutParams.gravity = Gravity.CENTER
                                    //SnackBerを複数行対応させる
                                    val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
                                    snackBer_textView.maxLines = Integer.MAX_VALUE
                                    //てきすと
                                    //snackBer_textView.setText(Html.fromHtml(profile_note,Html.FROM_HTML_MODE_COMPACT));
                                    //複数行対応させたおかげでずれたので修正
                                    val avater_ImageView = ImageView(context)
                                    avater_ImageView.layoutParams = progressBer_layoutParams
                                    //LinearLayout動的に生成
                                    val snackber_LinearLayout = LinearLayout(context)
                                    snackber_LinearLayout.orientation = LinearLayout.VERTICAL
                                    val warp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                    snackber_LinearLayout.layoutParams = warp
                                    //そこにTextViewをいれる（もとからあるTextViewは無視）
                                    val snackber_TextView = TextView(context)
                                    val imageGetter = PicassoImageGetter(snackber_TextView)
                                    snackber_TextView.layoutParams = warp
                                    snackber_TextView.setTextColor(Color.parseColor("#ffffff"))
                                    snackber_TextView.text = Html.fromHtml(finalProfile_note, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
                                    //ボタン追加
                                    val userPage_Button = Button(context, null, 0, R.style.Widget_AppCompat_Button_Borderless)
                                    userPage_Button.layoutParams = warp
                                    userPage_Button.background = context.getDrawable(R.drawable.button_style)
                                    userPage_Button.setTextColor(Color.parseColor("#ffffff"))
                                    userPage_Button.setText(R.string.user)
                                    val boostIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_person_black_24dp, null)
                                    boostIcon!!.setTint(Color.parseColor("#ffffff"))
                                    userPage_Button.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null)
                                    userPage_Button.setOnClickListener {
                                        val intent = Intent(context, UserActivity::class.java)
                                        //IDを渡す
                                        intent.putExtra("Account_ID", accountID)
                                        context.startActivity(intent)
                                    }


                                    //ふぉろー
                                    val follow_TextView = TextView(context)
                                    follow_TextView.setTextColor(Color.parseColor("#ffffff"))
                                    follow_TextView.text = context.getString(R.string.follow) + " : \n" + follow
                                    val done = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_done_black_24dp, null)
                                    done!!.setTint(Color.parseColor("#ffffff"))
                                    follow_TextView.layoutParams = warp
                                    follow_TextView.setCompoundDrawablesWithIntrinsicBounds(done, null, null, null)
                                    //ふぉろわー
                                    val follower_TextView = TextView(context)
                                    follower_TextView.setTextColor(Color.parseColor("#ffffff"))
                                    follower_TextView.text = context.getString(R.string.follower) + " : \n" + follower
                                    val done_all = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_done_all_black_24dp, null)
                                    done_all!!.setTint(Color.parseColor("#ffffff"))
                                    follower_TextView.layoutParams = warp
                                    follower_TextView.setCompoundDrawablesWithIntrinsicBounds(done_all, null, null, null)

                                    //ふぉろーされているか
                                    val follow_info = TextView(context)
                                    follow_info.setTextColor(Color.parseColor("#ffffff"))
                                    follow_info.layoutParams = warp
                                    val follow_info_drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_info_outline_black_24dp, null)
                                    follow_info_drawable!!.setTint(Color.parseColor("#ffffff"))
                                    follow_info.setCompoundDrawablesWithIntrinsicBounds(follow_info_drawable, null, null, null)
                                    //日本語のときだけ改行する
                                    val stringBuilder = StringBuilder(follow_back[0])
                                    if (!follow_back[0].contains("Following") && !follow_back[0].contains("not following")) {
                                        follow_info.text = stringBuilder.insert(4, "\n")
                                    } else {
                                        follow_info.text = follow_back[0]
                                    }


                                    //ぷろが、ふぉろーふぉろわー、ふぉろーじょうたい、アカウントベージ移動、用LinearLayout
                                    val account_info_LinearLayout = LinearLayout(context)
                                    account_info_LinearLayout.layoutParams = warp
                                    account_info_LinearLayout.orientation = LinearLayout.VERTICAL

                                    //追加
                                    account_info_LinearLayout.addView(avater_ImageView)
                                    account_info_LinearLayout.addView(follow_info)
                                    account_info_LinearLayout.addView(follow_TextView)
                                    account_info_LinearLayout.addView(follower_TextView)
                                    account_info_LinearLayout.addView(userPage_Button)

                                    //LinearLayoutについか
                                    snackber_LinearLayout.addView(snackber_TextView)

                                    snackBer_viewGrop.addView(account_info_LinearLayout, 0)
                                    snackBer_viewGrop.addView(snackber_LinearLayout, 1)
                                    //Bitmap
                                    avater_ImageView.setImageBitmap(bitmap)
                                    snackbar.show()
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            } catch (e: ExecutionException) {
                                e.printStackTrace()
                            }

                        }
                    })


                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * QuickProfile Misskey
     */
    private fun showMisskeyQuickProfile(v: View, userId: String) {
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance/api/users/show"
        //読み込み中お知らせ
        val snackbar = Snackbar.make(v, context.getString(R.string.loading_user_info) + "\r\n" + url, Snackbar.LENGTH_INDEFINITE)
        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 2
        //複数行対応させたおかげでずれたので修正
        val progressBar = ProgressBar(context)
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBer_layoutParams
        snackBer_viewGrop.addView(progressBar, 0)
        snackbar.show()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("userId", userId)
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
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失敗時
                holder!!.nicoru_button!!.post { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗時
                    holder!!.nicoru_button!!.post { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        val jsonObject = JSONObject(response_string)
                        var display_name = jsonObject.getString("name")
                        val username = jsonObject.getString("username")
                        var description = jsonObject.getString("description")
                        val avatarUrl = jsonObject.getString("avatarUrl")
                        val followingCount = jsonObject.getString("followingCount")
                        val followersCount = jsonObject.getString("followersCount")
                        val isFollowing = jsonObject.getBoolean("isFollowing")
                        val isFollowed = jsonObject.getBoolean("isFollowed")
                        //カスタム絵文字適用
                        if (emojis_show) {
                            //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
                            val emojis = jsonObject.getJSONArray("emojis")
                            for (i in 0 until emojis.length()) {
                                val emojiObject = emojis.getJSONObject(i)
                                val emoji_name = emojiObject.getString("name")
                                val emoji_url = emojiObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                //display_name
                                if (display_name.contains(emoji_name)) {
                                    //あったよ
                                    display_name = display_name.replace(":$emoji_name:", custom_emoji_src)
                                }
                                //note
                                if (description.contains(emoji_name)) {
                                    //あったよ
                                    description = description.replace(":$emoji_name:", custom_emoji_src)
                                }
                            }
                        }
                        //フォローされてるかどうかの文字
                        var follow_back = context.getString(R.string.follow_back_not)
                        if (isFollowing) {
                            follow_back = context.getString(R.string.follow_back)
                        }
                        val snackbar = Snackbar.make(v, "", Snackbar.LENGTH_SHORT)
                        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
                        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        progressBer_layoutParams.gravity = Gravity.CENTER
                        //SnackBerを複数行対応させる
                        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
                        snackBer_textView.maxLines = Integer.MAX_VALUE
                        //てきすと
                        //snackBer_textView.setText(Html.fromHtml(profile_note,Html.FROM_HTML_MODE_COMPACT));
                        //複数行対応させたおかげでずれたので修正
                        val avater_ImageView = ImageView(context)
                        avater_ImageView.layoutParams = progressBer_layoutParams
                        //LinearLayout動的に生成
                        val snackber_LinearLayout = LinearLayout(context)
                        snackber_LinearLayout.orientation = LinearLayout.VERTICAL
                        val warp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        snackber_LinearLayout.layoutParams = warp
                        //そこにTextViewをいれる（もとからあるTextViewは無視）
                        val snackber_TextView = TextView(context)
                        val imageGetter = PicassoImageGetter(snackber_TextView)
                        snackber_TextView.layoutParams = warp
                        snackber_TextView.setTextColor(Color.parseColor("#ffffff"))
                        snackber_TextView.text = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
                        //ボタン追加
                        val userPage_Button = Button(context, null, 0, R.style.Widget_AppCompat_Button_Borderless)
                        userPage_Button.layoutParams = warp
                        userPage_Button.background = context.getDrawable(R.drawable.button_style)
                        userPage_Button.setTextColor(Color.parseColor("#ffffff"))
                        userPage_Button.setText(R.string.user)
                        val boostIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_person_black_24dp, null)
                        boostIcon!!.setTint(Color.parseColor("#ffffff"))
                        userPage_Button.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null)
                        userPage_Button.setOnClickListener {
                            val intent = Intent(context, UserActivity::class.java)
                            //IDを渡す
                            intent.putExtra("Misskey", true)
                            intent.putExtra("Account_ID", userId)
                            context.startActivity(intent)
                        }


                        //ふぉろー
                        val follow_TextView = TextView(context)
                        follow_TextView.setTextColor(Color.parseColor("#ffffff"))
                        follow_TextView.text = context.getString(R.string.follow) + " : \n" + followingCount
                        val done = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_done_black_24dp, null)
                        done!!.setTint(Color.parseColor("#ffffff"))
                        follow_TextView.layoutParams = warp
                        follow_TextView.setCompoundDrawablesWithIntrinsicBounds(done, null, null, null)
                        //ふぉろわー
                        val follower_TextView = TextView(context)
                        follower_TextView.setTextColor(Color.parseColor("#ffffff"))
                        follower_TextView.text = context.getString(R.string.follower) + " : \n" + followersCount
                        val done_all = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_done_all_black_24dp, null)
                        done_all!!.setTint(Color.parseColor("#ffffff"))
                        follower_TextView.layoutParams = warp
                        follower_TextView.setCompoundDrawablesWithIntrinsicBounds(done_all, null, null, null)

                        //ふぉろーされているか
                        val follow_info = TextView(context)
                        follow_info.setTextColor(Color.parseColor("#ffffff"))
                        follow_info.layoutParams = warp
                        val follow_info_drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_info_outline_black_24dp, null)
                        follow_info_drawable!!.setTint(Color.parseColor("#ffffff"))
                        follow_info.setCompoundDrawablesWithIntrinsicBounds(follow_info_drawable, null, null, null)
                        //日本語のときだけ改行する
                        val stringBuilder = StringBuilder(follow_back)
                        if (!follow_back.contains("Following") && !follow_back.contains("not following")) {
                            follow_info.text = stringBuilder.insert(4, "\n")
                        } else {
                            follow_info.text = follow_back
                        }


                        //ぷろが、ふぉろーふぉろわー、ふぉろーじょうたい、アカウントベージ移動、用LinearLayout
                        val account_info_LinearLayout = LinearLayout(context)
                        account_info_LinearLayout.layoutParams = warp
                        account_info_LinearLayout.orientation = LinearLayout.VERTICAL

                        //追加
                        account_info_LinearLayout.addView(avater_ImageView)
                        account_info_LinearLayout.addView(follow_info)
                        account_info_LinearLayout.addView(follow_TextView)
                        account_info_LinearLayout.addView(follower_TextView)
                        account_info_LinearLayout.addView(userPage_Button)

                        //LinearLayoutについか
                        snackber_LinearLayout.addView(snackber_TextView)

                        snackBer_viewGrop.addView(account_info_LinearLayout, 0)
                        snackBer_viewGrop.addView(snackber_LinearLayout, 1)
                        //Bitmap
                        try {
                            val bitmap = Glide.with(context).asBitmap().load(avatarUrl).submit(100, 100).get()
                            avater_ImageView.setImageBitmap(bitmap)
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                        snackbar.show()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })
    }

    /**
     * Misskey リアクション
     */
    private fun showMisskeyReaction(id_string: String, textView: TextView?, item: ListItem) {
        //UI Thread
        Handler(Looper.getMainLooper()).post {
            val snackbar = Snackbar.make(holder!!.nicoru_button!!, "", Snackbar.LENGTH_INDEFINITE)
            val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
            //TextViewを非表示にする
            val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
            snackBer_textView.visibility = View.INVISIBLE

            //Linearlayout
            val main_LinearLayout = LinearLayout(context)
            main_LinearLayout.orientation = LinearLayout.VERTICAL
            main_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            //Text
            val title_TextView = TextView(context)
            title_TextView.setTextColor(Color.parseColor("#ffffff"))
            title_TextView.textSize = 18f
            title_TextView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            title_TextView.text = context.getText(R.string.add_reaction)

            //ボタン追加
            val reactionEmojis = arrayOf("👍", "❤", "😆", "🤔", "😮", "🎉", "💢", "😥", "😇", "🍣")
            val reactionNames = arrayOf("like", "love", "laugh", "hmm", "surprise", "congrats", "angry", "confused", "rip", "pudding")
            //2行にする
            val reaction_LinearLayout_up = LinearLayout(context)
            val reaction_LinearLayout_down = LinearLayout(context)
            reaction_LinearLayout_up.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            reaction_LinearLayout_down.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            reaction_LinearLayout_up.orientation = LinearLayout.HORIZONTAL
            reaction_LinearLayout_down.orientation = LinearLayout.HORIZONTAL
            val button_LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            button_LayoutParams.weight = 1f
            //for
            for (i in reactionEmojis.indices) {
                val button = Button(context)
                button.background = context.getDrawable(R.drawable.button_style)
                button.layoutParams = button_LayoutParams
                button.text = reactionEmojis[i]
                //クリックイベント
                button.setOnClickListener { v ->
                    //確認、ダイアログを出さない設定とう確認してから
                    if (pref_setting.getBoolean("pref_nicoru_dialog", true) && !dialog_not_show) {
                        Snackbar.make(v, context.getText(R.string.reaction_message), Snackbar.LENGTH_SHORT).setAction(context.getText(R.string.reaction_post)) {
                            postMisskeyReaction("create", reactionNames[i], id_string)
                            item.listItem!![17] = reactionEmojis[i]
                            textView!!.text = toReactionEmoji(reactionEmojis[i])
                        }.show()
                    } else {
                        postMisskeyReaction("create", reactionNames[i], id_string)
                        item.listItem!![17] = reactionEmojis[i]
                        textView!!.text = toReactionEmoji(reactionEmojis[i])
                    }
                }
                //0-4までは上の段
                if (i < 5) {
                    reaction_LinearLayout_up.addView(button)
                } else {
                    reaction_LinearLayout_down.addView(button)
                }
            }
            //絵文字を入力する
            //レイアウト読み込み
            val emoji_LinearLayout = LinearLayout(context)
            emoji_LinearLayout.orientation = LinearLayout.HORIZONTAL
            emoji_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val editText = EditText(context)
            editText.hint = context.getString(R.string.reaction_pick)
            editText.setHintTextColor(Color.parseColor("#ffffff"))
            //大きくする
            val edittext_Params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            editText.layoutParams = edittext_Params
            val post_Button = Button(context)
            post_Button.background = context.getDrawable(R.drawable.button_style)
            post_Button.text = context.getText(R.string.reaction_post)
            //ボタンのサイズ
            val button_Params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            edittext_Params.weight = 1f
            post_Button.layoutParams = button_Params
            post_Button.setTextColor(Color.parseColor("#ffffff"))
            //クリックイベント
            post_Button.setOnClickListener { v ->
                if (pref_setting.getBoolean("pref_nicoru_dialog", true) && !dialog_not_show) {
                    Snackbar.make(v, context.getText(R.string.reaction_message), Snackbar.LENGTH_SHORT).setAction(context.getText(R.string.reaction_post)) {
                        postMisskeyReaction("create", editText.text.toString(), id_string)
                        item.listItem!![17] = editText.text.toString()
                        textView!!.text = editText.text.toString()
                    }.show()
                } else {
                    postMisskeyReaction("create", editText.text.toString(), id_string)
                    item.listItem!![17] = editText.text.toString()
                    textView!!.text = editText.text.toString()
                }
            }
            //追加
            emoji_LinearLayout.addView(editText)
            emoji_LinearLayout.addView(post_Button)

            //追加
            main_LinearLayout.addView(title_TextView)
            main_LinearLayout.addView(reaction_LinearLayout_up)
            main_LinearLayout.addView(reaction_LinearLayout_down)
            main_LinearLayout.addView(emoji_LinearLayout)

            snackBer_viewGrop.addView(main_LinearLayout, 0)
            //表示
            snackbar.show()
        }
    }

    /**
     * Misskey Reactionする！
     *
     * @param create_delete createかdelete
     * @param reactionName  リアクション（リアクション一覧どこにあるの？）
     */
    private fun postMisskeyReaction(create_delete: String, reactionName: String, id_string: String) {
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance/api/notes/reactions/$create_delete"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("noteId", id_string)
            jsonObject.put("reaction", reactionName)
            jsonObject.put("i", token)
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
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失敗時
                holder!!.nicoru_button!!.post { Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗時
                    holder!!.nicoru_button!!.post { Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    //成功時
                    holder!!.nicoru_button!!.post {
                        //メッセージ
                        if (url.contains("create")) {
                            Toast.makeText(context, context.getString(R.string.reaction_ok) + ":" + toReactionEmoji(reactionName) + "\n" + id_string, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.reaction_delete_ok) + "\n" + id_string, Toast.LENGTH_SHORT).show()
                        }
                        holder!!.nicoru_button!!.text = toReactionEmoji(reactionName)
                    }
                }
            }
        })
    }


    private fun SQLitePut(item: ListItem) {
        if (sqLite == null) {
            sqLite = TootBookmark_SQLite(context)
        }

        if (sqLiteDatabase == null) {
            sqLiteDatabase = sqLite!!.writableDatabase
        }

        val toot_sq = item.listItem!![1]
        val id_sq = item.listItem!![4]
        val account = item.listItem!![2]
        val info = item.listItem!![3]
        val account_id = item.listItem!![6]
        val avater = item.listItem!![5]
        val account_id_string = item.listItem!![7]

        val media_1 = item.listItem!![8]
        val media_2 = item.listItem!![9]
        val media_3 = item.listItem!![10]
        val media_4 = item.listItem!![11]

        val contentValues = ContentValues()
        contentValues.put("toot", toot_sq)
        contentValues.put("id", id_sq)
        contentValues.put("account", account)
        contentValues.put("info", info)
        contentValues.put("account_id", account_id)
        contentValues.put("avater_url", avater)
        contentValues.put("username", account_id_string)

        contentValues.put("media1", media_1)
        contentValues.put("media2", media_2)
        contentValues.put("media3", media_3)
        contentValues.put("media4", media_4)


        sqLiteDatabase!!.insert("tootbookmarkdb", null, contentValues)

        Toast.makeText(context, R.string.add_Bookmark, Toast.LENGTH_SHORT).show()
    }

    fun LayoutSimple(holder: HomeTimeLineAdapter.ViewHolder?) {
        holder!!.button_linearLayout!!.removeView(holder.bookmark_button)
        holder.button_linearLayout!!.removeView(holder.web_button)
        holder.button_linearLayout!!.removeView(holder.boost_button)
        holder.button_linearLayout!!.removeView(holder.nicoru_button)
    }

/*
    fun setSVGAnimationIcon(animationIcon: Int, notAnimationIcon: Int, holder: ViewHolder) {
        //SVG許可
        val svgAnimation = pref_setting.getBoolean("pref_svg_animation", false)
        //無効
        if (svgAnimation) {
            holder.notification_icon!!.setImageResource(notAnimationIcon)
            if (holder.notification_icon!!.parent != null) {
                (holder.notification_icon!!.parent as ViewGroup).removeView(holder.notification_icon)
            }
            holder.avaterImageview_linearLayout!!.addView(holder.notification_icon, 0)
            //有効
        } else {
            holder.notification_icon!!.setImageResource(animationIcon)
            if (holder.notification_icon!!.parent != null) {
                (holder.notification_icon!!.parent as ViewGroup).removeView(holder.notification_icon)
            }
            val animatable = holder.notification_icon!!.drawable as Animatable2
            animatable.start()
            */
/*
            animatable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    animatable.start();
                    super.onAnimationEnd(drawable);
                }
            });
*//*

            holder.avaterImageview_linearLayout!!.addView(holder.notification_icon, 0)
        }
    }
*/

    companion object {

        //public static final long Account_ID = "com.takusan23.kaisendon.Account_ID";
        private val TAG = "TestImageGetter"

        /**
         * Misskey　リアクション絵文字変換
         */
        fun toReactionEmoji(emoji: String): String {
            var emoji = emoji
            when (emoji) {
                "like" -> emoji = "👍"
                "love" -> emoji = "❤"
                "laugh" -> emoji = "😆"
                "hmm" -> emoji = "🤔"
                "surprise" -> emoji = "😮"
                "congrats" -> emoji = "🎉"
                "angry" -> emoji = "💢"
                "confused" -> emoji = "😥"
                "rip" -> emoji = "😇"
                "pudding" -> emoji = "🍣"
                "star" -> emoji = "⭐"
                "null" -> emoji = ""
            }
            return emoji
        }
    }


}