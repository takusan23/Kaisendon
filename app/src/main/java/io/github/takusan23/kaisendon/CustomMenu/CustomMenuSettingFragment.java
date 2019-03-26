package io.github.takusan23.kaisendon.CustomMenu;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
    private void BackupRestoreMenu() {
        BackupRestoreBottomDialog dialogFragment = new BackupRestoreBottomDialog();
        dialogFragment.show(getActivity().getSupportFragmentManager(), "backup_restore_menu");
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

}
