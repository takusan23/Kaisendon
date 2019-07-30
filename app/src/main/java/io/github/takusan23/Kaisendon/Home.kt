package io.github.takusan23.Kaisendon

import android.Manifest
import androidx.appcompat.app.AlertDialog
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import android.graphics.*
import android.graphics.drawable.Animatable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import io.github.takusan23.Kaisendon.APIJSONParse.GlideSupport
import io.github.takusan23.Kaisendon.Activity.LoginActivity
import io.github.takusan23.Kaisendon.CustomMenu.*
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.MisskeyDriveBottomDialog
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.TLQuickSettingSnackber
import io.github.takusan23.Kaisendon.Theme.DarkModeSupport
import io.github.takusan23.Kaisendon.DesktopTL.DesktopFragment
import io.github.takusan23.Kaisendon.Fragment.HelloFragment
import io.github.takusan23.Kaisendon.Omake.LunchBonus
import io.github.takusan23.Kaisendon.Omake.ShinchokuLayout
import io.github.takusan23.Kaisendon.PaintPOST.PaintPOSTActivity
import kotlinx.android.synthetic.main.app_bar_home2.*
import kotlinx.android.synthetic.main.bottom_bar_layout.*
import okhttp3.*
import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class Home : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    internal var toot_text: String? = null
    internal var user: String? = null

    private var helper: CustomMenuSQLiteHelper? = null
    private var db: SQLiteDatabase? = null


    internal var display_name: String? = null
    internal var user_id: String? = null
    internal var user_avater: String? = null
    internal var user_header: String? = null
    internal var toot_count = "0"

    internal lateinit var account_id: String
    private val dialog: ProgressDialog? = null
    internal var alertDialog: AlertDialog? = null

    private val snackbar: Snackbar? = null

    internal var nicoru = false

    internal var test = 0

    internal var textToSpeech: TextToSpeech? = null

    internal var networkChangeBroadcast: BroadcastReceiver? = null

    /*
    @Override
    protected void onResume() {
        super.onResume();
        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        pref_setting.registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        pref_setting.unregisterOnSharedPreferenceChangeListener(this);
    }
*/


    //TextViewがどうしても欲しかった
    //TextViewを押す→ListViewの一番上に移動←これがしたかった
    /*
    https://stackoverflow.com/questions/31428086/access-toolbar-textview-from-fragment-in-android
    */
    lateinit var toolBer: Toolbar
        internal set
    internal lateinit var navigationView: NavigationView
    internal lateinit var drawer: DrawerLayout

    lateinit var toot_snackbar: Snackbar
        internal set
    internal var newNote_Snackbar: Snackbar? = null

    internal lateinit var pref_setting: SharedPreferences
    internal lateinit var fab: FloatingActionButton
    internal lateinit var media_LinearLayout: LinearLayout
    internal lateinit var post_button: Button
    internal lateinit var toot_area_Button: ImageButton
    internal lateinit var toot_EditText: EditText
    //公開範囲
    internal var toot_area = "public"
    //名前とか
    internal lateinit var snackber_DisplayName: String
    internal var snackber_Name = ""
    internal var Instance: String? = null
    internal var AccessToken: String? = null
    internal lateinit var snackber_Avatar: String
    internal lateinit var snackber_Avatar_notGif: String
    internal lateinit var snackberAccountAvaterImageView: ImageView
    internal lateinit var snackberAccount_TextView: TextView
    internal lateinit var account_menuBuilder: MenuBuilder
    internal lateinit var account_optionsMenu: MenuPopupHelper
    internal lateinit var misskey_account_menuBuilder: MenuBuilder
    internal lateinit var misskey_account_optionMenu: MenuPopupHelper
    internal lateinit var account_LinearLayout: LinearLayout
    internal var misskey_account_LinearLayout: LinearLayout? = null
    internal lateinit var misskey_drive_Button: ImageButton
    //時間指定投稿
    internal lateinit var mastodon_time_post_Button: ImageButton
    internal var post_date: String? = null
    internal var post_time: String? = null
    internal var time_post_Switch: Switch? = null
    internal var isTimePost = false
    internal var isMastodon_time_post: Boolean = false
    //投票
    internal lateinit var mastodon_vote_Button: ImageButton
    internal lateinit var paintPOSTButton: ImageButton
    internal lateinit var toot_Button_LinearLayout: LinearLayout
    internal var isMastodon_vote = false
    internal var isMastodon_vote_layout = false
    internal lateinit var vote_1: EditText
    internal lateinit var vote_2: EditText
    internal lateinit var vote_3: EditText
    internal lateinit var vote_4: EditText
    internal lateinit var vote_time: EditText
    internal lateinit var vote_use_Switch: Switch
    internal lateinit var vote_multi_Switch: Switch
    internal lateinit var vote_hide_Switch: Switch
    //マルチアカウント読み込み用
    internal var multi_account_instance: ArrayList<String>? = null
    internal lateinit var multi_account_access_token: ArrayList<String>
    internal var misskey_multi_account_instance: ArrayList<String>? = null
    internal lateinit var misskey_multi_account_access_token: ArrayList<String>
    //文字数カウント
    internal var tootTextCount = 0
    //カスタム絵文字表示に使う配列
    private var emojis_show = false

    //最後に開いたカスタムメニューを保管（）
    private val lastMenuName = ""

    //DesktomMode用Mastodon Misskey分岐
    private val isDesktoopMisskeyMode = false
    private val misskey_switch: Switch? = null
    private var isDesktop = false

    /*クイック設定を返すやつ*/
    var tlQuickSettingSnackber: TLQuickSettingSnackber? = null
        private set

    private var customMenuLoadSupport: CustomMenuLoadSupport? = null

    //裏機能？
    internal lateinit var shinchokuLayout: ShinchokuLayout

    //画像POST
    internal var count = 0
    internal var media_list = ArrayList<String>()
    internal var media_uri_list: ArrayList<Uri>? = ArrayList()

    //アカウント情報一回一回取ってくるの通信量的にどうなのってことで
    var tootSnackbarCustomMenuName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //設定のプリファレンス
        pref_setting = getDefaultSharedPreferences(this@Home)

        //Wi-Fi接続状況確認
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false)
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)


        //ダークモード処理
        val conf = resources.configuration
        var currecntNightMode = conf.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val darkModeSupport = DarkModeSupport(this)
        currecntNightMode = darkModeSupport.setIsDarkModeSelf(currecntNightMode)
        when (currecntNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> setTheme(R.style.AppTheme_NoActionBar)
            Configuration.UI_MODE_NIGHT_YES -> setTheme(R.style.OLED_Theme)
        }

        setContentView(R.layout.activity_home)

        navigationView = findViewById(R.id.nav_view)
        customMenuLoadSupport = CustomMenuLoadSupport(this, navigationView)

        //アプリ起動カウント
        LunchBonus(this)
        //進捗
        shinchokuLayout = ShinchokuLayout(this)


        //ログイン情報があるか
        //アクセストークンがない場合はログイン画面へ飛ばす
        if (pref_setting.getString("main_token", "") == "" && pref_setting.getString("misskey_instance_list", "") == "") {
            val login = Intent(this, LoginActivity::class.java)
            //login.putExtra("first_applunch", true);
            startActivity(login)
        }

        //SQLite
        if (helper == null) {
            helper = CustomMenuSQLiteHelper(this)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            db!!.disableWriteAheadLogging()
        }

        //起動時の
        FragmentChange(HelloFragment())
        //最後に開いたメニューを開くようにする
        val lastName = pref_setting.getString("custom_menu_last", null)
        //メニュー入れ替え
        navigationView.menu.clear()
        navigationView.inflateMenu(R.menu.custom_menu)
        customMenuLoadSupport!!.loadCustomMenu(null)
        if (lastName != null) {
            try {
                customMenuLoadSupport!!.loadCustomMenu(lastName)
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            }
        }

        //ViewPager
/*
        if (pref_setting.getBoolean("pref_view_pager_mode", false)) {
            //動的にViewPager生成
            val viewPager = ViewPager(this)
            viewPager.id = View.generateViewId()
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            viewPager.layoutParams = layoutParams
            container_container.addView(viewPager, 0)
            //Adapter
            val fragmentPagerAdapter = FragmentPagerAdapter(supportFragmentManager, this)
            viewPager.adapter = fragmentPagerAdapter
        }
*/


        //デスクトップモード時で再生成されないようにする
        val fragment = supportFragmentManager.findFragmentById(R.id.container_container)
        if (fragment != null && fragment is DesktopFragment) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container_container, DesktopFragment(), "desktop")
            fragmentTransaction.commit()
            isDesktop = true
        } else {
            isDesktop = false
        }

        //カスタム絵文字有効/無効
        if (pref_setting.getBoolean("pref_custom_emoji", true)) {
            if (pref_setting.getBoolean("pref_avater_wifi", true)) {
                //WIFIのみ表示有効時
                //ネットワーク未接続時はnullか出る
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        //WIFI
                        emojis_show = true
                    }
                }
            } else {
                //WIFI/MOBILE DATA 関係なく表示
                emojis_show = true
            }
        }


        //アクセストークン
        AccessToken = null
        //インスタンス
        Instance = null

        val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
            Instance = pref_setting.getString("pref_mastodon_instance", "")
        } else {
            AccessToken = pref_setting.getString("main_token", "")
            Instance = pref_setting.getString("main_instance", "")
        }

        //カスタムメニューの場合は追加処理
        if (pref_setting.getBoolean("custom_menu", false)) {
            //メニュー入れ替え
            navigationView.menu.clear()
            navigationView.inflateMenu(R.menu.custom_menu)
            customMenuLoadSupport!!.loadCustomMenu(null)
        }


        //TootSnackBerのコードがクソ長いのでメゾット化
        //Misskey
        //setNewNote_Snackber();
        tootSnackBer()

        setAppBar()


        //共有を受け取る
        val intent = intent
        val action_string = intent.action
        //System.out.println(action_string);
        if (Intent.ACTION_SEND == action_string) {
            val extras = intent.extras
            if (extras != null) {
                //URL
                var text = extras.getCharSequence(Intent.EXTRA_TEXT)
                //タイトル
                val title = extras.getCharSequence(Intent.EXTRA_SUBJECT)
                //画像URI
                val uri = extras.getParcelable<Uri>(Intent.EXTRA_STREAM)
                //EXTRA TEXTにタイトルが含まれているかもしれない？
                //含まれているときは消す
                if (text != null) {
                    if (title != null) {
                        text = text.toString().replace(title.toString(), "")
                        toot_EditText.append(title)
                        toot_EditText.append("\n")
                    }
                    toot_EditText.append(text)
                }
                //画像
                if (uri != null) {
                    media_uri_list!!.add(uri)
                    ImageViewClick()
                }
                //0.5秒後に起動するように
                val timer = Timer(false)
                val task = object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            showTootShortcut()
                            timer.cancel()
                        }
                    }
                }
                timer.schedule(task, 500)
            }
        }

        //App Shortcutから起動
        if (getIntent().getBooleanExtra("toot", false)) {
            //0.5秒後に起動するように
            val timer = Timer(false)
            val task = object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        showTootShortcut()
                        timer.cancel()
                    }
                }
            }
            timer.schedule(task, 500)
        }

        //お絵かき投稿から
        if (intent.getBooleanExtra("paint_data", false)) {
            //画像URI
            val uri = Uri.parse(intent.getStringExtra("paint_uri"))
            //画像配列に追加
            if (uri != null) {
                media_uri_list!!.add(uri)
                ImageViewClick()
                //0.5秒後に起動するように
                val timer = Timer(false)
                val task = object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            showTootShortcut()
                            timer.cancel()
                        }
                    }
                }
                timer.schedule(task, 500)
            }
        }

        /*
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //従来のTootActivityへー
                Intent toot = new Intent(Home.this, TestDragListView.class);
                startActivity(toot);
                return false;
            }
        });
*/


        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        val finalInstance = Instance
        val finalAccessToken = AccessToken


        //どろわーのイメージとか文字とか
        //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.nav_header_home_linearlayout);
        val navHeaderView = navigationView.getHeaderView(0)
        val avater_imageView = navHeaderView.findViewById<ImageView>(R.id.icon_image)
        val header_imageView = navHeaderView.findViewById<ImageView>(R.id.drawer_header)
        //ImageView header_imageView = navHeaderView
        val user_account_textView = navHeaderView.findViewById<TextView>(R.id.drawer_account)
        val user_id_textView = navHeaderView.findViewById<TextView>(R.id.drawer_id)
        if (!CustomMenuTimeLine.isMisskeyMode) {
            val url = "https://$Instance/api/v1/accounts/verify_credentials/?access_token=$AccessToken"
            //作成
            val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()
            //GETリクエスト
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        //Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        //成功時
                        val response_string = response.body()!!.string()
                        try {
                            val jsonObject = JSONObject(response_string)
                            display_name = jsonObject.getString("display_name")
                            user_id = jsonObject.getString("username")
                            user_avater = jsonObject.getString("avatar")
                            user_header = jsonObject.getString("header")
                            account_id = jsonObject.getString("id")
                            toot_count = jsonObject.getString("statuses_count")

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    } else {
                        //失敗時
                        runOnUiThread { Toast.makeText(this@Home, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    }
                }
            })
        }


        /*
        //ローカルタイムライントースト
        if (pref_setting.getInt("timeline_toast_check", 0) == 1) {

            String channel = "Kaisendon_1";
            String channel_1 = "notification_TL";

            //通知
            NotificationManager notificationManager = (NotificationManager) Home.this.getSystemService(Context.NOTIFICATION_SERVICE);

            //Android Oからは通知の仕様が変わった
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(channel, "LocalTimeline Toot", NotificationManager.IMPORTANCE_DEFAULT);

                notificationChannel.setDescription(getString(R.string.notification_LocalTimeline_Toast));
                notificationChannel.setName(getString(R.string.app_name));

                notificationManager.createNotificationChannel(notificationChannel);

                NotificationCompat.Builder notification_nougat = new NotificationCompat.Builder(Home.this, channel)
                        .setSmallIcon(R.drawable.ic_rate_review_white_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_LocalTimeline_Toast));

                Intent resultIntent = new Intent(Home.this, Home.class);

                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(Home.this);

                taskStackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        taskStackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                notification_nougat.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager_nougat.notify(R.string.app_name, notification_nougat.build());


            } else {

                //ブロードキャストを送信
                //ブロードキャスト先を指定（明示的ブロードキャスト）
                //マニフェストにも記入しないと動かないので注意
                Intent notification_Intent = new Intent(getApplicationContext(), BroadcastRecever_Notification_Button.class);

                //ボタンを追加する

                NotificationCompat.Action action = new NotificationCompat.Action(
                        R.drawable.ic_rate_review_black_24dp,
                        getString(R.string.timeline_toast_disable),
                        PendingIntent.getBroadcast(this, 0, notification_Intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );


                NotificationCompat.Builder notification_nougat = new NotificationCompat.Builder(Home.this)
                        .setSmallIcon(R.drawable.ic_rate_review_white_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_LocalTimeline_Toast)
                        );

                notification_nougat.addAction(action);

                Intent resultIntent = new Intent(Home.this, Home.class);

                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(Home.this);

                taskStackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        taskStackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                notification_nougat.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager_nougat.notify(R.string.app_name, notification_nougat.build());

            }

            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                @Override
                protected String doInBackground(String... string) {

                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken)
                            .useStreamingApi()
                            .build();

                    Handler handler = new Handler() {
                        @Override
                        public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {
                            String user_name = status.getAccount().getDisplayName();
                            String toot_text = status.getContent();
                            String user_avater_url = status.getAccount().getAvatar();
                            long toot_id = status.getId();

                            Spanned spanned = Html.fromHtml(toot_text, Html.FROM_HTML_MODE_LEGACY);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    //トースト表示
*/
        /*
                                    Toast toast = new Toast(getApplicationContext());
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.notification_toast_layout, null);
                                    LinearLayout toast_linearLayout = layout.findViewById(R.id.toast_layout_root);
                                    TextView toast_text = layout.findViewById(R.id.notification_text);
                                    toast_text.setText(Html.fromHtml(user_name + "\r\n" + toot_text, Html.FROM_HTML_MODE_COMPACT));
                                    ImageView toast_imageview = layout.findViewById(R.id.notification_icon);
                                    //toast_imageview.setImageDrawable(finalDrawable);
*//*


                                    //ブロードキャストを送信
                                    //ブロードキャスト先を指定（明示的ブロードキャスト）

                                    //途中で変更がある可能性があるので再度確認
                                    if (pref_setting.getInt("timeline_toast_check", 0) == 1) {


                                        NotificationChannel notificationChannel = null;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            notificationChannel = new NotificationChannel(channel_1, "Notification TL", NotificationManager.IMPORTANCE_HIGH);
                                            notificationChannel.setDescription(getString(R.string.notification_timeline));
                                            notificationChannel.setName(getString(R.string.notification_timeline));

                                            notificationManager.createNotificationChannel(notificationChannel);

                                            //トゥートようブロードキャスト
                                            String Type = "Type";

                                            Intent notification_localtimeline_toot = new Intent(getApplicationContext(), BroadcastReceiver_Notification_Timeline.class);
                                            notification_localtimeline_toot.putExtra(Type, "Toot");
                                            PendingIntent notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT);


                                            //マニフェストにも記入しないと動かないので注意
                                            Intent notififation_localtimeline_favouite = new Intent(getApplicationContext(), BroadcastReceiver_Notification_Timeline.class);
                                            notififation_localtimeline_favouite.putExtra(Type, "Favourite");
                                            notififation_localtimeline_favouite.putExtra("ID", toot_id);


                                            long[] pattern = {100};

                                            //通知
                                            NotificationCompat.Builder notification_localtimeline = new NotificationCompat.Builder(Home.this, channel_1)
                                                    .setSmallIcon(R.drawable.ic_rate_review_white_24dp)
                                                    .setContentTitle(user_name)
                                                    .setContentText(spanned)
                                                    .setPriority(2)
                                                    .setVibrate(pattern);

                                            //お気に入りボタン
                                            NotificationCompat.Action notification_favourite_action = new NotificationCompat.Action(
                                                    R.drawable.ic_star_black_24dp,
                                                    getString(R.string.favoutire),
                                                    PendingIntent.getBroadcast(Home.this, 0, notififation_localtimeline_favouite, PendingIntent.FLAG_UPDATE_CURRENT)
                                            );


                                            //トゥート
                                            RemoteInput remoteInput = new RemoteInput.Builder("Toot_Text")
                                                    .setLabel("今なにしてる？")
                                                    .build();

                                            NotificationCompat.Action notification_toot_action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send
                                                    , "今なにしてる？", notification_localtimeline_pendingIntent)
                                                    .addRemoteInput(remoteInput)
                                                    .build();

                                            notification_localtimeline.addAction(notification_toot_action);


                                            notification_localtimeline.addAction(notification_favourite_action);

                                            NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager_nougat.notify(R.string.notification_LocalTimeline_Notification, notification_localtimeline.build());


                                        } else {
                                            //トゥートようブロードキャスト
                                            String Type = "Type";

                                            Intent notification_localtimeline_toot = new Intent(getApplicationContext(), BroadcastReceiver_Notification_Timeline.class);
                                            notification_localtimeline_toot.putExtra(Type, "Toot");
                                            PendingIntent notification_localtimeline_pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notification_localtimeline_toot, PendingIntent.FLAG_UPDATE_CURRENT);


                                            //マニフェストにも記入しないと動かないので注意
                                            Intent notififation_localtimeline_favouite = new Intent(getApplicationContext(), BroadcastReceiver_Notification_Timeline.class);
                                            notififation_localtimeline_favouite.putExtra(Type, "Favourite");
                                            notififation_localtimeline_favouite.putExtra("ID", toot_id);


                                            long[] pattern = {100};

                                            //通知
                                            NotificationCompat.Builder notification_localtimeline = new NotificationCompat.Builder(Home.this)
                                                    .setSmallIcon(R.drawable.ic_rate_review_white_24dp)
                                                    .setContentTitle(user_name)
                                                    .setContentText(spanned)
                                                    .setPriority(1)
                                                    .setVibrate(pattern);

                                            //お気に入りボタン
                                            NotificationCompat.Action notification_favourite_action = new NotificationCompat.Action(
                                                    R.drawable.ic_star_black_24dp,
                                                    getString(R.string.dialog_favorite),
                                                    PendingIntent.getBroadcast(Home.this, 0, notififation_localtimeline_favouite, PendingIntent.FLAG_UPDATE_CURRENT)
                                            );


                                            //トゥート
                                            RemoteInput remoteInput = new RemoteInput.Builder("Toot_Text")
                                                    .setLabel("今なにしてる？")
                                                    .build();

                                            NotificationCompat.Action notification_toot_action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send
                                                    , "今なにしてる？", notification_localtimeline_pendingIntent)
                                                    .addRemoteInput(remoteInput)
                                                    .build();

                                            notification_localtimeline.addAction(notification_toot_action);


                                            notification_localtimeline.addAction(notification_favourite_action);

                                            NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager_nougat.notify(R.string.notification_LocalTimeline_Notification, notification_localtimeline.build());

                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onNotification(@NotNull Notification notification) {

                        }

                        @Override
                        public void onDelete(long l) {

                        }
                    };
                    Streaming streaming = new Streaming(client);
                    try {
                        if (pref_setting.getString("pref_notification_timeline", "Home").contains("Home")) {
                            Shutdownable shutdownable = streaming.user(handler);
                        }
                        if (pref_setting.getString("pref_notification_timeline", "Home").contains("Local")) {
                            Shutdownable shutdownable = streaming.localPublic(handler);
                        }
                        if (pref_setting.getString("pref_notification_timeline", "Home").contains("Federated")) {
                            Shutdownable shutdownable = streaming.federatedPublic(handler);
                        }

                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }


        //読み上げ　TTS
        if (pref_setting.getBoolean("pref_speech", false)) {
            NotificationManager notificationManager = (NotificationManager) Home.this.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel notificationChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel("Kaisendon_1", "LocalTimeline Toot", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription(getString(R.string.notification_timeline));
                notificationChannel.setName(getString(R.string.notification_timeline));

                notificationManager.createNotificationChannel(notificationChannel);

                NotificationCompat.Builder ttsNotification =
                        new NotificationCompat.Builder(this, "Kaisendon_1")
                                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                                .setContentTitle(getString(R.string.speech_timeline))
                                .setContentText(getString(R.string.notification_speech_timeline));
                Intent tts_intent = new Intent(getApplicationContext(), BroadcastRecever_Notification_Button.class);
                tts_intent.putExtra("TTS", true);
                NotificationCompat.Action ttsAction = new NotificationCompat.Action(
                        R.drawable.ic_volume_off_black_24dp,
                        getString(R.string.timeline_toast_disable),
                        PendingIntent.getBroadcast(getApplicationContext(), 0, tts_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );
                ttsNotification.addAction(ttsAction);
                NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager_nougat.notify(R.string.speech_timeline, ttsNotification.build());

            } else {
                NotificationCompat.Builder ttsNotification =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                                .setContentTitle(getString(R.string.speech_timeline))
                                .setContentText(getString(R.string.notification_speech_timeline));
                Intent tts_intent = new Intent(getApplicationContext(), BroadcastRecever_Notification_Button.class);
                tts_intent.putExtra("TTS", true);
                NotificationCompat.Action ttsAction = new NotificationCompat.Action(
                        R.drawable.ic_volume_off_black_24dp,
                        getString(R.string.timeline_toast_disable),
                        PendingIntent.getBroadcast(getApplicationContext(), 0, tts_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                );
                ttsNotification.addAction(ttsAction);
                NotificationManager notificationManager_nougat = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager_nougat.notify(R.string.speech_timeline, ttsNotification.build());
            }

            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getBooleanExtra("TTS", false)) {
                        SharedPreferences.Editor editor = pref_setting.edit();
                        editor.putBoolean("pref_speech", false);
                        editor.apply();
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(R.string.speech_timeline);
                    }
                }
            };

            textToSpeech = new TextToSpeech(Home.this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (TextToSpeech.SUCCESS == status) {
                        //初期化　使えるよ！
                        textToSpeech.setSpeechRate(Float.valueOf(pref_setting.getString("pref_speech_rate", "1.0f")));
                        Toast.makeText(Home.this, R.string.text_to_speech_preparation, Toast.LENGTH_SHORT).show();
                    } else {
                        //使えないよ
                    }
                }
            });

            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... string) {
                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken)
                            .useStreamingApi()
                            .build();
                    Handler handler = new Handler() {
                        @Override
                        public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {


                            String toot = status.getContent();
                            String finaltoot = Html.fromHtml(toot, Html.FROM_HTML_MODE_COMPACT).toString();


                            // 正規表現
                            finaltoot = Html.fromHtml(finaltoot, Html.FROM_HTML_MODE_COMPACT).toString();
                            String final_toot_1 = finaltoot;
                            //URL省略
                            if (pref_setting.getBoolean("pref_speech_url", true)) {
                                final_toot_1 = finaltoot.replaceAll("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", "URL省略");
                                System.out.println(final_toot_1);
                            } else {
                                final_toot_1 = finaltoot;
                            }

                            if (0 < toot.length()) {
                                textToSpeech.speak(final_toot_1, textToSpeech.QUEUE_ADD, null, "messageID");
                            }
                        }

                        @Override
                        public void onNotification(@NotNull Notification notification) {*/
        /* no op *//*
}

                        @Override
                        public void onDelete(long id) {*/
        /* no op *//*
}
                    };

                    Streaming streaming = new Streaming(client);
                    try {
                        if (pref_setting.getString("pref_speech_timeline_type", "Home").contains("Home")) {
                            Shutdownable shutdownable = streaming.user(handler);
                        }
                        if (pref_setting.getString("pref_speech_timeline_type", "Home").contains("Local")) {
                            Shutdownable shutdownable = streaming.localPublic(handler);
                        }
                        if (pref_setting.getString("pref_speech_timeline_type", "Home").contains("Federated")) {
                            Shutdownable shutdownable = streaming.federatedPublic(handler);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
*/
/*
        //スリープ無効？
        val setting_sleep = pref_setting.getBoolean("pref_no_sleep", false)
        if (setting_sleep) {
            //常時点灯
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            //常時点灯しない
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
*/

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val pref_setting = getDefaultSharedPreferences(this@Home)

        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data!!.data
                //System.out.println("値 : " + String.valueOf(resultCode) + " / " + data.getData());

                //ファイルパスとか
                val filePath = getPath(selectedImage)
                val file_extn = filePath.substring(filePath.lastIndexOf(".") + 1)
                val file = File(filePath)
                val finalPath = "file:\\\\$filePath"
                val layoutParams = LinearLayout.LayoutParams(200, 200)

                if (file_extn == "img" || file_extn == "jpg" || file_extn == "jpeg" || file_extn == "gif" || file_extn == "png") {
                    //配列に入れる
                    media_uri_list!!.add(data.data as Uri)
                    media_LinearLayout.removeAllViews()
                    //配列に入れた要素をもとにImageViewを生成する
                    for (i in media_uri_list!!.indices) {
                        val imageView = ImageView(this@Home)
                        imageView.layoutParams = layoutParams
                        imageView.setImageURI(media_uri_list!![i])
                        imageView.tag = i
                        media_LinearLayout.addView(imageView)
                        //押したとき
                        imageView.setOnClickListener {
                            //Toast.makeText(Home.this, "位置 : " + String.valueOf((Integer) imageView.getTag()), Toast.LENGTH_SHORT).show();
                            //要素の削除
                            //media_list.remove(0);
                            //再生成
                            ImageViewClick()
                        }
                    }
                }
            }
    }

    /**
     * context://→file://へ変換する
     * いまはUriをバイト配列にしてるので使ってない（）
     */
    fun getPath(uri: Uri?): String {
        //uri.getLastPathSegment();
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        val column_index = cursor!!
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val imagePath = cursor.getString(column_index)
        cursor.close()
        //Android Q から追加された Scoped Storage に一時的に対応
        //なにそれ→アプリごとにストレージサンドボックスが作られて、今まであったWRITE_EXTERNAL_STORAGEなしで扱える他
        //他のアプリからはアクセスできないようになってる。
        //<I>いやでも今までのfile://スキーマ変換が使えないのはクソクソクソでは</I>
        //今までのやつをAndroid Qで動かすと
        //Q /mnt/content/media ～
        //Pie /storage/emulated/0 ～
        //もう一回かけてようやくfile://スキーマのリンク取れた
        //Android Q
        if (Build.VERSION.CODENAME == "Q") {
            // /mnt/content/が邪魔なので取って、そこにcontent://スキーマをつける
            // Google Photoからしか動かねーわまあPixel以外にもQが配信される頃には情報がわさわさ出てくることでしょう。
            val content_text = imagePath.replace("/mnt/content/", "content://")
            //System.out.println(imagePath);
            //try-catch
            //実機で確認できず
            //imagePath = getPathAndroidQ(Home.this, Uri.parse(content_text));
        }
        //System.out.println(imagePath);
        return imagePath
    }

    private fun ImageViewClick() {
        val layoutParams = LinearLayout.LayoutParams(200, 200)
        media_LinearLayout.removeAllViews()
        //配列に入れた要素をもとにImageViewを生成する
        for (i in media_uri_list!!.indices) {
            val imageView = ImageView(this@Home)
            imageView.layoutParams = layoutParams
            imageView.setImageURI(media_uri_list!![i])
            imageView.tag = i
            media_LinearLayout.addView(imageView)
            //押したとき
            imageView.setOnClickListener {
                Toast.makeText(this@Home, getString(R.string.delete_ok) + " : " + (imageView.tag as Int).toString(), Toast.LENGTH_SHORT).show()
                //要素の削除
                //なんだこのくそｇｍコードは
                //removeにgetTagそのまま書くとなんかだめなんだけど何これ意味不
                if (imageView.tag as Int == 0) {
                    media_uri_list!!.removeAt(0)
                } else if (imageView.tag as Int == 1) {
                    media_uri_list!!.removeAt(1)
                } else if (imageView.tag as Int == 2) {
                    media_uri_list!!.removeAt(2)
                } else if (imageView.tag as Int == 3) {
                    media_uri_list!!.removeAt(3)
                }
                //再生成
                ImageViewClick()
            }
        }
    }

    /**
     * Misskey Driveの画像を表示させる
     */
    private fun setMisskeyDrivePhoto() {
        val layoutParams = LinearLayout.LayoutParams(200, 200)
        media_LinearLayout.removeAllViews()
        //配列に入れた要素をもとにImageViewを生成する
        for (i in misskey_media_url.indices) {
            val imageView = ImageView(this@Home)
            //Glide
            Glide.with(this@Home).load(misskey_media_url[i]).into(imageView)
            imageView.layoutParams = layoutParams
            imageView.tag = i
            media_LinearLayout.addView(imageView)
            //押したとき
            imageView.setOnClickListener {
                Toast.makeText(this@Home, "位置 : " + (imageView.tag as Int).toString(), Toast.LENGTH_SHORT).show()
                //要素の削除
                //なんだこのくそｇｍコードは
                //removeにgetTagそのまま書くとなんかだめなんだけど何これ意味不
                if (imageView.tag as Int == 0) {
                    misskey_media_url.removeAt(0)
                    post_media_id.removeAt(0)
                } else if (imageView.tag as Int == 1) {
                    misskey_media_url.removeAt(1)
                    post_media_id.removeAt(1)
                } else if (imageView.tag as Int == 2) {
                    misskey_media_url.removeAt(2)
                    post_media_id.removeAt(2)
                } else if (imageView.tag as Int == 3) {
                    misskey_media_url.removeAt(3)
                    post_media_id.removeAt(3)
                }
                //再生成
                setMisskeyDrivePhoto()
            }
        }
    }


    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (toot_snackbar.isShown) {
            toot_snackbar.dismiss()
        } else if (tlQuickSettingSnackber?.isShown() ?: false) {
            tlQuickSettingSnackber?.dismissSnackBer()
        } else {
            //　終了ダイアログ 修正（Android Qで動かないので）
            AlertDialog.Builder(this@Home)
                    .setTitle(getString(R.string.close_dialog_title))
                    .setIcon(R.drawable.ic_local_hotel_black_12dp)
                    .setMessage(getString(R.string.close_dialog_message))
                    .setPositiveButton(getString(R.string.close_dialog_ok)) { dialog, which -> finish(); super.onBackPressed() }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
                    .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (pref_setting.getBoolean("pref_bottom_navigation", false)) {
            getMenuInflater().inflate(R.menu.bottom_app_bar_menu, menu);
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        /*
        //noinspection SimplifiableIfStatement
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (id == R.id.home_menu_quick_setting) {
            Bundle bundle = new Bundle();
            bundle.putString("account_id", account_id);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "quick_setting");
        }
*/

        /*
            case R.id.home_menu_login:
                Intent login = new Intent(this, LoginActivity.class);
                startActivity(login);
                break;
            case R.id.home_menu_account_list:
                transaction.replace(R.id.container_container, new AccountListFragment());
                transaction.commit();
                break;
            case R.id.home_menu_account:
                Intent intent = new Intent(this, UserActivity.class);
                if (CustomMenuTimeLine.isMisskeyMode()) {
                    intent.putExtra("Account_ID", CustomMenuTimeLine.getAccount_id());
                } else {
                    intent.putExtra("Account_ID", account_id);
                }
                intent.putExtra("my", true);
                startActivity(intent);
                break;
            case R.id.home_menu_desktop_mode:
                DesktopFragment desktopFragment = new DesktopFragment();
                transaction.replace(R.id.container_container, new DesktopFragment(), "desktop");
                transaction.commit();
                break;
            case R.id.home_menu_setting:
                transaction.replace(R.id.container_container, new SettingFragment());
                transaction.commit();
                break;
            case R.id.home_menu_license:
                transaction.replace(R.id.container_container, new License_Fragment());
                transaction.commit();
                break;
            case R.id.home_menu_thisapp:
                Intent thisApp = new Intent(this, KonoAppNiTuite.class);
                startActivity(thisApp);
                break;
            case R.id.home_menu_privacy_policy:
                showPrivacyPolicy();
                break;
            case R.id.home_menu_wear:
                transaction.replace(R.id.container_container, new WearFragment());
                transaction.commit();
                break;
            case R.id.home_menu_bookmark:
                transaction.replace(R.id.container_container, new Bookmark_Frament());
                transaction.commit();
                break;
            case R.id.home_menu_activity_pub_viewer:
                transaction.replace(R.id.container_container, new ActivityPubViewer());
                transaction.commit();
                break;
            case R.id.home_menu_reload_menu:
                //再読み込み
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.custom_menu);
                customMenuLoadSupport.loadCustomMenu(null);
                break;
            case R.id.home_menu_old_drawer:
                navigationView.inflateMenu(R.menu.activity_home_drawer);
                break;
            case R.id.home_menu_flowlt:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_container);
                if (fragment != null && fragment instanceof CustomMenuTimeLine) {
                    FloatingTL floatingTL = new FloatingTL(this, fragment.getArguments().getString("json"));
                    floatingTL.setNotification();
                }
                break;
            case R.id.home_menu_quick_setting:
                Bundle bundle = new Bundle();
                bundle.putString("account_id", account_id);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "quick_setting");
                break;
            case R.id.home_menu_dark:
                //これはAndroid Qを搭載しない端末向け設定
                if (Build.VERSION.SDK_INT <= 28 && !Build.VERSION.CODENAME.equals("Q")) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    if (pref_setting.getBoolean("darkmode", false)) {
                        editor.putBoolean("darkmode", false);
                        editor.apply();
                    } else {
                        editor.putBoolean("darkmode", true);
                        editor.apply();
                    }
                    //Activity再起動
                    startActivity(new Intent(this, Home.class));
                } else {
                    Toast.makeText(this, getString(R.string.darkmode_error_os), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        if (id == R.id.action_settings) {
            return true;
        }
*/

        return super.onOptionsItemSelected(item)
    }

    /**
     * プライバシーポリシー
     */
    private fun showPrivacyPolicy() {
        val githubUrl = "https://github.com/takusan23/Kaisendon/blob/master/kaisendon-privacy-policy.md"
        if (pref_setting.getBoolean("pref_chrome_custom_tabs", true)) {
            val back_icon = BitmapFactory.decodeResource(this@Home.resources, R.drawable.ic_action_arrow_back)
            val custom = CustomTabsHelper.getPackageNameToUse(this@Home)
            val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(custom)
            customTabsIntent.launchUrl(this@Home, Uri.parse(githubUrl))
        } else {
            val uri = Uri.parse(githubUrl)
            val browser = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browser)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        val pref_setting = getDefaultSharedPreferences(this)
        if (id == R.id.custom_menu_mode_menu) {
            //モード切替
            val navigationView = findViewById<NavigationView>(R.id.nav_view)
            navigationView.menu.clear()
            val editor = pref_setting.edit()
            if (pref_setting.getBoolean("custom_menu", false)) {
                editor.putBoolean("custom_menu", false)
                //メニュー切り替え
                navigationView.inflateMenu(R.menu.activity_home_drawer)
            } else {
                editor.putBoolean("custom_menu", true)
                //メニュー切り替え
                navigationView.inflateMenu(R.menu.custom_menu)
                //適用処理
                customMenuLoadSupport!!.loadCustomMenu(null)
            }
            editor.apply()
        } else if (id == R.id.custom_menu_setting_menu) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container_container, CustomMenuSettingFragment())
            transaction.commit()
        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


/*
    //終了しますか？
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_local_hotel_black_12dp)
                    .setTitle("終了確認")
                    .setMessage("アプリを終了しますか？")
                    .setNegativeButton("いいえ", null)
                    .setPositiveButton("はい"

                    ) { dialog, which -> finish() }
                    .show()
        }
        return super.onKeyDown(keyCode, event)
    }
*/

    fun FragmentChange(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_container, fragment)
        transaction.commit()
    }


    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref_setting = getDefaultSharedPreferences(this)
        val editor = pref_setting.edit()
        editor.putBoolean("app_multipain_ui", false)
        editor.apply()
        if (pref_setting.getBoolean("pref_speech", false)) {
            textToSpeech!!.shutdown()
        }
        //レジーバー解除
        if (pref_setting.getBoolean("pref_networkchange", false)) {
            if (networkChangeBroadcast != null) {
                unregisterReceiver(networkChangeBroadcast)
            }
        }
    }


    @SuppressLint("RestrictedApi")
    fun tootSnackBer() {
        //画像ID配列
        post_media_id = ArrayList()
        misskey_media_url = ArrayList()

        val AccessToken = pref_setting.getString("main_token", "")
        val Instance = pref_setting.getString("main_instance", "")

        val view = this@Home.findViewById<View>(R.id.container_public)
        toot_snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)
        toot_snackbar.setBackgroundTint(Color.parseColor("#4c4c4c"))
        //Snackber生成
        val snackBer_viewGrop = toot_snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        //LinearLayout動的に生成
        val snackber_LinearLayout = LinearLayout(this@Home)
        snackber_LinearLayout.orientation = LinearLayout.VERTICAL
        val warp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        snackber_LinearLayout.layoutParams = warp
        //スワイプで消せないようにする
        toot_snackbar.view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                toot_snackbar.view.viewTreeObserver.removeOnPreDrawListener(this)
                (toot_snackbar.view.layoutParams as CoordinatorLayout.LayoutParams).behavior = null
                return true
            }
        })

        //押したアニメーション
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)

        //テキストボックス
        //Materialふうに
        val toot_textBox_LinearLayout = LinearLayout(this@Home)
        //レイアウト読み込み
        layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout)
        toot_EditText = layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_editText)
        //ヒント
        (layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById<View>(R.id.name_TextInputLayout) as TextInputLayout).hint = getString(R.string.imananisiteru)
        //色
        (layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById<View>(R.id.name_TextInputLayout) as TextInputLayout).defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))
        (layoutInflater.inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById<View>(R.id.name_TextInputLayout) as TextInputLayout).boxStrokeColor = Color.parseColor("#ffffff")
        toot_EditText.setTextColor(Color.parseColor("#ffffff"))
        toot_EditText.setHintTextColor(Color.parseColor("#ffffff"))
        //ボタン追加用LinearLayout
        toot_Button_LinearLayout = LinearLayout(this@Home)
        toot_Button_LinearLayout.orientation = LinearLayout.HORIZONTAL
        toot_Button_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        //Button
        //画像追加
        val add_image_Button = ImageButton(this@Home)
        add_image_Button.setPadding(20, 20, 20, 20)
        add_image_Button.setBackgroundResource(typedValue.resourceId)
        add_image_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        add_image_Button.setImageDrawable(getDrawable(R.drawable.ic_image_black_24dp))
        setToolTipText(add_image_Button, getString(R.string.image_attachment))
        add_image_Button.setOnClickListener {
            //キーボード隠す
            closeKeyboard()
            val REQUEST_PERMISSION = 1000
            //ストレージ読み込みの権限があるか確認
            //許可してないときは許可を求める
            if (ContextCompat.checkSelfPermission(this@Home, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this@Home)
                        .setTitle(getString(R.string.permission_dialog_titile))
                        .setMessage(getString(R.string.image_upload_storage_permisson))
                        .setPositiveButton(getString(R.string.permission_ok)) { dialog, which ->
                            //権限をリクエストする
                            requestPermissions(
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    REQUEST_PERMISSION)
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
            } else {
                //onActivityResultで受け取れる
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, 1)
            }
        }

        //公開範囲選択用Button
        toot_area_Button = ImageButton(this@Home)
        toot_area_Button.setPadding(20, 20, 20, 20)
        toot_area_Button.setBackgroundResource(typedValue.resourceId)
        toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp))
        toot_area_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        setToolTipText(toot_area_Button, getString(R.string.visibility))
        //toot_area_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0);
        //メニューセット
        if (CustomMenuTimeLine.isMisskeyMode) {
            setMisskeyVisibilityMenu(toot_area_Button)
        } else {
            setMastodonVisibilityMenu(toot_area_Button)
        }

        //投稿用LinearLayout
        val toot_LinearLayout = LinearLayout(this@Home)
        toot_LinearLayout.orientation = LinearLayout.HORIZONTAL
        val toot_button_LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        toot_button_LayoutParams.gravity = Gravity.RIGHT
        toot_LinearLayout.layoutParams = toot_button_LayoutParams

        //投稿用Button
        post_button = Button(this@Home, null, 0, R.style.Widget_AppCompat_Button_Borderless)
        post_button.text = tootTextCount.toString() + "/" + "500 " + getString(R.string.toot_text)
        post_button.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        post_button.setTextColor(Color.parseColor("#ffffff"))
        post_button.setBackgroundResource(typedValue.resourceId)
        post_button.setPadding(50, 0, 50, 0)
        val toot_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_create_black_24dp, null)
        post_button.setCompoundDrawablesWithIntrinsicBounds(toot_icon, null, null, null)
        //POST statuses
        val finalAccessToken = AccessToken
        val finalInstance = Instance
        post_button.setOnClickListener { v ->

            //感触フィードバックをつける？
            if (pref_setting.getBoolean("pref_post_haptics", false)) {
                post_button.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }

            //クローズでソフトキーボード非表示
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm != null) {
                if (this@Home.currentFocus != null) {
                    imm.hideSoftInputFromWindow(this@Home.currentFocus!!.windowToken, 0)
                }
            }
            //画像添付なしのときはここを利用して、
            //画像添付トゥートは別に書くよ
            if (media_uri_list!!.isEmpty()) {
                //テキスト0文字で投稿できないようにする
                if (toot_EditText.text.isNotEmpty()) {
                    //FABのアイコン戻す
                    fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp))
                    //時間指定投稿（予約投稿）を送信するね！メッセージ
                    val message: String
                    if (isTimePost) {
                        message = getString(R.string.time_post_post_button)
                    } else {
                        message = getString(R.string.note_create_message)
                    }
                    //Tootする
                    //確認SnackBer
                    Snackbar.make(v, message, Snackbar.LENGTH_SHORT).setAction(R.string.toot_text) {
                        //FABのアイコン戻す
                        fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp))
                        //Mastodon / Misskey
                        if (CustomMenuTimeLine.isMisskeyMode) {
                            misskeyNoteCreatePOST()
                        } else {
                            mastodonStatusesPOST()
                        }
                    }.show()
                } else {
                    Toast.makeText(this@Home, getString(R.string.toot_error_empty), Toast.LENGTH_SHORT).show()
                }
            } else {
                //画像投稿する
                for (i in media_uri_list!!.indices) {
                    //ひつようなやつ
                    val uri = media_uri_list!![i]
                    if (CustomMenuTimeLine.isMisskeyMode) {
                        uploadDrivePhoto(uri)
                    } else {
                        uploadMastodonPhoto(uri)
                    }
                }
            }
        }

        //端末情報とぅーと
        val device_Button = ImageButton(this@Home)
        device_Button.setPadding(20, 20, 20, 20)
        device_Button.setBackgroundResource(typedValue.resourceId)
        device_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        device_Button.setImageDrawable(getDrawable(R.drawable.ic_perm_device_information_black_24dp))
        setToolTipText(device_Button, getString(R.string.device_info))
        //ポップアップメニュー作成
        val device_menuBuilder = MenuBuilder(this@Home)
        val device_inflater = MenuInflater(this@Home)
        device_inflater.inflate(R.menu.device_info_menu, device_menuBuilder)
        val device_optionsMenu = MenuPopupHelper(this@Home, device_menuBuilder, device_Button)
        device_optionsMenu.setForceShowIcon(true)
        //コードネーム変換（手動
        var codeName = ""
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            codeName = "Nougat"
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            codeName = "Oreo"
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            codeName = "Pie"
        }
        val finalCodeName = codeName
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        device_Button.setOnClickListener {
            device_optionsMenu.show()
            device_menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                    //名前
                    if (menuItem.title.toString().contains(getString(R.string.device_name))) {
                        toot_EditText.append(Build.MODEL)
                        toot_EditText.append("\r\n")
                    }
                    //Androidバージョン
                    if (menuItem.title.toString().contains(getString(R.string.android_version))) {
                        toot_EditText.append(Build.VERSION.RELEASE)
                        toot_EditText.append("\r\n")
                    }
                    //めーかー
                    if (menuItem.title.toString().contains(getString(R.string.maker))) {
                        toot_EditText.append(Build.BRAND)
                        toot_EditText.append("\r\n")
                    }
                    //SDKバージョン
                    if (menuItem.title.toString().contains(getString(R.string.sdk_version))) {
                        toot_EditText.append(Build.VERSION.SDK_INT.toString())
                        toot_EditText.append("\r\n")
                    }
                    //コードネーム
                    if (menuItem.title.toString().contains(getString(R.string.codename))) {
                        toot_EditText.append(finalCodeName)
                        toot_EditText.append("\r\n")
                    }
                    //バッテリーレベル
                    if (menuItem.title.toString().contains(getString(R.string.battery_level))) {
                        toot_EditText.append(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toString() + "%")
                        toot_EditText.append("\r\n")
                    }
                    return false
                }

                override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                }
            })
        }

        //Misskey Driveボタン
        misskey_drive_Button = ImageButton(this@Home)
        //misskey_drive_Button.setBackground(getDrawable(R.drawable.button_clear));
        misskey_drive_Button.setPadding(20, 20, 20, 20)
        misskey_drive_Button.setBackgroundColor(Color.parseColor("#00000000"))
        misskey_drive_Button.setImageDrawable(getDrawable(R.drawable.ic_cloud_queue_white_24dp))
        misskey_drive_Button.setOnClickListener {
            //Misskey Drive API を叩く
            val dialogFragment = MisskeyDriveBottomDialog()
            dialogFragment.show(supportFragmentManager, "misskey_drive_dialog")
            setMisskeyDrivePhoto()
        }

        //時間投稿ボタン
        //高さ調整
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val mastodon_time_post_LinearLayout = LinearLayout(this@Home)
        layoutInflater.inflate(R.layout.mastodon_time_post_layout, mastodon_time_post_LinearLayout)
        mastodon_time_post_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mastodon_time_post_Button = ImageButton(this@Home)
        mastodon_time_post_Button.setPadding(20, 20, 20, 20)
        mastodon_time_post_Button.setBackgroundResource(typedValue.resourceId)
        mastodon_time_post_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        mastodon_time_post_Button.setImageDrawable(getDrawable(R.drawable.ic_timer_black_24dp))
        setToolTipText(mastodon_time_post_Button, getString(R.string.scheduled_statuses_name))
        mastodon_time_post_Button.setOnClickListener {
            //2番めに出す
            if (!isMastodon_time_post) {
                isMastodon_time_post = true
                snackber_LinearLayout.addView(mastodon_time_post_LinearLayout, 2)
                //設定ボタン等
                val day_setting_Button = snackber_LinearLayout.findViewById<Button>(R.id.time_post_button)
                time_post_Switch = snackber_LinearLayout.findViewById(R.id.time_post_switch)
                val date_TextView = snackber_LinearLayout.findViewById<TextView>(R.id.time_post_textview)
                val time_setting_Button = snackber_LinearLayout.findViewById<Button>(R.id.time_post_time_button)
                val time_TextView = snackber_LinearLayout.findViewById<TextView>(R.id.time_post_time_textview)
                //日付設定画面
                day_setting_Button.setOnClickListener { showDatePicker(date_TextView) }
                //時間設定画面
                time_setting_Button.setOnClickListener { showTimePicker(time_TextView) }
                //有効無効
                time_post_Switch!!.setOnCheckedChangeListener { buttonView, isChecked ->
                    //入れておく
                    isTimePost = isChecked
                    //色を変えとく？
                    if (isChecked) {
                        post_button.text = getString(R.string.time_post_post_button)
                        mastodon_time_post_Button.setColorFilter(Color.parseColor("#0069c0"), PorterDuff.Mode.SRC_IN)
                    } else {
                        post_button.text = getString(R.string.toot_text)
                        mastodon_time_post_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                    }
                }

            } else {
                //消す
                isMastodon_time_post = false
                snackber_LinearLayout.removeView(mastodon_time_post_LinearLayout)
            }
        }

        //投票
        //ここから
        val vote_LinearLayout = LinearLayout(this@Home)
        layoutInflater.inflate(R.layout.toot_vote_layout, vote_LinearLayout)
        vote_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size.y / 2)
        mastodon_vote_Button = ImageButton(this@Home)
        mastodon_vote_Button.setPadding(20, 20, 20, 20)
        mastodon_vote_Button.setBackgroundResource(typedValue.resourceId)
        mastodon_vote_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        mastodon_vote_Button.setImageDrawable(getDrawable(R.drawable.ic_baseline_how_to_vote_24px))
        setToolTipText(mastodon_vote_Button, getString(R.string.polls))
        mastodon_vote_Button.setOnClickListener {
            if (!isMastodon_vote_layout) {
                isMastodon_vote_layout = true
                snackber_LinearLayout.addView(vote_LinearLayout, 2)
                vote_use_Switch = snackber_LinearLayout.findViewById(R.id.vote_use_switch)
                vote_multi_Switch = snackber_LinearLayout.findViewById(R.id.vote_multi_switch)
                vote_hide_Switch = snackber_LinearLayout.findViewById(R.id.vote_hide_switch)
                vote_1 = snackber_LinearLayout.findViewById(R.id.vote_editText_1)
                vote_2 = snackber_LinearLayout.findViewById(R.id.vote_editText_2)
                vote_3 = snackber_LinearLayout.findViewById(R.id.vote_editText_3)
                vote_4 = snackber_LinearLayout.findViewById(R.id.vote_editText_4)
                vote_time = snackber_LinearLayout.findViewById(R.id.vote_editText_time)
                (snackber_LinearLayout.findViewById<View>(R.id.vote_textInputLayout_1) as TextInputLayout).defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                (snackber_LinearLayout.findViewById<View>(R.id.vote_textInputLayout_2) as TextInputLayout).defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                (snackber_LinearLayout.findViewById<View>(R.id.vote_textInputLayout_3) as TextInputLayout).defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                (snackber_LinearLayout.findViewById<View>(R.id.vote_textInputLayout_4) as TextInputLayout).defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                (snackber_LinearLayout.findViewById<View>(R.id.vote_textInputLayout_time) as TextInputLayout).defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                //有効無効
                vote_use_Switch.setOnCheckedChangeListener { buttonView, isChecked ->
                    //入れておく
                    isMastodon_vote = isChecked
                    //色を変えとく？
                    if (isChecked) {
                        mastodon_vote_Button.setColorFilter(Color.parseColor("#0069c0"), PorterDuff.Mode.SRC_IN)
                    } else {
                        mastodon_vote_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                    }
                }
            } else {
                isMastodon_vote_layout = false
                snackber_LinearLayout.removeView(vote_LinearLayout)
            }
        }

        //コマンド実行ボタン
        val command_Button = Button(this@Home, null, 0, R.style.Widget_AppCompat_Button_Borderless)
        command_Button.setText(R.string.command_run)
        command_Button.setTextColor(Color.parseColor("#ffffff"))
        //EditTextを監視する
        toot_EditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //コマンド実行メゾット？
                //CommandCode.commandSet(Home.this, toot_EditText, toot_LinearLayout, command_Button, "/sushi", "command_sushi");
                //CommandCode.commandSet(Home.this, toot_EditText, toot_LinearLayout, command_Button, "/friends.nico", "pref_friends_nico_mode");
                CommandCode.commandSetNotPreference(this@Home, this@Home, toot_EditText, toot_LinearLayout, command_Button, "/rate-limit", "rate-limit")
                CommandCode.commandSetNotPreference(this@Home, this@Home, toot_EditText, toot_LinearLayout, command_Button, "/fav-home", "home")
                CommandCode.commandSetNotPreference(this@Home, this@Home, toot_EditText, toot_LinearLayout, command_Button, "/fav-local", "local")
                CommandCode.commandSetNotPreference(this@Home, this@Home, toot_EditText, toot_LinearLayout, command_Button, "/じゃんけん", "じゃんけん")
                /*
                CommandCode.commandSetNotPreference(Home.this, Home.this, toot_EditText, toot_LinearLayout, command_Button, "/progress", "progress");
                CommandCode.commandSetNotPreference(Home.this, Home.this, toot_EditText, toot_LinearLayout, command_Button, "/lunch_bonus", "lunch_bonus");
*/
                //CommandCode.commandSetNotPreference(Home.this, Home.this, toot_EditText, toot_LinearLayout, command_Button, "/life", "life");
                //カウント
                tootTextCount = toot_EditText.text.toString().length
                //投稿ボタンの文字
                val buttonText: String
                if (isTimePost) {
                    buttonText = getString(R.string.time_post_post_button)
                } else {
                    buttonText = getString(R.string.toot_text)
                }
                post_button.text = "$tootTextCount/500 $buttonText"
            }

            override fun afterTextChanged(s: Editable) {

            }
        })


        //お絵かき投稿機能？
        paintPOSTButton = ImageButton(this@Home)
        paintPOSTButton.setPadding(20, 20, 20, 20)
        paintPOSTButton.setBackgroundResource(typedValue.resourceId)
        paintPOSTButton.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
        paintPOSTButton.setImageDrawable(getDrawable(R.drawable.ic_gesture_black_24dp))
        setToolTipText(paintPOSTButton, getString(R.string.polls))
        paintPOSTButton.setOnClickListener {
            //キーボード隠す
            closeKeyboard()
            //開発中メッセージ
            val dialog = AlertDialog.Builder(this@Home)
                    .setTitle(getString(R.string.paintPost))
                    .setMessage(getString(R.string.paint_post_description))
                    .setPositiveButton(getString(R.string.open_painit_post)) { dialogInterface, i ->
                        //お絵かきアクティビティへ移動
                        val intent = Intent(this@Home, PaintPOSTActivity::class.java)
                        startActivity(intent)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            //https://stackoverflow.com/questions/9467026/changing-position-of-the-dialog-on-screen-android
            val window = dialog.window
            val layoutParams = window?.attributes
            layoutParams?.gravity = Gravity.BOTTOM
            layoutParams?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            window?.attributes = layoutParams
        }


        //アカウント切り替えとか
        account_LinearLayout = LinearLayout(this)
        account_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val center_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        center_layoutParams.gravity = Gravity.CENTER
        //ImageView
        snackberAccountAvaterImageView = ImageView(this)
        snackberAccountAvaterImageView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        snackberAccountAvaterImageView.layoutParams = center_layoutParams
        //TextView
        snackberAccount_TextView = TextView(this)
        snackberAccount_TextView.textSize = 14f
        snackberAccount_TextView.setTextColor(Color.parseColor("#ffffff"))
        snackberAccount_TextView.layoutParams = center_layoutParams
        //アカウント情報を取得するところにテキスト設定とか書いたで
        if (CustomMenuTimeLine.isMisskeyMode) {
            getMisskeyAccount()
        } else {
            getAccount()
        }
        //アカウント切り替えポップアップ
        //ポップアップメニューを展開する
        account_menuBuilder = MenuBuilder(this)
        account_optionsMenu = MenuPopupHelper(this, account_menuBuilder, account_LinearLayout)
        misskey_account_menuBuilder = MenuBuilder(this)
        misskey_account_optionMenu = MenuPopupHelper(this, misskey_account_menuBuilder, account_LinearLayout)
        //マルチアカウント読み込み
        //押したときの処理とかもこっち
        //カスタムメニュー時は無効（）

        //LinearLayoutに入れる
        account_LinearLayout.setPadding(10, 10, 10, 10)
        account_LinearLayout.addView(snackberAccountAvaterImageView)
        account_LinearLayout.addView(snackberAccount_TextView)


        //画像追加用LinearLayout
        media_LinearLayout = LinearLayout(this@Home)
        media_LinearLayout.orientation = LinearLayout.HORIZONTAL
        media_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        //LinearLayoutに追加
        //メイン

        snackber_LinearLayout.addView(account_LinearLayout)
        snackber_LinearLayout.addView(toot_textBox_LinearLayout)
        snackber_LinearLayout.addView(toot_Button_LinearLayout)
        snackber_LinearLayout.addView(media_LinearLayout)
        snackber_LinearLayout.addView(toot_LinearLayout)

        if (pref_setting.getBoolean("life_mode", false)) {
            val linearLayout = shinchokuLayout.layout
            snackber_LinearLayout.addView(linearLayout, 1)
        }

        //ボタン追加
        toot_Button_LinearLayout.addView(add_image_Button)
        toot_Button_LinearLayout.addView(toot_area_Button)
        toot_Button_LinearLayout.addView(device_Button)

        //Toot LinearLayout
        toot_LinearLayout.addView(post_button)

        //SnackBerに追加
        snackBer_viewGrop.addView(snackber_LinearLayout)
    }

    fun setToolTipText(view: View, string: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.tooltipText = string
        }
    }

    fun closeKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (this@Home.currentFocus != null) {
            imm.hideSoftInputFromWindow(this@Home.currentFocus!!.windowToken, 0)
        }
    }


    //自分の情報を手に入れる
    private fun getAccount() {
        //Wi-Fi接続状況確認
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false)
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)
        val AccessToken = pref_setting.getString("main_token", "")
        val Instance = pref_setting.getString("main_instance", "")

        val glideSupport = GlideSupport()

        val url = "https://$Instance/api/v1/accounts/verify_credentials/?access_token=$AccessToken"
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()

        //GETリクエスト
        val client_1 = OkHttpClient()
        client_1.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                try {
                    val jsonObject = JSONObject(response_string)
                    val display_name = jsonObject.getString("display_name")
                    val user_id = jsonObject.getString("acct")
                    snackber_DisplayName = display_name
                    toot_count = jsonObject.getString("statuses_count")
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
                            if (snackber_DisplayName.contains(emoji_name)) {
                                //あったよ
                                snackber_DisplayName = snackber_DisplayName.replace(":$emoji_name:", custom_emoji_src)
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
                                if (snackber_DisplayName.contains(emoji_name)) {
                                    //あったよ
                                    snackber_DisplayName = snackber_DisplayName.replace(":$emoji_name:", custom_emoji_src)
                                }
                            }
                        }
                    }
                    snackber_Name = "@$user_id@$Instance"
                    snackber_Avatar = jsonObject.getString("avatar")
                    snackber_Avatar_notGif = jsonObject.getString("avatar_static")
                    //UIスレッド
                    runOnUiThread {
                        //画像サイズ
                        val layoutParams = LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.MATCH_PARENT)
                        snackberAccountAvaterImageView.layoutParams = layoutParams
                        //画像を入れる
                        //表示設定
                        if (setting_avater_hidden) {
                            snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp)
                            snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                        }
                        //GIF再生するか
                        var url = snackber_Avatar
                        if (setting_avater_gif) {
                            //再生しない
                            url = snackber_Avatar_notGif
                        }
                        //読み込む
                        if (setting_avater_wifi && networkCapabilities != null) {
                            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                //角丸設定込み
                                glideSupport.loadGlide(url, snackberAccountAvaterImageView)
                            } else {
                                //キャッシュで読み込む
                                glideSupport.loadGlideReadFromCache(url, snackberAccountAvaterImageView)
                            }
                        } else {
                            //キャッシュで読み込む
                            glideSupport.loadGlideReadFromCache(url, snackberAccountAvaterImageView)
                        }
                        //テキストビューに入れる
                        val imageGetter = PicassoImageGetter(snackberAccount_TextView)
                        snackberAccount_TextView.text = Html.fromHtml(snackber_DisplayName, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
                        snackberAccount_TextView.append("\n" + snackber_Name)
                        //裏機能？
                        shinchokuLayout.setStatusProgress(toot_count)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun showMultiAccount() {
        //押したときの処理
        account_LinearLayout.setOnClickListener {
            //そもそも呼ばれてない説
            if (multi_account_instance == null) {
                //一度だけ取得する
                readMultiAccount()
            } else {
                //追加中に押したら落ちるから回避
                if (account_menuBuilder.size() == multi_account_instance!!.size) {
                    account_optionsMenu.show()
                    account_menuBuilder.setCallback(object : MenuBuilder.Callback {
                        override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                            //ItemIdにマルチアカウントのカウントを入れている
                            val position = menuItem.itemId
                            val multi_instance = multi_account_instance!![position]
                            val multi_access_token = multi_account_access_token[position]
                            AccessToken = multi_access_token
                            Instance = multi_instance
                            val editor = pref_setting.edit()
                            editor.putString("main_instance", multi_instance)
                            editor.putString("main_token", multi_access_token)
                            editor.apply()
                            //アカウント情報更新
                            getAccount()
                            return false
                        }

                        override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                        }
                    })
                } else {
                    Toast.makeText(this@Home, R.string.loading, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showMisskeyMultiAccount() {
        //押したときの処理
        account_LinearLayout.setOnClickListener {
            //そもそも呼ばれてない説
            if (misskey_multi_account_instance == null) {
                //一度だけ取得する
                readMisskeyMultiAccount()
            } else {
                //追加中に押したら落ちるから回避
                if (misskey_account_menuBuilder.size() == misskey_multi_account_instance!!.size) {
                    misskey_account_optionMenu.show()
                    misskey_account_menuBuilder.setCallback(object : MenuBuilder.Callback {
                        override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                            //ItemIdにマルチアカウントのカウントを入れている
                            val position = menuItem.itemId
                            val multi_instance = misskey_multi_account_instance!![position]
                            val multi_access_token = misskey_multi_account_instance!![position]
                            AccessToken = multi_access_token
                            Instance = multi_instance
                            val editor = pref_setting.edit()
                            editor.putString("misskey_main_instance", multi_instance)
                            editor.putString("misskey_main_token", multi_access_token)
                            editor.apply()
                            //アカウント情報更新
                            getMisskeyAccount()
                            return false
                        }

                        override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                        }
                    })
                } else {
                    Toast.makeText(this@Home, R.string.loading, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun readMultiAccount() {
        multi_account_instance = ArrayList()
        multi_account_access_token = ArrayList()
        misskey_multi_account_instance = ArrayList()
        misskey_multi_account_access_token = ArrayList()
        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting.getString("instance_list", "")
        val account_instance_string = pref_setting.getString("access_list", "")
        val misskey_instance_instance_string = pref_setting.getString("misskey_instance_list", "")
        val misskey_account_instance_string = pref_setting.getString("misskey_access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance!!.add(instance_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        if (misskey_instance_instance_string != "") {
            try {
                val instance_array = JSONArray(misskey_account_instance_string)
                val access_array = JSONArray(misskey_account_instance_string)
                for (i in 0 until instance_array.length()) {
                    misskey_multi_account_access_token.add(access_array.getString(i))
                    misskey_multi_account_instance!!.add(instance_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        if (multi_account_instance!!.size >= 1) {
            for (count in multi_account_instance!!.indices) {
                val multi_instance = multi_account_instance!![count]
                val multi_access_token = multi_account_access_token[count]
                val finalCount = count
                //GetAccount
                val url = "https://$multi_instance/api/v1/accounts/verify_credentials/?access_token=$multi_access_token"
                //作成
                val request = Request.Builder()
                        .url(url)
                        .get()
                        .build()

                //GETリクエスト
                val client_1 = OkHttpClient()
                val finalInstance = Instance
                client_1.newCall(request).enqueue(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val response_string = response.body()!!.string()
                        try {
                            val jsonObject = JSONObject(response_string)
                            val display_name = jsonObject.getString("display_name")
                            val user_id = jsonObject.getString("acct")
                            //スナックバー更新
                            snackber_Name = "@$user_id@$finalInstance"
                            snackber_Avatar = jsonObject.getString("avatar")
                            account_menuBuilder.add(0, finalCount, 0, "$display_name($user_id / $multi_instance)")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                })
            }
        }
    }


    /**
     * マルチアカウント読み込み　Misskey
     */
    @SuppressLint("RestrictedApi")
    private fun readMisskeyMultiAccount() {
        misskey_multi_account_instance = ArrayList()
        misskey_multi_account_access_token = ArrayList()
        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting.getString("misskey_instance_list", "")
        val account_instance_string = pref_setting.getString("misskey_access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    misskey_multi_account_instance!!.add(instance_array.getString(i))
                    misskey_multi_account_access_token.add(access_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        if (misskey_multi_account_instance!!.size >= 1) {
            for (count in misskey_multi_account_instance!!.indices) {
                val multi_instance = misskey_multi_account_instance!![count]
                val multi_access_token = misskey_multi_account_access_token[count]
                val finalCount = count
                //GetAccount
                val url = "https://$multi_instance/api/i"
                //JSON
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("i", multi_access_token)
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
                val client_1 = OkHttpClient()
                client_1.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val response_string = response.body()!!.string()
                        try {
                            val jsonObject = JSONObject(response_string)
                            val display_name = jsonObject.getString("name")
                            val user_id = jsonObject.getString("username")
                            misskey_account_menuBuilder.add(0, finalCount, 0, "$display_name($user_id / $multi_instance)")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                })
            }
        }
    }


    //自分の情報を手に入れる Misskey版
    private fun getMisskeyAccount() {
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        //Wi-Fi接続状況確認
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        //通信量節約
        val setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false)
        //Wi-Fi接続時は有効？
        val setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true)
        //GIFを再生するか？
        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)
        val url = "https://$instance/api/users/show"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("username", username)
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
        val client_1 = OkHttpClient()
        val finalInstance = Instance
        client_1.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                try {
                    val jsonObject = JSONObject(response_string)
                    val display_name = jsonObject.getString("name")
                    toot_count = jsonObject.getString("notesCount")
                    snackber_DisplayName = display_name
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
                            if (snackber_DisplayName.contains(emoji_name)) {
                                //あったよ
                                snackber_DisplayName = snackber_DisplayName.replace(":$emoji_name:", custom_emoji_src)
                            }
                        }
                        if (!jsonObject.isNull("profile_emojis")) {
                            val profile_emojis = jsonObject.getJSONArray("profile_emojis")
                            for (i in 0 until profile_emojis.length()) {
                                val emojiObject = profile_emojis.getJSONObject(i)
                                val emoji_name = emojiObject.getString("name")
                                val emoji_url = emojiObject.getString("url")
                                val custom_emoji_src = "<img src=\'$emoji_url\'>"
                                //display_name
                                if (snackber_DisplayName.contains(emoji_name)) {
                                    //あったよ
                                    snackber_DisplayName = snackber_DisplayName.replace(":$emoji_name:", custom_emoji_src)
                                }
                            }
                        }
                    }
                    snackber_Name = "@$username@$instance"
                    snackber_Avatar = jsonObject.getString("avatarUrl")
                    //UIスレッド
                    runOnUiThread {
                        //画像を入れる
                        //表示設定
                        if (setting_avater_hidden) {
                            snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp)
                            snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                        }
                        //Wi-Fi
                        if (setting_avater_wifi) {
                            if (networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                if (setting_avater_gif) {
                                    //GIFアニメ再生させない
                                    Picasso.get()
                                            .load(snackber_Avatar)
                                            .resize(100, 100)
                                            .placeholder(R.drawable.ic_refresh_black_24dp)
                                            .into(snackberAccountAvaterImageView)
                                } else {
                                    //GIFアニメを再生
                                    Glide.with(applicationContext)
                                            .load(snackber_Avatar)
                                            .apply(RequestOptions().override(100, 100).placeholder(R.drawable.ic_refresh_black_24dp))
                                            .into(snackberAccountAvaterImageView)
                                }
                            }
                        } else {
                            snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp)
                            snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                        }
                        //テキストビューに入れる
                        val imageGetter = PicassoImageGetter(snackberAccount_TextView)
                        snackberAccount_TextView.text = Html.fromHtml(snackber_DisplayName, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
                        snackberAccount_TextView.append("\n" + snackber_Name)
                        //裏機能？
                        shinchokuLayout.setStatusProgress(toot_count)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * Misskey notes/create POST
     */
    private fun misskeyNoteCreatePOST() {
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance/api/notes/create"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("i", token)
            jsonObject.put("visibility", toot_area)
            jsonObject.put("text", toot_EditText.text.toString())
            jsonObject.put("viaMobile", true)//スマホからなので一応
            //添付メディア
            if (post_media_id.size >= 1) {
                val jsonArray = JSONArray()
                for (i in post_media_id.indices) {
                    jsonArray.put(post_media_id[i])
                }
                jsonObject.put("fileIds", jsonArray)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //System.out.println(jsonObject.toString());
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
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@Home, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@Home, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    runOnUiThread {
                        //EditTextを空にする
                        toot_EditText.setText("")
                        tootTextCount = 0
                        //TootSnackber閉じる
                        toot_snackbar.dismiss()
                        //配列を空にする
                        media_uri_list!!.clear()
                        post_media_id.clear()
                        media_LinearLayout.removeAllViews()
                    }
                }
            }
        })
    }

    /**
     * Mastodon statuses POST
     */
    private fun mastodonStatusesPOST() {
        val AccessToken = pref_setting.getString("main_token", "")
        val Instance = pref_setting.getString("main_instance", "")
        val url = "https://$Instance/api/v1/statuses/?access_token=$AccessToken"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("status", toot_EditText.text.toString())
            jsonObject.put("visibility", toot_area)
            //時間指定
            if (isTimePost) {
                //System.out.println(post_date + "/" + post_time);
                //nullCheck
                if (post_date != null && post_time != null) {
                    jsonObject.put("scheduled_at", post_date!! + post_time!!)
                }
            }
            //画像
            if (post_media_id.size != 0) {
                val media = JSONArray()
                for (i in post_media_id.indices) {
                    media.put(post_media_id[i])
                }
                jsonObject.put("media_ids", media)
            }
            //投票機能
            if (isMastodon_vote) {
                jsonObject.put("poll", createMastodonVote())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
        val request = Request.Builder()
                .url(url)
                .post(requestBody_json)
                .build()

        //POST
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@Home, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@Home, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    runOnUiThread {
                        //予約投稿・通常投稿でトースト切り替え
                        if (time_post_Switch != null) {
                            time_post_Switch!!.isChecked = false
                            Toast.makeText(this@Home, getString(R.string.time_post_ok), Toast.LENGTH_SHORT).show()
                            //予約投稿を無効化
                            isTimePost = false
                            mastodon_time_post_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN)
                        } else {
                            Toast.makeText(this@Home, getString(R.string.toot_ok), Toast.LENGTH_SHORT).show()
                        }
                        //投票
                        if (isMastodon_vote) {
                            isMastodon_vote = false
                            vote_use_Switch.isChecked = false
                        }
                        //EditTextを空にする
                        toot_EditText.setText("")
                        tootTextCount = 0
                        //TootSnackber閉じる
                        toot_snackbar.dismiss()
                        //配列を空にする
                        media_uri_list!!.clear()
                        post_media_id.clear()
                        media_LinearLayout.removeAllViews()
                        //目標更新
                        shinchokuLayout.setTootChallenge()
                        //JSONParseしてトゥート数変更する
                        val jsonObject = JSONObject(response_string)
                        val toot_count = jsonObject.getJSONObject("account").getInt("statuses_count").toString()
                        shinchokuLayout.setStatusProgress(toot_count)
                    }
                }
            }
        })
    }

    /**
     * Misskey 画像POST
     */
    private fun uploadDrivePhoto(uri: Uri) {
        val instance = pref_setting.getString("misskey_main_instance", "")
        val token = pref_setting.getString("misskey_main_token", "")
        val username = pref_setting.getString("misskey_main_username", "")
        val url = "https://$instance/api/drive/files/create"
        //くるくる
        SnackberProgress.showProgressSnackber(toot_EditText, this@Home, getString(R.string.loading) + "\n" + url)
        //ぱらめーたー
        val requestBody = MultipartBody.Builder()
        requestBody.setType(MultipartBody.FORM)
        //requestBody.addFormDataPart("file", file_post.getName(), RequestBody.create(MediaType.parse("image/" + file_extn_post), file_post));
        requestBody.addFormDataPart("i", token!!)
        requestBody.addFormDataPart("force", "true")
        //Android Qで動かないのでUriからBitmap変換してそれをバイトに変換してPOSTしてます
        val uri_byte = UriToByte(this@Home)
        try {
            val file_name = getFileNameUri(uri)
            val extn = contentResolver.getType(uri)

            System.out.println(file_name + "/" + extn)

            //requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn!!), uri_byte.getByte(uri)))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        requestBody.build()
        //作成
        val request = Request.Builder()
                .url(url)
                .post(requestBody.build())
                .build()

        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@Home, getString(R.string.error), Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                //System.out.println(response_string);
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@Home, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    try {
                        val jsonObject = JSONObject(response_string)
                        val media_id_long = jsonObject.getString("id")
                        //配列に格納
                        post_media_id.add(media_id_long)
                        //確認SnackBer
                        //数確認
                        if (media_uri_list!!.size == post_media_id.size) {
                            val view = findViewById<View>(R.id.container_public)
                            Snackbar.make(view, R.string.note_create_message, Snackbar.LENGTH_SHORT).setAction(R.string.toot_text) { misskeyNoteCreatePOST() }.show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })
    }

    /**
     * PNG / JPEG
     */
    private fun getImageType(extn: String): Bitmap.CompressFormat {
        var format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
        when (extn) {
            "jpg" -> format = Bitmap.CompressFormat.JPEG
            "jpeg" -> format = Bitmap.CompressFormat.JPEG
            "png" -> format = Bitmap.CompressFormat.PNG
        }
        return format
    }

    /**
     * Mastodon 画像POST
     */
    private fun uploadMastodonPhoto(uri: Uri) {
        //えんどぽいんと
        val url = "https://$Instance/api/v1/media/"
        //ぱらめーたー
        val requestBody = MultipartBody.Builder()
        requestBody.setType(MultipartBody.FORM)
        //requestBody.addFormDataPart("file", file_post.getName(), RequestBody.create(MediaType.parse("image/" + file_extn_post), file_post));
        requestBody.addFormDataPart("access_token", AccessToken!!)
        //くるくる
        SnackberProgress.showProgressSnackber(toot_EditText, this@Home, getString(R.string.loading) + "\n" + url)
        //Android Qで動かないのでUriからバイトに変換してPOSTしてます
        //重いから非同期処理
        Thread(Runnable {
            val uri_byte = UriToByte(this@Home);
            try {
                // file:// と content:// でわける
                if (uri.scheme?.contains("file") ?: false) {
                    val file_name = getFileSchemeFileName(uri)
                    val extn = getFileSchemeFileExtension(uri)
                    requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn!!), uri_byte.getByte(uri)))
                } else {
                    val file_name = getFileNameUri(uri)
                    val extn = contentResolver.getType(uri)
                    requestBody.addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("image/" + extn!!), uri_byte.getByte(uri)))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            //じゅんび
            val request = Request.Builder()
                    .url(url)
                    .post(requestBody.build())
                    .build()
            //画像Upload
            val okHttpClient = OkHttpClient()
            //POST実行
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    //失敗
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@Home, getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val response_string = response.body()!!.string()
                    //System.out.println("画像POST : " + response_string);
                    if (!response.isSuccessful) {
                        //失敗
                        runOnUiThread { Toast.makeText(this@Home, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        try {
                            val jsonObject = JSONObject(response_string)
                            val media_id_long = jsonObject.getString("id")
                            //配列に格納
                            post_media_id.add(media_id_long)
                            //確認SnackBer
                            //数確認
                            if (media_uri_list!!.size == post_media_id.size) {
                                val view = findViewById<View>(R.id.container_public)
                                Snackbar.make(view, R.string.note_create_message, Snackbar.LENGTH_INDEFINITE).setAction(R.string.toot_text) { mastodonStatusesPOST() }.show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }
            })
        }).start()
    }

    /**
     * Uri→FileName
     */
    private fun getFileNameUri(uri: Uri): String? {
        var file_name: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                file_name = cursor.getString(0)
            }
        }
        return file_name
    }

    /*
    * Uri→FileName
    * Fileスキーム限定
    * */
    fun getFileSchemeFileName(uri: Uri): String? {
        //file://なので使える
        val file = File(uri.path)
        return file.name
    }

    /*
    * Uri→Extension
    * 拡張子取得。Kotlinだと楽だね！
    * */
    fun getFileSchemeFileExtension(uri: Uri): String? {
        val file = File(uri.path)
        return file.extension
    }


    /**
     * Mastodon 投票
     */
    private fun createMastodonVote(): JSONObject {
        val `object` = JSONObject()
        try {
            //配列
            val jsonArray = JSONArray()
            if (vote_1.text.toString() != null) {
                jsonArray.put(vote_1.text.toString())
            }
            if (vote_2.text.toString() != null) {
                jsonArray.put(vote_2.text.toString())
            }
            if (vote_3.text.toString() != null) {
                jsonArray.put(vote_3.text.toString())
            }
            if (vote_4.text.toString() != null) {
                jsonArray.put(vote_4.text.toString())
            }
            `object`.put("options", jsonArray)
            `object`.put("expires_in", vote_time.text.toString())
            `object`.put("multiple", vote_multi_Switch.isChecked)
            //object.put("hide_totals", vote_hide_Switch.isChecked());
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return `object`
    }


    /**
     * Mastodon 公開範囲
     */
    @SuppressLint("RestrictedApi")
    private fun setMastodonVisibilityMenu(button: ImageButton) {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(this@Home)
        val inflater = MenuInflater(this@Home)
        inflater.inflate(R.menu.toot_area_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(this@Home, menuBuilder, button)
        optionsMenu.setForceShowIcon(true)
        //ポップアップメニューを展開する
        button.setOnClickListener {
            //表示
            optionsMenu.show()
            //押したときの反応
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                    //公開（全て）
                    if (menuItem.title.toString().contains(getString(R.string.visibility_public))) {
                        toot_area = "public"
                        button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp))
                    }
                    //未収載（TL公開なし・誰でも見れる）
                    if (menuItem.title.toString().contains(getString(R.string.visibility_unlisted))) {
                        toot_area = "unlisted"
                        button.setImageDrawable(getDrawable(R.drawable.ic_done_all_black_24dp))
                    }
                    //非公開（フォロワー限定）
                    if (menuItem.title.toString().contains(getString(R.string.visibility_private))) {
                        toot_area = "private"
                        button.setImageDrawable(getDrawable(R.drawable.ic_lock_open_black_24dp))
                    }
                    //ダイレクト（指定したアカウントと自分）
                    if (menuItem.title.toString().contains(getString(R.string.visibility_direct))) {
                        toot_area = "direct"
                        button.setImageDrawable(getDrawable(R.drawable.ic_assignment_ind_black_24dp))
                    }
                    return false
                }

                override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                }
            })
        }
    }

    /**
     * Misskey 公開範囲
     */
    @SuppressLint("RestrictedApi")
    private fun setMisskeyVisibilityMenu(button: ImageButton) {
        //ポップアップメニュー作成
        val menuBuilder = MenuBuilder(this@Home)
        val inflater = MenuInflater(this@Home)
        inflater.inflate(R.menu.misskey_visibility_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(this@Home, menuBuilder, button)
        optionsMenu.setForceShowIcon(true)
        //ポップアップメニューを展開する
        button.setOnClickListener {
            //表示
            optionsMenu.show()
            //押したときの反応
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
                    //公開（全て）
                    if (menuItem.title.toString().contains(getString(R.string.misskey_public))) {
                        toot_area = "public"
                        button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp))
                    }
                    //ホーム
                    if (menuItem.title.toString().contains(getString(R.string.misskey_home))) {
                        toot_area = "home"
                        button.setImageDrawable(getDrawable(R.drawable.ic_home_black_24dp))
                    }
                    //フォロワー限定
                    if (menuItem.title.toString().contains(getString(R.string.misskey_followers))) {
                        toot_area = "followers"
                        button.setImageDrawable(getDrawable(R.drawable.ic_person_add_black_24dp))
                    }
                    //ダイレクト（指定したアカウントと自分）
                    if (menuItem.title.toString().contains(getString(R.string.misskey_specified))) {
                        toot_area = "specified"
                        button.setImageDrawable(getDrawable(R.drawable.ic_assignment_ind_black_24dp))
                    }
                    //公開（ローカルのみ）
                    if (menuItem.title.toString().contains(getString(R.string.misskey_private))) {
                        toot_area = "private"
                        button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp))
                    }

                    return false
                }

                override fun onMenuModeChange(menuBuilder: MenuBuilder) {

                }
            })
        }
    }

    /**
     * DatePicker
     */
    private fun showDatePicker(textView: TextView) {
        val date = arrayOf("")

        val calendar = Calendar.getInstance()
        val dateBuilder = DatePickerDialog(this@Home, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            var month = month
            var month_string = ""
            var day_string = ""
            //1-9月は前に0を入れる
            if (month++ <= 9) {
                month_string = "0" + month++.toString()
            } else {
                month_string = month++.toString()
            }
            //1-9日も前に0を入れる
            if (dayOfMonth <= 9) {
                day_string = "0$dayOfMonth"
            } else {
                day_string = dayOfMonth.toString()
            }
            post_date = year.toString() + month_string + day_string + "T"
            textView.text = post_date
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        dateBuilder.show()
    }

    /**
     * TimePicker
     */
    private fun showTimePicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(this@Home, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            var hourOfDay = hourOfDay
            var hour_string = ""
            var minute_string = ""
            //1-9月は前に0を入れる
            if (hourOfDay <= 9) {
                hour_string = "0" + hourOfDay++.toString()
            } else {
                hour_string = hourOfDay++.toString()
            }
            //1-9日も前に0を入れる
            if (minute <= 9) {
                minute_string = "0$minute"
            } else {
                minute_string = minute.toString()
            }
            post_time = hour_string + minute_string + "00" + "+0900"
            textView.text = post_time
        }, hour, minute, true)
        dialog.show()
    }

    /**
     * TootShortcutShow
     */
    private fun showTootShortcut() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container_container)
        //CustomMenuTimeLine以外で投稿画面を開かないようにする
        if (fragment is CustomMenuTimeLine) {
            val customMenuTimeLine = fragment as CustomMenuTimeLine
            if (fragment != null && fragment is DesktopFragment) {
                //DesktopModeはPopupMenuからMastodon/Misskeyを選ぶ
                //Misskeyアカウントが登録されていなければ話にならない
                if (pref_setting.getString("misskey_instance_list", "") != "") {
                    //ポップアップメニュー作成
                    val sns_PopupMenu = PopupMenu(this, fab)
                    sns_PopupMenu.inflate(R.menu.desktop_mode_sns_menu)
                    //クリックイベント
                    sns_PopupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.desktop_mode_menu_mastodon -> {
                                getAccount()
                                setMastodonVisibilityMenu(toot_area_Button)
                                toot_Button_LinearLayout.removeView(misskey_drive_Button)
                                toot_Button_LinearLayout.removeView(mastodon_time_post_Button)
                                toot_Button_LinearLayout.removeView(mastodon_vote_Button)
                                toot_Button_LinearLayout.removeView(paintPOSTButton)
                                toot_Button_LinearLayout.addView(mastodon_time_post_Button)
                                toot_Button_LinearLayout.addView(mastodon_vote_Button)
                                toot_Button_LinearLayout.addView(paintPOSTButton)
                                //デスクトップモード利用時はマルチアカウント表示できるように
                                if (fragment != null && fragment is DesktopFragment) {
                                    showMultiAccount()
                                }
                            }
                            R.id.desktop_mode_menu_misskey -> if (pref_setting.getString("misskey_instance_list", "") != "") {
                                getMisskeyAccount()
                                setMisskeyVisibilityMenu(toot_area_Button)
                                toot_Button_LinearLayout.removeView(misskey_drive_Button)
                                toot_Button_LinearLayout.removeView(mastodon_time_post_Button)
                                toot_Button_LinearLayout.removeView(mastodon_vote_Button)
                                toot_Button_LinearLayout.removeView(paintPOSTButton)
                                toot_Button_LinearLayout.addView(misskey_drive_Button)
                                //デスクトップモード利用時はマルチアカウント表示できるように
                                if (fragment != null && fragment is DesktopFragment) {
                                    showMisskeyMultiAccount()
                                }
                            }
                        }
                        toot_snackbar.show()
                        //ふぉーかす
                        toot_EditText.requestFocus()
                        //キーボード表示
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                        false
                    }
                    //すでにTootSnackberが表示されている場合は消して、ポップアップメニューを表示する
                    if (toot_snackbar.isShown) {
                        toot_snackbar.dismiss()
                        //キーボード非表示
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        if (imm != null) {
                            if (this@Home.currentFocus != null) {
                                imm.hideSoftInputFromWindow(this@Home.currentFocus!!.windowToken, 0)
                            }
                        }
                        sns_PopupMenu.show()
                    } else {
                        sns_PopupMenu.show()
                    }
                } else {
                    //Mastodonのみ表示
                    getAccount()
                    setMastodonVisibilityMenu(toot_area_Button)
                    toot_Button_LinearLayout.removeView(misskey_drive_Button)
                    toot_Button_LinearLayout.removeView(mastodon_time_post_Button)
                    toot_Button_LinearLayout.removeView(mastodon_vote_Button)
                    toot_Button_LinearLayout.removeView(paintPOSTButton)
                    toot_Button_LinearLayout.addView(mastodon_time_post_Button)
                    toot_Button_LinearLayout.addView(mastodon_vote_Button)
                    toot_Button_LinearLayout.addView(paintPOSTButton)
                    //デスクトップモード利用時はマルチアカウント表示できるように
                    if (fragment != null && fragment is DesktopFragment) {
                        showMultiAccount()
                    }
                    if (!toot_snackbar.isShown) {
                        toot_snackbar.show()
                        //ふぉーかす
                        toot_EditText.requestFocus()
                        //キーボード表示
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    } else {
                        toot_snackbar.dismiss()
                        //クローズでソフトキーボード非表示
                        fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp))
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        if (imm != null) {
                            if (this@Home.currentFocus != null) {
                                imm.hideSoftInputFromWindow(this@Home.currentFocus!!.windowToken, 0)
                            }
                        }
                    }
                }
            } else {
                //ユーザー情報を取得
                //カスタムメニューが読み取り専用だったら動かないようにする
                if (!customMenuTimeLine.isReadOnly()) {
                    //MisskeyモードでMisskeyアカウントが登録されれいるときのみ表示
                    //避けたかったけどどうしてもisMisskeyMode()必要だから使う
                    if (CustomMenuTimeLine.isMisskeyMode && pref_setting.getString("misskey_instance_list", "") != "") {
                        shinchokuLayout.setOnDayProgress()
                        getMisskeyAccount()
                        setMisskeyVisibilityMenu(toot_area_Button)
                        toot_Button_LinearLayout.removeView(misskey_drive_Button)
                        toot_Button_LinearLayout.removeView(mastodon_time_post_Button)
                        toot_Button_LinearLayout.removeView(mastodon_vote_Button)
                        toot_Button_LinearLayout.removeView(paintPOSTButton)
                        toot_Button_LinearLayout.addView(misskey_drive_Button)
                    } else {
                        //FAB押すたびにAPI叩くの直す
                        //まずFragmentがCustomMenuTimeLineかどうか
                        if (fragment is CustomMenuTimeLine) {
                            if (tootSnackbarCustomMenuName.isEmpty()) {
                                //初回
                                tootSnackbarCustomMenuName = (fragment as CustomMenuTimeLine).getCustomMenuName()
                            } else if (!tootSnackbarCustomMenuName.contains((fragment as CustomMenuTimeLine).getCustomMenuName())) {
                                //名前が同じじゃなかったらAPI叩く
                                tootSnackbarCustomMenuName = (fragment as CustomMenuTimeLine).getCustomMenuName()
                                getAccount()
                            }
                        } else {
                            //API叩く
                            getAccount()
                        }
                        shinchokuLayout.setOnDayProgress()
                        setMastodonVisibilityMenu(toot_area_Button)
                        toot_Button_LinearLayout.removeView(misskey_drive_Button)
                        toot_Button_LinearLayout.removeView(mastodon_time_post_Button)
                        toot_Button_LinearLayout.removeView(mastodon_vote_Button)
                        toot_Button_LinearLayout.removeView(paintPOSTButton)
                        toot_Button_LinearLayout.addView(mastodon_time_post_Button)
                        toot_Button_LinearLayout.addView(mastodon_vote_Button)
                        toot_Button_LinearLayout.addView(paintPOSTButton)
                    }
                    if (!toot_snackbar.isShown) {
                        toot_snackbar.show()
                        //ふぉーかす
                        toot_EditText.requestFocus()
                        //キーボード表示
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    } else {
                        toot_snackbar.dismiss()
                        //クローズでソフトキーボード非表示
                        fab.setImageDrawable(getDrawable(R.drawable.ic_create_black_24dp))
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        if (imm != null) {
                            if (this@Home.currentFocus != null) {
                                imm.hideSoftInputFromWindow(this@Home.currentFocus!!.windowToken, 0)
                            }
                        }
                    }
                } else {
                    //読み取り専用だと投稿権限ないよ！
                    Toast.makeText(this, getString(R.string.read_only_toot), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /*タイムラインクイック設定ボタン生成*/
    @SuppressLint("RestrictedApi")
    private fun setTimelinQuickSettings(): ImageView {
        val qs = ImageView(this)
        qs.setImageResource(R.drawable.tl_quick_setting_icon)
        qs.setPadding(50, 10, 50, 10)
        qs.setOnClickListener {
            val drawable = qs.drawable
            if (drawable is Animatable) {
                (drawable as Animatable).start()
                (drawable as Animatable).start()
            }
            val list = ArrayList<String>()
            list.add(account_id)
            tlQuickSettingSnackber!!.setList(list)
            if (tlQuickSettingSnackber!!.snackbar.isShown) {
                tlQuickSettingSnackber!!.dismissSnackBer()
            } else {
                tlQuickSettingSnackber!!.showSnackBer()
            }
        }
        return qs
    }

    companion object {
        //添付メディア
        lateinit var post_media_id: ArrayList<String>
        lateinit var misskey_media_url: ArrayList<String>

        fun getPathAndroidQ(context: Context, contentUri: Uri): String {
            var cursor: Cursor? = null
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            var column_index = 0
            var path = ""
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                path = cursor.getString(column_index)
                cursor.close()
            }
            return path
        }
    }

    fun setAppBar() {
        toolBer = findViewById<View>(R.id.toolbar) as Toolbar
        fab = findViewById<View>(R.id.fab) as FloatingActionButton
        drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val darkModeSupport = DarkModeSupport(this@Home)

        if (pref_setting.getBoolean("pref_bottom_navigation", false)) {
            // 上のバーとFabをけす
            container_public.removeView(toolBer.parent as AppBarLayout)
            container_public.removeView(fab)

            layoutInflater.inflate(R.layout.bottom_bar_layout, container_public)
            setSupportActionBar(bottomAppBar)
            bottomAppBar.setNavigationOnClickListener() {
                drawer.openDrawer(Gravity.LEFT)
            }
            bottom_fab.setOnClickListener { showTootShortcut() }
            //一応代入
            fab = bottom_fab
            //追加されてなければ追加
            bottomAppBar.addView(setTimelinQuickSettings())
            tlQuickSettingSnackber = TLQuickSettingSnackber(this@Home, navigationView)
            // ダークモード対応
            if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES) {
                bottomAppBar.backgroundTintList = resources.getColorStateList(android.R.color.black, theme)
                //ナビゲーションバーの色を動的に変える
                window.navigationBarColor = getColor(R.color.black)
                //ActionBarの色設定
                bottomAppBar.backgroundTint = ColorStateList.valueOf(Color.parseColor("#000000"))
            } else {
                //ナビゲーションバーの色を動的に変える
                window.navigationBarColor = getColor(R.color.colorPrimary)
            }
        } else {
            setSupportActionBar(toolBer)
            val toggle = ActionBarDrawerToggle(
                    this, drawer, toolBer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawer.addDrawerListener(toggle)
            toggle.syncState()
            fab.setOnClickListener { showTootShortcut() }
            /*クイック設定*/
            toolBer.addView(setTimelinQuickSettings())
            tlQuickSettingSnackber = TLQuickSettingSnackber(this@Home, navigationView)
            //ActionBarの色設定
            if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES) {
                toolBer.setBackgroundColor(Color.parseColor("#000000"))
            }
        }
    }

    fun isSnackbarShow(): Boolean {
        return toot_snackbar.isShown
    }


}
