package io.github.takusan23.Kaisendon.Omake;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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

    public ShinchokuLayout(Context context) {
        this.context = context;
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        setProgressLayout();
    }

    /*返す*/
    public LinearLayout getLayout() {
        return linearLayout;
    }

    /*作る*/
    private void setProgressLayout() {
        linearLayout = new LinearLayout(context);
        LinearLayout progress_inflate = new LinearLayout(context);
        progress_inflate.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        progress_inflate.setPadding(10, 10, 10, 10);
        LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, progress_inflate);

        progressBar = progress_inflate.findViewById(R.id.progressber_infalte);
        prgress_textview = progress_inflate.findViewById(R.id.progressber_textview);
        content_LinearLayout = (LinearLayout) prgress_textview.getParent();

        linearLayout.addView(progress_inflate);

        //起動カウント
        setLunchCountLayout();

    }

    /*進捗設定*/
    public void setStatusProgress(String toot_count) {
        setProgressBerProgress(toot_count, progressBar, prgress_textview);
    }

    /*アプリ起動カウント*/
    private void setLunchCountLayout() {
        if (pref_setting.getBoolean("lunch_bonus_mode", false)) {
            LinearLayout lunch_count_LinearLayout = new LinearLayout(context);
            lunch_count_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            lunch_count_LinearLayout.setPadding(10, 10, 10, 10);

            LayoutInflater.from(context).inflate(R.layout.progressbar_inflate, lunch_count_LinearLayout);
            ProgressBar progressBar = lunch_count_LinearLayout.findViewById(R.id.progressber_infalte);
            TextView progress_textview = lunch_count_LinearLayout.findViewById(R.id.progressber_textview);
            progress_textview.setText(context.getString(R.string.lunch_count) + " : " + pref_setting.getString("lunch_count", "0") + " " + context.getString(R.string.day));

            //2桁以上で動くようにする
            String lunch_count = pref_setting.getString("lunch_count", "0");
            setProgressBerProgress(lunch_count, progressBar, prgress_textview);
            content_LinearLayout.addView(lunch_count_LinearLayout);
        }
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
