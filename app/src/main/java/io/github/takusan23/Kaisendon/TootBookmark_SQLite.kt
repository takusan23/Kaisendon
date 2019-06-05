package io.github.takusan23.Kaisendon

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TootBookmark_SQLite(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        db.execSQL(
                SQL_CREATE_ENTRIES
        )

        Log.d("debug", "onCreate(SQLiteDatabase db)")

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // アップデートの判別
        db.execSQL(
                SQL_DELETE_ENTRIES
        )
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // データーベースのバージョン
        private val DATABASE_VERSION = 1

        // データーベース名
        private val DATABASE_NAME = "TootBookmark.db"
        private val TABLE_NAME = "tootbookmarkdb"
        private val _ID = "_id"
        private val JSON = "json"
        private val INSTANCE = "instance"

        // , を付け忘れるとエラー
        private val SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                INSTANCE + " TEXT," +
                JSON + " TEXT)"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

}