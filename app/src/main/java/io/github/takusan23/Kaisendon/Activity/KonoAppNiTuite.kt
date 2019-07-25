package io.github.takusan23.Kaisendon.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONException
import org.json.JSONObject
import ru.noties.markwon.Markwon
import ru.noties.markwon.html.HtmlPlugin
import ru.noties.markwon.image.ImagesPlugin
import ru.noties.markwon.image.gif.GifPlugin
import java.io.IOException


class KonoAppNiTuite : AppCompatActivity() {

    private val release_name_2 = "まぐろ丼"
    private val release_ver_2 = "2.0"
    private val release_name_3 = "さーもんどん"
    private val release_ver_3 = "3.0"
    private val release_name_4 = "いかどん"
    //private String release_ver_4 = "4.0";
    private val release_ver_4 = "4.1.0"
    private val release_name_5 = "たこどん"
    //private String release_ver_5 = "5.0";
    private val release_ver_5 = "5.1.4"
    private val release_name_6 = "たまごどん"
    //private val release_ver_6 = "6.0.4"
    private val release_ver_6 = "6.1.0"

    private var version_TextView: TextView? = null
    private var pref_setting: SharedPreferences? = null
    private var main_LinearLayout: LinearLayout? = null
    private var bottomNavigationView: BottomNavigationView? = null
    private var iconImageView: ImageView? = null

    private var chrome_custom_tabs: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)

        //ダークテーマに切り替える機能
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)

        setContentView(R.layout.activity_kono_app_ni_tuite)

        setTitle(R.string.konoappnituite)

        version_TextView = findViewById(R.id.version_textview)
        main_LinearLayout = findViewById(R.id.kono_app_nituite_main_linearLayout)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        chrome_custom_tabs = pref_setting!!.getBoolean("pref_chrome_custom_tabs", true)
        iconImageView = findViewById(R.id.producer_avataer_imageView)
        if (iconImageView != null) {
            iconImageView!!.imageTintList = null
        }

        version_TextView!!.text = getString(R.string.version) + " " + release_ver_6 + "\r\n" + release_name_6

        /**
         * ナビゲーション
         */
        bottomNavigationView!!.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.version ->
                    //消す
                    main_LinearLayout!!.removeView(main_LinearLayout!!.getChildAt(1))
                R.id.producer -> {
                    //消す
                    main_LinearLayout!!.removeView(main_LinearLayout!!.getChildAt(1))
                    setProducerProfile()
                }
                R.id.document -> {
                    //消す
                    main_LinearLayout!!.removeView(main_LinearLayout!!.getChildAt(1))
                    setDocument()
                }
                R.id.release_note -> {
                    //消す
                    main_LinearLayout!!.removeView(main_LinearLayout!!.getChildAt(1))
                    setReleaseNote()
                }
            }
            true
        }


        /*


        TextView KonoAppTextView = findViewById(R.id.konoAppTextview);
        TextView KonoAppTextView_2 = findViewById(R.id.konoAppTextview_2);

        TextView producerTextview = findViewById(R.id.konoAppTextview_producer);
        ImageView producerImageview = findViewById(R.id.konoAppImageview_producer);
        ImageView producerIconApp = findViewById(R.id.konoAppIcon_producer_AppLunch);
        ImageView producerIconBrowser = findViewById(R.id.konoAppIcon_producer_Browser);
        LinearLayout producerLinearlayout = findViewById(R.id.linearlayout_producer);
        LinearLayout titleLinearLayout = findViewById(R.id.konoApp_title_linearlayout);

        Button githubButton = findViewById(R.id.githubButton);

        Button mastodon_contact = findViewById(R.id.mastodon_contact);
        Button twitter_contact = findViewById(R.id.twitter_contact);

        TextView document_textview = findViewById(R.id.wiki);
        Button document_button = findViewById(R.id.wiki_button);
        Button wear_document_Button = findViewById(R.id.wear_document_button);


        KonoAppTextView_2.setText(getString(R.string.version) + " " + release_ver_5 + "\r\n" + release_name_5);
        githubButton.setText(getString(R.string.sourceCode) + ": " + "GitHub");

        document_button.setText(getString(R.string.document) + "\n" + "GitHub Wiki");

        setTitle(R.string.konoappnituite);


        //製作者を埋める
        String url = "https://friends.nico/api/v1/accounts/6280";
        //作成
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        OkHttpClient client_1 = new OkHttpClient();
        client_1.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(response_string);

                    String display_name = jsonObject.getString("display_name");
                    String acct = jsonObject.getString("acct");
                    String avatar = jsonObject.getString("avatar");
                    String userURL = jsonObject.getString("url");
                    String account_id = jsonObject.getString("id");


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //ブラウザで起動
                            producerIconBrowser.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (chrome_custom_tabs) {
                                        Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                                        String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                        CustomTabsIntent customTabsIntent = builder.build();
                                        customTabsIntent.intent.setPackage(custom);
                                        customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(userURL));
                                    } else {
                                        Uri uri = Uri.parse(userURL);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                    }
                                }
                            });

                            //アプリ内で起動
                            producerIconApp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(KonoAppNiTuite.this, UserActivity.class);
                                    //IDを渡す
                                    intent.putExtra("Account_ID", account_id);
                                    startActivity(intent);
                                }
                            });

                            producerTextview.setText(display_name + "\r\n" + acct + "@friends.nico");
                            Glide.with(KonoAppNiTuite.this).load(avatar).into(producerImageview);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String githubUrl = "https://github.com/takusan23/Kaisendon";
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(githubUrl));
                } else {
                    Uri uri = Uri.parse(githubUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }

            }
        });

        wear_document_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String githubUrl = "https://github.com/takusan23/KaisendonWear/wiki";
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(githubUrl));
                } else {
                    Uri uri = Uri.parse(githubUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });


        twitter_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String twitterUrl = "https://twitter.com/takusan__23";
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(twitterUrl));
                } else {
                    Uri uri = Uri.parse(twitterUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        mastodon_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toot = new Intent(KonoAppNiTuite.this, TootSnackberActivity.class);
                toot.putExtra("contact", "@takusan_23@friends.nico ");
                startActivity(toot);
            }
        });


*/
        /*
        //うらわざ？
        titleLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(KonoAppNiTuite.this, "friends.nico", Toast.LENGTH_SHORT).show();
                if (pref_setting.getBoolean("pref_friends_nico_mode", false)) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_friends_nico_mode", false);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_friends_nico_mode", true);
                    editor.apply();
                }
                return false;
            }

        });
*//*



        document_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String documentUrl = "https://github.com/takusan23/Kaisendon/wiki";
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(documentUrl));
                } else {
                    Uri uri = Uri.parse(documentUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
*/
    }

    /**
     * 製作者取得
     */
    private fun setProducerProfile() {
        //レイアウト
        val linearLayout = LinearLayout(this)
        layoutInflater.inflate(R.layout.kono_app_nituite_producer_layout, linearLayout)
        main_LinearLayout!!.addView(linearLayout)

        //製作者を埋める
        val url = "https://best-friends.chat/api/v1/accounts/20498"
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()

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
                    val acct = jsonObject.getString("acct")
                    val avatar = jsonObject.getString("avatar")
                    val userURL = jsonObject.getString("url")
                    val account_id = jsonObject.getString("id")

                    runOnUiThread {
                        val avatar_ImageView = linearLayout.findViewById<ImageView>(R.id.producer_avataer_imageView)
                        val textView = linearLayout.findViewById<TextView>(R.id.producer_name_textView)
                        textView.text = display_name + "\n" + acct
                        Glide.with(this@KonoAppNiTuite).load(avatar).into(avatar_ImageView)
                        //プロフィールへ
                        val open_app = linearLayout.findViewById<Button>(R.id.producer_open_app)
                        val open_browser = linearLayout.findViewById<Button>(R.id.producer_open_browser)
                        val open_twitter = linearLayout.findViewById<Button>(R.id.producer_twitter)
                        //ブラウザで起動
                        open_browser.setOnClickListener {
                            if (chrome_custom_tabs) {
                                val back_icon = BitmapFactory.decodeResource(this@KonoAppNiTuite.resources, R.drawable.ic_action_arrow_back)
                                val custom = CustomTabsHelper.getPackageNameToUse(this@KonoAppNiTuite)
                                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                                val customTabsIntent = builder.build()
                                customTabsIntent.intent.setPackage(custom)
                                customTabsIntent.launchUrl(this@KonoAppNiTuite, Uri.parse(userURL))
                            } else {
                                val uri = Uri.parse(userURL)
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            }
                        }
                        //アプリ内で起動
                        open_app.setOnClickListener {
                            val intent = Intent(this@KonoAppNiTuite, UserActivity::class.java)
                            //IDを渡す
                            intent.putExtra("Account_ID", account_id)
                            startActivity(intent)
                        }
                        //一応青い鳥も
                        open_twitter.setOnClickListener {
                            val twitterUrl = "https://twitter.com/takusan__23"
                            if (chrome_custom_tabs) {
                                val back_icon = BitmapFactory.decodeResource(this@KonoAppNiTuite.resources, R.drawable.ic_action_arrow_back)
                                val custom = CustomTabsHelper.getPackageNameToUse(this@KonoAppNiTuite)
                                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                                val customTabsIntent = builder.build()
                                customTabsIntent.intent.setPackage(custom)
                                customTabsIntent.launchUrl(this@KonoAppNiTuite, Uri.parse(twitterUrl))
                            } else {
                                val uri = Uri.parse(twitterUrl)
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            }
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * ドキュメント
     */
    private fun setDocument() {
        //レイアウト
        val linearLayout = LinearLayout(this)
        layoutInflater.inflate(R.layout.kono_app_nituite_document_layout, linearLayout)
        main_LinearLayout!!.addView(linearLayout)
        val android = linearLayout.findViewById<Button>(R.id.document_android)
        val wear = linearLayout.findViewById<Button>(R.id.document_wear)
        val source_code = linearLayout.findViewById<Button>(R.id.source_code)
        android.setOnClickListener(View.OnClickListener {
            val documentUrl = "https://github.com/takusan23/Kaisendon/wiki"
            if (chrome_custom_tabs) {
                val back_icon = BitmapFactory.decodeResource(this@KonoAppNiTuite.resources, R.drawable.ic_action_arrow_back)
                val custom = CustomTabsHelper.getPackageNameToUse(this@KonoAppNiTuite)
                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                val customTabsIntent = builder.build()
                customTabsIntent.intent.setPackage(custom)
                customTabsIntent.launchUrl(this@KonoAppNiTuite, Uri.parse(documentUrl))
            } else {
                val uri = Uri.parse(documentUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        })
        wear.setOnClickListener(View.OnClickListener {
            val githubUrl = "https://github.com/takusan23/KaisendonWear/wiki"
            if (chrome_custom_tabs) {
                val back_icon = BitmapFactory.decodeResource(this@KonoAppNiTuite.resources, R.drawable.ic_action_arrow_back)
                val custom = CustomTabsHelper.getPackageNameToUse(this@KonoAppNiTuite)
                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                val customTabsIntent = builder.build()
                customTabsIntent.intent.setPackage(custom)
                customTabsIntent.launchUrl(this@KonoAppNiTuite, Uri.parse(githubUrl))
            } else {
                val uri = Uri.parse(githubUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        })
        source_code.setOnClickListener(View.OnClickListener {
            val githubUrl = "https://github.com/takusan23/Kaisendon"
            if (chrome_custom_tabs) {
                val back_icon = BitmapFactory.decodeResource(this@KonoAppNiTuite.resources, R.drawable.ic_action_arrow_back)
                val custom = CustomTabsHelper.getPackageNameToUse(this@KonoAppNiTuite)
                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                val customTabsIntent = builder.build()
                customTabsIntent.intent.setPackage(custom)
                customTabsIntent.launchUrl(this@KonoAppNiTuite, Uri.parse(githubUrl))
            } else {
                val uri = Uri.parse(githubUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        })
    }

    /*変更点を表示する機能*/
    private fun setReleaseNote() {
        //レイアウト
        val linearLayout = LinearLayout(this)
        layoutInflater.inflate(R.layout.kono_app_nituite_release_note_layout, linearLayout)
        main_LinearLayout!!.addView(linearLayout)
        val textView = main_LinearLayout!!.findViewById<TextView>(R.id.release_note_textView)
        //取得
        val url = "https://raw.githubusercontent.com/wiki/takusan23/Kaisendon/Ver6.0の進捗.md"
        //作成
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        //GETリクエスト
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@KonoAppNiTuite, R.string.error, Toast.LENGTH_SHORT).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val response_string = response.body()!!.string()
                if (!response.isSuccessful) {
                    //失敗
                    runOnUiThread { Toast.makeText(this@KonoAppNiTuite, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                } else {
                    var tree = textView.viewTreeObserver
                    var height = 66;
                    tree.addOnGlobalLayoutListener { ->
                        height = textView.height

                    }
                    //var mark = "GIF <img src=\"https://media.best-friends.chat/accounts/avatars/000/020/498/original/bc0bd14abb0bb063.gif\" width=\"${height}\" >"

                    runOnUiThread {
                        //Markdownのライブラリ入れた
                        val markwon = Markwon.builder(this@KonoAppNiTuite)
                                .usePlugin(HtmlPlugin.create())
                                .usePlugin(ImagesPlugin.create(this@KonoAppNiTuite))
                                .usePlugin(GifPlugin.create())
                                .build()
                        markwon.setMarkdown(textView, response_string)
                    }

                }
            }
        })
    }

}
