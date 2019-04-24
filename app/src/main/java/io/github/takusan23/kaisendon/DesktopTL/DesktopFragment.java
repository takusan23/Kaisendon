package io.github.takusan23.kaisendon.DesktopTL;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class DesktopFragment extends Fragment {

    private SharedPreferences pref_setting;
    private LinearLayout main_LinearLayout;
    private LinearLayout scrollview_LinearLayout;
    private String access_token;
    private String instance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_desktop, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        main_LinearLayout = view.findViewById(R.id.descktop_parent_linearlayout);
        scrollview_LinearLayout = view.findViewById(R.id.scrollview_linearlayout);

        instance = pref_setting.getString("main_instance", "");
        access_token = pref_setting.getString("main_token", "");

        getActivity().setTitle(getString(R.string.desktop_mode_depelopment));

        //とりあえず2つ
        String[] list = {"https://" + instance + "/api/v1/timelines/home","https://" + instance + "/api/v1/timelines/public?local=true","https://" + instance + "/api/v1/timelines/public"};
        String[] name = {"Home","Local","Federated"};
        for (int i = 0; i < 3; i++) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            scrollview_LinearLayout.addView(linearLayout);
            getLayoutInflater().inflate(R.layout.desktop_tl_column_layout, linearLayout);
            RecyclerView recyclerView = linearLayout.findViewById(R.id.desktop_tl_column_recyclerview);
            TextView title_TextView = linearLayout.findViewById(R.id.desktop_tl_column_title_textview);
            title_TextView.setText(name[i]);
            //ここから下三行必須
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(mLayoutManager);
            ArrayList<ArrayList> recyclerViewList = new ArrayList<>();
            CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
            loadTimeline(list[i], recyclerViewList, customMenuRecyclerViewAdapter, recyclerView);
        }
    }

    /**
     * TL読み込み
     */
    private void loadTimeline(String url, ArrayList<ArrayList> recyclerViewList, CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter, RecyclerView recyclerView) {
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
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

                            if (getActivity() != null && isAdded()) {

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

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        RecyclerView.LayoutManager recyclerViewLayoutManager = recyclerView.getLayoutManager();
                                        //CustomMenuRecyclerViewAdapter customMenuRecyclerViewAdapter = new CustomMenuRecyclerViewAdapter(recyclerViewList);
                                        recyclerView.setAdapter(customMenuRecyclerViewAdapter);
                                        SnackberProgress.closeProgressSnackber();
                                    }
                                });
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //失敗時
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), R.string.error + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}