package io.github.takusan23.kaisendon;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    View view;
    String toot_time;

    String toot;
    String user;
    String user_name;
    long toot_id;
    String toot_id_string;
    long account_id;
    String user_avater_url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_search, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //find
        SearchView searchView = view.findViewById(R.id.search_searchView);
        ListView searchListView = view.findViewById(R.id.search_listView);
        TextView searchTextView = view.findViewById(R.id.search_textview);

        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Twitter調子悪い？");

        searchTextView.setText("\r\n" + "Powered by tootsearch  https://tootsearch.chotto.moe/");

        getActivity().setTitle("tootsearch");


        //背景
        ImageView background_imageView = view.findViewById(R.id.tootsearch_background_imageview);

        if (pref_setting.getBoolean("background_image", true)) {
            Uri uri = Uri.parse(pref_setting.getString("background_image_path", ""));
            Glide.with(getContext())
                    .load(uri)
                    .into(background_imageView);
        }


        //画面に合わせる
        if (pref_setting.getBoolean("background_fit_image", false)) {
            //有効
            background_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        //透明度
        if (pref_setting.getFloat("transparency", 1.0f) != 0.0){
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        //くるくる
        //ProgressDialog API 26から非推奨に
        Snackbar snackbar = Snackbar.make(view, getString(R.string.loading_tootserch)+" : " +"\r\n Powered by tootsearch", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(getContext());
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar,0);


        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //エンターキーを押した時（決定？
            @Override
            public boolean onQueryTextSubmit(String query) {

                //Toast.makeText(getContext(),searchView.getQuery().toString(),Toast.LENGTH_SHORT).show();

                //検索する！！！！
                AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
                    @Override
                    protected String doInBackground(String... string) {

                        toot_list.clear();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                searchTextView.setText("\r\n" + "Powered by tootsearch  https://tootsearch.chotto.moe/");
                                snackbar.show();
                            }
                        });

                        //もういい！okhttpで実装する！！
                        String url = "https://tootsearch.chotto.moe/api/v1/search";
                        //パラメータを設定
                        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
                        builder.addQueryParameter("q", searchView.getQuery().toString());
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
                                //ListViewクリア
                                //adapter.clear();
                                //searchListView.setAdapter(adapter);

                                //JSON化
                                String response_string = response.body().string();
                                //System.out.println("レスポンス : " + response_string);
                                try {
                                    JSONObject jsonObject = new JSONObject(response_string);

                                    String hit = jsonObject.getJSONObject("hits").getString("total");

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            searchTextView.setText("約　" + hit + "件" + "\r\n" + "Powered by tootsearch  https://tootsearch.chotto.moe/");
                                        }
                                    });

                                    JSONArray toot_list = jsonObject.getJSONObject("hits").getJSONArray("hits");

                                    for (int i = 0; i < toot_list.length(); i++) {
                                        JSONObject toot_text = toot_list.getJSONObject(i);
                                        JSONObject toot_source = toot_text.getJSONObject("_source");
                                        JSONObject toot_account = toot_source.getJSONObject("account");
                                        //内容

                                        toot = toot_source.getString("content");
                                        user = toot_account.getString("acct");
                                        user_name = toot_account.getString("display_name");
                                        toot_id = toot_source.getLong("id");
                                        toot_id_string = String.valueOf(toot_id);
                                        account_id = toot_account.getLong("id");
                                        user_avater_url = toot_account.getString("avatar");


                                        boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                                        if (japan_timeSetting) {
                                            //時差計算？
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                            //日本用フォーマット
                                            SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text","yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                            try {
                                                Date date = simpleDateFormat.parse(toot_source.getString("created_at"));
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTime(date);
                                                //9時間足して日本時間へ
                                                calendar.add(Calendar.HOUR, + Integer.valueOf(pref_setting.getString("pref_time_add","9")));
                                                //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                                toot_time = japanDateFormat.format(calendar.getTime());
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            toot_time = toot_source.getString("created_at");
                                        }

                                        ListItem listItem = new ListItem(null, toot, user_name + " @" + user, "クライアント : " + "null" + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, null,null,null,null,null);

                                        //通知が行くように
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.add(listItem);
                                                adapter.notifyDataSetChanged();
                                            }
                                        });

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ListView listView = (ListView) view.findViewById(R.id.search_listView);
                                                listView.setAdapter(adapter);
                                                snackbar.dismiss();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        return null;
                    }
                    @Override
                    protected void onPostExecute(String result) {

                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                return false;
            }

            //入力されるたびに呼び出される
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }
}
