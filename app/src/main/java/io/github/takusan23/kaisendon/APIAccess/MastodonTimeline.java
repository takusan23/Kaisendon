package io.github.takusan23.kaisendon.APIAccess;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.CustomMenu.CustomMenuRecyclerViewAdapter;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.SnackberProgress;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MastodonTimeline {
    private Context context;
    private SharedPreferences pref_setting;

    public MastodonTimeline(String url, String instance, String access_token, ArrayList<ArrayList> recyclerViewList, CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter, RecyclerView recyclerView) {
        context = recyclerView.getContext();
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        getMastodonTimeline(url, instance, access_token, recyclerViewList, customMenuRecyclerViewAdapter, recyclerView);
    }

    public void getMastodonTimeline(String api_url, String instance, String access_token, ArrayList<ArrayList> recyclerViewList, CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter, RecyclerView recyclerView) {
        Handler handler = new Handler(Looper.getMainLooper());
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(api_url).newBuilder();
        builder.addQueryParameter("limit", "40");
        builder.addQueryParameter("access_token", access_token);
        String max_id_final_url = builder.build().toString();

        //作成
        Request request = new Request.Builder()
                .url(max_id_final_url)
                .get()
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //成功時
                if (response.isSuccessful()) {
                    String response_string = response.body().string();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(response_string);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                            //配列を作成
                            ArrayList<String> Item = new ArrayList<>();
                            //メモとか通知とかに
                            Item.add("CustomMenu Local");
                            //内容
                            Item.add("");
                            //ユーザー名
                            Item.add("");
                            //JSONObject
                            Item.add(toot_jsonObject.toString());
                            //ぶーすとした？
                            Item.add("false");
                            //ふぁぼした？
                            Item.add("false");

                            //ListItem listItem = new ListItem(Item);
                            recyclerViewList.add(Item);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    RecyclerView.LayoutManager recyclerViewLayoutManager = recyclerView.getLayoutManager();
                                    //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                                    recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                                    SnackberProgress.closeProgressSnackber();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //失敗時
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.error + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

}
