package io.github.takusan23.kaisendon.DesktopTL;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.takusan23.kaisendon.CustomMenu.CustomMenuSQLiteHelper;
import io.github.takusan23.kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.kaisendon.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class DesktopFragment extends Fragment {

    private SharedPreferences pref_setting;
    private LinearLayout main_LinearLayout;
    private LinearLayout scrollview_LinearLayout;
    private String access_token;
    private String instance;
    private CustomMenuSQLiteHelper helper = null;
    private SQLiteDatabase db = null;
    private Display display;
    private Point point;
    private Configuration config;
    private int x;
    private boolean isDesktopFragment = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_desktop, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        main_LinearLayout = view.findViewById(R.id.descktop_parent_linearlayout);
        scrollview_LinearLayout = view.findViewById(R.id.scrollview_linearlayout);
        isDesktopFragment = true;

        //投稿に関しての注意書き
        Toast.makeText(getContext(), getString(R.string.desktop_create_menu_message), Toast.LENGTH_LONG).show();

        if (helper == null) {
            helper = new CustomMenuSQLiteHelper(getContext());
        }
        if (db == null) {
            db = helper.getWritableDatabase();
            db.disableWriteAheadLogging();
        }

        //高さ調整で使う
        display = getActivity().getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getSize(point);
        config = getResources().getConfiguration();
        //縦、横
        int vertical = Integer.valueOf(pref_setting.getString("desktop_mode_vertical_number", "2"));
        int horizontal = Integer.valueOf(pref_setting.getString("desktop_mode_horizontal_number", "3"));

        //縦→２、横→３
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            x = point.x / horizontal;
        } else {
            x = point.x / vertical;
        }

        //再生成しない？
        setRetainInstance(true);

/*
        instance = pref_setting.getString("main_instance", "");
        access_token = pref_setting.getString("main_token", "");
*/
        //マルチカラム
        loadCustomMenu();
        getActivity().setTitle(getString(R.string.desktop_mode));

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
    private void loadCustomMenu() {
        Cursor cursor = db.query(
                "custom_menudb",
                new String[]{"name", "setting"},
                null,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            //LinearLayout生成
            LinearLayout linearLayout = new LinearLayout(getContext());
            CardView cardView = new CardView(getContext());
            ViewGroup.LayoutParams card_LayoutParams = new LinearLayout.LayoutParams(x, ViewGroup.LayoutParams.MATCH_PARENT);
            ((LinearLayout.LayoutParams) card_LayoutParams).setMargins(10, 10, 10, 10);
            cardView.setLayoutParams(card_LayoutParams);
            //Title + replaceするLinearLayoutを入れるLinearlayout
            LinearLayout card_LinearLayout = new LinearLayout(getContext());
            card_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            card_LinearLayout.setOrientation(LinearLayout.VERTICAL);
            //Title
            TextView title_TextView = new TextView(getContext());
            //replaceするView
            LinearLayout add_LinearLayout = new LinearLayout(getContext());
            //動的にID設定
            add_LinearLayout.setId(View.generateViewId());
            //addViewする
            card_LinearLayout.setPadding(10, 10, 10, 10);
            card_LinearLayout.addView(title_TextView);
            card_LinearLayout.addView(add_LinearLayout);
            //CardViewをAddView
            cardView.addView(card_LinearLayout);
            linearLayout.addView(cardView);
/*
            getLayoutInflater().inflate(R.layout.desktop_tl_column_layout, linearLayout);
            LinearLayout linearLayout_add = linearLayout.findViewById(R.id.desktop_tl_column_linearlayout);
            TextView title_TextView = linearLayout.findViewById(R.id.desktop_tl_column_title_textview);
*/
            scrollview_LinearLayout.addView(linearLayout);

            String misskey = "";
            String name = "";
            String content = "";
            String instance = "";
            String access_token = "";
            String image_load = "";
            String dialog = "";
            String dark_mode = "";
            String position = "";
            String streaming = "";
            String subtitle = "";
            String image_url = "";
            String background_transparency = "";
            String background_screen_fit = "";
            String quick_profile = "";
            String toot_counter = "";
            String custom_emoji = "";
            String gif = "";
            String font = "";
            String one_hand = "";
            String misskey_username = "";
            String setting = "";
            try {
                JSONObject jsonObject = new JSONObject(cursor.getString(1));
                name = jsonObject.getString("name");
                content = jsonObject.getString("content");
                instance = jsonObject.getString("instance");
                access_token = jsonObject.getString("access_token");
                image_load = jsonObject.getString("image_load");
                dialog = jsonObject.getString("dialog");
                dark_mode = jsonObject.getString("dark_mode");
                position = jsonObject.getString("position");
                streaming = jsonObject.getString("streaming");
                subtitle = jsonObject.getString("subtitle");
                image_url = jsonObject.getString("image_url");
                background_transparency = jsonObject.getString("background_transparency");
                background_screen_fit = jsonObject.getString("background_screen_fit");
                quick_profile = jsonObject.getString("quick_profile");
                toot_counter = jsonObject.getString("toot_counter");
                custom_emoji = jsonObject.getString("custom_emoji");
                gif = jsonObject.getString("gif");
                font = jsonObject.getString("font");
                one_hand = jsonObject.getString("one_hand");
                misskey = jsonObject.getString("misskey");
                misskey_username = jsonObject.getString("misskey_username");
                setting = jsonObject.getString("setting");
                Bundle bundle = new Bundle();
                bundle.putString("misskey", misskey);
                bundle.putString("name", name);
                bundle.putString("content", content);
                bundle.putString("instance", instance);
                bundle.putString("access_token", access_token);
                bundle.putString("image_load", image_load);
                bundle.putString("dialog", dialog);
                bundle.putString("dark_mode", dark_mode);
                bundle.putString("position", position);
                bundle.putString("streaming", streaming);
                bundle.putString("subtitle", subtitle);
                bundle.putString("image_url", image_url);
                bundle.putString("background_transparency", background_transparency);
                bundle.putString("background_screen_fit", background_screen_fit);
                bundle.putString("quick_profile", quick_profile);
                bundle.putString("toot_counter", toot_counter);
                bundle.putString("custom_emoji", custom_emoji);
                bundle.putString("gif", gif);
                bundle.putString("font", font);
                bundle.putString("one_hand", one_hand);
                bundle.putString("misskey_username", misskey_username);
                bundle.putString("setting", setting);
                bundle.putString("json", jsonObject.toString());
                CustomMenuTimeLine customMenuTimeLine = new CustomMenuTimeLine();
                customMenuTimeLine.setArguments(bundle);
                //置き換え
                title_TextView.setText(name);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(add_LinearLayout.getId(), customMenuTimeLine);
                transaction.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

}