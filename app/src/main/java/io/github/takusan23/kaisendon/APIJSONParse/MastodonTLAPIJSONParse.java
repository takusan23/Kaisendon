package io.github.takusan23.kaisendon.APIJSONParse;

import android.content.Context;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private String isFav;
    private String isBT;
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
    private String cardTitle;
    private String cardURL;
    private String cardImage;
    private String cardDescription;

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
            JSONObject toot_JsonObject = new JSONObject(response_string);
            //共通
            JSONObject account_JsonObject = toot_JsonObject.getJSONObject("account");
            toot_text = toot_JsonObject.getString("content");
            createdAt = toot_JsonObject.getString("created_at");
            isFav = toot_JsonObject.getString("favourited");
            isBT = toot_JsonObject.getString("reblogged");
            FavCount = toot_JsonObject.getString("favourites_count");
            BTCount = toot_JsonObject.getString("reblogs_count");
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
            }
            //card
            if (!toot_JsonObject.isNull("card")) {
                cardTitle = toot_JsonObject.getJSONObject("card").getString("title");
                cardURL = toot_JsonObject.getJSONObject("card").getString("url");
                cardDescription = toot_JsonObject.getJSONObject("card").getString("description");
                cardImage = toot_JsonObject.getJSONObject("card").getString("image");
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


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
