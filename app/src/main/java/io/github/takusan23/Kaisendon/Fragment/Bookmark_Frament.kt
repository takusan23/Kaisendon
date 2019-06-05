package io.github.takusan23.Kaisendon.Fragment

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.BackupRestoreBottomDialog
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.TootBookmark_SQLite
import java.util.*

class Bookmark_Frament : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var customMenuRecyclerViewAdapter: CustomMenuRecyclerViewAdapter? = null
    private var recyclerViewList: ArrayList<ArrayList<*>>? = null
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    //BookMarkDB
    private var tootBookmark_sqLite: TootBookmark_SQLite? = null
    private var db: SQLiteDatabase? = null
    //めにゅー
    private var backup_restore_Button: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bookmark_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity!!.setTitle(R.string.bookmark)
        recyclerView = view.findViewById(R.id.bookmark_recycler_view)
        backup_restore_Button = view.findViewById(R.id.bookmark_backup_restore_button)
        recyclerViewList = ArrayList()
        //ここから下三行必須
        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = mLayoutManager
        customMenuRecyclerViewAdapter = CustomMenuRecyclerViewAdapter(recyclerViewList!!)
        recyclerView!!.adapter = customMenuRecyclerViewAdapter
        recyclerViewLayoutManager = recyclerView!!.layoutManager

        if (tootBookmark_sqLite ==
                null) {
            tootBookmark_sqLite = TootBookmark_SQLite(context!!)
        }
        if (db == null) {
            db = tootBookmark_sqLite!!.writableDatabase
            db!!.disableWriteAheadLogging()
        }

        //読み込み
        getDBData()
        //Backup/Restore
        setBookmarkBackupRestore()
    }

    /**
     * データ読み込み
     */
    private fun getDBData() {
        val cursor = db!!.query(
                "tootbookmarkdb",
                arrayOf("instance", "json"), null, null, null, null, null
        )
        //スタートに
        cursor.moveToFirst()
        for (i in 0 until cursor.count) {
            //配列を作成
            val Item = ArrayList<String>()
            //メモとか通知とかに
            Item.add("BookMark")
            //内容
            Item.add("")
            //ユーザー名
            Item.add("")
            //JSONObject
            Item.add(cursor.getString(1))
            //ぶーすとした？
            Item.add("false")
            //ふぁぼした？
            Item.add("false")
            //Mastodon / Misskey
            Item.add("Mastodon")
            //あと適当
            Item.add("")
            Item.add("")
            Item.add("")
            //画像表示、こんてんとわーにんぐ
            Item.add("false")
            Item.add("false")

            recyclerViewList!!.add(Item)
            //つぎ
            cursor.moveToNext()
        }
        cursor.close()
        //配列を逆にする
        Collections.reverse(recyclerViewList!!)
        recyclerView!!.adapter = customMenuRecyclerViewAdapter
    }

    /**
     * ブックマーク　バックアップ・復元
     */
    private fun setBookmarkBackupRestore() {
        backup_restore_Button!!.setOnClickListener {
            val dialogFragment = BackupRestoreBottomDialog()
            dialogFragment.show(activity!!.supportFragmentManager, "backup_restore_menu")
        }
    }


}