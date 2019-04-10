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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.APIJSONParse.MastodonTLAPIJSONParse;
import io.github.takusan23.kaisendon.PicassoImageGetter;
import io.github.takusan23.kaisendon.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
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
        //画像
        public ImageView media_ImageView_1;
        public ImageView media_ImageView_2;
        public ImageView media_ImageView_3;
        public ImageView media_ImageView_4;
        //card
        public LinearLayout toot_card_LinearLayout;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
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
        MastodonTLAPIJSONParse api = new MastodonTLAPIJSONParse(viewHolder.toot_text_TextView.getContext(), item.get(3), CustomMenuTimeLine.isUseCustomEmoji());
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

        //アバター画像
        loadAvatarImage(api, viewHolder);
        //BT、Favできるようにする
        setStatusClick(viewHolder.toot_boost_TextView, "bt", api);
        setStatusClick(viewHolder.toot_favourite_TextView, "fav", api);
        //Fav、BT済み、カウント数を出す
        setCountAndIconColor(viewHolder, api);
        //添付メディア
        showMedia(viewHolder, api);
        //card
        setCard(viewHolder, api);

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
            ((LinearLayout) viewHolder.toot_avatar_ImageView.getParent()).removeView(viewHolder.toot_avatar_ImageView);
        }
    }

    /**
     * Boost,Favourite
     *
     * @param type favかbtか
     */
    private void setStatusClick(TextView textView, String type, MastodonTLAPIJSONParse api) {
        //クリックイベント
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackberのテキスト
                String message = "";
                String button = "";
                String apiUrl = "favourite";
                //Fav/BT/Fav+BT
                if (type.contains("fav")) {
                    //Fav済みか
                    if (Boolean.valueOf(api.getIsFav())) {
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
                } else if (type.contains("bt")) {
                    //Fav済みか
                    if (Boolean.valueOf(api.getIsBT())) {
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
                } else {
                    message = context.getString(R.string.favAndBT);
                    button = "Fab+BT";
                }
                //SnackBer生成
                String finalApiUrl = apiUrl;
                String finalButton = button;
                Snackbar.make(v, message, Snackbar.LENGTH_LONG).setAction(button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //実行
                        //Fab+BTモード以外
                        if (!finalButton.contains("Fab+BT")) {
                            TootAction(api.getToot_ID(), finalApiUrl, textView, api);
                        } else {
                            TootAction(api.getToot_ID(), "favourite", textView, api);
                            TootAction(api.getToot_ID(), "reblog", textView, api);
                        }
                    }
                }).show();
            }
        });
    }

    /**
     * Fav、BT済み、カウント数を入れる
     */
    private void setCountAndIconColor(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        viewHolder.toot_favourite_TextView.setText(api.getFavCount());
        viewHolder.toot_boost_TextView.setText(api.getBTCount());
        //りぶろぐ
        if (Boolean.valueOf(api.getIsBT())) {
            Drawable boostIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_repeat_black_24dp_2, null);
            boostIcon.setTint(Color.parseColor("#008000"));
            viewHolder.toot_boost_TextView.setCompoundDrawablesWithIntrinsicBounds(boostIcon, null, null, null);
        }
        //ふぁぼ
        //Mastodonでは使わない
        if (CustomMenuTimeLine.isMisskeyMode()) {
            if (Boolean.valueOf(api.getIsFav())) {
                Drawable favIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_star_black_24dp_1, null);
                favIcon.setTint(Color.parseColor("#ffd700"));
                viewHolder.toot_favourite_TextView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
            }
        }
    }

    /**
     * 画像表示
     */
    private void showMedia(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        //消す
        viewHolder.toot_media_LinearLayout.removeAllViews();
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
     * */
    private void useCustomTabs(String url){
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
     * Card実装する
     */
    private void setCard(ViewHolder viewHolder, MastodonTLAPIJSONParse api) {
        viewHolder.toot_card_LinearLayout.removeAllViews();
        if (api.getCardTitle() != null) {
            //getLayoutInflaterが使えないので
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //Cardのレイアウト適用
            layoutInflater.inflate(R.layout.custom_menu_recycler_adapter_card_layout, viewHolder.toot_card_LinearLayout);
            TextView card_TextView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_card_textView);
            ImageView card_ImageView = viewHolder.itemView.findViewById(R.id.custom_menu_adapter_card_imageView);
            if (CustomMenuTimeLine.isImageShow()){
                Glide.with(card_ImageView.getContext()).load(api.getCardImage()).into(card_ImageView);
            }else{
                ((LinearLayout)card_ImageView.getParent()).removeView(card_ImageView);
            }
            card_TextView.setText(api.getCardTitle() + "\n");
            card_TextView.append(Html.fromHtml(api.getCardDescription(), Html.FROM_HTML_MODE_LEGACY));
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
     * BT,FavのAPI
     */
    private void TootAction(String id, String endPoint, TextView textView, MastodonTLAPIJSONParse api) {
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
                                api.setIsBT("true");
                            }
                            if (endPoint.contains("favourite")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.favourite_add) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                                favIcon.setTint(Color.parseColor("#ffd700"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                api.setIsFav("true");
                            }
                            if (endPoint.contains("unfavourite")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.delete_fav_toast) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                                favIcon.setTint(Color.parseColor("#000000"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                api.setIsFav("false");
                            }
                            if (endPoint.contains("unreblog")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.delete_bt_toast) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                                favIcon.setTint(Color.parseColor("#000000"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                                api.setIsBT("false");
                            }
                        }
                    });
                }
            }
        });
    }


}
