package io.github.takusan23.kaisendon.Fragment;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.TootBookmark_SQLite;
import okhttp3.OkHttpClient;

public class Bookmark_Frament extends Fragment {

    private TootBookmark_SQLite sqLite;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.bookmark_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        getActivity().setTitle(R.string.bookmark);

        ArrayList<ListItem> toot_list = new ArrayList<>();
        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);
        ListView listView = view.findViewById(R.id.bookmark_listview);

        getActivity().setTitle(R.string.bookmark);

        //設定読み込み
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        String AccessToken = null;
        String Instance = null;

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)){
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }


        //背景
        ImageView background_imageView = view.findViewById(R.id.bookmark_background_imageview);

        if (pref_setting.getBoolean("background_image", true)) {
            Uri uri = Uri.parse(pref_setting.getString("background_image_path", ""));
            Glide.with(getContext())
                    .load(uri)
                    .into(background_imageView);
        }


        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f) != 0.0) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        //読み込み
        if (sqLite == null) {
            sqLite = new TootBookmark_SQLite(getContext());
        }

        if (sqLiteDatabase == null) {
            sqLiteDatabase = sqLite.getReadableDatabase();
        }
        Log.d("debug", "**********Cursor");

        Cursor cursor = sqLiteDatabase.query(
                "tootbookmarkdb",
                new String[]{"toot", "id", "account", "info", "account_id", "avater_url", "username", "media1", "media2", "media3", "media4"},
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
/*
            System.out.println("値 : " + cursor.getString(1));
            arrayAdapter.add(cursor.getString(1));
*/

            String toot = cursor.getString(0);
            String toot_id = cursor.getString(1);
            String account = cursor.getString(2);
            String info = cursor.getString(3);
            String account_id_string = cursor.getString(4);
            long account_id = Long.parseLong(account_id_string);
            String avater_url = cursor.getString(5);
            String username = cursor.getString(6);

            String media1 = cursor.getString(7);
            String media2 = cursor.getString(8);
            String media3 = cursor.getString(9);
            String media4 = cursor.getString(10);


            //配列を作成
            ArrayList<String> Item = new ArrayList<>();
            //メモとか通知とかに
            Item.add("bookmark");
            //内容
            Item.add(toot);
            //ユーザー名
            Item.add(account);
            //時間、クライアント名等
            Item.add(info);
            //Toot ID 文字列版
            Item.add(toot_id);
            //アバターURL
            Item.add(avater_url);
            //アカウントID
            Item.add(String.valueOf(account_id));
            //ユーザーネーム
            Item.add(null);
            //メディア
            Item.add(media1);
            Item.add(media2);
            Item.add(media3);
            Item.add(media4);
            //カード
            Item.add(null);
            Item.add(null);
            Item.add(null);
            Item.add(null);


            if (getActivity() != null){
                ListItem listItem = new ListItem(Item);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.insert(listItem, 0);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            cursor.moveToNext();
        }

        // 忘れずに！
        cursor.close();

        //アダプターにアイテムが有るか確認
        if (!adapter.isEmpty()) {
            //ある時（if反転）
            listView.setAdapter(adapter);
        } else {
            //無いとき
            LinearLayout linearLayout = view.findViewById(R.id.bookmark_linearlayout);
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            ((LinearLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;
            TextView textView = new TextView(getContext());
            textView.setTextSize(20);
            textView.setText(getString(R.string.bookmark_isempty)+"　😢");

            textView.setLayoutParams(layoutParams);
            linearLayout.addView(textView);
        }


        //Log.d("debug", "**********" + sbuilder.toString());
        //listView.setAdapter(adapter);


/*
        //Listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //読み込み
                if (sqLite == null) {
                    sqLite = new TootBookmark_SQLite(getContext());
                }

                if (sqLiteDatabase == null) {
                    sqLiteDatabase = sqLite.getReadableDatabase();
                }
                //消去
                TextView toot_text_box = view.findViewById(R.id.client);
                sqLiteDatabase.delete("tootbookmarkdb", "info=?", new String[]{toot_text_box.getText().toString()});
                Toast.makeText(getContext(), "Delete", Toast.LENGTH_SHORT).show();

            }
        });
*/
    }
}