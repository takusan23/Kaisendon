package io.github.takusan23.Kaisendon.Fragment

import android.app.ProgressDialog
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import io.github.takusan23.Kaisendon.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class InstanceInfo_Fragment : Fragment() {

    internal var instance_name: String? = null
    internal var instance_user: String? = null
    internal var instance_total_toot: String? = null
    internal var instance_description: String? = null


    internal lateinit var view: View
    private val dialog: ProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        view = inflater.inflate(R.layout.activity_instance_info, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pref_setting = getDefaultSharedPreferences(context)


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

        //        getActivity().getActionBar().setTitle(R.string.instance_info);
        //        getActivity().getActionBar().setSubtitle(Instance);
        activity!!.setTitle(R.string.instance_info)


        //背景
        val background_imageView = view.findViewById<ImageView>(R.id.instanceinfo_background_imageview)

        if (pref_setting.getBoolean("background_image", true)) {
            val uri = Uri.parse(pref_setting.getString("background_image_path", ""))
            Glide.with(context!!)
                    .load(uri)
                    .into(background_imageView)
        }

        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f).toDouble() != 0.1) {
            background_imageView.alpha = pref_setting.getFloat("transparency", 1.0f)
        }


        //くるくる
        /*
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("インスタンス情報取得中 \r\n /api/v1/instance \r\n /api/v1/instance/activity");
        dialog.show();
*/
        //くるくる
        //ProgressDialog API 26から非推奨に
        val snackbar = Snackbar.make(view, getString(R.string.loading_instance_info) + "\r\n /api/v1/instance \r\n /api/v1/instance/activity", Snackbar.LENGTH_INDEFINITE)
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

        //非同期通信
        val finalInstance = Instance
        val finalAccessToken = AccessToken
        val finalInstance1 = Instance
        val asyncTask = object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg string: String): String? {

                var url: URL? = null
                var connection: HttpURLConnection? = null
                val url_link = "https://$finalInstance/api/v1/instance/"

                try {

                    url = URL(url_link)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    val `in` = connection.inputStream
                    var encoding: String? = connection.contentEncoding
                    if (null == encoding) {
                        encoding = "UTF-8"
                    }
                    val inReader = InputStreamReader(`in`, encoding)
                    val bufReader = BufferedReader(inReader)
                    val response = StringBuilder()
                    var line: String? = null
/*
                    // 1行ずつ読み込む
                    while ((line = bufReader.readLine()) != null) {
                        response.append(line)
                    }
*/
                    bufReader.close()
                    inReader.close()
                    `in`.close()

                    // 受け取ったJSON文字列をパース
                    val jsonObject = JSONObject(response.toString())
                    //Status
                    val stats = jsonObject.getJSONObject("stats")

                    //タイトル
                    val title = jsonObject.getString("title")
                    //説明
                    val description = jsonObject.getString("description")
                    //バージョン？
                    val version = jsonObject.getString("version")
                    //ユーザー数
                    val user_total = stats.getString("user_count")
                    //トータルトゥート
                    val toot_total = stats.getString("status_count")

                    if (activity != null) {
                        //UI変更
                        activity!!.runOnUiThread {
                            val title_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_public_black_24dp, null)
                            title_icon!!.setBounds(0, 0, title_icon.intrinsicWidth, title_icon.intrinsicHeight)
                            val description_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_description_black_24dp, null)
                            description_icon!!.setBounds(0, 0, description_icon.intrinsicWidth, description_icon.intrinsicHeight)
                            val user_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_people_black_24dp, null)
                            user_icon!!.setBounds(0, 0, user_icon.intrinsicWidth, user_icon.intrinsicHeight)
                            val toot_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_create_black_24dp_black, null)
                            toot_icon!!.setBounds(0, 0, toot_icon.intrinsicWidth, toot_icon.intrinsicHeight)

                            val instance_tile = activity!!.findViewById<TextView>(R.id.instance_name)
                            val instance_description = activity!!.findViewById<TextView>(R.id.instance_description)
                            val instance_user = activity!!.findViewById<TextView>(R.id.instance_user)
                            val instance_toot_total = activity!!.findViewById<TextView>(R.id.instance_total_toot)

                            instance_tile.text = getString(R.string.instance_name) + " : " + title + " (" + version + ")"
                            instance_tile.setCompoundDrawables(title_icon, null, null, null)

                            instance_description.text = getString(R.string.instance_description) + " : " + description
                            instance_description.setCompoundDrawables(description_icon, null, null, null)

                            instance_user.text = getString(R.string.instance_user_count) + " : " + user_total
                            instance_user.setCompoundDrawables(user_icon, null, null, null)

                            instance_toot_total.text = getString(R.string.instance_status_count) + " : " + toot_total
                            instance_toot_total.setCompoundDrawables(toot_icon, null, null, null)
                        }
                    }

                    return null

                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                } catch (e: JSONException) {
                    e.printStackTrace()
                    return null
                } finally {
                    connection?.disconnect()
                }
            }

            override fun onPostExecute(result: String) {

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        //アクティブ?
        val asyncTask_active = object : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg string: String): String? {

                var url: URL? = null
                var connection: HttpURLConnection? = null
                val url_link = "https://$finalInstance/api/v1/instance/activity"

                try {

                    url = URL(url_link)
                    connection = url!!.openConnection() as HttpURLConnection
                    connection!!.connect()

                    val `in` = connection!!.inputStream
                    var encoding: String? = connection!!.contentEncoding
                    if (null == encoding) {
                        encoding = "UTF-8"
                    }
                    val inReader = InputStreamReader(`in`, encoding!!)
                    val bufReader = BufferedReader(inReader)
                    val response = StringBuilder()
                    var line: String? = null
//                    // 1行ずつ読み込む
//                    while ((line = bufReader.readLine()) != null) {
//                        response.append(line)
//                    }
                    bufReader.close()
                    inReader.close()
                    `in`.close()

                    // 受け取ったJSON文字列をパース
                    //                    JSONObject jsonObject = new JSONObject(response.toString());
                    val datas = JSONArray(response.toString())
                    //Status
                    val stats = datas.getJSONObject(0)

                    //すてーたす
                    val toot_total = stats.getString("statuses")
                    //ログイン
                    val user_login = stats.getString("logins")
                    //登録?
                    val registrations = stats.getString("registrations")


                    if (activity != null) {
                        //UI変更
                        activity!!.runOnUiThread {
                            val title_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_timeline_black_24dp, null)
                            title_icon!!.setBounds(0, 0, title_icon.intrinsicWidth, title_icon.intrinsicHeight)
                            val description_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_create_black_24dp_black, null)
                            description_icon!!.setBounds(0, 0, description_icon.intrinsicWidth, description_icon.intrinsicHeight)
                            val user_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_people_black_24dp, null)
                            user_icon!!.setBounds(0, 0, user_icon.intrinsicWidth, user_icon.intrinsicHeight)
                            val active_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_whatshot_black_24dp, null)
                            active_icon!!.setBounds(0, 0, active_icon.intrinsicWidth, active_icon.intrinsicHeight)
                            val people_icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_person_add_black_24dp, null)
                            people_icon!!.setBounds(0, 0, people_icon.intrinsicWidth, people_icon.intrinsicHeight)

                            val instance_active_title = activity!!.findViewById<TextView>(R.id.instance_active_title)
                            val instance_active_toot = activity!!.findViewById<TextView>(R.id.instance_active_toot)
                            val instance_active_longins = activity!!.findViewById<TextView>(R.id.instance_active_longins)
                            val instance_active_registrations = activity!!.findViewById<TextView>(R.id.instance_active_registrations)

                            instance_active_title.text = getString(R.string.instance_statistics) + "(" + getString(R.string.this_week) + ")"
                            instance_active_title.setCompoundDrawables(title_icon, null, null, null)

                            instance_active_toot.text = getString(R.string.instance_statistics_status) + "\r\n" + toot_total
                            instance_active_toot.setCompoundDrawables(user_icon, null, null, null)

                            instance_active_longins.text = getString(R.string.instance_statistics_login) + "\r\n" + user_login
                            instance_active_longins.setCompoundDrawables(active_icon, null, null, null)

                            instance_active_registrations.text = getString(R.string.instance_statistics_registrations) + "\r\n" + registrations
                            instance_active_registrations.setCompoundDrawables(people_icon, null, null, null)
                        }
                    }

                    return null

                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                } catch (e: JSONException) {
                    e.printStackTrace()
                    return null
                } finally {
                    if (connection != null) {
                        connection!!.disconnect()
                    }
                }
            }

            override fun onPostExecute(result: String) {
                //くるくるを終了
                //dialog.dismiss();
                snackbar.dismiss()
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


    }
}