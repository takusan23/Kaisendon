package io.github.takusan23.kaisendon.APIJSONParse;

import android.content.Context;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MastodonTLAPIJSONParse {

    private Context context;
    private String response_string;
    private boolean isCustomEmoji = false;
    //TLタイプ
    public static String HOME_TL = "home";
    public static String LOCAL_TL = "local";
    public static String FEDERATED_TL = "federated";

    //パース
    private String toot_text;
    private String display_name;
    private String acct;
    private String avatarUrl;
    private String avatarUrlNotGIF;
    private String toot_ID;
    private String isFav="false";
    private String isBT="false";
    private String FavCount;
    private String BTCount;
    private String client;
    private String createdAt;
    private String url;
    private String visibility;
    private String user_ID;
    private String BTAccountAcct;
    private String BTAccountDisplayName;
    private String BTAccountID;
    private String BTTootText;
    private String BTCreatedAt;
    private String BTAvatarUrl;
    private String BTAvatarUrlNotGif;
    private String cardTitle;
    private String cardURL;
    private String cardImage;
    private String cardDescription;
    private ArrayList<String> mediaList;
    private String notification_ID;
    private String notification_Type;

    //インスタンス
    public MastodonTLAPIJSONParse(Context context, String response_string, boolean customEmoji) {
        this.context = context;
        this.response_string = response_string;
        this.isCustomEmoji = customEmoji;
        setMastodonTLParse();
    }

    //それぞれ
    public String getToot_text() {
        return toot_text;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getAcct() {
        return acct;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getAvatarUrlNotGIF() {
        return avatarUrlNotGIF;
    }

    public String getToot_ID() {
        return toot_ID;
    }

    public String getIsFav() {
        return isFav;
    }

    public String getIsBT() {
        return isBT;
    }

    public String getFavCount() {
        return FavCount;
    }

    public String getBTCount() {
        return BTCount;
    }

    public String getClient() {
        return client;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUrl() {
        return url;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getUser_ID() {
        return user_ID;
    }

    public String getBTAccountAcct() {
        return BTAccountAcct;
    }

    public String getBTAccountDisplayName() {
        return BTAccountDisplayName;
    }

    public String getBTAccountID() {
        return BTAccountID;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public String getCardURL() {
        return cardURL;
    }

    public String getCardImage() {
        return cardImage;
    }

    public String getCardDescription() {
        return cardDescription;
    }

    public String getBTTootText() {
        return BTTootText;
    }

    public String getBTCreatedAt() {
        return BTCreatedAt;
    }

    public String getBTAvatarUrl() {
        return BTAvatarUrl;
    }

    public String getBTAvatarUrlNotGif() {
        return BTAvatarUrlNotGif;
    }

    public String getNotification_Type() {
        return notification_Type;
    }

    public ArrayList<String> getMediaList() {
        return mediaList;
    }

    //FavとBTは変更できるように
    public void setIsFav(String isFav) {
        this.isFav = isFav;
    }

    public void setIsBT(String isBT) {
        this.isBT = isBT;
    }

    //JSONパース
    private void setMastodonTLParse() {
        try {
            mediaList = new ArrayList<>();
            JSONObject toot_JsonObject = new JSONObject(response_string);
            //通知と分ける
            if (toot_JsonObject.isNull("type")) {
                //共通
                JSONObject account_JsonObject = toot_JsonObject.getJSONObject("account");
                JSONArray media_array = toot_JsonObject.getJSONArray("media_attachments");
                toot_text = toot_JsonObject.getString("content");
                createdAt = toot_JsonObject.getString("created_at");
                url = toot_JsonObject.getString("url");
                visibility = toot_JsonObject.getString("visibility");
                toot_ID = toot_JsonObject.getString("id");
                display_name = account_JsonObject.getString("display_name");
                acct = account_JsonObject.getString("acct");
                avatarUrl = account_JsonObject.getString("avatar");
                avatarUrlNotGIF = account_JsonObject.getString("avatar_static");
                user_ID = account_JsonObject.getString("id");
                //Local、その他同じクライアントのユーザー
                if (!toot_JsonObject.isNull("application")) {
                    client = toot_JsonObject.getJSONObject("application").getString("name");
                }
                //reBlog
                if (!toot_JsonObject.isNull("reblog")) {
                    BTAccountAcct = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("acct");
                    BTAccountDisplayName = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("display_name");
                    BTAccountID = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("id");
                    BTTootText = toot_JsonObject.getJSONObject("reblog").getString("content");
                    BTCreatedAt = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("created_at");
                    BTAvatarUrl = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("avatar");
                    BTAvatarUrlNotGif = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("avatar_static");
                }
                //card
                if (!toot_JsonObject.isNull("card")) {
                    cardTitle = toot_JsonObject.getJSONObject("card").getString("title");
                    cardURL = toot_JsonObject.getJSONObject("card").getString("url");
                    cardDescription = toot_JsonObject.getJSONObject("card").getString("description");
                    cardImage = toot_JsonObject.getJSONObject("card").getString("image");
                }
                //Streamingで取れない要素
                if (!toot_JsonObject.isNull("favourited")) {
                    isFav = toot_JsonObject.getString("favourited");
                    isBT = toot_JsonObject.getString("reblogged");
                    FavCount = toot_JsonObject.getString("favourites_count");
                    BTCount = toot_JsonObject.getString("reblogs_count");
                }

                //絵文字
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || isCustomEmoji) {
                    JSONArray emoji = toot_JsonObject.getJSONArray("emojis");
                    for (int e = 0; e < emoji.length(); e++) {
                        JSONObject jsonObject = emoji.getJSONObject(e);
                        String emoji_name = jsonObject.getString("shortcode");
                        String emoji_url = jsonObject.getString("url");
                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                    }

                    //ユーザーネームの方の絵文字
                    JSONArray account_emoji = account_JsonObject.getJSONArray("emojis");
                    for (int e = 0; e < account_emoji.length(); e++) {
                        JSONObject jsonObject = account_emoji.getJSONObject(e);
                        String emoji_name = jsonObject.getString("shortcode");
                        String emoji_url = jsonObject.getString("url");
                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                        display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                    }

                    //Pawooで取れない？
                    if (!toot_JsonObject.isNull("profile_emojis")) {
                        //アバター絵文字
                        JSONArray avater_emoji = toot_JsonObject.getJSONArray("profile_emojis");
                        for (int a = 0; a < avater_emoji.length(); a++) {
                            JSONObject jsonObject = avater_emoji.getJSONObject(a);
                            String emoji_name = jsonObject.getString("shortcode");
                            String emoji_url = jsonObject.getString("url");
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                        }


                        //ユーザーネームの方のアバター絵文字
                        JSONArray account_avater_emoji = account_JsonObject.getJSONArray("profile_emojis");
                        for (int a = 0; a < account_avater_emoji.length(); a++) {
                            JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                            String emoji_name = jsonObject.getString("shortcode");
                            String emoji_url = jsonObject.getString("url");
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                        }
                    }
                }
                //画像
                for (int i = 0; i < media_array.length(); i++) {
                    //要素があるか確認
                    if (!media_array.isNull(0)) {
                        mediaList.add(media_array.getJSONObject(i).getString("url"));
                    }
                }
            } else {
                //通知
                notification_ID = toot_JsonObject.getString("id");
                createdAt = toot_JsonObject.getString("created_at");
                notification_Type = toot_JsonObject.getString("type");
                //Account
                JSONObject account_JsonObject = toot_JsonObject.getJSONObject("account");
                display_name = account_JsonObject.getString("display_name");
                acct = account_JsonObject.getString("acct");
                avatarUrl = account_JsonObject.getString("avatar");
                avatarUrlNotGIF = account_JsonObject.getString("avatar_static");
                user_ID = account_JsonObject.getString("id");
                //Status
                toot_text = "";
                createdAt = "";
                url = "";
                visibility = "";
                toot_ID = "";
                //返信しかこれない
                if (!toot_JsonObject.isNull("status")) {
                    JSONObject status_JsonObject = toot_JsonObject.getJSONObject("status");
                    toot_text = status_JsonObject.getString("content");
                    url = status_JsonObject.getString("url");
                    visibility = status_JsonObject.getString("visibility");
                    toot_ID = status_JsonObject.getString("id");
                    JSONArray media_array = status_JsonObject.getJSONArray("media_attachments");
                    //画像
                    for (int i = 0; i < media_array.length(); i++) {
                        //要素があるか確認
                        if (!media_array.isNull(0)) {
                            mediaList.add(media_array.getJSONObject(i).getString("url"));
                        }
                    }
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || isCustomEmoji) {
                        JSONArray emoji = status_JsonObject.getJSONArray("emojis");
                        for (int e = 0; e < emoji.length(); e++) {
                            JSONObject jsonObject = emoji.getJSONObject(e);
                            String emoji_name = jsonObject.getString("shortcode");
                            String emoji_url = jsonObject.getString("url");
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                        }
                    }
                }

                //絵文字
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || isCustomEmoji) {
                    //ユーザーネームの方の絵文字
                    JSONArray account_emoji = account_JsonObject.getJSONArray("emojis");
                    for (int e = 0; e < account_emoji.length(); e++) {
                        JSONObject jsonObject = account_emoji.getJSONObject(e);
                        String emoji_name = jsonObject.getString("shortcode");
                        String emoji_url = jsonObject.getString("url");
                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                        display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
