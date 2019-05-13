package io.github.takusan23.kaisendon.CustomMenu;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.takusan23.kaisendon.R;

public class CustomMenuLoadSupport {
    private Context context;
    //private FragmentTransaction transaction;
    private NavigationView navigationView;
    private CustomMenuSQLiteHelper helper;
    private SQLiteDatabase db;
    private SharedPreferences pref_setting;

    public CustomMenuLoadSupport(Context context, NavigationView navigationView) {
        this.context = context;
        this.navigationView = navigationView;
        this.pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        //transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
    }


    /**
     * カスタムメニュー読み込み
     *
     * @param search 最後に開いた（ｒｙを使うときに使う。<b>使わないときはnullを入れてね</b>
     */
    public void loadCustomMenu(String search) {
        //SQLite
        if (helper == null) {
            helper = new CustomMenuSQLiteHelper(context);
        }
        if (db == null) {
            db = helper.getWritableDatabase();
            db.disableWriteAheadLogging();
        }
        //SQLite読み込み
        Cursor cursor;
        //検索するかどうか
        if (search != null) {
            cursor = db.query(
                    "custom_menudb",
                    new String[]{"name", "setting"},
                    "name=?",
                    new String[]{search},
                    null,
                    null,
                    null
            );
        } else {
            cursor = db.query(
                    "custom_menudb",
                    new String[]{"name", "setting"},
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

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
        String no_fav_icon = "";
        String yes_fav_icon = "";
        String json = "";

        cursor.moveToFirst();

        //最後に開く機能使うか？
        if (search != null) {
            try {
                JSONObject jsonObject = new JSONObject(cursor.getString(1));
                json = jsonObject.toString();
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
//                no_fav_icon = jsonObject.getString("no_fav_icon");
//                yes_fav_icon = jsonObject.getString("yes_fav_icon");
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
                bundle.putString("no_fav_icon", no_fav_icon);
                bundle.putString("yes_fav_icon", yes_fav_icon);
                bundle.putString("json", json);
                CustomMenuTimeLine customMenuTimeLine = new CustomMenuTimeLine();
                customMenuTimeLine.setArguments(bundle);
                //名前控える
                saveLastOpenCustomMenu(name);
                //置き換え
                FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container_container, customMenuTimeLine);
                transaction.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            for (int i = 0; i < cursor.getCount(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(cursor.getString(1));
                    json = jsonObject.toString();
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
//                    no_fav_icon = jsonObject.getString("no_fav_icon");
//                    yes_fav_icon = jsonObject.getString("yes_fav_icon");
                    setting = jsonObject.getString("setting");
                    //メニュー追加
                    String finalName = name;
                    String finalContent = content;
                    String finalInstance = instance;
                    String finalAccess_token = access_token;
                    String finalImage_load = image_load;
                    String finalDialog = dialog;
                    String finalDark_mode = dark_mode;
                    String finalPosition = position;
                    String finalStreaming = streaming;
                    String finalSubtitle = subtitle;
                    String finalImage_url = image_url;
                    String finalBackground_transparency = background_transparency;
                    String finalBackground_screen_fit = background_screen_fit;
                    String finalQuick_profile = quick_profile;
                    String finalToot_counter = toot_counter;
                    String finalCustom_emoji = custom_emoji;
                    String finalGif = gif;
                    String finalFont = font;
                    String finalOne_hand = one_hand;
                    String finalSetting = setting;
                    String finalMisskey = misskey;
                    String finalMisskey_username = misskey_username;
                    String finalJson = json;
                    String finalNo_fav_icon = no_fav_icon;
                    String finalYes_fav_icon = yes_fav_icon;
                    navigationView.getMenu().add(name).setIcon(urlToContent(content)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            //Fragment切り替え
                            //受け渡す
                            Bundle bundle = new Bundle();
                            bundle.putString("misskey", finalMisskey);
                            bundle.putString("name", finalName);
                            bundle.putString("content", finalContent);
                            bundle.putString("instance", finalInstance);
                            bundle.putString("access_token", finalAccess_token);
                            bundle.putString("image_load", finalImage_load);
                            bundle.putString("dialog", finalDialog);
                            bundle.putString("dark_mode", finalDark_mode);
                            bundle.putString("position", finalPosition);
                            bundle.putString("streaming", finalStreaming);
                            bundle.putString("subtitle", finalSubtitle);
                            bundle.putString("image_url", finalImage_url);
                            bundle.putString("background_transparency", finalBackground_transparency);
                            bundle.putString("background_screen_fit", finalBackground_screen_fit);
                            bundle.putString("quick_profile", finalQuick_profile);
                            bundle.putString("toot_counter", finalToot_counter);
                            bundle.putString("custom_emoji", finalCustom_emoji);
                            bundle.putString("gif", finalGif);
                            bundle.putString("font", finalFont);
                            bundle.putString("one_hand", finalOne_hand);
                            bundle.putString("misskey_username", finalMisskey_username);
                            bundle.putString("setting", finalSetting);
                            bundle.putString("no_fav_icon", finalNo_fav_icon);
                            bundle.putString("yes_fav_icon", finalYes_fav_icon);
                            bundle.putString("json", finalJson);
                            CustomMenuTimeLine customMenuTimeLine = new CustomMenuTimeLine();
                            customMenuTimeLine.setArguments(bundle);
                            //名前控える
                            saveLastOpenCustomMenu(finalName);
                            //置き換え
                            FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.container_container, customMenuTimeLine);
                            transaction.commit();
                            return false;
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    //なくてもとりあえず追加する
                    //メニュー追加
                    String finalName = name;
                    String finalContent = content;
                    String finalInstance = instance;
                    String finalAccess_token = access_token;
                    String finalImage_load = image_load;
                    String finalDialog = dialog;
                    String finalDark_mode = dark_mode;
                    String finalPosition = position;
                    String finalStreaming = streaming;
                    String finalSubtitle = subtitle;
                    String finalImage_url = image_url;
                    String finalBackground_transparency = background_transparency;
                    String finalBackground_screen_fit = background_screen_fit;
                    String finalQuick_profile = quick_profile;
                    String finalToot_counter = toot_counter;
                    String finalCustom_emoji = custom_emoji;
                    String finalGif = gif;
                    String finalFont = font;
                    String finalOne_hand = one_hand;
                    String finalSetting = setting;
                    String finalMisskey = misskey;
                    String finalMisskey_username = misskey_username;
                    String finalJson = json;
                    String finalNo_fav_icon = no_fav_icon;
                    String finalYes_fav_icon = yes_fav_icon;
                    navigationView.getMenu().add(name).setIcon(urlToContent(content)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            //Fragment切り替え
                            //受け渡す
                            Bundle bundle = new Bundle();
                            bundle.putString("misskey", finalMisskey);
                            bundle.putString("name", finalName);
                            bundle.putString("content", finalContent);
                            bundle.putString("instance", finalInstance);
                            bundle.putString("access_token", finalAccess_token);
                            bundle.putString("image_load", finalImage_load);
                            bundle.putString("dialog", finalDialog);
                            bundle.putString("dark_mode", finalDark_mode);
                            bundle.putString("position", finalPosition);
                            bundle.putString("streaming", finalStreaming);
                            bundle.putString("subtitle", finalSubtitle);
                            bundle.putString("image_url", finalImage_url);
                            bundle.putString("background_transparency", finalBackground_transparency);
                            bundle.putString("background_screen_fit", finalBackground_screen_fit);
                            bundle.putString("quick_profile", finalQuick_profile);
                            bundle.putString("toot_counter", finalToot_counter);
                            bundle.putString("custom_emoji", finalCustom_emoji);
                            bundle.putString("gif", finalGif);
                            bundle.putString("font", finalFont);
                            bundle.putString("one_hand", finalOne_hand);
                            bundle.putString("misskey_username", finalMisskey_username);
                            bundle.putString("setting", finalSetting);
                            bundle.putString("no_fav_icon", finalNo_fav_icon);
                            bundle.putString("yes_fav_icon", finalYes_fav_icon);
                            bundle.putString("json", finalJson);
                            CustomMenuTimeLine customMenuTimeLine = new CustomMenuTimeLine();
                            customMenuTimeLine.setArguments(bundle);
                            //名前控える
                            saveLastOpenCustomMenu(finalName);
                            //置き換え
                            FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.container_container, customMenuTimeLine);
                            transaction.commit();
                            return false;
                        }
                    });
                }
                cursor.moveToNext();

            }
        }
        cursor.close();
    }

    /**
     * 最後に開いたカスタムメニュー保存
     */
    private void saveLastOpenCustomMenu(String name) {
        SharedPreferences.Editor editor = pref_setting.edit();
        editor.putString("custom_menu_last", name);
        editor.apply();
    }

    /*アイコンを返す*/
    private Drawable urlToContent(String url) {
        Drawable drawable = context.getDrawable(R.drawable.ic_home_black_24dp);
        switch (url) {
            case "/api/v1/timelines/home":
                drawable = context.getDrawable(R.drawable.ic_home_black_24dp);
                break;
            case "/api/v1/notifications":
                drawable = context.getDrawable(R.drawable.ic_notifications_black_24dp);
                break;
            case "/api/v1/timelines/public?local=true":
                drawable = context.getDrawable(R.drawable.ic_train_black_24dp);
                break;
            case "/api/v1/timelines/public":
                drawable = context.getDrawable(R.drawable.ic_flight_black_24dp);
                break;
            case "/api/v1/timelines/direct":
                drawable = context.getDrawable(R.drawable.ic_assignment_ind_black_24dp);
                break;
            case "/api/v1/favourites":
                drawable = context.getDrawable(R.drawable.ic_star_black_24dp);
                break;
            case "/api/v1/scheduled_statuses":
                drawable = context.getDrawable(R.drawable.ic_access_alarm_black_24dp);
                break;
            case "/api/v1/suggestions":
                drawable = context.getDrawable(R.drawable.ic_person_add_black_24dp);
                break;
            case "/api/notes/timeline":
                drawable = context.getDrawable(R.drawable.ic_home_black_24dp);
                break;
            case "/api/i/notifications":
                drawable = context.getDrawable(R.drawable.ic_notifications_black_24dp);
                break;
            case "/api/notes/local-timeline":
                drawable = context.getDrawable(R.drawable.ic_train_black_24dp);
                break;
            case "/api/notes/global-timeline":
                drawable = context.getDrawable(R.drawable.ic_flight_black_24dp);
                break;
        }
        return drawable;
    }


}
