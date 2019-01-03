package io.github.takusan23.kaisendon;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Follow_Suggestions_Fragment extends Fragment {

    String display_name = null;
    String account_id_string = null;
    String avater_url = null;
    long account_id;

    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.activity_home_timeline, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        String AccessToken = null;
        String Instance = null;

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)){
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }


        //背景
        ImageView background_imageView = view.findViewById(R.id.hometimeline_background_imageview);

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
        if (pref_setting.getFloat("transparency", 1.0f) != 0.1){
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        //くるくる
/*
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("フォロー推奨ユーザー取得中 \r\n /api/v1/suggestions");
        dialog.show();
*/
        //くるくる
        //ProgressDialog API 26から非推奨に
        Snackbar snackbar = Snackbar.make(view, getString(R.string.loading_suggestions)+"\r\n /api/v1/suggestions", Snackbar.LENGTH_INDEFINITE);
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
        snackbar.show();


        ArrayList<ListItem> toot_list = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(getContext(), R.layout.timeline_item, toot_list);

        getActivity().setTitle(getString(R.string.follow_suggestions));

        //非同期通信
        String finalInstance = Instance;
        String finalAccessToken = AccessToken;
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {


                URL url = null;
                HttpURLConnection connection = null;
                String url_link = "https://" + finalInstance + "/api/v1/suggestions/?stream=user&access_token=" + finalAccessToken;

                try {

                    url = new URL(url_link);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    final InputStream in = connection.getInputStream();
                    String encoding = connection.getContentEncoding();
                    if (null == encoding) {
                        encoding = "UTF-8";
                    }
                    final InputStreamReader inReader = new InputStreamReader(in, encoding);
                    final BufferedReader bufReader = new BufferedReader(inReader);
                    StringBuilder response = new StringBuilder();
                    String line = null;
                    // 1行ずつ読み込む
                    while ((line = bufReader.readLine()) != null) {
                        response.append(line);
                    }
                    bufReader.close();
                    inReader.close();
                    in.close();

                    // 受け取ったJSON文字列をパース
                    //Json嫌いだあああ
                    JSONArray datas = new JSONArray(response.toString());
                    int count = 0;
                    while (count <= datas.length()) {
                        //Status
                        JSONObject stats = datas.getJSONObject(count);

                        long account_id = stats.getLong("id");
                        String display_name = stats.getString("display_name");
                        String account_id_string = stats.getString("acct");
                        String profile = stats.getString("note");
                        String avater_url = stats.getString("avatar");

                        System.out.println(display_name + "/" + account_id_string);

                        if (getActivity() != null){
                            ListItem listItem = new ListItem(null, profile, display_name + " @" + account_id_string, null, null, avater_url, account_id, display_name, null,null,null,null,null);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(listItem);
                                    adapter.notifyDataSetChanged();
                                    ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                    listView.setAdapter(adapter);

                                }
                            });
                        }

                        count++;
                    }

                    return null;

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {
                //くるくるを終了
                //dialog.dismiss();
                snackbar.dismiss();
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //引っ張って更新するやつ
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                dialog.show();
                toot_list.clear();
                AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... string) {


                        URL url = null;
                        HttpURLConnection connection = null;
                        String url_link = "https://" + finalInstance + "/api/v1/suggestions/?stream=user&access_token=" + finalAccessToken;

                        try {

                            url = new URL(url_link);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.connect();

                            final InputStream in = connection.getInputStream();
                            String encoding = connection.getContentEncoding();
                            if (null == encoding) {
                                encoding = "UTF-8";
                            }
                            final InputStreamReader inReader = new InputStreamReader(in, encoding);
                            final BufferedReader bufReader = new BufferedReader(inReader);
                            StringBuilder response = new StringBuilder();
                            String line = null;
                            // 1行ずつ読み込む
                            while ((line = bufReader.readLine()) != null) {
                                response.append(line);
                            }
                            bufReader.close();
                            inReader.close();
                            in.close();

                            // 受け取ったJSON文字列をパース
                            int count = 0;
                            while (count <= 40) {
                                //Json嫌いだあああ
                                JSONArray datas = new JSONArray(response.toString());
                                //Status
                                JSONObject stats = datas.getJSONObject(count);

                                long account_id = stats.getLong("id");
                                String display_name = stats.getString("display_name");
                                String account_id_string = stats.getString("acct");
                                String profile = stats.getString("note");
                                String avater_url = stats.getString("avatar");

                                //System.out.println(display_name + "/" + account_id_string);

                                if (getActivity() != null){
                                    ListItem listItem = new ListItem(null, profile, display_name + " @" + account_id_string, null, null, avater_url, account_id, display_name, null,null,null,null,null);

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.add(listItem);
                                            adapter.notifyDataSetChanged();
                                            ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                            listView.setAdapter(adapter);

                                        }
                                    });
                                }


                                count++;
                            }

                            return null;

                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return null;
                        } finally {
                            if (connection != null) {
                                connection.disconnect();
                            }
                        }
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        //くるくるを終了
                        //dialog.dismiss();
                        snackbar.dismiss();
                    }

                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });


    }

}
