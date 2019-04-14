package io.github.takusan23.kaisendon.APIJSONParse;

import android.content.Context;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.function.Consumer;

import io.github.takusan23.kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.kaisendon.HomeTimeLineAdapter;

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
    private String isFav = "false";
    private String isBT = "false";
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
    private String reaction_Type = "";
    private String spoiler_text;

    //インスタンス
    public MastodonTLAPIJSONParse(Context context, String response_string) {
        this.context = context;
        this.response_string = response_string;
        if (CustomMenuTimeLine.isMisskeyMode()) {
            setMisskeyParse();
        } else {
            setMastodonTLParse();
        }
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

    public String getReaction_Type() {
        return reaction_Type;
    }

    public String getSpoiler_text() {
        return spoiler_text;
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
                spoiler_text = toot_JsonObject.getString("spoiler_text");
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
                //createdAt = "";
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

    /**
     * Misskey Parse
     */
    private void setMisskeyParse() {
        try {
            mediaList = new ArrayList<>();
            JSONObject note_JsonObject = new JSONObject(response_string);
            //通知と分ける
            if (note_JsonObject.isNull("type")) {
                //共通
                JSONObject account_JsonObject = note_JsonObject.getJSONObject("user");
                JSONArray media_array = note_JsonObject.getJSONArray("media");
                toot_text = note_JsonObject.getString("text");
                createdAt = note_JsonObject.getString("createdAt");
                //url = note_JsonObject.getString("url");
                visibility = note_JsonObject.getString("visibility");
                toot_ID = note_JsonObject.getString("id");
                display_name = account_JsonObject.getString("name");
                acct = account_JsonObject.getString("username");
                avatarUrl = account_JsonObject.getString("avatarUrl");
                avatarUrlNotGIF = account_JsonObject.getString("avatarUrl");
                user_ID = account_JsonObject.getString("id");
                //Local、その他同じクライアントのユーザー
                if (!note_JsonObject.isNull("application")) {
                    client = note_JsonObject.getJSONObject("application").getString("name");
                }
                //reBlog
                if (!note_JsonObject.isNull("renote")) {
                    BTAccountAcct = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("username");
                    BTAccountDisplayName = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("name");
                    BTAccountID = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("id");
                    BTTootText = note_JsonObject.getJSONObject("renote").getString("text");
                    BTCreatedAt = note_JsonObject.getJSONObject("renote").getString("createdAt");
                    BTAvatarUrl = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("avatarUrl");
                    BTAvatarUrlNotGif = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("avatarUrl");
                }
/*
            //card
            if (!note_JsonObject.isNull("card")) {
                cardTitle = note_JsonObject.getJSONObject("card").getString("title");
                cardURL = note_JsonObject.getJSONObject("card").getString("url");
                cardDescription = note_JsonObject.getJSONObject("card").getString("description");
                cardImage = note_JsonObject.getJSONObject("card").getString("image");
            }
*/
                //MastodonでFavのところはMisskeyリアクション一覧の配列を渡す
                JSONObject reaction_Object = note_JsonObject.getJSONObject("reactionCounts");
                //名前を取り出す？
                FavCount = "";
                reaction_Object.keys().forEachRemaining(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        try {
                            //カウントを表示
                            String index = reaction_Object.getString(s);
                            FavCount = FavCount + " " + HomeTimeLineAdapter.toReactionEmoji(s) + ":" + index + "  ";
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                isBT = note_JsonObject.getString("myRenoteId");
                BTCount = note_JsonObject.getString("renoteCount");
                isFav = note_JsonObject.getString("myReaction");
                //絵文字
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || Boolean.valueOf(CustomMenuTimeLine.isUseCustomEmoji())) {
                    JSONArray emoji = note_JsonObject.getJSONArray("emojis");
                    for (int e = 0; e < emoji.length(); e++) {
                        JSONObject emoji_jsonObject = emoji.getJSONObject(e);
                        String emoji_name = emoji_jsonObject.getString("name");
                        String emoji_url = emoji_jsonObject.getString("url");
                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                    }

                    //ユーザーネームの方の絵文字
                    if (!account_JsonObject.isNull("emojis")) {
                        JSONArray account_emoji = account_JsonObject.getJSONArray("emojis");
                        for (int e = 0; e < account_emoji.length(); e++) {
                            JSONObject emoji_jsonObject = account_emoji.getJSONObject(e);
                            String emoji_name = emoji_jsonObject.getString("name");
                            String emoji_url = emoji_jsonObject.getString("url");
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
                notification_ID = note_JsonObject.getString("id");
                createdAt = note_JsonObject.getString("createdAt");
                notification_Type = note_JsonObject.getString("type");
                //Account
                JSONObject account_JsonObject = note_JsonObject.getJSONObject("user");
                display_name = account_JsonObject.getString("name");
                acct = account_JsonObject.getString("username");
                avatarUrl = account_JsonObject.getString("avatarUrl");
                avatarUrlNotGIF = account_JsonObject.getString("avatarUrl");
                user_ID = account_JsonObject.getString("id");
                //Status
                toot_text = "";
                //createdAt = "";
                url = "";
                visibility = "";
                toot_ID = "";
                //Null
                if (note_JsonObject.getString("type").contains("reaction")) {
                    reaction_Type = note_JsonObject.getString("reaction");
                }
                //返信しかこれない
                if (!note_JsonObject.isNull("note")) {
                    JSONObject status_JsonObject = note_JsonObject.getJSONObject("note");
                    toot_text = status_JsonObject.getString("text");
                    //url = status_JsonObject.getString("url");
                    visibility = status_JsonObject.getString("visibility");
                    toot_ID = status_JsonObject.getString("id");
/*
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
*/
                }

                //絵文字
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || isCustomEmoji) {
                    //ユーザーネームの方の絵文字
                    JSONArray account_emoji = account_JsonObject.getJSONArray("emojis");
                    for (int e = 0; e < account_emoji.length(); e++) {
                        JSONObject jsonObject = account_emoji.getJSONObject(e);
                        String emoji_name = jsonObject.getString("name");
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
