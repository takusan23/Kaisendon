package io.github.takusan23.Kaisendon.Fragment


import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter
import io.github.takusan23.Kaisendon.R
import io.github.takusan23.Kaisendon.SnackberProgress
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ActivityPubViewer : Fragment() {
    private var pref_setting: SharedPreferences? = null

    //RecyclerView
    private var recyclerViewList: ArrayList<ArrayList<*>>? = null
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var customMenuRecyclerViewAdapter: CustomMenuRecyclerViewAdapter? = null

    private var path = ""
    private var json = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_pub_viewer, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)

        recyclerView = view.findViewById(R.id.activity_pub_viwer_recyclerView)

        recyclerViewList = ArrayList()
        //ここから下三行必須
        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = mLayoutManager
        customMenuRecyclerViewAdapter = CustomMenuRecyclerViewAdapter(recyclerViewList!!)
        recyclerView!!.adapter = customMenuRecyclerViewAdapter
        recyclerViewLayoutManager = recyclerView!!.layoutManager

        activity!!.title = getString(R.string.activity_pub_viewer)

        //ぱす（Android Qから変わった
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            path = context?.getExternalFilesDir(null)?.path + "/Kaisendon/activity_pub_json/outbox.json"
        } else {
            path = Environment.getExternalStorageDirectory().path + "/Kaisendon/activity_pub_json/outbox.json"
        }

        Toast.makeText(context, getString(R.string.activity_pub_message) + "\n" + path, Toast.LENGTH_LONG).show()

        //ストレージ読み込み、書き込み権限チェック
        val read = ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
        val write = ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED) {
            //許可済み
            loadOutBoxJSON()
        } else {
            //許可を求める
            //配列なんだねこれ
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 4545)
        }

    }

    /**
     * outbox.jsonを読み込む？
     */
    private fun loadOutBoxJSON() {
        //くるくる
        SnackberProgress.showProgressSnackber(recyclerView, context!!, getString(R.string.loading))
        //ぱす
        var kaisendon_path = ""
        //ぱす
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            kaisendon_path = context?.getExternalFilesDir(null)?.path + "/Kaisendon"
        } else {
            kaisendon_path = Environment.getExternalStorageDirectory().path + "/Kaisendon"
        }
        val kaisendon_file = File(kaisendon_path)
        kaisendon_file.mkdir()
        //ディレクトリ生成
        val file = File("$kaisendon_path/activity_pub_json")
        file.mkdir()
        //ファイルかあるか
        val json_file = File(file.path + "/outbox.json")
        //Nexus 7 2013でクソ重かったので非同期処理
        Thread(Runnable {
            if (json_file.exists()) {
                try {
                    //JSONデータ取り出し
                    val fileInputStream = FileInputStream(json_file)
                    val inputStreamReader = InputStreamReader(fileInputStream, "UTF-8")
                    val reader = BufferedReader(inputStreamReader)
                    var lineBuffer: String
                    do {
                        lineBuffer = reader.readLine()
                        if (lineBuffer == null)
                            break
                    } while (true)
                    //RecyclerViewセット
                    setRecyclerView()
                    fileInputStream.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                activity!!.runOnUiThread { Toast.makeText(context, getString(R.string.activity_pub_message) + "\n" + path, Toast.LENGTH_LONG).show() }
            }
        }).start()
    }

    /**
     * RecyclerViewセット
     */
    private fun setRecyclerView() {
        try {
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("orderedItems")
            for (i in 0 until jsonArray.length()) {
                val toot_JsonObject = jsonArray.getJSONObject(i)
                if (activity != null && isAdded) {
                    //配列を作成
                    val Item = ArrayList<String>()
                    //メモとか通知とかに
                    Item.add("ActivityPub")
                    //内容
                    Item.add("")
                    //ユーザー名
                    Item.add("")
                    //JSONObject
                    Item.add(toot_JsonObject.toString())
                    //ぶーすとした？
                    Item.add("false")
                    //ふぁぼした？
                    Item.add("false")
                    //Mastodon / Misskey
                    Item.add("ActivityPub")
                    //Insatnce/AccessToken
                    Item.add("")
                    Item.add("")
                    Item.add("")
                    //画像表示、こんてんとわーにんぐ
                    Item.add("false")
                    Item.add("false")

                    recyclerViewList!!.add(Item)
                }
            }
            activity!!.runOnUiThread {
                customMenuRecyclerViewAdapter!!.notifyDataSetChanged()
                //くるくる終了
                SnackberProgress.closeProgressSnackber()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

}// Required empty public constructor
