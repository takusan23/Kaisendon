package io.github.takusan23.kaisendon.CustomMenu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import io.github.takusan23.kaisendon.APIJSONParse.MastodonTLAPIJSONParse;
import io.github.takusan23.kaisendon.Activity.UserActivity;
import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.PicassoImageGetter;
import io.github.takusan23.kaisendon.R;
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView toot_text_TextView;
        public TextView toot_user_TextView;
        public ImageView toot_avatar_ImageView;
        public TextView toot_boost_TextView;
        public TextView toot_favourite_TextView;
        public TextView toot_bookmark_TextView;
        public TextView toot_client_TextView;
        public TextView toot_createAt_TextView;
        public TextView toot_visibility_TextView;
        public LinearLayout toot_media_LinearLayout;
        public LinearLayout mainLinearLayout;
        //画像
        public ImageView media_ImageView_1;
        public ImageView media_ImageView_2;
        public ImageView media_ImageView_3;
        public ImageView media_ImageView_4;
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

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            mainLinearLayout = itemView.findViewById(R.id.custom_menu_adapter_mainLinearLayout);
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
            media_ImageView_1 = new ImageView(context);
            media_ImageView_2 = new ImageView(context);
            media_ImageView_3 = new ImageView(context);
            media_ImageView_4 = new ImageView(context);
            toot_card_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_cardLinearLayout);
            card_TextView = itemView.findViewById(R.id.custom_menu_adapter_card_textView);
            card_ImageView = itemView.findViewById(R.id.custom_menu_adapter_card_imageView);
            toot_reblog_LinearLayout = itemView.findViewById(R.id.custom_menu_adapter_reblogLinearLayout);
            reblog_avatar_ImageView = itemView.findViewById(R.id.custom_menu_adapter_reblog_avatar);
            reblog_user_TextView = itemView.findViewById(R.id.custom_menu_adapter_reblog_account);
            reblog_toot_text_TextView = itemView.findViewById(R.id.custom_menu_adapter_reblog_text);
            notification_type_TextView = new TextView(context);
            reaction_TextView = new TextView(context);
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
        //設定を取得
        AccessToken = pref_setting.getString("main_token", "");
        Instance = pref_setting.getString("main_instance", "");
        //Context
        context = viewHolder.toot_text_TextView.getContext();

        //レイアウト
        ArrayList<String> item = itemList.get(i);
        //JSONパース用クラス
        MastodonTLAPIJSONParse api = new MastodonTLAPIJSONParse(viewHolder.toot_text_TextView.getContext(), item.get(3));
        //カスタム絵文字
        PicassoImageGetter toot_ImageGetter = new PicassoImageGetter(viewHolder.toot_text_TextView);
        PicassoImageGetter user_ImageGetter = new PicassoImageGetter(viewHolder.toot_user_TextView);
        //SetText
        viewHolder.toot_text_TextView.setText(Html.fromHtml(api.getToot_text(), Html.FROM_HTML_MODE_COMPACT, toot_ImageGetter, null));
        viewHolder.toot_user_TextView.setText(Html.fromHtml(api.getDisplay_name(), Html.FROM_HTML_MODE_COMPACT, user_ImageGetter, null));
        viewHolder.toot_user_TextView.append("@" + api.getAcct());
        viewHolder.toot_createAt_TextView.setText(api.getCreatedAt());
        viewHolder.toot_client_TextView.setText(api.getClient());
        viewHolder.toot_visibility_TextView.setText(api.getVisibility());

        //Misskey
        if (CustomMenuTimeLine.isMisskeyMode()) {
            //アバター画像
            loadAvatarImage(api, viewHolder);
            //Misskeyリアクション
            setMisskeyReaction(viewHolder, api, item);
            //Fav、BT済み、カウント数を出す
            setCountAndIconColor(viewHolder, api, item);
            //添付メディア
            showMedia(viewHolder, api);
            //card
            setCard(viewHolder, api);
            //ブースト
            setReBlogToot(viewHolder, api);
            //通知タイプ
            showNotificationType(viewHolder, api);
            //クライアント名のTextViewを消す
            setClientTextViewRemove(viewHolder);
        } else {
            //アバター画像
            loadAvatarImage(api, viewHolder);
            //BT、Favできるようにする
            setStatusClick(viewHolder.toot_boost_TextView, "bt_only", api, item);
            setStatusClick(viewHolder.toot_favourite_TextView, "fav_only", api, item);
            //Fav+BTできるように
            setPostBtFav(viewHolder, api, item);
            //Fav、BT済み、カウント数を出す
            setCountAndIconColor(viewHolder, api, item);
            //添付メディア
            showMedia(viewHolder, api);
            //card
            setCard(viewHolder, api);
            //ブースト
            setReBlogToot(viewHolder, api);
            //通知タイプ
            showNotificationType(viewHolder, api);
            //クイックプロフィール
            showQuickProfile(viewHolder.toot_avatar_ImageView, api.getUser_ID(), viewHolder);
            //クライアント名のTextViewを消す
            setClientTextViewRemove(viewHolder);
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * 画像を読み込むかどうか（Wi-Fi接続時のみとか
     */
    private boolean getLoadImageConnection(ViewHolder viewHolder) {
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
        if (CustomMenuTimeLine.isImageShow()) {
            mode = true;
        }

        return mode;
    }


    /**
     * 画像表示とか
     */
    private void loadAvatarImage(MastodonTLAPIJSONParse api, ViewHolder viewHolder) {
        //画像
        if (getLoadImageConnection(viewHolder)) {
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
     * Boost,Favourite
     *
     * @param type favかbtか
     */
    private void setStatusClick(TextView textView, String type, MastodonTLAPIJSONParse api, ArrayList<String> item) {
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
                Snackbar.make(v, message, Snackbar.LENGTH_LONG).setAction(button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //実行
                        TootAction(api.getToot_ID(), finalApiUrl, textView, api, item);
                    }
                }).show();
            }
        });
    }

    /**
     * Favourite and Boost
     */
    private void setPostBtFav(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item) {
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
                Snackbar.make(v, message, Snackbar.LENGTH_LONG).setAction(button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //実行
                        //Fab+BTモード以外
                        TootAction(api.getToot_ID(), "favourite", viewHolder.toot_favourite_TextView, api, item);
                        TootAction(api.getToot_ID(), "reblog", viewHolder.toot_favourite_TextView, api, item);
                    }
                }).show();
                //OnClickListener呼ばれないようにする
                return true;
            }
        });
    }

    /**
     * Fav、BT済み、カウント数を入れる
     */
    private void setCountAndIconColor(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item) {
        viewHolder.toot_boost_TextView.setText(api.getBTCount());
        //りぶろぐした、もしくは押した
        if (api.getIsBT().contains("true") || item.get(4).contains("true")) {
            Drawable boostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_repeat_black_24dp_2, null);
            boostIcon.setTint(Color.parseColor("#008000"));
            viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
        } else {
            Drawable boostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_repeat_black_24dp, null);
            viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
        }
        //ふぁぼ
        //Mastodonでは使わない
        if (!CustomMenuTimeLine.isMisskeyMode()) {
            //ふぁぼした、もしくはふぁぼ押した
            if (api.getIsFav().contains("true") || item.get(5).contains("true")) {
                Drawable favIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_star_black_24dp_1, null);
                favIcon.setTint(Color.parseColor("#ffd700"));
                viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
            } else {
                Drawable favIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_star_black_24dp, null);
                viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
            }
        } else {
            viewHolder.toot_favourite_TextView.setText(HomeTimeLineAdapter.toReactionEmoji(api.getIsFav()));
            viewHolder.mainLinearLayout.removeView(viewHolder.reaction_TextView);
            viewHolder.reaction_TextView.setText(api.getFavCount());
            viewHolder.reaction_TextView.setTextSize(10);
            viewHolder.mainLinearLayout.addView(viewHolder.reaction_TextView, 2);
        }
    }

    /**
     * 画像表示
     */
    private void showMedia(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        //消す
        viewHolder.toot_media_LinearLayout.removeAllViews();
        //画像を表示してもよいか？
        if (getLoadImageConnection(viewHolder)) {
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
            parent_LinearLayout.removeView(linearLayout);
        }
    }

    /**
     * Card実装する
     */
    private void setCard(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        viewHolder.toot_card_LinearLayout.removeAllViews();
        if (api.getCardTitle() != null) {
            //getLayoutInflaterが使えないので
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //Cardのレイアウト適用
            layoutInflater.inflate(R.layout.custom_menu_recycler_adapter_card_layout, viewHolder.toot_card_LinearLayout);
            viewHolder.card_ImageView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_card_imageView);
            viewHolder.card_TextView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_card_textView);
            if (getLoadImageConnection(viewHolder)) {
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
    private void setReBlogToot(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
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
            //入れる
            if (getLoadImageConnection(viewHolder)) {
                //既定でGIFは再生しない方向で
                if (pref_setting.getBoolean("pref_avater_gif", true)) {
                    //GIFアニメ再生させない
                    Glide.with(context).load(api.getBTAvatarUrlNotGif()).into(viewHolder.reblog_avatar_ImageView);
                } else {
                    //GIFアニメを再生
                    Glide.with(context).load(api.getBTAvatarUrl()).into(viewHolder.reblog_avatar_ImageView);
                }
            } else {
                ((LinearLayout) viewHolder.reblog_avatar_ImageView.getParent()).removeView(viewHolder.reblog_avatar_ImageView);
            }
            PicassoImageGetter toot_ImageGetter = new PicassoImageGetter(viewHolder.reblog_toot_text_TextView);
            PicassoImageGetter user_ImageGetter = new PicassoImageGetter(viewHolder.reblog_user_TextView);
            viewHolder.reblog_user_TextView.setText(Html.fromHtml(api.getBTAccountDisplayName(), Html.FROM_HTML_MODE_COMPACT, toot_ImageGetter, null));
            viewHolder.reblog_toot_text_TextView.setText(Html.fromHtml(api.getBTTootText(), Html.FROM_HTML_MODE_COMPACT, user_ImageGetter, null));
            viewHolder.reblog_user_TextView.append("@" + api.getBTAccountAcct());
            //～～がブーストしましたを出す
            viewHolder.toot_user_TextView.append(context.getString(R.string.reblog));
            viewHolder.toot_text_TextView.setText("");
            Drawable drawable = context.getDrawable(R.drawable.ic_repeat_black_24dp_2);
            drawable.setTint(Color.parseColor("#008000"));
            viewHolder.toot_user_TextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            //クイックプロフィール
            showQuickProfile(viewHolder.reblog_avatar_ImageView, api.getBTAccountID(), viewHolder);
        }
    }

    /**
     * 通知タイプ
     */
    private void showNotificationType(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        //リアクションを出す
        viewHolder.mainLinearLayout.removeView(viewHolder.notification_type_TextView);
        if (api.getNotification_Type() != null) {
            viewHolder.notification_type_TextView.setText(toNotificationType(api.getNotification_Type()));
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
    private String toNotificationType(String type) {
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
        }
        return type;
    }


    /**
     * BT,FavのAPI
     */
    private void TootAction(String id, String endPoint, TextView textView, MastodonTLAPIJSONParse api, ArrayList<String> item) {
        String url = "https:" + Instance + "/api/v1/statuses/" + id + "/" + endPoint + "/?access_token=" + AccessToken;
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
                textView.post(new Runnable() {
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
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    //UI Thread
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (endPoint.contains("reblog")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.boost_ok) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable boostIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                                boostIcon.setTint(Color.parseColor("#008000"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
                                //BTしたぜ！
                                item.set(4, "true");
                            }
                            if (endPoint.contains("favourite")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.favourite_add) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                                favIcon.setTint(Color.parseColor("#ffd700"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                //Favしたぜ！
                                item.set(5, "true");
                            }
                            if (endPoint.contains("unfavourite")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.delete_fav_toast) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                                favIcon.setTint(Color.parseColor("#000000"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                //Favしたぜ！
                                item.set(5, "false");
                            }
                            if (endPoint.contains("unreblog")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.delete_bt_toast) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                                favIcon.setTint(Color.parseColor("#000000"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                //BTしたぜ！
                                item.set(4, "false");
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Misskey リアクション
     */
    private void setMisskeyReaction(ViewHolder viewHolder, MastodonTLAPIJSONParse api, ArrayList<String> item) {
        //アイコン変更
        viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(context.getDrawable(R.drawable.ic_add_black_24dp), null, null, null);
        viewHolder.toot_favourite_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_INDEFINITE);
                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                //TextViewを非表示にする
                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
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
                            if (pref_setting.getBoolean("pref_nicoru_dialog", true) && !CustomMenuTimeLine.isDialogNotShow()) {
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
                        if (pref_setting.getBoolean("pref_nicoru_dialog", true) && !CustomMenuTimeLine.isDialogNotShow()) {
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
    private void showMisskeyQuickProfile(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        viewHolder.toot_favourite_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String instance = pref_setting.getString("misskey_main_instance", "");
                String token = pref_setting.getString("misskey_main_token", "");
                String username = pref_setting.getString("misskey_main_username", "");
                String url = "https://" + instance + "/api/users/show";
                //読み込み中お知らせ
                Snackbar snackbar = Snackbar.make(v, context.getString(R.string.loading_user_info) + "\r\n" + url, Snackbar.LENGTH_INDEFINITE);
                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                //SnackBerを複数行対応させる
                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
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
                    jsonObject.put("userId", api.getUser_ID());
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
                                if (pref_setting.getBoolean("pref_custom_emoji", true) || CustomMenuTimeLine.isUseCustomEmoji()) {
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
                                ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                                LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                progressBer_layoutParams.gravity = Gravity.CENTER;
                                //SnackBerを複数行対応させる
                                TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
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
                                        intent.putExtra("Account_ID", api.getUser_ID());
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
    private void showQuickProfile(ImageView imageView, String id, ViewHolder viewHolder) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //設定・カスタムメニュー
                if (pref_setting.getBoolean("pref_quick_profile", false) || CustomMenuTimeLine.isQuickProfile()) {
                    //読み込み中お知らせ
                    Snackbar snackbar = Snackbar.make(v, context.getString(R.string.loading_user_info) + "\r\n /api/v1/accounts/" + id, Snackbar.LENGTH_INDEFINITE);
                    ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                    //SnackBerを複数行対応させる
                    TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
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
                                if (pref_setting.getBoolean("pref_custom_emoji", true) || CustomMenuTimeLine.isUseCustomEmoji()) {
                                    if (getLoadImageConnection(viewHolder)) {
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
                                                    ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                                                    LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                    progressBer_layoutParams.gravity = Gravity.CENTER;
                                                    //SnackBerを複数行対応させる
                                                    TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
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


}
