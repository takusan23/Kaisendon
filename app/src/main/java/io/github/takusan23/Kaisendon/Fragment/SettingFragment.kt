package io.github.takusan23.Kaisendon.Fragment

import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.preference.Preference

import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

import io.github.takusan23.Kaisendon.R
import java.io.File
import kotlin.math.round

class SettingFragment : PreferenceFragmentCompat() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = getString(R.string.setting)

        cacheClearDialog()
    }


    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.preference)
    }

    fun cacheClearDialog() {
        //キャッシュディレクトリ
        val cacheDirectory = context!!.cacheDir
        //ばらまかれると嫌なので別にディレクトリ生成
        val kaisendonCacheDirectory = File(cacheDirectory, "Kaisendon-PaintCache")
        //設定項目取り出し
        val cache_clear = findPreference("pref_paint_cache_clear")
        //チェック
        if (kaisendonCacheDirectory.exists()){
            //なんとなく枚数と容量だす。
            cache_clear.summary = cache_clear.summary.toString() + "\n枚数 : ${kaisendonCacheDirectory.listFiles().size} / 容量 : ${byteToMB(kaisendonCacheDirectory.length())} MB"
            //押した時
            cache_clear.setOnPreferenceClickListener {
                context?.let {
                    //ダイアログ出す
                    val dialog = AlertDialog.Builder(context!!)
                            .setTitle("キャッシュ削除")
                            .setMessage("お絵かき投稿の画像データを削除します")
                            .setPositiveButton(getString(R.string.delete_ok), DialogInterface.OnClickListener { dialogInterface, i ->
                                //データ削除
                                if (context != null) {
                                    //存在チェック
                                    if (kaisendonCacheDirectory.exists()) {
                                        //エラーチェック
                                        var errorCount = 0
                                        //中身消す
                                        for (fileItem: File in kaisendonCacheDirectory.listFiles()) {
                                            if (!fileItem.delete()) {
                                                errorCount++
                                            }
                                        }
                                        //しっかり削除できたか
                                        if (errorCount == 0) {
                                            //成功
                                            Toast.makeText(context, getString(R.string.delete), Toast.LENGTH_SHORT).show()
                                        } else {
                                            //えらー
                                            Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show()
                    //下に置くコード
                    val window = dialog.window
                    val layoutParams = window?.attributes
                    layoutParams?.gravity = Gravity.BOTTOM
                    layoutParams?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    window?.attributes = layoutParams
                }
                true
            }
        }
    }

    fun byteToMB(length: Long): Double {
        return round(length / 1024.0 / 1024.0)
    }

}
