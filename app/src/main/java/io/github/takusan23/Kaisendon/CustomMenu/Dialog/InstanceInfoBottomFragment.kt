package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.Theme.DarkModeSupport
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class InstanceInfoBottomFragment : BottomSheetDialogFragment() {

    private var pref_setting: SharedPreferences? = null
    private var name_TextView: TextView? = null
    private var description_TextView: TextView? = null
    private var status_TextView: TextView? = null

    /*はじっこを丸くする*/
    override fun getTheme(): Int {
        var theme = R.style.BottomSheetDialogThemeAppTheme
        val darkModeSupport = DarkModeSupport(context!!)
        if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES){
            theme =  R.style.BottomSheetDialogThemeDarkTheme
        }
        return theme
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.instance_info_bottom_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context!!)
        //ダークモード対応
        val darkModeSupport = DarkModeSupport(context!!)
        darkModeSupport.setLayoutAllThemeColor(view as LinearLayout)
        name_TextView = view.findViewById(R.id.instance_info_name)
        description_TextView = view.findViewById(R.id.instance_info_description)
        status_TextView = view.findViewById(R.id.instance_info_status)
        getInstanceInfo()
    }

    /*インスタンス情報を叩く*/
    private fun getInstanceInfo() {
        val request = Request.Builder()
                .url("https://" + pref_setting!!.getString("main_instance", "") + "/api/v1/instance")
                .get()
                .build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
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
                        val stats = jsonObject.getJSONObject("stats")
                        val name = jsonObject.getString("title")
                        val description = jsonObject.getString("description")
                        val version = jsonObject.getString("version")
                        val user_count = stats.getString("user_count")
                        val status_count = stats.getString("status_count")
                        val domain_count = stats.getString("domain_count")
                        //UIすれっど
                        activity!!.runOnUiThread {
                            name_TextView!!.append("\n$name $version")
                            description_TextView!!.append("\n" + Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT))
                            status_TextView!!.append("\n" + getString(R.string.instance_user_count) + " : " + user_count)
                            status_TextView!!.append("\n" + getString(R.string.instance_status_count) + " : " + status_count)
                            status_TextView!!.append("\n" + getString(R.string.domain) + " : " + domain_count)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        })
    }
}
