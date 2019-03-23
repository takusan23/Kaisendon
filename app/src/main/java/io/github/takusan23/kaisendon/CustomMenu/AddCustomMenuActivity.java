package io.github.takusan23.kaisendon.CustomMenu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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
    private Button background_image_set_Button;
    private Button background_image_reset_Button;
    private ImageView background_image_ImageView;
    private Switch dialog_Switch;
    private Switch image_Switch;
    private Switch dark_Switch;
    private Switch streaming_Switch;
    private Switch quickprofile_Switch;
    private Switch tootcounter_Switch;
    private Switch custom_emoji_Switch;
    private Switch gif_Switch;
    private EditText subtitle_EditText;
    private Switch background_screen_fit_Switch;
    private EditText background_transparency;
    private FloatingActionButton fab;

    private SharedPreferences pref_setting;

    private String load_url;
    private String instance;
    private String access_token;

    //画像のURL
    private String image_url = "";

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
        quickprofile_Switch = findViewById(R.id.custom_menu_quickprofile);
        tootcounter_Switch = findViewById(R.id.custom_menu_tootcounter);
        custom_emoji_Switch = findViewById(R.id.custom_menu_custom_emoji);
        gif_Switch = findViewById(R.id.custom_menu_gif);
        subtitle_EditText = findViewById(R.id.custom_menu_subtitle_edittext_edittext);
        background_image_set_Button = findViewById(R.id.custom_background_image_button);
        background_image_reset_Button = findViewById(R.id.custom_background_image_reset_button);
        background_image_ImageView = findViewById(R.id.custom_background_image_imageview);
        background_screen_fit_Switch = findViewById(R.id.custom_menu_background_screen_fit_switch);
        background_transparency = findViewById(R.id.custom_menu_background_transoarency_edittext_edittext);

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
            //タイトル変更
            setTitle(R.string.custom_menu_update_title);
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
                //更新・新規作成
                if (!getIntent().getBooleanExtra("delete_button", false)) {
                    //新規作成
                    //SnackBer
                    Snackbar.make(v, R.string.custom_add_message, Toast.LENGTH_SHORT).setAction(R.string.register, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveSQLite();
                            //戻る
                            startActivity(new Intent(getContext(), Home.class));
                        }
                    }).show();
                } else {
                    //SnackBer
                    Snackbar.make(v, R.string.custom_menu_update, Toast.LENGTH_SHORT).setAction(R.string.update, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //更新
                            String name = getIntent().getStringExtra("name");
                            updateSQLite(name);
                            //戻る
                            startActivity(new Intent(getContext(), Home.class));
                        }
                    }).show();
                }
            }
        });

        //背景画像
        background_setting();
    }

    /**
     * 画像受け取り
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (resultData.getData() != null) {
                Uri selectedImage = resultData.getData();
                //完全パス取得
                String get_Path = getPath(selectedImage);
                String image_Path = "file:\\\\" + get_Path;
                //置き換え
                String final_Path = image_Path.replaceAll("\\\\", "/");
                image_url = final_Path;
                //いれておく？
                background_image_set_Button.setText(image_url);
                //URI画像を入れる
                Glide.with(getContext())
                        .load(get_Path)
                        .into(background_image_ImageView);
            }
        }
    }

    /**
     * 背景画像のボタンクリックイベントとか
     */
    private void background_setting() {
        //画像選択画面に飛ばす
        background_image_set_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ストレージ読み込みの権限があるか確認
                //許可してないときは許可を求める
                if (ContextCompat.checkSelfPermission(AddCustomMenuActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(AddCustomMenuActivity.this)
                            .setTitle(getString(R.string.permission_dialog_titile))
                            .setMessage(getString(R.string.permission_dialog_message))
                            .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //権限をリクエストする
                                    ActivityCompat.requestPermissions(AddCustomMenuActivity.this,
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            1000);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                } else {
                    //画像選択
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                    //onActivityResultで処理
                }
            }
        });

        //リセットボタン
        background_image_reset_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //リンクをリセット
                image_url = "";
                //URI画像を入れる
                Glide.with(getContext()).load("").into(background_image_ImageView);
                background_image_set_Button.setText(R.string.custom_setting_background_image);
            }
        });

    }


    /**
     * SQLiteに保存する
     */
    private void saveSQLite() {
        ContentValues values = new ContentValues();
        //JSON化
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name_EditText.getText().toString());
            jsonObject.put("memo", "");
            jsonObject.put("content", load_url);
            jsonObject.put("instance", instance);
            jsonObject.put("access_token", access_token);
            jsonObject.put("image_load", String.valueOf(image_Switch.isChecked()));
            jsonObject.put("dialog", String.valueOf(dialog_Switch.isChecked()));
            jsonObject.put("dark_mode", String.valueOf(dark_Switch.isChecked()));
            jsonObject.put("position", "");
            jsonObject.put("streaming", String.valueOf(!streaming_Switch.isChecked())); //反転させてONのときStereaming有効に
            jsonObject.put("subtitle", subtitle_EditText.getText().toString());
            jsonObject.put("image_url", image_url);
            jsonObject.put("background_transparency", background_transparency.getText().toString());
            jsonObject.put("background_screen_fit", String.valueOf(background_screen_fit_Switch.isChecked()));
            jsonObject.put("quick_profile",String.valueOf(quickprofile_Switch.isChecked()));
            jsonObject.put("toot_counter",String.valueOf(tootcounter_Switch.isChecked()));
            jsonObject.put("custom_emoji",String.valueOf(custom_emoji_Switch.isChecked()));
            jsonObject.put("gif",String.valueOf(gif_Switch.isChecked()));
            jsonObject.put("setting", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        values.put("name", name_EditText.getText().toString());
        values.put("setting", jsonObject.toString());
        db.insert("custom_menudb", null, values);
    }

    /**
     * SQLite更新
     */
    private void updateSQLite(String name) {
        ContentValues values = new ContentValues();
        //JSON化
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name_EditText.getText().toString());
            jsonObject.put("memo", "");
            jsonObject.put("content", load_url);
            jsonObject.put("instance", instance);
            jsonObject.put("access_token", access_token);
            jsonObject.put("image_load", String.valueOf(image_Switch.isChecked()));
            jsonObject.put("dialog", String.valueOf(dialog_Switch.isChecked()));
            jsonObject.put("dark_mode", String.valueOf(dark_Switch.isChecked()));
            jsonObject.put("position", "");
            jsonObject.put("streaming", String.valueOf(!streaming_Switch.isChecked())); //反転させてONのときStereaming有効に
            jsonObject.put("subtitle", subtitle_EditText.getText().toString());
            jsonObject.put("image_url", image_url);
            jsonObject.put("background_transparency", background_transparency.getText().toString());
            jsonObject.put("background_screen_fit", String.valueOf(background_screen_fit_Switch.isChecked()));
            jsonObject.put("quick_profile",String.valueOf(quickprofile_Switch.isChecked()));
            jsonObject.put("toot_counter",String.valueOf(tootcounter_Switch.isChecked()));
            jsonObject.put("custom_emoji",String.valueOf(custom_emoji_Switch.isChecked()));
            jsonObject.put("gif",String.valueOf(gif_Switch.isChecked()));
            jsonObject.put("setting", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        values.put("name", name_EditText.getText().toString());
        values.put("setting", jsonObject.toString());
        db.update("custom_menudb", values, "name=?", new String[]{name});
    }


    //ListViewにあった場合は
    //読み込む

    /**
     * 読み込む
     */
    private void loadSQLite(String name) {
        Cursor cursor = db.query(
                "custom_menudb",
                new String[]{"setting"},
                "name=?",
                new String[]{name},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            try {
                JSONObject jsonObject = new JSONObject(cursor.getString(0));
                name_EditText.setText(jsonObject.getString("name"));
                urlToContent(jsonObject.getString("content"));
                instance = jsonObject.getString("instance");
                access_token = jsonObject.getString("access_token");
                image_Switch.setChecked(Boolean.valueOf(jsonObject.getString("image_load")));
                dark_Switch.setChecked(Boolean.valueOf(jsonObject.getString("dark_mode")));
                streaming_Switch.setChecked(!Boolean.valueOf(jsonObject.getString("streaming")));
                dialog_Switch.setChecked(Boolean.valueOf(jsonObject.getString("dialog")));
                subtitle_EditText.setText(jsonObject.getString("subtitle"));
                background_image_set_Button.setText(jsonObject.getString("image_url"));
                image_url = jsonObject.getString("image_url");
                background_image_set_Button.setText(image_url);
                Glide.with(getContext()).load(jsonObject.getString("image_url")).into(background_image_ImageView);
                background_transparency.setText(jsonObject.getString("background_transparency"));
                background_screen_fit_Switch.setChecked(Boolean.valueOf(jsonObject.getString("background_screen_fit")));
                quickprofile_Switch.setChecked(Boolean.valueOf(jsonObject.getString("quick_profile")));
                tootcounter_Switch.setChecked(Boolean.valueOf(jsonObject.getString("toot_counter")));
                custom_emoji_Switch.setChecked(Boolean.valueOf(jsonObject.getString("custom_emoji")));
                gif_Switch.setChecked(Boolean.valueOf(jsonObject.getString("gif")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();
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

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

}
