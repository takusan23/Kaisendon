package io.github.takusan23.Kaisendon.APIJSONParse

import org.json.JSONException
import org.json.JSONObject

class ActivityPubJSONParse(json: String) {

    val published: String? = null
    var context: String? = null
        private set

    init {
        json_parse(json)
    }

    private fun json_parse(json: String) {
        try {
            val jsonObject = JSONObject(json)
            //tootがあるJSONObject
            val `object` = jsonObject.getJSONObject("object")
            context = `object`.getString("content")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

}
