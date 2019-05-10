package io.github.takusan23.kaisendon.CustomMenu.Dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.github.takusan23.kaisendon.Activity.UserActivity;
import io.github.takusan23.kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.TootBookmark_SQLite;

public class TootOptionBottomDialog extends BottomSheetDialogFragment {

    private View view;
    private TextView account_Button;
    private TextView bookmark_Button;
    private TextView copy_TextView;
    private TextView favourite_TextView;
    private TextView boost_TextView;
    //BookMarkDB
    private TootBookmark_SQLite tootBookmark_sqLite;
    private SQLiteDatabase db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.toot_option_button_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        account_Button = view.findViewById(R.id.toot_option_account_button);
        bookmark_Button = view.findViewById(R.id.toot_option_bookmark_button);
        copy_TextView = view.findViewById(R.id.toot_option_copy_button);
//        DarkModeSupport darkModeSupport = new DarkModeSupport(getContext());
//        darkModeSupport.setLayoutAllThemeColor((LinearLayout) bookmark_Button.getParent().getParent());
//        darkModeSupport.setTextViewThemeColor(account_Button);
//        darkModeSupport.setTextViewThemeColor(bookmark_Button);
//        darkModeSupport.setTextViewThemeColor(copy_TextView);
        //クリック
        account_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(account_Button.getContext(), UserActivity.class);
                //IDを渡す
                if (CustomMenuTimeLine.isMisskeyMode()) {
                    intent.putExtra("Misskey", true);
                }
                intent.putExtra("Account_ID", getArguments().getString("user_id"));
                account_Button.getContext().startActivity(intent);

            }
        });
        //クリップボード
        copy_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(getArguments().getString("status_text"), "copy"));
                        Toast.makeText(getContext(), getString(R.string.copy) + " : " + getArguments().getString("status_text"), Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                }
            }
        });
        //ブックマーク
        bookmark_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tootBookmark_sqLite == null) {
                    tootBookmark_sqLite = new TootBookmark_SQLite(getContext());
                }
                if (db == null) {
                    db = tootBookmark_sqLite.getWritableDatabase();
                    db.disableWriteAheadLogging();
                }
                //DBに入れる
                String json = getArguments().getString("json");
                String instance = getArguments().getString("instance");
                ContentValues values = new ContentValues();
                values.put("json", json);
                values.put("instance", instance);
                db.insert("tootbookmarkdb", null, values);
                Toast.makeText(getContext(), getString(R.string.add_Bookmark), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

}
