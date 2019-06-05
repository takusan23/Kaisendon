package io.github.takusan23.Kaisendon

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

object SnackberProgress {
    internal var snackbar: Snackbar? = null
    internal var showMode = true

    /**
     * よく使う、Snackberにくるくるつけるやつ
     */
    fun showProgressSnackber(view: View?, context: Context, message: String) {
        try {
            //有効無効
            if (showMode) {
                snackbar = Snackbar.make(view!!, message, Snackbar.LENGTH_INDEFINITE)
                val snackBer_viewGrop = snackbar!!.view.findViewById<View>(R.id.snackbar_text).parent as ViewGroup
                //SnackBerを複数行対応させる
                val snackBer_textView = snackBer_viewGrop.findViewById<View>(R.id.snackbar_text) as TextView
                snackBer_textView.maxLines = 2
                //複数行対応させたおかげでずれたので修正
                val progressBar = ProgressBar(context)
                val progressBer_layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                progressBer_layoutParams.gravity = Gravity.CENTER
                progressBar.layoutParams = progressBer_layoutParams
                snackBer_viewGrop.addView(progressBar, 0)
                snackbar!!.show()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

    }

    //終わる
    fun closeProgressSnackber() {
        if (snackbar != null) {
            snackbar!!.dismiss()
        }
    }

    //無効・有効
    fun setShowMode(mode: Boolean) {
        showMode = mode
    }

}
