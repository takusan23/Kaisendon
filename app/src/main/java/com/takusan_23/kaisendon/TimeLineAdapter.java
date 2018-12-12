package com.takusan_23.kaisendon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.content.Context.MODE_PRIVATE;

public class TimeLineAdapter extends ArrayAdapter<ListItem> {

    private int mResource;
    private List<ListItem> mItems;
    private LayoutInflater mInflater;
    private int layoutId;
    private Set<Integer> visibleSet = new HashSet<Integer>();
    final android.os.Handler handler_1 = new android.os.Handler();

    private FragmentActivity myContext;

    SharedPreferences pref = Preference_ApplicationContext.getContext().getSharedPreferences("preferences", MODE_PRIVATE);

    //settingのプリファレンスをとる
    SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param resource リソースID
     * @param items    リストビューの要素
     */
    public TimeLineAdapter(Context context, int resource, List<ListItem> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = LayoutInflater.from(context);
        this.layoutId = layoutId;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        viewHolder = new ViewHolder();

        ViewHolder holder;

        //Emoji
        EmojiCompat.Config config = new BundledEmojiCompatConfig(getContext());
        config.setReplaceAll(true);
        EmojiCompat.init(config);
        final EmojiCompat compat = EmojiCompat.get();



        //データの再利用を許さない！！！！！！！！！！！！！！！！！！！
        if (convertView == null) {

            view = mInflater.inflate(R.layout.timeline_item, parent, false);

            holder = new ViewHolder();

            holder.linearLayout = (LinearLayout) view.findViewById(R.id.linearlayout_media);
            holder.vw1 = view.findViewById(R.id.vw1);
            holder.toot_linearLayout = view.findViewById(R.id.toot_linearlayout);
            holder.button_linearLayout = view.findViewById(R.id.button_layout);

            holder.media_imageview = new ImageView(holder.linearLayout.getContext());
            holder.avater_imageview = view.findViewById(R.id.thumbnail);

            holder.user_textview = view.findViewById(R.id.user);
            holder.tile_textview = view.findViewById(R.id.tile_);
            holder.client_textview = view.findViewById(R.id.client);

            holder.nicoru_button = view.findViewById(R.id.nicoru);
            holder.boost_button = view.findViewById(R.id.boost);
            holder.web_button = view.findViewById(R.id.web);

            view.setTag(holder);


        } else {
            holder = (ViewHolder) view.getTag();
        }


        //上から追加
        //ListItem item = mItems.get(getCount() -1 - position);
        ListItem item = mItems.get(position);
        //System.out.println("Count : " + String.valueOf(position));


        //設定を取得
        //アクセストークンを変更してる場合のコード
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


        Button nicoru = (Button) holder.nicoru_button;
        Button boost = (Button) holder.boost_button;
        //String media_url = item.getMedia()[0];

        String nicoru_text = null;

        //ニコるをお気に入りに変更 設定次第
        //メッセージも変更できるようにする
        boolean friends_nico_check_box = pref_setting.getBoolean("pref_friends_nico_mode", false);
        if (friends_nico_check_box) {

            nicoru.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black_24dp, 0, 0, 0);

            Locale locale = Locale.getDefault();
            if (locale.equals(Locale.JAPAN)) {
                nicoru.setText("お気に入り");
                nicoru_text = "お気に入りに登録しました : ";
            } else {
                nicoru.setText("Favorite");
                nicoru_text = "add Favorite";
            }


        } else {

            Drawable nicoru_image = ContextCompat.getDrawable(getContext(), R.drawable.nicoru);
            nicoru_image.setBounds(0, 0, 64, 47);
            nicoru.setCompoundDrawables(nicoru_image, null, null, null);
            nicoru_text = "ニコった！ : ";

        }


        //ニコる
        String finalNicoru_text = nicoru_text;
        String id_string = item.getNicoru();
        String avater_url = item.getAvater();
        //String media_url = item.getMedia();

        //メッセージ
        //設定で分けるように
        String favorite_message = null;
        String favorite_title = null;

        boolean nicoru_dialog_chack = pref_setting.getBoolean("pref_friends_nico_mode", false);
        if (nicoru_dialog_chack) {
            favorite_message = "お気に入り";
            favorite_title = "お気に入りに登録しますか";
        } else {
            favorite_message = "ニコる";
            favorite_title = "ニコりますか";
        }

        //ニコるダイアログ
        String finalFavorite_message = favorite_message;
        String finalFavorite_title = favorite_title;
        String finalInstance1 = Instance;
        String finalAccessToken = AccessToken;
        String finalAccessToken1 = AccessToken;
        nicoru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean favorite = pref_setting.getBoolean("pref_nicoru_dialog", false);
                if (favorite) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setTitle(R.string.confirmation);
                    alertDialog.setMessage(finalFavorite_title);
                    alertDialog.setPositiveButton(finalFavorite_message, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            new AsyncTask<String, Void, String>() {

                                @Override
                                protected String doInBackground(String... params) {
                                    AccessToken accessToken = new AccessToken();
                                    accessToken.setAccessToken(finalAccessToken);

                                    MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                    RequestBody requestBody = new FormBody.Builder()
                                            //.add(":id" , toot_id_string)
                                            .build();

                                    System.out.println("=====" + client.post("statuses/" + id_string + "/favourite", requestBody));

                                    return id_string;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    Toast.makeText(getContext(), finalNicoru_text + result, Toast.LENGTH_SHORT).show();
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

                    //テキストボックが未選択
                } else {

                    new AsyncTask<String, Void, String>() {

                        @Override
                        protected String doInBackground(String... params) {
                            AccessToken accessToken = new AccessToken();
                            accessToken.setAccessToken(finalAccessToken1);

                            MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                            RequestBody requestBody = new FormBody.Builder()
                                    //.add(":id" , toot_id_string)
                                    .build();

                            System.out.println("=====" + client.post("statuses/" + id_string + "/favourite", requestBody));

                            return id_string;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            Toast.makeText(getContext(), finalNicoru_text + result, Toast.LENGTH_SHORT).show();
                        }

                    }.execute();

                }

            }
        });


        //ブースト
        String finalAccessToken2 = AccessToken;
        boost.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //設定でダイアログをだすかどうか
                boolean boost_dialog = pref_setting.getBoolean("pref_boost_dialog", false);
                if (boost_dialog) {
                    //ダイアログ
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setTitle(R.string.confirmation);
                    alertDialog.setMessage(R.string.dialog_boost_info);
                    alertDialog.setPositiveButton(R.string.dialog_boost, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AsyncTask<String, Void, String>() {

                                @Override
                                protected String doInBackground(String... params) {
                                    AccessToken accessToken = new AccessToken();
                                    accessToken.setAccessToken(finalAccessToken1);

                                    MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                    RequestBody requestBody = new FormBody.Builder()
                                            //.add(":id" , toot_id_string)
                                            .build();

                                    System.out.println("=====" + client.post("statuses/" + id_string + "/reblog", requestBody));

                                    return id_string;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    Toast.makeText(getContext(), "ブーストしました : " + result, Toast.LENGTH_SHORT).show();
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

                    //チェックボックスが未チェックだったとき
                } else {

                    new AsyncTask<String, Void, String>() {

                        @Override
                        protected String doInBackground(String... params) {
                            AccessToken accessToken = new AccessToken();
                            accessToken.setAccessToken(finalAccessToken1);

                            MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                            RequestBody requestBody = new FormBody.Builder()
                                    //.add(":id" , toot_id_string)
                                    .build();

                            System.out.println("=====" + client.post("statuses/" + id_string + "/reblog", requestBody));

                            return id_string;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            Toast.makeText(getContext(), "ブーストしました : " + result, Toast.LENGTH_SHORT).show();
                        }

                    }.execute();

                }

            }
        });

        //ブーストボタンにアイコンつける
        Button boost_button = (Button) holder.boost_button;
        boost_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_black_24dp, 0, 0, 0);


        //ブラウザ、他クライアントで開くボタン設置
        Button web_button = (Button) holder.web_button;
        web_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_android_black_24dp, 0, 0, 0);


        String finalInstance = Instance;
        web_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);

                //戻るアイコン
                Bitmap back_icon = BitmapFactory.decodeResource(getContext().getApplicationContext().getResources(), R.drawable.baseline_arrow_back_black_24dp);

                //有効
                if (chrome_custom_tabs) {

                    String custom = CustomTabsHelper.getPackageNameToUse(getContext());

                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl((Activity) getContext(), Uri.parse("https://" + finalInstance + "/" + "@" + item.getUserID() + "/" + id_string));
                    //無効
                } else {
                    Uri uri = Uri.parse("https://" + finalInstance + "/" + "@" + item.getUserID() + "/" + id_string);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    getContext().startActivity(intent);

                }
            }
        });


        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());


        //タイムラインに画像を表示
        String media_url = item.getMedia1();
        //動的に画像を追加するよ
        LinearLayout linearLayout = (LinearLayout) holder.linearLayout;
        //Wi-Fi接続時は有効？
        boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
        //GIFを再生するか？
        boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);

        boolean toot_media = pref_setting.getBoolean("pref_toot_media", false);

        holder.media_imageview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        if (media_url != null) {

            //非表示
            if (toot_media) {
                //添付あるよマーク
                holder.media_imageview.setImageResource(R.drawable.ic_image_black_24dp);
                //ザイズ変更
                ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(500, 500);
                holder.media_imageview.setLayoutParams(layoutParams);
                //コレ呼ばないとえらー
                if (holder.media_imageview.getParent() != null) {
                    ((ViewGroup) holder.media_imageview.getParent()).removeView(holder.media_imageview);
                }
                //搭載
                linearLayout.addView(holder.media_imageview);

                //クリックしてイメージ表示
                holder.media_imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (setting_avater_gif) {
                            //GIFアニメ再生させない
                            Picasso.get()
                                    .load(media_url)
                                   // .resize(500, 500)
                                    .placeholder(R.drawable.ic_sync_black_24dp)
                                    .into(holder.media_imageview);

                            if (holder.media_imageview.getParent() != null) {
                                ((ViewGroup) holder.media_imageview.getParent()).removeView(holder.media_imageview);
                            }
                            //表示
                            linearLayout.addView(holder.media_imageview);

                        } else {

                            //画像を取ってくる
                            Glide.with(getContext())
                                    .load(media_url)
                                    //Overrideはサイズ、placeholderは読み込み中アイコン
                                    .apply(new RequestOptions()
                                           // .override(500, 500)
                                            .placeholder(R.drawable.ic_sync_black_24dp))
                                    .into(holder.media_imageview);
                            //呼び出し（こっわ
                            if (holder.media_imageview.getParent() != null) {
                                ((ViewGroup) holder.media_imageview.getParent()).removeView(holder.media_imageview);
                            }
                            //表示
                            linearLayout.addView(holder.media_imageview);
                        }
                    }
                });
            }

            //Wi-Fi接続時
            if (setting_avater_wifi) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    if (setting_avater_gif) {

                        //GIFアニメ再生させない
                        Picasso.get()
                                .load(media_url)
                               // .resize(500, 500)
                                .placeholder(R.drawable.ic_sync_black_24dp)
                                .into(holder.media_imageview);

                        if (holder.media_imageview.getParent() != null) {
                            ((ViewGroup) holder.media_imageview.getParent()).removeView(holder.media_imageview);
                        }
                        //表示
                        linearLayout.addView(holder.media_imageview);

                    } else {

                        //画像を取ってくる
                        Glide.with(getContext())
                                .load(media_url)
                                //Overrideはサイズ、placeholderは読み込み中アイコン
                                .apply(new RequestOptions()
                                       // .override(500, 500)
                                        .placeholder(R.drawable.ic_sync_black_24dp))
                                .into(holder.media_imageview);
                        //呼び出し（こっわ
                        if (holder.media_imageview.getParent() != null) {
                            ((ViewGroup) holder.media_imageview.getParent()).removeView(holder.media_imageview);
                        }
                        //表示
                        linearLayout.addView(holder.media_imageview);
                    }

                }

                //Wi-Fi未接続
            } else {
                //添付あるよマーク
                holder.media_imageview.setImageResource(R.drawable.ic_image_black_24dp);
                //ザイズ変更
                ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(500, 500);

                holder.media_imageview.setLayoutParams(layoutParams);
                //コレ呼ばないとえらー
                if (holder.media_imageview.getParent() != null) {
                    ((ViewGroup) holder.media_imageview.getParent()).removeView(holder.media_imageview);
                }
                //搭載
                linearLayout.addView(holder.media_imageview);

                //クリックしてイメージ表示
                holder.media_imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (setting_avater_gif) {
                            //GIFアニメ再生させない
                            Picasso.get()
                                    .load(media_url)
                                    .resize(500, 500)
                                    .placeholder(R.drawable.ic_sync_black_24dp)
                                    .into(holder.media_imageview);

                            if (holder.media_imageview.getParent() != null) {
                                ((ViewGroup) holder.media_imageview.getParent()).removeView(holder.media_imageview);
                            }
                            //表示
                            linearLayout.addView(holder.media_imageview);

                        } else {

                            //画像を取ってくる
                            Glide.with(getContext())
                                    .load(media_url)
                                    //Overrideはサイズ、placeholderは読み込み中アイコン
                                    .apply(new RequestOptions().override(500, 500).placeholder(R.drawable.ic_sync_black_24dp))
                                    .into(holder.media_imageview);
                            //呼び出し（こっわ
                            if (holder.media_imageview.getParent() != null) {
                                ((ViewGroup) holder.media_imageview.getParent()).removeView(holder.media_imageview);
                            }
                            //表示
                            linearLayout.addView(holder.media_imageview);
                        }
                    }
                });
            }
        }


        //      サムネイル画像を設定
        ImageView thumbnail = (ImageView) holder.avater_imageview;
        //通信量節約
        boolean setting_avater_hidden = pref_setting.getBoolean("pref_avater", false);
        //レイアウト取得
        LinearLayout vw1 = holder.vw1;

        //アバター画像
        if (setting_avater_hidden) {

            thumbnail.setImageBitmap(item.getThumbnail());
            //ImageViewを消し飛ばす
            //((ViewGroup) thumbnail.getParent()).removeView(thumbnail);

        }
        //Wi-Fi
        if (setting_avater_wifi) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                if (setting_avater_gif) {

                    //GIFアニメ再生させない
                    Picasso.get()
                            .load(avater_url)
                            .into(thumbnail);

                } else {

                    //GIFアニメを再生
                    Glide.with(getContext())
                            .load(avater_url)
                            .into(thumbnail);
                }
            }
        } else {
            thumbnail.setImageBitmap(item.getThumbnail());

            LinearLayout wv1 = holder.vw1;
            wv1.removeView(thumbnail);

        }


        long account_id = item.getID();

        final android.os.Handler handler_1 = new android.os.Handler();


        FragmentTransaction ft = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
        FragmentTransaction ft_1 = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
        FragmentManager fragmentManager = ((FragmentActivity) parent.getContext()).getSupportFragmentManager();

        //Fragment get_fragment = fragmentManager.findFragmentById(R.id.fragment3);
        Fragment get_tag_fragment = fragmentManager.findFragmentByTag("user");
        Fragment fragment = new User_Fragment();


        View finalConvertView = convertView;
        thumbnail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //読み込み
                boolean multipain_ui_mode = pref_setting.getBoolean("app_multipain_ui", false);

                if (multipain_ui_mode) {

                    Bundle bundle = new Bundle();
                    bundle.putLong("Account_ID", account_id);
                    fragment.setArguments(bundle);

                    ft.replace(R.id.fragment3, fragment).commit();

                } else {

                    Intent intent = new Intent(getContext(), UserActivity.class);
                    //IDを渡す
                    intent.putExtra("Account_ID", account_id);
                    getContext().startActivity(intent);
                }
            }
        });

        // トゥート
        TextView title = (TextView) holder.tile_textview;
        //title.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT));
        title.setTextSize(18);
        //フォントサイズの変更
        String toot_textsize = pref_setting.getString("pref_fontsize_timeline", "18");
        title.setTextSize(Integer.parseInt(toot_textsize));

        // ユーザー名
        TextView user = (TextView) holder.user_textview;
        //user.setText(item.getUser());
        user.setTextSize(18);
        //フォントサイズの変更
        String username_textsize = pref_setting.getString("pref_fontsize_user", "18");
        user.setTextSize(Integer.parseInt(username_textsize));

        //クライアント
        TextView client = (TextView) holder.client_textview;
        //client.setText(item.getClient());
        client.setTextSize(18);
        //フォントサイズ変更
        String client_textsize = pref_setting.getString("pref_fontsize_client", "18");
        client.setTextSize(Integer.parseInt(client_textsize));

        //フォントの色設定
        boolean font_setting_swich = pref_setting.getBoolean("pref_fontcolor_setting", false);
        if (font_setting_swich)

        {
            //ゆーざー
            String user_font_color = pref_setting.getString("pref_fontcolor", "#000000");
            user.setTextColor(Color.parseColor(user_font_color));

            //たいむらいん
            String toot_font_color = pref_setting.getString("pref_fontcolor_toot", "#000000");
            title.setTextColor(Color.parseColor(toot_font_color));
            //くらいあんと
            String client_font_color = pref_setting.getString("pref_fontcolor_client", "#000000");
            client.setTextColor(Color.parseColor(client_font_color));


        } else

        {

        }

        //絵文字強制
        boolean emoji_compatibility = pref_setting.getBoolean("pref_emoji_compatibility", false);
        if (emoji_compatibility) {
            //ユーザー名
            EmojiCompat.get().registerInitCallback(new EmojiCompat.InitCallback() {
                @Override
                public void onInitialized() {
                    if (user != null) {
                        user.setText(
                                compat.process(item.getUser()));
                    }
                }
            });
            //本文
            EmojiCompat.get().registerInitCallback(new EmojiCompat.InitCallback() {
                @Override
                public void onInitialized() {
                    if (title != null) {
                        title.setText(
                                compat.process((Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT))));
                    }
                }
            });
            //クライアント
            EmojiCompat.get().registerInitCallback(new EmojiCompat.InitCallback() {
                @Override
                public void onInitialized() {
                    if (client != null) {
                        client.setText(
                                compat.process(item.getClient()));
                    }
                }
            });

        } else {
            //無効時
            user.setText(item.getUser());
            title.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT));
            client.setText(item.getClient());
        }


        //アイコンオンリー
        boolean button_icon = pref_setting.getBoolean("pref_button_icon", false);
        if (button_icon) {

            boost_button.setText("");
            nicoru.setText("");
            web_button.setText("");

        } else {

        }


        //ボタン非表示モード
        boolean button_hidden = pref_setting.getBoolean("pref_timeline_button", false);
        if (button_hidden){

            LinearLayout button_layout = holder.button_linearLayout;
            button_layout.removeView(nicoru);
            button_layout.removeView(boost);
            button_layout.removeView(web_button);

            LinearLayout toot_layout = holder.toot_linearLayout;

            //めにゅー
            String finalFavorite_title1 = favorite_message;
            View finalView = view;
            toot_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getContext(), "Test", Toast.LENGTH_SHORT).show();

                    final String[] items = {finalFavorite_title1, finalView.getContext().getString(R.string.boost_button), "Web", finalView.getContext().getString(R.string.account)};
                    new AlertDialog.Builder(getContext())
                            .setTitle(finalView.getContext().getString(R.string.menu))
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                //whichは番号
                                public void onClick(DialogInterface dialog, int which) {
//                                    Toast.makeText(getContext(), String.valueOf(which), Toast.LENGTH_SHORT).show();

                                    //Favorite
                                    if (which == 0) {
                                        boolean favorite = pref_setting.getBoolean("pref_nicoru_dialog", false);
                                        if (favorite) {

                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                            alertDialog.setTitle(R.string.confirmation);
                                            alertDialog.setMessage(finalFavorite_title);
                                            alertDialog.setPositiveButton(finalFavorite_message, new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    new AsyncTask<String, Void, String>() {

                                                        @Override
                                                        protected String doInBackground(String... params) {
                                                            AccessToken accessToken = new AccessToken();
                                                            accessToken.setAccessToken(finalAccessToken);

                                                            MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                                            RequestBody requestBody = new FormBody.Builder()
                                                                    //.add(":id" , toot_id_string)
                                                                    .build();

                                                            System.out.println("=====" + client.post("statuses/" + id_string + "/favourite", requestBody));

                                                            return id_string;
                                                        }

                                                        @Override
                                                        protected void onPostExecute(String result) {
                                                            Toast.makeText(getContext(), finalNicoru_text + result, Toast.LENGTH_SHORT).show();
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

                                            //テキストボックが未選択
                                        } else {

                                            new AsyncTask<String, Void, String>() {

                                                @Override
                                                protected String doInBackground(String... params) {
                                                    AccessToken accessToken = new AccessToken();
                                                    accessToken.setAccessToken(finalAccessToken1);

                                                    MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                                    RequestBody requestBody = new FormBody.Builder()
                                                            //.add(":id" , toot_id_string)
                                                            .build();

                                                    System.out.println("=====" + client.post("statuses/" + id_string + "/favourite", requestBody));

                                                    return id_string;
                                                }

                                                @Override
                                                protected void onPostExecute(String result) {
                                                    Toast.makeText(getContext(), finalNicoru_text + result, Toast.LENGTH_SHORT).show();
                                                }

                                            }.execute();
                                        }
                                    }
                                    //Boost
                                    if (which == 1) {

                                        boolean favorite = pref_setting.getBoolean("pref_nicoru_dialog", false);
                                        if (favorite) {

                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                            alertDialog.setTitle(R.string.confirmation);
                                            alertDialog.setMessage(finalFavorite_title);
                                            alertDialog.setPositiveButton(finalFavorite_message, new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    new AsyncTask<String, Void, String>() {

                                                        @Override
                                                        protected String doInBackground(String... params) {
                                                            AccessToken accessToken = new AccessToken();
                                                            accessToken.setAccessToken(finalAccessToken);

                                                            MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                                            RequestBody requestBody = new FormBody.Builder()
                                                                    //.add(":id" , toot_id_string)
                                                                    .build();

                                                            System.out.println("=====" + client.post("statuses/" + id_string + "/favourite", requestBody));

                                                            return id_string;
                                                        }

                                                        @Override
                                                        protected void onPostExecute(String result) {
                                                            Toast.makeText(getContext(), finalNicoru_text + result, Toast.LENGTH_SHORT).show();
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

                                            //テキストボックが未選択
                                        } else {

                                            new AsyncTask<String, Void, String>() {

                                                @Override
                                                protected String doInBackground(String... params) {
                                                    AccessToken accessToken = new AccessToken();
                                                    accessToken.setAccessToken(finalAccessToken1);

                                                    MastodonClient client = new MastodonClient.Builder(finalInstance1, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                                    RequestBody requestBody = new FormBody.Builder()
                                                            //.add(":id" , toot_id_string)
                                                            .build();

                                                    System.out.println("=====" + client.post("statuses/" + id_string + "/favourite", requestBody));

                                                    return id_string;
                                                }

                                                @Override
                                                protected void onPostExecute(String result) {
                                                    Toast.makeText(getContext(), finalNicoru_text + result, Toast.LENGTH_SHORT).show();
                                                }

                                            }.execute();

                                        }

                                    }
                                    //Web
                                    if (which == 2) {
                                        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);

                                        //戻るアイコン
                                        Bitmap back_icon = BitmapFactory.decodeResource(getContext().getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);

                                        //有効
                                        if (chrome_custom_tabs) {

                                            String custom = CustomTabsHelper.getPackageNameToUse(getContext());

                                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                            CustomTabsIntent customTabsIntent = builder.build();
                                            customTabsIntent.intent.setPackage(custom);
                                            customTabsIntent.launchUrl((Activity) getContext(), Uri.parse("https://" + finalInstance + "/" + "@" + item.getUserID() + "/" + id_string));
                                            //無効
                                        } else {
                                            Uri uri = Uri.parse("https://" + finalInstance + "/" + "@" + item.getUserID() + "/" + id_string);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            getContext().startActivity(intent);

                                        }
                                    }
                                    //Account
                                    if (which == 3) {
                                        //読み込み
                                        boolean multipain_ui_mode = pref_setting.getBoolean("app_multipain_ui", false);

                                        if (multipain_ui_mode) {

                                            Bundle bundle = new Bundle();
                                            bundle.putLong("Account_ID", account_id);
                                            fragment.setArguments(bundle);

                                            ft.replace(R.id.fragment3, fragment).commit();

                                        } else {

                                            Intent intent = new Intent(getContext(), UserActivity.class);
                                            //IDを渡す
                                            intent.putExtra("Account_ID", account_id);
                                            getContext().startActivity(intent);
                                        }

                                    }


                                    // item_which pressed
                                }
                            })
                            .show();
                }
            });

        }else {

        }


        return view;
    }

//ニコる
//    ImageButton nicoru = (ImageButton) view.findViewById(R.id.nicoru);


    static class ViewHolder {
        ImageView media_imageview;
        ImageView avater_imageview;

        TextView user_textview;
        TextView tile_textview;
        TextView client_textview;

        Button nicoru_button;
        Button boost_button;
        Button web_button;

        LinearLayout linearLayout;
        LinearLayout vw1;
        LinearLayout toot_linearLayout;
        LinearLayout button_linearLayout;
    }


    @Override

    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }
}