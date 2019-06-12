package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport
import io.github.takusan23.Kaisendon.Home
import io.github.takusan23.Kaisendon.R
import java.io.*

class BackupRestoreBottomDialog : BottomSheetDialogFragment() {

    private var backup_Button: Button? = null
    private var restore_Button: Button? = null
    private var path_TextView: TextView? = null
    private var path: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.backup_restore_bottomdialogfragment, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backup_Button = view.findViewById(R.id.backup_restore_backup_Button)
        restore_Button = view.findViewById(R.id.backup_restore_restore_Button)
        path_TextView = view.findViewById(R.id.backup_restore_path_textView)
        val darkModeSupport = DarkModeSupport(context!!)
        darkModeSupport.setLayoutAllThemeColor(view as LinearLayout)
        backup_Button!!.setOnClickListener {
            startBackupDB()
            //終了
            dismiss()
        }
        restore_Button!!.setOnClickListener {
            startRestore()
            //終了
            dismiss()
            //再読み込み
            context!!.startActivity(Intent(context, Home::class.java))
        }
        //ぱす（Android Qから変わった
/*
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            path = Environment.getExternalStorageDirectory().path + "/Kaisendon"
        } else {
            path = "/sdcard/Android/sandbox/io.github.takusan23/Kaisendon"
        }
*/
        //知らんけどScoped Storage聞かない？
        path = Environment.getExternalStorageDirectory().path + "/Kaisendon"

        //パスをTextViewに入れる
        path_TextView!!.append("\n$path/kaisendon_backup")
    }

    /*バックアップ、リストアはちゃんとUI作って書き直す予定（）*/

    /**
     * DataBaseバックアップ？
     *
     *
     * https://stackoverflow.com/questions/18635412/restoring-sqlite-db-file
     */
    private fun startBackupDB() {
        //Android Pie（9.0）だと/sdcardに作られるけど、
        //Android Q（不明）はScoped Storageの関係上/sdcard/Android/sandbox/io.github.takusan23/kaisendonに作成されます
        backup("CustomMenu.db")
        backup("TootBookmark.db")
        Toast.makeText(context, getString(R.string.backup_successful) + "\n" + path + "/kaisendon_backup", Toast.LENGTH_SHORT).show()
    }

    /**
     * リストア
     */
    private fun startRestore() {
        restore("CustomMenu.db")
        restore("TootBookmark.db")
        Toast.makeText(context, getString(R.string.restore_successful), Toast.LENGTH_SHORT).show()
    }


    /**
     * Backup
     */
    private fun backup(fileName: String) {
        try {
            //ぱす
            val kaisendon_path = Environment.getExternalStorageDirectory().path + "/Kaisendon"
            val kaisendon_file = File(kaisendon_path)
            kaisendon_file.mkdir()
            val sd = File("$kaisendon_path/kaisendon_backup")
            // kaisendonディレクトリを作成する
            sd.mkdir()
            //ユーザーが扱えない領域？
            val data = Environment.getDataDirectory()
            if (sd.canWrite()) {
                val databasepath = "//data/io.github.takusan23.Kaisendon/databases/$fileName"
                val currentDB = File(data, databasepath)
                val backupDB = File(sd, fileName)
                if (currentDB.exists()) {
                    val src = FileInputStream(currentDB).channel
                    val dst = FileOutputStream(backupDB).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                }
            }
        } catch (e: FileNotFoundException) {
            e.fillInStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * リストア
     */
    private fun restore(fileName: String) {

        //ぱす
        val kaisendon_path = Environment.getExternalStorageDirectory().path + "/Kaisendon"
        val kaisendon_file = File(kaisendon_path)
        kaisendon_file.mkdir()
        val sd = File("$kaisendon_path/kaisendon_backup")
        //ユーザーが扱えない領域？
        val data = Environment.getDataDirectory()
        try {
            if (sd.canWrite()) {
                val databasepath = "//data/io.github.takusan23.Kaisendon/databases/$fileName"
                val currentDB = File(data, databasepath)
                val backupDB = File(sd, fileName)

                if (currentDB.exists()) {
                    val src = FileInputStream(backupDB).channel
                    val dst = FileOutputStream(currentDB).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                    //Toast.makeText(getContext(), "リストアが完了しました\n" + sd.getPath() + "/" + backupfilename, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

}