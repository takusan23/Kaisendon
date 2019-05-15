package io.github.takusan23.Kaisendon.Zyanken;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.github.takusan23.Kaisendon.Preference_ApplicationContext;
import io.github.takusan23.Kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ZyankenSetup extends AppCompatActivity {

    Button follower;
    Button follow;
    Button connection;

    TextView textView;
    EditText editText;

    ListView listView;

    SharedPreferences pref_setting;

    String acct;
    String display_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zyanken__setup);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //アクセストークン
        String AccessToken = null;
        //インスタンス
        String Instance = null;
        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");
        }

        //find
        connection = findViewById(R.id.zyanken_setup_connection);
        textView = findViewById(R.id.zyanken_setup_textView);
        editText = findViewById(R.id.zyanken_setup_editText);

        getSupportActionBar().setTitle("招待");

        //Intentでデータを受け取る
        String mode = getIntent().getStringExtra("mode");
        //ホスト側
        if (mode.contains("host")) {
            //テキスト変更
            textView.setText("@相手のID@インスタンス名\nを入力してください");
            //EditTextの内容
            //ボタンを押したらDM送信
            //ユーザー情報取得
            //getMyUser();
            //String finalMessage = "//じゃんけん//\n" + display_name + " さんから招待";
            //あいてはWebSocketで常時接続状態になっていることが必須
            connection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //DM送信
                    String userID = editText.getText().toString();
                    String finalMessage = userID + " " + "//じゃんけん//\n招待だよ";
                    sendDirectMessage(finalMessage);
                    //Activity移動
                    Intent intent = new Intent(ZyankenSetup.this, Zyanken.class);
                    intent.putExtra("mode", "host");
                    startActivity(intent);
                }
            });
        }
    }

    private void sendDirectMessage(String message) {
        //アクセストークン
        String AccessToken = null;
        //インスタンス
        String Instance = null;
        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");
        }

        String url = "https://" + Instance + "/api/v1/statuses/?access_token=" + AccessToken;
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
                .add("status", message)
                .add("visibility", "direct")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //POST
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ZyankenSetup.this, "送信しました", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getMyUser() {
        //アクセストークン
        String AccessToken = null;
        //インスタンス
        String Instance = null;
        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");
        }
        String url = "https://" + Instance + "/api/v1/accounts/verify_credentials/?access_token=" + AccessToken;
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
                    acct = jsonObject.getString("acct");
                    display_name = jsonObject.getString("display_name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
