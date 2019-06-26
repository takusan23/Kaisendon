package io.github.takusan23.Kaisendon

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Handler
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import io.github.takusan23.Kaisendon.CustomTabURL.LinkTransformationMethod
import org.chromium.customtabsclient.shared.CustomTabsHelper
import java.util.*

class SimpleAdapter
/**
 * コンストラクタ
 *
 * @param context  コンテキスト
 * @param resource リソースID
 * @param items    リストビューの要素
 */
(context: Context,
        //public static final long Account_ID = "com.takusan23.kaisendon.Account_ID";

 private val mResource: Int, private val mItems: List<io.github.takusan23.Kaisendon.ListItem>) : ArrayAdapter<ListItem>(context, mResource, mItems) {
    private val mInflater: LayoutInflater
    //private val layoutId: Int
    private val visibleSet = HashSet<Int>()
    internal val handler_1 = android.os.Handler()
    private val sqLite: TootBookmark_SQLite? = null
    private val sqLiteDatabase: SQLiteDatabase? = null

    //メディア
    private var media_url_1: String? = null
    private var media_url_2: String? = null
    private var media_url_3: String? = null
    private var media_url_4: String? = null


    //internal var pref = Preference_ApplicationContext.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    //settingのプリファレンスをとる
    internal var pref_setting = getDefaultSharedPreferences(context!!)

    //internal var AccessToken = pref.getString("token", "")

    init {
        mInflater = LayoutInflater.from(context)
        //this.layoutId = layoutId

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder
        //viewHolder = SimpleAdapter.ViewHolder()

        //Emoji
        val config = BundledEmojiCompatConfig(context)
        config.setReplaceAll(true)
        EmojiCompat.init(config)
        val compat = EmojiCompat.get()


        val holder: ViewHolder


        //データの再利用を許さない！！！！！！！！！！！！！！！！！！！
        if (convertView == null) {

            view = mInflater.inflate(R.layout.timeline_item, parent, false)

            holder = ViewHolder()

            holder.linearLayout = view!!.findViewById<View>(R.id.linearlayout_media) as LinearLayout
            holder.vw1 = view.findViewById(R.id.vw1)
            holder.toot_linearLayout = view.findViewById(R.id.toot_linearlayout)
            holder.button_linearLayout = view.findViewById(R.id.button_layout)
            holder.avaterImageview_linearLayout = view.findViewById(R.id.avater_imageview_linearlayout)

            //添付メディア
            holder.linearLayoutMediaButton = view.findViewById(R.id.linearlayout_mediaButton)
            holder.linearLayoutMedia = view.findViewById(R.id.linearlayout_media)
            holder.linearLayoutMedia2 = view.findViewById(R.id.linearlayout_media2)
            holder.media_imageview_1 = ImageView(holder.linearLayout!!.context)
            holder.media_imageview_2 = ImageView(holder.linearLayout!!.context)
            holder.media_imageview_3 = ImageView(holder.linearLayout!!.context)
            holder.media_imageview_4 = ImageView(holder.linearLayout!!.context)
            holder.imageButton = Button(context)
            holder.notification_icon = ImageView(context)

            holder.avater_imageview = view.findViewById(R.id.thumbnail)

            holder.user_textview = view.findViewById(R.id.user)
            holder.tile_textview = view.findViewById(R.id.tile_)
            holder.client_textview = view.findViewById(R.id.client)

            holder.bookmark_button = view.findViewById(R.id.bookmark)
            holder.nicoru_button = view.findViewById(R.id.nicoru)
            holder.boost_button = view.findViewById(R.id.boost)
            holder.web_button = view.findViewById(R.id.web)

            view.tag = holder


        } else {
            holder = view!!.tag as ViewHolder
        }


        val item = mItems[position]
        val listItem = item.listItem


        //URLをCustomTabで開くかどうか
        val chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true)
        if (chrome_custom_tabs) {
            holder.tile_textview!!.transformationMethod = LinkTransformationMethod()
            holder.tile_textview!!.movementMethod = LinkMovementMethod.getInstance()
        } else {
            holder.tile_textview!!.autoLinkMask = Linkify.WEB_URLS
        }


        //設定を取得
        //アクセストークンを変更してる場合のコード
        //アクセストークン
        var AccessToken: String? = null

        //インスタンス
        var Instance: String? = null

        val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
            Instance = pref_setting.getString("pref_mastodon_instance", "")

        } else {

            AccessToken = pref_setting.getString("main_token", "")
            Instance = pref_setting.getString("main_instance", "")

        }


        //        TextView nicoru = holder.nicoru_button;
        //        TextView boost = holder.boost_button;


        val handler = Handler()

        val finalConvertView2 = view

        //ボタンを消し飛ばす
        holder.button_linearLayout!!.removeView(holder.bookmark_button)
        holder.button_linearLayout!!.removeView(holder.web_button)
        holder.button_linearLayout!!.removeView(holder.boost_button)
        holder.button_linearLayout!!.removeView(holder.nicoru_button)


        val nicoru_text: String? = null

        //ニコる
        val finalNicoru_text = nicoru_text
        val id_string = listItem!![4]
        val avater_url = listItem[5]
        val media_url = ""


        //メッセージ
        //設定で分けるように
        var favorite_message: String? = null
        var favorite_title: String? = null

        val nicoru_dialog_chack = pref_setting.getBoolean("pref_friends_nico_mode", false)
        if (nicoru_dialog_chack) {
            favorite_message = "お気に入り"
            favorite_title = "お気に入りに登録しますか"
        } else {
            favorite_message = "ニコる"
            favorite_title = "ニコりますか"
        }


        //Wi-Fi接続状況確認
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)


        //タイムラインに画像を表示
        //動的に画像を追加するよ
        val linearLayout = holder.linearLayout
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)

        val toot_media = pref_setting.getBoolean("pref_toot_media", false)

/*
        media_url_1 = listItem[8]
        media_url_2 = listItem[9]
        media_url_3 = listItem[10]
        media_url_4 = listItem[11]


        if (media_url_1 != null) {
            //System.out.println("にゃーん :" + media_url_2);
            //非表示
            if (toot_media) {
                holder.imageButton!!.setOnClickListener {
                    if (setting_avater_gif) {
                        //GIFアニメ再生させない
                        ImageViewSetting(holder)
                        //表示
                        addMediaPicasso(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia)
                        addMediaPicasso(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia)
                        addMediaPicasso(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2)
                        addMediaPicasso(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2)

                    } else {
                        ImageViewSetting(holder)
                        //表示
                        //Glide.with(getContext()).load(media_url).into(holder.media_imageview_1);
                        addMediaGlide(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia)
                        addMediaGlide(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia)
                        addMediaGlide(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2)
                        addMediaGlide(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2)
                    }
                }
            }

            //Wi-Fi接続時
            if (setting_avater_wifi) {
                if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                    if (setting_avater_gif) {
                        ImageViewSetting(holder)
                        //表示
                        addMediaPicasso(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia)
                        addMediaPicasso(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia)
                        addMediaPicasso(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2)
                        addMediaPicasso(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2)
                    } else {
                        ImageViewSetting(holder)
                        //画像を取ってくる
                        //表示
                        addMediaGlide(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia)
                        addMediaGlide(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia)
                        addMediaGlide(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2)
                        addMediaGlide(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2)
                    }
                }

                //Wi-Fi未接続
            } else {
                holder.imageButton!!.setText(R.string.show_image)
                holder.imageButton!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_image_black_24dp, 0, 0, 0)
                val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.weight = 1f
                holder.imageButton!!.layoutParams = layoutParams
                if (holder.imageButton!!.parent != null) {
                    (holder.imageButton!!.parent as ViewGroup).removeView(holder.imageButton)
                }
                holder.linearLayoutMediaButton!!.addView(holder.imageButton)

                //クリックしてイメージ表示
                val finalMedia_url1 = media_url
                holder.imageButton!!.setOnClickListener {
                    if (setting_avater_gif) {
                        //GIFアニメ再生させない
                        ImageViewSetting(holder)
                        //表示
                        addMediaPicasso(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia)
                        addMediaPicasso(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia)
                        addMediaPicasso(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2)
                        addMediaPicasso(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2)

                    } else {
                        ImageViewSetting(holder)
                        //画像を取ってくる
                        //表示
                        addMediaGlide(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia)
                        addMediaGlide(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia)
                        addMediaGlide(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2)
                        addMediaGlide(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2)
                    }
                }
            }
        }

*/

        //      サムネイル画像を設定
        val thumbnail = holder.avater_imageview
        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_avater", false)
        //avatarがnullかどうか
        if (avater_url != null) {
            //じゃんけんあいこん と　TootShortcut以外
            if (!avater_url.contains("じゃんけん") || !avater_url.contains("toot_shortcut")) {
                if (setting_avater_hidden) {
                    //thumbnail.setImageBitmap(item.getThumbnail());
                }
                //Wi-Fi
                if (setting_avater_wifi) {
                    if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                        if (setting_avater_gif) {

                            //GIFアニメ再生させない
                            Picasso.get()
                                    .load(avater_url)
                                    .into(thumbnail)

                        } else {

                            //GIFアニメを再生
                            Glide.with(view)
                                    .load(avater_url)
                                    .into(thumbnail!!)
                        }
                    } else {
                        //レイアウトを消す
                        holder.vw1!!.removeView(holder.avaterImageview_linearLayout)
                    }//Wi-Fi no Connection

                } else {
                    //レイアウトを消す
                    holder.vw1!!.removeView(holder.avaterImageview_linearLayout)
                }
            } else {
                val imageString: String? = null
                //画像選択
                if (avater_url.contains("勝ちました")) {
                    Glide.with(view)
                            .load(R.drawable.ic_thumb_up_black_24dp)
                            .into(thumbnail!!)
                }
                if (avater_url.contains("負けました")) {
                    Glide.with(view)
                            .load(R.drawable.ic_thumb_down_black_24dp)
                            .into(thumbnail!!)
                }
                if (avater_url.contains("あいこだぜ")) {
                    Glide.with(view)
                            .load(R.drawable.ic_thumbs_up_down_black_24dp)
                            .into(thumbnail!!)
                }
                if (avater_url.contains("えらー")) {
                    Glide.with(view)
                            .load(R.drawable.ic_sync_problem_black_24dp)
                            .into(thumbnail!!)
                }
                if (avater_url.contains("おわり")) {
                    Glide.with(view)
                            .load(R.drawable.ic_local_hotel_black_18dp)
                            .into(thumbnail!!)
                }
            }

            //TootShortcut用
            if (avater_url.contains("toot_shortcut")) {
                val icon_text = avater_url.replace("toot_shortcut ", "")
                Glide.with(view)
                        .load(stringToDrawable(icon_text))
                        .into(thumbnail!!)

            }


        }


        val account_id = listItem[6]


        /*       //ユーザー情報
        FragmentTransaction ft = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
        Fragment fragment = new User_Fragment();
        View finalConvertView = convertView;
        //TootShortcutのときに呼ばれないようにする
        //アカウント一覧のときに呼ばれないようにする
        if (!avater_url.contains("toot_shortcut") || !listItem.get(0).contains("account_list")) {
            thumbnail.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (item.getListItem().get(0).equals("misskey_drive")) {
                        //Misskey Drive
                        //Toast.makeText(getContext(), "ID : " + id_string, Toast.LENGTH_LONG).show();
                        //メディアIDの配列に入れる
                        //追加済み説
                        if (Home.post_media_id.indexOf(id_string) == -1) {
                            Home.post_media_id.add(id_string);
                            Home.misskey_media_url.add(listItem.get(5));
                            Toast.makeText(getContext(), "追加しました : " + id_string, Toast.LENGTH_SHORT).show();
                        } else {
                            Home.post_media_id.remove(id_string);
                            Home.misskey_media_url.remove(listItem.get(5));
                            Toast.makeText(getContext(), "削除しました : " + id_string, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //読み込み
                        boolean multipain_ui_mode = pref_setting.getBoolean("app_multipain_ui", false);
                        Intent intent = new Intent(getContext(), UserActivity.class);
                        //IDを渡す
                        intent.putExtra("Account_ID", account_id);
                        getContext().startActivity(intent);
                    }

                }
            });
        }
*/

        //カスタムストリーミングで背景色を変える機能
        val type = listItem[0]
        if (type != null) {
            if (type.contains("now_account")) {
                holder.vw1!!.setBackgroundColor(Color.parseColor("#1A008080"))
            }
        }


        // トゥート
        val title = holder.tile_textview
        //title.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT));
        title!!.textSize = 18f
        //フォントサイズの変更
        val toot_textsize = pref_setting.getString("pref_fontsize_timeline", "18")
        title.textSize = Integer.parseInt(toot_textsize!!).toFloat()

        // ユーザー名
        val user = holder.user_textview
        //user.setText(item.getUser());
        user!!.textSize = 18f
        //フォントサイズの変更
        val username_textsize = pref_setting.getString("pref_fontsize_user", "18")
        user.textSize = Integer.parseInt(username_textsize!!).toFloat()

        //クライアント
        val client = holder.client_textview
        //client.setText(item.getClient());
        client!!.textSize = 10f
        //フォントサイズ変更
        val client_textsize = pref_setting.getString("pref_fontsize_client", "18")
        client.textSize = Integer.parseInt(client_textsize!!).toFloat()

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


        } else {

        }

        //絵文字強制
        val emoji_compatibility = pref_setting.getBoolean("pref_emoji_compatibility", false)
        val titleString = listItem[1]
        val userString = listItem[2]
        if (emoji_compatibility) {
            //ユーザー名
            EmojiCompat.get().registerInitCallback(object : EmojiCompat.InitCallback() {
                override fun onInitialized() {
                    if (user != null) {
                        user.text = compat.process(userString)
                    }
                }
            })
            //本文
            EmojiCompat.get().registerInitCallback(object : EmojiCompat.InitCallback() {
                override fun onInitialized() {
                    if (title != null) {
                        title.text = compat.process(Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT))
                    }
                }
            })
            //クライアント
            EmojiCompat.get().registerInitCallback(object : EmojiCompat.InitCallback() {
                override fun onInitialized() {
                    if (client != null) {
                        client.text = compat.process(listItem[4])
                    }
                }
            })

        } else {
            //無効時
            user.text = userString
            title.text = Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT)
            client.text = listItem[4]
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


        internal var linearLayout: LinearLayout? = null
        internal var vw1: LinearLayout? = null
        internal var toot_linearLayout: LinearLayout? = null
        internal var button_linearLayout: LinearLayout? = null
        internal var linearLayoutMedia: LinearLayout? = null
        internal var linearLayoutMedia2: LinearLayout? = null
        internal var linearLayoutMediaButton: LinearLayout? = null
        internal var avaterImageview_linearLayout: LinearLayout? = null

        internal var imageButton: Button? = null

    }


    override fun getViewTypeCount(): Int {

        return count
    }

    override fun getItemViewType(position: Int): Int {

        return position
    }


    fun addMediaGlide(mediaURL: String?, ImageView: ImageView?, linearLayout: LinearLayout?) {
        if (mediaURL != null) {
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

    fun addMediaPicasso(mediaURL: String?, ImageView: ImageView?, linearLayout: LinearLayout?) {
        if (mediaURL != null) {
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

    fun ImageViewSetting(holder: ViewHolder) {
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.weight = 1f
        holder.media_imageview_1!!.layoutParams = layoutParams
        holder.media_imageview_2!!.layoutParams = layoutParams
        holder.media_imageview_3!!.layoutParams = layoutParams
        holder.media_imageview_4!!.layoutParams = layoutParams
        holder.media_imageview_1!!.scaleType = ImageView.ScaleType.CENTER
        holder.media_imageview_2!!.scaleType = ImageView.ScaleType.CENTER
        holder.media_imageview_3!!.scaleType = ImageView.ScaleType.CENTER
        holder.media_imageview_4!!.scaleType = ImageView.ScaleType.CENTER

    }

    fun ImageViewClickCustomTab(ImageView: ImageView, mediaURL: String) {
        ImageView.setOnClickListener {
            val back_icon = BitmapFactory.decodeResource(context.applicationContext.resources, R.drawable.ic_action_arrow_back)
            val custom = CustomTabsHelper.getPackageNameToUse(context)

            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(custom)
            customTabsIntent.launchUrl(context, Uri.parse(mediaURL))
        }
    }


    //String→Drawable
    //Wear TootShortcut Setting 用
    private fun stringToDrawable(icon: String): Drawable? {
        var drawable = context.getDrawable(R.drawable.ic_public_black_24dp)
        when (icon) {
            "public" -> drawable = context.getDrawable(R.drawable.ic_public_black_24dp)
            "unlisted" -> drawable = context.getDrawable(R.drawable.ic_done_all_black_24dp_2)
            "private" -> drawable = context.getDrawable(R.drawable.ic_lock_open_black_24dp)
            "direct" -> drawable = context.getDrawable(R.drawable.ic_assignment_ind_black_24dp)
        }
        return drawable
    }


}
