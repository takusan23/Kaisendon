package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.app.DownloadManager
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.bottom_fragment_account_change.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AccountChangeBottomSheetFragment : BottomSheetDialogFragment() {

    lateinit var pref_setting: SharedPreferences

    val mastodonInstanceNameList = arrayListOf<String>()
    val mastodonAccessTokenList = arrayListOf<String>()

    val misskeyInstanceNameList = arrayListOf<String>()
    val misskeyAccessTokenList = arrayListOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_fragment_account_change, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)

        //Mastodon
        getMastodonMultiAccount()
        //Misskey
        getMisskeyMultiAccount()
    }

    private fun getMastodonMultiAccount() {
        val instance_instance_string = pref_setting.getString("instance_list", "")
        val account_instance_string = pref_setting.getString("access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    mastodonAccessTokenList.add(access_array.getString(i))
                    mastodonInstanceNameList.add(instance_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        for (s in 0 until mastodonInstanceNameList.size) {
            val instance = mastodonInstanceNameList[s]
            val accessToken = mastodonAccessTokenList[s]
            //APIたたく（存在チェック）
            //friends.nico / knzk.me　とかはもう。。
            val request = Request.Builder()
                    .url("https://${instance}/api/v1/accounts/verify_credentials?access_token=${accessToken}")
                    .get()
                    .build()
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    //成功したら追加
                    val response_string = response.body()?.string()
                    if (response.isSuccessful) {
                        activity?.runOnUiThread {
                            val jsonObject = JSONObject(response_string)
                            val display_name = jsonObject.getString("display_name")
                            val acct = jsonObject.getString("acct")
                            //ボタン動的生成
                            val button = MaterialButton(context!!)
                            button.text = "$display_name @$acct@$instance"
                            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            val space = 20
                            (layoutParams as ViewGroup.MarginLayoutParams).setMargins(space, space, space, space)
                            button.layoutParams = layoutParams
                            account_change_main_linearlayout.addView(button)
                            //クリックしたらアカウント情報切り替え
                            button.setOnClickListener {
                                val editor = pref_setting.edit()
                                editor.putString("main_instance", instance)
                                editor.putString("main_token", accessToken)
                                editor.apply()
                                //閉じる＋TootCardView作り直し
                                this@AccountChangeBottomSheetFragment.dismiss()
                                if (activity is Home) {
                                    (activity as Home).tootCardView.init()
                                    (activity as Home).tootCardView.cardViewShow()
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {

                }
            })
        }
    }

    private fun getMisskeyMultiAccount() {
        val instance_instance_string = pref_setting.getString("misskey_instance_list", "")
        val account_instance_string = pref_setting.getString("misskey_access_list", "")
        if (instance_instance_string != "") {
            try {
                val instance_array = JSONArray(instance_instance_string)
                val access_array = JSONArray(account_instance_string)
                for (i in 0 until instance_array.length()) {
                    misskeyAccessTokenList.add(access_array.getString(i))
                    misskeyInstanceNameList.add(instance_array.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        for (s in 0 until misskeyInstanceNameList.size) {
            val instance = misskeyInstanceNameList[s]
            val accessToken = misskeyAccessTokenList[s]
            //JSON
            val jsonObject = JSONObject()
            jsonObject.put("i", accessToken)

            val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
            val request = Request.Builder()
                    .url("https://$instance/api/i")
                    .post(requestBody)
                    .build()
            //GETリクエスト
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    //せいこうすれば
                    val response_string = response.body()?.string()
                    if (response.isSuccessful) {
                        activity?.runOnUiThread {
                            val jsonObject = JSONObject(response_string)
                            val note = jsonObject.getString("description")
                            val display_name = jsonObject.getString("name")
                            val acct = jsonObject.getString("username")
                            //ボタン動的生成
                            val button = MaterialButton(context!!)
                            button.text = "$display_name @$acct@$instance"
                            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            (layoutParams as ViewGroup.MarginLayoutParams).setMargins(10, 10, 10, 10)
                            button.layoutParams = layoutParams
                            account_change_main_linearlayout.addView(button)
                            //クリックしたらアカウント情報切り替え
                            button.setOnClickListener {
                                val editor = pref_setting.edit()
                                editor.putString("misskey_main_instance", instance)
                                editor.putString("misskey_main_token", accessToken)
                                editor.putString("misskey_main_username", acct)
                                editor.apply()
                                //閉じる＋TootCardView作り直し
                                this@AccountChangeBottomSheetFragment.dismiss()
                                if (activity is Home) {
                                    (activity as Home).tootCardView.init()
                                    (activity as Home).tootCardView.cardViewShow()
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {

                }
            })
        }

    }

}