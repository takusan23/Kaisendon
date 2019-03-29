package io.github.takusan23.kaisendon.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class KonoAppNiTuite extends AppCompatActivity {

    private String release_name_2 = "まぐろ丼";
    private String release_ver_2 = "2.0";
    private String release_name_3 = "べーた丼";
    private String release_ver_3 = "3.0(仮)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        if (dark_mode) {
            setTheme(R.style.Theme_AppCompat_DayNight);
        }

        //OLEDように真っ暗のテーマも使えるように
        //この画面用にNoActionBerなダークテーマを指定している
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            setTheme(R.style.OLED_Theme_Home);
        }
        setContentView(R.layout.activity_kono_app_ni_tuite);

        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);

        TextView KonoAppTextView = findViewById(R.id.konoAppTextview);
        TextView KonoAppTextView_2 = findViewById(R.id.konoAppTextview_2);

        TextView producerTextview = findViewById(R.id.konoAppTextview_producer);
        ImageView producerImageview = findViewById(R.id.konoAppImageview_producer);
        ImageView producerIconApp = findViewById(R.id.konoAppIcon_producer_AppLunch);
        ImageView producerIconBrowser = findViewById(R.id.konoAppIcon_producer_Browser);
        LinearLayout producerLinearlayout = findViewById(R.id.linearlayout_producer);
        LinearLayout titleLinearLayout = findViewById(R.id.konoApp_title_linearlayout);

        Button githubButton = findViewById(R.id.githubButton);

        Button mastodon_contact = findViewById(R.id.mastodon_contact);
        Button twitter_contact = findViewById(R.id.twitter_contact);

        TextView document_textview = findViewById(R.id.wiki);
        Button document_button = findViewById(R.id.wiki_button);
        Button wear_document_Button = findViewById(R.id.wear_document_button);


        KonoAppTextView_2.setText(getString(R.string.version) + " " + release_ver_3 + "\r\n" + release_name_3);
        githubButton.setText(getString(R.string.sourceCode) + ": " + "GitHub");

        document_button.setText(getString(R.string.document) + "\n" + "GitHub Wiki");

        setTitle(R.string.konoappnituite);


        //製作者を埋める
        String url = "https://friends.nico/api/v1/accounts/6280";
        //作成
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

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

                    String display_name = jsonObject.getString("display_name");
                    String acct = jsonObject.getString("acct");
                    String avatar = jsonObject.getString("avatar");
                    String userURL = jsonObject.getString("url");
                    long account_id = jsonObject.getLong("id");


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //ブラウザで起動
                            producerIconBrowser.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (chrome_custom_tabs) {
                                        Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                                        String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                        CustomTabsIntent customTabsIntent = builder.build();
                                        customTabsIntent.intent.setPackage(custom);
                                        customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(userURL));
                                    } else {
                                        Uri uri = Uri.parse(userURL);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                    }
                                }
                            });

                            //アプリ内で起動
                            producerIconApp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(KonoAppNiTuite.this, UserActivity.class);
                                    //IDを渡す
                                    intent.putExtra("Account_ID", account_id);
                                    startActivity(intent);
                                }
                            });

                            producerTextview.setText(display_name + "\r\n" + acct + "@friends.nico");
                            Glide.with(KonoAppNiTuite.this).load(avatar).into(producerImageview);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String githubUrl = "https://github.com/takusan23/Kaisendon";
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(githubUrl));
                } else {
                    Uri uri = Uri.parse(githubUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }

            }
        });

        wear_document_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String githubUrl = "https://github.com/takusan23/KaisendonWear/wiki";
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(githubUrl));
                } else {
                    Uri uri = Uri.parse(githubUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });


        twitter_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String twitterUrl = "https://twitter.com/takusan__23";
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(twitterUrl));
                } else {
                    Uri uri = Uri.parse(twitterUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        mastodon_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toot = new Intent(KonoAppNiTuite.this, TootSnackberActivity.class);
                toot.putExtra("contact", "@takusan_23@friends.nico ");
                startActivity(toot);
            }
        });


/*
        //うらわざ？
        titleLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(KonoAppNiTuite.this, "friends.nico", Toast.LENGTH_SHORT).show();
                if (pref_setting.getBoolean("pref_friends_nico_mode", false)) {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_friends_nico_mode", false);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.putBoolean("pref_friends_nico_mode", true);
                    editor.apply();
                }
                return false;
            }

        });
*/


        document_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String documentUrl = "https://github.com/takusan23/Kaisendon/wiki";
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(KonoAppNiTuite.this.getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(KonoAppNiTuite.this);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(KonoAppNiTuite.this, Uri.parse(documentUrl));
                } else {
                    Uri uri = Uri.parse(documentUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

    }
}
