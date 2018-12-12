package com.takusan_23.kaisendon;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Emoji;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Streaming;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class Federated_TimeLine_Fragment extends Fragment {

    String toot_text = null;
    String user = null;
    String user_name = null;
    String user_use_client = null;
    long toot_id;
    String toot_id_string = null;
    String user_avater_url = null;
    String toot_time = null;
    long account_id;

    long last_id;

    String media_url = null;

    String toot_count = null;

    private ProgressDialog dialog;
    View view;

    Shutdownable shutdownable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.activity_publc_time_line, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //設定のプリファレンス
        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //UIスレッド
        final android.os.Handler handler_1 = new android.os.Handler();

        //スリープ無効？
        boolean setting_sleep = pref_setting.getBoolean("pref_no_sleep_timeline", false);
        if (setting_sleep) {
            //常時点灯
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            //常時点灯しない
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }


        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);


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

        getActivity().setTitle(R.string.federated_timeline);

        //背景
        ImageView background_imageView = view.findViewById(R.id.publictimeline_background_imageview);

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
        dialog.setMessage("ローカルタイムラインを取得中 \r\n /api/v1/streaming/public");
        dialog.show();
*/
        //くるくる
        //ProgressDialog API 26から非推奨に
        Snackbar snackbar = Snackbar.make(view, "連合タイムラインを取得中 \r\n /api/v1/streaming/public", Snackbar.LENGTH_INDEFINITE);
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

        String finalAccessToken = AccessToken;

        String finalInstance = Instance;


        //非同期通信
        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... string) {

                MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                        .accessToken(finalAccessToken)
                        .useStreamingApi()
                        .build();
                Handler handler = new Handler() {

                    @Override
                    public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {
                        //System.out.println(status.getContent());
                        toot_text = status.getContent();
                        user = status.getAccount().getAcct();
                        user_name = status.getAccount().getDisplayName();
//                        user_use_client = status.getApplication().getName();
                        toot_id = status.getId();
                        toot_id_string = String.valueOf(toot_id);
                        //toot_time = status.getCreatedAt();
                        account_id = status.getAccount().getId();

                        //ユーザーのアバター取得
                        user_avater_url = status.getAccount().getAvatar();


                        final String[] media_url = {null};
                        //めでぃあ
                        List<Attachment> list = status.getMediaAttachments();
                        list.forEach(media -> {
                            media_url[0] = media.getUrl();
                        });

                        boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                        if (japan_timeSetting) {
                            //時差計算？
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                            //日本用フォーマット
                            SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text","yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                            try {
                                Date date = simpleDateFormat.parse(status.getCreatedAt());
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
                            toot_time = status.getCreatedAt();
                        }

                        //カスタム絵文字
                        List<Emoji> emoji_List = status.getEmojis();
                        emoji_List.forEach(emoji ->{
                            String emoji_name = emoji.getShortcode();
                            System.out.println("結果 : " + emoji_name);
                            String emoji_url = emoji.getUrl();
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            toot_text =  toot_text.replace(":"+emoji_name+":",custom_emoji_src);
                            System.out.println("結果 : " + toot_text);
                        });

                        //DisplayNameカスタム絵文字
                        List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                        account_emoji_List.forEach(emoji ->{
                            String emoji_name = emoji.getShortcode();
                            String emoji_url = emoji.getUrl();
                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                            user_name =  user_name.replace(":"+emoji_name+":",custom_emoji_src);
                        });

                        Bitmap bmp = null;
                        //BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);  // 今回はサンプルなのでデフォルトのAndroid Iconを利用
                        ImageButton nicoru_button = null;
                        ListItem listItem = new ListItem(null, toot_text,"@" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time)+ " : " + toot_time, toot_id_string, user_avater_url, account_id, user, media_url[0],null,null,null);

                        //toot_list.add(listItem);
/*
                        adapter.add(listItem);
                        adapter.notifyDataSetChanged();
*/

                        if (getActivity() == null)
                            return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ListView listView = (ListView) view.findViewById(R.id.public_time_line_list);

                                //adapter.add(listItem);
                                adapter.insert(listItem,0);
                                int y = 0;

                                int position = listView.getFirstVisiblePosition();
                                if (listView.getChildCount() > 0) {
                                    y = listView.getChildAt(0).getTop();
                                }

                                listView.setAdapter(adapter);
                                //adapter.setNotifyOnChange(false);

                                System.out.println(String.valueOf(y) + "/" + String.valueOf(position));

                                listView.setSelectionFromTop(position, y);


                                //listView.setSelection(position);

                                //くるくるを終了
                                //dialog.dismiss();
                                snackbar.dismiss();
                            }
                        });


/*
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                ListView listView = (ListView) view.findViewById(R.id.public_time_line_list);
                                int position = listView.getCheckedItemCount();
                                int y = 0;
                                if (listView.getChildCount() > 0) {
                                    y = listView.getChildAt(0).getTop();
                                }

                                listView.setAdapter(adapter);
                                //listView.setSelectionFromTop(position, y);

                            }
                        });
*/
                    }
                    @Override
                    public void onNotification(@NotNull Notification notification) {/* no op */}

                    @Override
                    public void onDelete(long id) {/* no op */}
                };

                Streaming streaming = new Streaming(client);
                try {
                    shutdownable = streaming.federatedPublic(handler);
                    Thread.sleep(10000L);
                    //shutdownable.shutdown();
                } catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return toot_text;
            }

            protected void onPostExecute(String result) {

                return;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //簡易トゥート
        TextView toot_text_edit = view.findViewById(R.id.toot_text_public);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        String finalAccessToken1 = AccessToken;

        toot_count = "0/500";


        //トゥートのカウント
        toot_text_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // 入力文字数の表示
                int txtLength = s.length();
                toot_count = Integer.toString(txtLength) + "/500";

            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });


        //テキストボックス長押し
        toot_text_edit.setOnLongClickListener(new View.OnLongClickListener()

        {

            @Override
            public boolean onLongClick(View v) {

                //設定でダイアログを出すかどうか
                boolean instant_toot_dialog = pref_setting.getBoolean("pref_timeline_toot_dialog", false);

                if (instant_toot_dialog) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setTitle(R.string.confirmation);
                    alertDialog.setMessage((getString(R.string.toot_dialog)) + "\r\n" + toot_count);
                    alertDialog.setPositiveButton(R.string.toot, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            final String toot_text = toot_text_edit.getText().toString();


                            new AsyncTask<String, Void, String>() {

                                @Override
                                protected String doInBackground(String... params) {
                                    com.sys1yagi.mastodon4j.api.entity.auth.AccessToken accessToken = new AccessToken();
                                    accessToken.setAccessToken(finalAccessToken1);

                                    MastodonClient client = new MastodonClient.Builder("friends.nico", new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                                    RequestBody requestBody = new FormBody.Builder()
                                            .add("status", toot_text)
                                            .build();

                                    System.out.println("=====" + client.post("statuses", requestBody));

                                    return toot_text;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    Toast.makeText(getContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                                }

                            }.execute();
                            toot_text_edit.setText(""); //投稿した後に入力フォームを空にする


                        }
                    });
                    alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.create().show();

                } else {

                    final String toot_text = toot_text_edit.getText().toString();

                    new AsyncTask<String, Void, String>() {

                        @Override
                        protected String doInBackground(String... params) {
                            AccessToken accessToken = new AccessToken();
                            accessToken.setAccessToken(finalAccessToken1);

                            MastodonClient client = new MastodonClient.Builder("friends.nico", new OkHttpClient.Builder(), new Gson()).accessToken(accessToken.getAccessToken()).build();

                            RequestBody requestBody = new FormBody.Builder()
                                    .add("status", toot_text)
                                    .build();

                            System.out.println("=====" + client.post("statuses", requestBody));

                            return toot_text;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            Toast.makeText(getContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                        }

                    }.execute();
                    toot_text_edit.setText(""); //投稿した後に入力フォームを空にする
                }

                return false;
            }
        });
    }
    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("終了");
        //ストリーミング終了
        if (shutdownable != null){
            shutdownable.shutdown();
        }
    }

}
