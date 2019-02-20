package io.github.takusan23.kaisendon;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;

import org.chromium.customtabsclient.shared.CustomTabsHelper;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;

public class Account_Fragment extends Fragment{

    String display_name = null;
    String user_account_id = null;
    String avater_url = null;
    String heander_url = null;
    String profile = null;
    String create_at = null;

    long account_id_button;

    int follow;
    int follower;

    private ProgressDialog dialog;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.activity_user_old, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        final android.os.Handler handler_1 = new android.os.Handler();

        //先に
        TextView displayname_textview = view.findViewById(R.id.username);
        TextView id_textview = view.findViewById(R.id.account_id);
        TextView profile_textview = view.findViewById(R.id.profile);

        //画像
        ImageView avater = view.findViewById(R.id.avater_user);
        ImageView header = view.findViewById(R.id.header_user);

        //ボタン
        Button follower_button = view.findViewById(R.id.follower_button);
        Button follow_button = view.findViewById(R.id.follow_button);


        //プリファレンス
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


/*
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("ユーザー情報を取得中");
        dialog.show();
*/
        //くるくる
        //ProgressDialog API 26から非推奨に

        Snackbar snackbar = Snackbar.make(view, "ユーザー情報を取得中", Snackbar.LENGTH_INDEFINITE);
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

        //非同期通信でアカウント情報を取得
        String finalInstance = Instance;
        String finalAccessToken = AccessToken;

        //Icon
        Bitmap back_icon = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_arrow_back_black_24dp);

        //どうでもいい
//
//        SpannableString spannableString = new SpannableString("アイコンテスト : ");
//        spannableString.setSpan(new ImageSpan(AccountActivity.this, R.mipmap.ic_launcher), 7, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        displayname_textview.setText(spannableString);
        //

        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .build();

                try {

                    Account account = new Accounts(client).getVerifyCredentials().execute();

                    display_name = account.getDisplayName();
                    user_account_id = account.getUserName();
                    profile = account.getNote();
                    avater_url = account.getAvatar();
                    heander_url = account.getHeader();
                    create_at = account.getCreatedAt();

                    follow = account.getFollowingCount();
                    follower = account.getFollowersCount();

                    account_id_button = account.getId();

                    handler_1.post(new Runnable() {
                        @Override
                        public void run() {
                            displayname_textview.setText(display_name);
                            displayname_textview.setTextSize(20);
                            id_textview.setText("@" + user_account_id + "@" + finalInstance + "\r\n" + create_at);
                            profile_textview.setText(Html.fromHtml(profile, Html.FROM_HTML_MODE_COMPACT));


                            follow_button.setText("フォロー : " + String.valueOf(follow));
                            follower_button.setText("フォロワー : " + String.valueOf(follower));

                            boolean setting_avater_gif = pref_setting.getBoolean("pref_avater_gif", false);
                            if (setting_avater_gif) {

                                //GIFアニメ再生させない
                                Picasso.get()
                                        .load(avater_url)
                                        .into(avater);

                                Picasso.get()
                                        .load(heander_url)
                                        .into(header);

                            } else {

                                //GIFアニメを再生
                                Glide.with(getContext())
                                        .load(avater_url)
                                        .into(avater);

                                Glide.with(getContext())
                                        .load(heander_url)
                                        .into(header);
                            }
                        }
                    });


                    //friends.nicoモードかな？
                    boolean frenico_mode = pref_setting.getBoolean("setting_friends_nico_mode", true);
                    //Chrome Custom Tab
                    boolean chrome_custom_tabs = pref_setting.getBoolean("pref_chrome_custom_tabs", true);

                    final String[] nico_url = {null};

                    //Json解析して"nico_url"取得
                    Account account_nico_url = new Accounts(client).getVerifyCredentials().doOnJson(jsonString -> {
                                //System.out.println(jsonString);
                                //String string_ = "{\"int array\":[100,200,300],\"boolean\":true,\"string\":\"string\",\"object\":{\"object_1\":1,\"object_3\":3,\"object_2\":2},\"null\":null,\"array\":[1,2,3],\"long\":18000305032230531,\"int\":100,\"double\":10.5}";
                                JsonParser parser = new JsonParser();
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(jsonString);

                                    nico_url[0] = jsonObject.getString("nico_url");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                    ).execute();


                    //URLあるよ
                    if (frenico_mode && !nico_url[0].equals("null")) {
                        //ニコニコURLへ
                        Button button = view.findViewById(R.id.button3);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setText("ニコニコ");
                            }
                        });

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (chrome_custom_tabs) {

                                    String custom = CustomTabsHelper.getPackageNameToUse(getContext());

                                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.intent.setPackage(custom);
                                    customTabsIntent.launchUrl((Activity) getContext(), Uri.parse(nico_url[0]));
                                    //無効
                                } else {
                                    Uri uri = Uri.parse(nico_url[0]);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            }
                        });
                        //URLなかった
                    } else {
                        Button button = view.findViewById(R.id.button3);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setText("Web");
                            }
                        });
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (chrome_custom_tabs) {

                                    String custom = CustomTabsHelper.getPackageNameToUse(getContext());

                                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setCloseButtonIcon(back_icon).setShowTitle(true);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.intent.setPackage(custom);
                                    customTabsIntent.launchUrl((Activity) getContext(), Uri.parse("https://" + finalInstance + "/" + "@" + user_account_id));
                                    //無効
                                } else {
                                    Uri uri = Uri.parse(nico_url[0]);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + finalInstance + "/" + "@" + user_account_id));
                                    startActivity(intent);
                                }
                            }
                        });
                    }



                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                //くるくるを終了
                //dialog.dismiss();
                snackbar.dismiss();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //ボタンクリック
        follow_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent follow = new Intent(getContext(), UserFollowActivity.class);
                follow.putExtra("account_id", account_id_button);
                follow.putExtra("follow_follower", true);
                startActivity(follow);
            }
        });

        follower_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent follower = new Intent(getContext(), UserFollowActivity.class);
                follower.putExtra("account_id", account_id_button);
                follower.putExtra("follow_follower", false);
                startActivity(follower);
            }
        });
/*

        //実験用
        Button button = view.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setShowTitle(true).setCloseButtonIcon(back_icon);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getContext(), Uri.parse("https://" + finalInstance + "/" + "@" + user_account_id));
            }
        });
*/

    }


}
