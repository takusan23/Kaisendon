package io.github.takusan23.kaisendon;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class Zyanken extends AppCompatActivity {

    //じゃんけんの状態
    //自分
    String zyanken_String_1 = null;
    //相手
    String zyanken_String_2 = null;
    //勝ったのは？
    String zyanken_final = null;

    //じぶん、あいて切り替え
    //false 自分
    //true あいて
    boolean player = false;
    TextView zyanken_TextView;

    Button rock_Button;
    Button caesar_Button;
    Button paper_Button;

    WebSocketClient webSocketClient;
    SharedPreferences pref_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zyanken);

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
        zyanken_TextView = findViewById(R.id.zyanken_TextView);
        rock_Button = findViewById(R.id.rock);
        caesar_Button = findViewById(R.id.caesar);
        paper_Button = findViewById(R.id.paper);

        //自分
        if (player == false) {
            firstPlayer(rock_Button);
            firstPlayer(caesar_Button);
            firstPlayer(paper_Button);
        }


        //WebSocket
        webSocketClient = new WebSocketClient(URI.create("wss://" + Instance + "/api/v1/streaming/?stream=public:local&access_token=" + AccessToken)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {

            }

            @Override
            public void onMessage(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //JSON解析
                        try {
                            //payloadのJSONObjectをとってくる
                            JSONObject jsonObject = new JSONObject(message);
                            String payload = jsonObject.getString("payload");
                            //payloadの中からcontentを取り出す
                            JSONObject payload_jsonObject = new JSONObject(payload);
                            String content = payload_jsonObject.getString("content");
                            //HTMLタグを適用させる？
                            zyanken_TextView.append(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT) + "\n");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception ex) {

            }
        };


        //接続
        webSocketClient.connect();

    }

    //先行
    private void firstPlayer(Button button) {
        final String[] dore = {""};
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断
                if (button.getText().toString().contains("✊")) {
                    dore[0] = "ぐー";
                }
                if (button.getText().toString().contains("✌")) {
                    dore[0] = "ちょき";
                }
                if (button.getText().toString().contains("✋")) {
                    dore[0] = "ぱー";
                }
                player = true;
                zyanken_String_1 = dore[0];
                zyanken_TextView.append("１番め : " + zyanken_String_1 + "\n");

                //相手
                if (zyanken_String_2 == null) {
                    secondPlayer(rock_Button);
                    secondPlayer(caesar_Button);
                    secondPlayer(paper_Button);
                }

            }
        });
    }

    private void secondPlayer(Button button) {
        final String[] dore = {""};
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断
                if (button.getText().toString().contains("✊")) {
                    dore[0] = "ぐー";
                }
                if (button.getText().toString().contains("✌")) {
                    dore[0] = "ちょき";
                }
                if (button.getText().toString().contains("✋")) {
                    dore[0] = "ぱー";
                }
                player = false;
                zyanken_String_2 = dore[0];
                zyanken_TextView.append("２番め : " + zyanken_String_2 + "\n");
                //結果
                kati_make(zyanken_String_1, zyanken_String_2);
                zyanken_TextView.append(zyanken_final + "\n");
                //戻す
                zyanken_String_1 = null;
                zyanken_String_2 = null;
                //再戦？
                firstPlayer(rock_Button);
                firstPlayer(caesar_Button);
                firstPlayer(paper_Button);
            }
        });
    }

    private void kati_make(String player_1, String player_2) {
        //ぐー
        if (player_1.contains("ぐー")) {
            //ぱーが勝ち
            if (player_2.contains("ぱー")) {
                zyanken_final = "２番目の勝ちです";
            }
            if (player_2.contains("ちょき")) {
                zyanken_final = "１番目の勝ちです";
            }
            if (player_2.contains("ぐー")) {
                zyanken_final = "あいこです";
            }
        }
        //きょき
        if (player_1.contains("ちょき")) {
            //ぱーが勝ち
            if (player_2.contains("ぐー")) {
                zyanken_final = "２番目の勝ちです";
            }
            if (player_2.contains("ぱー")) {
                zyanken_final = "１番目の勝ちです";
            }
            if (player_2.contains("ちょき")) {
                zyanken_final = "あいこです";
            }
        }
        //ぱー
        if (player_1.contains("ぱー")) {
            //ぱーが勝ち
            if (player_2.contains("きょき")) {
                zyanken_final = "２番目の勝ちです";
            }
            if (player_2.contains("ぐー")) {
                zyanken_final = "１番目の勝ちです";
            }
            if (player_2.contains("ぱー")) {
                zyanken_final = "あいこです";
            }
        }

    }
}
