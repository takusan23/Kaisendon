package io.github.takusan23.Kaisendon.CustomMenu;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import io.github.takusan23.Kaisendon.R;

class ItemAdapter extends DragItemAdapter<Pair<Long, String>, ItemAdapter.ViewHolder> {
    private CustomMenuSQLiteHelper helper;
    private SQLiteDatabase db;
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    ItemAdapter(ArrayList<Pair<Long, String>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setItemList(list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        String text = mItemList.get(position).second;
        holder.mText.setText(text);
        holder.itemView.setTag(mItemList.get(position));
        setPopupMenu(holder);
        holder.mText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), AddCustomMenuActivity.class);
                intent.putExtra("delete_button", true);
                intent.putExtra("name", mItemList.get(position).second);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        //SQLite
        if (helper == null) {
            helper = new CustomMenuSQLiteHelper(holder.mText.getContext());
        }
        if (db == null) {
            db = helper.getWritableDatabase();
            //WALを利用しない（一時ファイル？が作成されてしまってバックアップ関係でうまく動かないので）
            db.disableWriteAheadLogging();
        }

    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).first;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView mText;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = (TextView) itemView.findViewById(R.id.text);
        }

        @Override
        public void onItemClicked(View view) {
            //Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            //Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    /**
     * 編集画面出す
     */
    private void showCustomMenuEditor(String name) {

    }

    /*メニュー作成*/
    @SuppressLint("RestrictedApi")
    private void setPopupMenu(ViewHolder viewHolder) {
        Context context = viewHolder.itemView.getContext();
        MenuBuilder menuBuilder = new MenuBuilder(context);
        MenuInflater inflater = new MenuInflater(context);
        inflater.inflate(R.menu.custom_menu_list_long_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(context, menuBuilder, viewHolder.mText);
        optionsMenu.setForceShowIcon(true);

        viewHolder.mText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                optionsMenu.show();
                //選択
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.custom_menu_long_copy:
                                copyCustomMenu(viewHolder.mText.getText().toString(), context);
                                break;
                            case R.id.custom_menu_long_delete:
                                deleteCustomMenu(viewHolder.mText.getText().toString(), context, viewHolder);
                                break;
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {

                    }
                });
                return true;
            }
        });
    }

    /*データベースコピー*/
    private void copyCustomMenu(String name, Context context) {
        String setting = "";
        //読み込む
        Cursor cursor = db.query(
                "custom_menudb",
                new String[]{"setting"},
                "name=?",
                new String[]{name},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            setting = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();
        ContentValues values = new ContentValues();
        values.put("name", name + " (" + context.getString(R.string.copy) + ")");
        values.put("setting", setting);
        db.insert("custom_menudb", null, values);
        reStartFragment(context);
    }

    /*削除機能つける？*/
    private void deleteCustomMenu(String name, Context context, ViewHolder viewHolder) {
        Snackbar.make(viewHolder.mText, R.string.custom_setting_delete_message, Snackbar.LENGTH_SHORT).setAction(R.string.delete_ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.delete("custom_menudb", "name=?", new String[]{name});
                reStartFragment(context);
            }
        }).show();
    }

    /*Fragment再生成*/
    private void reStartFragment(Context context) {
        //Fragment再生成
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_container, new CustomMenuSettingFragment());
        transaction.commit();
    }
}