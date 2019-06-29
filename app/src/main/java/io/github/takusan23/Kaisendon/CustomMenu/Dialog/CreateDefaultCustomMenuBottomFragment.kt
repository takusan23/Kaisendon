package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSQLiteHelper
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.create_dafault_custommenu_bottom_fragment_layout.*
import org.json.JSONException
import org.json.JSONObject

class CreateDefaultCustomMenuBottomFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.create_dafault_custommenu_bottom_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        create_custommenu_create_button.setOnClickListener {
            val homecard = Intent(context, Home::class.java)
            startActivity(homecard)
        }
    }

    fun setCreateCustomMenu(){
        val instance = arguments?.getString("name")
        val token = arguments?.getString("token")
        if(context!=null){
            val customMenuSQLiteHelper = CustomMenuSQLiteHelper(context!!)
            val db = customMenuSQLiteHelper.writableDatabase
            db.disableWriteAheadLogging()
            val values = ContentValues()

            val content = arrayListOf("${getString(R.string.home)} : ", "${getString(R.string.notifications)} : ", "${getString(R.string.public_time_line)} : ")
            val url = arrayListOf("/api/v1/timelines/home", "/api/v1/notifications", "/api/v1/timelines/public?local=true")
            for (i in content) {
                //JSON化
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("misskey", "false")
                    jsonObject.put("name", "${i}${instance}")
                    jsonObject.put("memo", "")
                    jsonObject.put("content", url[content.indexOf(i)])
                    jsonObject.put("instance", instance)
                    jsonObject.put("access_token", token)
                    jsonObject.put("image_load", "false")
                    jsonObject.put("dialog", "false")
                    jsonObject.put("dark_mode", "false")
                    jsonObject.put("position", "")
                    jsonObject.put("streaming", "true") //反転させてONのときStereaming有効に
                    jsonObject.put("subtitle", "")
                    jsonObject.put("image_url", "")
                    jsonObject.put("background_transparency", "")
                    jsonObject.put("background_screen_fit", "false")
                    jsonObject.put("quick_profile", "true")
                    jsonObject.put("toot_counter", "false")
                    jsonObject.put("custom_emoji", "true")
                    jsonObject.put("gif", "false")
                    jsonObject.put("font", "")
                    jsonObject.put("one_hand", "false")
                    jsonObject.put("misskey_username", "")
                    jsonObject.put("no_fav_icon", null)
                    jsonObject.put("yes_fav_icon", null)
                    jsonObject.put("setting", "")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                values.put("name", "${i}")
                values.put("setting", jsonObject.toString())
                db.insert("custom_menudb", null, values)
            }
        }

    }
}