package io.github.takusan23.Kaisendon.Omake

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.*

class LunchBonus(private val context: Context) {
    private val pref_setting: SharedPreferences

    init {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        //記録
        if (pref_setting.getBoolean("life_mode", false)) {
            addSave()
        }
    }

    private fun addSave() {
        val calendar = Calendar.getInstance()
        val editor = pref_setting.edit()
        //記録があるとき
        if (pref_setting.getString("lunch_day", "")!!.contains("")) {
            //前回の記録から一日経過したらカウント
            var count = Integer.valueOf(pref_setting.getString("lunch_count", "0")!!)
            val yesterday = Integer.valueOf(pref_setting.getString("lunch_day", "0")!!)
            //これ月変わったら１から始まるから雑分岐
            if (calendar.get(Calendar.DATE) != yesterday) {
                //更新
                count++
                editor.putString("lunch_count", count.toString())
                editor.apply()
            }
        } else {
            //初回
            editor.putString("lunch_count", "0")
        }
        //起動日時を保存する
        editor.putString("lunch_day", calendar.get(Calendar.DATE).toString())
        editor.apply()
    }
}
