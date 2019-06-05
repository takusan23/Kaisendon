package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.ListItem
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SimpleAdapter
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MisskeyDriveBottomDialog : BottomSheetDialogFragment() {
    private var listView: ListView? = null
    private var pref_setting: SharedPreferences? = null
    private var toot_list: ArrayList<ListItem>? = null
    private var adapter: SimpleAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.misskey_drive_layout, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        listView = view.findViewById(R.id.misskey_drive_listview)
        ok_Button = view.findViewById(R.id.misskey_drive_ok_button)
        toot_list = ArrayList()
        adapter = SimpleAdapter(context!!, R.layout.timeline_item, toot_list!!)
        //取得
        getMisskeyDrive()
        //閉じる
        ok_Button.setOnClickListener { this@MisskeyDriveBottomDialog.dismiss() }
    }

    /**
     * Misskey Drive 取得
     */
    private fun getMisskeyDrive() {
        val instance = pref_setting!!.getString("misskey_main_instance", "")
        val token = pref_setting!!.getString("misskey_main_token", "")
        val username = pref_setting!!.getString("misskey_main_username", "")
        //URL
        val url = "https://$instance/api/drive/files"
        val jsonObject = JSONObject()
        try {
            jsonObject.put("limit", 100)
            jsonObject.put("i", token)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        println(jsonObject.toString())
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
                    setJSONParse(response_string)
                }
            }
        })
    }

    /**
     * Misskey Drive JSON Parse
     */
    private fun setJSONParse(response: String) {

        try {
            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getString("id")
                val url = jsonObject.getString("url")
                //配列を作成
                val Item = ArrayList<String>()
                //メモとか通知とかに
                Item.add("misskey_drive")
                //内容
                Item.add(url)
                //ユーザー名
                Item.add("")
                //時間、クライアント名等
                Item.add(null!!)
                //Toot ID 文字列版
                Item.add(id)
                //アバターURL
                Item.add(url)
                //アカウントID
                Item.add("")
                //ユーザーネーム
                Item.add("")
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
                    adapter!!.add(listItem)
                    adapter!!.notifyDataSetChanged()
                    listView!!.adapter = adapter
                }
                ///snackbar.dismiss();

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }


    }

    companion object {

        lateinit var ok_Button: Button
    }
}
