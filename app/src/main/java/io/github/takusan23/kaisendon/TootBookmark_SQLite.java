package io.github.takusan23.kaisendon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

public class TootBookmark_SQLite extends SQLiteOpenHelper {
    // データーベースのバージョン
    private static final int DATABASE_VERSION = 1;

    // データーベース名
    private static final String DATABASE_NAME = "TootBookmark.db";
    private static final String TABLE_NAME = "tootbookmarkdb";
    private static final String _ID = "_id";
    private static final String COLUMN_NAME_TITLE = "toot";
    private static final String COLUMN_NAME_SUBTITLE = "id";
    private static final String ACCOUNT_NAME = "account";
    private static final String INFO = "info";
    private static final String ACCOUNT_ID = "account_id";
    private static final String AVATER_URL = "avater_url";
    private static final String USERNAME = "username";
    private static final String MEDIA_1 = "media1";
    private static final String MEDIA_2 = "media2";
    private static final String MEDIA_3 = "media3";
    private static final String MEDIA_4 = "media4";

    // , を付け忘れるとエラー
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_TITLE + " TEXT," +
                    COLUMN_NAME_SUBTITLE + " INTEGER," +
                    ACCOUNT_NAME + " TEXT," +
                    INFO + " TEXT," +
                    ACCOUNT_ID + " TEXT," +
                    AVATER_URL + " TEXT," +
                    USERNAME + " TEXT," +
                    MEDIA_1 + " TEXT," +
                    MEDIA_2 + " TEXT," +
                    MEDIA_3 + " TEXT," +
                    MEDIA_4 + " TEXT" +
                    ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    TootBookmark_SQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        db.execSQL(
                SQL_CREATE_ENTRIES
        );

        Log.d("debug", "onCreate(SQLiteDatabase db)");

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