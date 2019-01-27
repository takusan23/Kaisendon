package io.github.takusan23.kaisendon.Zyanken;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
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
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.SimpleAdapter;
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

    //主催側
    String acct;
    //String content;
    //主催側が文字列を時刻だけにするのに自分のacctを使う
    String myAccountacct;
    //Host/Client共に使う時間
    String sendTime;
    //正しく結果が出るように
    String timeTemp;

    //じぶん、あいて切り替え
    //false 自分
    //true あいて
    boolean player = false;
    //TextView zyanken_TextView;
    ListView listView;
    //状況
    TextView zyanken_TextView_info;
    String zyanken_info;
    int myCount = 0;
    int OpponentCount = 0;
    int totalCount = 0;
    int aiko = 0;
    int errorCount = 0;

    //自分、相手のIDを控える
    String myName;
    String opponentName;
    long opponentID;

    //runningCheck
    boolean runningCheck = false;

    Button rock_Button;
    Button caesar_Button;
    Button paper_Button;


    SharedPreferences pref_setting;

    //mode
    boolean host_boo = false;
    boolean client_boo = false;
    boolean ok = false;

    int startCount = 0;
    Shutdownable shutdownable;
    TimerTask task;

    SimpleAdapter adapter;

    //終わりボタン
    Button finish_Button;
    Button share_Button;

    Timer timer;


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

        ArrayList<ListItem> toot_list = new ArrayList<>();

        adapter = new SimpleAdapter(Zyanken.this, R.layout.timeline_item, toot_list);

        //find
        //zyanken_TextView = findViewById(R.id.zyanken_TextView);
        listView = findViewById(R.id.zyanken_listView);
        zyanken_TextView_info = findViewById(R.id.zyanken_textView_info);
        //scrollView = findViewById(R.id.zyanken_scrollView);
        rock_Button = findViewById(R.id.rock);
        caesar_Button = findViewById(R.id.caesar);
        paper_Button = findViewById(R.id.paper);

        finish_Button = findViewById(R.id.zyanken_finish);
        share_Button = findViewById(R.id.zyanken_share);

        //共有ボタンで共有できるようにする
        share_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(Zyanken.this);
                //ダイアログの名前
                builder.setChooserTitle("結果を共有");
                //シェアするときのタイトル
                builder.setSubject("じゃんけん結果");
                //本文
                builder.setText(zyanken_TextView_info.getText().toString());
                //今回は文字なので
                builder.setType("text/plain");
                //ダイアログ
                builder.startChooser();
            }
        });

        //終了ボタン
        finish_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (acct != null){
                    //ストリーミング終了
                    shutdownable.shutdown();
                    //Timerも終了させる
                    task.cancel();
                    //DMで終わり通知
                    String finalMessage = "@" + acct + " " + "//じゃんけん//\nおわりだよ。またねー";
                    sendDirectMessage(finalMessage);
                    //UIスレッドで実行
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //ListView
                            //配列を作成
                            ArrayList<String> Item = new ArrayList<>();
                            //メモとか通知とかに
                            Item.add("じゃんけん");
                            //内容
                            Item.add("終了だって<br>" + zyanken_TextView_info.getText().toString());
                            //ユーザー名
                            Item.add(opponentName);
                            //時間、クライアント名等
                            Item.add(null);
                            //Toot ID 文字列版
                            Item.add(null);
                            //アバターURL
                            Item.add("おわり");
                            //アカウントID
                            Item.add(String.valueOf(opponentID));
                            //ユーザーネーム
                            Item.add("");
                            //メディア
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            //カード
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            ListItem listItem = new ListItem(Item);

                            adapter.add(listItem);
                            adapter.notifyDataSetChanged();
                            listView.setAdapter(adapter);

                            //ListView下にスクロール
                            listView.setSelection(listView.getCount() - 1);
                        }
                    });
                }
            }
        });

        //@ID@Instance取得
        //自分の名前も取ってくる
        getMyUser();

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
                Handler handler = new Handler() {
                    @Override
                    public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {
                    }

                    @Override
                    public void onNotification(@NotNull Notification notification) {
                        if (notification.getType().contains("mention")) {
                            String content = notification.getStatus().getContent();
                            opponentName = notification.getStatus().getAccount().getDisplayName() + " @" + notification.getStatus().getAccount().getAcct();
                            acct = notification.getAccount().getAcct();
                            opponentID = notification.getStatus().getAccount().getId();
                            //timeTemp = notification.getStatus().getCreatedAt();
                            //参加
                            //最初のゲームの開始時刻を一緒に送信する
                            if (client_boo) {
                                //ゲーム開始はDM送信から10秒後とする
                                Date date = new Date();
                                //10秒加算するのでCalendarを使う
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                //10秒追加しまーす（大物Youtubeｒ）
                                calendar.add(Calendar.SECOND, +10);
                                //Dateに戻しまーす
                                Date finalDate = new Date(calendar.getTimeInMillis());
                                //Stringへ
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                String dateString = simpleDateFormat.format(finalDate);
                                //送信時間確定
                                //もしかして：見えない改行エスケープ文字が存在する？
                                //参加側だけが改行されなかったのでなんとなく
                                sendTime = dateString + "\n";
                                startZyanken();

                                //DM送信
                                String finalMessage = "@" + acct + " " + "//じゃんけん//\nいいよ" + dateString;
                                sendDirectMessage(finalMessage);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //zyanken_TextView.append("準備完了/メッセージ送信完了\n");
                                        Toast.makeText(Zyanken.this, "準備完了/メッセージ送信完了", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(Zyanken.this, "ぐー・ちょき・ぱー\n選んでね！10秒後に勝負だよ！", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                            //企画
                            //参加側から送られてきた時刻をDateへ
                            if (host_boo) {
                                if (content.contains("いいよ") && content.contains("//じゃんけん//")) {
                                    //Date以外を取り除く
                                    String htmlRemoveContent = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT).toString();
                                    String finalContent = htmlRemoveContent.replace("@" + myAccountacct + " " + "//じゃんけん//\nいいよ", "");
                                    //送信時間確定
                                    sendTime = finalContent;
                                    startZyanken();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //zyanken_TextView.append("相手準備完了\n");
                                            Toast.makeText(Zyanken.this, "相手準備完了", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(Zyanken.this, "ぐー・ちょき・ぱー\n選んでね！10秒後に勝負だよ！", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            //タイトルバー
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getSupportActionBar().setTitle("相手 : " + opponentName);
                                }
                            });

                            //正負ちぇっく
                            //最初の動作確認は動かさないようにする
                            if (runningCheck) {
                                syoubu(zyanken_String, content);
                            }
                            //最初か判断する
                            runningCheck = true;
                            host_boo = false;
                            client_boo = false;

                            //おわらせる
                            //終わりメッセージを受け取る
                            if (content.contains("おわりだよ。またねー") && content.contains("//じゃんけん//")) {
                                //ストリーミング終了
                                shutdownable.shutdown();
                                //Timerも終了させる
                                task.cancel();
                                //UIスレッドで実行
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //ListView
                                        //配列を作成
                                        ArrayList<String> Item = new ArrayList<>();
                                        //メモとか通知とかに
                                        Item.add("じゃんけん");
                                        //内容
                                        Item.add("終了だって<br>" + zyanken_TextView_info.getText().toString());
                                        //ユーザー名
                                        Item.add(opponentName);
                                        //時間、クライアント名等
                                        Item.add(null);
                                        //Toot ID 文字列版
                                        Item.add(null);
                                        //アバターURL
                                        Item.add("じゃんけん おわり");
                                        //アカウントID
                                        Item.add(String.valueOf(opponentID));
                                        //ユーザーネーム
                                        Item.add("");
                                        //メディア
                                        Item.add(null);
                                        Item.add(null);
                                        Item.add(null);
                                        Item.add(null);
                                        //カード
                                        Item.add(null);
                                        Item.add(null);
                                        Item.add(null);
                                        Item.add(null);
                                        ListItem listItem = new ListItem(Item);

                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                        listView.setAdapter(adapter);

                                        //ListView下にスクロール
                                        listView.setSelection(listView.getCount() - 1);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onDelete(long l) {

                    }
                };

                Streaming streaming = new Streaming(client);
                try {
                    shutdownable = streaming.user(handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.

                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //用意できた
        //if (zyanken_TextView.getText().toString().length() == 0) {
        //10秒間隔でジャンケン勝負を行う
        sendButton(rock_Button);

        sendButton(caesar_Button);

        sendButton(paper_Button);
        //}
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.cancel();
        if (shutdownable != null) {
            shutdownable.shutdown();
        }
    }


    //ボタンの動作
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


    //DMをPOSTする
    //定期的に動かす？
    private void zyankenPost() {
        String finalMessage = "@" + acct + " " + "//じゃんけん//\n" + zyanken_String;
        sendDirectMessage(finalMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Zyanken.this, "送信したよ！", Toast.LENGTH_SHORT).show();
                //zyanken_TextView.append("送信したよ！\n");
            }
        });
    }

    //最初に使う
    private void startZyanken() {
        //UIすれっど
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //zyanken_TextView.append(sendTime);
            }
        });

        //if (zyanken_TextView.getText().toString().length() == 0) {

        //定期実行
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        timer = new Timer(true);
        task = new TimerTask() {
            @Override
            public void run() {
                //POST
                zyankenPost();
            }
        };
        //第一引数　TimerTast
        //第二引数　タイマー開始時間指定
        //第三引数　実行間隔
        try {
            timer.schedule(task, simpleDateFormat.parse(sendTime), 10000);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // }

    }

    //結果
    //毎回呼ばれる

    /**
     * @param my      自分の選んだ手
     * @param player1 相手の選んだ手
     */
    private void syoubu(String my, String player1) {
        String player_string = "";
        //NullCheck
        if (my != null && player1 != null) {
            //ぐー
            //自分
            if (my.contains("ぐー")) {
                //ぱーが勝ち
                //相手
                if (player1.contains("ぱー")) {
                    zyanken_final = "負けました";
                    player_string = "ぱー";
                    //相手のカウント
                    OpponentCount++;
                }
                if (player1.contains("ちょき")) {
                    zyanken_final = "勝ちました";
                    player_string = "ちょき";
                    //勝カウント
                    myCount++;
                }
                if (player1.contains("ぐー")) {
                    zyanken_final = "あいこだぜ";
                    player_string = "ぐー";
                    aiko++;
                }
            }
            //きょき
            if (my.contains("ちょき")) {
                //ぱーが勝ち
                if (player1.contains("ぐー")) {
                    zyanken_final = "負けました";
                    player_string = "ぐー";
                    //相手のカウント
                    OpponentCount++;
                }
                if (player1.contains("ぱー")) {
                    zyanken_final = "勝ちました";
                    player_string = "ぱー";
                    //勝カウント
                    myCount++;
                }
                if (player1.contains("ちょき")) {
                    zyanken_final = "あいこだぜ";
                    player_string = "ちょき";
                    aiko++;
                }
            }
            //ぱー
            if (my.contains("ぱー")) {
                //ぱーが勝ち
                if (player1.contains("きょき")) {
                    zyanken_final = "負けました";
                    player_string = "ちょき";
                    //相手のカウント
                    OpponentCount++;
                }
                if (player1.contains("ぐー")) {
                    zyanken_final = "勝ちました";
                    player_string = "ぐー";
                    //勝カウント
                    myCount++;
                }
                if (player1.contains("ぱー")) {
                    zyanken_final = "あいこだぜ";
                    player_string = "ぱー";
                    aiko++;
                }
            }
            //総ゲーム数カウント
            totalCount++;

            //自分の選んだものを表示
            //なーんかNullがあるっぽい？
            if (zyanken_final != null) {
                String finalPlayer_string = player_string;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //zyanken_TextView.append("自分 : " + myName + " / " + "相手 : " + opponentName + "\n");
                        //zyanken_TextView.append("自分 : " + zyanken_String + " / " + "相手 : " + finalPlayer_string + "\n");
                        //zyanken_TextView.append(zyanken_final + "\n");

                        String imageString = null;
                        //画像選択
                        if (zyanken_final.contains("勝ちました")) {
                            imageString = "じゃんけん " + "勝ちました";
                        }
                        if (zyanken_final.contains("負けました")) {
                            imageString = "じゃんけん " + "負けました";
                        }
                        if (zyanken_final.contains("あいこだぜ")) {
                            imageString = "じゃんけん " + "あいこだぜ";
                        }

                        //ListView
                        //配列を作成
                        ArrayList<String> Item = new ArrayList<>();
                        //メモとか通知とかに
                        Item.add("じゃんけん");
                        //内容
                        Item.add("自分 : " + zyanken_String + " / " + "相手 : " + finalPlayer_string + "<br>" + zyanken_final);
                        //ユーザー名
                        Item.add(opponentName);
                        //時間、クライアント名等
                        Item.add(null);
                        //Toot ID 文字列版
                        Item.add(null);
                        //アバターURL
                        Item.add(imageString);
                        //アカウントID
                        Item.add(String.valueOf(opponentID));
                        //ユーザーネーム
                        Item.add("");
                        //メディア
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        //カード
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        ListItem listItem = new ListItem(Item);

                        adapter.add(listItem);
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);

                        //状態を更新する
                        setZyankenInfo();

                        //ListView下にスクロール
                        listView.setSelection(listView.getCount() - 1);
                    }
                });
            } else {
                //えらーめせーじ
                String finalPlayer_string1 = player_string;
                errorCount++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String finalPlayer_string = finalPlayer_string1;
                        String imageString = null;
                        imageString = "じゃんけん " + "えらー";

                        //ListView
                        //配列を作成
                        ArrayList<String> Item = new ArrayList<>();
                        //メモとか通知とかに
                        Item.add("じゃんけん");
                        //内容
                        Item.add("相手の情報が取れなかったよ");
                        //ユーザー名
                        Item.add(opponentName);
                        //時間、クライアント名等
                        Item.add(null);
                        //Toot ID 文字列版
                        Item.add(null);
                        //アバターURL
                        Item.add(imageString);
                        //アカウントID
                        Item.add(String.valueOf(opponentID));
                        //ユーザーネーム
                        Item.add("");
                        //メディア
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        //カード
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        Item.add(null);
                        ListItem listItem = new ListItem(Item);

                        adapter.add(listItem);
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);
                        //状態を更新する
                        setZyankenInfo();

                        //ListView下にスクロール
                        listView.setSelection(listView.getCount() - 1);

                        //zyanken_TextView.append("相手の情報が取れなかったよ \n");
                    }
                });

            }
        }
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

    //じゃんけんの状態
    private void setZyankenInfo() {
        //定型文
        String player_my = "自分勝ち ";
        String player_pair = "相手勝ち ";
        String total = "ゲーム数 ";
        String aikoString = "あいこ ";
        String eroor = "エラー ";
        zyanken_TextView_info.setText(total + intToString(totalCount) + " / " + player_my + intToString(myCount) + " / " + player_pair + intToString(OpponentCount) + " / " + aikoString + intToString(aiko) + " / " + eroor + intToString(errorCount));
    }

    //int→String
    private String intToString(int convert) {
        return String.valueOf(convert);
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
                    myAccountacct = jsonObject.getString("acct");
                    //名前（DisplayName+acct）
                    myName = jsonObject.getString("display_name") + " @" + myAccountacct;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Titleber
                            getSupportActionBar().setSubtitle("自分 : " + myName);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
