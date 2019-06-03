package io.github.takusan23.Kaisendon.CustomMenu;


import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;

import io.github.takusan23.Kaisendon.CustomMenu.Dialog.BackupRestoreBottomDialog;
import io.github.takusan23.Kaisendon.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomMenuSettingFragment extends Fragment {
    private CustomMenuSQLiteHelper helper;
    private SQLiteDatabase db;

    private Button add_Button;
    private Button backup_restore_Button;
    private ArrayList<String> arrayList;
    private ArrayList<String> nameStringArrayList;
    private DragListView dragListView;

    //一時保存 移転先
    private String old_name = "";
    private String old_value = "";
    //移転前
    private String new_name = "";
    private String new_value = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_menu_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        add_Button = view.findViewById(R.id.add_custom_menu_button);
        backup_restore_Button = view.findViewById(R.id.custom_menu_backup_restore);
        dragListView = view.findViewById(R.id.custom_menu_listview);

        ((AppCompatActivity) getContext()).setTitle(R.string.custom_menu_setting);

        //SQLite
        if (helper == null) {
            helper = new CustomMenuSQLiteHelper(getContext());
        }
        if (db == null) {
            db = helper.getWritableDatabase();
            //WALを利用しない（一時ファイル？が作成されてしまってバックアップ関係でうまく動かないので）
            db.disableWriteAheadLogging();
        }


        //追加画面
        add_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddCustomMenuActivity.class));
            }
        });

        //バックアップ、リストアメニュー
        backup_restore_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //パーミッションチェック
                //ストレージ読み込み、書き込み権限チェック
                int read = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
                int write = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED) {
                    //許可済み
                    BackupRestoreMenu();
                } else {
                    //許可を求める
                    //配列なんだねこれ
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4545);
                }

            }
        });


        loadSQLite();
        //ListViewドラッグとか
        setDragListView();

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
    private void getOldData(String index) {
        Cursor cursor = db.query(
                "custom_menudb",
                new String[]{"name", "setting"},
                "_id=?",
                new String[]{index},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            old_name = cursor.getString(0);
            old_value = cursor.getString(1);
            cursor.close();
        }
    }

    /**
     * 置き換え実行
     */
    private void setNewData(String old_index, String new_index) {
        //移転前取得
        Cursor cursor = db.query(
                "custom_menudb",
                new String[]{"name", "setting"},
                "_id=?",
                new String[]{old_index},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            String new_name = cursor.getString(0);
            String new_value = cursor.getString(1);
            //入れる
            ContentValues values = new ContentValues();
            values.put("name", new_name);
            values.put("setting", new_value);
            db.update("custom_menudb", values, "_id=?", new String[]{new_index});
            cursor.close();
        }

    }

    /**
     * 一時避難してたデータを入れる
     */
    private void setNewTmpData(String new_index) {
        //移転したアイテムと入れ替え
        //入れる
        ContentValues values = new ContentValues();
        values.put("name", old_name);
        values.put("setting", old_value);
        db.update("custom_menudb", values, "_id=?", new String[]{new_index});
    }

    /**
     * SQLite読み込み
     */
    private void loadSQLite() {
        ArrayList<Pair<Long, String>> testArrayList = new ArrayList<>();
        arrayList = new ArrayList<>();
        nameStringArrayList = new ArrayList<>();
        Cursor cursor = db.query(
                "custom_menudb",
                new String[]{"name", "setting"},
                null,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            arrayList.add(cursor.getString(0));
            nameStringArrayList.add(cursor.getString(0));
            testArrayList.add(new Pair<>((long) i, cursor.getString(0)));
            cursor.moveToNext();
        }
        dragListView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter listAdapter = new ItemAdapter(testArrayList, R.layout.list_item, R.id.image, false);
        dragListView.setAdapter(listAdapter, true);
        dragListView.setCanDragHorizontally(false);
        cursor.close();
    }

    /**
     * DragListViewとか
     */
    private void setDragListView() {
        dragListView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {
                //Toast.makeText(getContext(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
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
                setSortMenu(fromPosition, toPosition);

            }
        });
    }

    /**
     * バックアップ、リストアメニュー
     */
    private void BackupRestoreMenu() {
        BackupRestoreBottomDialog dialogFragment = new BackupRestoreBottomDialog();
        dialogFragment.show(getActivity().getSupportFragmentManager(), "backup_restore_menu");
    }

    /**
     * Snackber
     */
    private Snackbar showSnackber(String message, String action, View.OnClickListener clickListener) {
        Snackbar snackbar = Snackbar.make(backup_restore_Button, message, Snackbar.LENGTH_SHORT);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
        snackBer_textView.setMaxLines(10);
        snackbar.setAction(action, clickListener);
        snackbar.show();
        return snackbar;
    }

    /**
     * 新置き換えシステム
     */
    private void setSortMenu(int start, int end) {
        //startを一時保存アンド削除
        String start_item = nameStringArrayList.get(start);
        nameStringArrayList.remove(start_item);
        //入れる
        nameStringArrayList.add(end, start_item);
        //一時的にSQLiteの内容を配列に入れる
        ArrayList<String> name_List = new ArrayList<>();
        ArrayList<String> value_List = new ArrayList<>();
        for (int i = 0; i < nameStringArrayList.size(); i++) {
            //Step 1.name/valueを取得する
            name_List.add(nameStringArrayList.get(i));
            value_List.add(getSQLiteDBValue(nameStringArrayList.get(i)));
        }

        //Step 2.SQLite更新
        //最初にDB全クリアする
        db.delete("custom_menudb", null, null);
        for (int i = 0; i < name_List.size(); i++) {
            writeSQLiteDB(String.valueOf((i) + 1), name_List.get(i), value_List.get(i));
        }

        //メニュー再読み込み
        reLoadMenu();

    }


    /**
     * SQLiteから指定した名前の値を返します
     */
    private String getSQLiteDBValue(String name) {
        String value = null;
        Cursor cursor = db.query(
                "custom_menudb",
                new String[]{"name", "setting"},
                "name=?",
                new String[]{name},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getString(1);
            cursor.close();
        }
        return value;
    }

    /**
     * SQLite書き込む
     */
    private void writeSQLiteDB(String position, String name, String value) {
        //入れる
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("setting", value);
        db.insert("custom_menudb", "", values);
    }

    /*Fragment再生成*/
    private void reLoadMenu() {
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        CustomMenuLoadSupport customMenuLoadSupport = new CustomMenuLoadSupport(getContext(), navigationView);
        //再読み込み
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.custom_menu);
        customMenuLoadSupport.loadCustomMenu(null);
    }

}