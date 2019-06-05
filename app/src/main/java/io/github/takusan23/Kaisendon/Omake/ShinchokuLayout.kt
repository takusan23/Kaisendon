package io.github.takusan23.Kaisendon.Omake

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.preference.PreferenceManager
import io.github.takusan23.Kaisendon.R
import java.util.*

class ShinchokuLayout(/*うらめにゅー？*/
        private val context: Context) {
    private var progressBar: ProgressBar? = null
    private var prgress_textview: TextView? = null
    /*返す*/
    var layout: LinearLayout? = null
        private set
    private val pref_setting: SharedPreferences
    private var content_LinearLayout: LinearLayout? = null
    private val layoutParams: ViewGroup.LayoutParams
    private val calendar: Calendar
    private var one_day_ProgressBar: ProgressBar? = null
    private var one_day_TextView: TextView? = null

    init {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        calendar = Calendar.getInstance()
        setLayout()
    }

    fun setLayout() {
        if (pref_setting.getBoolean("life_mode", false)) {
            setProgressLayout()
            //目標
            setOneDayTootLayout()
            //起動カウント
            setLunchCountLayout()
        }
    }

    /*作る*/
    private fun setProgressLayout() {
        layout = LinearLayout(context)
        layout!!.orientation = LinearLayout.VERTICAL
        val progress_inflate = LinearLayout(context)
        progress_inflate.layoutParams = layoutParams
        progress_inflate.setPadding(10, 10, 10, 10)
        LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, progress_inflate)

        progressBar = progress_inflate.findViewById(R.id.progressber_infalte)
        prgress_textview = progress_inflate.findViewById(R.id.progressber_textview)
        content_LinearLayout = prgress_textview!!.parent as LinearLayout

        layout!!.removeView(layout!!.getChildAt(0))
        layout!!.addView(progress_inflate, 0)
    }

    /*進捗設定*/
    fun setStatusProgress(toot_count: String) {
        if (pref_setting.getBoolean("life_mode", false)) {
            setProgressBerProgress(toot_count, progressBar, prgress_textview)
        }
    }

    /*アプリ起動カウント*/
    private fun setLunchCountLayout() {
        val lunch_count_LinearLayout = LinearLayout(context)
        lunch_count_LinearLayout.layoutParams = layoutParams
        lunch_count_LinearLayout.setPadding(10, 10, 10, 10)

        LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, lunch_count_LinearLayout)
        val progressBar = lunch_count_LinearLayout.findViewById<ProgressBar>(R.id.progressber_infalte)
        val progress_textview = lunch_count_LinearLayout.findViewById<TextView>(R.id.progressber_textview)
        progress_textview.text = context.getString(R.string.lunch_count) + " : " + pref_setting.getString("lunch_count", "0") + " " + context.getString(R.string.day)

        //2桁以上で動くようにする
        val lunch_count = pref_setting.getString("lunch_count", "0")
        setProgressBerProgress(lunch_count!!, progressBar, prgress_textview)
        layout!!.removeView(layout!!.getChildAt(2))
        layout!!.addView(lunch_count_LinearLayout, 2)
    }

    /*今日のトゥート目標レイアウト*/
    private fun setOneDayTootLayout() {
        val challenge = Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0")!!)
        val count = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0")!!)
        val challenge_day = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_day", "0")!!)
        if (challenge > 0) {
            //レイアウト
            val one_day_LinearLayout = LinearLayout(context)
            one_day_LinearLayout.layoutParams = layoutParams
            one_day_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            one_day_LinearLayout.setPadding(10, 10, 10, 10)
            LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, one_day_LinearLayout)
            val progressBar = one_day_LinearLayout.findViewById<ProgressBar>(R.id.progressber_infalte)
            val progress_textview = one_day_LinearLayout.findViewById<TextView>(R.id.progressber_textview)
            //次の日ならリセットする
            if (calendar.get(Calendar.DATE) != challenge_day) {
                val editor = pref_setting.edit()
                editor.putString("one_day_toot_challenge_count", "0")
                editor.putString("one_day_toot_challenge_day", calendar.get(Calendar.DATE).toString())
                editor.apply()
            }
            progress_textview.text = context.getString(R.string.toot_challenge_count) + " : " + pref_setting.getString("one_day_toot_challenge_count", "0") + " / " + pref_setting.getString("one_day_toot_challenge", "0")
            //2桁以上で動くようにする
            progressBar.max = Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0")!!)
            progressBar.progress = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0")!!)
            one_day_ProgressBar = progressBar
            one_day_TextView = progress_textview
            //目標に達したときの処理
            if (count >= challenge) {
                progress_textview.text = context.getString(R.string.toot_challenge_complete)
            }
            //既にあったら消す
            layout!!.removeView(layout!!.getChildAt(1))
            layout!!.addView(one_day_LinearLayout, 1)
        }
    }


    /*プログレスバーで進捗！*/
    private fun setProgressBerProgress(text: String, progressBar: ProgressBar?, prgress_textview: TextView?) {
        if (pref_setting.getBoolean("life_mode", false) && progressBar != null && prgress_textview != null) {
            //2桁以上で動くようにする
            if (text.length >= 2) {
                //先頭文字
                val nextStep = text.substring(1)
                //桁を取得
                val digit = text.length - 1
                val max_value = Integer.parseInt("1" + String.format("%0" + digit + "d", 0))
                val next_value = Integer.parseInt(String.format("%0" + digit + "d", 0))
                //次のステップを取得する
                //先頭の文字＋１と0を桁の数だけ用意する
                val nextStage = (Integer.valueOf(text.substring(0, 1)) + 1).toString() + String.format("%0" + digit + "d", 0)
                prgress_textview.text = context.getString(R.string.toot) + " : " + text + " / " + nextStage
                progressBar.max = max_value
                progressBar.progress = Integer.parseInt(nextStep)
            } else {
                (progressBar.parent as LinearLayout).removeView(progressBar)
            }
        }
    }

    /*トゥート目標*/
    fun setTootChallenge() {
        val challenge = Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0")!!)
        var count = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0")!!)
        val challenge_day = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_day", "0")!!)
        if (challenge > 0) {
            //比較
            val editor = pref_setting.edit()
            if (calendar.get(Calendar.DATE) == challenge_day) {
                //一足す
                count += 1
                //更新
                editor.putString("one_day_toot_challenge_count", count.toString())
            } else {
                //次の日なので0から始める
                editor.putString("one_day_toot_challenge_count", "1")
                editor.putString("one_day_toot_challenge_day", calendar.get(Calendar.DATE).toString())
            }
            //さいせいせい
            editor.apply()
            setOneDayTootLayout()
        }
    }

    /*値変更*/
    fun setOnDayProgress() {
        val challenge = Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0")!!)
        val count = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0")!!)
        if (one_day_ProgressBar != null && one_day_TextView != null) {
            //2桁以上で動くようにする
            one_day_TextView!!.text = context.getString(R.string.toot_challenge_count) + " : " + pref_setting.getString("one_day_toot_challenge_count", "0") + " / " + pref_setting.getString("one_day_toot_challenge", "0")
            one_day_ProgressBar!!.max = Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0")!!)
            one_day_ProgressBar!!.progress = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0")!!)
            //目標に達したときの処理
            if (count >= challenge) {
                one_day_TextView!!.text = context.getString(R.string.toot_challenge_complete)
            }
        }
    }

}
