package io.github.takusan23.kaisendon.CustomMenu;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView toot_text_TextView;
        public TextView toot_user_TextView;
        public ImageView toot_avatar_ImageView;
        public TextView toot_boost_TextView;
        public TextView toot_favourite_TextView;
        public TextView toot_bookmark_TextView;
        public TextView toot_client_TextView;
        public TextView toot_createAt_TextView;
        public TextView toot_visibility_TextView;

        public ViewHolder(@NonNull View itemView) {
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
        }
    }

    public CustomMenuRecyclerViewAdapter(ArrayList<ArrayList> arrayList) {
        itemList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_menu_recyclerview_adapter_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
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
        MastodonTLAPIJSONParse api = new MastodonTLAPIJSONParse(viewHolder.toot_text_TextView.getContext(),item.get(3),true);
        //カスタム絵文字
        PicassoImageGetter toot_ImageGetter = new PicassoImageGetter(viewHolder.toot_text_TextView);
        PicassoImageGetter user_ImageGetter = new PicassoImageGetter(viewHolder.toot_user_TextView);
        //SetText
        viewHolder.toot_text_TextView.setText(Html.fromHtml(api.getToot_text(), Html.FROM_HTML_MODE_COMPACT, toot_ImageGetter, null));
        viewHolder.toot_user_TextView.setText(Html.fromHtml(api.getDisplay_name(), Html.FROM_HTML_MODE_COMPACT, user_ImageGetter, null));
        viewHolder.toot_user_TextView.append("@"+api.getAcct());
        viewHolder.toot_createAt_TextView.setText(api.getCreatedAt());
        viewHolder.toot_client_TextView.setText(api.getClient());
        viewHolder.toot_visibility_TextView.setText(api.getVisibility());

        //アバター画像
        loadAvatarImage(api, viewHolder);
        //BT,Fav等
        //setBoostFavClick(viewHolder, item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }



    /**
     * 画像表示とか
     */
    private void loadAvatarImage(MastodonTLAPIJSONParse api, ViewHolder viewHolder) {
        //画像
        ConnectivityManager connectivityManager = (ConnectivityManager) viewHolder.toot_text_TextView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (pref_setting.getBoolean("pref_avater_wifi", true)) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                //既定でGIFは再生しない方向で
                if (pref_setting.getBoolean("pref_avater_gif", true)) {
                    //GIFアニメ再生させない
                    Glide.with(viewHolder.toot_avatar_ImageView.getContext()).load(api.getAvatarUrlNotGIF()).into(viewHolder.toot_avatar_ImageView);
                } else {
                    //GIFアニメを再生
                    Glide.with(viewHolder.toot_avatar_ImageView.getContext()).load(api.getAvatarUrl()).into(viewHolder.toot_avatar_ImageView);
                }
            }
        } else {
            //既定でGIFは再生しない方向で
            if (pref_setting.getBoolean("pref_avater_gif", true)) {
                //GIFアニメ再生させない
                Glide.with(viewHolder.toot_avatar_ImageView.getContext()).load(api.getAvatarUrlNotGIF()).into(viewHolder.toot_avatar_ImageView);
            } else {
                //GIFアニメを再生
                Glide.with(viewHolder.toot_avatar_ImageView.getContext()).load(api.getAvatarUrl()).into(viewHolder.toot_avatar_ImageView);
            }
        }
    }

    /**
     * Boost,Favourite
     * @param type favかbtか
     */
    private void setClickPOSTStatus(TextView textView,String type,MastodonTLAPIJSONParse api){
        //クリックイベント
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackberのテキスト
                String message = "";
                String button = "";
                //Fav/BT
                if (type.contains("fav")){
                    //Fav済みか
                    if (Boolean.valueOf(api.getIsFav())){
                        //Fav済み
                        message = context.getString(R.string.delete_fav);
                        button = context.getString(R.string.delete_ok);
                    }else{
                        //Favする
                        message = context.getString(R.string.favourite_add_message);
                        button = context.getString(R.string.favoutire);
                    }
                }else {
                    //Fav済みか
                    if (Boolean.valueOf(api.getIsBT())){
                        //BT済み


                    }else{
                        //BTする


                    }
                }
            }
        });
    }

    /**
     * BT,FavのAPI
     */
    private void TootAction(String id, String endPoint, TextView textView) {
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
                            }
                            if (endPoint.contains("favourite")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.favourite_add) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                                favIcon.setTint(Color.parseColor("#ffd700"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                            }
                            if (endPoint.contains("unfavourite")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.delete_fav_toast) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_star_black_24dp_1, null);
                                favIcon.setTint(Color.parseColor("#000000"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                            }
                            if (endPoint.contains("unreblog")) {
                                Toast.makeText(textView.getContext(), textView.getContext().getString(R.string.delete_bt_toast) + " : " + id, Toast.LENGTH_SHORT).show();
                                Drawable favIcon = ResourcesCompat.getDrawable(textView.getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                                favIcon.setTint(Color.parseColor("#000000"));
                                textView.setCompoundDrawablesWithIntrinsicBounds(favIcon, null, null, null);
                            }
                        }
                    });
                }
            }
        });
    }


}