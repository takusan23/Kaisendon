package io.github.takusan23.Kaisendon.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import io.github.takusan23.Kaisendon.CustomMenu.Dialog.CalenderDialog;
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.Kaisendon.Preference_ApplicationContext;
import io.github.takusan23.Kaisendon.R;
import io.github.takusan23.Kaisendon.SnackberProgress;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.github.takusan23.Kaisendon.Preference_ApplicationContext.getContext;

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

    private EditText fields_attributes_1_edittext_name;
    private EditText fields_attributes_2_edittext_name;
    private EditText fields_attributes_3_edittext_name;
    private EditText fields_attributes_4_edittext_name;
    private EditText fields_attributes_1_edittext_value;
    private EditText fields_attributes_2_edittext_value;
    private EditText fields_attributes_3_edittext_value;
    private EditText fields_attributes_4_edittext_value;

    private TextView avater_image_post_message_textview;
    private ImageView avater_image_imageview;
    private ImageView header_image_imageview;
    private Button avatar_button;
    private Button header_button;
    private FloatingActionButton fab;

    private TextView displayname_textview;
    private EditText displayname_edittext;
    private TextView note_textview;
    private TextView note_edittext;
    private Snackbar snackbar;
    private Snackbar snackbar_loading;
    private SharedPreferences pref_setting;

    private EditText place_EditText;
    public static Button birthday_Button;
    private Switch cat_Switch;

    private String misskey_avatar_id;
    private String misskey_banner_id;

    private Uri avatar_Uri;
    private Uri header_Uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //ダークテーマに切り替える機能
        DarkModeSupport darkModeSupport = new DarkModeSupport(this);
        darkModeSupport.setActivityTheme(this);

        if (getIntent().getBooleanExtra("Misskey", false)) {
            setContentView(R.layout.misskey_account_update_layout);
        } else {
            setContentView(R.layout.activity_account_info_update);
        }

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
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar[0].getView().findViewById(R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(AccountInfoUpdateActivity.this);
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar[0].show();

        //Misskey
        if (getIntent().getBooleanExtra("Misskey", false)) {

            displayname_edittext = findViewById(R.id.account_update_display_name_edittext);
            note_edittext = findViewById(R.id.account_update_note_name_edittext);
            avater_image_imageview = findViewById(R.id.account_update_avatar_imageview);
            header_image_imageview = findViewById(R.id.account_update_header_imageview);
            avatar_button = findViewById(R.id.account_update_avatar_button);
            header_button = findViewById(R.id.account_update_header_button);
            place_EditText = findViewById(R.id.place_edittext);
            birthday_Button = findViewById(R.id.birthday_button);
            cat_Switch = findViewById(R.id.cat_switch);
            fab = findViewById(R.id.fab);
            //タイトル
            setTitle(R.string.update_userinfo_title);

            //Misskey Account 取得
            getMisskeyAccount();

            //プロフィール更新用
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateMisskeyProfile();
                }
            });

            birthday_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CalenderDialog calenderDialog = new CalenderDialog();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "birthday");
                    calenderDialog.setArguments(bundle);
                    calenderDialog.show(getSupportFragmentManager(), "calender_dialog");
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

        } else {

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


            fab = (FloatingActionButton) findViewById(R.id.fab);

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
                                    ViewGroup snackBer_viewGrop = (ViewGroup) snackbar_loading.getView().findViewById(R.id.snackbar_text).getParent();
                                    //SnackBerを複数行対応させる
                                    TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
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
                                    ViewGroup snackBer_viewGrop = (ViewGroup) snackbar_loading.getView().findViewById(R.id.snackbar_text).getParent();
                                    //SnackBerを複数行対応させる
                                    TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
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
    }

    //画像をアップロードすつ
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView avater_image_imageview = findViewById(R.id.account_update_avatar_imageview);
        ImageView header_image_imageview = findViewById(R.id.account_update_header_imageview);
        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();

                String filePath = getFileNameUri(selectedImage);
                String file_extn = filePath.substring(filePath.lastIndexOf(".") + 1);
                if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
                    if (avatar) {
                        avater_image_imageview.setImageURI(selectedImage);
                        avatar_Uri = selectedImage;
                        avatar_post_name = getFileNameUri(selectedImage);
                        avatar_extn = file_extn;
                        //Misskey POST
                        if (getIntent().getBooleanExtra("Misskey", false)) {
                            postMisskeyPhotoPOST(avatar_extn, selectedImage, false);
                        }
                    } else {
                        header_image_imageview.setImageURI(selectedImage);
                        header_Uri = selectedImage;
                        //header_file = file;
                        header_post_name = getFileNameUri(selectedImage);
                        header_extn = file_extn;
                        //Misskey POST
                        if (getIntent().getBooleanExtra("Misskey", false)) {
                            postMisskeyPhotoPOST(header_extn, selectedImage, true);
                        }
                    }
                }
            }

        header = false;
        avatar = false;
    }


    /**
     * Uri→FileName
     */
    private String getFileNameUri(Uri uri) {
        String file_name = null;
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                file_name = cursor.getString(0);
            }
        }
        return file_name;
    }


    /**
     * Misskeyアカウント情報
     */
    private void getMisskeyAccount() {
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        String url = "https://" + instance + "/api/i";
        SnackberProgress.showProgressSnackber(displayname_edittext, AccountInfoUpdateActivity.this, getString(R.string.loading) + "\n" + url);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("i", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        //作成
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                if (!response.isSuccessful()) {
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AccountInfoUpdateActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(response_string);
                                displayname_edittext.setText(jsonObject.getString("name"));
                                note_edittext.setText(jsonObject.getString("description"));
                                JSONObject profile = jsonObject.getJSONObject("profile");
                                //null check
                                if (!profile.isNull("birthday")) {
                                    birthday_Button.setText(profile.getString("birthday"));
                                }
                                if (!profile.isNull("location")) {
                                    place_EditText.setText(profile.getString("location"));
                                }
                                cat_Switch.setChecked(Boolean.valueOf(jsonObject.getString("isCat")));
                                //画像
                                Glide.with(AccountInfoUpdateActivity.this).load(jsonObject.getString("avatarUrl")).into(avater_image_imageview);
                                Glide.with(AccountInfoUpdateActivity.this).load(jsonObject.getString("bannerUrl")).into(header_image_imageview);
                                //SubTitle
                                getSupportActionBar().setSubtitle(jsonObject.getString("name") + " ( @" + jsonObject.getString("username") + " / " + instance + " )");
                                //avatar / banner
                                misskey_avatar_id = jsonObject.getString("avatarId");
                                misskey_banner_id = jsonObject.getString("bannerId");

                                SnackberProgress.closeProgressSnackber();

                            } catch (JSONException e) {

                            }
                        }
                    });

                }
            }
        });
    }

    /**
     * Misskey Profile Update
     */
    private void updateMisskeyProfile() {
        //必須項目が埋まってるかチェック
        if (displayname_edittext.getText().toString() != null && note_edittext.getText().toString() != null) {
            Snackbar.make(displayname_edittext, R.string.upload_info, Snackbar.LENGTH_LONG).setAction(getString(R.string.update), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //画像アップロードは受け取りのところでやった
                    postMisskeyProfile();
                }
            }).show();
        }
    }

    /**
     * Misskey プロフィール更新
     */
    private void postMisskeyProfile() {
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        String url = "https://" + instance + "/api/i/update";
        //くるくる
        SnackberProgress.showProgressSnackber(displayname_edittext, AccountInfoUpdateActivity.this, getString(R.string.loading) + "\n" + url);
        //ぱらめーたー
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("i", token);
            jsonObject.put("name", displayname_edittext.getText().toString());
            jsonObject.put("description", note_edittext.getText().toString());
            jsonObject.put("location", place_EditText.getText().toString());
            jsonObject.put("bannerId", misskey_banner_id);
            jsonObject.put("avatarId", misskey_avatar_id);
            jsonObject.put("birthday", birthday_Button.getText().toString());
            jsonObject.put("isCat", cat_Switch.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        //作成
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                System.out.println(response_string);
                if (!response.isSuccessful()) {
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AccountInfoUpdateActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (!response.isSuccessful()) {
                        //失敗
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccountInfoUpdateActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccountInfoUpdateActivity.this, R.string.successful, Toast.LENGTH_SHORT).show();
                                SnackberProgress.closeProgressSnackber();
                            }
                        });
                    }
                }

            }
        });
    }

    /**
     * Misskey avatarUrl bannerUrl POST
     *
     * @param banner avatarUrlはfalse、bannerはtrue
     */
    private void postMisskeyPhotoPOST(String file_extn_post, Uri uri, boolean banner) {
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        String url = "https://" + instance + "/api/drive/files/create";
        //Uri → Bitmap → Byte
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(getImageType(file_extn_post), 100, baos);
        byte[] imageBytes = baos.toByteArray();
        //くるくる
        SnackberProgress.showProgressSnackber(displayname_edittext, AccountInfoUpdateActivity.this, getString(R.string.loading) + "\n" + url);
        //ぱらめーたー
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", getFileNameUri(uri), RequestBody.create(MediaType.parse("image/" + file_extn_post), imageBytes))
                .addFormDataPart("i", token)
                .addFormDataPart("force", "true")
                .build();
        //作成
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                //System.out.println(response_string);
                if (!response.isSuccessful()) {
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AccountInfoUpdateActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response_string);
                        String media_id_long = jsonObject.getString("id");
                        if (!banner) {
                            //avatar
                            misskey_avatar_id = media_id_long;
                        } else {
                            //banner
                            misskey_banner_id = media_id_long;
                        }
                        SnackberProgress.closeProgressSnackber();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
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

    /**
     * context://→file://へ変換する
     */
    public String getPath(Uri uri) {
        //uri.getLastPathSegment();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);
        cursor.close();
        return imagePath;
    }


    /**
     * PNG / JPEG
     */
    private Bitmap.CompressFormat getImageType(String extn) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        switch (extn) {
            case "jpg":
                format = Bitmap.CompressFormat.JPEG;
                break;
            case "jpeg":
                format = Bitmap.CompressFormat.JPEG;
                break;
            case "png":
                format = Bitmap.CompressFormat.PNG;
                break;
        }
        return format;
    }


    public void uploadProfile() {
        //非同期処理
        new Thread(new Runnable() {
            @Override
            public void run() {
                //編集前の内容にする！！
                //パラメータを設定
                String url = "https://" + Instance + "/api/v1/accounts/update_credentials/?access_token=" + AccessToken;
                HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
                String final_url = builder.build().toString();

                MultipartBody.Builder form = new MultipartBody.Builder();
                form.setType(MultipartBody.FORM);
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
                try {
                    if (avatar_Uri != null) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), avatar_Uri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(getImageType(avatar_extn), 100, baos);
                        byte[] imageBytes = baos.toByteArray();
                        form.addFormDataPart("avatar", getFileNameUri(avatar_Uri), RequestBody.create(MediaType.parse("image/" + avatar_extn), imageBytes));
                    }
                    if (header_Uri != null) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), header_Uri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(getImageType(header_extn), 100, baos);
                        byte[] imageBytes = baos.toByteArray();
                        form.addFormDataPart("header", getFileNameUri(header_Uri), RequestBody.create(MediaType.parse("image/" + header_extn), imageBytes));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccountInfoUpdateActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                snackbar_loading.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AccountInfoUpdateActivity.this, getString(R.string.successful), Toast.LENGTH_SHORT).show();
                                    snackbar_loading.dismiss();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AccountInfoUpdateActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                                    snackbar_loading.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }).start();

    }


}
