package io.github.takusan23.kaisendon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import io.github.takusan23.kaisendon.CustomTabURL.LinkTransformationMethod;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleAdapter extends ArrayAdapter<ListItem> {

    //public static final long Account_ID = "com.takusan23.kaisendon.Account_ID";

    private int mResource;
    private List<io.github.takusan23.kaisendon.ListItem> mItems;
    private LayoutInflater mInflater;
    private int layoutId;
    private Set<Integer> visibleSet = new HashSet<Integer>();
    final android.os.Handler handler_1 = new android.os.Handler();
    private TootBookmark_SQLite sqLite;
    private SQLiteDatabase sqLiteDatabase;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;


    SharedPreferences pref = Preference_ApplicationContext.getContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);

    //settingのプリファレンスをとる
    SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

    String AccessToken = pref.getString("token", "");

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param resource リソースID
     * @param items    リストビューの要素
     */
    public SimpleAdapter(Context context, int resource, List<io.github.takusan23.kaisendon.ListItem> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = LayoutInflater.from(context);
        this.layoutId = layoutId;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        SimpleAdapter.ViewHolder viewHolder;
        viewHolder = new SimpleAdapter.ViewHolder();

        //Emoji
        EmojiCompat.Config config = new BundledEmojiCompatConfig(getContext());
        config.setReplaceAll(true);
        EmojiCompat.init(config);
        final EmojiCompat compat = EmojiCompat.get();


        SimpleAdapter.ViewHolder holder;


        //データの再利用を許さない！！！！！！！！！！！！！！！！！！！
        if (convertView == null) {

            view = mInflater.inflate(R.layout.timeline_item, parent, false);

            holder = new SimpleAdapter.ViewHolder();

            holder.linearLayout = (LinearLayout) view.findViewById(R.id.linearlayout_media);
            holder.vw1 = view.findViewById(R.id.vw1);
            holder.toot_linearLayout = view.findViewById(R.id.toot_linearlayout);
            holder.button_linearLayout = view.findViewById(R.id.button_layout);
            holder.avaterImageview_linearLayout = view.findViewById(R.id.avater_imageview_linearlayout);

            //添付メディア
            holder.linearLayoutMediaButton = view.findViewById(R.id.linearlayout_mediaButton);
            holder.linearLayoutMedia = view.findViewById(R.id.linearlayout_media);
            holder.linearLayoutMedia2 = view.findViewById(R.id.linearlayout_media2);
            holder.media_imageview_1 = new ImageView(holder.linearLayout.getContext());
            holder.media_imageview_2 = new ImageView(holder.linearLayout.getContext());
            holder.media_imageview_3 = new ImageView(holder.linearLayout.getContext());
            holder.media_imageview_4 = new ImageView(holder.linearLayout.getContext());
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
            holder = (SimpleAdapter.ViewHolder) view.getTag();
        }


        io.github.takusan23.kaisendon.ListItem item = mItems.get(position);
        ArrayList<String> listItem = item.getListItem();


        //URLをCustomTabで開くかどうか
        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
        if (chrome_custom_tabs) {
            holder.tile_textview.setTransformationMethod(new LinkTransformationMethod());
            holder.tile_textview.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.tile_textview.setAutoLinkMask(Linkify.WEB_URLS);
        }


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


//        TextView nicoru = holder.nicoru_button;
//        TextView boost = holder.boost_button;


        Handler handler = new Handler();

        View finalConvertView2 = view;

        //ボタンを消し飛ばす
        holder.button_linearLayout.removeView(holder.bookmark_button);
        holder.button_linearLayout.removeView(holder.web_button);
        holder.button_linearLayout.removeView(holder.boost_button);
        holder.button_linearLayout.removeView(holder.nicoru_button);


        String nicoru_text = null;

        //ニコる
        String finalNicoru_text = nicoru_text;
        String id_string = listItem.get(4);
        String avater_url = listItem.get(5);
        String media_url = "";


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


        //Wi-Fi接続状況確認
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());


        //タイムラインに画像を表示
        //動的に画像を追加するよ
        LinearLayout linearLayout = (LinearLayout) holder.linearLayout;
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
            if (toot_media) {
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

            //Wi-Fi接続時
            if (setting_avater_wifi) {
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


        //      サムネイル画像を設定
        ImageView thumbnail = (ImageView) holder.avater_imageview;
        //通信量節約
        boolean setting_avater_hidden = pref_setting.getBoolean("pref_avater", false);
        //avatarがnullかどうか
        if (avater_url != null) {
            //じゃんけんあいこん と　TootShortcut以外
            if (!avater_url.contains("じゃんけん") || !avater_url.contains("toot_shortcut")) {
                if (setting_avater_hidden) {
                    //thumbnail.setImageBitmap(item.getThumbnail());
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
                        holder.vw1.removeView(holder.avaterImageview_linearLayout);
                    }

                } else {
                    //レイアウトを消す
                    holder.vw1.removeView(holder.avaterImageview_linearLayout);
                }
            } else {
                String imageString = null;
                //画像選択
                if (avater_url.contains("勝ちました")) {
                    Glide.with(view)
                            .load(R.drawable.ic_thumb_up_black_24dp)
                            .into(thumbnail);
                }
                if (avater_url.contains("負けました")) {
                    Glide.with(view)
                            .load(R.drawable.ic_thumb_down_black_24dp)
                            .into(thumbnail);
                }
                if (avater_url.contains("あいこだぜ")) {
                    Glide.with(view)
                            .load(R.drawable.ic_thumbs_up_down_black_24dp)
                            .into(thumbnail);
                }
                if (avater_url.contains("えらー")) {
                    Glide.with(view)
                            .load(R.drawable.ic_sync_problem_black_24dp)
                            .into(thumbnail);
                }
                if (avater_url.contains("おわり")) {
                    Glide.with(view)
                            .load(R.drawable.ic_local_hotel_black_18dp)
                            .into(thumbnail);
                }
            }

            //TootShortcut用
            if (avater_url.contains("toot_shortcut")) {
                String icon_text = avater_url.replace("toot_shortcut ","");
                    Glide.with(view)
                            .load(stringToDrawable(icon_text))
                            .into(thumbnail);

            }


        }


        long account_id = Long.valueOf(listItem.get(6));


        //ユーザー情報
        FragmentTransaction ft = ((FragmentActivity) parent.getContext()).getSupportFragmentManager().beginTransaction();
        Fragment fragment = new User_Fragment();
        View finalConvertView = convertView;
        //TootShortcutのときに呼ばれないようにする
        if (!avater_url.contains("toot_shortcut")) {
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
        }




        //カスタムストリーミングで背景色を変える機能
        String type = listItem.get(0);
        if (type != null) {
            if (type.contains("now_account")) {
                holder.vw1.setBackgroundColor(Color.parseColor("#1A008080"));
            }
        }


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
        client.setTextSize(10);
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
        String titleString = listItem.get(1);
        String userString = listItem.get(2);
        if (emoji_compatibility) {
            //ユーザー名
            EmojiCompat.get().registerInitCallback(new EmojiCompat.InitCallback() {
                @Override
                public void onInitialized() {
                    if (user != null) {
                        user.setText(
                                compat.process(userString));
                    }
                }
            });
            //本文
            EmojiCompat.get().registerInitCallback(new EmojiCompat.InitCallback() {
                @Override
                public void onInitialized() {
                    if (title != null) {
                        title.setText(
                                compat.process((Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT))));
                    }
                }
            });
            //クライアント
            EmojiCompat.get().registerInitCallback(new EmojiCompat.InitCallback() {
                @Override
                public void onInitialized() {
                    if (client != null) {
                        client.setText(
                                compat.process(listItem.get(4)));
                    }
                }
            });

        } else {
            //無効時
            user.setText(userString);
            title.setText(Html.fromHtml(titleString, Html.FROM_HTML_MODE_COMPACT));
            client.setText(listItem.get(4));
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


        LinearLayout linearLayout;
        LinearLayout vw1;
        LinearLayout toot_linearLayout;
        LinearLayout button_linearLayout;
        LinearLayout linearLayoutMedia;
        LinearLayout linearLayoutMedia2;
        LinearLayout linearLayoutMediaButton;
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

    public void ImageViewSetting(SimpleAdapter.ViewHolder holder) {
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
                Bitmap back_icon = BitmapFactory.decodeResource(getContext().getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);
                String custom = CustomTabsHelper.getPackageNameToUse(getContext());

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.setPackage(custom);
                customTabsIntent.launchUrl(getContext(), Uri.parse(mediaURL));
            }
        });
    }


    //String→Drawable
    //Wear TootShortcut Setting 用
    private Drawable stringToDrawable(String icon) {
        Drawable drawable = getContext().getDrawable(R.drawable.ic_public_black_24dp);
        switch (icon) {
            case "public":
                drawable = getContext().getDrawable(R.drawable.ic_public_black_24dp);
                break;
            case "unlisted":
                drawable = getContext().getDrawable(R.drawable.ic_done_all_black_24dp_2);
                break;
            case "private":
                drawable = getContext().getDrawable(R.drawable.ic_lock_open_black_24dp);
                break;
            case "direct":
                drawable = getContext().getDrawable(R.drawable.ic_assignment_ind_black_24dp);
                break;
        }
        return drawable;
    }


}
