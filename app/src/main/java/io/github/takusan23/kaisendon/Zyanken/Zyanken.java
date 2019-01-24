package io.github.takusan23.kaisendon.Zyanken;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.method.Streaming;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Zyanken extends AppCompatActivity {

    //じゃんけんの状態
    //自分
    String zyanken_String = "ぐー";
    //相手
    String zyanken_String_2 = null;
    //勝ったのは？
    String zyanken_final = null;

    String acct;
    String content;

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

    //mode
    boolean host_boo = false;
    boolean client_boo = false;
    boolean ok = false;

    int startCount = 0;

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

        //もーど
        if (getIntent().getStringExtra("mode").contains("host")) {
            host_boo = true;
        }
        //もーど
        if (getIntent().getStringExtra("mode").contains("client")) {
            client_boo = true;
        }


        //find
        zyanken_TextView = findViewById(R.id.zyanken_TextView);
        rock_Button = findViewById(R.id.rock);
        caesar_Button = findViewById(R.id.caesar);
        paper_Button = findViewById(R.id.paper);


        //通知取得
        String finalInstance = Instance;
        String finalAccessToken = AccessToken;
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... aVoid) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();
                com.sys1yagi.mastodon4j.api.Handler handler = new com.sys1yagi.mastodon4j.api.Handler() {
                    @Override
                    public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {
                    }

                    @Override
                    public void onNotification(@NotNull Notification notification) {
                        if (notification.getType().contains("mention")) {
                            content = notification.getStatus().getContent();
                            acct = notification.getAccount().getAcct();
                            //参加
                            if (client_boo) {
                                String finalMessage = "@" + acct + " " + "//じゃんけん//\nいいよ";
                                sendDirectMessage(finalMessage);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        zyanken_TextView.append("準備完了/メッセージ送信完了\n");
                                        Toast.makeText(Zyanken.this, "ぐー・ちょき・ぱー\n選んでね！10秒後に勝負だよ！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            //企画
                            if (host_boo) {
                                if (content.contains("いいよ") && content.contains("//じゃんけん//")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            zyanken_TextView.append("あいて準備完了\n");
                                            Toast.makeText(Zyanken.this, "ぐー・ちょき・ぱー\n選んでね！10秒後に勝負だよ！", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            syoubu(content);
                            host_boo = false;
                            client_boo = false;
                        }
                    }

                    @Override
                    public void onDelete(long l) {

                    }
                };

                Streaming streaming = new Streaming(client);
                try {
                    Shutdownable shutdownable = streaming.user(handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //用意できた
        if (zyanken_TextView.getText().length() == 0) {
            //10秒間隔でジャンケン勝負を行う
            sendButton(rock_Button);
            sendButton(caesar_Button);
            sendButton(paper_Button);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(Zyanken.this, "ぐー・ちょき・ぱー\n選んでね！10秒後に勝負だよ！", Toast.LENGTH_SHORT).show();
                }
            });
            //定期実行？
            Handler handler = new Handler();
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (startCount >= 1){
                                //準備OK!?
                                //POST
                                zyankenPost();
                                //結果

                                //入れる
                                zyanken_TextView.append(zyanken_final + "\n");

                            }


                        }
                    });
                }
            }, 1000, 15000);

        }


    }

    //先行
    private void sendButton(Button button) {
        final String[] dore = {""};
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCount++;
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
                zyanken_String = dore[0];
                Toast.makeText(Zyanken.this, zyanken_String + "　を選びました", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //POST
    //定期的に動かす？
    private void zyankenPost() {
        zyanken_TextView.append("自分 : " + zyanken_String + "\n");
        String finalMessage = "@" + acct + " " + "//じゃんけん//\n" + zyanken_String;
        sendDirectMessage(finalMessage);
        zyanken_TextView.append("送信したよ！\n");
    }

    //結果
    private void syoubu(String content) {
        //ぐー
        if (zyanken_String.contains("ぐー")) {
            //ぱーが勝ち
            if (content.contains("ぱー")) {
                zyanken_final = acct + "の勝ちです";
            }
            if (content.contains("ちょき")) {
                zyanken_final = "あなたの勝ちです";
            }
            if (content.contains("ぐー")) {
                zyanken_final = "あいこです";
            }
        }
        //きょき
        if (zyanken_String.contains("ちょき")) {
            //ぱーが勝ち
            if (content.contains("ぐー")) {
                zyanken_final = acct + "の勝ちです";
            }
            if (content.contains("ぱー")) {
                zyanken_final = "あなたの勝ちです";
            }
            if (content.contains("ちょき")) {
                zyanken_final = "あいこです";
            }
        }
        //ぱー
        if (zyanken_String.contains("ぱー")) {
            //ぱーが勝ち
            if (content.contains("きょき")) {
                zyanken_final = acct + "の勝ちです";
            }
            if (content.contains("ぐー")) {
                zyanken_final = "あなたの勝ちです";
            }
            if (content.contains("ぱー")) {
                zyanken_final = "あいこです";
            }
        }
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
/*
                //結果
                kati_make(zyanken_String_1, zyanken_String_2);
                zyanken_TextView.append(zyanken_final + "\n");
                //戻す
                zyanken_String_1 = null;
                zyanken_String_2 = null;
*/
                //再戦？
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
                        Toast.makeText(Zyanken.this, "送信しました", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
