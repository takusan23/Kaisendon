package io.github.takusan23.kaisendon.CustomMenu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class MisskeyDriveBottomDialog extends BottomSheetDialogFragment {

    public static Button ok_Button;
    private ListView listView;
    private SharedPreferences pref_setting;
    private ArrayList<ListItem> toot_list;
    private SimpleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.misskey_drive_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        listView = view.findViewById(R.id.misskey_drive_listview);
        ok_Button = view.findViewById(R.id.misskey_drive_ok_button);
        toot_list = new ArrayList<>();
        adapter = new SimpleAdapter(getContext(), R.layout.timeline_item, toot_list);
        //取得
        getMisskeyDrive();
        //閉じる
        ok_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MisskeyDriveBottomDialog.this.dismiss();
            }
        });
    }

    /**
     * Misskey Drive 取得
     */
    private void getMisskeyDrive() {
        String instance = pref_setting.getString("misskey_main_instance", "");
        String token = pref_setting.getString("misskey_main_token", "");
        String username = pref_setting.getString("misskey_main_username", "");
        //URL
        String url = "https://" + instance + "/api/drive/files";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("limit", 100);
            jsonObject.put("i", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonObject.toString());
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        //作成
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
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
                System.out.println(response_string);
                if (!response.isSuccessful()) {
                    //失敗
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    setJSONParse(response_string);
                }
            }
        });
    }

    /**
     * Misskey Drive JSON Parse
     */
    private void setJSONParse(String response) {

        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String url = jsonObject.getString("url");
                //配列を作成
                ArrayList<String> Item = new ArrayList<>();
                //メモとか通知とかに
                Item.add("misskey_drive");
                //内容
                Item.add(url);
                //ユーザー名
                Item.add("");
                //時間、クライアント名等
                Item.add(null);
                //Toot ID 文字列版
                Item.add(id);
                //アバターURL
                Item.add(url);
                //アカウントID
                Item.add("");
                //ユーザーネーム
                Item.add("");
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
                        adapter.add(listItem);
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);
                    }
                });
                ///snackbar.dismiss();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
