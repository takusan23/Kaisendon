package io.github.takusan23.Kaisendon.Omake;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import java.util.Calendar;

import io.github.takusan23.Kaisendon.R;

public class ShinchokuLayout {
    /*うらめにゅー？*/
    private Context context;
    private ProgressBar progressBar;
    private TextView prgress_textview;
    private LinearLayout linearLayout;
    private SharedPreferences pref_setting;
    private LinearLayout content_LinearLayout;
    private ViewGroup.LayoutParams layoutParams;
    private Calendar calendar;
    private ProgressBar one_day_ProgressBar;
    private TextView one_day_TextView;

    public ShinchokuLayout(Context context) {
        this.context = context;
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        calendar = Calendar.getInstance();
        setLayout();
    }

    public void setLayout() {
        if (pref_setting.getBoolean("life_mode", false)) {
            setProgressLayout();
            //目標
            setOneDayTootLayout();
            //起動カウント
            setLunchCountLayout();
        }
    }

    /*返す*/
    public LinearLayout getLayout() {
        return linearLayout;
    }

    /*作る*/
    private void setProgressLayout() {
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout progress_inflate = new LinearLayout(context);
        progress_inflate.setLayoutParams(layoutParams);
        progress_inflate.setPadding(10, 10, 10, 10);
        LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, progress_inflate);

        progressBar = progress_inflate.findViewById(R.id.progressber_infalte);
        prgress_textview = progress_inflate.findViewById(R.id.progressber_textview);
        content_LinearLayout = (LinearLayout) prgress_textview.getParent();

        linearLayout.removeView(linearLayout.getChildAt(0));
        linearLayout.addView(progress_inflate, 0);
    }

    /*進捗設定*/
    public void setStatusProgress(String toot_count) {
        if (pref_setting.getBoolean("life_mode", false)) {
            setProgressBerProgress(toot_count, progressBar, prgress_textview);
        }
    }

    /*アプリ起動カウント*/
    private void setLunchCountLayout() {
        LinearLayout lunch_count_LinearLayout = new LinearLayout(context);
        lunch_count_LinearLayout.setLayoutParams(layoutParams);
        lunch_count_LinearLayout.setPadding(10, 10, 10, 10);

        LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, lunch_count_LinearLayout);
        ProgressBar progressBar = lunch_count_LinearLayout.findViewById(R.id.progressber_infalte);
        TextView progress_textview = lunch_count_LinearLayout.findViewById(R.id.progressber_textview);
        progress_textview.setText(context.getString(R.string.lunch_count) + " : " + pref_setting.getString("lunch_count", "0") + " " + context.getString(R.string.day));

        //2桁以上で動くようにする
        String lunch_count = pref_setting.getString("lunch_count", "0");
        setProgressBerProgress(lunch_count, progressBar, prgress_textview);
        linearLayout.removeView(linearLayout.getChildAt(2));
        linearLayout.addView(lunch_count_LinearLayout, 2);
    }

    /*今日のトゥート目標レイアウト*/
    private void setOneDayTootLayout() {
        int challenge = Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0"));
        int count = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0"));
        int challenge_day = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_day", "0"));
        if (challenge > 0) {
            //レイアウト
            LinearLayout one_day_LinearLayout = new LinearLayout(context);
            one_day_LinearLayout.setLayoutParams(layoutParams);
            one_day_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            one_day_LinearLayout.setPadding(10, 10, 10, 10);
            LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, one_day_LinearLayout);
            ProgressBar progressBar = one_day_LinearLayout.findViewById(R.id.progressber_infalte);
            TextView progress_textview = one_day_LinearLayout.findViewById(R.id.progressber_textview);
            //次の日ならリセットする
            if (calendar.get(Calendar.DATE) != challenge_day) {
                SharedPreferences.Editor editor = pref_setting.edit();
                editor.putString("one_day_toot_challenge_count", "1");
                editor.putString("one_day_toot_challenge_day", String.valueOf(calendar.get(Calendar.DATE)));
                editor.apply();
            }
            progress_textview.setText("目標トゥート数 : " + pref_setting.getString("one_day_toot_challenge_count", "0") + " / " + pref_setting.getString("one_day_toot_challenge", "0"));
            //2桁以上で動くようにする
            progressBar.setMax(Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0")));
            progressBar.setProgress(Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0")));
            one_day_ProgressBar = progressBar;
            one_day_TextView = progress_textview;
            //目標に達したときの処理
            if (count >= challenge) {
                progress_textview.setText("おめでとう。今日のトゥート数目標達成だよ！");
            }
            //既にあったら消す
            linearLayout.removeView(linearLayout.getChildAt(1));
            linearLayout.addView(one_day_LinearLayout, 1);
        }
    }


    /*プログレスバーで進捗！*/
    private void setProgressBerProgress(String text, ProgressBar progressBar, TextView prgress_textview) {
        if (pref_setting.getBoolean("life_mode", false)) {
            //2桁以上で動くようにする
            if (text.length() >= 2) {
                //先頭文字
                String nextStep = text.substring(1);
                //桁を取得
                int digit = text.length() - 1;
                int max_value = Integer.parseInt("1" + String.format("%0" + digit + "d", 0));
                int next_value = Integer.parseInt(String.format("%0" + digit + "d", 0));
                //次のステップを取得する
                //先頭の文字＋１と0を桁の数だけ用意する
                String nextStage = (Integer.valueOf(text.substring(0, 1)) + 1) + String.format("%0" + digit + "d", 0);
                prgress_textview.setText(context.getString(R.string.toot) + " : " + text + " / " + nextStage);
                progressBar.setMax(max_value);
                progressBar.setProgress(Integer.parseInt(nextStep));
            } else {
                ((LinearLayout) progressBar.getParent()).removeView(progressBar);
            }
        }
    }

    /*トゥート目標*/
    public void setTootChallenge() {
        int challenge = Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0"));
        int count = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0"));
        int challenge_day = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_day", "0"));
        if (challenge > 0) {
            //比較
            SharedPreferences.Editor editor = pref_setting.edit();
            if (calendar.get(Calendar.DATE) == challenge_day) {
                //一足す
                count += 1;
                //更新
                editor.putString("one_day_toot_challenge_count", String.valueOf(count));
            } else {
                //次の日なので0から始める
                editor.putString("one_day_toot_challenge_count", "1");
                editor.putString("one_day_toot_challenge_day", String.valueOf(calendar.get(Calendar.DATE)));
            }
            //さいせいせい
            editor.apply();
            setOneDayTootLayout();
        }
    }

    /*値変更*/
    public void setOnDayProgress() {
        int challenge = Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0"));
        int count = Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0"));
        if (one_day_ProgressBar != null && one_day_TextView != null) {
            //2桁以上で動くようにする
            one_day_TextView.setText("目標トゥート数 : " + pref_setting.getString("one_day_toot_challenge_count", "0") + " / " + pref_setting.getString("one_day_toot_challenge", "0"));
            one_day_ProgressBar.setMax(Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0")));
            one_day_ProgressBar.setProgress(Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0")));
            //目標に達したときの処理
            if (count >= challenge) {
                one_day_TextView.setText("おめでとう。今日のトゥート数目標達成だよ！");
            }
        }
    }

}
