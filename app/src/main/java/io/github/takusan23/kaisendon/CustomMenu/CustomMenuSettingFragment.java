package io.github.takusan23.kaisendon.CustomMenu;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.BottomDialodFragment;
import io.github.takusan23.kaisendon.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomMenuSettingFragment extends Fragment {
    private CustomMenuSQLiteHelper helper;
    private SQLiteDatabase db;

    private Button add_Button;
    private ListView listView;
    private Button backup_restore_Button;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_menu_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        add_Button = view.findViewById(R.id.add_custom_menu_button);
        listView = view.findViewById(R.id.custom_menu_listview);
        backup_restore_Button = view.findViewById(R.id.custom_menu_backup_restore);

        ((AppCompatActivity) getContext()).setTitle(R.string.custom_menu_setting);

        //SQLite
        if (helper == null) {
            helper = new CustomMenuSQLiteHelper(getContext());
        }
        if (db == null) {
            db = helper.getWritableDatabase();
        }


        //追加画面
        add_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddCustomMenuActivity.class));
            }
        });

        //バックアップ、リストアメニュー
        BackupRestoreMenu();


        loadSQLite();

        //ListViewくりっく
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //編集画面に飛ばす
                ListView list = (ListView) parent;
                Intent intent = new Intent(getContext(), AddCustomMenuActivity.class);
                intent.putExtra("delete_button", true);
                intent.putExtra("name", (String) list.getItemAtPosition(position));
                startActivity(intent);
            }
        });


    }

    /**
     * SQLite読み込み
     */
    private void loadSQLite() {
        ArrayList<String> list = new ArrayList<>();
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
            list.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        ArrayAdapter adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }

    /**
     * バックアップ、リストアメニュー
     */
    @SuppressLint("RestrictedApi")
    private void BackupRestoreMenu() {
        //ポップアップメニュー作成
        MenuBuilder menuBuilder = new MenuBuilder(getContext());
        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(R.menu.custom_backup_restore_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(getContext(), menuBuilder, backup_restore_Button);
        optionsMenu.setForceShowIcon(true);
        //クリックイベント
        backup_restore_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //表示
                optionsMenu.show();
                //押したときの反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        //分ける
                        switch (menuItem.getItemId()) {
                            case R.id.backup_menu:
                                showSnackber("バックアップを作成しますか？\n背景画像はバックアップできません", "作成", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startBackupDB();
                                    }
                                });
                                break;
                            case R.id.restore_menu:
                                showSnackber("バックアップから復元しますか？\n現在のカスタムメニューは上書きされます", "復元", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startRestore();
                                    }
                                });
                                break;
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });
            }
        });
    }

    /**
     * Snackber
     */
    private Snackbar showSnackber(String message, String action, View.OnClickListener clickListener) {
        Snackbar snackbar = Snackbar.make(backup_restore_Button, message, Toast.LENGTH_SHORT);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(10);
        snackbar.setAction(action, clickListener);
        snackbar.show();
        return snackbar;
    }

    /*バックアップ、リストアはちゃんとUI作って書き直す予定（）*/

    /**
     * DataBaseバックアップ？
     *
     * https://stackoverflow.com/questions/18635412/restoring-sqlite-db-file
     */
    private void startBackupDB() {
        try {
            Toast.makeText(getContext(), "バックアップ実行", Toast.LENGTH_SHORT).show();
            File sd = new File(Environment.getExternalStorageDirectory().getPath() + "/kaisendon");
            // kaisendonディレクトリを作成する
            sd.mkdir();
            //ユーザーが扱えない領域？
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String databasepath = "//data/io.github.takusan23.kaisendon/databases/CustomMenu.db";
                String backupfilename = "CustomMenu.db";
                File currentDB = new File(data, databasepath);
                File backupDB = new File(sd, backupfilename);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getContext(), "バックアップを作成しました\n" + sd.getPath() + "/" + backupfilename, Toast.LENGTH_SHORT).show();
                }
            }

            if (sd.canWrite()) {
                String databasepath = "//data/io.github.takusan23.kaisendon/databases/CustomMenu.db-shm";
                String backupfilename = "CustomMenu.db-shm";
                File currentDB = new File(data, databasepath);
                File backupDB = new File(sd, backupfilename);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getContext(), "バックアップを作成しました\n" + sd.getPath() + "/" + backupfilename, Toast.LENGTH_SHORT).show();
                }
            }
            if (sd.canWrite()) {
                String databasepath = "//data/io.github.takusan23.kaisendon/databases/CustomMenu.db-wal";
                String backupfilename = "CustomMenu.db-wal";
                File currentDB = new File(data, databasepath);
                File backupDB = new File(sd, backupfilename);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getContext(), "バックアップを作成しました\n" + sd.getPath() + "/" + backupfilename, Toast.LENGTH_SHORT).show();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "try-catch", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * リストア
     */
    private void startRestore() {
        try {
            Toast.makeText(getContext(), "リストア実行", Toast.LENGTH_SHORT).show();
            File sd = new File(Environment.getExternalStorageDirectory().getPath() + "/kaisendon");
            //ユーザーが扱えない領域？
            File data = Environment.getDataDirectory();


            if (sd.canWrite()) {
                String databasepath = "//data/io.github.takusan23.kaisendon/databases/CustomMenu.db";
                String backupfilename = "CustomMenu.db";
                File currentDB = new File(data, databasepath);
                File backupDB = new File(sd, backupfilename);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getContext(), "リストアが完了しました\n" + sd.getPath() + "/" + backupfilename, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), sd.getPath() + "/" + "CustomMenu.db" + "\n" + "にデーターベースがあることを確認してください。", Toast.LENGTH_LONG).show();
            }


            if (sd.canWrite()) {
                String databasepath = "//data/io.github.takusan23.kaisendon/databases/CustomMenu.db-shm";
                String backupfilename = "CustomMenu.db-shm";
                File currentDB = new File(data, databasepath);
                File backupDB = new File(sd, backupfilename);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getContext(), "リストアが完了しました\n" + sd.getPath() + "/" + backupfilename, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), sd.getPath() + "/" + "CustomMenu.db" + "\n" + "にデーターベースがあることを確認してください。", Toast.LENGTH_LONG).show();
            }

            if (sd.canWrite()) {
                String databasepath = "//data/io.github.takusan23.kaisendon/databases/CustomMenu.db-wal";
                String backupfilename = "CustomMenu.db-wal";
                File currentDB = new File(data, databasepath);
                File backupDB = new File(sd, backupfilename);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getContext(), "リストアが完了しました\n" + sd.getPath() + "/" + backupfilename, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), sd.getPath() + "/" + "CustomMenu.db" + "\n" + "にデーターベースがあることを確認してください。", Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "try-catch", Toast.LENGTH_SHORT).show();
        }
    }

}
