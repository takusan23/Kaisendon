package io.github.takusan23.kaisendon.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.CommandCode;
import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.github.takusan23.kaisendon.Preference_ApplicationContext.getContext;

public class TootSnackberActivity extends AppCompatActivity {

    Snackbar toot_snackbar;
    SharedPreferences pref_setting;
    LinearLayout media_LinearLayout;
    Button post_button;
    EditText toot_EditText;
    //公開範囲
    String toot_area = "public";
    //画像
    int count = 0;
    ArrayList<String> media_list = new ArrayList<>();
    ArrayList<String> post_media_id = new ArrayList<>();
    //名前とか
    String snackber_Name = "";
    String Instance;
    String snackber_Avatar;
    ImageView snackberAccountAvaterImageView;
    TextView snackberAccount_TextView;
    MenuBuilder account_menuBuilder;
    MenuPopupHelper account_optionsMenu;
    LinearLayout account_LinearLayout;
    //マルチアカウント読み込み用
    ArrayList<String> multi_account_instance;
    ArrayList<String> multi_account_access_token;
    //文字数カウント
    int tootTextCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toot_snackber);

        //設定のプリファレンス
        pref_setting = PreferenceManager.getDefaultSharedPreferences(this);
        //スナックバー生成
        tootSnackBer();
        //表示
        toot_snackbar.show();
        //ふぉーかす
        toot_EditText.requestFocus();
        //キーボード表示
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }


        //共有受け取る
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null && toot_snackbar.isShown()) {
                toot_EditText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                //URL
                CharSequence text = extras.getCharSequence(Intent.EXTRA_TEXT);
                //タイトル
                CharSequence title = extras.getCharSequence(Intent.EXTRA_SUBJECT);
                //EXTRA TEXTにタイトルが含まれているかもしれない？
                //含まれているときは消す
                text = text.toString().replace(title, "");
                if (title != null) {
                    toot_EditText.append(title);
                }
                if (text != null) {
                    toot_EditText.append("\n");
                    toot_EditText.append(text);
                }
            }
        }

        //作者に連絡
        try {
            toot_EditText.append(getIntent().getStringExtra("contact"));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }


        //FABで閉じれるようにする
        FloatingActionButton fab = findViewById(R.id.toot_snackber_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAndRemoveTask();
                //クローズでソフトキーボード非表示
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    if (TootSnackberActivity.this.getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(TootSnackberActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();

                //ファイルパスとか
                String filePath = getPath(selectedImage);
                String file_extn = filePath.substring(filePath.lastIndexOf(".") + 1);
                File file = new File(filePath);
                String finalPath = "file:\\\\" + filePath;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);

                if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
                    //配列に入れる
                    media_list.add(selectedImage.toString());
                    media_LinearLayout.removeAllViews();
                    //配列に入れた要素をもとにImageViewを生成する
                    for (int i = 0; i < media_list.size(); i++) {
                        ImageView imageView = new ImageView(TootSnackberActivity.this);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setImageURI(Uri.parse(media_list.get(i)));
                        imageView.setTag(i);
                        media_LinearLayout.addView(imageView);
                        //押したとき
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(TootSnackberActivity.this, "位置 : " + String.valueOf((Integer) imageView.getTag()), Toast.LENGTH_SHORT).show();
                                //要素の削除
                                //media_list.remove(0);
                                //再生成
                                ImageViewClick();
                            }
                        });
                    }

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
                    //とぅーとする
                    String finalAccessToken = AccessToken;
                    String finalInstance = Instance;
                    post_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //クローズでソフトキーボード非表示
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                if (TootSnackberActivity.this.getCurrentFocus() != null) {
                                    imm.hideSoftInputFromWindow(TootSnackberActivity.this.getCurrentFocus().getWindowToken(), 0);
                                }
                            }
                            //配列からUriを取り出す
                            for (int i = 0; i < media_list.size(); i++) {
                                //ひつようなやつ
                                String filePath_post = getPath(Uri.parse(media_list.get(i)));
                                String file_extn_post = filePath_post.substring(filePath_post.lastIndexOf(".") + 1);
                                File file_post = new File(filePath_post);

                                //画像Upload
                                OkHttpClient okHttpClient = new OkHttpClient();
                                //えんどぽいんと
                                String url_link = "https://" + finalInstance + "/api/v1/media/";
                                //ぱらめーたー
                                RequestBody requestBody = new MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("file", file_post.getName(), RequestBody.create(MediaType.parse("image/" + file_extn_post), file_post))
                                        .addFormDataPart("access_token", finalAccessToken)
                                        .build();
                                //じゅんび
                                Request request = new Request.Builder()
                                        .url(url_link)
                                        .post(requestBody)
                                        .build();
                                //POST実行
                                okHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {

                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String response_string = response.body().string();
                                        //System.out.println("画像POST : " + response_string);

                                        try {
                                            JSONObject jsonObject = new JSONObject(response_string);
                                            String media_id_long = jsonObject.getString("id");
                                            //配列に格納
                                            post_media_id.add(media_id_long);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            //画像がPOSTできたらトゥート実行
                            //Tootする
                            //確認SnackBer
                            Snackbar.make(v, R.string.toot_dialog, Snackbar.LENGTH_SHORT).setAction(R.string.toot, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //なんかアップロードしてないときある？
                                    if (media_list.size() == post_media_id.size()) {
                                        String url = "https://" + finalInstance + "/api/v1/statuses/?access_token=" + finalAccessToken;
                                        //ぱらめーたー
                                        MultipartBody.Builder form = new MultipartBody.Builder();
                                        form.addFormDataPart("status", toot_EditText.getText().toString());
                                        form.addFormDataPart("visibility", toot_area);
                                        //画像
                                        for (int i = 0; i < post_media_id.size(); i++) {
                                            form.addFormDataPart("media_ids[]", post_media_id.get(i));
                                        }
                                        form.build();
                                        //ぱらめーたー
                                        RequestBody requestBody = form.build();
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
                                                //System.out.println("レスポンス : " + response.body().string());
                                                toot_snackbar.dismiss();
                                                //EditTextを空にする
                                                toot_EditText.setText("");
                                                tootTextCount = 0;
                                                //配列を空にする
                                                media_list.clear();
                                                post_media_id.clear();
                                                media_LinearLayout.removeAllViews();
                                                //アプリを閉じる
                                                finishAndRemoveTask();
                                            }
                                        });
                                    }
                                }
                            }).show();

                        }
                    });
                }
            }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

    private void ImageViewClick() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
        media_LinearLayout.removeAllViews();
        //配列に入れた要素をもとにImageViewを生成する
        for (int i = 0; i < media_list.size(); i++) {
            ImageView imageView = new ImageView(TootSnackberActivity.this);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageURI(Uri.parse(media_list.get(i)));
            imageView.setTag(i);
            media_LinearLayout.addView(imageView);
            //押したとき
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(TootSnackberActivity.this, "位置 : " + String.valueOf((Integer) imageView.getTag()), Toast.LENGTH_SHORT).show();
                    //要素の削除
                    //なんだこのくそｇｍコードは
                    //removeにgetTagそのまま書くとなんかだめなんだけど何これ意味不
                    if ((Integer) imageView.getTag() == 0) {
                        media_list.remove(0);
                    } else if ((Integer) imageView.getTag() == 1) {
                        media_list.remove(1);
                    } else if ((Integer) imageView.getTag() == 2) {
                        media_list.remove(2);
                    } else if ((Integer) imageView.getTag() == 3) {
                        media_list.remove(3);
                    }
                    //再生成
                    ImageViewClick();
                }
            });
        }
    }


    @SuppressLint("RestrictedApi")
    private void tootSnackBer() {

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

        View view = findViewById(R.id.toot_snackber_coordinator);
        toot_snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);
        //Snackber生成
        ViewGroup snackBer_viewGrop = (ViewGroup) toot_snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        //LinearLayout動的に生成
        LinearLayout snackber_LinearLayout = new LinearLayout(TootSnackberActivity.this);
        snackber_LinearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams warp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        snackber_LinearLayout.setLayoutParams(warp);
        //テキストボックス
        //Materialふうに
        LinearLayout toot_textBox_LinearLayout = new LinearLayout(TootSnackberActivity.this);
        //レイアウト読み込み
        getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout);
        toot_EditText = getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_editText);
        //ヒント
        ((TextInputLayout) getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_TextInputLayout)).setHint(getString(R.string.imananisiteru));
        //色
        ((TextInputLayout) getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_TextInputLayout)).setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));
        ((TextInputLayout) getLayoutInflater().inflate(R.layout.textinput_edittext, toot_textBox_LinearLayout).findViewById(R.id.name_TextInputLayout)).setBoxStrokeColor(Color.parseColor("#ffffff"));
        toot_EditText.setTextColor(Color.parseColor("#ffffff"));
        toot_EditText.setHintTextColor(Color.parseColor("#ffffff"));

        //ボタン追加用LinearLayout
        LinearLayout toot_Button_LinearLayout = new LinearLayout(TootSnackberActivity.this);
        toot_Button_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        toot_Button_LinearLayout.setLayoutParams(warp);

        //Button
        //画像追加
        ImageButton add_image_Button = new ImageButton(TootSnackberActivity.this, null, 0, R.style.Widget_AppCompat_Button_Borderless);
        add_image_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        add_image_Button.setImageDrawable(getDrawable(R.drawable.ic_image_black_24dp));
        add_image_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int REQUEST_PERMISSION = 1000;
                //ストレージ読み込みの権限があるか確認
                //許可してないときは許可を求める
                if (ContextCompat.checkSelfPermission(TootSnackberActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(TootSnackberActivity.this)
                            .setTitle(getString(R.string.permission_dialog_titile))
                            .setMessage(getString(R.string.image_upload_storage_permisson))
                            .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //権限をリクエストする
                                    requestPermissions(
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            REQUEST_PERMISSION);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                } else {
                    //onActivityResultで受け取れる
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                }
            }
        });

        //公開範囲選択用Button
        ImageButton toot_area_Button = new ImageButton(TootSnackberActivity.this, null, 0, R.style.Widget_AppCompat_Button_Borderless);
        toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp));
        toot_area_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        //toot_area_Button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0);

        //ポップアップメニュー作成
        MenuBuilder menuBuilder = new MenuBuilder(TootSnackberActivity.this);
        MenuInflater inflater = new MenuInflater(TootSnackberActivity.this);
        inflater.inflate(R.menu.toot_area_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(TootSnackberActivity.this, menuBuilder, toot_area_Button);
        optionsMenu.setForceShowIcon(true);

        //ポップアップメニューを展開する
        toot_area_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //表示
                optionsMenu.show();
                //押したときの反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        //公開（全て）
                        if (menuItem.getTitle().toString().contains(getString(R.string.visibility_public))) {
                            toot_area = "public";
                            toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_public_black_24dp));
                        }
                        //未収載（TL公開なし・誰でも見れる）
                        if (menuItem.getTitle().toString().contains(getString(R.string.visibility_unlisted))) {
                            toot_area = "unlisted";
                            toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_done_all_black_24dp));
                        }
                        //非公開（フォロワー限定）
                        if (menuItem.getTitle().toString().contains(getString(R.string.visibility_private))) {
                            toot_area = "private";
                            toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_lock_open_black_24dp));
                        }
                        //ダイレクト（指定したアカウントと自分）
                        if (menuItem.getTitle().toString().contains(getString(R.string.visibility_direct))) {
                            toot_area = "direct";
                            toot_area_Button.setImageDrawable(getDrawable(R.drawable.ic_assignment_ind_black_24dp));
                        }

                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });

            }
        });


        //投稿用LinearLayout
        LinearLayout toot_LinearLayout = new LinearLayout(TootSnackberActivity.this);
        toot_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams toot_button_LayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toot_button_LayoutParams.gravity = Gravity.RIGHT;
        toot_LinearLayout.setLayoutParams(toot_button_LayoutParams);

        //投稿用Button
        post_button = new Button(TootSnackberActivity.this, null, 0, R.style.Widget_AppCompat_Button_Borderless);
        post_button.setText(String.valueOf(tootTextCount) + "/" + "500 " + getString(R.string.toot));
        post_button.setTextColor(Color.parseColor("#ffffff"));
        Drawable toot_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_create_black_24dp, null);
        post_button.setCompoundDrawablesWithIntrinsicBounds(toot_icon, null, null, null);
        //POST statuses
        String finalAccessToken = AccessToken;
        String finalInstance = Instance;
        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //クローズでソフトキーボード非表示
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    if (TootSnackberActivity.this.getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(TootSnackberActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }
                }
                //画像添付なしのときはここを利用して、
                //画像添付トゥートは別に書くよ
                if (media_list.isEmpty() || media_list == null || media_list.get(0) == null) {
                    //Tootする
                    //確認SnackBer
                    Snackbar.make(v, R.string.toot_dialog, Snackbar.LENGTH_SHORT).setAction(R.string.toot, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = "https://" + finalInstance + "/api/v1/statuses/?access_token=" + finalAccessToken;
                            //ぱらめーたー
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("status", toot_EditText.getText().toString())
                                    .add("visibility", toot_area)
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
                                            //EditTextを空にする
                                            toot_EditText.setText("");
                                            tootTextCount = 0;
                                            //アプリを閉じる
                                            finishAndRemoveTask();
                                            toot_snackbar.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    }).show();
                }
            }
        });

        //端末情報とぅーと
        ImageButton device_Button = new ImageButton(TootSnackberActivity.this, null, 0, R.style.Widget_AppCompat_Button_Borderless);
        device_Button.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        device_Button.setImageDrawable(getDrawable(R.drawable.ic_perm_device_information_black_24dp));
        //ポップアップメニュー作成
        MenuBuilder device_menuBuilder = new MenuBuilder(TootSnackberActivity.this);
        MenuInflater device_inflater = new MenuInflater(TootSnackberActivity.this);
        device_inflater.inflate(R.menu.device_info_menu, device_menuBuilder);
        MenuPopupHelper device_optionsMenu = new MenuPopupHelper(TootSnackberActivity.this, device_menuBuilder, device_Button);
        device_optionsMenu.setForceShowIcon(true);
        //コードネーム変換（手動
        String codeName = "";
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            codeName = "Nougat";
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            codeName = "Oreo";
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            codeName = "Pie";
        }
        String finalCodeName = codeName;
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        device_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device_optionsMenu.show();
                device_menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        //名前
                        if (menuItem.getTitle().toString().contains(getString(R.string.device_name))) {
                            toot_EditText.append(Build.MODEL);
                            toot_EditText.append("\r\n");
                        }
                        //Androidバージョン
                        if (menuItem.getTitle().toString().contains(getString(R.string.android_version))) {
                            toot_EditText.append(Build.VERSION.RELEASE);
                            toot_EditText.append("\r\n");
                        }
                        //めーかー
                        if (menuItem.getTitle().toString().contains(getString(R.string.maker))) {
                            toot_EditText.append(Build.BRAND);
                            toot_EditText.append("\r\n");
                        }
                        //SDKバージョン
                        if (menuItem.getTitle().toString().contains(getString(R.string.sdk_version))) {
                            toot_EditText.append(String.valueOf(Build.VERSION.SDK_INT));
                            toot_EditText.append("\r\n");
                        }
                        //コードネーム
                        if (menuItem.getTitle().toString().contains(getString(R.string.codename))) {
                            toot_EditText.append(finalCodeName);
                            toot_EditText.append("\r\n");
                        }
                        //バッテリーレベル
                        if (menuItem.getTitle().toString().contains(getString(R.string.battery_level))) {
                            toot_EditText.append(String.valueOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) + "%");
                            toot_EditText.append("\r\n");
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });
            }
        });


        //コマンド実行ボタン
        Button command_Button = new Button(TootSnackberActivity.this, null, 0, R.style.Widget_AppCompat_Button_Borderless);
        command_Button.setText(R.string.command_run);
        command_Button.setTextColor(Color.parseColor("#ffffff"));
        //EditTextを監視する
        toot_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //コマンド実行メゾット？
                //CommandCode.commandSet(TootSnackberActivity.this, toot_EditText, toot_LinearLayout, command_Button, "/sushi", "command_sushi");
                CommandCode.commandSet(TootSnackberActivity.this, toot_EditText, toot_LinearLayout, command_Button, "/friends.nico", "pref_friends_nico_mode");
                CommandCode.commandSetNotPreference(TootSnackberActivity.this, TootSnackberActivity.this, toot_EditText, toot_LinearLayout, command_Button, "/rate-limit", "rate-limit");
                CommandCode.commandSetNotPreference(TootSnackberActivity.this, TootSnackberActivity.this, toot_EditText, toot_LinearLayout, command_Button, "/fav-home", "home");
                CommandCode.commandSetNotPreference(TootSnackberActivity.this, TootSnackberActivity.this, toot_EditText, toot_LinearLayout, command_Button, "/fav-local", "local");
                CommandCode.commandSetNotPreference(TootSnackberActivity.this, TootSnackberActivity.this, toot_EditText, toot_LinearLayout, command_Button, "/じゃんけん", "じゃんけん");
                //カウント
                tootTextCount = toot_EditText.getText().toString().length();
                post_button.setText(String.valueOf(tootTextCount) + "/" + "500 " + getString(R.string.toot));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //アカウント切り替えとか
        account_LinearLayout = new LinearLayout(this);
        account_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams center_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        center_layoutParams.gravity = Gravity.CENTER;
        //ImageView
        snackberAccountAvaterImageView = new ImageView(this);
        snackberAccountAvaterImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        snackberAccountAvaterImageView.setLayoutParams(center_layoutParams);
        //TextView
        snackberAccount_TextView = new TextView(this);
        snackberAccount_TextView.setTextSize(14);
        snackberAccount_TextView.setTextColor(Color.parseColor("#ffffff"));
        snackberAccount_TextView.setLayoutParams(center_layoutParams);
        //アカウント情報を取得するところにテキスト設定とか書いたで
        getAccount();
        //アカウント切り替えポップアップ
        //ポップアップメニューを展開する
        account_menuBuilder = new MenuBuilder(this);
        account_optionsMenu = new MenuPopupHelper(this, account_menuBuilder, account_LinearLayout);
        optionsMenu.setForceShowIcon(true);
        //マルチアカウント読み込み
        //押したときの処理とかもこっち
        readMultiAccount();

        //LinearLayoutに入れる
        account_LinearLayout.addView(snackberAccountAvaterImageView);
        account_LinearLayout.addView(snackberAccount_TextView);


        //画像追加用LinearLayout
        media_LinearLayout = new LinearLayout(TootSnackberActivity.this);
        media_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        media_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //LinearLayoutに追加
        //メイン
        snackber_LinearLayout.addView(account_LinearLayout);
        snackber_LinearLayout.addView(toot_textBox_LinearLayout);
        snackber_LinearLayout.addView(toot_Button_LinearLayout);
        snackber_LinearLayout.addView(media_LinearLayout);
        snackber_LinearLayout.addView(toot_LinearLayout);
        //ボタン追加
        toot_Button_LinearLayout.addView(add_image_Button);
        toot_Button_LinearLayout.addView(toot_area_Button);
        toot_Button_LinearLayout.addView(device_Button);
        //Toot LinearLayout
        toot_LinearLayout.addView(post_button);
        //SnackBerに追加
        snackBer_viewGrop.addView(snackber_LinearLayout);
    }


    //自分の情報を手に入れる
    private void getAccount() {
        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        //通信量節約
        boolean setting_avater_hidden = pref_setting.getBoolean("pref_drawer_avater", false);
        //Wi-Fi接続時は有効？
        boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
        //GIFを再生するか？
        boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);
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
        String finalInstance = Instance;
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
                    String user_id = jsonObject.getString("acct");
                    //スナックバー更新
                    snackber_Name = display_name + " ( @" + user_id + " / " + finalInstance + " )";
                    snackber_Avatar = jsonObject.getString("avatar");
                    //UIスレッド
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //画像を入れる
                            //表示設定
                            if (setting_avater_hidden) {
                                snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp);
                                snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            }
                            //Wi-Fi
                            if (setting_avater_wifi) {
                                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                    if (setting_avater_gif) {
                                        //GIFアニメ再生させない
                                        Picasso.get()
                                                .load(snackber_Avatar)
                                                .resize(100, 100)
                                                .placeholder(R.drawable.ic_refresh_black_24dp)
                                                .into(snackberAccountAvaterImageView);
                                    } else {
                                        //GIFアニメを再生
                                        Glide.with(getApplicationContext())
                                                .load(snackber_Avatar)
                                                .apply(new RequestOptions().override(100, 100).placeholder(R.drawable.ic_refresh_black_24dp))
                                                .into(snackberAccountAvaterImageView);
                                    }
                                }
                            } else {
                                snackberAccountAvaterImageView.setImageResource(R.drawable.ic_person_black_24dp);
                                snackberAccountAvaterImageView.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                            }
                            //テキストビューに入れる
                            snackberAccount_TextView.setText(snackber_Name);

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @SuppressLint("RestrictedApi")
    private void readMultiAccount() {
        multi_account_instance = new ArrayList<>();
        multi_account_access_token = new ArrayList<>();
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

        if (multi_account_instance.size() >= 1) {
            for (int count = 0; count < multi_account_instance.size(); count++) {
                String multi_instance = multi_account_instance.get(count);
                String multi_access_token = multi_account_access_token.get(count);
                int finalCount = count;
                //GetAccount
                String url = "https://" + multi_instance + "/api/v1/accounts/verify_credentials/?access_token=" + multi_access_token;
                //作成
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                //GETリクエスト
                OkHttpClient client_1 = new OkHttpClient();
                String finalInstance = Instance;
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
                            String user_id = jsonObject.getString("acct");
                            //スナックバー更新
                            snackber_Name = display_name + " ( @" + user_id + " / " + finalInstance + " )";
                            snackber_Avatar = jsonObject.getString("avatar");
                            account_menuBuilder.add(0, finalCount, 0, display_name + "(" + user_id + " / " + multi_instance + ")");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        //押したときの処理
        account_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //追加中に押したら落ちるから回避
                if (account_menuBuilder.size() == multi_account_instance.size()) {
                    account_optionsMenu.show();
                    account_menuBuilder.setCallback(new MenuBuilder.Callback() {
                        @Override
                        public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {

                            //ItemIdにマルチアカウントのカウントを入れている
                            int position = menuItem.getItemId();

                            String multi_instance = multi_account_instance.get(position);
                            String multi_access_token = multi_account_access_token.get(position);

                            SharedPreferences.Editor editor = pref_setting.edit();
                            editor.putString("main_instance", multi_instance);
                            editor.putString("main_token", multi_access_token);
                            editor.apply();

                            //アプリ再起動
                            Intent intent = new Intent(getContext(), Home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            return false;
                        }

                        @Override
                        public void onMenuModeChange(MenuBuilder menuBuilder) {

                        }
                    });

                } else {
                    Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
