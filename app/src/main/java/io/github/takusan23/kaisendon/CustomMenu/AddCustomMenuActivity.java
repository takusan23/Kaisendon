package io.github.takusan23.kaisendon.CustomMenu;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static io.github.takusan23.kaisendon.Preference_ApplicationContext.getContext;

public class AddCustomMenuActivity extends AppCompatActivity {
    private MenuBuilder account_menuBuilder;
    private MenuPopupHelper account_optionsMenu;
    private CustomMenuSQLiteHelper helper;
    private SQLiteDatabase db;

    private LinearLayout linearLayout;
    private EditText name_EditText;
    private Button load_Button;
    private Button account_Button;
    private Switch dialog_Switch;
    private Switch image_Switch;
    private Switch dark_Switch;
    private Switch streaming_Switch;
    private EditText subtitle_EditText;
    private FloatingActionButton fab;

    private SharedPreferences pref_setting;

    private String load_url;
    private String instance;
    private String access_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custom_menu);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(this);

        setTitle(R.string.custom_menu_add);

        fab = findViewById(R.id.custom_menu_add_fab);
        linearLayout = findViewById(R.id.add_custom_menu_linearlayout);
        name_EditText = findViewById(R.id.custom_menu_name_edittext_edittext);
        load_Button = findViewById(R.id.custom_menu_load);
        account_Button = findViewById(R.id.custom_menu_account);
        dialog_Switch = findViewById(R.id.custom_menu_dialog);
        image_Switch = findViewById(R.id.custom_menu_image);
        dark_Switch = findViewById(R.id.custom_menu_darkmode);
        streaming_Switch = findViewById(R.id.custom_menu_streaming);
        subtitle_EditText = findViewById(R.id.custom_menu_subtitle_edittext_edittext);

        //SQLite
        if (helper == null) {
            helper = new CustomMenuSQLiteHelper(getApplicationContext());
        }
        if (db == null) {
            db = helper.getWritableDatabase();
        }

        //削除ボタン
        //ListViewから来たとき
        if (getIntent().getBooleanExtra("delete_button", false)) {
            String name = getIntent().getStringExtra("name");
            //ボタンを動的に生成
            Button delete_Button = new Button(getContext());
            delete_Button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            delete_Button.setBackground(getDrawable(R.drawable.button_style_white));
            delete_Button.setText(R.string.custome_menu_delete);
            delete_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, R.string.custom_setting_delete_message, Toast.LENGTH_SHORT).setAction(R.string.delete_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            db.delete("custom_menudb", "name=?", new String[]{name});
                            startActivity(new Intent(AddCustomMenuActivity.this, Home.class));
                        }
                    }).show();
                }
            });
            loadSQLite(name);
            linearLayout.addView(delete_Button);
        }


        //メニュー
        setLoadMenu();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SnackBer
                Snackbar.make(v, R.string.custom_add_message, Toast.LENGTH_SHORT).setAction(R.string.register, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveSQLite();
                        //戻る
                        startActivity(new Intent(getContext(), Home.class));
                    }
                }).show();
            }
        });

    }

    /**
     * SQLiteに保存する
     */
    private void saveSQLite() {
        ContentValues values = new ContentValues();
        values.put("name", name_EditText.getText().toString());
        values.put("memo", "");
        values.put("content", load_url);
        values.put("instance", instance);
        values.put("access_token", access_token);
        values.put("image_load", String.valueOf(image_Switch.isChecked()));
        values.put("dialog", String.valueOf(dialog_Switch.isChecked()));
        values.put("dark_mode", String.valueOf(dark_Switch.isChecked()));
        values.put("position", "");
        values.put("streaming", String.valueOf(!streaming_Switch.isChecked())); //反転させてONのときStereaming有効に
        values.put("subtitle", subtitle_EditText.getText().toString());
        values.put("setting", "");

        db.insert("custom_menudb", null, values);
    }


    //ポップアップメニュー
    @SuppressLint("RestrictedApi")
    private void setLoadMenu() {
        //ポップアップメニュー作成
        MenuBuilder menuBuilder = new MenuBuilder(AddCustomMenuActivity.this);
        MenuInflater inflater = new MenuInflater(AddCustomMenuActivity.this);
        inflater.inflate(R.menu.custom_menu_load_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(AddCustomMenuActivity.this, menuBuilder, load_Button);
        optionsMenu.setForceShowIcon(true);

        load_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //表示
                optionsMenu.show();
                //押したときの反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {

                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.custom_menu_load_home:
                                load_url = "/api/v1/timelines/home";
                                load_Button.setText(R.string.home);
                                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0);
                                break;
                            case R.id.custom_menu_load_notification:
                                load_url = "/api/v1/notifications";
                                load_Button.setText(R.string.notifications);
                                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0);
                                break;
                            case R.id.custom_menu_load_local:
                                load_url = "/api/v1/timelines/public?local=true";
                                load_Button.setText(R.string.public_time_line);
                                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0);
                                break;
                            case R.id.custom_menu_load_federated:
                                load_url = "/api/v1/timelines/public";
                                load_Button.setText(R.string.federated_timeline);
                                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0);
                                break;
                            case R.id.custom_menu_load_direct:
                                load_url = "/api/v1/timelines/direct";
                                load_Button.setText(R.string.direct_message);
                                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0);
                                break;
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });

            }
        });


        account_menuBuilder = new MenuBuilder(this);
        account_optionsMenu = new MenuPopupHelper(this, account_menuBuilder, account_Button);
        optionsMenu.setForceShowIcon(true);
        ArrayList<String> multi_account_instance = new ArrayList<>();
        ArrayList<String> multi_account_access_token = new ArrayList<>();
        //とりあえずPreferenceに書き込まれた値を
        String instance_instance_string = pref_setting.getString("instance_list", "");
        String account_instance_string = pref_setting.getString("access_list", "");
        if (!instance_instance_string.equals("")) {
            try {
                JSONArray instance_array = new JSONArray(instance_instance_string);
                JSONArray access_array = new JSONArray(account_instance_string);
                for (int i = 0; i < instance_array.length(); i++) {
                    multi_account_access_token.add(access_array.getString(i));
                    multi_account_instance.add(instance_array.getString(i));
                }
            } catch (Exception e) {

            }
        }

        if (multi_account_instance.size() >= 1) {
            for (int count = 0; count < multi_account_instance.size(); count++) {
                String multi_instance = multi_account_instance.get(count);
                String multi_access_token = multi_account_access_token.get(count);
                int finalCount = count;
                //GetAccount
                String url = "https://" + multi_instance + "/api/v1/accounts/verify_credentials/?access_token=" + multi_access_token;
                //作成
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                //GETリクエスト
                OkHttpClient client_1 = new OkHttpClient();
                client_1.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String response_string = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(response_string);
                            String display_name = jsonObject.getString("display_name");
                            String user_id = jsonObject.getString("acct");
                            account_menuBuilder.add(0, finalCount, 0, display_name + "(" + user_id + " / " + multi_instance + ")");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        //押したときの処理
        account_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //追加中に押したら落ちるから回避
                if (account_menuBuilder.size() == multi_account_instance.size()) {
                    account_optionsMenu.show();
                    account_menuBuilder.setCallback(new MenuBuilder.Callback() {
                        @Override
                        public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {

                            //ItemIdにマルチアカウントのカウントを入れている
                            int position = menuItem.getItemId();

                            instance = multi_account_instance.get(position);
                            access_token = multi_account_access_token.get(position);
                            account_Button.setText(instance);
                            return false;
                        }

                        @Override
                        public void onMenuModeChange(MenuBuilder menuBuilder) {

                        }
                    });

                } else {
                    Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //ListViewにあった場合は
    //読み込む

    /**
     * 読み込む
     */
    private void loadSQLite(String name) {
        Cursor cursor = db.query(
                "custom_menudb",
                new String[]{"name", "memo", "content", "instance", "access_token", "image_load", "dialog", "dark_mode", "position", "streaming", "subtitle", "setting"},
                "name=?",
                new String[]{name},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            name_EditText.setText(cursor.getString(0));
            urlToContent(cursor.getString(2));
            instance = cursor.getString(3);
            access_token = cursor.getString(4);
            image_Switch.setChecked(Boolean.valueOf(cursor.getString(5)));
            dark_Switch.setChecked(Boolean.valueOf(cursor.getString(7)));
            streaming_Switch.setChecked(!Boolean.valueOf(cursor.getString(9)));
            dialog_Switch.setChecked(Boolean.valueOf(cursor.getString(6)));
            subtitle_EditText.setText(cursor.getString(10));

            cursor.moveToNext();
        }
        cursor.close();
    }

    /**
     * URL→こんてんと
     */
    private void urlToContent(String url) {
        switch (url) {
            case "/api/v1/timelines/home":
                load_url = "/api/v1/timelines/home";
                load_Button.setText(R.string.home);
                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_24dp, 0, 0, 0);
                break;
            case "/api/v1/notifications":
                load_url = "/api/v1/notifications";
                load_Button.setText(R.string.notifications);
                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_black_24dp, 0, 0, 0);
                break;
            case "/api/v1/timelines/public?local=true":
                load_url = "/api/v1/timelines/public?local=true";
                load_Button.setText(R.string.public_time_line);
                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_train_black_24dp, 0, 0, 0);
                break;
            case "/api/v1/timelines/public":
                load_url = "/api/v1/timelines/public";
                load_Button.setText(R.string.federated_timeline);
                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flight_black_24dp, 0, 0, 0);
                break;
            case "/api/v1/timelines/direct":
                load_url = "/api/v1/timelines/direct";
                load_Button.setText(R.string.direct_message);
                load_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0);
                break;
        }
    }
}
