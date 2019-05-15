package io.github.takusan23.Kaisendon.APIJSONParse;

import org.json.JSONException;
import org.json.JSONObject;

public class MastodonScheduledStatusesJSONParse {

    private String id;
    private String scheduled_at;
    private String text;
    private String visibility;


    public MastodonScheduledStatusesJSONParse(String response_string){
        jsonParse(response_string);
    }

    public String getId() {
        return id;
    }

    public String getScheduled_at() {
        return scheduled_at;
    }

    public String getText() {
        return text;
    }

    public String getVisibility() {
        return visibility;
    }

    private void jsonParse(String response_string){
        try {
            JSONObject jsonObject = new JSONObject(response_string);
            JSONObject params = jsonObject.getJSONObject("params");
            id = jsonObject.getString("id");
            scheduled_at = jsonObject.getString("scheduled_at");
            text = params.getString("text");
            visibility = params.getString("visibility");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
