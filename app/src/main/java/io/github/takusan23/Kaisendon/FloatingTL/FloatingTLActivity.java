package io.github.takusan23.Kaisendon.FloatingTL;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.Kaisendon.CustomMenu.CustomMenuTimeLine;
import io.github.takusan23.Kaisendon.Home;
import io.github.takusan23.Kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.github.takusan23.Kaisendon.Preference_ApplicationContext.getContext;

public class FloatingTLActivity extends AppCompatActivity {

    private EditText editText;
    private ImageButton postImageButton;
    private JSONObject jsonObject;
    private String isMisskey;
    private boolean pip_mode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.float_tl_layout);

        editText = findViewById(R.id.floating_tl_edittext);
        postImageButton = findViewById(R.id.floating_tl_post_button);
        pip_mode = getIntent().getBooleanExtra("pip", false);

        //Fragmentにわたすやつ
        try {
            jsonObject = new JSONObject(getIntent().getStringExtra("json"));
            String json = jsonObject.toString();
            String name = jsonObject.getString("name");
            String content = jsonObject.getString("content");
            String instance = jsonObject.getString("instance");
            String access_token = jsonObject.getString("access_token");
            String image_load = jsonObject.getString("image_load");
            String dialog = jsonObject.getString("dialog");
            String dark_mode = jsonObject.getString("dark_mode");
            String position = jsonObject.getString("position");
            String streaming = jsonObject.getString("streaming");
            String subtitle = jsonObject.getString("subtitle");
            String image_url = jsonObject.getString("image_url");
            String background_transparency = jsonObject.getString("background_transparency");
            String background_screen_fit = jsonObject.getString("background_screen_fit");
            String quick_profile = jsonObject.getString("quick_profile");
            String toot_counter = jsonObject.getString("toot_counter");
            String custom_emoji = jsonObject.getString("custom_emoji");
            String gif = jsonObject.getString("gif");
            String font = jsonObject.getString("font");
            String one_hand = jsonObject.getString("one_hand");
            String misskey = jsonObject.getString("misskey");
            String misskey_username = jsonObject.getString("misskey_username");
            String setting = jsonObject.getString("setting");
            //Fragmentに詰める
            Bundle bundle = new Bundle();
            bundle.putString("misskey", misskey);
            bundle.putString("name", name);
            bundle.putString("content", content);
            bundle.putString("instance", instance);
            bundle.putString("access_token", access_token);
            bundle.putString("image_load", image_load);
            bundle.putString("dialog", dialog);
            bundle.putString("dark_mode", dark_mode);
            bundle.putString("position", position);
            bundle.putString("streaming", streaming);
            bundle.putString("subtitle", subtitle);
            bundle.putString("image_url", image_url);
            bundle.putString("background_transparency", background_transparency);
            bundle.putString("background_screen_fit", background_screen_fit);
            bundle.putString("quick_profile", quick_profile);
            bundle.putString("toot_counter", toot_counter);
            bundle.putString("custom_emoji", custom_emoji);
            bundle.putString("gif", gif);
            bundle.putString("font", font);
            bundle.putString("one_hand", one_hand);
            bundle.putString("misskey_username", misskey_username);
            bundle.putString("setting", setting);
            bundle.putString("json", json);
            CustomMenuTimeLine customMenuTimeLine = new CustomMenuTimeLine();
            customMenuTimeLine.setArguments(bundle);
            //Fragmentおく？
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            String tag = "";
            //PiPモードで簡略表示するため
            if (!Build.VERSION.CODENAME.contains("Q") || pip_mode) {
                tag = "pip_fragment";
            }
            fragmentTransaction.replace(R.id.float_tl_linearlayout, customMenuTimeLine, tag);
            fragmentTransaction.commit();
            isMisskey = jsonObject.getString("misskey");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //投稿
        setStatusPOST();

        //Android 10以前の端末はPictureInPictureモードで起動する
        //Nougatユーザーは神バージョンだけど使えないよ😢
        //TLQuickSettingSnackbarの中にQ以外の場合はpipがtrueになることになる。
        if (pip_mode) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                //1:1の大きさにする設定
                Rational aspectRatio = new Rational(1, 1);
                PictureInPictureParams.Builder PIPParamsBuilder = null;
                PIPParamsBuilder = new PictureInPictureParams.Builder();
                PIPParamsBuilder.setAspectRatio(aspectRatio).build();
                //Mastodonのときは投稿ボタンを表示する
                if (!Boolean.valueOf(isMisskey)) {
                    ArrayList<RemoteAction> toot_RemoteAction = new ArrayList<>();
                    Intent intent = new Intent(getContext(), PiPBroadcastReciver.class);
                    RemoteAction remoteAction = new RemoteAction(Icon.createWithResource(getContext(), R.drawable.ic_create_black_24dp), getString(R.string.toot), "Toot", PendingIntent.getBroadcast(getContext(), 114, intent, 0));
                    toot_RemoteAction.add(remoteAction);
                    PIPParamsBuilder.setActions(toot_RemoteAction);
                }
                enterPictureInPictureMode(PIPParamsBuilder.build());
                //投稿ボタンを消す
                LinearLayout edit_LinearLayout = (LinearLayout) postImageButton.getParent();
                ((LinearLayout) edit_LinearLayout.getParent()).removeView(edit_LinearLayout);
            }
        }
    }

    /*とうこう*/
    private void setStatusPOST() {
        postImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.note_create_message), Snackbar.LENGTH_LONG).setAction(getString(R.string.toot_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (Boolean.valueOf(jsonObject.getString("misskey"))) {
                                misskeyStatusPOST();
                            } else {
                                mastodonStatusPOST();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).show();
            }
        });
    }

    /*Misskey*/
    private void misskeyStatusPOST() {
        try {
            String token = jsonObject.getString("access_token");
            String instance = jsonObject.getString("instance");
            String url = "https://" + instance + "/api/notes/create";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("i", token);
                jsonObject.put("text", editText.getText().toString());
                jsonObject.put("viaMobile", true);//スマホからなので一応
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody_json)
                    .build();
            //POST
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        //失敗
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FloatingTLActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FloatingTLActivity.this, getString(R.string.toot_ok), Toast.LENGTH_SHORT).show();
                                editText.setText("");
                            }
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*MastodonStatusPOST*/
    private void mastodonStatusPOST() {
        String AccessToken = null;
        String Instance = null;
        try {
            AccessToken = jsonObject.getString("access_token");
            Instance = jsonObject.getString("instance");
            String url = "https://" + Instance + "/api/v1/statuses/?access_token=" + AccessToken;
            JSONObject postJsonObject = new JSONObject();
            postJsonObject.put("status", editText.getText().toString());
            RequestBody requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postJsonObject.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody_json)
                    .build();
            //POST
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        //失敗
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FloatingTLActivity.this, getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FloatingTLActivity.this, getString(R.string.toot_ok), Toast.LENGTH_SHORT).show();
                                editText.setText("");
                            }
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*PiPからアプリ起動したときの処理*/
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (!isInPictureInPictureMode) {
            finishAndRemoveTask();
            startActivity(new Intent(getContext(), Home.class));
        }
    }

}
