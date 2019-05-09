package io.github.takusan23.kaisendon.DarkMode;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import io.github.takusan23.kaisendon.R;

public class DarkModeSupport {
    Context context;
    int nightMode;
    Activity activity;

    /*テキストビューの染色だけならここからどうぞ*/
    public DarkModeSupport(Context context) {
        this.context = context;
        setDarkmode();
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
    }

    //染色する
    public void setTextViewThemeColor(TextView textView) {
        setDarkmode();
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                textView.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                textView.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
                break;
        }
    }

    //すいっち
    public void setSwitchThemeColor(Switch sw) {
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                sw.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                sw.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
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

}
