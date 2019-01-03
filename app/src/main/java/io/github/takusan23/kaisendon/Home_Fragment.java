package io.github.takusan23.kaisendon;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Emoji;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Streaming;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Home_Fragment extends Fragment {

    String toot_text = null;
    String user = null;
    String user_name = null;
    String user_use_client = null;
    long toot_id;
    String toot_id_string = null;
    String user_avater_url = null;
    String toot_boost = null;
    String toot_time = null;
    String media_url = null;
    long account_id;
    private ProgressDialog dialog;
    View view;

    String max_id = null;
    String min_id = null;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;

    int count = 0;

    Shutdownable shutdownable;

    SharedPreferences pref_setting;

    int scrollPosition = 30;

    int position;
    int y;

    boolean notification_timeline_first_setting = false;

    String count_text = null;
    int akeome_count = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.activity_home_timeline, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //設定のプリファレンス
        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        //絵文字用SharedPreferences
        SharedPreferences pref_emoji = Preference_ApplicationContext.getContext().getSharedPreferences("preferences_emoji", Context.MODE_PRIVATE);

        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

        ListView listView = (ListView) view.findViewById(R.id.home_timeline);

        //アクセストークンを変更してる場合のコード
        //アクセストークン
        String AccessToken = null;

        //インスタンス
        String Instance = null;

        //getView().setBackgroundColor(Color.parseColor("#E687CEEB"));

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        getActivity().setTitle(R.string.home);


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
        if (pref_setting.getFloat("transparency", 1.0f) != 0.0) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }

        //通知にタイムライン表示している場合は削除する
        if (pref_setting.getInt("timeline_toast_check", 0) == 1) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putInt("timeline_toast_check", 0);
            editor.commit();
            notification_timeline_first_setting = true;
        }


        //くるくる
        //ProgressDialog API 26から非推奨に
/*
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("ホームを取得中 \r\n /api/v1/timelines/home");
        dialog.show();
*/

        Snackbar snackbar = Snackbar.make(view, getString(R.string.loading_home) + "\r\n /api/v1/timelines/home", Snackbar.LENGTH_INDEFINITE);
        ViewGroup snackBer_viewGrop = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        //SnackBerを複数行対応させる
        TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
        snackBer_textView.setMaxLines(2);
        //複数行対応させたおかげでずれたので修正
        ProgressBar progressBar = new ProgressBar(getContext());
        LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBer_layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressBer_layoutParams);
        snackBer_viewGrop.addView(progressBar, 0);
        snackbar.show();

        String finalAccessToken = AccessToken;

        String finalInstance = Instance;

        final ListItem[] listItem = new ListItem[1];

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        //カウンター機能！！！
        //レイアウト
        TextView countTextView = new TextView(getContext());
        if (pref_setting.getBoolean("pref_toot_count", false)) {
            LinearLayout timelineLinearLayout = view.findViewById(R.id.home_timeline_linerLayout);
            //カウンターようレイアウト
            LinearLayout.LayoutParams LayoutlayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout countLinearLayout = new LinearLayout(getContext());
            countLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            countLinearLayout.setLayoutParams(LayoutlayoutParams);
            timelineLinearLayout.addView(countLinearLayout, 0);
            //いろいろ

            EditText countEditText = new EditText(getContext());
            Button countButton = new Button(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            countTextView.setLayoutParams(layoutParams);
            countEditText.setLayoutParams(layoutParams);
            countButton.setText("カウント開始");
            countEditText.setHint("カウントしたい文字を入れてね");

            //コレ呼ばないとえらー
            if (countTextView.getParent() != null) {
                ((ViewGroup) countTextView.getParent()).removeView(countTextView);
            }
            if (countEditText.getParent() != null) {
                ((ViewGroup) countEditText.getParent()).removeView(countEditText);
            }
            if (countButton.getParent() != null) {
                ((ViewGroup) countButton.getParent()).removeView(countButton);
            }

            countLinearLayout.addView(countEditText);
            countLinearLayout.addView(countButton);
            countLinearLayout.addView(countTextView);

            //テキストを決定
            countButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    count_text = countEditText.getText().toString();
                    akeome_count = 0;
                    String count_template = "　を含んだトゥート数 : ";
                    countTextView.setText(count_text + count_template + String.valueOf(akeome_count));
                    //Toast.makeText(getContext(),count_text,Toast.LENGTH_SHORT).show();
                }
            });

            //長押しでコピー
            countEditText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    assert clipboardManager != null;
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("", String.valueOf(akeome_count)));
                    Toast.makeText(getContext(), R.string.copy, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }



        //ストリーミングAPI
        if (pref_setting.getBoolean("pref_streaming_api", true)) {
            //引っ張って更新するやつ無効
            swipeRefreshLayout.setEnabled(false);

            new AsyncTask<String, String, String>() {
                @Override
                protected String doInBackground(String... string) {

                    MastodonClient client = new MastodonClient.Builder(finalInstance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(finalAccessToken)
                            .useStreamingApi()
                            .build();
                    Handler handler = new Handler() {

                        @Override
                        public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {

                            //System.out.println("てすと : " + status.getContent());
                            toot_text = status.getContent();
                            user = status.getAccount().getUserName();
                            user_name = status.getAccount().getDisplayName();
                            user_use_client = null;
                            toot_id = status.getId();
                            toot_id_string = String.valueOf(toot_id);
                            //toot_time = status.getCreatedAt();
                            account_id = status.getAccount().getId();

                            //ユーザーのアバター取得
                            user_avater_url = status.getAccount().getAvatar();

                            //一番最初のIDを控える
                            if (max_id == null) {
                                max_id = toot_id_string;
                            }

                            //クライアント名
                            try {
                                user_use_client = status.getApplication().getName();
                            } catch (NullPointerException e) {
                                user_use_client = null;
                            }

                            //System.out.println("IDだよ : " + max_id);


                            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                            if (japan_timeSetting) {
                                //時差計算？
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                //日本用フォーマット
                                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                try {
                                    Date date = simpleDateFormat.parse(status.getCreatedAt());
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    //9時間足して日本時間へ
                                    calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                    //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                    toot_time = japanDateFormat.format(calendar.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                toot_time = status.getCreatedAt();
                            }

                            String[] mediaURL = {null, null, null, null};
                            //めでぃあ
                            //配列に入れる形で
                            final int[] i = {0};
                            List<Attachment> list = status.getMediaAttachments();
                            list.forEach(media -> {
                                mediaURL[i[0]] = media.getUrl();
                                i[0]++;
                            });

                            //System.out.println("配列 : " + Arrays.asList(mediaURL));

                            //配列から文字列に
                            media_url_1 = mediaURL[0];
                            media_url_2 = mediaURL[1];
                            media_url_3 = mediaURL[2];
                            media_url_4 = mediaURL[3];


                            if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                //カスタム絵文字
                                List<Emoji> emoji_List = status.getEmojis();
                                emoji_List.forEach(emoji -> {
                                    String emoji_name = emoji.getShortcode();
                                    String emoji_url = emoji.getUrl();
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                });

                                //DisplayNameカスタム絵文字
                                List<Emoji> account_emoji_List = status.getAccount().getEmojis();
                                account_emoji_List.forEach(emoji -> {
                                    String emoji_name = emoji.getShortcode();
                                    String emoji_url = emoji.getUrl();
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                });
                            }

                            Bitmap bmp = null;
                            //BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);  // 今回はサンプルなのでデフォルトのAndroid Iconを利用
                            ImageButton nicoru_button = null;

                            if (getActivity() != null) {

                                listItem[0] = new ListItem(null, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, media_url_1, media_url_2, media_url_3, media_url_4,null);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        //adapter.add(listItem);
                                        adapter.insert(listItem[0], 0);

                                        // 画面上で最上部に表示されているビューのポジションとTopを記録しておく

                                        int pos = listView.getFirstVisiblePosition();
                                        int top = 0;
                                        if (listView.getChildCount() > 0) {
                                            top = listView.getChildAt(0).getTop();
                                        }
                                        listView.setAdapter(adapter);
                                        //System.out.println("TOP == " + top);
                                        // 要素追加前の状態になるようセットする
                                        adapter.notifyDataSetChanged();
                                        listView.setSelectionFromTop(pos + 1, top);


                                        //一番上なら追いかける
                                        if (pos <= 1) {
                                            listView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    listView.smoothScrollToPosition(-10);
                                                    //listView.setSelectionFromTop(index, top_);
                                                }
                                            });
                                            //System.out.println("ねてた");
                                        }
                                        int finalTop = top;

                                        //くるくるを終了
                                        //dialog.dismiss();
                                        snackbar.dismiss();

                                        //カウンター
                                        if (count_text != null && pref_setting.getBoolean("pref_toot_count",false)) {
                                            //含んでいるか
                                            if (toot_text.contains(count_text)) {
                                                String count_template = "　を含んだトゥート数 : ";
                                                akeome_count++;
                                                countTextView.setText(count_text + count_template + String.valueOf(akeome_count));
                                            }
                                        }

                                    }
                                });
                            }

                        }

                        @Override
                        public void onNotification(@NotNull Notification notification) {/* no op */}

                        @Override
                        public void onDelete(long id) {/* no op */}
                    };

                    Streaming streaming = new Streaming(client);
                    try {
                        shutdownable = streaming.user(handler);
                        //Thread.sleep(10000L);
                        //shutdownable.shutdown();
                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }

                    return toot_text;

                }

                protected void onPostExecute(String result) {

                    return;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //ストリーミング前のトゥート取得
            //SnackBer表示
            Snackbar maxid_snackbar = Snackbar.make(view, getString(R.string.loading_home) + "\r\n /api/v1/timelines/home \r\nmax_id=" + max_id, Snackbar.LENGTH_INDEFINITE);
            ViewGroup maxid_viewGrop = (ViewGroup) maxid_snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
            //SnackBerを複数行対応させる
            TextView maxid_textView = (TextView) maxid_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
            maxid_textView.setMaxLines(4);
            //複数行対応させたおかげでずれたので修正
            ProgressBar maxid_progressBar = new ProgressBar(getContext());
            LinearLayout.LayoutParams maxid_progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            maxid_progressBer_layoutParams.gravity = Gravity.CENTER;
            maxid_progressBar.setLayoutParams(maxid_progressBer_layoutParams);
            maxid_viewGrop.addView(maxid_progressBar, 0);
            maxid_snackbar.show();


            //ストリーミング前のトゥート取得
            //パラメータを設定
            String url = "https://" + finalInstance + "/api/v1/timelines/home/?access_token=" + finalAccessToken;
            HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
            builder.addQueryParameter("limit", "40");
            builder.addQueryParameter("max_id", max_id);
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
                    String response_string = response.body().string();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(response_string);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                            JSONObject toot_account = toot_jsonObject.getJSONObject("account");
                            JSONArray media_array = toot_jsonObject.getJSONArray("media_attachments");
                            toot_text = toot_jsonObject.getString("content");
                            user = toot_account.getString("username");
                            user_name = toot_account.getString("display_name");
                            toot_time = toot_jsonObject.getString("created_at");
                            String type = null;

                            //クライアント名がある？ない？
                            try {
                                JSONObject application = toot_jsonObject.getJSONObject("application");
                                user_use_client = application.getString("name");
                            } catch (JSONException e) {
                                user_use_client = toot_jsonObject.getString("application");
                            }


                            //user_use_client = status.getApplication().getName();
                            //toot_id = toot_jsonObject.getString("id");
                            toot_id_string = toot_jsonObject.getString("id");

                            user_avater_url = toot_account.getString("avatar");

                            account_id = toot_account.getInt("id");

                            List<Attachment> attachment = Collections.singletonList(new Attachment());


                            final String[] medias = new String[1];

                            final String[] media_url = {null};

                            if (!media_array.isNull(0)) {
                                media_url_1 = media_array.getJSONObject(0).getString("url");
                            }
                            if (!media_array.isNull(1)) {
                                media_url_2 = media_array.getJSONObject(1).getString("url");
                            }
                            if (!media_array.isNull(2)) {
                                media_url_3 = media_array.getJSONObject(2).getString("url");
                            }
                            if (!media_array.isNull(3)) {
                                media_url_4 = media_array.getJSONObject(3).getString("url");
                            }
                            //System.out.println("これかあ！ ： " + media_url_1 + " / " + media_url_2  + " / " + media_url_3 + " / " + media_url_4);

                            if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                //絵文字
                                JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                                for (int e = 0; e < emoji.length(); e++) {
                                    JSONObject jsonObject = emoji.getJSONObject(e);
                                    String emoji_name = jsonObject.getString("shortcode");
                                    String emoji_url = jsonObject.getString("url");
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                }

                                //アバター絵文字
                                try {
                                    JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                                    for (int a = 0; a < avater_emoji.length(); a++) {
                                        JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                    }

                                    //ユーザーネームの方のアバター絵文字
                                    JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                                    for (int a = 0; a < account_avater_emoji.length(); a++) {
                                        JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                    }
                                } catch (JSONException e) {

                                }

                                //ユーザーネームの方の絵文字
                                JSONArray account_emoji = toot_account.getJSONArray("emojis");
                                for (int e = 0; e < account_emoji.length(); e++) {
                                    JSONObject jsonObject = account_emoji.getJSONObject(e);
                                    String emoji_name = jsonObject.getString("shortcode");
                                    String emoji_url = jsonObject.getString("url");
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                }
                            }

                            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                            if (japan_timeSetting) {
                                //時差計算？
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                //日本用フォーマット
                                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                try {
                                    Date date = simpleDateFormat.parse(toot_jsonObject.getString("created_at"));
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    //9時間足して日本時間へ
                                    calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                    //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                    toot_time = japanDateFormat.format(calendar.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                toot_time = toot_jsonObject.getString("created_at");
                            }


                            if (getActivity() != null) {
                                ListItem listItem = new ListItem(type, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, media_url_1, media_url_2, media_url_3, media_url_4,null);


                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                        ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                        listView.setAdapter(adapter);
                                        maxid_snackbar.dismiss();
                                        //maxid_snackbar.dismiss();
                                        //listView.setSelection(scrollPosition);
                                    }
                                });
                            }

                            media_url_1 = null;
                            media_url_2 = null;
                            media_url_3 = null;
                            media_url_4 = null;
                            type = null;

                        }
                        //最後のIDを更新する
                        JSONObject last_toot = jsonArray.getJSONObject(39);
                        max_id = last_toot.getString("id");

                        //更新用に最初のIDを控える
                        JSONObject first_toot = jsonArray.getJSONObject(0);
                        min_id = first_toot.getString("id");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } else {
            String url = "https://" + finalInstance + "/api/v1/timelines/home/?access_token=" + finalAccessToken;
            //パラメータを設定
            HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
            builder.addQueryParameter("limit", "40");
            String final_url = builder.build().toString();
            //作成
            Request max_id_request = new Request.Builder()
                    .url(final_url)
                    .get()
                    .build();

            //GETリクエスト
            OkHttpClient max_id_client = new OkHttpClient();
            max_id_client.newCall(max_id_request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String response_string = response.body().string();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(response_string);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                            JSONObject toot_account = toot_jsonObject.getJSONObject("account");
                            JSONArray media_array = toot_jsonObject.getJSONArray("media_attachments");
                            toot_text = toot_jsonObject.getString("content");
                            user = toot_account.getString("username");
                            user_name = toot_account.getString("display_name");
                            toot_time = toot_jsonObject.getString("created_at");
                            String type = null;

                            //クライアント名がある？ない？
                            try {
                                JSONObject application = toot_jsonObject.getJSONObject("application");
                                user_use_client = application.getString("name");
                            } catch (JSONException e) {
                                user_use_client = toot_jsonObject.getString("application");
                            }

                            //user_use_client = status.getApplication().getName();
                            //toot_id = toot_jsonObject.getString("id");
                            toot_id_string = toot_jsonObject.getString("id");

                            user_avater_url = toot_account.getString("avatar");

                            account_id = toot_account.getInt("id");

                            List<Attachment> attachment = Collections.singletonList(new Attachment());


                            final String[] medias = new String[1];

                            final String[] media_url = {null};

                            if (!media_array.isNull(0)) {
                                media_url_1 = media_array.getJSONObject(0).getString("url");
                            }
                            if (!media_array.isNull(1)) {
                                media_url_2 = media_array.getJSONObject(1).getString("url");
                            }
                            if (!media_array.isNull(2)) {
                                media_url_3 = media_array.getJSONObject(2).getString("url");
                            }
                            if (!media_array.isNull(3)) {
                                media_url_4 = media_array.getJSONObject(3).getString("url");
                            }
                            //System.out.println("これかあ！ ： " + media_url_1 + " / " + media_url_2  + " / " + media_url_3 + " / " + media_url_4);

                            if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                //絵文字
                                JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                                for (int e = 0; e < emoji.length(); e++) {
                                    JSONObject jsonObject = emoji.getJSONObject(e);
                                    String emoji_name = jsonObject.getString("shortcode");
                                    String emoji_url = jsonObject.getString("url");
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                }

                                //アバター絵文字
                                try {
                                    JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                                    for (int a = 0; a < avater_emoji.length(); a++) {
                                        JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                    }

                                    //ユーザーネームの方のアバター絵文字
                                    JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                                    for (int a = 0; a < account_avater_emoji.length(); a++) {
                                        JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                                        String emoji_name = jsonObject.getString("shortcode");
                                        String emoji_url = jsonObject.getString("url");
                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                    }
                                } catch (JSONException e) {

                                }

                                //ユーザーネームの方の絵文字
                                JSONArray account_emoji = toot_account.getJSONArray("emojis");
                                for (int e = 0; e < account_emoji.length(); e++) {
                                    JSONObject jsonObject = account_emoji.getJSONObject(e);
                                    String emoji_name = jsonObject.getString("shortcode");
                                    String emoji_url = jsonObject.getString("url");
                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                }
                            }

                            boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                            if (japan_timeSetting) {
                                //時差計算？
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                //日本用フォーマット
                                SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                try {
                                    Date date = simpleDateFormat.parse(toot_jsonObject.getString("created_at"));
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    //9時間足して日本時間へ
                                    calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                    //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                    toot_time = japanDateFormat.format(calendar.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                toot_time = toot_jsonObject.getString("created_at");
                            }


                            if (getActivity() != null){
                                ListItem listItem = new ListItem(type, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, media_url_1, media_url_2, media_url_3, media_url_4,null);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(listItem);
                                        adapter.notifyDataSetChanged();
                                        ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                        listView.setAdapter(adapter);
                                        snackbar.dismiss();
                                        //maxid_snackbar.dismiss();
                                        //listView.setSelection(scrollPosition);
                                    }
                                });
                            }

                            media_url_1 = null;
                            media_url_2 = null;
                            media_url_3 = null;
                            media_url_4 = null;
                            type = null;

                        }
                        //最後のIDを更新する
                        JSONObject last_toot = jsonArray.getJSONObject(39);
                        max_id = last_toot.getString("id");

                        //更新用に最初のIDを控える
                        JSONObject first_toot = jsonArray.getJSONObject(0);
                        min_id = first_toot.getString("id");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            //引っ張って更新するやつ
            swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#BBDEFB"), Color.parseColor("#90CAF9"), Color.parseColor("#42A5F5"), Color.parseColor("#1565C0"));
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {

                    adapter.clear();
                    snackbar.show();

                    //最後のトゥートIDを持ってくる
                    //もういい！okhttpで実装する！！
                    String max_id_url = "https://" + finalInstance + "/api/v1/timelines/home/?access_token=" + finalAccessToken;
                    //パラメータを設定
                    HttpUrl.Builder max_id_builder = HttpUrl.parse(max_id_url).newBuilder();
                    max_id_builder.addQueryParameter("limit", "40");
                    //max_id_builder.addQueryParameter("since_id", min_id);
                    String max_id_final_url = max_id_builder.build().toString();

                    //作成
                    Request max_id_request = new Request.Builder()
                            .url(max_id_final_url)
                            .get()
                            .build();

                    //GETリクエスト
                    OkHttpClient max_id_client = new OkHttpClient();
                    max_id_client.newCall(max_id_request).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String response_string = response.body().string();
                            JSONArray jsonArray = null;
                            try {
                                jsonArray = new JSONArray(response_string);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject toot_account = toot_jsonObject.getJSONObject("account");
                                    toot_text = toot_jsonObject.getString("content");
                                    user = toot_account.getString("username");
                                    user_name = toot_account.getString("display_name");
                                    toot_time = toot_jsonObject.getString("created_at");
                                    String type = null;

                                    //クライアント名がある？ない？
                                    try {
                                        JSONObject application = toot_jsonObject.getJSONObject("application");
                                        user_use_client = application.getString("name");
                                    } catch (JSONException e) {
                                        user_use_client = toot_jsonObject.getString("application");
                                    }


                                    //                       user_use_client = status.getApplication().getName();
                                    //toot_id = toot_jsonObject.getString("id");
                                    toot_id_string = toot_jsonObject.getString("id");

                                    user_avater_url = toot_account.getString("avatar");

                                    account_id = toot_account.getInt("id");

                                    List<Attachment> attachment = Collections.singletonList(new Attachment());


                                    final String[] medias = new String[1];

                                    final String[] media_url = {null};

                                    JSONArray media_array = toot_jsonObject.getJSONArray("media_attachments");
                                    if (!media_array.isNull(0)) {
                                        media_url_1 = media_array.getJSONObject(0).getString("url");
                                    }
                                    if (!media_array.isNull(1)) {
                                        media_url_2 = media_array.getJSONObject(1).getString("url");
                                    }
                                    if (!media_array.isNull(2)) {
                                        media_url_3 = media_array.getJSONObject(2).getString("url");
                                    }
                                    if (!media_array.isNull(3)) {
                                        media_url_4 = media_array.getJSONObject(3).getString("url");
                                    }


                                    if (pref_setting.getBoolean("pref_custom_emoji", false)) {
                                        //絵文字
                                        JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                                        for (int e = 0; e < emoji.length(); e++) {
                                            JSONObject jsonObject = emoji.getJSONObject(e);
                                            String emoji_name = jsonObject.getString("shortcode");
                                            String emoji_url = jsonObject.getString("url");
                                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                            toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                            user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                        }

                                        //アバター絵文字
                                        try {
                                            JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                                            for (int a = 0; a < avater_emoji.length(); a++) {
                                                JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                                String emoji_name = jsonObject.getString("shortcode");
                                                String emoji_url = jsonObject.getString("url");
                                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                            }

                                            //ユーザーネームの方のアバター絵文字
                                            JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                                            for (int a = 0; a < account_avater_emoji.length(); a++) {
                                                JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                                                String emoji_name = jsonObject.getString("shortcode");
                                                String emoji_url = jsonObject.getString("url");
                                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                            }
                                        } catch (JSONException e) {

                                        }

                                        //ユーザーネームの方の絵文字
                                        JSONArray account_emoji = toot_account.getJSONArray("emojis");
                                        for (int e = 0; e < account_emoji.length(); e++) {
                                            JSONObject jsonObject = account_emoji.getJSONObject(e);
                                            String emoji_name = jsonObject.getString("shortcode");
                                            String emoji_url = jsonObject.getString("url");
                                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                            user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                        }
                                    }

                                    boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                                    if (japan_timeSetting) {
                                        //時差計算？
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                        //日本用フォーマット
                                        SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                        try {
                                            Date date = simpleDateFormat.parse(toot_jsonObject.getString("created_at"));
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(date);
                                            //9時間足して日本時間へ
                                            calendar.add(Calendar.HOUR, +Integer.valueOf(pref_setting.getString("pref_time_add", "9")));
                                            //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
                                            toot_time = japanDateFormat.format(calendar.getTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        toot_time = toot_jsonObject.getString("created_at");
                                    }


                                    if (getActivity() != null){
                                        ListItem listItem = new ListItem(type, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, media_url_1, media_url_2, media_url_3, media_url_4,null);

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.add(listItem);
                                                adapter.notifyDataSetChanged();
                                                listView.setAdapter(adapter);
                                                snackbar.dismiss();
                                                //listView.setSelection(scrollPosition);
                                            }
                                        });
                                    }

                                    media_url_1 = null;
                                    media_url_2 = null;
                                    media_url_3 = null;
                                    media_url_4 = null;
                                    type = null;

                                }

                                //最後のIDを更新する
                                JSONObject last_toot = jsonArray.getJSONObject(39);
                                max_id = last_toot.getString("id");


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });

        }


        //最後になったら追加読込するやつ
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount != 0 && totalItemCount == firstVisibleItem + visibleItemCount) {
                    position = listView.getFirstVisiblePosition();
                    y = listView.getChildAt(0).getTop();

                    Snackbar snackbar_ = Snackbar.make(view, R.string.add_loading, Snackbar.LENGTH_LONG);
                    snackbar_.show();
                    if (snackbar_.isShown()) {
                        //System.out.println("最後だよ");

                        //scrollPosition = scrollPosition + 40;


                        //最後のトゥートIDを持ってくる
                        //もういい！okhttpで実装する！！
                        String url = "https://" + finalInstance + "/api/v1/timelines/home/?access_token=" + finalAccessToken;
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
                        OkHttpClient client_1 = new OkHttpClient();
                        client_1.newCall(request).enqueue(new Callback() {

                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String response_string = response.body().string();
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = new JSONArray(response_string);
                                    JSONObject last_toot_jsonObject = jsonArray.getJSONObject(39);
                                    max_id = last_toot_jsonObject.getString("id");
//                                    System.out.println("最後" + max_id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        if (max_id != null) {

                            //SnackBer表示
                            Snackbar maxid_snackbar = Snackbar.make(view, getString(R.string.loading_home) + "\r\n /api/v1/timelines/home \r\n max_id=" + max_id, Snackbar.LENGTH_INDEFINITE);
                            ViewGroup snackBer_viewGrop = (ViewGroup) maxid_snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                            //SnackBerを複数行対応させる
                            TextView snackBer_textView = (TextView) snackBer_viewGrop.findViewById(android.support.design.R.id.snackbar_text);
                            snackBer_textView.setMaxLines(3);
                            //複数行対応させたおかげでずれたので修正
                            ProgressBar progressBar = new ProgressBar(getContext());
                            LinearLayout.LayoutParams progressBer_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            progressBer_layoutParams.gravity = Gravity.CENTER;
                            progressBar.setLayoutParams(progressBer_layoutParams);
                            snackBer_viewGrop.addView(progressBar, 0);
                            maxid_snackbar.show();


                            //最後のトゥートIDを持ってくる
                            //もういい！okhttpで実装する！！
                            String max_id_url = "https://" + finalInstance + "/api/v1/timelines/home/?access_token=" + finalAccessToken;
                            //パラメータを設定
                            HttpUrl.Builder max_id_builder = HttpUrl.parse(max_id_url).newBuilder();
                            max_id_builder.addQueryParameter("limit", "40");
                            max_id_builder.addQueryParameter("max_id", max_id);
                            String max_id_final_url = max_id_builder.build().toString();

                            //作成
                            Request max_id_request = new Request.Builder()
                                    .url(max_id_final_url)
                                    .get()
                                    .build();

                            //GETリクエスト
                            OkHttpClient max_id_client = new OkHttpClient();
                            max_id_client.newCall(max_id_request).enqueue(new Callback() {

                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String response_string = response.body().string();
                                    JSONArray jsonArray = null;
                                    try {
                                        jsonArray = new JSONArray(response_string);
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject toot_jsonObject = jsonArray.getJSONObject(i);
                                            JSONObject toot_account = toot_jsonObject.getJSONObject("account");
                                            toot_text = toot_jsonObject.getString("content");
                                            user = toot_account.getString("username");
                                            user_name = toot_account.getString("display_name");
                                            toot_time = toot_jsonObject.getString("created_at");
                                            String type = null;

                                            //クライアント名がある？ない？
                                            try {
                                                JSONObject application = toot_jsonObject.getJSONObject("application");
                                                user_use_client = application.getString("name");
                                            } catch (JSONException e) {
                                                user_use_client = toot_jsonObject.getString("application");
                                            }


                                            //                       user_use_client = status.getApplication().getName();
                                            //toot_id = toot_jsonObject.getString("id");
                                            toot_id_string = toot_jsonObject.getString("id");

                                            user_avater_url = toot_account.getString("avatar");

                                            account_id = toot_account.getInt("id");

                                            List<Attachment> attachment = Collections.singletonList(new Attachment());


                                            final String[] medias = new String[1];

                                            final String[] media_url = {null};

                                            JSONArray media_array = toot_jsonObject.getJSONArray("media_attachments");
                                            if (!media_array.isNull(0)) {
                                                media_url_1 = media_array.getJSONObject(0).getString("url");
                                            }
                                            if (!media_array.isNull(1)) {
                                                media_url_2 = media_array.getJSONObject(1).getString("url");
                                            }
                                            if (!media_array.isNull(2)) {
                                                media_url_3 = media_array.getJSONObject(2).getString("url");
                                            }
                                            if (!media_array.isNull(3)) {
                                                media_url_4 = media_array.getJSONObject(3).getString("url");
                                            }


                                            if (pref_setting.getBoolean("pref_custom_emoji", false)) {

                                                //絵文字
                                                JSONArray emoji = toot_jsonObject.getJSONArray("emojis");
                                                for (int e = 0; e < emoji.length(); e++) {
                                                    JSONObject jsonObject = emoji.getJSONObject(e);
                                                    String emoji_name = jsonObject.getString("shortcode");
                                                    String emoji_url = jsonObject.getString("url");
                                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                    toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                }

                                                //アバター絵文字
                                                try {
                                                    JSONArray avater_emoji = toot_jsonObject.getJSONArray("profile_emojis");
                                                    for (int a = 0; a < avater_emoji.length(); a++) {
                                                        JSONObject jsonObject = avater_emoji.getJSONObject(a);
                                                        String emoji_name = jsonObject.getString("shortcode");
                                                        String emoji_url = jsonObject.getString("url");
                                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                        toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                    }

                                                    //ユーザーネームの方のアバター絵文字
                                                    JSONArray account_avater_emoji = toot_account.getJSONArray("profile_emojis");
                                                    for (int a = 0; a < account_avater_emoji.length(); a++) {
                                                        JSONObject jsonObject = account_avater_emoji.getJSONObject(a);
                                                        String emoji_name = jsonObject.getString("shortcode");
                                                        String emoji_url = jsonObject.getString("url");
                                                        String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                        user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                    }
                                                } catch (JSONException e) {

                                                }

                                                //ユーザーネームの方の絵文字
                                                JSONArray account_emoji = toot_account.getJSONArray("emojis");
                                                for (int e = 0; e < account_emoji.length(); e++) {
                                                    JSONObject jsonObject = account_emoji.getJSONObject(e);
                                                    String emoji_name = jsonObject.getString("shortcode");
                                                    String emoji_url = jsonObject.getString("url");
                                                    String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                    user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                }

                                            }


                                            if (getActivity() != null) {
                                                ListItem listItem = new ListItem(type, toot_text, user_name + " @" + user, "クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time, toot_id_string, user_avater_url, account_id, user, media_url_1, media_url_2, media_url_3, media_url_4,null);

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        adapter.add(listItem);
                                                        adapter.notifyDataSetChanged();
                                                        ListView listView = (ListView) view.findViewById(R.id.home_timeline);
                                                        listView.setAdapter(adapter);
                                                        maxid_snackbar.dismiss();
                                                        //listView.setSelection(scrollPosition);
                                                        listView.setSelectionFromTop(position, y);

                                                        //listView.setSelection(scrollPosition);
                                                    }
                                                });
                                            }

                                            media_url_1 = null;
                                            media_url_2 = null;
                                            media_url_3 = null;
                                            media_url_4 = null;
                                            type = null;
                                        }
                                        //最後のIDを更新する
                                        JSONObject last_toot = jsonArray.getJSONObject(39);
                                        max_id = last_toot.getString("id");
                                        scrollPosition += 30;

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }

    //フラグメントが外されたときに呼ばれる
    @Override
    public void onDetach() {
        super.onDetach();
        //System.out.println("終了");
//        asyncTask.cancel(true);
        //ストリーミング終了
        if (shutdownable != null) {
            shutdownable.shutdown();
        }

        //通知にタイムライン表示している場合は削除する
        if (notification_timeline_first_setting) {
            SharedPreferences.Editor editor = pref_setting.edit();
            editor.putInt("timeline_toast_check", 1);
            editor.apply();
        }
    }

}