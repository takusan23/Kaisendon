package io.github.takusan23.Kaisendon.PaintPOST

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.Theme.DarkModeSupport
import io.github.takusan23.Kaisendon.R
import kotlinx.android.synthetic.main.bottom_fragment_paint_post.*

class PaintPOSTBottomFragment : BottomSheetDialogFragment() {

    lateinit var darkModeSupport: DarkModeSupport
    lateinit var paintView: PaintView
    lateinit var paintPOSTActivity: PaintPOSTActivity

    /*はじっこを丸くする*/
    override fun getTheme(): Int {
        var theme = R.style.BottomSheetDialogThemeAppTheme
        darkModeSupport = DarkModeSupport(context!!)
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
        paintPOSTActivity = activity as PaintPOSTActivity
        paintView = paintPOSTActivity.getPaintView()
        //押した時
        paint_pen.setOnClickListener {
            setImageViewColor(paint_pen)
            removeImageViewColor(paint_eraser)
            paintView.setPan(paintPOSTActivity.colorInt)
        }
        paint_eraser.setOnClickListener {
            removeImageViewColor(paint_pen)
            setImageViewColor(paint_eraser)
            paintView.setEraser()
        }
        paint_undo.setOnClickListener { paintView.undoPaint() }
        paint_delete.setOnClickListener {
            showAllClearDialog()
        }
        paint_color_black.setOnClickListener {
            paintPOSTActivity.colorInt = Color.BLACK
            setPenMode()
        }
        paint_color_blue.setOnClickListener {
            paintPOSTActivity.colorInt = Color.BLUE
            setPenMode()
        }
        paint_color_red.setOnClickListener {
            paintPOSTActivity.colorInt = Color.RED
            setPenMode()
        }
        paint_color_green.setOnClickListener {
            paintPOSTActivity.colorInt = Color.parseColor("#008000")
            setPenMode()
        }
        paint_color_yellow.setOnClickListener {
            paintPOSTActivity.colorInt = Color.YELLOW
            setPenMode()
        }
        paint_size_small.setOnClickListener { paintPOSTActivity.sizeFloat = 10f }
        paint_size_normal.setOnClickListener { paintPOSTActivity.sizeFloat = 25f }
        paint_size_big.setOnClickListener { paintPOSTActivity.sizeFloat = 50f }

        /*
        * 現在のモードを取得する
        * 現在のモードがわかるようにする
        * */
        if (paintView.isPenMode()) {
            setImageViewColor(paint_pen)
        } else {
            setImageViewColor(paint_eraser)
        }
    }

    fun setImageViewColor(imageView: ImageView) {
        val drawable = imageView.drawable
        drawable.setTintList(ColorStateList.valueOf(context?.getColor(R.color.colorAccent)
                ?: Color.BLUE))
        imageView.setImageDrawable(drawable)
    }

    fun removeImageViewColor(imageView: ImageView) {
        val drawable = imageView.drawable
        //ダークモード対策
        imageView.setImageDrawable(darkModeSupport.setDrawableColor(drawable))
    }

    /*
    * ペンモードに切り替える
    * */
    fun setPenMode() {
        setImageViewColor(paint_pen)
        removeImageViewColor(paint_eraser)
        paintView.setPan(paintPOSTActivity.colorInt)
    }

    fun showAllClearDialog() {
        val allClearDialog = AlertDialog.Builder(paintView.context)
                .setTitle(getString(R.string.paint_post_all_clear_dialog_title))
                .setMessage(getString(R.string.paint_post_all_clear_dialog_message))
                .setPositiveButton(getString(R.string.delete_ok)) { dialogInterface, i ->
                    paintView.clear()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        //https://stackoverflow.com/questions/9467026/changing-position-of-the-dialog-on-screen-android
        val window = allClearDialog.window
        val layoutParams = window?.attributes
        layoutParams?.gravity = Gravity.BOTTOM
        layoutParams?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window?.attributes = layoutParams
    }


}