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
    var setting = ""
        internal set
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
            name = jsonObject.getString("name")
            content = jsonObject.getString("content")
            instance = jsonObject.getString("instance")
            access_token = jsonObject.getString("access_token")
            image_load = jsonObject.getString("image_load")
            dialog = jsonObject.getString("dialog")
            dark_mode = jsonObject.getString("dark_mode")
            position = jsonObject.getString("position")
            streaming = jsonObject.getString("streaming")
            subtitle = jsonObject.getString("subtitle")
            image_url = jsonObject.getString("image_url")
            background_transparency = jsonObject.getString("background_transparency")
            background_screen_fit = jsonObject.getString("background_screen_fit")
            quick_profile = jsonObject.getString("quick_profile")
            toot_counter = jsonObject.getString("toot_counter")
            custom_emoji = jsonObject.getString("custom_emoji")
            gif = jsonObject.getString("gif")
            font = jsonObject.getString("font")
            one_hand = jsonObject.getString("one_hand")
            misskey = jsonObject.getString("misskey")
            misskey_username = jsonObject.getString("misskey_username")
            setting = jsonObject.getString("setting")
            //試験的
            if (!jsonObject.isNull("no_fav_icon")) {
                no_fav_icon = jsonObject.getString("no_fav_icon")
            }
            if (!jsonObject.isNull("yes_fav_icon")) {
                yes_fav_icon = jsonObject.getString("yes_fav_icon")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }
}
