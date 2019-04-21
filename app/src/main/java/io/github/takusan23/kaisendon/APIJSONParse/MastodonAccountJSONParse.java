package io.github.takusan23.kaisendon.APIJSONParse;

import android.content.Context;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MastodonAccountJSONParse {
    private Context context;
    private String acct;
    private String display_name;
    private String user_id;
    private String avatar_url;
    private String note;

    public MastodonAccountJSONParse(Context context, String response_string) {
        this.context = context;
        jsonParse(response_string);
    }

    public String getAcct() {
        return acct;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public String getNote() {
        return note;
    }

    private void jsonParse(String response_string) {
        try {
            JSONObject jsonObject = new JSONObject(response_string);
            acct = jsonObject.getString("acct");
            display_name = jsonObject.getString("display_name");
            user_id = jsonObject.getString("id");
            avatar_url = jsonObject.getString("avatar_static");
            note = jsonObject.getString("note");
            //絵文字
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true)) {
                JSONArray emojis = jsonObject.getJSONArray("emojis");
                for (int i = 0; i < emojis.length(); i++) {
                    JSONObject emoji = emojis.getJSONObject(i);
                    String emoji_name = emoji.getString("shortcode");
                    String emoji_url = emoji.getString("url");
                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                    display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                    note = note.replace(":" + emoji_name + ":", custom_emoji_src);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
