package io.github.takusan23.Kaisendon.APIJSONParse

import org.json.JSONException
import org.json.JSONObject

class MastodonScheduledStatusesJSONParse(response_string: String) {

    var id: String? = null
        private set
    var scheduled_at: String? = null
        private set
    var text: String? = null
        private set
    var visibility: String? = null
        private set


    init {
        jsonParse(response_string)
    }

    private fun jsonParse(response_string: String) {
        try {
            val jsonObject = JSONObject(response_string)
            val params = jsonObject.getJSONObject("params")
            id = jsonObject.getString("id")
            scheduled_at = jsonObject.getString("scheduled_at")
            text = params.getString("text")
            visibility = params.getString("visibility")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

}
