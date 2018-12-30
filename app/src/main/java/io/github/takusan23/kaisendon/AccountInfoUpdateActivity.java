package io.github.takusan23.kaisendon;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountInfoUpdateActivity extends AppCompatActivity {

    String AccessToken, Instance;
    String display_name, note;
    String fields_name_1 = null, fields_name_2 = null, fields_name_3 = null, fields_name_4 = null;
    String fields_value_1 = null, fields_value_2 = null, fields_value_3 = null, fields_value_4 = null;
    String avatar_url, header_url;
    String image_url, image_uri, image_name;
    String header_post_url = null, header_post_uri = null, header_post_name = null;
    String avatar_post_path = null, avatar_post_uri = null, avatar_post_name = null;
    String avatar_extn = null, header_extn = null;
    File avatar_file = null, header_file = null;
    boolean avatar = false, header = false;

    EditText fields_attributes_1_edittext_name;
    EditText fields_attributes_2_edittext_name;
    EditText fields_attributes_3_edittext_name;
    EditText fields_attributes_4_edittext_name;
    EditText fields_attributes_1_edittext_value;
    EditText fields_attributes_2_edittext_value;
    EditText fields_attributes_3_edittext_value;
    EditText fields_attributes_4_edittext_value;

    TextView avater_image_post_message_textview;
    ImageView avater_image_imageview;
    ImageView header_image_imageview;
    Button avatar_button;
    Button header_button;

    TextView displayname_textview;
    EditText displayname_edittext;
    TextView note_textview;
    TextView note_edittext;

    Snackbar snackbar;
    Snackbar snackbar_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        if (dark_mode) {
            setTheme(R.style.Theme_AppCompat_DayNight);
        }


        //OLEDように真っ暗のテーマも使えるように
        //この画面用にNoActionBerなダークテーマを指定している
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            setTheme(R.style.OLED_Theme_Home);
        }


        setContentView(R.layout.activity_account_info_update);
/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
*/


        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");
        }


        //くるくる
        View view = findViewById(android.R.id.content);
        final Snackbar[] snackbar = {Snackbar.make(view, getString(R.string.loading_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE)};
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar[0].getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(AccountInfoUpdateActivity.this);
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar[0].show();


        //find
        displayname_textview = findViewById(R.id.account_update_display_name_textview);
        displayname_edittext = findViewById(R.id.account_update_display_name_edittext);
        note_textview = findViewById(R.id.account_update_note_name_textview);
        note_edittext = findViewById(R.id.account_update_note_name_edittext);

/*
        TextView fields_attributes_1_textview = findViewById(R.id.account_update_fields_attributes_1_textview);
        TextView fields_attributes_2_textview = findViewById(R.id.account_update_fields_attributes_2_textview);
        TextView fields_attributes_3_textview = findViewById(R.id.account_update_fields_attributes_3_textview);
        TextView fields_attributes_4_textview = findViewById(R.id.account_update_fields_attributes_4_textview);
*/

        fields_attributes_1_edittext_name = findViewById(R.id.account_update_fields_attributes_1_edittext_name);
        fields_attributes_2_edittext_name = findViewById(R.id.account_update_fields_attributes_2_edittext_name);
        fields_attributes_3_edittext_name = findViewById(R.id.account_update_fields_attributes_3_edittext_name);
        fields_attributes_4_edittext_name = findViewById(R.id.account_update_fields_attributes_4_edittext_name);

        fields_attributes_1_edittext_value = findViewById(R.id.account_update_fields_attributes_1_edittext_value);
        fields_attributes_2_edittext_value = findViewById(R.id.account_update_fields_attributes_2_edittext_value);
        fields_attributes_3_edittext_value = findViewById(R.id.account_update_fields_attributes_3_edittext_value);
        fields_attributes_4_edittext_value = findViewById(R.id.account_update_fields_attributes_4_edittext_value);

        avater_image_post_message_textview = findViewById(R.id.account_update_avatar_textview_title);
        avater_image_imageview = findViewById(R.id.account_update_avatar_imageview);
        header_image_imageview = findViewById(R.id.account_update_header_imageview);
        avatar_button = findViewById(R.id.account_update_avatar_button);
        header_button = findViewById(R.id.account_update_header_button);


        //説明文
        avater_image_post_message_textview.setText(getString(R.string.upload_avater_header) + "\r\n" + getString(R.string.image_upload_storage_permisson));


        //EditTextにヒントを入れる
        String label = getString(R.string.label);
        String context = getString(R.string.content);
        fields_attributes_1_edittext_name.setHint(label + "1");
        fields_attributes_2_edittext_name.setHint(label + "2");
        fields_attributes_3_edittext_name.setHint(label + "3");
        fields_attributes_4_edittext_name.setHint(label + "4");

        fields_attributes_1_edittext_value.setHint(context + "1");
        fields_attributes_2_edittext_value.setHint(context + "2");
        fields_attributes_3_edittext_value.setHint(context + "3");
        fields_attributes_4_edittext_value.setHint(context + "4");

        //タイトル
        setTitle(R.string.update_userinfo_title);


        //編集前の内容にする！！
        //パラメータを設定
        String url = "https://" + Instance + "/api/v1/accounts/verify_credentials/?access_token=" + AccessToken;
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        String final_url = builder.build().toString();

        //作成
        Request request = new Request.Builder()
                .url(final_url)
                .get()
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responce_string = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responce_string);
                    display_name = jsonObject.getString("display_name");
                    note = jsonObject.getString("note");
                    avatar_url = jsonObject.getString("avatar");
                    header_url = jsonObject.getString("header");

                    //補足情報
                    JSONArray fields_array = jsonObject.getJSONArray("fields");
                    //System.out.println("数" + String.valueOf(fields_array.length()));
                    if (0 < fields_array.length()) {
                        JSONObject fields_object = fields_array.getJSONObject(0);
                        fields_name_1 = fields_object.getString("name");
                        fields_value_1 = fields_object.getString("value");
                    }
                    if (1 < fields_array.length()) {
                        JSONObject fields_object = fields_array.getJSONObject(1);
                        fields_name_2 = fields_object.getString("name");
                        fields_value_2 = fields_object.getString("value");
                    }
                    if (2 < fields_array.length()) {
                        JSONObject fields_object = fields_array.getJSONObject(2);
                        fields_name_3 = fields_object.getString("name");
                        fields_value_3 = fields_object.getString("value");
                    }
                    if (3 < fields_array.length()) {
                        JSONObject fields_object = fields_array.getJSONObject(3);
                        fields_name_4 = fields_object.getString("name");
                        fields_value_4 = fields_object.getString("value");
                    }

                    //System.out.println(fields_name_1 + fields_value_1);

                    //UI更新
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayname_edittext.setText(display_name);
                            note_edittext.setText(Html.fromHtml(note, Html.FROM_HTML_MODE_COMPACT));

                            setTextNullChack(fields_attributes_1_edittext_name, fields_name_1);
                            setTextNullChack(fields_attributes_2_edittext_name, fields_name_2);
                            setTextNullChack(fields_attributes_3_edittext_name, fields_name_3);
                            setTextNullChack(fields_attributes_4_edittext_name, fields_name_4);

                            setTextNullChack(fields_attributes_1_edittext_value, fields_value_1);
                            setTextNullChack(fields_attributes_2_edittext_value, fields_value_2);
                            setTextNullChack(fields_attributes_3_edittext_value, fields_value_3);
                            setTextNullChack(fields_attributes_4_edittext_value, fields_value_4);

                            Glide.with(AccountInfoUpdateActivity.this)
                                    .load(avatar_url)
                                    .into(avater_image_imageview);
                            Glide.with(AccountInfoUpdateActivity.this)
                                    .load(header_url)
                                    .into(header_image_imageview);

                            try {
                                getSupportActionBar().setSubtitle(display_name + " ( @" + jsonObject.getString("acct") + " / " + Instance + " )");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    snackbar[0].dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        //画像のアップロード
        //権限があるか確認
        avatar_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickMediaSelect();
                avatar = true;
            }
        });
        header_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickMediaSelect();
                header = true;
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //情報を更新する
        //全部入力されてるか確認
        if (displayname_edittext.getText().toString() != null && note_edittext.getText().toString() != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", false);
                    if (replace_snackber) {
                        snackbar[0] = Snackbar.make(view, R.string.upload_info, Snackbar.LENGTH_LONG);
                        snackbar[0].setAction(R.string.update, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //くるくる
                                View view = findViewById(android.R.id.content);
                                snackbar_loading = Snackbar.make(view, getString(R.string.upload_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE);
                                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar_loading.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                                //SnackBerを複数行対応させる
                                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
                                snackBer_textView.setMaxLines(2);
                                //複数行対応させたおかげでずれたので修正
                                ProgressBar progressBar = new ProgressBar(AccountInfoUpdateActivity.this);
                                LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                progressBer_layoutParams.gravity = Gravity.CENTER;
                                progressBar.setLayoutParams(progressBer_layoutParams);
                                snackBer_viewGrop.addView(progressBar, 0);
                                snackbar_loading.show();

                                uploadProfile();

                            }
                        }).show();
                    } else {
                        //ダイアログ
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AccountInfoUpdateActivity.this);
                        alertDialog.setTitle(R.string.confirmation);
                        alertDialog.setMessage(R.string.upload_info);
                        alertDialog.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                View view = findViewById(android.R.id.content);
                                snackbar_loading = Snackbar.make(view, getString(R.string.upload_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE);
                                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar_loading.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                                //SnackBerを複数行対応させる
                                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
                                snackBer_textView.setMaxLines(2);
                                //複数行対応させたおかげでずれたので修正
                                ProgressBar progressBar = new ProgressBar(AccountInfoUpdateActivity.this);
                                LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                progressBer_layoutParams.gravity = Gravity.CENTER;
                                progressBar.setLayoutParams(progressBer_layoutParams);
                                snackBer_viewGrop.addView(progressBar, 0);
                                snackbar_loading.show();

                                uploadProfile();
                            }

                        });
                        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.create().show();
                    }


                }
            });
        } else {
            Toast.makeText(AccountInfoUpdateActivity.this, R.string.fillItem, Toast.LENGTH_SHORT).show();
        }


    }


    //画像をアップロードすつ
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView avater_image_imageview = findViewById(R.id.account_update_avatar_imageview);
        ImageView header_image_imageview = findViewById(R.id.account_update_header_imageview);

/*
        boolean avatar = data.getBooleanExtra("avatar", false);
        boolean header = data.getBooleanExtra("header", false);
*/

        //System.out.println("あばたー : " + String.valueOf(avatar));
        //System.out.println("へっだー : " + String.valueOf(header));

        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();

                String filePath = getPath(selectedImage);
                String file_extn = filePath.substring(filePath.lastIndexOf(".") + 1);
                File file = new File(filePath);
                String finalPath = "file:\\\\" + filePath;

                //image_name_tv.setText(filePath);

                if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
                    //System.out.println("パス : " + finalPath.replaceAll("\\\\", "/"));
                    //System.out.println("拡張子 : " + file_extn);
                    //System.out.println("ファイル名 : " + file.getName());

                    if (avatar) {
                        avater_image_imageview.setImageURI(selectedImage);
                        avatar_file = file;
                        avatar_post_name = file.getName();
                        avatar_extn = file_extn;
                    } else {
                        header_image_imageview.setImageURI(selectedImage);
                        header_file = file;
                        header_post_name = file.getName();
                        header_extn = file_extn;
                    }
                }
            }

        header = false;
        avatar = false;
    }


    private void setTextNullChack(EditText id, String string) {
        if (string != null) {
            id.setText(Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT));
        }
    }

    private void clickMediaSelect() {
        //ストレージ読み込みの権限があるか確認
        //許可してないときは許可を求める
        int REQUEST_PERMISSION = 1000;
        if (ContextCompat.checkSelfPermission(AccountInfoUpdateActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(AccountInfoUpdateActivity.this)
                    .setTitle(getString(R.string.permission_dialog_titile))
                    .setMessage(getString(R.string.image_upload_storage_permisson))
                    .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //権限をリクエストする
                            requestPermissions(
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSION);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        } else {

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        }

    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

    public void uploadProfile() {
        //編集前の内容にする！！
        //パラメータを設定
        String url = "https://" + Instance + "/api/v1/accounts/update_credentials/?access_token=" + AccessToken;
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        String final_url = builder.build().toString();

        MultipartBody.Builder form = new MultipartBody.Builder();
        form.addFormDataPart("display_name", displayname_edittext.getText().toString());
        form.addFormDataPart("note", note_edittext.getText().toString());

        if (fields_attributes_1_edittext_name.getText().toString() != null && fields_attributes_1_edittext_value.getText().toString() != null) {
            form.addFormDataPart("fields_attributes[0][name]", fields_attributes_1_edittext_name.getText().toString());
            form.addFormDataPart("fields_attributes[0][value]", fields_attributes_1_edittext_value.getText().toString());
        }
        if (fields_attributes_2_edittext_name.getText().toString() != null && fields_attributes_2_edittext_value.getText().toString() != null) {
            form.addFormDataPart("fields_attributes[1][name]", fields_attributes_2_edittext_name.getText().toString());
            form.addFormDataPart("fields_attributes[1][value]", fields_attributes_2_edittext_value.getText().toString());
        }
        if (fields_attributes_3_edittext_name.getText().toString() != null && fields_attributes_3_edittext_value.getText().toString() != null) {
            form.addFormDataPart("fields_attributes[2][name]", fields_attributes_3_edittext_name.getText().toString());
            form.addFormDataPart("fields_attributes[2][value]", fields_attributes_3_edittext_value.getText().toString());
        }
        if (fields_attributes_4_edittext_name.getText().toString() != null && fields_attributes_4_edittext_value.getText().toString() != null) {
            form.addFormDataPart("fields_attributes[3][name]", fields_attributes_4_edittext_name.getText().toString());
            form.addFormDataPart("fields_attributes[3][value]", fields_attributes_4_edittext_value.getText().toString());
        }

        //画像を投げる
        if (avatar_file != null && avatar_post_name != null && avatar_extn != null) {
            form.addFormDataPart("avatar", avatar_file.getName(), RequestBody.create(MediaType.parse("image/" + avatar_extn), avatar_file));
        }
        if (header_file != null && header_post_name != null && header_extn != null) {
            form.addFormDataPart("header", header_file.getName(), RequestBody.create(MediaType.parse("image/" + header_extn), header_file));
        }

        //ぱらめーたー
        RequestBody requestBody = form.build();

        //作成
        Request request = new Request.Builder()
                .url(final_url)
                .patch(requestBody)
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AccountInfoUpdateActivity.this, R.string.successful, Toast.LENGTH_SHORT).show();
                            snackbar_loading.dismiss();
                        }
                    });
                }
            }
        });

    }


}
