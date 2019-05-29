package io.github.takusan23.Kaisendon.Omake;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import io.github.takusan23.Kaisendon.R;

public class ShinchokuLayout {
    /*うらめにゅー？*/
    Context context;
    ProgressBar progressBar;
    TextView prgress_textview;
    LinearLayout linearLayout;
    SharedPreferences pref_setting;
    LinearLayout content_LinearLayout;
    ViewGroup.LayoutParams layoutParams;

    public ShinchokuLayout(Context context) {
        this.context = context;
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        setProgressLayout();
        //起動カウント
        //目標
        setOneDayTootLayout();
        setLunchCountLayout();

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

        linearLayout.addView(progress_inflate);
    }

    /*進捗設定*/
    public void setStatusProgress(String toot_count) {
        setProgressBerProgress(toot_count, progressBar, prgress_textview);
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
        linearLayout.addView(lunch_count_LinearLayout);
    }

    /*今日のトゥート目標レイアウト*/
    private void setOneDayTootLayout() {
        LinearLayout one_day_LinearLayout = new LinearLayout(context);
        one_day_LinearLayout.setLayoutParams(layoutParams);
        one_day_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        one_day_LinearLayout.setPadding(10, 10, 10, 10);

        LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, one_day_LinearLayout);
        ProgressBar progressBar = one_day_LinearLayout.findViewById(R.id.progressber_infalte);
        TextView progress_textview = one_day_LinearLayout.findViewById(R.id.progressber_textview);
        progress_textview.setText("目標トゥート数 : " + pref_setting.getString("one_day_toot_challenge", "0"));
        //2桁以上で動くようにする
        progressBar.setMax(Integer.valueOf(pref_setting.getString("one_day_toot_challenge", "0")));
        progressBar.setProgress(Integer.valueOf(pref_setting.getString("one_day_toot_challenge_count", "0")));

        linearLayout.addView(one_day_LinearLayout);

    }


    /*プログレスバーで進捗！*/
    private void setProgressBerProgress(String text, ProgressBar progressBar, TextView prgress_textview) {
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
