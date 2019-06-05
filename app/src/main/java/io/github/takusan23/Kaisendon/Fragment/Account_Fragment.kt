package io.github.takusan23.Kaisendon.Fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.preference.PreferenceManager.getDefaultSharedPreferences

import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.squareup.picasso.Picasso
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Accounts

import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.json.JSONException
import org.json.JSONObject

import io.github.takusan23.Kaisendon.Preference_ApplicationContext
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.Activity.UserFollowActivity
import okhttp3.OkHttpClient

class Account_Fragment : Fragment() {

    internal var display_name: String? = null
    internal var user_account_id: String? = null
    internal var avater_url: String? = null
    internal var heander_url: String? = null
    internal var profile: String? = null
    internal var create_at: String? = null

    internal var account_id_button: Long = 0

    internal var follow: Int = 0
    internal var follower: Int = 0

    private val dialog: ProgressDialog? = null

    internal lateinit var view: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        view = inflater.inflate(R.layout.activity_user_old, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pref_setting = getDefaultSharedPreferences(context)

        val handler_1 = android.os.Handler()

        //先に
        val displayname_textview = view.findViewById<TextView>(R.id.username)
        val id_textview = view.findViewById<TextView>(R.id.account_id)
        val profile_textview = view.findViewById<TextView>(R.id.profile)

        //画像
        val avater = view.findViewById<ImageView>(R.id.avater_user)
        val header = view.findViewById<ImageView>(R.id.header_user)

        //ボタン
        val follower_button = view.findViewById<Button>(R.id.follower_button)
        val follow_button = view.findViewById<Button>(R.id.follow_button)


        //プリファレンス
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


        /*
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("ユーザー情報を取得中");
        dialog.show();
*/
        //くるくる
        //ProgressDialog API 26から非推奨に

        val snackbar = Snackbar.make(view, "ユーザー情報を取得中", Snackbar.LENGTH_INDEFINITE)
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

        //非同期通信でアカウント情報を取得
        val finalInstance = Instance
        val finalAccessToken = AccessToken

        //Icon
        val back_icon = BitmapFactory.decodeResource(resources, R.drawable.baseline_arrow_back_black_24dp)

        //どうでもいい
        //
        //        SpannableString spannableString = new SpannableString("アイコンテスト : ");
        //        spannableString.setSpan(new ImageSpan(AccountActivity.this, R.mipmap.ic_launcher), 7, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //        displayname_textview.setText(spannableString);
        //

        val asyncTask = object : AsyncTask<String, Void, String>() {
            override fun doInBackground(vararg string: String): String? {

                val client = MastodonClient.Builder(finalInstance!!, OkHttpClient.Builder(), Gson())
                        .accessToken(finalAccessToken!!)
                        .build()

                try {

                    val account = Accounts(client).getVerifyCredentials().execute()

                    display_name = account.displayName
                    user_account_id = account.userName
                    profile = account.note
                    avater_url = account.avatar
                    heander_url = account.header
                    create_at = account.createdAt

                    follow = account.followingCount
                    follower = account.followersCount

                    account_id_button = account.id

                    handler_1.post {
                        displayname_textview.text = display_name
                        displayname_textview.textSize = 20f
                        id_textview.text = "@$user_account_id@$finalInstance\r\n$create_at"
                        profile_textview.text = Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT)


                        follow_button.text = "フォロー : $follow"
                        follower_button.text = "フォロワー : $follower"

                        val setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false)
                        if (setting_avater_gif) {

                            //GIFアニメ再生させない
                            Picasso.get()
                                    .load(avater_url)
                                    .into(avater)

                            Picasso.get()
                                    .load(heander_url)
                                    .into(header)

                        } else {

                            //GIFアニメを再生
                            Glide.with(context!!)
                                    .load(avater_url)
                                    .into(avater)

                            Glide.with(context!!)
                                    .load(heander_url)
                                    .into(header)
                        }
                    }


                    //friends.nicoモードかな？
                    val frenico_mode = pref_setting.getBoolean("setting_friends_nico_mode", true)
                    //Chrome Custom Tab
                    val chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true)

                    val nico_url = arrayOf<String>()

                    //Json解析して"nico_url"取得
                    val account_nico_url = Accounts(client).getVerifyCredentials().doOnJson { jsonString ->
                        //System.out.println(jsonString);
                        //String string_ = "{\"int array\":[100,200,300],\"boolean\":true,\"string\":\"string\",\"object\":{\"object_1\":1,\"object_3\":3,\"object_2\":2},\"null\":null,\"array\":[1,2,3],\"long\":18000305032230531,\"int\":100,\"double\":10.5}";
                        val parser = JsonParser()
                        var jsonObject: JSONObject? = null
                        try {
                            jsonObject = JSONObject(jsonString)

                            nico_url[0] = jsonObject.getString("nico_url")

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }


                    }.execute()


                    //URLあるよ
                    if (frenico_mode && nico_url[0] != "null") {
                        //ニコニコURLへ
                        val button = view.findViewById<Button>(R.id.button3)
                        activity!!.runOnUiThread { button.text = "ニコニコ" }

                        button.setOnClickListener {
                            if (chrome_custom_tabs) {

                                val custom = CustomTabsHelper.getPackageNameToUse(context)

                                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                                val customTabsIntent = builder.build()
                                customTabsIntent.intent.setPackage(custom)
                                customTabsIntent.launchUrl(context as Activity?, Uri.parse(nico_url[0]))
                                //無効
                            } else {
                                val uri = Uri.parse(nico_url[0])
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            }
                        }
                        //URLなかった
                    } else {
                        val button = view.findViewById<Button>(R.id.button3)
                        activity!!.runOnUiThread { button.text = "Web" }
                        button.setOnClickListener {
                            if (chrome_custom_tabs) {

                                val custom = CustomTabsHelper.getPackageNameToUse(context)

                                val builder = CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true)
                                val customTabsIntent = builder.build()
                                customTabsIntent.intent.setPackage(custom)
                                customTabsIntent.launchUrl(context as Activity?, Uri.parse("https://$finalInstance/@$user_account_id"))
                                //無効
                            } else {
                                val uri = Uri.parse(nico_url[0])
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$finalInstance/@$user_account_id"))
                                startActivity(intent)
                            }
                        }
                    }


                } catch (e: Mastodon4jRequestException) {
                    e.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(result: String) {
                //くるくるを終了
                //dialog.dismiss();
                snackbar.dismiss()
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        //ボタンクリック
        follow_button.setOnClickListener {
            val follow = Intent(context, UserFollowActivity::class.java)
            follow.putExtra("account_id", account_id_button)
            follow.putExtra("follow_follower", true)
            startActivity(follow)
        }

        follower_button.setOnClickListener {
            val follower = Intent(context, UserFollowActivity::class.java)
            follower.putExtra("account_id", account_id_button)
            follower.putExtra("follow_follower", false)
            startActivity(follower)
        }
        /*

        //実験用
        Button button = view.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setShowTitle(true).setCloseButtonIcon(back_icon);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getContext(), Uri.parse("https://" + finalInstance + "/" + "@" + user_account_id));
            }
        });
*/

    }


}
