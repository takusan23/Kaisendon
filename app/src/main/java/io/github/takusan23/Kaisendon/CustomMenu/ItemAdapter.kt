package io.github.takusan23.Kaisendon.CustomMenu

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.woxthebox.draglistview.DragItemAdapter
import io.github.takusan23.Kaisendon.R
import java.util.*

internal class ItemAdapter(activity: AppCompatActivity, list: ArrayList<Pair<Long, String>>, private val mLayoutId: Int, private val mGrabHandleId: Int, private val mDragOnLongPress: Boolean) : DragItemAdapter<Pair<Long, String>, ItemAdapter.ViewHolder>() {
    private var helper: CustomMenuSQLiteHelper? = null
    private var db: SQLiteDatabase? = null
    val activity = activity

    init {
        itemList = list
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val text = mItemList[position].second
        holder.mText.text = text
        holder.itemView.tag = mItemList[position]
        //setPopupMenu(holder)

        //コピーボタン、削除ボタン
        setCopyButton(holder)
        setDeleteButton(holder)

        holder.mText.setOnClickListener {
            //編集画面へ
            val edit = AddCustomMenuBottomFragment()
            val bundle = Bundle()
            bundle.putBoolean("delete_button", true)
            bundle.putString("name", mItemList.get(position).second)
            edit.arguments = bundle
            edit.show(activity.supportFragmentManager, "add_custom_menu")
/*
            val intent = Intent(holder.itemView.context, AddCustomMenuActivity::class.java)
            intent.putExtra("delete_button", true)
            intent.putExtra("name", mItemList[position].second)
            holder.itemView.context.startActivity(intent)
*/
        }

        //SQLite
        if (helper == null) {
            helper = CustomMenuSQLiteHelper(holder.mText.context)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            //WALを利用しない（一時ファイル？が作成されてしまってバックアップ関係でうまく動かないので）
            db!!.disableWriteAheadLogging()
        }

    }

    override fun getUniqueItemId(position: Int): Long {
        return mItemList[position].first!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(mLayoutId, parent, false)
        return ViewHolder(view)
    }

    internal inner class ViewHolder(itemView: View) : DragItemAdapter.ViewHolder(itemView, mGrabHandleId, mDragOnLongPress) {
        var mText: TextView
        var deleteButton: ImageView
        var copyButton: ImageView

        init {
            mText = itemView.findViewById<View>(R.id.text) as TextView
            deleteButton = itemView.findViewById(R.id.drag_item_delete_button)
            copyButton = itemView.findViewById(R.id.drag_item_copy_button)
        }

        override fun onItemClicked(view: View?) {
            //Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        override fun onItemLongClicked(view: View?): Boolean {
            //Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true
        }
    }

    /**
     * 編集画面出す
     */
    private fun showCustomMenuEditor(name: String) {

    }


    /*
    * コピーボタン
    * */
    private fun setCopyButton(viewHolder: ViewHolder) {
        val title = viewHolder.mText.text.toString()
        val context = viewHolder.mText.context
        viewHolder.copyButton.setOnClickListener {
            copyCustomMenu(title, context, viewHolder)
        }
    }

    /*
    * 削除ボタン
    * */
    private fun setDeleteButton(viewHolder: ViewHolder) {
        val title = viewHolder.mText.text.toString()
        val context = viewHolder.mText.context
        viewHolder.deleteButton.setOnClickListener {
            deleteCustomMenu(title, context, viewHolder)
        }
    }

    /*メニュー作成*/
    @SuppressLint("RestrictedApi")
    private fun setPopupMenu(viewHolder: ViewHolder) {
        val context = viewHolder.itemView.context
        val menuBuilder = MenuBuilder(context)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.custom_menu_list_long_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(context, menuBuilder, viewHolder.mText)
        optionsMenu.setForceShowIcon(true)

        viewHolder.mText.setOnLongClickListener {
            optionsMenu.show()
            //選択
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.custom_menu_long_copy -> copyCustomMenu(viewHolder.mText.text.toString(), context, viewHolder)
                        R.id.custom_menu_long_delete -> deleteCustomMenu(viewHolder.mText.text.toString(), context, viewHolder)
                    }
                    return false
                }

                override fun onMenuModeChange(menu: MenuBuilder) {

                }
            })
            true
        }
    }

    /*データベースコピー*/
    private fun copyCustomMenu(name: String, context: Context, viewHolder: ViewHolder) {
        Snackbar.make(viewHolder.mText, context.getString(R.string.custom_setting_copy_message), Snackbar.LENGTH_SHORT).setAction(context.getString(R.string.duplicate), View.OnClickListener {
            var setting = ""
            //読み込む
            val cursor = db!!.query(
                    "custom_menudb",
                    arrayOf("setting"),
                    "name=?",
                    arrayOf(name), null, null, null
            )
            cursor.moveToFirst()
            for (i in 0 until cursor.count) {
                setting = cursor.getString(0)
                cursor.moveToNext()
            }
            cursor.close()
            val values = ContentValues()
            values.put("name", name + " (" + context.getString(R.string.copy) + ")")
            values.put("setting", setting)
            db!!.insert("custom_menudb", null, values)
            reStartFragment(context)
        }).show()
    }

    /*削除機能つける？*/
    private fun deleteCustomMenu(name: String, context: Context, viewHolder: ViewHolder) {
        Snackbar.make(viewHolder.mText, R.string.custom_setting_delete_message, Snackbar.LENGTH_SHORT).setAction(R.string.delete_ok) {
            db!!.delete("custom_menudb", "name=?", arrayOf(name))
            reStartFragment(context)
        }.show()
    }

    /*Fragment再生成*/
    private fun reStartFragment(context: Context) {
        //Fragment再生成
        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_container, CustomMenuSettingFragment())
        transaction.commit()
    }
}