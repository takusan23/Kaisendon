package io.github.takusan23.kaisendon.Fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;

import io.github.takusan23.kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter;
import io.github.takusan23.kaisendon.CustomMenu.Dialog.BackupRestoreBottomDialog;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.TootBookmark_SQLite;

public class Bookmark_Frament extends Fragment {
    private RecyclerView recyclerView;
    private CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter;
    private ArrayList<ArrayList> recyclerViewList;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    //BookMarkDB
    private TootBookmark_SQLite tootBookmark_sqLite;
    private SQLiteDatabase db;
    //めにゅー
    private Button backup_restore_Button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.bookmark_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.bookmark);
        recyclerView = view.findViewById(R.id.bookmark_recycler_view);
        backup_restore_Button = view.findViewById(R.id.bookmark_backup_restore_button);
        recyclerViewList = new ArrayList<>();
        //ここから下三行必須
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
        recyclerView.setAdapter(customMenuRecyclerViewAdapter);
        recyclerViewLayoutManager = recyclerView.getLayoutManager();

        if (tootBookmark_sqLite == null) {
            tootBookmark_sqLite = new TootBookmark_SQLite(getContext());
        }
        if (db == null) {
            db = tootBookmark_sqLite.getWritableDatabase();
            db.disableWriteAheadLogging();
        }

        //読み込み
        getDBData();
        //Backup/Restore
        setBookmarkBackupRestore();
    }

    /**
     * データ読み込み
     */
    private void getDBData() {
        Cursor cursor = db.query(
                "tootbookmarkdb",
                new String[]{"instance", "json"},
                null,
                null,
                null,
                null,
                null
        );
        //スタートに
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            //配列を作成
            ArrayList<String> Item = new ArrayList<>();
            //メモとか通知とかに
            Item.add("BookMark");
            //内容
            Item.add("");
            //ユーザー名
            Item.add("");
            //JSONObject
            Item.add(cursor.getString(1));
            //ぶーすとした？
            Item.add("false");
            //ふぁぼした？
            Item.add("false");
            //Mastodon / Misskey
            Item.add("Mastodon");
            //あと適当
            Item.add("");
            Item.add("");
            Item.add("");

            recyclerViewList.add(Item);
            //つぎ
            cursor.moveToNext();
        }
        cursor.close();
        //配列を逆にする
        Collections.reverse(recyclerViewList);
        recyclerView.setAdapter(customMenuRecyclerViewAdapter);
    }

    /**
     * ブックマーク　バックアップ・復元
     */
    private void setBookmarkBackupRestore() {
        backup_restore_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackupRestoreBottomDialog dialogFragment = new BackupRestoreBottomDialog();
                dialogFragment.show(getActivity().getSupportFragmentManager(), "backup_restore_menu");
            }
        });
    }


}