package io.github.takusan23.Kaisendon

import java.util.*

class ListItem {
    /*
    private Bitmap mThumbnail = null;
    private String mTitle = null;
    private String mUser = null;
    private String mClient = null;
    private String mNicoru = null;
    private String mAvater = null;
    private String mAccountID = null;
    private long mID;
    //private String[] mMedia = new String[4];
    private String mInfo;
    private String mMedia_1;
    private String mMedia_2;
    private String mMedia_3;
    private String mMedia_4;
    private ArrayList<String> stringList = null;
*/


    //全部配列で管理するぞ！！！！
    // @param listItem 配列
    var listItem: ArrayList<String>? = null


    /**
     * 空のコンストラクタ
     */
    constructor() {}


    //@param item 配列
    constructor(item: ArrayList<String>) {
        listItem = item
    }


    /*
     */
    /*
     * コンストラクタ
     *
     * @param title タイトル
     *//*

    public ListItem(String info, String title, String user, String client, String nicoru, String avater, long account_id, String user_id, String media1, String media2, String media3, String media4,ArrayList<String> stringlist) {
        mInfo = info;
        mTitle = title;
        mUser = user;
        mClient = client;
        mNicoru = nicoru;
        mAvater = avater;
        mID = account_id;
        mAccountID = user_id;
        mMedia_1 = media1;
        mMedia_2 = media2;
        mMedia_3 = media3;
        mMedia_4 = media4;
        stringList = stringlist;
    }
*/


    /*
     */
    /**
     * サムネイル画像を設定
     *
     * @param thumbnail サムネイル画像
     *//*

    public void setThumbnail(Bitmap thumbnail) {
        mThumbnail = thumbnail;
    }


    //ニコる
    public void setmNicoru(String nicoru) {
        mNicoru = nicoru;
    }

    */
    /**
     * タイトルを設定
     *
     * @param title タイトル
     *//*

    public void setmTitle(String title) {
        mTitle = title;
    }


    //ユーザー名設定
    public void setmUserUser(String user) {
        mUser = user;
    }


    //クライアント
    public void setmClient(String client) {
        mClient = client;
    }

    //背景管理用
    public void setmInfo(String info) {
        mInfo = info;
    }


    //ニコる
    public String getNicoru() {
        return mNicoru;
    }


    //ユーザーアバター
    public String getAvater() {
        return mAvater;
    }

    //ID
    public long getID() {
        return mID;
    }

    //User ID
    public String getUserID() {
        return mAccountID;
    }

    //Media Url
    public String getMedia1() {
        return mMedia_1;
    }

    public String getMedia2() {
        return mMedia_2;
    }

    public String getMedia3() {
        return mMedia_3;
    }

    public String getMedia4() {
        return mMedia_4;
    }


    */
    /**
     * サムネイル画像を取得
     *
     * @return サムネイル画像
     *//*

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    */
    /**
     * タイトルを取得
     *
     * @return タイトル
     *//*

    public String getTitle() {
        return mTitle;
    }

    //ユーザー名
    public String getUser() {
        return mUser;
    }

    //クライアント
    public String getClient() {
        return mClient;
    }

    //メモ用
    public String getInfo() {
        return mInfo;
    }

    public ArrayList<String> getStringList() {return stringList;}
*/
}