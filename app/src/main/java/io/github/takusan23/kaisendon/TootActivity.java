package io.github.takusan23.kaisendon;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.github.takusan23.kaisendon.Preference_ApplicationContext.getContext;

public class TootActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private final int REQUEST_PERMISSION = 1000;

    String display_name = null;
    String user_id = null;
    String user_avater = null;
    private static final int RESULT_PICK_IMAGEFILE = 1001;

    long image_id;
    String image_url = null;

    String NowPlaying_Text = null;

    ArrayList<String> notificationList = new ArrayList<String>();

    IntentFilter intentFilter = new IntentFilter();

    BroadcastReceiver NowPlaying_broadcastReceiver;

    ArrayList<Long> media_ids = new ArrayList<Long>();

    Status.Visibility visibility = Status.Visibility.Private;

    String contact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String AccessToken = null;
        String instance = null;
        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        //ダークテーマに切り替える機能
        //setContentViewより前に実装する必要あり？
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        if (dark_mode) {
            setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar);
        } else {
            //ドロイド君かわいい
        }

        //OLEDように真っ暗のテーマも使えるように
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (oled_mode) {
            setTheme(R.style.OLED_Theme);
        } else {
            //なにもない
        }

        setContentView(R.layout.activity_toot);


        contact = getIntent().getStringExtra("contact");

        final TextView toot_textbox = findViewById(R.id.toot_text_public);
        final TextView toot_count = findViewById(R.id.toot_count);
        Button toot_button = findViewById(R.id.toot);
        Button nya = findViewById(R.id.nya_n);
        toot_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_create_black_24dp_black, 0, 0, 0);

        SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);

        //設定のプリファレンス
        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            instance = pref_setting.getString("main_instance", "");

        }

        //find
        TextView account_name = findViewById(R.id.account_name);
        TextView account_id = findViewById(R.id.account_id);
        ImageView avater_imageView = findViewById(R.id.avater_imageview);

        //image
        Button add_image_button = findViewById(R.id.add_image_button);
        Button now_playing_button = findViewById(R.id.nowplaying_button);

        //すぴなー
        Spinner spinner = findViewById(R.id.visibility_spinner);


        //作者に連絡
        try {
            toot_textbox.append(contact);
        }catch (RuntimeException e){
            e.printStackTrace();
        }


        //背景
        ImageView background_imageView = findViewById(R.id.activity_toot_background_imageview);

        if (pref_setting.getBoolean("background_image", true)) {
            Uri uri = Uri.parse(pref_setting.getString("background_image_path", ""));
            Glide.with(TootActivity.this)
                    .load(uri)
                    .into(background_imageView);
        }

        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f) != 0.1) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        //画像付き
        //とりあえず画像を選べるアクティビティへー
        add_image_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //ストレージ読み込みの権限があるか確認
                //許可してないときは許可を求める
                if (ContextCompat.checkSelfPermission(TootActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(TootActivity.this)
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

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                }
            }
        });


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

        //アバター画像と名前
        String finalInstance1 = instance;
        String finalAccessToken1 = AccessToken;
        new AsyncTask<String, Void, String>()

        {

            @Override
            protected String doInBackground(String... string) {
                MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken1).build();

                try {
                    Account account = new Accounts(client).getVerifyCredentials().execute();

                    display_name = account.getDisplayName();
                    user_id = account.getUserName();

                    user_avater = account.getAvatar();

                    //UIを変更するために別スレッド呼び出し
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //表示設定
                            if (setting_avater_hidden) {

                                avater_imageView.setImageResource(R.drawable.ic_person_black_24dp);

                            }
                            //Wi-Fi
                            if (setting_avater_wifi) {
                                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                                    if (setting_avater_gif) {

                                        //GIFアニメ再生させない
                                        Picasso.get()
                                                .load(user_avater)
                                                .resize(100, 100)
                                                .placeholder(R.drawable.ic_refresh_black_24dp)
                                                .into(avater_imageView);

                                    } else {

                                        //GIFアニメを再生
                                        Glide.with(getApplicationContext())
                                                .load(user_avater)
                                                .apply(new RequestOptions().override(100, 100).placeholder(R.drawable.ic_refresh_black_24dp))
                                                .into(avater_imageView);
                                    }
                                }

                            } else {

                                avater_imageView.setImageResource(R.drawable.ic_person_black_24dp);

                            }


                            account_name.setText(display_name);
                            account_id.setText("@" + user_id + "@" + finalInstance1);

                        }
                    });


                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }


                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //公開範囲設定
        String visibility_list[] = {getString(R.string.visibility_public), getString(R.string.visibility_unlisted), getString(R.string.visibility_private), getString(R.string.visibility_direct)};
        ArrayAdapter<String> spineer_adapter = new ArrayAdapter<>(TootActivity.this, android.R.layout.simple_spinner_item, visibility_list);
        spineer_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spineer_adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    visibility = Status.Visibility.Public;
                } else if (position == 1) {
                    visibility = Status.Visibility.Unlisted;
                } else if (position == 2) {
                    visibility = Status.Visibility.Private;
                } else if (position == 3) {
                    visibility = Status.Visibility.Direct;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        String finalAccessToken = AccessToken;
        String finalInstance = instance;

        //にゃーん
        nya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toot_textbox.append("にゃーん");
            }
        });
        toot_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String toot_text = toot_textbox.getText().toString();

                //ダイアログ出すかどうか
                boolean accessToken_boomelan = pref_setting.getBoolean("pref_toot_dialog", false);
                if (accessToken_boomelan) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(TootActivity.this);
                    alertDialog.setTitle(R.string.confirmation);
                    alertDialog.setMessage(R.string.toot_dialog);
                    alertDialog.setPositiveButton(R.string.toot, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //トゥートああああ

                            new AsyncTask<String, Void, String>() {

                                @Override
                                protected String doInBackground(String... params) {
                                    AccessToken accessToken = new AccessToken();
                                    accessToken.setAccessToken(finalAccessToken);

                                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();
                                    try {
                                        new Statuses(client).postStatus(toot_text, null, null, false, null, visibility).execute();
                                    } catch (Mastodon4jRequestException e) {
                                        e.printStackTrace();
                                    }
                                    return toot_text;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    Toast.makeText(getApplicationContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                                }

                            }.execute();
                            toot_textbox.setText(""); //投稿した後に入力フォームを空にする

                        }
                    });
                    alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.create().show();

                } else {

                    //トゥートああああ
                    new AsyncTask<String, Void, String>() {

                        @Override
                        protected String doInBackground(String... params) {
                            AccessToken accessToken = new AccessToken();
                            accessToken.setAccessToken(finalAccessToken);

                            MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();
                            try {
                                new Statuses(client).postStatus(toot_text, null, null, false, null, visibility).execute();
                            } catch (Mastodon4jRequestException e) {
                                e.printStackTrace();
                            }
                            return toot_text;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            Toast.makeText(getApplicationContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                        }

                    }.execute();
                    toot_textbox.setText(""); //投稿した後に入力フォームを空にする
                }


//                Toast.makeText(getApplicationContext(),token,Toast.LENGTH_SHORT).show();

                stopNowPlayingService();
            }
        });


        toot_count.setText("文字数カウント : " + "0/500");


        toot_textbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int textColor = Color.GRAY;

                // 入力文字数の表示
                int txtLength = s.length();
                toot_count.setText("文字数カウント : " + Integer.toString(txtLength) + "/500");

                // 指定文字数オーバーで文字色を赤くする
                if (txtLength > 500) {
                    textColor = Color.RED;
                }
                toot_count.setTextColor(textColor);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });


        //共有メニューから押したときの処理
        Intent share_intent = getIntent();
        String share_string = share_intent.getAction();
        try {
            if (share_string.equals(Intent.ACTION_SEND)) {
                Bundle bundle = share_intent.getExtras();
                if (bundle != null) {
                    CharSequence text = bundle.getCharSequence(Intent.EXTRA_TEXT);
                    if (text != null) {
                        toot_textbox.append(text);
                    }
                }
            }
        } catch (NullPointerException e) {

        }

        // thinking
        toot_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toot_textbox.append("🤔");
                return false;
            }
        });


        //NowPlaying
        now_playing_button.setText(getString(R.string.NowPlaying));
        now_playing_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //通知リスナーの権限がある確認
                ComponentName cn = new ComponentName(TootActivity.this, Kaisendon_NowPlaying_Service.class);
                String flat = Settings.Secure.getString(TootActivity.this.getContentResolver(), "enabled_notification_listeners");
                final boolean enabled = flat != null && flat.contains(cn.flattenToString());
                if (enabled) {

                    //ダイアログ
                    EditText editText_App_Name = new EditText(TootActivity.this);
                    editText_App_Name.append(pref_setting.getString("Now_Playing_AppName", ""));
                    AlertDialog.Builder alertDialog_editTranspatency = new AlertDialog.Builder(TootActivity.this);
                    alertDialog_editTranspatency.setView(editText_App_Name);
                    alertDialog_editTranspatency.setTitle(getString(R.string.Now_Playing_Dialog_title))
                            .setMessage(getString(R.string.Now_Playing_Dialog_message))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String editAppName = editText_App_Name.getText().toString();
                                    SharedPreferences.Editor editor = pref_setting.edit();
                                    editor.putString("Now_Playing_AppName", editAppName);
                                    editor.apply();

                                    //ブロードキャスト受信
                                    NowPlaying_broadcastReceiver = new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            String title = null;
                                            //ArrayList<String> notificationList = new ArrayList<String>();
                                            Bundle bundle = intent.getExtras();
                                            title = bundle.getString("title");
                                            // System.out.println("通知 : " + title);

                                            //テキストボックスにいれる

                                            //if (!toot_textbox.getText().toString().contains(title)) {
                                            toot_textbox.append(title);
                                            //}

                                            unregisterReceiver(NowPlaying_broadcastReceiver);
                                            //stopNowPlayingService();


                                            Intent stop_service = new Intent();
                                            stop_service.setAction("Stop_Now_Playing");
                                            sendBroadcast(stop_service);


                                        }
                                    };
                                    //ブロードキャスト関係
                                    intentFilter.addAction("Now_Playing");
                                    registerReceiver(NowPlaying_broadcastReceiver, intentFilter);
                                    Intent intent = new Intent(TootActivity.this, Kaisendon_NowPlaying_Service.class);
                                    //startService(intent);

                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();

                } else {
                    //権限を取りに行く
                    new AlertDialog.Builder(TootActivity.this)
                            .setTitle(getString(R.string.listenerservice_dialog_title))
                            .setMessage(getString(R.string.listenerservice_dialog_message))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }

            }
        });

        //長押しで設定画面に飛ばす
        now_playing_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);

                return false;
            }
        });
    }


    /*

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

            LinearLayout add_image_linearLayout = findViewById(R.id.media_linearLayout);


            String AccessToken = null;
            String instance = null;
            //設定のプリファレンス
            SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
            boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
            if (accessToken_boomelan) {

                AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
                instance = pref_setting.getString("pref_mastodon_instance", "");

            } else {

                AccessToken = pref_setting.getString("main_token", "");
                instance = pref_setting.getString("main_instance", "");

            }
            final TextView toot_textbox = findViewById(R.id.toot_text_public);
            final TextView toot_count = findViewById(R.id.toot_count);
            Button toot_button = findViewById(R.id.toot);
            Button nya = findViewById(R.id.nya_n);


            if (requestCode == RESULT_PICK_IMAGEFILE && resultCode == Activity.RESULT_OK) {
                if (resultData.getData() != null) {
                    ParcelFileDescriptor pfDescriptor = null;
                    try {
                        Uri uri = resultData.getData();

    */
/*
                    if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                        //ギャラリーからの場合
                        String id = DocumentsContract.getDocumentId(resultData.getData());
                        String selection = "_id=?";
                        String[] selectionArgs = new String[]{id.split(":")[1]};

                        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns.DATA}, selection, selectionArgs, null);

                        if (cursor.moveToFirst()) {
                            File file = new File(cursor.getString(0));
                            System.out.println("ディレクトリ : " + file);
                            // fileから写真を読み込む
                        }

                    }
*//*


                    pfDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                    if (pfDescriptor != null) {
                        FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
                        Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        pfDescriptor.close();

                        //動的にレイアウト作成
                        ImageView add_image_imageview = new ImageView(this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
                        add_image_imageview.setLayoutParams(layoutParams);
                        add_image_linearLayout.addView(add_image_imageview);

                        add_image_imageview.setImageURI(uri);

                        System.out.println("LINK : " + uri);

                        //がぞうをアップロード
                        String finalInstance = instance;
                        String finalAccessToken = AccessToken;

                        System.out.println(String.valueOf("イメージID : " + uri.toString()));

                        //URI > File
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];
                        if ("primary".equalsIgnoreCase(type)) {
                            String test =  Environment.getExternalStorageDirectory() + "/" + split[1];
                            System.out.println("てすお : " + test);
                        }else {
                            String test= "/stroage/" + type +  "/" + split[1];
                            System.out.println("てすよ : " + test);
                        }

                        new AsyncTask<String, Void, String>() {

                            @Override
                            protected String doInBackground(String... string) {
                                File file = new File(uri.toString());
                                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson()).accessToken(finalAccessToken).build();
                                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                //MultipartBody.Part part = MultipartBody.Part.createFormData("multipart/form-data", path);
                                MultipartBody.Part part = MultipartBody.Part.createFormData("multipart/form-data", "file:///storage/emulated/0/Sketch/test.png");

                                try {
                                    Attachment media = new Media(client).postMedia(part).execute();

                                    image_id = media.getId();
                                    String url = media.getPreviewUrl();

                                    System.out.println(String.valueOf("イメージID : " + image_id));
                                    System.out.println(String.valueOf("リンクID : " + url));


                                } catch (Mastodon4jRequestException e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }

                            //もしかしたらだめかも？
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (pfDescriptor != null) {
                            pfDescriptor.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }
*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        LinearLayout add_image_linearLayout = findViewById(R.id.media_linearLayout);
        ImageView background_imageView = findViewById(R.id.activity_toot_background_imageview);

        if (media_ids.size() < 4) {
            if (requestCode == 1)
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();

                    String filePath = getPath(selectedImage);
                    String file_extn = filePath.substring(filePath.lastIndexOf(".") + 1);
                    File file = new File(filePath);
                    String finalPath = "file:\\\\" + filePath;

                    //image_name_tv.setText(filePath);

                    if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("gif") || file_extn.equals("png")) {
                        //System.out.println("パス : " + finalPath.replaceAll("\\\\", "/"));
                        //System.out.println("拡張子 : " + file_extn);
                        //System.out.println("ファイル名 : " + file.getName());

                        //動的にレイアウト作成
                        ImageView add_image_imageview = new ImageView(this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
                        add_image_imageview.setLayoutParams(layoutParams);
                        add_image_linearLayout.addView(add_image_imageview);
                        add_image_imageview.setImageURI(selectedImage);


                        //画像を投げるだけ
                        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
                            @Override
                            protected String doInBackground(String... string) {
                                String AccessToken = null;
                                String instance = null;
                                //設定のプリファレンス
                                SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
                                boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
                                if (accessToken_boomelan) {
                                    AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
                                    instance = pref_setting.getString("pref_mastodon_instance", "");
                                } else {
                                    AccessToken = pref_setting.getString("main_token", "");
                                    instance = pref_setting.getString("main_instance", "");
                                }

                                View view = findViewById(android.R.id.content);
                                LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                /*
                                 *  ここから　MultiPartBody/FormDataをつかった画像POST
                                 *  念願の画像POSTです！！！！！！！！！！！
                                 *
                                 */

                                //くるくる

                                Snackbar snackbar = Snackbar.make(view, getString(R.string.upload_image) + "\r\n /api/v1/media", Snackbar.LENGTH_INDEFINITE);
                                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                                //SnackBerを複数行対応させる
                                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
                                snackBer_textView.setMaxLines(2);
                                //複数行対応させたおかげでずれたので修正
                                ProgressBar progressBar = new ProgressBar(TootActivity.this);
                                progressBer_layoutParams.gravity = Gravity.CENTER;
                                progressBar.setLayoutParams(progressBer_layoutParams);
                                snackBer_viewGrop.addView(progressBar, 0);
                                snackbar.show();


                                OkHttpClient okHttpClient = new OkHttpClient();

                                String url_link = "https://" + instance + "/api/v1/media/";

                                RequestBody requestBody = new MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("image/" + file_extn), file))
                                        .addFormDataPart("access_token", AccessToken)
                                        .build();

                                Request request = new Request.Builder()
                                        .url(url_link)
                                        .post(requestBody)
                                        .build();

                                String request_string = "";

                                okHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {

                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String response_string = response.body().string();
                                        //System.out.println("レスポンス : " + response_string);

                                        try {
                                            JSONObject jsonObject = new JSONObject(response_string);
                                            long media_id_long = jsonObject.getLong("id");
                                            media_ids.add(media_id_long);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    snackbar.dismiss();
                                                    add_image_imageview.setTag(media_ids.size());
                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });

                                Button toot_button = findViewById(R.id.toot);
                                TextView toot_textbox = findViewById(R.id.toot_text_public);
                                MastodonClient client = new MastodonClient.Builder(instance, new OkHttpClient.Builder(), new Gson()).accessToken(AccessToken).build();

                                //くるくる
                                Snackbar snackbar_status = Snackbar.make(view, "トゥート！ \r\n/api/v1/statuses", Snackbar.LENGTH_INDEFINITE);
                                ViewGroup snackBer_viewGrop_status = (ViewGroup) snackbar_status.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                                //SnackBerを複数行対応させる
                                TextView snackBer_textView_status = (TextView) snackBer_viewGrop_status.findViewById(android.support.design.R.id.snackbar_text);
                                snackBer_textView_status.setMaxLines(2);
                                //複数行対応させたおかげでずれたので修正
                                ProgressBar progressBar_status = new ProgressBar(TootActivity.this);
                                LinearLayout.LayoutParams progressBer_layoutParams_status = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                progressBer_layoutParams_status.gravity = Gravity.CENTER;
                                progressBar_status.setLayoutParams(progressBer_layoutParams);
                                snackBer_viewGrop_status.addView(progressBar_status, 0);

                                toot_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //ダイアログ出すかどうか
                                        boolean accessToken_boomelan = pref_setting.getBoolean("pref_toot_dialog", false);
                                        if (accessToken_boomelan) {
                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TootActivity.this);
                                            alertDialog.setTitle(R.string.confirmation);
                                            alertDialog.setMessage(R.string.toot_dialog);
                                            alertDialog.setPositiveButton(R.string.toot, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new AsyncTask<String, String, String>() {
                                                        @Override
                                                        protected String doInBackground(String... params) {
                                                            try {
                                                                new Statuses(client).postStatus(toot_textbox.getText().toString(), null, media_ids, false, null, visibility).execute();
                                                            } catch (Mastodon4jRequestException e) {
                                                                e.printStackTrace();
                                                            }
                                                            return toot_textbox.getText().toString();
                                                        }

                                                        @Override
                                                        protected void onPostExecute(String result) {
                                                            Toast.makeText(getApplicationContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }.execute();
                                                }
                                            });
                                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });
                                            alertDialog.create().show();
                                        } else {
                                            new AsyncTask<String, String, String>() {
                                                @Override
                                                protected String doInBackground(String... params) {
                                                    try {
                                                        new Statuses(client).postStatus(toot_textbox.getText().toString(), null, media_ids, false, null, visibility).execute();
                                                    } catch (Mastodon4jRequestException e) {
                                                        e.printStackTrace();
                                                    }
                                                    return toot_textbox.getText().toString();
                                                }

                                                @Override
                                                protected void onPostExecute(String result) {
                                                    Toast.makeText(getApplicationContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                                                }
                                            }.execute();
                                        }
                                    }
                                });


                                // System.out.println("=====" + client.post("statuses", requestBody));


/*
                            try {

                                Attachment media = new Media(client).postMedia(part).execute();

                                image_id = media.getId();
                                image_url = media.getPreviewUrl();

                                System.out.println("イメージID : " + String.valueOf(image_id));
                                System.out.println("イメージURL : " + image_url);

                                long aa = 6517780;

                                List<Long> media_list = new ArrayList<Long>();
                                media_list.add(aa);


                                //com.sys1yagi.mastodon4j.api.entity.Status statuses = new Statuses(client).postStatus("テスト",null,media_list,false, "").execute();


                            } catch (Mastodon4jRequestException e) {
                                e.printStackTrace();
                            }

*/


                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                        //画像を消す
                        add_image_imageview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(getContext(), String.valueOf((int) add_image_imageview.getTag()), Toast.LENGTH_SHORT).show();
                                int media_number = (int) add_image_imageview.getTag();

                                new AlertDialog.Builder(TootActivity.this)
                                        .setTitle(R.string.confirmation)
                                        .setMessage(R.string.media_delete)
                                        .setPositiveButton(R.string.delete_ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // OK button pressed
                                                try {
                                                    if (media_ids.size() >= media_number) {
                                                        media_ids.remove(media_number - 1);
                                                        add_image_linearLayout.removeViewAt(media_number - 1);
                                                    }
                                                } catch (IndexOutOfBoundsException e) {
                                                    media_ids.clear();
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            add_image_linearLayout.removeViewAt(2);
                                                        }
                                                    });
                                                }
                                            }
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();

                            }
                        });


                        //FINE
                    } else {
                        //NOT IN REQUIRED FORMAT
                    }
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopNowPlayingService();
        try {
            unregisterReceiver(NowPlaying_broadcastReceiver);
        } catch (RuntimeException e) {

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

    private static class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // ブロードキャストを受け取った時の処理を記述
            // 今回はログを出力しています。
            Log.i(getClass().getSimpleName(), intent.getStringExtra("KEY"));
        }
    }

    private void stopNowPlayingService() {
        Intent intent_stop = new Intent(TootActivity.this, Kaisendon_NowPlaying_Service.class);
        stopService(intent_stop);
    }

}
