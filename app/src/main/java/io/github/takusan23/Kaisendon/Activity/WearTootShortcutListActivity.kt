package io.github.takusan23.Kaisendon.Activity


import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

import org.json.JSONArray
import org.json.JSONException

import java.util.ArrayList
import java.util.concurrent.ExecutionException

import io.github.takusan23.Kaisendon.ListItem
import io.github.takusan23.Kaisendon.Preference_ApplicationContext
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SimpleAdapter


class WearTootShortcutListActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener, MenuBuilder.Callback {

    private var pref_setting: SharedPreferences? = null
    private var listView: ListView? = null
    private var add_button: Button? = null
    private var send_button: Button? = null
    private var area_button: Button? = null
    private var editText: EditText? = null

    private var toot_area = "public"

    private var adapter: SimpleAdapter? = null

    private val toot_list = ArrayList<String>()
    private val icon_list = ArrayList<String>()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_wear_toot_shortcut_list)
        title = getString(R.string.toot_shortcut_setting)

        pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        listView = findViewById(R.id.toot_shortcut_listview)
        add_button = findViewById(R.id.toot_shortcut_add_toot)
        area_button = findViewById(R.id.toot_shortcut_toot_area)
        send_button = findViewById(R.id.toot_shortcut_send)
        editText = findViewById(R.id.toot_shortcut_edittext)

        //ListView
        //メインアカウント
        val toot = ArrayList<ListItem>()
        adapter = SimpleAdapter(this@WearTootShortcutListActivity, R.layout.timeline_item, toot)

        //送信
        send_button!!.setOnClickListener {
            sendWearDeviceText("/clear", "clear")
            if (toot_list.size != 0) {
                //for
                //JSONArrayを投げる
                val text_array = JSONArray()
                val icon_array = JSONArray()
                for (i in toot_list.indices) {
                    text_array.put(toot_list[i])
                    icon_array.put(icon_list[i])
                }
                sendWearDeviceText("/toot_text", text_array.toString())
                sendWearDeviceText("/toot_icon", icon_array.toString())
                sendWearDeviceText("/finish", "finish")
            }
        }

        //公開範囲
        val menuBuilder = MenuBuilder(this@WearTootShortcutListActivity)
        val inflater = MenuInflater(this@WearTootShortcutListActivity)
        inflater.inflate(R.menu.toot_area_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(this@WearTootShortcutListActivity, menuBuilder, area_button!!)
        optionsMenu.setForceShowIcon(true)
        area_button!!.setOnClickListener {
            optionsMenu.show()
            menuBuilder.setCallback(this@WearTootShortcutListActivity)
        }

        //リストに追加
        add_button!!.setOnClickListener {
            //EditText
            val edittext_text = editText!!.text.toString()
            //List追加
            toot_list.add(edittext_text)
            icon_list.add(toot_area)
            //ListView更新
            adapter!!.clear()
            for (i in icon_list.indices) {
                setListView(i)
            }
        }

        //リストから消す
        //長押しで対応
        listView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            //削除できるように
            //Snackberで警告
            Snackbar.make(view, getString(R.string.toot_shortcut_delete), Snackbar.LENGTH_SHORT).setAction(getString(R.string.delete_ok)) {
                //削除選択時
                //リストから消す
                toot_list.removeAt(position)
                icon_list.removeAt(position)
                //ListView再読込
                if (toot_list.size == icon_list.size) {
                    adapter!!.clear()
                    for (i in toot_list.indices) {
                        setListView(i)
                    }
                }
            }.show()
            false
        }


    }


    public override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    public override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    //WearOS端末からリストを受け取る
    override fun onMessageReceived(messageEvent: MessageEvent) {
        //空にする
        adapter!!.clear()
        //Text
        if (messageEvent.path.contains("/toot_text")) {
            //JSONParse
            try {
                val text_array = JSONArray(String(messageEvent.data))
                for (i in 0 until text_array.length()) {
                    toot_list.add(text_array.get(i) as String)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        //Icon
        if (messageEvent.path.contains("/toot_icon")) {
            //JSONParse
            try {
                val icon_array = JSONArray(String(messageEvent.data))
                for (i in 0 until icon_array.length()) {
                    icon_list.add(icon_array.get(i) as String)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        //終わり
        if (messageEvent.path.contains("/finish")) {
            //ListViewに入れる
            //なんか数が合わないときがよくある
            if (icon_list.size == toot_list.size) {
                for (i in icon_list.indices) {
                    setListView(i)
                }
            } else {
                Toast.makeText(this@WearTootShortcutListActivity, getString(R.string.size_error), Toast.LENGTH_SHORT).show()
                toot_list.clear()
                icon_list.clear()
            }

        }
    }

    //ListViewに入れる
    private fun setListView(count: Int) {
        //配列を作成
        val Item = ArrayList<String>()
        //メモとか通知とかに
        Item.add("toot_shortcut")
        //内容
        Item.add(toot_list[count])
        //ユーザー名
        Item.add("")
        //時間、クライアント名等
        Item.add("")
        //Toot ID 文字列版
        Item.add("")
        //アバターURL
        Item.add("toot_shortcut " + icon_list[count])
        //アカウントID
        Item.add("0")
        //ユーザーネーム
        Item.add("")
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
        runOnUiThread {
            adapter!!.add(listItem)
            adapter!!.notifyDataSetChanged()
            listView!!.adapter = adapter
        }
    }

    private fun sendWearDeviceText(name: String, message: String) {
        Thread(Runnable {
            //Node(接続先？)検索
            val nodeListTask = Wearable.getNodeClient(this@WearTootShortcutListActivity).connectedNodes
            try {
                val nodes = Tasks.await(nodeListTask)
                for (node in nodes) {
                    //sendMessage var1 は名前
                    //sendMessage var2 はメッセージ
                    val sendMessageTask = Wearable.getMessageClient(this@WearTootShortcutListActivity).sendMessage(node.id, name, message.toByteArray())

                    val result = Tasks.await(sendMessageTask)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }).start()
    }

    //公開範囲
    override fun onMenuItemSelected(menuBuilder: MenuBuilder, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.toot_area_public -> {
                toot_area = "public"
                area_button!!.text = getString(R.string.visibility_public)
                area_button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0)
            }
            R.id.toot_area_unlisted -> {
                toot_area = "unlisted"
                area_button!!.text = getString(R.string.visibility_unlisted)
                area_button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_black_24dp, 0, 0, 0)
            }
            R.id.toot_area_local -> {
                toot_area = "private"
                area_button!!.text = getString(R.string.visibility_private)
                area_button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open_black_24dp, 0, 0, 0)
            }
            R.id.toot_area_direct -> {
                toot_area = "direct"
                area_button!!.text = getString(R.string.visibility_direct)
                area_button!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0)
            }
        }
        return false
    }

    override fun onMenuModeChange(menuBuilder: MenuBuilder) {

    }
}
