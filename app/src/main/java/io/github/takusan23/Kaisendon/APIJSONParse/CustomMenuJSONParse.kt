package io.github.takusan23.Kaisendon.APIJSONParse

import org.json.JSONException
import org.json.JSONObject

class CustomMenuJSONParse(json_data: String) {
    var misskey = ""
        internal set
    var name = ""
        internal set
    var content = ""
        internal set
    var instance = ""
        internal set
    var access_token = ""
        internal set
    var image_load = ""
        internal set
    var dialog = ""
        internal set
    var dark_mode = ""
        internal set
    var position = ""
        internal set
    var streaming = ""
        internal set
    var subtitle = ""
        internal set
    var image_url = ""
        internal set
    var background_transparency = ""
        internal set
    var background_screen_fit = ""
        internal set
    var quick_profile = ""
        internal set
    var toot_counter = ""
        internal set
    var custom_emoji = ""
        internal set
    var gif = ""
        internal set
    var font = ""
        internal set
    var one_hand = ""
        internal set
    var misskey_username = ""
        internal set
    var isReadOnly = ""
        internal set
    var setting = ""
        internal set
    var json_data = ""
    var timeline_strinaming = ""
    var notification_streaming = ""
    var no_fav_icon: String? = null
        private set
    var yes_fav_icon: String? = null
        private set

    init {
        json_parse(json_data)
    }

    private fun json_parse(json_data: String) {
        try {

            val jsonObject = JSONObject(json_data)
            this.json_data = json_data
            name = getStringJsonCheck(jsonObject, "name")
            content = getStringJsonCheck(jsonObject, "content")
            instance = getStringJsonCheck(jsonObject, "instance")
            access_token = getStringJsonCheck(jsonObject, "access_token")
            image_load = getStringJsonCheck(jsonObject, "image_load")
            dialog = getStringJsonCheck(jsonObject, "dialog")
            dark_mode = getStringJsonCheck(jsonObject, "dark_mode")
            position = getStringJsonCheck(jsonObject, "position")
            streaming = getStringJsonCheck(jsonObject, "streaming")
            subtitle = getStringJsonCheck(jsonObject, "subtitle")
            image_url = getStringJsonCheck(jsonObject, "image_url")
            background_transparency = getStringJsonCheck(jsonObject, "background_transparency")
            background_screen_fit = getStringJsonCheck(jsonObject, "background_screen_fit")
            quick_profile = getStringJsonCheck(jsonObject, "quick_profile")
            toot_counter = getStringJsonCheck(jsonObject, "toot_counter")
            custom_emoji = getStringJsonCheck(jsonObject, "custom_emoji")
            gif = getStringJsonCheck(jsonObject, "gif")
            font = getStringJsonCheck(jsonObject, "font")
            one_hand = getStringJsonCheck(jsonObject, "one_hand")
            misskey = getStringJsonCheck(jsonObject, "misskey")
            misskey_username = getStringJsonCheck(jsonObject, "misskey_username")
            setting = getStringJsonCheck(jsonObject, "setting")
            isReadOnly = getStringJsonCheck(jsonObject, "read_only")


        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getStringJsonCheck(jsonObject: JSONObject, string: String): String {
        if (jsonObject.has(string)) {
            return jsonObject.getString(string)
        } else {
            return ""
        }
    }

}
