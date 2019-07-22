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
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_paint_post.*
import com.google.android.gms.common.util.IOUtils.toByteArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class PaintPOSTActivity : AppCompatActivity() {

    var sizeFloat = 10f
    var colorInt = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ダークテーマに切り替える機能
        val darkModeSupport = DarkModeSupport(this)
        darkModeSupport.setActivityTheme(this)

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

                //確認するとFabが映り込むので消す
                fab.hide()

                val dialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.toot_text))
                        .setMessage("投稿してもよろしいですか？")
                        .setPositiveButton(getString(R.string.toot_text)) { dialogInterface, i ->
                            //HomeへとばすIntent生成
                            val intent = Intent(this, Home::class.java)
                            //Bitmap生成
                            //https://stackoverflow.com/questions/52642055/view-getdrawingcache-is-deprecated-in-android-api-28/52905682
                            //PixelCopy API ？を使う。
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val bitmap = Bitmap.createBitmap(paint_view.width, paint_view.height, Bitmap.Config.ARGB_8888)
                                val locationOfViewInWindow = IntArray(2)
                                paint_view.getLocationInWindow(locationOfViewInWindow)
                                try {
                                    PixelCopy.request(window, Rect(locationOfViewInWindow[0], locationOfViewInWindow[1], locationOfViewInWindow[0] + paint_view.width, locationOfViewInWindow[1] + paint_view.height), bitmap, { copyResult ->
                                        if (copyResult == PixelCopy.SUCCESS) {
                                            //nullチェック
                                            val uri = bitmapToUri(bitmap)
                                            if (uri != null) {
                                                intent.putExtra("paint_data", true)
                                                intent.putExtra("paint_uri", uri.toString())
                                                //画面推移
                                                startActivity(intent)
                                            } else {
                                                showToast("BitmapからUriへ変更できませんでした")
                                            }
                                        } else {
                                            showToast("Bitmapへ変換できませんでした")
                                        }
                                        // possible to handle other result codes ...
                                    }, Handler())
                                } catch (e: IllegalArgumentException) {
                                    // PixelCopy may throw IllegalArgumentException, make sure to handle it
                                    e.printStackTrace()
                                }
                            } else {
                                //今までの方法
                                //PixelColor API がOreo以降じゃないと利用できないため
                                paint_view.setDrawingCacheEnabled(true);
                                paint_view.buildDrawingCache(true)
                                val bitmap = paint_view.getDrawingCache(true).copy(Bitmap.Config.RGB_565, false)
                                //nullチェック
                                val uri = bitmapToUri(bitmap)
                                if (uri != null) {
                                    intent.putExtra("paint_data", true)
                                    intent.putExtra("paint_uri", uri.toString())
                                    //画面推移
                                    startActivity(intent)
                                } else {
                                    showToast("BitmapからUriへ変更できませんでした")
                                }
                            }
                        }
                        .setNegativeButton(getString(R.string.cancel)) { dialogInterface, i ->
                            //キャンセルはFab出す
                            fab.show()
                        }
                        .setOnCancelListener {
                            //キャンセルはFab出す
                            fab.show()
                        }
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

    /*
    * Bitmap -> Uri
    * 変換するために、Bitmapをキャッシュディレクトリに入れる。
    * https://qiita.com/isao_e/items/08617403ada12de12d7c
    * */
    fun bitmapToUri(bitmap: Bitmap): Uri? {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH) + 1
        val date = calender.get(Calendar.DATE)
        val hour = calender.get(Calendar.HOUR_OF_DAY)   //24時間表記
        val minute = calender.get(Calendar.MINUTE)
        val second = calender.get(Calendar.SECOND)
        //キャッシュディレクトリ
        val cacheDirectory = cacheDir
        //ばらまかれると嫌なので別にディレクトリ生成
        val kaisendonCacheDirectory = File(cacheDirectory, "Kaisendon-PaintCache")
        //フォルダ生成
        if (!kaisendonCacheDirectory.exists()) {
            kaisendonCacheDirectory.mkdir()
        }
        //ファイル名
        val fileName = "PaintFile-${month}-${date}-${hour}-${minute}.jpg"
        //からのファイル
        val file = File(kaisendonCacheDirectory, fileName)
        file.createNewFile()
        if (file.exists()) {
            //バイトデータ書き込み開始
            val fileOutputStream = FileOutputStream(file)
            //Bitmap書き込む
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            //書き込み終了
            fileOutputStream.close()
            //Uri取得
            //File -> Uri
            val paintUri = Uri.fromFile(file)
            return paintUri
        } else {
            showToast("キャッシュディレクトリへ保存できませんでした")
        }
        return null
    }

    fun showToast(message: String) {
        Toast.makeText(this@PaintPOSTActivity, message, Toast.LENGTH_SHORT).show()
    }

}
