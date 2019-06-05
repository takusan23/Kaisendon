package io.github.takusan23.Kaisendon.APIJSONParse

import android.content.Context
import android.preference.PreferenceManager
import io.github.takusan23.Kaisendon.HomeTimeLineAdapter
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MastodonTLAPIJSONParse//インスタンス
(private val context: Context, private val response_string: String, setting: CustomMenuJSONParse) {
    private val isCustomEmoji = false

    //パース
    //それぞれ
    var toot_text: String? = null
        private set
    var display_name: String? = null
        private set
    var acct: String? = null
        private set
    var spoiler_text: String? = null
        private set
    var avatarUrl: String? = null
        private set
    var avatarUrlNotGIF: String? = null
        private set
    var toot_ID: String? = null
        private set
    //FavとBTは変更できるように
    var isFav = "false"
    var isBT = "false"
    var favCount: String? = null
        private set
    var btCount: String? = null
        private set
    var client: String? = null
        private set
    var createdAt: String? = null
        private set
    var url: String? = null
        private set
    var visibility: String? = null
        private set
    var user_ID: String? = null
        private set
    var btAccountAcct: String? = null
        private set
    var btAccountDisplayName: String? = null
        private set
    var btAccountID: String? = null
        private set
    var btTootText: String? = null
        private set
    var btCreatedAt: String? = null
        private set
    var btAvatarUrl: String? = null
        private set
    var btAvatarUrlNotGif: String? = null
        private set
    var cardTitle: String? = null
        private set
    var cardURL: String? = null
        private set
    var cardImage: String? = null
        private set
    var cardDescription: String? = null
        private set
    var mediaList: ArrayList<String>? = null
        private set
    private var notification_ID: String? = null
    var notification_Type: String? = null
        private set
    var reaction_Type = ""
        private set
    var isVote = false
        private set
    var vote_id: String? = null
        private set
    var vote_expires_at: String? = null
        private set
    var total_votes_count: String? = null
        private set
    var votes_title: ArrayList<String>? = null
        private set
    var votes_count: ArrayList<String>? = null
        private set
    var note: String? = null
        private set
    var follow_count: String? = null
        private set
    var follower_count: String? = null
        private set
    var pinned: String? = null
        private set

    fun getIsFav(): String {
        return isFav
    }

    fun getIsBT(): String {
        return isBT
    }

    init {
        if (isMisskeyCheck(response_string)) {
            setMisskeyParse(response_string, setting)
        } else {
            setMastodonTLParse(response_string, setting)
        }
    }

    //createAtがあるとMisskey、ないとMastodonって分けるコード
    //ちなみにMastodonはcreated_at
    private fun isMisskeyCheck(response_string: String): Boolean {
        var isCheck = false
        try {
            val jsonObject = JSONObject(response_string)
            if (!jsonObject.isNull("createdAt")) {
                isCheck = true
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return isCheck
    }

    //JSONパース
    private fun setMastodonTLParse(response_string: String, setting: CustomMenuJSONParse) {
        try {
            mediaList = ArrayList()
            var toot_JsonObject = JSONObject(response_string)
            //通知/ダイレクトめっせーじと分ける
            if (toot_JsonObject.isNull("type") && toot_JsonObject.isNull("unread")) {
                //共通
                val account_JsonObject = toot_JsonObject.getJSONObject("account")
                val media_array = toot_JsonObject.getJSONArray("media_attachments")
                toot_text = toot_JsonObject.getString("content")
                createdAt = toot_JsonObject.getString("created_at")
                url = toot_JsonObject.getString("url")
                visibility = toot_JsonObject.getString("visibility")
                toot_ID = toot_JsonObject.getString("id")
                spoiler_text = toot_JsonObject.getString("spoiler_text")
                display_name = account_JsonObject.getString("display_name")
                acct = account_JsonObject.getString("acct")
                avatarUrl = account_JsonObject.getString("avatar")
                avatarUrlNotGIF = account_JsonObject.getString("avatar_static")
                user_ID = account_JsonObject.getString("id")
                note = account_JsonObject.getString("note")
                follow_count = account_JsonObject.getString("following_count")
                follower_count = account_JsonObject.getString("followers_count")
                //Local、その他同じクライアントのユーザー
                if (!toot_JsonObject.isNull("application")) {
                    client = toot_JsonObject.getJSONObject("application").getString("name")
                }
                if (!toot_JsonObject.isNull("pinned")) {
                    pinned = toot_JsonObject.getString("pinned")
                }
                //reBlog
                if (!toot_JsonObject.isNull("reblog")) {
                    btAccountAcct = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("acct")
                    btAccountDisplayName = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("display_name")
                    btAccountID = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("id")
                    btTootText = toot_JsonObject.getJSONObject("reblog").getString("content")
                    btCreatedAt = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("created_at")
                    btAvatarUrl = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("avatar")
                    btAvatarUrlNotGif = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("avatar_static")
                }
                //card
                if (!toot_JsonObject.isNull("card")) {
                    cardTitle = toot_JsonObject.getJSONObject("card").getString("title")
                    cardURL = toot_JsonObject.getJSONObject("card").getString("url")
                    cardDescription = toot_JsonObject.getJSONObject("card").getString("description")
                    cardImage = toot_JsonObject.getJSONObject("card").getString("image")
                }
                //Streamingで取れない要素
                if (!toot_JsonObject.isNull("favourited")) {
                    isFav = toot_JsonObject.getString("favourited")
                    isBT = toot_JsonObject.getString("reblogged")
                    favCount = toot_JsonObject.getString("favourites_count")
                    btCount = toot_JsonObject.getString("reblogs_count")
                }

                //絵文字
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || isCustomEmoji) {
                    val emoji = toot_JsonObject.getJSONArray("emojis")
                    for (e in 0 until emoji.length()) {
                        val jsonObject = emoji.getJSONObject(e)
                        val emoji_name = jsonObject.getString("shortcode")
                        val emoji_url = jsonObject.getString("url")
                        val custom_emoji_src = "<img src=\'$emoji_url\'>"
                        toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                    }

                    //ユーザーネームの方の絵文字
                    val account_emoji = account_JsonObject.getJSONArray("emojis")
                    for (e in 0 until account_emoji.length()) {
                        val jsonObject = account_emoji.getJSONObject(e)
                        val emoji_name = jsonObject.getString("shortcode")
                        val emoji_url = jsonObject.getString("url")
                        val custom_emoji_src = "<img src=\'$emoji_url\'>"
                        display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                    }

                    //Pawooで取れない？
                    if (!toot_JsonObject.isNull("profile_emojis")) {
                        //アバター絵文字
                        val avater_emoji = toot_JsonObject.getJSONArray("profile_emojis")
                        for (a in 0 until avater_emoji.length()) {
                            val jsonObject = avater_emoji.getJSONObject(a)
                            val emoji_name = jsonObject.getString("shortcode")
                            val emoji_url = jsonObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                        }


                        //ユーザーネームの方のアバター絵文字
                        val account_avater_emoji = account_JsonObject.getJSONArray("profile_emojis")
                        for (a in 0 until account_avater_emoji.length()) {
                            val jsonObject = account_avater_emoji.getJSONObject(a)
                            val emoji_name = jsonObject.getString("shortcode")
                            val emoji_url = jsonObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                        }
                    }
                }
                //画像
                for (i in 0 until media_array.length()) {
                    //要素があるか確認
                    if (!media_array.isNull(0)) {
                        mediaList!!.add(media_array.getJSONObject(i).getString("url"))
                    }
                }
                //投票
                if (!toot_JsonObject.isNull("poll")) {
                    votes_title = ArrayList()
                    votes_count = ArrayList()
                    val vote = toot_JsonObject.getJSONObject("poll")
                    val options = vote.getJSONArray("options")
                    isVote = true
                    vote_id = vote.getString("id")
                    total_votes_count = vote.getString("votes_count")
                    vote_expires_at = vote.getString("expires_at")
                    for (i in 0 until options.length()) {
                        val option = options.getJSONObject(i)
                        votes_title!!.add(option.getString("title"))
                        votes_count!!.add(option.getString("votes_count"))
                    }
                }
            } else if (!toot_JsonObject.isNull("type") && toot_JsonObject.isNull("unread")) {
                //通知
                notification_ID = toot_JsonObject.getString("id")
                createdAt = toot_JsonObject.getString("created_at")
                notification_Type = toot_JsonObject.getString("type")
                //Account
                val account_JsonObject = toot_JsonObject.getJSONObject("account")
                display_name = account_JsonObject.getString("display_name")
                acct = account_JsonObject.getString("acct")
                avatarUrl = account_JsonObject.getString("avatar")
                avatarUrlNotGIF = account_JsonObject.getString("avatar_static")
                user_ID = account_JsonObject.getString("id")
                note = account_JsonObject.getString("note")
                follow_count = account_JsonObject.getString("following_count")
                follower_count = account_JsonObject.getString("followers_count")

                //Status
                toot_text = ""
                //createdAt = "";
                url = ""
                visibility = ""
                toot_ID = ""

                //返信しかこれない
                if (!toot_JsonObject.isNull("status")) {
                    val status_JsonObject = toot_JsonObject.getJSONObject("status")
                    toot_text = status_JsonObject.getString("content")
                    url = status_JsonObject.getString("url")
                    visibility = status_JsonObject.getString("visibility")
                    toot_ID = status_JsonObject.getString("id")
                    val media_array = status_JsonObject.getJSONArray("media_attachments")
                    //画像
                    for (i in 0 until media_array.length()) {
                        //要素があるか確認
                        if (!media_array.isNull(0)) {
                            mediaList!!.add(media_array.getJSONObject(i).getString("url"))
                        }
                    }
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || isCustomEmoji) {
                        val emoji = status_JsonObject.getJSONArray("emojis")
                        for (e in 0 until emoji.length()) {
                            val jsonObject = emoji.getJSONObject(e)
                            val emoji_name = jsonObject.getString("shortcode")
                            val emoji_url = jsonObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                        }
                    }

                    //投票
                    if (!status_JsonObject.isNull("poll")) {
                        isVote = true
                        votes_title = ArrayList()
                        votes_count = ArrayList()
                        val vote = status_JsonObject.getJSONObject("poll")
                        val options = vote.getJSONArray("options")
                        vote_id = vote.getString("id")
                        total_votes_count = vote.getString("votes_count")
                        vote_expires_at = vote.getString("expires_at")
                        for (i in 0 until options.length()) {
                            val option = options.getJSONObject(i)
                            votes_title!!.add(option.getString("title"))
                            votes_count!!.add(option.getString("votes_count"))
                        }
                    }

                }

                //絵文字
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || isCustomEmoji) {
                    //ユーザーネームの方の絵文字
                    val account_emoji = account_JsonObject.getJSONArray("emojis")
                    for (e in 0 until account_emoji.length()) {
                        val jsonObject = account_emoji.getJSONObject(e)
                        val emoji_name = jsonObject.getString("shortcode")
                        val emoji_url = jsonObject.getString("url")
                        val custom_emoji_src = "<img src=\'$emoji_url\'>"
                        display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                    }
                }
            } else if (!toot_JsonObject.isNull("unread")) {
                //ストリーミングのダイレクトメッセージパース
                //共通
                //DMはlast_statusにある
                toot_JsonObject = toot_JsonObject.getJSONObject("last_status")
                val account_JsonObject = toot_JsonObject.getJSONObject("account")
                val media_array = toot_JsonObject.getJSONArray("media_attachments")
                toot_text = toot_JsonObject.getString("content")
                createdAt = toot_JsonObject.getString("created_at")
                url = toot_JsonObject.getString("url")
                visibility = toot_JsonObject.getString("visibility")
                toot_ID = toot_JsonObject.getString("id")
                spoiler_text = toot_JsonObject.getString("spoiler_text")
                display_name = account_JsonObject.getString("display_name")
                acct = account_JsonObject.getString("acct")
                avatarUrl = account_JsonObject.getString("avatar")
                avatarUrlNotGIF = account_JsonObject.getString("avatar_static")
                user_ID = account_JsonObject.getString("id")
                note = account_JsonObject.getString("note")
                follow_count = account_JsonObject.getString("following_count")
                follower_count = account_JsonObject.getString("followers_count")
                //Local、その他同じクライアントのユーザー
                if (!toot_JsonObject.isNull("application")) {
                    client = toot_JsonObject.getJSONObject("application").getString("name")
                }
                //reBlog
                if (!toot_JsonObject.isNull("reblog")) {
                    btAccountAcct = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("acct")
                    btAccountDisplayName = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("display_name")
                    btAccountID = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("id")
                    btTootText = toot_JsonObject.getJSONObject("reblog").getString("content")
                    btCreatedAt = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("created_at")
                    btAvatarUrl = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("avatar")
                    btAvatarUrlNotGif = toot_JsonObject.getJSONObject("reblog").getJSONObject("account").getString("avatar_static")
                }
                //card
                if (!toot_JsonObject.isNull("card")) {
                    cardTitle = toot_JsonObject.getJSONObject("card").getString("title")
                    cardURL = toot_JsonObject.getJSONObject("card").getString("url")
                    cardDescription = toot_JsonObject.getJSONObject("card").getString("description")
                    cardImage = toot_JsonObject.getJSONObject("card").getString("image")
                }
                //Streamingで取れない要素
                if (!toot_JsonObject.isNull("favourited")) {
                    isFav = toot_JsonObject.getString("favourited")
                    isBT = toot_JsonObject.getString("reblogged")
                    favCount = toot_JsonObject.getString("favourites_count")
                    btCount = toot_JsonObject.getString("reblogs_count")
                }

                //絵文字
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || isCustomEmoji) {
                    val emoji = toot_JsonObject.getJSONArray("emojis")
                    for (e in 0 until emoji.length()) {
                        val jsonObject = emoji.getJSONObject(e)
                        val emoji_name = jsonObject.getString("shortcode")
                        val emoji_url = jsonObject.getString("url")
                        val custom_emoji_src = "<img src=\'$emoji_url\'>"
                        toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                    }

                    //ユーザーネームの方の絵文字
                    val account_emoji = account_JsonObject.getJSONArray("emojis")
                    for (e in 0 until account_emoji.length()) {
                        val jsonObject = account_emoji.getJSONObject(e)
                        val emoji_name = jsonObject.getString("shortcode")
                        val emoji_url = jsonObject.getString("url")
                        val custom_emoji_src = "<img src=\'$emoji_url\'>"
                        display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                    }

                    //Pawooで取れない？
                    if (!toot_JsonObject.isNull("profile_emojis")) {
                        //アバター絵文字
                        val avater_emoji = toot_JsonObject.getJSONArray("profile_emojis")
                        for (a in 0 until avater_emoji.length()) {
                            val jsonObject = avater_emoji.getJSONObject(a)
                            val emoji_name = jsonObject.getString("shortcode")
                            val emoji_url = jsonObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                        }


                        //ユーザーネームの方のアバター絵文字
                        val account_avater_emoji = account_JsonObject.getJSONArray("profile_emojis")
                        for (a in 0 until account_avater_emoji.length()) {
                            val jsonObject = account_avater_emoji.getJSONObject(a)
                            val emoji_name = jsonObject.getString("shortcode")
                            val emoji_url = jsonObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                        }
                    }
                }
                //画像
                for (i in 0 until media_array.length()) {
                    //要素があるか確認
                    if (!media_array.isNull(0)) {
                        mediaList!!.add(media_array.getJSONObject(i).getString("url"))
                    }
                }
                //投票
                if (!toot_JsonObject.isNull("poll")) {
                    votes_title = ArrayList()
                    votes_count = ArrayList()
                    val vote = toot_JsonObject.getJSONObject("poll")
                    val options = vote.getJSONArray("options")
                    isVote = true
                    vote_id = vote.getString("id")
                    total_votes_count = vote.getString("votes_count")
                    vote_expires_at = vote.getString("expires_at")
                    for (i in 0 until options.length()) {
                        val option = options.getJSONObject(i)
                        votes_title!!.add(option.getString("title"))
                        votes_count!!.add(option.getString("votes_count"))
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /**
     * Misskey Parse
     */
    private fun setMisskeyParse(response_string: String, setting: CustomMenuJSONParse) {
        try {
            mediaList = ArrayList()
            val note_JsonObject = JSONObject(response_string)
            //通知と分ける
            if (note_JsonObject.isNull("type")) {
                //共通
                val account_JsonObject = note_JsonObject.getJSONObject("user")
                val media_array = note_JsonObject.getJSONArray("media")
                toot_text = note_JsonObject.getString("text")
                createdAt = note_JsonObject.getString("createdAt")
                //url = note_JsonObject.getString("url");
                visibility = note_JsonObject.getString("visibility")
                toot_ID = note_JsonObject.getString("id")
                display_name = account_JsonObject.getString("name")
                acct = account_JsonObject.getString("username")
                avatarUrl = account_JsonObject.getString("avatarUrl")
                avatarUrlNotGIF = account_JsonObject.getString("avatarUrl")
                user_ID = account_JsonObject.getString("id")
                //Local、その他同じクライアントのユーザー
                if (!note_JsonObject.isNull("application")) {
                    client = note_JsonObject.getJSONObject("application").getString("name")
                }
                //reBlog
                if (!note_JsonObject.isNull("renote")) {
                    btAccountAcct = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("username")
                    btAccountDisplayName = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("name")
                    btAccountID = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("id")
                    btTootText = note_JsonObject.getJSONObject("renote").getString("text")
                    btCreatedAt = note_JsonObject.getJSONObject("renote").getString("createdAt")
                    btAvatarUrl = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("avatarUrl")
                    btAvatarUrlNotGif = note_JsonObject.getJSONObject("renote").getJSONObject("user").getString("avatarUrl")
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
                val reaction_Object = note_JsonObject.getJSONObject("reactionCounts")
                //名前を取り出す？
                favCount = ""
                reaction_Object.keys().forEachRemaining { s ->
                    try {
                        //カウントを表示
                        val index = reaction_Object.getString(s)
                        favCount = favCount + " " + HomeTimeLineAdapter.toReactionEmoji(s) + ":" + index + "  "
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                isBT = note_JsonObject.getString("myRenoteId")
                btCount = note_JsonObject.getString("renoteCount")
                isFav = note_JsonObject.getString("myReaction")
                //絵文字
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_custom_emoji", true) || java.lang.Boolean.valueOf(setting.custom_emoji)) {
                    val emoji = note_JsonObject.getJSONArray("emojis")
                    for (e in 0 until emoji.length()) {
                        val emoji_jsonObject = emoji.getJSONObject(e)
                        val emoji_name = emoji_jsonObject.getString("name")
                        val emoji_url = emoji_jsonObject.getString("url")
                        val custom_emoji_src = "<img src=\'$emoji_url\'>"
                        toot_text = toot_text!!.replace(":$emoji_name:", custom_emoji_src)
                    }

                    //ユーザーネームの方の絵文字
                    if (!account_JsonObject.isNull("emojis")) {
                        val account_emoji = account_JsonObject.getJSONArray("emojis")
                        for (e in 0 until account_emoji.length()) {
                            val emoji_jsonObject = account_emoji.getJSONObject(e)
                            val emoji_name = emoji_jsonObject.getString("name")
                            val emoji_url = emoji_jsonObject.getString("url")
                            val custom_emoji_src = "<img src=\'$emoji_url\'>"
                            display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                        }
                    }
                }
                //画像
                for (i in 0 until media_array.length()) {
                    //要素があるか確認
                    if (!media_array.isNull(0)) {
                        mediaList!!.add(media_array.getJSONObject(i).getString("url"))
                    }
                }
            } else {
                //通知
                notification_ID = note_JsonObject.getString("id")
                createdAt = note_JsonObject.getString("createdAt")
                notification_Type = note_JsonObject.getString("type")
                //Account
                val account_JsonObject = note_JsonObject.getJSONObject("user")
                display_name = account_JsonObject.getString("name")
                acct = account_JsonObject.getString("username")
                avatarUrl = account_JsonObject.getString("avatarUrl")
                avatarUrlNotGIF = account_JsonObject.getString("avatarUrl")
                user_ID = account_JsonObject.getString("id")
                //Status
                toot_text = ""
                //createdAt = "";
                url = ""
                visibility = ""
                toot_ID = ""
                //Null
                if (note_JsonObject.getString("type").contains("reaction")) {
                    reaction_Type = note_JsonObject.getString("reaction")
                }
                //返信しかこれない
                if (!note_JsonObject.isNull("note")) {
                    val status_JsonObject = note_JsonObject.getJSONObject("note")
                    toot_text = status_JsonObject.getString("text")
                    //url = status_JsonObject.getString("url");
                    isBT = status_JsonObject.getString("myRenoteId")
                    btCount = status_JsonObject.getString("renoteCount")
                    isFav = status_JsonObject.getString("myReaction")
                    visibility = status_JsonObject.getString("visibility")
                    toot_ID = status_JsonObject.getString("id")
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
                    val account_emoji = account_JsonObject.getJSONArray("emojis")
                    for (e in 0 until account_emoji.length()) {
                        val jsonObject = account_emoji.getJSONObject(e)
                        val emoji_name = jsonObject.getString("name")
                        val emoji_url = jsonObject.getString("url")
                        val custom_emoji_src = "<img src=\'$emoji_url\'>"
                        display_name = display_name!!.replace(":$emoji_name:", custom_emoji_src)
                    }
                }


            }


        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    companion object {
        //TLタイプ
        var HOME_TL = "home"
        var LOCAL_TL = "local"
        var FEDERATED_TL = "federated"
    }

}
