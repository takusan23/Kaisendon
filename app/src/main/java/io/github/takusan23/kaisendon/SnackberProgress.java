package io.github.takusan23.kaisendon;

import android.content.Context;
import com.google.android.material.snackbar.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SnackberProgress {
    static Snackbar snackbar;
    static boolean showMode = true;

    /**
     * よく使う、Snackberにくるくるつけるやつ
     */
    public static void showProgressSnackber(View view, Context context, String message) {
        try {
            //有効無効
            if (showMode) {
                snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(R.id.snackbar_text).getParent();
                //SnackBerを複数行対応させる
                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
                snackBer_textView.setMaxLines(2);
                //複数行対応させたおかげでずれたので修正
                ProgressBar progressBar = new ProgressBar(context);
                LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                progressBer_layoutParams.gravity = Gravity.CENTER;
                progressBar.setLayoutParams(progressBer_layoutParams);
                snackBer_viewGrop.addView(progressBar, 0);
                snackbar.show();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    //終わる
    public static void closeProgressSnackber() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    //無効・有効
    public static void setShowMode(boolean mode) {
        showMode = mode;
    }

}
