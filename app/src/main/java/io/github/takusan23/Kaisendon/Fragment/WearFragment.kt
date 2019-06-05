package io.github.takusan23.Kaisendon.Fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import io.github.takusan23.Kaisendon.Activity.WearTootShortcutListActivity
import io.github.takusan23.Kaisendon.R
import java.util.concurrent.ExecutionException


class WearFragment : Fragment() {
    internal lateinit var view: View

    private var accountTransportButton: Button? = null
    private var pref_setting: SharedPreferences? = null
    private var toot_shortcut_button: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        view = inflater.inflate(R.layout.fragment_wear, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //設定のプリファレンス
        pref_setting = getDefaultSharedPreferences(context)

        accountTransportButton = view.findViewById(R.id.account_transport_button)
        toot_shortcut_button = view.findViewById(R.id.toot_shortcut_setting)
        //アクセストークンを変更してる場合のコード
        //アクセストークン
        var AccessToken: String? = null
        //インスタンス
        var Instance: String? = null

        val accessToken_boomelan = pref_setting!!.getBoolean("pref_advanced_setting_instance_change", false)
        if (accessToken_boomelan) {
            AccessToken = pref_setting!!.getString("pref_mastodon_accesstoken", "")
            Instance = pref_setting!!.getString("pref_mastodon_instance", "")
        } else {
            AccessToken = pref_setting!!.getString("main_token", "")
            Instance = pref_setting!!.getString("main_instance", "")
        }

        //タイトル
        activity!!.setTitle(R.string.kaisendon_wear)

        //アカウント転送ボタン
        val finalInstance = Instance
        val finalAccessToken = AccessToken
        accountTransportButton!!.setOnClickListener {
            sendWearDeviceText("/instance", finalInstance!!)
            sendWearDeviceText("/token", finalAccessToken!!)
            sendWearDeviceText("/finish", "finish")
        }

        toot_shortcut_button!!.setOnClickListener {
            val intent = Intent(context, WearTootShortcutListActivity::class.java)
            startActivity(intent)
        }

    }

    private fun sendWearDeviceText(name: String, message: String) {
        Thread(Runnable {
            //Node(接続先？)検索
            val nodeListTask = Wearable.getNodeClient(context!!).connectedNodes
            try {
                val nodes = Tasks.await(nodeListTask)
                for (node in nodes) {
                    //sendMessage var1 は名前
                    //sendMessage var2 はメッセージ
                    val sendMessageTask = Wearable.getMessageClient(context!!).sendMessage(node.id, name, message.toByteArray())

                    val result = Tasks.await(sendMessageTask)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }).start()
    }
}
