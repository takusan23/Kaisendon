package io.github.takusan23.Kaisendon.Omake;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Calendar;

public class LunchBonus {
    private Context context;
    private SharedPreferences pref_setting;

    public LunchBonus(Context context) {
        this.context = context;
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        //記録
        if (pref_setting.getBoolean("life_mode", false)) {
            addSave();
        }
    }

    private void addSave() {
        Calendar calendar = Calendar.getInstance();
        SharedPreferences.Editor editor = pref_setting.edit();
        //記録があるとき
        if (pref_setting.getString("lunch_day", "").contains("")) {
            //前回の記録から一日経過したらカウント
            int count = Integer.valueOf(pref_setting.getString("lunch_count", "0"));
            int yesterday = Integer.valueOf(pref_setting.getString("lunch_day", "0"));
            if (calendar.get(Calendar.DATE) > yesterday) {
                //更新
                count++;
                editor.putString("lunch_count", String.valueOf(count));
                editor.apply();
            }
        } else {
            //初回
            editor.putString("lunch_count", "0");
        }
        //起動日時を保存する
        editor.putString("lunch_day", String.valueOf(calendar.get(Calendar.DATE)));
        editor.apply();
    }

}
