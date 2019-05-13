package io.github.takusan23.kaisendon.Fragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Attachment;
import com.sys1yagi.mastodon4j.api.entity.Card;
import com.sys1yagi.mastodon4j.api.entity.Emoji;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Streaming;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.takusan23.kaisendon.Home;
import io.github.takusan23.kaisendon.HomeTimeLineAdapter;
import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.MarqueeTextView;
import io.github.takusan23.kaisendon.Preference_ApplicationContext;
import io.github.takusan23.kaisendon.R;
import okhttp3.OkHttpClient;

public class CustomStreamingFragment extends Fragment {

    String toot_text = null;
    String user = null;
    String user_name = null;
    String user_use_client = null;
    long toot_id;
    String toot_id_string = null;
    String user_avater_url = null;
    String toot_time = null;
    long account_id;

    String max_id;

    String toot_count = null;

    String type;


    View view;
    SharedPreferences pref_setting;

    Shutdownable local_shutdownable;

    boolean local_timeline_boolean;
    boolean home_timeline_boolean;
    boolean notification_timeline_boolean;

    //メディア
    String media_url_1 = null;
    String media_url_2 = null;
    String media_url_3 = null;
    String media_url_4 = null;


    String count_text = null;
    int akeome_count = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_custom_streaming, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //設定のプリファレンス
        pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        ListView custom_streaming_listview = view.findViewById(R.id.custom_streaming_listview);
        Button custom_streaming_setting_button = view.findViewById(R.id.custom_streaming_setting);
        //EditText custom_steaming_toot_edittext = view.findViewById(R.id.custom_streaming_toot_text);

        //スリープを無効にする
        if (pref_setting.getBoolean("pref_no_sleep", false)) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }


        //背景画像
        ImageView background_imageView = view.findViewById(R.id.custom_streaming_background_imageview);

        if (pref_setting.getBoolean("background_image", true)) {
            Uri uri = Uri.parse(pref_setting.getString("background_image_path", ""));
            //background_imageView.setImageURI(uri);
            //Glideで読み込み(URI)
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
        if (pref_setting.getFloat("transparency", 1.0f) != 0.1) {
            background_imageView.setAlpha(pref_setting.getFloat("transparency", 1.0f));
        }


        //ボタンに画像セット
        custom_streaming_setting_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_black_24dp, 0, 0, 0);

        //タイトル
        getActivity().setTitle(R.string.custom_streaming);
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

        //ToolBerをクリックしたら一番上に移動するようにする
        if (pref_setting.getBoolean("pref_listview_top", true)) {
            ((Home) getActivity()).getToolBer().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //これ一番上に移動するやつ
                    custom_streaming_listview.smoothScrollToPosition(0);
                }
            });
        }


        LinearLayout timelineLinearLayout = view.findViewById(R.id.custom_streaming_linearLayout);

        if (pref_setting.getBoolean("command_sushi",false)){
            //寿司を流す（開発中）
            MarqueeTextView sushi_TextView = new MarqueeTextView(getContext());
            //これで流れるように
            sushi_TextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            //レイアウト
            sushi_TextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            sushi_TextView.setMarqueeRepeatLimit(-1);
            //寿司設定
            sushi_TextView.setText("🍣　　🍣　🍣　　🍣　　🍣🍣🍣　　　🍣　　　🍣　　🍣🍣　🍣　　🍣　🍣　　🍣　　🍣🍣🍣　　　🍣　　　🍣　　🍣🍣　🍣　　🍣　🍣　　🍣　　🍣🍣🍣　　　🍣　　　🍣　　🍣🍣　🍣　　🍣　🍣　　🍣　　🍣🍣🍣　　　🍣　　　🍣　　🍣🍣");
            timelineLinearLayout.addView(sushi_TextView);
        }


        //カウンター機能！！！
        //レイアウト
        TextView countTextView = new TextView(getContext());
        if (pref_setting.getBoolean("pref_toot_count", false)) {
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
            countButton.setText(getString(R.string.toot_count_start));
            countEditText.setHint(getString(R.string.toot_count_hint));

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
                    String count_template = "　" + getString(R.string.toot_count_text) + " : ";
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


        MastodonClient client = new MastodonClient.Builder(Instance, new OkHttpClient.Builder(), new Gson())
                .accessToken(AccessToken)
                .useStreamingApi()
                .build();

        final ListItem[] listItem = new ListItem[1];

        ArrayList<ListItem> toot_list = new ArrayList<>();

        HomeTimeLineAdapter adapter = new HomeTimeLineAdapter(getContext(), R.layout.timeline_item, toot_list);

        boolean friends_nico_check_box = pref_setting.getBoolean("pref_friends_nico_mode", false);

        custom_streaming_setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = {getString(R.string.public_time_line), getString(R.string.home), getString(R.string.notifications)};
                final ArrayList<Integer> checkedItems = new ArrayList<Integer>();
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.CustomStreaming_Choose))
                        .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (which == 0 && isChecked) {
                                    //Toast.makeText(getContext(), "1", Toast.LENGTH_SHORT).show();
                                    local_timeline_boolean = true;
                                } else if (which == 0 && isChecked == false) {
                                    //Toast.makeText(getContext(), "-1", Toast.LENGTH_SHORT).show();
                                    local_timeline_boolean = false;
                                } else if (which == 1 && isChecked) {
                                    //Toast.makeText(getContext(), "2", Toast.LENGTH_SHORT).show();
                                    home_timeline_boolean = true;
                                } else if (which == 1 && isChecked == false) {
                                    //Toast.makeText(getContext(), "-2", Toast.LENGTH_SHORT).show();
                                    home_timeline_boolean = false;
                                } else if (which == 2 && isChecked) {
                                    //Toast.makeText(getContext(), "3", Toast.LENGTH_SHORT).show();
                                    notification_timeline_boolean = true;
                                } else if (which == 2 && isChecked == false) {
                                    //Toast.makeText(getContext(), "-3", Toast.LENGTH_SHORT).show();
                                    notification_timeline_boolean = false;
                                }

                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //if (which == 1) {
                                //OKを押したとき
                                Toast.makeText(getContext(), "ストリーミングへ接続します", Toast.LENGTH_SHORT).show();

                                //ローカルタイムライン
                                if (local_timeline_boolean) {
                                    AsyncTask local_asyncTask = new AsyncTask<String, Void, String>() {
                                        @Override
                                        protected String doInBackground(String... string) {
                                            com.sys1yagi.mastodon4j.api.Handler handler = new com.sys1yagi.mastodon4j.api.Handler() {
                                                @Override
                                                public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {
                                                    System.out.println(status.getContent());
                                                    toot_text = status.getContent();
                                                    user = status.getAccount().getUserName();
                                                    user_name = status.getAccount().getDisplayName();
                                                    user_use_client = status.getApplication().getName();
                                                    toot_id = status.getId();
                                                    toot_id_string = String.valueOf(toot_id);
                                                    //toot_time = status.getCreatedAt();
                                                    account_id = status.getAccount().getId();

                                                    //ユーザーのアバター取得
                                                    user_avater_url = status.getAccount().getAvatar();


                                                    final String[] media_url = {null};
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

                                                    //一番最初のIDを控える
                                                    if (max_id == null) {
                                                        max_id = toot_id_string;
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

                                                    //カスタム絵文字
                                                    if (pref_setting.getBoolean("pref_custom_emoji", true)) {
                                                        List<Emoji> emoji_List = status.getEmojis();
                                                        emoji_List.forEach(emoji -> {
                                                            String emoji_name = emoji.getShortcode();
                                                            String emoji_url = emoji.getUrl();
                                                            String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                            toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                            System.out.println("結果 : " + toot_text);
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

                                                    //Card
                                                    ArrayList<String> card = new ArrayList<>();
                                                    String cardTitle = null;
                                                    String cardURL = null;
                                                    String cardDescription = null;
                                                    String cardImage = null;

                                                    try {
                                                        Card statuses = new Statuses(client).getCard(toot_id).execute();
                                                        if (!statuses.getUrl().isEmpty()) {
                                                            cardTitle = statuses.getTitle();
                                                            cardURL = statuses.getUrl();
                                                            cardDescription = statuses.getDescription();
                                                            cardImage = statuses.getImage();

                                                            card.add(statuses.getTitle());
                                                            card.add(statuses.getUrl());
                                                            card.add(statuses.getDescription());
                                                            card.add(statuses.getImage());
                                                        }
                                                    } catch (Mastodon4jRequestException e) {
                                                        e.printStackTrace();
                                                    }

                                                    //ブースト　ふぁぼ
                                                    String isBoost = "no";
                                                    String isFav = "no";
                                                    String boostCount = "0";
                                                    String favCount = "0";
                                                    if (status.isReblogged()) {
                                                        isBoost = "reblogged";
                                                    }
                                                    if (status.isFavourited()) {
                                                        isFav = "favourited";
                                                    }
                                                    //かうんと
                                                    boostCount = String.valueOf(status.getReblogsCount());
                                                    favCount = String.valueOf(status.getFavouritesCount());

                                                    if (getActivity() != null && isAdded()) {
                                                        //配列を作成
                                                        ArrayList<String> Item = new ArrayList<>();
                                                        //メモとか通知とかに
                                                        Item.add("custom_local");
                                                        //内容
                                                        Item.add(toot_text);
                                                        //ユーザー名
                                                        Item.add(user_name + " @" + user);
                                                        //時間、クライアント名等
                                                        Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time);
                                                        //Toot ID 文字列版
                                                        Item.add(toot_id_string);
                                                        //アバターURL
                                                        Item.add(user_avater_url);
                                                        //アカウントID
                                                        Item.add(String.valueOf(account_id));
                                                        //ユーザーネーム
                                                        Item.add(user);
                                                        //メディア
                                                        Item.add(media_url_1);
                                                        Item.add(media_url_2);
                                                        Item.add(media_url_3);
                                                        Item.add(media_url_4);
                                                        //カード
                                                        Item.add(cardTitle);
                                                        Item.add(cardURL);
                                                        Item.add(cardDescription);
                                                        Item.add(cardImage);
                                                        //ブースト、ふぁぼしたか・ブーストカウント・ふぁぼかうんと
                                                        Item.add(isBoost);
                                                        Item.add(isFav);
                                                        Item.add(boostCount);
                                                        Item.add(favCount);

                                                        listItem[0] = new ListItem(Item);
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                //adapter.add(listItem);
                                                                adapter.insert(listItem[0], 0);

                                                                // 画面上で最上部に表示されているビューのポジションとTopを記録しておく

                                                                int pos = custom_streaming_listview.getFirstVisiblePosition();
                                                                int top = 0;
                                                                if (custom_streaming_listview.getChildCount() > 0) {
                                                                    top = custom_streaming_listview.getChildAt(0).getTop();
                                                                }
                                                                adapter.notifyDataSetChanged();
                                                                custom_streaming_listview.setAdapter(adapter);
                                                                //System.out.println("TOP == " + top);
                                                                // 要素追加前の状態になるようセットする
                                                                //一番上なら追いかける
                                                                if (pos == 0) {
                                                                    custom_streaming_listview.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            custom_streaming_listview.smoothScrollToPosition(0);
                                                                            //listView.setSelectionFromTop(index, top_);
                                                                        }
                                                                    });
                                                                    //System.out.println("ねてた");
                                                                } else {
                                                                    custom_streaming_listview.setSelectionFromTop(pos + 1, top);
                                                                }

                                                                int finalTop = top;
                                                                custom_streaming_listview.setOnScrollListener(new AbsListView.OnScrollListener() {
                                                                    @Override
                                                                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                                                                    }

                                                                    @Override
                                                                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                                                                        int totalItem = totalItemCount;

                                                                    }

                                                                });

                                                                //カウンター
                                                                if (count_text != null && pref_setting.getBoolean("pref_toot_count", false)) {
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
                                                public void onNotification(@NotNull Notification notification) {

                                                }

                                                @Override
                                                public void onDelete(long l) {

                                                }
                                            };

                                            Streaming streaming_local = new Streaming(client);
                                            try {
                                                local_shutdownable = streaming_local.localPublic(handler);
                                            } catch (Mastodon4jRequestException e) {
                                                e.printStackTrace();
                                            }


                                            return null;
                                        }
                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                                }

                                //ホームと通知
                                //どちらかがあれば
                                if (home_timeline_boolean || notification_timeline_boolean) {
                                    AsyncTask local_asyncTask = new AsyncTask<String, Void, String>() {
                                        @Override
                                        protected String doInBackground(String... string) {
                                            com.sys1yagi.mastodon4j.api.Handler handler = new com.sys1yagi.mastodon4j.api.Handler() {
                                                @Override
                                                public void onStatus(@NotNull com.sys1yagi.mastodon4j.api.entity.Status status) {
                                                    if (home_timeline_boolean) {
                                                        System.out.println(status.getContent());
                                                        toot_text = status.getContent();
                                                        user = status.getAccount().getAcct();
                                                        user_name = status.getAccount().getDisplayName();
                                                        toot_id = status.getId();
                                                        toot_id_string = String.valueOf(toot_id);
                                                        //toot_time = status.getCreatedAt();
                                                        account_id = status.getAccount().getId();

                                                        //ユーザーのアバター取得
                                                        user_avater_url = status.getAccount().getAvatar();

                                                        //ホームの場合はクライアント名が取れない時がある
                                                        //理由は多分同じインスタンスのユーザー以外はNullになる
                                                        try {
                                                            user_use_client = status.getApplication().getName();
                                                        } catch (NullPointerException e) {
                                                            user_use_client = null;
                                                        }


                                                        final String[] media_url = {null};
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


                                                        //カスタム絵文字
                                                        if (pref_setting.getBoolean("pref_custom_emoji", true)) {
                                                            List<Emoji> emoji_List = status.getEmojis();
                                                            emoji_List.forEach(emoji -> {
                                                                String emoji_name = emoji.getShortcode();
                                                                String emoji_url = emoji.getUrl();
                                                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                                toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                                System.out.println("結果 : " + toot_text);
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


                                                        //一番最初のIDを控える
                                                        if (max_id == null) {
                                                            max_id = toot_id_string;
                                                        }
                                                        // System.out.println("IDだよ : " + max_id);


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


                                                        //Card
                                                        ArrayList<String> card = new ArrayList<>();
                                                        String cardTitle = null;
                                                        String cardURL = null;
                                                        String cardDescription = null;
                                                        String cardImage = null;

                                                        try {
                                                            Card statuses = new Statuses(client).getCard(toot_id).execute();
                                                            if (!statuses.getUrl().isEmpty()) {
                                                                cardTitle = statuses.getTitle();
                                                                cardURL = statuses.getUrl();
                                                                cardDescription = statuses.getDescription();
                                                                cardImage = statuses.getImage();

                                                                card.add(statuses.getTitle());
                                                                card.add(statuses.getUrl());
                                                                card.add(statuses.getDescription());
                                                                card.add(statuses.getImage());
                                                            }
                                                        } catch (Mastodon4jRequestException e) {
                                                            e.printStackTrace();
                                                        }

                                                        //ブースト　ふぁぼ
                                                        String isBoost = "no";
                                                        String isFav = "no";
                                                        String boostCount = "0";
                                                        String favCount = "0";
                                                        if (status.isReblogged()) {
                                                            isBoost = "reblogged";
                                                        }
                                                        if (status.isFavourited()) {
                                                            isFav = "favourited";
                                                        }
                                                        //かうんと
                                                        boostCount = String.valueOf(status.getReblogsCount());
                                                        favCount = String.valueOf(status.getFavouritesCount());

                                                        if (getActivity() != null && isAdded()) {

                                                            //配列を作成
                                                            ArrayList<String> Item = new ArrayList<>();
                                                            //メモとか通知とかに
                                                            Item.add("custom_home");
                                                            //内容
                                                            Item.add(toot_text);
                                                            //ユーザー名
                                                            Item.add(user_name + " @" + user);
                                                            //時間、クライアント名等
                                                            Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time);
                                                            //Toot ID 文字列版
                                                            Item.add(toot_id_string);
                                                            //アバターURL
                                                            Item.add(user_avater_url);
                                                            //アカウントID
                                                            Item.add(String.valueOf(account_id));
                                                            //ユーザーネーム
                                                            Item.add(user);
                                                            //メディア
                                                            Item.add(media_url_1);
                                                            Item.add(media_url_2);
                                                            Item.add(media_url_3);
                                                            Item.add(media_url_4);
                                                            //カード
                                                            Item.add(cardTitle);
                                                            Item.add(cardURL);
                                                            Item.add(cardDescription);
                                                            Item.add(cardImage);
                                                            //ブースト、ふぁぼしたか・ブーストカウント・ふぁぼかうんと
                                                            Item.add(isBoost);
                                                            Item.add(isFav);
                                                            Item.add(boostCount);
                                                            Item.add(favCount);

                                                            ListItem listItem = null;
                                                            //自分の住んでるインスタンス以外のトゥートを表示するためのいｆ
                                                            listItem = new ListItem(Item);
                                                            ListItem finalListItem = listItem;
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (user.contains("@")) {
                                                                        //adapter.add(listItem);
                                                                        adapter.insert(finalListItem, 0);
                                                                        custom_streaming_listview.setAdapter(adapter);
                                                                        // 画面上で最上部に表示されているビューのポジションとTopを記録しておく

                                                                        int pos = custom_streaming_listview.getFirstVisiblePosition();
                                                                        int top = 0;
                                                                        if (custom_streaming_listview.getChildCount() > 0) {
                                                                            top = custom_streaming_listview.getChildAt(0).getTop();
                                                                        }
                                                                        adapter.notifyDataSetChanged();
                                                                        custom_streaming_listview.setAdapter(adapter);
                                                                        //System.out.println("TOP == " + top);
                                                                        // 要素追加前の状態になるようセットする
                                                                        // 要素追加前の状態になるようセットする
                                                                        //一番上なら追いかける
                                                                        if (pos == 0) {
                                                                            custom_streaming_listview.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    custom_streaming_listview.smoothScrollToPosition(0);
                                                                                    //listView.setSelectionFromTop(index, top_);
                                                                                }
                                                                            });
                                                                            //System.out.println("ねてた");
                                                                        } else {
                                                                            custom_streaming_listview.setSelectionFromTop(pos + 1, top);
                                                                        }

                                                                        //カウンター
                                                                        if (count_text != null && pref_setting.getBoolean("pref_toot_count", false)) {
                                                                            //含んでいるか
                                                                            if (toot_text.contains(count_text)) {
                                                                                String count_template = "　を含んだトゥート数 : ";
                                                                                akeome_count++;
                                                                                countTextView.setText(count_text + count_template + String.valueOf(akeome_count));
                                                                            }
                                                                        }

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onNotification(@NotNull Notification notification) {
                                                    if (notification_timeline_boolean) {
                                                        user_name = notification.getAccount().getDisplayName();
                                                        type = notification.getType();
                                                        //time = notification.getCreatedAt();
                                                        user_avater_url = notification.getAccount().getAvatar();
                                                        user = notification.getAccount().getAcct();

                                                        account_id = notification.getAccount().getId();

                                                        String toot_id_string = null;

                                                        //Followの通知のときにgetContent()するとNullでえらーでるのでtry/catch処理
                                                        try {
                                                            toot_text = notification.getStatus().getContent();
                                                            toot_id = notification.getStatus().getId();
                                                            toot_id_string = String.valueOf(toot_id);
                                                        } catch (NullPointerException e) {
                                                            toot_text = "";
                                                            toot_id = 0;
                                                            toot_id_string = String.valueOf(toot_id);
                                                        }

                                                        boolean japan_timeSetting = pref_setting.getBoolean("pref_custom_time_format", false);
                                                        if (japan_timeSetting) {
                                                            //時差計算？
                                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                                            //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                                                            //日本用フォーマット
                                                            SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
                                                            try {
                                                                Date date = simpleDateFormat.parse(notification.getCreatedAt());
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
                                                            toot_time = notification.getCreatedAt();
                                                        }

                                                        Locale locale = Locale.getDefault();
                                                        if (type.equals("mention") && locale.equals(Locale.JAPAN)) {
                                                            type = "返信しました";
                                                        }
                                                        if (type.equals("reblog") && locale.equals(Locale.JAPAN)) {
                                                            type = "ブーストしました";
                                                        }
                                                        if (type.equals("favourite") && locale.equals(Locale.JAPAN)) {
                                                            if (friends_nico_check_box) {
                                                                type = "お気に入りしました";
                                                            } else {
                                                                type = "二コりました";
                                                            }
                                                        }
                                                        if (type.equals("follow") && locale.equals(Locale.JAPAN)) {
                                                            type = "フォローしました";
                                                        }

                                                        //カスタム絵文字
                                                        if (pref_setting.getBoolean("pref_custom_emoji", true)) {
                                                            List<Emoji> emoji_List = notification.getStatus().getEmojis();
                                                            emoji_List.forEach(emoji -> {
                                                                String emoji_name = emoji.getShortcode();
                                                                System.out.println("結果 : " + emoji_name);
                                                                String emoji_url = emoji.getUrl();
                                                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                                toot_text = toot_text.replace(":" + emoji_name + ":", custom_emoji_src);
                                                                System.out.println("結果 : " + toot_text);
                                                            });

                                                            //DisplayNameカスタム絵文字
                                                            List<Emoji> account_emoji_List = notification.getAccount().getEmojis();
                                                            account_emoji_List.forEach(emoji -> {
                                                                String emoji_name = emoji.getShortcode();
                                                                String emoji_url = emoji.getUrl();
                                                                String custom_emoji_src = "<img src=\'" + emoji_url + "\'>";
                                                                user_name = user_name.replace(":" + emoji_name + ":", custom_emoji_src);
                                                            });
                                                        }
                                                        //Card
                                                        ArrayList<String> card = new ArrayList<>();
                                                        String cardTitle = null;
                                                        String cardURL = null;
                                                        String cardDescription = null;
                                                        String cardImage = null;

                                                        try {
                                                            Card statuses = new Statuses(client).getCard(toot_id).execute();
                                                            if (!statuses.getUrl().isEmpty()) {
                                                                cardTitle = statuses.getTitle();
                                                                cardURL = statuses.getUrl();
                                                                cardDescription = statuses.getDescription();
                                                                cardImage = statuses.getImage();

                                                                card.add(statuses.getTitle());
                                                                card.add(statuses.getUrl());
                                                                card.add(statuses.getDescription());
                                                                card.add(statuses.getImage());
                                                            }
                                                        } catch (Mastodon4jRequestException e) {
                                                            e.printStackTrace();
                                                        }
                                                        //ブースト　ふぁぼ
                                                        String isBoost = "no";
                                                        String isFav = "no";
                                                        String boostCount = "0";
                                                        String favCount = "0";
                                                        if (notification.getStatus().isReblogged()) {
                                                            isBoost = "reblogged";
                                                        }
                                                        if (notification.getStatus().isFavourited()) {
                                                            isFav = "favourited";
                                                        }
                                                        //かうんと
                                                        boostCount = String.valueOf(notification.getStatus().getReblogsCount());
                                                        favCount = String.valueOf(notification.getStatus().getFavouritesCount());

                                                        if (getActivity() != null && isAdded()) {

                                                            //配列を作成
                                                            ArrayList<String> Item = new ArrayList<>();
                                                            //メモとか通知とかに
                                                            Item.add("custom_notification");
                                                            //内容
                                                            Item.add(toot_text);
                                                            //ユーザー名
                                                            Item.add(user_name + " @" + user);
                                                            //時間、クライアント名等
                                                            Item.add("クライアント : " + user_use_client + " / " + "トゥートID : " + toot_id_string + " / " + getString(R.string.time) + " : " + toot_time);
                                                            //Toot ID 文字列版
                                                            Item.add(toot_id_string);
                                                            //アバターURL
                                                            Item.add(user_avater_url);
                                                            //アカウントID
                                                            Item.add(String.valueOf(account_id));
                                                            //ユーザーネーム
                                                            Item.add(user);
                                                            //メディア
                                                            Item.add(media_url_1);
                                                            Item.add(media_url_2);
                                                            Item.add(media_url_3);
                                                            Item.add(media_url_4);
                                                            //カード
                                                            Item.add(cardTitle);
                                                            Item.add(cardURL);
                                                            Item.add(cardDescription);
                                                            Item.add(cardImage);
                                                            //ブースト、ふぁぼしたか・ブーストカウント・ふぁぼかうんと
                                                            Item.add(isBoost);
                                                            Item.add(isFav);
                                                            Item.add(boostCount);
                                                            Item.add(favCount);

                                                            ListItem listItem = new ListItem(Item);

                                                            //UI変更
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (getActivity() != null) {
                                                                        adapter.notifyDataSetChanged();
                                                                        adapter.insert(listItem, 0);
                                                                        custom_streaming_listview.setAdapter(adapter);
                                                                        // 画面上で最上部に表示されているビューのポジションとTopを記録しておく

                                                                        int pos = custom_streaming_listview.getFirstVisiblePosition();
                                                                        int top = 0;
                                                                        if (custom_streaming_listview.getChildCount() > 0) {
                                                                            top = custom_streaming_listview.getChildAt(0).getTop();
                                                                        }
                                                                        adapter.notifyDataSetChanged();
                                                                        custom_streaming_listview.setAdapter(adapter);
                                                                        //System.out.println("TOP == " + top);
                                                                        // 要素追加前の状態になるようセットする
                                                                        // 要素追加前の状態になるようセットする
                                                                        //一番上なら追いかける
                                                                        if (pos == 0) {
                                                                            custom_streaming_listview.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    custom_streaming_listview.smoothScrollToPosition(0);
                                                                                    //listView.setSelectionFromTop(index, top_);
                                                                                }
                                                                            });
                                                                            //System.out.println("ねてた");
                                                                        } else {
                                                                            custom_streaming_listview.setSelectionFromTop(pos + 1, top);
                                                                        }
                                                                    }
                                                                }

                                                            });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onDelete(long l) {

                                                }
                                            };

                                            Streaming streaming_local = new Streaming(client);
                                            try {
                                                local_shutdownable = streaming_local.user(handler);
                                            } catch (Mastodon4jRequestException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }
                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                            //}
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

/*
        custom_steaming_toot_edittext.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String toot_text_string = custom_steaming_toot_edittext.getText().toString();

                //ダイアログ出すかどうか
                boolean accessToken_boomelan = pref_setting.getBoolean("pref_toot_dialog", false);
                if (accessToken_boomelan) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setTitle(R.string.confirmation);
                    alertDialog.setMessage(R.string.toot_dialog);
                    alertDialog.setPositiveButton(R.string.toot, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //トゥートああああ
                            new AsyncTask<String, Void, String>() {
                                @Override
                                protected String doInBackground(String... params) {
                                    RequestBody requestBody = new FormBody.Builder()
                                            .add("status", toot_text_string)
                                            .build();
                                    System.out.println("=====" + client.post("statuses", requestBody));
                                    return toot_text_string;
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    Toast.makeText(getContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                                }
                            }.execute();
                            custom_steaming_toot_edittext.setText(""); //投稿した後に入力フォームを空にする
                        }
                    });
                    alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.create().show();

                } else {
                    //トゥートああああ
                    new AsyncTask<String, Void, String>() {
                        @Override
                        protected String doInBackground(String... params) {
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("status", toot_text_string)
                                    .build();
                            System.out.println("=====" + client.post("statuses", requestBody));
                            return toot_text_string;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            Toast.makeText(getContext(), "トゥートしました : " + result, Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                    custom_steaming_toot_edittext.setText(""); //投稿した後に入力フォームを空にする
                }

                return false;
            }
        });
*/

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (local_shutdownable != null) {
            local_shutdownable.shutdown();
        }
    }
}
