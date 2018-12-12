package com.takusan_23.kaisendon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;

import org.json.JSONArray;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class MultiAccountList_Fragment extends Fragment {

    //アクセストークン、インスタンス
    String AccessToken = null;
    String Instance = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.multi_accountlist_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        SharedPreferences pref_setting = PreferenceManager.getDefaultSharedPreferences(Preference_ApplicationContext.getContext());

        boolean accessToken_boomelan = pref_setting.getBoolean("pref_advanced_setting_instance_change", false);
        if (accessToken_boomelan) {

            AccessToken = pref_setting.getString("pref_mastodon_accesstoken", "");
            Instance = pref_setting.getString("pref_mastodon_instance", "");

        } else {

            AccessToken = pref_setting.getString("main_token", "");
            Instance = pref_setting.getString("main_instance", "");

        }

        //読み込み中
        Snackbar snackbar = Snackbar.make(view, "アカウント読み込み中 \r\n /api/v1/accounts/verify_credentials", Snackbar.LENGTH_INDEFINITE);
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


        ListView account_listView = view.findViewById(R.id.accountlist_listview);

        //タイトル
        getActivity().setTitle(R.string.account_chenge);


        //メインアカウント
        ArrayList<ListItem> toot_list = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(getContext(), R.layout.timeline_item, toot_list);

        String main_access_token = pref_setting.getString("main_token", "");
        String main_instance = pref_setting.getString("main_instance", "");


        //マルチアカウント
        //配列を使えば幸せになれそう！！！
        ArrayList<String> multi_account_instance = new ArrayList<>();
        ArrayList<String> multi_account_access_token = new ArrayList<>();

        //とりあえずPreferenceに書き込まれた値を
        String instance_instance_string = pref_setting.getString("instance_list", "");
        String account_instance_string = pref_setting.getString("access_list", "");
        if (!instance_instance_string.equals("")) {
            try {
                JSONArray instance_array = new JSONArray(instance_instance_string);
                JSONArray access_array = new JSONArray(account_instance_string);
                for (int i = 0; i < instance_array.length(); i++) {
                    multi_account_access_token.add(access_array.getString(i));
                    multi_account_instance.add(instance_array.getString(i));
                }
            } catch (Exception e) {

            }
        }

        for (int count = 0; count < multi_account_instance.size(); count ++){
            String multi_instance = multi_account_instance.get(count);
            String multi_access_token = multi_account_access_token.get(count);
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... string) {
                    MastodonClient client = new MastodonClient.Builder(multi_instance, new OkHttpClient.Builder(), new Gson())
                            .accessToken(multi_access_token)
                            .build();

                    try {
                        Account main_accounts = new Accounts(client).getVerifyCredentials().execute();

                        long account_id = main_accounts.getId();
                        String display_name = main_accounts.getDisplayName();
                        String account_id_string = main_accounts.getUserName();
                        String profile = main_accounts.getNote();
                        String avater_url = main_accounts.getAvatar();

                        String now_account = null;
                        if (multi_access_token.equals(main_access_token)){
                            now_account = "now_account";
                        }

                        ListItem listItem = new ListItem(now_account, profile, display_name + " @" + account_id_string, null, null, avater_url, account_id, display_name, null,null,null,null);

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
                                ListView listView = (ListView) view.findViewById(R.id.accountlist_listview);
                                listView.setAdapter(adapter);
                            }
                        });

                    } catch (Mastodon4jRequestException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(String result) {
                    snackbar.dismiss();
                }

            }.execute();

        }


        //アカウント切り替え
        ListView listView = (ListView) view.findViewById(R.id.accountlist_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String multi_instance = multi_account_instance.get(position);
                String multi_access_token = multi_account_access_token.get(position);

                SharedPreferences.Editor editor = pref_setting.edit();
                editor.putString("main_instance", multi_instance);
                editor.putString("main_token", multi_access_token);
                editor.apply();

                //アプリ再起動
                Intent intent = new Intent(getContext(), Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //長押しで消せるように
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.account_delete_title)
                        .setMessage(R.string.account_delete_message)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //配列から要素を消す
                                multi_account_instance.remove(position);
                                multi_account_access_token.remove(position);

                                //Preferenceも上書きする
                                //Preferenceに配列は保存できないのでJSON化して保存する
                                //Write
                                JSONArray instance_array = new JSONArray();
                                JSONArray access_array = new JSONArray();
                                for (int i = 0; i < multi_account_instance.size(); i++) {
                                    instance_array.put(multi_account_instance.get(i));
                                }
                                for (int i = 0; i < multi_account_access_token.size(); i++) {
                                    access_array.put(multi_account_access_token.get(i));
                                }

                                //書き込む
                                SharedPreferences.Editor editor = pref_setting.edit();
                                editor.putString("instance_list", instance_array.toString());
                                editor.putString("access_list", access_array.toString());
                                editor.apply();
                                Toast.makeText(getContext(),"削除しました", Toast.LENGTH_SHORT).show();


                                //アプリ再起動
                                Intent intent = new Intent(getContext(), Home.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
/*
                int account_count = position + 1;
                //Toast.makeText(getContext(), "消す : " + String.valueOf(account_count), Toast.LENGTH_SHORT).show();
                //カウントが最大ではないとき（空間が開かないようにする）
*/
/*
                if (account_count < pref_setting.getInt("account_count", 2)) {
                    int add_count = 0;

                    //while (account_count < pref_setting.getInt("account_count", 2)){
                        SharedPreferences.Editor editor = pref_setting.edit();
                        editor.putString("token" + String.valueOf(account_count + add_count), pref_setting.getString("token" + String.valueOf(account_count++ + add_count), ""));
                        editor.putString("instance" + String.valueOf(account_count + add_count), pref_setting.getString("instance" + String.valueOf(account_count++ + add_count), ""));
                        editor.apply();
                        //add_count = add_count + 1;
                    //}
                    //SharedPreferences.Editor editor = pref_setting.edit();
                    editor.remove("token" + String.valueOf(pref_setting.getInt("account_count", 2)));
                    editor.remove("instance" + String.valueOf(pref_setting.getInt("account_count", 2)));
                    editor.apply();

                    Toast.makeText(getContext(), "最大以外", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = pref_setting.edit();
                    editor.remove("token" + String.valueOf(pref_setting.getInt("account_count", 2)));
                    editor.remove("instance" + String.valueOf(pref_setting.getInt("account_count", 2)));
                    editor.apply();

                }
*//*



                SharedPreferences.Editor editor = pref_setting.edit();
                editor.remove("token" + String.valueOf(account_count));
                editor.remove("instance" + String.valueOf(account_count));
                editor.apply();
*/


                return false;
            }
        });


    }
}

