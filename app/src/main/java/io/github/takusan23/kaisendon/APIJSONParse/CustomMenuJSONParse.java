package io.github.takusan23.kaisendon.APIJSONParse;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomMenuJSONParse {
    String misskey = "";
    String name = "";
    String content = "";
    String instance = "";
    String access_token = "";
    String image_load = "";
    String dialog = "";
    String dark_mode = "";
    String position = "";
    String streaming = "";
    String subtitle = "";
    String image_url = "";
    String background_transparency = "";
    String background_screen_fit = "";
    String quick_profile = "";
    String toot_counter = "";
    String custom_emoji = "";
    String gif = "";
    String font = "";
    String one_hand = "";
    String misskey_username = "";
    String setting = "";

    public CustomMenuJSONParse(String json_data) {
        json_parse(json_data);
    }

    private void json_parse(String json_data) {
        try {
            JSONObject jsonObject = new JSONObject(json_data);
            name = jsonObject.getString("name");
            content = jsonObject.getString("content");
            instance = jsonObject.getString("instance");
            access_token = jsonObject.getString("access_token");
            image_load = jsonObject.getString("image_load");
            dialog = jsonObject.getString("dialog");
            dark_mode = jsonObject.getString("dark_mode");
            position = jsonObject.getString("position");
            streaming = jsonObject.getString("streaming");
            subtitle = jsonObject.getString("subtitle");
            image_url = jsonObject.getString("image_url");
            background_transparency = jsonObject.getString("background_transparency");
            background_screen_fit = jsonObject.getString("background_screen_fit");
            quick_profile = jsonObject.getString("quick_profile");
            toot_counter = jsonObject.getString("toot_counter");
            custom_emoji = jsonObject.getString("custom_emoji");
            gif = jsonObject.getString("gif");
            font = jsonObject.getString("font");
            one_hand = jsonObject.getString("one_hand");
            misskey = jsonObject.getString("misskey");
            misskey_username = jsonObject.getString("misskey_username");
            setting = jsonObject.getString("setting");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getMisskey() {
        return misskey;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getInstance() {
        return instance;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getImage_load() {
        return image_load;
    }

    public String getDialog() {
        return dialog;
    }

    public String getDark_mode() {
        return dark_mode;
    }

    public String getPosition() {
        return position;
    }

    public String getStreaming() {
        return streaming;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getBackground_transparency() {
        return background_transparency;
    }

    public String getBackground_screen_fit() {
        return background_screen_fit;
    }

    public String getQuick_profile() {
        return quick_profile;
    }

    public String getToot_counter() {
        return toot_counter;
    }

    public String getCustom_emoji() {
        return custom_emoji;
    }

    public String getGif() {
        return gif;
    }

    public String getFont() {
        return font;
    }

    public String getOne_hand() {
        return one_hand;
    }

    public String getMisskey_username() {
        return misskey_username;
    }

    public String getSetting() {
        return setting;
    }
}
