package io.github.takusan23.kaisendon.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.kaisendon.ListItem;
import io.github.takusan23.kaisendon.R;
import io.github.takusan23.kaisendon.SimpleAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountListFragment extends Fragment {

    private ListView mastodon_listView;
    private ListView misskey_listView;
    private ArrayList<ListItem> mastodon_list;
    private ArrayList<ListItem> misskey_list;
    private SimpleAdapter mastodon_adapter;
    private SimpleAdapter misskey_adapter;
    private SharedPreferences pref_setting;
    private ArrayList<String> mastodon_instance_list;
    private ArrayList<String> mastodon_access_token_list;
    private ArrayList<String> misskey_instance_list;
    private ArrayList<String> misskey_access_token_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_account_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        mastodon_listView = view.findViewById(R.id.mastodon_account_list_listview);
        misskey_listView = view.findViewById(R.id.misskey_account_list_listview);
        mastodon_list = new ArrayList<>();
        misskey_list = new ArrayList<>();
        mastodon_adapter = new SimpleAdapter(getContext(), R.layout.timeline_item, mastodon_list);
        misskey_adapter = new SimpleAdapter(getContext(), R.layout.timeline_item, misskey_list);
        //タイトル
        getActivity().setTitle(R.string.account_list);
        //Mastodonアカウント
        if (pref_setting.getString("instance_list", "").length() >= 1) {
            loadMastodonAccount();
        }
        //Misskeyアカウント
        if (pref_setting.getString("misskey_instance_list", "").length() >= 1) {
            loadMisskeyAccount();
        }

    }

    /**
     * Mastodon アカウント読み込み
     */
    private void loadMastodonAccount() {
        ArrayList<String> multi_account_instance = new ArrayList<>();
        ArrayList<String> multi_account_access_token = new ArrayList<>();
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
                e.printStackTrace();
            }
        }
        for (int i = 0; i < multi_account_instance.size(); i++) {
            //Request
            //作成
            Request request = new Request.Builder()
                    .url("https://" + multi_account_instance.get(i) + "/api/v1/accounts/verify_credentials?access_token=" + multi_account_access_token.get(i))
                    .get()
                    .build();
            //GETリクエスト
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    //失敗
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String response_string = response.body().string();
                    if (!response.isSuccessful()) {
                        //失敗
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(response_string);
                            String note = jsonObject.getString("note");
                            String display_name = jsonObject.getString("display_name");
                            String acct = jsonObject.getString("acct");
                            String avatar = jsonObject.getString("avatar");
                            String account_id = jsonObject.getString("id");
                            //配列を作成
                            ArrayList<String> Item = new ArrayList<>();
                            //メモとか通知とかに
                            Item.add("account_list");
                            //内容
                            Item.add(note);
                            //ユーザー名
                            Item.add(display_name + " @" + acct);
                            //時間、クライアント名等
                            Item.add(null);
                            //Toot ID 文字列版
                            Item.add(null);
                            //アバターURL
                            Item.add(avatar);
                            //アカウントID
                            Item.add(account_id);
                            //ユーザーネーム
                            Item.add(display_name);
                            //メディア
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            //カード
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            ListItem listItem = new ListItem(Item);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mastodon_adapter.add(listItem);
                                    mastodon_adapter.notifyDataSetChanged();
                                    mastodon_listView.setAdapter(mastodon_adapter);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        //長押しで消せるように
        mastodon_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //確認
                Snackbar.make(view, getString(R.string.account_delete_message), Snackbar.LENGTH_SHORT).setAction(getString(R.string.delete_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //削除
                        //配列から要素を消す
                        multi_account_instance.remove(position);
                        multi_account_access_token.remove(position);
                        //JSONArray
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
                        Toast.makeText(getContext(), getString(R.string.delete), Toast.LENGTH_SHORT).show();
                        //再読み込み
                        mastodon_adapter.clear();
                        loadMastodonAccount();
                    }
                }).show();
                return false;
            }
        });
    }

    /**
     * Misskey アカウント読み込み
     */
    private void loadMisskeyAccount() {
        ArrayList<String> multi_account_instance = new ArrayList<>();
        ArrayList<String> multi_account_access_token = new ArrayList<>();
        String instance_instance_string = pref_setting.getString("misskey_instance_list", "");
        String account_instance_string = pref_setting.getString("misskey_access_list", "");
        if (!instance_instance_string.equals("")) {
            try {
                JSONArray instance_array = new JSONArray(instance_instance_string);
                JSONArray access_array = new JSONArray(account_instance_string);
                for (int i = 0; i < instance_array.length(); i++) {
                    multi_account_access_token.add(access_array.getString(i));
                    multi_account_instance.add(instance_array.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //System.out.println(multi_account_access_token);
        for (int i = 0; i < multi_account_instance.size(); i++) {
            //作成
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("i", multi_account_access_token.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            Request request = new Request.Builder()
                    .url("https://" + multi_account_instance.get(i) + "/api/i")
                    .post(requestBody)
                    .build();
            //GETリクエスト
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    //失敗
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String response_string = response.body().string();
                    //System.out.println(response_string);
                    if (!response.isSuccessful()) {
                        //失敗
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(response_string);
                            String note = jsonObject.getString("description");
                            String display_name = jsonObject.getString("name");
                            String acct = jsonObject.getString("username");
                            String avatar = jsonObject.getString("avatarUrl");
                            String account_id = jsonObject.getString("id");
                            //配列を作成
                            ArrayList<String> Item = new ArrayList<>();
                            //メモとか通知とかに
                            Item.add("account_list");
                            //内容
                            Item.add(note);
                            //ユーザー名
                            Item.add(display_name + " @" + acct);
                            //時間、クライアント名等
                            Item.add(null);
                            //Toot ID 文字列版
                            Item.add(null);
                            //アバターURL
                            Item.add(avatar);
                            //アカウントID
                            Item.add(account_id);
                            //ユーザーネーム
                            Item.add(display_name);
                            //メディア
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            //カード
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            Item.add(null);
                            ListItem listItem = new ListItem(Item);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    misskey_adapter.add(listItem);
                                    misskey_adapter.notifyDataSetChanged();
                                    misskey_listView.setAdapter(misskey_adapter);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        //長押しで消せるように
        misskey_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //確認
                Snackbar.make(view, getString(R.string.account_delete_message), Snackbar.LENGTH_SHORT).setAction(getString(R.string.delete_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //削除
                        //配列から要素を消す
                        multi_account_instance.remove(position);
                        multi_account_access_token.remove(position);
                        //JSONArray
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
                        editor.putString("misskey_instance_list", instance_array.toString());
                        editor.putString("misskeyaccess_list", access_array.toString());
                        editor.apply();
                        Toast.makeText(getContext(), "削除しました", Toast.LENGTH_SHORT).show();
                        //再読み込み
                        misskey_adapter.clear();
                        loadMisskeyAccount();
                    }
                }).show();
                return false;
            }
        });
    }

}
