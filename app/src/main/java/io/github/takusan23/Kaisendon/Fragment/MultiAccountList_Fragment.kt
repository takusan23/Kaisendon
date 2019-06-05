package io.github.takusan23.Kaisendon.Fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import com.sys1yagi.mastodon4j.api.method.Accounts
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.ListItem
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SimpleAdapter
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.util.*

class MultiAccountList_Fragment : Fragment() {

    //アクセストークン、インスタンス
    internal var AccessToken: String? = null
    internal var Instance: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.multi_accountlist_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val pref_setting = getDefaultSharedPreferences(context)

        val accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "")
            Instance = pref_setting.getString("pref_mastodon_instance", "")

        } else {

            AccessToken = pref_setting.getString("main_token", "")
            Instance = pref_setting.getString("main_instance", "")

        }

        //読み込み中
        val snackbar = Snackbar.make(view, "アカウント読み込み中 \r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE)
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


        val account_listView = view.findViewById<ListView>(R.id.accountlist_listview)

        //タイトル
        activity!!.setTitle(R.string.account_chenge)


        //メインアカウント
        val toot_list = ArrayList<ListItem>()

        val adapter = SimpleAdapter(context!!, R.layout.timeline_item, toot_list)

        val main_access_token = pref_setting.getString("main_token", "")
        val main_instance = pref_setting.getString("main_instance", "")

        val mHandler = Handler()

        //マルチアカウント
        //配列を使えば幸せになれそう！！！
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()

        //とりあえずPreferenceに書き込まれた値を
        val instance_instance_string = pref_setting.getString("instance_list", "")
        val account_instance_string = pref_setting.getString("access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance.add(instance_array.getString(i))
                }
            } catch (e: Exception) {

            }

        }

        val listItem = arrayOf(ListItem())

        for (count in multi_account_instance.indices) {
            val multi_instance = multi_account_instance[count]
            val multi_access_token = multi_account_access_token[count]
            object : AsyncTask<String, Void, ListItem>() {
                override fun doInBackground(vararg string: String): ListItem {
                    val client = MastodonClient.Builder(multi_instance, OkHttpClient.Builder(), Gson())
                            .accessToken(multi_access_token)
                            .build()

                    try {
                        val main_accounts = Accounts(client).getVerifyCredentials().execute()

                        val account_id = main_accounts.id
                        val display_name = main_accounts.displayName
                        val account_id_string = main_accounts.userName
                        val profile = main_accounts.note
                        val avater_url = main_accounts.avatar

                        var now_account: String? = null
                        if (multi_access_token == main_access_token) {
                            now_account = "now_account"
                        }


                        //配列を作成
                        val Item = ArrayList<String>()
                        //メモとか通知とかに
                        Item.add(now_account!!)
                        //内容
                        Item.add(profile)
                        //ユーザー名
                        Item.add("$display_name @$account_id_string")
                        //時間、クライアント名等
                        Item.add(null!!)
                        //Toot ID 文字列版
                        Item.add(null!!)
                        //アバターURL
                        Item.add(avater_url)
                        //アカウントID
                        Item.add(account_id.toString())
                        //ユーザーネーム
                        Item.add(display_name)
                        //メディア
                        Item.add(null!!)
                        Item.add(null!!)
                        Item.add(null!!)
                        Item.add(null!!)
                        //カード
                        Item.add(null!!)
                        Item.add(null!!)
                        Item.add(null!!)
                        Item.add(null!!)
                        listItem[0] = ListItem(Item)


                    } catch (e: Mastodon4jRequestException) {
                        e.printStackTrace()
                    }

                    return listItem[0]
                }

                override fun onPostExecute(result: ListItem) {
                    adapter.add(result)
                    adapter.notifyDataSetChanged()
                    val listView = view.findViewById<View>(R.id.accountlist_listview) as ListView
                    listView.adapter = adapter
                    snackbar.dismiss()
                }

            }.execute()

        }


        //アカウント切り替え
        val listView = view.findViewById<View>(R.id.accountlist_listview) as ListView

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val multi_instance = multi_account_instance[position]
            val multi_access_token = multi_account_access_token[position]

            val editor = pref_setting.edit()
            editor.putString("main_instance", multi_instance)
            editor.putString("main_token", multi_access_token)
            editor.apply()

            //アプリ再起動
            val intent = Intent(context, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        //長押しで消せるように
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            AlertDialog.Builder(activity)
                    .setTitle(R.string.account_delete_title)
                    .setMessage(R.string.account_delete_message)
                    .setPositiveButton(R.string.ok) { dialog, which ->
                        //配列から要素を消す
                        multi_account_instance.removeAt(position)
                        multi_account_access_token.removeAt(position)

                        //Preferenceも上書きする
                        //Preferenceに配列は保存できないのでJSON化して保存する
                        //Write
                        val instance_array = JSONArray()
                        val access_array = JSONArray()
                        for (i in multi_account_instance.indices) {
                            instance_array.put(multi_account_instance[i])
                        }
                        for (i in multi_account_access_token.indices) {
                            access_array.put(multi_account_access_token[i])
                        }

                        //書き込む
                        val editor = pref_setting.edit()
                        editor.putString("instance_list", instance_array.toString())
                        editor.putString("access_list", access_array.toString())
                        editor.apply()
                        Toast.makeText(context, "削除しました", Toast.LENGTH_SHORT).show()


                        //アプリ再起動
                        val intent = Intent(context, Home::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            /*
                int account_count = position + 1;
                //Toast.makeText(getContext(), "消す : " + String.valueOf(account_count), Toast.LENGTH_SHORT).show();
                //カウントが最大ではないとき（空間が開かないようにする）
*/
            /*
                if (account_count < pref_setting.getInt("account_count", 2)) {
                    int add_count = 0;

                    //while (account_count < pref_setting.getInt("account_count", 2)){
                        SharedPreferences.Editor editor = pref_setting.edit();
                        editor.putString("token" + String.valueOf(account_count + add_count), pref_setting.getString("token" + String.valueOf(account_count++ + add_count), ""));
                        editor.putString("instance" + String.valueOf(account_count + add_count), pref_setting.getString("instance" + String.valueOf(account_count++ + add_count), ""));
                        editor.apply();
                        //add_count = add_count + 1;
                    //}
                    //SharedPreferences.Editor editor = pref_setting.edit();
                    editor.remove("token" + String.valueOf(pref_setting.getInt("account_count", 2)));
                    editor.remove("instance" + String.valueOf(pref_setting.getInt("account_count", 2)));
                    editor.apply();

                    Toast.makeText(getContext(), "最大以外", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.remove("token" + String.valueOf(pref_setting.getInt("account_count", 2)));
                    editor.remove("instance" + String.valueOf(pref_setting.getInt("account_count", 2)));
                    editor.apply();

                }
*//*



                SharedPreferences.Editor editor = pref_setting.edit();
                editor.remove("token" + String.valueOf(account_count));
                editor.remove("instance" + String.valueOf(account_count));
                editor.apply();
*/


            false
        }


    }
}

