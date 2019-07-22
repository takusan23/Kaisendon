package io.github.takusan23.Kaisendon.PaintPOST

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.activity_paint_post.*
import kotlinx.android.synthetic.main.bottom_fragment_paint_post.*

class PaintPOSTBottomFragment : BottomSheetDialogFragment() {

    /*はじっこを丸くする*/
    override fun getTheme(): Int {
        var theme = R.style.BottomSheetDialogThemeAppTheme
        val darkModeSupport = DarkModeSupport(context!!)
        if (darkModeSupport.nightMode == Configuration.UI_MODE_NIGHT_YES) {
            theme = R.style.BottomSheetDialogThemeDarkTheme
        }
        return theme
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.bottom_fragment_paint_post, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
/*
        //ダークモード対応
        val darkModeSupport = DarkModeSupport(context!!)
        darkModeSupport.setLayoutAllThemeColor(view as LinearLayout)
*/
        val paintPOSTActivity = activity as PaintPOSTActivity
        val paintView = paintPOSTActivity.paint_view
        //押した時
        paint_pen.setOnClickListener { paintView.setPan(paintPOSTActivity.colorInt) }
        paint_eraser.setOnClickListener { paintView.setEraser() }
        paint_undo.setOnClickListener { paintView.undoPaint() }
        paint_delete.setOnClickListener { paintView.clear() }
        paint_color_black.setOnClickListener { paintPOSTActivity.colorInt = Color.BLACK }
        paint_color_blue.setOnClickListener { paintPOSTActivity.colorInt = Color.BLUE }
        paint_color_red.setOnClickListener { paintPOSTActivity.colorInt = Color.RED }
        paint_color_green.setOnClickListener { paintPOSTActivity.colorInt = Color.parseColor("#008000") }
        paint_color_yellow.setOnClickListener { paintPOSTActivity.colorInt = Color.YELLOW }
        paint_size_small.setOnClickListener { paintPOSTActivity.sizeFloat = 10f }
        paint_size_normal.setOnClickListener { paintPOSTActivity.sizeFloat = 25f }
        paint_size_big.setOnClickListener { paintPOSTActivity.sizeFloat = 50f }
    }

}