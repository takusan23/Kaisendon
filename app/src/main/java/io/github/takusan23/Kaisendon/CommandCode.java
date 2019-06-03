package io.github.takusan23.Kaisendon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.github.takusan23.Kaisendon.Omake.KaisendonLife;
import io.github.takusan23.Kaisendon.Zyanken.ZyankenMenu;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommandCode {


    //コマンドをここに書いていくと思うよ

    /**
     * @param editText          　値が正しいか確認
     * @param toot_LinearLayout 　コマンド実行ボタン生成
     * @param command_Button    　コマンド実行ボタン
     * @param commandText       　コマンド実行文
     * @param prefKey           　プリファレンスに保存する値
     */
    public static void commandSet(Activity activity, EditText editText, LinearLayout toot_LinearLayout, Button command_Button, String commandText, String prefKey) {
        //コマンド機能
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        if (editText.getText().toString().contains(commandText)) {
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            command_Button.setLayoutParams(layoutParams);
            command_Button.setText(R.string.command_run);
            command_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //クローズでソフトキーボード非表示
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        if (activity.getCurrentFocus() != null) {
                            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                        }
                    }

                    //スナックばー
                    Snackbar.make(v, R.string.command_run_message, Snackbar.LENGTH_SHORT).setAction(R.string.run, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //preference書かないコマンド
                            //設定変更
                            SharedPreferences.Editor editor = pref_setting.edit();
                            //モード切替
                            if (pref_setting.getBoolean(prefKey, false)) {
                                //ONのときはOFFにする
                                editor.putBoolean(prefKey, false);
                                editor.apply();
                            } else {
                                //OFFのときはONにする
                                editor.putBoolean(prefKey, true);
                                editor.apply();
                            }
                        }
                    }).show();
                }
            });
            toot_LinearLayout.addView(command_Button, 0);
        } else {
            toot_LinearLayout.removeView(command_Button);
        }
    }

    /**
     * @param context           いろいろ
     * @param editText          　値が正しいか確認
     * @param toot_LinearLayout 　コマンド実行ボタン生成
     * @param command_Button    　コマンド実行ボタン
     * @param commandText       　コマンド実行文
     * @param commandType       コマンド詳細（fav/btコマンドはタイムライン名）
     */
    public static void commandSetNotPreference(Activity activity, Context context, EditText editText, LinearLayout toot_LinearLayout, Button command_Button, String commandText, String commandType) {
        //コマンド機能
        if (editText.getText().toString().contains(commandText)) {
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            command_Button.setLayoutParams(layoutParams);
            command_Button.setText(R.string.command_run);
            command_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //スナックばー
                    Snackbar.make(v, R.string.command_run_message, Snackbar.LENGTH_SHORT).setAction(R.string.run, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //クローズでソフトキーボード非表示
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                if (activity.getCurrentFocus() != null) {
                                    imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                                }
                            }
                            //レートリミット
                            if (commandType.contains("rate-limit")) {
                                getMyRateLimit(context, editText);
                            }
                            //じゃんけん
                            if (commandType.contains("じゃんけん")) {
                                Intent intent = new Intent(context, ZyankenMenu.class);
                                context.startActivity(intent);
                            }
                            //favコマンド
                            if (commandType.contains("home")) {
                                favCommand("home");
                            }
                            if (commandType.contains("local")) {
                                favCommand("local");
                            }
                            //進捗機能
                            if (commandType.contains("progress")) {
                                saveSharedPreference(context, editText, "progress_mode");
                            }
                            //起動カウント
                            if (commandType.contains("lunch_bonus")) {
                                saveSharedPreference(context, editText, "lunch_bonus_mode");
                            }
                            //Life機能
                            if (commandType.contains("life")) {
                                context.startActivity(new Intent(context, KaisendonLife.class));
                            }
                            //戻す
                            editText.setText("");
                            toot_LinearLayout.removeView(command_Button);
                        }
                    }).show();
                }
            });
            toot_LinearLayout.addView(command_Button, 0);
        } else {
            //toot_LinearLayout.removeView(command_Button);
        }
    }

    /*保存*/
    public static void saveSharedPreference(Context context, EditText editText, String name) {
        SharedPreferences pref_setting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref_setting.edit();
        if (!pref_setting.getBoolean(name, false)) {
            editor.putBoolean(name, true);
        } else {
            editor.putBoolean(name, false);
        }
        editor.apply();
        editText.setText("");
    }

    //れーとりみっとかくにん

    /**
     * @param editText トゥートテキストボックス
     */
    private static void getMyRateLimit(Context context, EditText editText) {
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        //アクセストークンがあってるかユーザー情報を取得して確認する
        String AccessToken, instance;
        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            instance = pref_setting.getString("pref_mastodon_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            instance = pref_setting.getString("main_instance", "");
        }
        String url = "https://" + instance + "/api/v1/accounts/verify_credentials/?access_token=" + AccessToken;
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
                //レスポンスヘッダー
                Headers headers = response.headers();
                //残機確認
                String rateLimit = headers.get("x-ratelimit-limit");
                String rateLimit_nokori = headers.get("x-ratelimit-remaining");
                String rateLimit_time = headers.get("x-ratelimit-reset");
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editText.append("\n");
                        editText.append(context.getString(R.string.ratelimit_limit) + "(x-ratelimit-limit) : " + rateLimit + "\n");
                        editText.append(context.getString(R.string.ratelimit_remaining) + "(x-ratelimit-remaining) : " + rateLimit_nokori + "\n");
                        editText.append(context.getString(R.string.ratelimit_reset) + "(x-ratelimit-reset) : " + rateLimit_time + "\n");
                    }
                });
            }
        });
    }

    //ふぁぼる
    private static void favCommand(String timeline) {
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
        String AccessToken, instance;
        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {
            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            instance = pref_setting.getString("pref_mastodon_instance", "");
        } else {
            AccessToken = pref_setting.getString("main_token", "");
            instance = pref_setting.getString("main_instance", "");
        }

        String url = "https://" + instance + "/api/v1/timelines/" + timeline + "/?access_token=" + AccessToken + "&limit=40";
        //ローカルTL
        if (timeline.contains("local")) {
            url = "https://" + instance + "/api/v1/timelines/" + "public" + "/?access_token=" + AccessToken + "&limit=40" + "&local=true";
        }
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
                    String response_string = response.body().string();
                    //JSONArray
                    JSONArray jsonArray = new JSONArray(response_string);
                    //ぱーす
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        //ID取得
                        String id = jsonObject.getString("id");
                        //System.out.println("れすぽんす : " + id);
                        //Favouriteする
                        String url = "https://" + instance + "/api/v1/statuses/" + id + "/favourite/?access_token=" + AccessToken;
                        //ぱらめーたー
                        RequestBody requestBody = new FormBody.Builder()
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

                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}