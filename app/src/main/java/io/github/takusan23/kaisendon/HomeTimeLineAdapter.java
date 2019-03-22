package io.github.takusan23.kaisendon;

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
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.PaintDrawable;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;

import io.github.takusan23.kaisendon.Activity.UserActivity;
import io.github.takusan23.kaisendon.CustomTabURL.LinkTransformationMethod;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import io.github.takusan23.kaisendon.Fragment.User_Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    //ViewHolder
    ViewHolder holder;

    //絵文字表示するか
    private boolean emojis_show;

    //ブックマークのボタンの動作決定部分
    boolean bookmark_delete = false;

    //通知のフラグメントのときは画像非表示モードでもレイアウトを消さないように
    boolean notification_layout = false;

    //カスタムメニュー用
    private boolean dialog_not_show = false;    //ダイアログ出さない
    private boolean image_show = false;         //強制画像表示
    private boolean quick_profile = false;      //クイックプロフィール有効
    private boolean custom_emoji = false;       //トゥートカウンターを有効

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

            //Card
            holder.card_linearLayout = view.findViewById(R.id.linearlayout_card);
            holder.cardImageView = new ImageView(getContext());
            holder.cardTextView = new TextView(getContext());

            //添付メディア
            holder.media_imageview_1 = new ImageView(getContext());
            holder.media_imageview_2 = new ImageView(getContext());
            holder.media_imageview_3 = new ImageView(getContext());
            holder.media_imageview_4 = new ImageView(getContext());
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
        ArrayList<String> listItem = item.getListItem();


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

        //カスタムメニュー用設定
        if (listItem.get(0).contains("CustomMenu")) {
            //ダイアログ出さない
            if (Boolean.valueOf(listItem.get(25))) {
                dialog_not_show = true;
            }
            //強制画像表示
            if (Boolean.valueOf(listItem.get(26))) {
                image_show = true;
            }
            //クイックプロフィール
            if (Boolean.valueOf(listItem.get(27))) {
                quick_profile = true;
            }
            //カスタム絵文字
            if (Boolean.valueOf(listItem.get(28))) {
                emojis_show = true;
            }
        }


        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        //カスタム絵文字有効/無効
        if (pref_setting.getBoolean("pref_custom_emoji", false)) {
            if (pref_setting.getBoolean("pref_avater_wifi", true)) {
                //WIFIのみ表示有効時
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    //WIFI
                    emojis_show = true;
                }
            } else {
                //WIFI/MOBILE DATA 関係なく表示
                emojis_show = true;
            }
        }


        //ニコるをお気に入りに変更 設定次第
        //メッセージも変更できるようにする
        boolean friends_nico_check_box = pref_setting.getBoolean("pref_friends_nico_mode", false);
        if (!friends_nico_check_box) {

            nicoru.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black_24dp, 0, 0, 0);

            Locale locale = Locale.getDefault();
            if (locale.equals(Locale.JAPAN)) {
                //nicoru.setText("お気に入り");
                nicoru_text = "お気に入りに登録しました : ";
            } else {
                //nicoru.setText("Favorite");
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
        String id_string = listItem.get(4);
        String media_url = listItem.get(8);

        // ふぁぼった、ぶーすとした
        final boolean[] favClick = {false};
        final boolean[] boostClick = {false};

        //ホームのみ　ぶーすとのとき用
        //BoostしたTootのとき　ホーム用
        boolean reblogToot = false;
        boolean boostFavCount = false;
        if (item.getListItem().size() >= 21) {
            reblogToot = true;
        }
        if (item.getListItem().size() >= 17) {
            boostFavCount = true;
        }

        //ブースト　それ以外
        //ブーストの要素がnullだったらそのまま
        String avater_url = null;
        if (reblogToot && listItem.get(20) != null) {
            avater_url = listItem.get(22);
        } else {
            //要素があったとき
            avater_url = listItem.get(5);
        }


        //カード　配列管理
        String card_title = listItem.get(12);
        String card_url = listItem.get(13);
        String card_description = listItem.get(14);
        String card_image = listItem.get(15);


        //ArrayList<String> arrayList = item.getStringList();
        if (card_title != null) {

            //System.out.println("カード" + card_title);

            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imageLayoutParams.weight = 4;
            textLayoutParams.weight = 1;

            //カード実装
            if (holder.cardImageView.getParent() != null) {
                ((ViewGroup) holder.cardImageView.getParent()).removeView(holder.cardImageView);
            }
            //カード実装
            if (holder.cardTextView.getParent() != null) {
                ((ViewGroup) holder.cardTextView.getParent()).removeView(holder.cardTextView);
            }

            String finalCard_url = card_url;
            ImageViewClickCustomTab_LinearLayout(holder.card_linearLayout, finalCard_url);

            //タイムラインに画像を表示
            //動的に画像を追加するよ
            //LinearLayout linearLayout = (LinearLayout) holder.linearLayout;
            //Wi-Fi接続時は有効？
            boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
            boolean toot_media = pref_setting.getBoolean("pref_toot_media", false);

            //タイムラインに画像を表示
            if (card_url != null) {
                //System.out.println("にゃーん :" + media_url_2);
                //Wi-Fi接続時
                if (setting_avater_wifi) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        holder.card_linearLayout.addView(holder.cardImageView);
                        Glide.with(getContext()).load(card_image).into(holder.cardImageView);
                    }
                } else if (!toot_media || image_show) {
                    holder.card_linearLayout.addView(holder.cardImageView);
                    Glide.with(getContext()).load(card_image).into(holder.cardImageView);
                }
            }

            holder.card_linearLayout.setLayoutParams(linearLayoutParams);
            holder.card_linearLayout.addView(holder.cardTextView);
            holder.cardTextView.setLayoutParams(textLayoutParams);
            holder.cardTextView.setText(card_title + "\n" + card_description);
            holder.cardTextView.setTextSize(Integer.valueOf(pref_setting.getString("pref_fontsize_card", "10")));
            holder.cardImageView.setLayoutParams(imageLayoutParams);
            //Cardくそ見にくいから枠つけるか
            holder.card_linearLayout.setBackground(getContext().getDrawable(R.drawable.button_style));
        }


        //背景色を変える機能
        //ブックマーク削除など

        //SVG許可
        boolean svgAnimation = pref_setting.getBoolean("pref_svg_animation", false);

        String type = listItem.get(0);

        if (type != null) {
            if (type.contains("custom_notification")) {
                holder.vw1.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_notification_color", "#1A008000")));
            }
            if (type.contains("custom_home")) {
                holder.vw1.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_home_color", "#1Aff0000")));
            }
            if (type.contains("custom_local")) {
                holder.vw1.setBackgroundColor(Color.parseColor(pref_setting.getString("pref_custom_streaming_local_color", "#1A0000ff")));
            }
            if (type.contains("bookmark")) {
                bookmark_delete = true;
            }
            //ブースト
            if (type.contains("Notification_reblog")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder);
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true;
                //アニメーションアイコン
                setSVGAnimationIcon(R.drawable.notification_to_boost, R.drawable.ic_repeat_black_24dp, holder);
            }
            //お気に入り
            if (type.contains("Notification_favourite")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder);
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true;
                //アニメーションアイコン
                //friends.nicoモードかな？
                if (!friends_nico_check_box) {
                    setSVGAnimationIcon(R.drawable.notification_to_star, R.drawable.ic_star_black_24dp, holder);
                } else {
                    holder.notification_icon.setImageResource(R.drawable.nicoru);
                    if (holder.notification_icon.getParent() != null) {
                        ((ViewGroup) holder.notification_icon.getParent()).removeView(holder.notification_icon);
                    }
                    holder.avaterImageview_linearLayout.addView(holder.notification_icon, 0);
                }
            }
            //ふぉろー
            if (type.contains("Notification_follow")) {
                //ボタンを消し飛ばす
                LayoutSimple(holder);
                //アバター画像非表示モードでもレイアウトは残しておくように
                notification_layout = true;
                //アニメーションアイコン
                setSVGAnimationIcon(R.drawable.notification_to_person, R.drawable.ic_person_add_black_24dp, holder);
            }
            //めんしょん
            if (type.contains("Notification_mention")) {
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
        if (!nicoru_dialog_chack) {
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
        boolean finalBoostFavCount = boostFavCount;
        nicoru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //もってくる
                String apiURL = "favourite";
                //Snackber Text
                String snackberTitle = finalFavorite_title;
                String snackberButton = finalFavorite_message;
                //配列の範囲内にするため
                if (finalBoostFavCount) {
                    String isFav = item.getListItem().get(17);
                    //すでにFav済みの場合は外すAPIを叩く
                    if (isFav.contains("favourited") || favClick[0]) {
                        apiURL = "unfavourite";
                        snackberTitle = getContext().getString(R.string.delete_fav);
                        snackberButton = getContext().getString(R.string.delete_text);
                    }
                }
                String finalApiURL = apiURL;

                boolean favorite = pref_setting.getBoolean("pref_nicoru_dialog", true);
                boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", true);
                //ダイアログ表示する？
                if (favorite && !dialog_not_show) {
                    if (replace_snackber) {

                        Snackbar favourite_snackbar;
                        favourite_snackbar = Snackbar.make(finalView1, snackberTitle, Snackbar.LENGTH_SHORT);
                        favourite_snackbar.setAction(snackberButton, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TootAction(id_string, finalApiURL, nicoru);
                                favClick[0] = true;
                                if (finalBoostFavCount) {
                                    item.getListItem().set(17, "favourited");
                                }
                            }
                        });
                        favourite_snackbar.show();
                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setTitle(R.string.confirmation);
                        alertDialog.setMessage(snackberTitle);
                        alertDialog.setPositiveButton(snackberButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TootAction(id_string, finalApiURL, nicoru);
                                favClick[0] = true;
                                if (finalBoostFavCount) {
                                    item.getListItem().set(17, "favourited");
                                }
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
                    TootAction(id_string, finalApiURL, nicoru);
                    favClick[0] = true;
                    if (finalBoostFavCount) {
                        item.getListItem().set(17, "favourited");
                    }
                }

            }
        });


        //ブースト
        String finalAccessToken2 = AccessToken;
        boost.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //すでにブースト済みの場合は外すAPIにする
                String apiURL = "reblog";
                //Snackber
                String snackberTitle = getContext().getString(R.string.dialog_boost_info);
                String snackberButton = getContext().getString(R.string.dialog_boost);

                if (finalBoostFavCount) {
                    String isBoost = item.getListItem().get(16);
                    if (isBoost.contains("reblogged") || boostClick[0]) {
                        apiURL = "unreblog";
                        snackberTitle = getContext().getString(R.string.delete_bt);
                        snackberButton = getContext().getString(R.string.delete_text);
                    }
                }
                String finalApiURL = apiURL;
                //設定でダイアログをだすかどうか
                boolean boost_dialog = pref_setting.getBoolean("pref_boost_dialog", true);
                boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", true);
                //ダイアログ表示する？
                if (boost_dialog && !dialog_not_show) {
                    if (replace_snackber) {
                        Snackbar snackbar;
                        snackbar = Snackbar.make(finalView1, snackberTitle, Snackbar.LENGTH_SHORT);
                        snackbar.setAction(snackberButton, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TootAction(id_string, finalApiURL, boost);
                                boostClick[0] = true;
                                if (finalBoostFavCount) {
                                    item.getListItem().set(16, "reblogged");
                                }
                            }
                        });
                        snackbar.show();
                    } else {
                        //ダイアログ
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setTitle(R.string.confirmation);
                        alertDialog.setMessage(snackberTitle);
                        alertDialog.setPositiveButton(snackberButton, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TootAction(id_string, finalApiURL, boost);
                                boostClick[0] = true;
                                if (finalBoostFavCount) {
                                    item.getListItem().set(16, "reblogged");
                                }
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
                    TootAction(id_string, finalApiURL, boost);
                    boostClick[0] = true;
                    if (finalBoostFavCount) {
                        item.getListItem().set(16, "reblogged");
                    }
                }

            }
        });

        //Fav/BT機能
        nicoru.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //面倒なので事前に調べたりはしない
                //設定でダイアログをだすかどうか
                boolean fav_bt = pref_setting.getBoolean("pref_fav_and_bt_dialog", true);
                boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", true);
                if (fav_bt && !dialog_not_show) {
                    if (replace_snackber) {
                        Snackbar snackbar;
                        snackbar = Snackbar.make(finalView1, R.string.favAndBT, Snackbar.LENGTH_SHORT);
                        snackbar.setAction("Fav+BT", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TootAction(id_string, "favourite", boost);
                                TootAction(id_string, "reblog", boost);
                                boostClick[0] = true;
                                if (finalBoostFavCount) {
                                    item.getListItem().set(16, "reblogged");
                                    item.getListItem().set(17, "favourited");
                                }
                            }
                        });
                        snackbar.show();
                    } else {
                        //ダイアログ
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setTitle(R.string.confirmation);
                        alertDialog.setMessage(R.string.favAndBT);
                        alertDialog.setPositiveButton("Fav+BT", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TootAction(id_string, "favourite", boost);
                                TootAction(id_string, "reblog", boost);
                                boostClick[0] = true;
                                if (finalBoostFavCount) {
                                    item.getListItem().set(16, "reblogged");
                                    item.getListItem().set(17, "favourited");
                                }
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
                    TootAction(id_string, "favourite", boost);
                    TootAction(id_string, "reblog", boost);
                    boostClick[0] = true;
                    if (finalBoostFavCount) {
                        item.getListItem().set(16, "reblogged");
                        item.getListItem().set(17, "favourited");
                    }
                }
                //OnClickListenerが呼ばれないようにする
                return true;
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
        long account = Long.valueOf(listItem.get(6));
        String user_id = listItem.get(7);
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
                        if (item.toString().contains(getContext().getString(R.string.account))) {
                            //読み込み
                            //Quick Profile
                            if (pref_setting.getBoolean("pref_quick_profile", false) || quick_profile) {
                                //クイックプロフィーる
                                quickProfileSnackber(v, String.valueOf(account));
                            } else {
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
                        }
                        //ブラウザ
                        if (item.toString().contains(getContext().getString(R.string.browser))) {
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
                        if (item.toString().contains(getContext().getString(R.string.copy))) {
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

        //タイムラインに画像を表示
        //動的に画像を追加するよ
        //LinearLayout linearLayout = (LinearLayout) holder.linearLayout;
        //Wi-Fi接続時は有効？
        boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
        //GIFを再生するか？
        boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);

        boolean toot_media = pref_setting.getBoolean("pref_toot_media", false);


        media_url_1 = listItem.get(8);
        media_url_2 = listItem.get(9);
        media_url_3 = listItem.get(10);
        media_url_4 = listItem.get(11);


        if (media_url_1 != null) {
            //System.out.println("にゃーん :" + media_url_2);
            //非表示
            if (toot_media || image_show) {
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

            //Wi-Fi接続時　か　強制画像表示
            if (setting_avater_wifi || image_show) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

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
            //thumbnail.setImageBitmap(item.getThumbnail());
        }
        //Wi-Fi か　強制画像表示
        if (setting_avater_wifi || image_show) {
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

        //ブーストの要素がnullだったらそのまま
        long account_id = 0;
        if (reblogToot && listItem.get(20) != null) {
            account_id = Long.valueOf(listItem.get(23));
        } else {
            account_id = Long.valueOf(listItem.get(6));
        }


        //ユーザー情報
        FragmentTransaction ft = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
        Fragment fragment = new User_Fragment();
        View finalConvertView = convertView;
        long finalAccount_id = account_id;
        thumbnail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //読み込み
                boolean multipain_ui_mode = pref_setting.getBoolean("app_multipain_ui", false);

                if (pref_setting.getBoolean("pref_quick_profile", false)) {
                    //クイックプロフィーる
                    quickProfileSnackber(v, String.valueOf(finalAccount_id));
                } else {
                    if (multipain_ui_mode) {

                        Bundle bundle = new Bundle();
                        bundle.putLong("Account_ID", finalAccount_id);
                        fragment.setArguments(bundle);

                        ft.replace(R.id.fragment3, fragment).commit();

                    } else {

                        Intent intent = new Intent(getContext(), UserActivity.class);
                        //IDを渡す
                        intent.putExtra("Account_ID", finalAccount_id);
                        getContext().startActivity(intent);
                    }
                }
            }
        });


        //ブックマーク関係
        boolean replace_snackber = pref_setting.getBoolean("pref_one_hand_mode", true);
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

        if (listItem.get(1) != null && listItem.get(1).contains("friends.nico アンケート")) {
            //System.out.println("アンケート発見 : " + String.valueOf(item.getID()));

            //!で条件を反転させる
            if (!listItem.get(1).contains("friends.nico アンケート(結果)")) {

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

/*
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
        };*/


        // トゥート
        TextView title = (TextView) holder.tile_textview;
        //title.setText(Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT));
        title.setTextSize(10);
        //フォントサイズの変更
        String toot_textsize = pref_setting.getString("pref_fontsize_timeline", "10");
        title.setTextSize(Integer.parseInt(toot_textsize));

        // ユーザー名
        TextView user = (TextView) holder.user_textview;
        //user.setText(item.getUser());
        user.setTextSize(10);
        //フォントサイズの変更
        String username_textsize = pref_setting.getString("pref_fontsize_user", "10");
        user.setTextSize(Integer.parseInt(username_textsize));

        //クライアント
        TextView client = (TextView) holder.client_textview;
        //client.setText(item.getClient());
        client.setTextSize(10);
        //フォントサイズ変更
        String client_textsize = pref_setting.getString("pref_fontsize_client", "10");
        client.setTextSize(Integer.parseInt(client_textsize));

        //各アイコンはトゥートサイズに合わせる
        String button_textsize = pref_setting.getString("pref_fontsize_button", "10");
        nicoru.setTextSize(Integer.parseInt(button_textsize));
        boost.setTextSize(Integer.parseInt(button_textsize));
        holder.bookmark_button.setTextSize(Integer.parseInt(button_textsize));
        web_button.setTextSize(Integer.parseInt(button_textsize));


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
        //ブースト　それ以外
        String titleString = null;
        String userString = null;
        //ブーストの要素がnullだったらそのまま
        if (reblogToot && listItem.get(20) != null) {
            titleString = listItem.get(20);
            userString = listItem.get(21) + "<br>" + listItem.get(2) + " " + getContext().getString(R.string.reblog);
            //アイコンつける
            Drawable drawable = getContext().getDrawable(R.drawable.ic_repeat_black_24dp_2);
            drawable.setTint(Color.parseColor("#008000"));
            user.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            //色つける
            user.setTextColor(Color.parseColor("#008000"));
        } else {
            titleString = listItem.get(1);
            userString = listItem.get(2);
        }

        /**
         * 内容を表示する部分
         * カスタム絵文字もほぼ動くように←これ重要
         * ちなみに最新の絵文字サポート機能は削りましたいる？
         *
         *
         * https://medium.com/@rajeefmk/android-textview-and-image-loading-from-url-part-1-a7457846abb6
         *
         * */

        PicassoImageGetter title_imageGetter = new PicassoImageGetter(title);
        PicassoImageGetter user_imageGetter = new PicassoImageGetter(user);
        Spannable toot_html;
        Spannable user_html;

        if (title != null) {

            if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                //カスタム絵文字有効時
                if (setting_avater_wifi) {
                    //WIFIのみ表示有効時
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        //WIFI接続中か確認
                        //接続中
                        try {
                            user_html = (Spannable) Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null);
                            toot_html = (Spannable) Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null);
                            title.setText(toot_html);
                            user.setText(user_html);
                        } catch (NullPointerException e) {
                            user_html = (Spannable) Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null);
                            toot_html = (Spannable) Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null);
                            title.setText(toot_html);
                            user.setText(user_html);
                        }
                    } else {
                        //確認したけどWIFI接続確認できなかった
                        title.setText(Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT));
                        user.setText(Html.fromHtml(userString, Html.FROM_HTML_MODE_COMPACT));
                    }
                } else {
                    //WIFIのみ表示無効時
                    //そのまま表示させる
                    try {
                        user_html = (Spannable) Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null);
                        toot_html = (Spannable) Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null);
                        title.setText(toot_html);
                        user.setText(user_html);
                    } catch (NullPointerException e) {
                        user_html = (Spannable) Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null);
                        toot_html = (Spannable) Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null);
                        title.setText(toot_html);
                        user.setText(user_html);
                    }
                }
            } else {
                title.setText(Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT));
                user.setText(Html.fromHtml(userString, Html.FROM_HTML_MODE_COMPACT));
            }
        }

        //強制的に表示
        if (emojis_show){
            try {
                user_html = (Spannable) Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null);
                toot_html = (Spannable) Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null);
                title.setText(toot_html);
                user.setText(user_html);
            } catch (NullPointerException e) {
                user_html = (Spannable) Html.fromHtml(userString, Html.FROM_HTML_MODE_LEGACY, user_imageGetter, null);
                toot_html = (Spannable) Html.fromHtml(titleString, Html.FROM_HTML_MODE_LEGACY, title_imageGetter, null);
                title.setText(toot_html);
                user.setText(user_html);
            }
        }


        //title.setText((Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT)));
        client.setText(listItem.get(3));


        //URLをCustomTabで開くかどうか
        if (chrome_custom_tabs) {
            holder.tile_textview.setTransformationMethod(new LinkTransformationMethod());
            holder.tile_textview.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.tile_textview.setAutoLinkMask(Linkify.WEB_URLS);
        }


        //アイコンオンリー
        boolean button_icon = pref_setting.getBoolean("pref_button_icon", false);
        if (button_icon) {
            boost_button.setText("");
            nicoru.setText("");
            web_button.setText("");
            holder.bookmark_button.setText("");
        }

        //ダークモード、OLEDモード時にアイコンが見えない問題
        //どちらかが有効の場合
        //↑これ廃止ね。代わりに利用中のテーマを取得して変更する仕様にするからよろー
        //Theme比較わからんから変わりにToolberの背景が黒だったら動くように
        //なんか落ちる（要検証）
        try {
            if (((ColorDrawable) ((Home) getContext()).getToolBer().getBackground()).getColor() == Color.parseColor("#000000")) {
                boost_button.setTextColor(Color.parseColor("#ffffff"));
                nicoru.setTextColor(Color.parseColor("#ffffff"));
                web_button.setTextColor(Color.parseColor("#ffffff"));
                holder.bookmark_button.setTextColor(Color.parseColor("#ffffff"));

                //アイコンを取得
                Drawable boost_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp, null);
                Drawable web_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_more_vert_black_24dp, null);
                Drawable bookmark_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_bookmark_border_black_24dp, null);
                Drawable favourite_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_black_24dp, null);

                //染色
                boost_icon_white.setTint(Color.parseColor("#ffffff"));
                web_icon_white.setTint(Color.parseColor("#ffffff"));
                bookmark_icon_white.setTint(Color.parseColor("#ffffff"));
                favourite_icon_white.setTint(Color.parseColor("#ffffff"));

                //入れる
                boost_button.setCompoundDrawablesWithIntrinsicBounds(boost_icon_white, null, null, null);
                web_button.setCompoundDrawablesWithIntrinsicBounds(web_icon_white, null, null, null);
                holder.bookmark_button.setCompoundDrawablesWithIntrinsicBounds(bookmark_icon_white, null, null, null);


                //ニコるをお気に入りに変更 設定次第
                //メッセージも変更できるようにする
                if (friends_nico_check_box) {
                    holder.nicoru_button.setCompoundDrawablesWithIntrinsicBounds(favourite_icon_white, null, null, null);
                }
            } else {
                //アイコンを取得
                Drawable boost_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp, null);
                Drawable web_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_more_vert_black_24dp, null);
                Drawable bookmark_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_bookmark_border_black_24dp, null);
                Drawable favourite_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_black_24dp, null);

                //染色
                boost_icon_white.setTint(Color.parseColor("#000000"));
                web_icon_white.setTint(Color.parseColor("#000000"));
                bookmark_icon_white.setTint(Color.parseColor("#000000"));
                favourite_icon_white.setTint(Color.parseColor("#000000"));

                //入れる
                boost_button.setCompoundDrawablesWithIntrinsicBounds(boost_icon_white, null, null, null);
                web_button.setCompoundDrawablesWithIntrinsicBounds(web_icon_white, null, null, null);
                holder.bookmark_button.setCompoundDrawablesWithIntrinsicBounds(bookmark_icon_white, null, null, null);
            }
        } catch (ClassCastException e) {
            //アイコンを取得
            Drawable boost_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp, null);
            Drawable web_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_more_vert_black_24dp, null);
            Drawable bookmark_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_bookmark_border_black_24dp, null);
            Drawable favourite_icon_white = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_black_24dp, null);

            //染色
            boost_icon_white.setTint(Color.parseColor("#000000"));
            web_icon_white.setTint(Color.parseColor("#000000"));
            bookmark_icon_white.setTint(Color.parseColor("#000000"));
            favourite_icon_white.setTint(Color.parseColor("#000000"));

            //入れる
            boost_button.setCompoundDrawablesWithIntrinsicBounds(boost_icon_white, null, null, null);
            web_button.setCompoundDrawablesWithIntrinsicBounds(web_icon_white, null, null, null);
            holder.bookmark_button.setCompoundDrawablesWithIntrinsicBounds(bookmark_icon_white, null, null, null);
        }

        //自分、ブーストいいですか？
        //とりあえず要素数で
        if (boostFavCount) {

            //もってくる
            String isBoost = item.getListItem().get(16);
            String isFav = item.getListItem().get(17);
            String boostCount = item.getListItem().get(18);
            String favCount = item.getListItem().get(19);
            //りぶろぐした・りぶろぐおしたとき
            if (isBoost.contains("reblogged") || boostClick[0]) {
                Drawable boostIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                boostIcon.setTint(Color.parseColor("#008000"));
                boost_button.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
            }
            //ふぁぼした、ふぁぼおした
            if (isFav.contains("favourited") || favClick[0]) {
                Drawable favIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                favIcon.setTint(Color.parseColor("#ffd700"));
                nicoru.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
            }

            boost_button.setText(boostCount);
            nicoru.setText(favCount);

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
            long finalAccount_id1 = account_id;
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
                                            customTabsIntent.launchUrl((Activity) getContext(), Uri.parse("https://" + Instance + "/" + "@" + listItem.get(7) + "/" + id_string));
                                            //無効
                                        } else {
                                            Uri uri = Uri.parse("https://" + Instance + "/" + "@" + listItem.get(7) + "/" + id_string);
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

                                                    String toot_sq = listItem.get(1);
                                                    String id_sq = listItem.get(4);

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

                                            String toot_sq = listItem.get(1);
                                            String id_sq = listItem.get(4);

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
                                            bundle.putLong("Account_ID", finalAccount_id1);
                                            fragment.setArguments(bundle);

                                            ft.replace(R.id.fragment3, fragment).commit();

                                        } else {

                                            Intent intent = new Intent(getContext(), UserActivity.class);
                                            //IDを渡す
                                            intent.putExtra("Account_ID", finalAccount_id1);
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

        TextView cardTextView;
        ImageView cardImageView;

        LinearLayout linearLayoutMediaButton;
        LinearLayout linearLayoutMedia;
        LinearLayout linearLayoutMedia2;
        LinearLayout linearLayoutEnquate;
        LinearLayout vw1;
        LinearLayout toot_linearLayout;
        LinearLayout button_linearLayout;
        LinearLayout avaterImageview_linearLayout;
        LinearLayout card_linearLayout;

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
                Bitmap bitmap_glide = Glide.with(getContext()).asBitmap().load(source).submit(100, 100).get();
                return bitmap_glide;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute drawable " + mDrawable);
            Log.d(TAG, "onPostExecute bitmap " + bitmap);
            if (bitmap != null) {
                BitmapDrawable d = new BitmapDrawable(getContext().getResources(), bitmap);
                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, 40, 40);
                mDrawable.setLevel(1);
                // i don't know yet a better way to refresh TextView
                // mTv.invalidate() doesn't work as expected
                CharSequence t = mTv.getText();
                mTv.setText(t);
                holder.tile_textview.invalidate();
                holder.tile_textview.postInvalidate();
                holder.tile_textview.refreshDrawableState();
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

    private void addMediaPicasso(String mediaURL, ImageView ImageView, LinearLayout linearLayout) {
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

    private void ImageViewSetting(ViewHolder holder) {
        //適当にサイズ
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
        ((LinearLayout.LayoutParams) layoutParams).weight = 1;
        holder.media_imageview_1.setLayoutParams(layoutParams);
        holder.media_imageview_2.setLayoutParams(layoutParams);
        holder.media_imageview_3.setLayoutParams(layoutParams);
        holder.media_imageview_4.setLayoutParams(layoutParams);
/*
        holder.media_imageview_1.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_2.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_3.setScaleType(ImageView.ScaleType.CENTER);
        holder.media_imageview_4.setScaleType(ImageView.ScaleType.CENTER);
*/

    }

    public void ImageViewClickCustomTab_LinearLayout(LinearLayout linearLayout, String mediaURL) {
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
                //カスタムタグ有効
                if (chrome_custom_tabs) {
                    String custom = CustomTabsHelper.getPackageNameToUse((getContext()));

                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage(custom);
                    customTabsIntent.launchUrl((Activity) getContext(), Uri.parse(mediaURL));

                    //無効
                } else {
                    Uri uri = Uri.parse(mediaURL);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    linearLayout.getContext().startActivity(intent);
                }
            }
        });
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

    public void TootAction(String id, String endPoint, TextView textView) {
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
                if (endPoint.contains("reblog")) {
                    Toast.makeText(getContext(), getContext().getString(R.string.boost_ok) + " : " + result, Toast.LENGTH_SHORT).show();
                    Drawable boostIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                    boostIcon.setTint(Color.parseColor("#008000"));
                    textView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
                }
                if (endPoint.contains("favourite")) {
                    Toast.makeText(getContext(), nicoru_text + result, Toast.LENGTH_SHORT).show();
                    Drawable favIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                    favIcon.setTint(Color.parseColor("#ffd700"));
                    textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                }
                if (endPoint.contains("unfavourite")) {
                    Toast.makeText(getContext(), getContext().getString(R.string.delete_fav_toast) + result, Toast.LENGTH_SHORT).show();
                    Drawable favIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                    favIcon.setTint(Color.parseColor("#000000"));
                    textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                }
                if (endPoint.contains("unreblog")) {
                    Toast.makeText(getContext(), getContext().getString(R.string.delete_bt_toast) + result, Toast.LENGTH_SHORT).show();
                    Drawable favIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                    favIcon.setTint(Color.parseColor("#000000"));
                    textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
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

                System.out.println("=====" + client.post("votes/" + id_string, requestBody));

                return id_string;
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(getContext(), ToastMessage + result, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void quickProfileSnackber(View v, String accountID) {
        //読み込み中お知らせ
        Snackbar snackbar = Snackbar.make(v, getContext().getString(R.string.loading_user_info) + "\r\n /api/v1/accounts/" + accountID, Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(getContext());
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar.show();

        //APIを叩く
        String url = "https://" + Instance + "/api/v1/accounts/" + accountID;
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
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String display_name = jsonObject.getString("display_name");
                    String username = jsonObject.getString("acct");
                    String profile_note = jsonObject.getString("note");
                    String avater_url = jsonObject.getString("avatar");
                    String follow = jsonObject.getString("following_count");
                    String follower = jsonObject.getString("followers_count");

                    //カスタム絵文字適用
                    if (emojis_show) {
                        //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
                        JSONArray emojis = jsonObject.getJSONArray("emojis");
                        for (int i = 0; i < emojis.length(); i++) {
                            JSONObject emojiObject = emojis.getJSONObject(i);
                            String emoji_name = emojiObject.getString("shortcode");
                            String emoji_url = emojiObject.getString("url");
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            //display_name
                            if (display_name.contains(emoji_name)) {
                                //あったよ
                                display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                            //note
                            if (profile_note.contains(emoji_name)) {
                                //あったよ
                                profile_note = profile_note.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                        }
                        JSONArray profile_emojis = jsonObject.getJSONArray("profile_emojis");
                        for (int i = 0; i < profile_emojis.length(); i++) {
                            JSONObject emojiObject = profile_emojis.getJSONObject(i);
                            String emoji_name = emojiObject.getString("shortcode");
                            String emoji_url = emojiObject.getString("url");
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            //display_name
                            if (display_name.contains(emoji_name)) {
                                //あったよ
                                display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                            //note
                            if (profile_note.contains(emoji_name)) {
                                //あったよ
                                profile_note = profile_note.replace(":" + emoji_name + ":", custom_emoji_src);
                            }
                        }
                    }


                    //フォローされているか（無駄にAPI叩いてね？）
                    final String[] follow_back = {getContext().getString(R.string.follow_back_not)};
                    String follow_url = "https://" + Instance + "/api/v1/accounts/relationships/?stream=user&access_token=" + AccessToken;

                    //パラメータを設定
                    HttpUrl.Builder builder = HttpUrl.parse(follow_url).newBuilder();
                    builder.addQueryParameter("id", String.valueOf(accountID));
                    String final_url = builder.build().toString();

                    //作成
                    Request request = new Request.Builder()
                            .url(final_url)
                            .get()
                            .build();

                    //GETリクエスト
                    OkHttpClient client = new OkHttpClient();
                    String finalProfile_note = profile_note;
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
                                boolean followed_by = jsonObject.getBoolean("followed_by");
                                if (followed_by) {
                                    follow_back[0] = getContext().getString(R.string.follow_back);
                                }


                                Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_SHORT);
                                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                                LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                progressBer_layoutParams.gravity = Gravity.CENTER;
                                //SnackBerを複数行対応させる
                                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
                                snackBer_textView.setMaxLines(Integer.MAX_VALUE);
                                //てきすと
                                //snackBer_textView.setText(Html.fromHtml(profile_note,Html.FROM_HTML_MODE_COMPACT));
                                //複数行対応させたおかげでずれたので修正
                                ImageView avater_ImageView = new ImageView(getContext());
                                avater_ImageView.setLayoutParams(progressBer_layoutParams);
                                //LinearLayout動的に生成
                                LinearLayout snackber_LinearLayout = new LinearLayout(getContext());
                                snackber_LinearLayout.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams warp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                snackber_LinearLayout.setLayoutParams(warp);
                                //そこにTextViewをいれる（もとからあるTextViewは無視）
                                TextView snackber_TextView = new TextView(getContext());
                                PicassoImageGetter imageGetter = new PicassoImageGetter(snackber_TextView);
                                snackber_TextView.setLayoutParams(warp);
                                snackber_TextView.setTextColor(Color.parseColor("#ffffff"));
                                snackber_TextView.setText(Html.fromHtml(finalProfile_note, Html.FROM_HTML_MODE_LEGACY, imageGetter, null));
                                //ボタン追加
                                Button userPage_Button = new Button(getContext(), null, 0, R.style.Widget_AppCompat_Button_Borderless);
                                userPage_Button.setLayoutParams(warp);
                                userPage_Button.setBackground(getContext().getDrawable(R.drawable.button_style));
                                userPage_Button.setTextColor(Color.parseColor("#ffffff"));
                                userPage_Button.setText(R.string.user);
                                Drawable boostIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_person_black_24dp, null);
                                boostIcon.setTint(Color.parseColor("#ffffff"));
                                userPage_Button.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
                                userPage_Button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), UserActivity.class);
                                        //IDを渡す
                                        intent.putExtra("Account_ID", Long.valueOf(accountID));
                                        getContext().startActivity(intent);
                                    }
                                });


                                //ふぉろー
                                TextView follow_TextView = new TextView(getContext());
                                follow_TextView.setTextColor(Color.parseColor("#ffffff"));
                                follow_TextView.setText(getContext().getString(R.string.follow) + " : \n" + follow);
                                Drawable done = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_done_black_24dp, null);
                                done.setTint(Color.parseColor("#ffffff"));
                                follow_TextView.setLayoutParams(warp);
                                follow_TextView.setCompoundDrawablesWithIntrinsicBounds(done, null, null, null);
                                //ふぉろわー
                                TextView follower_TextView = new TextView(getContext());
                                follower_TextView.setTextColor(Color.parseColor("#ffffff"));
                                follower_TextView.setText(getContext().getString(R.string.follower) + " : \n" + follower);
                                Drawable done_all = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_done_all_black_24dp, null);
                                done_all.setTint(Color.parseColor("#ffffff"));
                                follower_TextView.setLayoutParams(warp);
                                follower_TextView.setCompoundDrawablesWithIntrinsicBounds(done_all, null, null, null);

                                //ふぉろーされているか
                                TextView follow_info = new TextView(getContext());
                                follow_info.setTextColor(Color.parseColor("#ffffff"));
                                follow_info.setLayoutParams(warp);
                                Drawable follow_info_drawable = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_info_outline_black_24dp, null);
                                follow_info_drawable.setTint(Color.parseColor("#ffffff"));
                                follow_info.setCompoundDrawablesWithIntrinsicBounds(follow_info_drawable, null, null, null);
                                //日本語のときだけ改行する
                                StringBuilder stringBuilder = new StringBuilder(follow_back[0]);
                                if (!follow_back[0].contains("Following") && !follow_back[0].contains("not following")) {
                                    follow_info.setText(stringBuilder.insert(4, "\n"));
                                } else {
                                    follow_info.setText(follow_back[0]);
                                }


                                //ぷろが、ふぉろーふぉろわー、ふぉろーじょうたい、アカウントベージ移動、用LinearLayout
                                LinearLayout account_info_LinearLayout = new LinearLayout(getContext());
                                account_info_LinearLayout.setLayoutParams(warp);
                                account_info_LinearLayout.setOrientation(LinearLayout.VERTICAL);

                                //追加
                                account_info_LinearLayout.addView(avater_ImageView);
                                account_info_LinearLayout.addView(follow_info);
                                account_info_LinearLayout.addView(follow_TextView);
                                account_info_LinearLayout.addView(follower_TextView);
                                account_info_LinearLayout.addView(userPage_Button);

                                //LinearLayoutについか
                                snackber_LinearLayout.addView(snackber_TextView);

                                snackBer_viewGrop.addView(account_info_LinearLayout, 0);
                                snackBer_viewGrop.addView(snackber_LinearLayout, 1);
                                //Bitmap
                                try {
                                    Bitmap bitmap = Glide.with(getContext()).asBitmap().load(avater_url).submit(100, 100).get();
                                    avater_ImageView.setImageBitmap(bitmap);
                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                                snackbar.show();


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void SQLitePut(ListItem item) {
        if (sqLite == null) {
            sqLite = new TootBookmark_SQLite(getContext());
        }

        if (sqLiteDatabase == null) {
            sqLiteDatabase = sqLite.getWritableDatabase();
        }

        String toot_sq = item.getListItem().get(1);
        String id_sq = item.getListItem().get(4);
        String account = item.getListItem().get(2);
        String info = item.getListItem().get(3);
        String account_id = item.getListItem().get(6);
        String avater = item.getListItem().get(5);
        String account_id_string = item.getListItem().get(7);

        String media_1 = item.getListItem().get(8);
        String media_2 = item.getListItem().get(9);
        String media_3 = item.getListItem().get(10);
        String media_4 = item.getListItem().get(11);

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
/*
            animatable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    animatable.start();
                    super.onAnimationEnd(drawable);
                }
            });
*/
            holder.avaterImageview_linearLayout.addView(holder.notification_icon, 0);
        }
    }


}