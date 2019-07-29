package io.github.takusan23.Kaisendon.CustomMenu


import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.woxthebox.draglistview.DragListView
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.BackupRestoreBottomDialog
import io.github.takusan23.Kaisendon.R
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class CustomMenuSettingFragment : Fragment() {
    private var helper: CustomMenuSQLiteHelper? = null
    private var db: SQLiteDatabase? = null

    private var add_Button: Button? = null
    private var backup_restore_Button: Button? = null
    private var arrayList: ArrayList<String>? = null
    private var nameStringArrayList: ArrayList<String>? = null
    private var dragListView: DragListView? = null

    //一時保存 移転先
    private var old_name = ""
    private var old_value = ""
    //移転前
    private val new_name = ""
    private val new_value = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_menu_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        add_Button = view.findViewById(R.id.add_custom_menu_button)
        backup_restore_Button = view.findViewById(R.id.custom_menu_backup_restore)
        dragListView = view.findViewById(R.id.custom_menu_listview)

        (context as AppCompatActivity).setTitle(R.string.custom_menu_setting)

        //SQLite
        if (helper == null) {
            helper = CustomMenuSQLiteHelper(context!!)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            //WALを利用しない（一時ファイル？が作成されてしまってバックアップ関係でうまく動かないので）
            db!!.disableWriteAheadLogging()
        }


        //追加画面
        add_Button!!.setOnClickListener {
            //startActivity(Intent(context, AddCustomMenuActivity::class.java))
            val fragment = AddCustomMenuBottomFragment()
            if (activity != null) {
                fragment.show(activity!!.supportFragmentManager, "add_custom_menu")
            }
        }

        //バックアップ、リストアメニュー
        backup_restore_Button!!.setOnClickListener {
            //パーミッションチェック
            //ストレージ読み込み、書き込み権限チェック
            val read = ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            val write = ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED) {
                //許可済み
                BackupRestoreMenu()
            } else {
                //許可を求める
                //配列なんだねこれ
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 4545)
            }
        }


        loadSQLite()
        //ListViewドラッグとか
        setDragListView()

        /*
        dragListView.setMyDragListener(new DragListView.MyDragListener() {
            @Override
            public void onDragFinish(int srcPositon, int finalPosition) {
                //置き換え先のデータ取得
                //_idが1から始まるので１足す
                //動かしたアイテムが入る場所に元あったアイテムを一時避難
                getOldData(String.valueOf((finalPosition) + 1));
                //一時避難で逃した場所に移動させたいアイテムを入れる
                setNewData(String.valueOf((srcPositon) + 1), String.valueOf((finalPosition) + 1));
                //一時避難してたアイテムを移動させたアイテムが元あった場所にしまう
                setNewTmpData(String.valueOf((srcPositon) + 1));

                //Toast.makeText(getContext(), "移動前 : " + String.valueOf((srcPositon) + 1) + "\n" + "移転後 : " + String.valueOf((finalPosition) + 1), Toast.LENGTH_LONG).show();
            }
        });
*/


        //ListViewくりっく
        /*
        dragListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //編集画面に飛ばす
                ListView list = (ListView) parent;
                Intent intent = new Intent(getContext(), AddCustomMenuActivity.class);
                intent.putExtra("delete_button", true);
                intent.putExtra("name", nameStringArrayList.get(position));
                startActivity(intent);
            }
        });
*/

    }

    /**
     * 置き換え先データ取得
     */
    private fun getOldData(index: String) {
        val cursor = db!!.query(
                "custom_menudb",
                arrayOf("name", "setting"),
                "_id=?",
                arrayOf(index), null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            old_name = cursor.getString(0)
            old_value = cursor.getString(1)
            cursor.close()
        }
    }

    /**
     * 置き換え実行
     */
    private fun setNewData(old_index: String, new_index: String) {
        //移転前取得
        val cursor = db!!.query(
                "custom_menudb",
                arrayOf("name", "setting"),
                "_id=?",
                arrayOf(old_index), null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val new_name = cursor.getString(0)
            val new_value = cursor.getString(1)
            //入れる
            val values = ContentValues()
            values.put("name", new_name)
            values.put("setting", new_value)
            db!!.update("custom_menudb", values, "_id=?", arrayOf(new_index))
            cursor.close()
        }

    }

    /**
     * 一時避難してたデータを入れる
     */
    private fun setNewTmpData(new_index: String) {
        //移転したアイテムと入れ替え
        //入れる
        val values = ContentValues()
        values.put("name", old_name)
        values.put("setting", old_value)
        db!!.update("custom_menudb", values, "_id=?", arrayOf(new_index))
    }

    /**
     * SQLite読み込み
     */
    private fun loadSQLite() {
        val testArrayList = ArrayList<Pair<Long, String>>()
        arrayList = ArrayList()
        nameStringArrayList = ArrayList()
        val cursor = db!!.query(
                "custom_menudb",
                arrayOf("name", "setting"), null, null, null, null, null
        )
        cursor.moveToFirst()
        for (i in 0 until cursor.count) {
            arrayList!!.add(cursor.getString(0))
            nameStringArrayList!!.add(cursor.getString(0))
            testArrayList.add(Pair(i.toLong(), cursor.getString(0)))
            cursor.moveToNext()
        }
        dragListView!!.setLayoutManager(LinearLayoutManager(context))
        val listAdapter = ItemAdapter(activity as AppCompatActivity, testArrayList, R.layout.list_item, R.id.image, false)
        dragListView!!.setAdapter(listAdapter, true)
        dragListView!!.setCanDragHorizontally(false)
        cursor.close()
    }

    /**
     * DragListViewとか
     */
    private fun setDragListView() {
        dragListView!!.setDragListListener(object : DragListView.DragListListener {
            override fun onItemDragStarted(position: Int) {
                //Toast.makeText(getContext(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {

            }

            override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
                /*

                //置き換え先のデータ取得
                //_idが1から始まるので１足す
                //動かしたアイテムが入る場所にあるアイテムを一時避難
                getOldData(String.valueOf((toPosition) + 1));
                //一時避難で逃した場所に移動させたいアイテムを入れる
                setNewData(String.valueOf((fromPosition) + 1), String.valueOf((toPosition) + 1));
                //一時避難してたアイテムを移動させたアイテムが元あった場所にしまう
                setNewTmpData(String.valueOf((fromPosition) + 1));
*/

                //Toast.makeText(getContext(), "End - position: " + toPosition + "\n" + "Start - position: " + fromPosition, Toast.LENGTH_SHORT).show();
                setSortMenu(fromPosition, toPosition)

            }
        })
    }

    /**
     * バックアップ、リストアメニュー
     */
    private fun BackupRestoreMenu() {
        val dialogFragment = BackupRestoreBottomDialog()
        dialogFragment.show(activity!!.supportFragmentManager, "backup_restore_menu")
    }

    /**
     * Snackber
     */
    private fun showSnackber(message: String, action: String, clickListener: View.OnClickListener): Snackbar {
        val snackbar = Snackbar.make(backup_restore_Button!!, message, Snackbar.LENGTH_SHORT)
        val snackBer_viewGrop = snackbar.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 10
        snackbar.setAction(action, clickListener)
        snackbar.show()
        return snackbar
    }

    /**
     * 新置き換えシステム
     */
    private fun setSortMenu(start: Int, end: Int) {
        //startを一時保存アンド削除
        val start_item = nameStringArrayList!![start]
        nameStringArrayList!!.remove(start_item)
        //入れる
        nameStringArrayList!!.add(end, start_item)
        //一時的にSQLiteの内容を配列に入れる
        val name_List = ArrayList<String>()
        val value_List = ArrayList<String>()
        for (i in nameStringArrayList!!.indices) {
            //Step 1.name/valueを取得する
            name_List.add(nameStringArrayList!![i])
            value_List.add(getSQLiteDBValue(nameStringArrayList!![i])!!)
        }

        //Step 2.SQLite更新
        //最初にDB全クリアする
        db!!.delete("custom_menudb", null, null)
        for (i in name_List.indices) {
            writeSQLiteDB((i + 1).toString(), name_List[i], value_List[i])
        }

        //メニュー再読み込み
        reLoadMenu()

    }


    /**
     * SQLiteから指定した名前の値を返します
     */
    private fun getSQLiteDBValue(name: String): String? {
        var value: String? = null
        val cursor = db!!.query(
                "custom_menudb",
                arrayOf("name", "setting"),
                "name=?",
                arrayOf(name), null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getString(1)
            cursor.close()
        }
        return value
    }

    /**
     * SQLite書き込む
     */
    private fun writeSQLiteDB(position: String, name: String, value: String) {
        //入れる
        val values = ContentValues()
        values.put("name", name)
        values.put("setting", value)
        db!!.insert("custom_menudb", "", values)
    }

    /*めにゅー再生成*/
    private fun reLoadMenu() {
        val navigationView = activity!!.findViewById<NavigationView>(R.id.nav_view)
        val customMenuLoadSupport = CustomMenuLoadSupport(context!!, navigationView)
        //再読み込み
        navigationView.menu.clear()
        navigationView.inflateMenu(R.menu.custom_menu)
        customMenuLoadSupport.loadCustomMenu(null)
    }


}
