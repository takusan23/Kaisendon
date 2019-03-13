package io.github.takusan23.kaisendon.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserActivity extends AppCompatActivity {
    private final static String TAG = "TestImageGetter";
    //private TextView mTv;
    String AccessToken = null;
    String Instance = null;


    //がんばってレイアウト作り直すわ
    private LinearLayout user_activity_LinearLayout;

    long account_id;

    String display_name = null;
    String user_account_id = null;
    String avater_url = null;
    String header_url = null;
    String profile = null;
    String create_at = null;
    String remote = null;

    String custom_emoji_src = null;

    TextView profile_textview;

    long follow_id;

    String user_url = null;
    String final_toot_text = null;

    int follow;
    int follower;
    int status_count;

    private JSONArray fieldsJsonArray;
    private String nico_url;

    private ProgressDialog dialog;

    //フォロー/フォローしてない
    boolean following = false;

    private SharedPreferences pref_setting;

    private Snackbar snackbar;

    private Button followButton;
    private ImageView headerImageView;
    private LinearLayout display_name_avatar_LinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        if (dark_mode) {
            setTheme(R.style.Theme_AppCompat_DayNight);
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        //この画面用にNoActionBerなダークテーマを指定している
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            setTheme(R.style.OLED_Theme);
        } else {
            //なにもない
        }


        setContentView(R.layout.activity_user);

        final android.os.Handler handler_1 = new android.os.Handler();

        Intent intent = getIntent();
        account_id = intent.getLongExtra("Account_ID", 0);


        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        //背景
        ImageView background_imageView = findViewById(R.id.user_activity_background_imageview);

        if (pref_setting.getBoolean("background_image", true)) {
            Uri uri = Uri.parse(pref_setting.getString("background_image_path", ""));
            Glide.with(UserActivity.this)
                    .load(uri)
                    .into(background_imageView);
        }


        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f) != 0.0) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        // Backボタンを有効にする
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //くるくる
/*
        dialog = new ProgressDialog(UserActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("ユーザー情報を取得中 \r\n /api/v1/accounts");
*/
//        dialog.show();

        View view = findViewById(android.R.id.content);
        snackbar = Snackbar.make(view, getString(R.string.loading_user_info) + "\r\n /api/v1/accounts", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(UserActivity.this);
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar.show();


        //アカウント情報読み込み
        loadAccount();

    }

    /**
     * アカウント情報を取得する
     */
    private void loadAccount() {
        //APIを叩く
        String url = "https://" + Instance + "/api/v1/accounts/" + String.valueOf(account_id);
        //作成
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        //GETリクエスト
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(response_string);

                    display_name = jsonObject.getString("display_name");
                    user_account_id = jsonObject.getString("acct");
                    profile = jsonObject.getString("note");
                    avater_url = jsonObject.getString("avatar");
                    header_url = jsonObject.getString("header");
                    create_at = jsonObject.getString("created_at");
                    remote = jsonObject.getString("acct");

                    follow = jsonObject.getInt("following_count");
                    follower = jsonObject.getInt("followers_count");
                    status_count = jsonObject.getInt("statuses_count");

                    //nico_url
                    if (!jsonObject.isNull("nico_url")) {
                        nico_url = jsonObject.getString("nico_url");
                    }

                    fieldsJsonArray = jsonObject.getJSONArray("fields");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            setTitle(display_name + " @" + remote);
                            user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout);
                            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                            //カードUIの用な感じに
                            LinearLayout top_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
                            CardView cardView = (CardView) top_linearLayout.findViewById(R.id.cardview);
                            TextView textView = (TextView) top_linearLayout.findViewById(R.id.cardview_textview);
                            //ここについか
                            LinearLayout main_LinearLayout = top_linearLayout.findViewById(R.id.cardview_lineaLayout_main);

                            //名前とかアバター画像とか
                            display_name_avatar_LinearLayout = new LinearLayout(UserActivity.this);
                            display_name_avatar_LinearLayout.setOrientation(LinearLayout.VERTICAL);
                            display_name_avatar_LinearLayout.setGravity(Gravity.CENTER);
                            display_name_avatar_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            display_name_avatar_LinearLayout.setBackground(getDrawable(R.drawable.button_style_white));

                            Drawable title_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_create_black_24dp_black, null);
                            title_icon.setBounds(0, 0, title_icon.getIntrinsicWidth(), title_icon.getIntrinsicHeight());
                            //ImageViewとか
                            headerImageView = new ImageView(UserActivity.this);
                            //今はどっちもMATCH_PARENTになっているけどレイアウトが完成したときにサイズを調整するからおｋ
                            headerImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            Glide.with(UserActivity.this).load(header_url).into(headerImageView);


                            ImageView avatarImageView = new ImageView(UserActivity.this);
                            TextView display_name_TextView = new TextView(UserActivity.this);
                            FrameLayout frameLayout = new FrameLayout(UserActivity.this);
                            //真ん中
                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.CENTER;
                            frameLayout.setLayoutParams(layoutParams);

                            //TextView
                            display_name_TextView.setText(display_name + "\n@" + remote);
                            display_name_TextView.setTextColor(Color.parseColor("#000000"));
                            display_name_TextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            display_name_TextView.setGravity(Gravity.CENTER);
                            //ImageView
                            Glide.with(UserActivity.this).load(avater_url).apply(new RequestOptions().override(200)).into(avatarImageView);
                            avatarImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            //Button
                            followButton = new Button(UserActivity.this);
                            followButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            followButton.setBackground(getDrawable(R.drawable.button_style));
                            followButton.setText(getString(R.string.follow));
                            followButton.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_person_add_black_24dp), null, null, null);
                            //自分の場合は編集ボタンを出す
                            if (getIntent().getBooleanExtra("my", false)) {
                                followButton.setText(getString(R.string.edit));
                                followButton.setTextColor(Color.parseColor("#000000"));
                                followButton.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_create_black_24dp_black), null, null, null);
                                followButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(UserActivity.this, AccountInfoUpdateActivity.class);
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                //フォロー状態取得
                                getFollowInfo();
                                //クリックイベント
                                followButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //フォローしてるかで条件分岐
                                        if (!following) {
                                            if (pref_setting.getBoolean("pref_follow_dialog", true)) {
                                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserActivity.this);
                                                alertDialog.setTitle(R.string.confirmation);
                                                alertDialog.setMessage(display_name + "(@ " + user_account_id + ")" + getString(R.string.follow_message) + "\r\n /api/v1/follows" + "(" + remote + ")");
                                                alertDialog.setPositiveButton(R.string.follow, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //リモートフォローかそうじゃないか
                                                        //@があるかどうか
                                                        if (remote.contains("@")) {
                                                            //remote follow
                                                            remoteFollow(getString(R.string.follow_ok));
                                                        } else {
                                                            follow("follow", getString(R.string.follow_ok));
                                                        }
                                                    }
                                                }).show();
                                            } else {
                                                //リモートフォローかそうじゃないか
                                                //@があるかどうか
                                                if (remote.contains("@")) {
                                                    //remote follow
                                                    remoteFollow(getString(R.string.follow_ok));
                                                } else {
                                                    follow("follow", getString(R.string.follow_ok));
                                                }
                                            }
                                        } else {
                                            //ふぉろーはずし
                                            if (pref_setting.getBoolean("pref_follow_dialog", true)) {
                                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserActivity.this);
                                                alertDialog.setTitle(R.string.confirmation);
                                                alertDialog.setMessage(display_name + "(@ " + user_account_id + ")" + getString(R.string.unfollow_message) + "\r\n /api/v1/follows" + "(" + remote + ")");
                                                alertDialog.setPositiveButton(R.string.follow, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        follow("unfollow", getString(R.string.unfollow_ok));
                                                    }
                                                }).show();
                                            } else {
                                                follow("unfollow", getString(R.string.unfollow_ok));
                                            }
                                        }
                                    }
                                });
                            }

                            display_name_avatar_LinearLayout.addView(avatarImageView);
                            display_name_avatar_LinearLayout.addView(display_name_TextView);
                            display_name_avatar_LinearLayout.addView(followButton);

                            //nico_url Button
                            if (nico_url != null) {
                                Button nicoButton = new Button(UserActivity.this);
                                nicoButton.setText("niconico");
                                nicoButton.setBackground(getDrawable(R.drawable.button_style));
                                nicoButton.setTextColor(Color.parseColor("#000000"));
                                nicoButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                nicoButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
                                        //戻るアイコン
                                        Bitmap back_icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_arrow_back);
                                        //有効
                                        if (chrome_custom_tabs) {
                                            String custom = CustomTabsHelper.getPackageNameToUse(UserActivity.this);
                                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                            CustomTabsIntent customTabsIntent = builder.build();
                                            customTabsIntent.intent.setPackage(custom);
                                            customTabsIntent.launchUrl(UserActivity.this, Uri.parse(nico_url));
                                            //無効
                                        } else {
                                            Uri uri = Uri.parse(nico_url);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(intent);
                                        }
                                    }
                                });
                                display_name_avatar_LinearLayout.addView(nicoButton);
                                headerImageSize();
                            }

                            //FrameLayoutに入れる
                            frameLayout.addView(display_name_avatar_LinearLayout);


                            //今回はタイトルを使ってないので消す
                            ((LinearLayout) top_linearLayout.findViewById(R.id.cardview_linearLayout)).removeView(textView);


                            //追加
                            main_LinearLayout.addView(headerImageView);
                            cardView.addView(frameLayout);


                            //ふぉろーふぉろわーすてーたす
                            //なげえし分けるわ
                            LinearLayout menuCardLinearLayout = followMenuCard();
                            //説明文
                            LinearLayout noteCardLinearLayout = noteCard();
                            //作成時
                            LinearLayout create_atCard = create_atCard();
                            //補足情報
                            LinearLayout fieldsLinearLayout = fieldsCard();
                            //CardView追加
                            user_activity_LinearLayout.addView(top_linearLayout);
                            user_activity_LinearLayout.addView(menuCardLinearLayout);
                            user_activity_LinearLayout.addView(create_atCard);
                            user_activity_LinearLayout.addView(noteCardLinearLayout);
                            if (!fieldsJsonArray.isNull(0)) {
                                user_activity_LinearLayout.addView(fieldsLinearLayout);
                            }
                            snackbar.dismiss();

                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * フォローしている等状況を取得する
     */
    private void getFollowInfo() {
        String url = "https://" + Instance + "/api/v1/accounts/relationships/?stream=user&access_token=" + AccessToken;

        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("id", String.valueOf(account_id));
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
                //JSON化
                //System.out.println("レスポンス : " + response.body().string());
                String response_string = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(response_string);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //フォロー中ってテキスト変更
                            try {
                                if (jsonObject.getBoolean("following")) {
                                    followButton.setText(getResources().getString(R.string.following));
                                    followButton.setTextColor(Color.parseColor("#2196f3"));
                                    Drawable favIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done_black_24dp, null);
                                    favIcon.setTint(Color.parseColor("#2196f3"));
                                    followButton.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                }
                                //フォローされている
                                if (jsonObject.getBoolean("followed_by")) {
                                    followButton.setText(followButton.getText() + "\n" + getResources().getString(R.string.follow_back));
                                    followButton.setTextColor(Color.parseColor("#2196f3"));
                                    Drawable favIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done_all_black_24dp, null);
                                    favIcon.setTint(Color.parseColor("#2196f3"));
                                    followButton.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                }
                                //背景のImageViewの大きさを変更する
                                //なんかボタンのテキストが改行されて二行になると下に白あまりができるので
                                headerImageSize();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });


                    boolean followed_by = jsonObject.getBoolean("followed_by");
                    boolean blocking = jsonObject.getBoolean("blocking");
                    boolean muting = jsonObject.getBoolean("muting");
                    following = jsonObject.getBoolean("following");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ボタンを押したときに移動するやつを指定。
     *
     * @param i １でstatuses、２でfollowing、３でfollower
     */
    private void clickAcitvityMode(int i) {
        Intent follow_intent = new Intent(UserActivity.this, UserFollowActivity.class);
        follow_intent.putExtra("account_id", account_id);
        follow_intent.putExtra("count", follow);
        switch (i) {
            case 1:
                follow_intent.putExtra("follow_follower", 3);
                startActivity(follow_intent);
                break;
            case 2:
                follow_intent.putExtra("follow_follower", 1);
                startActivity(follow_intent);
                break;
            case 3:
                follow_intent.putExtra("follow_follower", 2);
                startActivity(follow_intent);
                break;
        }
    }

    /**
     * フォローする（同じインスタンスの場合）
     *
     * @param followUrl "follow"または"unfollow"
     * @param message   POST終わったときに表示するメッセージ + account_id のToast
     */
    private void follow(String followUrl, final String message) {
        String url = "https://" + Instance + "/api/v1/accounts/" + String.valueOf(account_id) + "/" + followUrl + "?access_token=" + AccessToken;
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserActivity.this, message + " : " + String.valueOf(account_id), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * リモートフォロー（違うインスタンスでのフォロー）
     *
     * @param message POST終わったときに表示するメッセージ
     */
    private void remoteFollow(final String message) {
        String url = "https://" + Instance + "/api/v1/follows?access_token=" + AccessToken;
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
                .add("uri", remote)
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
                        Toast.makeText(UserActivity.this, message + " : " + String.valueOf(account_id), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private LinearLayout followMenuCard() {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //カードUIの用な感じに
        LinearLayout top_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        CardView cardView = (CardView) top_linearLayout.findViewById(R.id.cardview);
        TextView textView = (TextView) top_linearLayout.findViewById(R.id.cardview_textview);
        //名前とか
        textView.setText(getString(R.string.follow) + "/" + getString(R.string.follower));
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_person_black_24dp), null, null, null);
        //ここについか
        LinearLayout main_LinearLayout = top_linearLayout.findViewById(R.id.cardview_lineaLayout_main);
        //めにゅー
        String[] menuList = {getString(R.string.toot) + " : " + String.valueOf(status_count), getString(R.string.follow) + " : " + String.valueOf(follow), getString(R.string.follower) + " : " + String.valueOf(follower)};
        Drawable[] drawableList = {getDrawable(R.drawable.ic_create_black_24dp_black), getDrawable(R.drawable.ic_done_black_24dp_2), getDrawable(R.drawable.ic_done_all_black_24dp_2)};
        //forで回すか
        for (int i = 0; i < 3; i++) {
            TextView menuTextView = new TextView(UserActivity.this);
            menuTextView.setText(menuList[i]);
            menuTextView.setTextSize(24);
            menuTextView.setCompoundDrawablesWithIntrinsicBounds(drawableList[i], null, null, null);
            menuTextView.setBackground(getDrawable(R.drawable.button_style));
            menuTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            main_LinearLayout.addView(menuTextView);
            //クリックイベント
            int finalI = i;
            menuTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAcitvityMode(finalI + 1);
                }
            });
        }
        return top_linearLayout;
    }


    private LinearLayout noteCard() {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //カードUIの用な感じに
        LinearLayout top_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        CardView cardView = (CardView) top_linearLayout.findViewById(R.id.cardview);
        TextView textView = (TextView) top_linearLayout.findViewById(R.id.cardview_textview);
        //名前とか
        textView.setText(getString(R.string.note));
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_create_black_24dp_black), null, null, null);
        //ここについか
        LinearLayout main_LinearLayout = top_linearLayout.findViewById(R.id.cardview_lineaLayout_main);
        //note
        TextView noteTextView = new TextView(UserActivity.this);
        noteTextView.setAutoLinkMask(Linkify.WEB_URLS);
        noteTextView.setText(Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT));
        main_LinearLayout.addView(noteTextView);

        return top_linearLayout;
    }

    private LinearLayout create_atCard() {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //カードUIの用な感じに
        LinearLayout top_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        CardView cardView = (CardView) top_linearLayout.findViewById(R.id.cardview);
        TextView textView = (TextView) top_linearLayout.findViewById(R.id.cardview_textview);
        //名前とか
        textView.setText(getString(R.string.create_at_date));
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_date_range_black_24dp), null, null, null);
        //ここについか
        LinearLayout main_LinearLayout = top_linearLayout.findViewById(R.id.cardview_lineaLayout_main);
        TextView dateTextView = new TextView(UserActivity.this);
        dateTextView.setText(dateFormat(create_at));
        dateTextView.setTextSize(18);

        main_LinearLayout.addView(dateTextView);

        return top_linearLayout;
    }

    private String dateFormat(String dateText) {
        String dateString = dateText;
        boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
        if (japan_timeSetting) {
            //時差計算？
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            //日本用フォーマット
            SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
            try {
                Date date = simpleDateFormat.parse(dateText);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                //9時間足して日本時間へ
                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                dateString = japanDateFormat.format(calendar.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            dateString = dateText;
        }
        return dateString;
    }

    private LinearLayout fieldsCard() {
        user_activity_LinearLayout = findViewById(R.id.user_activity_linearLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //カードUIの用な感じに
        LinearLayout top_linearLayout = (LinearLayout) inflater.inflate(R.layout.cardview_layout, null);
        CardView cardView = (CardView) top_linearLayout.findViewById(R.id.cardview);
        TextView textView = (TextView) top_linearLayout.findViewById(R.id.cardview_textview);
        //名前とか
        textView.setText(getString(R.string.fields_attributes));
        textView.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_playlist_add_black_24dp), null, null, null);
        //ここについか
        LinearLayout main_LinearLayout = top_linearLayout.findViewById(R.id.cardview_lineaLayout_main);
        //forで回す
        for (int i = 0; i < fieldsJsonArray.length(); i++) {
            //ぱーす
            try {

                String name = fieldsJsonArray.getJSONObject(i).getString("name");
                String value = fieldsJsonArray.getJSONObject(i).getString("value");
                //LinearLayout
                LinearLayout fieldsLinearLayout = new LinearLayout(UserActivity.this);
                fieldsLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                fieldsLinearLayout.setOrientation(LinearLayout.VERTICAL);
                fieldsLinearLayout.setBackground(getDrawable(R.drawable.button_style));
                //TextView
                TextView nameTextView = new TextView(UserActivity.this);
                TextView valueTextView = new TextView(UserActivity.this);
                TextView created_atTextView = new TextView(UserActivity.this);
                nameTextView.setTextSize(18);
                valueTextView.setTextSize(18);
                valueTextView.setAutoLinkMask(Linkify.WEB_URLS);
                nameTextView.setText(name);
                valueTextView.setText(Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT));
                //入れる
                fieldsLinearLayout.addView(nameTextView);
                //認証済みのときはverified_atの値を取得する
                if (!fieldsJsonArray.getJSONObject(i).isNull("verified_at")) {
                    String verified_at_string = fieldsJsonArray.getJSONObject(i).getString("verified_at");
                    fieldsLinearLayout.setBackground(getDrawable(R.drawable.button_style_green));
                    //今までの方法だと使えない形なのでちょっと手を加える
                    created_atTextView.setText(getString(R.string.verification_text) + " : " + timeFormat(verified_at_string.replace("+00:00", "Z")));
                    created_atTextView.setTextSize(18);
                    //ちぇっくまーく
                    Drawable doneIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done_black_24dp, null);
                    doneIcon.setTint(Color.parseColor("#3c8e37"));
                    created_atTextView.setCompoundDrawablesWithIntrinsicBounds(doneIcon, null, null, null);
                    //入れる
                    fieldsLinearLayout.addView(created_atTextView);
                }
                fieldsLinearLayout.addView(valueTextView);
                main_LinearLayout.addView(fieldsLinearLayout);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return top_linearLayout;
    }

    private void headerImageSize() {
        //背景のImageViewの大きさを変更する
        //なんかボタンのテキストが改行されて二行になると下に白あまりができるので
        display_name_avatar_LinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                headerImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                headerImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, display_name_avatar_LinearLayout.getHeight()));
                headerImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                headerImageView.invalidate();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];
            Log.d(TAG, "doInBackground " + source);
            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute drawable " + mDrawable);
            Log.d(TAG, "onPostExecute bitmap " + bitmap);
            if (bitmap != null) {
                BitmapDrawable d = new BitmapDrawable(bitmap);
                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, 40, 40);
                mDrawable.setLevel(1);
                // i don't know yet a better way to refresh TextView
                // mTv.invalidate() doesn't work as expected
                CharSequence t = profile_textview.getText();
                profile_textview.setText(t);
            }
        }
    }

    //時差計算
    private String timeFormat(String time) {
        String timeReturn = time;
        boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
        if (japan_timeSetting) {
            //時差計算？
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            //日本用フォーマット
            SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
            try {
                Date date = simpleDateFormat.parse(time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                //9時間足して日本時間へ
                calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                timeReturn = japanDateFormat.format(calendar.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            timeReturn = time;
        }
        return timeReturn;
    }

}