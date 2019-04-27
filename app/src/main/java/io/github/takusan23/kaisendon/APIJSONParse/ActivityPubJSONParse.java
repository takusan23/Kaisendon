package io.github.takusan23.kaisendon.APIJSONParse;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityPubJSONParse {

    private String published;
    private String context;

    public ActivityPubJSONParse(String json) {
        json_parse(json);
    }

    public String getContext() {
        return context;
    }

    public String getPublished() {
        return published;
    }

    private void json_parse(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            //tootがあるJSONObject
            JSONObject object = jsonObject.getJSONObject("object");
            context = object.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
