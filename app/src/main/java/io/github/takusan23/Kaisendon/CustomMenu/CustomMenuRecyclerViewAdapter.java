package io.github.takusan23.Kaisendon.CustomMenu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import io.github.takusan23.Kaisendon.APIJSONParse.ActivityPubJSONParse;
import io.github.takusan23.Kaisendon.APIJSONParse.CustomMenuJSONParse;
import io.github.takusan23.Kaisendon.APIJSONParse.MastodonAccountJSONParse;
import io.github.takusan23.Kaisendon.APIJSONParse.MastodonScheduledStatusesJSONParse;
import io.github.takusan23.Kaisendon.APIJSONParse.MastodonTLAPIJSONParse;
import io.github.takusan23.Kaisendon.Activity.UserActivity;
import io.github.takusan23.Kaisendon.CustomMenu.Dialog.TootOptionBottomDialog;
import io.github.takusan23.Kaisendon.DarkMode.DarkModeSupport;
import io.github.takusan23.Kaisendon.DesktopTL.DesktopFragment;
import io.github.takusan23.Kaisendon.Home;
import io.github.takusan23.Kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.Kaisendon.PicassoImageGetter;
import io.github.takusan23.Kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustomMenuRecyclerViewAdapter extends RecyclerView.Adapter<CustomMenuRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ArrayList> itemList;
    private SharedPreferences pref_setting;
    private String Instance;
    private String AccessToken;
    private Context context;
    private boolean isScheduled_statuses = false;
    private boolean isFollowSuggestions = false;
    private SimpleDateFormat simpleDateFormat;
    private SimpleDateFormat japanDateFormat;
    private Calendar calendar;
    private boolean isMastodonStatus = false;
    private boolean isMastodonFollowes = false;
    private boolean isMisskeyNotes = false;
    private boolean isMisskeyFollowes = false;
    private boolean isMisskeyMode = false;
    private boolean isActivityPubViewer = false;
    private ArrayList<String> type_face_String;
    private ArrayList<Typeface> type_face_list;
    //アイコンの配列
    private ArrayList<String> no_fav_icon_String;
    private ArrayList<Drawable> no_fav_icon_list;
    private ArrayList<String> yes_fav_icon_String;
    private ArrayList<Drawable> yes_fav_icon_list;


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView toot_text_TextView;
        public TextView toot_user_TextView;
        public LinearLayout account_LinearLayout;
        public ImageView toot_avatar_ImageView;
        public TextView toot_boost_TextView;
        public TextView toot_favourite_TextView;
        public TextView toot_bookmark_TextView;
        public TextView toot_client_TextView;
        public TextView toot_createAt_TextView;
        public TextView toot_visibility_TextView;
        public LinearLayout toot_media_LinearLayout;
        public LinearLayout mainLinearLayout;
        public LinearLayout action_LinearLayout;
        public ImageView date_icon_ImageView;
        public ImageView client_icon_ImageView;
        public ImageView visibility_icon_ImageView;
        public ImageButton spoiler_text_ImageButton;
        //画像
        public ImageView media_ImageView_1;
        public ImageView media_ImageView_2;
        public ImageView media_ImageView_3;
        public ImageView media_ImageView_4;
        public ImageButton show_ImageButton;
        public LinearLayout option_LinearLayout;
        //card
        public LinearLayout toot_card_LinearLayout;
        public TextView card_TextView;
        public ImageView card_ImageView;
        //ReBlog
        public LinearLayout toot_reblog_LinearLayout;
        public ImageView reblog_avatar_ImageView;
        public TextView reblog_user_TextView;
        public TextView reblog_toot_text_TextView;
        //Notification
        public TextView notification_type_TextView;
        //Reaction
        public TextView reaction_TextView;
        //spoiler_text
        public Button spoiler_text_Button;
        //vote
        public LinearLayout vote_LinearLayout;
        public Button vote_1;
        public Button vote_2;
        public Button vote_3;
        public Button vote_4;
        public TextView vote_time;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            mainLinearLayout = itemView.findViewById(R.id.custom_menu_adapter_mainLinearLayout);
            account_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_account_linearlayout);
            toot_text_TextView = itemView.findViewById(R.id.custom_menu_adapter_text);
            toot_user_TextView = itemView.findViewById(R.id.custom_menu_adapter_account);
            toot_avatar_ImageView = itemView.findViewById(R.id.custom_menu_adapter_main_avatar);
            toot_boost_TextView = itemView.findViewById(R.id.custom_menu_adapter_boost);
            toot_favourite_TextView = itemView.findViewById(R.id.custom_menu_adapter_favourite);
            toot_bookmark_TextView = itemView.findViewById(R.id.custom_menu_adapter_bookmark);
            toot_client_TextView = itemView.findViewById(R.id.custom_menu_adapter_via);
            toot_createAt_TextView = itemView.findViewById(R.id.custom_menu_adapter_createAt);
            toot_visibility_TextView = itemView.findViewById(R.id.custom_menu_adapter_visibility);
            toot_media_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_mediaLinearLayout);
            action_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_notification_layout);
            date_icon_ImageView = itemView.findViewById(R.id.date_icon_imageview);
            visibility_icon_ImageView = itemView.findViewById(R.id.visibility_icon_imageview);
            client_icon_ImageView = itemView.findViewById(R.id.client_icon_imageview);
            spoiler_text_ImageButton = new ImageButton(context);
            media_ImageView_1 = new ImageView(context);
            media_ImageView_2 = new ImageView(context);
            media_ImageView_3 = new ImageView(context);
            media_ImageView_4 = new ImageView(context);
            show_ImageButton = new ImageButton(context);
            option_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_optionLinearLayout);
            toot_card_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_cardLinearLayout);
            card_TextView = itemView.findViewById(R.id.custom_menu_adapter_card_textView);
            card_ImageView = itemView.findViewById(R.id.custom_menu_adapter_card_imageView);
            toot_reblog_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_reblogLinearLayout);
            reblog_avatar_ImageView = itemView.findViewById(R.id.custom_menu_adapter_reblog_avatar);
            reblog_user_TextView = itemView.findViewById(R.id.custom_menu_adapter_reblog_account);
            reblog_toot_text_TextView = itemView.findViewById(R.id.custom_menu_adapter_reblog_text);
            notification_type_TextView = new TextView(context);
            reaction_TextView = new TextView(context);
            spoiler_text_Button = new Button(context);
            vote_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_voteLinearLayout);
            vote_1 = new Button(context);
            vote_2 = new Button(context);
            vote_3 = new Button(context);
            vote_4 = new Button(context);
            vote_time = new TextView(context);
        }
    }

    public CustomMenuRecyclerViewAdapter(ArrayList<ArrayList> arrayList) {
        itemList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_menu_recyclerview_adapter_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view, viewGroup.getContext());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(viewHolder.toot_text_TextView.getContext());
        //Context
        context = viewHolder.toot_text_TextView.getContext();

        ArrayList<String> item = itemList.get(i);
        AccessToken = item.get(8);
        Instance = item.get(7);
        //設定を読むやつ
        CustomMenuJSONParse setting = new CustomMenuJSONParse(item.get(9));

        //パースする種類
        if (item.get(1).contains("/api/v1/scheduled_statuses")) {
            isScheduled_statuses = true;
        }
        if (item.get(1).contains("/api/v1/suggestions")) {
            isFollowSuggestions = true;
        }
        if (item.get(1).contains("/api/v1/accounts/")) {
            if (item.get(1).contains("/following") || item.get(1).contains("/followers")) {
                isMastodonFollowes = true;
            }
            if (item.get(1).contains("/statuses")) {
                isMastodonStatus = true;
            }
        }
        if (item.get(1).contains("/api/users/following") || item.get(1).contains("/api/users/followers")) {
            isMisskeyFollowes = true;
        }
        if (item.get(1).contains("/api/users/notes")) {
            isMisskeyNotes = true;
        }
        if (item.get(0).contains("ActivityPub")) {
            isActivityPubViewer = true;
        }
        //デスクトップモードかPiP利用時は簡略表示する
        Fragment fragment = ((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.container_container);
        Fragment pip_Fragment = ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("pip_fragment");
        if (fragment instanceof DesktopFragment) {
            MastodonTLAPIJSONParse api = new MastodonTLAPIJSONParse(viewHolder.toot_text_TextView.getContext(), item.get(3), setting);
            setAccountLayout(viewHolder);
            setDesktopTootOption(viewHolder, api, item);
        }
        if (pip_Fragment != null) {
            setPiPLayout(viewHolder);
        }

        //TL/それ以外
        if (!isScheduled_statuses && !isFollowSuggestions && !isMisskeyFollowes && !isMastodonFollowes && !isActivityPubViewer) {
            //レイアウト
            //JSONパース用クラス
            //System.out.println(item.get(3));
            MastodonTLAPIJSONParse api = new MastodonTLAPIJSONParse(viewHolder.toot_text_TextView.getContext(), item.get(3), setting);
            //カスタム絵文字
            PicassoImageGetter toot_ImageGetter = new PicassoImageGetter(viewHolder.toot_text_TextView);
            PicassoImageGetter user_ImageGetter = new PicassoImageGetter(viewHolder.toot_user_TextView);
            //色を変える機能
            String text = api.getToot_text();
            if (context instanceof Home) {
                String highlight = ((Home) context).getTlQuickSettingSnackber().getHighlightText();
                if (!highlight.equals("")) {
                    text = text.replace(highlight, "<font color=\"red\">" + highlight + "</font>");
                }
            }
            //SetText
            viewHolder.toot_text_TextView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT, toot_ImageGetter, null));
            viewHolder.toot_user_TextView.setText(Html.fromHtml(api.getDisplay_name(), Html.FROM_HTML_MODE_COMPACT, user_ImageGetter, null));
            viewHolder.toot_user_TextView.append("@" + api.getAcct());
            viewHolder.toot_createAt_TextView.setText(getCreatedAtFormat(api.getCreatedAt()));
            viewHolder.toot_client_TextView.setText(api.getClient());
            viewHolder.toot_visibility_TextView.setText(api.getVisibility());
            //IDを配列に入れておく
            item.set(2, api.getToot_ID());
            //Misskey
            if (item.get(6).contains("Misskey")) {
                //アバター画像
                loadAvatarImage(api, viewHolder, setting);
                //Misskeyリアクション
                setMisskeyReaction(viewHolder, api, item, setting);
                //Renote
                setRenote(viewHolder, api, item);
                //Fav、BT済み、カウント数を出す
                setCountAndIconColor(viewHolder, api, item, setting);
                //添付メディア
                showMedia(viewHolder, api, setting, item);
                //card
                setCard(viewHolder, api, setting);
                //ブースト
                setReBlogToot(viewHolder, api, item, setting);
                //クイックプロフィール
                showMisskeyQuickProfile(viewHolder.account_LinearLayout, api.getUser_ID(), item, setting);
                //通知タイプ
                showNotificationType(viewHolder, api);
                //クライアント名のTextViewを消す
                setClientTextViewRemove(viewHolder);
                //カスタムフォント
                setTypeFace(setting);
                setCustomFont(viewHolder, setting);
                //ボタン
                showTootOption(viewHolder, api, item);
                //透明度
                setTransparency(viewHolder, setting);
                //フォント
                setFontSetting(viewHolder);
                //ダークモード
                setThemeIconColor(viewHolder, api);
                //警告文
                setContentWarning(viewHolder, api, item);
            } else {
                //アバター画像
                loadAvatarImage(api, viewHolder, setting);
                //BT、Favできるようにする
                setStatusClick(viewHolder.toot_boost_TextView, "bt_only", api, item, setting);
                setStatusClick(viewHolder.toot_favourite_TextView, "fav_only", api, item, setting);
                //Fav+BTできるように
                setPostBtFav(viewHolder, api, item, setting);
                //Fav、BT済み、カウント数を出す
                setCountAndIconColor(viewHolder, api, item, setting);
                //添付メディア
                showMedia(viewHolder, api, setting, item);
                //card
                setCard(viewHolder, api, setting);
                //ブースト
                setReBlogToot(viewHolder, api, item, setting);
                //通知タイプ
                showNotificationType(viewHolder, api);
                //クイックプロフィール
                showQuickProfile(viewHolder.account_LinearLayout, api.getUser_ID(), viewHolder, item, setting);
                //クライアント名のTextViewを消す
                setClientTextViewRemove(viewHolder);
                //カスタムフォント
                setTypeFace(setting);
                setCustomFont(viewHolder, setting);
                //隠す
                setSpoiler_text(viewHolder, api);
                //ボタン
                showTootOption(viewHolder, api, item);
                //投票
                showVoteLayout(viewHolder, api);
                //透明度
                setTransparency(viewHolder, setting);
                //フォント
                setFontSetting(viewHolder);
                //ふぁぼぼたんのかすたまいず
                //setCustomizeFavIcon(viewHolder,api, setting);
                //ダークモード
                setThemeIconColor(viewHolder, api);
                //警告文
                setContentWarning(viewHolder, api, item);
            }
        } else if (isScheduled_statuses) {
            MastodonScheduledStatusesJSONParse api = new MastodonScheduledStatusesJSONParse(item.get(3));
            //時間指定投稿（予約投稿）ようレイアウト
            setSimpleLayout(viewHolder);
            setScheduled_statuses_layout(viewHolder, api);
            //ダークモード
            setThemeIconColor(viewHolder, null);
        } else if (isFollowSuggestions) {
            MastodonAccountJSONParse api = new MastodonAccountJSONParse(viewHolder.mainLinearLayout.getContext(), item.get(3));
            setAccountLayout(viewHolder);
            createAccountLinearLayout(viewHolder, api, item, setting);
            //ダークモード
            setThemeIconColor(viewHolder, null);
        } else if (isMastodonFollowes || isMisskeyFollowes) {
            MastodonAccountJSONParse api = new MastodonAccountJSONParse(viewHolder.mainLinearLayout.getContext(), item.get(3));
            setAccountLayout(viewHolder);
            createAccountLinearLayout(viewHolder, api, item, setting);
            //ダークモード
            setThemeIconColor(viewHolder, null);
        } else if (isActivityPubViewer) {
            ActivityPubJSONParse api = new ActivityPubJSONParse(item.get(3));
            setSimpleLayout(viewHolder);
            viewHolder.toot_text_TextView.setText(Html.fromHtml(api.getContext(), Html.FROM_HTML_MODE_COMPACT));
            viewHolder.toot_user_TextView.setTextSize(18);
            //ダークモード
            setThemeIconColor(viewHolder, null);
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * 画像を読み込むかどうか（Wi-Fi接続時のみとか
     */
    private boolean getLoadImageConnection(ViewHolder viewHolder, CustomMenuJSONParse setting) {
        boolean mode = false;
        //画像
        ConnectivityManager connectivityManager = (ConnectivityManager) viewHolder.toot_text_TextView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (pref_setting.getBoolean("pref_avater_wifi", true)) {
            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    mode = true;
                }
            }
        } else {
            mode = true;
        }
        //強制表示モード
        if (Boolean.valueOf(setting.getImage_load())) {
            mode = true;
        }

        return mode;
    }


    /**
     * 画像表示とか
     */
    private void loadAvatarImage(MastodonTLAPIJSONParse api, ViewHolder viewHolder, CustomMenuJSONParse setting) {
        //画像
        if (getLoadImageConnection(viewHolder, setting)) {
            //既定でGIFは再生しない方向で
            if (pref_setting.getBoolean("pref_avater_gif", true)) {
                //GIFアニメ再生させない
                Glide.with(viewHolder.toot_avatar_ImageView.getContext()).load(api.getAvatarUrlNotGIF()).into(viewHolder.toot_avatar_ImageView);
            } else {
                //GIFアニメを再生
                Glide.with(viewHolder.toot_avatar_ImageView.getContext()).load(api.getAvatarUrl()).into(viewHolder.toot_avatar_ImageView);
            }
        } else {
            //Layout Remove
            if (((LinearLayout) viewHolder.toot_avatar_ImageView.getParent()) != null) {
                ((LinearLayout) viewHolder.toot_avatar_ImageView.getParent()).removeView(viewHolder.toot_avatar_ImageView);
            }
        }
    }

    /**
     * Account Layout 画像表示
     */
    private void loadAccountLayoutAvatarImage(MastodonAccountJSONParse api, ViewHolder viewHolder, CustomMenuJSONParse setting) {
        //画像
        if (getLoadImageConnection(viewHolder, setting)) {
            //既定でGIFは再生しない方向で
            if (pref_setting.getBoolean("pref_avater_gif", true)) {
                //GIFアニメ再生させない
                Glide.with(viewHolder.toot_avatar_ImageView.getContext()).load(api.getAvatar_url()).into(viewHolder.toot_avatar_ImageView);
            } else {
                //GIFアニメを再生
                Glide.with(viewHolder.toot_avatar_ImageView.getContext()).load(api.getAvatar_url()).into(viewHolder.toot_avatar_ImageView);
            }
        } else {
            //Layout Remove
            if (((LinearLayout) viewHolder.toot_avatar_ImageView.getParent()) != null) {
                ((LinearLayout) viewHolder.toot_avatar_ImageView.getParent()).removeView(viewHolder.toot_avatar_ImageView);
            }
        }
    }

    /**
     * Boost,Favourite
     *
     * @param type favかbtか
     */
    private void setStatusClick(TextView textView, String type, MastodonTLAPIJSONParse api, ArrayList<String> item, CustomMenuJSONParse setting) {
        //クリックイベント
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackberのテキスト
                String message = "";
                String button = "";
                String apiUrl = "favourite";
                //Fav/BT
                if (type.equals("fav_only")) {
                    //Fav済みか
                    if (Boolean.valueOf(api.getIsFav()) || item.get(5).contains("true")) {
                        //Fav済み
                        message = context.getString(R.string.delete_fav);
                        button = context.getString(R.string.delete_ok);
                        apiUrl = "unfavourite";
                    } else {
                        //Favする
                        message = context.getString(R.string.favourite_add_message);
                        button = context.getString(R.string.favoutire);
                        apiUrl = "favourite";
                    }
                } else {
                    //Fav済みか
                    if (Boolean.valueOf(api.getIsBT()) || item.get(4).contains("true")) {
                        //BT済み
                        message = context.getString(R.string.delete_bt);
                        button = context.getString(R.string.delete_text);
                        apiUrl = "unreblog";
                    } else {
                        //BTする
                        message = context.getString(R.string.dialog_boost_info);
                        button = context.getString(R.string.dialog_boost);
                        apiUrl = "reblog";
                    }
                }
                //SnackBer生成
                String finalApiUrl = apiUrl;
                String finalButton = button;
                //ダイアログ非表示
                if (Boolean.valueOf(setting.getDialog())) {
                    //実行
                    TootAction(api.getToot_ID(), finalApiUrl, textView, api, item, setting);
                } else {
                    Snackbar.make(v, message, Snackbar.LENGTH_LONG).setAction(button, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //実行
                            TootAction(api.getToot_ID(), finalApiUrl, textView, api, item, setting);
                        }
                    }).show();
                }

            }
        });
    }

    /**
     * Favourite and Boost
     */
    private void setPostBtFav(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item, CustomMenuJSONParse setting) {
        //Favourite+Boost
        viewHolder.toot_favourite_TextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Snackberのテキスト
                String message = "";
                String button = "";
                String apiUrl = "favourite";
                message = context.getString(R.string.favAndBT);
                button = "Fab+BT";
                //SnackBer生成
                String finalApiUrl = apiUrl;
                String finalButton = button;
                if (Boolean.valueOf(setting.getDialog())) {
                    //実行
                    //Fab+BTモード以外
                    TootAction(api.getToot_ID(), "favourite", viewHolder.toot_favourite_TextView, api, item, setting);
                    TootAction(api.getToot_ID(), "reblog", viewHolder.toot_favourite_TextView, api, item, setting);
                } else {
                    Snackbar.make(v, message, Snackbar.LENGTH_LONG).setAction(button, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //実行
                            //Fab+BTモード以外
                            TootAction(api.getToot_ID(), "favourite", viewHolder.toot_favourite_TextView, api, item, setting);
                            TootAction(api.getToot_ID(), "reblog", viewHolder.toot_favourite_TextView, api, item, setting);
                        }
                    }).show();
                }
                //OnClickListener呼ばれないようにする
                return true;
            }
        });
    }

    /**
     * Fav、BT済み、カウント数を入れる
     */
    private void setCountAndIconColor(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item, CustomMenuJSONParse setting) {
        viewHolder.toot_boost_TextView.setText(api.getBTCount());
        viewHolder.toot_favourite_TextView.setText(api.getFavCount());
        Drawable boostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_repeat_black_24dp, null);
        Drawable favIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_star_black_24dp, null);
        if (Boolean.valueOf(setting.getDark_mode())) {
            boostIcon.setTint(Color.parseColor("#ffffff"));
            favIcon.setTint(Color.parseColor("#ffffff"));
            viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
            viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
            //Toot詳細も白アイコン
            viewHolder.date_icon_ImageView.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            viewHolder.visibility_icon_ImageView.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            viewHolder.client_icon_ImageView.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
        }
        //りぶろぐした、もしくは押した
        if (api.getIsBT().contains("true") || item.get(4).contains("true")) {
            Drawable isBoostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_repeat_black_24dp_1, null);
            isBoostIcon.setTint(Color.parseColor("#008000"));
            viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(isBoostIcon, null, null, null);
        } else {
            viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
        }
        //ふぁぼ
        //Mastodonでは使わない
        if (item.get(6).contains("Mastodon")) {
            //ふぁぼした、もしくはふぁぼ押した
            if (api.getIsFav().contains("true") || item.get(5).contains("true")) {
                Drawable isFavIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_star_black_24dp_1, null);
                isFavIcon.setTint(Color.parseColor("#ffd700"));
                viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(isFavIcon, null, null, null);
            } else {
                viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
            }
        } else {
            viewHolder.toot_favourite_TextView.setText(HomeTimeLineAdapter.toReactionEmoji(api.getIsFav()));
            viewHolder.mainLinearLayout.removeView(viewHolder.reaction_TextView);
            viewHolder.reaction_TextView.setPadding(viewHolder.toot_text_TextView.getPaddingLeft(), 0, viewHolder.toot_text_TextView.getPaddingRight(), 0);
            viewHolder.reaction_TextView.setText(api.getFavCount());
            viewHolder.reaction_TextView.setTextSize(10);
            viewHolder.mainLinearLayout.addView(viewHolder.reaction_TextView, 2);
        }


    }

    /**
     * 画像表示
     */
    private void showMedia(ViewHolder viewHolder, MastodonTLAPIJSONParse api, CustomMenuJSONParse setting, ArrayList<String> item) {
        //消す
        viewHolder.toot_media_LinearLayout.removeAllViews();
        viewHolder.option_LinearLayout.removeView(viewHolder.show_ImageButton);
        //画像を表示してもよいか？
        if (getLoadImageConnection(viewHolder, setting)) {
            if (api.getMediaList().size() == 0) {
                //配列の要素０のときも消す
                viewHolder.toot_media_LinearLayout.removeAllViews();
            }
            if (api.getMediaList().size() >= 1) {
                setGlide(viewHolder.media_ImageView_1, viewHolder, api.getMediaList().get(0));
            }
            if (api.getMediaList().size() >= 2) {
                setGlide(viewHolder.media_ImageView_2, viewHolder, api.getMediaList().get(1));
            }
            if (api.getMediaList().size() >= 3) {
                setGlide(viewHolder.media_ImageView_3, viewHolder, api.getMediaList().get(2));
            }
            if (api.getMediaList().size() >= 4) {
                setGlide(viewHolder.media_ImageView_4, viewHolder, api.getMediaList().get(3));
            }
        } else {
            //表示ボタン
            if (api.getMediaList().size() != 0) {
                //表示済み
                if (Boolean.valueOf(item.get(10))) {
                    if (api.getMediaList().size() >= 1) {
                        setGlide(viewHolder.media_ImageView_1, viewHolder, api.getMediaList().get(0));
                    }
                    if (api.getMediaList().size() >= 2) {
                        setGlide(viewHolder.media_ImageView_2, viewHolder, api.getMediaList().get(1));
                    }
                    if (api.getMediaList().size() >= 3) {
                        setGlide(viewHolder.media_ImageView_3, viewHolder, api.getMediaList().get(2));
                    }
                    if (api.getMediaList().size() >= 4) {
                        setGlide(viewHolder.media_ImageView_4, viewHolder, api.getMediaList().get(3));
                    }
                } else {
                    //配列の要素０のときは使わない
                    viewHolder.option_LinearLayout.addView(viewHolder.show_ImageButton);
                    viewHolder.show_ImageButton.setBackgroundColor(Color.parseColor("#00000000"));
                    viewHolder.show_ImageButton.setPadding(10, 10, 10, 10);
                    viewHolder.show_ImageButton.setImageDrawable(context.getDrawable(R.drawable.ic_image_black_24dp));
                    viewHolder.show_ImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //次から読み込めるようにtrueを入れとく
                            item.set(10, "true");
                            viewHolder.option_LinearLayout.removeView(viewHolder.show_ImageButton);
                            if (api.getMediaList().size() >= 1) {
                                setGlide(viewHolder.media_ImageView_1, viewHolder, api.getMediaList().get(0));
                            }
                            if (api.getMediaList().size() >= 2) {
                                setGlide(viewHolder.media_ImageView_2, viewHolder, api.getMediaList().get(1));
                            }
                            if (api.getMediaList().size() >= 3) {
                                setGlide(viewHolder.media_ImageView_3, viewHolder, api.getMediaList().get(2));
                            }
                            if (api.getMediaList().size() >= 4) {
                                setGlide(viewHolder.media_ImageView_4, viewHolder, api.getMediaList().get(3));
                            }
                        }
                    });
                }

            }
        }
    }

    /**
     * Glide
     */
    private void setGlide(ImageView imageView, ViewHolder viewHolder, String url) {
        imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        Glide.with(imageView.getContext()).load(url).into(imageView);
        viewHolder.toot_media_LinearLayout.addView(imageView);
        //クリックイベント
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useCustomTabs(url);
            }
        });
    }

    /**
     * CustomTab
     */
    private void useCustomTabs(String url) {
        boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);
        //カスタムタグ有効
        if (chrome_custom_tabs) {
            Bitmap back_icon = BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_action_arrow_back);
            String custom = CustomTabsHelper.getPackageNameToUse(context);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage(custom);
            customTabsIntent.launchUrl(context, Uri.parse(url));
            //無効
        } else {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    /**
     * クライアント名の欄を消す
     */
    private void setClientTextViewRemove(ViewHolder viewHolder) {
        //空だったら消す
        if (viewHolder.toot_client_TextView.getText().toString().equals("")) {
            //TextViewとImageViewがあるLinearLayout特定
            LinearLayout linearLayout = ((LinearLayout) viewHolder.toot_client_TextView.getParent());
            //上のLinearLayoutがあるレイアウトを特定
            LinearLayout parent_LinearLayout = ((LinearLayout) linearLayout.getParent());
            //消す
            if (parent_LinearLayout != null) {
                parent_LinearLayout.removeView(linearLayout);
            }
        }
    }

    /**
     * Card実装する
     */
    private void setCard(ViewHolder viewHolder, MastodonTLAPIJSONParse api, CustomMenuJSONParse setting) {
        viewHolder.toot_card_LinearLayout.removeAllViews();
        if (api.getCardTitle() != null) {
            //getLayoutInflaterが使えないので
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //Cardのレイアウト適用
            layoutInflater.inflate(R.layout.custom_menu_recycler_adapter_card_layout, viewHolder.toot_card_LinearLayout);
            viewHolder.card_ImageView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_card_imageView);
            viewHolder.card_TextView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_card_textView);
            if (getLoadImageConnection(viewHolder, setting) && api.getCardImage() != null && viewHolder.card_ImageView != null) {
                Glide.with(viewHolder.card_ImageView.getContext()).load(api.getCardImage()).into(viewHolder.card_ImageView);
            } else {
                ((LinearLayout) viewHolder.card_ImageView.getParent()).removeView(viewHolder.card_ImageView);
            }
            viewHolder.card_TextView.setText(api.getCardTitle() + "\n");
            viewHolder.card_TextView.append(Html.fromHtml(api.getCardDescription(), Html.FROM_HTML_MODE_LEGACY));
            //クリック
            viewHolder.toot_card_LinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    useCustomTabs(api.getCardURL());
                }
            });
        }
    }

    /**
     * Reblogに対応させる
     */
    private void setReBlogToot(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item, CustomMenuJSONParse setting) {
        //null Check
        viewHolder.toot_reblog_LinearLayout.removeAllViews();
        if (api.getBTAccountID() != null) {
            //getLayoutInflaterが使えないので
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //Cardのレイアウト適用
            layoutInflater.inflate(R.layout.custom_menu_recyclerview_adapter_reblog, viewHolder.toot_reblog_LinearLayout);
            viewHolder.reblog_avatar_ImageView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_reblog_avatar);
            viewHolder.reblog_user_TextView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_reblog_account);
            viewHolder.reblog_toot_text_TextView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_reblog_text);
            if (viewHolder.reblog_avatar_ImageView != null && viewHolder.reblog_user_TextView != null && viewHolder.reblog_toot_text_TextView != null && viewHolder.toot_user_TextView != null && viewHolder.toot_text_TextView != null && viewHolder.reblog_avatar_ImageView != null) {
                //入れる
                if (getLoadImageConnection(viewHolder, setting)) {
                    //既定でGIFは再生しない方向で
                    if (pref_setting.getBoolean("pref_avater_gif", true)) {
                        //GIFアニメ再生させない
                        Glide.with(context).load(api.getBTAvatarUrlNotGif()).into(viewHolder.reblog_avatar_ImageView);
                    } else {
                        //GIFアニメを再生
                        Glide.with(context).load(api.getBTAvatarUrl()).into(viewHolder.reblog_avatar_ImageView);
                    }
                } else {
                    if (viewHolder.reblog_avatar_ImageView != null) {
                        ((LinearLayout) viewHolder.reblog_avatar_ImageView.getParent()).removeView(viewHolder.reblog_avatar_ImageView);
                    }
                }
                PicassoImageGetter toot_ImageGetter = new PicassoImageGetter(viewHolder.reblog_toot_text_TextView);
                PicassoImageGetter user_ImageGetter = new PicassoImageGetter(viewHolder.reblog_user_TextView);
                //色を変える機能
                String text = api.getBTTootText();
                if (context instanceof Home) {
                    String highlight = ((Home) context).getTlQuickSettingSnackber().getHighlightText();
                    if (!highlight.equals("")) {
                        text = text.replace(highlight, "<font color=\"red\">" + highlight + "</font>");
                    }
                }
                viewHolder.reblog_user_TextView.setText(Html.fromHtml(api.getBTAccountDisplayName(), Html.FROM_HTML_MODE_COMPACT, toot_ImageGetter, null));
                viewHolder.reblog_toot_text_TextView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT, user_ImageGetter, null));
                viewHolder.reblog_user_TextView.append("@" + api.getBTAccountAcct());
                //～～がブーストしましたを出す
                viewHolder.toot_user_TextView.append(context.getString(R.string.reblog));
                viewHolder.toot_text_TextView.setText("");
                Drawable drawable = context.getDrawable(R.drawable.ic_repeat_black_24dp_2);
                drawable.setTint(Color.parseColor("#008000"));
                viewHolder.toot_user_TextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                //クイックプロフィール
                if (item.get(6).contains("Misskey")) {
                    showMisskeyQuickProfile(viewHolder.reblog_avatar_ImageView, api.getBTAccountID(), item, setting);
                } else {
                    showQuickProfile(viewHolder.reblog_avatar_ImageView, api.getBTAccountID(), viewHolder, item, setting);
                }
            }
        }
    }

    /**
     * 通知タイプ
     */
    private void showNotificationType(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        //リアクションを出す
        viewHolder.mainLinearLayout.removeView(viewHolder.notification_type_TextView);
        if (api.getNotification_Type() != null) {
            viewHolder.notification_type_TextView.setText(toNotificationType(context, api.getNotification_Type()));
            if (api.getReaction_Type() != null) {
                //Misskey Reaction
                viewHolder.notification_type_TextView.append("  " + HomeTimeLineAdapter.toReactionEmoji(api.getReaction_Type()));
            }
            viewHolder.notification_type_TextView.setPadding(10, 10, 10, 10);
            viewHolder.mainLinearLayout.addView(viewHolder.notification_type_TextView, 0);
            //DM以外でレイアウト消す
            if (api.getNotification_Type().equals("follow")) {
                LinearLayout linearLayout = ((LinearLayout) viewHolder.toot_favourite_TextView.getParent());
                viewHolder.mainLinearLayout.removeView(linearLayout);
            }
        }
    }

    /**
     * 通知タイプ分ける
     */
    public static String toNotificationType(Context context, String type) {
        switch (type) {
            case "follow":
                type = context.getString(R.string.notification_followed);
                break;
            case "favourite":
                type = context.getString(R.string.notification_favourite);
                break;
            case "reblog":
                type = context.getString(R.string.notification_Boost);
                break;
            case "mention":
                type = context.getString(R.string.notification_mention);
                break;
            case "reaction":
                type = context.getString(R.string.reaction_ok);
                break;
            case "poll":
                type = context.getString(R.string.notification_poll);
                break;
        }
        return type;
    }

    /**
     * BT,FavのAPI
     */
    private void TootAction(String id, String endPoint, TextView textView, MastodonTLAPIJSONParse api, ArrayList<String> item, CustomMenuJSONParse setting) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://" + Instance + "/api/v1/statuses/" + item.get(2) + "/" + endPoint + "/?access_token=" + AccessToken;
                RequestBody requestBody = new FormBody.Builder()
                        .build();
                //作成
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
                //GETリクエスト
                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String response_string = response.body().string();
                        if (!response.isSuccessful()) {
                            //失敗
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            //UI Thread
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //Fav/BT Countを表示できるようにする
                                    MastodonTLAPIJSONParse api = new MastodonTLAPIJSONParse(context, response_string, setting);
                                    if (setting.getYes_fav_icon() != null && setting.getNo_fav_icon() != null) {
                                        //setCustomizeFavIcon();
                                    } else {
                                        if (endPoint.contains("reblog")) {
                                            Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.boost_ok) + " : " + id, Toast.LENGTH_SHORT).show();
                                            Drawable boostIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                                            boostIcon.setTint(Color.parseColor("#008000"));
                                            textView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
                                            textView.setText(api.getBTCount());
                                            //BTしたぜ！
                                            item.set(4, "true");
                                        }
                                        if (endPoint.contains("favourite")) {
                                            Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.favourite_add) + " : " + id, Toast.LENGTH_SHORT).show();
                                            Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                                            favIcon.setTint(Color.parseColor("#ffd700"));
                                            textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                            textView.setText(api.getFavCount());
                                            //Favしたぜ！
                                            item.set(5, "true");
                                        }
                                        if (endPoint.contains("unfavourite")) {
                                            Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.delete_fav_toast) + " : " + id, Toast.LENGTH_SHORT).show();
                                            Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                                            favIcon.setTint(Color.parseColor("#000000"));
                                            textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                            textView.setText(api.getFavCount());
                                            //Favしたぜ！
                                            item.set(5, "false");
                                        }
                                        if (endPoint.contains("unreblog")) {
                                            Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.delete_bt_toast) + " : " + id, Toast.LENGTH_SHORT).show();
                                            Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                                            favIcon.setTint(Color.parseColor("#000000"));
                                            textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                            textView.setText(api.getBTCount());
                                            //BTしたぜ！
                                            item.set(4, "false");
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }).start();

    }

    /**
     * Misskey リアクション
     */
    private void setMisskeyReaction(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item, CustomMenuJSONParse setting) {
        //アイコン変更
        viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(context.getDrawable(R.drawable.ic_add_black_24dp), null, null, null);
        viewHolder.toot_favourite_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_INDEFINITE);
                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(R.id.snackbar_text).getParent();
                //TextViewを非表示にする
                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
                snackBer_textView.setVisibility(View.INVISIBLE);

                //Linearlayout
                LinearLayout main_LinearLayout = new LinearLayout(context);
                main_LinearLayout.setOrientation(LinearLayout.VERTICAL);
                main_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                //Text
                TextView title_TextView = new TextView(context);
                title_TextView.setTextColor(Color.parseColor("#ffffff"));
                title_TextView.setTextSize(18);
                title_TextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                title_TextView.setText(context.getText(R.string.add_reaction));

                //ボタン追加
                String[] reactionEmojis = new String[]{"👍", "❤", "😆", "🤔", "😮", "🎉", "💢", "😥", "😇", "🍣"};
                String[] reactionNames = new String[]{"like", "love", "laugh", "hmm", "surprise", "congrats", "angry", "confused", "rip", "pudding",};
                //2行にする
                LinearLayout reaction_LinearLayout_up = new LinearLayout(context);
                LinearLayout reaction_LinearLayout_down = new LinearLayout(context);
                reaction_LinearLayout_up.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                reaction_LinearLayout_down.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                reaction_LinearLayout_up.setOrientation(LinearLayout.HORIZONTAL);
                reaction_LinearLayout_down.setOrientation(LinearLayout.HORIZONTAL);
                ViewGroup.LayoutParams button_LayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ((LinearLayout.LayoutParams) button_LayoutParams).weight = 1;
                //for
                for (int i = 0; i < reactionEmojis.length; i++) {
                    Button button = new Button(context);
                    button.setBackground(context.getDrawable(R.drawable.button_style));
                    button.setLayoutParams(button_LayoutParams);
                    button.setText(reactionEmojis[i]);
                    //クリックイベント
                    int finalI = i;
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //確認、ダイアログを出さない設定とう確認してから
                            if (pref_setting.getBoolean("pref_nicoru_dialog", true) && !Boolean.valueOf(setting.getDialog())) {
                                Snackbar.make(v, context.getText(R.string.reaction_message), Snackbar.LENGTH_SHORT).setAction(context.getText(R.string.reaction_post), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        postMisskeyReaction("create", reactionNames[finalI], api.getToot_ID(), viewHolder);
                                        item.set(5, reactionEmojis[finalI]);
                                        viewHolder.toot_favourite_TextView.setText(HomeTimeLineAdapter.toReactionEmoji(reactionEmojis[finalI]));
                                    }
                                }).show();
                            } else {
                                postMisskeyReaction("create", reactionNames[finalI], api.getToot_ID(), viewHolder);
                                item.set(5, reactionEmojis[finalI]);
                                viewHolder.toot_favourite_TextView.setText(HomeTimeLineAdapter.toReactionEmoji(reactionEmojis[finalI]));
                            }
                        }
                    });
                    //0-4までは上の段
                    if (i < 5) {
                        reaction_LinearLayout_up.addView(button);
                    } else {
                        reaction_LinearLayout_down.addView(button);
                    }
                }
                //絵文字を入力する
                //レイアウト読み込み
                LinearLayout emoji_LinearLayout = new LinearLayout(context);
                emoji_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                emoji_LinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                EditText editText = new EditText(context);
                editText.setHint(context.getString(R.string.reaction_pick));
                editText.setHintTextColor(Color.parseColor("#ffffff"));
                //大きくする
                ViewGroup.LayoutParams edittext_Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                editText.setLayoutParams(edittext_Params);
                Button post_Button = new Button(context);
                post_Button.setBackground(context.getDrawable(R.drawable.button_style));
                post_Button.setText(context.getText(R.string.reaction_post));
                //ボタンのサイズ
                ViewGroup.LayoutParams button_Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ((LinearLayout.LayoutParams) edittext_Params).weight = 1;
                post_Button.setLayoutParams(button_Params);
                post_Button.setTextColor(Color.parseColor("#ffffff"));
                //クリックイベント
                post_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pref_setting.getBoolean("pref_nicoru_dialog", true) && !Boolean.valueOf(setting.getDialog())) {
                            Snackbar.make(v, context.getText(R.string.reaction_message), Snackbar.LENGTH_SHORT).setAction(context.getText(R.string.reaction_post), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    postMisskeyReaction("create", editText.getText().toString(), api.getToot_ID(), viewHolder);
                                    item.set(17, editText.getText().toString());
                                    viewHolder.toot_favourite_TextView.setText(editText.getText().toString());
                                }
                            }).show();
                        } else {
                            postMisskeyReaction("create", editText.getText().toString(), api.getToot_ID(), viewHolder);
                            item.set(17, editText.getText().toString());
                            viewHolder.toot_favourite_TextView.setText(editText.getText().toString());
                        }
                    }
                });
                //追加
                emoji_LinearLayout.addView(editText);
                emoji_LinearLayout.addView(post_Button);

                //追加
                main_LinearLayout.addView(title_TextView);
                main_LinearLayout.addView(reaction_LinearLayout_up);
                main_LinearLayout.addView(reaction_LinearLayout_down);
                main_LinearLayout.addView(emoji_LinearLayout);

                snackBer_viewGrop.addView(main_LinearLayout, 0);
                //表示
                snackbar.show();
            }
        });
    }

    /**
     * Misskey クイックプロフィール
     */
    private void showMisskeyQuickProfile(View imageView, String id, ArrayList<String> item, CustomMenuJSONParse setting) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String instance = pref_setting.getString("misskey_main_instance", "");
                String token = pref_setting.getString("misskey_main_token", "");
                String username = pref_setting.getString("misskey_main_username", "");
                String url = "https://" + instance + "/api/users/show";
                //読み込み中お知らせ
                Snackbar snackbar = Snackbar.make(v, context.getString(R.string.loading_user_info) + "\r\n" + url, Snackbar.LENGTH_INDEFINITE);
                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(R.id.snackbar_text).getParent();
                //SnackBerを複数行対応させる
                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
                snackBer_textView.setMaxLines(2);
                //複数行対応させたおかげでずれたので修正
                ProgressBar progressBar = new ProgressBar(context);
                LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                progressBer_layoutParams.gravity = Gravity.CENTER;
                progressBar.setLayoutParams(progressBer_layoutParams);
                snackBer_viewGrop.addView(progressBar, 0);
                snackbar.show();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("i", token);
                    jsonObject.put("userId", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
                //作成
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                //GETリクエスト
                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //失敗時
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String response_string = response.body().string();
                        if (!response.isSuccessful()) {
                            //失敗時
                            imageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, context.getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(response_string);
                                String display_name = jsonObject.getString("name");
                                String username = jsonObject.getString("username");
                                String description = jsonObject.getString("description");
                                String avatarUrl = jsonObject.getString("avatarUrl");
                                String followingCount = jsonObject.getString("followingCount");
                                String followersCount = jsonObject.getString("followersCount");
                                Boolean isFollowing = jsonObject.getBoolean("isFollowing");
                                Boolean isFollowed = jsonObject.getBoolean("isFollowed");
                                //カスタム絵文字適用
                                if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(setting.getCustom_emoji())) {
                                    //他のところでは一旦配列に入れてるけど今回はここでしか使ってないから省くね
                                    JSONArray emojis = jsonObject.getJSONArray("emojis");
                                    for (int i = 0; i < emojis.length(); i++) {
                                        JSONObject emojiObject = emojis.getJSONObject(i);
                                        String emoji_name = emojiObject.getString("name");
                                        String emoji_url = emojiObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        //display_name
                                        if (display_name.contains(emoji_name)) {
                                            //あったよ
                                            display_name = display_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                        }
                                        //note
                                        if (description.contains(emoji_name)) {
                                            //あったよ
                                            description = description.replace(":" + emoji_name + ":", custom_emoji_src);
                                        }
                                    }
                                }
                                //フォローされてるかどうかの文字
                                String follow_back = context.getString(R.string.follow_back_not);
                                if (isFollowing) {
                                    follow_back = context.getString(R.string.follow_back);
                                }
                                Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_SHORT);
                                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(R.id.snackbar_text).getParent();
                                LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                progressBer_layoutParams.gravity = Gravity.CENTER;
                                //SnackBerを複数行対応させる
                                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
                                snackBer_textView.setMaxLines(Integer.MAX_VALUE);
                                //てきすと
                                //snackBer_textView.setText(Html.fromHtml(profile_note,Html.FROM_HTML_MODE_COMPACT));
                                //複数行対応させたおかげでずれたので修正
                                ImageView avater_ImageView = new ImageView(context);
                                avater_ImageView.setLayoutParams(progressBer_layoutParams);
                                //LinearLayout動的に生成
                                LinearLayout snackber_LinearLayout = new LinearLayout(context);
                                snackber_LinearLayout.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams warp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                snackber_LinearLayout.setLayoutParams(warp);
                                //そこにTextViewをいれる（もとからあるTextViewは無視）
                                TextView snackber_TextView = new TextView(context);
                                PicassoImageGetter imageGetter = new PicassoImageGetter(snackber_TextView);
                                snackber_TextView.setLayoutParams(warp);
                                snackber_TextView.setTextColor(Color.parseColor("#ffffff"));
                                snackber_TextView.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY, imageGetter, null));
                                //ボタン追加
                                Button userPage_Button = new Button(context, null, 0, R.style.Widget_AppCompat_Button_Borderless);
                                userPage_Button.setLayoutParams(warp);
                                userPage_Button.setBackground(context.getDrawable(R.drawable.button_style));
                                userPage_Button.setTextColor(Color.parseColor("#ffffff"));
                                userPage_Button.setText(R.string.user);
                                Drawable boostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_person_black_24dp, null);
                                boostIcon.setTint(Color.parseColor("#ffffff"));
                                userPage_Button.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
                                userPage_Button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(context, UserActivity.class);
                                        //IDを渡す
                                        intent.putExtra("Misskey", true);
                                        intent.putExtra("Account_ID", id);
                                        saveInstanceToken(item);
                                        context.startActivity(intent);
                                    }
                                });


                                //ふぉろー
                                TextView follow_TextView = new TextView(context);
                                follow_TextView.setTextColor(Color.parseColor("#ffffff"));
                                follow_TextView.setText(context.getString(R.string.follow) + " : \n" + followingCount);
                                Drawable done = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_done_black_24dp, null);
                                done.setTint(Color.parseColor("#ffffff"));
                                follow_TextView.setLayoutParams(warp);
                                follow_TextView.setCompoundDrawablesWithIntrinsicBounds(done, null, null, null);
                                //ふぉろわー
                                TextView follower_TextView = new TextView(context);
                                follower_TextView.setTextColor(Color.parseColor("#ffffff"));
                                follower_TextView.setText(context.getString(R.string.follower) + " : \n" + followersCount);
                                Drawable done_all = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_done_all_black_24dp, null);
                                done_all.setTint(Color.parseColor("#ffffff"));
                                follower_TextView.setLayoutParams(warp);
                                follower_TextView.setCompoundDrawablesWithIntrinsicBounds(done_all, null, null, null);

                                //ふぉろーされているか
                                TextView follow_info = new TextView(context);
                                follow_info.setTextColor(Color.parseColor("#ffffff"));
                                follow_info.setLayoutParams(warp);
                                Drawable follow_info_drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_info_outline_black_24dp, null);
                                follow_info_drawable.setTint(Color.parseColor("#ffffff"));
                                follow_info.setCompoundDrawablesWithIntrinsicBounds(follow_info_drawable, null, null, null);
                                //日本語のときだけ改行する
                                StringBuilder stringBuilder = new StringBuilder(follow_back);
                                if (!follow_back.contains("Following") && !follow_back.contains("not following")) {
                                    follow_info.setText(stringBuilder.insert(4, "\n"));
                                } else {
                                    follow_info.setText(follow_back);
                                }


                                //ぷろが、ふぉろーふぉろわー、ふぉろーじょうたい、アカウントベージ移動、用LinearLayout
                                LinearLayout account_info_LinearLayout = new LinearLayout(context);
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
                                    Bitmap bitmap = Glide.with(context).asBitmap().load(avatarUrl).submit(100, 100).get();
                                    avater_ImageView.setImageBitmap(bitmap);
                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                                snackbar.show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }


    /**
     * QuickProfile
     */
    private void showQuickProfile(View imageView, String id, ViewHolder viewHolder, ArrayList<String> item, CustomMenuJSONParse setting) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //設定・カスタムメニュー
                if (pref_setting.getBoolean("pref_quick_profile", false) || Boolean.valueOf(setting.getQuick_profile())) {
                    //読み込み中お知らせ
                    Snackbar snackbar = Snackbar.make(v, context.getString(R.string.loading_user_info) + "\r\n /api/v1/accounts/" + id, Snackbar.LENGTH_INDEFINITE);
                    ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(R.id.snackbar_text).getParent();
                    //SnackBerを複数行対応させる
                    TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
                    snackBer_textView.setMaxLines(2);
                    //複数行対応させたおかげでずれたので修正
                    ProgressBar progressBar = new ProgressBar(context);
                    LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    progressBer_layoutParams.gravity = Gravity.CENTER;
                    progressBar.setLayoutParams(progressBer_layoutParams);
                    snackBer_viewGrop.addView(progressBar, 0);
                    snackbar.show();

                    //APIを叩く
                    String url = "https://" + Instance + "/api/v1/accounts/" + id;
                    //System.out.println(url);
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
                                if (pref_setting.getBoolean("pref_custom_emoji", true) || Boolean.valueOf(setting.getQuick_profile())) {
                                    if (getLoadImageConnection(viewHolder, setting)) {
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
                                        if (!jsonObject.isNull("profile_emojis")) {
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
                                    }
                                }


                                //フォローされているか（無駄にAPI叩いてね？）
                                final String[] follow_back = {context.getString(R.string.follow_back_not)};
                                String follow_url = "https://" + Instance + "/api/v1/accounts/relationships/?stream=user&access_token=" + AccessToken;

                                //パラメータを設定
                                HttpUrl.Builder builder = HttpUrl.parse(follow_url).newBuilder();
                                builder.addQueryParameter("id", String.valueOf(id));
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
                                                follow_back[0] = context.getString(R.string.follow_back);
                                            }
                                            Bitmap bitmap = Glide.with(context).asBitmap().load(avater_url).submit(100, 100).get();

                                            v.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_SHORT);
                                                    ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(R.id.snackbar_text).getParent();
                                                    LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                    progressBer_layoutParams.gravity = Gravity.CENTER;
                                                    //SnackBerを複数行対応させる
                                                    TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(R.id.snackbar_text);
                                                    snackBer_textView.setMaxLines(Integer.MAX_VALUE);
                                                    //てきすと
                                                    //snackBer_textView.setText(Html.fromHtml(profile_note,Html.FROM_HTML_MODE_COMPACT));
                                                    //複数行対応させたおかげでずれたので修正
                                                    ImageView avater_ImageView = new ImageView(context);
                                                    avater_ImageView.setLayoutParams(progressBer_layoutParams);
                                                    //LinearLayout動的に生成
                                                    LinearLayout snackber_LinearLayout = new LinearLayout(context);
                                                    snackber_LinearLayout.setOrientation(LinearLayout.VERTICAL);
                                                    LinearLayout.LayoutParams warp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                    snackber_LinearLayout.setLayoutParams(warp);
                                                    //そこにTextViewをいれる（もとからあるTextViewは無視）
                                                    TextView snackber_TextView = new TextView(context);
                                                    PicassoImageGetter imageGetter = new PicassoImageGetter(snackber_TextView);
                                                    snackber_TextView.setLayoutParams(warp);
                                                    snackber_TextView.setTextColor(Color.parseColor("#ffffff"));
                                                    snackber_TextView.setText(Html.fromHtml(finalProfile_note, Html.FROM_HTML_MODE_LEGACY, imageGetter, null));
                                                    //ボタン追加
                                                    Button userPage_Button = new Button(context, null, 0, R.style.Widget_AppCompat_Button_Borderless);
                                                    userPage_Button.setLayoutParams(warp);
                                                    userPage_Button.setBackground(context.getDrawable(R.drawable.button_style));
                                                    userPage_Button.setTextColor(Color.parseColor("#ffffff"));
                                                    userPage_Button.setText(R.string.user);
                                                    Drawable boostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_person_black_24dp, null);
                                                    boostIcon.setTint(Color.parseColor("#ffffff"));
                                                    userPage_Button.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
                                                    userPage_Button.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent intent = new Intent(context, UserActivity.class);
                                                            //IDを渡す
                                                            intent.putExtra("Account_ID", id);
                                                            saveInstanceToken(item);
                                                            context.startActivity(intent);
                                                        }
                                                    });


                                                    //ふぉろー
                                                    TextView follow_TextView = new TextView(context);
                                                    follow_TextView.setTextColor(Color.parseColor("#ffffff"));
                                                    follow_TextView.setText(context.getString(R.string.follow) + " : \n" + follow);
                                                    Drawable done = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_done_black_24dp, null);
                                                    done.setTint(Color.parseColor("#ffffff"));
                                                    follow_TextView.setLayoutParams(warp);
                                                    follow_TextView.setCompoundDrawablesWithIntrinsicBounds(done, null, null, null);
                                                    //ふぉろわー
                                                    TextView follower_TextView = new TextView(context);
                                                    follower_TextView.setTextColor(Color.parseColor("#ffffff"));
                                                    follower_TextView.setText(context.getString(R.string.follower) + " : \n" + follower);
                                                    Drawable done_all = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_done_all_black_24dp, null);
                                                    done_all.setTint(Color.parseColor("#ffffff"));
                                                    follower_TextView.setLayoutParams(warp);
                                                    follower_TextView.setCompoundDrawablesWithIntrinsicBounds(done_all, null, null, null);

                                                    //ふぉろーされているか
                                                    TextView follow_info = new TextView(context);
                                                    follow_info.setTextColor(Color.parseColor("#ffffff"));
                                                    follow_info.setLayoutParams(warp);
                                                    Drawable follow_info_drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_info_outline_black_24dp, null);
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
                                                    LinearLayout account_info_LinearLayout = new LinearLayout(context);
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
                                                    avater_ImageView.setImageBitmap(bitmap);
                                                    snackbar.show();
                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Intent intent = new Intent(context, UserActivity.class);
                    intent.putExtra("Account_ID", id);
                    context.startActivity(intent);
                }
            }
        });
    }


    /**
     * Misskey Reactionする！
     *
     * @param create_delete createかdelete
     * @param reactionName  リアクション（リアクション一覧どこにあるの？）
     */
    private void postMisskeyReaction(String create_delete, String reactionName, String id_string, ViewHolder viewHolder) {
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        String url = "https://" + instance + "/api/notes/reactions/" + create_delete;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("noteId", id_string);
            jsonObject.put("reaction", reactionName);
            jsonObject.put("i", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        //作成
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //失敗時
                viewHolder.toot_favourite_TextView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                if (!response.isSuccessful()) {
                    //失敗時
                    viewHolder.toot_favourite_TextView.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, context.getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    //成功時
                    viewHolder.toot_favourite_TextView.post(new Runnable() {
                        @Override
                        public void run() {
                            //メッセージ
                            if (url.contains("create")) {
                                Toast.makeText(context, context.getString(R.string.reaction_ok) + ":" + HomeTimeLineAdapter.toReactionEmoji(reactionName) + "\n" + id_string, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, context.getString(R.string.reaction_delete_ok) + "\n" + id_string, Toast.LENGTH_SHORT).show();
                            }
                            viewHolder.toot_favourite_TextView.setText(HomeTimeLineAdapter.toReactionEmoji(reactionName));
                        }
                    });
                }
            }
        });
    }

    /**
     * 時刻をフォーマットして返す
     */
    private String getCreatedAtFormat(String createdAt) {
        //フォーマットを規定の設定にする？
        //ここtrueにした
        if (pref_setting.getBoolean("pref_custom_time_format", true)) {
            //時差計算？
            if (simpleDateFormat == null && japanDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                //日本用フォーマット
                japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"));
                japanDateFormat.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
                //calendar = Calendar.getInstance();
            }
            try {
                Date date = simpleDateFormat.parse(createdAt);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                //タイムゾーンを設定
                //calendar.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
                //calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                createdAt = japanDateFormat.format(calendar.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return createdAt;
    }

    /**
     * カスタムフォントを利用する
     */
    private void setCustomFont(ViewHolder viewHolder, CustomMenuJSONParse setting) {
        File file = new File(setting.getFont());
        if (file.exists()) {
            //配列の場所特定
            int position = type_face_String.indexOf(setting.getFont());
            Typeface typeface = type_face_list.get(position);
            viewHolder.toot_user_TextView.setTypeface(typeface);
            viewHolder.toot_createAt_TextView.setTypeface(typeface);
            viewHolder.toot_visibility_TextView.setTypeface(typeface);
            viewHolder.toot_text_TextView.setTypeface(typeface);
            viewHolder.toot_boost_TextView.setTypeface(typeface);
            viewHolder.toot_favourite_TextView.setTypeface(typeface);
            if (!viewHolder.toot_client_TextView.getText().toString().equals("")) {
                viewHolder.toot_client_TextView.setTypeface(typeface);
            }
        }
    }

    /**
     * 隠すやつ（語彙力
     */
    private void setSpoiler_text(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        //なにもないときは動かない
        if (api.getSpoiler_text() != null) {
            if (!api.getSpoiler_text().contains("")) {
                //本文を消す
                PicassoImageGetter picassoImageGetter = new PicassoImageGetter(viewHolder.toot_text_TextView);
                viewHolder.toot_text_TextView.setText(Html.fromHtml(api.getSpoiler_text(), 0, picassoImageGetter, null));
                //ボタン追加
                viewHolder.mainLinearLayout.removeView(viewHolder.spoiler_text_Button);
                viewHolder.spoiler_text_Button.setText(context.getString(R.string.show));
                viewHolder.spoiler_text_Button.setBackground(context.getDrawable(R.drawable.button_style));
                viewHolder.spoiler_text_Button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                viewHolder.mainLinearLayout.addView(viewHolder.spoiler_text_Button, 2);
                //クリックイベント
                viewHolder.spoiler_text_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!viewHolder.spoiler_text_Button.getText().toString().equals(context.getString(R.string.show))) {
                            viewHolder.toot_text_TextView.setText(api.getSpoiler_text());
                            viewHolder.spoiler_text_Button.setText(context.getString(R.string.show));
                        } else {
                            viewHolder.toot_text_TextView.setText(Html.fromHtml(api.getToot_text(), Html.FROM_HTML_MODE_COMPACT, picassoImageGetter, null));
                            viewHolder.spoiler_text_Button.setText(context.getString(R.string.hidden));
                        }
                    }
                });
            }
        }
    }

    /**
     * トゥートオプション
     */
    private void showTootOption(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item) {
        //ブックマークボタン
        viewHolder.toot_bookmark_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TootOptionBottomDialog dialog = new TootOptionBottomDialog();
                Bundle bundle = new Bundle();
                bundle.putString("instance", Instance);
                bundle.putString("user_id", api.getUser_ID());
                bundle.putString("user_name", api.getAcct());
                bundle.putString("status_id", api.getToot_ID());
                bundle.putString("status_text", viewHolder.toot_text_TextView.getText().toString());
                bundle.putString("json", item.get(3));
                dialog.setArguments(bundle);
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "toot_option");
            }
        });
    }

    /**
     * レイアウトをほぼ消す
     */
    private void setSimpleLayout(ViewHolder viewHolder) {
        //TootTextView以外消す
        LinearLayout parent_LinearLayout = viewHolder.mainLinearLayout;
        parent_LinearLayout.removeView(viewHolder.account_LinearLayout);
        parent_LinearLayout.removeView(viewHolder.toot_reblog_LinearLayout);
        parent_LinearLayout.removeView(viewHolder.toot_media_LinearLayout);
        parent_LinearLayout.removeView(viewHolder.toot_card_LinearLayout);
        parent_LinearLayout.removeView(viewHolder.action_LinearLayout);
    }

    /**
     * FollowLayout
     */
    private void setAccountLayout(ViewHolder viewHolder) {
        //TootTextView/ImageView以外消す
        LinearLayout parent_LinearLayout = viewHolder.mainLinearLayout;
        LinearLayout account_LinearLayout = viewHolder.account_LinearLayout;
        LinearLayout toot_info_LinearLayout = (LinearLayout) viewHolder.toot_createAt_TextView.getParent().getParent().getParent();
        if (toot_info_LinearLayout != null) {
            toot_info_LinearLayout.removeView((LinearLayout) viewHolder.toot_createAt_TextView.getParent().getParent());
            toot_info_LinearLayout.removeView((LinearLayout) viewHolder.toot_visibility_TextView.getParent().getParent());
            toot_info_LinearLayout.removeView((LinearLayout) viewHolder.toot_client_TextView.getParent());
        }
        //parent_LinearLayout.removeView(viewHolder.toot_reblog_LinearLayout);
        //parent_LinearLayout.removeView(viewHolder.toot_media_LinearLayout);
        //parent_LinearLayout.removeView(viewHolder.toot_card_LinearLayout);
        //parent_LinearLayout.removeView(viewHolder.action_LinearLayout);
    }

    /*PiPLayout*/
    private void setPiPLayout(ViewHolder viewHolder) {
        //TootTextView以外消す
        setAccountLayout(viewHolder);
        LinearLayout parent_LinearLayout = viewHolder.mainLinearLayout;
        parent_LinearLayout.removeView(viewHolder.toot_reblog_LinearLayout);
        parent_LinearLayout.removeView(viewHolder.toot_media_LinearLayout);
        parent_LinearLayout.removeView(viewHolder.toot_card_LinearLayout);
        parent_LinearLayout.removeView(viewHolder.action_LinearLayout);
    }

    /**
     * Account Layout設定
     */
    private void createAccountLinearLayout(ViewHolder viewHolder, MastodonAccountJSONParse api, ArrayList<String> item, CustomMenuJSONParse setting) {
        //カスタム絵文字
        PicassoImageGetter toot_ImageGetter = new PicassoImageGetter(viewHolder.toot_text_TextView);
        PicassoImageGetter user_ImageGetter = new PicassoImageGetter(viewHolder.toot_user_TextView);
        //SetText
        viewHolder.toot_text_TextView.setText(Html.fromHtml(api.getNote(), Html.FROM_HTML_MODE_COMPACT, toot_ImageGetter, null));
        viewHolder.toot_user_TextView.setText(Html.fromHtml(api.getDisplay_name(), Html.FROM_HTML_MODE_COMPACT, user_ImageGetter, null));
        viewHolder.toot_user_TextView.append("@" + api.getAcct());
        //アバター画像
        loadAccountLayoutAvatarImage(api, viewHolder, setting);
        //クイックプロフィール
        showQuickProfile(viewHolder.toot_avatar_ImageView, api.getUser_id(), viewHolder, item, setting);
    }

    /**
     * 投票
     */
    private void showVoteLayout(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        viewHolder.vote_LinearLayout.removeAllViews();
        //投票があるか
        if (api.isVote()) {
            viewHolder.vote_1.setText(api.getVotes_title().get(0) + " (" + api.getVotes_count().get(0) + ")");
            viewHolder.vote_2.setText(api.getVotes_title().get(1) + " (" + api.getVotes_count().get(1) + ")");
            viewHolder.vote_1.setBackground(context.getDrawable(R.drawable.button_style));
            viewHolder.vote_2.setBackground(context.getDrawable(R.drawable.button_style));
            viewHolder.vote_LinearLayout.addView(viewHolder.vote_1);
            viewHolder.vote_LinearLayout.addView(viewHolder.vote_2);
            postVote(viewHolder, api, "0", viewHolder.vote_1);
            postVote(viewHolder, api, "1", viewHolder.vote_2);

            if (api.getVotes_title().size() > 2) {
                viewHolder.vote_3.setText(api.getVotes_title().get(2) + " (" + api.getVotes_count().get(2) + ")");
                viewHolder.vote_3.setBackground(context.getDrawable(R.drawable.button_style));
                viewHolder.vote_LinearLayout.addView(viewHolder.vote_3);
                postVote(viewHolder, api, "2", viewHolder.vote_3);
            }
            if (api.getVotes_title().size() > 3) {
                viewHolder.vote_4.setText(api.getVotes_title().get(3) + " (" + api.getVotes_count().get(3) + ")");
                viewHolder.vote_4.setBackground(context.getDrawable(R.drawable.button_style));
                viewHolder.vote_LinearLayout.addView(viewHolder.vote_4);
                postVote(viewHolder, api, "3", viewHolder.vote_4);
            }
            //時間
            viewHolder.vote_time.setText(getCreatedAtFormat(api.getVote_expires_at()));
            viewHolder.vote_time.setCompoundDrawablesRelativeWithIntrinsicBounds(context.getDrawable(R.drawable.ic_access_time_black_24dp), null, null, null);
            viewHolder.vote_LinearLayout.addView(viewHolder.vote_time);
        }
    }

    /**
     * 投票API
     */
    private void postVote(ViewHolder viewHolder, MastodonTLAPIJSONParse api, String choices, Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(viewHolder.mainLinearLayout, context.getString(R.string.vote_post_message) + " : " + choices, Snackbar.LENGTH_LONG).setAction(context.getText(R.string.vote_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = "https://" + Instance + "/api/v1/polls/" + api.getVote_id() + "/votes";
                        //複数行けるっぽい？
                        JSONObject jsonObject = new JSONObject();
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.put(choices);
                        try {
                            jsonObject.put("access_token", AccessToken);
                            jsonObject.put("choices", jsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        RequestBody requestBody_json = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody_json)
                                .build();
                        //GETリクエスト
                        OkHttpClient client = new OkHttpClient();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                viewHolder.mainLinearLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String response_strging = response.body().string();
                                if (!response.isSuccessful()) {
                                    //失敗時
                                    viewHolder.mainLinearLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, context.getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    viewHolder.mainLinearLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, context.getString(R.string.vote_successful) + choices, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }).show();
            }
        });
    }


    /**
     * 時間指定投稿（予約投稿）のレイアウト
     */
    private void setScheduled_statuses_layout(ViewHolder viewHolder, MastodonScheduledStatusesJSONParse api) {
        LinearLayout parent_LinearLayout = viewHolder.mainLinearLayout;
        //parent_LinearLayout.setPadding(viewHolder.toot_text_TextView.getPaddingLeft(),10,viewHolder.toot_text_TextView.getPaddingRight(),10);
        //TextView
        TextView scheduled_status_at_TextView = new TextView(viewHolder.mainLinearLayout.getContext());
        scheduled_status_at_TextView.setText(getCreatedAtFormat(api.getScheduled_at()));
        scheduled_status_at_TextView.setTextSize(14);
        scheduled_status_at_TextView.setPadding(viewHolder.toot_text_TextView.getPaddingLeft(), 10, viewHolder.toot_text_TextView.getPaddingRight(), 10);
        viewHolder.toot_text_TextView.setText(api.getText());
        viewHolder.toot_text_TextView.setTextSize(14);
        parent_LinearLayout.addView(scheduled_status_at_TextView, 0);
        //削除ボタン
        TextView delete_TextView = new TextView(viewHolder.mainLinearLayout.getContext());
        delete_TextView.setCompoundDrawablesWithIntrinsicBounds(context.getDrawable(R.drawable.ic_alarm_off_black_24dp), null, null, null);
        delete_TextView.setPadding(viewHolder.toot_text_TextView.getPaddingLeft(), 10, viewHolder.toot_text_TextView.getPaddingRight(), 10);
        delete_TextView.setText(context.getString(R.string.delete_ok));
        delete_TextView.setPadding(viewHolder.toot_text_TextView.getPaddingLeft(), 10, 10, 10);
        parent_LinearLayout.addView(delete_TextView);
        //API叩く
        delete_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, context.getString(R.string.toot_shortcut_delete), Snackbar.LENGTH_LONG).setAction(context.getText(R.string.delete_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //削除
                        String url = "https://" + Instance + "/api/v1/scheduled_statuses/" + api.getId() + "?access_token=" + AccessToken;
                        Request request = new Request.Builder()
                                .url(url)
                                .delete()
                                .build();
                        //GETリクエスト
                        OkHttpClient client = new OkHttpClient();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                viewHolder.mainLinearLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    //失敗時
                                    viewHolder.mainLinearLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, context.getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    viewHolder.mainLinearLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, context.getString(R.string.delete_successful), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }).show();
            }
        });
    }

    /**
     * Misskey Renote
     */
    private void setRenote(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item) {
        final String[] message = {context.getString(R.string.renote_message)};
        final String[] button_text = {context.getString(R.string.renote)};
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        final String[] api_url = {"https://" + instance + "/api/notes/create"};
        final JSONObject[] jsonObject = {new JSONObject()};
        viewHolder.toot_boost_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (api.getIsBT().contains("true") || item.set(4, "false").contains("true")) {
                    message[0] = context.getString(R.string.renote_delete_message);
                    button_text[0] = context.getString(R.string.delete_renote);
                    api_url[0] = "https://" + instance + "/api/notes/delete";
                    //APIを叩く
                    try {
                        jsonObject[0].put("i", token);
                        jsonObject[0].put("noteId", api.getToot_ID());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    message[0] = context.getString(R.string.renote_message);
                    button_text[0] = context.getString(R.string.renote);
                    api_url[0] = "https://" + instance + "/api/notes/create";
                    //APIを叩く
                    try {
                        jsonObject[0].put("i", token);
                        jsonObject[0].put("visibility", "home");
                        jsonObject[0].put("renoteId", api.getToot_ID());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //SnackBar
                Snackbar.make(v, message[0], Snackbar.LENGTH_SHORT).setAction(button_text[0], new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject[0].toString());
                        Request request = new Request.Builder().url(api_url[0]).post(requestBody).build();
                        OkHttpClient client = new OkHttpClient();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                viewHolder.toot_boost_TextView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println(jsonObject[0].toString());
                                System.out.println(response.body().string());
                                if (!response.isSuccessful()) {
                                    viewHolder.toot_boost_TextView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, context.getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    viewHolder.toot_boost_TextView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (api_url[0].contains("create")) {
                                                Toast.makeText(context, context.getString(R.string.renote_ok) + "\n" + api.getToot_ID(), Toast.LENGTH_SHORT).show();
                                                Drawable boostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                                                boostIcon.setTint(Color.parseColor("#008000"));
                                                viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
                                                item.set(4, "true");
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.renote_delete_ok) + "\n" + api.getToot_ID(), Toast.LENGTH_SHORT).show();
                                                Drawable boostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_repeat_black_24dp, null);
                                                viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
                                                item.set(4, "false");
                                            }
                                            jsonObject[0] = new JSONObject();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }).show();
            }
        });
    }

    /**
     * とうめいどせってい
     */
    private void setTransparency(ViewHolder viewHolder, CustomMenuJSONParse setting) {
        //CardView取得
        CardView cardView = (CardView) viewHolder.mainLinearLayout.getParent();
        //透明度
        //背景画像設定時のみ
        if (setting.getImage_url().length() != 0) {
            if (Boolean.valueOf(setting.getDark_mode())) {
                cardView.setBackgroundColor(Color.parseColor("#" + setting.getBackground_transparency() + "000000"));
            } else {
                cardView.setBackgroundColor(Color.parseColor("#" + setting.getBackground_transparency() + "ffffff"));
            }
        }
    }


    /**
     * DesktopMode用TootOption
     */
    private void setDesktopTootOption(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item) {
        viewHolder.toot_text_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TootOptionBottomDialog dialog = new TootOptionBottomDialog();
                Bundle bundle = new Bundle();
                bundle.putString("user_id", api.getUser_ID());
                bundle.putString("status_id", api.getToot_ID());
                bundle.putString("status_text", viewHolder.toot_text_TextView.getText().toString());
                bundle.putString("json", item.get(3));
                dialog.setArguments(bundle);
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "toot_option");
            }
        });
    }

    /**
     * インスタンス、アクセストークンの保存
     */
    private void saveInstanceToken(ArrayList<String> item) {
        if (item.get(6).contains("Misskey")) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putString("misskey_main_instance", Instance);
            editor.putString("misskey_main_token", AccessToken);
            editor.apply();
        } else {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putString("main_instance", Instance);
            editor.putString("main_token", AccessToken);
            editor.apply();
        }
    }

    /**
     * 文字サイズ
     */
    private void setFontSetting(ViewHolder viewHolder) {
        viewHolder.toot_user_TextView.setTextSize(Integer.valueOf(pref_setting.getString("pref_fontsize_user", "10")));
        viewHolder.toot_createAt_TextView.setTextSize(Integer.valueOf(pref_setting.getString("pref_fontsize_client", "10")));
        viewHolder.toot_visibility_TextView.setTextSize(Integer.valueOf(pref_setting.getString("pref_fontsize_client", "10")));
        viewHolder.toot_text_TextView.setTextSize(Integer.valueOf(pref_setting.getString("pref_fontsize_timeline", "10")));
        viewHolder.toot_boost_TextView.setTextSize(Integer.valueOf(pref_setting.getString("pref_fontsize_button", "10")));
        viewHolder.toot_favourite_TextView.setTextSize(Integer.valueOf(pref_setting.getString("pref_fontsize_button", "10")));
        if (!viewHolder.toot_client_TextView.getText().toString().equals("")) {
            viewHolder.toot_client_TextView.setTextSize(Integer.valueOf(pref_setting.getString("pref_fontsize_button", "10")));
        }
    }

    /**
     * フォント？
     */
    private void setTypeFace(CustomMenuJSONParse setting) {
        type_face_list = new ArrayList<>();
        type_face_String = new ArrayList<>();
        //配列になかったら生成
        File file = new File(setting.getFont());
        if (file.exists()) {
            if (type_face_String.indexOf(setting.getFont()) == -1) {
                Typeface font_Typeface = Typeface.createFromFile(setting.getFont());
                type_face_list.add(font_Typeface);
                type_face_String.add(setting.getFont());
            }
        }
    }

    /**
     * ふぁぼぼたｎ
     */
    private void setCustomizeFavIcon(ViewHolder viewHolder, MastodonTLAPIJSONParse api, CustomMenuJSONParse setting) {
        //System.out.println(Uri.parse(setting.getNo_fav_icon()));
        //初期化
        if (no_fav_icon_list == null) {
            no_fav_icon_String = new ArrayList<>();
            no_fav_icon_list = new ArrayList<>();
            yes_fav_icon_String = new ArrayList<>();
            yes_fav_icon_list = new ArrayList<>();
        }
        //設定値が存在しなければ利用しない
        if (setting.getNo_fav_icon() != null && setting.getYes_fav_icon() != null) {
            //Fav/BT Check
            if (Boolean.valueOf(api.getIsFav())) {
                if (yes_fav_icon_String.indexOf(setting.getYes_fav_icon()) == -1) {
                    //ないときは配列に入れる準備
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(setting.getYes_fav_icon()));
                        BitmapDrawable resizeDrawable = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
                        viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(resizeDrawable, null, null, null);
                        yes_fav_icon_String.add(setting.getNo_fav_icon());
                        yes_fav_icon_list.add(resizeDrawable);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //あるときは配列から持ってくる
                    viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(yes_fav_icon_list.get(yes_fav_icon_String.indexOf(setting.getNo_fav_icon())), null, null, null);
                }
            } else {
                if (no_fav_icon_String.indexOf(setting.getNo_fav_icon()) == -1) {
                    //ないときは配列に入れる準備
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(setting.getNo_fav_icon()));
                        BitmapDrawable resizeDrawable = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
                        viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(resizeDrawable, null, null, null);
                        no_fav_icon_String.add(setting.getNo_fav_icon());
                        no_fav_icon_list.add(resizeDrawable);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //あるときは配列から持ってくる
                    viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(no_fav_icon_list.get(no_fav_icon_String.indexOf(setting.getNo_fav_icon())), null, null, null);
                }
            }
        }
    }

    //ダークモード時はアイコンをすぺるま
    private void setThemeIconColor(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        //OLED
        //ダークモード処理
        Configuration conf = context.getResources().getConfiguration();
        int currecntNightMode = conf.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        DarkModeSupport darkModeSupport = new DarkModeSupport(context);
        currecntNightMode = darkModeSupport.setIsDarkModeSelf(currecntNightMode);
        //Avatarのときは無効にする
        viewHolder.toot_avatar_ImageView.setImageTintList(null);
        if (viewHolder.reblog_avatar_ImageView != null) {
            viewHolder.reblog_avatar_ImageView.setImageTintList(null);
        }

        Drawable isBoostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_repeat_black_24dp, null);
        Drawable isFavIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_star_black_24dp, null);

        if (api != null && Boolean.valueOf(api.getIsBT())) {
            isBoostIcon.setTint(Color.parseColor("#008000"));
        } else if (api != null && Boolean.valueOf(api.getIsFav())) {
            isFavIcon.setTint(Color.parseColor("#ffd700"));
        } else {
            switch (currecntNightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    isBoostIcon.setTint(Color.parseColor("#000000"));
                    isFavIcon.setTint(Color.parseColor("#000000"));
                    viewHolder.toot_bookmark_TextView.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    isBoostIcon.setTint(Color.parseColor("#ffffff"));
                    isFavIcon.setTint(Color.parseColor("#ffffff"));
                    viewHolder.toot_bookmark_TextView.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.white, context.getTheme()));
                    break;
            }
        }
        viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(isFavIcon, null, null, null);
        viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(isBoostIcon, null, null, null);
    }

    /*こんてんとわーにんぐ 11*/
    private void setContentWarning(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item) {
        viewHolder.option_LinearLayout.removeView(viewHolder.spoiler_text_ImageButton);
        //あった場合h
        if (api.getSpoiler_text() != null && !api.getSpoiler_text().equals("")) {
            //警告文に置き換える
            viewHolder.toot_text_TextView.setText(api.getSpoiler_text() + "\n");
            //表示ボタン追加
            viewHolder.option_LinearLayout.addView(viewHolder.spoiler_text_ImageButton);
            viewHolder.spoiler_text_ImageButton.setImageDrawable(context.getDrawable(R.drawable.ic_warning_black_24dp));
            viewHolder.spoiler_text_ImageButton.setBackgroundColor(Color.parseColor("#00000000"));
            viewHolder.spoiler_text_ImageButton.setPadding(10, 10, 10, 10);
            viewHolder.spoiler_text_ImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //カスタム絵文字
                    PicassoImageGetter toot_ImageGetter = new PicassoImageGetter(viewHolder.toot_text_TextView);
                    //色を変える機能
                    String text = api.getToot_text();
                    if (context instanceof Home) {
                        String highlight = ((Home) context).getTlQuickSettingSnackber().getHighlightText();
                        if (!highlight.equals("")) {
                            text = text.replace(highlight, "<font color=\"red\">" + highlight + "</font>");
                        }
                    }
                    if (!Boolean.valueOf(item.get(11))) {
                        item.set(11, "true");
                        viewHolder.toot_text_TextView.append(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT, toot_ImageGetter, null));
                    } else {
                        item.set(11, "false");
                        viewHolder.toot_text_TextView.setText(api.getSpoiler_text() + "\n");
                    }
                }
            });
        }
    }
}