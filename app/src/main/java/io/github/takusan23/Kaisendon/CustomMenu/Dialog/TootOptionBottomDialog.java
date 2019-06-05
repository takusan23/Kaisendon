package io.github.takusan23.Kaisendon.CustomMenu.Dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.takusan23.Kaisendon.Activity.UserActivity;
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.Kaisendon.R;
import io.github.takusan23.Kaisendon.TootBookmark_SQLite;

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
        DarkModeSupport darkModeSupport = new DarkModeSupport(getContext());
        darkModeSupport.setLayoutAllThemeColor((LinearLayout) view);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        account_Button = view.findViewById(R.id.toot_option_account_button);
        bookmark_Button = view.findViewById(R.id.toot_option_bookmark_button);
        copy_TextView = view.findViewById(R.id.toot_option_copy_button);
        copy_toot_id_TextView = view.findViewById(R.id.toot_option_toot_id_copy);
        browser_TextView = view.findViewById(R.id.toot_option_browser);
        toot_option_LinearLayout = view.findViewById(R.id.toot_option_linearlayout);

        //ハッシュタグ
        setHashtagButton();

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
    private void setHashtagButton(){
        LinearLayout linearLayout = (LinearLayout)account_Button.getParent();
        SpannableString spannableString = new SpannableString(getArguments().getString("status_text"));
        //正規表現
        Matcher hashtag_Matcher = Pattern.compile("#([A-Za-z0-9あ-んァ-ヶ亜-煕_-]+)").matcher(spannableString);
        Matcher nicoID_Matcher = Pattern.compile("sm([0-9]+)").matcher(spannableString);
        while (hashtag_Matcher.find()) {
            TextView textView = new TextView(getContext());
            textView.setText(hashtag_Matcher.group());
            textView.setPadding(account_Button.getLeft(),account_Button.getTop(),account_Button.getRight(),account_Button.getBottom());
            textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_label_outline_black_24dp,getActivity().getTheme()),null,null,null);
            toot_option_LinearLayout.addView(textView);
        }
    }

}
