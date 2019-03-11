package io.github.takusan23.kaisendon.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Instance;
import com.sys1yagi.mastodon4j.api.entity.InstanceUrls;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import okhttp3.Connection;
import okhttp3.OkHttpClient;

public class InstanceInfo_Fragment extends Fragment {

    String instance_name = null;
    String instance_user = null;
    String instance_total_toot = null;
    String instance_description = null;


    View view;
    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.activity_instance_info, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());


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

//        getActivity().getActionBar().setTitle(R.string.instance_info);
//        getActivity().getActionBar().setSubtitle(Instance);
        getActivity().setTitle(R.string.instance_info);


        //背景
        ImageView background_imageView = view.findViewById(R.id.instanceinfo_background_imageview);

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
        dialog.setMessage("インスタンス情報取得中 \r\n /api/v1/instance \r\n /api/v1/instance/activity");
        dialog.show();
*/
        //くるくる
        //ProgressDialog API 26から非推奨に
        Snackbar snackbar = Snackbar.make(view, getString(R.string.loading_instance_info)+"\r\n /api/v1/instance \r\n /api/v1/instance/activity", Snackbar.LENGTH_INDEFINITE);
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

        //非同期通信
        String finalInstance = Instance;
        String finalAccessToken = AccessToken;
        String finalInstance1 = Instance;
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                URL url = null;
                HttpURLConnection connection = null;
                String url_link = "https://" + finalInstance + "/api/v1/instance/";

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
                    JSONObject jsonObject = new JSONObject(response.toString());
                    //Status
                    JSONObject stats = jsonObject.getJSONObject("stats");

                    //タイトル
                    String title = jsonObject.getString("title");
                    //説明
                    String description = jsonObject.getString("description");
                    //バージョン？
                    String version = jsonObject.getString("version");
                    //ユーザー数
                    String user_total = stats.getString("user_count");
                    //トータルトゥート
                    String toot_total = stats.getString("status_count");

                    if (getActivity()!=null) {
                        //UI変更
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Drawable title_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_public_black_24dp, null);
                                title_icon.setBounds(0, 0, title_icon.getIntrinsicWidth(), title_icon.getIntrinsicHeight());
                                Drawable description_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_description_black_24dp, null);
                                description_icon.setBounds(0, 0, description_icon.getIntrinsicWidth(), description_icon.getIntrinsicHeight());
                                Drawable user_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_people_black_24dp, null);
                                user_icon.setBounds(0, 0, user_icon.getIntrinsicWidth(), user_icon.getIntrinsicHeight());
                                Drawable toot_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_create_black_24dp_black, null);
                                toot_icon.setBounds(0, 0, toot_icon.getIntrinsicWidth(), toot_icon.getIntrinsicHeight());

                                TextView instance_tile = getActivity().findViewById(R.id.instance_name);
                                TextView instance_description = getActivity().findViewById(R.id.instance_description);
                                TextView instance_user = getActivity().findViewById(R.id.instance_user);
                                TextView instance_toot_total = getActivity().findViewById(R.id.instance_total_toot);

                                instance_tile.setText(getString(R.string.instance_name) + " : " + title + " (" + version + ")");
                                instance_tile.setCompoundDrawables(title_icon, null, null, null);

                                instance_description.setText(getString(R.string.instance_description) + " : " + description);
                                instance_description.setCompoundDrawables(description_icon, null, null, null);

                                instance_user.setText(getString(R.string.instance_user_count) + " : " + user_total);
                                instance_user.setCompoundDrawables(user_icon, null, null, null);

                                instance_toot_total.setText(getString(R.string.instance_status_count) + " : " + toot_total);
                                instance_toot_total.setCompoundDrawables(toot_icon, null, null, null);

                            }
                        });
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

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        //アクティブ?
        AsyncTask<String, Void, String> asyncTask_active = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... string) {

                URL url = null;
                HttpURLConnection connection = null;
                String url_link = "https://" + finalInstance + "/api/v1/instance/activity";

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
//                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray datas = new JSONArray(response.toString());
                    //Status
                    JSONObject stats = datas.getJSONObject(0);

                    //すてーたす
                    String toot_total = stats.getString("statuses");
                    //ログイン
                    String user_login = stats.getString("logins");
                    //登録?
                    String registrations = stats.getString("registrations");


                    if (getActivity()!=null) {
                        //UI変更
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Drawable title_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_timeline_black_24dp, null);
                                title_icon.setBounds(0, 0, title_icon.getIntrinsicWidth(), title_icon.getIntrinsicHeight());
                                Drawable description_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_create_black_24dp_black, null);
                                description_icon.setBounds(0, 0, description_icon.getIntrinsicWidth(), description_icon.getIntrinsicHeight());
                                Drawable user_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_people_black_24dp, null);
                                user_icon.setBounds(0, 0, user_icon.getIntrinsicWidth(), user_icon.getIntrinsicHeight());
                                Drawable active_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_whatshot_black_24dp, null);
                                active_icon.setBounds(0, 0, active_icon.getIntrinsicWidth(), active_icon.getIntrinsicHeight());
                                Drawable people_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_person_add_black_24dp, null);
                                people_icon.setBounds(0, 0, people_icon.getIntrinsicWidth(), people_icon.getIntrinsicHeight());

                                TextView instance_active_title = getActivity().findViewById(R.id.instance_active_title);
                                TextView instance_active_toot = getActivity().findViewById(R.id.instance_active_toot);
                                TextView instance_active_longins = getActivity().findViewById(R.id.instance_active_longins);
                                TextView instance_active_registrations = getActivity().findViewById(R.id.instance_active_registrations);

                                instance_active_title.setText(getString(R.string.instance_statistics) + "(" + getString(R.string.this_week) + ")");
                                instance_active_title.setCompoundDrawables(title_icon, null, null, null);

                                instance_active_toot.setText(getString(R.string.instance_statistics_status) + "\r\n" + toot_total);
                                instance_active_toot.setCompoundDrawables(user_icon, null, null, null);

                                instance_active_longins.setText(getString(R.string.instance_statistics_login) + "\r\n" + user_login);
                                instance_active_longins.setCompoundDrawables(active_icon, null, null, null);

                                instance_active_registrations.setText(getString(R.string.instance_statistics_registrations) + "\r\n" + registrations);
                                instance_active_registrations.setCompoundDrawables(people_icon, null, null, null);

                            }
                        });
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


    }
}