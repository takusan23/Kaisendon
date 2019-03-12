package io.github.takusan23.kaisendon.CustomMenu;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
                new String[]{"name", "memo", "content", "instance", "access_token", "image_load", "dialog", "dark_mode", "position", "streaming", "subtitle", "setting"},
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

}
