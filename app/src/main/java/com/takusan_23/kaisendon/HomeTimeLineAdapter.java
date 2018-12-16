package com.takusan_23.kaisendon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class HomeTimeLineAdapter extends ArrayAdapter<ListItem> {

    //public static final long Account_ID = "com.takusan23.kaisendon.Account_ID";
    private final static String TAG = "TestImageGetter";
    private TextView mTv;

    //String AccessToken = null;
    //インスタンス
    String Instance = null;


    private int mResource;
    private List<ListItem> mItems;
    private LayoutInflater mInflater;
    private int layoutId;
    private Set<Integer> visibleSet = new HashSet<Integer>();
    final android.os.Handler handler_1 = new android.os.Handler();
    private TootBookmark_SQLite sqLite;
    private SQLiteDatabase sqLiteDatabase;
    //カスタム絵文字関係
    String final_toot_text;
    String custom_emoji_src;
    boolean avater_emoji = false;
    String avater_custom_emoji_src;
    String nicoru_text = null;
    String emoji_name;
    ArrayList<String> emoji_name_list = new ArrayList<>();

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;

    //ブックマークのボタンの動作決定部分
    boolean bookmark_delete = false;

    //通知のフラグメントのときは画像非表示モードでもレイアウトを消さないように
    boolean notification_layout = false;


    //絵文字用SharedPreferences
    SharedPreferences pref_emoji = Preference_ApplicationContext.getContext().getSharedPreferences("preferences_emoji", Context.MODE_PRIVATE);

    //settingのプリファレンスをとる
    SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

    String AccessToken = null;

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param resource リソースID
     * @param items    リストビューの要素
     */
    public HomeTimeLineAdapter(Context context, int resource, List<ListItem> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = LayoutInflater.from(context);
        this.layoutId = layoutId;

    }

    @SuppressLint("RestrictedApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        HomeTimeLineAdapter.ViewHolder viewHolder;
        viewHolder = new HomeTimeLineAdapter.ViewHolder();

        //Emoji
        EmojiCompat.Config config = new BundledEmojiCompatConfig(getContext());
        config.setReplaceAll(true);
        EmojiCompat.init(config);
        final EmojiCompat compat = EmojiCompat.get();

        //メディア

        ViewHolder holder;


        //データの再利用を許さない！！！！！！！！！！！！！！！！！！！
        if (convertView == null) {

            view = mInflater.inflate(R.layout.timeline_item, parent, false);

            holder = new ViewHolder();

            holder.linearLayoutMediaButton = view.findViewById(R.id.linearlayout_mediaButton);
            holder.linearLayoutMedia = view.findViewById(R.id.linearlayout_media);
            holder.linearLayoutMedia2 = view.findViewById(R.id.linearlayout_media2);
            holder.linearLayoutEnquate = view.findViewById(R.id.linearlayout_enquate);
            holder.vw1 = view.findViewById(R.id.vw1);
            holder.toot_linearLayout = view.findViewById(R.id.toot_linearlayout);
            holder.button_linearLayout = view.findViewById(R.id.button_layout);
            holder.avaterImageview_linearLayout = view.findViewById(R.id.avater_imageview_linearlayout);

            //添付メディア
            holder.media_imageview_1 = new ImageView(holder.linearLayoutMedia.getContext());
            holder.media_imageview_2 = new ImageView(holder.linearLayoutMedia.getContext());
            holder.media_imageview_3 = new ImageView(holder.linearLayoutMedia.getContext());
            holder.media_imageview_4 = new ImageView(holder.linearLayoutMedia.getContext());
            holder.imageButton = new Button(getContext());
            holder.notification_icon = new ImageView(getContext());

            holder.avater_imageview = view.findViewById(R.id.thumbnail);

            holder.user_textview = view.findViewById(R.id.user);
            holder.tile_textview = view.findViewById(R.id.tile_);
            holder.client_textview = view.findViewById(R.id.client);
            holder.bookmark_button = view.findViewById(R.id.bookmark);

            holder.nicoru_button = view.findViewById(R.id.nicoru);
            holder.boost_button = view.findViewById(R.id.boost);
            holder.web_button = view.findViewById(R.id.web);

            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }


        ListItem item = mItems.get(position);
        //System.out.println("Count : " + String.valueOf(getCount()));

        mTv = view.findViewById(R.id.tile_);


        //設定を取得
        //アクセストークンを変更してる場合のコード
        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }


        TextView nicoru = holder.nicoru_button;
        TextView boost = holder.boost_button;

        //autoLinkを動的に設定
        holder.tile_textview.setAutoLinkMask(Linkify.ALL);


        Handler handler = new Handler();

        View finalConvertView2 = view;


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
        String media_url = item.getMedia1();


        //背景色を変える機能
        //ブックマーク削除など

        //SVG許可
        boolean svgAnimation = pref_setting.getBoolean("pref_svg_animation", false);


        if (item.getInfo() != null) {
            if (item.getInfo().contains("custom_notification")) {
                holder.vw1.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_notification_color", "#1A008000")));
            }
            if (item.getInfo().contains("custom_home")) {
                holder.vw1.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_home_color", "#1Aff0000")));
            }
            if (item.getInfo().contains("custom_local")) {
                holder.vw1.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_local_color", "#1A0000ff")));
            }
            if (item.getInfo().contains("bookmark")) {
                bookmark_delete = true;
            }
            //ブースト
            if (item.getInfo().contains("Notification_reblog")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder);
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true;
                //アニメーションアイコン
                setSVGAnimationIcon(R.drawable.notification_to_boost, R.drawable.ic_repeat_black_24dp, holder);
            }
            //お気に入り
            if (item.getInfo().contains("Notification_favourite")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder);
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true;
                //アニメーションアイコン
                //friends.nicoモードかな？
                if (friends_nico_check_box){
                    setSVGAnimationIcon(R.drawable.notification_to_star, R.drawable.ic_star_black_24dp, holder);
                }else {
                    holder.notification_icon.setImageResource(R.drawable.nicoru);
                    if (holder.notification_icon.getParent() != null) {
                        ((ViewGroup) holder.notification_icon.getParent()).removeView(holder.notification_icon);
                    }
                    holder.avaterImageview_linearLayout.addView(holder.notification_icon, 0);
                }
            }
            //ふぉろー
            if (item.getInfo().contains("Notification_follow")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder);
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true;
                //アニメーションアイコン
                setSVGAnimationIcon(R.drawable.notification_to_person, R.drawable.ic_person_add_black_24dp, holder);
            }
            //めんしょん
            if (item.getInfo().contains("Notification_mention")) {
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true;
                //アニメーションアイコン
                setSVGAnimationIcon(R.drawable.notification_to_mention, R.drawable.ic_announcement_black_24dp, holder);
            }
        }


        //メッセージ
        //設定で分けるように
        String favorite_message = null;
        String favorite_title = null;

        boolean nicoru_dialog_chack = pref_setting.getBoolean("pref_friends_nico_mode", false);
        if (nicoru_dialog_chack) {
            favorite_message = getContext().getString(R.string.favoutire);
            favorite_title = getContext().getString(R.string.favourite_add_message);
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
        View finalConvertView1 = view;
        View finalView1 = view;
        nicoru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean favorite = pref_setting.getBoolean("pref_nicoru_dialog", false);
                boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", false);
                if (favorite) {
                    if (replace_snackber) {
                        Snackbar favourite_snackbar;
                        favourite_snackbar = Snackbar.make(finalView1, finalFavorite_title, Snackbar.LENGTH_SHORT);
                        favourite_snackbar.setAction(finalFavorite_message, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TootAction(id_string, "favourite");
                            }
                        });
                        favourite_snackbar.show();
                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setTitle(R.string.confirmation);
                        alertDialog.setMessage(finalFavorite_title);
                        alertDialog.setPositiveButton(finalFavorite_message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TootAction(id_string, "favourite");
                            }
                        });
                        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.create().show();
                    }

                    //テキストボックが未選択
                } else {
                    TootAction(id_string, "favourite");
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
                boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", false);
                if (boost_dialog) {
                    if (replace_snackber) {
                        Snackbar snackbar;
                        snackbar = Snackbar.make(finalView1, R.string.dialog_boost_info, Snackbar.LENGTH_SHORT);
                        snackbar.setAction(R.string.dialog_boost, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TootAction(id_string, "reblog");
                            }
                        });
                        snackbar.show();
                    } else {
                        //ダイアログ
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setTitle(R.string.confirmation);
                        alertDialog.setMessage(R.string.dialog_boost_info);
                        alertDialog.setPositiveButton(R.string.dialog_boost, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TootAction(id_string, "reblog");
                            }

                        });
                        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.create().show();
                    }

                    //チェックボックスが未チェックだったとき
                } else {
                    TootAction(id_string, "reblog");
                }

            }
        });

        //ブーストボタンにアイコンつける
        TextView boost_button = holder.boost_button;
        boost_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_black_24dp, 0, 0, 0);


        //ブラウザ、他クライアントで開くボタン設置
        TextView web_button = holder.web_button;
        web_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_more_vert_black_24dp, 0, 0, 0);


        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
        Bitmap back_icon = BitmapFactory.decodeResource(getContext().getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);

        //ポップアップメニューを展開する
        MenuBuilder menuBuilder = new MenuBuilder(getContext());
        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(R.menu.timeline_popup_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(getContext(), menuBuilder, web_button);
        optionsMenu.setForceShowIcon(true);


        String finalInstance = Instance;
        long account = item.getID();
        String user_id = item.getUserID();
        web_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Display the menu
                optionsMenu.show();

                //押したときの反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        //アカウント
                        if (item.getTitle().toString().contains(getContext().getString(R.string.account))) {
                            //読み込み
                            //画面分割用
                            boolean multipain_ui_mode = pref_setting.getBoolean("app_multipain_ui", false);
                            if (multipain_ui_mode) {
                                FragmentTransaction ft = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
                                Fragment fragment = new User_Fragment();
                                long account_id = account;
                                Bundle bundle = new Bundle();
                                bundle.putLong("Account_ID", account_id);
                                fragment.setArguments(bundle);
                                ft.replace(R.id.fragment3, fragment).commit();

                            } else {
                                long account_id = account;
                                Intent intent = new Intent(getContext(), UserActivity.class);
                                //IDを渡す
                                intent.putExtra("Account_ID", account_id);
                                getContext().startActivity(intent);
                            }
                        }
                        //ブラウザ
                        if (item.getTitle().toString().contains(getContext().getString(R.string.browser))) {
                            //有効
                            if (chrome_custom_tabs) {
                                String custom = CustomTabsHelper.getPackageNameToUse(getContext());
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.intent.setPackage(custom);
                                customTabsIntent.launchUrl((Activity) getContext(), Uri.parse("https://" + finalInstance + "/" + "@" + user_id + "/" + id_string));
                                //無効
                            } else {
                                Uri uri = Uri.parse("https://" + finalInstance + "/" + "@" + user_id + "/" + id_string);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                getContext().startActivity(intent);
                            }
                        }
                        //コピー
                        if (item.getTitle().toString().contains(getContext().getString(R.string.copy))) {
                            ClipboardManager clipboardManager =
                                    (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("", holder.tile_textview.getText().toString()));

                            Toast.makeText(getContext(), getContext().getString(R.string.copy) + " : " + holder.tile_textview.getText().toString(), Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {
                    }
                });
            }
        });

        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());


        //タイムラインに画像を表示
        //動的に画像を追加するよ
        //LinearLayout linearLayout = (LinearLayout) holder.linearLayout;
        //Wi-Fi接続時は有効？
        boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
        //GIFを再生するか？
        boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);

        boolean toot_media = pref_setting.getBoolean("pref_toot_media", false);


        media_url_1 = item.getMedia1();
        media_url_2 = item.getMedia2();
        media_url_3 = item.getMedia3();
        media_url_4 = item.getMedia4();


        if (media_url_1 != null) {
            //System.out.println("にゃーん :" + media_url_2);
            //非表示
            if (toot_media) {
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        media_url_1 = item.getMedia1();
                        media_url_2 = item.getMedia2();
                        media_url_3 = item.getMedia3();
                        media_url_4 = item.getMedia4();

                        if (setting_avater_gif) {
                            //GIFアニメ再生させない
                            ImageViewSetting(holder);
                            //表示
                            addMediaPicasso(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia);
                            addMediaPicasso(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia);
                            addMediaPicasso(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2);
                            addMediaPicasso(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2);

                        } else {
                            ImageViewSetting(holder);
                            //表示
                            //Glide.with(getContext()).load(media_url).into(holder.media_imageview_1);
                            addMediaGlide(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia);
                            addMediaGlide(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia);
                            addMediaGlide(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2);
                            addMediaGlide(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2);
                        }
                    }
                });
            }

            //Wi-Fi接続時
            if (setting_avater_wifi) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    media_url_1 = item.getMedia1();
                    media_url_2 = item.getMedia2();
                    media_url_3 = item.getMedia3();
                    media_url_4 = item.getMedia4();

                    if (setting_avater_gif) {
                        ImageViewSetting(holder);
                        //表示
                        addMediaPicasso(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia);
                        addMediaPicasso(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia);
                        addMediaPicasso(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2);
                        addMediaPicasso(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2);
                    } else {
                        ImageViewSetting(holder);
                        //画像を取ってくる
                        //表示
                        addMediaGlide(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia);
                        addMediaGlide(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia);
                        addMediaGlide(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2);
                        addMediaGlide(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2);
                    }
                }

                //Wi-Fi未接続
            } else {
                holder.imageButton.setText(R.string.show_image);
                holder.imageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_image_black_24dp, 0, 0, 0);
                ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ((LinearLayout.LayoutParams) layoutParams).weight = 1;
                holder.imageButton.setLayoutParams(layoutParams);
                if (holder.imageButton.getParent() != null) {
                    ((ViewGroup) holder.imageButton.getParent()).removeView(holder.imageButton);
                }
                holder.linearLayoutMediaButton.addView(holder.imageButton);

                //クリックしてイメージ表示
                String finalMedia_url1 = media_url;
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //表示
                        media_url_1 = item.getMedia1();
                        media_url_2 = item.getMedia2();
                        media_url_3 = item.getMedia3();
                        media_url_4 = item.getMedia4();

                        if (setting_avater_gif) {
                            //GIFアニメ再生させない
                            ImageViewSetting(holder);
                            //表示
                            addMediaPicasso(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia);
                            addMediaPicasso(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia);
                            addMediaPicasso(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2);
                            addMediaPicasso(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2);

                        } else {
                            ImageViewSetting(holder);
                            //画像を取ってくる
                            //表示
                            addMediaGlide(media_url_1, holder.media_imageview_1, holder.linearLayoutMedia);
                            addMediaGlide(media_url_2, holder.media_imageview_2, holder.linearLayoutMedia);
                            addMediaGlide(media_url_3, holder.media_imageview_3, holder.linearLayoutMedia2);
                            addMediaGlide(media_url_4, holder.media_imageview_4, holder.linearLayoutMedia2);
                        }
                    }
                });
            }
        }


        //サムネイル画像を設定
        ImageView thumbnail = (ImageView) holder.avater_imageview;
        //通信量節約
        boolean setting_avater_hidden = pref_setting.getBoolean("pref_avater", false);

        if (setting_avater_hidden) {
            thumbnail.setImageBitmap(item.getThumbnail());
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
                    Glide.with(view)
                            .load(avater_url)
                            .into(thumbnail);
                }
            }
            //Wi-Fi no Connection
            else {
                //レイアウトを消す
                if (!notification_layout) {
                    holder.vw1.removeView(holder.avaterImageview_linearLayout);
                }
            }
        } else {
            //レイアウトを消す
            if (!notification_layout) {
                holder.vw1.removeView(holder.avaterImageview_linearLayout);
            }
        }


        long account_id = item.getID();


        //ユーザー情報
        FragmentTransaction ft = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
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


        //ブックマーク関係
        boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", false);
        holder.bookmark_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark_border_black_24dp, 0, 0, 0);
        holder.bookmark_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookmark_delete) {
                    //消去
                    if (replace_snackber) {
                        Snackbar snackbar;
                        snackbar = Snackbar.make(finalView1, R.string.bookmark_delete_title, Snackbar.LENGTH_SHORT);
                        snackbar.setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //読み込み
                                if (sqLite == null) {
                                    sqLite = new TootBookmark_SQLite(getContext());
                                }

                                if (sqLiteDatabase == null) {
                                    sqLiteDatabase = sqLite.getReadableDatabase();
                                }
                                String info = holder.client_textview.getText().toString();
                                sqLiteDatabase.delete("tootbookmarkdb", "info=?", new String[]{info});
                                Toast.makeText(getContext(), R.string.delete, Toast.LENGTH_SHORT).show();
                            }
                        });
                        snackbar.show();
                    } else {
                        //ダイアログ
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setTitle(R.string.bookmark_delete_title);
                        alertDialog.setMessage(R.string.bookmark_delete_message);
                        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //読み込み
                                if (sqLite == null) {
                                    sqLite = new TootBookmark_SQLite(getContext());
                                }

                                if (sqLiteDatabase == null) {
                                    sqLiteDatabase = sqLite.getReadableDatabase();
                                }
                                String info = holder.client_textview.getText().toString();
                                sqLiteDatabase.delete("tootbookmarkdb", "info=?", new String[]{info});
                                Toast.makeText(getContext(), "Delete", Toast.LENGTH_SHORT).show();
                            }

                        });
                        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.create().show();
                    }

                    //書き込み
                } else {
                    boolean favorite_dialog = pref_setting.getBoolean("pref_bookmark_dialog", true);
                    boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", false);
                    if (favorite_dialog) {
                        if (replace_snackber) {
                            Snackbar snackbar;
                            snackbar = Snackbar.make(finalView1, R.string.dialog_bookmark_info, Snackbar.LENGTH_SHORT);
                            snackbar.setAction(R.string.bookmark, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    SQLitePut(item);
                                    Toast.makeText(getContext(), R.string.add_Bookmark, Toast.LENGTH_SHORT).show();
                                }
                            });
                            snackbar.show();

                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                            alertDialog.setTitle(R.string.confirmation);
                            alertDialog.setMessage(R.string.dialog_bookmark_info);
                            alertDialog.setPositiveButton(R.string.bookmark, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SQLitePut(item);
                                    Toast.makeText(getContext(), R.string.add_Bookmark, Toast.LENGTH_SHORT).show();
                                }
                            });
                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            alertDialog.create().show();
                        }
                    } else {
                        SQLitePut(item);
                        Toast.makeText(getContext(), R.string.add_Bookmark, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        //friends.nicoようにアンケートも実装するぞ！
        //アンケートっぽいトゥートを見つける

        if (item.getTitle().contains("friends.nico アンケート")) {
            System.out.println("アンケート発見 : " + String.valueOf(item.getID()));

            //!で条件を反転させる
            if (!item.getTitle().contains("friends.nico アンケート(結果)")) {

                LinearLayout.LayoutParams button_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                button_params.weight = 1;

                //imageLinearLayoutにボタンを入れる
                Button enquete_1 = new Button(getContext());
                enquete_1.setText("1");
                enquete_1.setLayoutParams(button_params);
                Button enquete_2 = new Button(getContext());
                enquete_2.setText("2");
                enquete_2.setLayoutParams(button_params);
                Button enquete_3 = new Button(getContext());
                enquete_3.setText("3");
                enquete_3.setLayoutParams(button_params);
                Button enquete_4 = new Button(getContext());
                enquete_4.setText("4");
                enquete_4.setLayoutParams(button_params);
                Button enquete_5 = new Button(getContext());
                enquete_5.setText("🤔");
                enquete_5.setLayoutParams(button_params);
                holder.linearLayoutEnquate.addView(enquete_1);
                holder.linearLayoutEnquate.addView(enquete_2);
                holder.linearLayoutEnquate.addView(enquete_3);
                holder.linearLayoutEnquate.addView(enquete_4);
                holder.linearLayoutEnquate.addView(enquete_5);

                //何個目か?
                final int[] enquete_select = new int[1];

                enquete_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enquete_select[0] = 1;

                        boolean enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false);
                        if (enquate_dialog) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                            alertDialog.setTitle(R.string.confirmation);
                            alertDialog.setMessage(R.string.enquate_dialog);
                            alertDialog.setPositiveButton(R.string.vote, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FriendsNicoEnquate(id_string, "0", "１番目に投票しました : ");
                                }
                            });
                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.create().show();
                        } else {
                            FriendsNicoEnquate(id_string, "0", "１番目に投票しました : ");
                        }
                    }
                });

                enquete_2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enquete_select[0] = 2;
                        boolean enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false);
                        if (enquate_dialog) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                            alertDialog.setTitle(R.string.confirmation);
                            alertDialog.setMessage(R.string.enquate_dialog);
                            alertDialog.setPositiveButton(R.string.vote, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FriendsNicoEnquate(id_string, "1", "２番目に投票しました : ");
                                }
                            });
                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.create().show();
                        } else {
                            FriendsNicoEnquate(id_string, "1", "２番目に投票しました : ");
                        }
                    }
                });

                enquete_3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enquete_select[0] = 3;
                        boolean enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false);
                        if (enquate_dialog) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                            alertDialog.setTitle(R.string.confirmation);
                            alertDialog.setMessage(R.string.enquate_dialog);
                            alertDialog.setPositiveButton(R.string.vote, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FriendsNicoEnquate(id_string, "2", "３番目に投票しました : ");
                                }
                            });
                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.create().show();
                        } else {
                            FriendsNicoEnquate(id_string, "2", "３番目に投票しました : ");
                        }
                    }
                });

                enquete_4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enquete_select[0] = 4;
                        boolean enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false);
                        if (enquate_dialog) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                            alertDialog.setTitle(R.string.confirmation);
                            alertDialog.setMessage(R.string.enquate_dialog);
                            alertDialog.setPositiveButton(R.string.vote, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FriendsNicoEnquate(id_string, "3", "４番目に投票しました : ");
                                }
                            });
                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.create().show();
                        } else {
                            FriendsNicoEnquate(id_string, "3", "４番目に投票しました : ");
                        }
                    }
                });

                enquete_5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enquete_select[0] = 5;
                        boolean enquate_dialog = pref_setting.getBoolean("pref_enquete_dialog", false);
                        if (enquate_dialog) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                            alertDialog.setTitle(R.string.confirmation);
                            alertDialog.setMessage(R.string.enquate_dialog);
                            alertDialog.setPositiveButton(R.string.vote, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FriendsNicoEnquate(id_string, "4", "５番目に投票しました : ");
                                }
                            });
                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.create().show();
                        } else {
                            FriendsNicoEnquate(id_string, "4", "５番目に投票しました : ");
                        }
                    }
                });
            }
        }


        //html src　を読み込めるようにする
        //カスタム絵文字用

        //とりあえずトゥートだけ

        String toot_text = Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT).toString();
        System.out.println("もとの : " + toot_text);

        String first_toot_text = item.getTitle();
        String second_toot_text = null;

        String finalFirst_toot_text = item.getTitle();

        //ImageGetter
        //カスタム絵文字
        Html.ImageGetter toot_imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                final LevelListDrawable[] d = {new LevelListDrawable()};
                //final Drawable[] d = {null};
                Drawable empty = getContext().getResources().getDrawable(R.drawable.ic_refresh_black_24dp);
                d[0].addLevel(0, 0, empty);
                d[0].setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());

                new LoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, source, d[0]);
                return d[0];
            }
        };


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
        if (font_setting_swich) {
            //ゆーざー
            String user_font_color = pref_setting.getString("pref_fontcolor", "#000000");
            user.setTextColor(Color.parseColor(user_font_color));

            //たいむらいん
            String toot_font_color = pref_setting.getString("pref_fontcolor_toot", "#000000");
            title.setTextColor(Color.parseColor(toot_font_color));
            //くらいあんと
            String client_font_color = pref_setting.getString("pref_fontcolor_client", "#000000");
            client.setTextColor(Color.parseColor(client_font_color));

        } else {

        }

        //絵文字強制
        boolean emoji_compatibility = pref_setting.getBoolean("pref_emoji_compatibility", false);
        if (emoji_compatibility) {
/*
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
*/
            //本文 ユーザー名
            EmojiCompat.get().registerInitCallback(new EmojiCompat.InitCallback() {
                @Override
                public void onInitialized() {
                    if (title != null) {
                        if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                            //カスタム絵文字有効時
                            if (setting_avater_wifi) {
                                //WIFIのみ表示有効時
                                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                    //WIFI接続中か確認
                                    //接続中
                                    try {
                                        title.setText((Html.fromHtml(final_toot_text, toot_imageGetter, null)));
                                        user.setText((Html.fromHtml(item.getUser(), toot_imageGetter, null)));
                                    } catch (NullPointerException e) {
                                        title.setText((Html.fromHtml(item.getTitle(), toot_imageGetter, null)));
                                        user.setText((Html.fromHtml(item.getUser(), toot_imageGetter, null)));
                                    }
                                } else {
                                    //確認したけどWIFI接続確認できなかった
                                    title.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT));
                                    user.setText(Html.fromHtml(item.getUser(), Html.FROM_HTML_MODE_COMPACT));
                                }
                            } else {
                                //WIFIのみ表示無効時
                                //そのまま表示させる
                                try {
                                    title.setText((Html.fromHtml(final_toot_text, toot_imageGetter, null)));
                                    user.setText((Html.fromHtml(item.getUser(), toot_imageGetter, null)));
                                } catch (NullPointerException e) {
                                    title.setText((Html.fromHtml(item.getTitle(), toot_imageGetter, null)));
                                    user.setText((Html.fromHtml(item.getUser(), toot_imageGetter, null)));
                                }
                            }
                        } else {
                            title.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT));
                            user.setText(Html.fromHtml(item.getUser(), Html.FROM_HTML_MODE_COMPACT));
                        }
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
            //user.setText(item.getUser());
            if (title != null) {
                if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                    //カスタム絵文字有効時
                    if (setting_avater_wifi) {
                        //WIFIのみ表示有効時
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            //WIFI接続中か確認
                            //接続中
                            try {
                                title.setText((Html.fromHtml(final_toot_text, toot_imageGetter, null)));
                                user.setText((Html.fromHtml(item.getUser(), toot_imageGetter, null)));
                            } catch (NullPointerException e) {
                                title.setText((Html.fromHtml(item.getTitle(), toot_imageGetter, null)));
                                user.setText((Html.fromHtml(item.getUser(), toot_imageGetter, null)));
                            }
                        } else {
                            //確認したけどWIFI接続確認できなかった
                            title.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT));
                            user.setText(Html.fromHtml(item.getUser(), Html.FROM_HTML_MODE_COMPACT));
                        }
                    } else {
                        //WIFIのみ表示無効時
                        //そのまま表示させる
                        try {
                            title.setText((Html.fromHtml(final_toot_text, toot_imageGetter, null)));
                            user.setText((Html.fromHtml(item.getUser(), toot_imageGetter, null)));
                        } catch (NullPointerException e) {
                            title.setText((Html.fromHtml(item.getTitle(), toot_imageGetter, null)));
                            user.setText((Html.fromHtml(item.getUser(), toot_imageGetter, null)));
                        }
                    }
                } else {
                    title.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT));
                    user.setText(Html.fromHtml(item.getUser(), Html.FROM_HTML_MODE_COMPACT));
                }
            }
            //title.setText((Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT)));
            client.setText(item.getClient());
        }


        final_toot_text = null;


        //アイコンオンリー
        boolean button_icon = pref_setting.getBoolean("pref_button_icon", false);
        if (button_icon)

        {
            boost_button.setText("");
            nicoru.setText("");
            web_button.setText("");
            holder.bookmark_button.setText("");
        } else

        {

        }

        //ダークモード、OLEDモード時にアイコンが見えない問題
        //どちらかが有効の場合
        boolean dark_mode = pref_setting.getBoolean("pref_dark_theme", false);
        boolean oled_mode = pref_setting.getBoolean("pref_oled_mode", false);
        if (dark_mode || oled_mode)

        {
            boost_button.setTextColor(Color.parseColor("#ffffff"));
            nicoru.setTextColor(Color.parseColor("#ffffff"));
            web_button.setTextColor(Color.parseColor("#ffffff"));
            holder.bookmark_button.setTextColor(Color.parseColor("#ffffff"));

            //アイコンを取得
            Drawable boost_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp, null);
            Drawable web_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_more_vert_black_24dp, null);
            Drawable bookmark_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_bookmark_border_black_24dp, null);

            //染色
            boost_icon_white.setTint(Color.parseColor("#ffffff"));
            web_icon_white.setTint(Color.parseColor("#ffffff"));
            bookmark_icon_white.setTint(Color.parseColor("#ffffff"));

            //入れる
            boost_button.setCompoundDrawablesWithIntrinsicBounds(boost_icon_white, null, null, null);
            web_button.setCompoundDrawablesWithIntrinsicBounds(web_icon_white, null, null, null);
            holder.bookmark_button.setCompoundDrawablesWithIntrinsicBounds(bookmark_icon_white, null, null, null);


            //ニコるをお気に入りに変更 設定次第
            //メッセージも変更できるようにする
            if (friends_nico_check_box) {
                Drawable favourite_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_black_24dp, null);
                favourite_icon_white.setTint(Color.parseColor("#ffffff"));
                nicoru.setCompoundDrawablesWithIntrinsicBounds(favourite_icon_white, null, null, null);
            }
        }


        //ボタン非表示モード
        boolean button_hidden = pref_setting.getBoolean("pref_timeline_button", false);
        if (button_hidden) {

            LinearLayout button_layout = holder.button_linearLayout;
            button_layout.removeView(nicoru);
            button_layout.removeView(boost);
            button_layout.removeView(web_button);
            button_layout.removeView(holder.bookmark_button);

            LinearLayout toot_layout = holder.toot_linearLayout;

            //めにゅー
            String finalFavorite_title1 = favorite_message;
            View finalView = view;
            toot_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getContext(), "Test", Toast.LENGTH_SHORT).show();

                    final String[] items = {finalFavorite_title1, finalView.getContext().getString(R.string.boost_button), "Web", finalView.getContext().getString(R.string.bookmark), finalView.getContext().getString(R.string.account)};
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
                                            alertDialog.setMessage(R.string.dialog_boost_info);
                                            alertDialog.setPositiveButton(R.string.boost_button, new DialogInterface.OnClickListener() {

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

                                                            System.out.println("=====" + client.post("statuses/" + id_string + "/reblog", requestBody));

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
                                                    Toast.makeText(getContext(), finalNicoru_text + result, Toast.LENGTH_SHORT).show();
                                                }

                                            }.execute();

                                        }

                                    }
                                    //Web
                                    if (which == 2) {
                                        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);

                                        //戻るアイコン
                                        Bitmap back_icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_arrow_back);

                                        //有効
                                        if (chrome_custom_tabs) {

                                            String custom = CustomTabsHelper.getPackageNameToUse(getContext());

                                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                            CustomTabsIntent customTabsIntent = builder.build();
                                            customTabsIntent.intent.setPackage(custom);
                                            customTabsIntent.launchUrl((Activity) getContext(), Uri.parse("https://" + Instance + "/" + "@" + item.getUserID() + "/" + id_string));
                                            //無効
                                        } else {
                                            Uri uri = Uri.parse("https://" + Instance + "/" + "@" + item.getUserID() + "/" + id_string);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            getContext().startActivity(intent);

                                        }
                                    }
                                    //ブックマーク
                                    if (which == 3) {
                                        boolean favorite = pref_setting.getBoolean("pref_nicoru_dialog", false);
                                        if (favorite) {

                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                            alertDialog.setTitle(R.string.confirmation);
                                            alertDialog.setMessage(R.string.dialog_boost_info);
                                            alertDialog.setPositiveButton(R.string.boost_button, new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (sqLite == null) {
                                                        sqLite = new TootBookmark_SQLite(getContext());
                                                    }

                                                    if (sqLiteDatabase == null) {
                                                        sqLiteDatabase = sqLite.getWritableDatabase();
                                                    }

                                                    String toot_sq = item.getTitle();
                                                    String id_sq = String.valueOf(item.getNicoru());

                                                    ContentValues contentValues = new ContentValues();
                                                    contentValues.put("toot", toot_sq);
                                                    contentValues.put("id", id_sq);
                                                    sqLiteDatabase.insert("tootbookmarkdb", null, contentValues);

                                                    Toast.makeText(getContext(), R.string.add_Bookmark, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            alertDialog.create().show();

                                        } else {
                                            if (sqLite == null) {
                                                sqLite = new TootBookmark_SQLite(getContext());
                                            }

                                            if (sqLiteDatabase == null) {
                                                sqLiteDatabase = sqLite.getWritableDatabase();
                                            }

                                            String toot_sq = item.getTitle();
                                            String id_sq = String.valueOf(item.getNicoru());

                                            ContentValues contentValues = new ContentValues();
                                            contentValues.put("toot", toot_sq);
                                            contentValues.put("id", id_sq);
                                            sqLiteDatabase.insert("tootbookmarkdb", null, contentValues);
                                            Toast.makeText(getContext(), R.string.add_Bookmark, Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    //Account
                                    if (which == 4) {
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
                                }
                            }).show();
                }
            });

        } else {

        }

        return view;
    }
//ニコる
//    ImageButton nicoru = (ImageButton) view.findViewById(R.id.nicoru);


    private class ViewHolder {
        ImageView media_imageview_1;
        ImageView media_imageview_2;
        ImageView media_imageview_3;
        ImageView media_imageview_4;
        ImageView notification_icon;

        ImageView avater_imageview;

        TextView user_textview;
        TextView tile_textview;
        TextView client_textview;

        TextView nicoru_button;
        TextView boost_button;
        TextView bookmark_button;
        TextView web_button;

        LinearLayout linearLayoutMediaButton;
        LinearLayout linearLayoutMedia;
        LinearLayout linearLayoutMedia2;
        LinearLayout linearLayoutEnquate;
        LinearLayout vw1;
        LinearLayout toot_linearLayout;
        LinearLayout button_linearLayout;
        LinearLayout avaterImageview_linearLayout;

        Button imageButton;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            final Bitmap[] bitmap = {null};
            final Drawable[] drawable = {null};
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
                CharSequence t = mTv.getText();
                mTv.setText(t);
                mTv.refreshDrawableState();
            }
        }

    }


    public void addMediaGlide(String mediaURL, ImageView ImageView, LinearLayout linearLayout) {
        if (mediaURL != null) {
            //画像を取ってくる
            Glide.with(getContext())
                    .load(mediaURL)
                    //Overrideはサイズ、placeholderは読み込み中アイコン
                    .apply(new RequestOptions()
                            //.override(500, 500)
                            .placeholder(R.drawable.ic_sync_black_24dp))
                    .into(ImageView);
            //呼び出し（こっわ
            if (ImageView.getParent() != null) {
                ((ViewGroup) ImageView.getParent()).removeView(ImageView);
            }
            //表示
            ImageViewClickCustomTab(ImageView, mediaURL);
            linearLayout.addView(ImageView);
        }
    }

    public void addMediaPicasso(String mediaURL, ImageView ImageView, LinearLayout linearLayout) {
        if (mediaURL != null) {
            //画像を取ってくる
            Picasso.get()
                    .load(mediaURL)
                    //.resize(500, 500)
                    .placeholder(R.drawable.ic_sync_black_24dp)
                    .into(ImageView);
            //呼び出し（こっわ
            if (ImageView.getParent() != null) {
                ((ViewGroup) ImageView.getParent()).removeView(ImageView);
            }
            //表示
            ImageViewClickCustomTab(ImageView, mediaURL);
            linearLayout.addView(ImageView);
        }
    }

    public void ImageViewSetting(ViewHolder holder) {
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((LinearLayout.LayoutParams) layoutParams).weight = 1;
        holder.media_imageview_1.setLayoutParams(layoutParams);
        holder.media_imageview_2.setLayoutParams(layoutParams);
        holder.media_imageview_3.setLayoutParams(layoutParams);
        holder.media_imageview_4.setLayoutParams(layoutParams);
        holder.media_imageview_1.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_2.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_3.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_4.setScaleType(ImageView.ScaleType.CENTER);

    }

    public void ImageViewClickCustomTab(ImageView ImageView, String mediaURL) {
        ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
                //カスタムタグ有効
                if (chrome_custom_tabs) {
                    Bitmap back_icon = BitmapFactory.decodeResource(getContext().getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);
                    String custom = CustomTabsHelper.getPackageNameToUse(getContext());
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl(getContext(), Uri.parse(mediaURL));
                    //無効
                } else {
                    Uri uri = Uri.parse(mediaURL);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    getContext().startActivity(intent);
                }
            }
        });
    }

    public void TootAction(String id, String endPoint) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                MastodonClient client = new MastodonClient.Builder(Instance, new OkHttpClient.Builder(), new Gson()).accessToken(AccessToken).build();
                RequestBody requestBody = new FormBody.Builder()
                        .build();
                client.post("statuses/" + id + "/" + endPoint, requestBody);
                return id;
            }

            @Override
            protected void onPostExecute(String result) {
                if (endPoint.contains("Replace")) {
                    Toast.makeText(getContext(), getContext().getString(R.string.boost_ok) + result, Toast.LENGTH_SHORT).show();
                }
                if (endPoint.contains("favourite")) {
                    Toast.makeText(getContext(), nicoru_text + result, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public void FriendsNicoEnquate(String id_string, String number, String ToastMessage) {
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                MastodonClient client = new MastodonClient.Builder(Instance, new OkHttpClient.Builder(), new Gson()).accessToken(AccessToken).build();

                RequestBody requestBody = new FormBody.Builder()
                        .add("item_index", number)
                        .build();


                String enquate_json_1 = "{\"item_index\":\"" + "1" + "\"\"}";
                String url_link = "https://" + Instance + "/api/v1/suggestions/?stream=user&access_token=" + AccessToken;

                System.out.println("=====" + client.post("votes/" + id_string, requestBody));

                return id_string;
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(getContext(), ToastMessage + result, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }


    public void SQLitePut(ListItem item) {
        if (sqLite == null) {
            sqLite = new TootBookmark_SQLite(getContext());
        }

        if (sqLiteDatabase == null) {
            sqLiteDatabase = sqLite.getWritableDatabase();
        }

        String toot_sq = item.getTitle();
        String id_sq = String.valueOf(item.getNicoru());
        String account = item.getUser();
        String info = item.getClient();
        String account_id = String.valueOf(item.getID());
        String avater = item.getAvater();
        String account_id_string = item.getUserID();

        String media_1 = item.getMedia1();
        String media_2 = item.getMedia2();
        String media_3 = item.getMedia3();
        String media_4 = item.getMedia4();

        ContentValues contentValues = new ContentValues();
        contentValues.put("toot", toot_sq);
        contentValues.put("id", id_sq);
        contentValues.put("account", account);
        contentValues.put("info", info);
        contentValues.put("account_id", account_id);
        contentValues.put("avater_url", avater);
        contentValues.put("username", account_id_string);

        contentValues.put("media1", media_1);
        contentValues.put("media2", media_2);
        contentValues.put("media3", media_3);
        contentValues.put("media4", media_4);


        sqLiteDatabase.insert("tootbookmarkdb", null, contentValues);

        Toast.makeText(getContext(), R.string.add_Bookmark, Toast.LENGTH_SHORT).show();
    }

    public void LayoutSimple(ViewHolder holder) {
        holder.button_linearLayout.removeView(holder.bookmark_button);
        holder.button_linearLayout.removeView(holder.web_button);
        holder.button_linearLayout.removeView(holder.boost_button);
        holder.button_linearLayout.removeView(holder.nicoru_button);
    }

    public void setSVGAnimationIcon(int animationIcon, int notAnimationIcon, ViewHolder holder) {
        //SVG許可
        boolean svgAnimation = pref_setting.getBoolean("pref_svg_animation", false);
        //無効
        if (svgAnimation) {
            holder.notification_icon.setImageResource(notAnimationIcon);
            if (holder.notification_icon.getParent() != null) {
                ((ViewGroup) holder.notification_icon.getParent()).removeView(holder.notification_icon);
            }
            holder.avaterImageview_linearLayout.addView(holder.notification_icon, 0);
            //有効
        } else {
            holder.notification_icon.setImageResource(animationIcon);
            if (holder.notification_icon.getParent() != null) {
                ((ViewGroup) holder.notification_icon.getParent()).removeView(holder.notification_icon);
            }
            Animatable2 animatable = (Animatable2) holder.notification_icon.getDrawable();
            animatable.start();
            animatable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    animatable.start();
                    super.onAnimationEnd(drawable);
                }
            });
            holder.avaterImageview_linearLayout.addView(holder.notification_icon, 0);
        }
    }


}