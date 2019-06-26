package io.github.takusan23.Kaisendon.Fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.github.takusan23.Kaisendon.ListItem
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SimpleAdapter
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class AccountListFragment : Fragment() {

    private var mastodon_listView: ListView? = null
    private var misskey_listView: ListView? = null
    private var mastodon_list: ArrayList<ListItem>? = null
    private var misskey_list: ArrayList<ListItem>? = null
    private var mastodon_adapter: SimpleAdapter? = null
    private var misskey_adapter: SimpleAdapter? = null
    private var pref_setting: SharedPreferences? = null
    private val mastodon_instance_list: ArrayList<String>? = null
    private val mastodon_access_token_list: ArrayList<String>? = null
    private val misskey_instance_list: ArrayList<String>? = null
    private val misskey_access_token_list: ArrayList<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_account_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        mastodon_listView = view.findViewById(R.id.mastodon_account_list_listview)
        misskey_listView = view.findViewById(R.id.misskey_account_list_listview)
        mastodon_list = ArrayList()
        misskey_list = ArrayList()
        mastodon_adapter = SimpleAdapter(context!!, R.layout.timeline_item, mastodon_list!!)
        misskey_adapter = SimpleAdapter(context!!, R.layout.timeline_item, misskey_list!!)
        //タイトル
        activity!!.setTitle(R.string.account_list)
        //Mastodonアカウント
        if (pref_setting!!.getString("instance_list", "")!!.length >= 1) {
            loadMastodonAccount()
        }
        //Misskeyアカウント
        if (pref_setting!!.getString("misskey_instance_list", "")!!.length >= 1) {
            loadMisskeyAccount()
        }

    }

    /**
     * Mastodon アカウント読み込み
     */
    private fun loadMastodonAccount() {
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()
        val instance_instance_string = pref_setting!!.getString("instance_list", "")
        val account_instance_string = pref_setting!!.getString("access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance.add(instance_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        for (i in multi_account_instance.indices) {
            //Request
            //作成
            val request = Request.Builder()
                    .url("https://" + multi_account_instance[i] + "/api/v1/accounts/verify_credentials?access_token=" + multi_account_access_token[i])
                    .get()
                    .build()
            //GETリクエスト
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    //失敗
                    activity!!.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val response_string = response.body()!!.string()
                    if (!response.isSuccessful) {
                        //失敗
                        activity!!.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        try {
                            val jsonObject = JSONObject(response_string)
                            val note = jsonObject.getString("note")
                            val display_name = jsonObject.getString("display_name")
                            val acct = jsonObject.getString("acct")
                            val avatar = jsonObject.getString("avatar")
                            val account_id = jsonObject.getString("id")
                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add("account_list")
                            //内容
                            Item.add(note)
                            //ユーザー名
                            Item.add("$display_name @$acct")
                            //時間、クライアント名等
                            Item.add("")
                            //Toot ID 文字列版
                            Item.add("")
                            //アバターURL
                            Item.add(avatar)
                            //アカウントID
                            Item.add(account_id)
                            //ユーザーネーム
                            Item.add(display_name)
                            //メディア
                            Item.add("")
                            Item.add("")
                            Item.add("")
                            Item.add("")
                            //カード
                            Item.add("")
                            Item.add("")
                            Item.add("")
                            Item.add("")
                            val listItem = ListItem(Item)
                            activity!!.runOnUiThread{
                                mastodon_adapter!!.add(listItem)
                                mastodon_adapter!!.notifyDataSetChanged()
                                mastodon_listView!!.adapter = mastodon_adapter
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }
            })
        }
        //長押しで消せるように
        mastodon_listView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            //確認
            Snackbar.make(view, getString(R.string.account_delete_message), Snackbar.LENGTH_SHORT).setAction(getString(R.string.delete_ok)) {
                //削除
                //配列から要素を消す
                multi_account_instance.removeAt(position)
                multi_account_access_token.removeAt(position)
                //JSONArray
                val instance_array = JSONArray()
                val access_array = JSONArray()
                for (i in multi_account_instance.indices) {
                    instance_array.put(multi_account_instance[i])
                }
                for (i in multi_account_access_token.indices) {
                    access_array.put(multi_account_access_token[i])
                }
                //書き込む
                val editor = pref_setting!!.edit()
                editor.putString("instance_list", instance_array.toString())
                editor.putString("access_list", access_array.toString())
                editor.apply()
                Toast.makeText(context, getString(R.string.delete), Toast.LENGTH_SHORT).show()
                //再読み込み
                mastodon_adapter!!.clear()
                loadMastodonAccount()
            }.show()
            false
        }
    }

    /**
     * Misskey アカウント読み込み
     */
    private fun loadMisskeyAccount() {
        val multi_account_instance = ArrayList<String>()
        val multi_account_access_token = ArrayList<String>()
        val instance_instance_string = pref_setting!!.getString("misskey_instance_list", "")
        val account_instance_string = pref_setting!!.getString("misskey_access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    multi_account_access_token.add(access_array.getString(i))
                    multi_account_instance.add(instance_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        //System.out.println(multi_account_access_token);
        for (i in multi_account_instance.indices) {
            //作成
            val jsonObject = JSONObject()
            try {
                jsonObject.put("i", multi_account_access_token[i])
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
            val request = Request.Builder()
                    .url("https://" + multi_account_instance[i] + "/api/i")
                    .post(requestBody)
                    .build()
            //GETリクエスト
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    //失敗
                    activity!!.runOnUiThread { Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val response_string = response.body()!!.string()
                    //System.out.println(response_string);
                    if (!response.isSuccessful) {
                        //失敗
                        activity!!.runOnUiThread { Toast.makeText(context, getString(R.string.error) + "\n" + response.code().toString(), Toast.LENGTH_SHORT).show() }
                    } else {
                        try {
                            val jsonObject = JSONObject(response_string)
                            val note = jsonObject.getString("description")
                            val display_name = jsonObject.getString("name")
                            val acct = jsonObject.getString("username")
                            val avatar = jsonObject.getString("avatarUrl")
                            val account_id = jsonObject.getString("id")
                            //配列を作成
                            val Item = ArrayList<String>()
                            //メモとか通知とかに
                            Item.add("account_list")
                            //内容
                            Item.add(note)
                            //ユーザー名
                            Item.add("$display_name @$acct")
                            //時間、クライアント名等
                            Item.add(null!!)
                            //Toot ID 文字列版
                            Item.add(null!!)
                            //アバターURL
                            Item.add(avatar)
                            //アカウントID
                            Item.add(account_id)
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
                            val listItem = ListItem(Item)
                            activity!!.runOnUiThread {
                                misskey_adapter!!.add(listItem)
                                misskey_adapter!!.notifyDataSetChanged()
                                misskey_listView!!.adapter = misskey_adapter
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }
            })
        }
        //長押しで消せるように
        misskey_listView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            //確認
            Snackbar.make(view, getString(R.string.account_delete_message), Snackbar.LENGTH_SHORT).setAction(getString(R.string.delete_ok)) {
                //削除
                //配列から要素を消す
                multi_account_instance.removeAt(position)
                multi_account_access_token.removeAt(position)
                //JSONArray
                val instance_array = JSONArray()
                val access_array = JSONArray()
                for (i in multi_account_instance.indices) {
                    instance_array.put(multi_account_instance[i])
                }
                for (i in multi_account_access_token.indices) {
                    access_array.put(multi_account_access_token[i])
                }
                //書き込む
                val editor = pref_setting!!.edit()
                editor.putString("misskey_instance_list", instance_array.toString())
                editor.putString("misskeyaccess_list", access_array.toString())
                editor.apply()
                Toast.makeText(context, "削除しました", Toast.LENGTH_SHORT).show()
                //再読み込み
                misskey_adapter!!.clear()
                loadMisskeyAccount()
            }.show()
            false
        }
    }

}
