package com.takusan_23.kaisendon;

import android.accounts.Account;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountInfoUpdateActivity extends AppCompatActivity {

    String AccessToken, Instance;
    String display_name, note;
    String fields_name_1 = null, fields_name_2 = null, fields_name_3 = null, fields_name_4 = null;
    String fields_value_1 = null, fields_value_2 = null, fields_value_3 = null, fields_value_4 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info_update);
/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
*/

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

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
        Snackbar snackbar = Snackbar.make(view, getString(R.string.loading_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(AccountInfoUpdateActivity.this);
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar.show();


        //find
        TextView displayname_textview = findViewById(R.id.account_update_display_name_textview);
        EditText displayname_edittext = findViewById(R.id.account_update_display_name_edittext);
        TextView note_textview = findViewById(R.id.account_update_note_name_textview);
        TextView note_edittext = findViewById(R.id.account_update_note_name_edittext);

/*
        TextView fields_attributes_1_textview = findViewById(R.id.account_update_fields_attributes_1_textview);
        TextView fields_attributes_2_textview = findViewById(R.id.account_update_fields_attributes_2_textview);
        TextView fields_attributes_3_textview = findViewById(R.id.account_update_fields_attributes_3_textview);
        TextView fields_attributes_4_textview = findViewById(R.id.account_update_fields_attributes_4_textview);
*/

        EditText fields_attributes_1_edittext_name = findViewById(R.id.account_update_fields_attributes_1_edittext_name);
        EditText fields_attributes_2_edittext_name = findViewById(R.id.account_update_fields_attributes_2_edittext_name);
        EditText fields_attributes_3_edittext_name = findViewById(R.id.account_update_fields_attributes_3_edittext_name);
        EditText fields_attributes_4_edittext_name = findViewById(R.id.account_update_fields_attributes_4_edittext_name);

        EditText fields_attributes_1_edittext_value = findViewById(R.id.account_update_fields_attributes_1_edittext_value);
        EditText fields_attributes_2_edittext_value = findViewById(R.id.account_update_fields_attributes_2_edittext_value);
        EditText fields_attributes_3_edittext_value = findViewById(R.id.account_update_fields_attributes_3_edittext_value);
        EditText fields_attributes_4_edittext_value = findViewById(R.id.account_update_fields_attributes_4_edittext_value);

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


                    //補足情報
                    JSONArray fields_array = jsonObject.getJSONArray("fields");
                    System.out.println("数" + String.valueOf(fields_array.length()));
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

                    System.out.println(fields_name_1 + fields_value_1);

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

                            try {
                                getSupportActionBar().setSubtitle(display_name + " ( @" + jsonObject.getString("acct") + " / " + Instance + " )");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    snackbar.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //情報を更新する
        //全部入力されてるか確認
        if (displayname_edittext.getText().

                toString() != null && note_edittext.getText().

                toString() != null)

        {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, R.string.upload_info, Snackbar.LENGTH_LONG)
                            .setAction(R.string.update, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //くるくる
                                    View view = findViewById(android.R.id.content);
                                    Snackbar snackbar = Snackbar.make(view, getString(R.string.upload_user_info) + "\r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE);
                                    ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                                    //SnackBerを複数行対応させる
                                    TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
                                    snackBer_textView.setMaxLines(2);
                                    //複数行対応させたおかげでずれたので修正
                                    ProgressBar progressBar = new ProgressBar(AccountInfoUpdateActivity.this);
                                    LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    progressBer_layoutParams.gravity = Gravity.CENTER;
                                    progressBar.setLayoutParams(progressBer_layoutParams);
                                    snackBer_viewGrop.addView(progressBar, 0);
                                    snackbar.show();

                                    //編集前の内容にする！！
                                    //パラメータを設定
                                    String url = "https://" + Instance + "/api/v1/accounts/update_credentials/?access_token=" + AccessToken;
                                    HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
                                    String final_url = builder.build().toString();

                                    FormBody.Builder form = new FormBody.Builder();
                                    form.add("display_name", displayname_edittext.getText().toString());
                                    form.add("note", note_edittext.getText().toString());

                                    if (fields_attributes_1_edittext_name.getText().toString() != null && fields_attributes_1_edittext_value.getText().toString() != null) {
                                        form.add("fields_attributes[0][name]", fields_attributes_1_edittext_name.getText().toString());
                                        form.add("fields_attributes[0][value]", fields_attributes_1_edittext_value.getText().toString());
                                    }
                                    if (fields_attributes_2_edittext_name.getText().toString() != null && fields_attributes_2_edittext_value.getText().toString() != null) {
                                        form.add("fields_attributes[1][name]", fields_attributes_2_edittext_name.getText().toString());
                                        form.add("fields_attributes[1][value]", fields_attributes_2_edittext_value.getText().toString());
                                    }
                                    if (fields_attributes_3_edittext_name.getText().toString() != null && fields_attributes_3_edittext_value.getText().toString() != null) {
                                        form.add("fields_attributes[2][name]", fields_attributes_3_edittext_name.getText().toString());
                                        form.add("fields_attributes[2][value]", fields_attributes_3_edittext_value.getText().toString());
                                    }
                                    if (fields_attributes_4_edittext_name.getText().toString() != null && fields_attributes_4_edittext_value.getText().toString() != null) {
                                        form.add("fields_attributes[3][name]", fields_attributes_4_edittext_name.getText().toString());
                                        form.add("fields_attributes[3][value]", fields_attributes_4_edittext_value.getText().toString());
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
                                                        snackbar.dismiss();
                                                    }
                                                });
                                            }
                                        }
                                    });

                                }
                            }).show();
                }
            });
        } else

        {
            Toast.makeText(AccountInfoUpdateActivity.this, R.string.fillItem, Toast.LENGTH_SHORT).show();
        }


    }

    private void setTextNullChack(EditText id, String string) {
        if (string != null) {
            id.setText(Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT));
        }
    }

}
