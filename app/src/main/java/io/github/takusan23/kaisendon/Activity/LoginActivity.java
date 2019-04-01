package io.github.takusan23.kaisendon.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.SnackberProgress;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences pref_setting;

    private String client_id = null;
    private String client_secret = null;
    private String client_name = null;
    private String redirect_url = null;
    private String callback_code = null;

    private EditText client_name_EditText;
    private TextInputLayout client_name_TextInputEditText;
    private EditText instance_name_EditText;
    private Switch access_token_Switch;
    private Button login_Button;
    private LinearLayout access_token_LinearLayout;

    private boolean accesstoken_imput = false;

    //マルチアカウント
    int multi_account_count;

    int swich;

    private ProgressDialog dialog;

    //Misskeyモード
    private Switch misskey_login_Switch;
    private String misskey_code;
    private SharedPreferences.Editor editor;
    //強制保存モード
    private boolean error_mode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //設定プリファレンス
        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        editor = pref_setting.edit();

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        if (dark_mode) {
            //setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar);
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            // setTheme(R.style.OLED_Theme);
        } else {
            //なにもない
        }

        setContentView(R.layout.activity_login);

        //ログイン画面再構築
        client_name_EditText = findViewById(R.id.client_name_textbox_textbox);
        client_name_TextInputEditText = findViewById(R.id.client_name_textbox);
        instance_name_EditText = findViewById(R.id.instance_name_editText);
        access_token_Switch = findViewById(R.id.login_access_token_swich);
        login_Button = findViewById(R.id.login_button);
        access_token_LinearLayout = findViewById(R.id.access_token_linearLayout);
        misskey_login_Switch = findViewById(R.id.misskey_login_switch);

        //クライアント名をグレーアウトしない
        client_name_TextInputEditText.setEnabled(true);
        access_token_LinearLayout.removeAllViews();
        //認証開始
        //onResumeにアクセストークン取得部分が書いてあります
        login_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Step1:client_id,client_secretを取得せよ
                //Misskeyと分ける
                if (misskey_login_Switch.isChecked()) {
                    getMisskeyApp();
                } else {
                    getClientIDSecret();
                }
            }
        });

        //アクセストークン手打ちモード
        access_token_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Misskeyモード
                    if (misskey_login_Switch.isChecked()) {
                        client_name_TextInputEditText.setHint("Username");
                        client_name_EditText.setText("");
                        //レイアウトを取り込む
                        getLayoutInflater().inflate(R.layout.textinput_edittext, access_token_LinearLayout);
                        //Hint
                        ((TextInputLayout) getLayoutInflater().inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById(R.id.name_TextInputLayout)).setHint(getString(R.string.setting_mastodon_accesstoken));
                        //保存
                        login_Button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //アクセストークン検証あんd保存
                                checkMisskeyAccount(client_name_EditText.getText().toString(), ((EditText) getLayoutInflater().inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById(R.id.name_editText)).getText().toString());
                            }
                        });
                        //強制保存モード(非推奨)
                        //一回失敗しないと利用できないように
                        login_Button.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                saveMisskeyAccount(instance_name_EditText.getText().toString(), ((EditText) getLayoutInflater().inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById(R.id.name_editText)).getText().toString(), client_name_EditText.getText().toString());
                                return false;
                            }
                        });
                    } else {
                        //クライアント名をグレーアウトする
                        client_name_TextInputEditText.setEnabled(false);
                        //レイアウトを取り込む
                        getLayoutInflater().inflate(R.layout.textinput_edittext, access_token_LinearLayout);
                        //Hint
                        ((TextInputLayout) getLayoutInflater().inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById(R.id.name_TextInputLayout)).setHint(getString(R.string.setting_mastodon_accesstoken));
                        //保存
                        login_Button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //アクセストークン検証あんd保存
                                checkAccount(((EditText) getLayoutInflater().inflate(R.layout.textinput_edittext, access_token_LinearLayout).findViewById(R.id.name_editText)).getText().toString());
                            }
                        });
                    }
                } else {
                    //クライアント名をグレーアウトしない
                    client_name_TextInputEditText.setEnabled(true);
                    client_name_TextInputEditText.setHint(getString(R.string.setting_client_name));
                    client_name_EditText.setText("Kaisendon");
                    access_token_LinearLayout.removeAllViews();
                    //認証開始
                    //onResumeにアクセストークン取得部分が書いてあります
                    login_Button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Step1:client_id,client_secretを取得せよ
                            //Misskeyと分ける
                            if (misskey_login_Switch.isChecked()) {
                                String instance = instance_name_EditText.getText().toString();
                                getMisskeyApp();
                            } else {
                                getClientIDSecret();
                            }
                        }
                    });
                }
            }
        });


        //通常ログイン
        //ClientIDとClientSecretを取得

/*        //準備
        TextView first_massage = findViewById(R.id.first_message);

        Button login_button = findViewById(R.id.login_button);

        TextView second_message = findViewById(R.id.second_message);

        AutoCompleteTextView acess = findViewById(R.id.code);

        EditText pin = findViewById(R.id.Instance_Name);

        Button acess_button = findViewById(R.id.access_button);

        Switch accesstoken_swich = findViewById(R.id.login_access_token_swich);


        //インスタンスリストを補充して入力しやすくする
        ArrayList<String> instance_list_list = new ArrayList<>();
        instance_list_list.add("friends.nico");

        //読み込み中
        View view = findViewById(android.R.id.content);
        Snackbar snackbar_instance_list = Snackbar.make(view, getString(R.string.loading_instance_list) + "\r\n https://friends.nico/api/v1/instance/peers", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar_instance_list.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(LoginActivity.this);
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar_instance_list.show();


        //インスタンス一覧を取得すりゅ
        //Peers APIでインスタンス一覧が帰ってくるよ！
        if (getIntent().getData() == null) {
            AsyncTask asyncTask = new AsyncTask<String, String, String>() {
                @Override
                protected String doInBackground(String... string) {
                    String url = "https://friends.nico/api/v1/instance/peers";
                    //作成
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String response_String = response.body().string();
                            try {
                                JSONArray jsonArray = new JSONArray(response_String);
                                for (int count = 0; count < jsonArray.length(); count++) {
                                    String instance_name = jsonArray.getString(count);
                                    instance_list_list.add(instance_name);
                                    //くるくる終了
                                    snackbar_instance_list.dismiss();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return null;
                }

                @Override
                protected void onPostExecute(String result) {
                    //候補に入れる
                    ArrayAdapter<String> instance_list_adapter = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_dropdown_item_1line, instance_list_list);
                    acess.setAdapter(instance_list_adapter);
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }


        //クライアント名を変更している場合
        if (pref_setting.getString("pref_client_name", "").equals("")) {
            client_name = "Kaisendon";
        } else {
            client_name = pref_setting.getString("pref_client_name", "");
        }

        //アクセストークン手打ちの場合の処理
        accesstoken_swich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    accesstoken_imput = true;
                    second_message.setText(R.string.please_access_token);
                } else {
                    accesstoken_imput = false;
                    second_message.setText(R.string.second_message);
                }
            }
        });


*//*
        //スイッチ
        multi_account_swich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    swich = 1;
                } else {
                    swich = 0;
                }
            }
        });
*//*

        String instance_instance_string_1 = pref_setting.getString("access_list", "");
        try {
            JSONArray instance_array = new JSONArray(instance_instance_string_1);
            for (int i = 0; i < instance_array.length(); i++) {
                //System.out.println("こんな感じ : " + instance_array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //コールバック
        if (getIntent().getData() != null) {
            callback_code = getIntent().getData().getQueryParameter("code");
            pin.setText(callback_code);
            //非同期処理
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... aVoid) {
                    //やっぱりActivity再起動された。Client (ryを持ってくる
                    MastodonClient client = new MastodonClient.Builder(acess.getText().toString(), new OkHttpClient.Builder(), new Gson()).build();
                    Apps apps = new Apps(client);
                    AppRegistration appRegistration = new AppRegistration();
                    String redirectUrl = appRegistration.getRedirectUri();

                    // String url = apps.getOAuthUrl(client_id, new Scope(Scope.Name.ALL),"urn:ietf:wg:oauth:2.0:oob");
                    try {
                        AccessToken accessToken = apps.getAccessToken(
                                pref_setting.getString("client_id", ""),
                                pref_setting.getString("client_secret", ""),
                                pref_setting.getString("redirect_url", ""),
                                callback_code,
                                "authorization_code"
                        ).execute();

                        // アクセストークン！！！！！！！！！！！
                        String accessToken_string = accessToken.getAccessToken();

                        //プリファレンスにかきかき（token）
                        SharedPreferences.Editor editor = pref_setting.edit();

                        //祝　マルチアカウント対応
                        //ここから

                        //配列を使えば幸せになれそう！！！
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

                        multi_account_instance.add(acess.getText().toString());
                        multi_account_access_token.add(accessToken_string);

                        //Preferenceに配列は保存できないのでJSON化して保存する
                        //Write
                        JSONArray instance_array = new JSONArray();
                        JSONArray access_array = new JSONArray();
                        for (int i = 0; i < multi_account_instance.size(); i++) {
                            instance_array.put(multi_account_instance.get(i));
                        }
                        for (int i = 0; i < multi_account_access_token.size(); i++) {
                            access_array.put(multi_account_access_token.get(i));
                        }

                        //書き込む
                        editor.putString("instance_list", instance_array.toString());
                        editor.putString("access_list", access_array.toString());
                        editor.commit();

                        //テーマを初期へ
                        editor.putBoolean("pref_dark_theme", false);
                        editor.putBoolean("pref_oled_mode", false);
                        editor.commit();
                        //ログインできたらとりあえずそれにする
                        editor.putString("main_token", accessToken_string);
                        editor.putString("main_instance", acess.getText().toString());
                        editor.commit();

                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    //HomeCardへ画面を戻す
                    Intent homecard = new Intent(LoginActivity.this, Home.class);
                    startActivity(homecard);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        }


        //アクセストークンを取得するためのidとsecretを取得
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!accesstoken_imput) {

                    //読み込み中
                    Snackbar snackbar = Snackbar.make(v, getString(R.string.loading_clientid_clientsecret) + "\r\n /api/v1/apps", Snackbar.LENGTH_INDEFINITE);
                    ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                    //SnackBerを複数行対応させる
                    TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
                    snackBer_textView.setMaxLines(2);
                    //複数行対応させたおかげでずれたので修正
                    ProgressBar progressBar = new ProgressBar(LoginActivity.this);
                    LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    progressBer_layoutParams.gravity = Gravity.CENTER;
                    progressBar.setLayoutParams(progressBer_layoutParams);
                    snackBer_viewGrop.addView(progressBar, 0);
                    snackbar.show();


                    AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
                        @Override
                        protected String doInBackground(String... string) {

                            //idとsecretを取得する
                            MastodonClient client = new MastodonClient.Builder(acess.getText().toString(), new OkHttpClient.Builder(), new Gson()).build();
                            Apps apps = new Apps(client);
                            try {
                                AppRegistration appRegistration = apps.createApp(
                                        client_name,
                                        "https://takusan23.github.io/Kaisendon-Callback-Website/",
                                        new Scope(Scope.Name.ALL),
                                        "https://play.google.com/store/apps/details?id=io.github.takusan23.kaisendon"
                                ).execute();

                                register_client_id = appRegistration.getClientId();
                                register_client_secret = appRegistration.getClientSecret();
                                redirect_url = appRegistration.getRedirectUri();
                                //保存（コールバック対応させる）
                                SharedPreferences.Editor editor = pref_setting.edit();
                                editor.putString("client_id", register_client_id);
                                editor.putString("client_secret", register_client_secret);
                                editor.putString("redirect_url", redirect_url);
                                editor.apply();

                                //dialog.dismiss();
                                snackbar.dismiss();

                            } catch (Mastodon4jRequestException e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, R.string.code_copy, Toast.LENGTH_LONG).show();
                                }
                            });

                            //PINを生成する
                            Uri url = Uri.parse("https://" + acess.getText().toString() + "/oauth/authorize?client_id=" + register_client_id + "&redirect_uri=" + redirect_url + "&response_type=code&scope=read%20write%20follow");

                            boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
                            //戻るアイコン
                            Bitmap back_icon = BitmapFactory.decodeResource(LoginActivity.this.getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);

                            //CutomTabを使うかどうか
                            //有効
                            if (chrome_custom_tabs) {

                                String custom = CustomTabsHelper.getPackageNameToUse((LoginActivity.this));

                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.intent.setPackage(custom);
                                customTabsIntent.launchUrl((Activity) LoginActivity.this, url);
                                //無効
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW, url);
                                LoginActivity.this.startActivity(intent);

                            }
                            return null;
                        }

                    }.execute();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.please_access_token, Toast.LENGTH_SHORT).show();
                }
            }
        });


 *//*       //プリファレンスに保存する
        acess_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String code = pin.getText().toString();

                if (accesstoken_imput) {

                    //アクセストークンがあってるかユーザー情報を取得して確認する
                    //最後のトゥートIDを持ってくる
                    //もういい！okhttpで実装する！！
                    String url = "https://" + acess.getText().toString() + "/api/v1/accounts/verify_credentials/?access_token=" + code;
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
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                String username = jsonObject.getString("display_name");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, username, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    //プリファレンスにかきかき（token）
                    SharedPreferences.Editor editor = pref_setting.edit();

                    //祝　マルチアカウント対応
                    //ここから

                    //配列を使えば幸せになれそう！！！
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

                    multi_account_instance.add(acess.getText().toString());
                    multi_account_access_token.add(code);

                    //Preferenceに配列は保存できないのでJSON化して保存する
                    //Write
                    JSONArray instance_array = new JSONArray();
                    JSONArray access_array = new JSONArray();
                    for (int i = 0; i < multi_account_instance.size(); i++) {
                        instance_array.put(multi_account_instance.get(i));
                    }
                    for (int i = 0; i < multi_account_access_token.size(); i++) {
                        access_array.put(multi_account_access_token.get(i));
                    }

                    //書き込む
                    editor.putString("instance_list", instance_array.toString());
                    editor.putString("access_list", access_array.toString());
                    editor.commit();

                    //ログインできたらとりあえずそれにする
                    editor.putString("main_token", code);
                    editor.putString("main_instance", acess.getText().toString());
                    editor.commit();

                    //テーマを初期へ
                    editor.putBoolean("pref_dark_theme", false);
                    editor.putBoolean("pref_oled_mode", false);
                    editor.commit();

                    //HomeCardへ画面を戻す
                    Intent homecard = new Intent(LoginActivity.this, Home.class);
                    startActivity(homecard);

                } else {

                    String accessToken_string = null;

                    new AsyncTask<String, Void, String>() {

                        @Override
                        protected String doInBackground(String... params) {

                            MastodonClient client = new MastodonClient.Builder(acess.getText().toString(), new OkHttpClient.Builder(), new Gson()).build();
                            Apps apps = new Apps(client);
                            AppRegistration appRegistration = new AppRegistration();
                            String redirectUrl = appRegistration.getRedirectUri();

                            // String url = apps.getOAuthUrl(client_id, new Scope(Scope.Name.ALL),"urn:ietf:wg:oauth:2.0:oob");
                            try {
                                AccessToken accessToken = apps.getAccessToken(
                                        register_client_id,
                                        register_client_secret,
                                        "urn:ietf:wg:oauth:2.0:oob",
                                        code,
                                        "authorization_code"
                                ).execute();

                                // アクセストークン！！！！！！！！！！！
                                String accessToken_string = accessToken.getAccessToken();

                                //プリファレンスにかきかき（token）
                                SharedPreferences.Editor editor = pref_setting.edit();

                                //祝　マルチアカウント対応
                                //ここから

                                //配列を使えば幸せになれそう！！！
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

                                multi_account_instance.add(acess.getText().toString());
                                multi_account_access_token.add(accessToken_string);

                                //Preferenceに配列は保存できないのでJSON化して保存する
                                //Write
                                JSONArray instance_array = new JSONArray();
                                JSONArray access_array = new JSONArray();
                                for (int i = 0; i < multi_account_instance.size(); i++) {
                                    instance_array.put(multi_account_instance.get(i));
                                }
                                for (int i = 0; i < multi_account_access_token.size(); i++) {
                                    access_array.put(multi_account_access_token.get(i));
                                }

                                //書き込む
                                editor.putString("instance_list", instance_array.toString());
                                editor.putString("access_list", access_array.toString());
                                editor.commit();

                                //ログインできたらとりあえずそれにする
                                editor.putString("main_token", accessToken_string);
                                editor.putString("main_instance", acess.getText().toString());
                                editor.commit();

                                //System.out.println("マルチアカウント : " + pref_setting.getString("token2", ""));

                            } catch (Mastodon4jRequestException e) {
                                e.printStackTrace();
                            }

                            return accessToken_string;
                        }


                        @Override
                        protected void onPostExecute(String result) {
                            //設定プリファレンス
                            SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
                            //Toast.makeText(getApplicationContext(), "プリファレンス : " + pref_setting.getString("main_token", ""), Toast.LENGTH_SHORT).show();

                            //テーマを初期へ
                            SharedPreferences.Editor editor = pref_setting.edit();
                            editor.putBoolean("pref_dark_theme", false);
                            editor.putBoolean("pref_oled_mode", false);
                            editor.commit();

                            //HomeCardへ画面を戻す
                            Intent homecard = new Intent(LoginActivity.this, Home.class);
                            startActivity(homecard);
                        }
                    }.execute();
                }
            }
        });
*//*
         */
    }

    /*
     * ClientID / ClientSecret 取得
     * */
    private void getClientIDSecret() {
        String url = "https://" + instance_name_EditText.getText().toString() + "/api/v1/apps";
        //SnackberProgress
        SnackberProgress.showProgressSnackber(instance_name_EditText, LoginActivity.this, getString(R.string.loading) + "\n" + url);
        //OkHttp
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
                .add("client_name", instance_name_EditText.getText().toString())
                .add("redirect_uris", "https://takusan23.github.io/Kaisendon-Callback-Website/")
                .add("scopes", "read write follow")
                .add("website", "https://play.google.com/store/apps/details?id=io.github.takusan23.kaisendon")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    //エラー
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String response_string = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(response_string);
                        //ぱーす
                        client_id = jsonObject.getString("client_id");
                        client_secret = jsonObject.getString("client_secret");
                        redirect_url = jsonObject.getString("redirect_uri");
                        //アクセストークン取得のときにActivity再起動されるので保存しておく
                        SharedPreferences.Editor editor = pref_setting.edit();
                        editor.putString("client_id", client_id);
                        editor.putString("client_secret", client_secret);
                        editor.putString("redirect_uri", redirect_url);
                        //リダイレクト時にインスタンス名飛ぶので保存
                        editor.putString("register_instance", instance_name_EditText.getText().toString());
                        editor.apply();
                        //Step2:認証画面を表示させる
                        showApplicationRequest();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        SnackberProgress.closeProgressSnackber();
    }

    /**
     * 認証画面表示
     */
    private void showApplicationRequest() {
        //PINを生成する
        Uri url = Uri.parse("https://" + instance_name_EditText.getText().toString() + "/oauth/authorize?client_id=" + client_id + "&redirect_uri=" + redirect_url + "&response_type=code&scope=read%20write%20follow");
        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
        //戻るアイコン
        Bitmap back_icon = BitmapFactory.decodeResource(LoginActivity.this.getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);
        //CutomTabを使うかどうか
        //有効
        if (chrome_custom_tabs) {
            String custom = CustomTabsHelper.getPackageNameToUse((LoginActivity.this));
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(custom);
            customTabsIntent.launchUrl((Activity) LoginActivity.this, url);
        } else {
            //無効
            Intent intent = new Intent(Intent.ACTION_VIEW, url);
            LoginActivity.this.startActivity(intent);
        }
    }

    /**
     * アクセストークン取得
     */
    private void getAccessToken(String code) {
        String url = "https://" + pref_setting.getString("register_instance", "") + "/oauth/token";
        //SnackberProgress
        SnackberProgress.showProgressSnackber(instance_name_EditText, LoginActivity.this, getString(R.string.loading) + "\n" + url);
        //OkHttp
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", pref_setting.getString("client_id", ""))
                .add("client_secret", pref_setting.getString("client_secret", ""))
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", pref_setting.getString("redirect_uri", ""))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    //エラー
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    //成功
                    String response_string = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(response_string);
                        String access_token = jsonObject.getString("access_token");
                        //保存
                        saveAccount(pref_setting.getString("register_instance", ""), access_token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * アクセストークン取得
     */
    @Override
    protected void onResume() {
        super.onResume();
        //認証最後の仕事、アクセストークン取得
        //URLスキーマからの起動のときの処理
        if (getIntent().getData() != null) {
            //code URLパース
            String code = getIntent().getData().getQueryParameter("code");
            //Step3:アクセストークン取得
            //onResume()が呼ばれます
            //MastodonかMisskeyか
            if (code != null) {
                getAccessToken(code);
            } else {
                String token = getIntent().getData().getQueryParameter("token");
                getMisskeyAccessToken(token);
            }
        }
    }

    /**
     * アカウント保存
     */
    private void saveAccount(String instance, String access_token) {
        SharedPreferences.Editor editor = pref_setting.edit();
        //祝　マルチアカウント対応
        //ここから
        //配列を使えば幸せになれそう！！！
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
                e.printStackTrace();
            }
        }
        multi_account_instance.add(instance);
        multi_account_access_token.add(access_token);

        //Preferenceに配列は保存できないのでJSON化して保存する
        //Write
        JSONArray instance_array = new JSONArray();
        JSONArray access_array = new JSONArray();
        for (int i = 0; i < multi_account_instance.size(); i++) {
            instance_array.put(multi_account_instance.get(i));
        }
        for (int i = 0; i < multi_account_access_token.size(); i++) {
            access_array.put(multi_account_access_token.get(i));
        }

        //書き込む
        editor.putString("instance_list", instance_array.toString());
        editor.putString("access_list", access_array.toString());
        editor.apply();

        //ログインできたらとりあえずそれにする
        editor.putString("main_token", access_token);
        editor.putString("main_instance", instance);
        editor.apply();

        //HomeCardへ画面を戻す
        editor.putBoolean("pref_dark_theme", false);
        editor.putBoolean("pref_oled_mode", false);
        editor.commit();
        Intent homecard = new Intent(LoginActivity.this, Home.class);
        startActivity(homecard);
    }

    /**
     * Misskeyアカウント保存
     */
    private void saveMisskeyAccount(String instance, String access_token, String username) {
        //上記Mastodonアカウント保存とだいたいおなじ
        ArrayList<String> multi_account_instance = new ArrayList<>();
        ArrayList<String> multi_account_access_token = new ArrayList<>();
        ArrayList<String> multi_account_username = new ArrayList<>();
        //とりあえずPreferenceに書き込まれた値を
        String instance_instance_string = pref_setting.getString("misskey_instance_list", "");
        String account_instance_string = pref_setting.getString("misskey_access_list", "");
        String account_username_string = pref_setting.getString("misskey_username_list", "");
        if (!instance_instance_string.equals("")) {
            try {
                JSONArray instance_array = new JSONArray(instance_instance_string);
                JSONArray access_array = new JSONArray(account_instance_string);
                JSONArray username_array = new JSONArray(account_username_string);
                for (int i = 0; i < instance_array.length(); i++) {
                    multi_account_access_token.add(access_array.getString(i));
                    multi_account_instance.add(instance_array.getString(i));
                    multi_account_username.add(username_array.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        multi_account_instance.add(instance);
        multi_account_access_token.add(access_token);
        multi_account_username.add(username);
        //Preferenceに配列は保存できないのでJSON化して保存する
        //Write
        JSONArray instance_array = new JSONArray();
        JSONArray access_array = new JSONArray();
        JSONArray username_array = new JSONArray();
        for (int i = 0; i < multi_account_instance.size(); i++) {
            instance_array.put(multi_account_instance.get(i));
        }
        for (int i = 0; i < multi_account_access_token.size(); i++) {
            access_array.put(multi_account_access_token.get(i));
        }
        for (int i = 0; i < multi_account_username.size(); i++) {
            username_array.put(multi_account_username.get(i));
        }
        //書き込む
        editor.putString("misskey_instance_list", instance_array.toString());
        editor.putString("misskey_access_list", access_array.toString());
        editor.putString("misskey_username_list", username_array.toString());
        editor.apply();
        //Misskeyはカスタムメニュー限定要素の予定なので
/*
        editor.putString("main_token", access_token);
        editor.putString("main_instance", instance);
        editor.apply();
*/
        //注意文、出しとく？
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, "Misskeyはカスタムメニューで利用してください。", Toast.LENGTH_SHORT).show();
            }
        });

        //HomeCardへ画面を戻す
        editor.putBoolean("pref_dark_theme", false);
        editor.putBoolean("pref_oled_mode", false);
        editor.commit();
        Intent homecard = new Intent(LoginActivity.this, Home.class);
        startActivity(homecard);
    }

    /**
     * アクセストークン手打ち
     * アカウントチェック
     */
    private void checkMisskeyAccount(String username, String access_token) {
        String url = "https://" + instance_name_EditText.getText().toString() + "/api/users/show";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                error_mode = true;
                                login_Button.append("\n" + "長押しで強制保存モードになります（非推奨）");
                                Toast.makeText(LoginActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                if (!response.isSuccessful()){
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            error_mode = true;
                            login_Button.append("\n" + "長押しで強制保存モードになります（非推奨）");
                            Toast.makeText(LoginActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {

                }
                try {
                    JSONObject jsonObject = new JSONObject(response_string);
                    String name = jsonObject.getString("name");
                    if (name.length() != 0) {
                        saveMisskeyAccount(instance_name_EditText.getText().toString(), access_token, username);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * アカウント検証
     */
    private void checkAccount(String access_token) {
        //アクセストークンがあってるかユーザー情報を取得して確認する
        //最後のトゥートIDを持ってくる
        //もういい！okhttpで実装する！！
        String url = "https://" + instance_name_EditText.getText().toString() + "/api/v1/accounts/verify_credentials/?access_token=" + access_token;
        //作成
        Request request = new Request.Builder()
                .url(url)
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
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String username = jsonObject.getString("display_name");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, username, Toast.LENGTH_SHORT).show();
                        }
                    });
                    //何もなければ保存
                    if (username != null) {
                        saveAccount(instance_name_EditText.getText().toString(), access_token);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Misskeyにアプリ登録
     */
    private void getMisskeyApp() {
        //SnackberProgress
        String test = instance_name_EditText.getText().toString();
        System.out.println("テキストボックス:" + test);
        if (test != null) {
            String url = "https://" + test + "/api/app/create/";
            System.out.println("りんく:" + url);
            SnackberProgress.showProgressSnackber(instance_name_EditText, LoginActivity.this, getString(R.string.loading) + "\n" + url);

            //アクセストークン取得の前準備
            //Permissionは一覧無いけどこれが全てだと思います
            String object = "{\n" +
                    "\"name\": \"" + client_name_EditText.getText().toString() + "\",\n" +
                    "\"description\": \"Android Mastodon/Misskey Client\",\n" +
                    "\"callbackUrl\": \"https://takusan23.github.io/Kaisendon-Callback-Website/\",\n" +
                    "\"permission\":[\"account-read\",\n" +
                    "\"account-write\",\n" +
                    "\"account/read\",\n" +
                    "\"account/write\",\n" +
                    "\"note-write\",\n" +
                    "\"reaction-write\",\n" +
                    "\"drive-read\",\n" +
                    "\"drive-write\",\n" +
                    "\"favorite-write\",\n" +
                    "\"favorites-read\",\n" +
                    "\"following-write\",\n" +
                    "\"following-read\",\n" +
                    "\"messaging-read\",\n" +
                    "\"messaging-write\",\n" +
                    "\"notification-read\",\n" +
                    "\"notification-write\"\n" +
                    "]\n" +
                    "}";
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), object);
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
                            Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(LoginActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(response_string);
                            editor.putString("misskey_secret", jsonObject.getString("secret"));
                            //インスタンス名一時保存
                            editor.putString("misskey_instance_tmp", instance_name_EditText.getText().toString());
                            editor.apply();
                            getMisskeyLogin(jsonObject.getString("secret"));
                            //くるくる終了
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SnackberProgress.closeProgressSnackber();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
    }

    /**
     * 認証画面に飛ばす
     */
    private void getMisskeyLogin(String secretKey) {
        String url = "https://" + instance_name_EditText.getText().toString() + "/api/auth/session/generate";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("appSecret", secretKey);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(LoginActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response_string);
                        String url = jsonObject.getString("url");
                        //CutomTabを使うかどうか
                        //有効
                        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
                        Bitmap back_icon = BitmapFactory.decodeResource(LoginActivity.this.getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);
                        if (chrome_custom_tabs) {
                            String custom = CustomTabsHelper.getPackageNameToUse((LoginActivity.this));
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.intent.setPackage(custom);
                            customTabsIntent.launchUrl((Activity) LoginActivity.this, Uri.parse(url));
                        } else {
                            //無効
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            LoginActivity.this.startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * アクセストークン生成
     */
    private void getMisskeyAccessToken(String token) {
        String url = "https://misskey.m544.net/api/auth/session/userkey";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("appSecret", pref_setting.getString("misskey_secret", ""));
            jsonObject.put("token", token);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resopnse_string = response.body().string();
                if (!response.isSuccessful()) {
                    //失敗
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(resopnse_string);
                        String secret = pref_setting.getString("misskey_secret", "");
                        String token = jsonObject.getString("accessToken");
                        String text = token + secret;
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        digest.reset();
                        digest.update(text.getBytes("utf8"));
                        String access_token_sha_256 = String.format("%040x", new BigInteger(1, digest.digest()));
                        //アクセストークンでアカウント情報がわかるAPIがMisskeyに無いのでここでユーザーネーム保存しておく
                        String user_name = jsonObject.getJSONObject("user").getString("username");
                        //保存すりゅ
                        //System.out.println(access_token_sha_256);
                        saveMisskeyAccount(pref_setting.getString("misskey_instance_tmp", ""), access_token_sha_256, user_name);

                        //アカウント確認？
                        String name = jsonObject.getJSONObject("user").getString("name");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, name, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * ためしにAPI叩いた
     */
    private void testMisskey() {
        String url = "https://misskey.m544.net/api/notes/timeline";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("i", "26e301f8dbc6ca77bc2029e7296ddad806e3f695b5a5f0da84a90f9dc24a3ed6");
            jsonObject.put("limit", 100);
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

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.body().string());
            }
        });
    }

}
