package io.github.takusan23.Kaisendon.APIJSONParse

import android.content.Context
import android.preference.PreferenceManager
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import org.json.JSONException
import org.json.JSONObject

class MastodonAccountJSONParse(private val context: Context, response_string: String) {
    var acct: String? = null
        private set
    var display_name: String? = null
        private set
    var user_id: String? = null
        private set
    var avatar_url: String? = null
        private set
    var note: String? = null
        private set

    init {
        if (CustomMenuTimeLine.isMisskeyMode) {
            setMisskeyParse(response_string)
        } else {
            jsonParse(response_string)
        }
    }

    private fun jsonParse(response_string: String) {
        try {
            val jsonObject = JSONObject(response_string)
            acct = jsonObject.getString("acct")
            display_name = jsonObject.getString("display_name")
            user_id = jsonObject.getString("id")
            avatar_url = jsonObject.getString("avatar_static")
            note = jsonObject.getString("note")
            //絵文字
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true)) {
                val emojis = jsonObject.getJSONArray("emojis")
                for (i in 0 until emojis.length()) {
                    val emoji = emojis.getJSONObject(i)
                    val emoji_name = emoji.getString("shortcode")
                    val emoji_url = emoji.getString("url")
                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                    display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                    note = note!!.replace(":$emoji_name:", custom_emoji_src)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    private fun setMisskeyParse(response_string: String) {
        try {
            val jsonObject = JSONObject(response_string)
            acct = jsonObject.getString("username")
            display_name = jsonObject.getString("name")
            user_id = jsonObject.getString("id")
            avatar_url = jsonObject.getString("avatarUrl")
            note = jsonObject.getString("description")
            //絵文字
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true)) {
                val emojis = jsonObject.getJSONArray("emojis")
                for (i in 0 until emojis.length()) {
                    val emoji = emojis.getJSONObject(i)
                    val emoji_name = emoji.getString("name")
                    val emoji_url = emoji.getString("url")
                    val custom_emoji_src = "<img src=\'$emoji_url\'>"
                    display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                    note = note!!.replace(":$emoji_name:", custom_emoji_src)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

}
