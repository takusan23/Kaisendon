package io.github.takusan23.kaisendon.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Scope;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Apps;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    String register_client_id = null;
    String register_client_secret = null;
    String client_name = null;
    String redirect_url = null;
    String callback_code = null;

    boolean accesstoken_imput = false;

    //マルチアカウント
    int multi_account_count;

    int swich;

    private ProgressDialog dialog;

    public LoginActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //設定プリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //絵文字用SharedPreferences
        SharedPreferences pref_emoji = Preference_ApplicationContext.getContext().getSharedPreferences("preferences_emoji", Context.MODE_PRIVATE);


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


        //準備
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


/*
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
*/

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
            new AsyncTask<Void,Void,Void>(){

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
                                pref_setting.getString("client_id",""),
                                pref_setting.getString("client_secret",""),
                                "urn:ietf:wg:oauth:2.0:oob",
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

                        //ログインできたらとりあえずそれにする
                        editor.putString("main_token", accessToken_string);
                        editor.putString("main_instance", acess.getText().toString());
                        editor.commit();
                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }
                    return null;
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


            //プリファレンスに保存する
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
        }
    }
