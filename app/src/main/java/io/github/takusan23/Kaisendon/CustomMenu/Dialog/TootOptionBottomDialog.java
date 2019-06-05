package io.github.takusan23.Kaisendon.CustomMenu.Dialog;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.takusan23.Kaisendon.Activity.UserActivity;
import io.github.takusan23.Kaisendon.CustomMenu.AddCustomMenuActivity;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuLoadSupport;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSQLiteHelper;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.Kaisendon.R;
import io.github.takusan23.Kaisendon.TootBookmark_SQLite;

import static io.github.takusan23.Kaisendon.Preference_ApplicationContext.getContext;

public class TootOptionBottomDialog extends BottomSheetDialogFragment {

    private SharedPreferences pref_setting;
    private View view;
    private LinearLayout toot_option_LinearLayout;
    private TextView account_Button;
    private TextView bookmark_Button;
    private TextView copy_TextView;
    private TextView copy_toot_id_TextView;
    private TextView browser_TextView;
    private TextView favourite_TextView;
    private TextView boost_TextView;
    private int padding = 35;

    //BookMarkDB
    private TootBookmark_SQLite tootBookmark_sqLite;
    private SQLiteDatabase db;
    private CustomMenuSQLiteHelper customMenuSQLiteHelper;
    private SQLiteDatabase custom_SqLiteDatabase;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.toot_option_button_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        DarkModeSupport darkModeSupport = new DarkModeSupport(getContext());
        darkModeSupport.setLayoutAllThemeColor((LinearLayout) view);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        account_Button = view.findViewById(R.id.toot_option_account_button);
        bookmark_Button = view.findViewById(R.id.toot_option_bookmark_button);
        copy_TextView = view.findViewById(R.id.toot_option_copy_button);
        copy_toot_id_TextView = view.findViewById(R.id.toot_option_toot_id_copy);
        browser_TextView = view.findViewById(R.id.toot_option_browser);
        toot_option_LinearLayout = view.findViewById(R.id.toot_option_linearlayout);

        //SQLite
        if (customMenuSQLiteHelper == null) {
            customMenuSQLiteHelper = new CustomMenuSQLiteHelper(getContext());
        }
        if (custom_SqLiteDatabase == null) {
            custom_SqLiteDatabase = customMenuSQLiteHelper.getWritableDatabase();
            custom_SqLiteDatabase.disableWriteAheadLogging();
        }
        //ハッシュタグ
        setHashtagButton();

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
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("とぅーとこぴー", getArguments().getString("status_text")));
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
        //トゥートID
        copy_toot_id_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(getArguments().getString("status_id"), "copy"));
                        Toast.makeText(getContext(), getString(R.string.copy) + " : " + getArguments().getString("status_id"), Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                }
            }
        });
        //ブラウザで開く
        browser_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://" + getArguments().getString("instance") + "/@" + getArguments().getString("user_name") + "/" + getArguments().getString("status_id");
                if (pref_setting.getBoolean("pref_chrome_custom_tabs", true)) {
                    Bitmap back_icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(getContext());
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(getContext(), Uri.parse(url));
                } else {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
    }

    //ハッシュタグ
    private void setHashtagButton() {
        SpannableString spannableString = new SpannableString(getArguments().getString("status_text"));
        //正規表現
        //パクった→https://qiita.com/corin8823/items/75309761833d823cac6f
        Matcher hashtag_Matcher = Pattern.compile("#([Ａ-Ｚａ-ｚA-Za-z一-鿆0-9０-９ぁ-ヶｦ-ﾟー]+)").matcher(spannableString);
        Matcher nicoID_Matcher = Pattern.compile("sm([0-9]+)").matcher(spannableString);
        while (hashtag_Matcher.find()) {
            TextView textView = new TextView(getContext());
            textView.setPadding(padding, padding, padding, padding);
            textView.setText(hashtag_Matcher.group());
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_label_outline_black_24dp, getActivity().getTheme()), null, null, null);
            toot_option_LinearLayout.addView(textView);
            //押したらメニュー追加
            setHashtagMenu(hashtag_Matcher.group(), textView);
        }
        String temp_id = null;
        while (nicoID_Matcher.find()) {
            //動画リンクと#smがあると2つ生成されるので回避する
            if (temp_id == null) {
                temp_id = nicoID_Matcher.group();
                setニコニコ動画で開く(nicoID_Matcher.group());
            } else {
                if (!temp_id.contains(nicoID_Matcher.group())) {
                    //かぶってなかったら生成
                    setニコニコ動画で開く(nicoID_Matcher.group());
                } else {
                    //同じだったらnullにしとく
                    temp_id = null;
                }
            }
        }
    }

    //ニコニコ動画で開く
    private void setニコニコ動画で開く(String title) {
        String id = title;
        //かぶってなかったら生成
        TextView textView = new TextView(getContext());
        //日本語と英語でわける。英語わけわかめ
        if (getString(R.string.open_nicovideo).contains("をニコニコ動画で開く")) {
            title = title + getString(R.string.open_nicovideo);
        } else {
            title = "Open" + title + getString(R.string.open_nicovideo);
        }
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(title);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_open_in_new_black_24dp, getActivity().getTheme()), null, null, null);
        toot_option_LinearLayout.addView(textView);
        //押したらリンク
        setニコニコ動画ID(id, textView);
    }

    //メニュー作成
    @SuppressLint("RestrictedApi")
    private void setHashtagMenu(String name, View view) {
        MenuBuilder menuBuilder = new MenuBuilder(getContext());
        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(R.menu.add_hashtag_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(getContext(), menuBuilder, view);
        optionsMenu.setForceShowIcon(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //表示
                optionsMenu.show();
                //押したときの反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        String id = name.replace("#", "");
                        switch (item.getItemId()) {
                            case R.id.add_hashtag_tl_local:
                                insertDB(id, "/api/v1/timelines/tag/?local=true");
                                break;
                            case R.id.add_hashtag_tl_public:
                                insertDB(id, "/api/v1/timelines/tag/");
                                break;
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {

                    }
                });
            }
        });
    }

    //動画ID？
    private void setニコニコ動画ID(String id, View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String base_url = "https://www.nicovideo.jp/watch/";
                base_url += id;
                useCustomTabs(base_url);
            }
        });
    }

    //データベース追加
    private void insertDB(String title, String contentUrl) {
        if (getArguments().getString("cmtl_name") != null) {
            String cmtl_name = getArguments().getString("cmtl_name");
            Cursor cursor = custom_SqLiteDatabase.query(
                    "custom_menudb",
                    new String[]{"setting"},
                    "name=?",
                    new String[]{cmtl_name},
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(cursor.getString(0));
                    jsonObject.put("name", title);
                    jsonObject.put("content", contentUrl);
                    ContentValues values = new ContentValues();
                    values.put("name", title);
                    values.put("setting", jsonObject.toString());
                    custom_SqLiteDatabase.insert("custom_menudb", null, values);
                    reLoadMenu();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    /**
     * CustomTab
     */
    private void useCustomTabs(String url) {
        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
        //カスタムタグ有効
        if (chrome_custom_tabs) {
            Bitmap back_icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_arrow_back);
            String custom = CustomTabsHelper.getPackageNameToUse(getContext());
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(custom);
            customTabsIntent.launchUrl(getContext(), Uri.parse(url));
            //無効
        } else {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            getContext().startActivity(intent);
        }
    }

    /*めにゅー再生成*/
    private void reLoadMenu() {
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        CustomMenuLoadSupport customMenuLoadSupport = new CustomMenuLoadSupport(getContext(), navigationView);
        //再読み込み
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.custom_menu);
        customMenuLoadSupport.loadCustomMenu(null);
    }
}
