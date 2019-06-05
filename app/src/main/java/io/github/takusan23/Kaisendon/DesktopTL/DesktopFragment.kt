package io.github.takusan23.Kaisendon.DesktopTL


import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.graphics.Point
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuSQLiteHelper
import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine
import io.github.takusan23.Kaisendon.R
import org.json.JSONException
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class DesktopFragment : Fragment() {

    private var pref_setting: SharedPreferences? = null
    private var main_LinearLayout: LinearLayout? = null
    private var scrollview_LinearLayout: LinearLayout? = null
    private val access_token: String? = null
    private val instance: String? = null
    private var helper: CustomMenuSQLiteHelper? = null
    private var db: SQLiteDatabase? = null
    private var display: Display? = null
    private var point: Point? = null
    private var config: Configuration? = null
    private var x: Int = 0
    private var isDesktopFragment = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_desktop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        main_LinearLayout = view.findViewById(R.id.descktop_parent_linearlayout)
        scrollview_LinearLayout = view.findViewById(R.id.scrollview_linearlayout)
        isDesktopFragment = true

        //投稿に関しての注意書き
        Toast.makeText(context, getString(R.string.desktop_create_menu_message), Toast.LENGTH_LONG).show()

        if (helper ==
                null) {
            helper = CustomMenuSQLiteHelper(context!!)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            db!!.disableWriteAheadLogging()
        }

        //高さ調整で使う
        display = activity!!.windowManager.defaultDisplay
        point = Point()
        display!!.getSize(point)
        config = resources.configuration
        //縦、横
        val vertical = Integer.valueOf(pref_setting!!.getString("desktop_mode_vertical_number", "2")!!)
        val horizontal = Integer.valueOf(pref_setting!!.getString("desktop_mode_horizontal_number", "3")!!)

        //縦→２、横→３
        if (config!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            x = point!!.x / horizontal
        } else {
            x = point!!.x / vertical
        }

        //再生成しない？
        retainInstance = true

        /*
        instance = pref_setting.getString("main_instance", "");
        access_token = pref_setting.getString("main_token", "");
*/
        //マルチカラム
        loadCustomMenu()
        activity!!.title = getString(R.string.desktop_mode)

        /*
        for (int i = 0; i < 3; i++) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            scrollview_LinearLayout.addView(linearLayout);
            getLayoutInflater().inflate(R.layout.desktop_tl_column_layout, linearLayout);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.desktop_tl_column_recyclerview);
            TextView title_TextView = linearLayout.findViewById(R.id.desktop_tl_column_title_textview);
            title_TextView.setText(name[i]);
            //ここから下三行必須
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(mLayoutManager);
            ArrayList<ArrayList> recyclerViewList = new ArrayList<>();
            CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
            loadTimeline(list[i], recyclerViewList, customMenuRecyclerViewAdapter, recyclerView);
        }
*/
    }

    /**
     * SQLite読み込み
     */
    private fun loadCustomMenu() {
        val cursor = db!!.query(
                "custom_menudb",
                arrayOf("name", "setting"), null, null, null, null, null
        )
        cursor.moveToFirst()

        for (i in 0 until cursor.count) {
            //LinearLayout生成
            val linearLayout = LinearLayout(context)
            val cardView = CardView(context!!)
            val card_LayoutParams = LinearLayout.LayoutParams(x, ViewGroup.LayoutParams.MATCH_PARENT)
            card_LayoutParams.setMargins(10, 10, 10, 10)
            cardView.layoutParams = card_LayoutParams
            //Title + replaceするLinearLayoutを入れるLinearlayout
            val card_LinearLayout = LinearLayout(context)
            card_LinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            card_LinearLayout.orientation = LinearLayout.VERTICAL
            //Title
            val title_TextView = TextView(context)
            //replaceするView
            val add_LinearLayout = LinearLayout(context)
            //動的にID設定
            add_LinearLayout.id = View.generateViewId()
            //addViewする
            card_LinearLayout.setPadding(10, 10, 10, 10)
            card_LinearLayout.addView(title_TextView)
            card_LinearLayout.addView(add_LinearLayout)
            //CardViewをAddView
            cardView.addView(card_LinearLayout)
            linearLayout.addView(cardView)
            /*
            getLayoutInflater().inflate(R.layout.desktop_tl_column_layout, linearLayout);
            LinearLayout linearLayout_add = linearLayout.findViewById(R.id.desktop_tl_column_linearlayout);
            TextView title_TextView = linearLayout.findViewById(R.id.desktop_tl_column_title_textview);
*/
            scrollview_LinearLayout!!.addView(linearLayout)

            var misskey = ""
            var name = ""
            var content = ""
            var instance = ""
            var access_token = ""
            var image_load = ""
            var dialog = ""
            var dark_mode = ""
            var position = ""
            var streaming = ""
            var subtitle = ""
            var image_url = ""
            var background_transparency = ""
            var background_screen_fit = ""
            var quick_profile = ""
            var toot_counter = ""
            var custom_emoji = ""
            var gif = ""
            var font = ""
            var one_hand = ""
            var misskey_username = ""
            val no_fav_icon = ""
            val yes_fav_icon = ""
            var setting = ""
            try {
                val jsonObject = JSONObject(cursor.getString(1))
                name = jsonObject.getString("name")
                content = jsonObject.getString("content")
                instance = jsonObject.getString("instance")
                access_token = jsonObject.getString("access_token")
                image_load = jsonObject.getString("image_load")
                dialog = jsonObject.getString("dialog")
                dark_mode = jsonObject.getString("dark_mode")
                position = jsonObject.getString("position")
                streaming = jsonObject.getString("streaming")
                subtitle = jsonObject.getString("subtitle")
                image_url = jsonObject.getString("image_url")
                background_transparency = jsonObject.getString("background_transparency")
                background_screen_fit = jsonObject.getString("background_screen_fit")
                quick_profile = jsonObject.getString("quick_profile")
                toot_counter = jsonObject.getString("toot_counter")
                custom_emoji = jsonObject.getString("custom_emoji")
                gif = jsonObject.getString("gif")
                font = jsonObject.getString("font")
                one_hand = jsonObject.getString("one_hand")
                misskey = jsonObject.getString("misskey")
                misskey_username = jsonObject.getString("misskey_username")
                setting = jsonObject.getString("setting")
                val bundle = Bundle()
                bundle.putString("misskey", misskey)
                bundle.putString("name", name)
                bundle.putString("content", content)
                bundle.putString("instance", instance)
                bundle.putString("access_token", access_token)
                bundle.putString("image_load", image_load)
                bundle.putString("dialog", dialog)
                bundle.putString("dark_mode", dark_mode)
                bundle.putString("position", position)
                bundle.putString("streaming", streaming)
                bundle.putString("subtitle", subtitle)
                bundle.putString("image_url", image_url)
                bundle.putString("background_transparency", background_transparency)
                bundle.putString("background_screen_fit", background_screen_fit)
                bundle.putString("quick_profile", quick_profile)
                bundle.putString("toot_counter", toot_counter)
                bundle.putString("custom_emoji", custom_emoji)
                bundle.putString("gif", gif)
                bundle.putString("font", font)
                bundle.putString("one_hand", one_hand)
                bundle.putString("misskey_username", misskey_username)
                bundle.putString("setting", setting)
                bundle.putString("json", jsonObject.toString())
                //                bundle.putString("no_fav_icon", no_fav_icon);
                //                bundle.putString("yes_fav_icon", yes_fav_icon);
                val customMenuTimeLine = CustomMenuTimeLine()
                customMenuTimeLine.arguments = bundle
                //置き換え
                title_TextView.text = name
                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(add_LinearLayout.id, customMenuTimeLine)
                transaction.commit()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            cursor.moveToNext()
        }
        cursor.close()
    }

}