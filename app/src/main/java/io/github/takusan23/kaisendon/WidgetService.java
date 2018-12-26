package io.github.takusan23.kaisendon;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WidgetService extends RemoteViewsService {

    String AccessToken, Instance;

    private JSONArray jsonArray = new JSONArray();

    private static final String TAG = "WidgetTest";


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetFactory();
    }

    private class WidgetFactory implements RemoteViewsFactory {

        SharedPreferences pref_setting;

        public void onCreate() {
            Log.v(TAG, "[onCreate]");

        }

        public void onDataSetChanged() {
            Log.v(TAG, "[onDataSetChanged]");
            //タイムライン読み込み
            getTimeLineJson();
        }

        public void onDestroy() {
            Log.v(TAG, "[onDestroy]");
        }

        public RemoteViews getViewAt(int position) {
            Log.e(TAG, "[getViewAt]: " + position);

            //ここでListViewに追加する
            RemoteViews remoteViews = null;
            remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_listview_layout);
            pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());
            //URL
            String toot_url = null;
            //画像を表示するかの判断]
            boolean avater_show = false;
            //通信量節約
            boolean setting_avater_hidden = pref_setting.getBoolean("pref_avater", false);
            boolean setting_avater_wifi = pref_setting.getBoolean("pref_avater_wifi", true);
            //Wi-Fi接続状況確認
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            //Wi-Fi
            if (setting_avater_wifi) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    avater_show = true;
                }
                //Wi-Fi no Connection
                else {
                    avater_show = false;
                }
            } else {
                avater_show = false;
            }


            //ここで追加
            try {
                //Htmlなんとか～はJSONの中のトゥートにHTMLタグがついてるため
                if (pref_setting.getString("WidgetTLType", "Home").contains("Notification")) {
                    String type = jsonArray.getJSONObject(position).getString("type");
                    String content = jsonArray.getJSONObject(position).getJSONObject("status").getString("content");
                    String account = jsonArray.getJSONObject(position).getJSONObject("account").getString("acct");
                    String display_name = jsonArray.getJSONObject(position).getJSONObject("account").getString("display_name");
                    String avater_url = jsonArray.getJSONObject(position).getJSONObject("account").getString("avatar");
                    toot_url = jsonArray.getJSONObject(position).getJSONObject("status").getString("url");

                    remoteViews.setTextViewText(R.id.widget_listview_item_linearLayout, Html.fromHtml(type + "\r\n" + display_name + " / @" + account + "\r\n" + content, Html.FROM_HTML_MODE_COMPACT));

                    //Glideは神！！！！！！！！！！！！！！！！！！！！！！！！！！！
                    if (avater_show) {
                        try {
                            Bitmap bitmap = Glide.with(getApplicationContext()).asBitmap().load(avater_url).submit(100, 100).get();
                            remoteViews.setImageViewBitmap(R.id.widget_listview_layout_imageview, bitmap);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    String content = jsonArray.getJSONObject(position).getString("content");
                    String account = jsonArray.getJSONObject(position).getJSONObject("account").getString("acct");
                    String display_name = jsonArray.getJSONObject(position).getJSONObject("account").getString("display_name");
                    String avater_url = jsonArray.getJSONObject(position).getJSONObject("account").getString("avatar");
                    toot_url = jsonArray.getJSONObject(position).getString("url");

                    remoteViews.setTextViewText(R.id.widget_listview_layout_textview, Html.fromHtml(display_name + " / @" + account + "\r\n" + content, Html.FROM_HTML_MODE_COMPACT));

                    //Glideは神！！！！！！！！！！！！！！！！！！！！！！！！！！！
                    if (avater_show) {
                        try {
                            Bitmap bitmap = Glide.with(getApplicationContext()).asBitmap().load(avater_url).submit(100, 100).get();
                            remoteViews.setImageViewBitmap(R.id.widget_listview_layout_imageview, bitmap);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }


                //ListViewの項目をクリックできるようにする
                Intent btnClickIntent = new Intent(getApplicationContext(),NewAppWidget.class);
                btnClickIntent.putExtra("URL",toot_url);
                btnClickIntent.putExtra("ListViewClick", true);

                remoteViews.setOnClickFillInIntent(R.id.widget_listview_layout_textview,btnClickIntent);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return remoteViews;
        }

        public long getItemId(int position) {
            Log.v(TAG, "[getItemId]: " + position);

            return 0;
        }

        public int getCount() {
            Log.v(TAG, "[getCount]");

            //returnにListViewの合計数を入れる
            //今回はJSONの配列の数
            return jsonArray.length();
        }

        public RemoteViews getLoadingView() {
            Log.v(TAG, "[getLoadingView]");

            return null;
        }


        public int getViewTypeCount() {
            Log.v(TAG, "[getViewTypeCount]");

            return 1;
        }

        public boolean hasStableIds() {
            Log.v(TAG, "[hasStableIds]");

            return true;
        }


        //JSON取得
        public void getTimeLineJson() {
            Log.v(TAG, "[Request]");

            pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

            boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
            if (accessToken_boomelan) {
                AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
                Instance = pref_setting.getString("pref_mastodon_instance", "");
            } else {
                AccessToken = pref_setting.getString("main_token", "");
                Instance = pref_setting.getString("main_instance", "");
            }

            //URL設定
            String url = "https://" + Instance + "/api/v1/timelines/public/?local=true";

            if (pref_setting.getString("WidgetTLType", "Home").contains("Home")) {
                url = "https://" + Instance + "/api/v1/timelines/home/?access_token=" + AccessToken;
            }
            if (pref_setting.getString("WidgetTLType", "Home").contains("Local")) {
                url = "https://" + Instance + "/api/v1/timelines/public/?local=true";
            }
            if (pref_setting.getString("WidgetTLType", "Home").contains("Notification")) {
                url = "https://" + Instance + "/api/v1/notifications?access_token=" + AccessToken;
            }
            if (pref_setting.getString("WidgetTLType", "Home").contains("Federated")) {
                url = "https://" + Instance + "/api/v1/timelines/public/";
            }

            //パラメータを設定
            HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
            builder.addQueryParameter("limit", "40");
            String final_url = builder.build().toString();

            //作成
            Request request = new Request.Builder()
                    .url(final_url)
                    .get()
                    .build();

            //GETリクエスト
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responce_string = response.body().string();
                    //System.out.println(responce_string);
                    try {
                        jsonArray = new JSONArray(responce_string);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

    }


}