package io.github.takusan23.kaisendon.CustomMenu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CustomMenuSQLiteHelper extends SQLiteOpenHelper {
    // データーベースのバージョン
    private static final int DATABASE_VERSION = 1;

    // データーベース名
    private static final String DATABASE_NAME = "CustomMenu.db";
    private static final String TABLE_NAME = "custom_menudb";
    private static final String NAME = "name";
    private static final String MEMO = "memo";
    private static final String CONTENT = "content";
    private static final String ACCOUNT_INSTANCE = "instance";
    private static final String ACCOUNT_ACCESS_TOKEN = "access_token";
    private static final String IMAGE_LOAD = "image_load";
    private static final String DIALOG = "dialog";
    private static final String DARKMODE = "dark_mode";
    private static final String POSITION = "position";
    private static final String STREAMING = "streaming";
    private static final String SUBTITLE = "subtitle";
    private static final String BACKGROUND_IMAGE = "image_url";
    private static final String BACKGROUND_TRANSPARENCY = "background_transparency";
    private static final String BACKGROUND_SCREEN_FIT = "background_screen_fit";
    private static final String SETTING = "setting";


    // , を付け忘れるとエラー
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    NAME + " INTEGER," +
                    MEMO + " TEXT," +
                    CONTENT + " TEXT," +
                    ACCOUNT_INSTANCE + " TEXT," +
                    ACCOUNT_ACCESS_TOKEN + " TEXT," +
                    IMAGE_LOAD + " TEXT," +
                    DIALOG + " TEXT," +
                    DARKMODE + " TEXT," +
                    POSITION + " TEXT," +
                    STREAMING + " TEXT," +
                    SUBTITLE + " TEXT," +
                    BACKGROUND_IMAGE + " TEXT," +
                    BACKGROUND_TRANSPARENCY + " TEXT," +
                    BACKGROUND_SCREEN_FIT + " TEXT," +
                    SETTING + " TEXT" +
                    ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    public CustomMenuSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        db.execSQL(
                SQL_CREATE_ENTRIES
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // アップデートの判別
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
