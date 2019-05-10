package io.github.takusan23.kaisendon.DarkMode;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import io.github.takusan23.kaisendon.R;

public class DarkModeSupport {
    Context context;
    int nightMode;
    Activity activity;
    SharedPreferences pref_setting;

    /*テキストビューの染色だけならここからどうぞ*/
    public DarkModeSupport(Context context) {
        this.context = context;
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        setDarkmode();
    }

    /*背景ダークモード*/
    public void setBackgroundDarkMode(LinearLayout linearLayout) {
        setDarkmode();
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                linearLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white, context.getTheme()));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                linearLayout.setBackgroundColor(context.getResources().getColor(android.R.color.black, context.getTheme()));
                break;
        }
    }


    /*テーマを適用する*/
    public void setActivityTheme(Activity activity) {
        this.activity = activity;
        setDarkmode();
        //テーマ切り替え
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                activity.setTheme(R.style.AppTheme);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                activity.setTheme(R.style.OLED_ActionBer);
                break;
        }
    }

    //ダークモード設定
    private void setDarkmode() {
        //ダークモード処理
        Configuration conf = context.getResources().getConfiguration();
        nightMode = conf.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        //Android Q以前の場合はメニューから切り替えを行う
        if (Build.VERSION.SDK_INT <= 28 && !Build.VERSION.CODENAME.equals("Q")) {
            if (pref_setting.getBoolean("darkmode", false)) {
                nightMode = Configuration.UI_MODE_NIGHT_YES;
            } else {
                nightMode = Configuration.UI_MODE_NIGHT_NO;
            }
        }
    }

    //染色する
    public void setTextViewThemeColor(TextView textView) {
        setDarkmode();
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                textView.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                textView.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.white, context.getTheme()));
                break;
        }
    }

    //ダークモードかどうか判断（手動）
    public int setIsDarkModeSelf(int darkmode) {
        //Android Pie以前端末用
        if (Build.VERSION.SDK_INT <= 28 && !Build.VERSION.CODENAME.equals("Q")) {
            if (pref_setting.getBoolean("darkmode", false)) {
                darkmode = Configuration.UI_MODE_NIGHT_YES;
            } else {
                darkmode = Configuration.UI_MODE_NIGHT_NO;
            }
        }
        return darkmode;
    }

    //すいっち
    public void setSwitchThemeColor(Switch sw) {
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                sw.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                sw.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.white, context.getTheme()));
                break;
        }
    }

    //Linearlayoutから子View全取得してTextViewなら染色を行う
    public void setLayoutAllThemeColor(LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            System.out.println(layout.getChildCount());
            if (layout.getChildAt(i) instanceof TextView) {
                setTextViewThemeColor((TextView) layout.getChildAt(i));
            }
        }
    }

    /*Drawableに染色する*/
    private Drawable setDrawableColor(Drawable drawable) {
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                drawable.setTint(Color.parseColor("#ffffff"));
                ;
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                drawable.setTint(Color.parseColor("#000000"));
                ;
                break;
        }
        return drawable;
    }

}
