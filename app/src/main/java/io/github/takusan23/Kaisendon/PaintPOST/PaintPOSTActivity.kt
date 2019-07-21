package io.github.takusan23.Kaisendon.PaintPOST

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.Home

import io.github.takusan23.Kaisendon.R
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.*
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_paint_post.*


class PaintPOSTActivity : AppCompatActivity() {

    var sizeFloat = 10f
    var colorInt = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*
        //ダークテーマに切り替える機能
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)
*/
        setContentView(R.layout.activity_paint_post)
        //ハードウェアアクセラレーション？を無効にする
        //これしないと消しゴム機能が使えない
        paint_view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //Fab押した時にパレット出す
        fab.setOnClickListener {
            val paintPOSTBottomFragment = PaintPOSTBottomFragment()
            paintPOSTBottomFragment.show(supportFragmentManager, "paint_bottom_fragment")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.paint_post_menu, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.paint_post_send -> {
                //投稿しますか？
                val dialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.toot_text))
                        .setMessage("投稿してもよろしいですか？")
                        .setPositiveButton(getString(R.string.toot_text)) { dialogInterface, i ->
                            //お絵かきアクティビティへ移動
                            val intent = Intent(this, Home::class.java)
                            startActivity(intent)
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                //https://stackoverflow.com/questions/9467026/changing-position-of-the-dialog-on-screen-android
                val window = dialog.window
                val layoutParams = window?.attributes
                layoutParams?.gravity = Gravity.BOTTOM
                layoutParams?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                window?.attributes = layoutParams
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
